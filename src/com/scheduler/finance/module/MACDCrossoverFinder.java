package com.scheduler.finance.module;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.scheduler.comm.util.GetHttpsURLConnection;

public class MACDCrossoverFinder {
    public static void main(String[] args) {
        String candlesUrl = "https://api.alphasquare.co.kr/data/v3/prices/candles/"+"1014"+"?freq="+"day"+"&limit="+"50"+"&include_current_candle=true";
    	String result;
		try {
			result = GetHttpsURLConnection.getHttpsGet(candlesUrl);
	
			System.out.println(result);
	        // JSON 데이터를 파싱
	        JSONObject json = new JSONObject(result);
	        JSONArray data = json.getJSONArray("data");
	
	        // MACD 계산에 필요한 변수 초기화
	        int shortTerm = 12;
	        int longTerm = 26;
	        int signalPeriod = 9;
	        double[] shortTermEMA = new double[data.length()];
	        double[] longTermEMA = new double[data.length()];
	        double[] macd = new double[data.length()];
	        double[] signalLine = new double[data.length()];
	
	        // 단기 및 장기 이동 평균 계산
	        for (int i = longTerm; i < data.length(); i++) {
	            double sumShortTerm = 0;
	            double sumLongTerm = 0;
	
	            for (int j = 0; j < shortTerm; j++) {
	                sumShortTerm += data.getJSONArray(i - j).getDouble(4); // 종가 사용
	            }
	
	            for (int j = 0; j < longTerm; j++) {
	                sumLongTerm += data.getJSONArray(i - j).getDouble(4); // 종가 사용
	            }
	
	            shortTermEMA[i] = sumShortTerm / shortTerm;
	            longTermEMA[i] = sumLongTerm / longTerm;
	            macd[i] = shortTermEMA[i] - longTermEMA[i];
	        }
	
	        // Signal Line 계산
	        for (int i = longTerm + signalPeriod - 1; i < data.length(); i++) {
	            double sumSignal = 0;
	            for (int j = 0; j < signalPeriod; j++) {
	                sumSignal += macd[i - j];
	            }
	            signalLine[i] = sumSignal / signalPeriod;
	        }
	        
	        boolean dataDecreasing = false; // 데이터가 감소하는지 여부를 나타내는 플래그

	        for (int i = longTerm + signalPeriod - 1; i < data.length(); i++) {
	            if ((macd[i] > signalLine[i] && macd[i - 1] <= signalLine[i - 1])) {
	                long timestamp = (long) data.getJSONArray(i).getDouble(0);
	                String date = convertTimestampToDate(timestamp);
	                System.out.println("날짜: " + date + ", MACD: " + macd[i] + ", Signal: " + signalLine[i] + ", 매수 신호");
	                
	                // 이전에 데이터가 감소했던 경우 감소하는 부분을 출력하고 플래그 초기화
	                if (dataDecreasing) {
	                    for (int j = i - 1; j >= 0; j--) {
	                        long prevTimestamp = (long) data.getJSONArray(j).getDouble(0);
	                        String prevDate = convertTimestampToDate(prevTimestamp);
	                        System.out.println("데이터 감소 부분 : " + prevDate);
	                        //System.out.println("날짜: " + prevDate + ", MACD: " + macd[j] + ", Signal: " + signalLine[j]);
	                        if (macd[j] <= signalLine[j]) {
	                            break; // 데이터가 다시 증가하는 지점을 찾으면 루프 종료
	                        }
	                    }
	                    dataDecreasing = false; // 플래그 초기화
	                }
	            } else if ((macd[i] < signalLine[i] && macd[i - 1] >= signalLine[i - 1])) {
	                long timestamp = (long) data.getJSONArray(i).getDouble(0);
	                String date = convertTimestampToDate(timestamp);
	                System.out.println("날짜: " + date + ", MACD: " + macd[i] + ", Signal: " + signalLine[i] + ", 매도 신호");
	                
	                // 이전에 데이터가 감소했던 경우 감소하는 부분을 출력하고 플래그 초기화
	                if (dataDecreasing) {
	                    for (int j = i - 1; j >= 0; j--) {
	                        long prevTimestamp = (long) data.getJSONArray(j).getDouble(0);
	                        String prevDate = convertTimestampToDate(prevTimestamp);
	                        System.out.println("데이터 감소 부분 : " + prevDate);
	                        //System.out.println("날짜: " + prevDate + ", MACD: " + macd[j] + ", Signal: " + signalLine[j]);
	                        if (macd[j] >= signalLine[j]) {
	                            break; // 데이터가 다시 증가하는 지점을 찾으면 루프 종료
	                        }
	                    }
	                    dataDecreasing = false; // 플래그 초기화
	                }
	            } else {
	                // MACD와 Signal Line이 교차하지 않은 경우 데이터가 감소하는 중임을 표시
	                dataDecreasing = true;
	            }
	        }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private static String convertTimestampToDate(long timestamp) {
  	  // SimpleDateFormat을 사용하여 타임스탬프를 "YYYY-MM-DD" 형식으로 변환
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return dateFormat.format(new Date(timestamp));
  }
}
