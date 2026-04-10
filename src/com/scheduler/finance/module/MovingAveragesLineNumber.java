package com.scheduler.finance.module;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import com.scheduler.finance.vo.ParseStockDataVo;
import com.scheduler.finance.vo.StockDataVo;

public class MovingAveragesLineNumber {
    
    public static HashMap<String, String> getAveragesLineNumber(List<StockDataVo> dataArray) throws IOException {
    	String stockLines ="";
    	// DecimalFormat 객체 생성
        DecimalFormat df = new DecimalFormat("#.##");
       
        HashMap<String, String> averagesLinePrice = new  HashMap<String, String>();
    	try {
    		// 가격 데이터 추출
    		List<Double> closingPrices = extractClosingPrices(dataArray);
    		// 이동평균 계산
    		List<ParseStockDataVo> movingAverages = calculateMovingAverages(closingPrices);
    		// 이동평균 가격이 높은 순으로 정렬
    		movingAverages.sort(Comparator.comparingDouble(ParseStockDataVo::getAverage).reversed());
    		ArrayList<String> lineList = new ArrayList<String>();
    		
    		// 결과 출력
            int index = 0;
    		for (ParseStockDataVo average : movingAverages) {
    			// 숫자를 소수점 둘째 자리까지 포맷
    	        String line_price = df.format(average.getAverage());
    	        
    			//System.out.println(average.getPeriod() + " 이동평균: " +   line_price);
    			averagesLinePrice.put("stock_sma"+average.getPeriod()+"_price", line_price);
    		       
//    			 // 숫자에 소수점이 있는지 확인
//    	        if (average.getAverage() % 1 == 0) {
//    	            // 소수점이 없는 경우
//    	            System.out.println("Formatted number not: " + (int)average.getAverage());
//    	        } else {
//    	            // 소수점이 있는 경우
//    	            System.out.println("Formatted number: " + String.format("%.2f", average.getAverage()));
//    	        }
    	        
    			lineList.add(average.getPeriod());
    			if(index == 4) {
    				stockLines+= average.getPeriod();
    			}else {
    				stockLines += average.getPeriod()+",";	
    			}
    			index ++;
    		}
    		averagesLinePrice.put("stock_sma_lines", stockLines);
    	
    	} catch (JSONException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return averagesLinePrice;
    }
    
    // 종가 데이터 추출
    private static List<Double> extractClosingPrices(List<StockDataVo> data) {
    	List<Double> closingPrices = new ArrayList<>();
    	for (StockDataVo entry : data) {
    		closingPrices.add(entry.getClose()); // 종가 데이터는 인덱스 4에 위치
    	}
    	return closingPrices;
    }

    // 이동평균 계산
    private static List<ParseStockDataVo> calculateMovingAverages(List<Double> prices) throws Exception {
        int[] periods = {5, 20, 60, 120, 240};
        List<ParseStockDataVo> movingAverages = periodsToAverages(prices, periods);
        return movingAverages;
    }

    private static List<ParseStockDataVo> periodsToAverages(List<Double> prices, int[] periods) throws Exception {
        List<ParseStockDataVo> movingAverages = new ArrayList<>();
        for (int period : periods) {
            double average = calculateMovingAverage(prices, period);
            movingAverages.add(new ParseStockDataVo(period + "", average));
        }
        return movingAverages;
    }

    // 이동평균 계산
    private static double calculateMovingAverage(List<Double> prices, int period) throws Exception {
    	double result = 0;
    	double sum = 0;
    	if (prices.size() < period) {
			//System.out.println("데이터가 이동평균 기간보다 짧습니다.");
            //throw new IllegalArgumentException("데이터가 이동평균 기간보다 짧습니다.");
        }else {
            for (int i = prices.size() - period; i < prices.size(); i++) {
                sum += prices.get(i);
            }
            result = sum / period;
        }
        return result;
    }
}