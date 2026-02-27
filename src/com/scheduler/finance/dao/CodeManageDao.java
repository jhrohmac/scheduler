// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.dao;

import java.util.List;

import com.scheduler.finance.vo.CodeVo;

import java.util.HashMap;

public interface CodeManageDao
{
		public List<?> selectCodeGroup(HashMap<String, String> map) throws Exception;
	
	    public  List<?> selectCodeList(HashMap<String, String> map) throws Exception;
    
	    public int selectOneCnt(HashMap<String, String> map) throws Exception;
	    
	    public CodeVo selectCodeSeq(HashMap<String, String> map) throws Exception;
    
	    public int save(HashMap<String, String> map) throws Exception;
    
	    public  int delete(HashMap<String, String> map) throws Exception;
}
