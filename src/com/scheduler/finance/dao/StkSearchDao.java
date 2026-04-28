package com.scheduler.finance.dao;

import java.util.HashMap;
import java.util.List;

public interface StkSearchDao {
    List<?> selectStkSearchList(HashMap<String, String> map) throws Exception;
}
