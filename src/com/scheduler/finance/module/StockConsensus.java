package com.scheduler.finance.module;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.scheduler.comm.util.GetHttpsURLConnection;

public class StockConsensus {

	public static HashMap<String, String> getStockConsensus(String code, String frq) throws IOException {
        // JSON 형식의 데이터 문자열을 파싱하여 JSONArray로 변환
       // String data = "위에서 제공한 데이터 문자열";
        //JSONArray dataArray = new JSONArray(data);

		//String code = "139480";
		//String frq = "1";
		//String sDT = "20231120";
		String result;
		       
		//String url ="https://navercomp.wisereport.co.kr/company/ajax/c1050001_data.aspx?flag=2&cmp_cd="+code+"&finGubun=MAIN&frq="+frq+"&sDT="+sDT+"&chartType=svg";
		String url ="https://navercomp.wisereport.co.kr/company/ajax/c1050001_data.aspx?flag=2&cmp_cd="+code+"&finGubun=MAIN&frq="+frq+"&chartType=svg";
		//System.out.println(url);
		result = GetHttpsURLConnection.getHttpsGet(url);
		System.out.println(result);
		// Parsing JSON data
		JSONObject jsonData = new JSONObject(result);
		JSONArray entries = jsonData.getJSONArray("JsonData"); 
		System.out.println(entries);
		System.out.println(entries.length());
		HashMap<String, String> consensusMap = new HashMap<String, String>();
		if(entries.length() > 0) {
			// Extracting the latest entry (assuming it's the current quarter)
			JSONObject currentQuarter = entries.getJSONObject(entries.length() - 1);
	
			// Extracting current quarter's values
			String currentSales = currentQuarter.getString("SALES");			// 현재 매출
			String currentOperatingProfit = currentQuarter.getString("OP");		// 현재 영업이익
			String currentEarningsPerShare = currentQuarter.getString("EPS");	// 현재 주당순이익
			
			// Extracting the previous quarter's values
			JSONObject prevQuarter = entries.getJSONObject(entries.length() - 2);
			String prevSales = prevQuarter.getString("SALES");					// 이전 분기 매출
			String prevOperatingProfit = prevQuarter.getString("OP");			// 이전 분기 영업이익
			String prevEarningsPerShare = prevQuarter.getString("EPS");			// 이전 분기 주당순이익
			// Compare values
			consensusMap.putAll(compareValues("sales", parseValue(currentSales), parseValue(prevSales)));
			consensusMap.putAll(compareValues("op", parseValue(currentOperatingProfit), parseValue(prevOperatingProfit)));
			consensusMap.putAll(compareValues("eps", parseValue(currentEarningsPerShare), parseValue(prevEarningsPerShare)));
		}else {
			// Compare values
			consensusMap.putAll(compareValues("sales", 0, 0));
			consensusMap.putAll(compareValues("op", 0, 0));
			consensusMap.putAll(compareValues("eps", 0, 0));
		}
		return consensusMap;
	}

	private static double parseValue(String value) {
		try {
			// 문자열에서 쉼표(,) 제거 후 숫자로 변환
			NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
			Number parsed = format.parse(value.replace(",", ""));
			return parsed.doubleValue();
		} catch (Exception e) {
			//e.printStackTrace();
			return 0.0; // 오류 발생 시 0 반환
		}
	}

	private static HashMap<String, String> compareValues(String label, double currentValue, double prevValue) {

		String trend = (currentValue > prevValue) ? "UP" : (currentValue == 0) ? "NULL" : "DOWN";
		double difference = Math.abs(currentValue - prevValue);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(label+"_trend", trend);
		map.put(label+"_difference",  String.format("%.2f", difference));
		//System.out.println(label + "은(는) 이전 분기 대비 " + trend + "했으며, 차이는 " + String.format("%.2f", difference) + "입니다.");
		return map;
	}
}
