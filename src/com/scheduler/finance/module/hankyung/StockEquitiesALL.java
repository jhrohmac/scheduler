package com.scheduler.finance.module.hankyung;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.scheduler.comm.util.GetHttpsURLConnection;
import com.scheduler.finance.vo.StockInfoVo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* 종목 전체 가져오기
*/
public class StockEquitiesALL {
    public static List<StockInfoVo> getStockEquitiesALL(String global, String market) {
    	
	    long currentTimeMillis = System.currentTimeMillis();
	    String date = String.valueOf(currentTimeMillis);
	    //String global ="korea" or "us";
	    //String market ="kospi" or "kosdaq";
	    
	    List<StockInfoVo> list = new ArrayList<StockInfoVo>();
	    String resultString ="";
	                 
    	String url ="https://datacenter.hankyung.com/equities-all/"+global+"?type="+market+"&_="+date;
    	System.out.println(url);
        try {
        	resultString = GetHttpsURLConnection.getHttpsGet(url);
	        Gson gson = new Gson();
	        
	        if(global.equals("us")) {
	            Data[] dataArray = gson.fromJson(resultString, Data[].class);
	            for (Data data : dataArray) {
	            	String stock_sector =data.name.trim();
	                System.out.println("Name: " + data.name);
	                //System.out.println("Market Cap: " + data.marketCap);
	                for (SubData sub : data.sub) {
	                	StockInfoVo stockVo = new StockInfoVo();
	                	stockVo.setStock_code(sub.symbol);
	                	stockVo.setStock_sector(stock_sector);
//	                    System.out.println("Sub Name: " + sub.name);
//	                    System.out.println("Code: " + sub.code);
//	                    System.out.println("Symbol: " + sub.symbol);
//	                    System.out.println("Close Price: " + sub.closePrice);
//	                    System.out.println("Change Net: " + sub.chgNet);
//	                    System.out.println("Change Rate: " + sub.chgRate);
//	                    System.out.println("Market Cap: " + sub.marketCap);
//	                    System.out.println("Class: " + sub.classType);
//	                    System.out.println("Mark: " + sub.mark);
	                	list.add(stockVo);
	                }
	            }
			}else {
			
				Result result = gson.fromJson(resultString, Result.class);
				for (Data data : result.data) {
					String stock_sector =data.name.trim();
		            for (SubData sub : data.sub) {
		            	StockInfoVo stockVo = new StockInfoVo();
		            	stockVo.setStock_code(sub.shortcode);
		            	stockVo.setStock_sector(stock_sector);
		            	list.add(stockVo);
	            	}
	        	}
	        }
        } catch (Exception e) {
			e.printStackTrace();
		}
        return list;
    }
}

class Time {
    @SerializedName("date")
    String date;

    @SerializedName("status")
    String status;
}

class SubData {

    @SerializedName("code")
    String code;

    @SerializedName("symbol")
    String symbol;

    @SerializedName("close_price")
    String closePrice;

    @SerializedName("chg_net")
    String chgNet;

    @SerializedName("chg_rate")
    String chgRate;

    @SerializedName("market_cap")
    String marketCap;

    @SerializedName("class")
    String classType;

    @SerializedName("mark")
    String mark;
    
    @SerializedName("name")
    String name;

    @SerializedName("type1")
    String type1;

    @SerializedName("type2")
    String type2;

    @SerializedName("shortcode")
    String shortcode;

    @SerializedName("stock_count")
    String stockCount;

    @SerializedName("close")
    String close;

    @SerializedName("diff")
    String diff;

    @SerializedName("rate")
    String rate;

    @SerializedName("li_class")
    String liClass;

    @SerializedName("sign")
    String sign;

    @SerializedName("total_price")
    String totalPrice;
}

class Data {

    @SerializedName("market_cap")
    double marketCap;

    @SerializedName("total_price")
    long totalPrice;

    @SerializedName("name")
    String name;

    @SerializedName("sub")
    List<SubData> sub;
}

class Result {
    @SerializedName("time")
    Time time;

    @SerializedName("data")
    List<Data> data;
}