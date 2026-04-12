package com.scheduler.stock.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.stock.dto.RecSignalDto;
import com.scheduler.stock.dto.StkMasterDto;

public interface StkMasterDao {

    /** RecSignal 배치용 — USE_YN='Y' AND STK_STATUS='NORMAL' 종목 목록 */
    List<RecSignalDto> selectStkMasterList(HashMap<String, String> map) throws Exception;

    /** 특정 시장의 활성 종목코드 목록 (DELIST 제외) — 상장폐지 감지용 */
    List<String> selectActiveStkCdListByMktCd(HashMap<String, String> map) throws Exception;

    /** 종목 MERGE (신규 INSERT / 기존 UPDATE) */
    int mergeStkMaster(StkMasterDto dto) throws Exception;

    /** 특정 종목을 상장폐지 처리 (STK_STATUS=DELIST, USE_YN=N, DELIST_DT=SYSDATE) */
    int updateStkMasterDelist(HashMap<String, String> map) throws Exception;
}
