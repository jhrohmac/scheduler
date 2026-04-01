package com.scheduler.stock.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.stock.dao.StkMasterDao;
import com.scheduler.stock.dto.RecSignalDto;

public class StkMasterDaoImpl extends SqlSessionDaoSupport implements StkMasterDao {

    private static final String NS = "com.scheduler.stock.sql.StkMaster.";

    @Override
    public List<RecSignalDto> selectStkMasterList(HashMap<String, String> map) throws Exception {
        return getSqlSession().selectList(NS + "selectStkMasterList", map);
    }
}
