package com.scheduler.kis_client.client.http;

public final class KisRateLimiter {

    private static final Object LOCK = new Object();
    private static long lastCallAt = 0L;

    private KisRateLimiter() {}

    public static void throttle() throws InterruptedException {
        long minIntervalMs = getLong("kis.rate.minIntervalMs", 120L);
        if (minIntervalMs <= 0) return;

        synchronized (LOCK) {
            long now = System.currentTimeMillis();
            long wait = (lastCallAt + minIntervalMs) - now;
            if (wait > 0) sleep(wait);
            lastCallAt = System.currentTimeMillis();
        }
    }

    public static int maxRetries() {
        return (int) getLong("kis.rate.maxRetries", 5L);
    }

    public static void backoffSleep(int attempt) throws InterruptedException {
        long base = getLong("kis.rate.backoffBaseMs", 300L);
        long max = getLong("kis.rate.backoffMaxMs", 3000L);

        long ms = base * (1L << Math.max(0, attempt));
        if (ms > max) ms = max;
        sleep(ms);
    }

    public static boolean isRateLimitResponse(int statusCode, String body) {
        if (body == null) return false;
        if (body.contains("EGW00201")) return true;
        if (body.contains("초당 거래건수를 초과")) return true;
        return false;
    }

    private static long getLong(String key, long def) {
        try {
            String v = System.getProperty(key);
            if (v == null || v.trim().isEmpty()) return def;
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
