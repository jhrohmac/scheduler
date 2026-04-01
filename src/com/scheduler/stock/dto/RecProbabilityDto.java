package com.scheduler.stock.dto;

public class RecProbabilityDto {

    private String evalType;
    private Integer holdDays;
    private String sourceMktCd;
    private String listingMarket;
    private String recGrade;
    private String entryRuleCode;
    private String trendBucket;
    private String monthBucket;
    private Integer sampleCnt;
    private Double winRate;
    private Double tp1Rate;
    private Double stopRate;
    private Double avgReturnPct;
    private Double avgMfePct;
    private Double avgMaePct;

    public String getEvalType() {
        return evalType;
    }

    public void setEvalType(String evalType) {
        this.evalType = evalType;
    }

    public Integer getHoldDays() {
        return holdDays;
    }

    public void setHoldDays(Integer holdDays) {
        this.holdDays = holdDays;
    }

    public String getSourceMktCd() {
        return sourceMktCd;
    }

    public void setSourceMktCd(String sourceMktCd) {
        this.sourceMktCd = sourceMktCd;
    }

    public String getListingMarket() {
        return listingMarket;
    }

    public void setListingMarket(String listingMarket) {
        this.listingMarket = listingMarket;
    }

    public String getRecGrade() {
        return recGrade;
    }

    public void setRecGrade(String recGrade) {
        this.recGrade = recGrade;
    }

    public String getEntryRuleCode() {
        return entryRuleCode;
    }

    public void setEntryRuleCode(String entryRuleCode) {
        this.entryRuleCode = entryRuleCode;
    }

    public String getTrendBucket() {
        return trendBucket;
    }

    public void setTrendBucket(String trendBucket) {
        this.trendBucket = trendBucket;
    }

    public String getMonthBucket() {
        return monthBucket;
    }

    public void setMonthBucket(String monthBucket) {
        this.monthBucket = monthBucket;
    }

    public Integer getSampleCnt() {
        return sampleCnt;
    }

    public void setSampleCnt(Integer sampleCnt) {
        this.sampleCnt = sampleCnt;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public Double getTp1Rate() {
        return tp1Rate;
    }

    public void setTp1Rate(Double tp1Rate) {
        this.tp1Rate = tp1Rate;
    }

    public Double getStopRate() {
        return stopRate;
    }

    public void setStopRate(Double stopRate) {
        this.stopRate = stopRate;
    }

    public Double getAvgReturnPct() {
        return avgReturnPct;
    }

    public void setAvgReturnPct(Double avgReturnPct) {
        this.avgReturnPct = avgReturnPct;
    }

    public Double getAvgMfePct() {
        return avgMfePct;
    }

    public void setAvgMfePct(Double avgMfePct) {
        this.avgMfePct = avgMfePct;
    }

    public Double getAvgMaePct() {
        return avgMaePct;
    }

    public void setAvgMaePct(Double avgMaePct) {
        this.avgMaePct = avgMaePct;
    }
}
