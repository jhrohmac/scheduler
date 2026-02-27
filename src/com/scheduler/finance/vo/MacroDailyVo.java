package com.scheduler.finance.vo;

import java.io.Serializable;
import java.util.Date;

public class MacroDailyVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date checkDate;
    private Double cpi;
    private Double ppi;
    private Double pmi;
    private Double policyRate;
    private Double oilPrice;
    private Double oilMedian60d;

    private Double idxKospi;
    private Double idxNasdaq;
    private Double idxSpx;

    private String riskRegime;

    private Date modifyDate;
    private Date createDate;

    public Date getCheckDate() { return checkDate; }
    public void setCheckDate(Date checkDate) { this.checkDate = checkDate; }

    public Double getCpi() { return cpi; }
    public void setCpi(Double cpi) { this.cpi = cpi; }

    public Double getPpi() { return ppi; }
    public void setPpi(Double ppi) { this.ppi = ppi; }

    public Double getPmi() { return pmi; }
    public void setPmi(Double pmi) { this.pmi = pmi; }

    public Double getPolicyRate() { return policyRate; }
    public void setPolicyRate(Double policyRate) { this.policyRate = policyRate; }

    public Double getOilPrice() { return oilPrice; }
    public void setOilPrice(Double oilPrice) { this.oilPrice = oilPrice; }

    public Double getOilMedian60d() { return oilMedian60d; }
    public void setOilMedian60d(Double oilMedian60d) { this.oilMedian60d = oilMedian60d; }

    public Double getIdxKospi() { return idxKospi; }
    public void setIdxKospi(Double idxKospi) { this.idxKospi = idxKospi; }

    public Double getIdxNasdaq() { return idxNasdaq; }
    public void setIdxNasdaq(Double idxNasdaq) { this.idxNasdaq = idxNasdaq; }

    public Double getIdxSpx() { return idxSpx; }
    public void setIdxSpx(Double idxSpx) { this.idxSpx = idxSpx; }

    public String getRiskRegime() { return riskRegime; }
    public void setRiskRegime(String riskRegime) { this.riskRegime = riskRegime; }

    public Date getModifyDate() { return modifyDate; }
    public void setModifyDate(Date modifyDate) { this.modifyDate = modifyDate; }

    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
}
