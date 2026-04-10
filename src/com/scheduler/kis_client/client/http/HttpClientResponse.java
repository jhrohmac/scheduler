package com.scheduler.kis_client.client.http;

import java.util.List;
import java.util.Map;

import com.scheduler.kis_client.client.NetworkResponse;

/**
 * Lombok(@Data, @Builder) 제거 버전 HttpClientResponse.
 */
public class HttpClientResponse implements NetworkResponse {

    private int statusCode;
    private Map<String, List<String>> headers;
    private String body;

    public HttpClientResponse() {
    }

    private HttpClientResponse(int statusCode,
                               Map<String, List<String>> headers,
                               String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int statusCode;
        private Map<String, List<String>> headers;
        private String body;

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpClientResponse build() {
            return new HttpClientResponse(statusCode, headers, body);
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
