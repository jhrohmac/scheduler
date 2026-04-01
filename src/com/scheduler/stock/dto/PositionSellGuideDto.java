package com.scheduler.stock.dto;

public class PositionSellGuideDto {

    private Long positionId;
    private Long sourcePickId;
    private String stockCode;
    private String sellGuideState;
    private String sellSignalCode;
    private String sellSignalText;
    private Double currentReturnPct;
    private Double targetPrice;
    private Double stopPrice;
    private Double tp1Price;
    private String asOfTradeDt;

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getSourcePickId() {
        return sourcePickId;
    }

    public void setSourcePickId(Long sourcePickId) {
        this.sourcePickId = sourcePickId;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getSellGuideState() {
        return sellGuideState;
    }

    public void setSellGuideState(String sellGuideState) {
        this.sellGuideState = sellGuideState;
    }

    public String getSellSignalCode() {
        return sellSignalCode;
    }

    public void setSellSignalCode(String sellSignalCode) {
        this.sellSignalCode = sellSignalCode;
    }

    public String getSellSignalText() {
        return sellSignalText;
    }

    public void setSellSignalText(String sellSignalText) {
        this.sellSignalText = sellSignalText;
    }

    public Double getCurrentReturnPct() {
        return currentReturnPct;
    }

    public void setCurrentReturnPct(Double currentReturnPct) {
        this.currentReturnPct = currentReturnPct;
    }

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public Double getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(Double stopPrice) {
        this.stopPrice = stopPrice;
    }

    public Double getTp1Price() {
        return tp1Price;
    }

    public void setTp1Price(Double tp1Price) {
        this.tp1Price = tp1Price;
    }

    public String getAsOfTradeDt() {
        return asOfTradeDt;
    }

    public void setAsOfTradeDt(String asOfTradeDt) {
        this.asOfTradeDt = asOfTradeDt;
    }
}
