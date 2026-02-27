package com.scheduler.management.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.management.dao.CodeManagementDao;
import com.scheduler.management.vo.CodeManagementVo;

public class CodeManagementDaoImpl extends SqlSessionDaoSupport implements CodeManagementDao {
	
	private static final Logger logger = LoggerFactory.getLogger(CodeManagementDaoImpl.class);

    public List<CodeManagementVo> selectCodeList(final HashMap<String, String> map) throws Exception {
        List<CodeManagementVo> codeList = null;
        try {
            codeList = getSqlSession().selectList("sql.CodeManagement.selectCodeList", map);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return codeList;
    }
    
    public CodeManagementVo selectSortOrder(final HashMap<String, String> param) throws Exception {
    	CodeManagementVo vo = new CodeManagementVo();
    	try {
    		vo = getSqlSession().selectOne("sql.CodeManagement.selectSortOrder", param);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	return vo;
    }
    
    public int selectOneCnt(final HashMap<String, String> param) throws Exception {
    	int cnt = 0;
    	try {
    		cnt = (int)this.getSqlSession().selectOne("sql.CodeManagement.selectOneCnt", param);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
    	}
    	return cnt;
    }

    
    @Override
	public List<?> selectCodeGroupList(HashMap<String, String> map) throws Exception {
    	
    	List<?> list = null;
    	        		
		try{
			list = getSqlSession().selectList("sql.CodeManagement.selectCodeGroupList");
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
		}
		return list;
	}
    
    /**
     * Code UPDATE, SAVE
     */
    public int save(final HashMap<String, String> map) throws Exception {
        int result = 0;
        try {
            if (map.get("in_eventDiv").equals("insert")) {
                result = getSqlSession().insert("sql.CodeManagement.saveCode", map);
            }
            else if (map.get("in_eventDiv").equals("update")) {            	
                result = getSqlSession().insert("sql.CodeManagement.updateCode", map);
            }
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return result;
    }

    /**
     * Code DELETE
     */
    public int delete(final HashMap<String, String> map) throws Exception {
        int result = 0;
        try {
            result = getSqlSession().delete("sql.CodeManagement.deleteCode", map);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(this.getClass().getName() + e.getMessage(), e);
        }
        return result;
    }
}