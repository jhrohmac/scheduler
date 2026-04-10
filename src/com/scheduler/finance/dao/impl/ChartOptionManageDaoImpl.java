
package com.scheduler.finance.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.finance.dao.ChartOptionManageDao;
import com.scheduler.finance.vo.ChartOptionVo;

public class ChartOptionManageDaoImpl extends SqlSessionDaoSupport implements ChartOptionManageDao
{
    private static final String NS ="sql.ChartOption.";
    
    
    @Override
   	public List<?> selectOptionGroup(HashMap<String, String> map) throws Exception {
       	List<ChartOptionVo> list;
   		try{
   				list = getSqlSession().selectList(NS+"selectOptionGroup");
   		}catch(Exception e){
   			throw new UserTransactionException(this.getClass().getName()+".selectOptionGroup() : ", e.getLocalizedMessage());
   		}
   		return list;
   	}

    public List<ChartOptionVo> selectOptionList(final HashMap<String, String> map) throws Exception {
        List<ChartOptionVo> menuList;
        try {										
            menuList = getSqlSession().selectList(NS+"selectOptionList", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectOptionList() : ", e.getLocalizedMessage());
        }
        return menuList;
    }
    
    public int selectOneCnt(final HashMap<String, String> param) throws Exception {
        int cnt = 0;
        try {
            cnt = (int)this.getSqlSession().selectOne(NS+"selectOneCnt", param);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectOneCnt() : ", e.getLocalizedMessage());
        }
        return cnt;
    }
    
    public ChartOptionVo selectOptionSeq(final HashMap<String, String> param) throws Exception {
    	ChartOptionVo vo = new ChartOptionVo();
    	try {
    		vo = getSqlSession().selectOne(NS+"selectOptionSeq", param);
    	}
    	catch (Exception e) {
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectOptionSeq() : ", e.getLocalizedMessage());
    	}
    	return vo;
    }
    
    public int save(final HashMap<String, String> map) throws Exception {
        int result = 0;
        try {
            if (map.get("in_eventdiv").equals("insert")) {
                result = getSqlSession().insert(NS+"saveOption", map);
            }
            else if (map.get("in_eventdiv").equals("update")) {
                result = getSqlSession().insert(NS+"updateOption", map);
            }
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".insert() : ", e.getLocalizedMessage());
        }
        return result;
    }
    
    public int delete(final HashMap<String, String> map) throws Exception {
        int result = 0;
        try {
            result = getSqlSession().delete(NS+"deleteOption", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".delete() : ", e.getLocalizedMessage());
        }
        return result;
    }
}