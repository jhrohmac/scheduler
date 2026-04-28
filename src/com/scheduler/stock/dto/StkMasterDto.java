package com.scheduler.stock.dto;

/**
 * TB_STK_MASTER MERGE/조회용 DTO
 *
 * stkStatus 허용값: NORMAL / HALT / CAUTION / DELIST
 * indexCd   허용값: KOSPI200 / KOSDAQ150 / SNP500 / DOW / null (하위호환 유지)
 */
public class StkMasterDto {

    /* ── 기본 식별 ── */
    private String stkCd;         // 종목코드 (PK)
    private String stkNm;         // 한글종목명
    private String stkNmEn;       // 영문종목명
    private String mktCd;         // 상장시장: KOSPI/KOSDAQ/NASDAQ/NYSE
    private String stkStatus;     // NORMAL/HALT/CAUTION/DELIST
    private String stkType;       // STOCK / ETF / ETN / ELW
    private String indexCd;       // 주요지수편입 (하위호환)
    private String sectorCd;      // 업종대분류코드 4자리
    private String mktCapSize;    // 시가총액규모 1=대형 2=중형 3=소형
    private String prefYn;        // 우선주여부
    private String settleMonth;   // 결산월
    private String currency;      // 통화코드
    private String listDt;        // 상장일자 YYYYMMDD

    /* ── 그룹1: 공통 ── */
    private String scrtGrpClsCd;  // 증권그룹구분코드 (ST/ETF/EW/MF 등)
    private String symbol;        // 심볼 (해외종목 별도 심볼)

    /* ── 그룹2: 해외종목 전용 ── */
    private String natnCd;        // 국가구분코드
    private String exchCd;        // 거래소코드
    private String ovrsBstpCd;    // 해외업종분류코드
    private String dow30Yn;       // 다우30 편입여부
    private String nas100Yn;      // 나스닥100 편입여부
    private String snp500Yn;      // S&P500 편입여부

    /* ── 그룹3: 업종분류 ── */
    private String bstpMedmDivCd; // 지수업종중분류코드
    private String bstpSmalDivCd; // 지수업종소분류코드

    /* ── 그룹3: 지수편입 플래그 ── */
    private String kospi200SectCd; // KOSPI200섹터업종코드
    private String kospi100Yn;
    private String kospi50Yn;
    private String kospiYn;        // KOSPI지수 편입여부
    private String kosdaq150Yn;
    private String krxYn;
    private String krx100Yn;
    private String krx300Yn;
    private String krxCarYn;
    private String krxSmcnYn;
    private String krxBioYn;
    private String krxBankYn;
    private String krxEnrgChmsYn;
    private String krxStelYn;
    private String krxMediCmncYn;
    private String krxCnstYn;
    private String krxScrtYn;
    private String krxShipYn;
    private String krxInsuYn;
    private String krxTrnsYn;
    private String sriYn;
    private String etpClsCd;
    private String elwPblcYn;
    private String spacYn;

    /* ── 그룹3: 종목특성 ── */
    private String mninYn;         // 제조업여부 (코스피)
    private String lowCurrentYn;   // 저유동성여부
    private String sprnNmixYn;     // 지배구조지수종목여부 (코스피)
    private String ventYn;         // 벤처기업여부 (코스닥)
    private String invtCatnYn;     // 투자주의환기여부 (코스닥)

    /* ── 그룹3: 거래경보 ── */
    private String sltrYn;         // 정리매매여부
    private String mangYn;         // 관리종목여부
    private String shortOverClsCd; // 단기과열구분코드
    private String mrktAlrmClsCd;  // 시장경고구분코드
    private String mrktAlrmRiskYn; // 시장경고위험예고여부
    private String insnPbntYn;     // 불성실공시여부
    private String bypsLstnYn;     // 우회상장여부
    private String flngClsCd;      // 락구분코드
    private String fcamModClsCd;   // 액면변경구분코드
    private String icicClsCd;      // 증자구분코드
    private String sstsHotYn;      // 공매도과열여부
    private String stangeRunupYn;  // 이상급등여부

    /* ── 그룹3: 신용/거래조건 ── */
    private String margRate;           // 증거금비율
    private String crdtAble;           // 신용가능여부
    private String crdtDays;           // 신용기간
    private String coCrdtLimtOverYn;   // 회사신용한도초과여부
    private String secuLendAbleYn;     // 담보대출가능여부
    private String stlnAbleYn;         // 대주가능여부

    /* ── 그룹3: 기본수치 ── */
    private String stckSdpr;               // 주식기준가
    private String frmlMrktDealQtyUnit;    // 정규시장매매수량단위
    private String ovtmMrktDealQtyUnit;    // 시간외시장매매수량단위
    private String prdyVol;               // 전일거래량
    private String stckFcam;              // 주식액면가
    private String lstnStcn;              // 상장주수(천)
    private String cpfn;                  // 자본금
    private String poPrc;                 // 공모가격

    /* ── 그룹3: 재무정보 ── */
    private String saleAccount;    // 매출액
    private String bsopPrfi;       // 영업이익
    private String opPrfi;         // 경상이익
    private String thtrNtin;       // 당기순이익
    private String roeVal;         // ROE
    private String baseDt;         // 기준년월 YYYYMM
    private String prdyAvlsScal;   // 전일기준시가총액(억)
    private String coCd;           // 그룹사코드

    /* ── 페이징 행번호 ── */
    private int rnum;
    public int getRnum() { return rnum; }
    public void setRnum(int v) { this.rnum = v; }


    /* ════════════════ getters / setters ════════════════ */

    public String getStkCd() { return stkCd; }
    public void setStkCd(String v) { this.stkCd = v; }

    public String getStkNm() { return stkNm; }
    public void setStkNm(String v) { this.stkNm = v; }

    public String getStkNmEn() { return stkNmEn; }
    public void setStkNmEn(String v) { this.stkNmEn = v; }

    public String getMktCd() { return mktCd; }
    public void setMktCd(String v) { this.mktCd = v; }

    public String getStkStatus() { return stkStatus; }
    public void setStkStatus(String v) { this.stkStatus = v; }

    public String getStkType() { return stkType; }
    public void setStkType(String v) { this.stkType = v; }

    public String getIndexCd() { return indexCd; }
    public void setIndexCd(String v) { this.indexCd = v; }

    public String getSectorCd() { return sectorCd; }
    public void setSectorCd(String v) { this.sectorCd = v; }

    public String getMktCapSize() { return mktCapSize; }
    public void setMktCapSize(String v) { this.mktCapSize = v; }

    public String getPrefYn() { return prefYn; }
    public void setPrefYn(String v) { this.prefYn = v; }

    public String getSettleMonth() { return settleMonth; }
    public void setSettleMonth(String v) { this.settleMonth = v; }

    public String getCurrency() { return currency; }
    public void setCurrency(String v) { this.currency = v; }

    public String getListDt() { return listDt; }
    public void setListDt(String v) { this.listDt = v; }

    public String getScrtGrpClsCd() { return scrtGrpClsCd; }
    public void setScrtGrpClsCd(String v) { this.scrtGrpClsCd = v; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String v) { this.symbol = v; }

    public String getNatnCd() { return natnCd; }
    public void setNatnCd(String v) { this.natnCd = v; }

    public String getExchCd() { return exchCd; }
    public void setExchCd(String v) { this.exchCd = v; }

    public String getOvrsBstpCd() { return ovrsBstpCd; }
    public void setOvrsBstpCd(String v) { this.ovrsBstpCd = v; }

    public String getDow30Yn() { return dow30Yn; }
    public void setDow30Yn(String v) { this.dow30Yn = v; }

    public String getNas100Yn() { return nas100Yn; }
    public void setNas100Yn(String v) { this.nas100Yn = v; }

    public String getSnp500Yn() { return snp500Yn; }
    public void setSnp500Yn(String v) { this.snp500Yn = v; }

    public String getBstpMedmDivCd() { return bstpMedmDivCd; }
    public void setBstpMedmDivCd(String v) { this.bstpMedmDivCd = v; }

    public String getBstpSmalDivCd() { return bstpSmalDivCd; }
    public void setBstpSmalDivCd(String v) { this.bstpSmalDivCd = v; }

    public String getKospi200SectCd() { return kospi200SectCd; }
    public void setKospi200SectCd(String v) { this.kospi200SectCd = v; }

    public String getKospi100Yn() { return kospi100Yn; }
    public void setKospi100Yn(String v) { this.kospi100Yn = v; }

    public String getKospi50Yn() { return kospi50Yn; }
    public void setKospi50Yn(String v) { this.kospi50Yn = v; }

    public String getKospiYn() { return kospiYn; }
    public void setKospiYn(String v) { this.kospiYn = v; }

    public String getKosdaq150Yn() { return kosdaq150Yn; }
    public void setKosdaq150Yn(String v) { this.kosdaq150Yn = v; }

    public String getKrxYn() { return krxYn; }
    public void setKrxYn(String v) { this.krxYn = v; }

    public String getKrx100Yn() { return krx100Yn; }
    public void setKrx100Yn(String v) { this.krx100Yn = v; }

    public String getKrx300Yn() { return krx300Yn; }
    public void setKrx300Yn(String v) { this.krx300Yn = v; }

    public String getKrxCarYn() { return krxCarYn; }
    public void setKrxCarYn(String v) { this.krxCarYn = v; }

    public String getKrxSmcnYn() { return krxSmcnYn; }
    public void setKrxSmcnYn(String v) { this.krxSmcnYn = v; }

    public String getKrxBioYn() { return krxBioYn; }
    public void setKrxBioYn(String v) { this.krxBioYn = v; }

    public String getKrxBankYn() { return krxBankYn; }
    public void setKrxBankYn(String v) { this.krxBankYn = v; }

    public String getKrxEnrgChmsYn() { return krxEnrgChmsYn; }
    public void setKrxEnrgChmsYn(String v) { this.krxEnrgChmsYn = v; }

    public String getKrxStelYn() { return krxStelYn; }
    public void setKrxStelYn(String v) { this.krxStelYn = v; }

    public String getKrxMediCmncYn() { return krxMediCmncYn; }
    public void setKrxMediCmncYn(String v) { this.krxMediCmncYn = v; }

    public String getKrxCnstYn() { return krxCnstYn; }
    public void setKrxCnstYn(String v) { this.krxCnstYn = v; }

    public String getKrxScrtYn() { return krxScrtYn; }
    public void setKrxScrtYn(String v) { this.krxScrtYn = v; }

    public String getKrxShipYn() { return krxShipYn; }
    public void setKrxShipYn(String v) { this.krxShipYn = v; }

    public String getKrxInsuYn() { return krxInsuYn; }
    public void setKrxInsuYn(String v) { this.krxInsuYn = v; }

    public String getKrxTrnsYn() { return krxTrnsYn; }
    public void setKrxTrnsYn(String v) { this.krxTrnsYn = v; }

    public String getSriYn() { return sriYn; }
    public void setSriYn(String v) { this.sriYn = v; }

    public String getEtpClsCd() { return etpClsCd; }
    public void setEtpClsCd(String v) { this.etpClsCd = v; }

    public String getElwPblcYn() { return elwPblcYn; }
    public void setElwPblcYn(String v) { this.elwPblcYn = v; }

    public String getSpacYn() { return spacYn; }
    public void setSpacYn(String v) { this.spacYn = v; }

    public String getMninYn() { return mninYn; }
    public void setMninYn(String v) { this.mninYn = v; }

    public String getLowCurrentYn() { return lowCurrentYn; }
    public void setLowCurrentYn(String v) { this.lowCurrentYn = v; }

    public String getSprnNmixYn() { return sprnNmixYn; }
    public void setSprnNmixYn(String v) { this.sprnNmixYn = v; }

    public String getVentYn() { return ventYn; }
    public void setVentYn(String v) { this.ventYn = v; }

    public String getInvtCatnYn() { return invtCatnYn; }
    public void setInvtCatnYn(String v) { this.invtCatnYn = v; }

    public String getSltrYn() { return sltrYn; }
    public void setSltrYn(String v) { this.sltrYn = v; }

    public String getMangYn() { return mangYn; }
    public void setMangYn(String v) { this.mangYn = v; }

    public String getShortOverClsCd() { return shortOverClsCd; }
    public void setShortOverClsCd(String v) { this.shortOverClsCd = v; }

    public String getMrktAlrmClsCd() { return mrktAlrmClsCd; }
    public void setMrktAlrmClsCd(String v) { this.mrktAlrmClsCd = v; }

    public String getMrktAlrmRiskYn() { return mrktAlrmRiskYn; }
    public void setMrktAlrmRiskYn(String v) { this.mrktAlrmRiskYn = v; }

    public String getInsnPbntYn() { return insnPbntYn; }
    public void setInsnPbntYn(String v) { this.insnPbntYn = v; }

    public String getBypsLstnYn() { return bypsLstnYn; }
    public void setBypsLstnYn(String v) { this.bypsLstnYn = v; }

    public String getFlngClsCd() { return flngClsCd; }
    public void setFlngClsCd(String v) { this.flngClsCd = v; }

    public String getFcamModClsCd() { return fcamModClsCd; }
    public void setFcamModClsCd(String v) { this.fcamModClsCd = v; }

    public String getIcicClsCd() { return icicClsCd; }
    public void setIcicClsCd(String v) { this.icicClsCd = v; }

    public String getSstsHotYn() { return sstsHotYn; }
    public void setSstsHotYn(String v) { this.sstsHotYn = v; }

    public String getStangeRunupYn() { return stangeRunupYn; }
    public void setStangeRunupYn(String v) { this.stangeRunupYn = v; }

    public String getMargRate() { return margRate; }
    public void setMargRate(String v) { this.margRate = v; }

    public String getCrdtAble() { return crdtAble; }
    public void setCrdtAble(String v) { this.crdtAble = v; }

    public String getCrdtDays() { return crdtDays; }
    public void setCrdtDays(String v) { this.crdtDays = v; }

    public String getCoCrdtLimtOverYn() { return coCrdtLimtOverYn; }
    public void setCoCrdtLimtOverYn(String v) { this.coCrdtLimtOverYn = v; }

    public String getSecuLendAbleYn() { return secuLendAbleYn; }
    public void setSecuLendAbleYn(String v) { this.secuLendAbleYn = v; }

    public String getStlnAbleYn() { return stlnAbleYn; }
    public void setStlnAbleYn(String v) { this.stlnAbleYn = v; }

    public String getStckSdpr() { return stckSdpr; }
    public void setStckSdpr(String v) { this.stckSdpr = v; }

    public String getFrmlMrktDealQtyUnit() { return frmlMrktDealQtyUnit; }
    public void setFrmlMrktDealQtyUnit(String v) { this.frmlMrktDealQtyUnit = v; }

    public String getOvtmMrktDealQtyUnit() { return ovtmMrktDealQtyUnit; }
    public void setOvtmMrktDealQtyUnit(String v) { this.ovtmMrktDealQtyUnit = v; }

    public String getPrdyVol() { return prdyVol; }
    public void setPrdyVol(String v) { this.prdyVol = v; }

    public String getStckFcam() { return stckFcam; }
    public void setStckFcam(String v) { this.stckFcam = v; }

    public String getLstnStcn() { return lstnStcn; }
    public void setLstnStcn(String v) { this.lstnStcn = v; }

    public String getCpfn() { return cpfn; }
    public void setCpfn(String v) { this.cpfn = v; }

    public String getPoPrc() { return poPrc; }
    public void setPoPrc(String v) { this.poPrc = v; }

    public String getSaleAccount() { return saleAccount; }
    public void setSaleAccount(String v) { this.saleAccount = v; }

    public String getBsopPrfi() { return bsopPrfi; }
    public void setBsopPrfi(String v) { this.bsopPrfi = v; }

    public String getOpPrfi() { return opPrfi; }
    public void setOpPrfi(String v) { this.opPrfi = v; }

    public String getThtrNtin() { return thtrNtin; }
    public void setThtrNtin(String v) { this.thtrNtin = v; }

    public String getRoeVal() { return roeVal; }
    public void setRoeVal(String v) { this.roeVal = v; }

    public String getBaseDt() { return baseDt; }
    public void setBaseDt(String v) { this.baseDt = v; }

    public String getPrdyAvlsScal() { return prdyAvlsScal; }
    public void setPrdyAvlsScal(String v) { this.prdyAvlsScal = v; }

    public String getCoCd() { return coCd; }
    public void setCoCd(String v) { this.coCd = v; }

    /* ── 페이징용 ── */
    private int totalCount;
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int v) { this.totalCount = v; }
}
