package com.scheduler.stock.batchadmin.task;

import java.util.HashMap;
import java.util.Map;

import com.scheduler.stock.batch.RecPickDailyBatch;

/**
 * 추천 종목 일별 추적 배치 Task
 *
 * TB_STK_BATCH_TASK_DEF 에 등록할 bean_name : recPickRunTask
 * task_key : REC_PICK_DAILY_TRACK
 *
 * 기본 파라미터
 *   mktCd          : KR | US (생략 시 전체)
 *   forcePickId    : 특정 pickId 1건만 실행 (테스트용)
 */
public class RecPickRunTask implements StockBatchTask {

    private RecPickDailyBatch recPickDailyBatch;

    public void setRecPickDailyBatch(RecPickDailyBatch recPickDailyBatch) {
        this.recPickDailyBatch = recPickDailyBatch;
    }

    @Override
    public Map<String, Object> execute(HashMap<String, Object> jobDef,
                                       HashMap<String, String> params,
                                       StopSignal stopSignal) throws Exception {

        if (recPickDailyBatch == null) {
            throw new IllegalStateException("recPickDailyBatch bean 이 설정되지 않았습니다.");
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            throw new RuntimeException("STOP REQUESTED");
        }

        HashMap<String, String> runMap = new HashMap<String, String>();
        if (params != null) {
            runMap.putAll(params);
        }

        /* 기본값 세팅 */
        if (isBlank(runMap.get("mktCd")) && isBlank(runMap.get("forcePickId"))) {
            // mktCd 미지정 시 전체 대상 처리 (selectTrackTargetPickList 조건 없음)
        }

        HashMap<String, Object> result = recPickDailyBatch.run(runMap);
        if (result == null) {
            result = new HashMap<String, Object>();
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            result.put("status", "STOP");
            result.put("message", "STOP REQUESTED");
            return result;
        }

        int failedCnt = toInt(result.get("failedPickCnt"));
        int targetCnt = toInt(result.get("targetPickCnt"));

        result.put("status", failedCnt == 0 ? "SUCCESS" : (failedCnt < targetCnt ? "PARTIAL" : "FAIL"));

        // 실패 시 lastError 를 message 에 포함 → buildResultMessage() 에서 UI 에 표시됨
        if (failedCnt > 0) {
            String lastErr = result.containsKey("lastError") ? String.valueOf(result.get("lastError")) : "";
            if (!isBlank(lastErr)) {
                result.put("message", "fail=" + failedCnt + "/" + targetCnt + " | " + lastErr);
            }
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
