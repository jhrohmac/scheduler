package com.scheduler.stock.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONObject;

import com.scheduler.finance.dao.MarketSummaryDao;
import com.scheduler.finance.dao.PositionDao;
import com.scheduler.finance.vo.PositionTxnVo;
import com.scheduler.finance.vo.PositionVo;
import com.scheduler.stock.dao.RecPickDao;
import com.scheduler.stock.dto.RecPickDto;
import com.scheduler.stock.dto.RecSignalDto;
import com.scheduler.stock.util.RecPickUtil;

/**
 * 추천 종목 저장/매수 등록 서비스
 *
 * 변경 이력
 *  - 중복 헬퍼 RecPickUtil 로 위임
 *  - ruleTargetPrice / ruleStopPrice / ruleTp1Price 상수 정리
 *  - saveToWatchlist(): 중복 pick 존재 시 watchAnchorPrice 갱신 로직 명시
 */
public class RecPickService {

    private static final String RECOMMEND_GROUP_DIV = "recommend";

    /* 진입 규칙별 목표가/손절/TP1 비율 */
    private static final double TARGET_DEFAULT    = 1.10d;
    private static final double TARGET_BREAKOUT   = 1.12d;
    private static final double TARGET_PULLBACK   = 1.08d;

    private static final double STOP_DEFAULT      = 0.93d;
    private static final double STOP_BREAKOUT     = 0.95d;
    private static final double STOP_PULLBACK     = 0.95d;

    private static final double TP1_DEFAULT       = 1.05d;
    private static final double TP1_BREAKOUT      = 1.06d;
    private static final double TP1_PULLBACK      = 1.04d;

    private RecSignalService recSignalService;
    private RecPickDao recPickDao;
    private MarketSummaryDao marketSummaryDao;
    private PositionDao positionDao;
    private TradeDateService tradeDateService;

    public void setRecSignalService(RecSignalService s)   { this.recSignalService = s; }
    public void setRecPickDao(RecPickDao d)                { this.recPickDao = d; }
    public void setMarketSummaryDao(MarketSummaryDao d)    { this.marketSummaryDao = d; }
    public void setPositionDao(PositionDao d)              { this.positionDao = d; }
    public void setTradeDateService(TradeDateService s)    { this.tradeDateService = s; }

    /* ─────────────────────────────────────────────
       추천 저장 → 관심종목 등록
    ───────────────────────────────────────────── */

    public HashMap<String, Object> saveToWatchlist(HashMap<String, String> map) throws Exception {
        require(map, "baseDt",       "baseDt 는 필수입니다.");
        require(map, "mktCd",        "mktCd 는 필수입니다.");
        require(map, "stkCd",        "stkCd 는 필수입니다.");
        require(map, "watchGroupId", "watchGroupId 는 필수입니다.");

        // 추천 신호 상세 조회
        HashMap<String, String> recMap = new HashMap<String, String>();
        recMap.put("baseDt", RecPickUtil.trim(map.get("baseDt")));
        recMap.put("mktCd",  RecPickUtil.trim(map.get("mktCd")));
        recMap.put("stkCd",  RecPickUtil.trim(map.get("stkCd")));
        RecSignalDto rec = recSignalService.selectRecSignalDetail(recMap);
        if (rec == null) throw new IllegalStateException("추천신호 상세를 찾을 수 없습니다.");

        String normalizedMkt   = RecPickUtil.normalizeMarketGroup(rec.getMktCd());
        String watchGroupId    = RecPickUtil.trim(map.get("watchGroupId"));

        // 기존 PICK 이력 조회
        HashMap<String, String> existingMap = new HashMap<String, String>();
        existingMap.put("sourceBaseDt", rec.getBaseDt());
        existingMap.put("mktCd",        normalizedMkt);
        existingMap.put("watchGroupId", watchGroupId);
        existingMap.put("stkCd",        rec.getStkCd());
        existingMap.put("pickStatus",   "WATCH");
        List<RecPickDto> existingList = recPickDao.selectRecPickList(existingMap);

        // 관심종목 저장 (중복이어도 upsert)
        HashMap<String, String> watchMap = new HashMap<String, String>();
        watchMap.put("groupId",    watchGroupId);
        watchMap.put("groupDiv",   RECOMMEND_GROUP_DIV);
        watchMap.put("stockCode",  rec.getStkCd());
        watchMap.put("stockClose", rec.getCurPrice() != null ? String.valueOf(rec.getCurPrice()) : null);
        int affected = marketSummaryDao.insertWatchlistItem(watchMap);
        if (isDmlFailure(affected)) throw new IllegalStateException("관심종목 저장에 실패했습니다.");

        RecPickDto pick;
        if (existingList != null && !existingList.isEmpty()) {
            // ★ 이미 있으면 watchAnchorPrice 최신화 (재저장 시 가격 갱신)
            pick = existingList.get(0);
        } else {
            RecPickDto dto = new RecPickDto();
            dto.setSourceType("REC_SIGNAL_V110");
            dto.setSourceBaseDt(rec.getBaseDt());
            dto.setSourceMktCd(normalizedMkt);
            dto.setStkCd(rec.getStkCd());
            dto.setStkNm(rec.getStkNm());
            dto.setListingMarket(rec.getListingMarket());
            dto.setWatchGroupId(watchGroupId);
            dto.setPickStatus("WATCH");
            dto.setRecRank(RecPickUtil.parseInteger(map.get("recRank"), null));
            dto.setRecYn(RecPickUtil.defaultString(rec.getRecYn(), "Y"));
            dto.setRecGrade(rec.getRecGrade());
            dto.setRecReason(rec.getRecReason());
            dto.setGoldenYn(rec.getGoldenYn());
            dto.setMonUpYn(rec.getMonUpYn());
            dto.setMonChgRate(rec.getMonChgRate());
            dto.setTrendStrength(rec.getTrendStrength());
            dto.setAvgTrdVal20(rec.getAvgTrdVal20());
            dto.setRecAnchorPrice(RecPickUtil.safePrice(rec.getCurPrice()));
            dto.setWatchAnchorPrice(RecPickUtil.safePrice(rec.getCurPrice()));
            dto.setCreateUser(RecPickUtil.trim(map.get("userId")));
            dto.setSnapshotJson(JSONObject.fromObject(rec).toString());

            int insertCnt = recPickDao.insertRecPick(dto);
            if (isDmlFailure(insertCnt)) throw new IllegalStateException("추천 저장 이력 생성에 실패했습니다.");

            List<RecPickDto> insertedList = recPickDao.selectRecPickList(existingMap);
            if (insertedList == null || insertedList.isEmpty()) throw new IllegalStateException("추천 저장 이력 조회 실패.");
            pick = insertedList.get(0);
        }

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("pickId",          pick.getPickId());
        result.put("watchGroupId",    pick.getWatchGroupId());
        result.put("stkCd",           pick.getStkCd());
        result.put("pickStatus",      pick.getPickStatus());
        result.put("watchAnchorPrice", pick.getWatchAnchorPrice());
        result.put("sourceBaseDt",    pick.getSourceBaseDt());
        return result;
    }

    /* ─────────────────────────────────────────────
       매수 등록 → TB_S_POSITION 생성 + RECO_LINK
    ───────────────────────────────────────────── */

    public HashMap<String, Object> registerBuy(HashMap<String, String> map) throws Exception {
        require(map, "pickId", "pickId 는 필수입니다.");

        int    qty            = RecPickUtil.isBlank(map.get("qty")) ? 1 : parsePositiveInt(map.get("qty"), "qty 는 1 이상이어야 합니다.");
        double buyPrice       = parsePositiveDouble(map.get("buyPrice"), "buyPrice 는 0보다 커야 합니다.");
        String buyDate        = RecPickUtil.isBlank(map.get("buyDate")) ? today() : RecPickUtil.normalizeDate(map.get("buyDate"));
        String entryRuleCode  = RecPickUtil.isBlank(map.get("entryRuleCode")) ? "MANUAL" : RecPickUtil.trim(map.get("entryRuleCode"));

        // PICK 이력 조회
        HashMap<String, String> pickMap = new HashMap<String, String>();
        pickMap.put("pickId", RecPickUtil.trim(map.get("pickId")));
        RecPickDto pick = recPickDao.selectRecPick(pickMap);
        if (pick == null) throw new IllegalStateException("추천 저장 이력을 찾을 수 없습니다.");
        if (pick.getPositionId() != null) throw new IllegalStateException("이미 매수 등록된 추천 이력입니다.");

        // 기존 활성 포지션 조회 (있으면 누적, 없으면 신규 생성)
        HashMap<String, String> existPosMap = new HashMap<String, String>();
        existPosMap.put("stockGroup", pick.getWatchGroupId());
        existPosMap.put("stockCode",  pick.getStkCd());
        PositionVo existingPosition = positionDao.selectPosition(existPosMap);
        boolean hasActivePosition = existingPosition != null
                && existingPosition.getPositionId() != null
                && "N".equals(existingPosition.getCloseFlag());

        PositionVo savedPosition;
        double targetPrice;
        double stopPrice;
        double tp1Price;

        if (hasActivePosition) {
            /* ── 기존 포지션 누적 (추가 매수) ── */
            int    beforeQty = existingPosition.getTotalQty()  != null ? existingPosition.getTotalQty()  : 0;
            double beforeAvg = existingPosition.getAvgPrice()  != null ? existingPosition.getAvgPrice()  : 0d;

            HashMap<String, String> avgDownMap = new HashMap<String, String>();
            avgDownMap.put("positionId", String.valueOf(existingPosition.getPositionId()));
            avgDownMap.put("addQty",     String.valueOf(qty));
            avgDownMap.put("addPrice",   String.valueOf(buyPrice));
            positionDao.averageDownPosition(avgDownMap);

            // 누적 후 재조회 (갱신된 totalQty, avgPrice 반영)
            HashMap<String, String> reloadMap = new HashMap<String, String>();
            reloadMap.put("positionId", String.valueOf(existingPosition.getPositionId()));
            savedPosition = positionDao.selectPosition(reloadMap);
            if (savedPosition == null) throw new IllegalStateException("포지션 누적 후 재조회에 실패했습니다.");

            // 거래내역 저장
            PositionTxnVo txn = new PositionTxnVo();
            txn.setPositionId(savedPosition.getPositionId());
            txn.setStockGroup(savedPosition.getStockGroup());
            txn.setStockCode(savedPosition.getStockCode());
            txn.setActionType("BUY");
            txn.setQty(Integer.valueOf(qty));
            txn.setPrice(Double.valueOf(buyPrice));
            txn.setBeforeQty(Integer.valueOf(beforeQty));
            txn.setBeforeAvg(Double.valueOf(beforeAvg));
            txn.setAfterQty(savedPosition.getTotalQty());
            txn.setAfterAvg(savedPosition.getAvgPrice());
            txn.setReasonText("추천 종목 추가 매수");
            txn.setCreateUser(RecPickUtil.trim(map.get("userId")));
            int txnCnt = positionDao.insertPositionTxn(txn);
            if (isDmlFailure(txnCnt)) throw new IllegalStateException("보유종목 거래내역 생성에 실패했습니다.");

            // 기존 목표가/손절/TP1 유지 (없으면 신규 계산)
            targetPrice = existingPosition.getTargetPrice() != null
                    ? existingPosition.getTargetPrice() : RecPickUtil.round4(ruleTargetPrice(buyPrice, entryRuleCode));
            stopPrice   = existingPosition.getStopPrice()   != null
                    ? existingPosition.getStopPrice()   : RecPickUtil.round4(ruleStopPrice(buyPrice, entryRuleCode));
            tp1Price    = existingPosition.getTp1Price()    != null
                    ? existingPosition.getTp1Price()    : RecPickUtil.round4(ruleTp1Price(buyPrice, entryRuleCode));

        } else {
            /* ── 신규 포지션 생성 ── */
            PositionVo position = new PositionVo();
            position.setStockGroup(pick.getWatchGroupId());
            position.setStockCode(pick.getStkCd());
            position.setMarketCode(RecPickUtil.normalizeMarketGroup(pick.getSourceMktCd()));
            position.setTotalQty(Integer.valueOf(qty));
            position.setAvgPrice(Double.valueOf(buyPrice));
            position.setCloseFlag("N");
            position.setStateCode("HOLD");
            position.setCreateUser(RecPickUtil.trim(map.get("userId")));
            int insertCnt = positionDao.insertPosition(position);
            if (isDmlFailure(insertCnt)) throw new IllegalStateException("보유종목 생성에 실패했습니다.");

            // 저장된 포지션 재조회 (POSITION_ID 확보)
            HashMap<String, String> positionQueryMap = new HashMap<String, String>();
            positionQueryMap.put("stockGroup", pick.getWatchGroupId());
            positionQueryMap.put("stockCode",  pick.getStkCd());
            savedPosition = positionDao.selectPosition(positionQueryMap);
            if (savedPosition == null || savedPosition.getPositionId() == null) {
                throw new IllegalStateException("보유종목 생성에 실패했습니다.");
            }

            // 거래내역 저장
            PositionTxnVo txn = new PositionTxnVo();
            txn.setPositionId(savedPosition.getPositionId());
            txn.setStockGroup(savedPosition.getStockGroup());
            txn.setStockCode(savedPosition.getStockCode());
            txn.setActionType("BUY");
            txn.setQty(Integer.valueOf(qty));
            txn.setPrice(Double.valueOf(buyPrice));
            txn.setAfterQty(Integer.valueOf(qty));
            txn.setAfterAvg(Double.valueOf(buyPrice));
            txn.setReasonText("추천 종목 매수 등록");
            txn.setCreateUser(RecPickUtil.trim(map.get("userId")));
            int txnCnt = positionDao.insertPositionTxn(txn);
            if (isDmlFailure(txnCnt)) throw new IllegalStateException("보유종목 거래내역 생성에 실패했습니다.");

            // 목표가/손절가/TP1 계산 및 RECO_LINK 갱신
            targetPrice = RecPickUtil.round4(ruleTargetPrice(buyPrice, entryRuleCode));
            stopPrice   = RecPickUtil.round4(ruleStopPrice(buyPrice, entryRuleCode));
            tp1Price    = RecPickUtil.round4(ruleTp1Price(buyPrice, entryRuleCode));

            savedPosition.setSourcePickId(pick.getPickId());
            savedPosition.setBuyDate(java.sql.Date.valueOf(buyDate));
            savedPosition.setTargetPrice(Double.valueOf(targetPrice));
            savedPosition.setStopPrice(Double.valueOf(stopPrice));
            savedPosition.setTp1Price(Double.valueOf(tp1Price));
            savedPosition.setSellGuideState("HOLD");
            savedPosition.setLastSellSignalCode("NONE");
            savedPosition.setLastTrackDate(java.sql.Date.valueOf(buyDate));
            positionDao.updatePositionRecoLink(savedPosition);
        }

        // PICK 상태 갱신 (신규/누적 공통)
        RecPickDto updatePick = new RecPickDto();
        updatePick.setPickId(pick.getPickId());
        updatePick.setPickStatus("BOUGHT");
        updatePick.setFirstBuyDate(buyDate);
        updatePick.setFirstBuyPrice(Double.valueOf(buyPrice));
        updatePick.setFirstBuyQty(Integer.valueOf(qty));
        updatePick.setPositionId(savedPosition.getPositionId());
        updatePick.setEntryRuleCode(entryRuleCode);
        recPickDao.updateRecPickBuyLink(updatePick);

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("pickId",      pick.getPickId());
        result.put("positionId",  savedPosition.getPositionId());
        result.put("pickStatus",  "BOUGHT");
        result.put("buyPrice",    Double.valueOf(buyPrice));
        result.put("targetPrice", Double.valueOf(targetPrice));
        result.put("stopPrice",   Double.valueOf(stopPrice));
        result.put("tp1Price",    Double.valueOf(tp1Price));
        return result;
    }

    /* ─────────────────────────────────────────────
       조회
    ───────────────────────────────────────────── */

    public List<RecPickDto> selectRecPickList(HashMap<String, String> map) throws Exception {
        List<RecPickDto> list = recPickDao.selectRecPickList(map);
        return list == null ? new ArrayList<RecPickDto>() : list;
    }

    public RecPickDto selectRecPickDetail(HashMap<String, String> map) throws Exception {
        return recPickDao.selectRecPick(map);
    }

    public HashMap<String, Object> deleteRecPick(HashMap<String, String> map) throws Exception {
        require(map, "pickId", "pickId 는 필수입니다.");

        HashMap<String, String> pickMap = new HashMap<String, String>();
        pickMap.put("pickId", RecPickUtil.trim(map.get("pickId")));
        RecPickDto pick = recPickDao.selectRecPick(pickMap);
        if (pick == null) throw new IllegalStateException("추천 저장 이력을 찾을 수 없습니다.");
        if (pick.getPositionId() != null || "BOUGHT".equalsIgnoreCase(pick.getPickStatus())) {
            throw new IllegalStateException("매수 등록된 추천 이력은 삭제할 수 없습니다.");
        }

        int deleteCnt = recPickDao.deleteRecPick(pickMap);
        if (isDmlFailure(deleteCnt)) throw new IllegalStateException("추천 저장 이력 삭제에 실패했습니다.");

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("pickId", pick.getPickId());
        result.put("stkCd", pick.getStkCd());
        return result;
    }

    /* ─────────────────────────────────────────────
       진입 규칙별 가격 계산
    ───────────────────────────────────────────── */

    private double ruleTargetPrice(double buyPrice, String rule) {
        if ("BREAKOUT_20".equalsIgnoreCase(rule))  return buyPrice * TARGET_BREAKOUT;
        if ("PULLBACK_MA20".equalsIgnoreCase(rule)) return buyPrice * TARGET_PULLBACK;
        return buyPrice * TARGET_DEFAULT;
    }

    private double ruleStopPrice(double buyPrice, String rule) {
        if ("BREAKOUT_20".equalsIgnoreCase(rule))  return buyPrice * STOP_BREAKOUT;
        if ("PULLBACK_MA20".equalsIgnoreCase(rule)) return buyPrice * STOP_PULLBACK;
        return buyPrice * STOP_DEFAULT;
    }

    private double ruleTp1Price(double buyPrice, String rule) {
        if ("BREAKOUT_20".equalsIgnoreCase(rule))  return buyPrice * TP1_BREAKOUT;
        if ("PULLBACK_MA20".equalsIgnoreCase(rule)) return buyPrice * TP1_PULLBACK;
        return buyPrice * TP1_DEFAULT;
    }

    /* ─────────────────────────────────────────────
       파라미터 검증 헬퍼
    ───────────────────────────────────────────── */

    private void require(HashMap<String, String> map, String key, String message) {
        if (map == null || RecPickUtil.isBlank(map.get(key))) throw new IllegalArgumentException(message);
    }

    private int parsePositiveInt(String value, String message) {
        try {
            int v = Integer.parseInt(RecPickUtil.trim(value));
            if (v <= 0) throw new IllegalArgumentException(message);
            return v;
        } catch (NumberFormatException e) { throw new IllegalArgumentException(message); }
    }

    private double parsePositiveDouble(String value, String message) {
        try {
            double v = Double.parseDouble(RecPickUtil.trim(value));
            if (v <= 0d) throw new IllegalArgumentException(message);
            return v;
        } catch (NumberFormatException e) { throw new IllegalArgumentException(message); }
    }

    /**
     * 전역 MyBatis executor 가 BATCH 라서 flush 전 insert/update 결과는 음수 sentinel 이다.
     * 즉시 실패는 0 건인 경우만 판단하고, 실제 반영 여부는 후속 재조회로 검증한다.
     */
    private boolean isDmlFailure(int affectedRows) {
        return affectedRows == 0;
    }

    private String tradeDateOrToday() {
        return tradeDateService == null
            ? java.time.LocalDate.now().toString()
            : tradeDateService.resolveToday();
    }

    private String today() {
        return java.time.LocalDate.now().toString();
    }
}
