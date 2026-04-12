package com.scheduler.stock.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.stock.dao.BatchExecItemLogDao;
import com.scheduler.stock.dto.RecSignalDto;

public class BatchExecItemLogDaoImpl extends SqlSessionDaoSupport implements BatchExecItemLogDao {

    private static final String NS = "com.scheduler.stock.sql.BatchExecItemLog.";

    @Override
    public int insertBatchExecItemLog(HashMap<String, Object> map) throws Exception {
        return getSqlSession().insert(NS + "insertBatchExecItemLog", map);
    }

    @Override
    public List<RecSignalDto> selectRetryStockList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectRetryStockList", map);
    }
}
