package com.scheduler.management.dao;

import java.util.HashMap;
import java.util.List;

public interface UserManagementDao {
    
    public List<?> selectUserAll(HashMap<String, String> param) throws Exception;
    
    public Object selectUserInfo(HashMap<String, String> param) throws Exception;
    
    public List<?> userStartPageList(HashMap<String, String> param) throws Exception;
    
    public int updatePw(HashMap<String, String> param) throws Exception;
    
    public int updateExtensionDate(HashMap<String, String> param) throws Exception;
    
    public int userEvent(HashMap<String, String> param) throws Exception;
    
    public int delete(List<String> param, String userName) throws Exception;
}