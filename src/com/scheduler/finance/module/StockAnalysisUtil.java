package com.scheduler.finance.module;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scheduler.comm.util.GetHttpsURLConnection;
import com.scheduler.comm.util.ImageDownload;
import com.scheduler.comm.util.TimeUtil;
import com.scheduler.comm.util.VoSorter;
import com.scheduler.finance.vo.StockDataVo;
import com.scheduler.finance.vo.StockInfoVo;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class StockAnalysisUtil {
	
	//	매월 워킹데이 중간일자 찾기
	public static int getMiddleWorkingDay(Calendar calendar) {
        // 현재 월의 첫 날로 설정합니다.
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // 월요일(일자 2)부터 금요일(일자 6)까지의 날짜 수
        int workingDaysCount = 0;
        int middleWorkingDay = 0;

        while (calendar.get(Calendar.DAY_OF_MONTH) <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // 월요일(일자 2)부터 금요일(일자 6)까지인 경우
            if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
                workingDaysCount++;
            }
            // 월 중간일자 계산 (워킹데이의 중간)
            if (workingDaysCount == 10) {
                middleWorkingDay = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1); // 다음 날짜로 이동
        }
        middleWorkingDay = middleWorkingDay+1;
        return middleWorkingDay;
	}
	
	/**
	 *  Chart Data
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList getRebuildChartData(HashMap<String, String> input_map,String[] dataArray){
		
		String  period 		= input_map.get("period");
		String  country 		= input_map.get("country");
		String  doubleChart 	= input_map.get("doubleChart");
		              
		ArrayList list = null;
		try {
			
	        SimpleDateFormat sdf = null;
            if(period.indexOf("minute") !=-1) {
            	sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }else {
            	sdf = new SimpleDateFormat("yyyy-MM-dd");
            }
            
            //현재 시간 장열린곳 시간 타임
            if(country.equals("KR")){
            	sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            }else if(country.equals("US")){
            	sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            }
            list = new ArrayList();
	        for (String item : dataArray) {

	            String[] values = item.split(",");
	            double timestampDouble = Double.parseDouble(values[0].trim());
	            long timestamp = (long) timestampDouble;
	            double open = Double.parseDouble(values[1]);
	            double high = Double.parseDouble(values[2]);
	            double low = Double.parseDouble(values[3]);
	            double close = Double.parseDouble(values[4]);
	            
	            // 타임스탬프 포맷 변경
	            Date date = new Date(timestamp);
	            String time = sdf.format(date);
	            Date parsedDate = sdf.parse(time);
	            // 1초 추가
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTime(parsedDate);
	            if(period.indexOf("minute") ==-1) {
	            	calendar.add(Calendar.DAY_OF_MONTH, 1);
	            }
	            
	            //월봉 더블 챠트에 캔들을 월 중간에 놓기 위해 중간일로 고정
	            //if(period.equals("month") && doubleChart.equals("Y")) {
	            //    calendar.set(Calendar.DAY_OF_MONTH, getMiddleWorkingDay(calendar));
	            //}
	            
	            long parsedTimestamp = calendar.getTimeInMillis();
	            List<Object> stockOJ = new ArrayList<>();
	            stockOJ.add(parsedTimestamp);
	            stockOJ.add(open);
	            stockOJ.add(high);
	            stockOJ.add(low);
	            stockOJ.add(close);
	            list.add(stockOJ);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 *  StockCurrent Data
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */public static List<StockInfoVo> stockCurrent(List<StockInfoVo> list, HashMap<String, String> map) throws IOException {

		    String groupMarket = map.get("groupMarket");
		    String sortField = map.get("sortField");
		    String sortOrder = map.get("sortOrder");
		    List<String> groupList = new ArrayList<>();

		    // 종목코드 목록 구성 (null / 공백 방어)
		    for (int i = 0; i < list.size(); i++) {
		        StockInfoVo vo = list.get(i);
		        if (vo == null) {
		            continue;
		        }
		        String code = vo.getStock_code();
		        if (code == null) {
		            continue;
		        }
		        code = code.trim();
		        if (code.length() == 0) {
		            continue;
		        }
		        groupList.add(code);
		    }

		    // 조회할 종목이 없으면 그대로 반환
		    if (groupList.isEmpty()) {
		        return list;
		    }

		    String[] groups = groupList.toArray(new String[0]);
		    String codes = Arrays.toString(groups);
		    String encodedData = URLEncoder.encode(
		            codes.replace("[", "").replace("]", "").replace(" ", ""),
		            "UTF-8"
		    );

		    String url = "https://api.alphasquare.co.kr/data/v2/price/current-price?code=" + encodedData;
		    String result = GetHttpsURLConnection.getHttpsGet(url);

		    // 응답이 비어 있으면 그대로 반환
		    if (result == null || result.trim().length() == 0) {
		        return list;
		    }

		    // JSON 파싱은 한 번만
		    JSONObject json = JSONObject.fromObject(result);
		    DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

		    for (int i = 0; i < list.size(); i++) {

		        StockInfoVo vo = list.get(i);
		        if (vo == null) {
		            continue;
		        }

		        String code = vo.getStock_code();
		        if (code == null) {
		            continue;
		        }
		        code = code.trim();
		        if (code.length() == 0) {
		            continue;
		        }

		        // 해당 종목이 응답에 없으면 스킵 (delist, 에러 등)
		        JSONObject data = json.optJSONObject(code);
		        if (data == null || data.isNullObject()) {
		            // 필요하면 로그 남기기
		            // System.out.println("No price data for code: " + code);
		            continue;
		        }

		        // JSONNull 방어하면서 close / prev_close 추출
		        Object closeObj = data.get("close");
		        Object prevCloseObj = data.get("prev_close");

		        double close = 0.0;
		        double prevClose = 0.0;

		        if (closeObj != null && !(closeObj instanceof JSONNull)) {
		            close = data.getDouble("close");
		        }
		        if (prevCloseObj != null && !(prevCloseObj instanceof JSONNull)) {
		            prevClose = data.getDouble("prev_close");
		        }

		        // prevClose 가 0 이면 나누기 방지
		        double priceDifference = 0.0;
		        double pricePercentage = 0.0;
		        if (prevClose != 0.0) {
		            priceDifference = close - prevClose;
		            pricePercentage = (priceDifference / prevClose) * 100.0;
		        }

		        String stock_flag = "text-gray";
		        if (close > prevClose) {
		            stock_flag = "text-red";
		        } else if (prevClose > close) {
		            stock_flag = "text-blue";
		        }

		        vo.setStock_flag(stock_flag);
		        vo.setStock_close(String.valueOf(close));
		        vo.setStock_prevClose(decimalFormat.format(prevClose));
		        vo.setPriceDifference(decimalFormat.format(priceDifference));
		        vo.setPricePercentage(decimalFormat.format(pricePercentage));

		        int quantity = 0;
		        try {
		            quantity = Integer.parseInt(vo.getTotal_quantity() == null ? "0" : vo.getTotal_quantity());
		        } catch (NumberFormatException e) {
		            quantity = 0;
		        }

		        if (quantity > 0) {
		            double totalPrice = 0.0;
		            double avgQuantity = 0.0;

		            try {
		                totalPrice = Double.parseDouble(vo.getTotal_purchase_price());
		            } catch (Exception e) {
		                totalPrice = 0.0;
		            }
		            try {
		                avgQuantity = Double.parseDouble(vo.getTotal_quantity());
		            } catch (Exception e) {
		                avgQuantity = 0.0;
		            }

		            if (avgQuantity > 0.0) {
		                double avgPrice = totalPrice / avgQuantity;                                // 평균 단가
		                double avgPriceDifference = (close - avgPrice) * avgQuantity;              // 손익가
		                double avgPercentage = 0.0;

		                if (avgPrice != 0.0) {
		                    avgPercentage = ((close - avgPrice) / avgPrice) * 100.0;               // 손익률
		                }

		                String avg_flag = "text-gray";
		                if (close > avgPrice) {
		                    avg_flag = "text-red";
		                } else if (avgPrice > close) {
		                    avg_flag = "text-blue";
		                }

		                vo.setStockAVG_flag(avg_flag);
		                avgPrice = ("true".equals(groupMarket)) ? avgPrice : Math.round(avgPrice);

		                vo.setStock_price_roi(decimalFormat.format(avgPriceDifference));           // 손익 금액
		                vo.setAvg_purchase_price(decimalFormat.format(avgPrice));                  // 평균 단가
		                vo.setAvg_pricePercentage(decimalFormat.format(avgPercentage));            // 손익률 %
		                vo.setTotal_purchase_price(decimalFormat.format(totalPrice));              // 총금액
		            }
		        }

		        // SMA UP,DOWN Data
		        String stock_30minsma = vo.getStock_30minsma();
		        if (stock_30minsma != null) {
		            vo.setStock_30minsma(settingSMA(stock_30minsma));
		        }
		        String stock_sma = vo.getStock_sma();
		        if (stock_sma != null) {
		            vo.setStock_sma(settingSMA(stock_sma));
		        }

		        // MACD Signal Data
		        String macd_signal = vo.getStock_macd_signal();
		        String stock_5bc = vo.getStock_5bc();
		        if (macd_signal != null) {
		            String streaksignal = vo.getStock_macd_streaksignal();
		            String streakdays = vo.getStock_macd_streakdays();
		            String timedays = vo.getStock_macd_timedays();
		            String html = "";

		            String macdColor = macd_signal.equals("BUY") ? "red" : "blue";
		            String streakColor = (streaksignal != null && streaksignal.equals("UP")) ? "red" : "blue";

		            html += "<b class=\"text-xxs text-" + macdColor + "\" title=MACD_SIGNAL>" + macd_signal + " </b>";
		            html += "<b class=\"text-xxs text-" + macdColor + "\" title=MACD_SIGNALDAY> " + timedays + "일</b>";
		            html += "<b class=\"text-xxs text-" + streakColor + "\" title=MACD_OSC구분>" +
		                    "<i class=\"fas fa-arrow-" + streaksignal + " text-xxs\"></i>" +
		                    "</b>";
		            html += "<b class=\"text-xxs text-" + streakColor + "\" title=MACD_변동일수> " + streakdays + "일</b>";
		            if (stock_5bc != null) {
		                String bcColor = stock_5bc.equals("UP") ? "red" : "blue";
		                html += "<b class=\"text-xxs text-" + bcColor + "\" title=5BC>5BC</b>";
		            }
		            vo.setStock_macd_signal(html); // MACD
		        }

		        String modifyDate = vo.getModify_date();
		        if (modifyDate != null && modifyDate.length() >= 8) {
		            String textColor = "text-gray";
		            String today = TimeUtil.getCountryTime();

		            if (today != null && today.length() >= 8) {
		                if (modifyDate.substring(0, 8).equals(today.substring(0, 8))) {
		                    textColor = "text-yellow";
		                }
		                if (modifyDate.equals(today)) {
		                    textColor = "text-green";
		                }
		            }

		            vo.setModify_state(textColor);

		            // 타임스탬프 포맷 변경
		            modifyDate = TimeUtil.getFormatTimestamp(modifyDate);
		            vo.setModify_date(modifyDate);
		        }
		    }

		    // 정렬 옵션이 있을 때만 정렬
		    if (sortField != null && sortOrder != null) {
		        VoSorter<StockInfoVo> sorter = new VoSorter<>();
		        sorter.sort(list, sortField, sortOrder);
		    }

		    return list;
		}

	/**
	 *  getAverageVolume Data
	 *  data Volume의 Average를 구한뒤 금일 Volume이 Average보다 높으면 매수우위 낮으면 매수하위   
	 *  @return 
	 * @throws Exception 
	 */
	public static HashMap<String, String> getAverageVolumeVo(List<StockDataVo> monthlyDataList,double todayVolume) {
		// 주어진 데이터를 이용하여 계산
		HashMap<String, String> map = new  HashMap<String, String>();
		double totalVolume = 0;
		double averageVolume = 0;
		String avVolumeSignal ="";
		for(int i=200; i<monthlyDataList.size(); i++) {
			
            double volume = 0;
            if(monthlyDataList.get(i).getVolume()> 0) {
            	volume = monthlyDataList.get(i).getVolume();
            }
			totalVolume += volume;
		}
		
		averageVolume = Math.round(totalVolume / monthlyDataList.size());
		if(Math.round(todayVolume) > averageVolume) {
			avVolumeSignal = "UP";
		}else {
			avVolumeSignal = "DOWN";
		}
		 map.put("stock_av_volume",String.valueOf(averageVolume));
		 map.put("stock_av_volumesignal", avVolumeSignal);
		return map;
	}
	/**
	 *  getAverageVolume Data
	 *  data Volume의 Average를 구한뒤 금일 Volume이 Average보다 높으면 매수우위 낮으면 매수하위   
	 *  @return 
	 * @throws Exception 
	 */
	public static HashMap<String, String> getAverageVolume(String[] dataArray,double todayVolume) {
		// 주어진 데이터를 이용하여 계산
		HashMap<String, String> map = new  HashMap<String, String>();
		double totalVolume = 0;
		double averageVolume = 0;
		String avVolumeSignal ="";
		for(int i=200; i<dataArray.length; i++) {
			String[] values = dataArray[i].split(",");
            double volume = 0;
            if(!values[5].equals("null")) {
            	volume = Double.parseDouble(values[5]);
            }
			totalVolume += volume;
		}
//		for (String item : dataArray) {
//            String[] values = item.split(",");
//            double volume = 0;
//            if(!values[5].equals("null")) {
//            	volume = Double.parseDouble(values[5]);
//            }
//			totalVolume += volume;
//		}
		averageVolume = Math.round(totalVolume / dataArray.length);
		if(Math.round(todayVolume) > averageVolume) {
			avVolumeSignal = "UP";
		}else {
			avVolumeSignal = "DOWN";
		}
		
		//String averageVolume = String.valueOf();
		System.out.println(todayVolume+"====/averageVolume==="+averageVolume+"==avVolumeSignal=="+avVolumeSignal);
//		System.out.println("todayVolume============"+Math.round(todayVolume));
//		System.out.println("avVolumeSignal============"+avVolumeSignal);
		 map.put("stock_av_volume",String.valueOf(averageVolume));
		 map.put("stock_av_volumesignal", avVolumeSignal);
		return map;
	}
	/**
	 *  getSMAData Data
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	public static HashMap<String, String> getAnalysisSMADataVo(List<StockDataVo> monthlyDataList, double lastClosingPrice){
		// 주어진 데이터를 이용하여 계산
		HashMap<String, String> map = new  HashMap<String, String>();
		String[] smaint = {"5","20","60","120","240"};
		for(String sma: smaint) {
			int movingAveragePeriod = Integer.parseInt(sma);
	        double sum = 0;
	        int startIndex = Math.max(0, monthlyDataList.size() - movingAveragePeriod);  // 시작 인덱스
	        
	        for (int i = startIndex; i < monthlyDataList.size(); i++) {
	            double close = monthlyDataList.get(i).getClose();
	            sum += close;  // 종가 더하기
	        }
	        
	        double movingAverage = sum / movingAveragePeriod;  // 이동평균
	        // 비교하여 결과 출력
	        String dat = "";
	        if (lastClosingPrice > movingAverage) {
	        	dat = "UP";
	        } else {
	        	dat = "DOWN";
	        }
	        map.put("stock_sma"+sma, dat);
		}
		return map;
	}
	
	public static HashMap<String, String> getAnalysisSMAData(String[] dataArray, double lastClosingPrice){
		// 주어진 데이터를 이용하여 계산
		HashMap<String, String> map = new  HashMap<String, String>();
		String[] smaint = {"5","20","60","120","240"};
		for(String sma: smaint) {
			int movingAveragePeriod = Integer.parseInt(sma);
	        double sum = 0;
	        int startIndex = Math.max(0, dataArray.length - movingAveragePeriod);  // 시작 인덱스
		   
	        for (int i = startIndex; i < dataArray.length; i++) {
	            String item = dataArray[i];
	            String[] values = item.split(",");
	            double close = Double.parseDouble(values[4]);
	            sum += close;  // 종가 더하기
	        }
	        double movingAverage = sum / movingAveragePeriod;  // 이동평균
	        // 비교하여 결과 출력
	        String dat = "";
	        if (lastClosingPrice > movingAverage) {
	        	dat = "UP";
	        } else {
	        	dat = "DOWN";
	        }
	        map.put("stock_sma"+sma, dat);
		}
		return map;
	}
	/**
	 *  getSMAData Data
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	public static String settingSMA(String sma){
		// 주어진 데이터를 이용하여 계산
		String[] smaint = {"5","20","60","120","240"};
		String[] smacolor = {"black","red","green","blue","pink"};
		String[] smalist = sma.split(",");
		int index = 0;
		String html = "";
		for(String value: smalist) {
	        html +="<b class=\"text-"+smacolor[index]+"\" title=\""+Integer.parseInt(smaint[index])+"SMA\">"
	        				+ "<i class=\"fas fa-arrow-"+value+" text-xxs\">"+ "</i>"
	        				+ "</b>";
	        index++;
		}
		
		return html;
	}
	/**
	 *  getSMAData Data
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	public static String settingMACD(String macd){
		// 주어진 데이터를 이용하여 계산
	        String html ="<b class=\"text-"+(macd.equals("BUY")?"red" : "blue")+"\" title=MACD_SIGNAL\">"
	        				+macd+ "</b>";
		return html;
	}
	/**
	 *  getSMAData Data
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public static ArrayList getSMAData(String[] dataArray, double lastClosingPrice){
		
		// 주어진 데이터를 이용하여 계산
		ArrayList list = new ArrayList();
		String[] smaint = {"5","20","60","120","240"};
		String[] smacolor = {"black","red","green","blue","pink"};
		int index = 0;
		for(String sma: smaint) {
			int movingAveragePeriod = Integer.parseInt(sma);
	        double sum = 0;
	        int startIndex = Math.max(0, dataArray.length - movingAveragePeriod);  // 시작 인덱스
		   
	        for (int i = startIndex; i < dataArray.length; i++) {
	            String item = dataArray[i];
	            String[] values = item.split(",");
	            double close = Double.parseDouble(values[4]);
	            sum += close;  // 종가 더하기
	        }
	        double movingAverage = sum / movingAveragePeriod;  // 이동평균
	        // 비교하여 결과 출력
	        String dat = "";
	        if (lastClosingPrice > movingAverage) {
	        	dat = "UP";
	        } else {
	        	dat = "DOWN";
	        }
	        
	        String html ="<b class=\"text-"+smacolor[index]+"\" title=\""+movingAveragePeriod+"SMA\">"
	        				+ "<i class=\"fas fa-arrow-"+dat+" text-xxs\">"+ "</i>"
	        				+ "</b>";
	        list.add(html);
	        index++;
		}
		System.out.println(list);
		return list;
	}

	/* 빅골드/빅데드
	 * 60일 이동평균선
	 * 240일 이동평균선
	 * 크로스 지점 찾기
	*/
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public static ArrayList stockCross(String[] dataArray) throws ParseException {
        // 서울시간 (한국 시간)으로 타임존 설정
        TimeZone seoulTimeZone = TimeZone.getTimeZone("Asia/Seoul");
        // SimpleDateFormat을 사용하여 포맷 변경
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(seoulTimeZone);
         
        // 이동평균을 계산할 기간 설정 (60일, 240일)
        int shortTerm = 60;
        int longTerm = 240;
        // 이동평균 계산
        double[] shortTermMovingAverage = calculateMovingAverage(dataArray, shortTerm);
        double[] longTermMovingAverage = calculateMovingAverage(dataArray, longTerm);
        // 크로스 지점 찾기
        CrossIndices crossIndices = findCrossingIndices(shortTermMovingAverage, longTermMovingAverage);
        
        ArrayList list = new ArrayList();
        
        if (crossIndices.getCombinedCrossingIndices().length > 0) {
           // System.out.println("60일 이동평균선과 240일 이동평균선과의 크로스 지점:");

            // 크로스 지점을 타임스탬프로 변환하고 정렬
            List<CrossIndexInfo> sortedCrossIndices = sortAndConvertToTimestamps(dataArray, crossIndices);

            for (CrossIndexInfo crossIndexInfo : sortedCrossIndices) {
            	
                String crossType = crossIndexInfo.isAbove() ? "빅골드" : "빅데드";
                
             // 타임스탬프 포맷 변경
	            Date date = new Date(crossIndexInfo.getTimestamp());
	            String time = sdf.format(date);
	            Date parsedDate = sdf.parse(time);
	            // 1초 추가
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTime(parsedDate);
	            //calendar.add(Calendar.DAY_OF_MONTH, 1);	            	
	            long parsedTimestamp = calendar.getTimeInMillis();
	            HashMap<String, String> flagOj = new HashMap<String, String>();
                flagOj.put("x", String.valueOf(parsedTimestamp));
                flagOj.put("title", crossType);
                list.add(flagOj);
            }
        } else {
            System.out.println("크로스 지점을 찾을 수 없습니다.");
        }
        
        return list;
	}

	/**
	 *  stock 코드 종목 조회
	 *  @param  
	 *  @return 
	 * @throws Exception 
	 */
	public void saveStockCodeInfo(String incode) throws Exception{
		System.out.println("saveStockCodeInfo===="+incode);
		try {

            // 공통 유틸을 통해 /stock/details 호출 + StockInfoVo 생성
            StockInfoVo stockInfoVo = new StockInfoVo();// //AlphaSquareApiUtil.getStockInfoByCode(incode);

            // 기존 로직에서 사용하던 group, 기타 필드 세팅 (필요 시 보완)
            stockInfoVo.setStock_group("0001");

            // 로고가 있으면 이미지 다운로드 (기존 로직 유지)
            if (stockInfoVo.getStock_url() != null && stockInfoVo.getStock_url().length() > 0) {
                ImageDownload.imageDownload(stockInfoVo);
            }

            // 기존에는 주석 처리 되어 있었는데, 실제 저장을 원하면 DAO 쪽 merge 호출
            // stockCodeInfoDao.mergeInterestStock(stockInfoVo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	 // 타임스탬프 추출 및 변환 함수
   private static long extractTimestamp(String data) {
   	
   	double timestampDouble = 0;
       // 주어진 데이터에서 타임스탬프 추출
       timestampDouble = Double.parseDouble(data.split(",")[0]);
       long timestamp = (long) timestampDouble;
       return timestamp;
   }
   
	// 이동평균 계산 함수
   private static double[] calculateMovingAverage(String[] dataArray, int period) {
       double[] movingAverage = new double[dataArray.length];

       for (int i = period - 1; i < dataArray.length; i++) {
           double sum = 0;
           for (int j = 0; j < period; j++) {
               String[] dataRow = dataArray[i - j].split(",");
               double closePrice = Double.parseDouble(dataRow[4]); // 종가 (인덱스 4)
               sum += closePrice;
           }
           movingAverage[i] = sum / period;
       }
       return movingAverage;
   }

// 크로스 지점 찾기 함수
   private static CrossIndices findCrossingIndices(double[] shortTermMovingAverage, double[] longTermMovingAverage) {
       CrossIndices crossIndices = new CrossIndices();

       for (int i = shortTermMovingAverage.length - 2; i >= 0; i--) {
           if (shortTermMovingAverage[i] > longTermMovingAverage[i] &&
               shortTermMovingAverage[i + 1] <= longTermMovingAverage[i + 1]) {
               // 아래로 크로스
               crossIndices.addCrossingIndex(i + 1, false);
           } else if (shortTermMovingAverage[i] < longTermMovingAverage[i] &&
                      shortTermMovingAverage[i + 1] >= longTermMovingAverage[i + 1]) {
               // 위로 크로스
               crossIndices.addCrossingIndex(i + 1, true);
           }
       }

       return crossIndices;
   }
   // 크로스 지점을 타임스탬프로 변환하고 정렬하는 함수
   private static List<CrossIndexInfo> sortAndConvertToTimestamps(String[] dataArray, CrossIndices crossIndices) {
       List<CrossIndexInfo> sortedCrossIndices = new ArrayList<>();
       for (CrossIndexInfo crossIndexInfo : crossIndices.getCombinedCrossingIndices()) {
           long timestamp = extractTimestamp(dataArray[crossIndexInfo.getIndex()]);
           crossIndexInfo.setTimestamp(timestamp);
           sortedCrossIndices.add(crossIndexInfo);
       }
       // 시간순으로 정렬
       sortedCrossIndices.sort(Comparator.comparingLong(CrossIndexInfo::getTimestamp));
       return sortedCrossIndices;
   }

	public static HashMap<String, String> getAnalysisMACDDataVo(List<StockDataVo> dataArray) throws ParseException {
		HashMap<String, String> analsisMACDMap = new  HashMap<String, String>();
       // Define MACD parameters
       int shortPeriod = 12;
       int longPeriod = 26;
       int signalPeriod = 9;

       double shortEMA = 0;
       double longEMA = 0;
       double signalEMA = 0;
       double macd = 0;
       double signalLine = 0;
       boolean isCrossing = false;
       double prevCacul = 0; // 이전 cacul 값 저장
       
       long signaltime = 0;
       String signalSign = "";
       String streakSign = "";
       int streakDays = 0; // 크로스 이후의 연속된 일 수 계산
       
       for (int i = 0; i < dataArray.size(); i++) {
           double timestampDouble =dataArray.get(i).getDate();
           
           double closingPrice =dataArray.get(i).getClose();
           // Calculate EMA for short and long periods
           shortEMA = calculateEMA(closingPrice, shortPeriod, shortEMA);
           longEMA = calculateEMA(closingPrice, longPeriod, longEMA);

           // Calculate MACD
           macd = shortEMA - longEMA;

           // Calculate EMA for the MACD to get the signal line
           signalEMA = calculateEMA(macd, signalPeriod, signalEMA);

           //double cacul = Math.round(macd - signalLine);
           double cacul = macd - signalLine;
           //System.out.println("macd===="+macd+"    signalEMA=="+signalEMA+"   cacul=="+cacul);            
           if (i >= signalPeriod) {

               // 이전 cacul 값과 비교하여 UP 또는 DOWN 여부 판단
               if (cacul > prevCacul) {
               	if(streakDays > 0 && streakSign.equals("DOWN")) {
               		streakSign = "UP";
               		streakDays = 0; // 크로스 이후 첫째 날이므로 0일로 초기화
               	}
                   if(streakSign.equals("UP")) {
                   	streakDays++;
                   	streakSign = "UP";
                   }
                  // System.out.println(convertTimestampToDate((long) timestampDouble)+"  "+cacul+" UP : " + streakDays + "일 ");
               } else if (cacul < prevCacul) {
               	if(streakDays > 0 && streakSign.equals("UP")) {
               		streakSign = "DOWN";
               		streakDays = 0; // 크로스 이후 첫째 날이므로 0일로 초기화
               	}
                   if(streakSign.equals("DOWN")) {
                   	streakDays++;
                   	streakSign = "DOWN";
                   }
                  // System.out.println(convertTimestampToDate((long) timestampDouble)+"  "+cacul+" DOWN : " + streakDays + "일 ");
               } else {
               	//System.out.println(convertTimestampToDate((long) timestampDouble)+"  "+cacul+" 변화없음 : " + streakDays + "일 ");
               }
               
               // 현재 크로스 이후의 연속된 일 수 출력
               //System.out.println("연속된 일 수: " + streakDays + "일");
               prevCacul = cacul; // 이전 cacul 값 업데이트
               if (macd > signalLine && !isCrossing) {
                   signalSign = "BUY";
                   signaltime = (long) timestampDouble;
                  // System.out.println(convertTimestampToDate((long) timestampDouble) + " MACD: " + Math.round(macd) + " signalLine: " + Math.round(signalLine) + " = " + Math.round(cacul) + " buy");
                   isCrossing = true;
                   streakSign ="UP";
               } else if (macd < signalLine && isCrossing) {
                   signalSign = "SELL";
                   signaltime = (long) timestampDouble;
                  // System.out.println(convertTimestampToDate((long) timestampDouble) + " MACD: " + Math.round(macd) + " signalLine: " + Math.round(signalLine) + " = " + Math.round(cacul) + " sell");
                   isCrossing = false;
                   streakSign ="DOWN";
               }
           }
           signalLine = signalEMA;
       }
       analsisMACDMap.put("stock_macd_time", convertTimestampToDate((long) signaltime));
       analsisMACDMap.put("stock_macd_timedays", timestampToDate((long) signaltime));
       analsisMACDMap.put("stock_macd_signal", signalSign);
       analsisMACDMap.put("stock_macd_streakdays", String.valueOf(streakDays));
       analsisMACDMap.put("stock_macd_streaksignal", streakSign);
       return analsisMACDMap;
   }

    private static double calculateEMA(double currentValue, int period, double previousEMA) {
        double smoothing = 2.0 / (period + 1);
        return (currentValue - previousEMA) * smoothing + previousEMA;
    }
     
    public static String convertTimestampToDate(long timestamp) {
    	  // SimpleDateFormat을 사용하여 타임스탬프를 "YYYY-MM-DD" 형식으로 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date(timestamp));
    }
	
	private static String timestampToDate(long timestamp) {
		// 타임스탬프를 Date로 변환
		Date date = new Date(timestamp);
		// Date를 LocalDate로 변환
		Instant instant = date.toInstant();
		LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
		// 현재 날짜 가져오기
		LocalDate today = LocalDate.now();
		// 두 날짜 사이의 일 수 계산
		long daysPassed = ChronoUnit.DAYS.between(localDate, today);
		// System.out.println("오늘까지 " + daysPassed + "일이 지났습니다.");
		return String.valueOf(daysPassed);
	}
	
	

    public static HashMap<String, String> monthlyStockData(List<StockDataVo> dataArray) throws IOException {

    	HashMap<String, String> monthlyStockMap = new HashMap<String, String>();
       // Gson gson = new Gson();
       // ParseStockDataVo stockData = gson.fromJson(dataArray, ParseStockDataVo.class);
        // 주어진 데이터에서 Unix 타임스탬프와 시가, 고가, 저가, 종가를 포함하는 2차원 배열
		try {

	        // 비교할 월 데이터를 찾기 위한 날짜 포맷
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

	        // 현재 월 구하기
	        Calendar cal = Calendar.getInstance();
	        String currentMonth = dateFormat.format(cal.getTime());

	        // 이전 월 구하기
	        cal.add(Calendar.MONTH, -1);
	        String previousMonth = dateFormat.format(cal.getTime());

	        //System.out.println("현재 월: " + currentMonth);
	        //System.out.println("이전 월: " + previousMonth);
	        // 문자열을 Date 객체로 파싱
	        Date currentDate = dateFormat.parse(currentMonth);
	        Date previousDate = dateFormat.parse(previousMonth);

	        // Date 객체를 타임스탬프로 변환 (밀리초 단위)
	        long currentTimestamp = currentDate.getTime();		//당월
	        long previousTimestamp = previousDate.getTime();	//작월
	        
	        
	        List<StockDataVo> monthlyDataList =  generateMonthlyData(dataArray,TimeUtil.getCountry());
	        StockDataVo previousMonthData = null;
	        StockDataVo currentMonthData = null;

	        // 이전 월과 현재 월 데이터 찾기
	        for (StockDataVo monthlyData : monthlyDataList) {
	            if (monthlyData.getDate() == previousTimestamp) {
	                previousMonthData = monthlyData;
	            } else if (monthlyData.getDate() == currentTimestamp) {
	                currentMonthData = monthlyData;
	            }

	            // 찾았으면 더 이상 반복하지 않음
	            if (previousMonthData != null && currentMonthData != null) {
	                break;
	            }
	        }

	        // 11월과 12월 데이터가 모두 존재할 때 비교하여 상승, 하락 여부 확인
	        if (previousMonthData != null && currentMonthData != null) {
	            double previousMonthClose = previousMonthData.getClose();
	            double currentMonthClose = currentMonthData.getClose();
	            if (currentMonthClose > previousMonthClose) {
	            	monthlyStockMap.put("stock_mon",  "UP");
	                //System.out.println( currentMonth+"은 "+previousMonth+" 대비 상승했습니다.");
	            } else if (currentMonthClose < previousMonthClose) {
	            	monthlyStockMap.put("stock_mon",  "DOWN");
	                //System.out.println( currentMonth+"은 "+previousMonth+" 대비 하락했습니다.");
	            } else {
	            	monthlyStockMap.put("stock_mon",  "SAME");
	                //System.out.println( currentMonth+"은 "+previousMonth+" 과 동일한 가격입니다.");
	            }
	        } else {
	        	monthlyStockMap.put("stock_mon",  "");
	            //System.out.println("해당 월 데이터가 없습니다.");
	        }
	        // 생성된 월봉 데이터 출력
//	        for (StockDataVo monthlyData : monthlyDataList) {
//	            System.out.println("Date: " + monthlyData.getDate());
//	            System.out.println("Open: " + monthlyData.getOpen());
//	            System.out.println("High: " + monthlyData.getHigh());
//	            System.out.println("Low: " + monthlyData.getLow());
//	            System.out.println("Close: " + monthlyData.getClose());
//	            System.out.println("-----------------------");
//	        }

	        //=============월봉 5일 이동평균선 가격 계산 로직===================
	        // 가격 데이터 추출
            List<Double> closingPrices = extractClosingPrices(monthlyDataList);
	        // 이동평균 계산
            List<MovingAverage> movingAverages = calculateMovingAverages(closingPrices);
            // 이동평균 가격이 높은 순으로 정렬
            movingAverages.sort(Comparator.comparingDouble(MovingAverage::getAverage).reversed());
            // 결과 출력
            for (MovingAverage average : movingAverages) {
                //System.out.println(average.getPeriod() + " 월 5일 이동평균 CTZ: " + average.getAverage());
                // 숫자를 소수점 둘째 자리까지 포맷
                monthlyStockMap.put("stock_sma5_mon_price",String.valueOf(Math.round(average.getAverage())));
            }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
     return monthlyStockMap;
    }
    
    public static List<StockDataVo> generateMonthlyData(List<StockDataVo> rawData, String country_code) throws ParseException {
        List<StockDataVo> monthlyDataList = new ArrayList<>();
        Map<Long, List<Double>> monthlyMap = new HashMap<>();

        // Unix 타임스탬프를 날짜로 변환하기 위한 SimpleDateFormat
        SimpleDateFormat dateFormat = null;

        dateFormat = new SimpleDateFormat("yyyy-MM"); // yyyyMMddkk (년월일시)
		if (country_code.equals("KR")) {
			dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
		}else if (country_code.equals("US")) {
			dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		}
        // 데이터를 월별로 그룹화하여 시가, 고가, 저가, 종가를 계산
        for (StockDataVo data : rawData) {
            Date date = new Date(data.getDate());
            String monthKey = dateFormat.format(date);		//월을 key로 한다.
            Date reMonthkey = dateFormat.parse(monthKey);	//yyyy-mm -> timestamp로 변환
            // Date 객체를 타임스탬프로 변환 (밀리초 단위)
            long reTimestamp = reMonthkey.getTime();
            if (!monthlyMap.containsKey(reTimestamp)) {
                monthlyMap.put(reTimestamp, new ArrayList<>());
            }

            monthlyMap.get(reTimestamp).add(data.getOpen()); // Open
            monthlyMap.get(reTimestamp).add(data.getHigh()); // High
            monthlyMap.get(reTimestamp).add(data.getLow()); // Low
            monthlyMap.get(reTimestamp).add(data.getClose()); // Close
        }

        // 월별로 계산된 데이터를 StockData 객체로 생성하여 리스트에 추가
        for (Map.Entry<Long, List<Double>> entry : monthlyMap.entrySet()) {
            List<Double> values = entry.getValue();
            double open = values.get(0);
            double high = Collections.max(values.subList(1, values.size()));
            double low = Collections.min(values.subList(2, values.size()));
            double close = values.get(values.size() - 1);
            StockDataVo monthlyData = new StockDataVo(entry.getKey(), open, high, low, close, 0);
            monthlyDataList.add(monthlyData);
        }
        monthlyDataList = monthlyDataList.stream().sorted(Comparator.comparing(StockDataVo::getDate)).collect(Collectors.toList());
        return monthlyDataList;
    }
    
	// =======월봉 5일 이동 평균선 가격 계산 로직
	//=====================================================================
	// 종가 데이터 추출
	private static List<Double> extractClosingPrices(List<StockDataVo> monthlyDataList) {
		List<Double> closingPrices = new ArrayList<>();
		for (StockDataVo entry : monthlyDataList) {
			closingPrices.add(entry.getClose()); // 종가 데이터는 인덱스 4에 위치
		}
		return closingPrices;
	}

	// 이동평균 계산
	private static List<MovingAverage> calculateMovingAverages(List<Double> prices) {
		int[] periods = { 5 };
		List<MovingAverage> movingAverages = periodsToAverages(prices, periods);
		return movingAverages;
	}

	private static List<MovingAverage> periodsToAverages(List<Double> prices, int[] periods) {
		List<MovingAverage> movingAverages = new ArrayList<>();
		for (int period : periods) {
			double average = calculateMovingAverage(prices, period);
			movingAverages.add(new MovingAverage(period + "", average));
		}
		return movingAverages;
	}

	// 이동평균 계산
	private static double calculateMovingAverage(List<Double> prices, int period) {
		double result = 0;
		double sum = 0;
		if (prices.size() < period) {
			//System.out.println("데이터가 이동평균 기간보다 짧습니다.");
			// throw new IllegalArgumentException("데이터가 이동평균 기간보다 짧습니다.");
		} else {
			for (int i = prices.size() - period; i < prices.size(); i++) {
				sum += prices.get(i);
			}
			result = sum / period;
		}
		return result;
	}

	// Custom 객체
	static class MovingAverage {
		private String period;
		private double average;

		public MovingAverage(String period, double average) {
			this.period = period;
			this.average = average;
		}

		public String getPeriod() {
			return period;
		}

		public double getAverage() {
			return average;
		}
	}
}

class CrossIndexInfo {
    private int index;
    private boolean above;
    private long timestamp;

    public CrossIndexInfo(int index, boolean above) {
        this.index = index;
        this.above = above;
    }

    public int getIndex() {
        return index;
    }

    public boolean isAbove() {
        return above;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

class CrossIndices {
    private List<CrossIndexInfo> crossingIndices;

    public CrossIndices() {
        crossingIndices = new ArrayList<>();
    }

    public CrossIndexInfo[] getCombinedCrossingIndices() {
        return crossingIndices.toArray(new CrossIndexInfo[0]);
    }

    public void addCrossingIndex(int index, boolean above) {
        crossingIndices.add(new CrossIndexInfo(index, above));
    }
}