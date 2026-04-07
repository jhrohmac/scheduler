package com.scheduler.stock.dto;

/**
 * TB_STK_MASTER MERGE/조회용 DTO
 *
 * - StkMasterRefreshService 에서 .mst / .cod 파일 파싱 결과를 담아 DB에 MERGE 할 때 사용
 * - stkStatus 허용값: NORMAL / HALT / CAUTION / DELIST
 * - indexCd  허용값: KOSPI200 / KOSDAQ150 / SNP500 / DOW / null
 */
public class StkMasterDto {

    /** 종목코드 (PK) */
    private String stkCd;

    /** 종목명 */
    private String stkNm;

    /** 상장 시장: KOSPI, KOSDAQ, NASDAQ, NYSE */
    private String mktCd;

    /** 종목상태: NORMAL / HALT / CAUTION / DELIST */
    private String stkStatus;

    /** 상장일자 (YYYYMMDD) */
    private String listDt;

    /** 주요지수 편입: KOSPI200 / KOSDAQ150 / SNP500 / DOW / null */
    private String indexCd;

    /** 종목구분: STOCK(일반종목) / ETF */
    private String stkType;

    public String getStkCd() {
        return stkCd;
    }

    public void setStkCd(String stkCd) {
        this.stkCd = stkCd;
    }

    public String getStkNm() {
        return stkNm;
    }

    public void setStkNm(String stkNm) {
        this.stkNm = stkNm;
    }

    public String getMktCd() {
        return mktCd;
    }

    public void setMktCd(String mktCd) {
        this.mktCd = mktCd;
    }

    public String getStkStatus() {
        return stkStatus;
    }

    public void setStkStatus(String stkStatus) {
        this.stkStatus = stkStatus;
    }

    public String getListDt() {
        return listDt;
    }

    public void setListDt(String listDt) {
        this.listDt = listDt;
    }

    public String getIndexCd() {
        return indexCd;
    }

    public void setIndexCd(String indexCd) {
        this.indexCd = indexCd;
    }

    public String getStkType() {
        return stkType;
    }

    public void setStkType(String stkType) {
        this.stkType = stkType;
    }
}
