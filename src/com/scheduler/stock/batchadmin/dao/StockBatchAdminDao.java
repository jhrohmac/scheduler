package com.scheduler.stock.batchadmin.dao;

import java.util.HashMap;
import java.util.List;

public interface StockBatchAdminDao {

    List<HashMap<String, Object>> selectTaskCatalog(HashMap<String, String> map) throws Exception;

    List<HashMap<String, Object>> selectJobList(HashMap<String, String> map) throws Exception;

    HashMap<String, Object> selectJobOne(HashMap<String, String> map) throws Exception;

    HashMap<String, Object> selectJobSchedule(HashMap<String, String> map) throws Exception;

    List<HashMap<String, Object>> selectJobParams(HashMap<String, String> map) throws Exception;

    List<HashMap<String, Object>> selectJobLogList(HashMap<String, String> map) throws Exception;

    List<HashMap<String, Object>> selectJobLogItemFailList(HashMap<String, String> map) throws Exception;

    int insertJobDef(HashMap<String, String> map) throws Exception;

    int updateJobDef(HashMap<String, String> map) throws Exception;

    int deleteJobDef(HashMap<String, String> map) throws Exception;

    int upsertJobSchedule(HashMap<String, String> map) throws Exception;

    int deleteJobParams(HashMap<String, String> map) throws Exception;

    int insertJobParam(HashMap<String, String> map) throws Exception;

    int ensureJobRuntime(HashMap<String, String> map) throws Exception;

    int tryAcquireRuntimeLock(HashMap<String, String> map) throws Exception;

    int heartbeatRuntime(HashMap<String, String> map) throws Exception;

    int releaseRuntime(HashMap<String, String> map) throws Exception;

    int requestStop(HashMap<String, String> map) throws Exception;

    String selectStopRequestYn(HashMap<String, String> map) throws Exception;

    int clearStopRequest(HashMap<String, String> map) throws Exception;

    int updateScheduleNextRun(HashMap<String, String> map) throws Exception;

    List<HashMap<String, Object>> selectDueJobs(HashMap<String, String> map) throws Exception;

    List<String> selectStaleJobIds(HashMap<String, String> map) throws Exception;

    int clearStaleBatchExecLogs(HashMap<String, String> map) throws Exception;

    int clearStaleRuntimeLocks(HashMap<String, String> map) throws Exception;

    int fixOrphanRunningStatus(HashMap<String, String> map) throws Exception;

    int markJobStart(HashMap<String, String> map) throws Exception;

    int markJobFinish(HashMap<String, String> map) throws Exception;

    int purgeOldBatchExecLogs(HashMap<String, String> map) throws Exception;

    HashMap<String, Object> selectTaskDef(HashMap<String, String> map) throws Exception;
}
