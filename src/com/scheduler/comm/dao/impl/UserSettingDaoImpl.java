package com.scheduler.comm.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.comm.dao.UserSettingDao;
import com.scheduler.comm.util.Base64Util;
import com.scheduler.comm.util.Sha256Util;
import com.scheduler.comm.vo.UserSettingVo;
import com.scheduler.management.vo.UserVo;

public class UserSettingDaoImpl extends SqlSessionDaoSupport implements UserSettingDao{
	
	private static final Logger logger = LoggerFactory.getLogger(UserSettingDaoImpl.class);
	
    private static final String NS = "sql.UserSetting.";

	
	@Override
	public List<UserSettingVo> selectDuplicateUser(HashMap<String, String> param) throws Exception {
    	List<UserSettingVo> userList = null;
    	
    	System.out.println(param);
		try{
			userList = getSqlSession().selectList(NS+"selectDuplicateUser",param);
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		return userList;
    }
	
	/*
	 * 2025 
	*/
	@Override
	public List<?> selectUserDefaultPage(HashMap<String, String> map) throws Exception {
    	List<UserSettingVo> list = null;
		try{
			list =  getSqlSession().selectList(NS+"selectUserDefaultInfo", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > selectUserDefaultPage");
		return list;
	}
	
	@Override
	public List<?> selectUserSystemCodeInfo(HashMap<String, String> map) throws Exception {
		List<UserSettingVo> list = null;
		try{
			list =  getSqlSession().selectList(NS+"selectUserSystemCodeInfo", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > selectUserSystemCodeInfo");
		return list;
	}

	@Override
	public List<?> selectUserTopMenu(HashMap<String, String> map) throws Exception {
    	List<UserSettingVo> list = null;
		try{
			list =  getSqlSession().selectList(NS+"selectUserTopMenu", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > selectUserTopMenu");
		return list;
	}

	@Override
	public List<?> selectUserSubMenu(HashMap<String, String> map) throws Exception {
    	List<UserSettingVo> list = null;
		try{
			list =  getSqlSession().selectList(NS+"selectUserSubMenu", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > selectUserSubMenu");
		return list;
	}

	@Override
	public List<?> selectUserTimerList(HashMap<String, String> map)	throws Exception {
    	List<UserSettingVo> list = null;
		try{
			list =  getSqlSession().selectList(NS+"selectUserTimerList", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > selectUserTimerList");
		return list;
	}

	@Override
	public void updateUserLoginPage(HashMap<String, String> map)	throws Exception {
		try{
			getSqlSession().update(NS+"updateUserLoginPage", map);
		//	list = getSqlSession().selectList(NS+"selectUserLoginPage", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
	}

	@Override
	public Object selectUserInfo(HashMap<String, String> map) throws Exception {
		UserSettingVo userSettingVo = null;
		try{
			userSettingVo =  getSqlSession().selectOne(NS+"selectUserInfo", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > selectUserInfo");
		
		return userSettingVo;
	}
	
	@Override
	public int updUserList() throws Exception {
		int result = 0;
		List<UserSettingVo> userSettingVo = null;

		try{
			userSettingVo =  getSqlSession().selectList(NS+"selectUserList");
			if(userSettingVo.size() > 0){
				for(UserSettingVo userlist : userSettingVo){
					String userid = userlist.getUser_id();
					String pw = userlist.getPw();
					String decodepw = Base64Util.decode(pw);
					String changepw = Sha256Util.sha256(decodepw);
					HashMap<String, String> map2 = new HashMap<String, String>();
					map2.put("usr_id", userid);
					map2.put("new_pwd", changepw);
					
					getSqlSession().update(NS+"updatePw", map2);
											
					result ++;
				}
			}
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > selectUserInfo");
		
		return result;
	}
   @Override
   public int selectInsPwConfirm(HashMap<String, String> param) throws Exception {

	    	int result = 0;
			String old_pwd =  param.get("old_pwd");
			String old_pw_result ="";
			UserSettingVo userSettingVo = new UserSettingVo();
	    	try{
	    		userSettingVo =  getSqlSession().selectOne(NS+"selectUserInfo", param);
	    		old_pw_result = userSettingVo.getPw();
	    		if(old_pw_result.equals(old_pwd)){
					result =  getSqlSession().update(NS+"updatePw", param);
	    		}
			}catch(Exception e){
				e.getLocalizedMessage();
			}
			logger.info("End UserSettingDaoImpl > selectPwConfirm");
	    	
	    	return result;
	    } 
	@Override
	public int updateUserInfo(HashMap<String, String> map) throws Exception {
		
		int result = 0;
		try{
				result =  getSqlSession().update(NS+"updateUserInfo", map);
		}catch(Exception e){
			e.getLocalizedMessage();
		}
		logger.info("End UserSettingDaoImpl > updateUserInfo");
		
		return result;
	}
	
}