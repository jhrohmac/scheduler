package com.scheduler.finance.vo;

/**
 * 이동평균선 정배열 + 장대양봉/연속양봉 패턴 신호 VO
 *
 * 패턴 설명:
 * - 완전 정배열: close > MA5 > MA20 > MA60 > MA120 > MA240
 * - 장대양봉: 실체(close-open)가 최근 평균 실체의 2배 이상
 * - 연속양봉: 3일 이상 연속 양봉(close > open)
 */
public class BullishMomentumSignal {
    private long signalTime;
    private String stockCode;
    private String stockName;
    private double closePrice;
    private String signalType;  // "LONG_BULL_BODY" | "CONSECUTIVE_BULL"
    private int consecutiveDays;  // 연속양봉 일수 (장대양봉은 1)
    private double bodyRatio;     // 실체 비율 (장대양봉: avgBody 대비 비율, 연속양봉: 0)

    // MA 값들
    private double ma5;
    private double ma20;
    private double ma60;
    private double ma120;
    private double ma240;

    // 캔들 정보
    private double open;
    private double high;
    private double low;
    private double volume;

    // 기술적 정보
    private double rsi;  // RSI (과매수 필터)
    private boolean macdBullish;  // MACD 골든크로스

    public BullishMomentumSignal() {
    }

    public BullishMomentumSignal(long signalTime, String stockCode, double closePrice,
                                 String signalType, double ma5, double ma20, double ma60,
                                 double ma120, double ma240) {
        this.signalTime = signalTime;
        this.stockCode = stockCode;
        this.closePrice = closePrice;
        this.signalType = signalType;
        this.ma5 = ma5;
        this.ma20 = ma20;
        this.ma60 = ma60;
        this.ma120 = ma120;
        this.ma240 = ma240;
    }

    public long getSignalTime() {
        return signalTime;
    }

    public void setSignalTime(long signalTime) {
        this.signalTime = signalTime;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public double getBodyRatio() {
        return bodyRatio;
    }

    public void setBodyRatio(double bodyRatio) {
        this.bodyRatio = bodyRatio;
    }

    public double getMa5() {
        return ma5;
    }

    public void setMa5(double ma5) {
        this.ma5 = ma5;
    }

    public double getMa20() {
        return ma20;
    }

    public void setMa20(double ma20) {
        this.ma20 = ma20;
    }

    public double getMa60() {
        return ma60;
    }

    public void setMa60(double ma60) {
        this.ma60 = ma60;
    }

    public double getMa120() {
        return ma120;
    }

    public void setMa120(double ma120) {
        this.ma120 = ma120;
    }

    public double getMa240() {
        return ma240;
    }

    public void setMa240(double ma240) {
        this.ma240 = ma240;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getRsi() {
        return rsi;
    }

    public void setRsi(double rsi) {
        this.rsi = rsi;
    }

    public boolean isMacdBullish() {
        return macdBullish;
    }

    public void setMacdBullish(boolean macdBullish) {
        this.macdBullish = macdBullish;
    }
}
