package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class InquirePriceResult extends CommonRestResult {

    /** 성공 실패 여부 */
    private String rtCd;

    /** 응답코드 */
    private String msgCd;

    /** 응답메세지 */
    private String msg1;

    /** 응답상세 */
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

        /** 종목 상태 구분 코드 */
        private String iscdStatClsCode;

        /** 증거금 비율 */
        private String margRate;

        /** 대표 시장 한글 명 */
        private String rprsMrktKorName;

        /** 신 고가 저가 구분 코드 */
        private String newHgprLwprClsCode;

        /** 업종 한글 종목명 */
        private String bstpKorIsnm;

        /** 임시 정지 여부 */
        private String tempStopYn;

        /** 시가 범위 연장 여부 */
        private String oprcRangContYn;

        /** 종가 범위 연장 여부 */
        private String clprRangContYn;

        /** 신용 가능 여부 */
        private String crdtAbleYn;

        /** 보증금 비율 구분 코드 */
        private String grmnRateClsCode;

        /** ELW 발행 여부 */
        private String elwPblcYn;

        /** 주식 현재가 */
        private String stckPrpr;

        /** 전일 대비 */
        private String prdyVrss;

        /** 전일 대비 부호 */
        private String prdyVrssSign;

        /** 전일 대비율 */
        private String prdyCtrt;

        /** 누적 거래 대금 */
        private String acmlTrPbmn;

        /** 누적 거래량 */
        private String acmlVol;

        /** 전일 대비 거래량 비율 */
        private String prdyVrssVolRate;

        /** 주식 시가2 */
        private String stckOprc;

        /** 주식 최고가 */
        private String stckHgpr;

        /** 주식 최저가 */
        private String stckLwpr;

        /** 주식 상한가 */
        private String stckMxpr;

        /** 주식 하한가 */
        private String stckLlam;

        /** 주식 기준가 */
        private String stckSdpr;

        /** 가중 평균 주식 가격 */
        private String wghnAvrgStckPrc;

        /** HTS 외국인 소진율 */
        private String htsFrgnEhrt;

        /** 외국인 순매수 수량 */
        private String frgnNtbyQty;

        /** 프로그램매매 순매수 수량 */
        private String pgtrNtbyQty;

        /** 피벗 2차 디저항 가격 */
        private String pvtScndDmrsPrc;

        /** 피벗 1차 디저항 가격 */
        private String pvtFrstDmrsPrc;

        /** 피벗 포인트 값 */
        private String pvtPontVal;

        /** 피벗 1차 디지지 가격 */
        private String pvtFrstDmspPrc;

        /** 피벗 2차 디지지 가격 */
        private String pvtScndDmspPrc;

        /** 디저항 값 */
        private String dmrsVal;

        /** 디지지 값 */
        private String dmspVal;

        /** 자본금 */
        private String cpfn;

        /** 제한 폭 가격 */
        private String rstcWdthPrc;

        /** 주식 액면가 */
        private String stckFcam;

        /** 주식 대용가 */
        private String stckSspr;

        /** 호가단위 */
        private String asprUnit;

        /** HTS 매매 수량 단위 값 */
        private String htsDealQtyUnitVal;

        /** 상장 주수 */
        private String lstnStcn;

        /** HTS 시가총액 */
        private String htsAvls;

        /** PER */
        private String per;

        /** PBR */
        private String pbr;

        /** 결산 월 */
        private String stacMonth;

        /** 거래량 회전율 */
        private String volTnrt;

        /** EPS */
        private String eps;

        /** BPS */
        private String bps;

        /** 250일 최고가 */
        private String d250Hgpr;

        /** 250일 최고가 일자 */
        private String d250HgprDate;

        /** 250일 최고가 대비 현재가 비율 */
        private String d250HgprVrssPrprRate;

        /** 250일 최저가 */
        private String d250Lwpr;

        /** 250일 최저가 일자 */
        private String d250LwprDate;

        /** 250일 최저가 대비 현재가 비율 */
        private String d250LwprVrssPrprRate;

        /** 주식 연중 최고가 */
        private String stckDryyHgpr;

        /** 연중 최고가 대비 현재가 비율 */
        private String dryyHgprVrssPrprRate;

        /** 연중 최고가 일자 */
        private String dryyHgprDate;

        /** 주식 연중 최저가 */
        private String stckDryyLwpr;

        /** 연중 최저가 대비 현재가 비율 */
        private String dryyLwprVrssPrprRate;

        /** 연중 최저가 일자 */
        private String dryyLwprDate;

        /** 52주일 최고가 */
        private String w52Hgpr;

        /** 52주일 최고가 대비 현재가 대비 */
        private String w52HgprVrssPrprCtrt;

        /** 52주일 최고가 일자 */
        private String w52HgprDate;

        /** 52주일 최저가 */
        private String w52Lwpr;

        /** 52주일 최저가 대비 현재가 대비 */
        private String w52LwprVrssPrprCtrt;

        /** 52주일 최저가 일자 */
        private String w52LwprDate;

        /** 전체 융자 잔고 비율 */
        private String wholLoanRmndRate;

        /** 공매도가능여부 */
        private String sstsYn;

        /** 주식 단축 종목코드 */
        private String stckShrnIscd;
        
        /** 주식 종목명 */
        private String htSdtNm;

        /** 액면가 통화명 */
        private String fcamCnnm;

        /** 자본금 통화명 */
        private String cpfnCnnm;

        /** 접근도 */
        private String apprchRate;

        /** 외국인 보유 수량 */
        private String frgnHldnQty;

        /** VI적용구분코드 */
        private String viClsCode;

        /** 시간외단일가VI적용구분코드 */
        private String ovtmViClsCode;

        /** 최종 공매도 체결 수량 */
        private String lastSstsCntgQty;

        /** 투자유의여부 */
        private String invtCafulYn;

        /** 시장경고코드 */
        private String mrktWarnClsCode;

        /** 단기과열여부 */
        private String shortOverYn;

        /** 정리매매여부 */
        private String sltrYn;

        /** 관리종목여부 */
        private String mangIssuClsCode;

		public String getIscdStatClsCode() {
			return iscdStatClsCode;
		}

		public void setIscdStatClsCode(String iscdStatClsCode) {
			this.iscdStatClsCode = iscdStatClsCode;
		}

		public String getMargRate() {
			return margRate;
		}

		public void setMargRate(String margRate) {
			this.margRate = margRate;
		}

		public String getRprsMrktKorName() {
			return rprsMrktKorName;
		}

		public void setRprsMrktKorName(String rprsMrktKorName) {
			this.rprsMrktKorName = rprsMrktKorName;
		}

		public String getNewHgprLwprClsCode() {
			return newHgprLwprClsCode;
		}

		public void setNewHgprLwprClsCode(String newHgprLwprClsCode) {
			this.newHgprLwprClsCode = newHgprLwprClsCode;
		}

		public String getBstpKorIsnm() {
			return bstpKorIsnm;
		}

		public void setBstpKorIsnm(String bstpKorIsnm) {
			this.bstpKorIsnm = bstpKorIsnm;
		}

		public String getTempStopYn() {
			return tempStopYn;
		}

		public void setTempStopYn(String tempStopYn) {
			this.tempStopYn = tempStopYn;
		}

		public String getOprcRangContYn() {
			return oprcRangContYn;
		}

		public void setOprcRangContYn(String oprcRangContYn) {
			this.oprcRangContYn = oprcRangContYn;
		}

		public String getClprRangContYn() {
			return clprRangContYn;
		}

		public void setClprRangContYn(String clprRangContYn) {
			this.clprRangContYn = clprRangContYn;
		}

		public String getCrdtAbleYn() {
			return crdtAbleYn;
		}

		public void setCrdtAbleYn(String crdtAbleYn) {
			this.crdtAbleYn = crdtAbleYn;
		}

		public String getGrmnRateClsCode() {
			return grmnRateClsCode;
		}

		public void setGrmnRateClsCode(String grmnRateClsCode) {
			this.grmnRateClsCode = grmnRateClsCode;
		}

		public String getElwPblcYn() {
			return elwPblcYn;
		}

		public void setElwPblcYn(String elwPblcYn) {
			this.elwPblcYn = elwPblcYn;
		}

		public String getStckPrpr() {
			return stckPrpr;
		}

		public void setStckPrpr(String stckPrpr) {
			this.stckPrpr = stckPrpr;
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

		public String getAcmlTrPbmn() {
			return acmlTrPbmn;
		}

		public void setAcmlTrPbmn(String acmlTrPbmn) {
			this.acmlTrPbmn = acmlTrPbmn;
		}

		public String getAcmlVol() {
			return acmlVol;
		}

		public void setAcmlVol(String acmlVol) {
			this.acmlVol = acmlVol;
		}

		public String getPrdyVrssVolRate() {
			return prdyVrssVolRate;
		}

		public void setPrdyVrssVolRate(String prdyVrssVolRate) {
			this.prdyVrssVolRate = prdyVrssVolRate;
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

		public String getStckMxpr() {
			return stckMxpr;
		}

		public void setStckMxpr(String stckMxpr) {
			this.stckMxpr = stckMxpr;
		}

		public String getStckLlam() {
			return stckLlam;
		}

		public void setStckLlam(String stckLlam) {
			this.stckLlam = stckLlam;
		}

		public String getStckSdpr() {
			return stckSdpr;
		}

		public void setStckSdpr(String stckSdpr) {
			this.stckSdpr = stckSdpr;
		}

		public String getWghnAvrgStckPrc() {
			return wghnAvrgStckPrc;
		}

		public void setWghnAvrgStckPrc(String wghnAvrgStckPrc) {
			this.wghnAvrgStckPrc = wghnAvrgStckPrc;
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

		public String getPgtrNtbyQty() {
			return pgtrNtbyQty;
		}

		public void setPgtrNtbyQty(String pgtrNtbyQty) {
			this.pgtrNtbyQty = pgtrNtbyQty;
		}

		public String getPvtScndDmrsPrc() {
			return pvtScndDmrsPrc;
		}

		public void setPvtScndDmrsPrc(String pvtScndDmrsPrc) {
			this.pvtScndDmrsPrc = pvtScndDmrsPrc;
		}

		public String getPvtFrstDmrsPrc() {
			return pvtFrstDmrsPrc;
		}

		public void setPvtFrstDmrsPrc(String pvtFrstDmrsPrc) {
			this.pvtFrstDmrsPrc = pvtFrstDmrsPrc;
		}

		public String getPvtPontVal() {
			return pvtPontVal;
		}

		public void setPvtPontVal(String pvtPontVal) {
			this.pvtPontVal = pvtPontVal;
		}

		public String getPvtFrstDmspPrc() {
			return pvtFrstDmspPrc;
		}

		public void setPvtFrstDmspPrc(String pvtFrstDmspPrc) {
			this.pvtFrstDmspPrc = pvtFrstDmspPrc;
		}

		public String getPvtScndDmspPrc() {
			return pvtScndDmspPrc;
		}

		public void setPvtScndDmspPrc(String pvtScndDmspPrc) {
			this.pvtScndDmspPrc = pvtScndDmspPrc;
		}

		public String getDmrsVal() {
			return dmrsVal;
		}

		public void setDmrsVal(String dmrsVal) {
			this.dmrsVal = dmrsVal;
		}

		public String getDmspVal() {
			return dmspVal;
		}

		public void setDmspVal(String dmspVal) {
			this.dmspVal = dmspVal;
		}

		public String getCpfn() {
			return cpfn;
		}

		public void setCpfn(String cpfn) {
			this.cpfn = cpfn;
		}

		public String getRstcWdthPrc() {
			return rstcWdthPrc;
		}

		public void setRstcWdthPrc(String rstcWdthPrc) {
			this.rstcWdthPrc = rstcWdthPrc;
		}

		public String getStckFcam() {
			return stckFcam;
		}

		public void setStckFcam(String stckFcam) {
			this.stckFcam = stckFcam;
		}

		public String getStckSspr() {
			return stckSspr;
		}

		public void setStckSspr(String stckSspr) {
			this.stckSspr = stckSspr;
		}

		public String getAsprUnit() {
			return asprUnit;
		}

		public void setAsprUnit(String asprUnit) {
			this.asprUnit = asprUnit;
		}

		public String getHtsDealQtyUnitVal() {
			return htsDealQtyUnitVal;
		}

		public void setHtsDealQtyUnitVal(String htsDealQtyUnitVal) {
			this.htsDealQtyUnitVal = htsDealQtyUnitVal;
		}

		public String getLstnStcn() {
			return lstnStcn;
		}

		public void setLstnStcn(String lstnStcn) {
			this.lstnStcn = lstnStcn;
		}

		public String getHtsAvls() {
			return htsAvls;
		}

		public void setHtsAvls(String htsAvls) {
			this.htsAvls = htsAvls;
		}

		public String getPer() {
			return per;
		}

		public void setPer(String per) {
			this.per = per;
		}

		public String getPbr() {
			return pbr;
		}

		public void setPbr(String pbr) {
			this.pbr = pbr;
		}

		public String getStacMonth() {
			return stacMonth;
		}

		public void setStacMonth(String stacMonth) {
			this.stacMonth = stacMonth;
		}

		public String getVolTnrt() {
			return volTnrt;
		}

		public void setVolTnrt(String volTnrt) {
			this.volTnrt = volTnrt;
		}

		public String getEps() {
			return eps;
		}

		public void setEps(String eps) {
			this.eps = eps;
		}

		public String getBps() {
			return bps;
		}

		public void setBps(String bps) {
			this.bps = bps;
		}

		public String getD250Hgpr() {
			return d250Hgpr;
		}

		public void setD250Hgpr(String d250Hgpr) {
			this.d250Hgpr = d250Hgpr;
		}

		public String getD250HgprDate() {
			return d250HgprDate;
		}

		public void setD250HgprDate(String d250HgprDate) {
			this.d250HgprDate = d250HgprDate;
		}

		public String getD250HgprVrssPrprRate() {
			return d250HgprVrssPrprRate;
		}

		public void setD250HgprVrssPrprRate(String d250HgprVrssPrprRate) {
			this.d250HgprVrssPrprRate = d250HgprVrssPrprRate;
		}

		public String getD250Lwpr() {
			return d250Lwpr;
		}

		public void setD250Lwpr(String d250Lwpr) {
			this.d250Lwpr = d250Lwpr;
		}

		public String getD250LwprDate() {
			return d250LwprDate;
		}

		public void setD250LwprDate(String d250LwprDate) {
			this.d250LwprDate = d250LwprDate;
		}

		public String getD250LwprVrssPrprRate() {
			return d250LwprVrssPrprRate;
		}

		public void setD250LwprVrssPrprRate(String d250LwprVrssPrprRate) {
			this.d250LwprVrssPrprRate = d250LwprVrssPrprRate;
		}

		public String getStckDryyHgpr() {
			return stckDryyHgpr;
		}

		public void setStckDryyHgpr(String stckDryyHgpr) {
			this.stckDryyHgpr = stckDryyHgpr;
		}

		public String getDryyHgprVrssPrprRate() {
			return dryyHgprVrssPrprRate;
		}

		public void setDryyHgprVrssPrprRate(String dryyHgprVrssPrprRate) {
			this.dryyHgprVrssPrprRate = dryyHgprVrssPrprRate;
		}

		public String getDryyHgprDate() {
			return dryyHgprDate;
		}

		public void setDryyHgprDate(String dryyHgprDate) {
			this.dryyHgprDate = dryyHgprDate;
		}

		public String getStckDryyLwpr() {
			return stckDryyLwpr;
		}

		public void setStckDryyLwpr(String stckDryyLwpr) {
			this.stckDryyLwpr = stckDryyLwpr;
		}

		public String getDryyLwprVrssPrprRate() {
			return dryyLwprVrssPrprRate;
		}

		public void setDryyLwprVrssPrprRate(String dryyLwprVrssPrprRate) {
			this.dryyLwprVrssPrprRate = dryyLwprVrssPrprRate;
		}

		public String getDryyLwprDate() {
			return dryyLwprDate;
		}

		public void setDryyLwprDate(String dryyLwprDate) {
			this.dryyLwprDate = dryyLwprDate;
		}

		public String getW52Hgpr() {
			return w52Hgpr;
		}

		public void setW52Hgpr(String w52Hgpr) {
			this.w52Hgpr = w52Hgpr;
		}

		public String getW52HgprVrssPrprCtrt() {
			return w52HgprVrssPrprCtrt;
		}

		public void setW52HgprVrssPrprCtrt(String w52HgprVrssPrprCtrt) {
			this.w52HgprVrssPrprCtrt = w52HgprVrssPrprCtrt;
		}

		public String getW52HgprDate() {
			return w52HgprDate;
		}

		public void setW52HgprDate(String w52HgprDate) {
			this.w52HgprDate = w52HgprDate;
		}

		public String getW52Lwpr() {
			return w52Lwpr;
		}

		public void setW52Lwpr(String w52Lwpr) {
			this.w52Lwpr = w52Lwpr;
		}

		public String getW52LwprVrssPrprCtrt() {
			return w52LwprVrssPrprCtrt;
		}

		public void setW52LwprVrssPrprCtrt(String w52LwprVrssPrprCtrt) {
			this.w52LwprVrssPrprCtrt = w52LwprVrssPrprCtrt;
		}

		public String getW52LwprDate() {
			return w52LwprDate;
		}

		public void setW52LwprDate(String w52LwprDate) {
			this.w52LwprDate = w52LwprDate;
		}

		public String getWholLoanRmndRate() {
			return wholLoanRmndRate;
		}

		public void setWholLoanRmndRate(String wholLoanRmndRate) {
			this.wholLoanRmndRate = wholLoanRmndRate;
		}

		public String getSstsYn() {
			return sstsYn;
		}

		public void setSstsYn(String sstsYn) {
			this.sstsYn = sstsYn;
		}

		public String getStckShrnIscd() {
			return stckShrnIscd;
		}

		public void setStckShrnIscd(String stckShrnIscd) {
			this.stckShrnIscd = stckShrnIscd;
		}

		public String getHtSdtNm() {
			return htSdtNm;
		}

		public void setHtSdtNm(String htSdtNm) {
			this.htSdtNm = htSdtNm;
		}

		public String getFcamCnnm() {
			return fcamCnnm;
		}

		public void setFcamCnnm(String fcamCnnm) {
			this.fcamCnnm = fcamCnnm;
		}

		public String getCpfnCnnm() {
			return cpfnCnnm;
		}

		public void setCpfnCnnm(String cpfnCnnm) {
			this.cpfnCnnm = cpfnCnnm;
		}

		public String getApprchRate() {
			return apprchRate;
		}

		public void setApprchRate(String apprchRate) {
			this.apprchRate = apprchRate;
		}

		public String getFrgnHldnQty() {
			return frgnHldnQty;
		}

		public void setFrgnHldnQty(String frgnHldnQty) {
			this.frgnHldnQty = frgnHldnQty;
		}

		public String getViClsCode() {
			return viClsCode;
		}

		public void setViClsCode(String viClsCode) {
			this.viClsCode = viClsCode;
		}

		public String getOvtmViClsCode() {
			return ovtmViClsCode;
		}

		public void setOvtmViClsCode(String ovtmViClsCode) {
			this.ovtmViClsCode = ovtmViClsCode;
		}

		public String getLastSstsCntgQty() {
			return lastSstsCntgQty;
		}

		public void setLastSstsCntgQty(String lastSstsCntgQty) {
			this.lastSstsCntgQty = lastSstsCntgQty;
		}

		public String getInvtCafulYn() {
			return invtCafulYn;
		}

		public void setInvtCafulYn(String invtCafulYn) {
			this.invtCafulYn = invtCafulYn;
		}

		public String getMrktWarnClsCode() {
			return mrktWarnClsCode;
		}

		public void setMrktWarnClsCode(String mrktWarnClsCode) {
			this.mrktWarnClsCode = mrktWarnClsCode;
		}

		public String getShortOverYn() {
			return shortOverYn;
		}

		public void setShortOverYn(String shortOverYn) {
			this.shortOverYn = shortOverYn;
		}

		public String getSltrYn() {
			return sltrYn;
		}

		public void setSltrYn(String sltrYn) {
			this.sltrYn = sltrYn;
		}

		public String getMangIssuClsCode() {
			return mangIssuClsCode;
		}

		public void setMangIssuClsCode(String mangIssuClsCode) {
			this.mangIssuClsCode = mangIssuClsCode;
		}
        
        
    }

}
