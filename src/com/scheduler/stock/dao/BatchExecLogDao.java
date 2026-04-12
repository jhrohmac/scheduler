package com.scheduler.stock.dao;

import java.util.HashMap;

public interface BatchExecLogDao {

    int insertBatchExecLog(HashMap<String, Object> map) throws Exception;

    int updateBatchExecLog(HashMap<String, Object> map) throws Exception;

    HashMap<String, Object> selectLatestBatchExecLog(HashMap<String, String> map) throws Exception;
}
