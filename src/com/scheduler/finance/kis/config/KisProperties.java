package com.scheduler.finance.kis.config;

import java.util.Properties;

/**
 * kis.properties 파일의 내용을 보관하는 설정 객체.
 *
 * 사용되는 키:
 *  - kis.env
 *  - kis.http.host
 *  - kis.socket.host
 *  - kis.app.key
 *  - kis.app.secret
 *  - kis.app.tokenKey          (선택)
 *  - kis.app.tokenExpiredAt    (선택, String - KIS에서 내려주는 형식)
 *  - kis.account.no
 *  - kis.account.product.code
 *  - kis.rest.limit.per.second
 *  - kis.socket.limit.per.second
 *  - kis.watchlist.quote.period.sec
 *  - kis.watchlist.quote.fetch.pool.size
 *  - kis.watchlist.quote.max.tokens.per.cycle
 *  - kis.ws.max.subscriptions
 *  - kis.ws.subscribe.min.interval.ms
 *  - kis.ws.unsubscribe.min.interval.ms
 *  - kis.watchlist.domestic.realtime.enabled
 */
public class KisProperties {

    private final String env;
    private final String httpHost;
    private final String websocketHost;

    private final String appKey;
    private final String appSecret;

    // 토큰 / 만료시간 (선택값)
    private String appTokenKey;
    private String appTokenExpiredAt;

    private final String accountNo;
    private final String accountProductCode;

    private final int restLimitPerSecond;
    private final int socketLimitPerSecond;
    private final int watchlistQuotePeriodSec;
    private final int watchlistQuotePeriodMs;
    private final int watchlistQuoteFetchPoolSize;
    private final int watchlistQuoteMaxTokensPerCycle;
    private final int wsMaxSubscriptions;
    private final int wsSubscribeMinIntervalMs;
    private final int wsUnsubscribeMinIntervalMs;
    private final boolean watchlistDomesticRealtimeEnabled;

    public KisProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("KisProperties 생성 시 properties 가 null 입니다.");
        }

        this.env = getOrDefault(properties, "kis.env", "PROD");
        this.httpHost = getOrDefault(properties, "kis.http.host",
                "https://openapi.koreainvestment.com:9443");
        this.websocketHost = getOrDefault(properties, "kis.socket.host",
                "ws://ops.koreainvestment.com:21000");

        this.appKey = getRequired(properties, "kis.app.key");
        this.appSecret = getRequired(properties, "kis.app.secret");

        // 최초에는 없을 수 있으므로 선택값 처리
        this.appTokenKey = getOrDefault(properties, "kis.app.tokenKey", null);
        this.appTokenExpiredAt = getOrDefault(properties, "kis.app.tokenExpiredAt", null);

        this.accountNo = getOrDefault(properties, "kis.account.no", "");
        this.accountProductCode = getOrDefault(properties, "kis.account.product.code", "");

        this.restLimitPerSecond = getInt(properties, "kis.rest.limit.per.second", 20);
        this.socketLimitPerSecond = getInt(properties, "kis.socket.limit.per.second", 10);
        this.watchlistQuotePeriodSec = getInt(properties, "kis.watchlist.quote.period.sec", 5);

        // Try millisecond config first, fall back to seconds * 1000 for backward compatibility
        int periodMs = getInt(properties, "kis.watchlist.quote.period.ms", -1);
        if (periodMs < 0) {
            // Fallback: convert existing seconds config to milliseconds
            this.watchlistQuotePeriodMs = this.watchlistQuotePeriodSec * 1000;
        } else {
            this.watchlistQuotePeriodMs = periodMs;
        }

        this.watchlistQuoteFetchPoolSize = getInt(properties, "kis.watchlist.quote.fetch.pool.size", 4);
        this.watchlistQuoteMaxTokensPerCycle = getInt(properties, "kis.watchlist.quote.max.tokens.per.cycle", 4);
        this.wsMaxSubscriptions = getInt(properties, "kis.ws.max.subscriptions", 40);
        this.wsSubscribeMinIntervalMs = getInt(properties, "kis.ws.subscribe.min.interval.ms", 100);
        this.wsUnsubscribeMinIntervalMs = getInt(properties, "kis.ws.unsubscribe.min.interval.ms", 100);
        this.watchlistDomesticRealtimeEnabled = getBool(properties, "kis.watchlist.domestic.realtime.enabled", false);
    }

    private static String getRequired(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required KIS property is missing: " + key);
        }
        return value.trim();
    }

    private static String getOrDefault(Properties props, String key, String defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static int getInt(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean getBool(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        String v = value.trim().toLowerCase();
        if ("true".equals(v) || "1".equals(v) || "y".equals(v) || "yes".equals(v)) {
            return true;
        }
        if ("false".equals(v) || "0".equals(v) || "n".equals(v) || "no".equals(v)) {
            return false;
        }
        return defaultValue;
    }

    public String getEnv() {
        return env;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public String getWebsocketHost() {
        return websocketHost;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public String getAppTokenKey() {
        return appTokenKey;
    }

    public void setAppTokenKey(String appTokenKey) {
        this.appTokenKey = appTokenKey;
    }

    public String getAppTokenExpiredAt() {
        return appTokenExpiredAt;
    }

    public void setAppTokenExpiredAt(String appTokenExpiredAt) {
        this.appTokenExpiredAt = appTokenExpiredAt;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getAccountProductCode() {
        return accountProductCode;
    }

    public int getRestLimitPerSecond() {
        return restLimitPerSecond;
    }

    public int getSocketLimitPerSecond() {
        return socketLimitPerSecond;
    }

    public int getWatchlistQuotePeriodSec() {
        return watchlistQuotePeriodSec;
    }

    public int getWatchlistQuotePeriodMs() {
        return watchlistQuotePeriodMs;
    }

    public int getWatchlistQuoteFetchPoolSize() {
        return watchlistQuoteFetchPoolSize;
    }

    public int getWatchlistQuoteMaxTokensPerCycle() {
        return watchlistQuoteMaxTokensPerCycle;
    }

    public int getWsMaxSubscriptions() {
        return wsMaxSubscriptions;
    }

    public int getWsSubscribeMinIntervalMs() {
        return wsSubscribeMinIntervalMs;
    }

    public int getWsUnsubscribeMinIntervalMs() {
        return wsUnsubscribeMinIntervalMs;
    }

    public boolean isWatchlistDomesticRealtimeEnabled() {
        return watchlistDomesticRealtimeEnabled;
    }

    @Override
    public String toString() {
        return "KisProperties{" +
                "env='" + env + '\'' +
                ", httpHost='" + httpHost + '\'' +
                ", websocketHost='" + websocketHost + '\'' +
                ", appKey='****'" +
                ", appSecret='****'" +
                ", appTokenKey=" + (appTokenKey != null ? "****" : "null") +
                ", appTokenExpiredAt='" + appTokenExpiredAt + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", accountProductCode='" + accountProductCode + '\'' +
                ", restLimitPerSecond=" + restLimitPerSecond +
                ", socketLimitPerSecond=" + socketLimitPerSecond +
                ", watchlistQuotePeriodSec=" + watchlistQuotePeriodSec +
                ", watchlistQuotePeriodMs=" + watchlistQuotePeriodMs +
                ", watchlistQuoteFetchPoolSize=" + watchlistQuoteFetchPoolSize +
                ", watchlistQuoteMaxTokensPerCycle=" + watchlistQuoteMaxTokensPerCycle +
                ", wsMaxSubscriptions=" + wsMaxSubscriptions +
                ", wsSubscribeMinIntervalMs=" + wsSubscribeMinIntervalMs +
                ", wsUnsubscribeMinIntervalMs=" + wsUnsubscribeMinIntervalMs +
                ", watchlistDomesticRealtimeEnabled=" + watchlistDomesticRealtimeEnabled +
                '}';
    }
}
