package com.scheduler.finance.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.finance.dao.StkSearchDao;

public class StkSearchDaoImpl extends SqlSessionDaoSupport implements StkSearchDao {

    @Override
    public List<?> selectStkSearchList(HashMap<String, String> map) throws Exception {
        try {
            return getSqlSession().selectList("com.scheduler.finance.sql.StkSearch.selectStkSearchList", map);
        } catch (Exception e) {
            throw new UserTransactionException(this.getClass().getName() + ".selectStkSearchList() : ", e.getLocalizedMessage());
        }
    }
}
