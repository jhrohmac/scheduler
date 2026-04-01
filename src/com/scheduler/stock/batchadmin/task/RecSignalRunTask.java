package com.scheduler.stock.batchadmin.task;

import java.util.HashMap;
import java.util.Map;

import com.scheduler.stock.batch.RecSignalDailyBatch;

public class RecSignalRunTask implements StockBatchTask {

    private RecSignalDailyBatch recSignalDailyBatch;

    public void setRecSignalDailyBatch(RecSignalDailyBatch recSignalDailyBatch) {
        this.recSignalDailyBatch = recSignalDailyBatch;
    }

    @Override
    public Map<String, Object> execute(HashMap<String, Object> jobDef,
                                       HashMap<String, String> params,
                                       StopSignal stopSignal) throws Exception {

        if (recSignalDailyBatch == null) {
            throw new IllegalStateException("recSignalDailyBatch is not configured");
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            throw new RuntimeException("STOP REQUESTED");
        }

        HashMap<String, String> runMap = new HashMap<String, String>();
        if (params != null) {
            runMap.putAll(params);
        }

        // Keep defaults aligned with current RecSignal batch behavior.
        if (isBlank(runMap.get("marketGroup"))) runMap.put("marketGroup", "KR");
        if (isBlank(runMap.get("days"))) runMap.put("days", "400");
        if (isBlank(runMap.get("requestIntervalMs"))) {
            String retryOnly = runMap.get("retryOnly");
            runMap.put("requestIntervalMs", "Y".equalsIgnoreCase(retryOnly) ? "2000" : "1000");
        }

        HashMap<String, Object> result = recSignalDailyBatch.run(runMap);
        if (result == null) {
            result = new HashMap<String, Object>();
        }

        String status = toStr(result.get("status"));
        if (isBlank(status)) {
            status = "SUCCESS";
            if ("0".equals(toStr(result.get("failCnt")))) {
                status = "SUCCESS";
            }
        }
        result.put("status", status);

        if (stopSignal != null && stopSignal.shouldStop()) {
            result.put("status", "STOP");
            result.put("message", "STOP REQUESTED");
        }

        return result;
    }

    private String toStr(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().length() == 0;
    }
}
