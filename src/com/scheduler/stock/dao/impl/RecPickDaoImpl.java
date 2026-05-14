package com.scheduler.stock.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.stock.dao.RecPickDao;
import com.scheduler.stock.dto.RecPickDailyDto;
import com.scheduler.stock.dto.RecPickDto;
import com.scheduler.stock.dto.RecPickEvalDto;
import com.scheduler.stock.dto.RecProbabilityDto;

public class RecPickDaoImpl extends SqlSessionDaoSupport implements RecPickDao {

    private static final String NS = "com.scheduler.stock.sql.RecPick.";

    @Override
    public int insertRecPick(RecPickDto dto) throws Exception {
        return getSqlSession().insert(NS + "insertRecPick", dto);
    }

    @Override
    public int updateRecPickBuyLink(RecPickDto dto) throws Exception {
        return getSqlSession().update(NS + "updateRecPickBuyLink", dto);
    }

    @Override
    public int deleteRecPick(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "deleteRecPick", map);
    }

    @Override
    public int updateRecPickTrackDate(HashMap<String, Object> map) throws Exception {
        return getSqlSession().update(NS + "updateRecPickTrackDate", map);
    }

    @Override
    public int mergeRecPickDaily(RecPickDailyDto dto) throws Exception {
        return getSqlSession().update(NS + "mergeRecPickDaily", dto);
    }

    @Override
    public int mergeRecPickEval(RecPickEvalDto dto) throws Exception {
        return getSqlSession().update(NS + "mergeRecPickEval", dto);
    }

    @Override
    public RecPickDto selectRecPick(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectRecPick", map);
    }

    @Override
    public List<RecPickDto> selectRecPickList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectRecPickList", map);
    }

    @Override
    public List<RecPickDto> selectTrackTargetPickList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectTrackTargetPickList", map);
    }

    @Override
    public List<RecPickDto> selectProbabilityTargetPickList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectProbabilityTargetPickList", map);
    }

    @Override
    public List<RecPickDailyDto> selectRecPickDailyList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectRecPickDailyList", map);
    }

    @Override
    public List<RecPickEvalDto> selectRecPickEvalList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectRecPickEvalList", map);
    }

    @Override
    public List<RecProbabilityDto> selectProbabilityStats(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectProbabilityStats", map);
    }
}
