package com.scheduler.finance.vo;

import java.util.Date;

/**
 * WF-2-4: 보유종목 이벤트(TB_S_POSITION_EVENT)
 */
public class PositionEventVo {
    
    private Long eventId;
    private Long positionId;
    private String stockGroup;
    private String stockCode;
    private String eventType; // ENTER | RISK_OFF | TREND_FOLLOW | EXIT | ALERT
    private String eventLevel; // INFO | WARN | ERROR
    private Date eventTime;
    private String message;
    private String jsonParams;
    private Long sourceTxnId;
    private Date createDate;
    
    // Getters and Setters
    
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
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
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getEventLevel() {
        return eventLevel;
    }
    
    public void setEventLevel(String eventLevel) {
        this.eventLevel = eventLevel;
    }
    
    public Date getEventTime() {
        return eventTime;
    }
    
    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getJsonParams() {
        return jsonParams;
    }
    
    public void setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
    }
    
    public Long getSourceTxnId() {
        return sourceTxnId;
    }
    
    public void setSourceTxnId(Long sourceTxnId) {
        this.sourceTxnId = sourceTxnId;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
