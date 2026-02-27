package com.scheduler.finance.dao.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.finance.dao.BatchJobCtrlDao;
import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.finance.module.MovingAveragesLineNumber;
import com.scheduler.finance.module.StockAnalysisUtil;
import com.scheduler.finance.vo.BatchJobCtrlVo;
import com.scheduler.finance.vo.StockDataVo;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.kis_api.api.rest.quotations.EtfetnInquireComponentStockPriceApi;
import com.scheduler.kis_api.api.rest.quotations.EtfetnInquireComponentStockPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyIndexchartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyIndexchartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireTimeDailychartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireTimeDailychartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.SearchStockInfoApi;
import com.scheduler.kis_api.api.rest.quotations.SearchStockInfoResult;
import com.scheduler.kis_api.api.rest.quotations.SearchOverseasStockInfoApi;
import com.scheduler.kis_api.api.rest.quotations.SearchOverseasStockInfoResult;
import com.scheduler.kis_client.KisClient;

public class BatchJobCtrlDaoImpl extends SqlSessionDaoSupport implements BatchJobCtrlDao {
    // WF-2-1: 배치/분석 저장, WF-2-2: 차트 분석 신호, WF-2-9: 실시간 신호 스트림 연계(확장 예정)

    private static final String NS = "sql.BatchJobCtrl.";
    private static final String NS_BATCH = "com.scheduler.finance.sql.BatchStock.";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<String, Thread> RUNNING_THREADS = new ConcurrentHashMap<String, Thread>();
    private static final Map<String, AtomicBoolean> STOP_FLAGS = new ConcurrentHashMap<String, AtomicBoolean>();

    private static class StopRequestedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        StopRequestedException(String msg) { super(msg); }
    }

    @Override
    public List<BatchJobCtrlVo> selectJobList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectJobList", map);
    }

    @Override
    public BatchJobCtrlVo selectJobOne(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectJobOne", map);
    }

    @Override
    public int insertJobCtrl(HashMap<String, String> map) throws Exception {
        map.put("job_id", nvl(map.get("job_id"), ""));
        map.put("job_name", nvl(map.get("job_name"), ""));
        map.put("enable_yn", nvl(map.get("enable_yn"), "N"));
        map.put("interval_min", nvl(map.get("interval_min"), "60"));
        map.put("limit_cnt", nvl(map.get("limit_cnt"), "200"));
        map.put("stale_days", nvl(map.get("stale_days"), "7"));
        map.put("country_code", nvl(map.get("country_code"), "KR"));

        Integer exists = getSqlSession().selectOne(NS + "selectJobIdExists", map);
        if (exists != null && exists.intValue() > 0) {
            return getSqlSession().update(NS + "updateJobCtrl", map);
        }
        return getSqlSession().insert(NS + "insertJobCtrl", map);
    }

    @Override
    public int deleteJobCtrl(HashMap<String, String> map) throws Exception {
        map.put("job_id", nvl(map.get("job_id"), ""));
        return getSqlSession().delete(NS + "deleteJobCtrl", map);
    }

    @Override
    public int updateJobCtrl(HashMap<String, String> map) throws Exception {
        map.put("job_name", nvl(map.get("job_name"), ""));
        map.put("enable_yn", nvl(map.get("enable_yn"), "N"));
        map.put("interval_min", nvl(map.get("interval_min"), "60"));
        map.put("limit_cnt", nvl(map.get("limit_cnt"), "200"));
        map.put("stale_days", nvl(map.get("stale_days"), "7"));
        map.put("country_code", nvl(map.get("country_code"), "KR"));
        return getSqlSession().update(NS + "updateJobCtrl", map);
    }

    private void recoverStaleRunningIfNeeded(BatchJobCtrlVo job, int staleMinutes) {
        if (job == null) return;
        try {
            HashMap<String, String> m = new HashMap<String, String>();
            m.put("job_id", nvl(job.getJob_id(), ""));
            m.put("stale_minutes", String.valueOf(staleMinutes));
            m.put("last_result_msg", "AUTO_RECOVER_STALE_RUNNING(" + staleMinutes + "m)");
            getSqlSession().update(NS + "forceClearRunningIfStale", m);
        } catch (Exception ignore) {
        }
    }

    private int staleRecoverMinutes(String jobId) {
        String id = nvl(jobId, "");
        if ("STOCK_SMA_ANALYSIS_REFRESH".equals(id) || "STOCK_30MIN_ANALYSIS_REFRESH".equals(id)) {
            return 180; // 분석 배치는 종목 수가 많아 장시간 실행 가능
        }
        return 10;
    }

    @Override
    public Map<String, Object> runJobNow(HashMap<String, String> map) throws Exception {
        try {
            String jobId = nvl(map.get("job_id"), "INTEREST_STOCK_MASTER_REFRESH");
            
            HashMap<String, String> q = new HashMap<String, String>();
            q.put("job_id", jobId);

            BatchJobCtrlVo job = selectJobOne(q);
            if (job == null) {
                return out("ERROR", "JOB NOT FOUND", null);
            }
            recoverStaleRunningIfNeeded(job, staleRecoverMinutes(jobId));
            job = selectJobOne(q);

            // Allow run-now overrides (do not persist to TB_S_BATCH_JOB_CTRL)
            String overrideCountry = nvl(map.get("stock_country_code"), "").trim();
            if (!overrideCountry.isEmpty()) {
                job.setCountry_code(overrideCountry);
            }
            String overrideLimit = nvl(map.get("limit"), "").trim();
            if (!overrideLimit.isEmpty()) {
                job.setLimit_cnt(overrideLimit);
            }
            String overrideStale = nvl(map.get("stale_days"), "").trim();
            if (!overrideStale.isEmpty()) {
                job.setStale_days(overrideStale);
            }

            if (!tryLock(jobId)) {
                // 재기동 후 DB RUNNING_YN만 남은 고아 락(orphan lock) 자동 복구
                Thread running = RUNNING_THREADS.get(jobId);
                boolean orphan = (running == null || !running.isAlive());
                if (orphan) {
                    HashMap<String, String> orphanMap = new HashMap<String, String>();
                    orphanMap.put("job_id", jobId);
                    orphanMap.put("last_result_msg", "AUTO_RECOVER_ORPHAN_LOCK");
                    getSqlSession().update(NS + "forceClearRunning", orphanMap);
                    RUNNING_THREADS.remove(jobId);
                    STOP_FLAGS.remove(jobId);

                    if (!tryLock(jobId)) {
                        return out("SKIP", "RUNNING_YN=Y (already running)", null);
                    }
                } else {
                    return out("SKIP", "RUNNING_YN=Y (already running)", null);
                }
            }
            register(jobId);
            String resultCode = "SUCCESS";
            String resultMsg = "OK";
            int runCnt = 0;
            Map<String, Object> runResult = null;
            
            try {
                runResult = executeJob(jobId, job);
                runCnt = toInt(runResult.get("updatedCount"), 0);
                resultMsg = "updated=" + runResult.get("updatedCount")
                        + ", skipped=" + runResult.get("skippedCount")
                        + ", failed=" + runResult.get("failedCount");
            } catch (StopRequestedException se) {
                resultCode = "STOP";
                resultMsg = se.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resultCode = "ERROR";
                resultMsg = e.getLocalizedMessage();
            } finally {
                finish(jobId, resultCode, resultMsg, runCnt);
                unregister(jobId);
            }

            return out(resultCode, resultMsg, runResult);

        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".runJobNow() : ", e.getLocalizedMessage());
        }
    }

    @Override
    public Map<String, Object> schedulerTick(HashMap<String, String> map) throws Exception {
        try {
            List<BatchJobCtrlVo> jobs = selectJobList(new HashMap<String, String>());

            int triggered = 0;
            int skipped = 0;

            if (jobs == null || jobs.isEmpty()) {
                return out("SUCCESS", "NO JOB", null);
            }

            for (BatchJobCtrlVo job : jobs) {

                if (!"Y".equalsIgnoreCase(nvl(job.getEnable_yn(), "N"))) {
                    skipped++;
                    continue;
                }

                recoverStaleRunningIfNeeded(job, staleRecoverMinutes(job.getJob_id()));

                if (!isDue(job.getJob_id())) {
                    skipped++;
                    continue;
                }
                if (!tryLock(job.getJob_id())) {
                    skipped++;
                    continue;
                }
                triggered++;

                register(job.getJob_id());

                String resultCode = "SUCCESS";
                String resultMsg = "OK";
                int runCnt = 0;

                try {
                    Map<String, Object> runResult = executeJob(job.getJob_id(), job);
                    runCnt = toInt(runResult.get("updatedCount"), 0);
                    resultMsg = "updated=" + runResult.get("updatedCount")
                            + ", skipped=" + runResult.get("skippedCount")
                            + ", failed=" + runResult.get("failedCount");
                } catch (StopRequestedException se) {
                    resultCode = "STOP";
                    resultMsg = se.getMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                    resultCode = "ERROR";
                    resultMsg = e.getLocalizedMessage();
                } finally {
                    finish(job.getJob_id(), resultCode, resultMsg, runCnt);
                    unregister(job.getJob_id());
                }
            }

            Map<String, Object> meta = new LinkedHashMap<String, Object>();
            meta.put("triggered", triggered);
            meta.put("skipped", skipped);

            return out("SUCCESS", "TICK OK", meta);

        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".schedulerTick() : ", e.getLocalizedMessage());
        }
    }

    @Override
    public Map<String, Object> stopJob(HashMap<String, String> map) throws Exception {
        try {
            String jobId = nvl(map.get("job_id"), "");
            if (jobId.isEmpty()) {
                return out("ERROR", "JOB_ID REQUIRED", null);
            }

            HashMap<String, String> q = new HashMap<String, String>();
            q.put("job_id", jobId);

            BatchJobCtrlVo job = selectJobOne(q);
            if (job == null) {
                return out("ERROR", "JOB NOT FOUND", null);
            }

            if (!"Y".equalsIgnoreCase(nvl(job.getRunning_yn(), "N"))) {
                return out("SKIP", "NOT RUNNING", null);
            }

            requestStop(jobId);

            Map<String, Object> meta = new LinkedHashMap<String, Object>();
            meta.put("job_id", jobId);
            meta.put("running_yn", nvl(job.getRunning_yn(), "N"));
            meta.put("stop_requested", "Y");
            return out("SUCCESS", "STOP REQUESTED", meta);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserTransactionException(this.getClass().getName() + ".stopJob() : ", e.getLocalizedMessage());
        }
    }

    private Map<String, Object> executeJob(String jobId, BatchJobCtrlVo job) throws Exception {

        if ("INTEREST_STOCK_MASTER_REFRESH".equals(jobId)) {
            HashMap<String, String> p = new HashMap<String, String>();
            p.put("limit", nvl(job.getLimit_cnt(), "200"));
            p.put("stale_days", nvl(job.getStale_days(), "7"));
            p.put("stock_country_code", nvl(job.getCountry_code(), "KR"));
            return refreshInterestStockMaster(jobId, p);
        }

        if ("KOSPI200_INTEREST_SYNC".equals(jobId)) {
            // KIS에서 코스피200 지수 원본 구성종목을 직접 제공하는 API가 없어서,
            // 코스피200 추종 ETF(기본: 069500 KODEX 200)의 구성종목 API를 사용한다.
            // TB_S_BATCH_JOB_CTRL.LIMIT_CNT 컬럼을 ETF코드(6자리)로 재사용한다.
            String etfCode = nvl(job.getLimit_cnt(), "069500");
            return syncKospi200ByEtf(jobId, etfCode);
        }
        
        if ("STOCK_30MIN_ANALYSIS_REFRESH".equals(jobId)) {
            HashMap<String, String> p = new HashMap<String, String>();
            p.put("limit", nvl(job.getLimit_cnt(), "200"));
            p.put("stock_country_code", nvl(job.getCountry_code(), "KR"));
            if ("0".equals(nvl(job.getStale_days(), ""))) p.put("force_yn", "Y");
            return refreshStock30MinAnalysis(jobId, p);
        }

        if ("STOCK_SMA_ANALYSIS_REFRESH".equals(jobId)) {
            HashMap<String, String> p = new HashMap<String, String>();
            p.put("limit", nvl(job.getLimit_cnt(), "200"));
            p.put("stock_country_code", nvl(job.getCountry_code(), "KR"));
            if ("0".equals(nvl(job.getStale_days(), ""))) p.put("force_yn", "Y");
            return refreshStockSmaAnalysis(jobId, p);
        }

        Map<String, Object> none = new LinkedHashMap<String, Object>();
        none.put("updatedCount", 0);
        none.put("skippedCount", 0);
        none.put("failedCount", 0);
        return none;
    }

    /**
     * KOSPI200 구성종목 동기화 (KIS - ETF 구성종목시세 기반)
     *
     * KIS OpenAPI에는 'KOSPI200 지수 원본 구성종목'을 직접 내려주는 엔드포인트가 없으므로,
     * 코스피200 추종 ETF(기본: 069500 KODEX 200)의 구성종목을 이용하여 구성종목을 관리한다.
     *
     * 동작
     *  1) TB_S_INTEREST_STOCK 중 STOCK_ALIASES에 'KOSPI200' 태그가 있는 종목을 USE_DIV='N' 처리
     *  2) ETF 구성종목을 TB_S_INTEREST_STOCK에 MERGE (존재하면 활성화/갱신, 없으면 신규 입력)
     *
     * 주의
     *  - ETF 구성종목은 지수 원본과 완전히 동일하다고 보장되지는 않음(리밸런싱/현금비중 등)
     *  - 상세 기업정보(업종/시장/타입 등)는 이후 INTEREST_STOCK_MASTER_REFRESH에서 보강
     */
    private Map<String, Object> syncKospi200ByEtf(String jobId, String etfCode) throws Exception {

        ensureNotStopped(jobId);
        String code = nvl(etfCode, "069500").trim();
        if (code.length() != 6) {
            code = "069500";
        }
        
        KisClient client = KisClientFactory.getClient();

        EtfetnInquireComponentStockPriceApi api = new EtfetnInquireComponentStockPriceApi(code);
        EtfetnInquireComponentStockPriceResult result = client.execute(api);

        if (result == null || !"0".equals(result.getRtCd()) || result.getOutput2() == null) {
            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("updatedCount", 0);
            r.put("skippedCount", 0);
            r.put("failedCount", 1);
            r.put("reason", "KIS ETF component stock price failed");
            r.put("etfCode", code);
            return r;
        }

        EtfetnInquireComponentStockPriceResult.Output2[] arr = result.getOutput2();
        Set<String> uniq = new LinkedHashSet<String>();
        for (EtfetnInquireComponentStockPriceResult.Output2 o : arr) {

            ensureNotStopped(jobId);
            if (o == null) continue;
            String sc = nvl(o.getStckShrnIscd(), "").trim();
            if (sc.length() != 6) continue;
            uniq.add(sc);
        }

        int totalTarget = uniq.size();
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        // ORA-38104 방지: 과거 데이터 중 국가코드 NULL을 KR로 정규화
        getSqlSession().update(NS + "normalizeInterestStockCountryCodeKr", new HashMap<String, String>());

        // 1) 기존 KOSPI200 태그 종목 비활성화
        HashMap<String, String> off = new HashMap<String, String>();
        off.put("tag", "KOSPI200");
        off.put("stock_country_code", "KR");
        getSqlSession().update(NS + "disableTaggedInterestStock", off);

        // 2) 구성종목 MERGE
        for (EtfetnInquireComponentStockPriceResult.Output2 o : arr) {

            ensureNotStopped(jobId);
            if (o == null) continue;

            String stockCode = nvl(o.getStckShrnIscd(), "").trim();
            if (stockCode.length() != 6) {
                skipped++;
                continue;
            }

            if (!uniq.contains(stockCode)) {
                skipped++;
                continue;
            }

            try {
                String ko = nvl(o.getHtsKorIsnm(), "").trim();
                if (ko.isEmpty()) ko = stockCode;

                HashMap<String, String> m = new HashMap<String, String>();
                m.put("stock_code", stockCode);
                m.put("stock_ko_name", ko);
                m.put("stock_country_code", "KR");
                m.put("stock_currency", "KRW");
                m.put("stock_timezone", "Asia/Seoul");

                // aliases는 간단하게 생성(상세는 master refresh에서 보강)
                m.put("stock_aliases", stockCode + "|" + ko + "|KOSPI200|ETF:" + code);
                m.put("tag", "KOSPI200");

                int rows = getSqlSession().update(NS + "mergeKospi200InterestStock", m);
                if (rows > 0) {
                    updated++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed++;
            }
        }

        Map<String, Object> r = refreshResult(totalTarget, updated, skipped, failed);
        r.put("etfCode", code);
        r.put("note", "KIS ETF component holdings used (may differ from KOSPI200 index constituents)");
        return r;
    }

    /**
     * 관심종목 마스터 갱신(통합 SQL 사용)
     */
    /**
     * 관심종목 마스터 갱신
     *  - TB_S_INTEREST_STOCK(등록된 종목)만 대상으로 수행 (신규상장 자동 추가 로직 없음)
     *  - KIS 주식기본조회(search-stock-info) 결과로 기업정보 최신화
     *  - 상장폐지(폐지일자 존재)면 USE_DIV = 'N'
     */
    private Map<String, Object> refreshInterestStockMaster(String jobId, HashMap<String, String> map) throws Exception {

        int limit = parseInt(map.get("limit"), 200);
        int staleDays = parseInt(map.get("stale_days"), 7);
        String country = nvl(map.get("stock_country_code"), "KR");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("limit", String.valueOf(limit));
        q.put("stale_days", String.valueOf(staleDays));
        q.put("stock_country_code", country);

        List<HashMap<String, Object>> targets = getSqlSession().selectList(NS + "selectRefreshTargets", q);

        int totalTarget = (targets == null ? 0 : targets.size());
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        int delisted = 0;

        if (targets == null || targets.isEmpty()) {
            Map<String, Object> r = refreshResult(totalTarget, updated, skipped, failed);
            r.put("delistedCount", delisted);
            return r;
        }

        KisClient client = KisClientFactory.getClient();

        for (HashMap<String, Object> base : targets) {

            ensureNotStopped(jobId);

            String stockId = "";
            String stockCode = "";

            try {
                stockId = toStr(base.get("STOCK_ID"));
                stockCode = toStr(base.get("STOCK_CODE"));

                if (stockId.isEmpty() || stockCode.isEmpty()) {
                    skipped++;
                    continue;
                }
			
			// 국내/해외 분기
			if (!"KR".equalsIgnoreCase(country)) {
			
			    String baseMarket = toStr(base.get("STOCK_MARKET"));
			    String prdtTypeCd = resolveOverseasPrdtTypeCd(baseMarket, country);
			
			    ensureNotStopped(jobId);
			    SearchOverseasStockInfoResult.Output ov = fetchKisOverseasStockInfo(client, prdtTypeCd, stockCode);
			    if (ov == null) {
			        // 해외 KIS 실패 시에도 MODIFY_DATE를 갱신하지 않음 (다음 배치에서 재시도)
			        failed++;
			        continue;
			    }
			
			    String en = nvl(ov.getPrdtEngName(), "");
			    String ko = nvl(ov.getOvrsItemName(), "");
			    if (ko.isEmpty()) ko = en;
			
			    String stockSector = nvl(ov.getPrdtClsfName(), "");
			    if (stockSector.isEmpty()) stockSector = nvl(ov.getPrdtClsfCd(), "");
			
			    String stockMarket = nvl(ov.getTrMketName(), "");
			    String marketSection = nvl(ov.getTrMketCd(), "");
			    if (marketSection.isEmpty()) marketSection = nvl(ov.getOvrsExcgCd(), "");
			
			    String stockType = nvl(ov.getOvrsStckDvsnCd(), "");
			    String stockTypeSpecs = nvl(ov.getPrdtClsfCd(), "");
			
			    String currency = nvl(ov.getTrCrcyCd(), "");
			    if (currency.isEmpty()) currency = nvl(ov.getCrcyName(), "");
			
			    String timezone = resolveTimezoneByCountry(country, stockMarket);
			
			    boolean isDelisted = "Y".equalsIgnoreCase(nvl(ov.getLstgAbolItemYn(), ""))
			            || !nvl(ov.getLstgAbolDt(), "").isEmpty()
			            || "N".equalsIgnoreCase(nvl(ov.getLstgYn(), ""));
			
			    String useDiv = isDelisted ? "N" : "Y";
			
			    String aliases = buildOverseasAliases(stockCode, ov, ko, en);
			
			    HashMap<String, String> upd = new HashMap<String, String>();
			    upd.put("stock_id", stockId);
			    upd.put("stock_ko_name", ko);
			    upd.put("stock_en_name", en);
			    upd.put("stock_sector", stockSector);
			    upd.put("stock_market", stockMarket);
			    upd.put("market_section", marketSection);
			    upd.put("stock_country_code", country);
			    upd.put("stock_timezone", timezone);
			    upd.put("stock_type", stockType);
			    upd.put("stock_type_specs", stockTypeSpecs);
			    upd.put("stock_currency", currency);
			    upd.put("stock_aliases", aliases);
			    upd.put("use_div", useDiv);
			
			    int rows = getSqlSession().update(NS + "updateInterestStockMeta", upd);
			    if (rows > 0) {
			        updated++;
			        if (isDelisted) delisted++;
			    } else {
			        failed++;
			    }
			
			    continue;
			}
			
			ensureNotStopped(jobId);
                SearchStockInfoResult.Output out = fetchKisStockInfo(client, stockCode);
                if (out == null) {
                    // ✅ KIS 실패(네트워크/일시 오류 등) 시 MODIFY_DATE를 갱신하면 재시도가 막히므로 touch 하지 않음
                    failed++;
                    continue;
                }

                String ko = nvl(out.getPrdtAbrvName(), "");
                if (ko.isEmpty()) ko = nvl(out.getPrdtName(), "");

                String en = nvl(out.getPrdtEngAbrvName(), "");
                if (en.isEmpty()) en = nvl(out.getPrdtEngName(), "");

                String marketSection = nvl(out.getExcgDvsnCd(), "");
                String stockMarket = nvl(out.getMketIdCd(), "");
                String stockSector = nvl(out.getStdIdstClsfCd(), "");
                String stockType = nvl(out.getPrdtTypeCd(), "");
                String stockTypeSpecs = nvl(out.getSctyGrpIdCd(), "");

                String abol1 = nvl(out.getLstgAbolDt(), "");
                String abol2 = nvl(out.getSctsMketLstgAbolDt(), "");
                String abol3 = nvl(out.getKosdaqMketLstgAbolDt(), "");
                String abol4 = nvl(out.getDpsiErlmCnclDt(), "");

                boolean isDelisted = (!abol1.isEmpty() || !abol2.isEmpty() || !abol3.isEmpty() || !abol4.isEmpty());
                String useDiv = isDelisted ? "N" : "Y";

                String currency = "KRW";
                String timezone = "Asia/Seoul";

                String aliases = buildAliases(stockCode, out, ko, en);

                HashMap<String, String> upd = new HashMap<String, String>();
                upd.put("stock_id", stockId);
                upd.put("stock_ko_name", ko);
                upd.put("stock_en_name", en);
                upd.put("stock_sector", stockSector);
                upd.put("stock_market", stockMarket);
                upd.put("market_section", marketSection);
                upd.put("stock_country_code", country);
                upd.put("stock_timezone", timezone);
                upd.put("stock_type", stockType);
                upd.put("stock_type_specs", stockTypeSpecs);
                upd.put("stock_currency", currency);
                upd.put("stock_aliases", aliases);
                upd.put("use_div", useDiv);

                int rows = getSqlSession().update(NS + "updateInterestStockMeta", upd);
                if (rows > 0) {
                    updated++;
                    if (isDelisted) delisted++;
                } else {
                    failed++;
                }

            } catch (Exception e) {
                e.printStackTrace();
                // 실패 시 MODIFY_DATE를 변경하지 않고, 다음 주기에 다시 시도되도록 둔다.
                failed++;
            }
        }

        Map<String, Object> r = refreshResult(totalTarget, updated, skipped, failed);
        r.put("delistedCount", delisted);
        return r;
    }

    // =========================================================
    // Stock Analysis Jobs (moved from BatchStockController)
    // =========================================================

    // Signal event logger (optional). Wired via Spring XML.
    private com.scheduler.finance.dao.SignalEventDao signalEventDao;

    public com.scheduler.finance.dao.SignalEventDao getSignalEventDao() {
        return signalEventDao;
    }

    public void setSignalEventDao(com.scheduler.finance.dao.SignalEventDao signalEventDao) {
        this.signalEventDao = signalEventDao;
    }

    private void tryLogSignalEvent(String jobId, String stockCode, String timeframe, String signalType,
            String signalGrade, String scoreDelta, String eventTime, String priceRef, String jsonParams, String message) {
        try {
            if (signalEventDao == null) return;
            java.util.HashMap<String, String> m = new java.util.HashMap<String, String>();
            m.put("stock_code", stockCode);
            m.put("country_code", "KR");
            m.put("timeframe", timeframe);
            m.put("signal_type", signalType);
            m.put("signal_grade", signalGrade);
            m.put("score_delta", scoreDelta);
            m.put("event_time", eventTime);
            m.put("price_ref", priceRef);
            m.put("json_params", jsonParams);
            m.put("message", message);
            m.put("source_job_id", jobId);
            signalEventDao.insertSignalEvent(m);
        } catch (Exception e) {
            // signal logging must never break the batch
        }
    }

    private Map<String, Object> refreshStockSmaAnalysis(String jobId, HashMap<String, String> map) throws Exception {

        int limit = parseInt(map.get("limit"), 200);
        String country = nvl(map.get("stock_country_code"), "KR");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("stock_country_code", country);
        q.put("analysisTable", "TB_S_STOCK_ANALYSIS");
        q.put("bucket_mode", "DAY");
        q.put("limit", String.valueOf(limit));
        applyAnalysisPriceRange(q, country, true);

        @SuppressWarnings("unchecked")
        List<StockInfoVo> mktTargets = (List<StockInfoVo>) (List<?>) getSqlSession().selectList(NS_BATCH + "batchMarketStockList", q);
        @SuppressWarnings("unchecked")
        List<StockInfoVo> grpTargets = (List<StockInfoVo>) (List<?>) getSqlSession().selectList(NS_BATCH + "batchGroupStockList", q);
        @SuppressWarnings("unchecked")
        List<StockInfoVo> allTargets = (List<StockInfoVo>) (List<?>) getSqlSession().selectList(NS_BATCH + "batchGroupOrderStockList", q);

        java.util.LinkedHashMap<String, StockInfoVo> uniq = new java.util.LinkedHashMap<String, StockInfoVo>();
        java.util.List<StockInfoVo> merged = new java.util.ArrayList<StockInfoVo>();
        if (mktTargets != null) merged.addAll(mktTargets);
        if (grpTargets != null) merged.addAll(grpTargets);
        if (allTargets != null) merged.addAll(allTargets);

        List<StockInfoVo> targets = new java.util.ArrayList<StockInfoVo>();
        for (StockInfoVo it : merged) {
            if (it == null) continue;
            String code = nvl(it.getStock_code(), "");
            if (code.isEmpty() || uniq.containsKey(code)) continue;
            uniq.put(code, it);
            targets.add(it);
        }

        int totalTarget = (targets == null ? 0 : targets.size());
        int updated = 0;
        int skipped = 0;
        int failed = 0;

        if (targets == null || targets.isEmpty()) {
            return refreshResult(totalTarget, updated, skipped, failed);
        }

        StockAnalysisUtil util = new StockAnalysisUtil();

        for (StockInfoVo s : targets) {
            ensureNotStopped(jobId);

            String stockCode = (s == null ? "" : nvl(s.getStock_code(), ""));
            if (stockCode.isEmpty()) {
                skipped++;
                continue;
            }

            try {
                // 240SMA를 위해 최소 300개 이상 확보 (휴일/공휴일 감안)
                List<StockDataVo> data = fetchDailyCandles(jobId, stockCode, 320);
                int minRequired = 60;
                String scUpper = nvl(stockCode, "").toUpperCase();
                boolean isMarketRegimeIndex = ("0001".equals(scUpper) || "2001".equals(scUpper) || "1001".equals(scUpper)
                        || scUpper.startsWith(".") || "SPX".equals(scUpper) || "COMP".equals(scUpper) || "NDX".equals(scUpper));
                if (isMarketRegimeIndex) {
                    // 해외지수 chart API는 구간 제한이 있을 수 있어 더 낮은 최소 데이터로도 분석 저장 허용
                    minRequired = 20;
                }
                if (data == null || data.size() < minRequired) {
                    skipped++;
                    continue;
                }

                StockDataVo last = data.get(data.size() - 1);
                double close = last.getClose();
                double volume = last.getVolume();

                HashMap<String, String> analysis = StockAnalysisUtil.getAnalysisSMADataVo(data, close);
                HashMap<String, String> avgVol = StockAnalysisUtil.getAverageVolumeVo(data, volume);
                HashMap<String, String> macd = StockAnalysisUtil.getAnalysisMACDDataVo(data);
                HashMap<String, String> lines = MovingAveragesLineNumber.getAveragesLineNumber(data);
                HashMap<String, String> month = StockAnalysisUtil.monthlyStockData(data);

                analysis.putAll(avgVol);
                analysis.putAll(macd);
                analysis.putAll(lines);
                analysis.putAll(month);

                analysis.put("analysisTable", "TB_S_STOCK_ANALYSIS");
                analysis.put("stock_code", stockCode);
                analysis.put("stock_close", String.valueOf(close));
                ensureAnalysisFields(analysis);

                getSqlSession().insert(NS_BATCH + "mergeInterastStockAnalsis", analysis);
                updated++;

                // --- Signal events (daily) ---
                try {
                    String eventTime = java.time.Instant.ofEpochMilli(last.getDate())
                            .atZone(KOREA_ZONE)
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    double sma60 = parseDoubleSafe(analysis.get("stock_sma60_price"));
                    double sma20 = parseDoubleSafe(analysis.get("stock_sma20_price"));

                    boolean trendOk = (close > sma60) && (sma20 > sma60);
                    if (trendOk) {
                        String msg = "일봉 상승추세 OK (종가>MA60, MA20>MA60)";
                        String json = "{\"close\":" + close + ",\"ma20\":" + sma20 + ",\"ma60\":" + sma60 + "}";
                        tryLogSignalEvent(jobId, stockCode, "DAY", "DAILY_TREND_OK", "GOOD", "0", eventTime,
                                String.valueOf(close), json, msg);
                    }

                    // 핵심 구성요소 이벤트(추천 상위 종목에 사유가 비지 않도록)
                    try {
                        String smaLines = nvl(analysis.get("stock_sma_lines"), "");
                        if (!smaLines.isEmpty()) {
                            String grade = (smaLines.indexOf("5,20,60") == 0 ? "GOOD" : "INFO");
                            tryLogSignalEvent(jobId, stockCode, "DAY", "DAILY_SMA_LINES", grade, "0", eventTime,
                                    String.valueOf(close), "{\"lines\":\"" + smaLines + "\"}", "일봉 정배열: " + smaLines);
                        }
                    } catch (Exception ignore2) {}

                    try {
                        String macdSig = nvl(analysis.get("stock_macd_signal"), "");
                        if (!macdSig.isEmpty()) {
                            String grade = ("BUY".equalsIgnoreCase(macdSig) ? "GOOD" : ("SELL".equalsIgnoreCase(macdSig) ? "WARN" : "INFO"));
                            tryLogSignalEvent(jobId, stockCode, "DAY", "DAILY_MACD_" + macdSig.toUpperCase(), grade, "0", eventTime,
                                    String.valueOf(close), "", "일봉 MACD: " + macdSig);
                        }
                    } catch (Exception ignore2) {}

                    try {
                        String volSig = nvl(analysis.get("stock_av_volumesignal"), "");
                        if (!volSig.isEmpty()) {
                            String grade = ("UP".equalsIgnoreCase(volSig) ? "GOOD" : "INFO");
                            tryLogSignalEvent(jobId, stockCode, "DAY", "DAILY_VOLUME_" + volSig.toUpperCase(), grade, "0", eventTime,
                                    String.valueOf(close), "", "일봉 거래량: " + volSig);
                        }
                    } catch (Exception ignore2) {}

                    // MA 하향 이탈(일봉): 5/20선이 60선을 깨고 내려갔는지
                    try {
                        if (hasCrossDownOnLastBar(data, 5, 60)) {
                            String json = buildCrossJson(data, 5, 60);
                            tryLogSignalEvent(jobId, stockCode, "DAY", "DAY_CROSSDOWN_5_60", "WARN", "0", eventTime,
                                    String.valueOf(close), json, "일봉 5일선이 60일선을 하향 이탈");
                        }
                        if (hasCrossDownOnLastBar(data, 20, 60)) {
                            String json = buildCrossJson(data, 20, 60);
                            tryLogSignalEvent(jobId, stockCode, "DAY", "DAY_CROSSDOWN_20_60", "WARN", "0", eventTime,
                                    String.valueOf(close), json, "일봉 20일선이 60일선을 하향 이탈");
                        }
                    } catch (Exception ignore2) {}

                    // BullishMomentum (일봉)
                    // - 완전 정배열 + (장대양봉 or 연속양봉)
                    java.util.List<com.scheduler.finance.vo.BullishMomentumSignal> bm =
                            com.scheduler.finance.module.BullishMomentumDetector.detectBullishMomentum(
                                    data, stockCode, null, 3, 2.0);
                    if (bm != null && !bm.isEmpty()) {
                        for (com.scheduler.finance.vo.BullishMomentumSignal s1 : bm) {
                            if (s1 == null) continue;
                            if (s1.getSignalTime() != last.getDate()) continue; // 최근 봉만 저장(노이즈 방지)

                            String st = (s1.getSignalType() == null ? "" : s1.getSignalType().trim());
                            String sigType = "BULLISH_MOMENTUM_" + (st.isEmpty() ? "UNKNOWN" : st);

                            String grade = "GOOD";
                            String msg = "상승 모멘텀: " + ("LONG_BULL_BODY".equals(st) ? "장대양봉" : ("CONSECUTIVE_BULL".equals(st) ? ("연속양봉 " + s1.getConsecutiveDays() + "일") : st));

                            String json = "{\"close\":" + s1.getClosePrice()
                                    + ",\"open\":" + s1.getOpen()
                                    + ",\"bodyRatio\":" + s1.getBodyRatio()
                                    + ",\"consecutiveDays\":" + s1.getConsecutiveDays()
                                    + ",\"ma5\":" + s1.getMa5()
                                    + ",\"ma20\":" + s1.getMa20()
                                    + ",\"ma60\":" + s1.getMa60()
                                    + ",\"ma120\":" + s1.getMa120()
                                    + ",\"ma240\":" + s1.getMa240()
                                    + "}";

                            tryLogSignalEvent(jobId, stockCode, "DAY", sigType, grade, "0", eventTime,
                                    String.valueOf(s1.getClosePrice()), json, msg);
                        }
                    }

                    // WF-2-2a 모듈 연동: PositionRuleEngine 결과를 배치에서 SIGNAL_EVENT로 저장
                    try {
                        java.util.List<com.scheduler.finance.vo.StockDataVo> m30Data = fetch30MinCandles(jobId, stockCode, 320);
                        com.scheduler.finance.module.PositionRuleEngine.Result pe =
                                com.scheduler.finance.module.PositionRuleEngine.evaluatePreEntry(data, m30Data);

                        com.scheduler.finance.vo.PositionPlanVo plan = (pe == null ? null : pe.getPlan());
                        java.util.List<com.scheduler.finance.vo.PositionSignalVo> sigs = (pe == null ? null : pe.getSignals());

                        if (plan != null) {
                            String planState = nvl(plan.getState(), "WATCH").toUpperCase();
                            String planJson = "{\"state\":\"" + planState
                                    + "\",\"entry\":" + plan.getSuggestedEntryPrice()
                                    + ",\"stop\":" + plan.getInitialStopPrice()
                                    + ",\"tp1\":" + plan.getTakeProfit1Price()
                                    + ",\"riskR\":" + plan.getRiskR()
                                    + ",\"partialTpRatio\":" + plan.getPartialTakeProfitRatio()
                                    + "}";
                            String planMsg = "포지션 플랜: 상태 " + planState
                                    + " · 진입 " + plan.getSuggestedEntryPrice()
                                    + " · 손절 " + plan.getInitialStopPrice()
                                    + " · 1차익절 " + plan.getTakeProfit1Price();
                            tryLogSignalEvent(jobId, stockCode, "DAY", "PLAN_STATE_" + planState, "INFO", "0", eventTime,
                                    String.valueOf(close), planJson, planMsg);
                        }

                        if (sigs != null && !sigs.isEmpty()) {
                            for (com.scheduler.finance.vo.PositionSignalVo ps : sigs) {
                                if (ps == null) continue;
                                String st = nvl(ps.getType(), "INFO").toUpperCase();
                                String msg = nvl(ps.getMessage(), "");
                                long ts = ps.getTime() > 0 ? ps.getTime() : last.getDate();
                                String et = java.time.Instant.ofEpochMilli(ts)
                                        .atZone(KOREA_ZONE)
                                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                String grade = "INFO";
                                if (st.indexOf("WARNING") >= 0 || st.indexOf("RISK") >= 0 || st.indexOf("EXIT") >= 0) grade = "WARN";
                                else if (st.indexOf("OK") >= 0 || st.indexOf("REBOUND") >= 0 || st.indexOf("PLAN") >= 0) grade = "GOOD";

                                String json = "{\"signalType\":\"" + st + "\",\"price\":" + ps.getPrice() + "}";
                                tryLogSignalEvent(jobId, stockCode, "DAY", "PLAN_SIG_" + st, grade, "0", et,
                                        String.valueOf(ps.getPrice()), json, msg);
                            }
                        }
                    } catch (Exception ignore2) {
                    }

                } catch (Exception ignore) {
                }

            } catch (Exception e) {
                e.printStackTrace();
                // 종목 조회 실패가 지속되는 경우 운영자가 USE_DIV를 내릴 수 있도록 그대로 둔다.
                failed++;
            }
        }

        return refreshResult(totalTarget, updated, skipped, failed);
    }

    private Map<String, Object> refreshStock30MinAnalysis(String jobId, HashMap<String, String> map) throws Exception {

        int limit = parseInt(map.get("limit"), 200);
        String country = nvl(map.get("stock_country_code"), "KR");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("stock_country_code", country);
        q.put("analysisTable", "TB_S_STOCK_30MINANALYSIS");
        q.put("bucket_mode", "30MIN");
        q.put("limit", String.valueOf(limit));
        applyAnalysisPriceRange(q, country, false);

        @SuppressWarnings("unchecked")
        List<StockInfoVo> mktTargets = (List<StockInfoVo>) (List<?>) getSqlSession().selectList(NS_BATCH + "batchMarketStockList", q);
        @SuppressWarnings("unchecked")
        List<StockInfoVo> grpTargets = (List<StockInfoVo>) (List<?>) getSqlSession().selectList(NS_BATCH + "batchGroupStockList", q);
        @SuppressWarnings("unchecked")
        List<StockInfoVo> allTargets = (List<StockInfoVo>) (List<?>) getSqlSession().selectList(NS_BATCH + "batchGroupOrderStockList", q);

        java.util.LinkedHashMap<String, StockInfoVo> uniq = new java.util.LinkedHashMap<String, StockInfoVo>();
        java.util.List<StockInfoVo> merged = new java.util.ArrayList<StockInfoVo>();
        if (mktTargets != null) merged.addAll(mktTargets);
        if (grpTargets != null) merged.addAll(grpTargets);
        if (allTargets != null) merged.addAll(allTargets);

        List<StockInfoVo> targets = new java.util.ArrayList<StockInfoVo>();
        for (StockInfoVo it : merged) {
            if (it == null) continue;
            String code = nvl(it.getStock_code(), "");
            if (code.isEmpty() || uniq.containsKey(code)) continue;
            uniq.put(code, it);
            targets.add(it);
        }

        int totalTarget = (targets == null ? 0 : targets.size());
        int updated = 0;
        int skipped = 0;
        int failed = 0;

        if (targets == null || targets.isEmpty()) {
            return refreshResult(totalTarget, updated, skipped, failed);
        }

        StockAnalysisUtil util = new StockAnalysisUtil();

        for (StockInfoVo s : targets) {
            ensureNotStopped(jobId);

            String stockCode = (s == null ? "" : nvl(s.getStock_code(), ""));
            if (stockCode.isEmpty()) {
                skipped++;
                continue;
            }

            try {
                // 30분봉은 최근 데이터만으로도 SMA5/20/60/120/240 계산이 가능하도록 300개 이상 확보
                List<StockDataVo> data = fetch30MinCandles(jobId, stockCode, 320);
                if (data == null || data.size() < 60) {
                    skipped++;
                    continue;
                }

                StockDataVo last = data.get(data.size() - 1);
                double close = last.getClose();
                double volume = last.getVolume();

                HashMap<String, String> analysis = util.getAnalysisSMADataVo(data, close);
                HashMap<String, String> avgVol = util.getAverageVolumeVo(data, volume);
                HashMap<String, String> macd = util.getAnalysisMACDDataVo(data);
                HashMap<String, String> lines = MovingAveragesLineNumber.getAveragesLineNumber(data);

                analysis.putAll(avgVol);
                analysis.putAll(macd);
                analysis.putAll(lines);

                // 30분봉에서는 월봉 데이터는 저장하지 않음
                analysis.put("analysisTable", "TB_S_STOCK_30MINANALYSIS");
                analysis.put("stock_code", stockCode);
                analysis.put("stock_close", String.valueOf(close));
                ensureAnalysisFields(analysis);

                getSqlSession().insert(NS_BATCH + "mergeInterastStockAnalsis", analysis);
                updated++;

                // --- Signal events (30min) ---
                try {
                    String eventTime = java.time.Instant.ofEpochMilli(last.getDate())
                            .atZone(KOREA_ZONE)
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    double sma60 = parseDoubleSafe(analysis.get("stock_sma60_price"));
                    double sma20 = parseDoubleSafe(analysis.get("stock_sma20_price"));
                    // "상승추세 안에서의 눌림"을 아주 단순하게: 종가가 MA60 위 + MA20 근처(±1.2%)
                    if (close > sma60 && sma20 > 0) {
                        double distPct = (close - sma20) / sma20 * 100.0;
                        if (distPct >= -1.2 && distPct <= 1.2) {
                            String msg = "30분봉 눌림(근접) 후보 (종가≈MA20, 종가>MA60)";
                            String json = "{\"close\":" + close + ",\"ma20\":" + sma20 + ",\"ma60\":" + sma60 + ",\"distPct\":"
                                    + (Math.round(distPct * 100.0) / 100.0) + "}";
                            tryLogSignalEvent(jobId, stockCode, "30MIN", "PULLBACK_NEAR_MA20", "INFO", "0", eventTime,
                                    String.valueOf(close), json, msg);
                        }
                    }

                    // 30분봉 핵심 구성요소 이벤트
                    try {
                        String macdSig = nvl(analysis.get("stock_macd_signal"), "");
                        if (!macdSig.isEmpty()) {
                            String grade = ("BUY".equalsIgnoreCase(macdSig) ? "GOOD" : ("SELL".equalsIgnoreCase(macdSig) ? "WARN" : "INFO"));
                            tryLogSignalEvent(jobId, stockCode, "30MIN", "M30_MACD_" + macdSig.toUpperCase(), grade, "0", eventTime,
                                    String.valueOf(close), "", "30분 MACD: " + macdSig);
                        }
                    } catch (Exception ignore2) {}

                    try {
                        String volSig = nvl(analysis.get("stock_av_volumesignal"), "");
                        if (!volSig.isEmpty()) {
                            String grade = ("UP".equalsIgnoreCase(volSig) ? "GOOD" : "INFO");
                            tryLogSignalEvent(jobId, stockCode, "30MIN", "M30_VOLUME_" + volSig.toUpperCase(), grade, "0", eventTime,
                                    String.valueOf(close), "", "30분 거래량: " + volSig);
                        }
                    } catch (Exception ignore2) {}

                    // MA 하향 이탈(30분봉): 5/20선이 60선을 깨고 내려갔는지
                    try {
                        if (hasCrossDownOnLastBar(data, 5, 60)) {
                            String json = buildCrossJson(data, 5, 60);
                            tryLogSignalEvent(jobId, stockCode, "30MIN", "M30_CROSSDOWN_5_60", "WARN", "0", eventTime,
                                    String.valueOf(close), json, "30분봉 5선이 60선을 하향 이탈");
                        }
                        if (hasCrossDownOnLastBar(data, 20, 60)) {
                            String json = buildCrossJson(data, 20, 60);
                            tryLogSignalEvent(jobId, stockCode, "30MIN", "M30_CROSSDOWN_20_60", "WARN", "0", eventTime,
                                    String.valueOf(close), json, "30분봉 20선이 60선을 하향 이탈");
                        }
                    } catch (Exception ignore2) {}

                } catch (Exception ignore) {
                }

            } catch (Exception e) {
                e.printStackTrace();
                failed++;
            }
        }

        return refreshResult(totalTarget, updated, skipped, failed);
    }

    private void applyAnalysisPriceRange(HashMap<String, String> q, String country, boolean isDaily) {
        String c = nvl(country, "KR").toUpperCase();
        if ("KR".equals(c)) {
            q.put("start_price", "10000");
            q.put("end_price", isDaily ? "2000000" : "1000000");
            return;
        }
        q.put("start_price", "30");
        q.put("end_price", "2000");
    }

    private void ensureAnalysisFields(HashMap<String, String> m) {
        // MERGE SQL에서 요구하는 key 누락 방지
        String[] keys = new String[] {
                "stock_mon",
                "stock_sma5", "stock_sma20", "stock_sma60", "stock_sma120", "stock_sma240",
                "stock_sma5_price", "stock_sma20_price", "stock_sma60_price", "stock_sma120_price", "stock_sma240_price",
                "stock_sma5_mon_price",
                "stock_macd_time", "stock_macd_timedays", "stock_macd_signal", "stock_macd_streaksignal", "stock_macd_streakdays",
                "stock_sma_lines",
                "stock_av_volume", "stock_av_volumesignal"
        };

        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            if (!m.containsKey(k) || m.get(k) == null) {
                m.put(k, "");
            }
        }
    }

    private List<StockDataVo> fetchDailyCandles(String jobId, String stockCode, int minBars) throws Exception {

        String code = nvl(stockCode, "").trim();
        if (code.isEmpty()) {
            return new ArrayList<StockDataVo>();
        }

        LocalDate endDate = LocalDate.now(KOREA_ZONE);
        // 휴일/비영업일 감안하여 충분히 여유있게 조회
        LocalDate startDate = endDate.minusDays(Math.max(450, minBars * 2));

        KisClient client = KisClientFactory.getClient();
        Map<Long, StockDataVo> merged = new LinkedHashMap<Long, StockDataVo>();

        LocalDate currentEnd = endDate;
        int loop = 0;

        boolean isDomesticIndex = ("0001".equals(code) || "2001".equals(code) || "1001".equals(code));
        boolean isOverseasIndex = (code.startsWith(".") || "SPX".equalsIgnoreCase(code) || "COMP".equalsIgnoreCase(code)
                || "NDX".equalsIgnoreCase(code));

        // 해외지수 chart API는 장기간(수백일) 범위를 주면 빈 결과가 나오는 경우가 있어, 조회 기간을 제한한다.
        LocalDate effectiveStart = startDate;
        if (isOverseasIndex) {
            effectiveStart = endDate.minusDays(Math.max(60, Math.min(180, minBars * 2)));
        }

        String start = effectiveStart.format(DATE_YYYYMMDD);

        while (!currentEnd.isBefore(startDate)) {
            ensureNotStopped(jobId);

            String currentEndStr = currentEnd.format(DATE_YYYYMMDD);

            if (isDomesticIndex) {
                // 국내 지수: inquire-daily-indexchartprice (시장분류코드 fallback)
                InquireDailyIndexchartpriceResult result = null;
                String[] marketDivs = new String[] { "U", "UN", "J" };
                for (int i = 0; i < marketDivs.length; i++) {
                    InquireDailyIndexchartpriceApi api = new InquireDailyIndexchartpriceApi();
                    api.setFidCondMrktDivCode(marketDivs[i]);
                    api.setFidInputIscd(code);
                    api.setFidInputDate1(start);
                    api.setFidInputDate2(currentEndStr);
                    api.setFidPeriodDivCode("D");
                    result = client.execute(api);
                    if (result != null && "0".equals(result.getRtCd())) {
                        break;
                    }
                }

                if (result == null) {
                    throw new IllegalStateException("KIS inquire-daily-indexchartprice result is null");
                }
                if (!"0".equals(result.getRtCd())) {
                    // 지수는 일부 구간에서 실패가 날 수 있어, 이미 누적이 있으면 종료
                    if (!merged.isEmpty()) {
                        break;
                    }
                    throw new IllegalStateException("KIS inquire-daily-indexchartprice error rtCd=" + result.getRtCd()
                            + ", msgCd=" + result.getMsgCd() + ", msg1=" + result.getMsg1());
                }

                java.util.List<InquireDailyIndexchartpriceResult.Output2> outputs = result.getOutput2();
                if (outputs == null || outputs.isEmpty()) {
                    break;
                }

                LocalDate oldest = null;
                for (InquireDailyIndexchartpriceResult.Output2 o : outputs) {
                    if (o == null) continue;

                    String dateStr = pickIndexField(o, "stck_bsop_date", "bsop_date", "date", "xymd");
                    if (dateStr == null || dateStr.length() != 8) continue;

                    LocalDate d = LocalDate.parse(dateStr, DATE_YYYYMMDD);
                    if (d.isBefore(startDate) || d.isAfter(endDate)) continue;

                    long epoch = d.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();

                    double close = parseDoubleSafe(pickIndexField(o, "bstp_nmix_prpr", "close", "clpr", "prpr"));
                    double open = parseDoubleSafe(pickIndexField(o, "bstp_nmix_oprc", "open", "oprc"));
                    double high = parseDoubleSafe(pickIndexField(o, "bstp_nmix_hgpr", "high", "hgpr"));
                    double low = parseDoubleSafe(pickIndexField(o, "bstp_nmix_lwpr", "low", "lwpr"));
                    double vol = parseDoubleSafe(pickIndexField(o, "acml_vol", "tvol", "volume", "vol"));

                    if (Double.isNaN(close)) continue;
                    if (Double.isNaN(open)) open = close;
                    if (Double.isNaN(high)) high = close;
                    if (Double.isNaN(low)) low = close;

                    merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));

                    if (oldest == null || d.isBefore(oldest)) {
                        oldest = d;
                    }
                }

                if (oldest == null) break;
                if (!oldest.isAfter(startDate)) break;

                currentEnd = oldest.minusDays(1);
                loop++;
                if (loop > 60) break;

            } else if (isOverseasIndex) {
                // 해외 지수: inquire-overseas-daily-chartprice (marketDiv=N)
                InquireOverseasDailyChartPriceApi api = new InquireOverseasDailyChartPriceApi();
                api.setFidCondMrktDivCode("N");
                api.setFidInputIscd(code);
                api.setFidInputDate1(start);
                api.setFidInputDate2(currentEndStr);
                api.setFidPeriodDivCode("D");

                InquireOverseasDailyChartPriceResult result = client.execute(api);
                if (result == null) {
                    throw new IllegalStateException("KIS inquire-overseas-daily-chartprice result is null");
                }
                if (!"0".equals(result.getRtCd())) {
                    if (!merged.isEmpty()) {
                        break;
                    }
                    throw new IllegalStateException("KIS inquire-overseas-daily-chartprice error rtCd=" + result.getRtCd()
                            + ", msgCd=" + result.getMsgCd() + ", msg1=" + result.getMsg1());
                }

                java.util.List<InquireOverseasDailyChartPriceResult.Output2> outputs = result.getOutput2();
                if (outputs == null || outputs.isEmpty()) {
                    // date2가 휴일/비영업일인 경우 등: 이미 누적이 있으면 조금 뒤로 밀어서 재시도
                    if (!merged.isEmpty()) {
                        currentEnd = currentEnd.minusDays(7);
                        loop++;
                        if (loop > 60) break;
                        continue;
                    }
                    break;
                }

                LocalDate oldest = null;
                for (InquireOverseasDailyChartPriceResult.Output2 o : outputs) {
                    if (o == null) continue;

                    String dateStr = pickOverseasChartField(o, "stck_bsop_date", "bsop_date", "xymd", "XYMD", "date", "DATE");
                    dateStr = nvl(dateStr, "").trim();
                    if (dateStr.length() != 8) continue;

                    LocalDate d = LocalDate.parse(dateStr, DATE_YYYYMMDD);
                    if (d.isBefore(startDate) || d.isAfter(endDate)) continue;

                    long epoch = d.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();

                    double open = parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_oprc", "open", "OPEN"));
                    double high = parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_hgpr", "high", "HIGH"));
                    double low = parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_lwpr", "low", "LOW"));
                    double close = parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_prpr", "clos", "CLOS", "close", "CLOSE"));
                    double vol = parseDoubleSafe(pickOverseasChartField(o, "acml_vol", "tvol", "TVOL", "volume", "VOLUME"));

                    if (Double.isNaN(close)) continue;
                    if (Double.isNaN(open)) open = close;
                    if (Double.isNaN(high)) high = close;
                    if (Double.isNaN(low)) low = close;

                    merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));

                    if (oldest == null || d.isBefore(oldest)) {
                        oldest = d;
                    }
                }

                if (oldest == null) {
                    if (!merged.isEmpty()) {
                        currentEnd = currentEnd.minusDays(7);
                        loop++;
                        if (loop > 60) break;
                        continue;
                    }
                    break;
                }
                if (!oldest.isAfter(startDate)) break;

                currentEnd = oldest.minusDays(1);
                loop++;
                if (loop > 60) break;

            } else {
                // 일반(국내 종목)
                InquireDailyItemchartpriceApi api = new InquireDailyItemchartpriceApi();
                api.setFidInputIscd(code);
                api.setFidInputDate1(start);
                api.setFidInputDate2(currentEndStr);
                api.setFidPeriodDivCode("D");
                api.setFidOrgAdjPrc("0");

                InquireDailyItemchartpriceResult result = client.execute(api);
                if (result == null) {
                    throw new IllegalStateException("KIS inquire-daily-itemchartprice result is null");
                }
                if (!"0".equals(result.getRtCd())) {
                    throw new IllegalStateException("KIS inquire-daily-itemchartprice error rtCd=" + result.getRtCd()
                            + ", msgCd=" + result.getMsgCd() + ", msg1=" + result.getMsg1());
                }

                InquireDailyItemchartpriceResult.Output2[] outputs = result.getOutput2();
                if (outputs == null || outputs.length == 0) {
                    break;
                }

                LocalDate oldest = null;
                for (InquireDailyItemchartpriceResult.Output2 o : outputs) {
                    if (o == null) continue;
                    String dateStr = o.getStckBsopDate();
                    if (dateStr == null || dateStr.length() != 8) continue;
                    LocalDate d = LocalDate.parse(dateStr, DATE_YYYYMMDD);

                    if (d.isBefore(startDate) || d.isAfter(endDate)) continue;

                    long epoch = d.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();

                    double open = parseDoubleSafe(o.getStckOprc());
                    double high = parseDoubleSafe(o.getStckHgpr());
                    double low = parseDoubleSafe(o.getStckLwpr());
                    double close = parseDoubleSafe(o.getStckClpr());
                    double vol = parseDoubleSafe(o.getAcmlVol());

                    merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));

                    if (oldest == null || d.isBefore(oldest)) {
                        oldest = d;
                    }
                }

                if (oldest == null) break;
                if (!oldest.isAfter(startDate)) break;

                currentEnd = oldest.minusDays(1);
                loop++;
                if (loop > 50) break;
            }
        }

        List<StockDataVo> list = new ArrayList<StockDataVo>(merged.values());
        Collections.sort(list, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        // 최근 minBars만 남김 (SMA 계산에 필요한 구간 유지)
        if (list.size() > Math.max(minBars, 400)) {
            int from = list.size() - Math.max(minBars, 400);
            return new ArrayList<StockDataVo>(list.subList(from, list.size()));
        }

        return list;
    }

    private String pickIndexField(InquireDailyIndexchartpriceResult.Output2 o, String... keys) {
        if (o == null || keys == null) return "";
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            if (k == null) continue;
            Object v = o.getField(k);
            if (v == null) continue;
            String s = String.valueOf(v).trim();
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                return s;
            }
        }
        return "";
    }

    private String pickOverseasChartField(InquireOverseasDailyChartPriceResult.Output2 o, String... keys) {
        if (o == null || keys == null) return "";
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            if (k == null) continue;
            Object v = o.getField(k);
            if (v == null) continue;
            String s = String.valueOf(v).trim();
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                return s;
            }
        }
        return "";
    }

    private String firstNonEmpty(String... arr) {
        if (arr == null) return "";
        for (int i = 0; i < arr.length; i++) {
            String v = arr[i];
            if (v != null && !v.trim().isEmpty()) return v;
        }
        return "";
    }

    private List<StockDataVo> fetch30MinCandles(String jobId, String stockCode, int minBars) throws Exception {

        String code = nvl(stockCode, "").trim();
        if (code.isEmpty()) {
            return new ArrayList<StockDataVo>();
        }

        KisClient client = KisClientFactory.getClient();
        List<StockDataVo> all = new ArrayList<StockDataVo>();

        LocalDate today = LocalDate.now(KOREA_ZONE);
        int safetyDays = 20;

        for (int i = 0; i < safetyDays; i++) {
            ensureNotStopped(jobId);

            LocalDate d = today.minusDays(i);
            List<StockDataVo> oneDay = fetch30MinCandlesOneDay(jobId, client, code, d);
            if (oneDay != null && !oneDay.isEmpty()) {
                all.addAll(oneDay);
            }

            if (all.size() >= Math.max(minBars, 320)) {
                break;
            }
        }

        Collections.sort(all, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        // 최근 구간만 남김
        int keep = Math.max(minBars, 400);
        if (all.size() > keep) {
            return new ArrayList<StockDataVo>(all.subList(all.size() - keep, all.size()));
        }

        return all;
    }

    private List<StockDataVo> fetch30MinCandlesOneDay(String jobId, KisClient client, String stockCode, LocalDate date) throws Exception {

        ensureNotStopped(jobId);
        String dateStr = date.format(DATE_YYYYMMDD);

        InquireTimeDailychartpriceApi api = new InquireTimeDailychartpriceApi();
        api.setFidInputIscd(stockCode);
        api.setFidInputDate1(dateStr);
        api.setFidInputHour1("153000");
        api.setFidPwDataIncuYn("Y");
        api.setFidFakeTickIncuYn("N");

        InquireTimeDailychartpriceResult result = client.execute(api);
        if (result == null) {
            return new ArrayList<StockDataVo>();
        }
        if (!"0".equals(result.getRtCd())) {
            // 휴장일/비영업일 등은 빈 배열로 처리
            return new ArrayList<StockDataVo>();
        }

        InquireTimeDailychartpriceResult.Output2[] arr = result.getOutput2();
        if (arr == null || arr.length == 0) {
            return new ArrayList<StockDataVo>();
        }

        // minute bars -> 30min aggregate
        List<StockDataVo> mins = new ArrayList<StockDataVo>();

        for (InquireTimeDailychartpriceResult.Output2 o : arr) {
            if (o == null) continue;

            String bsop = nvl(o.getStckBsopDate(), "");
            String hhmmss = nvl(o.getStckCntgHour(), "");
            if (bsop.length() != 8 || hhmmss.length() < 4) continue;

            int hh = parseInt(hhmmss.substring(0, 2), -1);
            int mm = parseInt(hhmmss.substring(2, 4), -1);
            if (hh < 0 || mm < 0) continue;

            LocalDate d = LocalDate.parse(bsop, DATE_YYYYMMDD);
            LocalTime t = LocalTime.of(hh, mm, 0);
            long epoch = ZonedDateTime.of(d, t, KOREA_ZONE).toInstant().toEpochMilli();

            double open = parseDoubleSafe(o.getStckOprc());
            double high = parseDoubleSafe(o.getStckHgpr());
            double low = parseDoubleSafe(o.getStckLwpr());
            double close = parseDoubleSafe(o.getStckPrpr());
            double vol = parseDoubleSafe(o.getCntgVol());

            mins.add(new StockDataVo(epoch, open, high, low, close, vol));
        }

        if (mins.isEmpty()) {
            return new ArrayList<StockDataVo>();
        }

        Collections.sort(mins, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        class CandleAgg {
            long epoch;
            double open;
            double high;
            double low;
            double close;
            double volume;
        }

        Map<Long, CandleAgg> buckets = new LinkedHashMap<Long, CandleAgg>();

        for (StockDataVo m : mins) {

            ZonedDateTime z = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(m.getDate()), KOREA_ZONE);
            int totalMin = z.getHour() * 60 + z.getMinute();
            int bucketMin = (totalMin / 30) * 30;
            int bh = bucketMin / 60;
            int bm = bucketMin % 60;

            ZonedDateTime bucketTime = ZonedDateTime.of(z.toLocalDate(), LocalTime.of(bh, bm, 0), KOREA_ZONE);
            long bucketEpoch = bucketTime.toInstant().toEpochMilli();

            CandleAgg agg = buckets.get(bucketEpoch);
            if (agg == null) {
                agg = new CandleAgg();
                agg.epoch = bucketEpoch;
                agg.open = m.getOpen();
                agg.high = m.getHigh();
                agg.low = m.getLow();
                agg.close = m.getClose();
                agg.volume = m.getVolume();
                buckets.put(bucketEpoch, agg);
            } else {
                // open은 최초 유지
                agg.high = Math.max(agg.high, m.getHigh());
                agg.low = Math.min(agg.low, m.getLow());
                agg.close = m.getClose();
                agg.volume = agg.volume + m.getVolume();
            }
        }

        List<StockDataVo> out = new ArrayList<StockDataVo>();
        for (CandleAgg a : buckets.values()) {
            out.add(new StockDataVo(a.epoch, a.open, a.high, a.low, a.close, a.volume));
        }
        Collections.sort(out, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        return out;
    }

    private double parseDoubleSafe(String s) {
        try {
            if (s == null) return 0.0;
            String t = s.trim();
            if (t.isEmpty()) return 0.0;
            return Double.parseDouble(t);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean hasCrossDownOnLastBar(List<StockDataVo> candles, int shortPeriod, int longPeriod) {
        if (candles == null) return false;
        int last = candles.size() - 1;
        if (last < 1) return false;

        double prevShort = smaAt(candles, last - 1, shortPeriod);
        double prevLong = smaAt(candles, last - 1, longPeriod);
        double currShort = smaAt(candles, last, shortPeriod);
        double currLong = smaAt(candles, last, longPeriod);

        if (Double.isNaN(prevShort) || Double.isNaN(prevLong) || Double.isNaN(currShort) || Double.isNaN(currLong)) {
            return false;
        }

        // 직전에는 위(또는 동일), 현재는 아래 -> 하향 이탈
        return (prevShort >= prevLong) && (currShort < currLong);
    }

    private double smaAt(List<StockDataVo> candles, int endIndex, int period) {
        if (candles == null || period <= 0 || endIndex < 0) return Double.NaN;
        int start = endIndex - period + 1;
        if (start < 0) return Double.NaN;

        double sum = 0.0;
        for (int i = start; i <= endIndex; i++) {
            StockDataVo c = candles.get(i);
            if (c == null) return Double.NaN;
            sum += c.getClose();
        }
        return sum / period;
    }

    private String buildCrossJson(List<StockDataVo> candles, int shortPeriod, int longPeriod) {
        int last = (candles == null ? -1 : candles.size() - 1);
        if (last < 1) return "";

        double prevShort = smaAt(candles, last - 1, shortPeriod);
        double prevLong = smaAt(candles, last - 1, longPeriod);
        double currShort = smaAt(candles, last, shortPeriod);
        double currLong = smaAt(candles, last, longPeriod);

        return "{\"shortPeriod\":" + shortPeriod
                + ",\"longPeriod\":" + longPeriod
                + ",\"prevShort\":" + (Math.round(prevShort * 100.0) / 100.0)
                + ",\"prevLong\":" + (Math.round(prevLong * 100.0) / 100.0)
                + ",\"currShort\":" + (Math.round(currShort * 100.0) / 100.0)
                + ",\"currLong\":" + (Math.round(currLong * 100.0) / 100.0)
                + "}";
    }

    private String buildAliases(String stockCode, SearchStockInfoResult.Output out, String ko, String en) {
        StringBuilder sb = new StringBuilder();
        appendAlias(sb, stockCode);
        appendAlias(sb, nvl(out.getPdno(), ""));
        appendAlias(sb, nvl(out.getStdPdno(), ""));
        appendAlias(sb, nvl(out.getPrdtName(), ""));
        appendAlias(sb, nvl(out.getPrdtName120(), ""));
        appendAlias(sb, ko);
        appendAlias(sb, en);
        appendAlias(sb, nvl(out.getPrdtEngName(), ""));
        appendAlias(sb, nvl(out.getPrdtEngName120(), ""));
        return sb.toString();
    }

    private void appendAlias(StringBuilder sb, String v) {
        if (v == null) return;
        String t = v.trim();
        if (t.isEmpty()) return;

        // 중복 방지(간단히 contains로 처리)
        String s = sb.toString();
        if (s.contains(t)) return;

        if (sb.length() > 0) sb.append("|");
        sb.append(t);
    }
    
private String buildOverseasAliases(String stockCode, SearchOverseasStockInfoResult.Output out, String ko, String en) {
    StringBuilder sb = new StringBuilder();
    appendAlias(sb, stockCode);
    appendAlias(sb, nvl(out.getStdPdno(), ""));
    appendAlias(sb, nvl(out.getPrdtEngName(), ""));
    appendAlias(sb, nvl(out.getOvrsItemName(), ""));
    appendAlias(sb, ko);
    appendAlias(sb, en);
    appendAlias(sb, nvl(out.getNatnName(), ""));
    appendAlias(sb, nvl(out.getTrMketName(), ""));
    appendAlias(sb, nvl(out.getOvrsExcgName(), ""));
    return sb.toString();
}

private SearchOverseasStockInfoResult.Output fetchKisOverseasStockInfo(KisClient client, String prdtTypeCd, String pdno) throws Exception {

    SearchOverseasStockInfoApi api = new SearchOverseasStockInfoApi();
    api.setPrdtTypeCd(nvl(prdtTypeCd, "512"));
    api.setPdno(pdno);

    SearchOverseasStockInfoResult result = client.execute(api);
    if (result == null) return null;
    if (!"0".equals(result.getRtCd())) return null;
    if (result.getOutput() == null) return null;

    return result.getOutput();
}

/**
 * 해외주식 상품유형코드(PRDT_TYPE_CD) 매핑
 * - API 문서(해외주식 상품기본정보): 512/513/529/515/501/543/558/507/508/551/552
 */
private String resolveOverseasPrdtTypeCd(String stockMarket, String country) {

    String m = nvl(stockMarket, "").toUpperCase();
    String c = nvl(country, "").toUpperCase();

    // 시장 우선 매핑
    if (m.contains("NASDAQ") || m.contains("NAS")) return "512";
    if (m.contains("NYSE") || m.contains("NEW") || "NY".equals(m)) return "513";
    if (m.contains("AMEX")) return "529";

    if (m.contains("JAPAN") || m.contains("TOKYO") || m.contains("TSE")) return "515";

    if (m.contains("HONG") || m.contains("HK")) return "501";
    if (m.contains("HANOI")) return "507";
    if (m.contains("HOCHIMINH") || m.contains("HO CHI") || m.contains("HCM")) return "508";

    if (m.contains("SHANGHAI") || m.contains("SSE")) return "551";
    if (m.contains("SHENZHEN") || m.contains("SZSE")) return "552";

    // 국가 fallback
    if ("US".equals(c)) return "512";
    if ("JP".equals(c)) return "515";
    if ("HK".equals(c)) return "501";
    if ("VN".equals(c)) return "508";
    if ("CN".equals(c)) return "551";

    return "512";
}

private String resolveTimezoneByCountry(String country, String stockMarket) {

    String c = nvl(country, "").toUpperCase();

    if ("US".equals(c)) return "America/New_York";
    if ("JP".equals(c)) return "Asia/Tokyo";
    if ("HK".equals(c)) return "Asia/Hong_Kong";
    if ("CN".equals(c)) return "Asia/Shanghai";
    if ("VN".equals(c)) return "Asia/Ho_Chi_Minh";

    // 시장명으로 추가 보정
    String m = nvl(stockMarket, "").toUpperCase();
    if (m.contains("NASDAQ") || m.contains("NYSE") || m.contains("AMEX")) return "America/New_York";
    if (m.contains("TOKYO") || m.contains("JAPAN")) return "Asia/Tokyo";
    if (m.contains("HONG") || m.contains("HK")) return "Asia/Hong_Kong";
    if (m.contains("SHANGHAI") || m.contains("SHENZHEN")) return "Asia/Shanghai";

    return "UTC";
}
private SearchStockInfoResult.Output fetchKisStockInfo(KisClient client, String stockCode) throws Exception {

        SearchStockInfoApi api = new SearchStockInfoApi();
        api.setPdno(stockCode);
        api.setPrdtTypeCd("300");

        SearchStockInfoResult result = client.execute(api);
        if (result == null) return null;
        if (!"0".equals(result.getRtCd())) return null;
        if (result.getOutput() == null) return null;

        return result.getOutput();
    }

    private void touchModifyDate(String stockId) {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("stock_id", stockId);
        getSqlSession().update(NS + "touchModifyDate", m);
    }

    private void register(String jobId) {
        clearStopFlag(jobId);
        RUNNING_THREADS.put(jobId, Thread.currentThread());
    }

    private void unregister(String jobId) {
        RUNNING_THREADS.remove(jobId);
        STOP_FLAGS.remove(jobId);
    }

    private void clearStopFlag(String jobId) {
        AtomicBoolean f = STOP_FLAGS.get(jobId);
        if (f == null) {
            STOP_FLAGS.put(jobId, new AtomicBoolean(false));
        } else {
            f.set(false);
        }
        Thread t = RUNNING_THREADS.get(jobId);
        if (t != null && t.isInterrupted()) {
            // interrupt 상태가 남아있으면 이후 로직이 바로 중단되므로, 신규 실행 전에 제거하지는 않는다.
            // (Java Thread interrupt flag는 강제로 clear할 수 없고, InterruptedException을 통해 clear되는 방식임)
        }
    }

    private void requestStop(String jobId) {
        AtomicBoolean f = STOP_FLAGS.get(jobId);
        if (f == null) {
            f = new AtomicBoolean(true);
            STOP_FLAGS.put(jobId, f);
        } else {
            f.set(true);
        }

        Thread t = RUNNING_THREADS.get(jobId);
        if (t != null) {
            t.interrupt();
        }
    }

    private void ensureNotStopped(String jobId) {
        AtomicBoolean f = STOP_FLAGS.get(jobId);
        if (f != null && f.get()) {
            throw new StopRequestedException("STOPPED BY USER");
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new StopRequestedException("STOPPED BY USER");
        }
    }

    private Map<String, Object> refreshResult(int totalTarget, int updated, int skipped, int failed) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("totalTarget", totalTarget);
        out.put("updatedCount", updated);
        out.put("skippedCount", skipped);
        out.put("failedCount", failed);
        out.put("asOf", java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul")).toString().replace("T", " ").substring(0, 19));
        return out;
    }

    private boolean tryLock(String jobId) {
    	boolean updated = true;
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("job_id", jobId);
        HashMap<String, String> rt = new HashMap<String, String>();
        rt = getSqlSession().selectOne(NS + "selectTryLock", m);
        if(rt.get("RUNNING_YN").equals("N")) {
        	getSqlSession().update(NS + "tryLock", m);        	
        }
        return updated;
    }

    private void finish(String jobId, String resultCode, String resultMsg, int runCnt) {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("job_id", jobId);
        m.put("last_result_code", resultCode);
        m.put("last_result_msg", safeMsg(resultMsg));
        m.put("last_run_cnt", String.valueOf(runCnt));
        getSqlSession().update(NS + "finish", m);
    }

    private boolean isDue(String jobId) {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("job_id", jobId);
        Integer due = getSqlSession().selectOne(NS + "isDue", m);
        return due != null && due.intValue() == 1;
    }

    private Map<String, Object> out(String code, String msg, Object data) {
        Map<String, Object> r = new LinkedHashMap<String, Object>();
        r.put("code", code);
        r.put("message", msg);
        r.put("data", data);
        r.put("asOf", java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul")).toString().replace("T", " ").substring(0, 19));
        return r;
    }

    private String nvl(String s, String dft) {
        if (s == null) return dft;
        String t = s.trim();
        return t.isEmpty() ? dft : t;
    }

    private int toInt(Object v, int dft) {
        try {
            if (v == null) return dft;
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return dft;
        }
    }

    private int parseInt(String s, int dft) {
        try {
            if (s == null) return dft;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return dft;
        }
    }

    private String toStr(Object v) {
        if (v == null) return "";
        return String.valueOf(v).trim();
    }

    private String safeMsg(String s) {
        if (s == null) return "";
        if (s.length() > 1900) return s.substring(0, 1900);
        return s;
    }
}
