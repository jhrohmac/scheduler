package com.scheduler.stock.batchadmin.task;

import java.util.HashMap;
import java.util.Map;

public interface StockBatchTask {

    public interface StopSignal {
        boolean shouldStop();
    }

    Map<String, Object> execute(HashMap<String, Object> jobDef,
                                HashMap<String, String> params,
                                StopSignal stopSignal) throws Exception;
}
