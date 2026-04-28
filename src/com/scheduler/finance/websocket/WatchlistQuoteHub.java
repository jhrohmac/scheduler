package com.scheduler.finance.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.finance.kis.config.KisConfigLoader;
import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_api.api.rest.quotations.PriceApi;
import com.scheduler.kis_api.api.rest.quotations.PriceResult;
import com.scheduler.kis_client.KisClient;
import com.scheduler.kis_client.util.JsonUtil;

/**
 * 관심종목 현재가(국내+해외) 폴링 허브
 *
 * 역할:
 * - 브라우저 WS 세션이 요청한 stock token 목록을 관리
 * - kis.properties의 'kis.watchlist.quote.period.ms' 값에 따라 주기적으로 token별 현재가 조회
 * - 조회한 현재가를 해당 token을 구독한 모든 세션에 push (비동기)
 *
 * 설정 (kis.properties):
 * - kis.watchlist.quote.period.ms: 폴링 주기(밀리초) / 최소 500ms, 기본값 5000ms(5초)
 *   예시: 500(0.5초), 1000(1초), 2000(2초), 5000(5초)
 * - kis.watchlist.quote.period.sec: 레거시 설정(초 단위, 하위호환용)
 * - kis.watchlist.domestic.realtime.enabled: 국내주식 실시간 WebSocket 활성화 여부
 *
 * Token 형식:
 * - 국내: "005930" 또는 "KR|005930" 또는 "KR|KRX|005930"
 * - 해외: "US|nasdaq|AAPL" (country|market|code)
 *
 * 최적화:
 * - 배치 처리: 대량 관심종목을 MAX_TOKENS_PER_CYCLE(6개)씩 나누어 조회하여 timeout 방지
 * - 폴링 타임아웃: FETCH_TIMEOUT_MS(3.5초)로 API 응답 지연 감지 및 자동 취소
 * - 변화 감지: 가격 변화 없으면 중복 전송 생략 (대역폭 절약)
 * - 밀리초 정밀도: 0.5초부터 수 초대까지 세밀한 폴링 주기 조정 가능
 */
public class WatchlistQuoteHub {

	private static final Logger logger = LoggerFactory.getLogger(WatchlistQuoteHub.class);

	private static final WatchlistQuoteHub INSTANCE = new WatchlistQuoteHub();

	public static WatchlistQuoteHub getInstance() {
		return INSTANCE;
	}

	// kis.properties의 'kis.watchlist.quote.period.ms' 값으로 초기화 (밀리초 단위)
	// 사용자가 kis.properties 파일을 수정하여 폴링 주기를 변경할 수 있음 (예: 500, 1000, 2000)
	// kis.watchlist.quote.period.sec 설정이 있으면 자동으로 밀리초로 변환
	private static final int PERIOD_MS = resolvePeriodMs();

	// kis.properties의 'kis.watchlist.domestic.realtime.enabled' 값으로 초기화
	// true면 국내주식은 실시간 WebSocket으로, false면 REST API 폴링으로 조회
    private static final boolean USE_DOMESTIC_REALTIME = resolveDomesticRealtimeEnabled();

	// KIS REST 조회가 멈추면(네트워크/Hang) watchlist 전체 갱신이 중단되는 문제가 발생할 수 있어
	// Future.get()에 타임아웃을 걸어 허브 자체가 멈추지 않도록 방어한다.
	private static final long FETCH_TIMEOUT_MS = 3500L;

	private static final ScheduledExecutorService SCHED = Executors.newScheduledThreadPool(1, r -> {
		Thread t = new Thread(r);
		t.setName("watchlist-quote-hub");
		t.setDaemon(true);
		return t;
	});

    private static final int FETCH_POOL_SIZE = Math.max(6, Runtime.getRuntime().availableProcessors() * 2);
    private static final int MAX_TOKENS_PER_CYCLE = Math.max(6, FETCH_POOL_SIZE);

	private static final ExecutorService FETCH_POOL = Executors.newFixedThreadPool(FETCH_POOL_SIZE, r -> {
		Thread t = new Thread(r);
		t.setName("watchlist-quote-fetch");
		t.setDaemon(true);
		return t;
	});

	private volatile KisClient client;
	private volatile String lastInitError;
    private final WatchlistDomesticHub domesticHub = WatchlistDomesticHub.getInstance();

	private static class Sub {
		final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
		volatile String lastKey;
		volatile long lastSendAt;
        volatile QuotePayload lastPayload;
	}
    
    private static class PollStats {
        int totalTokens;
        int batchSize;
        int sent;
        int noData;
        int skippedNoSub;
        int skippedNoChange;
        int timeouts;
        int errors;
        int emptyPayload;
        long startedAt;
    }

	private final ConcurrentMap<String, Sub> subs = new ConcurrentHashMap<String, Sub>();
	private final ConcurrentMap<Session, Set<String>> sessionTokens = new ConcurrentHashMap<Session, Set<String>>();

	private volatile ScheduledFuture<?> task;
    private final AtomicInteger cursor = new AtomicInteger(0);

    private WatchlistQuoteHub() {
    }

    /**
     * kis.properties에서 관심종목 폴링 주기를 읽고 검증 (밀리초 단위)
     *
     * 설정 키: kis.watchlist.quote.period.ms (선호) 또는 kis.watchlist.quote.period.sec (하위호환)
     * - kis.watchlist.quote.period.ms가 있으면 그 값을 사용
     * - 없으면 kis.watchlist.quote.period.sec * 1000으로 변환
     *
     * 값 검증:
     * - 0 이하 또는 파일 없음 → 기본값 5000ms(5초) 반환
     * - 500ms 미만 → 500ms로 조정 (API 제한 고려)
     * - 500ms 이상 → 설정값 그대로 반환
     *
     * @return 폴링 주기 (밀리초 단위)
     */
    private static int resolvePeriodMs() {
        try {
            int v = KisConfigLoader.getKisProperties().getWatchlistQuotePeriodMs();
            if (v <= 0) {
                return 5000;
            }
            if (v < 500) {
                logger.warn("[WL-QUOTE] watchlist quote period too low ({}ms). Clamping to 500ms.", Integer.valueOf(v));
                return 500;
            }
            return v;
        } catch (Exception e) {
            return 5000;
        }
    }

    private static boolean resolveDomesticRealtimeEnabled() {
        try {
            return KisConfigLoader.getKisProperties().isWatchlistDomesticRealtimeEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void shutdown() {
        try {
            synchronized (this) {
                if (task != null) {
                    try {
                        task.cancel(true);
                    } catch (Exception ignore) {
                    }
                    task = null;
                }
            }
            subs.clear();
            sessionTokens.clear();
        } catch (Exception ignore) {
        }

        shutdownExecutor(SCHED, "watchlist-quote-hub");
        shutdownExecutor(FETCH_POOL, "watchlist-quote-fetch");

        client = null;
        lastInitError = null;
    }

    private static void shutdownExecutor(ExecutorService executor, String name) {
        if (executor == null || executor.isShutdown()) {
            return;
        }
        try {
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("[WL-QUOTE] executor shutdown error name={}", name, e);
        }
    }

	public void subscribe(Session session, Set<String> tokens) {
		if (session == null || tokens == null || tokens.isEmpty()) {
			return;
		}

        Set<String> domesticCodes = new HashSet<String>();
        Set<String> restTokens = new HashSet<String>();

        for (String t : tokens) {
            String token = normalizeToken(t);
            if (token == null) {
                continue;
            }
            TokenParts p = parseToken(token);
            if (p == null) {
                continue;
            }
            if (USE_DOMESTIC_REALTIME && p.isDomestic) {
                if (p.code != null && !p.code.trim().isEmpty()) {
                    domesticCodes.add(p.code.trim());
                }
            } else {
                restTokens.add(token);
            }
        }

        Set<String> domesticFailed = Collections.emptySet();
        if (USE_DOMESTIC_REALTIME && !domesticCodes.isEmpty()) {
            try {
                domesticFailed = domesticHub.subscribe(session, domesticCodes);
            } catch (Exception e) {
                logger.warn("[WL-QUOTE] domestic subscribe failed session={} count={}", safeId(session),
                        Integer.valueOf(domesticCodes.size()), e);
                domesticFailed = domesticCodes;
            }
        }

        if (USE_DOMESTIC_REALTIME && domesticFailed != null && !domesticFailed.isEmpty()) {
            for (String code : domesticFailed) {
                if (code != null && !code.trim().isEmpty()) {
                    restTokens.add("KR|KRX|" + code.trim());
                }
            }
            logger.warn("[WL-QUOTE] domestic realtime failed. fallback to REST count={}", Integer.valueOf(domesticFailed.size()));
        }

        if (!restTokens.isEmpty()) {
            if (!ensureClient(session)) {
                return;
            }

            Set<String> mine = sessionTokens.computeIfAbsent(session,
                    k -> Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));

            for (String token : restTokens) {
                mine.add(token);
                Sub sub = subs.computeIfAbsent(token, k -> new Sub());
                sub.sessions.add(session);
                if (sub.lastPayload != null && hasQuoteData(sub.lastPayload)) {
                    try {
                        if (session.isOpen()) {
                            session.getAsyncRemote().sendText(JsonUtil.toJson(sub.lastPayload));
                        }
                    } catch (Exception ignore) {
                    }
                }
            }

            startTaskIfNeeded();
        }

        if (logger.isInfoEnabled()) {
            logger.info("[WL-QUOTE] subscribe session={} total={} domestic={} overseas={}",
                    safeId(session),
                    Integer.valueOf(tokens.size()),
                    Integer.valueOf(domesticCodes.size()),
                    Integer.valueOf(restTokens.size()));
        }
	}

	public void unsubscribe(Session session) {
		if (session == null) {
			return;
		}

        if (USE_DOMESTIC_REALTIME) {
            try {
                domesticHub.unsubscribe(session);
            } catch (Exception e) {
                logger.warn("[WL-QUOTE] domestic unsubscribe failed session={}", safeId(session), e);
            }
        }

		Set<String> mine = sessionTokens.remove(session);
		if (mine == null || mine.isEmpty()) {
			cleanupClosedSessions();
			stopTaskIfIdle();
			return;
		}

		for (String token : new HashSet<String>(mine)) {
			Sub sub = subs.get(token);
			if (sub == null) {
				continue;
			}
			sub.sessions.remove(session);
			if (sub.sessions.isEmpty()) {
				subs.remove(token);
			}
		}

		stopTaskIfIdle();
	}

	private void startTaskIfNeeded() {
		if (task != null && !task.isCancelled()) {
			return;
		}
		synchronized (this) {
			if (task != null && !task.isCancelled()) {
				return;
			}
            logger.info("[WL-QUOTE] polling start periodMs={} fetchPool={} maxTokensPerCycle={}",
                    Integer.valueOf(PERIOD_MS),
                    Integer.valueOf(FETCH_POOL_SIZE),
                    Integer.valueOf(MAX_TOKENS_PER_CYCLE));
			task = SCHED.scheduleWithFixedDelay(() -> {
				try {
					pollAndBroadcast();
				} catch (Exception e) {
					// 디버그 레벨에만 남기면 원인 파악이 어려워, 경고로 출력
					logger.warn("[WL-QUOTE] poll error", e);
				}
			}, 0, PERIOD_MS, TimeUnit.MILLISECONDS);
		}
	}

	private void stopTaskIfIdle() {
		if (!subs.isEmpty()) {
			return;
		}
		synchronized (this) {
			if (!subs.isEmpty()) {
				return;
			}
			if (task != null) {
				try {
					task.cancel(true);
				} catch (Exception ignore) {
				}
			}
			task = null;
		}
	}

	private void cleanupClosedSessions() {
		for (String token : new HashSet<String>(subs.keySet())) {
			Sub sub = subs.get(token);
			if (sub == null) {
				continue;
			}

			for (Session s : new HashSet<Session>(sub.sessions)) {
				try {
					if (s == null || !s.isOpen()) {
						sub.sessions.remove(s);
					}
				} catch (Exception ignore) {
					sub.sessions.remove(s);
				}
			}

			if (sub.sessions.isEmpty()) {
				subs.remove(token);
			}
		}
	}

	private void pollAndBroadcast() {
		if (subs.isEmpty()) {
			return;
		}

		KisClient client = getClientSafe();
		if (client == null) {
			return;
		}

		cleanupClosedSessions();
		if (subs.isEmpty()) {
			stopTaskIfIdle();
			return;
		}

		List<String> tokens = new ArrayList<String>(subs.keySet());
        if (tokens.isEmpty()) {
            stopTaskIfIdle();
            return;
        }

        if (tokens.size() > MAX_TOKENS_PER_CYCLE) {
            Collections.sort(tokens);
        }
        List<String> batch = pickTokensForCycle(tokens);
        if (batch.isEmpty()) {
            return;
        }

        PollStats stats = new PollStats();
        stats.totalTokens = tokens.size();
        stats.batchSize = batch.size();
        stats.startedAt = System.currentTimeMillis();

        CompletionService<QuotePayload> ecs = new ExecutorCompletionService<QuotePayload>(FETCH_POOL);
        List<Future<QuotePayload>> futures = new ArrayList<Future<QuotePayload>>(batch.size());
        Map<Future<QuotePayload>, String> tokenByFuture = new HashMap<Future<QuotePayload>, String>(batch.size());

        for (String token : batch) {
            Future<QuotePayload> f = ecs.submit(() -> fetchQuote(client, token));
            futures.add(f);
            tokenByFuture.put(f, token);
        }

        long budgetMs = resolvePollBudgetMs();
        long deadline = System.currentTimeMillis() + budgetMs;
        int received = 0;

        while (received < batch.size()) {
            long remain = deadline - System.currentTimeMillis();
            if (remain <= 0) {
                break;
            }
            Future<QuotePayload> f = null;
            try {
                f = ecs.poll(remain, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
            if (f == null) {
                break;
            }
            received++;

            QuotePayload payload = null;
            try {
                payload = f.get(0, TimeUnit.MILLISECONDS);
            } catch (TimeoutException te) {
                // 이미 poll 로 완료된 future 이므로 거의 발생하지 않음
            } catch (Exception e) {
                stats.errors++;
            }

            if (payload == null) {
                stats.emptyPayload++;
                continue;
            }

            broadcastPayload(payload, stats);
        }

        // 남은 작업은 취소 (네트워크/외부 지연 방어)
        List<String> timeoutSamples = new ArrayList<String>();
        for (Future<QuotePayload> f : futures) {
            if (f == null || f.isDone()) {
                continue;
            }
            try {
                f.cancel(true);
            } catch (Exception ignore) {
            }
            stats.timeouts++;
            if (timeoutSamples.size() < 5) {
                String t = tokenByFuture.get(f);
                if (t != null) {
                    timeoutSamples.add(t);
                }
            }
        }

        if (stats.timeouts > 0 || stats.noData > 0 || stats.errors > 0 || stats.emptyPayload > 0) {
            long tookMs = System.currentTimeMillis() - stats.startedAt;
            logger.warn("[WL-QUOTE] cycle tokens={} batch={} sent={} noData={} noSub={} noChange={} timeout={} errors={} empty={} tookMs={} sampleTimeouts={}",
                    Integer.valueOf(stats.totalTokens),
                    Integer.valueOf(stats.batchSize),
                    Integer.valueOf(stats.sent),
                    Integer.valueOf(stats.noData),
                    Integer.valueOf(stats.skippedNoSub),
                    Integer.valueOf(stats.skippedNoChange),
                    Integer.valueOf(stats.timeouts),
                    Integer.valueOf(stats.errors),
                    Integer.valueOf(stats.emptyPayload),
                    Long.valueOf(tookMs),
                    timeoutSamples);
        } else if (logger.isDebugEnabled()) {
            long tookMs = System.currentTimeMillis() - stats.startedAt;
            logger.debug("[WL-QUOTE] cycle tokens={} batch={} sent={} tookMs={}",
                    Integer.valueOf(stats.totalTokens),
                    Integer.valueOf(stats.batchSize),
                    Integer.valueOf(stats.sent),
                    Long.valueOf(tookMs));
        }

		stopTaskIfIdle();
	}

    private List<String> pickTokensForCycle(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return Collections.emptyList();
        }
        if (tokens.size() <= MAX_TOKENS_PER_CYCLE) {
            return tokens;
        }

        int size = tokens.size();
        int start = Math.floorMod(cursor.getAndAdd(MAX_TOKENS_PER_CYCLE), size);
        List<String> out = new ArrayList<String>(MAX_TOKENS_PER_CYCLE);

        for (int i = 0; i < MAX_TOKENS_PER_CYCLE; i++) {
            out.add(tokens.get((start + i) % size));
        }

        return out;
    }

    private long resolvePollBudgetMs() {
        long budget = PERIOD_MS > 0 ? (PERIOD_MS * 2L) : FETCH_TIMEOUT_MS;
        budget = Math.min(budget, FETCH_TIMEOUT_MS);
        if (budget < 800L) {
            budget = 800L;
        }
        return budget;
    }

    private void broadcastPayload(QuotePayload payload, PollStats stats) {
        if (payload == null) {
            if (stats != null) {
                stats.noData++;
            }
            return;
        }

        String token = payload.token;
        if (token == null || token.trim().isEmpty()) {
            if (stats != null) {
                stats.noData++;
            }
            return;
        }

        Sub sub = subs.get(token);
        if (sub == null || sub.sessions.isEmpty()) {
            if (stats != null) {
                stats.skippedNoSub++;
            }
            return;
        }

        QuotePayload effective = payload;
        boolean hasPayload = hasQuoteData(payload);
        if (!hasPayload && sub.lastPayload != null) {
            effective = sub.lastPayload;
        }

        if (!hasPayload && sub.lastPayload == null) {
            if (stats != null) {
                stats.noData++;
            }
            return;
        }

        if (hasQuoteData(effective)) {
            sub.lastPayload = effective;
        }

        String key = safe(effective.price) + "|" + safe(effective.diff) + "|" + safe(effective.rate) + "|"
                + safe(effective.sign);
        long now = System.currentTimeMillis();

        if (key.equals(sub.lastKey) && (now - sub.lastSendAt) < (PERIOD_MS - 200L)) {
            if (stats != null) {
                stats.skippedNoChange++;
            }
            return;
        }

        sub.lastKey = key;
        sub.lastSendAt = now;

        String msg = JsonUtil.toJson(effective);
        for (Session s : new HashSet<Session>(sub.sessions)) {
            try {
                if (s != null && s.isOpen()) {
                    s.getAsyncRemote().sendText(msg);
                } else {
                    sub.sessions.remove(s);
                }
            } catch (Exception ignore) {
                sub.sessions.remove(s);
            }
        }

        if (sub.sessions.isEmpty()) {
            subs.remove(token);
        }

        if (stats != null) {
            stats.sent++;
        }
    }

    private boolean hasQuoteData(QuotePayload p) {
        if (p == null) {
            return false;
        }
        if (!safe(p.price).isEmpty()) {
            return true;
        }
        if (!safe(p.diff).isEmpty()) {
            return true;
        }
        if (!safe(p.rate).isEmpty()) {
            return true;
        }
        return false;
    }

	private QuotePayload fetchQuote(KisClient client, String token) {
		TokenParts p = parseToken(token);
		if (p == null) {
			return null;
		}

		QuotePayload payload = new QuotePayload();
		payload.type = "WL";
		payload.token = token;
		payload.country = p.country;
		payload.market = p.market;
		payload.code = p.code;

		try {
			if (p.isDomestic) {
				InquirePriceApi api = new InquirePriceApi();
				api.setFidInputIscd(p.code);
				api.setFidCondMrktDivCode("UN");

				InquirePriceResult result = client.execute(api);
                if (result == null || result.getOutput() == null) {
                    return payload;
                }

                String rtCd = result.getRtCd();
                if (rtCd != null && !rtCd.isEmpty() && !"0".equals(rtCd)) {
                    return payload;
                }

                InquirePriceResult.Output o = result.getOutput();
                payload.price = safe(o.getStckPrpr());
                payload.diff = safe(o.getPrdyVrss());
                payload.rate = safe(o.getPrdyCtrt());
                payload.sign = safe(o.getPrdyVrssSign());
                return payload;
			}

			String excd = normalizeExcd(p.market, p.country);
			if (excd == null || excd.isEmpty()) {
				excd = "NAS";
			}

			PriceApi api = new PriceApi();
			api.setExcd(excd);
			api.setSymb(p.code);
			PriceResult result = client.execute(api);
			if (result == null || result.getOutput() == null) {
				return payload;
			}

            String rtCd = result.getRtCd();
            if (rtCd != null && !rtCd.isEmpty() && !"0".equals(rtCd)) {
                return payload;
            }

			PriceResult.Output o = result.getOutput();
			payload.price = safe(o.getLast());
			payload.rate = safe(o.getRate());
			payload.sign = safe(o.getSign());
			// 해외 API는 diff가 절대값으로 오므로 sign(4=하락,5=하한)일 때 음수로 변환
			String diffVal = safe(o.getDiff());
			String signCode = payload.sign;
			if (!diffVal.isEmpty() && !diffVal.startsWith("-")
					&& ("4".equals(signCode) || "5".equals(signCode))) {
				diffVal = "-" + diffVal;
			}
			payload.diff = diffVal;
			return payload;
		} catch (Exception e) {
			return payload;
		}
	}

	private KisClient getClientSafe() {
		KisClient local = client;
		if (local != null) {
			return local;
		}
		synchronized (this) {
			if (client != null) {
				return client;
			}
			try {
				client = KisClientFactory.getClient();
				lastInitError = null;
			} catch (Exception e) {
				lastInitError = (e.getMessage() == null ? e.toString() : e.getMessage());
				client = null;
			}
			return client;
		}
	}

	private boolean ensureClient(Session session) {
		KisClient c = getClientSafe();
		if (c != null) {
			return true;
		}
		sendError(session, "KIS client init failed: " + safe(lastInitError));
		return false;
	}

	private void sendError(Session session, String msg) {
		if (session == null || !session.isOpen()) {
			return;
		}
		ErrorPayload p = new ErrorPayload();
		p.type = "ERR";
		p.message = msg == null ? "" : msg;
		try {
			session.getAsyncRemote().sendText(JsonUtil.toJson(p));
		} catch (Exception ignore) {
		}
	}

	public static class ErrorPayload {
		public String type;
		public String message;
	}

	private static class TokenParts {
		String country;
		String market;
		String code;
		boolean isDomestic;
	}

	private TokenParts parseToken(String token) {
		if (token == null) {
			return null;
		}

		String t = token.trim();
		if (t.isEmpty()) {
			return null;
		}

		String[] parts = t.split("\\|");
		TokenParts p = new TokenParts();

		if (parts.length == 1) {
			// legacy: 005930
			p.country = "KR";
			p.market = "KRX";
			p.code = parts[0].trim();
			p.isDomestic = true;
			return p;
		}

		if (parts.length == 2) {
			// KR|005930
			p.country = safe(parts[0]).toUpperCase();
			p.market = "KRX";
			p.code = safe(parts[1]);
			p.isDomestic = "KR".equalsIgnoreCase(p.country);
			return p;
		}

		// country|market|code
		p.country = safe(parts[0]).toUpperCase();
		p.market = safe(parts[1]);
		p.code = safe(parts[2]);

		// 국내로 들어오는 변형도 방어
		if (p.country.isEmpty() || "KR".equalsIgnoreCase(p.country)) {
			p.country = "KR";
			p.isDomestic = true;
			if (p.market.isEmpty()) {
				p.market = "KRX";
			}
			return p;
		}

		p.isDomestic = false;
		if (p.market.isEmpty()) {
			p.market = "NAS";
		}
		return p;
	}

	private String normalizeToken(String raw) {
		if (raw == null) {
			return null;
		}
		String t = raw.trim();
		if (t.isEmpty()) {
			return null;
		}

		// legacy: 005930
		if (t.indexOf('|') < 0) {
			return t;
		}

		// 최소 3파트로 정규화 (country|market|code)
		TokenParts p = parseToken(t);
		if (p == null) {
			return null;
		}

		String c = safe(p.country).toUpperCase();
		String m = safe(p.market);
		String code = safe(p.code);

		if (c.isEmpty()) {
			c = "KR";
		}
		if (m.isEmpty()) {
			m = "KR".equalsIgnoreCase(c) ? "KRX" : "NAS";
		}

		return c + "|" + m + "|" + code;
	}

	private String normalizeExcd(String market, String country) {
		String c = safe(country).toUpperCase();
		String m = safe(market).trim();
		if (m.isEmpty()) {
			return "US".equals(c) ? "NAS" : m;
		}

		String u = m.toUpperCase();

		// 이미 EXCD 인 경우
		if (u.equals("NAS") || u.equals("NYS") || u.equals("AMS") || u.equals("HKS") || u.equals("SHS")
				|| u.equals("SZS") || u.equals("TSE") || u.equals("TYO") || u.equals("HSX") || u.equals("HNX")) {
			return u;
		}

		// US
		if ("US".equals(c)) {
			if (u.contains("NASDAQ") || u.contains("NAS")) {
				return "NAS";
			}
			if (u.contains("NYSE") || u.contains("NYS")) {
				return "NYS";
			}
			if (u.contains("AMEX") || u.contains("AMS")) {
				return "AMS";
			}
			return "NAS";
		}

		// HK
		if ("HK".equals(c) || u.contains("HONG") || u.contains("HK")) {
			return "HKS";
		}

		// JP
		if ("JP".equals(c) || u.contains("TOKYO") || u.contains("TSE") || u.contains("TYO")) {
			return "TSE";
		}

		// CN
		if ("CN".equals(c) || u.contains("SHANG") || u.contains("SH")) {
			return "SHS";
		}
		if ("CN".equals(c) || u.contains("SHEN") || u.contains("SZ")) {
			return "SZS";
		}

		return u;
	}

    private String safeId(Session session) {
        try {
            return session == null ? "" : session.getId();
        } catch (Exception e) {
            return "";
        }
    }

	private String safe(Object v) {
		return v == null ? "" : String.valueOf(v).trim();
	}

	public static class QuotePayload {
		public String type;
		public String token;
		public String country;
		public String market;
		public String code;
		public String price;
		public String diff;
		public String rate;
		public String sign;
	}
}
