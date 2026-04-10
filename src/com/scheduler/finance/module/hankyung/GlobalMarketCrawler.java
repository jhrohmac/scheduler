package com.scheduler.finance.module.hankyung;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class GlobalMarketCrawler {

    public static List<String> getGlobalMarket() {
    	boolean page_div = false;
    	
    	List<String> stockCodes = new ArrayList<>();
    	
        String baseUrl = "https://www.hankyung.com/globalmarket/usa-marketcap-nasdaq?page=";
        		//baseUrl = "https://www.hankyung.com/globalmarket/usa-marketcap-nyse?page=";
        //https://markets.hankyung.com/api/v2/korean-stocks?sortBy=kospi200
        
        //https://markets.hankyung.com/api/v2/korean-stocks?sortBy=mkt_cap
        	//baseUrl = "https://www.hankyung.com/globalmarket/usa-stock-sp500";	// sp500
        	baseUrl = "https://markets.hankyung.com/indices/kospi200";	// sp500
        	
        	page_div = true;
        try {
        	if(page_div) {
        		stockCodes = getStockInfo(baseUrl);
        	}else {
        		int totalPages = getTotalPages(baseUrl);
                if(totalPages > 5) {
                	totalPages = 5;
                }
                
                for (int page = 1; page <= totalPages; page++) {
                    String url = baseUrl + page;
                    System.out.println("Fetching data from: " + url);
                    stockCodes = getStockInfo(baseUrl);
                }
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockCodes;
    }

    public static List<String> getStockInfo(String baseUrl) throws IOException {
    	List<String> stockCodes = new ArrayList<String>();
        // URL에서 HTML 가져오기
        Document doc = org.jsoup.Jsoup.connect(baseUrl).get();
        
        Elements stockRows = doc.select("table.table-stock tbody tr");

        for (Element row : stockRows) {
        	String symbol = row.select(".symbol.txt-en").first().text().trim();
        	   stockCodes.add(symbol);
//            String companyName = row.select(".stock-name").first().text().trim();
//            String price = row.select(".price.txt-num").first().text().trim();
//            String fluctuation = row.select(".ratio-box.txt-num").first().text().trim();
//            String volume = row.select(".col4.col-pc .txt-num").first().text().trim();
//            String tradingValue = row.select(".col5.col-pc .txt-num").first().text().trim();
//            String marketCap = row.select(".col6.col-pc .txt-num").first().text().trim();
//            String fiftyTwoWeekRange = row.select(".col7.col-pc .progress-val .min").first().text().trim() + " - " +
//                    row.select(".col7.col-pc .progress-val .max").first().text().trim();
            
         
            
//            System.out.println("Number: " + "");
//            System.out.println("종목명: " + companyName);
//            System.out.println("symbol: " + symbol);
//            System.out.println("시세: " + price);
//            System.out.println("등락률: " + fluctuation);
//            System.out.println("거래량: " + volume);
//            System.out.println("거래대금: " + tradingValue);
//            System.out.println("시가총액: " + marketCap);
//            System.out.println("52주 범위: " + fiftyTwoWeekRange);
//            System.out.println();
        }
        return stockCodes;
    }
    private static int getTotalPages(String baseUrl) throws IOException {
        Document doc = Jsoup.connect(baseUrl + "1").get();
        Element selectBox = doc.selectFirst("div.select-box select");
        String lastOptionValue = selectBox.select("option").last().attr("value");
        return Integer.parseInt(lastOptionValue);
    }
}
