package com.scheduler.kis_api.api.rest.quotations;

import java.util.List;

import com.scheduler.kis_api.api.CommonRestResult;

public class InquireOverseasDailyChartPriceResult extends CommonRestResult {

    private String rtCd;
    private String msgCd;
    private String msg1;

    private Output1 output1;
    private List<Output2> output2;

    public String getRtCd() {
        return rtCd;
    }

    public void setRtCd(String rtCd) {
        this.rtCd = rtCd;
    }

    public String getMsgCd() {
        return msgCd;
    }

    public void setMsgCd(String msgCd) {
        this.msgCd = msgCd;
    }

    public String getMsg1() {
        return msg1;
    }

    public void setMsg1(String msg1) {
        this.msg1 = msg1;
    }

    public Output1 getOutput1() {
        return output1;
    }

    public void setOutput1(Output1 output1) {
        this.output1 = output1;
    }

    public List<Output2> getOutput2() {
        return output2;
    }

    public void setOutput2(List<Output2> output2) {
        this.output2 = output2;
    }

    public static class Output1 {
        private String prdyVrssSign;
        private String htsKorIsnm;

        private String ovrsNmixPrpr;
        private String ovrsNmixPrdyVrss;
        private String prdyCtrt;

        private String stckShrnIscd;

        public String getPrdyVrssSign() {
            return prdyVrssSign;
        }

        public void setPrdyVrssSign(String prdyVrssSign) {
            this.prdyVrssSign = prdyVrssSign;
        }

        public String getHtsKorIsnm() {
            return htsKorIsnm;
        }

        public void setHtsKorIsnm(String htsKorIsnm) {
            this.htsKorIsnm = htsKorIsnm;
        }

        public String getOvrsNmixPrpr() {
            return ovrsNmixPrpr;
        }

        public void setOvrsNmixPrpr(String ovrsNmixPrpr) {
            this.ovrsNmixPrpr = ovrsNmixPrpr;
        }

        public String getOvrsNmixPrdyVrss() {
            return ovrsNmixPrdyVrss;
        }

        public void setOvrsNmixPrdyVrss(String ovrsNmixPrdyVrss) {
            this.ovrsNmixPrdyVrss = ovrsNmixPrdyVrss;
        }

        public String getPrdyCtrt() {
            return prdyCtrt;
        }

        public void setPrdyCtrt(String prdyCtrt) {
            this.prdyCtrt = prdyCtrt;
        }

        public String getStckShrnIscd() {
            return stckShrnIscd;
        }

        public void setStckShrnIscd(String stckShrnIscd) {
            this.stckShrnIscd = stckShrnIscd;
        }
    }

    public static class Output2 {
        private final java.util.Map<String, Object> fields = new java.util.LinkedHashMap<String, Object>();

        @com.fasterxml.jackson.annotation.JsonAnySetter
        public void setField(String key, Object value) {
            fields.put(key, value);
        }

        public Object getField(String key) {
            return fields.get(key);
        }

        public java.util.Map<String, Object> getFields() {
            return fields;
        }
    }
}
