package com.scheduler.stock.dao.impl;

import java.util.HashMap;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.stock.dao.BatchExecLogDao;

public class BatchExecLogDaoImpl extends SqlSessionDaoSupport implements BatchExecLogDao {

    private static final String NS = "com.scheduler.stock.sql.BatchExecLog.";

    @Override
    public int insertBatchExecLog(HashMap<String, Object> map) throws Exception {
        return getSqlSession().insert(NS + "insertBatchExecLog", map);
    }

    @Override
    public int updateBatchExecLog(HashMap<String, Object> map) throws Exception {
        return getSqlSession().update(NS + "updateBatchExecLog", map);
    }

    @Override
    public HashMap<String, Object> selectLatestBatchExecLog(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectLatestBatchExecLog", map);
    }
}
