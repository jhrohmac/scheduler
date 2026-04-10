package com.scheduler.kis_api.api.rest.quotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scheduler.kis_api.api.CommonRestResult;

/**
 * KIS 일자별 시세 조회 결과 클래스
 *
 * 실제 KIS 응답 예시 (요약):
 * {
 *   "rt_cd": "0",
 *   "msg_cd": "MCA00000",
 *   "msg1": "정상처리 되었습니다.",
 *   "output": [
 *     {
 *       "stck_bsop_date": "20251121",
 *       "stck_oprc": "97400",
 *       "stck_hgpr": "98100",
 *       "stck_lwpr": "93500",
 *       "stck_clpr": "95200",
 *       "acml_vol": "37742660",
 *       ...
 *     },
 *     ...
 *   ]
 * }
 */
public class InquireDailyPriceResult extends CommonRestResult {

    /** 성공 실패 여부 (KIS: rt_cd) */
    @JsonProperty("rt_cd")
    private String rtCd;

    /** 응답코드 (KIS: msg_cd) */
    @JsonProperty("msg_cd")
    private String msgCd;

    /** 응답메세지 (KIS: msg1) */
    @JsonProperty("msg1")
    private String msg1;

    /**
     * 일자별 시세 배열
     * KIS 응답의 "output" 필드가 그대로 여기로 매핑된다.
     */
    @JsonProperty("output")
    private Output1[] output1;

    // -------------------------------------------------------
    // 기본 생성자
    // -------------------------------------------------------

    public InquireDailyPriceResult() {
    }

    // -------------------------------------------------------
    // Getter / Setter
    // -------------------------------------------------------

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

    public Output1[] getOutput1() {
        return output1;
    }

    public void setOutput1(Output1[] output1) {
        this.output1 = output1;
    }

    @Override
    public String toString() {
        return "InquireDailyPriceResult{" +
                "rtCd='" + rtCd + '\'' +
                ", msgCd='" + msgCd + '\'' +
                ", msg1='" + msg1 + '\'' +
                '}';
    }

    // -------------------------------------------------------
    // 일자별 시세 한 건(Output1) 정의
    // -------------------------------------------------------

    public static class Output1 {

        /** 기준일자 (yyyyMMdd) */
        @JsonProperty("stck_bsop_date")
        private String stckBsopDate;

        /** 시가 */
        @JsonProperty("stck_oprc")
        private String stckOprc;

        /** 고가 */
        @JsonProperty("stck_hgpr")
        private String stckHgpr;

        /** 저가 */
        @JsonProperty("stck_lwpr")
        private String stckLwpr;

        /** 종가 */
        @JsonProperty("stck_clpr")
        private String stckClpr;

        /** 누적 거래량 */
        @JsonProperty("acml_vol")
        private String acmlVol;

        /** 전일 대비 */
        @JsonProperty("prdy_vrss")
        private String prdyVrss;

        /** 전일 대비 부호 (1:상승, 2:하락, 3:보합 등) */
        @JsonProperty("prdy_vrss_sign")
        private String prdyVrssSign;

        /** 전일 대비 등락률 */
        @JsonProperty("prdy_ctrt")
        private String prdyCtrt;

        /** 외국인 지분율 */
        @JsonProperty("hts_frgn_ehrt")
        private String htsFrgnEhrt;

        /** 외국인 순매수 수량 */
        @JsonProperty("frgn_ntby_qty")
        private String frgnNtbyQty;

        // 필요에 따라 아래에 다른 필드(피벗, 52주 고/저 등)도
        // 동일한 패턴으로 추가해서 사용하면 된다.

        public Output1() {
        }

        public String getStckBsopDate() {
            return stckBsopDate;
        }

        public void setStckBsopDate(String stckBsopDate) {
            this.stckBsopDate = stckBsopDate;
        }

        public String getStckOprc() {
            return stckOprc;
        }

        public void setStckOprc(String stckOprc) {
            this.stckOprc = stckOprc;
        }

        public String getStckHgpr() {
            return stckHgpr;
        }

        public void setStckHgpr(String stckHgpr) {
            this.stckHgpr = stckHgpr;
        }

        public String getStckLwpr() {
            return stckLwpr;
        }

        public void setStckLwpr(String stckLwpr) {
            this.stckLwpr = stckLwpr;
        }

        public String getStckClpr() {
            return stckClpr;
        }

        public void setStckClpr(String stckClpr) {
            this.stckClpr = stckClpr;
        }

        public String getAcmlVol() {
            return acmlVol;
        }

        public void setAcmlVol(String acmlVol) {
            this.acmlVol = acmlVol;
        }

        public String getPrdyVrss() {
            return prdyVrss;
        }

        public void setPrdyVrss(String prdyVrss) {
            this.prdyVrss = prdyVrss;
        }

        public String getPrdyVrssSign() {
            return prdyVrssSign;
        }

        public void setPrdyVrssSign(String prdyVrssSign) {
            this.prdyVrssSign = prdyVrssSign;
        }

        public String getPrdyCtrt() {
            return prdyCtrt;
        }

        public void setPrdyCtrt(String prdyCtrt) {
            this.prdyCtrt = prdyCtrt;
        }

        public String getHtsFrgnEhrt() {
            return htsFrgnEhrt;
        }

        public void setHtsFrgnEhrt(String htsFrgnEhrt) {
            this.htsFrgnEhrt = htsFrgnEhrt;
        }

        public String getFrgnNtbyQty() {
            return frgnNtbyQty;
        }

        public void setFrgnNtbyQty(String frgnNtbyQty) {
            this.frgnNtbyQty = frgnNtbyQty;
        }

        @Override
        public String toString() {
            return "Output1{" +
                    "stckBsopDate='" + stckBsopDate + '\'' +
                    ", stckOprc='" + stckOprc + '\'' +
                    ", stckHgpr='" + stckHgpr + '\'' +
                    ", stckLwpr='" + stckLwpr + '\'' +
                    ", stckClpr='" + stckClpr + '\'' +
                    ", acmlVol='" + acmlVol + '\'' +
                    ", prdyVrss='" + prdyVrss + '\'' +
                    ", prdyVrssSign='" + prdyVrssSign + '\'' +
                    ", prdyCtrt='" + prdyCtrt + '\'' +
                    ", htsFrgnEhrt='" + htsFrgnEhrt + '\'' +
                    ", frgnNtbyQty='" + frgnNtbyQty + '\'' +
                    '}';
        }
    }

}
