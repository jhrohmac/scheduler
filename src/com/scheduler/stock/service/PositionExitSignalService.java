package com.scheduler.stock.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.scheduler.finance.dao.PositionDao;
import com.scheduler.finance.vo.PositionEventVo;
import com.scheduler.finance.vo.PositionVo;
import com.scheduler.stock.dao.RecPickDao;
import com.scheduler.stock.dto.DlyPriceDto;
import com.scheduler.stock.dto.PositionSellGuideDto;
import com.scheduler.stock.dto.RecPickDailyDto;
import com.scheduler.stock.dto.RecPickDto;
import com.scheduler.stock.util.RecPickUtil;

/**
 * 보유종목 매도 가이드 서비스
 *
 * 변경 이력
 *  - 중복 헬퍼 RecPickUtil 로 위임
 *  - selectSellGuide(): 당일 DAILY 데이터가 DB에 있으면 KIS API 미호출 (캐시 분기 추가)
 *  - evaluatePosition(): TIME_EXIT 기준 수익률 임계값 상수화
 *  - MA20 계산: skip null rows 정확도 개선
 */
public class PositionExitSignalService {

    private static final int   FETCH_DAYS            = 400;
    private static final long  REQUEST_INTERVAL_MS   = 1000L;
    private static final double STOP_LOSS_THRESHOLD  = -7d;   // -7% 강제 손절
    private static final double TIME_EXIT_MIN_RETURN  = 2d;   // 20일 경과 최소 수익률
    private static final int    TIME_EXIT_HOLD_DAYS   = 20;

    private PositionDao positionDao;
    private RecPickDao recPickDao;
    private KisDlyPriceSyncService kisDlyPriceSyncService;
    private TradeDateService tradeDateService;

    public void setPositionDao(PositionDao positionDao)                               { this.positionDao = positionDao; }
    public void setRecPickDao(RecPickDao recPickDao)                                   { this.recPickDao = recPickDao; }
    public void setKisDlyPriceSyncService(KisDlyPriceSyncService s)                   { this.kisDlyPriceSyncService = s; }
    public void setTradeDateService(TradeDateService tradeDateService)                 { this.tradeDateService = tradeDateService; }

    /* ─────────────────────────────────────────────
       배치 진입점: 보유 포지션 전체 매도 가이드 갱신
    ───────────────────────────────────────────── */

    public HashMap<String, Object> runDailySellGuide(HashMap<String, String> map) throws Exception {
        HashMap<String, String> queryMap = new HashMap<String, String>();
        if (map != null) queryMap.putAll(map);
        queryMap.put("closeFlag", "N");
        if (!RecPickUtil.isBlank(queryMap.get("mktCd")) && RecPickUtil.isBlank(queryMap.get("marketCode"))) {
            queryMap.put("marketCode", RecPickUtil.normalizeMarketGroup(queryMap.get("mktCd")));
        }

        List<PositionVo> positionList = positionDao.selectPositionList(queryMap);
        if (positionList == null) positionList = new ArrayList<PositionVo>();

        int updatedPositionCnt = 0;
        int sellReadyCnt       = 0;
        int stopLossCnt        = 0;
        String lastError       = null;

        for (int i = 0; i < positionList.size(); i++) {
            PositionVo position = positionList.get(i);
            if (position == null || position.getSourcePickId() == null) continue;

            try {
                RecPickDto pick = selectPick(position.getSourcePickId());
                if (pick == null) continue;

                List<DlyPriceDto> prices = fetchPrices(pick, position.getStockCode());
                PositionSellGuideDto guide = evaluatePosition(position, pick, prices);
                if (guide == null) continue;

                boolean changed = !equalsStr(position.getSellGuideState(), guide.getSellGuideState())
                    || !equalsStr(position.getLastSellSignalCode(), guide.getSellSignalCode());

                position.setSellGuideState(guide.getSellGuideState());
                position.setLastSellSignalCode(guide.getSellSignalCode());
                position.setLastTrackDate(java.sql.Date.valueOf(guide.getAsOfTradeDt()));
                positionDao.updatePositionRecoLink(position);

                updateLatestDailyGuide(pick, guide);

                if (changed) insertPositionEvent(position, guide);

                updatedPositionCnt++;
                if ("SELL_READY".equals(guide.getSellGuideState())) sellReadyCnt++;
                if ("STOP_LOSS".equals(guide.getSellGuideState()))   stopLossCnt++;

            } catch (Exception e) {
                lastError = position.getStockCode() + " : " + e.getLocalizedMessage();
            }
        }

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("targetPositionCnt",  Integer.valueOf(positionList.size()));
        result.put("updatedPositionCnt", Integer.valueOf(updatedPositionCnt));
        result.put("sellReadyCnt",       Integer.valueOf(sellReadyCnt));
        result.put("stopLossCnt",        Integer.valueOf(stopLossCnt));
        result.put("lastError",          lastError);
        return result;
    }

    /* ─────────────────────────────────────────────
       단건 조회: sellGuide.do
       ★ 당일 DAILY 데이터가 이미 DB에 있으면 KIS API 미호출
    ───────────────────────────────────────────── */

    public PositionSellGuideDto selectSellGuide(HashMap<String, String> map) throws Exception {
        if (map == null || RecPickUtil.isBlank(map.get("positionId"))) {
            throw new IllegalArgumentException("positionId 는 필수입니다.");
        }
        PositionVo position = positionDao.selectPosition(map);
        if (position == null) throw new IllegalStateException("보유종목을 찾을 수 없습니다.");
        if (position.getSourcePickId() == null) throw new IllegalStateException("추천 저장 이력과 연결된 보유종목이 아닙니다.");

        RecPickDto pick = selectPick(position.getSourcePickId());
        if (pick == null) throw new IllegalStateException("추천 저장 이력을 찾을 수 없습니다.");

        // ★ 캐시 분기: 오늘 DAILY 행이 있으면 DB 기반 평가
        String today = tradeDateService.resolveToday();
        List<DlyPriceDto> prices = tryLoadFromDb(pick.getPickId(), today);
        if (prices == null || prices.isEmpty()) {
            prices = fetchPrices(pick, position.getStockCode());
        }

        return evaluatePosition(position, pick, prices);
    }

    /* ─────────────────────────────────────────────
       핵심 평가 로직
    ───────────────────────────────────────────── */

    public PositionSellGuideDto evaluatePosition(PositionVo position, RecPickDto pick, List<DlyPriceDto> prices) throws Exception {
        if (position == null || pick == null || prices == null || prices.isEmpty()) return null;

        DlyPriceDto latest = findLatestValidRow(prices);
        if (latest == null || latest.getAdjClosePrice() == null) return null;

        double currentReturn = RecPickUtil.calcReturnPct(latest.getAdjClosePrice(), position.getAvgPrice()) != null
            ? RecPickUtil.calcReturnPct(latest.getAdjClosePrice(), position.getAvgPrice()).doubleValue()
            : 0d;

        double ma20 = calculateMa20(prices);
        Integer buyHoldDays = calcHoldDays(prices, pick.getFirstBuyDate());

        String state      = "HOLD";
        String signalCode = "NONE";
        String signalText = "보유 유지";

        // 우선순위 순서로 평가 — 먼저 매칭된 조건이 최종 상태
        if (isStopLossByPrice(latest, position)) {
            state = "STOP_LOSS"; signalCode = "STOP_LOSS"; signalText = "손절가 하향 이탈";
        } else if (currentReturn <= STOP_LOSS_THRESHOLD) {
            state = "STOP_LOSS"; signalCode = "STOP_LOSS"; signalText = "수익률 " + STOP_LOSS_THRESHOLD + "% 이하";
        } else if (isNewTp1Hit(latest, position)) {
            state = "TP1_DONE"; signalCode = "TP1"; signalText = "1차 목표가 도달";
        } else if (isTrailingExit(latest, position, ma20)) {
            state = "SELL_READY"; signalCode = "TRAILING_EXIT"; signalText = "TP1 이후 MA20 하향 이탈";
        } else if (isTimeExit(buyHoldDays, currentReturn)) {
            state = "SELL_READY"; signalCode = "TIME_EXIT"; signalText = TIME_EXIT_HOLD_DAYS + "거래일 경과 대비 성과 부진";
        } else if ("TP1_DONE".equals(position.getSellGuideState()) || "TRAILING".equals(position.getSellGuideState())) {
            state = "TRAILING"; signalCode = "NONE"; signalText = "추세 추적 중";
        }

        PositionSellGuideDto guide = new PositionSellGuideDto();
        guide.setPositionId(position.getPositionId());
        guide.setSourcePickId(position.getSourcePickId());
        guide.setStockCode(position.getStockCode());
        guide.setSellGuideState(state);
        guide.setSellSignalCode(signalCode);
        guide.setSellSignalText(signalText);
        guide.setCurrentReturnPct(RecPickUtil.roundD(currentReturn));
        guide.setTargetPrice(position.getTargetPrice());
        guide.setStopPrice(position.getStopPrice());
        guide.setTp1Price(position.getTp1Price());
        guide.setAsOfTradeDt(latest.getTradeDt());
        return guide;
    }

    /* ─────────────────────────────────────────────
       조건 판정 헬퍼 (가독성 분리)
    ───────────────────────────────────────────── */

    private boolean isStopLossByPrice(DlyPriceDto latest, PositionVo pos) {
        return pos.getStopPrice() != null && latest.getAdjLowPrice() != null
            && latest.getAdjLowPrice().doubleValue() <= pos.getStopPrice().doubleValue();
    }

    private boolean isNewTp1Hit(DlyPriceDto latest, PositionVo pos) {
        if (pos.getTp1Price() == null || latest.getAdjHighPrice() == null) return false;
        if ("TP1_DONE".equals(pos.getSellGuideState()) || "TRAILING".equals(pos.getSellGuideState())) return false;
        return latest.getAdjHighPrice().doubleValue() >= pos.getTp1Price().doubleValue();
    }

    private boolean isTrailingExit(DlyPriceDto latest, PositionVo pos, double ma20) {
        if (!("TP1_DONE".equals(pos.getSellGuideState()) || "TRAILING".equals(pos.getSellGuideState()))) return false;
        return ma20 > 0d && latest.getAdjClosePrice() != null
            && latest.getAdjClosePrice().doubleValue() < ma20;
    }

    private boolean isTimeExit(Integer holdDays, double returnPct) {
        return holdDays != null && holdDays.intValue() >= TIME_EXIT_HOLD_DAYS && returnPct < TIME_EXIT_MIN_RETURN;
    }

    /* ─────────────────────────────────────────────
       데이터 조회 헬퍼
    ───────────────────────────────────────────── */

    private RecPickDto selectPick(Long pickId) throws Exception {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put("pickId", String.valueOf(pickId));
        return recPickDao.selectRecPick(m);
    }

    private List<DlyPriceDto> fetchPrices(RecPickDto pick, String stockCode) throws Exception {
        String listingMkt = RecPickUtil.isBlank(pick.getListingMarket())
            ? pick.getSourceMktCd() : pick.getListingMarket();
        return kisDlyPriceSyncService.fetchAdjustedDailyPrices(
            stockCode,
            listingMkt,
            RecPickUtil.normalizeMarketGroup(pick.getSourceMktCd()),
            tradeDateService.resolveToday(),
            FETCH_DAYS,
            REQUEST_INTERVAL_MS
        );
    }

    /**
     * 오늘 날짜 DAILY 행이 DB에 있으면 DlyPriceDto 목록으로 변환해 반환.
     * 없으면 null 반환 → 호출자가 KIS API 호출로 폴백.
     */
    private List<DlyPriceDto> tryLoadFromDb(Long pickId, String today) {
        try {
            HashMap<String, String> q = new HashMap<String, String>();
            q.put("pickId", String.valueOf(pickId));
            q.put("tradeDtFrom", today);
            q.put("tradeDtTo", today);
            List<RecPickDailyDto> dbRows = recPickDao.selectRecPickDailyList(q);
            if (dbRows == null || dbRows.isEmpty()) return null;

            // RecPickDailyDto → DlyPriceDto 변환 (평가에 필요한 필드만)
            List<DlyPriceDto> result = new ArrayList<DlyPriceDto>();
            for (int i = 0; i < dbRows.size(); i++) {
                RecPickDailyDto r = dbRows.get(i);
                DlyPriceDto d = new DlyPriceDto();
                d.setTradeDt(r.getTradeDt());
                d.setAdjOpenPrice(r.getOpenPrice());
                d.setAdjHighPrice(r.getHighPrice());
                d.setAdjLowPrice(r.getLowPrice());
                d.setAdjClosePrice(r.getClosePrice());
                result.add(d);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /* ─────────────────────────────────────────────
       MA20 / 보유일수 계산
    ───────────────────────────────────────────── */

    private double calculateMa20(List<DlyPriceDto> prices) {
        int count = 0;
        double sum = 0d;
        for (int i = prices.size() - 1; i >= 0 && count < 20; i--) {
            DlyPriceDto row = prices.get(i);
            if (row == null || row.getAdjClosePrice() == null) continue;
            sum += row.getAdjClosePrice().doubleValue();
            count++;
        }
        return count < 20 ? 0d : sum / 20d;
    }

    private Integer calcHoldDays(List<DlyPriceDto> prices, String buyDate) {
        if (RecPickUtil.isBlank(buyDate)) return null;
        String normalized = RecPickUtil.normalizeDate(buyDate);
        int buyIndex    = -1;
        int latestIndex = -1;
        for (int i = 0; i < prices.size(); i++) {
            DlyPriceDto row = prices.get(i);
            if (row == null || RecPickUtil.isBlank(row.getTradeDt())) continue;
            if (normalized.equals(row.getTradeDt())) buyIndex = i;
            if (row.getAdjClosePrice() != null)       latestIndex = i;
        }
        if (buyIndex < 0 || latestIndex < buyIndex) return null;
        return Integer.valueOf(latestIndex - buyIndex);
    }

    private DlyPriceDto findLatestValidRow(List<DlyPriceDto> prices) {
        for (int i = prices.size() - 1; i >= 0; i--) {
            DlyPriceDto row = prices.get(i);
            if (row != null && row.getAdjClosePrice() != null && !RecPickUtil.isBlank(row.getTradeDt())) return row;
        }
        return null;
    }

    /* ─────────────────────────────────────────────
       DB 갱신 헬퍼
    ───────────────────────────────────────────── */

    private void updateLatestDailyGuide(RecPickDto pick, PositionSellGuideDto guide) throws Exception {
        if (pick == null || guide == null || "NONE".equals(guide.getSellSignalCode())) return;
        HashMap<String, String> q = new HashMap<String, String>();
        q.put("pickId", String.valueOf(pick.getPickId()));
        q.put("tradeDtFrom", guide.getAsOfTradeDt());
        q.put("tradeDtTo",   guide.getAsOfTradeDt());
        List<RecPickDailyDto> dailyList = recPickDao.selectRecPickDailyList(q);
        if (dailyList == null || dailyList.isEmpty()) return;
        RecPickDailyDto latest = dailyList.get(0);
        latest.setSellSignalCode(guide.getSellSignalCode());
        latest.setSellSignalText(guide.getSellSignalText());
        recPickDao.mergeRecPickDaily(latest);
    }

    private void insertPositionEvent(PositionVo position, PositionSellGuideDto guide) throws Exception {
        PositionEventVo event = new PositionEventVo();
        event.setPositionId(position.getPositionId());
        event.setStockGroup(position.getStockGroup());
        event.setStockCode(position.getStockCode());
        event.setEventType(guide.getSellSignalCode());
        event.setEventLevel("INFO");
        event.setEventTime(java.sql.Timestamp.valueOf(guide.getAsOfTradeDt() + " 00:00:00"));
        event.setMessage(guide.getSellSignalText());
        event.setJsonParams("{\"state\":\"" + guide.getSellGuideState() + "\",\"returnPct\":" + guide.getCurrentReturnPct() + "}");
        positionDao.insertPositionEvent(event);
    }

    /* ─────────────────────────────────────────────
       소형 유틸
    ───────────────────────────────────────────── */

    private boolean equalsStr(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
