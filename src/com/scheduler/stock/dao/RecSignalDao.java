package com.scheduler.stock.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.stock.dto.RecSignalDto;

public interface RecSignalDao {

    int mergeRecSignal(RecSignalDto dto) throws Exception;

    int deleteRecSignalByBaseDtAndMarket(HashMap<String, String> map) throws Exception;

    int deleteRecSignalByMarket(HashMap<String, String> map) throws Exception;

    List<RecSignalDto> selectRecSignalList(HashMap<String, String> map) throws Exception;

    RecSignalDto selectRecSignalDetail(HashMap<String, String> map) throws Exception;

    String selectLatestBaseDt() throws Exception;

    String selectLatestBaseDtByMarket(HashMap<String, String> map) throws Exception;
}
