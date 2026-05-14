package com.scheduler.finance.kis.quote;

/**
 * 화면/서버 내부 market 명칭과 KIS REST 시장코드를 한 곳에서 변환한다.
 */
public final class KisMarketCode {
    private KisMarketCode() {
    }

    public static String normalizeDomesticMarket(String market) {
        String m = safe(market).toUpperCase();
        if (m.isEmpty() || "J".equals(m) || "KRX".equals(m)) {
            return "KRX";
        }
        if ("NX".equals(m) || "NXT".equals(m)) {
            return "NXT";
        }
        if ("UN".equals(m) || "UNI".equals(m) || "INTEGRATED".equals(m)) {
            return "UN";
        }
        if ("OVERTIME".equals(m) || "AFTER".equals(m) || "AFTER_HOURS".equals(m) || "AFTERHOURS".equals(m)) {
            return "OVERTIME";
        }
        if ("W".equals(m) || "ELW".equals(m)) {
            return "ELW";
        }
        return m;
    }

    public static String toDomesticRestCode(String market) {
        String m = normalizeDomesticMarket(market);
        if ("NXT".equals(m)) {
            return "NX";
        }
        if ("UN".equals(m)) {
            return "UN";
        }
        if ("ELW".equals(m)) {
            return "W";
        }
        return "J";
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
