package com.scheduler.finance.vo;

import java.util.Date;

/**
 * WF-2-4: 보유종목 거래 내역(TB_S_POSITION_TXN)
 */
public class PositionTxnVo {
    
    private Long txnId;
    private Long positionId;
    private String stockGroup;
    private String stockCode;
    private String actionType; // ADD | DELETE | AVERAGE_DOWN | BUY | SELL
    private Integer qty;
    private Double price;
    private Integer beforeQty;
    private Double beforeAvg;
    private Integer afterQty;
    private Double afterAvg;
    private String reasonText;
    private String jsonParams;
    private String createUser;
    private Date createDate;
    
    // Getters and Setters
    
    public Long getTxnId() {
        return txnId;
    }
    
    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }
    
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
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public Integer getQty() {
        return qty;
    }
    
    public void setQty(Integer qty) {
        this.qty = qty;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getBeforeQty() {
        return beforeQty;
    }
    
    public void setBeforeQty(Integer beforeQty) {
        this.beforeQty = beforeQty;
    }
    
    public Double getBeforeAvg() {
        return beforeAvg;
    }
    
    public void setBeforeAvg(Double beforeAvg) {
        this.beforeAvg = beforeAvg;
    }
    
    public Integer getAfterQty() {
        return afterQty;
    }
    
    public void setAfterQty(Integer afterQty) {
        this.afterQty = afterQty;
    }
    
    public Double getAfterAvg() {
        return afterAvg;
    }
    
    public void setAfterAvg(Double afterAvg) {
        this.afterAvg = afterAvg;
    }
    
    public String getReasonText() {
        return reasonText;
    }
    
    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }
    
    public String getJsonParams() {
        return jsonParams;
    }
    
    public void setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
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
