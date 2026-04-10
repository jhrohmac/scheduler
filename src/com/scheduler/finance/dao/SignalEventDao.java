package com.scheduler.finance.dao;

import java.util.HashMap;

public interface SignalEventDao {
    int insertSignalEvent(HashMap<String, String> map) throws Exception;
}
