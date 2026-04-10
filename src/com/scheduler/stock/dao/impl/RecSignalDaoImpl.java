package com.scheduler.stock.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.stock.dao.RecSignalDao;
import com.scheduler.stock.dto.RecSignalDto;

public class RecSignalDaoImpl extends SqlSessionDaoSupport implements RecSignalDao {

    private static final String NS = "com.scheduler.stock.sql.RecSignal.";

    @Override
    public int mergeRecSignal(RecSignalDto dto) throws Exception {
        return getSqlSession().update(NS + "mergeRecSignal", dto);
    }

    @Override
    public int deleteRecSignalByBaseDtAndMarket(HashMap<String, String> map) throws Exception {
        return getSqlSession().delete(NS + "deleteRecSignalByBaseDtAndMarket", map);
    }

    @Override
    public int deleteRecSignalByMarket(HashMap<String, String> map) throws Exception {
        return getSqlSession().delete(NS + "deleteRecSignalByMarket", map);
    }

    @Override
    public int truncateRecSignalPartition(HashMap<String, String> map) throws Exception {
        return getSqlSession().update(NS + "truncateRecSignalPartition", map);
    }

    @Override
    public List<RecSignalDto> selectRecSignalList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectRecSignalList", map);
    }

    @Override
    public RecSignalDto selectRecSignalDetail(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectRecSignalDetail", map);
    }

    @Override
    public String selectLatestBaseDt() throws Exception {
        return getSqlSession().selectOne(NS + "selectLatestBaseDt");
    }

    @Override
    public String selectLatestBaseDtByMarket(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectOne(NS + "selectLatestBaseDtByMarket", map);
    }

    @Override
    public String selectRecSignalPartitionedYn() throws Exception {
        return getSqlSession().selectOne(NS + "selectRecSignalPartitionedYn");
    }
}
