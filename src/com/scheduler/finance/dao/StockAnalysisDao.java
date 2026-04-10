
package com.scheduler.finance.dao;

import java.util.List;

import com.scheduler.finance.vo.FinanceOptionsVo;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.finance.vo.StockPortfolioVo;

import java.util.HashMap;

public interface StockAnalysisDao
{
	
	public List<?> selectStockList(HashMap<String, String> map) throws Exception;
	
	public void update_stockCode(HashMap<String, String> map) throws Exception;
	
	public int deleteStockCode(List<String> param, String userName) throws Exception;
}
