package com.scheduler.comm.dao;

import java.util.HashMap;
import java.util.List;


public interface UserSettingDao{

	
	public List<?> selectDuplicateUser(HashMap<String, String> map) throws Exception;
	
	/*
	=========================2025============================
	*/
	
	public Object selectUserInfo(HashMap<String, String> map) throws Exception;
	public int updUserList() throws Exception;
	
	public List<?> selectUserDefaultPage(HashMap<String, String> map) throws Exception;
	
	public List<?> selectUserSystemCodeInfo(HashMap<String, String> map) throws Exception;
	
	public List<?> selectUserTopMenu(HashMap<String, String> map) throws Exception;
	
	public List<?> selectUserSubMenu(HashMap<String, String> map) throws Exception;
	
	public List<?> selectUserTimerList(HashMap<String, String> map) throws Exception;
	
	
	public int selectInsPwConfirm(HashMap<String, String> map) throws Exception;
	
	public int updateUserInfo(HashMap<String, String> map) throws Exception;
	
	public void updateUserLoginPage(HashMap<String, String> map) throws Exception;
	
}