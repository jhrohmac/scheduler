package com.scheduler.finance.websocket;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.kis_client.util.JsonUtil;

/**
 * 브라우저용 관심종목 리얼타임 엔드포인트
 *
 * 접속 예)
 * ws(s)://{host}{contextPath}/finance/watchlistRealtime.ws?codes=KR|kospi|005930,US|nasdaq|AAPL
 *
 * 주의)
 * - 브라우저가 쿼리스트링을 URL 인코딩하여 전달할 수 있다.
 *   (예: ',' -> %2C, '|' -> %7C)
 * - 따라서 codes 파싱은 반드시 URLDecode 후 split 해야 한다.
 */
@ServerEndpoint("/finance/watchlistRealtime.ws")
public class WatchlistRealtimeEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(WatchlistRealtimeEndpoint.class);

    private final WatchlistQuoteHub quoteHub = WatchlistQuoteHub.getInstance();

    @OnOpen
    public void onOpen(Session session) {
        try {
            ParseResult pr = parseCodes(session);
            Set<String> tokens = pr.tokens;

            if (tokens.isEmpty()) {
                sendError(session, "codes 파라미터가 비어있습니다.");
                logger.warn("[WL-WS] open rejected session={} reason=noCodes raw={} decoded={}", safeId(session), pr.raw, pr.decoded);
                return;
            }

            quoteHub.subscribe(session, tokens);

            String sample = tokens.iterator().hasNext() ? tokens.iterator().next() : "";
            logger.info("[WL-WS] open session={} tokens={} rawLen={} decodedLen={} sample={}",
                    safeId(session),
                    Integer.valueOf(tokens.size()),
                    Integer.valueOf(pr.raw == null ? 0 : pr.raw.length()),
                    Integer.valueOf(pr.decoded == null ? 0 : pr.decoded.length()),
                    sample);

            if (logger.isDebugEnabled()) {
                logger.debug("[WL-WS] open session={} raw={} decoded={}", safeId(session), pr.raw, pr.decoded);
            }

        } catch (Exception e) {
            logger.error("[WL-WS] open error", e);
            sendError(session, e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        try {
            quoteHub.unsubscribe(session);
        } catch (Exception ignore) {
        }
        logger.info("[WL-WS] close session={}", safeId(session));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        logger.warn("[WL-WS] error session={}", safeId(session), t);
    }

    private ParseResult parseCodes(Session session) {
        if (session == null) {
            return new ParseResult(null, null, Collections.<String>emptySet());
        }

        Map<String, List<String>> pm = session.getRequestParameterMap();
        if (pm == null) {
            return new ParseResult(null, null, Collections.<String>emptySet());
        }

        String raw = first(pm, "codes");
        if (raw == null || raw.trim().isEmpty()) {
            return new ParseResult(raw, raw, Collections.<String>emptySet());
        }

        String decoded = raw;
        try {
            decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
        } catch (Exception ignore) {
            decoded = raw;
        }

        Set<String> out = new LinkedHashSet<String>();
        String[] arr = decoded.split(",");
        for (String a : arr) {
            if (a == null) {
                continue;
            }
            String c = a.trim();
            if (!c.isEmpty()) {
                out.add(c);
            }
        }

        return new ParseResult(raw, decoded, out);
    }

    private String first(Map<String, List<String>> pm, String key) {
        if (pm == null || key == null) {
            return null;
        }
        List<String> v = pm.get(key);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return v.get(0);
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

    private String safeId(Session session) {
        try {
            return session == null ? "" : session.getId();
        } catch (Exception e) {
            return "";
        }
    }

    private static class ParseResult {
        private final String raw;
        private final String decoded;
        private final Set<String> tokens;

        private ParseResult(String raw, String decoded, Set<String> tokens) {
            this.raw = raw;
            this.decoded = decoded;
            this.tokens = tokens == null ? Collections.<String>emptySet() : tokens;
        }
    }

    public static class ErrorPayload {
        public String type;
        public String message;
    }
}
