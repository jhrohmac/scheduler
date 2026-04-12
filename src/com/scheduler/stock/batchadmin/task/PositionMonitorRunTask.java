package com.scheduler.stock.batchadmin.task;

import java.util.HashMap;
import java.util.Map;

import com.scheduler.stock.batch.PositionMonitorDailyBatch;

/**
 * 보유종목 매도 가이드 배치 Task
 *
 * TB_STK_BATCH_TASK_DEF 에 등록할 bean_name : positionMonitorRunTask
 * task_key : POSITION_MONITOR_DAILY
 *
 * 기본 파라미터
 *   mktCd          : KR | US (생략 시 전체)
 *   closeFlag      : N (생략 시 기본값 N — 보유중인 것만)
 */
public class PositionMonitorRunTask implements StockBatchTask {

    private PositionMonitorDailyBatch positionMonitorDailyBatch;

    public void setPositionMonitorDailyBatch(PositionMonitorDailyBatch positionMonitorDailyBatch) {
        this.positionMonitorDailyBatch = positionMonitorDailyBatch;
    }

    @Override
    public Map<String, Object> execute(HashMap<String, Object> jobDef,
                                       HashMap<String, String> params,
                                       StopSignal stopSignal) throws Exception {

        if (positionMonitorDailyBatch == null) {
            throw new IllegalStateException("positionMonitorDailyBatch bean 이 설정되지 않았습니다.");
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            throw new RuntimeException("STOP REQUESTED");
        }

        HashMap<String, String> runMap = new HashMap<String, String>();
        if (params != null) {
            runMap.putAll(params);
        }

        /* 기본값: 보유중인 포지션만 처리 */
        if (isBlank(runMap.get("closeFlag"))) {
            runMap.put("closeFlag", "N");
        }

        HashMap<String, Object> result = positionMonitorDailyBatch.run(runMap);
        if (result == null) {
            result = new HashMap<String, Object>();
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            result.put("status", "STOP");
            result.put("message", "STOP REQUESTED");
            return result;
        }

        String lastError = result.get("lastError") != null ? String.valueOf(result.get("lastError")) : null;
        result.put("status", isBlank(lastError) ? "SUCCESS" : "PARTIAL");

        /* 매도 신호 발생 건수를 요약 메시지로 포함 */
        int sellReady = toInt(result.get("sellReadyCnt"));
        int stopLoss  = toInt(result.get("stopLossCnt"));
        if (sellReady > 0 || stopLoss > 0) {
            result.put("alert", "SELL_READY=" + sellReady + " STOP_LOSS=" + stopLoss);
        }

        return result;
    }

    private int toInt(Object v) {
        if (v == null) return 0;
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().length() == 0;
    }
}
