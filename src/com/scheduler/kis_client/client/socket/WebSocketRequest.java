package com.scheduler.kis_client.client.socket;

import java.util.HashMap;
import java.util.Map;

import com.scheduler.kis_client.client.NetworkRequest;
import com.scheduler.kis_client.util.JsonUtil;

public class WebSocketRequest implements NetworkRequest, Cloneable {

    private String url;
    private Map<String, Object> headers;
    private Map<String, Object> body;

    public WebSocketRequest() {
    }

    public WebSocketRequest(String url, Map<String, Object> headers, Map<String, Object> body) {
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    // ========== NetworkRequest 구현부 ==========

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Map<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public Map<String, Object> getBody() {
        return body;
    }

    @Override
    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    // ========== JSON 변환 (웹소켓 전송용) ==========

    /**
     * KIS 실시간 웹소켓이 요구하는 형태대로
     * header / body 를 JSON으로 감싸서 전송하는 메서드.
     *
     * 대략 이런 구조:
     * {
     *   "header": { ... },
     *   "body":   { ... }
     * }
     */
    public String toJson() {
        Map<String, Object> map = new HashMap<String, Object>();

        if (headers != null) {
            map.put("header", headers);
        }
        if (body != null) {
            map.put("body", body);
        }

        return JsonUtil.toJson(map);
    }

    // ========== clone (구독/해지 요청용 복제) ==========

    /**
     * unsubscribe 요청을 만들 때 사용.
     * url 은 그대로 두고, header/body 만 복사해서 새 인스턴스를 만든다.
     */
    public WebSocketRequest clone() {
        WebSocketRequest copy = new WebSocketRequest();
        copy.setUrl(this.url);

        if (this.headers != null) {
            copy.setHeaders(new HashMap<String, Object>(this.headers));
        }

        if (this.body != null) {
            copy.setBody(new HashMap<String, Object>(this.body));
        }

        return copy;
    }
}
