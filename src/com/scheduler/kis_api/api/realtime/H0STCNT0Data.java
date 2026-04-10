package com.scheduler.kis_api.api.realtime;

import com.scheduler.kis_client.api.RealTimeApiData;
import com.scheduler.kis_client.api.annotation.Seq;

import lombok.Data;
import lombok.ToString;

/**
 * 국내주식 실시간체결가 (KRX) [실시간-003] 실시간 데이터
 */
@Data
@ToString(callSuper = true)
public class H0STCNT0Data implements RealTimeApiData {

    /** 유가증권 단축 종목코드 */
    @Seq(1)
    private String mkscShrnIscd;

    /** 주식 체결 시간 */
    @Seq(2)
    private String stckCntgHour;

    /** 주식 현재가 */
    @Seq(3)
    private Number stckPrpr;

    /** 전일 대비 부호 */
    @Seq(4)
    private String prdyVrssSign;

    /** 전일 대비 */
    @Seq(5)
    private Number prdyVrss;

    /** 전일 대비율 */
    @Seq(6)
    private Number prdyCtrt;

    /** 가중 평균 주식 가격 */
    @Seq(7)
    private Number wghnAvrgStckPrc;

    /** 주식 시가 */
    @Seq(8)
    private Number stckOprc;

    /** 주식 최고가 */
    @Seq(9)
    private Number stckHgpr;

    /** 주식 최저가 */
    @Seq(10)
    private Number stckLwpr;

    /** 매도호가1 */
    @Seq(11)
    private Number askp1;

    /** 매수호가1 */
    @Seq(12)
    private Number bidp1;

    /** 체결 거래량 */
    @Seq(13)
    private Number cntgVol;

    /** 누적 거래량 */
    @Seq(14)
    private Number acmlVol;

    /** 누적 거래 대금 */
    @Seq(15)
    private Number acmlTrPbmn;

    /** 매도 체결 건수 */
    @Seq(16)
    private Number selnCntgCsnu;

    /** 매수 체결 건수 */
    @Seq(17)
    private Number shnuCntgCsnu;

    /** 순매수 체결 건수 */
    @Seq(18)
    private Number ntbyCntgCsnu;

    /** 체결강도 */
    @Seq(19)
    private Number cttr;

    /** 총 매도 수량 */
    @Seq(20)
    private Number selnCntgSmtn;

    /** 총 매수 수량 */
    @Seq(21)
    private Number shnuCntgSmtn;

    /** 체결구분 */
    @Seq(22)
    private String ccldDvsn;

    /** 매수비율 */
    @Seq(23)
    private Number shnuRate;

    /** 전일 거래량 대비 등락율 */
    @Seq(24)
    private Number prdyVolVrssAcmlVolRate;

    /** 시가 시간 */
    @Seq(25)
    private String oprcHour;

    /** 시가대비구분 */
    @Seq(26)
    private String oprcVrssPrprSign;

    /** 시가대비 */
    @Seq(27)
    private Number oprcVrssPrpr;

    /** 최고가 시간 */
    @Seq(28)
    private String hgprHour;

    /** 고가대비구분 */
    @Seq(29)
    private String hgprVrssPrprSign;

    /** 고가대비 */
    @Seq(30)
    private Number hgprVrssPrpr;

    /** 최저가 시간 */
    @Seq(31)
    private String lwprHour;

    /** 저가대비구분 */
    @Seq(32)
    private String lwprVrssPrprSign;

    /** 저가대비 */
    @Seq(33)
    private Number lwprVrssPrpr;

    /** 영업 일자 */
    @Seq(34)
    private String bsopDate;

    /** 신 장운영 구분 코드 */
    @Seq(35)
    private String newMkopClsCode;

    /** 거래정지 여부 */
    @Seq(36)
    private String trhtYn;

    /** 매도호가 잔량1 */
    @Seq(37)
    private Number askpRsqn1;

    /** 매수호가 잔량1 */
    @Seq(38)
    private Number bidpRsqn1;

    /** 총 매도호가 잔량 */
    @Seq(39)
    private Number totalAskpRsqn;

    /** 총 매수호가 잔량 */
    @Seq(40)
    private Number totalBidpRsqn;

    /** 거래량 회전율 */
    @Seq(41)
    private Number volTnrt;

    /** 전일 동시간 누적 거래량 */
    @Seq(42)
    private Number prdySmnsHourAcmlVol;

    /** 전일 동시간 누적 거래량 비율 */
    @Seq(43)
    private Number prdySmnsHourAcmlVolRate;

    /** 시간 구분 코드 */
    @Seq(44)
    private String hourClsCode;

    /** 임의종료구분코드 */
    @Seq(45)
    private String mrktTrtmClsCode;

    /** 정적VI발동기준가 */
    @Seq(46)
    private Number viStndPrc;

	public String getMkscShrnIscd() {
		return mkscShrnIscd;
	}

	public void setMkscShrnIscd(String mkscShrnIscd) {
		this.mkscShrnIscd = mkscShrnIscd;
	}

	public String getStckCntgHour() {
		return stckCntgHour;
	}

	public void setStckCntgHour(String stckCntgHour) {
		this.stckCntgHour = stckCntgHour;
	}

	public Number getStckPrpr() {
		return stckPrpr;
	}

	public void setStckPrpr(Number stckPrpr) {
		this.stckPrpr = stckPrpr;
	}

	public String getPrdyVrssSign() {
		return prdyVrssSign;
	}

	public void setPrdyVrssSign(String prdyVrssSign) {
		this.prdyVrssSign = prdyVrssSign;
	}

	public Number getPrdyVrss() {
		return prdyVrss;
	}

	public void setPrdyVrss(Number prdyVrss) {
		this.prdyVrss = prdyVrss;
	}

	public Number getPrdyCtrt() {
		return prdyCtrt;
	}

	public void setPrdyCtrt(Number prdyCtrt) {
		this.prdyCtrt = prdyCtrt;
	}

	public Number getWghnAvrgStckPrc() {
		return wghnAvrgStckPrc;
	}

	public void setWghnAvrgStckPrc(Number wghnAvrgStckPrc) {
		this.wghnAvrgStckPrc = wghnAvrgStckPrc;
	}

	public Number getStckOprc() {
		return stckOprc;
	}

	public void setStckOprc(Number stckOprc) {
		this.stckOprc = stckOprc;
	}

	public Number getStckHgpr() {
		return stckHgpr;
	}

	public void setStckHgpr(Number stckHgpr) {
		this.stckHgpr = stckHgpr;
	}

	public Number getStckLwpr() {
		return stckLwpr;
	}

	public void setStckLwpr(Number stckLwpr) {
		this.stckLwpr = stckLwpr;
	}

	public Number getAskp1() {
		return askp1;
	}

	public void setAskp1(Number askp1) {
		this.askp1 = askp1;
	}

	public Number getBidp1() {
		return bidp1;
	}

	public void setBidp1(Number bidp1) {
		this.bidp1 = bidp1;
	}

	public Number getCntgVol() {
		return cntgVol;
	}

	public void setCntgVol(Number cntgVol) {
		this.cntgVol = cntgVol;
	}

	public Number getAcmlVol() {
		return acmlVol;
	}

	public void setAcmlVol(Number acmlVol) {
		this.acmlVol = acmlVol;
	}

	public Number getAcmlTrPbmn() {
		return acmlTrPbmn;
	}

	public void setAcmlTrPbmn(Number acmlTrPbmn) {
		this.acmlTrPbmn = acmlTrPbmn;
	}

	public Number getSelnCntgCsnu() {
		return selnCntgCsnu;
	}

	public void setSelnCntgCsnu(Number selnCntgCsnu) {
		this.selnCntgCsnu = selnCntgCsnu;
	}

	public Number getShnuCntgCsnu() {
		return shnuCntgCsnu;
	}

	public void setShnuCntgCsnu(Number shnuCntgCsnu) {
		this.shnuCntgCsnu = shnuCntgCsnu;
	}

	public Number getNtbyCntgCsnu() {
		return ntbyCntgCsnu;
	}

	public void setNtbyCntgCsnu(Number ntbyCntgCsnu) {
		this.ntbyCntgCsnu = ntbyCntgCsnu;
	}

	public Number getCttr() {
		return cttr;
	}

	public void setCttr(Number cttr) {
		this.cttr = cttr;
	}

	public Number getSelnCntgSmtn() {
		return selnCntgSmtn;
	}

	public void setSelnCntgSmtn(Number selnCntgSmtn) {
		this.selnCntgSmtn = selnCntgSmtn;
	}

	public Number getShnuCntgSmtn() {
		return shnuCntgSmtn;
	}

	public void setShnuCntgSmtn(Number shnuCntgSmtn) {
		this.shnuCntgSmtn = shnuCntgSmtn;
	}

	public String getCcldDvsn() {
		return ccldDvsn;
	}

	public void setCcldDvsn(String ccldDvsn) {
		this.ccldDvsn = ccldDvsn;
	}

	public Number getShnuRate() {
		return shnuRate;
	}

	public void setShnuRate(Number shnuRate) {
		this.shnuRate = shnuRate;
	}

	public Number getPrdyVolVrssAcmlVolRate() {
		return prdyVolVrssAcmlVolRate;
	}

	public void setPrdyVolVrssAcmlVolRate(Number prdyVolVrssAcmlVolRate) {
		this.prdyVolVrssAcmlVolRate = prdyVolVrssAcmlVolRate;
	}

	public String getOprcHour() {
		return oprcHour;
	}

	public void setOprcHour(String oprcHour) {
		this.oprcHour = oprcHour;
	}

	public String getOprcVrssPrprSign() {
		return oprcVrssPrprSign;
	}

	public void setOprcVrssPrprSign(String oprcVrssPrprSign) {
		this.oprcVrssPrprSign = oprcVrssPrprSign;
	}

	public Number getOprcVrssPrpr() {
		return oprcVrssPrpr;
	}

	public void setOprcVrssPrpr(Number oprcVrssPrpr) {
		this.oprcVrssPrpr = oprcVrssPrpr;
	}

	public String getHgprHour() {
		return hgprHour;
	}

	public void setHgprHour(String hgprHour) {
		this.hgprHour = hgprHour;
	}

	public String getHgprVrssPrprSign() {
		return hgprVrssPrprSign;
	}

	public void setHgprVrssPrprSign(String hgprVrssPrprSign) {
		this.hgprVrssPrprSign = hgprVrssPrprSign;
	}

	public Number getHgprVrssPrpr() {
		return hgprVrssPrpr;
	}

	public void setHgprVrssPrpr(Number hgprVrssPrpr) {
		this.hgprVrssPrpr = hgprVrssPrpr;
	}

	public String getLwprHour() {
		return lwprHour;
	}

	public void setLwprHour(String lwprHour) {
		this.lwprHour = lwprHour;
	}

	public String getLwprVrssPrprSign() {
		return lwprVrssPrprSign;
	}

	public void setLwprVrssPrprSign(String lwprVrssPrprSign) {
		this.lwprVrssPrprSign = lwprVrssPrprSign;
	}

	public Number getLwprVrssPrpr() {
		return lwprVrssPrpr;
	}

	public void setLwprVrssPrpr(Number lwprVrssPrpr) {
		this.lwprVrssPrpr = lwprVrssPrpr;
	}

	public String getBsopDate() {
		return bsopDate;
	}

	public void setBsopDate(String bsopDate) {
		this.bsopDate = bsopDate;
	}

	public String getNewMkopClsCode() {
		return newMkopClsCode;
	}

	public void setNewMkopClsCode(String newMkopClsCode) {
		this.newMkopClsCode = newMkopClsCode;
	}

	public String getTrhtYn() {
		return trhtYn;
	}

	public void setTrhtYn(String trhtYn) {
		this.trhtYn = trhtYn;
	}

	public Number getAskpRsqn1() {
		return askpRsqn1;
	}

	public void setAskpRsqn1(Number askpRsqn1) {
		this.askpRsqn1 = askpRsqn1;
	}

	public Number getBidpRsqn1() {
		return bidpRsqn1;
	}

	public void setBidpRsqn1(Number bidpRsqn1) {
		this.bidpRsqn1 = bidpRsqn1;
	}

	public Number getTotalAskpRsqn() {
		return totalAskpRsqn;
	}

	public void setTotalAskpRsqn(Number totalAskpRsqn) {
		this.totalAskpRsqn = totalAskpRsqn;
	}

	public Number getTotalBidpRsqn() {
		return totalBidpRsqn;
	}

	public void setTotalBidpRsqn(Number totalBidpRsqn) {
		this.totalBidpRsqn = totalBidpRsqn;
	}

	public Number getVolTnrt() {
		return volTnrt;
	}

	public void setVolTnrt(Number volTnrt) {
		this.volTnrt = volTnrt;
	}

	public Number getPrdySmnsHourAcmlVol() {
		return prdySmnsHourAcmlVol;
	}

	public void setPrdySmnsHourAcmlVol(Number prdySmnsHourAcmlVol) {
		this.prdySmnsHourAcmlVol = prdySmnsHourAcmlVol;
	}

	public Number getPrdySmnsHourAcmlVolRate() {
		return prdySmnsHourAcmlVolRate;
	}

	public void setPrdySmnsHourAcmlVolRate(Number prdySmnsHourAcmlVolRate) {
		this.prdySmnsHourAcmlVolRate = prdySmnsHourAcmlVolRate;
	}

	public String getHourClsCode() {
		return hourClsCode;
	}

	public void setHourClsCode(String hourClsCode) {
		this.hourClsCode = hourClsCode;
	}

	public String getMrktTrtmClsCode() {
		return mrktTrtmClsCode;
	}

	public void setMrktTrtmClsCode(String mrktTrtmClsCode) {
		this.mrktTrtmClsCode = mrktTrtmClsCode;
	}

	public Number getViStndPrc() {
		return viStndPrc;
	}

	public void setViStndPrc(Number viStndPrc) {
		this.viStndPrc = viStndPrc;
	}

}
