package com.scheduler.finance.vo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ParseStockDataVo {
	private String period;
    private double average;
    
	List<List<Double>> data;

	public List<List<Double>> getData() {
		return data;
	}

	public void setData(List<List<Double>> data) {
		this.data = data;
	}
	
    public ParseStockDataVo(String period, double average) {
        this.period = period;
        this.average = average;
    }

    public String getPeriod() {
        return period;
    }

    public double getAverage() {
        return average;
    }

    public static List<StockDataVo> generateData(List<List<Double>> rawData) {
        List<StockDataVo> monthlyDataList = new ArrayList<>();
        // List Vo에 추가 날짜, 시가, 고가, 저가, 종가, 거래량
        for (List<Double> data : rawData) {
        	Long  timeStamp = (new Double(data.get(0))).longValue();
            StockDataVo monthlyData = new StockDataVo(timeStamp, data.get(1), data.get(2), data.get(3), data.get(4), data.get(5));
            monthlyDataList.add(monthlyData);
        }
        monthlyDataList = monthlyDataList.stream().sorted(Comparator.comparing(StockDataVo::getDate)).collect(Collectors.toList());
        return monthlyDataList;
    }
}
