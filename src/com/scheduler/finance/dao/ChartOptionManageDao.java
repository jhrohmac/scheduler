// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.dao;

import java.util.List;

import com.scheduler.finance.vo.ChartOptionVo;

import java.util.HashMap;

public interface ChartOptionManageDao
{
		public List<?> selectOptionGroup(HashMap<String, String> map) throws Exception;
	
	    public List<?> selectOptionList(HashMap<String, String> map) throws Exception;
    
	    public int selectOneCnt(HashMap<String, String> map) throws Exception;
	    
	    public ChartOptionVo selectOptionSeq(HashMap<String, String> map) throws Exception;
    
	    public int save(HashMap<String, String> map) throws Exception;
    
	    public int delete(HashMap<String, String> map) throws Exception;
}
