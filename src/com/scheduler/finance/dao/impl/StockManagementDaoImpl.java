// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.finance.dao.StockManagementDao;
import com.scheduler.finance.vo.StockInfoVo;

public class StockManagementDaoImpl extends SqlSessionDaoSupport implements StockManagementDao
{
    private static Logger logger;
    
    static {
        logger = LoggerFactory.getLogger((Class)StockManagementDaoImpl.class);
    }
    
    public List<?> selectStockList(HashMap<String, String> map) throws Exception {
    	List<StockInfoVo> list;
    	try {
    		list = getSqlSession().selectList("com.scheduler.finance.sql.StockManagement.selectStockList", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectStockList() : ", e.getLocalizedMessage());
    	}
    	return list;
    }
    
    public void updateSNP500List(HashMap<String, String> map) throws Exception {
    	try {
    		getSqlSession().update("com.scheduler.finance.sql.StockManagement.updateSNP500List", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".updateSNP500List() : ", e.getLocalizedMessage());
    	}
    }
    
    public void update_stockCode(HashMap<String, String> map) throws Exception {
    	try {
    		getSqlSession().update("com.scheduler.finance.sql.StockManagement.update_stockCode", map);
    		//분석 테이블도 모두 사용 플레그 변경
    		if(map.get("use_div").equals("N")) {
    			getSqlSession().update("com.scheduler.finance.sql.StockManagement.update_stockAnalysis", map);
    			getSqlSession().update("com.scheduler.finance.sql.StockManagement.update_stock3minAnalysis", map);
    			getSqlSession().update("com.scheduler.finance.sql.StockManagement.update_stockConsensus", map);	
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".update_stockCode() : ", e.getLocalizedMessage());
    	}
    }
    
	@Override
	public int deleteStockCode(List<String> param, String userName) throws Exception {
    	int result = 0;
		try{
			result = getSqlSession().delete("com.scheduler.finance.sql.StockManagement.deleteStockCode", param);
		}catch(Exception e){
			throw new UserTransactionException(this.getClass().getName()+".deleteStockCode() : ", e.getLocalizedMessage());
		}
		logger.info("End StockManagement > deleteStockCode");
		
		return result;
	}

}
