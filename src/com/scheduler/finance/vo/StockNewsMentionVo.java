package com.scheduler.finance.vo;

import java.io.Serializable;
import java.util.Date;

public class StockNewsMentionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date checkDate;
    private String stockCode;

    private Integer mentionCnt1d;
    private Integer mentionCnt3d;
    private Integer mentionCnt7d;
    private Integer mentionCnt14d;

    private Double mentionZ7d;
    private Double sentimentScore;

    private Date modifyDate;
    private Date createDate;

    public Date getCheckDate() { return checkDate; }
    public void setCheckDate(Date checkDate) { this.checkDate = checkDate; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public Integer getMentionCnt1d() { return mentionCnt1d; }
    public void setMentionCnt1d(Integer mentionCnt1d) { this.mentionCnt1d = mentionCnt1d; }

    public Integer getMentionCnt3d() { return mentionCnt3d; }
    public void setMentionCnt3d(Integer mentionCnt3d) { this.mentionCnt3d = mentionCnt3d; }

    public Integer getMentionCnt7d() { return mentionCnt7d; }
    public void setMentionCnt7d(Integer mentionCnt7d) { this.mentionCnt7d = mentionCnt7d; }

    public Integer getMentionCnt14d() { return mentionCnt14d; }
    public void setMentionCnt14d(Integer mentionCnt14d) { this.mentionCnt14d = mentionCnt14d; }

    public Double getMentionZ7d() { return mentionZ7d; }
    public void setMentionZ7d(Double mentionZ7d) { this.mentionZ7d = mentionZ7d; }

    public Double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(Double sentimentScore) { this.sentimentScore = sentimentScore; }

    public Date getModifyDate() { return modifyDate; }
    public void setModifyDate(Date modifyDate) { this.modifyDate = modifyDate; }

    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
}
