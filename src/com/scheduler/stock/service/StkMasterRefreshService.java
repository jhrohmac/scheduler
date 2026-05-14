package com.scheduler.stock.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.scheduler.stock.dao.StkMasterDao;
import com.scheduler.stock.dto.StkMasterDto;

/**
 * KIS 종목 마스터 파일 다운로드 및 TB_STK_MASTER 갱신 서비스
 *
 * 다운로드 URL (인증 불필요, 매일 갱신):
 *   KOSPI   : https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip
 *   KOSDAQ  : https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip
 *   NASDAQ  : https://new.real.download.dws.co.kr/common/master/nasmst.cod.zip
 *   NYSE    : https://new.real.download.dws.co.kr/common/master/nysmst.cod.zip
 *
 * 파일 포맷 참조: /Users/jinhyun/projects/open-trading-api-main/stocks_info/
 *   - kis_kospi_code_mst.py  : KOSPI .mst 파싱 로직
 *   - kis_kosdaq_code_mst.py : KOSDAQ .mst 파싱 로직
 *   - overseas_stock_code.py : 해외 .cod 파싱 로직
 */
public class StkMasterRefreshService {

    /* ─── 다운로드 URL ─────────────────────────────────────────── */
    private static final String URL_KOSPI  = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";
    private static final String URL_KOSDAQ = "https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip";
    private static final String URL_NASDAQ = "https://new.real.download.dws.co.kr/common/master/nasmst.cod.zip";
    private static final String URL_NYSE   = "https://new.real.download.dws.co.kr/common/master/nysmst.cod.zip";

    /* ─── KOSPI Part2 = 마지막 227바이트, 70필드 ────────────────── */
    private static final int   KOSPI_PART2_LEN   = 227;
    private static final int[] KOSPI_FIELD_SPECS  = {
        2, 1, 4, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 5, 5, 1, 1, 1, 2, 1, 1,
        1, 2, 2, 2, 3, 1, 3,12,12, 8,15,21, 2, 7, 1, 1, 1, 1, 1, 9,
        9, 9, 5, 9, 8, 9, 3, 1, 1, 1
    };
    /*
     * KOSPI Part2 필드 인덱스 (0-based)
     * idx  0 = 그룹코드(2)            idx  1 = 시가총액규모(1)
     * idx  2 = 업종대분류(4)           idx  3 = 업종중분류(4)          idx  4 = 업종소분류(4)
     * idx  5 = 제조업(1)              idx  6 = 저유동성(1)             idx  7 = 지배구조지수(1)
     * idx  8 = KOSPI200섹터(1)        idx  9 = KOSPI100(1)            idx 10 = KOSPI50(1)
     * idx 11 = KRX(1)                idx 12 = ETP(1)                  idx 13 = ELW발행(1)
     * idx 14 = KRX100(1)             idx 15 = KRX자동차(1)            idx 16 = KRX반도체(1)
     * idx 17 = KRX바이오(1)           idx 18 = KRX은행(1)              idx 19 = SPAC(1)
     * idx 20 = KRX에너지화학(1)        idx 21 = KRX철강(1)             idx 22 = 단기과열(1)
     * idx 23 = KRX미디어통신(1)        idx 24 = KRX건설(1)             idx 25 = Non1-삭제(1)
     * idx 26 = KRX증권(1)             idx 27 = KRX선박(1)             idx 28 = KRX섹터보험(1)
     * idx 29 = KRX섹터운송(1)         idx 30 = SRI(1)                 idx 31 = 기준가(9)
     * idx 32 = 정규매매수량(5)         idx 33 = 시간외수량(5)           idx 34 = 거래정지(1)
     * idx 35 = 정리매매(1)            idx 36 = 관리종목(1)             idx 37 = 시장경고(2)
     * idx 38 = 경고예고(1)            idx 39 = 불성실공시(1)           idx 40 = 우회상장(1)
     * idx 41 = 락구분(2)             idx 42 = 액면변경(2)             idx 43 = 증자구분(2)
     * idx 44 = 증거금비율(3)          idx 45 = 신용가능(1)             idx 46 = 신용기간(3)
     * idx 47 = 전일거래량(12)         idx 48 = 액면가(12)              idx 49 = 상장일자(8)
     * idx 50 = 상장주수(15)          idx 51 = 자본금(21)              idx 52 = 결산월(2)
     * idx 53 = 공모가(7)             idx 54 = 우선주(1)               idx 55 = 공매도과열(1)
     * idx 56 = 이상급등(1)           idx 57 = KRX300(1)              idx 58 = KOSPI(1)
     * idx 59 = 매출액(9)             idx 60 = 영업이익(9)             idx 61 = 경상이익(9)
     * idx 62 = 당기순이익(5)          idx 63 = ROE(9)                 idx 64 = 기준년월(8)
     * idx 65 = 시가총액(9)           idx 66 = 그룹사코드(3)           idx 67 = 회사신용한도초과(1)
     * idx 68 = 담보대출가능(1)        idx 69 = 대주가능(1)
     */
    private static final int KOSPI_IDX_SCRT_GRP      =  0;
    private static final int KOSPI_IDX_MKT_CAP_SIZE   =  1;
    private static final int KOSPI_IDX_SECTOR_CD      =  2;
    private static final int KOSPI_IDX_BSTP_MEDM      =  3;
    private static final int KOSPI_IDX_BSTP_SMAL      =  4;
    private static final int KOSPI_IDX_MNIN            =  5;
    private static final int KOSPI_IDX_LOW_CURRENT     =  6;
    private static final int KOSPI_IDX_SPRN_NMIX       =  7;
    private static final int KOSPI_IDX_KOSPI200        =  8;
    private static final int KOSPI_IDX_KOSPI100        =  9;
    private static final int KOSPI_IDX_KOSPI50         = 10;
    private static final int KOSPI_IDX_KRX             = 11;
    private static final int KOSPI_IDX_ETP             = 12;
    private static final int KOSPI_IDX_ELW_PBLC        = 13;
    private static final int KOSPI_IDX_KRX100          = 14;
    private static final int KOSPI_IDX_KRX_CAR         = 15;
    private static final int KOSPI_IDX_KRX_SMCN        = 16;
    private static final int KOSPI_IDX_KRX_BIO         = 17;
    private static final int KOSPI_IDX_KRX_BANK        = 18;
    private static final int KOSPI_IDX_SPAC            = 19;
    private static final int KOSPI_IDX_KRX_ENRG_CHMS  = 20;
    private static final int KOSPI_IDX_KRX_STEL        = 21;
    private static final int KOSPI_IDX_SHORT_OVER      = 22;
    private static final int KOSPI_IDX_KRX_MEDI_CMNC  = 23;
    private static final int KOSPI_IDX_KRX_CNST        = 24;
    /* idx 25 = Non1 (삭제됨) */
    private static final int KOSPI_IDX_KRX_SCRT        = 26;
    private static final int KOSPI_IDX_KRX_SHIP        = 27;
    private static final int KOSPI_IDX_KRX_INSU        = 28;
    private static final int KOSPI_IDX_KRX_TRNS        = 29;
    private static final int KOSPI_IDX_SRI             = 30;
    private static final int KOSPI_IDX_STCK_SDPR       = 31;
    private static final int KOSPI_IDX_FRML_QTY        = 32;
    private static final int KOSPI_IDX_OVTM_QTY        = 33;
    private static final int KOSPI_IDX_HALT            = 34;
    private static final int KOSPI_IDX_LIQUIDATION     = 35;
    private static final int KOSPI_IDX_CAUTION         = 36;
    private static final int KOSPI_IDX_MRKT_ALRM       = 37;
    private static final int KOSPI_IDX_MRKT_ALRM_RISK  = 38;
    private static final int KOSPI_IDX_INSN_PBNT       = 39;
    private static final int KOSPI_IDX_BYPS_LSTN       = 40;
    private static final int KOSPI_IDX_FLNG_CLS        = 41;
    private static final int KOSPI_IDX_FCAM_MOD        = 42;
    private static final int KOSPI_IDX_ICIC_CLS        = 43;
    private static final int KOSPI_IDX_MARG_RATE       = 44;
    private static final int KOSPI_IDX_CRDT_ABLE       = 45;
    private static final int KOSPI_IDX_CRDT_DAYS       = 46;
    private static final int KOSPI_IDX_PRDY_VOL        = 47;
    private static final int KOSPI_IDX_STCK_FCAM       = 48;
    private static final int KOSPI_IDX_LIST_DT         = 49;
    private static final int KOSPI_IDX_LSTN_STCN       = 50;
    private static final int KOSPI_IDX_CPFN            = 51;
    private static final int KOSPI_IDX_SETTLE_MONTH    = 52;
    private static final int KOSPI_IDX_PO_PRC          = 53;
    private static final int KOSPI_IDX_PREF_YN         = 54;
    private static final int KOSPI_IDX_SSTS_HOT        = 55;
    private static final int KOSPI_IDX_STANGE_RUNUP    = 56;
    private static final int KOSPI_IDX_KRX300          = 57;
    private static final int KOSPI_IDX_KOSPI_YN        = 58;
    private static final int KOSPI_IDX_SALE_ACCOUNT    = 59;
    private static final int KOSPI_IDX_BSOP_PRFI       = 60;
    private static final int KOSPI_IDX_OP_PRFI         = 61;
    private static final int KOSPI_IDX_THTR_NTIN       = 62;
    private static final int KOSPI_IDX_ROE             = 63;
    private static final int KOSPI_IDX_BASE_DT         = 64;
    private static final int KOSPI_IDX_PRDY_AVLS_SCAL  = 65;
    private static final int KOSPI_IDX_CO_CD           = 66;
    private static final int KOSPI_IDX_CO_CRDT_LIMT    = 67;
    private static final int KOSPI_IDX_SECU_LEND       = 68;
    private static final int KOSPI_IDX_STLN_ABLE       = 69;

    /* ─── KOSDAQ Part2 = 마지막 221바이트, 64필드 ───────────────── */
    private static final int   KOSDAQ_PART2_LEN  = 221;
    private static final int[] KOSDAQ_FIELD_SPECS = {
        2, 1, 4, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 9, 5, 5, 1, 1, 1, 2, 1, 1, 1, 2, 2, 2, 3,
        1, 3,12,12, 8,15,21, 2, 7, 1, 1, 1, 1, 9, 9, 9, 5, 9, 8, 9,
        3, 1, 1, 1
    };
    /*
     * KOSDAQ Part2 필드 인덱스 (0-based) — 종목마스터정보(코스닥).h ST_KSQ_CODE 기준
     * idx  0 = 증권그룹구분코드(2)     idx  1 = 시가총액규모(1)
     * idx  2 = 업종대분류(4)           idx  3 = 업종중분류(4)          idx  4 = 업종소분류(4)
     * idx  5 = 벤처기업(1)             idx  6 = 저유동성(1)            idx  7 = KRX(1)
     * idx  8 = ETP(1)                 idx  9 = KRX100(1)              idx 10 = KRX자동차(1)
     * idx 11 = KRX반도체(1)           idx 12 = KRX바이오(1)           idx 13 = KRX은행(1)
     * idx 14 = SPAC(1)               idx 15 = KRX에너지화학(1)        idx 16 = KRX철강(1)
     * idx 17 = 단기과열(1)            idx 18 = KRX미디어통신(1)        idx 19 = KRX건설(1)
     * idx 20 = 투자주의환기(1)         idx 21 = KRX증권(1)             idx 22 = KRX선박(1)
     * idx 23 = KRX섹터보험(1)         idx 24 = KRX섹터운송(1)         idx 25 = KOSDAQ150(1)
     * idx 26 = 기준가(9)              idx 27 = 정규매매수량(5)         idx 28 = 시간외수량(5)
     * idx 29 = 거래정지(1)            idx 30 = 정리매매(1)            idx 31 = 관리종목(1)
     * idx 32 = 시장경고(2)            idx 33 = 경고예고(1)            idx 34 = 불성실공시(1)
     * idx 35 = 우회상장(1)            idx 36 = 락구분(2)              idx 37 = 액면변경(2)
     * idx 38 = 증자구분(2)            idx 39 = 증거금비율(3)          idx 40 = 신용가능(1)
     * idx 41 = 신용기간(3)            idx 42 = 전일거래량(12)         idx 43 = 액면가(12)
     * idx 44 = 상장일자(8)            idx 45 = 상장주수(15)           idx 46 = 자본금(21)
     * idx 47 = 결산월(2)             idx 48 = 공모가(7)              idx 49 = 우선주(1)
     * idx 50 = 공매도과열(1)          idx 51 = 이상급등(1)            idx 52 = KRX300(1)
     * idx 53 = 매출액(9)             idx 54 = 영업이익(9)            idx 55 = 경상이익(9)
     * idx 56 = 당기순이익(5)          idx 57 = ROE(9)                idx 58 = 기준년월(8)
     * idx 59 = 시가총액(9)           idx 60 = 그룹사코드(3)           idx 61 = 회사신용한도초과(1)
     * idx 62 = 담보대출가능(1)        idx 63 = 대주가능(1)
     */
    private static final int KOSDAQ_IDX_SCRT_GRP      =  0;
    private static final int KOSDAQ_IDX_MKT_CAP_SIZE  =  1;
    private static final int KOSDAQ_IDX_SECTOR_CD     =  2;
    private static final int KOSDAQ_IDX_BSTP_MEDM     =  3;
    private static final int KOSDAQ_IDX_BSTP_SMAL     =  4;
    private static final int KOSDAQ_IDX_VENT          =  5;
    private static final int KOSDAQ_IDX_LOW_CURRENT   =  6;
    private static final int KOSDAQ_IDX_KRX           =  7;
    private static final int KOSDAQ_IDX_ETP           =  8;
    private static final int KOSDAQ_IDX_KRX100        =  9;
    private static final int KOSDAQ_IDX_KRX_CAR       = 10;
    private static final int KOSDAQ_IDX_KRX_SMCN      = 11;
    private static final int KOSDAQ_IDX_KRX_BIO       = 12;
    private static final int KOSDAQ_IDX_KRX_BANK      = 13;
    private static final int KOSDAQ_IDX_SPAC          = 14;
    private static final int KOSDAQ_IDX_KRX_ENRG_CHMS = 15;
    private static final int KOSDAQ_IDX_KRX_STEL      = 16;
    private static final int KOSDAQ_IDX_SHORT_OVER    = 17;
    private static final int KOSDAQ_IDX_KRX_MEDI_CMNC = 18;
    private static final int KOSDAQ_IDX_KRX_CNST      = 19;
    private static final int KOSDAQ_IDX_INVT_CATN     = 20;
    private static final int KOSDAQ_IDX_KRX_SCRT      = 21;
    private static final int KOSDAQ_IDX_KRX_SHIP      = 22;
    private static final int KOSDAQ_IDX_KRX_INSU      = 23;
    private static final int KOSDAQ_IDX_KRX_TRNS      = 24;
    private static final int KOSDAQ_IDX_KOSDAQ150     = 25;
    private static final int KOSDAQ_IDX_STCK_SDPR     = 26;
    private static final int KOSDAQ_IDX_FRML_QTY      = 27;
    private static final int KOSDAQ_IDX_OVTM_QTY      = 28;
    private static final int KOSDAQ_IDX_HALT          = 29;
    private static final int KOSDAQ_IDX_LIQUIDATION   = 30;
    private static final int KOSDAQ_IDX_CAUTION       = 31;
    private static final int KOSDAQ_IDX_MRKT_ALRM     = 32;
    private static final int KOSDAQ_IDX_MRKT_ALRM_RISK = 33;
    private static final int KOSDAQ_IDX_INSN_PBNT     = 34;
    private static final int KOSDAQ_IDX_BYPS_LSTN     = 35;
    private static final int KOSDAQ_IDX_FLNG_CLS      = 36;
    private static final int KOSDAQ_IDX_FCAM_MOD      = 37;
    private static final int KOSDAQ_IDX_ICIC_CLS      = 38;
    private static final int KOSDAQ_IDX_MARG_RATE     = 39;
    private static final int KOSDAQ_IDX_CRDT_ABLE     = 40;
    private static final int KOSDAQ_IDX_CRDT_DAYS     = 41;
    private static final int KOSDAQ_IDX_PRDY_VOL      = 42;
    private static final int KOSDAQ_IDX_STCK_FCAM     = 43;
    private static final int KOSDAQ_IDX_LIST_DT       = 44;
    private static final int KOSDAQ_IDX_LSTN_STCN     = 45;
    private static final int KOSDAQ_IDX_CPFN          = 46;
    private static final int KOSDAQ_IDX_SETTLE_MONTH  = 47;
    private static final int KOSDAQ_IDX_PO_PRC        = 48;
    private static final int KOSDAQ_IDX_PREF_YN       = 49;
    private static final int KOSDAQ_IDX_SSTS_HOT      = 50;
    private static final int KOSDAQ_IDX_STANGE_RUNUP  = 51;
    private static final int KOSDAQ_IDX_KRX300        = 52;
    private static final int KOSDAQ_IDX_SALE_ACCOUNT  = 53;
    private static final int KOSDAQ_IDX_BSOP_PRFI     = 54;
    private static final int KOSDAQ_IDX_OP_PRFI       = 55;
    private static final int KOSDAQ_IDX_THTR_NTIN     = 56;
    private static final int KOSDAQ_IDX_ROE           = 57;
    private static final int KOSDAQ_IDX_BASE_DT       = 58;
    private static final int KOSDAQ_IDX_PRDY_AVLS_SCAL = 59;
    private static final int KOSDAQ_IDX_CO_CD         = 60;
    private static final int KOSDAQ_IDX_CO_CRDT_LIMT  = 61;
    private static final int KOSDAQ_IDX_SECU_LEND     = 62;
    private static final int KOSDAQ_IDX_STLN_ABLE     = 63;

    /* ─── 해외 .cod TSV 컬럼 인덱스 (0-based, 첫 행=헤더 skip) ── */
    /*
     * overseas_stock_code.py 컬럼 순서:
     * 0=National code, 1=Exchange id, 2=Exchange code, 3=Exchange name,
     * 4=Symbol, 5=realtime symbol, 6=Korea name, 7=English name,
     * 8=Security type(2=주식), 9=currency, 10=float position, 11=data type,
     * 12=base price, 13=Bid order size, 14=Ask order size,
     * 15=market start, 16=market end, 17=DR여부, 18=DR국가코드,
     * 19=업종분류코드, 20=지수구성종목존재여부, 21=Tick size Type,
     * 22=구분코드(ETF/ETN), 23=Tick size type 상세
     */
    private static final int COD_IDX_NATN_CD    = 0;
    private static final int COD_IDX_EXCH_CODE  = 2;
    private static final int COD_IDX_SYMBOL     = 4;
    private static final int COD_IDX_KOREA_NAME = 6;
    private static final int COD_IDX_ENG_NAME   = 7;
    private static final int COD_IDX_SEC_TYPE   = 8;
    private static final int COD_IDX_CURRENCY   = 9;
    private static final int COD_IDX_OVRS_BSTP  = 19;
    private static final int COD_IDX_ETP_CLS    = 22; /* 구분코드: 001=ETF,002=ETN,003=ETC,004=Others,005=VIX ETF,006=VIX ETN */

    private static final int HTTP_TIMEOUT_MS = 30_000;

    private StkMasterDao stkMasterDao;

    public void setStkMasterDao(StkMasterDao stkMasterDao) {
        this.stkMasterDao = stkMasterDao;
    }

    /* ═══════════════════════════════════════════════════════════
       외부 진입점
    ═══════════════════════════════════════════════════════════ */

    /**
     * 종목 마스터 갱신 실행
     *
     * @param map marketGroup: KR | US | ALL (생략 시 ALL)
     *            stockType:   ALL | STOCK | ETF (생략 시 ALL)
     */
    public HashMap<String, Object> refresh(HashMap<String, String> map) throws Exception {
        String marketGroup = trim(map == null ? null : map.get("marketGroup"));
        if (isBlank(marketGroup)) marketGroup = "ALL";
        marketGroup = marketGroup.toUpperCase();

        String stockType = trim(map == null ? null : map.get("stockType"));
        if (isBlank(stockType) || "ALL".equalsIgnoreCase(stockType)) stockType = "ALL";
        stockType = stockType.toUpperCase();

        int krMerged = 0, krDelisted = 0;
        int usMerged = 0, usDelisted = 0;
        String lastError = null;

        if ("KR".equals(marketGroup) || "ALL".equals(marketGroup)) {
            try {
                int[] r = refreshKr(stockType);
                krMerged = r[0]; krDelisted = r[1];
            } catch (Exception e) {
                lastError = "KR 갱신 오류: " + e.getMessage();
                System.err.println("[StkMasterRefreshService] " + lastError);
            }
        }

        if ("US".equals(marketGroup) || "ALL".equals(marketGroup)) {
            try {
                int[] r = refreshUs(stockType);
                usMerged = r[0]; usDelisted = r[1];
            } catch (Exception e) {
                String msg = "US 갱신 오류: " + e.getMessage();
                lastError = lastError == null ? msg : lastError + " | " + msg;
                System.err.println("[StkMasterRefreshService] " + msg);
            }
        }

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("marketGroup",   marketGroup);
        result.put("stockType",     stockType);
        result.put("krMergedCnt",   Integer.valueOf(krMerged));
        result.put("krDelistedCnt", Integer.valueOf(krDelisted));
        result.put("usMergedCnt",   Integer.valueOf(usMerged));
        result.put("usDelistedCnt", Integer.valueOf(usDelisted));
        result.put("status",  lastError == null ? "SUCCESS" : "PARTIAL");
        result.put("message", lastError);
        return result;
    }

    /* ═══════════════════════════════════════════════════════════
       KR (KOSPI + KOSDAQ)
    ═══════════════════════════════════════════════════════════ */

    private int[] refreshKr(String stockType) throws Exception {
        System.out.println("[StkMasterRefreshService] KR 종목 마스터 갱신 시작 (stockType=" + stockType + ")");

        List<StkMasterDto> allList = new ArrayList<StkMasterDto>();
        allList.addAll(downloadAndParseKospi());
        allList.addAll(downloadAndParseKosdaq());

        Set<String> downloadedCodes = new HashSet<String>();
        int mergedCnt = 0;
        for (int i = 0; i < allList.size(); i++) {
            StkMasterDto dto = allList.get(i);
            if (dto == null || isBlank(dto.getStkCd())) continue;
            if (!"ALL".equals(stockType) && !stockType.equalsIgnoreCase(dto.getStkType())) continue;
            stkMasterDao.mergeStkMaster(dto);
            downloadedCodes.add(dto.getStkCd());
            mergedCnt++;
        }

        int delistedCnt = 0;
        if ("ALL".equals(stockType)) {
            delistedCnt  = markDelisted("KOSPI",  downloadedCodes);
            delistedCnt += markDelisted("KOSDAQ", downloadedCodes);
        }

        System.out.println("[StkMasterRefreshService] KR 갱신 완료 — merged=" + mergedCnt + ", delisted=" + delistedCnt);
        return new int[]{mergedCnt, delistedCnt};
    }

    private List<StkMasterDto> downloadAndParseKospi() throws Exception {
        System.out.println("[StkMasterRefreshService] KOSPI 파일 다운로드: " + URL_KOSPI);
        InputStream is = downloadZip(URL_KOSPI, "kospi_code.mst");
        try {
            return parseMst(is, KOSPI_PART2_LEN, KOSPI_FIELD_SPECS, "KOSPI");
        } finally {
            closeQuietly(is);
        }
    }

    private List<StkMasterDto> downloadAndParseKosdaq() throws Exception {
        System.out.println("[StkMasterRefreshService] KOSDAQ 파일 다운로드: " + URL_KOSDAQ);
        InputStream is = downloadZip(URL_KOSDAQ, "kosdaq_code.mst");
        try {
            return parseMst(is, KOSDAQ_PART2_LEN, KOSDAQ_FIELD_SPECS, "KOSDAQ");
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * .mst 고정폭 파일 파싱 (KOSPI / KOSDAQ 공용)
     *
     * 행 구조:
     *   Part1 = row[0 : len - part2Len]
     *     [0:9]  단축코드, [9:21] 표준코드, [21:] 한글명
     *   Part2 = row[-part2Len:]
     *     fieldSpecs 배열로 고정폭 분리
     */
    private List<StkMasterDto> parseMst(InputStream is, int part2Len, int[] fieldSpecs,
                                         String mktCd) throws Exception {
        List<StkMasterDto> list = new ArrayList<StkMasterDto>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "MS949"));
        String line;

        boolean isKospi  = "KOSPI".equals(mktCd);
        boolean isKosdaq = "KOSDAQ".equals(mktCd);

        while ((line = reader.readLine()) != null) {
            if (line.length() <= part2Len) continue;

            int p2Start = line.length() - part2Len;
            String part1 = line.substring(0, p2Start);
            String stkCd = trim(part1.length() >= 9  ? part1.substring(0, 9)  : part1);
            String stdCd = trim(part1.length() >= 21 ? part1.substring(9, 21) : "");
            String stkNm = trim(part1.length() > 21  ? part1.substring(21)    : "");

            if (isBlank(stkCd)) continue;

            String part2  = line.substring(p2Start);
            String[] flds = parseFixedWidth(part2, fieldSpecs);

            String stkType = resolveDomesticStkType(stdCd, flds, isKospi);

            /* STK_STATUS */
            String stkStatus;
            if (isKospi) {
                if ("Y".equalsIgnoreCase(getField(flds, KOSPI_IDX_LIQUIDATION))) stkStatus = "DELIST";
                else if ("Y".equalsIgnoreCase(getField(flds, KOSPI_IDX_HALT)))    stkStatus = "HALT";
                else if ("Y".equalsIgnoreCase(getField(flds, KOSPI_IDX_CAUTION))) stkStatus = "CAUTION";
                else stkStatus = "NORMAL";
            } else {
                if ("Y".equalsIgnoreCase(getField(flds, KOSDAQ_IDX_LIQUIDATION)))         stkStatus = "DELIST";
                else if ("Y".equalsIgnoreCase(getField(flds, KOSDAQ_IDX_HALT)))            stkStatus = "HALT";
                else if ("Y".equalsIgnoreCase(getField(flds, KOSDAQ_IDX_CAUTION)))         stkStatus = "CAUTION";
                else stkStatus = "NORMAL";
            }

            /* INDEX_CD (하위호환) */
            String indexCd = null;
            if (isKospi) {
                String s = getField(flds, KOSPI_IDX_KOSPI200);
                if (!isBlank(s) && !"0".equals(s)) indexCd = "KOSPI200";
            } else {
                if ("Y".equalsIgnoreCase(getField(flds, KOSDAQ_IDX_KOSDAQ150))) indexCd = "KOSDAQ150";
            }

            StkMasterDto dto = new StkMasterDto();
            dto.setStkCd(stkCd);
            dto.setStkNm(stkNm);
            dto.setMktCd(mktCd);
            dto.setStkStatus(stkStatus);
            dto.setStkType(stkType);
            dto.setIndexCd(indexCd);

            if (isKospi) {
                fillKospiFields(dto, flds);
            } else {
                fillKosdaqFields(dto, flds);
            }

            list.add(dto);
        }

        validateDomesticStatusDistribution(list, mktCd);

        System.out.println("[StkMasterRefreshService] " + mktCd + " 파싱 완료: " + list.size() + "건");
        return list;
    }

    private void validateDomesticStatusDistribution(List<StkMasterDto> list, String mktCd) {
        if (list == null || list.isEmpty() || (!"KOSPI".equals(mktCd) && !"KOSDAQ".equals(mktCd))) {
            return;
        }

        int normalCnt = 0;
        int haltCnt = 0;
        int cautionCnt = 0;
        int delistCnt = 0;
        for (int i = 0; i < list.size(); i++) {
            StkMasterDto dto = list.get(i);
            String status = dto == null ? "" : trim(dto.getStkStatus());
            if ("NORMAL".equals(status)) {
                normalCnt++;
            } else if ("HALT".equals(status)) {
                haltCnt++;
            } else if ("CAUTION".equals(status)) {
                cautionCnt++;
            } else if ("DELIST".equals(status)) {
                delistCnt++;
            }
        }

        if (normalCnt == 0 && list.size() >= 100) {
            throw new IllegalStateException(mktCd + " 종목 마스터 상태값 이상: NORMAL=0"
                    + ", total=" + list.size()
                    + ", halt=" + haltCnt
                    + ", caution=" + cautionCnt
                    + ", delist=" + delistCnt);
        }
    }

    /**
     * STK_TYPE 결정: 표준코드 prefix → ETP_CLS_CD → SCRT_GRP_CLS_CD 순 우선순위
     *
     * KR3 = ETF(집합투자증권), KR6 = ETN(파생결합증권)
     * ETP_CLS_CD: 1/2/5=ETF형, 3/4=ETN형
     * SCRT_GRP_CLS_CD: EF/MF=ETF, EW=ELW
     */
    private String resolveDomesticStkType(String stdCd, String[] flds, boolean isKospi) {
        if (stdCd.startsWith("KR3")) return "ETF";
        if (stdCd.startsWith("KR6")) return "ETN";

        int etpIdx = isKospi ? KOSPI_IDX_ETP : KOSDAQ_IDX_ETP;
        String etpCls = trim(getField(flds, etpIdx));
        if ("3".equals(etpCls) || "4".equals(etpCls)) return "ETN";
        if ("1".equals(etpCls) || "2".equals(etpCls) || "5".equals(etpCls)) return "ETF";

        int scrtIdx = isKospi ? KOSPI_IDX_SCRT_GRP : KOSDAQ_IDX_SCRT_GRP;
        String scrtGrp = trim(getField(flds, scrtIdx));
        if ("EF".equals(scrtGrp) || "MF".equals(scrtGrp)) return "ETF";
        if ("EW".equals(scrtGrp)) return "ELW";

        return "STOCK";
    }

    /** KOSPI Part2 → DTO 필드 매핑 */
    private void fillKospiFields(StkMasterDto dto, String[] f) {
        dto.setScrtGrpClsCd(toNull(getField(f, KOSPI_IDX_SCRT_GRP)));
        dto.setMktCapSize(toNull(getField(f, KOSPI_IDX_MKT_CAP_SIZE)));
        dto.setSectorCd(toNull(getField(f, KOSPI_IDX_SECTOR_CD)));
        dto.setBstpMedmDivCd(toNull(getField(f, KOSPI_IDX_BSTP_MEDM)));
        dto.setBstpSmalDivCd(toNull(getField(f, KOSPI_IDX_BSTP_SMAL)));
        dto.setMninYn(toNull(getField(f, KOSPI_IDX_MNIN)));
        dto.setLowCurrentYn(toNull(getField(f, KOSPI_IDX_LOW_CURRENT)));
        dto.setSprnNmixYn(toNull(getField(f, KOSPI_IDX_SPRN_NMIX)));

        String kospi200Sect = toNull(getField(f, KOSPI_IDX_KOSPI200));
        dto.setKospi200SectCd("0".equals(kospi200Sect) ? null : kospi200Sect);

        dto.setKospi100Yn(toNull(getField(f, KOSPI_IDX_KOSPI100)));
        dto.setKospi50Yn(toNull(getField(f, KOSPI_IDX_KOSPI50)));
        dto.setKrxYn(toNull(getField(f, KOSPI_IDX_KRX)));
        dto.setEtpClsCd(toNull(getField(f, KOSPI_IDX_ETP)));
        dto.setElwPblcYn(toNull(getField(f, KOSPI_IDX_ELW_PBLC)));
        dto.setKrx100Yn(toNull(getField(f, KOSPI_IDX_KRX100)));
        dto.setKrxCarYn(toNull(getField(f, KOSPI_IDX_KRX_CAR)));
        dto.setKrxSmcnYn(toNull(getField(f, KOSPI_IDX_KRX_SMCN)));
        dto.setKrxBioYn(toNull(getField(f, KOSPI_IDX_KRX_BIO)));
        dto.setKrxBankYn(toNull(getField(f, KOSPI_IDX_KRX_BANK)));
        dto.setSpacYn(toNull(getField(f, KOSPI_IDX_SPAC)));
        dto.setKrxEnrgChmsYn(toNull(getField(f, KOSPI_IDX_KRX_ENRG_CHMS)));
        dto.setKrxStelYn(toNull(getField(f, KOSPI_IDX_KRX_STEL)));
        dto.setShortOverClsCd(toNull(getField(f, KOSPI_IDX_SHORT_OVER)));
        dto.setKrxMediCmncYn(toNull(getField(f, KOSPI_IDX_KRX_MEDI_CMNC)));
        dto.setKrxCnstYn(toNull(getField(f, KOSPI_IDX_KRX_CNST)));
        dto.setKrxScrtYn(toNull(getField(f, KOSPI_IDX_KRX_SCRT)));
        dto.setKrxShipYn(toNull(getField(f, KOSPI_IDX_KRX_SHIP)));
        dto.setKrxInsuYn(toNull(getField(f, KOSPI_IDX_KRX_INSU)));
        dto.setKrxTrnsYn(toNull(getField(f, KOSPI_IDX_KRX_TRNS)));
        dto.setSriYn(toNull(getField(f, KOSPI_IDX_SRI)));
        dto.setStckSdpr(toNull(getField(f, KOSPI_IDX_STCK_SDPR)));
        dto.setFrmlMrktDealQtyUnit(toNull(getField(f, KOSPI_IDX_FRML_QTY)));
        dto.setOvtmMrktDealQtyUnit(toNull(getField(f, KOSPI_IDX_OVTM_QTY)));
        dto.setSltrYn(toNull(getField(f, KOSPI_IDX_LIQUIDATION)));
        dto.setMangYn(toNull(getField(f, KOSPI_IDX_CAUTION)));
        dto.setMrktAlrmClsCd(toNull(getField(f, KOSPI_IDX_MRKT_ALRM)));
        dto.setMrktAlrmRiskYn(toNull(getField(f, KOSPI_IDX_MRKT_ALRM_RISK)));
        dto.setInsnPbntYn(toNull(getField(f, KOSPI_IDX_INSN_PBNT)));
        dto.setBypsLstnYn(toNull(getField(f, KOSPI_IDX_BYPS_LSTN)));
        dto.setFlngClsCd(toNull(getField(f, KOSPI_IDX_FLNG_CLS)));
        dto.setFcamModClsCd(toNull(getField(f, KOSPI_IDX_FCAM_MOD)));
        dto.setIcicClsCd(toNull(getField(f, KOSPI_IDX_ICIC_CLS)));
        dto.setMargRate(toNull(getField(f, KOSPI_IDX_MARG_RATE)));
        dto.setCrdtAble(toNull(getField(f, KOSPI_IDX_CRDT_ABLE)));
        dto.setCrdtDays(toNull(getField(f, KOSPI_IDX_CRDT_DAYS)));
        dto.setPrdyVol(toNull(getField(f, KOSPI_IDX_PRDY_VOL)));
        dto.setStckFcam(toNull(getField(f, KOSPI_IDX_STCK_FCAM)));
        dto.setListDt(extractDate8(getField(f, KOSPI_IDX_LIST_DT)));
        dto.setLstnStcn(toNull(getField(f, KOSPI_IDX_LSTN_STCN)));
        dto.setCpfn(toNull(getField(f, KOSPI_IDX_CPFN)));
        dto.setSettleMonth(toNull(getField(f, KOSPI_IDX_SETTLE_MONTH)));
        dto.setPoPrc(toNull(getField(f, KOSPI_IDX_PO_PRC)));

        String prefRaw = getField(f, KOSPI_IDX_PREF_YN);
        dto.setPrefYn((!isBlank(prefRaw) && !"0".equals(prefRaw.trim())) ? "Y" : "N");

        dto.setSstsHotYn(toNull(getField(f, KOSPI_IDX_SSTS_HOT)));
        dto.setStangeRunupYn(toNull(getField(f, KOSPI_IDX_STANGE_RUNUP)));
        dto.setKrx300Yn(toNull(getField(f, KOSPI_IDX_KRX300)));
        dto.setKospiYn(toNull(getField(f, KOSPI_IDX_KOSPI_YN)));
        dto.setSaleAccount(toNull(getField(f, KOSPI_IDX_SALE_ACCOUNT)));
        dto.setBsopPrfi(toNull(getField(f, KOSPI_IDX_BSOP_PRFI)));
        dto.setOpPrfi(toNull(getField(f, KOSPI_IDX_OP_PRFI)));
        dto.setThtrNtin(toNull(getField(f, KOSPI_IDX_THTR_NTIN)));
        dto.setRoeVal(toNull(getField(f, KOSPI_IDX_ROE)));
        dto.setBaseDt(toNull(getField(f, KOSPI_IDX_BASE_DT)));
        dto.setPrdyAvlsScal(toNull(getField(f, KOSPI_IDX_PRDY_AVLS_SCAL)));
        dto.setCoCd(toNull(getField(f, KOSPI_IDX_CO_CD)));
        dto.setCoCrdtLimtOverYn(toNull(getField(f, KOSPI_IDX_CO_CRDT_LIMT)));
        dto.setSecuLendAbleYn(toNull(getField(f, KOSPI_IDX_SECU_LEND)));
        dto.setStlnAbleYn(toNull(getField(f, KOSPI_IDX_STLN_ABLE)));
    }

    /** KOSDAQ Part2 → DTO 필드 매핑 */
    private void fillKosdaqFields(StkMasterDto dto, String[] f) {
        dto.setScrtGrpClsCd(toNull(getField(f, KOSDAQ_IDX_SCRT_GRP)));
        dto.setMktCapSize(toNull(getField(f, KOSDAQ_IDX_MKT_CAP_SIZE)));
        dto.setSectorCd(toNull(getField(f, KOSDAQ_IDX_SECTOR_CD)));
        dto.setBstpMedmDivCd(toNull(getField(f, KOSDAQ_IDX_BSTP_MEDM)));
        dto.setBstpSmalDivCd(toNull(getField(f, KOSDAQ_IDX_BSTP_SMAL)));
        dto.setVentYn(toNull(getField(f, KOSDAQ_IDX_VENT)));
        dto.setLowCurrentYn(toNull(getField(f, KOSDAQ_IDX_LOW_CURRENT)));
        dto.setKrxYn(toNull(getField(f, KOSDAQ_IDX_KRX)));
        dto.setEtpClsCd(toNull(getField(f, KOSDAQ_IDX_ETP)));
        dto.setKrx100Yn(toNull(getField(f, KOSDAQ_IDX_KRX100)));
        dto.setKrxCarYn(toNull(getField(f, KOSDAQ_IDX_KRX_CAR)));
        dto.setKrxSmcnYn(toNull(getField(f, KOSDAQ_IDX_KRX_SMCN)));
        dto.setKrxBioYn(toNull(getField(f, KOSDAQ_IDX_KRX_BIO)));
        dto.setKrxBankYn(toNull(getField(f, KOSDAQ_IDX_KRX_BANK)));
        dto.setSpacYn(toNull(getField(f, KOSDAQ_IDX_SPAC)));
        dto.setKrxEnrgChmsYn(toNull(getField(f, KOSDAQ_IDX_KRX_ENRG_CHMS)));
        dto.setKrxStelYn(toNull(getField(f, KOSDAQ_IDX_KRX_STEL)));
        dto.setShortOverClsCd(toNull(getField(f, KOSDAQ_IDX_SHORT_OVER)));
        dto.setKrxMediCmncYn(toNull(getField(f, KOSDAQ_IDX_KRX_MEDI_CMNC)));
        dto.setKrxCnstYn(toNull(getField(f, KOSDAQ_IDX_KRX_CNST)));
        dto.setInvtCatnYn(toNull(getField(f, KOSDAQ_IDX_INVT_CATN)));
        dto.setKrxScrtYn(toNull(getField(f, KOSDAQ_IDX_KRX_SCRT)));
        dto.setKrxShipYn(toNull(getField(f, KOSDAQ_IDX_KRX_SHIP)));
        dto.setKrxInsuYn(toNull(getField(f, KOSDAQ_IDX_KRX_INSU)));
        dto.setKrxTrnsYn(toNull(getField(f, KOSDAQ_IDX_KRX_TRNS)));

        String kd150 = getField(f, KOSDAQ_IDX_KOSDAQ150);
        dto.setKosdaq150Yn("Y".equalsIgnoreCase(kd150) ? "Y" : null);

        dto.setStckSdpr(toNull(getField(f, KOSDAQ_IDX_STCK_SDPR)));
        dto.setFrmlMrktDealQtyUnit(toNull(getField(f, KOSDAQ_IDX_FRML_QTY)));
        dto.setOvtmMrktDealQtyUnit(toNull(getField(f, KOSDAQ_IDX_OVTM_QTY)));
        dto.setSltrYn(toNull(getField(f, KOSDAQ_IDX_LIQUIDATION)));
        dto.setMangYn(toNull(getField(f, KOSDAQ_IDX_CAUTION)));
        dto.setMrktAlrmClsCd(toNull(getField(f, KOSDAQ_IDX_MRKT_ALRM)));
        dto.setMrktAlrmRiskYn(toNull(getField(f, KOSDAQ_IDX_MRKT_ALRM_RISK)));
        dto.setInsnPbntYn(toNull(getField(f, KOSDAQ_IDX_INSN_PBNT)));
        dto.setBypsLstnYn(toNull(getField(f, KOSDAQ_IDX_BYPS_LSTN)));
        dto.setFlngClsCd(toNull(getField(f, KOSDAQ_IDX_FLNG_CLS)));
        dto.setFcamModClsCd(toNull(getField(f, KOSDAQ_IDX_FCAM_MOD)));
        dto.setIcicClsCd(toNull(getField(f, KOSDAQ_IDX_ICIC_CLS)));
        dto.setMargRate(toNull(getField(f, KOSDAQ_IDX_MARG_RATE)));
        dto.setCrdtAble(toNull(getField(f, KOSDAQ_IDX_CRDT_ABLE)));
        dto.setCrdtDays(toNull(getField(f, KOSDAQ_IDX_CRDT_DAYS)));
        dto.setPrdyVol(toNull(getField(f, KOSDAQ_IDX_PRDY_VOL)));
        dto.setStckFcam(toNull(getField(f, KOSDAQ_IDX_STCK_FCAM)));
        dto.setListDt(extractDate8(getField(f, KOSDAQ_IDX_LIST_DT)));
        dto.setLstnStcn(toNull(getField(f, KOSDAQ_IDX_LSTN_STCN)));
        dto.setCpfn(toNull(getField(f, KOSDAQ_IDX_CPFN)));
        dto.setSettleMonth(toNull(getField(f, KOSDAQ_IDX_SETTLE_MONTH)));
        dto.setPoPrc(toNull(getField(f, KOSDAQ_IDX_PO_PRC)));

        String prefRaw = getField(f, KOSDAQ_IDX_PREF_YN);
        dto.setPrefYn((!isBlank(prefRaw) && !"0".equals(prefRaw.trim())) ? "Y" : "N");

        dto.setSstsHotYn(toNull(getField(f, KOSDAQ_IDX_SSTS_HOT)));
        dto.setStangeRunupYn(toNull(getField(f, KOSDAQ_IDX_STANGE_RUNUP)));
        dto.setKrx300Yn(toNull(getField(f, KOSDAQ_IDX_KRX300)));
        dto.setSaleAccount(toNull(getField(f, KOSDAQ_IDX_SALE_ACCOUNT)));
        dto.setBsopPrfi(toNull(getField(f, KOSDAQ_IDX_BSOP_PRFI)));
        dto.setOpPrfi(toNull(getField(f, KOSDAQ_IDX_OP_PRFI)));
        dto.setThtrNtin(toNull(getField(f, KOSDAQ_IDX_THTR_NTIN)));
        dto.setRoeVal(toNull(getField(f, KOSDAQ_IDX_ROE)));
        dto.setBaseDt(toNull(getField(f, KOSDAQ_IDX_BASE_DT)));
        dto.setPrdyAvlsScal(toNull(getField(f, KOSDAQ_IDX_PRDY_AVLS_SCAL)));
        dto.setCoCd(toNull(getField(f, KOSDAQ_IDX_CO_CD)));
        dto.setCoCrdtLimtOverYn(toNull(getField(f, KOSDAQ_IDX_CO_CRDT_LIMT)));
        dto.setSecuLendAbleYn(toNull(getField(f, KOSDAQ_IDX_SECU_LEND)));
        dto.setStlnAbleYn(toNull(getField(f, KOSDAQ_IDX_STLN_ABLE)));
    }

    /* ═══════════════════════════════════════════════════════════
       US (NASDAQ + NYSE)
    ═══════════════════════════════════════════════════════════ */

    private int[] refreshUs(String stockType) throws Exception {
        System.out.println("[StkMasterRefreshService] US 종목 마스터 갱신 시작 (stockType=" + stockType + ")");

        List<StkMasterDto> allList = new ArrayList<StkMasterDto>();
        allList.addAll(downloadAndParseCod(URL_NASDAQ, "nasmst.cod", "NASDAQ"));
        allList.addAll(downloadAndParseCod(URL_NYSE,   "nysmst.cod", "NYSE"));

        Set<String> downloadedCodes = new HashSet<String>();
        int mergedCnt = 0;
        for (int i = 0; i < allList.size(); i++) {
            StkMasterDto dto = allList.get(i);
            if (dto == null || isBlank(dto.getStkCd())) continue;
            /* stockType 필터: ALL=전체, STOCK/ETF/ETN/ELW=해당 타입만 */
            if (!"ALL".equals(stockType) && !stockType.equals(dto.getStkType())) continue;
            stkMasterDao.mergeStkMaster(dto);
            downloadedCodes.add(dto.getStkCd());
            mergedCnt++;
        }

        int delistedCnt = 0;
        if ("ALL".equals(stockType)) {
            delistedCnt  = markDelisted("NASDAQ", downloadedCodes);
            delistedCnt += markDelisted("NYSE",   downloadedCodes);
        }

        System.out.println("[StkMasterRefreshService] US 갱신 완료 — merged=" + mergedCnt + ", delisted=" + delistedCnt);
        return new int[]{mergedCnt, delistedCnt};
    }

    private List<StkMasterDto> downloadAndParseCod(String url, String entryName, String mktCd) throws Exception {
        System.out.println("[StkMasterRefreshService] " + mktCd + " 파일 다운로드: " + url);
        InputStream is = downloadZip(url, entryName);
        try {
            return parseCod(is, mktCd);
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * 해외 .cod TSV 파일 파싱
     * secType: 1=Index, 2=Stock, 3=ETP(ETF), 4=Warrant — Index는 제외
     */
    private List<StkMasterDto> parseCod(InputStream is, String mktCd) throws Exception {
        List<StkMasterDto> list = new ArrayList<StkMasterDto>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "MS949"));
        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) { firstLine = false; continue; } /* 헤더 skip */

            String[] cols = line.split("\t", -1);
            if (cols.length <= COD_IDX_SEC_TYPE) continue;

            String secType = trim(cols[COD_IDX_SEC_TYPE]);
            if ("1".equals(secType)) continue; /* Index 제외 */

            String symbol    = trim(cols[COD_IDX_SYMBOL]);
            String koreaName = trim(cols[COD_IDX_KOREA_NAME]);
            String engName   = cols.length > COD_IDX_ENG_NAME  ? trim(cols[COD_IDX_ENG_NAME])  : "";
            String currency  = cols.length > COD_IDX_CURRENCY  ? trim(cols[COD_IDX_CURRENCY])  : "";
            String natnCd    = cols.length > COD_IDX_NATN_CD   ? trim(cols[COD_IDX_NATN_CD])   : "";
            String exchCd    = cols.length > COD_IDX_EXCH_CODE ? trim(cols[COD_IDX_EXCH_CODE]) : "";
            String ovrsBstp  = cols.length > COD_IDX_OVRS_BSTP ? trim(cols[COD_IDX_OVRS_BSTP]) : "";
            String etpCls    = cols.length > COD_IDX_ETP_CLS   ? trim(cols[COD_IDX_ETP_CLS])   : "";

            if (isBlank(symbol)) continue;

            String stkType   = resolveOvrsStkType(secType, etpCls);
            String scrtGrpCd = "2".equals(secType) ? "ST" : "3".equals(secType) ? "EF" : "EW";

            StkMasterDto dto = new StkMasterDto();
            dto.setStkCd(symbol);
            dto.setSymbol(symbol);
            dto.setStkNm(isBlank(koreaName) ? symbol : koreaName);
            dto.setStkNmEn(isBlank(engName) ? null : engName);
            dto.setMktCd(mktCd);
            dto.setStkStatus("NORMAL");
            dto.setStkType(stkType);
            dto.setScrtGrpClsCd(scrtGrpCd);
            dto.setNatnCd(isBlank(natnCd) ? null : natnCd);
            dto.setExchCd(isBlank(exchCd) ? null : exchCd);
            dto.setOvrsBstpCd(isBlank(ovrsBstp) ? null : ovrsBstp);
            dto.setCurrency(isBlank(currency) ? null : currency);
            /* DOW30_YN / NAS100_YN / SNP500_YN : .cod 파일에 없음 → NULL */
            list.add(dto);
        }

        System.out.println("[StkMasterRefreshService] " + mktCd + " 파싱 완료: " + list.size() + "건");
        return list;
    }

    /**
     * 해외 종목 STK_TYPE 결정
     * secType: 2=Stock, 3=ETP(ETF), 4=Warrant
     * etpCls: 001=ETF, 002=ETN, 003=ETC, 004=Others, 005=VIX ETF, 006=VIX ETN
     */
    private String resolveOvrsStkType(String secType, String etpCls) {
        if ("4".equals(secType)) return "ELW";
        if ("3".equals(secType)) {
            if ("002".equals(etpCls) || "006".equals(etpCls)) return "ETN";
            return "ETF"; /* 001/003/004/005 또는 미분류 */
        }
        return "STOCK";
    }

    /* ═══════════════════════════════════════════════════════════
       상장폐지 감지
    ═══════════════════════════════════════════════════════════ */

    private int markDelisted(String mktCd, Set<String> downloadedCodes) throws Exception {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("mktCd", mktCd);
        List<String> dbCodes = stkMasterDao.selectActiveStkCdListByMktCd(param);

        int delistedCnt = 0;
        for (int i = 0; dbCodes != null && i < dbCodes.size(); i++) {
            String stkCd = dbCodes.get(i);
            if (isBlank(stkCd) || downloadedCodes.contains(stkCd)) continue;
            HashMap<String, String> delParam = new HashMap<String, String>();
            delParam.put("stkCd", stkCd);
            if (stkMasterDao.updateStkMasterDelist(delParam) > 0) {
                delistedCnt++;
                System.out.println("[StkMasterRefreshService] 상장폐지 처리: " + stkCd + " (" + mktCd + ")");
            }
        }
        return delistedCnt;
    }

    /* ═══════════════════════════════════════════════════════════
       유틸 — 다운로드
    ═══════════════════════════════════════════════════════════ */

    private InputStream downloadZip(String urlStr, String entryName) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "scheduler/1.0");

        ZipInputStream zis = new ZipInputStream(conn.getInputStream());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equalsIgnoreCase(entryName)) return zis;
            zis.closeEntry();
        }
        zis.close();
        throw new IllegalStateException("ZIP 내 파일을 찾을 수 없습니다: " + entryName + " (url=" + urlStr + ")");
    }

    /* ═══════════════════════════════════════════════════════════
       유틸 — 고정폭 파싱
    ═══════════════════════════════════════════════════════════ */

    private String[] parseFixedWidth(String line, int[] fieldSpecs) {
        String[] result = new String[fieldSpecs.length];
        int pos = 0;
        for (int i = 0; i < fieldSpecs.length; i++) {
            int end = pos + fieldSpecs[i];
            if (pos >= line.length()) {
                result[i] = "";
            } else if (end > line.length()) {
                result[i] = line.substring(pos).trim();
            } else {
                result[i] = line.substring(pos, end).trim();
            }
            pos = end;
        }
        return result;
    }

    private String getField(String[] fields, int idx) {
        if (fields == null || idx < 0 || idx >= fields.length) return "";
        return fields[idx] == null ? "" : fields[idx];
    }

    private String extractDate8(String raw) {
        if (isBlank(raw)) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 8) return null;
        String yyyymmdd = digits.substring(0, 8);
        if ("19000101".compareTo(yyyymmdd) >= 0) return null;
        try {
            int month = Integer.parseInt(yyyymmdd.substring(4, 6));
            int day   = Integer.parseInt(yyyymmdd.substring(6, 8));
            if (month < 1 || month > 12 || day < 1 || day > 31) return null;
        } catch (NumberFormatException ignore) {
            return null;
        }
        return yyyymmdd;
    }

    /* ═══════════════════════════════════════════════════════════
       유틸 — 공통
    ═══════════════════════════════════════════════════════════ */

    private String trim(String v) { return v == null ? "" : v.trim(); }

    private boolean isBlank(String v) { return v == null || v.trim().length() == 0; }

    private String toNull(String v) { return isBlank(v) ? null : v.trim(); }

    private void closeQuietly(InputStream is) {
        if (is != null) { try { is.close(); } catch (Exception ignore) {} }
    }
}
