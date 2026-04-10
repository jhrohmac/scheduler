package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

public class InquireOverseasTimeItemchartpriceResult extends CommonRestResult {

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
        private String stim;
        private String etim;
        private String sktm;
        private String ektm;
        private String next;
        private String more;
        private String nrec;

        public String getRsym() {
            return rsym;
        }

        public String getZdiv() {
            return zdiv;
        }

        public String getStim() {
            return stim;
        }

        public String getEtim() {
            return etim;
        }

        public String getSktm() {
            return sktm;
        }

        public String getEktm() {
            return ektm;
        }

        public String getNext() {
            return next;
        }

        public String getMore() {
            return more;
        }

        public String getNrec() {
            return nrec;
        }
    }

    @Getter
    @ToString
    public static class Output2 {
        private String tymd;
        private String xymd;
        private String xhms;
        private String kymd;
        private String khms;
        private String open;
        private String high;
        private String low;
        private String last;
        private String evol;
        private String eamt;

        public String getTymd() {
            return tymd;
        }

        public String getXymd() {
            return xymd;
        }

        public String getXhms() {
            return xhms;
        }

        public String getKymd() {
            return kymd;
        }

        public String getKhms() {
            return khms;
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

        public String getLast() {
            return last;
        }

        public String getEvol() {
            return evol;
        }

        public String getEamt() {
            return eamt;
        }
    }
}
