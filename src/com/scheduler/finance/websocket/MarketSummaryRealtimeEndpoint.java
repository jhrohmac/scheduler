package com.scheduler.finance.websocket;

import java.lang.reflect.Field;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.kis_api.api.rest.quotations.InquireIndexPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireIndexPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceResult;
import com.scheduler.kis_client.KisClient;
import com.scheduler.kis_client.util.JsonUtil;

/**
 * ?쒖옣?붿빟 ?ㅼ떆媛??쒕쾭?믩툕?쇱슦? WS)
 *
 * - KOSPI/KOSDAQ: 援?궡吏??議고쉶
 * - ?댁쇅吏???섏쑉: InquireOverseasDailyChartPriceApi (N/X)
 *
 * 二쇨린?곸쑝濡?REST 議고쉶 ??釉뚮씪?곗???push ?쒕떎.
 */
@ServerEndpoint(value = "/finance/marketSummaryRealtime.ws", configurator = NoExtensionsConfigurator.class)
public class MarketSummaryRealtimeEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MarketSummaryRealtimeEndpoint.class);

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ScheduledExecutorService EXEC = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setName("market-summary-realtime");
        t.setDaemon(true);
        return t;
    });

    private static final MarketSummaryRealtimeEndpoint WORKER = new MarketSummaryRealtimeEndpoint();
    private static final Set<Session> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
    private static volatile ScheduledFuture<?> GLOBAL_TASK;
    private static volatile SummaryPayload LAST_PAYLOAD;

    private volatile KisClient client;
    private volatile String lastInitError;

    public static void shutdown() {
        try {
            synchronized (MarketSummaryRealtimeEndpoint.class) {
                if (GLOBAL_TASK != null) {
                    GLOBAL_TASK.cancel(true);
                    GLOBAL_TASK = null;
                }
            }
            SESSIONS.clear();
            EXEC.shutdown();
            if (!EXEC.awaitTermination(2, TimeUnit.SECONDS)) {
                EXEC.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXEC.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception ignore) {
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        if (session == null) {
            return;
        }

        try {
            SESSIONS.add(session);
            SummaryPayload cached = LAST_PAYLOAD;
            if (cached != null) {
                session.getAsyncRemote().sendText(JsonUtil.toJson(cached));
            }
            startGlobalTaskIfNeeded();
            logger.info("[MS-WS] open session={}", safeId(session));
        } catch (Exception e) {
            logger.error("[MS-WS] open error", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        cancel(session);
        logger.info("[MS-WS] close session={}", safeId(session));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        cancel(session);
        logger.warn("[MS-WS] error session={}", safeId(session), t);
    }

    private void cancel(Session session) {
        if (session == null) {
            return;
        }
        SESSIONS.remove(session);
        Object o = session.getUserProperties().get("MS_TASK");
        if (o instanceof ScheduledFuture) {
            try {
                ((ScheduledFuture<?>) o).cancel(true);
            } catch (Exception ignore) {
            }
        }
        stopGlobalTaskIfIdle();
    }

    private static void startGlobalTaskIfNeeded() {
        if (GLOBAL_TASK != null && !GLOBAL_TASK.isCancelled()) {
            return;
        }
        synchronized (MarketSummaryRealtimeEndpoint.class) {
            if (GLOBAL_TASK != null && !GLOBAL_TASK.isCancelled()) {
                return;
            }
            GLOBAL_TASK = EXEC.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    WORKER.pollAndBroadcast();
                }
            }, 0, 5, TimeUnit.SECONDS);
            logger.info("[MS-WS] global polling started");
        }
    }

    private static void stopGlobalTaskIfIdle() {
        if (!SESSIONS.isEmpty()) {
            return;
        }
        synchronized (MarketSummaryRealtimeEndpoint.class) {
            if (!SESSIONS.isEmpty()) {
                return;
            }
            if (GLOBAL_TASK != null) {
                GLOBAL_TASK.cancel(true);
                GLOBAL_TASK = null;
                logger.info("[MS-WS] global polling stopped");
            }
        }
    }

    private void pollAndBroadcast() {
        cleanupClosedSessions();
        if (SESSIONS.isEmpty()) {
            stopGlobalTaskIfIdle();
            return;
        }

        KisClient client = getClientSafe();
        if (client == null) {
            broadcastInitErrorOnce();
            return;
        }

        SummaryPayload payload = buildPayload(client);
        LAST_PAYLOAD = payload;
        String msg = JsonUtil.toJson(payload);
        for (Session session : new HashSet<Session>(SESSIONS)) {
            try {
                if (session != null && session.isOpen()) {
                    session.getAsyncRemote().sendText(msg);
                } else {
                    SESSIONS.remove(session);
                }
            } catch (Exception ignore) {
                SESSIONS.remove(session);
            }
        }
        stopGlobalTaskIfIdle();
    }

    private static void cleanupClosedSessions() {
        for (Session session : new HashSet<Session>(SESSIONS)) {
            try {
                if (session == null || !session.isOpen()) {
                    SESSIONS.remove(session);
                }
            } catch (Exception e) {
                SESSIONS.remove(session);
            }
        }
    }

    private void broadcastInitErrorOnce() {
        for (Session session : new HashSet<Session>(SESSIONS)) {
            try {
                if (session == null || !session.isOpen()) {
                    SESSIONS.remove(session);
                    continue;
                }
                if (session.getUserProperties().get("MS_INIT_ERR_SENT") == null) {
                    session.getUserProperties().put("MS_INIT_ERR_SENT", Boolean.TRUE);
                    sendError(session, "KIS client init failed: " + safe(lastInitError));
                }
            } catch (Exception ignore) {
                SESSIONS.remove(session);
            }
        }
    }

    private SummaryPayload buildPayload(KisClient client) {
        SummaryPayload payload = new SummaryPayload();
        payload.type = "SUMMARY";
        payload.asOf = LocalDateTime.now(KOREA_ZONE).format(TS_FMT);
        payload.items = new LinkedHashMap<String, SummaryItem>();

        // 援?궡吏??
        payload.items.put("KOSPI", fetchDomesticIndex(client, "0001"));
        payload.items.put("KOSDAQ", fetchDomesticIndex(client, "1001"));

        // ?섏쑉
        payload.items.put("USDKRW", fetchOverseasLike(client, new String[] { "X" },
                new String[] { "USDKRW", "USD/KRW", "USD" }));

        // ?댁쇅吏??
        payload.items.put("DJI", fetchOverseasLike(client, new String[] { "N" },
                new String[] { ".DJI", "DJI", "DOW" }));
        payload.items.put("IXIC", fetchOverseasLike(client, new String[] { "N" },
                new String[] { "COMP", ".COMP", ".IXIC", "IXIC", "NASDAQ" }));
        payload.items.put("SPX", fetchOverseasLike(client, new String[] { "N" },
                new String[] { ".INX", "SPX", "S&P500" }));

        return payload;
    }

    private SummaryItem fetchDomesticIndex(KisClient client, String fidInputIscd) {
        SummaryItem it = new SummaryItem();
        it.text = "-";
        it.sign = "0";

        try {
            InquireIndexPriceApi api = newIndexPriceApi(fidInputIscd);
            InquireIndexPriceResult result = client.execute(api);
            com.scheduler.finance.kis.quote.KisQuoteDto dto =
                    com.scheduler.finance.kis.quote.KisQuoteMapper.fromDomesticIndex(
                            "KR|KRX|" + safe(fidInputIscd), fidInputIscd, result);
            if (dto.isStale()) {
                return it;
            }
            String prpr = safe(dto.getPrice());
            String diff = safe(dto.getDiff());
            String rate = safe(dto.getRate());
            String sign = safe(dto.getSign());
            if (sign.isEmpty()) sign = "0";

            it.text = buildText(prpr, diff, rate, sign);
            it.sign = sign;
            it.price = prpr;
            it.diff = diff;
            it.rate = rate;
            return it;
        } catch (Exception e) {
            return it;
        }
    }

    private InquireIndexPriceApi newIndexPriceApi(String fidInputIscd) throws Exception {
        // 1) Lombok @RequiredArgsConstructor ?앹꽦??String) 議댁옱?섎뒗 寃쎌슦
        try {
            return InquireIndexPriceApi.class.getConstructor(String.class).newInstance(fidInputIscd);
        } catch (Exception ignore) {
        }

        // 2) 湲곕낯?앹꽦??+ ?꾨뱶 二쇱엯 諛⑹떇
        InquireIndexPriceApi api = InquireIndexPriceApi.class.getConstructor().newInstance();
        setField(api, "fidInputIscd", fidInputIscd);
        setField(api, "fidCondMrktDivCode", "U");
        return api;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = findField(target.getClass(), fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) throws Exception {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private SummaryItem fetchOverseasLike(KisClient client, String[] marketDivCodes, String[] codes) {
        for (String m : marketDivCodes) {
            for (String c : codes) {
                SummaryItem it = fetchOverseasOne(client, m, c);
                if (it != null && it.text != null && !it.text.isEmpty() && !"-".equals(it.text)) {
                    return it;
                }
            }
        }
        SummaryItem it = new SummaryItem();
        it.text = "-";
        it.sign = "0";
        return it;
    }

    private SummaryItem fetchOverseasOne(KisClient client, String fidCondMrktDivCode, String fidInputIscd) {
        SummaryItem it = new SummaryItem();
        it.text = "-";
        it.sign = "0";

        try {
            String end = java.time.LocalDate.now(KOREA_ZONE).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String start = java.time.LocalDate.now(KOREA_ZONE).minusDays(7)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            InquireOverseasDailyChartPriceApi api = new InquireOverseasDailyChartPriceApi();
            api.setFidCondMrktDivCode(fidCondMrktDivCode);
            api.setFidInputIscd(fidInputIscd);
            api.setFidInputDate1(start);
            api.setFidInputDate2(end);
            api.setFidPeriodDivCode("D");

            InquireOverseasDailyChartPriceResult result = client.execute(api);
            com.scheduler.finance.kis.quote.KisQuoteDto dto =
                    com.scheduler.finance.kis.quote.KisQuoteMapper.fromOverseasIndex(
                            safe(fidCondMrktDivCode) + "|IDX|" + safe(fidInputIscd),
                            safe(fidCondMrktDivCode), "IDX", fidInputIscd, result);
            if (dto.isStale()) {
                return it;
            }
            String prpr = safe(dto.getPrice());
            String diff = safe(dto.getDiff());
            String rate = safe(dto.getRate());
            String sign = safe(dto.getSign());
            if (sign.isEmpty()) sign = "0";

            if (isMissingOverseasValue(prpr, diff, rate)) {
                return null;
            }

            it.text = buildText(prpr, diff, rate, sign);
            it.sign = sign;
            it.price = prpr;
            it.diff = diff;
            it.rate = rate;
            return it;
        } catch (Exception e) {
            return it;
        }
    }

    private String buildText(String prpr, String diff, String rate, String sign) {
        prpr = safe(prpr);
        diff = safe(diff);
        rate = safe(rate);

        if (prpr.isEmpty()) {
            return "-";
        }

        return prpr + " (" + formatSignedDiff(diff, sign) + " / " + rate + "%)";
    }

    private String formatSignedDiff(String diff, String sign) {
        String value = safe(diff);
        if (value.isEmpty()) {
            return value;
        }
        if (value.startsWith("+") || value.startsWith("-")) {
            return value;
        }
        if ("+".equals(sign) || "-".equals(sign)) {
            return sign + value;
        }
        return value;
    }

    private boolean isMissingOverseasValue(String price, String diff, String rate) {
        String p = safe(price);
        if (p.isEmpty()) return true;
        if (!isZeroLike(p)) return false;

        String d = safe(diff);
        String r = safe(rate);
        return isZeroLike(d) && isZeroLike(r);
    }

    private boolean isZeroLike(String v) {
        if (v == null) return true;
        String s = v.trim();
        if (s.isEmpty()) return true;
        s = s.replace(",", "");
        if (s.startsWith("+")) s = s.substring(1);
        try {
            return Double.parseDouble(s) == 0.0;
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeSign(String prdyVrssSign) {
        String v = safe(prdyVrssSign);
        // KIS 부호코드: 1=상한, 2=상승, 3=보합, 4=하한, 5=하락
        if ("1".equals(v) || "2".equals(v)) {
            return "+";
        }
        if ("4".equals(v) || "5".equals(v)) {
            return "-";
        }
        return "0";
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

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String safeId(Session session) {
        try {
            return session == null ? "" : session.getId();
        } catch (Exception e) {
            return "";
        }
    }

    public static class SummaryPayload {
        public String type;
        public String asOf;
        public Map<String, SummaryItem> items;
    }

    public static class SummaryItem {
        public String text;
        public String sign;
        public String price;
        public String diff;
        public String rate;
    }

    public static class ErrorPayload {
        public String type;
        public String message;
    }
}
