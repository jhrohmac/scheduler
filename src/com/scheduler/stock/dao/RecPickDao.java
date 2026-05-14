package com.scheduler.stock.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.stock.dto.RecPickDailyDto;
import com.scheduler.stock.dto.RecPickDto;
import com.scheduler.stock.dto.RecPickEvalDto;
import com.scheduler.stock.dto.RecProbabilityDto;

public interface RecPickDao {

    int insertRecPick(RecPickDto dto) throws Exception;

    int updateRecPickBuyLink(RecPickDto dto) throws Exception;

    int deleteRecPick(HashMap<String, String> map) throws Exception;

    int updateRecPickTrackDate(HashMap<String, Object> map) throws Exception;

    int mergeRecPickDaily(RecPickDailyDto dto) throws Exception;

    int mergeRecPickEval(RecPickEvalDto dto) throws Exception;

    RecPickDto selectRecPick(HashMap<String, String> map) throws Exception;

    List<RecPickDto> selectRecPickList(HashMap<String, String> map) throws Exception;

    List<RecPickDto> selectTrackTargetPickList(HashMap<String, String> map) throws Exception;

    List<RecPickDto> selectProbabilityTargetPickList(HashMap<String, String> map) throws Exception;

    List<RecPickDailyDto> selectRecPickDailyList(HashMap<String, String> map) throws Exception;

    List<RecPickEvalDto> selectRecPickEvalList(HashMap<String, String> map) throws Exception;

    List<RecProbabilityDto> selectProbabilityStats(HashMap<String, String> map) throws Exception;
}
