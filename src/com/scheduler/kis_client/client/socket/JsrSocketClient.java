package com.scheduler.kis_client.client.socket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.kis_client.api.RealTimeApiData;
import com.scheduler.kis_client.api.realtime.TransactionType;
import com.scheduler.kis_client.config.Configuration;
import com.scheduler.kis_client.context.ApiContext;
import com.scheduler.kis_client.context.ApiData;
import com.scheduler.kis_client.exception.KisClientException;
import com.scheduler.kis_client.util.CollectionUtil;
import com.scheduler.kis_client.util.JsonUtil;
import com.scheduler.kis_client.util.ReflectionUtil;
import com.scheduler.kis_client.util.StringUtil;

final class Key {
    private final String trId;
    private final String trKey;

    public Key(String trId, String trKey) {
        this.trId = trId;
        this.trKey = trKey;
    }

    public String getTrId() {
        return trId;
    }

    public String getTrKey() {
        return trKey;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trId, trKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Key other = (Key) obj;
        return Objects.equals(this.trId, other.trId) && Objects.equals(this.trKey, other.trKey);
    }

    @Override
    public String toString() {
        return "Key{" + String.valueOf(trId) + "," + String.valueOf(trKey) + "}";
    }
}

/**
 * KIS 실시간(WebSocket) 클라이언트
 *
 * 한국투자증권 오픈API 문서(엑셀) 기준:
 * - 요청(JSON)
 *   {"header":{...,"tr_type":"1"}, "body":{"input":{"tr_id":"H0STCNT0","tr_key":"005930"}}}
 * - 응답(JSON)
 *   body.msg_cd: OPSP0000(등록), OPSP0001(해제)
 * - 실시간 데이터(문자열)
 *   0|H0STCNT0|004|005930^...^005930^... (|로 구분, 마지막은 ^로 구분)
 *   첫 값: 0(평문) / 1(암호화)
 */
@ClientEndpoint
public class JsrSocketClient extends SocketClient {

    private static final Logger logger = LoggerFactory.getLogger(JsrSocketClient.class);

    private static final String MSG_CD_SUB_OK = "OPSP0000";
    private static final String MSG_CD_UNSUB_OK = "OPSP0001";

    private final Configuration config;
    private final WebSocketContainer container;
    private final URI uri;

    private volatile Session session;

    private volatile String aesKey;
    private volatile String ivKey;

    /**
     * 등록/해제 응답 대기 Future
     * - 키: (tr_id, tr_key)
     */
    private final ConcurrentHashMap<Key, CompletableFuture<WebSocketResponse>> locks = new ConcurrentHashMap<>();

    /**
     * 데이터 수신 디스패치용 구독자 정보
     * - 키: (tr_id, tr_key)
     */
    private final ConcurrentHashMap<Key, SubscriberInfo<?>> subscribers = new ConcurrentHashMap<>();

    class SubscriberInfo<T extends RealTimeApiData> {
        private final SubscribableApiResult<T> apiResult;
        private final List<Consumer<T[]>> handlers;
        private final Class<T> dataType;

        @SuppressWarnings("unchecked")
        public SubscriberInfo(SubscribableApiResult<T> apiResult, List<Consumer<?>> handlers, Class<? extends RealTimeApiData> dataType) {
            this.apiResult = apiResult;
            this.handlers = (List<Consumer<T[]>>)(List<?>)handlers;
            this.dataType = (Class<T>)dataType;
        }

        public SubscribableApiResult<T> getApiResult() {
            return apiResult;
        }

        public List<Consumer<T[]>> getHandlers() {
            return handlers;
        }

        public Class<T> getDataType() {
            return dataType;
        }

        public int getFieldCount() {
            return ReflectionUtil.getSortedSeqFields(dataType).size();
        }
    }

    public JsrSocketClient(Configuration config) {
        this.config = config;
        this.container = ContainerProvider.getWebSocketContainer();
        this.uri = URI.create(config.getSocketHost());
    }

    /**
     * ApiParser(CommonRealTimeApi)는 body를 {tr_id, tr_key} 평면으로 만든다.
     * 문서 규격은 body.input.{tr_id,tr_key} 이므로 여기서 래핑한다.
     */
    @Override
    public WebSocketRequest makeRequest(ApiData apiData) {
        String apiHost = config.getHttpHost();
        String urlPath = apiData.getUrlPath();
        String url = StringUtil.concatUrl(apiHost, urlPath);

        WebSocketRequest request = new WebSocketRequest();
        request.setUrl(url);
        request.setHeaders(apiData.getHeaders());

        Map<String, Object> body = apiData.getBody();
        if (body == null) {
            body = new HashMap<String, Object>();
        }

        // 이미 input 구조면 그대로, 아니면 input으로 래핑
        if (body.containsKey("input") && body.get("input") instanceof Map) {
            request.setBody(body);
        } else {
            Map<String, Object> wrapped = new HashMap<String, Object>();
            wrapped.put("input", body);
            request.setBody(wrapped);
        }

        return request;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("[KIS-WS] connected");
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        logger.warn("[KIS-WS] closed: {} / {}", reason.getCloseCode(), reason.getReasonPhrase());

        // 세션 종료 시, 대기 중인 요청들을 즉시 실패 처리(Timeout 연쇄 방지)
        failAllPending(new KisClientException("WebSocket closed: " + reason));

        this.session = null;
    }

    @OnMessage
    public void onMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        try {
            if (isDataResponse(message)) {
                processDataMessage(message);
                return;
            }

            processStateMessage(message);
        } catch (Exception e) {
            // onMessage에서 예외 throw하면 Tyrus가 endpoint를 불안정하게 만들 수 있음
            logger.error("[KIS-WS] onMessage error", e);
        }
    }

    private void ensureConnected() {
        Session s = this.session;
        if (s != null && s.isOpen()) {
            return;
        }

        synchronized (this) {
            s = this.session;
            if (s != null && s.isOpen()) {
                return;
            }

            try {
                logger.info("[KIS-WS] connecting...");
                container.connectToServer(this, uri);
            } catch (Exception e) {
                throw new KisClientException("WebSocket connection failed", e);
            }
        }
    }

    private void safeClose() {
        try {
            Session s = this.session;
            if (s != null) {
                s.close();
            }
        } catch (Exception ignore) {
        } finally {
            this.session = null;
        }
    }

    private boolean sendRaw(String message) {
        ensureConnected();

        try {
            Session s = this.session;
            if (s == null || !s.isOpen()) {
                return false;
            }

            s.getBasicRemote().sendText(message);
            return true;
        } catch (IOException e) {
            logger.error("[KIS-WS] send failed", e);
            safeClose();
            return false;
        }
    }

    private boolean isDataResponse(String message) {
        return message.charAt(0) != '{';
    }

    private void processStateMessage(String message) {
        WebSocketResponse response;
        try {
            response = JsonUtil.fromJson(message, WebSocketResponse.class);
        } catch (Exception e) {
            logger.warn("[KIS-WS] invalid json response: {}", message);
            return;
        }

        String trId = response.getHeader("tr_id");
        if ("PINGPONG".equals(trId)) {
            // 문서/샘플: 서버가 PINGPONG을 주면 그대로 echo
            sendRaw(message);
            return;
        }

        String msgCd = (response.getBody() == null) ? null : response.getBody().getMsgCd();
        String rtCd = (response.getBody() == null) ? null : response.getBody().getRtCd();

        String trKey = response.getHeader("tr_key");

        // 정상 응답 키를 찾기 어려운 경우(예: tr_id=null) -> 대기 요청 전체 실패
        if (isBlank(trId) || isBlank(trKey)) {
            if (!"0".equals(rtCd) || (msgCd != null && !MSG_CD_SUB_OK.equals(msgCd) && !MSG_CD_UNSUB_OK.equals(msgCd))) {
                logger.error("[KIS-WS] failure response without key: {}", response);
                failAllPending(new KisClientException("Failure response without key: " + response));
                safeClose();
            } else {
                logger.warn("[KIS-WS] response without key: {}", response);
            }
            return;
        }

        Key key = new Key(trId, trKey);

        // 성공 케이스
        if (MSG_CD_SUB_OK.equals(msgCd) || MSG_CD_UNSUB_OK.equals(msgCd) || "0".equals(rtCd)) {
            setEncryptionKeysIfPresent(response);
            completeSuccess(key, response);
            return;
        }

        // 실패 케이스
        logger.error("[KIS-WS] server failure: {}", response);
        completeFailure(key, new KisClientException("Server failure: " + response));
    }

    private void setEncryptionKeysIfPresent(WebSocketResponse response) {
        if (response == null || response.getBody() == null) {
            return;
        }

        Map<String, String> out = response.getBody().getOutput();
        if (out == null) {
            return;
        }

        String key = out.get("key");
        String iv = out.get("iv");

        if (!isBlank(key) && !isBlank(iv)) {
            this.aesKey = key;
            this.ivKey = iv;
        }
    }

    private void completeSuccess(Key key, WebSocketResponse response) {
        CompletableFuture<WebSocketResponse> future = locks.remove(key);
        if (future != null) {
            future.complete(response);
        } else {
            logger.debug("[KIS-WS] state response for unknown key: {}", key);
        }
    }

    private void completeFailure(Key key, RuntimeException ex) {
        CompletableFuture<WebSocketResponse> future = locks.remove(key);
        if (future != null) {
            future.completeExceptionally(ex);
        }
    }

    private void failAllPending(RuntimeException ex) {
        for (Map.Entry<Key, CompletableFuture<WebSocketResponse>> e : locks.entrySet()) {
            try {
                e.getValue().completeExceptionally(ex);
            } catch (Exception ignore) {
            }
        }
        locks.clear();
    }

    private void processDataMessage(String message) {
        // 형식: 0|H0STCNT0|004|payload
        int i1 = message.indexOf('|');
        int i2 = message.indexOf('|', i1 + 1);
        int i3 = message.indexOf('|', i2 + 1);

        if (i1 < 0 || i2 < 0 || i3 < 0) {
            logger.warn("[KIS-WS] invalid data frame: {}", message);
            return;
        }

        boolean encrypted = "1".equals(message.substring(0, i1));
        String trId = message.substring(i1 + 1, i2);
        int recordCount;

        try {
            recordCount = Integer.parseInt(message.substring(i2 + 1, i3));
        } catch (Exception e) {
            logger.warn("[KIS-WS] invalid recordCount: {}", message);
            return;
        }

        String payload = message.substring(i3 + 1);

        // trId 기준으로 fieldCount를 얻기 위해, 현재 등록된 subscriber 중 아무거나 가져온다.
        // (동일 trId는 fieldCount가 동일)
        SubscriberInfo<?> any = findAnySubscriberByTrId(trId);
        if (any == null) {
            logger.debug("[KIS-WS] no subscriber for trId={}, drop", trId);
            return;
        }

        int fieldCount = any.getFieldCount();
        if (fieldCount <= 0) {
            logger.warn("[KIS-WS] invalid fieldCount for trId={}, drop", trId);
            return;
        }

        String[][] rows = parseRows(payload, encrypted, recordCount, fieldCount);
        if (rows == null || rows.length == 0) {
            return;
        }

        // 첫 컬럼(0) = tr_key(종목코드) 기준으로 그룹핑(문서 기준)
        Map<String, List<String[]>> byKey = new HashMap<String, List<String[]>>();
        for (String[] r : rows) {
            if (r == null || r.length == 0) {
                continue;
            }
            String trKey = r[0];
            if (isBlank(trKey)) {
                continue;
            }
            byKey.computeIfAbsent(trKey, k -> new ArrayList<String[]>()).add(r);
        }

        for (Map.Entry<String, List<String[]>> entry : byKey.entrySet()) {
            String trKey = entry.getKey();
            Key key = new Key(trId, trKey);

            SubscriberInfo<?> sub = subscribers.get(key);
            if (sub == null) {
                // 일부 메시지는 같은 trId라도 여러 채널이 있을 수 있어 warn 대신 debug
                logger.debug("[KIS-WS] no subscriber for key={}, drop", key);
                continue;
            }

            String[][] grouped = entry.getValue().toArray(new String[0][]);
            dispatch(sub, grouped);
        }
    }

    private SubscriberInfo<?> findAnySubscriberByTrId(String trId) {
        for (Map.Entry<Key, SubscriberInfo<?>> e : subscribers.entrySet()) {
            Key k = e.getKey();
            if (k != null && trId != null && trId.equals(k.getTrId())) {
                return e.getValue();
            }
        }
        return null;
    }

    private String[][] parseRows(String payload, boolean encrypted, int recordCount, int fieldCount) {
        if (encrypted) {
            if (isBlank(aesKey) || isBlank(ivKey)) {
                logger.warn("[KIS-WS] encrypted payload but no key/iv");
                return null;
            }

            payload = StringUtil.decryptAes256(payload, aesKey, ivKey);
        }

        String[] tokens = StringUtil.fastSplit(payload, '^');

        int expected = recordCount * fieldCount;
        if (tokens.length < expected) {
            // 페이징/분할 전송이 있을 수 있으므로, 최소 기준만 맞추고 가능한 만큼 처리
            logger.warn("[KIS-WS] token shortage: tokens={}, expected={} (recordCount={}, fieldCount={})", tokens.length, expected, recordCount, fieldCount);
        }

        // tokens를 fieldCount 단위로 끊어서 row 구성 (recordCount는 참고용)
        if (tokens.length % fieldCount != 0) {
            logger.warn("[KIS-WS] token not aligned by fieldCount: tokens={}, fieldCount={} payload={}", tokens.length, fieldCount, payload);
            return null;
        }

        return CollectionUtil.splitArray(tokens, fieldCount);
    }

    private <T extends RealTimeApiData> void dispatch(SubscriberInfo<?> subAny, String[][] body) {
        @SuppressWarnings("unchecked")
        SubscriberInfo<T> sub = (SubscriberInfo<T>) subAny;

        Class<T> dataType = sub.getDataType();
        T[] data = ReflectionUtil.createObjects(body, dataType);

        for (Consumer<T[]> handler : sub.getHandlers()) {
            try {
                handler.accept(data);
            } catch (Exception e) {
                logger.warn("[KIS-WS] handler error", e);
            }
        }
    }

    @Override
    public void execute(ApiContext context) throws IOException {
        ApiData apiData = context.getApiData();

        WebSocketRequest request = (WebSocketRequest) context.getRequest();
        WebSocketRequest unsubscribeRequest = request.clone();

        // body.input 에서 tr_id/tr_key 읽기
        Map<String, Object> body = request.getBody();
        Map<String, Object> input = getInput(body);

        String trId = String.valueOf(input.get("tr_id"));
        String trKey = String.valueOf(input.get("tr_key"));

        if (isBlank(trId) || isBlank(trKey)) {
            throw new KisClientException("Invalid websocket request: tr_id/tr_key missing");
        }

        Key key = new Key(trId, trKey);

        @SuppressWarnings("unchecked")
        Class<SubscribableApiResult<?>> responseClass = (Class<SubscribableApiResult<?>>) apiData.getResponseClass();
        if (responseClass == null) {
            throw new KisClientException("Response class not found for trId=" + trId);
        }

        SubscriberInfo<?> existed = subscribers.get(key);
        if (existed != null) {
            context.setApiResult(existed.getApiResult());
            return;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        SubscriberInfo<?> subscriber = subscribers.computeIfAbsent(key, k -> {
            WebSocketResponse response = sendAndWait(key, request);

            try {
                SubscribableApiResult<?> apiResult = responseClass.getDeclaredConstructor().newInstance();

                apiResult.setUnsubscribeCallback((UnsubscribeCallback) (source) -> {
                    if (!subscribers.containsKey(key)) {
                        return;
                    }

                    // 문서: tr_type "2" = 해제
                    unsubscribeRequest.getHeaders().put("tr_type", TransactionType.UNSUBSCRIBE);

                    try {
                        sendAndWait(key, unsubscribeRequest);
                    } catch (Exception e) {
                        logger.warn("[KIS-WS] unsubscribe failed: {}", key, e);
                    } finally {
                        subscribers.remove(key);
                    }
                });

                Class<? extends RealTimeApiData> dataType = ReflectionUtil.getGenericParameterType(apiResult);
                List<Consumer<?>> handlersList = (List<Consumer<?>>)(List<?>) apiResult.getAllHandlers();

                // 키/IV 저장
                setEncryptionKeysIfPresent(response);
                context.setResponse(response);

                return new SubscriberInfo(apiResult, handlersList, dataType);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        context.setApiResult(subscriber.getApiResult());
    }

    private WebSocketResponse sendAndWait(Key key, WebSocketRequest request) {
        CompletableFuture<WebSocketResponse> future = new CompletableFuture<WebSocketResponse>();

        CompletableFuture<WebSocketResponse> prev = locks.putIfAbsent(key, future);
        if (prev != null) {
            // 같은 키로 이미 대기 중이면 기존 future 재사용
            future = prev;
        } else {
            boolean ok = sendRaw(request.toJson());
            if (!ok) {
                locks.remove(key);
                throw new KisClientException("Failed to send request");
            }
        }

        try {
            return future.get(config.getSocketTimeout().toSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            locks.remove(key);
            throw new KisClientException("Interrupted", e);
        } catch (ExecutionException e) {
            locks.remove(key);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new KisClientException("Execution error", e);
        } catch (TimeoutException e) {
            locks.remove(key);
            throw new KisClientException("Timeout waiting websocket response", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getInput(Map<String, Object> body) {
        if (body == null) {
            return new HashMap<String, Object>();
        }

        Object input = body.get("input");
        if (input instanceof Map) {
            return (Map<String, Object>) input;
        }

        // 안전망: input 구조가 아니면 body 자체를 input처럼 취급
        return body;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || "null".equalsIgnoreCase(s.trim());
    }

    private void shutdownContainerQuietly() {
        invokeContainerMethod("shutdown");
        invokeContainerMethod("close");
    }

    private void invokeContainerMethod(String methodName) {
        if (container == null || isBlank(methodName)) {
            return;
        }

        try {
            Method method = container.getClass().getMethod(methodName);
            method.setAccessible(true);
            method.invoke(container);
        } catch (NoSuchMethodException ignore) {
        } catch (Exception e) {
            logger.debug("[KIS-WS] container {} failed", methodName, e);
        }
    }

    @Override
    public void close() throws IOException {
        failAllPending(new KisClientException("WebSocket client closing"));
        subscribers.clear();
        aesKey = null;
        ivKey = null;
        safeClose();
        shutdownContainerQuietly();
    }

    @Override
    public int hashCode() {
        Session s = this.session;
        return (s == null) ? -1 : s.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        JsrSocketClient other = (JsrSocketClient) obj;
        return this.session == other.session;
    }
}
