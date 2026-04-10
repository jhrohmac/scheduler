package com.scheduler.finance.vo;

import java.io.Serializable;
import java.util.Date;

public class StockSignalEvalVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stockCode;
    private String signalType;
    private Date signalDate;
    private Date nextSignalDate;

    private Double entryPrice;
    private Double exitPrice;
    private Double returnPct;
    private Double maxRunupPct;
    private Double maxDrawdownPct;

    private String hitYn;
    private Date createDate;

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }

    public Date getSignalDate() { return signalDate; }
    public void setSignalDate(Date signalDate) { this.signalDate = signalDate; }

    public Date getNextSignalDate() { return nextSignalDate; }
    public void setNextSignalDate(Date nextSignalDate) { this.nextSignalDate = nextSignalDate; }

    public Double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(Double entryPrice) { this.entryPrice = entryPrice; }

    public Double getExitPrice() { return exitPrice; }
    public void setExitPrice(Double exitPrice) { this.exitPrice = exitPrice; }

    public Double getReturnPct() { return returnPct; }
    public void setReturnPct(Double returnPct) { this.returnPct = returnPct; }

    public Double getMaxRunupPct() { return maxRunupPct; }
    public void setMaxRunupPct(Double maxRunupPct) { this.maxRunupPct = maxRunupPct; }

    public Double getMaxDrawdownPct() { return maxDrawdownPct; }
    public void setMaxDrawdownPct(Double maxDrawdownPct) { this.maxDrawdownPct = maxDrawdownPct; }

    public String getHitYn() { return hitYn; }
    public void setHitYn(String hitYn) { this.hitYn = hitYn; }

    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
}
