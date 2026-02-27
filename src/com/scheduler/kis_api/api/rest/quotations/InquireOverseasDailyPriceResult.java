package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

public class InquireOverseasDailyPriceResult extends CommonRestResult {

    private String rtCd;
    private String msgCd;
    private String msg1;
    private Output1 output1;
    private Output2[] output2;

    public String getRtCd() {
        return rtCd;
    }

    public String getMsgCd() {
        return msgCd;
    }

    public String getMsg1() {
        return msg1;
    }

    public Output1 getOutput1() {
        return output1;
    }

    public Output2[] getOutput2() {
        return output2;
    }

    @Getter
    @ToString
    public static class Output1 {
        private String rsym;
        private String zdiv;
        private String nrec;
        private String keyb;

        public String getRsym() {
            return rsym;
        }

        public String getZdiv() {
            return zdiv;
        }

        public String getNrec() {
            return nrec;
        }

        public String getKeyb() {
            return keyb;
        }
    }

    @Getter
    @ToString
    public static class Output2 {
        private String xymd;
        private String clos;
        private String sign;
        private String diff;
        private String rate;
        private String open;
        private String high;
        private String low;
        private String tvol;
        private String tamt;
        private String pbid;
        private String vbid;
        private String pask;
        private String vask;

        public String getXymd() {
            return xymd;
        }

        public String getClos() {
            return clos;
        }

        public String getSign() {
            return sign;
        }

        public String getDiff() {
            return diff;
        }

        public String getRate() {
            return rate;
        }

        public String getOpen() {
            return open;
        }

        public String getHigh() {
            return high;
        }

        public String getLow() {
            return low;
        }

        public String getTvol() {
            return tvol;
        }

        public String getTamt() {
            return tamt;
        }

        public String getPbid() {
            return pbid;
        }

        public String getVbid() {
            return vbid;
        }

        public String getPask() {
            return pask;
        }

        public String getVask() {
            return vask;
        }
    }
}
