package com.scheduler.stock.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.scheduler.finance.dao.PositionDao;
import com.scheduler.finance.vo.PositionVo;
import com.scheduler.stock.dao.RecPickDao;
import com.scheduler.stock.dao.TradeDateDao;
import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.dto.RecPickDailyDto;
import com.scheduler.stock.dto.RecPickDto;
import com.scheduler.stock.dto.RecPickEvalDto;
import com.scheduler.stock.util.RecPickUtil;

/**
 * 추천 종목 일별 추적 서비스
 *
 * 변경 이력
 *  - N+1 수정: upsertEvalRows 내 selectPositionForPick 루프 호출 → 메서드 상단 1회 호출로 변경
 *  - 중복 헬퍼 제거: RecPickUtil 로 위임
 *  - sellSignalCode NONE 덮어쓰기 방어: mergeRecPickDaily 호출 시 기존 코드가 있으면 유지
 */
public class RecPickTrackService {

    private static final int FETCH_DAYS = 400;
    private static final long REQUEST_INTERVAL_MS = 1000L;
    private static final int[] EVAL_HORIZONS = new int[] {5, 20, 60};

    private RecPickDao recPickDao;
    private TradeDateDao tradeDateDao;
    private KisDlyPriceSyncService kisDlyPriceSyncService;
    private PositionDao positionDao;
    private TradeDateService tradeDateService;

    public void setRecPickDao(RecPickDao recPickDao) {
        this.recPickDao = recPickDao;
    }

    public void setTradeDateDao(TradeDateDao tradeDateDao) {
        this.tradeDateDao = tradeDateDao;
    }

    public void setKisDlyPriceSyncService(KisDlyPriceSyncService kisDlyPriceSyncService) {
        this.kisDlyPriceSyncService = kisDlyPriceSyncService;
    }

    public void setPositionDao(PositionDao positionDao) {
        this.positionDao = positionDao;
    }

    public void setTradeDateService(TradeDateService tradeDateService) {
        this.tradeDateService = tradeDateService;
    }

    /* ─────────────────────────────────────────────
       배치 진입점
    ───────────────────────────────────────────── */

    public HashMap<String, Object> runDailyTrack(HashMap<String, String> map) throws Exception {
        HashMap<String, String> queryMap = new HashMap<String, String>();
        if (map != null) queryMap.putAll(map);
        if (RecPickUtil.isBlank(queryMap.get("pickId")) && !RecPickUtil.isBlank(queryMap.get("forcePickId"))) {
            queryMap.put("pickId", RecPickUtil.trim(queryMap.get("forcePickId")));
        }

        List<RecPickDto> targetList = recPickDao.selectTrackTargetPickList(queryMap);
        if (targetList == null) targetList = new ArrayList<RecPickDto>();

        int updatedDailyCnt = 0;
        int updatedEvalCnt  = 0;
        int failedPickCnt   = 0;
        String lastError    = null;
        String today        = tradeDateService.resolveToday();

        for (int i = 0; i < targetList.size(); i++) {
            RecPickDto pick = targetList.get(i);
            if (pick == null || RecPickUtil.isBlank(pick.getStkCd())) continue;

            try {
                List<DlyPriceDto> priceList = kisDlyPriceSyncService.fetchAdjustedDailyPrices(
                    pick.getStkCd(),
                    resolveListingMarketForFetch(pick),
                    RecPickUtil.normalizeMarketGroup(pick.getSourceMktCd()),
                    today,
                    FETCH_DAYS,
                    REQUEST_INTERVAL_MS
                );

                mergeTradeCalendar(priceList, RecPickUtil.normalizeMarketGroup(pick.getSourceMktCd()));
                updatedDailyCnt += upsertDailyRows(pick, priceList);
                updatedEvalCnt  += upsertEvalRows(pick, priceList);

                HashMap<String, Object> updateMap = new HashMap<String, Object>();
                updateMap.put("pickId", pick.getPickId());
                updateMap.put("lastTrackDt", today);
                recPickDao.updateRecPickTrackDate(updateMap);

            } catch (Exception e) {
                failedPickCnt++;
                lastError = pick.getStkCd() + " : " + e.getLocalizedMessage();
            }
        }

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("targetPickCnt",  Integer.valueOf(targetList.size()));
        result.put("updatedDailyCnt", Integer.valueOf(updatedDailyCnt));
        result.put("updatedEvalCnt",  Integer.valueOf(updatedEvalCnt));
        result.put("failedPickCnt",   Integer.valueOf(failedPickCnt));
        result.put("lastError",       lastError);
        return result;
    }

    public List<RecPickDailyDto> selectDailyTrack(HashMap<String, String> map) throws Exception {
        HashMap<String, String> queryMap = new HashMap<String, String>();
        if (map != null) {
            queryMap.putAll(map);
        }

        List<RecPickDailyDto> list = recPickDao.selectRecPickDailyList(queryMap);
        if (shouldBackfillDailyTrack(queryMap, list)) {
            HashMap<String, String> runMap = new HashMap<String, String>();
            runMap.put("pickId", RecPickUtil.trim(queryMap.get("pickId")));
            runDailyTrack(runMap);
            list = recPickDao.selectRecPickDailyList(queryMap);
        }
        return list == null ? new ArrayList<RecPickDailyDto>() : list;
    }

    /* ─────────────────────────────────────────────
       Daily 행 MERGE
    ───────────────────────────────────────────── */

    private int upsertDailyRows(RecPickDto pick, List<DlyPriceDto> priceList) throws Exception {
        if (priceList == null || priceList.isEmpty()) return 0;

        PositionVo position = selectPositionForPick(pick);  // 1회만 조회

        int sourceIndex = RecPickUtil.findTradeDateIndex(priceList, pick.getSourceBaseDt());
        int watchIndex  = RecPickUtil.findTradeDateIndex(priceList, pick.getWatchSavedAt());
        int buyIndex    = RecPickUtil.findTradeDateIndex(priceList, pick.getFirstBuyDate());

        if (sourceIndex < 0 && watchIndex < 0 && buyIndex < 0) return 0;

        int startIndex = resolveStartIndex(sourceIndex, watchIndex, buyIndex);

        double recAnchor   = RecPickUtil.safePrice(pick.getRecAnchorPrice());
        double watchAnchor = RecPickUtil.safePrice(pick.getWatchAnchorPrice());
        double buyAnchor   = RecPickUtil.safePrice(pick.getFirstBuyPrice());
        int updatedCnt = 0;

        for (int i = startIndex; i < priceList.size(); i++) {
            DlyPriceDto row = priceList.get(i);
            if (row == null || RecPickUtil.isBlank(row.getTradeDt()) || row.getAdjClosePrice() == null) continue;

            RecPickDailyDto daily = new RecPickDailyDto();
            daily.setPickId(pick.getPickId());
            daily.setTradeDt(row.getTradeDt());
            daily.setMktCd(RecPickUtil.normalizeMarketGroup(pick.getSourceMktCd()));
            daily.setStkCd(pick.getStkCd());
            daily.setOpenPrice(row.getAdjOpenPrice());
            daily.setHighPrice(row.getAdjHighPrice());
            daily.setLowPrice(row.getAdjLowPrice());
            daily.setClosePrice(row.getAdjClosePrice());
            daily.setHoldDayNoFromRec(RecPickUtil.indexDiff(sourceIndex, i));
            daily.setHoldDayNoFromWatch(RecPickUtil.indexDiff(watchIndex, i));
            daily.setHoldDayNoFromBuy(RecPickUtil.indexDiff(buyIndex, i));
            daily.setReturnFromRecPct(RecPickUtil.calcReturnPct(row.getAdjClosePrice(), recAnchor));
            daily.setReturnFromWatchPct(RecPickUtil.calcReturnPct(row.getAdjClosePrice(), watchAnchor));
            daily.setReturnFromBuyPct(RecPickUtil.calcReturnPct(row.getAdjClosePrice(), buyAnchor));
            daily.setMfeFromRecPct(RecPickUtil.calcMfePct(priceList, sourceIndex, i, recAnchor));
            daily.setMaeFromRecPct(RecPickUtil.calcMaePct(priceList, sourceIndex, i, recAnchor));
            daily.setMfeFromBuyPct(RecPickUtil.calcMfePct(priceList, buyIndex, i, buyAnchor));
            daily.setMaeFromBuyPct(RecPickUtil.calcMaePct(priceList, buyIndex, i, buyAnchor));
            daily.setUpDownFlag(resolveUpDownFlag(priceList, i, recAnchor));
            daily.setPriceSource("KIS_DAILY");

            if (position != null && position.getTp1Price() != null && position.getStopPrice() != null
                    && buyIndex >= 0 && i >= buyIndex) {
                daily.setTp1HitYn(RecPickUtil.isTp1Hit(row, position.getTp1Price()) ? "Y" : "N");
                daily.setStopHitYn(RecPickUtil.isStopHit(row, position.getStopPrice()) ? "Y" : "N");
            } else {
                daily.setTp1HitYn("N");
                daily.setStopHitYn("N");
            }

            // sellSignalCode 는 null 로 전달 — Mapper의 CASE 로직에서 기존값 보존
            daily.setSellSignalCode(null);
            daily.setSellSignalText(null);

            recPickDao.mergeRecPickDaily(daily);
            updatedCnt++;
        }
        return updatedCnt;
    }

    /* ─────────────────────────────────────────────
       Eval 행 MERGE  (N+1 수정: position 을 파라미터로 전달)
    ───────────────────────────────────────────── */

    private int upsertEvalRows(RecPickDto pick, List<DlyPriceDto> priceList) throws Exception {
        if (priceList == null || priceList.isEmpty()) return 0;

        // ★ N+1 수정 포인트: 루프 외부에서 1회 조회
        PositionVo position = selectPositionForPick(pick);

        int sourceIndex = RecPickUtil.findTradeDateIndex(priceList, pick.getSourceBaseDt());
        int watchIndex  = RecPickUtil.findTradeDateIndex(priceList, pick.getWatchSavedAt());
        int buyIndex    = RecPickUtil.findTradeDateIndex(priceList, pick.getFirstBuyDate());
        int updatedCnt  = 0;

        for (int hi = 0; hi < EVAL_HORIZONS.length; hi++) {
            int horizon = EVAL_HORIZONS[hi];
            updatedCnt += mergeEvalForAnchor(pick, priceList, sourceIndex, "REC",   horizon, RecPickUtil.safePrice(pick.getRecAnchorPrice()),   pick.getSourceBaseDt(), position);
            updatedCnt += mergeEvalForAnchor(pick, priceList, watchIndex,  "WATCH", horizon, RecPickUtil.safePrice(pick.getWatchAnchorPrice()), pick.getWatchSavedAt(),  position);
            updatedCnt += mergeEvalForAnchor(pick, priceList, buyIndex,    "BUY",   horizon, RecPickUtil.safePrice(pick.getFirstBuyPrice()),    pick.getFirstBuyDate(),  position);
        }

        return updatedCnt;
    }

    private int mergeEvalForAnchor(RecPickDto pick, List<DlyPriceDto> priceList,
            int anchorIndex, String evalType, int holdDays,
            double anchorPrice, String anchorDate,
            PositionVo position) throws Exception {

        if (anchorIndex < 0 || anchorPrice <= 0d || RecPickUtil.isBlank(anchorDate)) return 0;

        int targetIndex = anchorIndex + holdDays;

        RecPickEvalDto eval = new RecPickEvalDto();
        eval.setPickId(pick.getPickId());
        eval.setEvalType(evalType);
        eval.setHoldDays(Integer.valueOf(holdDays));
        eval.setAnchorDate(anchorDate);

        if (targetIndex >= priceList.size()) {
            eval.setCalcStatus("PENDING");
            recPickDao.mergeRecPickEval(eval);
            return 1;
        }

        DlyPriceDto targetRow = priceList.get(targetIndex);
        eval.setTargetDate(targetRow.getTradeDt());
        eval.setReturnPct(RecPickUtil.calcReturnPct(targetRow.getAdjClosePrice(), anchorPrice));
        eval.setMfePct(RecPickUtil.calcMfePct(priceList, anchorIndex, targetIndex, anchorPrice));
        eval.setMaePct(RecPickUtil.calcMaePct(priceList, anchorIndex, targetIndex, anchorPrice));
        eval.setWinYn(eval.getReturnPct() != null && eval.getReturnPct().doubleValue() > 0d ? "Y" : "N");

        if ("BUY".equals(evalType) && position != null
                && position.getTp1Price() != null && position.getStopPrice() != null) {
            eval.setTp1HitYn(RecPickUtil.scanTp1Hit(priceList, anchorIndex, targetIndex, position.getTp1Price()) ? "Y" : "N");
            eval.setStopHitYn(RecPickUtil.scanStopHit(priceList, anchorIndex, targetIndex, position.getStopPrice()) ? "Y" : "N");
        } else {
            eval.setTp1HitYn("N");
            eval.setStopHitYn("N");
        }
        eval.setCalcStatus("READY");
        recPickDao.mergeRecPickEval(eval);
        return 1;
    }

    /* ─────────────────────────────────────────────
       내부 유틸
    ───────────────────────────────────────────── */

    private PositionVo selectPositionForPick(RecPickDto pick) throws Exception {
        if (pick == null || pick.getPositionId() == null) return null;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("positionId", String.valueOf(pick.getPositionId()));
        return positionDao.selectPosition(map);
    }

    private boolean shouldBackfillDailyTrack(HashMap<String, String> map, List<RecPickDailyDto> list) {
        if (list != null && !list.isEmpty()) return false;
        if (map == null) return false;
        if (RecPickUtil.isBlank(map.get("pickId"))) return false;
        if (!RecPickUtil.isBlank(map.get("tradeDtFrom"))) return false;
        if (!RecPickUtil.isBlank(map.get("tradeDtTo"))) return false;
        return true;
    }

    private void mergeTradeCalendar(List<DlyPriceDto> priceList, String marketGroup) throws Exception {
        if (priceList == null) return;
        for (int i = 0; i < priceList.size(); i++) {
            DlyPriceDto dto = priceList.get(i);
            if (dto == null || RecPickUtil.isBlank(dto.getTradeDt())) continue;
            HashMap<String, Object> m = new HashMap<String, Object>();
            m.put("tradeDt", dto.getTradeDt());
            m.put("mktCd", marketGroup);
            m.put("tradeYn", "Y");
            tradeDateDao.mergeTradeDate(m);
        }
    }

    private int resolveStartIndex(int sourceIndex, int watchIndex, int buyIndex) {
        int start = sourceIndex >= 0 ? sourceIndex : Integer.MAX_VALUE;
        if (watchIndex >= 0 && watchIndex < start) start = watchIndex;
        if (buyIndex   >= 0 && buyIndex   < start) start = buyIndex;
        return start == Integer.MAX_VALUE ? 0 : start;
    }

    private String resolveListingMarketForFetch(RecPickDto pick) {
        return RecPickUtil.isBlank(pick.getListingMarket()) ? pick.getSourceMktCd() : pick.getListingMarket();
    }

    private String resolveUpDownFlag(List<DlyPriceDto> priceList, int index, double fallbackAnchor) {
        DlyPriceDto current = priceList.get(index);
        if (current == null || current.getAdjClosePrice() == null) return null;
        double comparePrice = fallbackAnchor;
        if (index > 0) {
            DlyPriceDto prev = priceList.get(index - 1);
            if (prev != null && prev.getAdjClosePrice() != null) {
                comparePrice = prev.getAdjClosePrice().doubleValue();
            }
        }
        double cp = current.getAdjClosePrice().doubleValue();
        if (cp > comparePrice) return "U";
        if (cp < comparePrice) return "D";
        return "F";
    }
}
