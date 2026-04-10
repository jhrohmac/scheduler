// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.management.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.comm.vo.MenuVo;

public interface MenuManagementDao
{
	    public  List<?> selectMenuList(HashMap<String, String> p0) throws Exception;
	    
	    public int selectOneCnt(HashMap<String, String> p0) throws Exception;
    
	    public  MenuVo selectMenuSeq(HashMap<String, String> p0) throws Exception;
	    
	    public  List<?> selectMenuGroupList(HashMap<String, String> p0) throws Exception;
	    
	    public  List<?> selectMenuAccessUsers(HashMap<String, String> p0) throws Exception;
    
	    public int saveMenuOrder(HashMap<String, String> map) throws Exception;
	    
	    public  List<?> checkMenuAccessUsers(HashMap<String, String> p0) throws Exception;
	    
	    public int saveMenuAccessUsers(HashMap<String, String> p0) throws Exception;
	    
	    public int save(HashMap<String, String> p0) throws Exception;

	    public MenuVo selectMenuBySeq(HashMap<String, String> p0) throws Exception;
    
	    public  int delete(HashMap<String, String> p0) throws Exception;

	    // ===== [TREE VIEW INTEGRATION] =====
	    List<?> selectTreeViewUsers(HashMap<String, String> map) throws Exception;
	    List<?> selectAllMenus(HashMap<String, String> map) throws Exception;
	    List<?> selectUserMenuAuth(HashMap<String, String> map) throws Exception;
	    int deleteUserMenuAuth(HashMap<String, String> map) throws Exception;
	    int insertUserMenuAuth(HashMap<String, String> map) throws Exception;

}
