package com.scheduler.stock.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.stock.dao.TradeDateDao;

public class TradeDateDaoImpl extends SqlSessionDaoSupport implements TradeDateDao {

    private static final String NS = "com.scheduler.stock.sql.TradeDate.";

    @Override
    public int mergeTradeDate(HashMap<String, Object> map) throws Exception {
        return getSqlSession().update(NS + "mergeTradeDate", map);
    }

    @Override
    public String selectEffectiveBaseDate(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectEffectiveBaseDate", map);
    }

    @Override
    public String selectTradingDateByOffset(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectTradingDateByOffset", map);
    }

    @Override
    public Integer selectTradingDaysBetween(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectTradingDaysBetween", map);
    }

    @Override
    public List<String> selectTradingDateListBetween(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectTradingDateListBetween", map);
    }
}
