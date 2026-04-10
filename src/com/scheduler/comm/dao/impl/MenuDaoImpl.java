package com.scheduler.comm.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.comm.dao.MenuDao;
import com.scheduler.comm.util.DataSet;
import com.scheduler.comm.util.Parameter;
import com.scheduler.comm.vo.MenuVo;

public class MenuDaoImpl extends SqlSessionDaoSupport implements MenuDao {
	
	private static final Logger logger = LoggerFactory.getLogger(MenuDaoImpl.class);
	
	@Override
	public DataSet login(Parameter param) throws Exception{
		return null;
	}

    public List<MenuVo> selectSetMenu(HashMap<String, String> param) throws Exception {
        List<MenuVo> topMenuInfo = null;
        try {										  
            topMenuInfo = getSqlSession().selectList("sql.MenuInfo.selectSetMenu", param);
        }
        catch (Exception e) {
			e.getLocalizedMessage();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return topMenuInfo;
    }	
    
    @Override
   	public List<MenuVo> selectPageMove(HashMap<String, String> param) throws Exception {
       	
       	List<MenuVo> pageInfo = null;
   		try{
   			pageInfo = getSqlSession().selectList("sql.MenuInfo.selectPageInfo", param);
   		}catch(Exception e){
   			e.getStackTrace();
   			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
   		}
   		return pageInfo;		
   }
    
    @Override
    public Object selectPageInfo(HashMap<String, String> param) throws Exception {
    	
    	MenuVo pageInfo = null;
    	try{
    		pageInfo = getSqlSession().selectOne("sql.MenuInfo.selectPageInfo", param);
    	}catch(Exception e){
    		e.getStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	
    	return pageInfo;		
    }
}
