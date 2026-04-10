package com.scheduler.login.dao;

import java.util.Map;

import com.scheduler.login.service.UserSession;
import com.scheduler.management.vo.UserVo;

public interface LoginDao {
	
	public UserSession login(Map<String, String> map) throws Exception;
	
	public void setExtensionDate(Map<String, String> map) throws Exception;
	
	public void updateUserSessionTime(Map<String, String> map) throws Exception;
	
	//사용자 등록 체크	
	public UserVo selectUserCheck(Map<String, String> map) throws Exception;
	
	//사용자 등록
	public void registerUser(Map<String, String> map) throws Exception;
	
	//사용자 패스워드 reset
	public void resetPassword(Map<String, String> map) throws Exception;
}
