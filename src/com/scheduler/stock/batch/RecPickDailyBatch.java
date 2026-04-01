package com.scheduler.stock.batch;

import java.util.HashMap;

import com.scheduler.stock.service.RecPickTrackService;

public class RecPickDailyBatch {

    private RecPickTrackService recPickTrackService;

    public void setRecPickTrackService(RecPickTrackService recPickTrackService) {
        this.recPickTrackService = recPickTrackService;
    }

    public HashMap<String, Object> run(HashMap<String, String> map) throws Exception {
        return recPickTrackService.runDailyTrack(map);
    }
}
