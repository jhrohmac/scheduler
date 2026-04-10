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

    /* ─── .mst 고정폭 파싱 스펙 ────────────────────────────────── */
    /** KOSPI Part2 = 마지막 228바이트, 70필드 */
    private static final int   KOSPI_PART2_LEN   = 228;
    private static final int[] KOSPI_FIELD_SPECS  = {
        2, 1, 4, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 5, 5, 1, 1, 1, 2, 1, 1,
        1, 2, 2, 2, 3, 1, 3,12,12, 8,15,21, 2, 7, 1, 1, 1, 1, 1, 9,
        9, 9, 5, 9, 8, 9, 3, 1, 1, 1
    };
    /* Part2 필드 인덱스 (0-based)
       8  = KOSPI200섹터업종  (width 1, '0'/blank → 미편입, 나머지 → KOSPI200)
       34 = 거래정지           (width 1, Y/N)
       35 = 정리매매           (width 1, Y/N)
       36 = 관리종목           (width 1, Y/N)
       49 = 상장일자           (width 8, YYYYMMDD)
    */
    private static final int KOSPI_IDX_KOSPI200   = 8;
    private static final int KOSPI_IDX_HALT       = 34;
    private static final int KOSPI_IDX_LIQUIDATION = 35;
    private static final int KOSPI_IDX_CAUTION    = 36;
    private static final int KOSPI_IDX_LIST_DT    = 49;

    /** KOSDAQ Part2 = 마지막 222바이트, 63필드 */
    private static final int   KOSDAQ_PART2_LEN  = 222;
    private static final int[] KOSDAQ_FIELD_SPECS = {
        2, 1, 4, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 9, 5, 5, 1, 1, 1, 2, 1, 1, 1, 2, 2, 2, 3, 1,
        3,12,12, 8,15,21, 2, 7, 1, 1, 1, 1, 9, 9, 9, 5, 9, 8, 9, 3,
        1, 1, 1
    };
    /* Part2 필드 인덱스 (0-based)
       25 = KOSDAQ150지수여부  (width 9, 'Y'/blank)
       29 = 거래정지여부        (width 1, Y/N)
       30 = 정리매매여부        (width 1, Y/N)
       31 = 관리종목여부        (width 2)
       44 = 주식상장일자        (width 15, YYYYMMDD 포함)
    */
    private static final int KOSDAQ_IDX_KOSDAQ150  = 25;
    private static final int KOSDAQ_IDX_HALT       = 29;
    private static final int KOSDAQ_IDX_LIQUIDATION = 30;
    private static final int KOSDAQ_IDX_CAUTION    = 31;
    private static final int KOSDAQ_IDX_LIST_DT    = 44;

    /* ─── 해외 .cod TSV 컬럼 인덱스 (0-based, 첫 행=헤더 skip) ── */
    /* overseas_stock_code.py 기준
       2  = Exchange code  ('NAS' → NASDAQ, 'NYS' → NYSE)
       4  = Symbol         (종목코드)
       6  = Korea name     (한글종목명)
       8  = Security type  ('2' = 주식만 처리)
    */
    private static final int COD_IDX_EXCH_CODE   = 2;
    private static final int COD_IDX_SYMBOL       = 4;
    private static final int COD_IDX_KOREA_NAME   = 6;
    private static final int COD_IDX_SEC_TYPE     = 8;

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
     *            stockType:   ALL | STOCK | ETF (생략 시 ALL — 전체 처리)
     */
    public HashMap<String, Object> refresh(HashMap<String, String> map) throws Exception {
        String marketGroup = trim(map == null ? null : map.get("marketGroup"));
        if (isBlank(marketGroup)) {
            marketGroup = "ALL";
        }
        marketGroup = marketGroup.toUpperCase();

        String stockType = trim(map == null ? null : map.get("stockType"));
        if (isBlank(stockType) || "ALL".equalsIgnoreCase(stockType)) {
            stockType = "ALL";
        }
        stockType = stockType.toUpperCase();

        int krMerged = 0, krDelisted = 0;
        int usMerged = 0, usDelisted = 0;
        String lastError = null;

        if ("KR".equals(marketGroup) || "ALL".equals(marketGroup)) {
            try {
                int[] krResult = refreshKr(stockType);
                krMerged   = krResult[0];
                krDelisted = krResult[1];
            } catch (Exception e) {
                lastError = "KR 갱신 오류: " + e.getMessage();
                System.err.println("[StkMasterRefreshService] " + lastError);
            }
        }

        if ("US".equals(marketGroup) || "ALL".equals(marketGroup)) {
            try {
                int[] usResult = refreshUs(stockType);
                usMerged   = usResult[0];
                usDelisted = usResult[1];
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

        List<StkMasterDto> kospiList  = downloadAndParseKospi();
        List<StkMasterDto> kosdaqList = downloadAndParseKosdaq();

        List<StkMasterDto> allList = new ArrayList<StkMasterDto>();
        allList.addAll(kospiList);
        allList.addAll(kosdaqList);

        Set<String> downloadedCodes = new HashSet<String>();
        int mergedCnt = 0;
        for (int i = 0; i < allList.size(); i++) {
            StkMasterDto dto = allList.get(i);
            if (dto == null || isBlank(dto.getStkCd())) {
                continue;
            }
            /* stockType 필터: ALL이 아닌 경우 해당 구분만 처리 */
            if (!"ALL".equals(stockType) && !stockType.equalsIgnoreCase(dto.getStkType())) {
                continue;
            }
            stkMasterDao.mergeStkMaster(dto);
            downloadedCodes.add(dto.getStkCd());
            mergedCnt++;
        }

        /* KOSPI / KOSDAQ 상장폐지 감지 (전체 처리 시에만 수행) */
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
     * 각 행 구조:
     *   Part1 = row[0 : len - part2Len]
     *     - [0:9]  단축코드
     *     - [9:21] 표준코드
     *     - [21:]  한글명
     *   Part2 = row[-part2Len:]
     *     - fieldSpecs 배열로 고정폭 분리
     */
    private List<StkMasterDto> parseMst(InputStream is, int part2Len, int[] fieldSpecs,
                                         String mktCd) throws Exception {
        List<StkMasterDto> list = new ArrayList<StkMasterDto>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "MS949"));
        String line;

        boolean isKospi  = "KOSPI".equals(mktCd);
        boolean isKosdaq = "KOSDAQ".equals(mktCd);

        while ((line = reader.readLine()) != null) {
            if (line.length() <= part2Len) {
                continue;
            }

            /* Part1 분리 */
            int p2Start = line.length() - part2Len;
            String part1 = line.substring(0, p2Start);
            String stkCd   = trim(part1.length() >= 9  ? part1.substring(0, 9)  : part1);
            String stdCd   = trim(part1.length() >= 21 ? part1.substring(9, 21) : ""); /* 표준코드 12자리 */
            String stkNm   = trim(part1.length() > 21  ? part1.substring(21)    : "");

            if (isBlank(stkCd)) {
                continue;
            }

            /* STK_TYPE 결정: 표준코드 prefix KR3* → ETF, 나머지 → STOCK */
            String stkType = stdCd.startsWith("KR3") ? "ETF" : "STOCK";

            /* Part2 고정폭 파싱 */
            String part2 = line.substring(p2Start);
            String[] fields = parseFixedWidth(part2, fieldSpecs);

            /* STK_STATUS 결정 (정리매매 > 거래정지 > 관리종목 > NORMAL) */
            String stkStatus;
            if (isKospi) {
                String liquidation = getField(fields, KOSPI_IDX_LIQUIDATION);
                String halt        = getField(fields, KOSPI_IDX_HALT);
                String caution     = getField(fields, KOSPI_IDX_CAUTION);
                if ("Y".equalsIgnoreCase(liquidation)) {
                    stkStatus = "DELIST";
                } else if ("Y".equalsIgnoreCase(halt)) {
                    stkStatus = "HALT";
                } else if ("Y".equalsIgnoreCase(caution)) {
                    stkStatus = "CAUTION";
                } else {
                    stkStatus = "NORMAL";
                }
            } else { /* KOSDAQ */
                String liquidation = getField(fields, KOSDAQ_IDX_LIQUIDATION);
                String halt        = getField(fields, KOSDAQ_IDX_HALT);
                String caution     = getField(fields, KOSDAQ_IDX_CAUTION);
                if ("Y".equalsIgnoreCase(liquidation)) {
                    stkStatus = "DELIST";
                } else if ("Y".equalsIgnoreCase(halt)) {
                    stkStatus = "HALT";
                } else if (!isBlank(caution)) {
                    stkStatus = "CAUTION";
                } else {
                    stkStatus = "NORMAL";
                }
            }

            /* INDEX_CD 결정 */
            String indexCd = null;
            if (isKospi) {
                String kospi200Sector = getField(fields, KOSPI_IDX_KOSPI200);
                /* '0' 이 아니고 공백도 아니면 KOSPI200 편입 */
                if (!isBlank(kospi200Sector) && !"0".equals(kospi200Sector)) {
                    indexCd = "KOSPI200";
                }
            } else if (isKosdaq) {
                String kosdaq150yn = getField(fields, KOSDAQ_IDX_KOSDAQ150);
                if ("Y".equalsIgnoreCase(kosdaq150yn)) {
                    indexCd = "KOSDAQ150";
                }
            }

            /* 상장일자 */
            String listDt = null;
            if (isKospi) {
                listDt = extractDate8(getField(fields, KOSPI_IDX_LIST_DT));
            } else {
                listDt = extractDate8(getField(fields, KOSDAQ_IDX_LIST_DT));
            }

            StkMasterDto dto = new StkMasterDto();
            dto.setStkCd(stkCd);
            dto.setStkNm(stkNm);
            dto.setMktCd(mktCd);
            dto.setStkStatus(stkStatus);
            dto.setStkType(stkType);
            dto.setListDt(listDt);
            dto.setIndexCd(indexCd);
            list.add(dto);
        }

        System.out.println("[StkMasterRefreshService] " + mktCd + " 파싱 완료: " + list.size() + "건");
        return list;
    }

    /* ═══════════════════════════════════════════════════════════
       US (NASDAQ + NYSE)
    ═══════════════════════════════════════════════════════════ */

    private int[] refreshUs(String stockType) throws Exception {
        System.out.println("[StkMasterRefreshService] US 종목 마스터 갱신 시작 (stockType=" + stockType + ")");

        List<StkMasterDto> nasdaqList = downloadAndParseCod(URL_NASDAQ, "nasmst.cod", "NASDAQ");
        List<StkMasterDto> nyseList   = downloadAndParseCod(URL_NYSE,   "nysmst.cod", "NYSE");

        List<StkMasterDto> allList = new ArrayList<StkMasterDto>();
        allList.addAll(nasdaqList);
        allList.addAll(nyseList);

        Set<String> downloadedCodes = new HashSet<String>();
        int mergedCnt = 0;
        for (int i = 0; i < allList.size(); i++) {
            StkMasterDto dto = allList.get(i);
            if (dto == null || isBlank(dto.getStkCd())) {
                continue;
            }
            /* US는 현재 secType=2(주식)만 파싱 → 기본 STOCK
               stockType=ETF 요청 시 US 종목은 건너뜀 */
            if ("ETF".equals(stockType)) {
                continue;
            }
            stkMasterDao.mergeStkMaster(dto);
            downloadedCodes.add(dto.getStkCd());
            mergedCnt++;
        }

        /* NASDAQ / NYSE 상장폐지 감지 (전체 처리 시에만 수행) */
        int delistedCnt = 0;
        if ("ALL".equals(stockType)) {
            delistedCnt  = markDelisted("NASDAQ", downloadedCodes);
            delistedCnt += markDelisted("NYSE",   downloadedCodes);
        }

        System.out.println("[StkMasterRefreshService] US 갱신 완료 — merged=" + mergedCnt + ", delisted=" + delistedCnt);
        return new int[]{mergedCnt, delistedCnt};
    }

    private List<StkMasterDto> downloadAndParseCod(String url, String entryName,
                                                    String mktCd) throws Exception {
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
     *
     * 파일 구조 (overseas_stock_code.py 기준):
     *   - cp949 인코딩, 탭 구분
     *   - 첫 번째 행: 헤더 (skip)
     *   - 컬럼 [2]: Exchange code ('NAS' / 'NYS')
     *   - 컬럼 [4]: Symbol (종목코드)
     *   - 컬럼 [6]: Korea name (한글명)
     *   - 컬럼 [8]: Security type (2 = 주식만 처리)
     */
    private List<StkMasterDto> parseCod(InputStream is, String mktCd) throws Exception {
        List<StkMasterDto> list = new ArrayList<StkMasterDto>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "MS949"));
        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false; /* 헤더 행 skip */
                continue;
            }

            String[] cols = line.split("\t", -1);
            if (cols.length <= COD_IDX_SEC_TYPE) {
                continue;
            }

            /* 주식(Security type = '2')만 처리 */
            String secType = trim(cols[COD_IDX_SEC_TYPE]);
            if (!"2".equals(secType)) {
                continue;
            }

            String symbol    = trim(cols[COD_IDX_SYMBOL]);
            String koreaName = trim(cols[COD_IDX_KOREA_NAME]);

            if (isBlank(symbol)) {
                continue;
            }

            StkMasterDto dto = new StkMasterDto();
            dto.setStkCd(symbol);
            dto.setStkNm(isBlank(koreaName) ? symbol : koreaName);
            dto.setMktCd(mktCd);
            dto.setStkStatus("NORMAL");
            dto.setStkType("STOCK"); /* .cod secType=2(주식)만 파싱 */
            dto.setListDt(null);
            dto.setIndexCd(null); /* S&P500/DOW 정보는 .cod 파일에 없음 */
            list.add(dto);
        }

        System.out.println("[StkMasterRefreshService] " + mktCd + " 파싱 완료: " + list.size() + "건");
        return list;
    }

    /* ═══════════════════════════════════════════════════════════
       상장폐지 감지
    ═══════════════════════════════════════════════════════════ */

    /**
     * DB에는 있으나 다운로드 파일에 없는 종목 → DELIST 처리
     *
     * @param mktCd           시장코드 (KOSPI / KOSDAQ / NASDAQ / NYSE)
     * @param downloadedCodes 다운로드 파일에서 수집된 종목코드 Set
     * @return 상장폐지 처리된 종목 수
     */
    private int markDelisted(String mktCd, Set<String> downloadedCodes) throws Exception {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("mktCd", mktCd);
        List<String> dbCodes = stkMasterDao.selectActiveStkCdListByMktCd(param);

        int delistedCnt = 0;
        for (int i = 0; dbCodes != null && i < dbCodes.size(); i++) {
            String stkCd = dbCodes.get(i);
            if (isBlank(stkCd) || downloadedCodes.contains(stkCd)) {
                continue;
            }
            HashMap<String, String> delParam = new HashMap<String, String>();
            delParam.put("stkCd", stkCd);
            int updated = stkMasterDao.updateStkMasterDelist(delParam);
            if (updated > 0) {
                delistedCnt++;
                System.out.println("[StkMasterRefreshService] 상장폐지 처리: " + stkCd + " (" + mktCd + ")");
            }
        }
        return delistedCnt;
    }

    /* ═══════════════════════════════════════════════════════════
       유틸 — 다운로드
    ═══════════════════════════════════════════════════════════ */

    /**
     * ZIP URL 다운로드 후 지정 entryName 에 해당하는 ZipEntry InputStream 반환
     * 호출측에서 반드시 closeQuietly 해야 함
     */
    private InputStream downloadZip(String urlStr, String entryName) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(HTTP_TIMEOUT_MS);
        conn.setReadTimeout(HTTP_TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "scheduler/1.0");

        ZipInputStream zis = new ZipInputStream(conn.getInputStream());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equalsIgnoreCase(entryName)) {
                return zis;
            }
            zis.closeEntry();
        }
        zis.close();
        throw new IllegalStateException("ZIP 내 파일을 찾을 수 없습니다: " + entryName + " (url=" + urlStr + ")");
    }

    /* ═══════════════════════════════════════════════════════════
       유틸 — 고정폭 파싱
    ═══════════════════════════════════════════════════════════ */

    /**
     * 고정폭 문자열을 fieldSpecs 배열 기준으로 분리
     * 각 필드는 trim() 처리된 상태로 반환
     */
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
        if (fields == null || idx < 0 || idx >= fields.length) {
            return "";
        }
        return fields[idx] == null ? "" : fields[idx];
    }

    /**
     * 날짜 문자열에서 YYYYMMDD 8자리 숫자만 추출
     * (KOSPI 8자리 그대로, KOSDAQ 15자리 필드에서 앞 8자리 추출)
     */
    private String extractDate8(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 8) {
            return null;
        }
        String yyyymmdd = digits.substring(0, 8);
        /* 19000101 이하이면 유효하지 않음 */
        if ("19000101".compareTo(yyyymmdd) >= 0) {
            return null;
        }
        /* 월(01-12) / 일(01-31) 범위 검증 — ORA-01843 방지 */
        try {
            int month = Integer.parseInt(yyyymmdd.substring(4, 6));
            int day   = Integer.parseInt(yyyymmdd.substring(6, 8));
            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }
        } catch (NumberFormatException ignore) {
            return null;
        }
        return yyyymmdd;
    }

    /* ═══════════════════════════════════════════════════════════
       유틸 — 공통
    ═══════════════════════════════════════════════════════════ */

    private String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().length() == 0;
    }

    private void closeQuietly(InputStream is) {
        if (is != null) {
            try { is.close(); } catch (Exception ignore) {}
        }
    }
}
