package com.scheduler.stock.recpickdynamic.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.dto.RecSignalDto;
import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.IndicatorRegistry;
import com.scheduler.stock.indicator.SqlFragment;
import com.scheduler.stock.recpickdynamic.dao.RecPickDynamicDao;
import com.scheduler.stock.recpickdynamic.vo.FilterRequestVo;
import com.scheduler.stock.recpickdynamic.vo.FilterRequestVo.IndicatorRequest;

/**
 * 동적 추천 종목 선별 서비스.
 * 2단계 필터링:
 *   ① SQL 단계 — 사전계산 지표 WHERE 결합
 *   ② Java 단계 — 실시간 계산 지표 evaluate
 */
public class RecPickDynamicService {

    private RecPickDynamicDao recPickDynamicDao;
    private IndicatorRegistry indicatorRegistry;

    public void setRecPickDynamicDao(RecPickDynamicDao dao) { this.recPickDynamicDao = dao; }
    public void setIndicatorRegistry(IndicatorRegistry r) { this.indicatorRegistry = r; }

    public IndicatorRegistry getIndicatorRegistry() { return indicatorRegistry; }

    /**
     * 메인 검색 — 필터 요청을 받아 매칭 종목 리스트 반환.
     */
    public Map<String, Object> selectStockList(FilterRequestVo req) throws Exception {

        // ── 0) 기준일 확정
        HashMap<String, Object> dtMap = new HashMap<String, Object>();
        dtMap.put("mktGroup", req.getMktGroup());
        String baseDt = req.getBaseDt();
        if (isBlank(baseDt)) {
            baseDt = recPickDynamicDao.selectLatestBaseDt(dtMap);
        }
        if (isBlank(baseDt)) {
            return buildEmptyResult(null);
        }

        // ── 1) SQL 단계: 사전계산 지표 WHERE 합성
        List<Indicator> selected = new ArrayList<Indicator>();
        List<SqlFragment> sqlFragments = new ArrayList<SqlFragment>();
        List<Indicator> realtimeIndicators = new ArrayList<Indicator>();

        for (IndicatorRequest ir : req.getIndicators()) {
            if (ir == null || isBlank(ir.getId())) continue;
            Indicator ind = indicatorRegistry.get(ir.getId());
            if (ind == null) continue;
            selected.add(ind);

            SqlFragment frag = ind.toSqlFragment(ir.getParams());
            if (frag != null && frag.getWhereClause() != null && !frag.getWhereClause().trim().isEmpty()) {
                sqlFragments.add(frag);
            } else {
                realtimeIndicators.add(ind);
            }
        }

        SqlFragment merged = SqlFragment.merge(sqlFragments);

        HashMap<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("baseDt", baseDt);
        queryMap.put("mktGroup", req.getMktGroup());
        queryMap.put("marketFilter", req.getMarketFilter());
        queryMap.put("stockType", req.getStockType());
        queryMap.put("useYn", "Y");
        queryMap.put("sortColumn", req.getSortColumn());
        queryMap.put("sortDir", req.getSortDir());
        queryMap.put("limit", req.getLimit() == null ? 200 : req.getLimit());

        if (merged != null) {
            queryMap.put("dynamicWhere", merged.getWhereClause());
            // 바인드 파라미터 머지 (#{p_xxx} 들이 OGNL로 풀림)
            if (merged.getBindParams() != null) {
                queryMap.putAll(merged.getBindParams());
            }
        }

        List<RecSignalDto> candidates = recPickDynamicDao.selectDynamicCandidates(queryMap);
        if (candidates == null) candidates = new ArrayList<RecSignalDto>();

        // ── 2) Java 단계: 실시간 지표 평가
        Map<String, List<DlyPriceDto>> priceMap = new HashMap<String, List<DlyPriceDto>>();
        boolean needPriceList = false;
        for (Indicator ind : realtimeIndicators) {
            if (ind.requiresPriceList()) { needPriceList = true; break; }
        }

        boolean priceListAvailable = true;
        String priceListWarning = null;
        if (needPriceList && !candidates.isEmpty()) {
            try {
                priceMap = prefetchPriceList(candidates);
                if (priceMap.isEmpty()) {
                    priceListAvailable = false;
                    priceListWarning = "TB_STK_DLY_PRICE 데이터 없음 → 실시간 지표 SKIP";
                }
            } catch (Exception ex) {
                priceListAvailable = false;
                priceListWarning = "일봉 조회 실패 (" + ex.getClass().getSimpleName() + ") → 실시간 지표 SKIP";
                System.err.println("[RecPickDynamicService] " + priceListWarning + ": " + ex.getMessage());
            }
        }

        List<Map<String, Object>> resultRows = new ArrayList<Map<String, Object>>();
        for (RecSignalDto stock : candidates) {
            IndicatorContext ctx = new IndicatorContext(stock, priceMap.get(stock.getStkCd()));

            // 모든 실시간 지표 통과해야 최종 포함 (가격리스트 없으면 SKIP=통과 처리)
            boolean allPassed = true;
            if (priceListAvailable) {
                for (Indicator ind : realtimeIndicators) {
                    IndicatorRequest match = findIndicatorRequest(req, ind.getId());
                    Map<String, Object> ps = match == null ? new HashMap<String, Object>() : match.getParams();
                    if (!ind.evaluate(ctx, ps)) { allPassed = false; break; }
                }
            }
            if (!allPassed) continue;

            Map<String, Object> row = toRowMap(stock);
            row.put("matchedIndicators", collectMatchedIndicatorIds(selected));
            resultRows.add(row);
        }

        // ── 3) 통계 계산 (등급별 카운트)
        Map<String, Integer> gradeCount = new LinkedHashMap<String, Integer>();
        gradeCount.put("A", 0); gradeCount.put("B", 0); gradeCount.put("C", 0);
        for (Map<String, Object> r : resultRows) {
            String g = (String) r.get("recGrade");
            if (g != null && gradeCount.containsKey(g)) {
                gradeCount.put(g, gradeCount.get(g) + 1);
            }
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("baseDt", baseDt);
        result.put("totalCount", resultRows.size());
        result.put("indicatorCount", selected.size());
        result.put("gradeCount", gradeCount);
        result.put("data", resultRows);
        if (priceListWarning != null) {
            result.put("warning", priceListWarning);
        }
        return result;
    }

    /** 종목 상세 + 차트용 일봉 데이터 */
    public Map<String, Object> selectStockDetail(String baseDt, String mktCd, String stkCd) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("baseDt", baseDt);
        map.put("mktCd", mktCd);
        map.put("stkCd", stkCd);

        RecSignalDto detail = recPickDynamicDao.selectStockDetail(map);

        // 일봉 200일치
        HashMap<String, Object> priceMap = new HashMap<String, Object>();
        priceMap.put("fromDt", addDays(baseDt, -300));
        priceMap.put("toDt", baseDt);
        List<String> stkList = new ArrayList<String>();
        stkList.add(stkCd);
        priceMap.put("stkCdList", stkList);

        List<DlyPriceDto> priceList;
        try {
            priceList = recPickDynamicDao.selectPriceListBatch(priceMap);
        } catch (Exception ex) {
            System.err.println("[RecPickDynamicService] 상세 일봉 조회 실패: " + ex.getMessage());
            priceList = new ArrayList<DlyPriceDto>();
        }
        if (priceList == null) priceList = new ArrayList<DlyPriceDto>();

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("detail", detail == null ? null : toRowMap(detail));
        result.put("priceList", convertPriceListForChart(priceList));
        return result;
    }

    /** UI 패널용 지표 메타데이터 (화면 진입 시 1회 호출) */
    public List<Map<String, Object>> getIndicatorMetadata() {
        return indicatorRegistry.getMetadataList();
    }

    // ────────────────────────────────────────────────────────────
    // helpers
    // ────────────────────────────────────────────────────────────

    private Map<String, List<DlyPriceDto>> prefetchPriceList(List<RecSignalDto> candidates) throws Exception {
        Set<String> stkSet = new LinkedHashSet<String>();
        String baseDt = null;
        for (RecSignalDto s : candidates) {
            if (s == null) continue;
            stkSet.add(s.getStkCd());
            if (baseDt == null) baseDt = s.getBaseDt();
        }
        if (stkSet.isEmpty() || baseDt == null) return new HashMap<String, List<DlyPriceDto>>();

        HashMap<String, Object> priceMap = new HashMap<String, Object>();
        priceMap.put("fromDt", addDays(baseDt, -300));
        priceMap.put("toDt", baseDt);
        priceMap.put("stkCdList", new ArrayList<String>(stkSet));

        List<DlyPriceDto> all = recPickDynamicDao.selectPriceListBatch(priceMap);
        Map<String, List<DlyPriceDto>> grouped = new HashMap<String, List<DlyPriceDto>>();
        if (all != null) {
            for (DlyPriceDto p : all) {
                if (p == null || p.getStkCd() == null) continue;
                List<DlyPriceDto> list = grouped.get(p.getStkCd());
                if (list == null) {
                    list = new ArrayList<DlyPriceDto>();
                    grouped.put(p.getStkCd(), list);
                }
                list.add(p);
            }
        }
        return grouped;
    }

    private List<String> collectMatchedIndicatorIds(List<Indicator> indicators) {
        Set<String> ids = new LinkedHashSet<String>();
        for (Indicator ind : indicators) ids.add(ind.getId());
        return new ArrayList<String>(ids);
    }

    private IndicatorRequest findIndicatorRequest(FilterRequestVo req, String id) {
        for (IndicatorRequest ir : req.getIndicators()) {
            if (ir != null && id.equals(ir.getId())) return ir;
        }
        return null;
    }

    private Map<String, Object> toRowMap(RecSignalDto s) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("baseDt",        s.getBaseDt());
        m.put("stkCd",         s.getStkCd());
        m.put("stkNm",         s.getStkNm());
        m.put("mktCd",         s.getMktCd());
        m.put("listingMarket", s.getListingMarket());
        m.put("curPrice",      s.getCurPrice());
        m.put("monOpenPrice",  s.getMonOpenPrice());
        m.put("ma5",           s.getMa5());
        m.put("ma20",          s.getMa20());
        m.put("ma60",          s.getMa60());
        m.put("ma120",         s.getMa120());
        m.put("ma240",         s.getMa240());
        m.put("goldenYn",      s.getGoldenYn());
        m.put("monUpYn",       s.getMonUpYn());
        m.put("monChgRate",    s.getMonChgRate());
        m.put("trendStrength", s.getTrendStrength());
        m.put("avgTrdVal20",   s.getAvgTrdVal20());
        m.put("recYn",         s.getRecYn());
        m.put("recGrade",      s.getRecGrade());
        m.put("recReason",     s.getRecReason());
        return m;
    }

    private List<Map<String, Object>> convertPriceListForChart(List<DlyPriceDto> list) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (DlyPriceDto p : list) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("tradeDt", p.getTradeDt());
            row.put("close",   p.getAdjClosePrice());
            row.put("open",    p.getAdjOpenPrice());
            row.put("high",    p.getAdjHighPrice());
            row.put("low",     p.getAdjLowPrice());
            row.put("volume",  p.getVolume());
            result.add(row);
        }
        return result;
    }

    private Map<String, Object> buildEmptyResult(String baseDt) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("baseDt", baseDt);
        result.put("totalCount", 0);
        result.put("indicatorCount", 0);
        Map<String, Integer> gc = new HashMap<String, Integer>();
        gc.put("A", 0); gc.put("B", 0); gc.put("C", 0);
        result.put("gradeCount", gc);
        result.put("data", new ArrayList<Object>());
        return result;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /** baseDt(YYYY-MM-DD) 에 days 일을 더해 YYYY-MM-DD 반환 */
    private String addDays(String baseDt, int days) {
        try {
            String[] parts = baseDt.split("-");
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            cal.add(Calendar.DAY_OF_MONTH, days);
            int yy = cal.get(Calendar.YEAR);
            int mm = cal.get(Calendar.MONTH) + 1;
            int dd = cal.get(Calendar.DAY_OF_MONTH);
            return String.format("%04d-%02d-%02d", yy, mm, dd);
        } catch (Exception e) {
            return baseDt;
        }
    }
    @SuppressWarnings("unused")
    private Date now() { return new Date(); }
    @SuppressWarnings("unused")
    private Set<String> emptySet() { return new HashSet<String>(); }
}
