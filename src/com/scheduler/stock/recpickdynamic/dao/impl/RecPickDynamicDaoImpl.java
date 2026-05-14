package com.scheduler.stock.recpickdynamic.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;

import com.scheduler.stock.dto.RecSignalDto;
import com.scheduler.stock.recpickdynamic.dao.RecPickDynamicDao;

public class RecPickDynamicDaoImpl implements RecPickDynamicDao {

    private static final String NAMESPACE = "com.scheduler.stock.sql.RecPickDynamic.";
    private SqlSessionTemplate sqlSessionTemplate;

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public List<RecSignalDto> selectDynamicCandidates(HashMap<String, Object> map) throws Exception {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectDynamicCandidates", map);
    }

    public String selectLatestBaseDt(HashMap<String, Object> map) throws Exception {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectLatestBaseDt", map);
    }

    public RecSignalDto selectStockDetail(HashMap<String, Object> map) throws Exception {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectStockDetail", map);
    }
}
