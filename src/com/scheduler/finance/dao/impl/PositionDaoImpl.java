package com.scheduler.finance.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.finance.dao.PositionDao;
import com.scheduler.finance.vo.PositionVo;
import com.scheduler.finance.vo.PositionTxnVo;
import com.scheduler.finance.vo.PositionEventVo;

/**
 * WF-2-4: 보유종목 관리 DAO 구현
 */
public class PositionDaoImpl extends SqlSessionDaoSupport implements PositionDao {
    
    private static final String NS = "com.scheduler.finance.sql.Position.";
    
    @Override
    public List<PositionVo> selectPositionList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectPositionList", map);
    }
    
    @Override
    public PositionVo selectPosition(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectPosition", map);
    }
    
    @Override
    public int insertPosition(PositionVo vo) throws Exception {
        return getSqlSession().insert(NS + "insertPosition", vo);
    }
    
    @Override
    public int updatePosition(PositionVo vo) throws Exception {
        return getSqlSession().update(NS + "updatePosition", vo);
    }
    
    @Override
    public int deletePosition(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "deletePosition", map);
    }
    
    @Override
    public int averageDownPosition(HashMap<String, String> map) throws Exception {
        // 물타기 로직: 기존 평단/수량 조회 → 새 평단 계산 → 업데이트 + 거래내역 기록
        // 실제 구현은 XML에서 처리하거나, 여기서 직접 계산 후 updatePosition 호출
        return getSqlSession().update(NS + "averageDownPosition", map);
    }
    
    @Override
    public int insertPositionTxn(PositionTxnVo vo) throws Exception {
        return getSqlSession().insert(NS + "insertPositionTxn", vo);
    }
    
    @Override
    public List<PositionTxnVo> selectPositionTxnList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectPositionTxnList", map);
    }
    
    @Override
    public int insertPositionEvent(PositionEventVo vo) throws Exception {
        return getSqlSession().insert(NS + "insertPositionEvent", vo);
    }
    
    @Override
    public List<PositionEventVo> selectPositionEventList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectPositionEventList", map);
    }
}
