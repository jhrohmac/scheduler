package com.scheduler.stock.dto;

public class RecPickEvalDto {

    private Long pickId;
    private String evalType;
    private Integer holdDays;
    private String anchorDate;
    private String targetDate;
    private Double returnPct;
    private Double mfePct;
    private Double maePct;
    private String winYn;
    private String tp1HitYn;
    private String stopHitYn;
    private String calcStatus;

    public Long getPickId() {
        return pickId;
    }

    public void setPickId(Long pickId) {
        this.pickId = pickId;
    }

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

    public String getAnchorDate() {
        return anchorDate;
    }

    public void setAnchorDate(String anchorDate) {
        this.anchorDate = anchorDate;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public Double getReturnPct() {
        return returnPct;
    }

    public void setReturnPct(Double returnPct) {
        this.returnPct = returnPct;
    }

    public Double getMfePct() {
        return mfePct;
    }

    public void setMfePct(Double mfePct) {
        this.mfePct = mfePct;
    }

    public Double getMaePct() {
        return maePct;
    }

    public void setMaePct(Double maePct) {
        this.maePct = maePct;
    }

    public String getWinYn() {
        return winYn;
    }

    public void setWinYn(String winYn) {
        this.winYn = winYn;
    }

    public String getTp1HitYn() {
        return tp1HitYn;
    }

    public void setTp1HitYn(String tp1HitYn) {
        this.tp1HitYn = tp1HitYn;
    }

    public String getStopHitYn() {
        return stopHitYn;
    }

    public void setStopHitYn(String stopHitYn) {
        this.stopHitYn = stopHitYn;
    }

    public String getCalcStatus() {
        return calcStatus;
    }

    public void setCalcStatus(String calcStatus) {
        this.calcStatus = calcStatus;
    }
}
