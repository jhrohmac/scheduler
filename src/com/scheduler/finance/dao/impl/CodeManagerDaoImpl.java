
package com.scheduler.finance.dao.impl;

import java.util.List;
import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.finance.dao.CodeManageDao;
import com.scheduler.finance.vo.CodeVo;

import java.util.HashMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mybatis.spring.support.SqlSessionDaoSupport;

public class CodeManagerDaoImpl extends SqlSessionDaoSupport implements CodeManageDao
{
    private static final Logger logger;
    
    static {
        logger = LoggerFactory.getLogger((Class)CodeManagerDaoImpl.class);
    }
    
    @Override
   	public List<?> selectCodeGroup(HashMap<String, String> map) throws Exception {
       	
       	List<CodeVo> list;
       	        		
   		try{
   				list = getSqlSession().selectList("com.scheduler.finance.sql.CodeManage.selectCodeGroup");
   		}catch(Exception e){
   			throw new UserTransactionException(this.getClass().getName()+".selectCodeGroup() : ", e.getLocalizedMessage());
   		}
   		logger.info("End CodeManagerDaoImpl > selectCodeGroup");
   		
   		return list;
   		
   	}
    
    public int selectOneCnt(final HashMap<String, String> param) throws Exception {
        int cnt = 0;
        try {
            cnt = (int)this.getSqlSession().selectOne("com.scheduler.finance.sql.CodeManage.selectOneCnt", param);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectOneCnt() : ", e.getLocalizedMessage());
        }
        return cnt;
    }
    
    public CodeVo selectCodeSeq(final HashMap<String, String> param) throws Exception {
    	CodeVo vo = new CodeVo();
    	try {
    		vo = getSqlSession().selectOne("com.scheduler.finance.sql.CodeManage.selectCodeSeq", param);
    		System.out.println("=================="+vo.getCode_seq());
    	}
    	catch (Exception e) {
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectCodeSeq() : ", e.getLocalizedMessage());
    	}
    	return vo;
    }
    
    public List<CodeVo> selectCodeList(final HashMap<String, String> map) throws Exception {
        CodeManagerDaoImpl.logger.info("Start CodeManagerDaoImpl > selectCodeList");
        List<CodeVo> menuList;
        try {
            menuList = getSqlSession().selectList("com.scheduler.finance.sql.CodeManage.selectCodeList", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectAll() : ", e.getLocalizedMessage());
        }
        CodeManagerDaoImpl.logger.info("End CodeManagerDaoImpl > selectCodeList");
        return menuList;
    }
    
    public int save(final HashMap<String, String> map) throws Exception {
        CodeManagerDaoImpl.logger.info("Start CodeManagerDaoImpl > save");
        int result = 0;
        try {
            if (map.get("in_eventdiv").equals("insert")) {
                result = getSqlSession().insert("com.scheduler.finance.sql.CodeManage.saveCode", map);
            }
            else if (map.get("in_eventdiv").equals("update")) {
                result = getSqlSession().insert("com.scheduler.finance.sql.CodeManage.updateCode", map);
            }
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".insert() : ", e.getLocalizedMessage());
        }
        CodeManagerDaoImpl.logger.info("End CodeManagerDaoImpl > save");
        return result;
    }
    
    public int delete(final HashMap<String, String> map) throws Exception {
        CodeManagerDaoImpl.logger.info("Start CodeManagerDaoImpl > delete");
        int result = 0;
        try {
            result = getSqlSession().delete("com.scheduler.finance.sql.CodeManage.deleteCode", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".delete() : ", e.getLocalizedMessage());
        }
        CodeManagerDaoImpl.logger.info("End CodeManagerDaoImpl > delete");
        return result;
    }
}