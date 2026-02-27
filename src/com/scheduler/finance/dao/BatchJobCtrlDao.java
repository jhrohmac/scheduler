package com.scheduler.finance.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scheduler.finance.vo.BatchJobCtrlVo;

public interface BatchJobCtrlDao {

    List<BatchJobCtrlVo> selectJobList(HashMap<String, String> map) throws Exception;

    BatchJobCtrlVo selectJobOne(HashMap<String, String> map) throws Exception;

    int insertJobCtrl(HashMap<String, String> map) throws Exception;

    int updateJobCtrl(HashMap<String, String> map) throws Exception;

    int deleteJobCtrl(HashMap<String, String> map) throws Exception;

    Map<String, Object> runJobNow(HashMap<String, String> map) throws Exception;

    Map<String, Object> schedulerTick(HashMap<String, String> map) throws Exception;
    
    Map<String, Object> stopJob(HashMap<String, String> map) throws Exception;
}
