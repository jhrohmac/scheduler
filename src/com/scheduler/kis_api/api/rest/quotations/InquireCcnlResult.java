package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class InquireCcnlResult extends CommonRestResult {

    /** 성공 실패 여부 */
    private String rtCd;

    /** 응답코드 */
    private String msgCd;

    /** 응답메세지 */
    private String msg1;

    /** 응답상세 */
    private Output[] output;

    @Getter
    @ToString
    public static class Output {

        /** 주식 체결 시간 */
        private String stckCntgHour;

        /** 주식 현재가 */
        private String stckPrpr;

        /** 전일 대비 */
        private String prdyVrss;

        /** 전일 대비 부호 */
        private String prdyVrssSign;

        /** 체결 거래량 */
        private String cntgVol;

        /** 당일 체결강도 */
        private String tdayRltv;

        /** 전일 대비율 */
        private String prdyCtrt;

		public String getStckCntgHour() {
			return stckCntgHour;
		}

		public void setStckCntgHour(String stckCntgHour) {
			this.stckCntgHour = stckCntgHour;
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

		public String getCntgVol() {
			return cntgVol;
		}

		public void setCntgVol(String cntgVol) {
			this.cntgVol = cntgVol;
		}

		public String getTdayRltv() {
			return tdayRltv;
		}

		public void setTdayRltv(String tdayRltv) {
			this.tdayRltv = tdayRltv;
		}

		public String getPrdyCtrt() {
			return prdyCtrt;
		}

		public void setPrdyCtrt(String prdyCtrt) {
			this.prdyCtrt = prdyCtrt;
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

	public Output[] getOutput() {
		return output;
	}

	public void setOutput(Output[] output) {
		this.output = output;
	}

}
