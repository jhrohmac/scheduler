package com.scheduler.comm.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.comm.dao.ItemCodeDao;
import com.scheduler.comm.vo.ItemCodeVo;

public class ItemCodeDaoImpl extends SqlSessionDaoSupport implements ItemCodeDao{
	
	private static final Logger logger = LoggerFactory.getLogger(ItemCodeDaoImpl.class);
	    
    @Override
	public List<?> selectItemCode(HashMap<String, String> map) throws Exception {

    	List<ItemCodeVo> list = null;
		try{
			list = getSqlSession().selectList("sql.ItemCode.selectItemCode", map);
		}catch(Exception e){
			e.getMessage();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		
		return list;
    }
}