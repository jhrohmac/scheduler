package com.scheduler.stock.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.stock.dto.RecSignalDto;

public interface StkMasterDao {

    List<RecSignalDto> selectStkMasterList(HashMap<String, String> map) throws Exception;
}
