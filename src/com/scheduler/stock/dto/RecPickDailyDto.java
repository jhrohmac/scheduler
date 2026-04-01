package com.scheduler.stock.dto;

public class RecPickDailyDto {

    private Long pickId;
    private String tradeDt;
    private String mktCd;
    private String stkCd;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Integer holdDayNoFromRec;
    private Integer holdDayNoFromWatch;
    private Integer holdDayNoFromBuy;
    private Double returnFromRecPct;
    private Double returnFromWatchPct;
    private Double returnFromBuyPct;
    private Double mfeFromRecPct;
    private Double maeFromRecPct;
    private Double mfeFromBuyPct;
    private Double maeFromBuyPct;
    private String upDownFlag;
    private String tp1HitYn;
    private String stopHitYn;
    private String sellSignalCode;
    private String sellSignalText;
    private String priceSource;

    public Long getPickId() {
        return pickId;
    }

    public void setPickId(Long pickId) {
        this.pickId = pickId;
    }

    public String getTradeDt() {
        return tradeDt;
    }

    public void setTradeDt(String tradeDt) {
        this.tradeDt = tradeDt;
    }

    public String getMktCd() {
        return mktCd;
    }

    public void setMktCd(String mktCd) {
        this.mktCd = mktCd;
    }

    public String getStkCd() {
        return stkCd;
    }

    public void setStkCd(String stkCd) {
        this.stkCd = stkCd;
    }

    public Double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(Double openPrice) {
        this.openPrice = openPrice;
    }

    public Double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(Double highPrice) {
        this.highPrice = highPrice;
    }

    public Double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(Double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Integer getHoldDayNoFromRec() {
        return holdDayNoFromRec;
    }

    public void setHoldDayNoFromRec(Integer holdDayNoFromRec) {
        this.holdDayNoFromRec = holdDayNoFromRec;
    }

    public Integer getHoldDayNoFromWatch() {
        return holdDayNoFromWatch;
    }

    public void setHoldDayNoFromWatch(Integer holdDayNoFromWatch) {
        this.holdDayNoFromWatch = holdDayNoFromWatch;
    }

    public Integer getHoldDayNoFromBuy() {
        return holdDayNoFromBuy;
    }

    public void setHoldDayNoFromBuy(Integer holdDayNoFromBuy) {
        this.holdDayNoFromBuy = holdDayNoFromBuy;
    }

    public Double getReturnFromRecPct() {
        return returnFromRecPct;
    }

    public void setReturnFromRecPct(Double returnFromRecPct) {
        this.returnFromRecPct = returnFromRecPct;
    }

    public Double getReturnFromWatchPct() {
        return returnFromWatchPct;
    }

    public void setReturnFromWatchPct(Double returnFromWatchPct) {
        this.returnFromWatchPct = returnFromWatchPct;
    }

    public Double getReturnFromBuyPct() {
        return returnFromBuyPct;
    }

    public void setReturnFromBuyPct(Double returnFromBuyPct) {
        this.returnFromBuyPct = returnFromBuyPct;
    }

    public Double getMfeFromRecPct() {
        return mfeFromRecPct;
    }

    public void setMfeFromRecPct(Double mfeFromRecPct) {
        this.mfeFromRecPct = mfeFromRecPct;
    }

    public Double getMaeFromRecPct() {
        return maeFromRecPct;
    }

    public void setMaeFromRecPct(Double maeFromRecPct) {
        this.maeFromRecPct = maeFromRecPct;
    }

    public Double getMfeFromBuyPct() {
        return mfeFromBuyPct;
    }

    public void setMfeFromBuyPct(Double mfeFromBuyPct) {
        this.mfeFromBuyPct = mfeFromBuyPct;
    }

    public Double getMaeFromBuyPct() {
        return maeFromBuyPct;
    }

    public void setMaeFromBuyPct(Double maeFromBuyPct) {
        this.maeFromBuyPct = maeFromBuyPct;
    }

    public String getUpDownFlag() {
        return upDownFlag;
    }

    public void setUpDownFlag(String upDownFlag) {
        this.upDownFlag = upDownFlag;
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

    public String getPriceSource() {
        return priceSource;
    }

    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }
}
