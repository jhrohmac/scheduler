package com.scheduler.finance.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.finance.vo.PositionVo;
import com.scheduler.finance.vo.PositionTxnVo;
import com.scheduler.finance.vo.PositionEventVo;

/**
 * WF-2-4: 보유종목 관리 DAO
 * - 설계: _workflow/notes/30_BUSINESS_LOGIC_FLOW.md
 * - 테이블: TB_S_POSITION, TB_S_POSITION_TXN, TB_S_POSITION_EVENT
 */
public interface PositionDao {
    
    /**
     * 보유종목 목록 조회
     */
    List<PositionVo> selectPositionList(HashMap<String, String> map) throws Exception;
    
    /**
     * 특정 보유종목 상세 조회
     */
    PositionVo selectPosition(HashMap<String, String> map) throws Exception;
    
    /**
     * 보유종목 추가(ADD)
     */
    int insertPosition(PositionVo vo) throws Exception;
    
    /**
     * 보유종목 업데이트(평단/수량 갱신)
     */
    int updatePosition(PositionVo vo) throws Exception;
    
    /**
     * 보유종목 삭제(DELETE) - 실제로는 CLOSE_FLAG='Y' 처리
     */
    int deletePosition(HashMap<String, String> map) throws Exception;
    
    /**
     * 물타기(AVERAGE_DOWN) - 수량 추가 + 평단 재계산
     */
    int averageDownPosition(HashMap<String, String> map) throws Exception;
    
    /**
     * 거래 내역 기록
     */
    int insertPositionTxn(PositionTxnVo vo) throws Exception;
    
    /**
     * 거래 내역 조회
     */
    List<PositionTxnVo> selectPositionTxnList(HashMap<String, String> map) throws Exception;
    
    /**
     * 이벤트 기록
     */
    int insertPositionEvent(PositionEventVo vo) throws Exception;
    
    /**
     * 이벤트 조회
     */
    List<PositionEventVo> selectPositionEventList(HashMap<String, String> map) throws Exception;
}
