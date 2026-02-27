package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class InquireTimeDailychartpriceResult extends CommonRestResult {

    /** 성공 실패 여부 */
    private String rtCd;

    /** 응답코드 */
    private String msgCd;

    /** 응답메세지 */
    private String msg1;

    /** 응답상세 (단일 데이터) */
    private Output1 output1;

    /** 응답상세2 (배열 데이터) */
    private Output2[] output2;

    // Explicit getters for environments without Lombok annotation processing
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

        /** 전일 대비 */
        private String prdyVrss;

        /** 전일 대비 부호 */
        private String prdyVrssSign;

        /** 전일 대비율 */
        private String prdyCtrt;

        /** 주식 전일 종가 */
        private String stckPrdyClpr;

        /** 누적 거래량 */
        private String acmlVol;

        /** 누적 거래 대금 */
        private String acmlTrPbmn;

        /** HTS 한글 종목명 */
        private String htsKorIsnm;

        /** 주식 현재가 */
        private String stckPrpr;

        // Explicit getters
        public String getPrdyVrss() { return prdyVrss; }
        public String getPrdyVrssSign() { return prdyVrssSign; }
        public String getPrdyCtrt() { return prdyCtrt; }
        public String getStckPrdyClpr() { return stckPrdyClpr; }
        public String getAcmlVol() { return acmlVol; }
        public String getAcmlTrPbmn() { return acmlTrPbmn; }
        public String getHtsKorIsnm() { return htsKorIsnm; }
        public String getStckPrpr() { return stckPrpr; }
    }

    @Getter
    @ToString
    public static class Output2 {

        /** 주식 영업 일자 */
        private String stckBsopDate;

        /** 주식 체결 시간 */
        private String stckCntgHour;

        /** 주식 현재가 */
        private String stckPrpr;

        /** 주식 시가2 */
        private String stckOprc;

        /** 주식 최고가 */
        private String stckHgpr;

        /** 주식 최저가 */
        private String stckLwpr;

        /** 체결 거래량 */
        private String cntgVol;

        /** 누적 거래 대금 */
        private String acmlTrPbmn;

        // Explicit getters
        public String getStckBsopDate() { return stckBsopDate; }
        public String getStckCntgHour() { return stckCntgHour; }
        public String getStckPrpr() { return stckPrpr; }
        public String getStckOprc() { return stckOprc; }
        public String getStckHgpr() { return stckHgpr; }
        public String getStckLwpr() { return stckLwpr; }
        public String getCntgVol() { return cntgVol; }
        public String getAcmlTrPbmn() { return acmlTrPbmn; }
    }

}
