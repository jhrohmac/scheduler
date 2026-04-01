package com.scheduler.stock.batch;

import java.util.HashMap;

import com.scheduler.stock.service.PositionExitSignalService;

public class PositionMonitorDailyBatch {

    private PositionExitSignalService positionExitSignalService;

    public void setPositionExitSignalService(PositionExitSignalService positionExitSignalService) {
        this.positionExitSignalService = positionExitSignalService;
    }

    public HashMap<String, Object> run(HashMap<String, String> map) throws Exception {
        return positionExitSignalService.runDailySellGuide(map);
    }
}
