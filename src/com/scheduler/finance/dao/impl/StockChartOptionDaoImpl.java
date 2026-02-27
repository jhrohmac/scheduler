package com.scheduler.finance.dao.impl;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.finance.dao.StockChartOptionDao;
import com.scheduler.finance.vo.StockChartOptionVo;

public class StockChartOptionDaoImpl extends SqlSessionDaoSupport implements StockChartOptionDao {

    private static final String NAMESPACE = "sql.StockChartOptionManage.";

    @Override
    public List<StockChartOptionVo> selectChartOptionList(Map<String, String> paramMap) {
        return getSqlSession().selectList(NAMESPACE + "selectChartOptionList", paramMap);
    }

    @Override
    public int updateChartOption(StockChartOptionVo vo) {
        return getSqlSession().update(NAMESPACE + "updateChartOption", vo);
    }

    @Override
    public int insertChartOption(StockChartOptionVo vo) {
        return getSqlSession().insert(NAMESPACE + "insertChartOption", vo);
    }

    @Override
    public int deleteChartOptions(Map<String, String> paramMap) {
        return getSqlSession().delete(NAMESPACE + "deleteChartOptions", paramMap);
    }
}
