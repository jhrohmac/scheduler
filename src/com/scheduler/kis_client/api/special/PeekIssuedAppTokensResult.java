package com.scheduler.kis_client.api.special;

import java.time.LocalDateTime;
import java.util.Map;

import com.scheduler.kis_client.api.ApiResult;

public class PeekIssuedAppTokensResult implements ApiResult {

    private Map<String, AppToken> appTokens;

    public Map<String, AppToken> getAppTokens() {
        return appTokens;
    }

    public void setAppTokens(Map<String, AppToken> appTokens) {
        this.appTokens = appTokens;
    }

    public static class AppToken {

        private String appToken;
        private LocalDateTime appTokenExpiredAt;

        public AppToken(String appToken, LocalDateTime appTokenExpiredAt) {
            this.appToken = appToken;
            this.appTokenExpiredAt = appTokenExpiredAt;
        }

        public String getAppToken() {
            return appToken;
        }

        public void setAppToken(String appToken) {
            this.appToken = appToken;
        }

        public LocalDateTime getAppTokenExpiredAt() {
            return appTokenExpiredAt;
        }

        public void setAppTokenExpiredAt(LocalDateTime appTokenExpiredAt) {
            this.appTokenExpiredAt = appTokenExpiredAt;
        }
    }
}
