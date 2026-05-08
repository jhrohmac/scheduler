package com.scheduler.stock.recpickdynamic.dao.impl;

import java.util.HashMap;

import org.mybatis.spring.SqlSessionTemplate;

import com.scheduler.stock.recpickdynamic.dao.UserFilterPresetDao;
import com.scheduler.stock.recpickdynamic.vo.UserFilterPresetVo;

public class UserFilterPresetDaoImpl implements UserFilterPresetDao {

    private static final String NAMESPACE = "com.scheduler.stock.sql.UserFilterPreset.";
    private SqlSessionTemplate sqlSessionTemplate;

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public UserFilterPresetVo selectPreset(HashMap<String, Object> map) throws Exception {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectPreset", map);
    }

    public int mergePreset(HashMap<String, Object> map) throws Exception {
        return sqlSessionTemplate.update(NAMESPACE + "mergePreset", map);
    }

    public int deletePreset(HashMap<String, Object> map) throws Exception {
        return sqlSessionTemplate.delete(NAMESPACE + "deletePreset", map);
    }
}
