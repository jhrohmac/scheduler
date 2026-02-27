package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class InquireIndexPriceResult extends CommonRestResult {

    private String rtCd;
    private String msgCd;
    private String msg1;
    private Output output;

    public String getRtCd() {
        return rtCd;
    }

    public Output getOutput() {
        return output;
    }

    @Getter
    @ToString
    public static class Output {

        private String bstpNmixPrpr;
        private String bstpNmixPrdyVrss;
        private String prdyVrssSign;
        private String bstpNmixPrdyCtrt;

        public String getBstpNmixPrpr() {
            return bstpNmixPrpr;
        }

        public String getBstpNmixPrdyVrss() {
            return bstpNmixPrdyVrss;
        }

        public String getPrdyVrssSign() {
            return prdyVrssSign;
        }

        public String getBstpNmixPrdyCtrt() {
            return bstpNmixPrdyCtrt;
        }
    }
}
