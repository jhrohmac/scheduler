
package com.scheduler.finance.dao;

import java.util.List;

import com.scheduler.finance.vo.FinanceOptionsVo;
import com.scheduler.finance.vo.StockDataVo;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.finance.vo.StockPortfolioVo;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;

import java.util.HashMap;

public interface StockCodeInfoDao
{
	public FinanceOptionsVo financeUserSetting(HashMap<String, String> map) throws Exception;
	
	public List<?> searchStocksKeyword(HashMap<String, String> map) throws Exception;
	
	public StockInfoVo selectStockInfo(HashMap<String, String> map) throws Exception;
	
	public List<?> selectPortfolioStockList(HashMap<String, String> map) throws Exception;
	
	public List<?> selectStockMarket(HashMap<String, String> map) throws Exception;
	
	public List<?> selectStockAnalysis(HashMap<String, String> map) throws Exception;
	
	public List<?> selectGroupStockInfo(HashMap<String, String> map) throws Exception;
    
	public List<?> selectGroupStockList(HashMap<String, String> map) throws Exception;
	
	public List<?> selectPortfolioGroupList(HashMap<String, String> map) throws Exception;
	
	public List<?> selectPriceHistory(HashMap<String, String> map) throws Exception;
    
	public StockPortfolioVo selectPriceCalculate(HashMap<String, String> map) throws Exception;
	
	public List<?> selectPositionEventList(HashMap<String, String> map) throws Exception;
	
	public List<?> selectChartEventMarkers(HashMap<String, String> map) throws Exception;
	
	public HashMap<String, Object> selectPositionState(HashMap<String, String> map) throws Exception;
	
	public void mergeInterestStock(StockInfoVo map) throws Exception;
    
	public void portfolioGroupEvent(HashMap<String, String> map) throws Exception;
    
	public void portfolioGroupSort(HashMap<String, String> map) throws Exception;
    
	public void interastStockEvent(HashMap<String, String> map) throws Exception;
	
	public void priceSettingEvent(HashMap<String, String> map) throws Exception;
	
	public void priceHistoryEvent(HashMap<String, String> map) throws Exception;
	
	public void updateUserStockCode(HashMap<String, String> map) throws Exception;
	

    /**
     * 국내 주식 일자별 시세(최근 30일)를 조회해서 StockDataVo 리스트로 반환.
     *
     * @param stockCode 종목코드(예: 005930)
     * @return OHLCV 리스트 (최근일 포함, 날짜 오름차순)
     * @throws Exception KIS 호출 또는 파싱 실패 시
     */
    InquireDailyPriceResult getDailyPrices(String stockCode) throws Exception;
    
    /**
     * KIS 상품기본조회 (search-info)
     *
     * @param pdno        상품번호 (예: 005930)
     * @param prdtTypeCd  상품유형코드 (국내주식: "300")
     * @return KIS 상품기본조회 결과를 StockInfoVo 로 매핑한 객체
     * @throws Exception  KIS 호출 또는 파싱 실패 시
     */
    StockInfoVo getSearchInfo(HashMap<String, String> map) throws Exception;

    /**
     * 주식기본조회 (search-stock-info)
     *
     * @param pdno       상품번호 (종목코드)
     * @param prdtTypeCd 상품유형코드 (국내주식: 300)
     */
    StockInfoVo getSearchStockInfo(HashMap<String, String> map) throws Exception;
    
    /**
     * 국내주식기간별시세(일/주/월/년) – inquire-daily-itemchartprice
     *
     * @param stockCode     종목코드 (FID_INPUT_ISCD)
     * @param fromDate      시작일자(YYYYMMDD, FID_INPUT_DATE_1)
     * @param toDate        종료일자(YYYYMMDD, FID_INPUT_DATE_2)
     * @param periodDivCode 기간분류코드(D/W/M/Y, FID_PERIOD_DIV_CODE)
     * @param orgAdjPrc     수정주가/원주가 구분(0/1, FID_ORG_ADJ_PRC)
     */
    public List<?> getInquireDailyItemchartprice(HashMap<String, String> map) throws Exception;
    
    /**
     * KIS 상품기본조회 (search-info)
     *
     * @param pdno        상품번호 (예: 005930)
     * @param prdtTypeCd  상품유형코드 (국내주식: "300")
     * @return KIS 상품기본조회 결과를 StockInfoVo 로 매핑한 객체
     * @throws Exception  KIS 호출 또는 파싱 실패 시
     */
    StockInfoVo getInquirePriceApi(HashMap<String, String> map) throws Exception;
    
    /**
     * [국내주식] 기본시세 - 주식현재가 시세(inquire-price)를 KIS OpenAPI로 호출
     *
     * InquireDailyPriceApi
     *  - KisClientFactory 에서 KisClient 확보
     *  - InquirePriceApi 생성 및 파라미터 세팅
     *  - client.execute(api) 로 호출
     *  - 응답 검증 후 StockDataVo 로 매핑
     *
     * @param stockCode 종목코드 (예: 005930)
     * @return 현재가 1건 정보가 담긴 StockDataVo (없으면 null)
     */
    InquirePriceResult getCurrentPriceByInquirePrice(HashMap<String, String> map) throws Exception;

	
}