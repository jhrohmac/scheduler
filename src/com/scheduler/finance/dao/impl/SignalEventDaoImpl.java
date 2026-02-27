package com.scheduler.finance.dao.impl;

import java.util.HashMap;

import org.mybatis.spring.SqlSessionTemplate;

import com.scheduler.finance.dao.SignalEventDao;

public class SignalEventDaoImpl implements SignalEventDao {

    private static final String NS = "com.scheduler.finance.sql.SignalEvent.";

    private SqlSessionTemplate sqlSessionTemplate;

    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @Override
    public int insertSignalEvent(HashMap<String, String> map) throws Exception {
        return sqlSessionTemplate.insert(NS + "insertSignalEvent", map);
    }
}
