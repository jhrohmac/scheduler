package com.scheduler.kis_client.context;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

import com.scheduler.kis_client.api.ApiResult;
import com.scheduler.kis_client.util.AnnotationUtil;

/**
 * Api 클래스에서 추출한 메타 정보를 담는 객체
 *  - headers / parameters / body
 *  - HTTP method, urlPath
 *  - 응답 타입(responseClass)
 *  - 애노테이션 정보(annotations)
 */
public class ApiData implements Cloneable {

    private Map<String, Object> body;
    private Map<String, Object> headers;
    private Map<String, Object> parameters;

    private String method;
    private String urlPath;
    private Class<? extends ApiResult> responseClass;

    private Annotation[] annotations;

    public ApiData() {
        this.body = new LinkedHashMap<String, Object>();
        this.headers = new LinkedHashMap<String, Object>();
        this.parameters = new LinkedHashMap<String, Object>();
        this.annotations = new Annotation[0];
    }

    private ApiData(Builder builder) {
        this.body = builder.body != null ? new LinkedHashMap<String, Object>(builder.body)
                                         : new LinkedHashMap<String, Object>();
        this.headers = builder.headers != null ? new LinkedHashMap<String, Object>(builder.headers)
                                               : new LinkedHashMap<String, Object>();
        this.parameters = builder.parameters != null ? new LinkedHashMap<String, Object>(builder.parameters)
                                                     : new LinkedHashMap<String, Object>();
        this.method = builder.method;
        this.urlPath = builder.urlPath;
        this.responseClass = builder.responseClass;
        this.annotations = builder.annotations != null ? builder.annotations : new Annotation[0];
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, Object> body = new LinkedHashMap<String, Object>();
        private Map<String, Object> headers = new LinkedHashMap<String, Object>();
        private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

        private String method;
        private String urlPath;
        private Class<? extends ApiResult> responseClass;

        private Annotation[] annotations = new Annotation[0];

        public Builder body(Map<String, Object> body) {
            this.body = body;
            return this;
        }

        public Builder headers(Map<String, Object> headers) {
            this.headers = headers;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder urlPath(String urlPath) {
            this.urlPath = urlPath;
            return this;
        }

        public Builder responseClass(Class<? extends ApiResult> responseClass) {
            this.responseClass = responseClass;
            return this;
        }

        public Builder annotations(Annotation[] annotations) {
            this.annotations = annotations;
            return this;
        }

        public ApiData build() {
            return new ApiData(this);
        }
    }

    /**
     * Api 클래스에 특정 애노테이션이 붙어 있는지 여부
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        if (annotations == null) {
            return false;
        }
        return AnnotationUtil.contains(annotations, annotationClass);
    }

    @Override
    public ApiData clone() {
        try {
            ApiData cloned = (ApiData) super.clone();
            cloned.body = new LinkedHashMap<String, Object>(this.body);
            cloned.headers = new LinkedHashMap<String, Object>(this.headers);
            cloned.parameters = new LinkedHashMap<String, Object>(this.parameters);
            if (this.annotations != null) {
                cloned.annotations = this.annotations.clone();
            } else {
                cloned.annotations = new Annotation[0];
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone ApiData", e);
        }
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public Class<? extends ApiResult> getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Class<? extends ApiResult> responseClass) {
        this.responseClass = responseClass;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }
}
