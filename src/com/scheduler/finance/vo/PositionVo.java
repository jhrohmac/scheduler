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
