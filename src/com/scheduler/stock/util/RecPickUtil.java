package com.scheduler.stock.util;

import java.util.List;

import com.scheduler.stock.dto.DlyPriceDto;

/**
 * 추천 종목 추적 공통 유틸리티
 *
 * RecPickService / RecPickTrackService / PositionExitSignalService 에서
 * 중복 정의된 헬퍼 메서드를 통합한 유틸 클래스.
 * 모든 메서드는 static — 인스턴스 불필요.
 */
public final class RecPickUtil {

    private RecPickUtil() {}

    /* ──────────────────────────────────────────
       문자열 헬퍼
    ────────────────────────────────────────── */

    public static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static String defaultString(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : trim(value);
    }

    /**
     * yyyyMMdd 또는 yyyy-MM-dd 를 yyyy-MM-dd 로 정규화.
     * 길이 10 이상이면 앞 10자 사용.
     */
    public static String normalizeDate(String value) {
        if (isBlank(value)) return value;
        String v = value.trim();
        if (v.length() >= 10) return v.substring(0, 10);
        if (v.length() == 8 && v.indexOf('-') < 0) {
            return v.substring(0, 4) + "-" + v.substring(4, 6) + "-" + v.substring(6, 8);
        }
        return v;
    }

    /* ──────────────────────────────────────────
       숫자 헬퍼
    ────────────────────────────────────────── */

    public static double safePrice(Double value) {
        return value == null ? 0d : value.doubleValue();
    }

    public static double round4(double value) {
        return Math.round(value * 10000d) / 10000d;
    }

    public static Double roundD(double value) {
        return Double.valueOf(round4(value));
    }

    public static Integer parseInteger(String value, Integer defaultValue) {
        if (isBlank(value)) return defaultValue;
        try { return Integer.valueOf(value.trim()); } catch (Exception e) { return defaultValue; }
    }

    /* ──────────────────────────────────────────
       시장 코드 헬퍼
    ────────────────────────────────────────── */

    /** mktCd를 KR / US 두 값으로 정규화 */
    public static String normalizeMarketGroup(String marketGroup) {
        return "US".equalsIgnoreCase(trim(marketGroup)) ? "US" : "KR";
    }

    /* ──────────────────────────────────────────
       수익률 계산
    ────────────────────────────────────────── */

    /**
     * 수익률 (%) = (closePrice / anchorPrice - 1) * 100
     * anchorPrice <= 0 이면 null 반환
     */
    public static Double calcReturnPct(Double closePrice, double anchorPrice) {
        if (closePrice == null || anchorPrice <= 0d) return null;
        return roundD(((closePrice.doubleValue() / anchorPrice) - 1d) * 100d);
    }

    /**
     * MFE (%) — startIndex~endIndex 구간 최고가 기준 수익률
     */
    public static Double calcMfePct(List<DlyPriceDto> priceList, int startIndex, int endIndex, double anchorPrice) {
        if (startIndex < 0 || endIndex < startIndex || anchorPrice <= 0d) return null;
        double maxHigh = Double.MIN_VALUE;
        for (int i = startIndex; i <= endIndex; i++) {
            DlyPriceDto row = priceList.get(i);
            if (row != null && row.getAdjHighPrice() != null) {
                maxHigh = Math.max(maxHigh, row.getAdjHighPrice().doubleValue());
            }
        }
        if (maxHigh == Double.MIN_VALUE) return null;
        return roundD(((maxHigh / anchorPrice) - 1d) * 100d);
    }

    /**
     * MAE (%) — startIndex~endIndex 구간 최저가 기준 손실률
     */
    public static Double calcMaePct(List<DlyPriceDto> priceList, int startIndex, int endIndex, double anchorPrice) {
        if (startIndex < 0 || endIndex < startIndex || anchorPrice <= 0d) return null;
        double minLow = Double.MAX_VALUE;
        for (int i = startIndex; i <= endIndex; i++) {
            DlyPriceDto row = priceList.get(i);
            if (row != null && row.getAdjLowPrice() != null) {
                minLow = Math.min(minLow, row.getAdjLowPrice().doubleValue());
            }
        }
        if (minLow == Double.MAX_VALUE) return null;
        return roundD(((minLow / anchorPrice) - 1d) * 100d);
    }

    /* ──────────────────────────────────────────
       가격 인덱스 탐색
    ────────────────────────────────────────── */

    /**
     * priceList 에서 tradeDt 와 일치하는 인덱스 반환.
     * 없으면 -1.
     */
    public static int findTradeDateIndex(List<DlyPriceDto> priceList, String tradeDt) {
        if (priceList == null || isBlank(tradeDt)) return -1;
        String normalized = normalizeDate(tradeDt);
        for (int i = 0; i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto != null && normalized.equals(dto.getTradeDt())) return i;
        }
        return -1;
    }

    /**
     * anchorIndex 로부터 currentIndex 까지의 일수.
     * anchorIndex < 0 이거나 current < anchor 이면 null.
     */
    public static Integer indexDiff(int anchorIndex, int currentIndex) {
        if (anchorIndex < 0 || currentIndex < anchorIndex) return null;
        return Integer.valueOf(currentIndex - anchorIndex);
    }

    /* ──────────────────────────────────────────
       TP1 / Stop 판정
    ────────────────────────────────────────── */

    public static boolean isTp1Hit(DlyPriceDto row, Double tp1Price) {
        return row != null && row.getAdjHighPrice() != null && tp1Price != null
            && row.getAdjHighPrice().doubleValue() >= tp1Price.doubleValue();
    }

    public static boolean isStopHit(DlyPriceDto row, Double stopPrice) {
        return row != null && row.getAdjLowPrice() != null && stopPrice != null
            && row.getAdjLowPrice().doubleValue() <= stopPrice.doubleValue();
    }

    public static boolean scanTp1Hit(List<DlyPriceDto> priceList, int startIndex, int endIndex, Double tp1Price) {
        if (tp1Price == null || startIndex < 0) return false;
        for (int i = startIndex; i <= endIndex && i < priceList.size(); i++) {
            if (isTp1Hit(priceList.get(i), tp1Price)) return true;
        }
        return false;
    }

    public static boolean scanStopHit(List<DlyPriceDto> priceList, int startIndex, int endIndex, Double stopPrice) {
        if (stopPrice == null || startIndex < 0) return false;
        for (int i = startIndex; i <= endIndex && i < priceList.size(); i++) {
            if (isStopHit(priceList.get(i), stopPrice)) return true;
        }
        return false;
    }
}
