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

    /** 한글종목명 */
    private String stkNm;

    /** 영문종목명 (해외주식 .cod English name) */
    private String stkNmEn;

    /** 상장 시장: KOSPI, KOSDAQ, NASDAQ, NYSE */
    private String mktCd;

    /** 종목상태: NORMAL / HALT / CAUTION / DELIST */
    private String stkStatus;

    /** 종목구분: STOCK(일반종목) / ETF */
    private String stkType;

    /** 주요지수 편입: KOSPI200 / KOSDAQ150 / SNP500 / DOW / null */
    private String indexCd;

    /** 업종대분류코드 (KOSPI/KOSDAQ .mst 지수업종대분류, 4자리) */
    private String sectorCd;

    /** 시가총액규모구분: 1=대형, 2=중형, 3=소형 (KOSPI/KOSDAQ) */
    private String mktCapSize;

    /** 우선주여부: Y=우선주, N=보통주 */
    private String prefYn;

    /** 결산월 (01~12, KOSPI/KOSDAQ) */
    private String settleMonth;

    /** 통화코드 (해외주식: USD, HKD 등) */
    private String currency;

    /** 상장일자 (YYYYMMDD) */
    private String listDt;

    public String getStkCd() { return stkCd; }
    public void setStkCd(String stkCd) { this.stkCd = stkCd; }

    public String getStkNm() { return stkNm; }
    public void setStkNm(String stkNm) { this.stkNm = stkNm; }

    public String getStkNmEn() { return stkNmEn; }
    public void setStkNmEn(String stkNmEn) { this.stkNmEn = stkNmEn; }

    public String getMktCd() { return mktCd; }
    public void setMktCd(String mktCd) { this.mktCd = mktCd; }

    public String getStkStatus() { return stkStatus; }
    public void setStkStatus(String stkStatus) { this.stkStatus = stkStatus; }

    public String getStkType() { return stkType; }
    public void setStkType(String stkType) { this.stkType = stkType; }

    public String getIndexCd() { return indexCd; }
    public void setIndexCd(String indexCd) { this.indexCd = indexCd; }

    public String getSectorCd() { return sectorCd; }
    public void setSectorCd(String sectorCd) { this.sectorCd = sectorCd; }

    public String getMktCapSize() { return mktCapSize; }
    public void setMktCapSize(String mktCapSize) { this.mktCapSize = mktCapSize; }

    public String getPrefYn() { return prefYn; }
    public void setPrefYn(String prefYn) { this.prefYn = prefYn; }

    public String getSettleMonth() { return settleMonth; }
    public void setSettleMonth(String settleMonth) { this.settleMonth = settleMonth; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getListDt() { return listDt; }
    public void setListDt(String listDt) { this.listDt = listDt; }
}
