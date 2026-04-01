package com.scheduler.stock.batchadmin.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.stock.batchadmin.dao.StockBatchAdminDao;

public class StockBatchAdminDaoImpl extends SqlSessionDaoSupport implements StockBatchAdminDao {

    private static final String NS = "com.scheduler.stock.sql.StockBatchAdmin.";

    @Override
    public List<HashMap<String, Object>> selectTaskCatalog(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectTaskCatalog", map);
    }

    @Override
    public List<HashMap<String, Object>> selectJobList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectJobList", map);
    }

    @Override
    public HashMap<String, Object> selectJobOne(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectJobOne", map);
    }

    @Override
    public HashMap<String, Object> selectJobSchedule(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectJobSchedule", map);
    }

    @Override
    public List<HashMap<String, Object>> selectJobParams(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectJobParams", map);
    }

    @Override
    public List<HashMap<String, Object>> selectJobLogList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectJobLogList", map);
    }

    @Override
    public List<HashMap<String, Object>> selectJobLogItemFailList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectJobLogItemFailList", map);
    }

    @Override
    public int insertJobDef(HashMap<String, String> map) throws Exception {
        return getSqlSession().insert(NS + "insertJobDef", map);
    }

    @Override
    public int updateJobDef(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "updateJobDef", map);
    }

    @Override
    public int deleteJobDef(HashMap<String, String> map) throws Exception {
        return getSqlSession().delete(NS + "deleteJobDef", map);
    }

    @Override
    public int upsertJobSchedule(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "upsertJobSchedule", map);
    }

    @Override
    public int deleteJobParams(HashMap<String, String> map) throws Exception {
        return getSqlSession().delete(NS + "deleteJobParams", map);
    }

    @Override
    public int insertJobParam(HashMap<String, String> map) throws Exception {
        return getSqlSession().insert(NS + "insertJobParam", map);
    }

    @Override
    public int ensureJobRuntime(HashMap<String, String> map) throws Exception {
        return getSqlSession().insert(NS + "ensureJobRuntime", map);
    }

    @Override
    public int tryAcquireRuntimeLock(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "tryAcquireRuntimeLock", map);
    }

    @Override
    public int heartbeatRuntime(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "heartbeatRuntime", map);
    }

    @Override
    public int releaseRuntime(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "releaseRuntime", map);
    }

    @Override
    public int requestStop(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "requestStop", map);
    }

    @Override
    public String selectStopRequestYn(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectStopRequestYn", map);
    }

    @Override
    public int clearStopRequest(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "clearStopRequest", map);
    }

    @Override
    public int updateScheduleNextRun(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "updateScheduleNextRun", map);
    }

    @Override
    public List<HashMap<String, Object>> selectDueJobs(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectDueJobs", map);
    }

    @Override
    public List<String> selectStaleJobIds(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectStaleJobIds", map);
    }

    @Override
    public int clearStaleBatchExecLogs(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "clearStaleBatchExecLogs", map);
    }

    @Override
    public int clearStaleRuntimeLocks(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "clearStaleRuntimeLocks", map);
    }

    @Override
    public int markJobStart(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "markJobStart", map);
    }

    @Override
    public int markJobFinish(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "markJobFinish", map);
    }

    @Override
    public HashMap<String, Object> selectTaskDef(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectTaskDef", map);
    }
}
