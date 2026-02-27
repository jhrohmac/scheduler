package com.scheduler.comm.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.comm.util.DataSet;
import com.scheduler.comm.util.Parameter;

public interface MenuDao {
	
	public DataSet login(Parameter param) throws Exception;
	    
	public List<?> selectSetMenu(HashMap<String, String> param) throws Exception;
	
    public List<?> selectPageMove(HashMap<String, String> param) throws Exception;
    
    public Object selectPageInfo(HashMap<String, String> param) throws Exception;
    
}
