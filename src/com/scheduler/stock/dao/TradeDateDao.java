package com.scheduler.stock.dao;

import java.util.HashMap;
import java.util.List;

public interface TradeDateDao {

    int mergeTradeDate(HashMap<String, Object> map) throws Exception;

    String selectEffectiveBaseDate(HashMap<String, String> map) throws Exception;

    String selectTradingDateByOffset(HashMap<String, String> map) throws Exception;

    Integer selectTradingDaysBetween(HashMap<String, String> map) throws Exception;

    List<String> selectTradingDateListBetween(HashMap<String, String> map) throws Exception;
}
