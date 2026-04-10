package com.scheduler.management.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.management.dao.UserManagementDao;
import com.scheduler.management.vo.UserVo;

public class UserManageDaoImpl extends SqlSessionDaoSupport implements UserManagementDao {
	
	private static final Logger logger = LoggerFactory.getLogger(UserManageDaoImpl.class);
	private static final String  NS ="sql.UserManage.";
	
    @Override
	public List<UserVo> selectUserAll(HashMap<String, String> param) throws Exception {
    	List<UserVo> userList = null;
		try{
			userList = getSqlSession().selectList(NS+"selectUserAll",param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		return userList;
    }
    
    @Override
	public Object selectUserInfo(HashMap<String, String> param) throws Exception {
    	UserVo userVo = null;
		try{
			userVo = getSqlSession().selectOne(NS+"selectUserInfo",param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		return userVo;
    }
    
    @Override
    public List<?> userStartPageList(HashMap<String, String> param) throws Exception {
    	List<ItemCodeVo> list = null;
    	try{
    		list = getSqlSession().selectList(NS+"userStartPageList",param);
    	}catch(Exception e){
    		e.printStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	
    	return list;
    }
    
    @Override
	public int userEvent(HashMap<String, String> param) throws Exception {
    	int result = 0;
		try{

	        System.out.println(param);
			if(param.get("in_eventDiv").equals("I") ){
				result = getSqlSession().insert(NS+"insertUser",param);
			}else{
				result = getSqlSession().insert(NS+"updateUser",param);
			}
			System.out.println(param);
			getSqlSession().insert(NS+"insertUserHistory",param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		
		return result;
    }

	@Override
	public int delete(List<String> param, String userName) throws Exception {
    	int result = 0;
		try{
			result = getSqlSession().delete(NS+"deleteUser", param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		
		return result;
	}
    
    @Override
    public int updateExtensionDate(HashMap<String, String> param) throws Exception {
    	int result = 0;
    	try{
    		result = getSqlSession().insert(NS+"updateExtensionDate",param);
    	}catch(Exception e){
    		e.printStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	return result;
    }

    
    @Override
	public int updatePw(HashMap<String, String> param) throws Exception {
    	int tot_cnt = 0;
    	try{
				tot_cnt = getSqlSession().update(NS+"updatePw",param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
    	
    	return tot_cnt;
    }
}