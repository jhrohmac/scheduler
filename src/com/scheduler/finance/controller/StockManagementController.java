// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.exp.SchedulerException;
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.StockManagementDao;
import com.scheduler.finance.module.hankyung.GlobalMarketCrawler;
import com.scheduler.finance.module.hankyung.StockEquitiesALL;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

import net.sf.json.JSONObject;

@Controller
public class StockManagementController
{
    public StockManagementDao stockManagementDao;
    
    public StockManagementDao getStockManagementDao() {
        return this.stockManagementDao;
    }
     
    public void setStockManagementDao(StockManagementDao stockManagementDao) {
        this.stockManagementDao = stockManagementDao;
    }
	
    @SuppressWarnings("unchecked")
	@RequestMapping({ "/stockManagement/selectStockList.do" })
    public void c_selectCodeList(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);

		String draw = StringUtil.nvl(req.getParameter("draw"));
		String start = StringUtil.nvl(req.getParameter("start"));
		String length = StringUtil.nvl(req.getParameter("length"));
		
		int size = 0;
		int total_cnt = 0;
		List<StockInfoVo> list = null;
		
		
		DataTableSettingVo resultVo = new DataTableSettingVo();
		try {
			UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
			if (userSession == null) {
				resultVo.setResult_code("-1");
				resultVo.setResult_msg("Session이 종료되었습니다.");
			}
			list = (List<StockInfoVo>) stockManagementDao.selectStockList(map);
			size = list.size();
			if (size > 0) {
				total_cnt = list.get(0).getTotal_count();
			}
		} catch (Exception e) {
			resultVo.setResult_code("-1");
			resultVo.setResult_msg(e.getMessage());
		}
		 
		resultVo.setData(list);
		resultVo.setDraw(draw);
		resultVo.setStart_no(Integer.parseInt(start));
		resultVo.setPage_length(Integer.parseInt(length));
		resultVo.setRecordsFiltered(total_cnt);
		resultVo.setRecordsTotal(total_cnt);
		JSONObject obj = JSONObject.fromObject((Object) resultVo);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
    }

    //Market 종목 가져오기
    @RequestMapping({ "/stockManagement/stockEquitiesALL.do" })
    public ModelAndView stockEquitiesALL(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
		
        String global = req.getParameter("global");
        String market = req.getParameter("market");
        int stockSize = 0;
        try {
        	if(!global.isEmpty() && !market.isEmpty()) {
        		if(market.equals("snp500")) {
        			try {
        				//snp500 지수 외 지수리스트를 넣기 위한 빈값 map
        				HashMap<String, String> snpmap = new HashMap<String, String>();
        				stockManagementDao.updateSNP500List(snpmap); 	//기존 snp500 초기화
        				List<String> stockCodeList = GlobalMarketCrawler.getGlobalMarket();
        				int num = 0;
        				for(String stockcode  : stockCodeList){
        					HashMap<String, String> map = new HashMap<String, String>();
        					String encodedData = URLEncoder.encode(stockcode, "UTF-8");
        	    			map.put("stock_code", encodedData);
        	    			map.put("stock_Aliases", "snp500");
        					System.out.println("encodedData==="+num+"=="+stockcode);
        					StockUtilController.saveStockDeatail(map);
        				}
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}else {
        			List<StockInfoVo> list = new ArrayList<StockInfoVo>();
    	        	list = StockEquitiesALL.getStockEquitiesALL(global, market);
    	        	if(list.size() > 0) {
    		    		for(StockInfoVo stockInfo : list) {
    		    			HashMap<String, String> map = new HashMap<String, String>();
    		    			map.put("stock_code", stockInfo.getStock_code());
    		    			map.put("stock_sector", stockInfo.getStock_sector());
    		    			map.put("stock_Aliases", "");
    		    			StockUtilController.saveStockDeatail(map);
    		    		}
    		    		stockSize = list.size();
    	        	}
        		}
        	}
        	return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, null); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
    }
    
    @RequestMapping({ "/stockManagement/updateStockCode.do" })
    public ModelAndView c_saveCode(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
         UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
 		//PARAMETER KEY VALUE Setting
 		HashMap<String, String> map = RequestHandler.extractParameters(req);

        map.put("in_user_id", userSession.user_id);
        try {
            this.stockManagementDao.update_stockCode(map);
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, null); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
    }
    
    @RequestMapping({ "/stockManagement/deleteStockCode.do" })
    public ModelAndView deleteStockCode( HttpServletRequest req,  HttpServletResponse res) throws SchedulerException {
        UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
        /* 삭제조건 변수 */
		String delStockCode = StringUtil.nvl(req.getParameter("deleteList"));
		String[] listStock  = delStockCode.split(",");
		List<String> list = new ArrayList<String>();
		for(String code : listStock){
			list.add(code);
		}
		try {
			//codeManageDao.delete(map);
			stockManagementDao.deleteStockCode(list, userSession.user_id);
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, null); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
    }
}
