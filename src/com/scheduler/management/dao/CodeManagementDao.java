// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.management.dao;

import java.util.HashMap;
import java.util.List;

import com.scheduler.management.vo.CodeManagementVo;

public interface CodeManagementDao
{
	    public  List<?> selectCodeList(final HashMap<String, String> p0) throws Exception;
    
	    public int selectOneCnt(final HashMap<String, String> p0) throws Exception;
	    
	    public  CodeManagementVo selectSortOrder(HashMap<String, String> map) throws Exception;
	    
	    public  List<?> selectCodeGroupList(final HashMap<String, String> p0) throws Exception;
    
	    public int save(final HashMap<String, String> p0) throws Exception;
    
	    public  int delete(final HashMap<String, String> p0) throws Exception;
}
