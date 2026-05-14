package com.scheduler.finance.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MarketSummaryDao {

    List<?> selectWatchlistGroups(HashMap<String, String> map) throws Exception;

    List<?> selectWatchlistItems(HashMap<String, String> map) throws Exception;
    
    List<?> selectWatchlistItemsRealtime(HashMap<String, String> map) throws Exception;

    Map<String, Object> selectMarketSummary(HashMap<String, String> map) throws Exception;
    
    int insertWatchlistItem(HashMap<String, String> map) throws Exception;

    int deleteWatchlistItem(HashMap<String, String> map) throws Exception;

    int selectWatchlistItemCount(HashMap<String, String> map) throws Exception;

    List<?> selectMarketRegimeSummary() throws Exception;
}
