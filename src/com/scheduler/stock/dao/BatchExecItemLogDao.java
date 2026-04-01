package com.scheduler.stock.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.stock.dto.RecSignalDto;

public interface BatchExecItemLogDao {

    int insertBatchExecItemLog(HashMap<String, Object> map) throws Exception;

    List<RecSignalDto> selectRetryStockList(HashMap<String, String> map) throws Exception;
}
