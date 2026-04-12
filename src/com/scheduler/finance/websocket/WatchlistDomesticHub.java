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

import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.kis_api.api.realtime.H0STCNT0Api;
import com.scheduler.kis_api.api.realtime.H0STCNT0Data;
import com.scheduler.kis_api.api.realtime.H0STCNT0Response;
import com.scheduler.kis_client.KisClient;
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

    private static class Sub {
        final AtomicInteger refCount = new AtomicInteger(0);
        final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
        H0STCNT0Response response;
        Consumer<H0STCNT0Data[]> handler;
        TickPayload lastPayload;
    }

    private final ConcurrentMap<String, Sub> subs = new ConcurrentHashMap<String, Sub>();

    private WatchlistDomesticHub() {
        this.client = KisClientFactory.getClient();
    }

    public Set<String> subscribe(Session session, Set<String> codes) {
        Set<String> failed = new HashSet<String>();
        if (session == null || codes == null || codes.isEmpty()) {
            return failed;
        }

        for (String code : codes) {
            String c = normalize(code);
            if (c == null) {
                continue;
            }
            boolean ok = subscribeOne(session, c);
            if (!ok) {
                failed.add(c);
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

        for (String code : new HashSet<String>(subs.keySet())) {
            Sub sub = subs.get(code);
            if (sub == null) {
                continue;
            }
            if (!sub.sessions.contains(session)) {
                continue;
            }
            unsubscribeOne(session, code);
        }
    }

    private boolean subscribeOne(Session session, String code) {
        Sub sub = subs.computeIfAbsent(code, k -> new Sub());

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
                sendSubAck(session, code);
            }
            return true;
        }

        try {
            H0STCNT0Api api = new H0STCNT0Api(code);
            H0STCNT0Response resp = client.execute(api);

            Consumer<H0STCNT0Data[]> handler = (arr) -> {
                if (arr == null || arr.length == 0) {
                    return;
                }
                H0STCNT0Data d = arr[0];
                if (d == null) {
                    return;
                }
                broadcastTick(code, d);
            };

            resp.addHandler(handler);

            sub.response = resp;
            sub.handler = handler;

            // KIS 구독 성공 → 첫 tick 도착 전에 브라우저 "연결됨" 상태로 전환
            if (sub.lastPayload == null) {
                sendSubAck(session, code);
            }

            logger.info("[WL-HUB] subscribed code={} (ref=1)", code);
            return true;
        } catch (Exception e) {
            // 실패 시 롤백(다음 subscribe에서 정상 재시도 가능)
            logger.error("[WL-HUB] subscribe failed code={} (rollback)", code, e);

            sub.sessions.remove(session);
            int ref = sub.refCount.decrementAndGet();
            if (ref <= 0) {
                subs.remove(code);
            }
            return false;
        }
    }

    /**
     * KIS 구독 등록 완료 ack 전송 (첫 tick 도착 전 브라우저 상태 업데이트용)
     */
    private void sendSubAck(Session session, String code) {
        if (session == null || !session.isOpen()) {
            return;
        }
        SubAckPayload ack = new SubAckPayload();
        ack.type = "WL_SUB";
        ack.code = code;
        try {
            session.getAsyncRemote().sendText(JsonUtil.toJson(ack));
        } catch (Exception ignore) {
        }
    }

    private void unsubscribeOne(Session session, String code) {
        Sub sub = subs.get(code);
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
            if (sub.response != null && sub.handler != null) {
                sub.response.removeHandler(sub.handler);
            }
            if (sub.response != null) {
                sub.response.unsubscribe();
            }
        } catch (Exception e) {
            logger.warn("[WL-HUB] unsubscribe error code={}", code, e);
        } finally {
            subs.remove(code);
            logger.info("[WL-HUB] unsubscribed code={} (ref=0)", code);
        }
    }

    private void broadcastTick(String code, H0STCNT0Data d) {
        Sub sub = subs.get(code);
        if (sub == null) {
            return;
        }

        TickPayload payload = new TickPayload();
        payload.type = "WL";
        payload.code = code;
        payload.price = safe(d.getStckPrpr());
        payload.diff = safe(d.getPrdyVrss());
        payload.rate = safe(d.getPrdyCtrt());
        payload.sign = safe(d.getPrdyVrssSign());
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

    private String normalize(String code) {
        if (code == null) {
            return null;
        }
        String c = code.trim();
        return c.isEmpty() ? null : c;
    }

    private String safe(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    public static class TickPayload {
        public String type;
        public String code;
        public String price;
        public String diff;
        public String rate;
        public String sign;
    }

    public static class SubAckPayload {
        public String type;
        public String code;
    }
}
