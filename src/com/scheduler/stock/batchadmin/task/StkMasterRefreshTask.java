package com.scheduler.stock.batchadmin.task;

import java.util.HashMap;
import java.util.Map;

import com.scheduler.stock.service.StkMasterRefreshService;

/**
 * 종목 마스터 갱신 배치 Task
 *
 * TB_STK_BATCH_TASK_DEF 에 등록할 bean_name : stkMasterRefreshTask
 * task_key : STK_MASTER_REFRESH
 *
 * 파라미터
 *   marketGroup : KR | US | ALL (생략 시 ALL)
 */
public class StkMasterRefreshTask implements StockBatchTask {

    private StkMasterRefreshService stkMasterRefreshService;

    public void setStkMasterRefreshService(StkMasterRefreshService stkMasterRefreshService) {
        this.stkMasterRefreshService = stkMasterRefreshService;
    }

    @Override
    public Map<String, Object> execute(HashMap<String, Object> jobDef,
                                       HashMap<String, String> params,
                                       StopSignal stopSignal) throws Exception {

        if (stkMasterRefreshService == null) {
            throw new IllegalStateException("stkMasterRefreshService bean 이 설정되지 않았습니다.");
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            throw new RuntimeException("STOP REQUESTED");
        }

        HashMap<String, String> runMap = new HashMap<String, String>();
        if (params != null) {
            runMap.putAll(params);
        }

        HashMap<String, Object> result = stkMasterRefreshService.refresh(runMap);
        if (result == null) {
            result = new HashMap<String, Object>();
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            result.put("status", "STOP");
            result.put("message", "STOP REQUESTED");
            return result;
        }

        /* StkMasterRefreshService 가 반환한 status 를 그대로 사용 */
        if (!result.containsKey("status")) {
            result.put("status", "SUCCESS");
        }

        return result;
    }
}
