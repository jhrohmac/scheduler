package com.scheduler.kis_api.api.rest.quotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

/**
 * 상품기본조회 [v1_국내주식-029] 응답 결과
 *
 * KIS 예시 응답 형태 (요약):
 *
 * {
 *   "output": {
 *     "pdno": "005930",
 *     "prdt_type_cd": "300",
 *     "prdt_name": "삼성전자",
 *     "prdt_name120": "...",
 *     "prdt_abrv_name": "삼성전자",
 *     "prdt_eng_name": "SAMSUNG ELECTRONICS",
 *     "prdt_eng_name120": "...",
 *     "prdt_eng_abrv_name": "SEC",
 *     "std_pdno": "...",
 *     "shtn_pdno": "005930",
 *     "prdt_sale_stat_cd": "...",
 *     "prdt_risk_grad_cd": "...",
 *     "prdt_clsf_cd": "...",
 *     "prdt_clsf_name": "...",
 *     "sale_strt_dt": "YYYYMMDD",
 *     "sale_end_dt": "YYYYMMDD",
 *     "wrap_asst_type_cd": "...",
 *     "ivst_prdt_type_cd": "...",
 *     "ivst_prdt_type_cd_name": "...",
 *     "frst_erlm_dt": "YYYYMMDD"
 *   },
 *   "rt_cd": "0",
 *   "msg_cd": "KIOK0530",
 *   "msg1": "조회되었습니다 ..."
 * }
 */
@Getter
@ToString(callSuper = true)
public class SearchInfoResult extends CommonRestResult {

    /** 성공 실패 여부 */
    @JsonProperty("rt_cd")
    private String rtCd;

    /** 응답코드 */
    @JsonProperty("msg_cd")
    private String msgCd;

    /** 응답메세지 */
    @JsonProperty("msg1")
    private String msg1;

    /** 상품 기본 정보 */
    private Output output;

    /**
     * output 객체 (상품 기본 정보)
     */
    @Getter
    @ToString
    public static class Output {

        /** 상품번호 */
        private String pdno;

        /** 상품유형코드 */
        @JsonProperty("prdt_type_cd")
        private String prdtTypeCd;
        
        /** 시장ID코드 */
        @JsonProperty("mket_id_cd")
        private String mketIdCd;

        /** 상품명 */
        @JsonProperty("prdt_name")
        private String prdtName;

        /** 상품명120 */
        @JsonProperty("prdt_name120")
        private String prdtName120;

        /** 상품약어명 */
        @JsonProperty("prdt_abrv_name")
        private String prdtAbrvName;

        /** 상품영문명 */
        @JsonProperty("prdt_eng_name")
        private String prdtEngName;

        /** 상품영문명120 */
        @JsonProperty("prdt_eng_name120")
        private String prdtEngName120;

        /** 상품영문약어명 */
        @JsonProperty("prdt_eng_abrv_name")
        private String prdtEngAbrvName;

        /** 표준상품번호 */
        @JsonProperty("std_pdno")
        private String stdPdno;

        /** 단축상품번호 */
        @JsonProperty("shtn_pdno")
        private String shtnPdno;

        /** 상품판매상태코드 */
        @JsonProperty("prdt_sale_stat_cd")
        private String prdtSaleStatCd;

        /** 상품위험등급코드 */
        @JsonProperty("prdt_risk_grad_cd")
        private String prdtRiskGradCd;

        /** 상품분류코드 */
        @JsonProperty("prdt_clsf_cd")
        private String prdtClsfCd;

        /** 상품분류명 */
        @JsonProperty("prdt_clsf_name")
        private String prdtClsfName;

        /** 판매시작일자 (YYYYMMDD) */
        @JsonProperty("sale_strt_dt")
        private String saleStrtDt;

        /** 판매종료일자 (YYYYMMDD) */
        @JsonProperty("sale_end_dt")
        private String saleEndDt;

        /** 랩어카운트자산유형코드 */
        @JsonProperty("wrap_asst_type_cd")
        private String wrapAsstTypeCd;

        /** 투자상품유형코드 */
        @JsonProperty("ivst_prdt_type_cd")
        private String ivstPrdtTypeCd;

        /** 투자상품유형코드명 */
        @JsonProperty("ivst_prdt_type_cd_name")
        private String ivstPrdtTypeCdName;

        /** 최초등록일자 */
        @JsonProperty("frst_erlm_dt")
        private String frstErlmDt;

		public String getPdno() {
			return pdno;
		}

		public void setPdno(String pdno) {
			this.pdno = pdno;
		}

		public String getPrdtTypeCd() {
			return prdtTypeCd;
		}

		public void setPrdtTypeCd(String prdtTypeCd) {
			this.prdtTypeCd = prdtTypeCd;
		}

		public String getMketIdCd() {
			return mketIdCd;
		}

		public void setMketIdCd(String mketIdCd) {
			this.mketIdCd = mketIdCd;
		}

		public String getPrdtName() {
			return prdtName;
		}

		public void setPrdtName(String prdtName) {
			this.prdtName = prdtName;
		}

		public String getPrdtName120() {
			return prdtName120;
		}

		public void setPrdtName120(String prdtName120) {
			this.prdtName120 = prdtName120;
		}

		public String getPrdtAbrvName() {
			return prdtAbrvName;
		}

		public void setPrdtAbrvName(String prdtAbrvName) {
			this.prdtAbrvName = prdtAbrvName;
		}

		public String getPrdtEngName() {
			return prdtEngName;
		}

		public void setPrdtEngName(String prdtEngName) {
			this.prdtEngName = prdtEngName;
		}

		public String getPrdtEngName120() {
			return prdtEngName120;
		}

		public void setPrdtEngName120(String prdtEngName120) {
			this.prdtEngName120 = prdtEngName120;
		}

		public String getPrdtEngAbrvName() {
			return prdtEngAbrvName;
		}

		public void setPrdtEngAbrvName(String prdtEngAbrvName) {
			this.prdtEngAbrvName = prdtEngAbrvName;
		}

		public String getStdPdno() {
			return stdPdno;
		}

		public void setStdPdno(String stdPdno) {
			this.stdPdno = stdPdno;
		}

		public String getShtnPdno() {
			return shtnPdno;
		}

		public void setShtnPdno(String shtnPdno) {
			this.shtnPdno = shtnPdno;
		}

		public String getPrdtSaleStatCd() {
			return prdtSaleStatCd;
		}

		public void setPrdtSaleStatCd(String prdtSaleStatCd) {
			this.prdtSaleStatCd = prdtSaleStatCd;
		}

		public String getPrdtRiskGradCd() {
			return prdtRiskGradCd;
		}

		public void setPrdtRiskGradCd(String prdtRiskGradCd) {
			this.prdtRiskGradCd = prdtRiskGradCd;
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

		public String getSaleStrtDt() {
			return saleStrtDt;
		}

		public void setSaleStrtDt(String saleStrtDt) {
			this.saleStrtDt = saleStrtDt;
		}

		public String getSaleEndDt() {
			return saleEndDt;
		}

		public void setSaleEndDt(String saleEndDt) {
			this.saleEndDt = saleEndDt;
		}

		public String getWrapAsstTypeCd() {
			return wrapAsstTypeCd;
		}

		public void setWrapAsstTypeCd(String wrapAsstTypeCd) {
			this.wrapAsstTypeCd = wrapAsstTypeCd;
		}

		public String getIvstPrdtTypeCd() {
			return ivstPrdtTypeCd;
		}

		public void setIvstPrdtTypeCd(String ivstPrdtTypeCd) {
			this.ivstPrdtTypeCd = ivstPrdtTypeCd;
		}

		public String getIvstPrdtTypeCdName() {
			return ivstPrdtTypeCdName;
		}

		public void setIvstPrdtTypeCdName(String ivstPrdtTypeCdName) {
			this.ivstPrdtTypeCdName = ivstPrdtTypeCdName;
		}

		public String getFrstErlmDt() {
			return frstErlmDt;
		}

		public void setFrstErlmDt(String frstErlmDt) {
			this.frstErlmDt = frstErlmDt;
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
