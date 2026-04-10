package com.scheduler.kis_client.client.http;

import java.util.Map;

import com.scheduler.kis_client.client.NetworkRequest;

/**
 * Lombok(@Data, @Builder) 제거 버전 HttpClientRequest.
 */
public class HttpClientRequest implements NetworkRequest {

    private String method;
    private String url;
    private Map<String, Object> parameters;
    private Map<String, Object> headers;
    private Map<String, Object> body;

    public HttpClientRequest() {
    }

    private HttpClientRequest(
            String method,
            String url,
            Map<String, Object> parameters,
            Map<String, Object> headers,
            Map<String, Object> body) {
        this.method = method;
        this.url = url;
        this.parameters = parameters;
        this.headers = headers;
        this.body = body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String method;
        private String url;
        private Map<String, Object> parameters;
        private Map<String, Object> headers;
        private Map<String, Object> body;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder headers(Map<String, Object> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(Map<String, Object> body) {
            this.body = body;
            return this;
        }

        public HttpClientRequest build() {
            return new HttpClientRequest(method, url, parameters, headers, body);
        }
    }

    public String getMethod() {
        return method != null ? method.toUpperCase() : null;
    }

    public void setMethod(String method) {
        this.method = method;
    }

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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
