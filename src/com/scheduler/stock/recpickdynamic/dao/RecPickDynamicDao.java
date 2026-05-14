package com.scheduler.stock.recpickdynamic.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.stock.dto.RecSignalDto;

public interface RecPickDynamicDao {

    /** 동적 WHERE 조각을 받아 1차 후보 종목 조회 (TB_REC_SIGNAL 최신 BASE_DT) */
    List<RecSignalDto> selectDynamicCandidates(HashMap<String, Object> map) throws Exception;

    /** 최신 BASE_DT */
    String selectLatestBaseDt(HashMap<String, Object> map) throws Exception;

    /** 종목 상세 */
    RecSignalDto selectStockDetail(HashMap<String, Object> map) throws Exception;
}
