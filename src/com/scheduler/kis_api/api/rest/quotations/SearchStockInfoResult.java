package com.scheduler.kis_api.api.rest.quotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

/**
 * 주식기본조회 [v1_국내주식-067] 응답 결과
 *
 * Response Body 요약:
 *  - rt_cd, msg_cd, msg1
 *  - output (상품 기본 정보)
 */
@Getter
@ToString(callSuper = true)
public class SearchStockInfoResult extends CommonRestResult {

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

        /** 상품번호 */
        @JsonProperty("pdno")
        private String pdno;
        
    	/*
    	 * "AGR.농축산물파생 BON.채권파생 CMD.일반상품시장 CUR.통화파생 ENG.에너지파생 EQU.주식파생 ETF.ETF파생
    	 * IRT.금리파생 KNX.코넥스 KSQ.코스닥 MTL.금속파생 SPI.주가지수파생 STK.유가증권"
    	 */
        
        /** 상품유형코드 */
        @JsonProperty("prdt_type_cd")
        private String prdtTypeCd;

        /** 시장ID코드 */
        @JsonProperty("mket_id_cd")
        private String mketIdCd;

        /** 증권그룹ID코드 */
        @JsonProperty("scty_grp_id_cd")
        private String sctyGrpIdCd;

        /** 거래소구분코드 */
        @JsonProperty("excg_dvsn_cd")
        private String excgDvsnCd;

        /** 결산월일 (MMDD) */
        @JsonProperty("setl_mmdd")
        private String setlMmdd;

        /** 상장주수 */
        @JsonProperty("lstg_stqt")
        private String lstgStqt;

        /** 상장자본금액 */
        @JsonProperty("lstg_cptl_amt")
        private String lstgCptlAmt;

        /** 자본금 */
        @JsonProperty("cpta")
        private String cpta;

        /** 액면가 */
        @JsonProperty("papr")
        private String papr;

        /** 발행가격 */
        @JsonProperty("issu_pric")
        private String issuPric;

        /** 코스피200종목여부 */
        @JsonProperty("kospi200_item_yn")
        private String kospi200ItemYn;

        /** 유가증권시장상장일자 */
        @JsonProperty("scts_mket_lstg_dt")
        private String sctsMketLstgDt;

        /** 유가증권시장상장폐지일자 */
        @JsonProperty("scts_mket_lstg_abol_dt")
        private String sctsMketLstgAbolDt;

        /** 코스닥시장상장일자 */
        @JsonProperty("kosdaq_mket_lstg_dt")
        private String kosdaqMketLstgDt;

        /** 코스닥시장상장폐지일자 */
        @JsonProperty("kosdaq_mket_lstg_abol_dt")
        private String kosdaqMketLstgAbolDt;

        /** 예탁등록일자 */
        @JsonProperty("dpsi_erlm_dt")
        private String dpsiErlmDt;

        /** 예탁등록취소일자 */
        @JsonProperty("dpsi_erlm_cncl_dt")
        private String dpsiErlmCnclDt;

        /** ETFCU수량 */
        @JsonProperty("etf_cu_qty")
        private String etfCuQty;

        /** 상품명 */
        @JsonProperty("prdt_name")
        private String prdtName;

        /** 상품명120 */
        @JsonProperty("prdt_name120")
        private String prdtName120;

        /** 상품약어명 */
        @JsonProperty("prdt_abrv_name")
        private String prdtAbrvName;

        /** 표준상품번호 */
        @JsonProperty("std_pdno")
        private String stdPdno;

        /** 상품영문명 */
        @JsonProperty("prdt_eng_name")
        private String prdtEngName;

        /** 상품영문명120 */
        @JsonProperty("prdt_eng_name120")
        private String prdtEngName120;

        /** 상품영문약어명 */
        @JsonProperty("prdt_eng_abrv_name")
        private String prdtEngAbrvName;

        /** 예탁지정등록여부 */
        @JsonProperty("dpsi_aptm_erlm_yn")
        private String dpsiAptmErlmYn;

        /** ETF과세유형코드 */
        @JsonProperty("etf_txtn_type_cd")
        private String etfTxtnTypeCd;

        /** ETF유형코드 */
        @JsonProperty("etf_type_cd")
        private String etfTypeCd;

        /** 상장폐지일자 */
        @JsonProperty("lstg_abol_dt")
        private String lstgAbolDt;

        /** 신주구주구분코드 */
        @JsonProperty("nwst_odst_dvsn_cd")
        private String nwstOdstDvsnCd;

        /** 대용가격 */
        @JsonProperty("sbst_pric")
        private String sbstPric;

        /** 당사대용가격 */
        @JsonProperty("thco_sbst_pric")
        private String thcoSbstPric;

        /** 당사대용가격변경일자 */
        @JsonProperty("thco_sbst_pric_chng_dt")
        private String thcoSbstPricChngDt;

        /** 거래정지여부 */
        @JsonProperty("tr_stop_yn")
        private String trStopYn;

        /** 관리종목여부 */
        @JsonProperty("admn_item_yn")
        private String admnItemYn;

        /** 당일종가 */
        @JsonProperty("thdt_clpr")
        private String thdtClpr;

        /** 전일종가 */
        @JsonProperty("bfdy_clpr")
        private String bfdyClpr;

        /** 종가변경일자 */
        @JsonProperty("clpr_chng_dt")
        private String clprChngDt;

        /** 표준산업분류코드 */
        @JsonProperty("std_idst_clsf_cd")
        private String stdIdstClsfCd;

        /** 외국인보유수량 */
        @JsonProperty("frnr_hldn_qty")
        private String frnrHldnQty;

        /** 외국인한도수량 */
        @JsonProperty("frnr_lmt_qty")
        private String frnrLmtQty;

        /** 외국인한도총수량 */
        @JsonProperty("frnr_lmt_exhs_qty")
        private String frnrLmtExhsQty;

        /** 외국인한도비율 */
        @JsonProperty("frnr_lmt_rate")
        private String frnrLmtRate;

        /** 외국인보유비율 */
        @JsonProperty("frnr_hldn_rate")
        private String frnrHldnRate;

        /** 외국인개인한도비율 */
        @JsonProperty("frnr_psnl_lmt_rt")
        private String frnrPsnlLmtRt;

        /** 상장신청인발행기관코드 */
        @JsonProperty("lstg_rqsr_issu_istt_cd")
        private String lstgRqsrIssuIsttCd;

        /** 상장신청인종목코드 */
        @JsonProperty("lstg_rqsr_item_cd")
        private String lstgRqsrItemCd;

        /** 신탁기관발행기관코드 */
        @JsonProperty("trst_istt_issu_istt_cd")
        private String trstIsttIssuIsttCd;

        /** NXT 거래종목여부 */
        @JsonProperty("cptt_trad_tr_psbl_yn")
        private String cpttTradTrPsblYn;

        /** NXT 거래정지여부 */
        @JsonProperty("nxt_tr_stop_yn")
        private String nxtTrStopYn;

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

		public String getSctyGrpIdCd() {
			return sctyGrpIdCd;
		}

		public void setSctyGrpIdCd(String sctyGrpIdCd) {
			this.sctyGrpIdCd = sctyGrpIdCd;
		}

		public String getExcgDvsnCd() {
			return excgDvsnCd;
		}

		public void setExcgDvsnCd(String excgDvsnCd) {
			this.excgDvsnCd = excgDvsnCd;
		}

		public String getSetlMmdd() {
			return setlMmdd;
		}

		public void setSetlMmdd(String setlMmdd) {
			this.setlMmdd = setlMmdd;
		}

		public String getLstgStqt() {
			return lstgStqt;
		}

		public void setLstgStqt(String lstgStqt) {
			this.lstgStqt = lstgStqt;
		}

		public String getLstgCptlAmt() {
			return lstgCptlAmt;
		}

		public void setLstgCptlAmt(String lstgCptlAmt) {
			this.lstgCptlAmt = lstgCptlAmt;
		}

		public String getCpta() {
			return cpta;
		}

		public void setCpta(String cpta) {
			this.cpta = cpta;
		}

		public String getPapr() {
			return papr;
		}

		public void setPapr(String papr) {
			this.papr = papr;
		}

		public String getIssuPric() {
			return issuPric;
		}

		public void setIssuPric(String issuPric) {
			this.issuPric = issuPric;
		}

		public String getKospi200ItemYn() {
			return kospi200ItemYn;
		}

		public void setKospi200ItemYn(String kospi200ItemYn) {
			this.kospi200ItemYn = kospi200ItemYn;
		}

		public String getSctsMketLstgDt() {
			return sctsMketLstgDt;
		}

		public void setSctsMketLstgDt(String sctsMketLstgDt) {
			this.sctsMketLstgDt = sctsMketLstgDt;
		}

		public String getSctsMketLstgAbolDt() {
			return sctsMketLstgAbolDt;
		}

		public void setSctsMketLstgAbolDt(String sctsMketLstgAbolDt) {
			this.sctsMketLstgAbolDt = sctsMketLstgAbolDt;
		}

		public String getKosdaqMketLstgDt() {
			return kosdaqMketLstgDt;
		}

		public void setKosdaqMketLstgDt(String kosdaqMketLstgDt) {
			this.kosdaqMketLstgDt = kosdaqMketLstgDt;
		}

		public String getKosdaqMketLstgAbolDt() {
			return kosdaqMketLstgAbolDt;
		}

		public void setKosdaqMketLstgAbolDt(String kosdaqMketLstgAbolDt) {
			this.kosdaqMketLstgAbolDt = kosdaqMketLstgAbolDt;
		}

		public String getDpsiErlmDt() {
			return dpsiErlmDt;
		}

		public void setDpsiErlmDt(String dpsiErlmDt) {
			this.dpsiErlmDt = dpsiErlmDt;
		}

		public String getDpsiErlmCnclDt() {
			return dpsiErlmCnclDt;
		}

		public void setDpsiErlmCnclDt(String dpsiErlmCnclDt) {
			this.dpsiErlmCnclDt = dpsiErlmCnclDt;
		}

		public String getEtfCuQty() {
			return etfCuQty;
		}

		public void setEtfCuQty(String etfCuQty) {
			this.etfCuQty = etfCuQty;
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

		public String getDpsiAptmErlmYn() {
			return dpsiAptmErlmYn;
		}

		public void setDpsiAptmErlmYn(String dpsiAptmErlmYn) {
			this.dpsiAptmErlmYn = dpsiAptmErlmYn;
		}

		public String getEtfTxtnTypeCd() {
			return etfTxtnTypeCd;
		}

		public void setEtfTxtnTypeCd(String etfTxtnTypeCd) {
			this.etfTxtnTypeCd = etfTxtnTypeCd;
		}

		public String getEtfTypeCd() {
			return etfTypeCd;
		}

		public void setEtfTypeCd(String etfTypeCd) {
			this.etfTypeCd = etfTypeCd;
		}

		public String getLstgAbolDt() {
			return lstgAbolDt;
		}

		public void setLstgAbolDt(String lstgAbolDt) {
			this.lstgAbolDt = lstgAbolDt;
		}

		public String getNwstOdstDvsnCd() {
			return nwstOdstDvsnCd;
		}

		public void setNwstOdstDvsnCd(String nwstOdstDvsnCd) {
			this.nwstOdstDvsnCd = nwstOdstDvsnCd;
		}

		public String getSbstPric() {
			return sbstPric;
		}

		public void setSbstPric(String sbstPric) {
			this.sbstPric = sbstPric;
		}

		public String getThcoSbstPric() {
			return thcoSbstPric;
		}

		public void setThcoSbstPric(String thcoSbstPric) {
			this.thcoSbstPric = thcoSbstPric;
		}

		public String getThcoSbstPricChngDt() {
			return thcoSbstPricChngDt;
		}

		public void setThcoSbstPricChngDt(String thcoSbstPricChngDt) {
			this.thcoSbstPricChngDt = thcoSbstPricChngDt;
		}

		public String getTrStopYn() {
			return trStopYn;
		}

		public void setTrStopYn(String trStopYn) {
			this.trStopYn = trStopYn;
		}

		public String getAdmnItemYn() {
			return admnItemYn;
		}

		public void setAdmnItemYn(String admnItemYn) {
			this.admnItemYn = admnItemYn;
		}

		public String getThdtClpr() {
			return thdtClpr;
		}

		public void setThdtClpr(String thdtClpr) {
			this.thdtClpr = thdtClpr;
		}

		public String getBfdyClpr() {
			return bfdyClpr;
		}

		public void setBfdyClpr(String bfdyClpr) {
			this.bfdyClpr = bfdyClpr;
		}

		public String getClprChngDt() {
			return clprChngDt;
		}

		public void setClprChngDt(String clprChngDt) {
			this.clprChngDt = clprChngDt;
		}

		public String getStdIdstClsfCd() {
			return stdIdstClsfCd;
		}

		public void setStdIdstClsfCd(String stdIdstClsfCd) {
			this.stdIdstClsfCd = stdIdstClsfCd;
		}

		public String getFrnrHldnQty() {
			return frnrHldnQty;
		}

		public void setFrnrHldnQty(String frnrHldnQty) {
			this.frnrHldnQty = frnrHldnQty;
		}

		public String getFrnrLmtQty() {
			return frnrLmtQty;
		}

		public void setFrnrLmtQty(String frnrLmtQty) {
			this.frnrLmtQty = frnrLmtQty;
		}

		public String getFrnrLmtExhsQty() {
			return frnrLmtExhsQty;
		}

		public void setFrnrLmtExhsQty(String frnrLmtExhsQty) {
			this.frnrLmtExhsQty = frnrLmtExhsQty;
		}

		public String getFrnrLmtRate() {
			return frnrLmtRate;
		}

		public void setFrnrLmtRate(String frnrLmtRate) {
			this.frnrLmtRate = frnrLmtRate;
		}

		public String getFrnrHldnRate() {
			return frnrHldnRate;
		}

		public void setFrnrHldnRate(String frnrHldnRate) {
			this.frnrHldnRate = frnrHldnRate;
		}

		public String getFrnrPsnlLmtRt() {
			return frnrPsnlLmtRt;
		}

		public void setFrnrPsnlLmtRt(String frnrPsnlLmtRt) {
			this.frnrPsnlLmtRt = frnrPsnlLmtRt;
		}

		public String getLstgRqsrIssuIsttCd() {
			return lstgRqsrIssuIsttCd;
		}

		public void setLstgRqsrIssuIsttCd(String lstgRqsrIssuIsttCd) {
			this.lstgRqsrIssuIsttCd = lstgRqsrIssuIsttCd;
		}

		public String getLstgRqsrItemCd() {
			return lstgRqsrItemCd;
		}

		public void setLstgRqsrItemCd(String lstgRqsrItemCd) {
			this.lstgRqsrItemCd = lstgRqsrItemCd;
		}

		public String getTrstIsttIssuIsttCd() {
			return trstIsttIssuIsttCd;
		}

		public void setTrstIsttIssuIsttCd(String trstIsttIssuIsttCd) {
			this.trstIsttIssuIsttCd = trstIsttIssuIsttCd;
		}

		public String getCpttTradTrPsblYn() {
			return cpttTradTrPsblYn;
		}

		public void setCpttTradTrPsblYn(String cpttTradTrPsblYn) {
			this.cpttTradTrPsblYn = cpttTradTrPsblYn;
		}

		public String getNxtTrStopYn() {
			return nxtTrStopYn;
		}

		public void setNxtTrStopYn(String nxtTrStopYn) {
			this.nxtTrStopYn = nxtTrStopYn;
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
