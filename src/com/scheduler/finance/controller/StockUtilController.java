package com.scheduler.finance.controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scheduler.comm.exp.SchedulerException;
import com.scheduler.comm.util.GetHttpsURLConnection;
import com.scheduler.comm.util.ImageDownload;
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.finance.dao.StockUtilDao;
import com.scheduler.finance.module.AlphaSquareApiUtil;
import com.scheduler.finance.vo.CodeVo;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.util.handler.RequestHandler;

import net.sf.json.JSONObject;

@Controller
public class StockUtilController {
	private static StockUtilDao stockUtilDao;

	public static StockUtilDao getStockUtilDao() {
		return stockUtilDao;
	}

	@SuppressWarnings("static-access")
	public void setStockUtilDao(StockUtilDao stockUtilDao) {
		this.stockUtilDao = stockUtilDao;
	}

	@ExceptionHandler(value = Exception.class)
	public ModelAndView exceptionHandler(HttpServletRequest req, HttpServletResponse res, Exception e){
		
		String msg = StringUtil.parseErrorMsg(e.getMessage());
		
		ModelAndView mav = new ModelAndView();
		mav.addObject(ResultMsg.RESULT_MSG, msg);
		mav.setViewName("comm/result/result_msg_script");
		return mav;
	}
	

	@SuppressWarnings("unchecked")
	@RequestMapping({ "/finance/selectModalOptions.do" })
	public ModelAndView selectModalOptions(HttpServletRequest req, HttpServletResponse res) throws SchedulerException{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);

		String modal_id = req.getParameter("modal_id");
		String tab_id = req.getParameter("tab_id");
		String groupMarket = req.getParameter("groupMarket");
		
		System.out.println("selectModalOptions groupMarket============="+groupMarket);

		
		System.out.println("modal_id====="+modal_id);
		System.out.println("tab_id====="+tab_id);
		
		if(modal_id.equals("WatchList")) {
			switch (tab_id) {
			case "marketInfo":
				modal_id = "MarketChart";
				break;
			case "stockPortfolio":
				modal_id = "StockPortfolio";
				break;
			case "stockAnalysis":
				modal_id = "StockAnalysis";
				break;
			default:
				modal_id = "MarketChart";
				break;
			}
		}
		map.put("modal_id", modal_id);
		List<CodeVo> list = new ArrayList<CodeVo>();
		try {
			list = (List<CodeVo>) stockUtilDao.selectModalOptions(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String page_id = "";
		if(modal_id.indexOf("Chart") != -1) {
			page_id = "Charts";
		}else{
			page_id = modal_id;
		}
		ModelAndView mav = new ModelAndView();	
		mav.addObject("groupMarket", groupMarket);
		mav.addObject("codeList", list);
		mav.addObject("modal_id", modal_id);
		mav.setViewName("/finance/stock/pop/modal_"+page_id);
		return mav;
	}

	@SuppressWarnings("unchecked")
	public static List<CodeVo> getModalOptions(HashMap<String, String> map) throws SchedulerException{
		
		List<CodeVo> list = new ArrayList<CodeVo>();
		try {
			list = (List<CodeVo>) stockUtilDao.selectModalOptions(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	@RequestMapping({ "/finance/saveModalOptions.do" })
	public void saveModalOptions(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		try {
			stockUtilDao.saveModalOptions(map);
			System.out.println("======stockList_Options=========");

		} catch (Exception e) {
			throw new SchedulerException(this.getClass().getName()+".portfolioGroupEvent() : " + e.getLocalizedMessage());
		}
		HashMap<String, String> resultMap = new HashMap<String, String>();		
		resultMap.put("result_code", "1");
		resultMap.put("result_msg", "1");
		resultMap.put("result_Cnt","1");
		
		//JSON Object 변환
		JSONObject obj = JSONObject.fromObject(resultMap);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}

	/**
	 *  추천 종목 리스트에서 삭제
	 *  @param  조건  : 
	 *  @return 사용자 목록
	 * @throws Exception 
	 */
	@RequestMapping("/finance/delAnalysis.do")
	public void delAnalysis(HttpServletRequest req, HttpServletResponse res) throws Exception{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		try {
        	stockUtilDao.delAnalysis(map);
		} catch (Exception e) {
			throw new SchedulerException(this.getClass().getName()+".portfolioGroupEvent() : " + e.getLocalizedMessage());
		}
		HashMap<String, String> resultMap = new HashMap<String, String>();		
		resultMap.put("result_code", "1");
		resultMap.put("result_msg", "1");
		resultMap.put("result_Cnt","1");
		
		//JSON Object 변환
		JSONObject obj = JSONObject.fromObject(resultMap);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}

	/**
	 *  종목 정보
	 *  @param  조건  : 
	 *  @return 사용자 목록
	 * @throws Exception 
	 */
	public static void saveStockInfo(HashMap<String, String> map) throws Exception{

		String stockCode = map.get("stock_code");
		String stockSector ="";
		String stockAliases = map.get("stock_Aliases");
		String result = "";
		try {
			StockInfoVo stockInfo = new StockInfoVo();
			stockInfo = stockUtilDao.selectStockInfo(map);
			if(stockInfo != null) {

				String url ="https://api.alphasquare.co.kr/data/v2/stock/stocks?keyword="+stockCode;
		        result = GetHttpsURLConnection.getHttpsGet(url);
		        System.out.println(result);
		
		        Gson gson = new Gson();
		        // JSON 문자열을 HashMap으로 변환
		        Type type = new TypeToken<HashMap<String, List<HashMap<String, Object>>>>() {}.getType();
		        HashMap<String, List<HashMap<String, Object>>> hashMap = gson.fromJson(result, type);
		        List<HashMap<String, Object>> dataList = hashMap.get("data");

		        for (HashMap<String, Object> data : dataList) {
		        	System.out.println("saveStockInfo data==============================");
		        	System.out.println(data);
		        	System.out.println("saveStockInfo data==============================");
		        	data.replace("null", "");
		            double id = Double.parseDouble(String.valueOf(data.get("id")));
		            String code = String.valueOf(data.get("code"));
		            String logo = String.valueOf(data.get("logo"));
		            String ko_name = String.valueOf(data.get("ko_name"));
		            String en_name = String.valueOf(data.get("en_name"));
		            String market = String.valueOf(data.get("market"));
		            String timezone = String.valueOf(data.get("timezone"));
		            String stock_type = String.valueOf(data.get("type"));
		            String stock_type_specs = String.valueOf(data.get("type_specs")).replace("[", "").replace("]", "");
		            String stock_currency = String.valueOf(data.get("currency"));
		            String industry = String.valueOf(data.get("industry"));
		            String country_code = String.valueOf(data.get("country_code"));		            
		            if(stockAliases == null) {
		            	stockAliases= String.valueOf(data.get("aliases"));	
		            }else {
		            	stockAliases= String.valueOf("");
		            }
		            if(stockSector == null) {
		            	stockSector= String.valueOf(data.get("sector"));
		            	stockSector= String.valueOf("");
		            }
		            
		            if(market.equals("kospi")) {
		            	market = "STK";
		            }else if(market.equals("kosdaq")) {
		            	market = "KSQ";
		            }
		            
		            StockInfoVo stockInfoVo = new StockInfoVo();
		            stockInfoVo.setStock_group("0001");
		            stockInfoVo.setStock_id(String.valueOf((int) Math.floor(id)));
		            stockInfoVo.setStock_code(code);
		            stockInfoVo.setStock_ko_name(ko_name);
		            stockInfoVo.setStock_en_name(en_name);
		            stockInfoVo.setStock_market(market);
		            stockInfoVo.setStock_timezone(timezone);
		            stockInfoVo.setStock_type(stock_type);
		            stockInfoVo.setStock_type_specs(stock_type_specs);
		            stockInfoVo.setStock_currency(stock_currency);
		            stockInfoVo.setStock_sector(stockSector);
		            stockInfoVo.setStock_desc(industry);
		            stockInfoVo.setStock_url(logo);
		            stockInfoVo.setStock_country_code(country_code);
		            stockInfoVo.setStock_aliases(stockAliases);
		            if (logo != "null" && logo != null && !logo.equals("") && logo.length() > 0){
		            	// 이미지 파일다운로드 (DB에 이미지 파일 넣는 부분은 나중에)
		            	ImageDownload.imageDownload(stockInfoVo);
		            }
		            stockUtilDao.mergeInterestStock(stockInfoVo);
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  종목 정보
	 *  @param  조건  : 
	 *  @return 사용자 목록
	 * @throws Exception 
	 */
	public static void saveStockDeatail(HashMap<String, String> map) throws Exception{

		String stockCode = map.get("stock_code");
		String stockSector = map.get("stock_sector");
		String stockAliases = map.get("stock_Aliases");
		String result = "";
		try {
			StockInfoVo stockInfo = new StockInfoVo();
			stockInfo = stockUtilDao.selectStockInfo(map);
			if(stockInfo == null) {
				String url ="https://api.alphasquare.co.kr/data/v2/stock/details?code="+stockCode;
		        result = GetHttpsURLConnection.getHttpsGet(url);
		        System.out.println(result);
		
				//System.out.println(result);
				JSONObject json = JSONObject.fromObject(result);
				// Extract values
				JSONObject data = json.getJSONObject(stockCode);
				double id = Double.parseDouble(String.valueOf(data.get("id")));
				String code = String.valueOf(data.get("code"));
				String logo = String.valueOf(data.get("logo"));
				String ko_name = String.valueOf(data.get("ko_name"));
				String en_name = String.valueOf(data.get("en_name"));
				String market = String.valueOf(data.get("market"));
				String timezone = String.valueOf(data.get("timezone"));
				String stock_type = String.valueOf(data.get("type"));
	            String stock_type_specs = String.valueOf(data.get("type_specs")).replace("[", "").replace("]", "");
				String stock_currency = String.valueOf(data.get("currency"));
				String description = String.valueOf(data.get("description"));
				String country_code = String.valueOf(data.get("country_code"));
				
				if(stockAliases == null) {
	            	stockAliases= String.valueOf(data.get("aliases"));	
	            }
	            if(stockSector == null) {
	            	stockSector= String.valueOf(data.get("sector"));
	            }
	            if(market.equals("kospi")) {
	            	market = "STK";
	            }else if(market.equals("kosdaq")) {
	            	market = "KSQ";
	            }
	            StockInfoVo stockInfoVo = new StockInfoVo();
	            stockInfoVo.setStock_id(String.valueOf((int) Math.floor(id)));
	            stockInfoVo.setStock_code(code);
	            stockInfoVo.setStock_ko_name(ko_name);
	            stockInfoVo.setStock_en_name(en_name);
	            stockInfoVo.setStock_market(market);
	            stockInfoVo.setStock_timezone(timezone);
	            stockInfoVo.setStock_type(stock_type);
	            stockInfoVo.setStock_type_specs(stock_type_specs.replaceAll("\"", ""));
	            stockInfoVo.setStock_currency(stock_currency);
	            stockInfoVo.setStock_sector(stockSector);
	            stockInfoVo.setStock_desc(description);
	            stockInfoVo.setStock_url(logo);
	            stockInfoVo.setStock_country_code(country_code);
	            stockInfoVo.setStock_aliases(stockAliases);
	            
				if(logo != null) {
					// 이미지 파일다운로드 (DB에 이미지 파일 넣는 부분은 나중에)
					ImageDownload.imageDownload(stockInfoVo);
				}
				 stockUtilDao.mergeInterestStock(stockInfoVo);
			}else {
				 stockUtilDao.updateInterestStock(map);
			}
		} catch (Exception e) {

			System.out.println("stockCode=========="+stockCode);
			System.out.println("stockSector=========="+stockSector);
			System.out.println("stockAliases=========="+stockAliases);
			e.printStackTrace();
		}
	}
	

	/**
	 *  종목 정보
	 *  @param  조건  : 
	 *  @return 사용자 목록
	 * @throws Exception 
	 */
	public static int saveStockKeywold(HashMap<String, String> map) throws Exception{
		int stockSize= 0;
		try {
				String result = AlphaSquareApiUtil.callKeywordSingle(map.get("in_stockCode"));
		        Gson gson = new Gson();
		        // JSON 문자열을 HashMap으로 변환
		        Type type = new TypeToken<HashMap<String, List<HashMap<String, Object>>>>() {}.getType();
		        HashMap<String, List<HashMap<String, Object>>> hashMap = gson.fromJson(result, type);
		        List<HashMap<String, Object>> dataList = hashMap.get("data");
		        
		        for (HashMap<String, Object> data : dataList) {
		        	System.out.println("saveStockKeywold data==============================");
		        	System.out.println(data);
		        	System.out.println("saveStockKeywold data==============================");
		        	data.replace("null", "");
		            double id = Double.parseDouble(String.valueOf(data.get("id")));
		            String code = String.valueOf(data.get("code"));
		            String logo = String.valueOf(data.get("logo"));
		            String ko_name = String.valueOf(data.get("ko_name"));
		            String en_name = String.valueOf(data.get("en_name"));
		            String market = String.valueOf(data.get("market"));
		            String timezone = String.valueOf(data.get("timezone"));
		            String stock_type = String.valueOf(data.get("type"));
		            String stock_type_specs = String.valueOf(data.get("type_specs")).replace("[", "").replace("]", "");
		            String stock_currency = String.valueOf(data.get("currency"));
		            String industry = String.valueOf(data.get("industry"));
		            String country_code = String.valueOf(data.get("country_code"));
		            String stockAliases= String.valueOf(data.get("aliases"));	
		            String stockSector= String.valueOf(data.get("sector"));
		            if(market.equals("kospi")) {
		            	market = "STK";
		            }else if(market.equals("kosdaq")) {
		            	market = "KSQ";
		            }
		            StockInfoVo stockInfoVo = new StockInfoVo();
		            stockInfoVo.setStock_group("0001");
		            stockInfoVo.setStock_id(String.valueOf((int) Math.floor(id)));
		            stockInfoVo.setStock_code(code);
		            stockInfoVo.setStock_ko_name(ko_name);
		            stockInfoVo.setStock_en_name(en_name);
		            stockInfoVo.setStock_market(market);
		            stockInfoVo.setStock_timezone(timezone);
		            stockInfoVo.setStock_type(stock_type);
		            stockInfoVo.setStock_type_specs(stock_type_specs);
		            stockInfoVo.setStock_currency(stock_currency);
		            stockInfoVo.setStock_sector(stockSector);
		            stockInfoVo.setStock_desc(industry);
		            stockInfoVo.setStock_url(logo);
		            stockInfoVo.setStock_country_code(country_code);
		            stockInfoVo.setStock_aliases(stockAliases);
		            if (logo != "null" && logo != null && !logo.equals("") && logo.length() > 0){
		            	// 이미지 파일다운로드 (DB에 이미지 파일 넣는 부분은 나중에)
		            	ImageDownload.imageDownload(stockInfoVo);
		            }
		            stockUtilDao.mergeInterestStock(stockInfoVo);
		            
		            stockSize++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stockSize;
	}
}
