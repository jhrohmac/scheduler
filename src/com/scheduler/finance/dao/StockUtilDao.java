
package com.scheduler.finance.dao;

import java.util.List;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.finance.vo.StockPortfolioVo;

import java.util.HashMap;

public interface StockUtilDao
{
	public StockInfoVo selectStockInfo(HashMap<String, String> map) throws Exception;
	
	public List<?> selectModalOptions(HashMap<String, String> map) throws Exception;
	
	public void saveModalOptions(HashMap<String, String> map) throws Exception;
	
	public void delAnalysis(HashMap<String, String> map) throws Exception;
	
	public void updateInterestStock(HashMap<String, String> map) throws Exception;
	
	public void mergeInterestStock(StockInfoVo map) throws Exception;
	
}