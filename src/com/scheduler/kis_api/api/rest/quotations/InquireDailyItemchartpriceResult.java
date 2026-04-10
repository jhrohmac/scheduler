package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class InquireDailyItemchartpriceResult extends CommonRestResult {

    /** 성공 실패 여부 */
    private String rtCd;

    /** 응답코드 */
    private String msgCd;

    /** 응답메세지 */
    private String msg1;

    /** 응답상세1 */
    private Output1 output1;

    /** 응답상세2 */
    private Output2[] output2;
    
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

	public Output1 getOutput1() {
		return output1;
	}

	public void setOutput1(Output1 output1) {
		this.output1 = output1;
	}

	public Output2[] getOutput2() {
		return output2;
	}

	public void setOutput2(Output2[] output2) {
		this.output2 = output2;
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

        /** 주식 단축 종목코드 */
        private String stckShrnIscd;

        /** 전일 거래량 */
        private String prdyVol;

        /** 주식 상한가 */
        private String stckMxpr;

        /** 주식 하한가 */
        private String stckLlam;

        /** 주식 시가2 */
        private String stckOprc;

        /** 주식 최고가 */
        private String stckHgpr;

        /** 주식 최저가 */
        private String stckLwpr;

        /** 주식 전일 시가 */
        private String stckPrdyOprc;

        /** 주식 전일 최고가 */
        private String stckPrdyHgpr;

        /** 주식 전일 최저가 */
        private String stckPrdyLwpr;

        /** 매도호가 */
        private String askp;

        /** 매수호가 */
        private String bidp;

        /** 전일 대비 거래량 */
        private String prdyVrssVol;

        /** 거래량 회전율 */
        private String volTnrt;

        /** 주식 액면가 */
        private String stckFcam;

        /** 상장 주수 */
        private String lstnStcn;

        /** 자본금 */
        private String cpfn;

        /** HTS 시가총액 */
        private String htsAvls;

        /** PER */
        private String per;

        /** EPS */
        private String eps;

        /** PBR */
        private String pbr;

        /** 전체 융자 잔고 비율 */
        private String itewholLoanRmndRatem;

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

		public String getStckPrdyClpr() {
			return stckPrdyClpr;
		}

		public void setStckPrdyClpr(String stckPrdyClpr) {
			this.stckPrdyClpr = stckPrdyClpr;
		}

		public String getAcmlVol() {
			return acmlVol;
		}

		public void setAcmlVol(String acmlVol) {
			this.acmlVol = acmlVol;
		}

		public String getAcmlTrPbmn() {
			return acmlTrPbmn;
		}

		public void setAcmlTrPbmn(String acmlTrPbmn) {
			this.acmlTrPbmn = acmlTrPbmn;
		}

		public String getHtsKorIsnm() {
			return htsKorIsnm;
		}

		public void setHtsKorIsnm(String htsKorIsnm) {
			this.htsKorIsnm = htsKorIsnm;
		}

		public String getStckPrpr() {
			return stckPrpr;
		}

		public void setStckPrpr(String stckPrpr) {
			this.stckPrpr = stckPrpr;
		}

		public String getStckShrnIscd() {
			return stckShrnIscd;
		}

		public void setStckShrnIscd(String stckShrnIscd) {
			this.stckShrnIscd = stckShrnIscd;
		}

		public String getPrdyVol() {
			return prdyVol;
		}

		public void setPrdyVol(String prdyVol) {
			this.prdyVol = prdyVol;
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

		public String getStckPrdyOprc() {
			return stckPrdyOprc;
		}

		public void setStckPrdyOprc(String stckPrdyOprc) {
			this.stckPrdyOprc = stckPrdyOprc;
		}

		public String getStckPrdyHgpr() {
			return stckPrdyHgpr;
		}

		public void setStckPrdyHgpr(String stckPrdyHgpr) {
			this.stckPrdyHgpr = stckPrdyHgpr;
		}

		public String getStckPrdyLwpr() {
			return stckPrdyLwpr;
		}

		public void setStckPrdyLwpr(String stckPrdyLwpr) {
			this.stckPrdyLwpr = stckPrdyLwpr;
		}

		public String getAskp() {
			return askp;
		}

		public void setAskp(String askp) {
			this.askp = askp;
		}

		public String getBidp() {
			return bidp;
		}

		public void setBidp(String bidp) {
			this.bidp = bidp;
		}

		public String getPrdyVrssVol() {
			return prdyVrssVol;
		}

		public void setPrdyVrssVol(String prdyVrssVol) {
			this.prdyVrssVol = prdyVrssVol;
		}

		public String getVolTnrt() {
			return volTnrt;
		}

		public void setVolTnrt(String volTnrt) {
			this.volTnrt = volTnrt;
		}

		public String getStckFcam() {
			return stckFcam;
		}

		public void setStckFcam(String stckFcam) {
			this.stckFcam = stckFcam;
		}

		public String getLstnStcn() {
			return lstnStcn;
		}

		public void setLstnStcn(String lstnStcn) {
			this.lstnStcn = lstnStcn;
		}

		public String getCpfn() {
			return cpfn;
		}

		public void setCpfn(String cpfn) {
			this.cpfn = cpfn;
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

		public String getEps() {
			return eps;
		}

		public void setEps(String eps) {
			this.eps = eps;
		}

		public String getPbr() {
			return pbr;
		}

		public void setPbr(String pbr) {
			this.pbr = pbr;
		}

		public String getItewholLoanRmndRatem() {
			return itewholLoanRmndRatem;
		}

		public void setItewholLoanRmndRatem(String itewholLoanRmndRatem) {
			this.itewholLoanRmndRatem = itewholLoanRmndRatem;
		}
    }

    @Getter
    @ToString
    public static class Output2 {

        /** 전일 대비 */
        private String stckBsopDate;

        /** 전일 대비 부호 */
        private String stckClpr;

        /** 전일 대비율 */
        private String stckOprc;

        /** 주식 전일 종가 */
        private String stckHgpr;

        /** 누적 거래량 */
        private String stckLwpr;

        /** 누적 거래 대금 */
        private String acmlVol;

        /** HTS 한글 종목명 */
        private String acmlTrPbmn;

        /** 주식 현재가 */
        private String flngClsCode;

        /** 주식 단축 종목코드 */
        private String prttRate;

        /** 전일 거래량 */
        private String modYn;

        /** 주식 상한가 */
        private String prdyVrssSign;

        /** 주식 하한가 */
        private String prdyVrss;

        /** 주식 시가2 */
        private String revlIssuReas;

		public String getStckBsopDate() {
			return stckBsopDate;
		}

		public void setStckBsopDate(String stckBsopDate) {
			this.stckBsopDate = stckBsopDate;
		}

		public String getStckClpr() {
			return stckClpr;
		}

		public void setStckClpr(String stckClpr) {
			this.stckClpr = stckClpr;
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

		public String getAcmlVol() {
			return acmlVol;
		}

		public void setAcmlVol(String acmlVol) {
			this.acmlVol = acmlVol;
		}

		public String getAcmlTrPbmn() {
			return acmlTrPbmn;
		}

		public void setAcmlTrPbmn(String acmlTrPbmn) {
			this.acmlTrPbmn = acmlTrPbmn;
		}

		public String getFlngClsCode() {
			return flngClsCode;
		}

		public void setFlngClsCode(String flngClsCode) {
			this.flngClsCode = flngClsCode;
		}

		public String getPrttRate() {
			return prttRate;
		}

		public void setPrttRate(String prttRate) {
			this.prttRate = prttRate;
		}

		public String getModYn() {
			return modYn;
		}

		public void setModYn(String modYn) {
			this.modYn = modYn;
		}

		public String getPrdyVrssSign() {
			return prdyVrssSign;
		}

		public void setPrdyVrssSign(String prdyVrssSign) {
			this.prdyVrssSign = prdyVrssSign;
		}

		public String getPrdyVrss() {
			return prdyVrss;
		}

		public void setPrdyVrss(String prdyVrss) {
			this.prdyVrss = prdyVrss;
		}

		public String getRevlIssuReas() {
			return revlIssuReas;
		}

		public void setRevlIssuReas(String revlIssuReas) {
			this.revlIssuReas = revlIssuReas;
		}
    }
}
