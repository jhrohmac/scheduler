package com.scheduler.finance.module;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.scheduler.comm.util.GetHttpsURLConnection;
import com.scheduler.finance.vo.StockDataVo;

import net.sf.json.JSONObject;

public class MACDOSCCalculator {
	public static void main(String[] arg){
		
		try {

			String id = "11800";
					id = "1020";
			String period = "day";
			String limit = "600";
			String code = "029780";
			
			// Today stock 거래 이력 조회
			String url ="https://api.alphasquare.co.kr/data/v2/price/current-price?code="+code;
			String result_code = GetHttpsURLConnection.getHttpsGet(url);
			HashMap<String,List> resultData = new HashMap<String,List>();
			
			JSONObject json = JSONObject.fromObject(result_code);
			JSONObject data = json.getJSONObject(code);
			long dt = data.optLong("dt", 0L);
			double open = data.optDouble("open", 0.0);
			double high = data.optDouble("high", 0.0);
			double low = data.optDouble("low", 0.0);
			double close = data.optDouble("close", 0.0);
			  
            //금일 Data
	        List<Object> stockOJ = new ArrayList<>();
			stockOJ.add(dt);
			stockOJ.add(open);
			stockOJ.add(high);
			stockOJ.add(low);
			stockOJ.add(close);
			
			String candlesUrl = "https://api.alphasquare.co.kr/data/v3/prices/candles/" + id + "?freq=" + period
					+ "&limit=" + limit + "&include_current_candle=true";

			String result;
			result = GetHttpsURLConnection.getHttpsGet(candlesUrl);
			System.out.println(result);
			// JSON 데이터를 파싱
			String[] dataArray = result.substring(result.indexOf("[[") + 2, result.indexOf("]]")).split("\\],\\[");
			//System.out.println(dataArray);
			long dd = (long) 1698897600000.0 ;
							 
			System.out.println("실시간 MACD==========dd==========="+convertTimestampToDate(dd));
		    String[] newArray = new String[dataArray.length + 1];
		    System.arraycopy(dataArray, 0, newArray, 0, dataArray.length);
	    	newArray[dataArray.length] = stockOJ.toString().replace("[", "").replace("]", "");
		
			getAnalysisMACDData(newArray);
			System.out.println("실시간 MACD=====================");
			//getMACDOSCData(newArray);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static HashMap<String, String> getAnalysisMACDData(String[] dataArray) throws ParseException {
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
        
        for (int i = 0; i < dataArray.length; i++) {
            String item = dataArray[i];
            String[] values = item.split(",");
            double timestampDouble = Double.parseDouble(values[0]);
            
            double closingPrice = Double.parseDouble(values[4]);
            // Calculate EMA for short and long periods
            shortEMA = calculateEMA(closingPrice, shortPeriod, shortEMA);
            longEMA = calculateEMA(closingPrice, longPeriod, longEMA);

            // Calculate MACD
            macd = shortEMA - longEMA;

            // Calculate EMA for the MACD to get the signal line
            signalEMA = calculateEMA(macd, signalPeriod, signalEMA);

            //double cacul = Math.round(macd - signalLine);
            double cacul = macd - signalLine;
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
                } else if (cacul < prevCacul) {
                	if(streakDays > 0 && streakSign.equals("UP")) {
                		streakSign = "DOWN";
                		streakDays = 0; // 크로스 이후 첫째 날이므로 0일로 초기화
                	}
                    if(streakSign.equals("DOWN")) {
                    	streakDays++;
                    	streakSign = "DOWN";
                    }
                } else {
                }
                
                // 현재 크로스 이후의 연속된 일 수 출력
                //System.out.println("연속된 일 수: " + streakDays + "일");
                prevCacul = cacul; // 이전 cacul 값 업데이트
                if (macd > signalLine && !isCrossing) {
                    signalSign = "BUY";
                    signaltime = (long) timestampDouble;
                    isCrossing = true;
                    streakSign ="UP";
                } else if (macd < signalLine && isCrossing) {
                    signalSign = "SELL";
                    signaltime = (long) timestampDouble;
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

	        //System.out.println("오늘까지 " + daysPassed + "일이 지났습니다.");
	        return String.valueOf(daysPassed);
	}

	public static ArrayList<List<Object>> getMACDOSCData(String[] dataArray) throws ParseException {
		ArrayList<List<Object>> signalList = new ArrayList<List<Object>>();
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
        
        for (int i = 250; i < dataArray.length; i++) {
            String item = dataArray[i];
            String[] values = item.split(",");
            double timestampDouble = Double.parseDouble(values[0]);
            long timestamp = (long) timestampDouble;
            double closingPrice = Double.parseDouble(values[4]);
            // Calculate EMA for short and long periods
            shortEMA = calculateEMA(closingPrice, shortPeriod, shortEMA);
            longEMA = calculateEMA(closingPrice, longPeriod, longEMA);

            // Calculate MACD
            macd = shortEMA - longEMA;

            // Calculate EMA for the MACD to get the signal line
            signalEMA = calculateEMA(macd, signalPeriod, signalEMA);

            if (i >= signalPeriod) {
                // 현재 크로스 이후의 연속된 일 수 출력
                //System.out.println("연속된 일 수: " + streakDays + "일");
                if (macd > signalLine && !isCrossing) {
                    List<Object> oscObj = new ArrayList<>();
                    oscObj.add(timestamp);
                    oscObj.add("BUY");
                    signalList.add(oscObj);
                    //System.out.println(convertTimestampToDate(timestamp) + " MACD: " + Math.round(macd) + " signalLine: " + Math.round(signalLine) + " = " + Math.round(cacul) + " buy");
                    isCrossing = true;
                } else if (macd < signalLine && isCrossing) {
                    List<Object> oscObj = new ArrayList<>();
                    oscObj.add(timestamp);
                    oscObj.add("SELL");
                    signalList.add(oscObj);
                    //System.out.println(convertTimestampToDate(timestamp) + " MACD: " + Math.round(macd) + " signalLine: " + Math.round(signalLine) + " = " + Math.round(cacul) + " sell");
                    isCrossing = false;
                }
            }
            signalLine = signalEMA;
        }

        return signalList;
    }

    private static double calculateEMA(double currentValue, int period, double previousEMA) {
        double smoothing = 2.0 / (period + 1);
        return (currentValue - previousEMA) * smoothing + previousEMA;
    }
     
    private static String convertTimestampToDate(long timestamp) {
    	  // SimpleDateFormat을 사용하여 타임스탬프를 "YYYY-MM-DD" 형식으로 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date(timestamp));
    }
}
