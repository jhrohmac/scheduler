package com.scheduler.kis_api.api.rest.quotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

/**
 * 해외주식 상품기본정보 [v1_해외주식-034] 응답 결과
 *
 * Response Body 요약:
 *  - rt_cd, msg_cd, msg1
 *  - output (상품 기본 정보)
 */
@Getter
@ToString(callSuper = true)
public class SearchOverseasStockInfoResult extends CommonRestResult {

    /** 성공 실패 여부 (0: 성공) */
    @JsonProperty("rt_cd")
    private String rtCd;

    /** 응답코드 */
    @JsonProperty("msg_cd")
    private String msgCd;

    /** 응답메시지 */
    @JsonProperty("msg1")
    private String msg1;

    /** 상품 기본 정보 */
    private Output output;

    @Getter
    @ToString
    public static class Output {

        @JsonProperty("std_pdno")
        private String stdPdno;

        @JsonProperty("prdt_eng_name")
        private String prdtEngName;

        @JsonProperty("natn_cd")
        private String natnCd;

        @JsonProperty("natn_name")
        private String natnName;

        @JsonProperty("tr_mket_cd")
        private String trMketCd;

        @JsonProperty("tr_mket_name")
        private String trMketName;

        @JsonProperty("ovrs_excg_cd")
        private String ovrsExcgCd;

        @JsonProperty("ovrs_excg_name")
        private String ovrsExcgName;

        @JsonProperty("tr_crcy_cd")
        private String trCrcyCd;

        @JsonProperty("crcy_name")
        private String crcyName;

        @JsonProperty("ovrs_stck_dvsn_cd")
        private String ovrsStckDvsnCd;

        @JsonProperty("prdt_clsf_cd")
        private String prdtClsfCd;

        @JsonProperty("prdt_clsf_name")
        private String prdtClsfName;

        @JsonProperty("lstg_abol_item_yn")
        private String lstgAbolItemYn;

        @JsonProperty("lstg_abol_dt")
        private String lstgAbolDt;

        @JsonProperty("lstg_yn")
        private String lstgYn;

        @JsonProperty("ovrs_item_name")
        private String ovrsItemName;

        public String getStdPdno() {
            return stdPdno;
        }

        public void setStdPdno(String stdPdno) {
            this.stdPdno = stdPdno;
        }

        public String getPrdtEngName() {
            return prdtEngName;
        }

        public void setPrdtEngName(String prdtEngName) {
            this.prdtEngName = prdtEngName;
        }

        public String getNatnCd() {
            return natnCd;
        }

        public void setNatnCd(String natnCd) {
            this.natnCd = natnCd;
        }

        public String getNatnName() {
            return natnName;
        }

        public void setNatnName(String natnName) {
            this.natnName = natnName;
        }

        public String getTrMketCd() {
            return trMketCd;
        }

        public void setTrMketCd(String trMketCd) {
            this.trMketCd = trMketCd;
        }

        public String getTrMketName() {
            return trMketName;
        }

        public void setTrMketName(String trMketName) {
            this.trMketName = trMketName;
        }

        public String getOvrsExcgCd() {
            return ovrsExcgCd;
        }

        public void setOvrsExcgCd(String ovrsExcgCd) {
            this.ovrsExcgCd = ovrsExcgCd;
        }

        public String getOvrsExcgName() {
            return ovrsExcgName;
        }

        public void setOvrsExcgName(String ovrsExcgName) {
            this.ovrsExcgName = ovrsExcgName;
        }

        public String getTrCrcyCd() {
            return trCrcyCd;
        }

        public void setTrCrcyCd(String trCrcyCd) {
            this.trCrcyCd = trCrcyCd;
        }

        public String getCrcyName() {
            return crcyName;
        }

        public void setCrcyName(String crcyName) {
            this.crcyName = crcyName;
        }

        public String getOvrsStckDvsnCd() {
            return ovrsStckDvsnCd;
        }

        public void setOvrsStckDvsnCd(String ovrsStckDvsnCd) {
            this.ovrsStckDvsnCd = ovrsStckDvsnCd;
        }

        public String getPrdtClsfCd() {
            return prdtClsfCd;
        }

        public void setPrdtClsfCd(String prdtClsfCd) {
            this.prdtClsfCd = prdtClsfCd;
        }

        public String getPrdtClsfName() {
            return prdtClsfName;
        }

        public void setPrdtClsfName(String prdtClsfName) {
            this.prdtClsfName = prdtClsfName;
        }

        public String getLstgAbolItemYn() {
            return lstgAbolItemYn;
        }

        public void setLstgAbolItemYn(String lstgAbolItemYn) {
            this.lstgAbolItemYn = lstgAbolItemYn;
        }

        public String getLstgAbolDt() {
            return lstgAbolDt;
        }

        public void setLstgAbolDt(String lstgAbolDt) {
            this.lstgAbolDt = lstgAbolDt;
        }

        public String getLstgYn() {
            return lstgYn;
        }

        public void setLstgYn(String lstgYn) {
            this.lstgYn = lstgYn;
        }

        public String getOvrsItemName() {
            return ovrsItemName;
        }

        public void setOvrsItemName(String ovrsItemName) {
            this.ovrsItemName = ovrsItemName;
        }
    }

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
}
