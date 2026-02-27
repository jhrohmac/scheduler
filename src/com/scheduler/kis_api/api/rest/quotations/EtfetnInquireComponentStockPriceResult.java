package com.scheduler.kis_api.api.rest.quotations;

import com.scheduler.kis_api.api.CommonRestResult;

// NOTE
// - 프로젝트 일부 환경에서 Lombok 애노테이션 프로세싱이 비활성화되어
//   @Getter/@ToString 등이 동작하지 않는 사례가 있어
//   컴파일/실행 안정성을 위해 Getter를 직접 구현한다.

public class EtfetnInquireComponentStockPriceResult extends CommonRestResult {

    /** 성공 실패 여부 */
    private String rtCd;

    /** 응답코드 */
    private String msgCd;

    /** 응답메세지 */
    private String msg1;

    /** 응답상세 */
    private Output1 output1;

    /** 응답상세 (배열) */
    private Output2[] output2;

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

    public static class Output1 {

        /** 매매 일자 */
        private String stckPrpr;

        /** 주식 현재가 */
        private String prdyVrss;

        /** 전일 대비 부호 */
        private String prdyVrssSign;

        /** 전일 대비 */
        private String prdyCtrt;

        /** 전일 대비율 */
        private String etfCnfgIssuAvls;

        /** 누적 거래량 */
        private String nav;

        /** 결제 일자 */
        private String navPrdyVrssSign;

        /** 전체 융자 신규 주수 */
        private String navPrdyVrss;

        /** 전체 융자 상환 주수 */
        private String navPrdyCtrt;

        /** 전체 융자 잔고 주수 */
        private String etfNtasTtam;

        /** 전체 융자 신규 금액 */
        private String prdyClprNav;

        /** 전체 융자 상환 금액 */
        private String oprcNav;

        /** 전체 융자 잔고 금액 */
        private String hprcNav;

        /** 전체 융자 잔고 비율 */
        private String lprcNav;

        /** 전체 융자 공여율 */
        private String etfCuUnitScrtCnt;

        /** 전체 대주 신규 주수 */
        private String etfCnfgIssuCnt;
    }

    public static class Output2 {

        /** 주식 단축 종목코드 */
        private String stckShrnIscd;

        /** HTS 한글 종목명 */
        private String htsKorIsnm;

        /** 주식 현재가 */
        private String stckPrpr;

        /** 전일 대비 */
        private String prdyVrss;

        /** 전일 대비 부호 */
        private String prdyVrssSign;

        /** 전일 대비율 */
        private String prdyCtrt;

        /** 누적 거래량 */
        private String acmlVol;

        /** 누적 거래 대금 */
        private String acmlTrPbmn;

        /** 당일 등락 비율 */
        private String tdayRsflRate;

        /** 전일 대비 거래량 */
        private String prdyVrssVol;

        /** 거래대금회전율 */
        private String trPbmnTnrt;

        /** HTS 시가총액 */
        private String htsAvls;

        /** ETF구성종목시가총액 */
        private String etfCnfgIssuAvls;

        /** ETF구성종목비중 */
        private String etfCnfgIssuRlim;

        /** ETF구성종목내평가금액 */
        private String etfVltnAmt;

        public String getStckShrnIscd() {
            return stckShrnIscd;
        }

        public String getHtsKorIsnm() {
            return htsKorIsnm;
        }

        public String getStckPrpr() {
            return stckPrpr;
        }

        public String getPrdyVrss() {
            return prdyVrss;
        }

        public String getPrdyVrssSign() {
            return prdyVrssSign;
        }

        public String getPrdyCtrt() {
            return prdyCtrt;
        }

        public String getAcmlVol() {
            return acmlVol;
        }

        public String getAcmlTrPbmn() {
            return acmlTrPbmn;
        }

        public String getTdayRsflRate() {
            return tdayRsflRate;
        }

        public String getPrdyVrssVol() {
            return prdyVrssVol;
        }

        public String getTrPbmnTnrt() {
            return trPbmnTnrt;
        }

        public String getHtsAvls() {
            return htsAvls;
        }

        public String getEtfCnfgIssuAvls() {
            return etfCnfgIssuAvls;
        }

        public String getEtfCnfgIssuRlim() {
            return etfCnfgIssuRlim;
        }

        public String getEtfVltnAmt() {
            return etfVltnAmt;
        }
    }

}
