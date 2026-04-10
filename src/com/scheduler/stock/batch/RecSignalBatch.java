package com.scheduler.stock.batch;

import java.util.HashMap;

import com.scheduler.stock.service.RecSignalService;

public class RecSignalBatch {

    private RecSignalService recSignalService;

    public void setRecSignalService(RecSignalService recSignalService) {
        this.recSignalService = recSignalService;
    }

    public HashMap<String, Object> run(HashMap<String, String> map) throws Exception {
        return recSignalService.runBatch(map);
    }
}
