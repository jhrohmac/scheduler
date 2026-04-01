package com.scheduler.finance.vo;

import java.util.Date;

/**
 * WF-2-4: 보유종목 원장(TB_S_POSITION)
 */
public class PositionVo {
    
    private Long positionId;
    private String stockGroup;
    private String stockCode;
    private String marketCode;
    private Integer totalQty;
    private Double avgPrice;
    private String closeFlag;
    private String stateCode;
    private Long lastTxnId;
    private Long sourcePickId;
    private Date buyDate;
    private Double targetPrice;
    private Double stopPrice;
    private Double tp1Price;
    private String sellGuideState;
    private String lastSellSignalCode;
    private Date lastTrackDate;
    private Date modifyDate;
    private String createUser;
    private Date createDate;
    
    // Getters and Setters
    
    public Long getPositionId() {
        return positionId;
    }
    
    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }
    
    public String getStockGroup() {
        return stockGroup;
    }
    
    public void setStockGroup(String stockGroup) {
        this.stockGroup = stockGroup;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    
    public String getMarketCode() {
        return marketCode;
    }
    
    public void setMarketCode(String marketCode) {
        this.marketCode = marketCode;
    }
    
    public Integer getTotalQty() {
        return totalQty;
    }
    
    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }
    
    public Double getAvgPrice() {
        return avgPrice;
    }
    
    public void setAvgPrice(Double avgPrice) {
        this.avgPrice = avgPrice;
    }
    
    public String getCloseFlag() {
        return closeFlag;
    }
    
    public void setCloseFlag(String closeFlag) {
        this.closeFlag = closeFlag;
    }
    
    public String getStateCode() {
        return stateCode;
    }
    
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }
    
    public Long getLastTxnId() {
        return lastTxnId;
    }
    
    public void setLastTxnId(Long lastTxnId) {
        this.lastTxnId = lastTxnId;
    }

    public Long getSourcePickId() {
        return sourcePickId;
    }

    public void setSourcePickId(Long sourcePickId) {
        this.sourcePickId = sourcePickId;
    }

    public Date getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(Date buyDate) {
        this.buyDate = buyDate;
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

    public String getSellGuideState() {
        return sellGuideState;
    }

    public void setSellGuideState(String sellGuideState) {
        this.sellGuideState = sellGuideState;
    }

    public String getLastSellSignalCode() {
        return lastSellSignalCode;
    }

    public void setLastSellSignalCode(String lastSellSignalCode) {
        this.lastSellSignalCode = lastSellSignalCode;
    }

    public Date getLastTrackDate() {
        return lastTrackDate;
    }

    public void setLastTrackDate(Date lastTrackDate) {
        this.lastTrackDate = lastTrackDate;
    }
    
    public Date getModifyDate() {
        return modifyDate;
    }
    
    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }
    
    public String getCreateUser() {
        return createUser;
    }
    
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
