package com.scheduler.stock.batch;

import java.util.HashMap;

public class RecSignalDailyBatch {

    private static final String DEFAULT_FETCH_DAYS = "400";
    private static final String PRIMARY_INTERVAL_MS = "1000";
    private static final String RETRY_INTERVAL_MS = "2000";

    private RecSignalBatch recSignalBatch;

    public void setRecSignalBatch(RecSignalBatch recSignalBatch) {
        this.recSignalBatch = recSignalBatch;
    }

    public HashMap<String, Object> run(HashMap<String, String> map) throws Exception {
        HashMap<String, String> batchMap = new HashMap<String, String>();
        if (map != null) {
            batchMap.putAll(map);
        }
        if (isBlank(batchMap.get("days"))) {
            batchMap.put("days", DEFAULT_FETCH_DAYS);
        }
        if (isBlank(batchMap.get("marketGroup"))) {
            batchMap.put("marketGroup", "KR");
        }
        if (isBlank(batchMap.get("requestIntervalMs"))) {
            batchMap.put("requestIntervalMs", isRetry(batchMap) ? RETRY_INTERVAL_MS : PRIMARY_INTERVAL_MS);
        }
        return recSignalBatch.run(batchMap);
    }

    public HashMap<String, Object> runToday() throws Exception {
        return run(new HashMap<String, String>());
    }

    public HashMap<String, Object> runScheduled(String marketGroup, boolean retryOnly, String baseDt) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("marketGroup", marketGroup);
        map.put("retryOnly", retryOnly ? "Y" : "N");
        map.put("baseDt", baseDt);
        map.put("days", DEFAULT_FETCH_DAYS);
        map.put("requestIntervalMs", retryOnly ? RETRY_INTERVAL_MS : PRIMARY_INTERVAL_MS);
        return run(map);
    }

    private boolean isRetry(HashMap<String, String> map) {
        return map != null && "Y".equalsIgnoreCase(map.get("retryOnly"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
