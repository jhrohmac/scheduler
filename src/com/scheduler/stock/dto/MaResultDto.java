package com.scheduler.stock.dto;

public class MaResultDto {

    private Integer cntValid;
    private Double minPrice;
    private Double ma5;
    private Double ma20;
    private Double ma60;
    private Double ma120;
    private Double ma240;

    public Integer getCntValid() {
        return cntValid;
    }

    public void setCntValid(Integer cntValid) {
        this.cntValid = cntValid;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMa5() {
        return ma5;
    }

    public void setMa5(Double ma5) {
        this.ma5 = ma5;
    }

    public Double getMa20() {
        return ma20;
    }

    public void setMa20(Double ma20) {
        this.ma20 = ma20;
    }

    public Double getMa60() {
        return ma60;
    }

    public void setMa60(Double ma60) {
        this.ma60 = ma60;
    }

    public Double getMa120() {
        return ma120;
    }

    public void setMa120(Double ma120) {
        this.ma120 = ma120;
    }

    public Double getMa240() {
        return ma240;
    }

    public void setMa240(Double ma240) {
        this.ma240 = ma240;
    }
}
