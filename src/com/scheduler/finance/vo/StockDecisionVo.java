package com.scheduler.finance.vo;

import java.io.Serializable;
import java.util.Date;

public class StockDecisionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date checkDate;
    private String stockCode;
    private String timeframe;

    private String signalCode;

    private Double scoreTotal;
    private Double scoreTech;
    private Double scoreNews;
    private Double scoreMacro;
    private Double scoreFund;
    private Double scoreReliability;

    private String keyFlags;
    private String korDesc;

    private Date modifyDate;
    private Date createDate;

    public Date getCheckDate() { return checkDate; }
    public void setCheckDate(Date checkDate) { this.checkDate = checkDate; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public String getSignalCode() { return signalCode; }
    public void setSignalCode(String signalCode) { this.signalCode = signalCode; }

    public Double getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(Double scoreTotal) { this.scoreTotal = scoreTotal; }

    public Double getScoreTech() { return scoreTech; }
    public void setScoreTech(Double scoreTech) { this.scoreTech = scoreTech; }

    public Double getScoreNews() { return scoreNews; }
    public void setScoreNews(Double scoreNews) { this.scoreNews = scoreNews; }

    public Double getScoreMacro() { return scoreMacro; }
    public void setScoreMacro(Double scoreMacro) { this.scoreMacro = scoreMacro; }

    public Double getScoreFund() { return scoreFund; }
    public void setScoreFund(Double scoreFund) { this.scoreFund = scoreFund; }

    public Double getScoreReliability() { return scoreReliability; }
    public void setScoreReliability(Double scoreReliability) { this.scoreReliability = scoreReliability; }

    public String getKeyFlags() { return keyFlags; }
    public void setKeyFlags(String keyFlags) { this.keyFlags = keyFlags; }

    public String getKorDesc() { return korDesc; }
    public void setKorDesc(String korDesc) { this.korDesc = korDesc; }

    public Date getModifyDate() { return modifyDate; }
    public void setModifyDate(Date modifyDate) { this.modifyDate = modifyDate; }

    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
}
