package com.scheduler.finance.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MarketSummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String result;   // ok / fail
    private String message;  // fail 시 메시지
    private String asOf;     // 기준시각

    private List<Item> domestic = new ArrayList<Item>(); // 국내지수
    private List<Item> fx = new ArrayList<Item>();       // 환율
    private List<Item> overseas = new ArrayList<Item>(); // 해외지수

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String code;

        private String price;
        private String change;
        private String changeRate;
        private String sign;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getPrice() {
            return price;
        }
        public void setPrice(String price) {
            this.price = price;
        }
        public String getChange() {
            return change;
        }
        public void setChange(String change) {
            this.change = change;
        }
        public String getChangeRate() {
            return changeRate;
        }
        public void setChangeRate(String changeRate) {
            this.changeRate = changeRate;
        }
        public String getSign() {
            return sign;
        }
        public void setSign(String sign) {
            this.sign = sign;
        }
    }

    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getAsOf() {
        return asOf;
    }
    public void setAsOf(String asOf) {
        this.asOf = asOf;
    }
    public List<Item> getDomestic() {
        return domestic;
    }
    public void setDomestic(List<Item> domestic) {
        this.domestic = domestic;
    }
    public List<Item> getFx() {
        return fx;
    }
    public void setFx(List<Item> fx) {
        this.fx = fx;
    }
    public List<Item> getOverseas() {
        return overseas;
    }
    public void setOverseas(List<Item> overseas) {
        this.overseas = overseas;
    }
}
