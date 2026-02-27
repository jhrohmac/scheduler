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
import com.scheduler.finance.dao.StockUtilDao;
import com.scheduler.finance.vo.CodeVo;
import com.scheduler.finance.vo.ModalOptionsVo;
import com.scheduler.finance.vo.StockInfoVo;

public class StockUtilDaoImpl extends SqlSessionDaoSupport implements StockUtilDao
{
    private static Logger logger;
    
    static {
        logger = LoggerFactory.getLogger((Class)StockUtilDaoImpl.class);
    }
    
    public StockInfoVo selectStockInfo(HashMap<String, String> map) throws Exception {
        StockInfoVo stockVo = new StockInfoVo();
        try {
            stockVo = getSqlSession().selectOne("sql.StockUtil.selectStockInfo", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectStockInfo() : ", e.getLocalizedMessage());
        }
        return stockVo;
    }
    
    public List<?> selectModalOptions(HashMap<String, String> map) throws Exception {
        List<CodeVo> list;
        try {
            list = getSqlSession().selectList("sql.StockUtil.selectModalOptions", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectModalOptions() : ", e.getLocalizedMessage());
        }
        StockUtilDaoImpl.logger.info("End StockUtilDaoImpl > selectModalOptions");
        return list;
    }
    
    public void saveModalOptions(HashMap<String, String> map) throws Exception {
    	try {
    		if(map.get("code_reset").equals("Y")) {
    			getSqlSession().update("sql.StockUtil.resetModalOptions", map);
    		}
    		getSqlSession().update("sql.StockUtil.saveModalOptions", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".saveModalOptions() : ", e.getLocalizedMessage());
    	}
    }
    
    public void delAnalysis(HashMap<String, String> map) throws Exception {
    	try {
    		getSqlSession().update("sql.StockUtil.delAnalysis", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".delAnalysis() : ", e.getLocalizedMessage());
    	}
    }
	
    public void mergeInterestStock(StockInfoVo stockInfoVo) throws Exception {
    	try {
    		System.out.println("mergeInterestStock======="+stockInfoVo.getStock_code());
	       this.getSqlSession().insert("sql.StockCodeInfo.mergeInterestStock", stockInfoVo);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".mergeInterestStock() : ", e.getLocalizedMessage());
       }
   }
	
    public void updateInterestStock(HashMap<String, String> map) throws Exception {
    	try {
	       this.getSqlSession().update("sql.StockUtil.updateInterestStock", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".updateInterestStock() : ", e.getLocalizedMessage());
       }
   }
}
