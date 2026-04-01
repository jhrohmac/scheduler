package com.scheduler.stock.dto;

public class DlyPriceDto {

    private String stkCd;
    private String tradeDt;
    private Double rawOpenPrice;
    private Double rawHighPrice;
    private Double rawLowPrice;
    private Double rawClosePrice;
    private Double adjOpenPrice;
    private Double adjHighPrice;
    private Double adjLowPrice;
    private Double adjClosePrice;
    private Long volume;
    private Long tradeValue;
    private Double adjFactor;

    public String getStkCd() {
        return stkCd;
    }

    public void setStkCd(String stkCd) {
        this.stkCd = stkCd;
    }

    public String getTradeDt() {
        return tradeDt;
    }

    public void setTradeDt(String tradeDt) {
        this.tradeDt = tradeDt;
    }

    public Double getRawOpenPrice() {
        return rawOpenPrice;
    }

    public void setRawOpenPrice(Double rawOpenPrice) {
        this.rawOpenPrice = rawOpenPrice;
    }

    public Double getRawHighPrice() {
        return rawHighPrice;
    }

    public void setRawHighPrice(Double rawHighPrice) {
        this.rawHighPrice = rawHighPrice;
    }

    public Double getRawLowPrice() {
        return rawLowPrice;
    }

    public void setRawLowPrice(Double rawLowPrice) {
        this.rawLowPrice = rawLowPrice;
    }

    public Double getRawClosePrice() {
        return rawClosePrice;
    }

    public void setRawClosePrice(Double rawClosePrice) {
        this.rawClosePrice = rawClosePrice;
    }

    public Double getAdjOpenPrice() {
        return adjOpenPrice;
    }

    public void setAdjOpenPrice(Double adjOpenPrice) {
        this.adjOpenPrice = adjOpenPrice;
    }

    public Double getAdjHighPrice() {
        return adjHighPrice;
    }

    public void setAdjHighPrice(Double adjHighPrice) {
        this.adjHighPrice = adjHighPrice;
    }

    public Double getAdjLowPrice() {
        return adjLowPrice;
    }

    public void setAdjLowPrice(Double adjLowPrice) {
        this.adjLowPrice = adjLowPrice;
    }

    public Double getAdjClosePrice() {
        return adjClosePrice;
    }

    public void setAdjClosePrice(Double adjClosePrice) {
        this.adjClosePrice = adjClosePrice;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Long getTradeValue() {
        return tradeValue;
    }

    public void setTradeValue(Long tradeValue) {
        this.tradeValue = tradeValue;
    }

    public Double getAdjFactor() {
        return adjFactor;
    }

    public void setAdjFactor(Double adjFactor) {
        this.adjFactor = adjFactor;
    }
}
