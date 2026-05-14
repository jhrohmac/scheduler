package com.scheduler.finance.websocket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.finance.kis.config.KisConfigLoader;
import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.finance.kis.quote.KisMarketCode;
import com.scheduler.finance.kis.quote.KisQuoteDto;
import com.scheduler.finance.kis.quote.KisQuoteMapper;
import com.scheduler.kis_api.api.CommonRealTimeApi;
import com.scheduler.kis_api.api.CommonRealTimeResult;
import com.scheduler.kis_api.api.realtime.H0NXCNT0Api;
import com.scheduler.kis_api.api.realtime.H0STOUP0Api;
import com.scheduler.kis_api.api.realtime.H0STCNT0Api;
import com.scheduler.kis_api.api.realtime.H0UNCNT0Api;
import com.scheduler.kis_client.KisClient;
import com.scheduler.kis_client.api.RealTimeApiData;
import com.scheduler.kis_client.util.JsonUtil;

/**
 * 국내 관심종목 실시간 체결가 허브
 *
 * 문제(로그 기준):
 * - F5/그룹변경 시 동일 session이 같은 종목을 중복 구독하면서 refCount가 증가
 * - 구독 실패 시(refCount 롤백 없음) 재시도가 꼬임
 *
 * 해결:
 * - session이 이미 구독 중이면 refCount 증가/재구독 금지
 * - 최초 구독 실패 시 sessions/refCount 롤백
 */
public class WatchlistDomesticHub {

    private static final Logger logger = LoggerFactory.getLogger(WatchlistDomesticHub.class);

    private static final WatchlistDomesticHub INSTANCE = new WatchlistDomesticHub();

    public static WatchlistDomesticHub getInstance() {
        return INSTANCE;
    }

    private final KisClient client;
    private final int maxSubscriptions;
    private final long subscribeMinIntervalMs;
    private final long unsubscribeMinIntervalMs;
    private final Object subscribeThrottleLock = new Object();
    private final Object unsubscribeThrottleLock = new Object();
    private long lastSubscribeSentAt;
    private long lastUnsubscribeSentAt;

    private static class Sub {
        final AtomicInteger refCount = new AtomicInteger(0);
        final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
        CommonRealTimeResult<? extends RealTimeApiData> response;
        Consumer<RealTimeApiData[]> handler;
        TickPayload lastPayload;
    }

    private final ConcurrentMap<String, Sub> subs = new ConcurrentHashMap<String, Sub>();

    private WatchlistDomesticHub() {
        this.client = KisClientFactory.getClient();
        this.maxSubscriptions = resolveMaxSubscriptions();
        this.subscribeMinIntervalMs = resolveSubscribeMinIntervalMs();
        this.unsubscribeMinIntervalMs = resolveUnsubscribeMinIntervalMs();
    }

    private long resolveUnsubscribeMinIntervalMs() {
        try {
            int value = KisConfigLoader.getKisProperties().getWsUnsubscribeMinIntervalMs();
            return value < 0 ? 100L : value;
        } catch (Exception e) {
            return 100L;
        }
    }

    private int resolveMaxSubscriptions() {
        try {
            int value = KisConfigLoader.getKisProperties().getWsMaxSubscriptions();
            return value <= 0 ? 40 : value;
        } catch (Exception e) {
            return 40;
        }
    }

    private long resolveSubscribeMinIntervalMs() {
        try {
            int value = KisConfigLoader.getKisProperties().getWsSubscribeMinIntervalMs();
            return value < 0 ? 100L : value;
        } catch (Exception e) {
            return 100L;
        }
    }

    public Set<String> subscribe(Session session, Set<String> codes) {
        Set<String> failed = new HashSet<String>();
        if (session == null || codes == null || codes.isEmpty()) {
            return failed;
        }

        for (String code : codes) {
            DomesticSubscriptionKey key = DomesticSubscriptionKey.parse(code);
            if (key == null) {
                continue;
            }
            boolean ok = subscribeOne(session, key);
            if (!ok) {
                failed.add(key.token);
            }
        }
        return failed;
    }

    /**
     * 세션이 가진 모든 종목 구독 해제
     */
    public void unsubscribe(Session session) {
        if (session == null) {
            return;
        }

        for (String key : new HashSet<String>(subs.keySet())) {
            Sub sub = subs.get(key);
            if (sub == null) {
                continue;
            }
            if (!sub.sessions.contains(session)) {
                continue;
            }
            unsubscribeOne(session, key);
        }
    }

    private boolean subscribeOne(Session session, DomesticSubscriptionKey key) {
        if (!subs.containsKey(key.token) && subs.size() >= maxSubscriptions) {
            logger.warn("[WL-HUB] subscribe rejected by upstream limit token={} active={} max={}", key.token,
                    Integer.valueOf(subs.size()), Integer.valueOf(maxSubscriptions));
            return false;
        }

        Sub sub = subs.computeIfAbsent(key.token, k -> new Sub());

        // 이미 구독 중이면 refCount 증가 금지
        boolean added = sub.sessions.add(session);
        if (!added) {
            return true;
        }

        if (sub.lastPayload != null) {
            // 캐시된 최근 tick 전송 → 브라우저 "연결됨" 전환
            try {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(JsonUtil.toJson(sub.lastPayload));
                }
            } catch (Exception ignore) {
            }
        }

        int after = sub.refCount.incrementAndGet();

        // 첫 구독자일 때만 KIS 구독
        if (after != 1) {
            // KIS 이미 구독 중. lastPayload 없으면 ack 전송
            if (sub.lastPayload == null) {
                sendSubAck(session, key);
            }
            return true;
        }

        try {
            throttleSubscribeSend();
            CommonRealTimeApi<? extends CommonRealTimeResult<? extends RealTimeApiData>> api = createApi(key);
            CommonRealTimeResult<? extends RealTimeApiData> resp = client.execute(api);

            Consumer<RealTimeApiData[]> handler = (arr) -> {
                if (arr == null || arr.length == 0) {
                    return;
                }
                RealTimeApiData d = arr[0];
                if (d == null) {
                    return;
                }
                broadcastTick(key, d);
            };

            addHandler(resp, handler);

            sub.response = resp;
            sub.handler = handler;

            // KIS 구독 성공 → 첫 tick 도착 전에 브라우저 "연결됨" 상태로 전환
            if (sub.lastPayload == null) {
                sendSubAck(session, key);
            }

            logger.info("[WL-HUB] subscribed token={} tr={} (ref=1)", key.token, key.trId);
            return true;
        } catch (Exception e) {
            // 실패 시 롤백(다음 subscribe에서 정상 재시도 가능)
            logger.error("[WL-HUB] subscribe failed token={} (rollback)", key.token, e);

            sub.sessions.remove(session);
            int ref = sub.refCount.decrementAndGet();
            if (ref <= 0) {
                subs.remove(key.token);
            }
            return false;
        }
    }

    private void throttleSubscribeSend() {
        if (subscribeMinIntervalMs <= 0) {
            return;
        }
        synchronized (subscribeThrottleLock) {
            long now = System.currentTimeMillis();
            long wait = subscribeMinIntervalMs - (now - lastSubscribeSentAt);
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastSubscribeSentAt = System.currentTimeMillis();
        }
    }

    private void throttleUnsubscribeSend() {
        if (unsubscribeMinIntervalMs <= 0) {
            return;
        }
        synchronized (unsubscribeThrottleLock) {
            long now = System.currentTimeMillis();
            long wait = unsubscribeMinIntervalMs - (now - lastUnsubscribeSentAt);
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastUnsubscribeSentAt = System.currentTimeMillis();
        }
    }

    /**
     * KIS 구독 등록 완료 ack 전송 (첫 tick 도착 전 브라우저 상태 업데이트용)
     */
    private void sendSubAck(Session session, DomesticSubscriptionKey key) {
        if (session == null || !session.isOpen()) {
            return;
        }
        SubAckPayload ack = new SubAckPayload();
        ack.type = "WL_SUB";
        ack.token = key.token;
        ack.country = "KR";
        ack.market = key.market;
        ack.code = key.code;
        ack.source = "WS_SUB";
        try {
            session.getAsyncRemote().sendText(JsonUtil.toJson(ack));
        } catch (Exception ignore) {
        }
    }

    private void unsubscribeOne(Session session, String token) {
        Sub sub = subs.get(token);
        if (sub == null) {
            return;
        }

        boolean removed = sub.sessions.remove(session);
        if (!removed) {
            return;
        }

        int after = sub.refCount.decrementAndGet();

        if (after > 0) {
            return;
        }

        try {
            throttleUnsubscribeSend();
            if (sub.response != null && sub.handler != null) {
                removeHandler(sub.response, sub.handler);
            }
            if (sub.response != null) {
                sub.response.unsubscribe();
            }
        } catch (Exception e) {
            logger.warn("[WL-HUB] unsubscribe error token={}", token, e);
        } finally {
            subs.remove(token);
            logger.info("[WL-HUB] unsubscribed token={} (ref=0)", token);
        }
    }

    private void broadcastTick(DomesticSubscriptionKey key, RealTimeApiData d) {
        Sub sub = subs.get(key.token);
        if (sub == null) {
            return;
        }

        KisQuoteDto quote = KisQuoteMapper.fromDomesticTick(key.token, key.market, key.code, d);
        TickPayload payload = TickPayload.from(quote);
        sub.lastPayload = payload;

        String msg = JsonUtil.toJson(payload);

        for (Session s : sub.sessions) {
            try {
                if (s != null && s.isOpen()) {
                    s.getAsyncRemote().sendText(msg);
                }
            } catch (Exception ignore) {
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addHandler(CommonRealTimeResult<? extends RealTimeApiData> response, Consumer<RealTimeApiData[]> handler) {
        ((CommonRealTimeResult) response).addHandler((Consumer) handler);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void removeHandler(CommonRealTimeResult<? extends RealTimeApiData> response, Consumer<RealTimeApiData[]> handler) {
        ((CommonRealTimeResult) response).removeHandler((Consumer) handler);
    }

    private CommonRealTimeApi<? extends CommonRealTimeResult<? extends RealTimeApiData>> createApi(
            DomesticSubscriptionKey key) {
        if ("NXT".equals(key.market)) {
            return new H0NXCNT0Api(key.code);
        }
        if ("UN".equals(key.market)) {
            return new H0UNCNT0Api(key.code);
        }
        if ("OVERTIME".equals(key.market)) {
            return new H0STOUP0Api(key.code);
        }
        return new H0STCNT0Api(key.code);
    }

    private String safe(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static class DomesticSubscriptionKey {
        final String market;
        final String code;
        final String token;
        final String trId;

        DomesticSubscriptionKey(String market, String code) {
            this.market = KisMarketCode.normalizeDomesticMarket(market);
            this.code = code;
            this.token = "KR|" + this.market + "|" + code;
            this.trId = resolveTrId(this.market);
        }

        static DomesticSubscriptionKey parse(String raw) {
            String s = raw == null ? "" : raw.trim();
            if (s.isEmpty()) {
                return null;
            }
            String market = "KRX";
            String code = s;
            String[] parts = s.split("\\|");
            if (parts.length == 2) {
                code = safeStatic(parts[1]);
            } else if (parts.length >= 3) {
                market = safeStatic(parts[1]);
                code = safeStatic(parts[parts.length - 1]);
            }
            if (code.isEmpty()) {
                return null;
            }
            return new DomesticSubscriptionKey(market, code);
        }

        private static String resolveTrId(String market) {
            if ("NXT".equals(market)) {
                return "H0NXCNT0";
            }
            if ("UN".equals(market)) {
                return "H0UNCNT0";
            }
            if ("OVERTIME".equals(market)) {
                return "H0STOUP0";
            }
            return "H0STCNT0";
        }

        private static String safeStatic(String value) {
            return value == null ? "" : value.trim();
        }
    }

    public static class TickPayload {
        public String type;
        public String token;
        public String country;
        public String market;
        public String code;
        public String price;
        public String diff;
        public String rate;
        public String sign;
        public String basePrice;
        public String source;
        public long fetchedAt;
        public boolean stale;

        public static TickPayload from(KisQuoteDto quote) {
            TickPayload p = new TickPayload();
            if (quote == null) {
                return p;
            }
            p.type = quote.getType();
            p.token = quote.getToken();
            p.country = quote.getCountry();
            p.market = quote.getMarket();
            p.code = quote.getCode();
            p.price = quote.getPrice();
            p.diff = quote.getDiff();
            p.rate = quote.getRate();
            p.sign = quote.getSign();
            p.basePrice = quote.getBasePrice();
            p.source = quote.getSource();
            p.fetchedAt = quote.getFetchedAt();
            p.stale = quote.isStale();
            return p;
        }
    }

    public static class SubAckPayload {
        public String type;
        public String token;
        public String country;
        public String market;
        public String code;
        public String source;
    }
}
