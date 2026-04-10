package com.scheduler.finance.controller;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.scheduler.comm.exp.SchedulerException;
import com.scheduler.comm.util.GetHttpsURLConnection;
import com.scheduler.comm.util.ImageDownload;
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.util.TimeUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.StockCodeInfoDao;
import com.scheduler.finance.module.AlphaSquareApiUtil;
import com.scheduler.finance.module.MACDOSCCalculator;
import com.scheduler.finance.module.MovingAverageAlignmentModule;
import com.scheduler.finance.module.StockAnalysisUtil;
import com.scheduler.finance.module.StockHighPointUtil;
import com.scheduler.finance.vo.CodeVo;
import com.scheduler.finance.vo.FinanceOptionsVo;
import com.scheduler.finance.vo.ParseStockDataVo;
import com.scheduler.finance.vo.StockDataVo;
import com.scheduler.finance.vo.StockGroupVo;
import com.scheduler.finance.vo.StockHighPointVo;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.finance.vo.StockLowPointVo;
import com.scheduler.finance.vo.StockPortfolioVo;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceResult.Output1;
import com.scheduler.login.service.UserSession;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

import net.sf.json.JSONObject;



@Controller
public class StockCodeInfoController {
	
	private StockCodeInfoDao stockCodeInfoDao;

	public StockCodeInfoDao getStockCodeInfoDao() {
		return stockCodeInfoDao;
	}

	public void setStockCodeInfoDao(StockCodeInfoDao stockCodeInfoDao) {
		this.stockCodeInfoDao = stockCodeInfoDao;
	}
	StockAnalysisUtil stockUtil = new StockAnalysisUtil();
	
	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	
	@RequestMapping({ "/finance/financeUserSetting.do" })
	public void c_financeUserSetting(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		map.put("in_userId",		userSession.user_id);
		
		//사용자별 tab 리스트, 해외리스트 표시
		FinanceOptionsVo financeVo = new FinanceOptionsVo();
		
		try {
			financeVo = stockCodeInfoDao.financeUserSetting(map);
			System.out.println("financeVo=="+financeVo.getDefaultstock());
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
	    	resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);
			resultVo.setSingleData(financeVo.getDefaultstock());
			
			ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
	}

	
	@SuppressWarnings({ "unchecked", "static-access" })
	@RequestMapping({ "/finance/sidetabCallPage.do" })
	public ModelAndView c_itemManageCall(HttpServletRequest req, HttpServletResponse res) throws SchedulerException{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		String tab_id = req.getParameter("tab_id");
		String code_id = req.getParameter("code_id");
		String code_value = req.getParameter("code_value");
		String groupMarket = req.getParameter("groupMarket");
		System.out.println("c_itemManageCall groupMarket============="+groupMarket);

		List<StockInfoVo> list = new ArrayList<StockInfoVo>();
		ModelAndView mav = new ModelAndView();
		
		map.put(code_id, code_value);

		switch (tab_id) {
			
			case "stockPortfolio":
				/*구매 목록*/
				try {
					list = (List<StockInfoVo>) stockCodeInfoDao.selectPortfolioStockList(map);
					list = stockUtil.stockCurrent(list, map);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "marketChart":
				/*시장 정보*/
				try {
					list = (List<StockInfoVo>) stockCodeInfoDao.selectStockMarket(map);

					String marketState = "NO_DATA";

					if (list != null && !list.isEmpty()) {
						String sma = list.get(0).getStock_sma();

						if (sma != null && sma.trim().length() > 0) {
							String[] smaAnalysis = sma.split(",");

							if (smaAnalysis.length >= 2) {
								String first = smaAnalysis[0];
								String second = smaAnalysis[1];

								if ("UP".equals(first) && "UP".equals(second)) {
									marketState = "RED";
								} else if ("UP".equals(first) && "DOWN".equals(second)) {
									marketState = "YELLOW";
								} else if ("DOWN".equals(first) && "UP".equals(second)) {
									marketState = "YELLOW";
								} else if ("DOWN".equals(first) && "DOWN".equals(second)) {
									marketState = "BLUE";
								} else {
									// 정의되지 않은 패턴
									marketState = "UNKNOWN";
								}
							} else {
								// SMA 데이터 개수가 부족한 경우
								marketState = "INSUFFICIENT_DATA";
							}
						} else {
							// SMA 값이 없는 경우
							marketState = "INSUFFICIENT_DATA";
						}
					}

					mav.addObject("marketState", marketState);	
					//list = stockUtil.stockCurrent(list, map);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "stockAnalysis":
				/*추천 종목*/
				try {
					//Chart Options
					HashMap<String,String> modalmap = new HashMap<String,String>();
					modalmap.put("modal_id", "StockAnalysis");
					
					List<CodeVo> codevo = new ArrayList<CodeVo>(); 
					codevo = StockUtilController.getModalOptions(modalmap);

			    	if(codevo.size() > 0) {
			    		for(CodeVo vo : codevo) {
			    			if(vo.getCode_id().contains("-")) {
			    				String[] id = vo.getCode_id().split("-");
								
								//국내/글로벌 옵션
								if(id[0].equals(groupMarket)) {
							    	map.put(id[1], vo.getCode_value());
								}
								//FLAG 리스트
								if(id[0].equals("F") && vo.getCode_value().equals("Y")) {
									map.put(id[1], "UP");
								}
			    			}else {
			    				if(vo.getDefault_yn().equals("Y")) {
			        				map.put("sortField", vo.getCode_id());
			        				map.put("sortOrder", vo.getCode_value());
			        				map.put(vo.getCode_id(), vo.getCode_value());
			        			}
			    			}
			    		}
			    	}
					
					list = (List<StockInfoVo>) stockCodeInfoDao.selectStockAnalysis(map);
					list = stockUtil.stockCurrent(list, map);

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
		
		mav.addObject("listVo", list);
		mav.addObject("tab_id", tab_id);
		mav.addObject("groupMarket", groupMarket);
		mav.setViewName("/finance/stock/sidetab/"+tab_id);
		return mav;
	};
	/*
	 * market Info
	*/
	@SuppressWarnings({ "unchecked", "static-access" })
	@RequestMapping({ "/finance/marketInfo.do"})
	public ModelAndView marketInfo(HttpServletRequest req, HttpServletResponse res) throws SchedulerException{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);

		List<StockInfoVo> list = new ArrayList<StockInfoVo>();
		try {
			list = (List<StockInfoVo>) stockCodeInfoDao.selectStockMarket(map);
			list = stockUtil.stockCurrent(list, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("listVo", list);
		mav.setViewName("/finance/stock/sidetab/marketInfo");
		return mav;
	}
	

	/**
	 *  stock Detail 조회
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	@RequestMapping("/finance/selectStockDetails.do")
	public void selectStockDetails(HttpServletRequest req, HttpServletResponse res) throws Exception{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
		String stock_code = StringUtil.nvl(req.getParameter("stock_code"));
		  String result = "";

	        try {
	            // 공통 유틸 사용: /stock/details 호출
	            result = AlphaSquareApiUtil.callStockDetails(stock_code);

	            // DB 저장 및 이미지 다운로드를 위해 VO로도 한 번 파싱
	            StockInfoVo stockInfoVo = AlphaSquareApiUtil.parseStockDetailsToVo(stock_code, result);
	            if (stockInfoVo != null) {
	                // 기본 그룹 값 세팅 (기존 로직 유지)
	                stockInfoVo.setStock_group("0001");

	                if (stockInfoVo.getStock_url() != null && stockInfoVo.getStock_url().length() > 0) {
	                    ImageDownload.imageDownload(stockInfoVo);
	                }

	                // 기준 정보는 기존과 동일하게 merge
	                stockCodeInfoDao.mergeInterestStock(stockInfoVo);
	            }

	            DataTableSettingVo resultVo = new DataTableSettingVo();
	        	resultVo.setSingleData(result);

	        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	        } catch (Exception e) {
	        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	        }
	}
	
	/**
	 *  조회 종목 저장 stock info insert
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	@RequestMapping("/finance/selectStockPrice.do")
	public void selectStockPrice(HttpServletRequest req, HttpServletResponse res) throws Exception{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		
        String  priceObj   = StringUtil.nvl(req.getParameter("priceObj"));
        String  market_div = StringUtil.nvl(req.getParameter("market_div"));
        JSONObject getParams = JSONObject.fromObject(priceObj);

        String  stock_code  = getParams.getString("stock_code");
        String  group_id    = getParams.getString("group_id");
        String  groupMarket = getParams.getString("groupMarket");

        HashMap<String, String> infoData = new HashMap<String, String>();

		try {
            // 공통 유틸 사용
			String resultKey = AlphaSquareApiUtil.callKeywordSingle(stock_code);
            String result = AlphaSquareApiUtil.callCurrentPriceSingle(stock_code);
            JSONObject data = AlphaSquareApiUtil.getCurrentPriceData(stock_code, result);
            System.out.println(resultKey);
            System.out.println("selectStockPrice DATA==========");
            System.out.println(data);
            System.out.println("selectStockPrice DATA==========");
			stock_code = data.optString("code", "");
			System.out.println("selectStockPrice DATA stock_code==========" + stock_code);
			
		 	long dt = data.optLong("dt", 0L);
		    double close = data.optDouble("close");
		    double open = data.optDouble("open", 0.0);
		    double volume = data.optDouble("volume", 0.0);
		    double prevClose = data.optDouble("prev_close", 0.0);
		    double volumeValued = data.optDouble("volume_valued", 0.0);
		    
            // Convert timestamp to LocalDateTime
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dt), ZoneId.systemDefault());
            // Format date
            String realTime = dateTime.toString().replace("T", " ");
            
	        Double priceDifference = close - prevClose;						// 등락폭 =  금일가 - 전일가
	        Double pricePercentage = (priceDifference / prevClose) * 100; 	//전일가대비 % = (등락폭 / 전일가)*100
        	DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        	
        	StockInfoVo stockVo = new StockInfoVo();
        	HashMap<String, String> input_map = new HashMap<>();           	
        	input_map.put("stock_code", stock_code);
        	input_map.put("group_id", group_id);
        		
        	// Stock 기준정보 조회
        	stockVo =  stockCodeInfoDao.selectStockInfo(input_map);
        	
        	if(stockVo == null) {
        		stockUtil.saveStockCodeInfo(stock_code);
        	}else {
        		// Stock 기준정보
	        	infoData.put("id", stockVo.getStock_id());
	        	infoData.put("code", stockVo.getStock_code());
	        	infoData.put("name", stockVo.getStock_ko_name());
	        	infoData.put("market", stockVo.getStock_market());
	        	infoData.put("url", stockVo.getStock_url());
	        	infoData.put("group_div", stockVo.getStock_group());
	        	infoData.put("group_id", group_id);
	        	infoData.put("country_code", stockVo.getStock_country_code());
	        	infoData.put("stock_type", stockVo.getStock_type());
	        	
	        	// Stock Info 실시간 정보
	        	infoData.put("realTime", realTime);
	        	infoData.put("open",decimalFormat.format(open));
	        	infoData.put("close",decimalFormat.format(close));
	        	infoData.put("prevClose", decimalFormat.format(prevClose));
	        	infoData.put("volume", decimalFormat.format(volume));
	        	infoData.put("volumeValued", decimalFormat.format(Math.floor(volumeValued)));
	        	infoData.put("priceDifference", decimalFormat.format(priceDifference));
	            infoData.put("pricePercentage", decimalFormat.format(pricePercentage));
        	}

        	//구매 종목 손익 계산
        	StockPortfolioVo calcVo = new StockPortfolioVo();
			calcVo = stockCodeInfoDao.selectPriceCalculate(input_map);
    			
			if(calcVo != null && Integer.parseInt(calcVo.getTotal_quantity()) > 0) {
				double totalPrice = Double.parseDouble(calcVo.getTotal_purchase_price());	
	        	double avgQuantity = Double.parseDouble(calcVo.getTotal_quantity());		
	        	double avgPrice = (totalPrice/avgQuantity);	// 평균 단가
	        	double avgPriceDifference = (close - avgPrice) * avgQuantity;	//손익가 =  (금일가 - 평균단가) * 평균갯수
	        	//avgPriceDifference = (avgPriceDifference * 0.015)+avgPriceDifference;
	        	//System.out.println("avgPriceDifference=========2======="+avgPriceDifference);
	        	double avgPercentage = ((close - avgPrice) / avgPrice) * 100; 	//전일가대비 % = (등락폭 / 전일가)*100
	        	avgPrice =  (groupMarket.equals("true"))? avgPrice : Math.round (avgPrice);		// 평균 단가
	        	infoData.put("stock_price_roi", decimalFormat.format(avgPriceDifference));		//손익 금액
	        	infoData.put("avg_purchase_price", String.valueOf((avgPrice)));					//평균 단가
	        	infoData.put("avg_pricePercentage",decimalFormat.format(avgPercentage));		//손익률 %
	        	infoData.put("total_purchase_price",decimalFormat.format(totalPrice));			//총 구입가
	        	infoData.put("total_quantity",decimalFormat.format(avgQuantity));				//평균 갯수
			}
			
			//SMA UP,DOWN Data
        	//String stock_sma = stockVo.getStock_sma();
        	//if(stock_sma != null) {
        		//infoData.put("smaAnalysis",stockUtil.settingSMA(stock_sma));				//SMA
        	//}

        	//MACD Signal Data
        	//String macd_signal = stockVo.getStock_macd_signal();
        	//String stock_sma5_mon_price = stockVo.getStock_sma5_mon_price();
        	//infoData.put("stock_sma5_mon_price",stock_sma5_mon_price);				// CTZ  stock_sma5_mon_price
			
//        	if(macd_signal != null) {
//        		String streaksignal = stockVo.getStock_macd_streaksignal();
//        		String streakdays = stockVo.getStock_macd_streakdays();
//        		String html ="";
//        		
//        		html +="<b class=\"text-xxs text-"+(macd_signal.equals("BUY")?"red" : "blue")+"\" title=MACD_SIGNAL>"+macd_signal+" </b>";
//        		html +="<b class=\"text-xxs text-"+(streaksignal.equals("UP")?"red" : "blue")+"\" title=MACD_OSC구분>"+"<i class=\"fas fa-arrow-"+streaksignal+" text-xxs\"></i>"+"</b>";
//        		html +="<b class=\"text-xxs text-"+(streaksignal.equals("UP")?"red" : "blue")+"\" title=MACD_변동일수> "+streakdays+"일</b>";
//        		infoData.put("macdAnalysis",html);				//macd
//        	}
			
        	//사용자가 조회한 Stock Code 저장
//        	if(!market_div.equals("true")) {
//        		//User Default Stock Code Update
//                HashMap<String, String> userStock = new HashMap<String, String>();
//                userStock.put("user_id", "jhroh");
//                userStock.put("stock_code", stockVo.getStock_code());
//                stockCodeInfoDao.updateUserStockCode(userStock); 
//        	}
            DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setSingleData(infoData);

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
	}
	
	
	/**
	 *  조회 종목 저장 stock info insert
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	@RequestMapping("/finance/realStockPrice.do")
	public void realStockPrice(HttpServletRequest req, HttpServletResponse res) throws Exception{
		
        String stock_code = req.getParameter("stock_code");
        List<Object> stockOJ = new ArrayList<Object>();
		try {
            // 공통 유틸 사용
            String result = AlphaSquareApiUtil.callCurrentPriceSingle(stock_code);
            JSONObject data = AlphaSquareApiUtil.getCurrentPriceData(stock_code, result);
				
			long dt = data.optLong("dt", 0L);
			double open = data.optDouble("open", 0.0);
			double high = data.optDouble("high", 0.0);
			double low = data.optDouble("low", 0.0);
			double close = data.optDouble("close", 0.0);
			stockOJ = new ArrayList<>();
			stockOJ.add(dt);
			stockOJ.add(open);
			stockOJ.add(high);
			stockOJ.add(low);
			stockOJ.add(close);
			
			System.out.println("==========="+dt+"========");
			System.out.println(stockOJ);
			System.out.println("==========="+dt+"========");
			
			HashMap<String,List> resultData = new HashMap<String,List>();
			resultData.put("obj",stockOJ);
			
          DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setSingleData(resultData);

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
	}
	
	/**
	 *  조회 종목 저장 stock info insert
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "static-access"})
	@RequestMapping("/finance/getChartData.do")
	public void getChartData(HttpServletRequest req, HttpServletResponse res) throws Exception{
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
		// 메서드 내
		String  chartObj 		= StringUtil.nvl(req.getParameter("chartObj"));
		
		JSONObject getParams 	= JSONObject.fromObject(chartObj);
		JSONObject infoData 	= getParams.getJSONObject("infoData");
		System.out.println("infoData==========================");
		System.out.println(infoData);
		System.out.println("infoData==========================");
		String  chartNm 		= getParams.getString("chartNm");
		String  period 			= getParams.getString("period");
		String  limit 			= getParams.getString("limit");
		
		// Extract 'singleData' first to access the nested fields
		JSONObject singleData = infoData.getJSONObject("singleData");

		String id = StringUtil.nvl(singleData.getString("id"));
		String code = StringUtil.nvl(singleData.getString("code"));
		String country_code = StringUtil.nvl(singleData.getString("country_code"));
		
		
		//String  id 				= StringUtil.nvl("2320");
		//String  code 			= StringUtil.nvl("003670");
		//String  country_code 	= StringUtil.nvl("KR");
		
     	SimpleDateFormat sdf = null;
        if(period.indexOf("minute") !=-1) {
        	sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }else {
        	sdf = new SimpleDateFormat("yyyy-MM-dd");
        }
       
        if(country_code.equals("KR")) {
        	sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        }

        if(country_code.equals("US")) {
        	sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        }
        
        String realTime_country= TimeUtil.getCountry();
        System.out.println("country_code===="+country_code);
        System.out.println("realTime_country===="+realTime_country);
        
        // Today 종목 현재가 조회
        String result = AlphaSquareApiUtil.callCurrentPriceSingle(code);
        JSONObject data = AlphaSquareApiUtil.getCurrentPriceData(code, result);
        
		//HashMap<String,List> resultData = new HashMap<String,List>();
		HashMap<String, Object> resultData = new HashMap<String, Object>();
		//Chart Options
		List<CodeVo> codevo = new ArrayList<CodeVo>(); 
		HashMap<String,String> modalmap = new HashMap<String,String>();
		modalmap.put("modal_id", chartNm);
		codevo = StockUtilController.getModalOptions(modalmap);
		HashMap<String, String> chartOptions = new HashMap<String, String>();
		for(CodeVo vo : codevo) {
			chartOptions.put(vo.getCode_id(), vo.getCode_value());
		}
		if(limit.equals("")) {
			limit = chartOptions.get("I-LIMIT");
		}
		resultData.put("chartOptions", codevo);
		
//		chartOptions.put("F-DOUBLECHART", "Y");
//		chartOptions.put("F-FLAGS", "Y");
//		chartOptions.put("F-MACDOSC", "Y");
//		chartOptions.put("F-SMA", "Y");
		
		try {
			if(data.size() > 0) {
				long dt = data.optLong("dt", 0L);
			    double open = data.optDouble("open", 0.0);
			    double high = data.optDouble("high", 0.0);
			    double low = data.optDouble("low", 0.0);
			    double close = data.optDouble("close", 0.0);
			    String current_candle_div ="false";
			    System.out.println("period================"+period);
			 // 타임스탬프 포맷 변경
		        Date date = new Date(dt);
		        String time = sdf.format(date);
		        Date parsedDate = sdf.parse(time);
		        Calendar calendar = Calendar.getInstance();
				calendar.setTime(parsedDate);
                long realTimestamp = calendar.getTimeInMillis();
                
                //금일 Data
		        List<Object> stockOJ = new ArrayList<>();
				stockOJ.add(realTimestamp);
				stockOJ.add(open);
				stockOJ.add(high);
				stockOJ.add(low);
				stockOJ.add(close);

				//Local Stock Time
				int realTime = calendar.get(Calendar.DAY_OF_MONTH);
				
				
			    if(period.equals("month") || period.equals("week") ) {
			    	current_candle_div ="true";
			    }

				// today price 차트에 넣기
			    if(country_code.equals(realTime_country) && period.equals("day")) {
			    	current_candle_div ="true";
			    }
			    if(limit == null) {
			    	limit = "300";
			    }

	        	result = AlphaSquareApiUtil.callCandles(id, period, limit, current_candle_div);
	        	System.out.println("Chart Data 호출==========");
	        	//System.out.println(result);
	        	System.out.println("Chart Data 호출==========");
	        	org.json.JSONObject checkJson = new org.json.JSONObject(result);
	        	org.json.JSONArray checkData = checkJson.getJSONArray("data");
		        
		        if(checkData.length() > 0) {
			        String[] dataArray = result.substring(result.indexOf("[[") + 2, result.indexOf("]]")).split("\\],\\[");
					
					
					// chart date의 마지막 날짜로 금일 chart data를 넣을지 안넣을지 체크
					String[] lastChartDate = dataArray[dataArray.length-1].split(",");
	                double lastdateDouble = Double.parseDouble(lastChartDate[0].trim());
			        String lastcharttime = sdf.format(new Date((long) lastdateDouble));
			        Date lastTime = sdf.parse(lastcharttime);
			        
			        calendar.setTime(lastTime);
					int chartTime = calendar.get(Calendar.DAY_OF_MONTH);

					int addChart = 0;
					// today price 차트에 넣기
				    if(country_code.equals(realTime_country) && (period.equals("day") && realTime != chartTime)) {
				    	addChart = 1;
				    }

				    String[] newArray = new String[dataArray.length + addChart];
				    System.arraycopy(dataArray, 0, newArray, 0, dataArray.length);
				    
				    if(country_code.equals(realTime_country) && (period.equals("day") && realTime != chartTime)) {
				    	newArray[dataArray.length] = stockOJ.toString().replace("[", "").replace("]", "");
				    }
		        
					HashMap<String, String> input_map = new HashMap<>();
		        	input_map.put("period", period);
		        	input_map.put("country", realTime_country);
		        	input_map.put("doubleChart", chartOptions.get("F-DOUBLECHART"));
		        	
		        	
		        	
		            //Chart Data 날짜를 한국시간으로 맞춘다.
		        	ArrayList chartlist = null;
		            chartlist = stockUtil.getRebuildChartData(input_map,newArray);
		            resultData.put("data", chartlist);
		         // 마지막 종가 기준 전고점 계산
		            //StockHighPointVo lastClosePrevHigh = StockHighPointUtil.findPreviousHighForLastClose(chartlist);
		            //resultData.put("lastClosePrevHigh", lastClosePrevHigh);
		         // 전저점
		            //StockLowPointVo lastClosePrevLow = StockHighPointUtil.findPreviousLowForLastClose(chartlist);
		            //resultData.put("lastClosePrevLow", lastClosePrevLow);

		            // 1) 정배열 시작 구간 분석 (기본: 5,20,60,120,240)
		           
		            // 정배열 시작 구간 분석 (기본: 5,20,60,120,240)
		            Map<String, Object> bullishAlignmentStart =
		                    MovingAverageAlignmentModule.findDefaultBullishAlignmentAsMapFromChartList(chartlist);
		            if (bullishAlignmentStart != null) {
		                resultData.put("bullishAlignmentStart", bullishAlignmentStart);
		            }

		            // 마지막 종가 기준 전고점 계산
		           // StockHighPointVo lastClosePrevHigh = StockHighPointUtil.findPreviousHighForLastClose(chartlist);
		           // resultData.put("lastClosePrevHigh", lastClosePrevHigh);
		            // 전저점
		           // StockLowPointVo lastClosePrevLow = StockHighPointUtil.findPreviousLowForLastClose(chartlist);
		           // resultData.put("lastClosePrevLow", lastClosePrevLow);
		            
		            //DAY ChartDATA에서 Month Chart Data 추출 (일봉 DATA로 월봉 DATA 만들기)
		            if(period.equals("day") && chartOptions.get("F-DOUBLECHART").equals("Y")) {
		            	// 비교할 월 데이터를 찾기 위한 날짜 포맷
						Gson gson = new Gson();
						ParseStockDataVo stockData = gson.fromJson(result.replaceAll("null", "0"), ParseStockDataVo.class);
						List<StockDataVo> dataList = ParseStockDataVo.generateData(stockData.getData());
		            	List<StockDataVo> monthlyDataList =  stockUtil.generateMonthlyData(dataList, country_code);	// 월봉 차트 DATA 생성
		            	ArrayList monthChartlist =  new ArrayList();
		    	        for (StockDataVo monthdata : monthlyDataList) {
		    	        	
		    	        	// 타임스탬프 포맷 변경
			 	            Date monthDate = new Date(monthdata.getDate());
			 	            String monthTime = sdf.format(monthDate);
			 	            Date parsedMonthDate = sdf.parse(monthTime);
			 	            calendar.setTime(parsedMonthDate);

			 	            // 월봉 차트를 일봉 챠트 중간에 셋팅
			 	             int middleWorkingDay = stockUtil.getMiddleWorkingDay(calendar);
			 	             calendar.set(Calendar.DAY_OF_MONTH, middleWorkingDay);
			 	            //월봉 차트 high low 셋팅
			                long parsedTimestamp = calendar.getTimeInMillis();
		    	            List<Object> stockMonthOJ = new ArrayList<>();
		    	            stockMonthOJ.add(parsedTimestamp);
		    	            stockMonthOJ.add(monthdata.getOpen());
		    	            stockMonthOJ.add(monthdata.getHigh());
		    	            stockMonthOJ.add(monthdata.getLow());
		    	            stockMonthOJ.add(monthdata.getClose());
		    	            monthChartlist.add(stockMonthOJ);
		    	        }

			            // 월봉 차트에 금일 종가 추가 (더블차트 아님)
			            int lastdata =  monthChartlist.size()-1;
			            Object lastvalues = monthChartlist.get(lastdata);
			            
			            ArrayList<Object> lastList = (ArrayList<Object>) lastvalues;
			            long lasttimestamp = (long) lastList.get(0);
			            double lastopen = (double) lastList.get(1);
			            double lasthigh = (double) lastList.get(2);
			            double lastlow = (double) lastList.get(3);
			            
			            //마지막 월이 현재 날짜보다 크면 DAY CHART 마지막 전일자로 월봉을 넣는다.  
		 	           	long currentTimestamp = System.currentTimeMillis();
		 	           	long parsedTimestamp = 0;
		 	           	if (lasttimestamp > currentTimestamp) {
			 	           	int chartlistdata =  chartlist.size()-2;
				            Object lastvalues2 = chartlist.get(chartlistdata);
				            ArrayList<Object> lastList2 = (ArrayList<Object>) lastvalues2;
				            long last2timestamp = (long) lastList2.get(0);
		 	           		parsedTimestamp = last2timestamp;
		 	           	}else {
		 	           		parsedTimestamp = lasttimestamp;
		 	           	}
			 	          
		 	            //월봉 차트 high low 셋팅
			            List<Object> monthOJ = new ArrayList<>();
			            monthOJ.add(parsedTimestamp);
			            monthOJ.add(lastopen);
			            monthOJ.add(lasthigh > high ?lasthigh : high);
			            monthOJ.add(low > lastlow ?lastlow : low);
			            monthOJ.add(close);
			            monthChartlist.set(lastdata, monthOJ);
				            
		            	resultData.put("monthData", monthChartlist);
		            }
		            
		            if(!period.equals("month")){
		            	//Chart 60-240  골드/데드 Cross Flag Data SIGNAL 가져오기
		            	if(chartOptions.get("F-FLAGS").equals("Y")) {
		            		ArrayList flaglist = null;
		            		flaglist = stockUtil.stockCross(newArray);
				            if(flaglist.size() > 0) {
				            	resultData.put("flag", flaglist);
				            	//System.out.println("**********Chart 60-240  골드/데드 Cross*"+flaglist.size()+"*************");
				            }
		            	}
		            	//MACDOSC SIGNAL Cross Data 가져오기
		            	if(chartOptions.get("F-MACDOSC").equals("Y")) {
		            		ArrayList signalList = null;
			            	signalList = MACDOSCCalculator.getMACDOSCData(newArray);
			            	if(signalList.size() > 0) {
			            		resultData.put("macdosc", signalList);
			            		//System.out.println("**********MACDOSC Cross Data*"+signalList.size()+"**************");
			            	}	
		            	}
		            }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			List<Object> cs_message = new ArrayList<>();
			cs_message.add(e.getMessage());
			resultData.put("cs_message", cs_message);
			throw new SchedulerException(this.getClass().getName()+".getChartData() : " + e.getLocalizedMessage());
		}
		
		JSONObject obj = JSONObject.fromObject(resultData);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}
	
	/**
	 *  그룹 목록 조회
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/selectGroupStockInfo.do")
	public void c_selectGroupStockInfo(HttpServletRequest req, HttpServletResponse res) throws Exception{
		
		String  stock_code 		= StringUtil.nvl(req.getParameter("stock_code"));
		String  group_id 		= StringUtil.nvl(req.getParameter("group_id"));
		String  group_div 		= StringUtil.nvl(req.getParameter("group_div"));
		int size = 0;
		
		HashMap<String, String> map = new HashMap<>();
		map.put("group_id", group_id);
		map.put("stock_code", stock_code);
		map.put("group_div", group_div);
		List<StockInfoVo> list = new ArrayList<>();
		try {
			list = (List<StockInfoVo>)stockCodeInfoDao.selectGroupStockInfo(map);
			size = list.size();
		} catch (Exception e) {
			throw new SchedulerException(this.getClass().getName()+".c_selectUserAll() : " + e.getLocalizedMessage());
		}
		HashMap<String, String> resultMap = new HashMap<String, String>();		
		resultMap.put("size", String.valueOf(size));
		
		//JSON Object 변환
		JSONObject obj = JSONObject.fromObject(resultMap);
		res.setContentType("text/javascript;charset=UTF-8");
		PrintWriter pw = res.getWriter();
		pw.println(obj.toString());
		pw.flush();
		pw.close();
	}

	/**
	 *  그룹 DML 작업
	 *  @param  조건  : 
	 *  @return 사용자 목록
	 * @throws Exception 
	 */
	@RequestMapping("/finance/portfolioGroupEvent.do")
	public void c_portfolioGroupEvent(HttpServletRequest req, HttpServletResponse res) throws Exception{
		
		String  eventCode 		= StringUtil.nvl(req.getParameter("eventCode"));
		String  group_id 		= StringUtil.nvl(req.getParameter("group_id"));
		String  group_name		= StringUtil.nvl(req.getParameter("group_name"));
		String  group_desc		= StringUtil.nvl(req.getParameter("group_desc"));
		String  groupMarket		= StringUtil.nvl(req.getParameter("groupMarket"));
		System.out.println("c_portfolioGroupEvent groupMarket============="+groupMarket);

		HashMap<String, String> map = new HashMap<>();
		map.put("eventCode", eventCode);
		map.put("group_id", group_id);
		map.put("group_name", group_name);
		map.put("group_desc", group_desc);
		map.put("groupMarket", groupMarket);
		try {
			stockCodeInfoDao.portfolioGroupEvent(map);
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
	 *  그룹에 관심 Stock 등록/삭제
	 *  @param 
	 *  @return 
	 *  @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/interastStockEvent.do")
    public void interastStockEvent(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		
		String  stock_code 		= StringUtil.nvl(req.getParameter("stock_code"));
		String  eventCode 		= StringUtil.nvl(req.getParameter("eventCode"));
		String  group_id 		= StringUtil.nvl(req.getParameter("group_id"));
		String  group_div 		= StringUtil.nvl(req.getParameter("group_div"));
		System.out.println("stock_code==="+stock_code);
		System.out.println("eventCode==="+eventCode);
		System.out.println("group_id==="+group_id);
		System.out.println("group_div==="+group_div);
		// 개발중이라서 주석해놓음
		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		
		HashMap<String, String> map = new HashMap<String, String>();
		String result_code ="";
		String result_msg ="";
		map.put("eventCode", eventCode);
		map.put("stock_code", stock_code);
		map.put("group_id", group_id);
		map.put("group_div", group_div);
		map.put("in_user_id" ,userSession.user_id);	
		List<StockInfoVo> list = new ArrayList<>();
		try {
			list = (List<StockInfoVo>)stockCodeInfoDao.selectGroupStockInfo(map);
			System.out.println("selectGroupStockInfo==============="+list.size());
			if(eventCode.equals("I") && list.size() > 0) {
				result_code ="error";
				result_msg = "등록된 정보입니다.";
			}else{
				stockCodeInfoDao.interastStockEvent(map);
				result_code ="success";
				switch (eventCode) {
				case "I":
					result_msg = "정상 등록 되었습니다.";	
					break;
				case "D":
					result_msg = "정상 삭제 되었습니다.";	
					break;
				default:
					break;
				}
			};
		      
	    	DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);
			
	    	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
	}
	
	/**
	 *  그룹에 관심 Stock 등록/삭제
	 *  @param 
	 *  @return 
	 *  @throws IOException 
	 */
	@RequestMapping("/finance/portfolioGroupSort.do")
	public void portfolioGroupSort(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		
		HashMap<String, String> map = new HashMap<String, String>();
		String  group_List 		= StringUtil.nvl(req.getParameter("group_List"));
		
		// 개발중이라서 주석해놓음
		
		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		map.put("group_List", group_List);
		map.put("in_user_id" ,userSession.user_id);
		
		try {
			stockCodeInfoDao.portfolioGroupSort(map);
		   	DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);
			
	    	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
	}
	
	/**
	 *  그룹리스트 stock 개별 조회
	 *  @param  조건  : 
	 *  @return 
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "static-access"})
	@RequestMapping("/finance/selectGroupStockList.do")
    public ModelAndView selectGroupStockList(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
		
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map2 = RequestHandler.extractParameters(req);
				
    	System.out.println("===========selectGroupStockList===============");
    	String groupMarket = StringUtil.nvl(req.getParameter("groupMarket"));	//국가
    	String group_id = StringUtil.nvl(req.getParameter("interastGroup"));	//그룹ID
    	String group_div = StringUtil.nvl(req.getParameter("group_div"));		//그룹 구분
    	
		System.out.println("selectGroupStockList groupMarket============="+groupMarket);

		// 수정(정상)
		if("N".equals(groupMarket)) {
		    groupMarket = "KR";
		}else {
		    groupMarket = "US";
		}
		
    	System.out.println("group_div========="+group_div);
    	
    	//Chart Options
		List<CodeVo> codevo; 
		HashMap<String,String> modalmap = new HashMap<String,String>();
		modalmap.put("modal_id", "StockList");
		codevo = StockUtilController.getModalOptions(modalmap);
		HashMap<String, String> map = new HashMap<String, String>();
    	map.put("group_id", group_id);
    	map.put("groupMarket", groupMarket);
    	map.put("group_div", group_div);
    	if(codevo.size() > 0) {
    		for(CodeVo vo : codevo) {
    			if(vo.getCode_id().contains("-")) {
    				String[] id = vo.getCode_id().split("-");
        			if(vo.getDefault_yn().equals("Y")) {
        				map.put(id[1], vo.getCode_value());
        			}
    			}else {
    				if(vo.getDefault_yn().equals("Y")) {
        				map.put("sortField", vo.getCode_id());
        				map.put("sortOrder", vo.getCode_value());
        			}
    			}
    		}
    	}
    	List<StockInfoVo> list = new ArrayList<StockInfoVo>();

		try {			
			list = 	(List<StockInfoVo>) stockCodeInfoDao.selectGroupStockList(map);
			
			// 보유 종목 금액 계산
			list = stockUtil.stockCurrent(list, map);
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("listVo", list);
			resParams.put("view_Name", "finance/stock/sub/interestStockList");
			
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/selectHoldingStocks.do")
	public void selectHoldingStocks(HttpServletRequest req, HttpServletResponse res) {
		HashMap<String, String> map = RequestHandler.extractParameters(req);

		String market = StringUtil.nvl(req.getParameter("market"));
		String groupMarket = StringUtil.nvl(req.getParameter("groupMarket"));

		if (groupMarket == null || groupMarket.trim().isEmpty()) {
			if ("A".equalsIgnoreCase(market)) {
				groupMarket = "US";
			} else {
				groupMarket = "KR";
			}
		} else if ("N".equalsIgnoreCase(groupMarket)) {
			groupMarket = "KR";
		} else if ("A".equalsIgnoreCase(groupMarket)) {
			groupMarket = "US";
		}

		map.put("groupMarket", groupMarket);

		try {
			List<StockInfoVo> list = (List<StockInfoVo>) stockCodeInfoDao.selectPortfolioStockList(map);
			if (list == null) {
				list = new ArrayList<StockInfoVo>();
			}

			try {
				list = stockUtil.stockCurrent(list, map);
			} catch (Exception e) {
				e.printStackTrace();
			}

			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setData(list);

			ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			e.printStackTrace();
			ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/selectPositionEventList.do")
	public void selectPositionEventList(HttpServletRequest req, HttpServletResponse res) {
		String group_id = StringUtil.nvl(req.getParameter("group_id"));
		String stock_code = StringUtil.nvl(req.getParameter("stock_code"));
		int limit = 5;
		try {
			limit = Integer.parseInt(StringUtil.nvl(req.getParameter("limit"), "5"));
		} catch (Exception ignore) {}
		if (limit <= 0) limit = 5;
		if (limit > 50) limit = 50;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("group_id", group_id);
		map.put("stock_code", stock_code);

		try {
			List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) stockCodeInfoDao.selectPositionEventList(map);
			if (list == null) {
				list = new ArrayList<HashMap<String, Object>>();
			}
			if (list.size() > limit) {
				list = list.subList(0, limit);
			}
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setData(list);
			ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			e.printStackTrace();
			ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/selectChartEventMarkers.do")
	public void selectChartEventMarkers(HttpServletRequest req, HttpServletResponse res) {
		String group_id = StringUtil.nvl(req.getParameter("group_id"));
		String stock_code = StringUtil.nvl(req.getParameter("stock_code"));
		int limit = 30;
		try {
			limit = Integer.parseInt(StringUtil.nvl(req.getParameter("limit"), "30"));
		} catch (Exception ignore) {}
		if (limit <= 0) limit = 30;
		if (limit > 200) limit = 200;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("group_id", group_id);
		map.put("stock_code", stock_code);

		try {
			List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) stockCodeInfoDao.selectChartEventMarkers(map);
			if (list == null) list = new ArrayList<HashMap<String, Object>>();
			if (list.size() > limit) list = list.subList(0, limit);
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setData(list);
			ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			e.printStackTrace();
			ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/selectPositionState.do")
	public void selectPositionState(HttpServletRequest req, HttpServletResponse res) {
		String group_id = StringUtil.nvl(req.getParameter("group_id"));
		String stock_code = StringUtil.nvl(req.getParameter("stock_code"));

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("group_id", group_id);
		map.put("stock_code", stock_code);

		try {
			HashMap<String, Object> row = stockCodeInfoDao.selectPositionState(map);
			if (row == null) row = new HashMap<String, Object>();
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setSingleData(row);
			ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			e.printStackTrace();
			ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/selectPortfolioGroupList.do")
    public ModelAndView c_comboboxPortfolioGroup(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
		
		String groupMarket =  req.getParameter("groupMarket");
		String use_flag =  req.getParameter("use_flag");
		String flag =  req.getParameter("flag");
		
		System.out.println("selectPortfolioGroupList groupMarket============="+groupMarket);

    	HashMap<String, String> map = new HashMap<String, String>();
    	if(groupMarket.equals("N")) {
    		groupMarket = "KR";
    	}else {
    		groupMarket = "US";
    	}
    	map.put("groupMarket", groupMarket);
    	map.put("use_flag", use_flag);
    	map.put("flag", flag);
    	
    	List<StockGroupVo> list;
    	
		try {			
			list = (List<StockGroupVo>) stockCodeInfoDao.selectPortfolioGroupList(map);
			
			
			HashMap<String, Object> resParams = new HashMap<String, Object>();
			resParams.put("groupList", list);
			if(flag.equals("combobox")) {
				resParams.put("view_Name", "finance/stock/sub/group_combo");
			}else {
				resParams.put("view_Name", "finance/stock/sub/interestGroupList");
			}
			return ResponseHandler.handleSuccess(ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resParams); // 성공 처리
		} catch (Exception e) {
			return ResponseHandler.handleException(ResultMsg.ERROR_CODE, e, this.getClass().getName(), null); // 예외 처리
		}
	}

	/**
	 *  그룹에 관심 Stock 등록/삭제
	 *  @param 
	 *  @return 
	 *  @throws IOException 
	 */
	@RequestMapping("/finance/priceSettingEvent.do")
    public void priceSettingEvent(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		
		HashMap<String, String> map = new HashMap<String, String>();
		String  group_id 		= StringUtil.nvl(req.getParameter("group_id"));
		String  stock_code 		= StringUtil.nvl(req.getParameter("stock_code"));
		String  eventCode 		= StringUtil.nvl(req.getParameter("eventCode"));
		String  order_div 		= StringUtil.nvl(req.getParameter("order_div"));
		String  bidRate 		= StringUtil.nvl(req.getParameter("bidRate"));
		String  price_description 		= StringUtil.nvl(req.getParameter("price_description"));
		String  priceCount 		= StringUtil.nvl(req.getParameter("priceCount"));
		String  totalOrderPrice 		= StringUtil.nvl(req.getParameter("totalOrderPrice"));
		String  start_price 		= StringUtil.nvl(req.getParameter("startprice"));
		String  end_price 		= StringUtil.nvl(req.getParameter("endprice"));
		
		System.out.println("====================price_description"+price_description);
		
		// 개발중이라서 주석해놓음
		//UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		
		//if(userSession.user_id.equals("")){
		//	mav.addObject(ResultMsg.RESULT_MSG, "비정상적인 접근입니다.");
		//	mav.setViewName("comm/result/result_msg_script");
		//	return mav;
		//}
		
		map.put("eventCode", eventCode);
		map.put("group_id", group_id);
		map.put("stock_code", stock_code);
		map.put("start_price", start_price.replaceAll(",", ""));
		map.put("end_price", end_price.replaceAll(",", ""));
		map.put("priceCount", priceCount);
		map.put("order_div", order_div);
		map.put("bidRate", bidRate);
		map.put("price_description", price_description);
		map.put("close_flag", "N");
		map.put("totalOrderPrice", totalOrderPrice);
		map.put("in_user_id" ,"jhroh");
		
		try {
			stockCodeInfoDao.priceSettingEvent(map);
			
		  	DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);
			
	    	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
	}

	/**
	 *  그룹에 관심 Stock 등록/삭제
	 *  @param 
	 *  @return 
	 *  @throws IOException 
	 */
	@RequestMapping("/finance/holdingPositionEvent.do")
    public void holdingPositionEvent(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {

		// WF-2-4: 보유종목 관리(ADD/DELETE/AVERAGE_DOWN)
		String action = StringUtil.nvl(req.getParameter("action")).toUpperCase(); // ADD | DELETE | AVERAGE_DOWN
		String group_id = StringUtil.nvl(req.getParameter("group_id"));
		String stock_code = StringUtil.nvl(req.getParameter("stock_code"));
		String qty = StringUtil.nvl(req.getParameter("qty"), "1");
		String price = StringUtil.nvl(req.getParameter("price"), "0").replaceAll(",", "");
		String desc = StringUtil.nvl(req.getParameter("description"));

		UserSession userSession = (UserSession) req.getSession().getAttribute(UserSession.KEY);
		String userId = (userSession == null || userSession.user_id == null || userSession.user_id.trim().isEmpty()) ? "jhroh" : userSession.user_id;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("group_id", group_id);
		map.put("stock_code", stock_code);
		map.put("in_user_id", userId);
		map.put("close_flag", "N");

		try {
			if ("ADD".equals(action) || "AVERAGE_DOWN".equals(action)) {
				map.put("eventCode", "I");
				map.put("start_price", price);
				map.put("end_price", price);
				map.put("priceCount", qty);
				map.put("order_div", "BUY");
				map.put("bidRate", "");
				map.put("totalOrderPrice", "");
				map.put("price_description", ("AVERAGE_DOWN".equals(action) ? "물타기" : "보유추가") + (desc.isEmpty() ? "" : (" - " + desc)));
				stockCodeInfoDao.priceSettingEvent(map);
			} else if ("DELETE".equals(action)) {
				// 종목 보유를 일괄 종료(논리 close)
				map.put("eventCode", "C");
				map.put("stock_seq", "");
				map.put("create_date", "");
				map.put("purchase_price", "0");
				stockCodeInfoDao.priceHistoryEvent(map);
			} else {
				ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "지원하지 않는 action 입니다.", null);
				return;
			}

			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);
			ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
		}
	}

	@RequestMapping("/finance/priceHistoryEvent.do")
    public void priceHistoryEvent(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		String  chartObj 		= StringUtil.nvl(req.getParameter("chartObj"));
		JSONObject getParams = JSONObject.fromObject(chartObj);
		JSONObject infoData = getParams.getJSONObject("infoData");
		
		String  group_id 		= StringUtil.nvl(infoData.getString("group_id"));
		String  code 			= StringUtil.nvl(infoData.getString("code"));
		
		String  eventCode 		= StringUtil.nvl(getParams.getString("eventCode"));
		String  stock_seq 		= StringUtil.nvl(getParams.getString("stock_seq"));
		String  create_date 		= StringUtil.nvl(getParams.getString("create_date"));
		String  purchase_price 		= StringUtil.nvl(getParams.getString("purchase_price"));

		System.out.println("eventCode===="+ eventCode);
		System.out.println("stock_code===="+ code);
		System.out.println("group_id===="+ group_id);
		System.out.println("purchase_price===="+ purchase_price);
		
		// 개발중이라서 주석해놓음
		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("eventCode", eventCode);
		map.put("stock_seq", stock_seq);
		map.put("group_id", group_id);
		map.put("stock_code", code);
		map.put("create_date", create_date);
		map.put("purchase_price", purchase_price.replaceAll(",", ""));
		map.put("close_flag", "N");
		map.put("in_user_id" ,userSession.user_id);	
		try {
			stockCodeInfoDao.priceHistoryEvent(map);
			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
			resultVo.setResult_msg(ResultMsg.SUCCESS_MSG);
			
	    	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
	}

	@SuppressWarnings("unchecked")
	@RequestMapping({ "/finance/selectPriceHistory.do" })
    public void selectPriceHistory(final HttpServletRequest req, final HttpServletResponse res) throws SchedulerException, IOException {
        String stock_code = StringUtil.nvl(req.getParameter("stock_code"));
        String group_id = StringUtil.nvl(req.getParameter("group_id"));
        
        String draw = StringUtil.nvl(req.getParameter("draw"));
        String start = StringUtil.nvl(req.getParameter("start"));
        String length = StringUtil.nvl(req.getParameter("length"));
        int totalPagesize = Integer.parseInt(start) + Integer.parseInt(length);
        int currentPageSize = Integer.parseInt(start);
        
        int size = 0;
        int total_cnt = 0;
        List<StockPortfolioVo> list = null;
        
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("stock_code", stock_code);
        map.put("group_id", group_id);
        map.put("totalPagesize", String.valueOf(totalPagesize));
        map.put("currentPageSize", String.valueOf(currentPageSize));
        
        try {
            list = (List<StockPortfolioVo>)stockCodeInfoDao.selectPriceHistory(map);
            size = list.size();
            if (size > 0) {
                total_cnt = Integer.parseInt(list.get(0).getTotal_count());
            }
	    	DataTableSettingVo resultVo = new DataTableSettingVo();
	    	resultVo.setData(list);
	    	resultVo.setDraw(draw);
	        resultVo.setStart_no(Integer.parseInt(start));
	    	resultVo.setPage_length(Integer.parseInt(length));
	    	resultVo.setRecordsFiltered(total_cnt);
	    	resultVo.setRecordsTotal(total_cnt);
	
	    	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
    }

	/**
	 *  그룹에 관심 Stock 등록/삭제
	 *  @param 
	 *  @return 
	 *  @throws IOException 
	 */
	@RequestMapping("/finance/selectPriceCalculate.do")
    public void selectPriceCalculate(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		String  chartObj 		= StringUtil.nvl(req.getParameter("chartObj"));
		JSONObject getParams 	= JSONObject.fromObject(chartObj);
		JSONObject infoData 	= getParams.getJSONObject("infoData");
		
		String  group_id 	= StringUtil.nvl(infoData.getString("group_id"));
		String  code 		= StringUtil.nvl(infoData.getString("code"));
		// 개발중이라서 주석해놓음
		UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("group_id", group_id);
		map.put("stock_code", code);
		map.put("in_user_id" ,userSession.user_id);
		StockPortfolioVo calcVo = new StockPortfolioVo();
		HashMap<String, String> resultMap = new HashMap<String, String>();		
		try {
			calcVo = stockCodeInfoDao.selectPriceCalculate(map);
			if(calcVo != null && Integer.parseInt(calcVo.getTotal_quantity()) > 0) {
				DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
				
				double close = Double.parseDouble(infoData.getString("close").replaceAll(",", ""));
	        	
				double totalPrice = Double.parseDouble(calcVo.getTotal_purchase_price());	
	        	double avgQuantity = Double.parseDouble(calcVo.getTotal_quantity());		
	        	double avgPrice = (totalPrice/avgQuantity);	// 평균 단가
	        	double avgPriceDifference = (close - avgPrice) * avgQuantity;	//손익가 =  (금일가 - 평균단가) * 평균갯수
	        	double avgPercentage = ((close - avgPrice) / avgPrice) * 100; 	//전일가대비 % = (등락폭 / 전일가)*100
	        				        
	        	resultMap.put("stock_price_roi", decimalFormat.format(avgPriceDifference));	//손익 금액
	    		resultMap.put("avg_purchase_price", decimalFormat.format(avgPrice));		//평균 단가
	    		resultMap.put("avg_pricePercentage",decimalFormat.format(avgPercentage));	//손익률 %
	    		resultMap.put("total_purchase_price",decimalFormat.format(totalPrice));		//총 구입가
	    		resultMap.put("total_quantity",decimalFormat.format(avgQuantity));			//평균 갯수
			}
		
        	DataTableSettingVo resultVo = new DataTableSettingVo();
        	resultVo.setSingleData(resultMap);

        	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
        }
	}

	@RequestMapping("/finance/holdingAverageDownPreview.do")
    public void holdingAverageDownPreview(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		// WF-2-4: 보유종목 물타기 프리뷰(예상 평단/손익률)
		String group_id = StringUtil.nvl(req.getParameter("group_id"));
		String stock_code = StringUtil.nvl(req.getParameter("stock_code"));
		String addQtyStr = StringUtil.nvl(req.getParameter("add_qty"), "0");
		String addPriceStr = StringUtil.nvl(req.getParameter("add_price"), "0").replaceAll(",", "");
		String currentPriceStr = StringUtil.nvl(req.getParameter("current_price"), "").replaceAll(",", "");

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("group_id", group_id);
		map.put("stock_code", stock_code);

		HashMap<String, String> resultMap = new HashMap<String, String>();
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

		try {
			StockPortfolioVo calcVo = stockCodeInfoDao.selectPriceCalculate(map);

			double addQty = 0.0;
			double addPrice = 0.0;
			double currentPrice = 0.0;
			try { addQty = Double.parseDouble(addQtyStr.replaceAll(",", "")); } catch (Exception ignore) {}
			try { addPrice = Double.parseDouble(addPriceStr); } catch (Exception ignore) {}
			try { currentPrice = Double.parseDouble(currentPriceStr); } catch (Exception ignore) {}
			if (currentPrice <= 0) currentPrice = addPrice;

			double beforeQty = 0.0;
			double beforeTotal = 0.0;
			if (calcVo != null) {
				try { beforeQty = Double.parseDouble(StringUtil.nvl(calcVo.getTotal_quantity(), "0")); } catch (Exception ignore) {}
				try { beforeTotal = Double.parseDouble(StringUtil.nvl(calcVo.getTotal_purchase_price(), "0")); } catch (Exception ignore) {}
			}

			double beforeAvg = (beforeQty > 0 ? beforeTotal / beforeQty : 0.0);
			double afterQty = beforeQty + addQty;
			double afterTotal = beforeTotal + (addPrice * addQty);
			double afterAvg = (afterQty > 0 ? afterTotal / afterQty : 0.0);

			double beforePct = (beforeAvg > 0 && currentPrice > 0) ? ((currentPrice - beforeAvg) / beforeAvg * 100.0) : 0.0;
			double afterPct = (afterAvg > 0 && currentPrice > 0) ? ((currentPrice - afterAvg) / afterAvg * 100.0) : 0.0;

			resultMap.put("before_qty", decimalFormat.format(beforeQty));
			resultMap.put("before_avg", decimalFormat.format(beforeAvg));
			resultMap.put("after_qty", decimalFormat.format(afterQty));
			resultMap.put("after_avg", decimalFormat.format(afterAvg));
			resultMap.put("before_pct", decimalFormat.format(beforePct));
			resultMap.put("after_pct", decimalFormat.format(afterPct));
			resultMap.put("add_qty", decimalFormat.format(addQty));
			resultMap.put("add_price", decimalFormat.format(addPrice));
			resultMap.put("current_price", decimalFormat.format(currentPrice));

			DataTableSettingVo resultVo = new DataTableSettingVo();
			resultVo.setSingleData(resultMap);
			ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
		} catch (Exception e) {
			ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/finance/searchStocksKeyword.do")
	public void searchStocksKeyword(HttpServletRequest req, HttpServletResponse res) throws Exception {

	    HashMap<String, String> map = RequestHandler.extractParameters(req);
	    DataTableSettingVo resultVo = new DataTableSettingVo();
	    try {
			String keyword = StringUtil.nvl(req.getParameter("in_stockCode"));
			String result = AlphaSquareApiUtil.callKeywordSingle(keyword);
			System.out.println(result);
	        // 2. KIS 상품기본조회 (search-info)로 종목 기본 정보 조회
	    	List<StockInfoVo> list =  new ArrayList<StockInfoVo>(); 
	        list = (List<StockInfoVo>) stockCodeInfoDao.searchStocksKeyword(map);

	        if (list == null) {
	        	
	            resultVo.setResult_code(ResultMsg.ERROR_CODE);
	            resultVo.setResult_msg("조회된 종목이 없습니다.");
	            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "조회된 종목이 없습니다.", resultVo);
	            return;
	        }

	        // DataTables 포맷에 맞게 세팅
	        resultVo.setData(list);
	        resultVo.setResult_code(ResultMsg.SUCCESS_CODE);
	        resultVo.setResult_msg("success");

	        ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, "success", resultVo);

	    } catch (IOException e) {
	        e.printStackTrace();

	        resultVo.setResult_code(ResultMsg.ERROR_CODE);
	        resultVo.setResult_msg("KIS 종목 조회 중 오류가 발생했습니다.");

	        ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "KIS 종목 조회 중 오류가 발생했습니다.", resultVo);
	    } catch (Exception e) {
	        e.printStackTrace();

	        resultVo.setResult_code(ResultMsg.ERROR_CODE);
	        resultVo.setResult_msg("종목 검색 처리 중 시스템 오류가 발생했습니다.");

	        ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "종목 검색 처리 중 시스템 오류가 발생했습니다.", resultVo);
	    }
	}

    /**
     * Ajax: 일자별 시세 조회
     * 파라미터:
     *   stockCode : 종목코드 (예: 005930)
     */
    @RequestMapping({ "/finance/kisDailyPriceData.do" })
    public void kisDailyPriceData(HttpServletRequest req, HttpServletResponse res) {

        String stockCode = req.getParameter("stockCode");
        if (stockCode == null || stockCode.trim().isEmpty()) {
            stockCode = "005930"; // 기본 삼성전자
        }

        try {
            InquireDailyPriceResult result = stockCodeInfoDao.getDailyPrices(stockCode);

            // 5) KIS 일자별 시세 → StockDataVo 리스트로 변환
            List<StockDataVo> list = new ArrayList<StockDataVo>();
            
            Output1[] outputs = result.getOutput1();
            for (Output1 o : outputs) {
                if (o == null) {
                    continue;
                }

                String dateStr = o.getStckBsopDate();   // yyyyMMdd
                String openStr = o.getStckOprc();
                String highStr = o.getStckHgpr();
                String lowStr = o.getStckLwpr();
                String closeStr = o.getStckClpr();
                String volStr = o.getAcmlVol();      // 누적 거래 대금 or 수량 필드 중 원하는 것을 선택

                if (dateStr == null || dateStr.length() != 8) {
                    continue;
                }
                
                LocalDate date = LocalDate.parse(dateStr, DATE_YYYYMMDD);
                long epochMillis = date.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();

                double open = StringUtil.parseDoubleSafe(openStr);
                double high = StringUtil.parseDoubleSafe(highStr);
                double low = StringUtil.parseDoubleSafe(lowStr);
                double close = StringUtil.parseDoubleSafe(closeStr);
                double volume = StringUtil.parseDoubleSafe(volStr);

                StockDataVo vo = new StockDataVo(epochMillis, open, high, low, close, volume);
                list.add(vo);
            }

            // 6) 날짜 오름차순 정렬 (차트용)
            Collections.sort(list, new Comparator<StockDataVo>() {
                @Override
                public int compare(StockDataVo o1, StockDataVo o2) {
                    return Long.compare(o1.getDate(), o2.getDate());
                }
            });
            System.out.println("list===================="+list.size());
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(list);
            resultVo.setSingleData(stockCode);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * Ajax: 일자별 시세 조회
     * 파라미터:
     *   stockCode : 종목코드 (예: 005930)
     */
    @RequestMapping({ "/finance/getCurrentPriceByInquirePrice.do" })
    public void getCurrentPriceByInquirePrice(HttpServletRequest req, HttpServletResponse res) {
    	
    	 HashMap<String, String> map = RequestHandler.extractParameters(req);
    	String stockCode = req.getParameter("in_stockCode");
    	if (stockCode == null || stockCode.trim().isEmpty()) {
    		stockCode = "005930"; // 기본 삼성전자
    	}
    	
    	InquirePriceResult inquirePriceResult = new InquirePriceResult();
    	try {
    		inquirePriceResult = stockCodeInfoDao.getCurrentPriceByInquirePrice(map);
    		
    		DataTableSettingVo resultVo = new DataTableSettingVo();
    		resultVo.setSingleData(inquirePriceResult);
    		
    		ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
    	} catch (Exception e) {
    		e.printStackTrace();
    		ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
    	}
    }
    
    /**
     * KIS 상품 기본조회 
     * - 엑셀 문서 기준: /uapi/domestic-stock/v1/quotations/search-info (TR: CTPF1604R)
     * - 파라미터: PDNO(종목코드), PRDT_TYPE_CD(300: 주식)
     */
    @RequestMapping("/finance/getSearchInfo.do")
    public void getSearchInfo(HttpServletRequest req, HttpServletResponse res) {
    	HashMap<String, String> map = RequestHandler.extractParameters(req);

        DataTableSettingVo resultVo = new DataTableSettingVo();

        try {
            // KIS OpenAPI 상품기본조회 호출 (엑셀 스펙 기반으로 구현된 Service)
            StockInfoVo info = stockCodeInfoDao.getSearchInfo(map);

            resultVo.setSingleData(info);

            if (info == null) {
                ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,"조회된 종목이 없습니다.",resultVo);
            } else {
            	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG,resultVo);
            }

        } catch (Exception e) {
        	e.printStackTrace();
        	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(),null);
        }
    }


    /**
     * KIS 상품 상세조회 
     * - 엑셀 문서 기준: /uapi/domestic-stock/v1/quotations/search-stock-info (TR: CTPF1002R)
     * - 파라미터: PDNO(종목코드), PRDT_TYPE_CD(300: 주식)
     * 
     * 파라미터:
     *   stockCode   : 종목코드 (PDNO, 예: 005930)
     *   prdtTypeCd  : 상품유형코드 (기본값 300 - 국내 주식)
     */
    @RequestMapping({ "/finance/getSearchStockInfo.do" })
    public void getSearchStockInfo(HttpServletRequest req, HttpServletResponse res) {

        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            // DAO에서 KIS OpenAPI 주식기본조회 호출
            StockInfoVo info = stockCodeInfoDao.getSearchStockInfo(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            if (info != null) {
                List<StockInfoVo> list = new ArrayList<StockInfoVo>();
                list.add(info);
                resultVo.setData(list);
            }

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Ajax: 국내주식기간별시세(일/주/월/년) – getInquireDailyItemchartprice 호출
     *
     * 파라미터
     *  - stockCode     : 종목코드 (예: 005930)
     *  - fromDate      : 시작일자 (YYYYMMDD 또는 YYYY-MM-DD)
     *  - toDate        : 종료일자 (YYYYMMDD 또는 YYYY-MM-DD)
     *  - periodDivCode : 기간분류코드 (D/W/M/Y, 기본 D)
     *  - orgAdjPrc     : 수정주가/원주가 (0/1, 기본 0 - 수정주가)
     */
    @SuppressWarnings("unchecked")
	@RequestMapping({ "/finance/kisItemchartpriceData.do" })
    public void kisItemchartpriceData(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String stockCode = StringUtil.nvl(req.getParameter("in_stockCode")).trim();
        String fromDate = StringUtil.nvl(req.getParameter("in_fromDate")).trim();
        String toDate = StringUtil.nvl(req.getParameter("in_toDate")).trim();
        String periodDivCode = StringUtil.nvl(req.getParameter("in_periodDivCode")).trim();
        String orgAdjPrc = StringUtil.nvl(req.getParameter("in_orgAdjPrc")).trim();
        
      //PARAMETER KEY VALUE Setting
      	HashMap<String, String> map = RequestHandler.extractParameters(req);

        if (stockCode.length() == 0) {
            stockCode = "005930";     // 기본: 삼성전자
        }

        LocalDate today = LocalDate.now();
        toDate = normalizeChartDateInput(toDate, today, "toDate");
        fromDate = normalizeChartDateInput(fromDate, today.minusDays(60), "fromDate");

        if (periodDivCode.length() == 0) {
            periodDivCode = "D";      // 기본: 일봉
        }
        if (orgAdjPrc.length() == 0) {
            orgAdjPrc = "0";          // 기본: 수정주가
        }

        map.put("in_stockCode", stockCode);
        map.put("in_fromDate", fromDate);
        map.put("in_toDate", toDate);
        map.put("in_periodDivCode", periodDivCode);
        map.put("in_orgAdjPrc", orgAdjPrc);

        try {
            List<StockDataVo> list = (List<StockDataVo>) stockCodeInfoDao.getInquireDailyItemchartprice(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            if (list == null) {
                list = new ArrayList<StockDataVo>();
            }
            System.out.println("list================="+list.size());
            resultVo.setData(list);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    private String normalizeChartDateInput(String value, LocalDate fallback, String fieldName) {
        String trimmed = StringUtil.nvl(value).trim();
        if (trimmed.length() == 0) {
            return fallback.format(DATE_YYYYMMDD);
        }

        try {
            if (trimmed.indexOf('-') >= 0) {
                return LocalDate.parse(trimmed, DATE_YYYY_MM_DD).format(DATE_YYYYMMDD);
            }
            return LocalDate.parse(trimmed, DATE_YYYYMMDD).format(DATE_YYYYMMDD);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " 형식이 올바르지 않습니다. yyyyMMdd 또는 yyyy-MM-dd 를 사용하세요.");
        }
    }
}
