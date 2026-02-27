package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PriceResult extends CommonRestResult {

    private String rtCd;
    private String msgCd;
    private String msg1;
    private Output output;

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

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    @Getter
    @ToString
    public static class Output {

        private String rsym;
        private String zdiv;
        private String base;
        private String pvol;
        private String last;
        private String sign;
        private String diff;
        private String rate;
        private String tvol;
        private String tamt;
        private String ordy;

        public String getRsym() {
            return rsym;
        }

        public void setRsym(String rsym) {
            this.rsym = rsym;
        }

        public String getZdiv() {
            return zdiv;
        }

        public void setZdiv(String zdiv) {
            this.zdiv = zdiv;
        }

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getPvol() {
            return pvol;
        }

        public void setPvol(String pvol) {
            this.pvol = pvol;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getDiff() {
            return diff;
        }

        public void setDiff(String diff) {
            this.diff = diff;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public String getTvol() {
            return tvol;
        }

        public void setTvol(String tvol) {
            this.tvol = tvol;
        }

        public String getTamt() {
            return tamt;
        }

        public void setTamt(String tamt) {
            this.tamt = tamt;
        }

        public String getOrdy() {
            return ordy;
        }

        public void setOrdy(String ordy) {
            this.ordy = ordy;
        }
    }
}
