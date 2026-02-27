// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.dao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.scheduler.comm.exp.UserTransactionException;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.finance.dao.StockCodeInfoDao;
import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.finance.vo.FinanceOptionsVo;
import com.scheduler.finance.vo.StockDataVo;
import com.scheduler.finance.vo.StockGroupVo;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.finance.vo.StockPortfolioVo;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult.Output2;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyPriceResult.Output1;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyIndexchartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireDailyIndexchartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyPriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasTimeItemchartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasTimeItemchartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquirePriceResult;
import com.scheduler.kis_api.api.rest.quotations.InquireTimeDailychartpriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireTimeDailychartpriceResult;
import com.scheduler.kis_api.api.rest.quotations.SearchInfoApi;
import com.scheduler.kis_api.api.rest.quotations.SearchInfoResult;
import com.scheduler.kis_api.api.rest.quotations.SearchInfoResult.Output;
import com.scheduler.kis_api.api.rest.quotations.SearchStockInfoApi;
import com.scheduler.kis_api.api.rest.quotations.SearchStockInfoResult;
import com.scheduler.kis_client.KisClient;

public class StockCodeInfoDaoImpl extends SqlSessionDaoSupport implements StockCodeInfoDao
{


    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static Logger logger;
    private static final String  NS ="sql.StockCodeInfo.";
    
    public FinanceOptionsVo financeUserSetting(HashMap<String, String> map) throws Exception {
    	FinanceOptionsVo financeVo;
    	try {
    		financeVo = getSqlSession().selectOne(NS+"financeUserSetting", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".financeUserSetting() : ", e.getLocalizedMessage());
    	}
    	return financeVo;
    }
    public List<?> searchStocksKeyword(HashMap<String, String> map) throws Exception {
    	List<StockInfoVo> list;
    	try {
    		list = getSqlSession().selectList(NS+"searchStocksKeyword", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".searchStocksKeyword() : ", e.getLocalizedMessage());
    	}
    	return list;
    }

    public List<?> selectPortfolioStockList(HashMap<String, String> map) throws Exception {
    	List<StockInfoVo> list;
    	try {
    		list = getSqlSession().selectList(NS+"selectPortfolioStockList", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectPortfolioStockList() : ", e.getLocalizedMessage());
    	}
    	return list;
    }
    public List<?> selectStockMarket(HashMap<String, String> map) throws Exception {
    	List<StockInfoVo> list;
    	try {
    		list = getSqlSession().selectList(NS+"selectStockMarket", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectStockMarket() : ", e.getLocalizedMessage());
    	}
    	return list;
    }
    
    public List<?> selectStockAnalysis(HashMap<String, String> map) throws Exception {
    	List<StockInfoVo> list;
    	try {
    		list = getSqlSession().selectList(NS+"selectStockAnalysis", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectStockAnalysis() : ", e.getLocalizedMessage());
    	}
    	return list;
    }
    
    public List<StockInfoVo> selectGroupStockList(HashMap<String, String> map) throws Exception {
        List<StockInfoVo> list = null;
        try {
            list = getSqlSession().selectList(NS+"selectGroupStockList", map);
            System.out.println("===========size===="+list.size());
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectGroupStockList() : ", e.getLocalizedMessage());
        }
        return list;
    }
    
    public StockInfoVo selectStockInfo(HashMap<String, String> map) throws Exception {
    	System.out.println(map);
        StockInfoVo stockVo = new StockInfoVo();
        try {
            stockVo = (StockInfoVo)this.getSqlSession().selectOne(NS+"selectStockInfo", map);
            System.out.println(">>>>>>>>>>>"+stockVo.getStock_code());
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectStockInfo() : ", e.getLocalizedMessage());
        }
        return stockVo;
    }
    
    public List<?> selectGroupStockInfo(HashMap<String, String> map) throws Exception {
        List<StockInfoVo> list;
        try {
            list = getSqlSession().selectList(NS+"selectGroupStockInfo",map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectGroupStockInfo() : ", e.getLocalizedMessage());
        }
        StockCodeInfoDaoImpl.logger.info("End StockCodeInfoDaoImpl > selectGroupStockInfo");
        return list;
    }
    
    public List<?> selectPortfolioGroupList(HashMap<String, String> map) throws Exception {
        List<StockGroupVo> list;
        try {
            list = getSqlSession().selectList(NS+"selectPortfolioGroupList", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectPortfolioGroupList() : ", e.getLocalizedMessage());
        }
        return list;
    }
    
    public List<?> selectPriceHistory(final HashMap<String, String> map) throws Exception {
        List<StockPortfolioVo> list;
        try {
        	 list = getSqlSession().selectList(NS+"selectPriceHistory", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectPriceHistory() : ", e.getLocalizedMessage());
        }
        return list;
    }
    
    public void mergeInterestStock(StockInfoVo stockInfoVo) throws Exception {
        try {
            this.getSqlSession().insert(NS+"mergeInterestStock", stockInfoVo);
        }
        catch (Exception e) {
        	e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".mergeInterestStock() : ", e.getLocalizedMessage());
        }
    }
    
    public void portfolioGroupEvent(HashMap<String, String> map) throws Exception {
        try {
            
            switch (map.get("eventCode")) {
                case "I": {
                    this.getSqlSession().insert(NS+"insertGroupEvent", map);
                    break;
                }
                case "C": {
                	//그룹 생성
                	this.getSqlSession().insert(NS+"insertGroupEvent", map);
                	// 복사 그룹 종목 추가
                	this.getSqlSession().update(NS+"copyGroupStockListEvent", map);
                	break;
                }
                case "U": {
                    this.getSqlSession().update(NS+"updateGroupEvent", map);
                    break;
                }
                case "D": {
                    this.getSqlSession().delete(NS+"deleteGroupEvent", map);
                    this.getSqlSession().delete(NS+"deleteGroupStockListEvent", map);
                    break;
                }
                default:
                    break;
            }
        }
        catch (Exception e) {
    		e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".portfolioGroupEvent() : ", e.getLocalizedMessage());
        }
    }
    
    public void portfolioGroupSort(HashMap<String, String> map) throws Exception {
        try {
            String[] code = map.get("group_List").split(",");
            for (int i = 0; i < code.length; ++i) {
                HashMap<String, String> sortMap = new HashMap<String, String>();
                sortMap.put("group_seq", String.valueOf(i));
                sortMap.put("group_id", code[i]);
                this.getSqlSession().update(NS+"updateGroupSort", sortMap);
            }
        }
        catch (Exception e) {
    		e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".portfolioGroupSort() : ", e.getLocalizedMessage());
        }
    }
    
    public void interastStockEvent(HashMap<String, String> map) throws Exception {
        System.out.println(map);
        try {
            String s;
            switch (s = map.get("eventCode")) {
                case "D": {
                    this.getSqlSession().delete(NS+"deleteInterastStockEvent", map);
                    break;
                }
                case "I": {
                    this.getSqlSession().insert(NS+"insertInterastStockEvent", map);
                    break;
                }
                case "U": {
                    String[] codeList = map.get("stock_code").split(",");
                    for (int i = 0; i < codeList.length; ++i) {
                        HashMap<String, String> sortMap = new HashMap<String, String>();
                        sortMap.put("stock_seq", String.valueOf(i));
                        sortMap.put("group_id", map.get("group_id"));
                        sortMap.put("stock_code", codeList[i]);
                        System.out.println(sortMap);
                        this.getSqlSession().update(NS+"updateInterastStockEvent", sortMap);
                    }
                    break;
                }
                default:
                    break;
            }
        }
        catch (Exception e) {
        	e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".interastStockEvent() : ", e.getLocalizedMessage());
        }
    }
    
    public void priceSettingEvent(HashMap<String, String> map) throws Exception {
    	try {
    		String s;
    		switch (s = map.get("eventCode")) {
	    		case "I": {
	    			this.getSqlSession().insert(NS+"insertStockPortfolioEvent", map);
	    			// WF-3-8: 보유 포지션 스냅샷/이력 동기화
	    			syncPositionLedger(map, "ADD");
	    			break;
	    		}
	    		case "U": {
	    			String[] codeList = map.get("stock_code").split(",");
	    			for (int i = 0; i < codeList.length; ++i) {
	    				HashMap<String, String> sortMap = new HashMap<String, String>();
	    				sortMap.put("stock_seq", String.valueOf(i));
	    				sortMap.put("group_id", map.get("group_id"));
	    				sortMap.put("stock_code", codeList[i]);
	    				
	    				this.getSqlSession().update(NS+"updateStockPortfolioEvent", sortMap);
	    			}
	    			break;
	    		}
	    		
	    		case "D": {
	    			this.getSqlSession().delete(NS+"deleteStockPortfolioEvent", map);
	    			syncPositionLedger(map, "DELETE");
	    			break;
	    		}
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    		throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".interastStockEvent() : ", e.getLocalizedMessage());
    	}
    }
    
    public StockPortfolioVo selectPriceCalculate(HashMap<String, String> map) throws Exception {
        StockPortfolioVo stockPortfolioVo;
        try {
        	stockPortfolioVo = getSqlSession().selectOne(NS+"selectPriceCalculate", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectPriceCalculate() : ", e.getLocalizedMessage());
        }
        return stockPortfolioVo;
    }

    public List<?> selectPositionEventList(HashMap<String, String> map) throws Exception {
        List<HashMap<String, Object>> list;
        try {
            list = getSqlSession().selectList(NS+"selectPositionEventList", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectPositionEventList() : ", e.getLocalizedMessage());
        }
        return list;
    }

    public List<?> selectChartEventMarkers(HashMap<String, String> map) throws Exception {
        List<HashMap<String, Object>> list;
        try {
            list = getSqlSession().selectList(NS+"selectChartEventMarkers", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectChartEventMarkers() : ", e.getLocalizedMessage());
        }
        return list;
    }

    public HashMap<String, Object> selectPositionState(HashMap<String, String> map) throws Exception {
        HashMap<String, Object> row;
        try {
            row = getSqlSession().selectOne(NS+"selectPositionState", map);
        }
        catch (Exception e) {
            throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".selectPositionState() : ", e.getLocalizedMessage());
        }
        return row;
    }
   
    
    public void priceHistoryEvent(HashMap<String, String> map) throws Exception {
    	try {
    		String s;
    		switch (s = map.get("eventCode")) {
	    		case "I": {
	    			this.getSqlSession().insert(NS+"insertStockPortfolioEvent", map);
	    			syncPositionLedger(map, "ADD");
	    			break;
	    		}
	    		case "U": {
	    			this.getSqlSession().insert(NS+"updatePriceHistoryEvent", map);
	    			break;
	    		}
	    		case "C": {
	    			this.getSqlSession().insert(NS+"closePriceHistoryEvent", map);
	    			syncPositionLedger(map, "DELETE");
	    			break;
	    		}
	    		case "D": {
	    			getSqlSession().delete(NS+"deletePriceHistoryEvent", map);
	    			syncPositionLedger(map, "DELETE");
	    			
	    			List<StockPortfolioVo> list = null;
	    		 	list = getSqlSession().selectList(NS+"selectPriceHistoryEvent", map);
	    			for (int i=0; i<list.size(); i++) {
	    				HashMap<String, String> sortMap = new HashMap<String, String>();
	    				sortMap.put("stock_newSeq", String.valueOf(i+1));
	    				sortMap.put("stock_oldSeq", list.get(i).getStock_seq());
	    				sortMap.put("group_id", map.get("group_id"));
	    				sortMap.put("stock_code", map.get("stock_code"));
	    				System.out.println(sortMap);
	    				getSqlSession().update(NS+"updatePriceHistoryEvent", sortMap);
	    			}
				break;
				}
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    		//throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".priceHistoryEvent() : ", e.getLocalizedMessage());
    	}
    }
    
    private void syncPositionLedger(HashMap<String, String> map, String actionType) {
    	try {
    		HashMap<String, String> m = new HashMap<String, String>();
    		m.put("group_id", map.get("group_id"));
    		m.put("stock_code", map.get("stock_code"));
    		m.put("in_user_id", map.get("in_user_id"));

    		String qty = String.valueOf(map.get("priceCount") == null ? "0" : map.get("priceCount"));
    		String price = String.valueOf(map.get("start_price") == null ? map.get("purchase_price") : map.get("start_price"));
    		String reason = String.valueOf(map.get("price_description") == null ? "" : map.get("price_description"));

    		StockPortfolioVo calcVo = getSqlSession().selectOne(NS + "selectPriceCalculate", m);
    		double afterQty = 0.0;
    		double afterAvg = 0.0;
    		if (calcVo != null) {
    			try { afterQty = Double.parseDouble(calcVo.getTotal_quantity()); } catch (Exception ignore) {}
    			try {
    				double total = Double.parseDouble(calcVo.getTotal_purchase_price());
    				afterAvg = (afterQty > 0 ? total / afterQty : 0.0);
    			} catch (Exception ignore) {}
    		}

    		String act = actionType;
    		if ("ADD".equals(actionType) && reason.contains("물타기")) {
    			act = "AVERAGE_DOWN";
    		}

    		double tradePrice = parseDoubleSafe(price);
    		String eventType = standardizePositionEventType(act, afterQty, afterAvg, tradePrice);

    		m.put("action_type", act);
    		m.put("qty", qty == null ? "0" : qty);
    		m.put("price", price == null ? "0" : price);
    		m.put("before_qty", "0");
    		m.put("before_avg", "0");
    		m.put("after_qty", String.valueOf(afterQty));
    		m.put("after_avg", String.valueOf(afterAvg));
    		String eventJson = buildPositionEventJson(eventType, act, qty, price, afterQty, afterAvg, tradePrice);
    		String eventMsg = buildPositionEventMessage(eventType, act, qty, price, afterQty, afterAvg, tradePrice);

    		m.put("reason_text", reason);
    		m.put("json_params", eventJson);

    		getSqlSession().insert(NS + "insertPositionTxnEvent", m);

    		if ("EXIT".equals(eventType)) {
    			getSqlSession().update(NS + "closePositionSnapshot", m);
    			m.put("event_level", "INFO");
    		} else {
    			getSqlSession().update(NS + "mergePositionSnapshot", m);
    			m.put("event_level", ("RISK_OFF".equals(eventType) ? "WARN" : "INFO"));
    		}
    		m.put("event_type", eventType);
    		m.put("message", eventMsg);
    		m.put("json_params", eventJson);
    		getSqlSession().insert(NS + "insertPositionEvent", m);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    private double parseDoubleSafe(String v) {
    	try {
    		if (v == null) return 0.0;
    		return Double.parseDouble(v.replaceAll(",", "").trim());
    	} catch (Exception ignore) {
    		return 0.0;
    	}
    }

    private String standardizePositionEventType(String actionType, double afterQty, double afterAvg, double tradePrice) {
    	final double RISK_OFF_THRESHOLD = -0.03d;
    	final double TREND_FOLLOW_THRESHOLD = 0.01d;

    	if ("DELETE".equals(actionType) || afterQty <= 0) return "EXIT";
    	if ("AVERAGE_DOWN".equals(actionType)) return "AVERAGE_DOWN";

    	if (afterAvg > 0 && tradePrice > 0) {
    		double pct = (tradePrice - afterAvg) / afterAvg;
    		if (pct <= RISK_OFF_THRESHOLD) return "RISK_OFF";
    		if (pct >= TREND_FOLLOW_THRESHOLD) return "TREND_FOLLOW";
    	}
    	return "ENTER";
    }

    private String buildPositionEventMessage(String eventType, String actionType, String qty, String price, double afterQty, double afterAvg, double tradePrice) {
    	String base = "[" + eventType + "] action=" + actionType + ", qty=" + qty + ", price=" + price
    			+ ", after_qty=" + String.format("%.4f", afterQty)
    			+ ", after_avg=" + String.format("%.4f", afterAvg);

    	if (afterAvg > 0 && tradePrice > 0) {
    		double pct = ((tradePrice - afterAvg) / afterAvg) * 100.0d;
    		base += ", pnl_pct=" + String.format("%.2f", pct) + "%";
    	}

    	if ("RISK_OFF".equals(eventType)) {
    		return base + " (손실 축소 구간 진입)";
    	}
    	if ("TREND_FOLLOW".equals(eventType)) {
    		return base + " (추세추종 구간 유지)";
    	}
    	if ("AVERAGE_DOWN".equals(eventType)) {
    		return base + " (평단 조정)";
    	}
    	if ("EXIT".equals(eventType)) {
    		return base + " (포지션 종료)";
    	}
    	return base;
    }

    private String buildPositionEventJson(String eventType, String actionType, String qty, String price, double afterQty, double afterAvg, double tradePrice) {
    	double pnlPct = 0.0d;
    	if (afterAvg > 0 && tradePrice > 0) {
    		pnlPct = ((tradePrice - afterAvg) / afterAvg) * 100.0d;
    	}
    	return "{"
    		+ "\"event_type\":\"" + eventType + "\"," 
    		+ "\"action_type\":\"" + actionType + "\"," 
    		+ "\"qty\":\"" + qty + "\"," 
    		+ "\"price\":\"" + price + "\"," 
    		+ "\"after_qty\":" + String.format("%.4f", afterQty) + ","
    		+ "\"after_avg\":" + String.format("%.4f", afterAvg) + ","
    		+ "\"pnl_pct\":" + String.format("%.2f", pnlPct)
    		+ "}";
    }

    public void updateUserStockCode(HashMap<String, String> map) throws Exception {
    	try {
    		
    		getSqlSession().update(NS+"updateUserStockCode", map);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    		//throw new UserTransactionException(String.valueOf(this.getClass().getName()) + ".priceHistoryEvent() : ", e.getLocalizedMessage());
    	}
    	StockCodeInfoDaoImpl.logger.info("End StockCodeInfoDaoImpl > priceHistoryEvent");
    }
    
    /**
     * 종목 일별 30일 조회 (KIS 사용)
     *
     * 요청 파라미터:
     *  - stock_code : 6자리 종목코드 (예: 005930)
     *
     * 응답:
     *  {
     *    "success": true,
     *    "data": {
     *      "stockCode": "005930",
     *      "tradeDate": "20251122",
     *      "closePrice": "72800",
     *      "openPrice": "73000",
     *      ...
     *    },
     *    "message": ""
     *  }
     */
    @Override
    public InquireDailyPriceResult getDailyPrices(String stockCode) throws Exception {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("stockCode is required");
        }

        String trimmedCode = stockCode.trim();
        if ("KOSPI".equalsIgnoreCase(trimmedCode)) {
            trimmedCode = "0001";
        } else if ("KOSDAQ".equalsIgnoreCase(trimmedCode)) {
            trimmedCode = "1001";
        }
        
        // 1) 공통 KisClient 얻기 (KisConfigLoader + KisClientFactory)
        // 30분봉 요청은 KIS "주식일별분봉조회" API로 처리
        // 30분봉 처리는 getInquireDailyItemchartprice() 경로에서 수행
        KisClient client = KisClientFactory.getClient();

        // 2) KIS API 요청 객체 생성
        InquireDailyPriceApi api = new InquireDailyPriceApi();
        api.setFidInputIscd(trimmedCode);   // 종목코드
        // 기본값: fidPeriodDivCode = "D" (일), fidOrgAdjPrc = "1" (수정주가)
        // 필요시 다른 옵션 세팅 가능
        System.out.println(api.getFidCondMrktDivCode());
        System.out.println(api.getFidInputIscd());
        System.out.println(api.getFidOrgAdjPrc());
        System.out.println(api.getTrId());
        
        // 3) 호출
        InquireDailyPriceResult result = client.execute(api);
        
        // 4) 결과 코드 체크
        if (result == null) {
        	System.out.println("KIS 응답이 null 입니다.");
        	throw new IllegalStateException("KIS 응답이 null 입니다.");
        }
        
        // 6) 결과 출력
        System.out.println("=== InquirePriceResult ===");
        System.out.println("rt_cd  = " + result.getRtCd());
        System.out.println("msg_cd = " + result.getMsgCd());
        System.out.println("msg1   = " + result.getMsg1());

        if (!"0".equals(result.getRtCd())) {
            String msg = "KIS API 오류 rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd()
                    + ", msg1=" + result.getMsg1();
            throw new IllegalStateException(msg);
        }

        Output1[] outputs = result.getOutput1();
        if (outputs == null || outputs.length == 0) {
            return new InquireDailyPriceResult();
        }

        return result;
    }
    
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
    public InquirePriceResult getCurrentPriceByInquirePrice(HashMap<String, String> map) throws Exception {
    	String stockCode 		= map.get("in_stockCode"); 
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("stockCode is required");
        }

        String trimmedCode = stockCode.trim();

        // 1) 공통 KisClient 얻기
        KisClient client = KisClientFactory.getClient();

        // 2) KIS API 요청 객체 생성
        InquirePriceApi api = new InquirePriceApi();
        api.setFidInputIscd(trimmedCode);  // 종목코드
        // 필요 시 시장 코드 변경 가능 (기본값 UN)
        // api.setFidCondMrktDivCode("UN");

        // 3) 호출
        InquirePriceResult result = client.execute(api);

        // 4) 결과 코드 체크
        if (result == null) {
            System.out.println("KIS 응답이 null 입니다.");
            throw new IllegalStateException("KIS 응답이 null 입니다.");
        }

        System.out.println("=== InquirePriceResult ===");
        System.out.println("rt_cd  = " + result.getRtCd());
        System.out.println("msg_cd = " + result.getMsgCd());
        System.out.println("msg1   = " + result.getMsg1());

        if (!"0".equals(result.getRtCd())) {
            String msg = "KIS API 오류 rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd()
                    + ", msg1=" + result.getMsg1();
            throw new IllegalStateException(msg);
        }
        
        return result;
    }

    /**
     * 상품기본조회(search-info)를 KIS OpenAPI로 호출하는 메서드.
     * 주식현재가 시세[v1_국내주식-008]
     * /uapi/domestic-stock/v1/quotations/inquire-price
     * InquirePriceApi
     * - KisClientFactory 에서 KisClient 확보
     * - SearchInfoApi 생성 및 파라미터 세팅
     * - client.execute(api) 로 호출
     * - 응답 검증 후 StockInfoVo 로 매핑한다.
     */
    @Override
    public StockInfoVo getInquirePriceApi(HashMap<String, String> map) throws Exception {
    	String pdno 		= map.get("in_stockCode"); 
    	String prdtTypeCd 	= map.get("prdtTypeCd"); 
        if (pdno == null || pdno.trim().isEmpty()) {
            throw new IllegalArgumentException("pdno(상품번호)는 필수입니다.");
        }

        String trimmedPdno = pdno.trim();

        // 국내 주식 기본값: 300(주식)
        String effectivePrdtTypeCd;
        if (prdtTypeCd == null || prdtTypeCd.trim().isEmpty()) {
            effectivePrdtTypeCd = "300";	//국내
        } else {
            effectivePrdtTypeCd = prdtTypeCd.trim();
        }

        // 1) 공통 KisClient 얻기
        KisClient client = KisClientFactory.getClient();

        // 2) KIS API 요청 객체 생성
        SearchInfoApi api = new SearchInfoApi();
        api.setPdno(trimmedPdno);
        api.setPrdtTypeCd(effectivePrdtTypeCd);

        // 3) 상품기본조회 호출
        SearchInfoResult result = client.execute(api);

        // 4) 결과 Null 체크
        if (result == null) {
            logger.error("KIS 상품기본조회 응답이 null 입니다. pdno={}, prdtTypeCd={}", trimmedPdno, effectivePrdtTypeCd);
            throw new IllegalStateException("KIS 상품기본조회 응답이 null 입니다.");
        }

        // 5) 결과 코드 출력
        System.out.println("===== SearchInfoResult =====");
        System.out.println("rt_cd  = " + result.getRtCd());
        System.out.println("msg_cd = " + result.getMsgCd());
        System.out.println("msg1   = " + result.getMsg1());

        // 6) rt_cd 체크 (0: 정상)
        if (!"0".equals(result.getRtCd())) {
            String msg = "KIS 상품기본조회 API 오류 rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd()
                    + ", msg1=" + result.getMsg1();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }

        // 7) output 객체 획득
        Output output = result.getOutput();
        if (output == null) {
            logger.warn("KIS 상품기본조회 output 이 null 입니다. pdno={}, prdtTypeCd={}", trimmedPdno, effectivePrdtTypeCd);
            return null;
        }

        // 8) KIS output → StockInfoVo 매핑
        StockInfoVo vo = new StockInfoVo();
        vo.setStock_code(output.getPdno());
        vo.setStock_ko_name(output.getPrdtName());
        vo.setStock_en_name(output.getPrdtEngName());
        // 필요 시 추가 필드 매핑
        // vo.setStock_market(...);
        // vo.setStock_sector(...);
        // vo.setStock_type(...);
        // vo.setStock_type_specs(...);
        // vo.setStock_currency(...);
        // 등등

        System.out.println("===== Mapped StockInfoVo (SearchInfo) =====");
        System.out.println("stock_code    = " + vo.getStock_code());
        System.out.println("stock_ko_name = " + vo.getStock_ko_name());
        System.out.println("stock_en_name = " + vo.getStock_en_name());

        return vo;
    }

    /**
     * 상품기본조회(search-info)를 KIS OpenAPI로 호출하는 메서드.
     *
     * InquireDailyPriceApi / getDailyPrices 와 동일한 패턴으로
     * - KisClientFactory 에서 KisClient 확보
     * - SearchInfoApi 생성 및 파라미터 세팅
     * - client.execute(api) 로 호출
     * - 응답 검증 후 StockInfoVo 로 매핑한다.
     */
    @Override
    public StockInfoVo getSearchInfo(HashMap<String, String> map) throws Exception {
    	String pdno 		= map.get("in_stockCode"); 
    	String prdtTypeCd 	= map.get("prdtTypeCd"); 
        if (pdno == null || pdno.trim().isEmpty()) {
            throw new IllegalArgumentException("pdno(상품번호)는 필수입니다.");
        }

        String trimmedPdno = pdno.trim();

        // 국내 주식 기본값: 300(주식)
        String effectivePrdtTypeCd;
        if (prdtTypeCd == null || prdtTypeCd.trim().isEmpty()) {
            effectivePrdtTypeCd = "300";	//국내
        } else {
            effectivePrdtTypeCd = prdtTypeCd.trim();
        }

        // 1) 공통 KisClient 얻기
        KisClient client = KisClientFactory.getClient();

        // 2) KIS API 요청 객체 생성
        SearchInfoApi api = new SearchInfoApi();
        api.setPdno(trimmedPdno);
        api.setPrdtTypeCd(effectivePrdtTypeCd);

        // 3) 상품기본조회 호출
        SearchInfoResult result = client.execute(api);

        // 4) 결과 Null 체크
        if (result == null) {
            logger.error("KIS 상품기본조회 응답이 null 입니다. pdno={}, prdtTypeCd={}", trimmedPdno, effectivePrdtTypeCd);
            throw new IllegalStateException("KIS 상품기본조회 응답이 null 입니다.");
        }

        // 5) 결과 코드 출력
        System.out.println("===== SearchInfoResult =====");
        System.out.println("rt_cd  = " + result.getRtCd());
        System.out.println("msg_cd = " + result.getMsgCd());
        System.out.println("msg1   = " + result.getMsg1());

        // 6) rt_cd 체크 (0: 정상)
        if (!"0".equals(result.getRtCd())) {
            String msg = "KIS 상품기본조회 API 오류 rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd()
                    + ", msg1=" + result.getMsg1();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }

        // 7) output 객체 획득
        Output output = result.getOutput();
        if (output == null) {
            logger.warn("KIS 상품기본조회 output 이 null 입니다. pdno={}, prdtTypeCd={}", trimmedPdno, effectivePrdtTypeCd);
            return null;
        }

        // 8) KIS output → StockInfoVo 매핑
        StockInfoVo vo = new StockInfoVo();
        vo.setStock_code(output.getPdno());
        vo.setStock_ko_name(output.getPrdtName());
        vo.setStock_en_name(output.getPrdtEngName());
        // 필요 시 추가 필드 매핑
        // vo.setStock_market(...);
        // vo.setStock_sector(...);
        // vo.setStock_type(...);
        // vo.setStock_type_specs(...);
        // vo.setStock_currency(...);
        // 등등

        System.out.println("===== Mapped StockInfoVo (SearchInfo) =====");
        System.out.println("stock_code    = " + vo.getStock_code());
        System.out.println("stock_ko_name = " + vo.getStock_ko_name());
        System.out.println("stock_en_name = " + vo.getStock_en_name());

        return vo;
    }

    /**
     * 주식기본조회(search-stock-info)를 KIS OpenAPI로 호출하는 메서드.
     *
     * InquireDailyPriceApi / getDailyPrices 와 동일한 패턴:
     *  - KisClientFactory 에서 KisClient 확보
     *  - SearchStockInfoApi 생성 및 파라미터 세팅
     *  - client.execute(api) 로 호출
     *  - 응답 검증 후 StockInfoVo 로 매핑
     */
    @Override
    public StockInfoVo getSearchStockInfo(HashMap<String, String> map) throws Exception {
    	
    	String pdno 		= map.get("in_stockCode"); 
    	String prdtTypeCd 	= map.get("prdtTypeCd"); 
        if (pdno == null || pdno.trim().isEmpty()) {
            throw new IllegalArgumentException("pdno(상품번호)는 필수입니다.");
        }

        String trimmedPdno = pdno.trim();

        String effectivePrdtTypeCd;
        if (prdtTypeCd == null || prdtTypeCd.trim().isEmpty()) {
            effectivePrdtTypeCd = "300";  // 국내 주식 기본값
        } else {
            effectivePrdtTypeCd = prdtTypeCd.trim();
        }

        KisClient client = KisClientFactory.getClient();

        SearchStockInfoApi api = new SearchStockInfoApi();
        api.setPdno(trimmedPdno);
        api.setPrdtTypeCd(effectivePrdtTypeCd);

        SearchStockInfoResult result = client.execute(api);

        if (result == null) {
            logger.error("KIS 주식기본조회 응답이 null 입니다. pdno={}, prdtTypeCd={}", trimmedPdno, effectivePrdtTypeCd);
            throw new IllegalStateException("KIS 주식기본조회 응답이 null 입니다.");
        }

        System.out.println("===== SearchStockInfoResult =====");
        System.out.println("rt_cd  = " + result.getRtCd());
        System.out.println("msg_cd = " + result.getMsgCd());
        System.out.println("msg1   = " + result.getMsg1());

        if (!"0".equals(result.getRtCd())) {
            String msg = "KIS 주식기본조회 API 오류 rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd()
                    + ", msg1=" + result.getMsg1();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        
        SearchStockInfoResult.Output output = result.getOutput();
        if (output == null) {
            logger.warn("KIS 주식기본조회 output 이 null 입니다. pdno={}, prdtTypeCd={}", trimmedPdno, effectivePrdtTypeCd);
            return null;
        }
        
        //DB에 저장되어있는 정보 가져오기
        StockInfoVo vo = new StockInfoVo();
        vo = getSqlSession().selectOne(NS+"searchStockOne", map);
    	
        // 필요 시 추가 매핑
        System.out.println("===== Mapped StockInfoVo (SearchStockInfo) =====");
        System.out.println("stock_code    = " + vo.getStock_code());
        System.out.println("stock_ko_name = " + vo.getStock_ko_name());
        System.out.println("stock_en_name = " + vo.getStock_en_name());
        System.out.println("stock_market  = " + vo.getStock_market());

        return vo;
    }
    
    public List<StockDataVo> getInquireDailyItemchartprice(HashMap<String, String> map) throws Exception {

        String stockCode      = map.get("in_stockCode");
        String fromDate       = map.get("in_fromDate");
        String toDate         = map.get("in_toDate");
        String periodDivCode  = map.get("in_periodDivCode");
        String orgAdjPrc      = map.get("in_orgAdjPrc");

        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("stockCode is required");
        }
        if (fromDate == null || fromDate.trim().isEmpty()) {
            throw new IllegalArgumentException("fromDate is required (YYYYMMDD)");
        }
        if (toDate == null || toDate.trim().isEmpty()) {
            throw new IllegalArgumentException("toDate is required (YYYYMMDD)");
        }

        String trimmedCode = stockCode.trim();
        String start = fromDate.trim();
        String end = toDate.trim();
        String period = (periodDivCode == null || periodDivCode.trim().isEmpty())
                ? "D" : periodDivCode.trim();
        period = period.toUpperCase();
        Integer minuteInterval = parseMinuteInterval(period);
        String adjPrc = (orgAdjPrc == null || orgAdjPrc.trim().isEmpty())
                ? "0" : orgAdjPrc.trim();

        java.time.LocalDate startDate = java.time.LocalDate.parse(start, DATE_YYYYMMDD);
        java.time.LocalDate endDate = java.time.LocalDate.parse(end, DATE_YYYYMMDD);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("toDate must be after or equal to fromDate");
        }

        String stockMarket = safeString(map.get("in_stockMarket"));
        if (stockMarket.isEmpty()) {
            stockMarket = safeString(map.get("stock_market"));
        }
        if (stockMarket.isEmpty()) {
            stockMarket = safeString(map.get("market"));
        }
        if (stockMarket.isEmpty()) {
            stockMarket = safeString(map.get("market_section"));
        }

        String stockCountry = safeString(map.get("in_stockCountryCode"));
        if (stockCountry.isEmpty()) {
            stockCountry = safeString(map.get("stock_country_code"));
        }
        if (stockCountry.isEmpty()) {
            stockCountry = safeString(map.get("country"));
        }

        if (stockMarket.isEmpty() || stockCountry.isEmpty()) {
            StockInfoVo info = lookupStockInfo(trimmedCode);
            if (info != null) {
                if (stockMarket.isEmpty()) {
                    stockMarket = safeString(info.getStock_market());
                }
                if (stockCountry.isEmpty()) {
                    stockCountry = safeString(info.getStock_country_code());
                }
            }
        }

        if (isOverseasStock(trimmedCode, stockMarket, stockCountry)) {
            if (isFxLike(trimmedCode)) {
                return getOverseasFxDailyChartData(trimmedCode, startDate, endDate, period);
            }
            if (minuteInterval != null) {
                return getOverseasTimeItemchartpriceNMin(trimmedCode, stockMarket, stockCountry, startDate, endDate, minuteInterval);
            }
            if (isIndexLike(trimmedCode, stockMarket)) {
                return getOverseasIndexDailyChartData(trimmedCode, startDate, endDate, period);
            }
            return getOverseasDailyPriceData(trimmedCode, stockMarket, stockCountry, startDate, endDate, period);
        }

        if (isDomesticIndexCode(trimmedCode)) {
            String indexPeriod = (minuteInterval != null) ? "D" : period;
            List<StockDataVo> indexList = null;
            try {
                indexList = getDomesticIndexChartData(trimmedCode, startDate, endDate, indexPeriod);
            } catch (Exception ignore) {
                indexList = null;
            }
            if (indexList != null && !indexList.isEmpty()) {
                return indexList;
            }
            return getDomesticIndexDailyPriceData(trimmedCode, startDate, endDate, indexPeriod);
        }

        if (minuteInterval != null) {
            return getInquireTimeDailychartpriceNMin(trimmedCode, startDate, endDate, minuteInterval);
        }

        KisClient client = KisClientFactory.getClient();

        // date(epochMillis)를 key로 한 통합 Map (중복 제거용)
        Map<Long, StockDataVo> merged = new LinkedHashMap<Long, StockDataVo>();

        java.time.LocalDate currentEnd = endDate;
        int loopCount = 0;

        // KIS가 한 번에 최대 100개만 내려주므로
        // currentEnd를 점점 과거로 옮기면서 여러 번 호출
        while (!currentEnd.isBefore(startDate)) {

            String currentEndStr = currentEnd.format(DATE_YYYYMMDD);

            InquireDailyItemchartpriceApi api = new InquireDailyItemchartpriceApi();
            api.setFidInputIscd(trimmedCode);
            api.setFidInputDate1(start);          // 전체 조회 시작일은 고정
            api.setFidInputDate2(currentEndStr);  // 끝 날짜만 점점 과거로 이동
            api.setFidPeriodDivCode(period);
            api.setFidOrgAdjPrc(adjPrc);
            // fidCondMrktDivCode 는 기본 "UN" 그대로 사용

//            System.out.println("===== InquireDailyItemchartprice Request =====");
//            System.out.println("LOOP_INDEX             = " + loopCount);
//            System.out.println("FID_COND_MRKT_DIV_CODE = " + api.getFidCondMrktDivCode());
//            System.out.println("FID_INPUT_ISCD         = " + api.getFidInputIscd());
//            System.out.println("FID_INPUT_DATE_1       = " + api.getFidInputDate1());
//            System.out.println("FID_INPUT_DATE_2       = " + api.getFidInputDate2());
//            System.out.println("FID_PERIOD_DIV_CODE    = " + api.getFidPeriodDivCode());
//            System.out.println("FID_ORG_ADJ_PRC        = " + api.getFidOrgAdjPrc());
//            System.out.println("TR_ID                  = " + api.getTrId());

            InquireDailyItemchartpriceResult result = client.execute(api);

            if (result == null) {
                System.out.println("KIS 기간별시세 응답이 null 입니다.");
                throw new IllegalStateException("KIS 기간별시세 응답이 null 입니다.");
            }

//            System.out.println("===== InquireDailyItemchartprice Result =====");
//            System.out.println("rt_cd  = " + result.getRtCd());
//            System.out.println("msg_cd = " + result.getMsgCd());
//            System.out.println("msg1   = " + result.getMsg1());

            if (!"0".equals(result.getRtCd())) {
                String msg = "KIS 기간별시세 API 오류 rtCd=" + result.getRtCd()
                        + ", msgCd=" + result.getMsgCd()
                        + ", msg1=" + result.getMsg1();
                throw new IllegalStateException(msg);
            }

            Output2[] outputs = result.getOutput2();
            if (outputs == null || outputs.length == 0) {
                System.out.println("InquireDailyItemchartprice outputs is empty. stop loop.");
                break;
            }

            LocalDate oldestDateInBatch = null;

            for (Output2 o : outputs) {
                if (o == null) {
                    continue;
                }

                String dateStr  = o.getStckBsopDate();   // yyyyMMdd
                String openStr  = o.getStckOprc();
                String highStr  = o.getStckHgpr();
                String lowStr   = o.getStckLwpr();
                String closeStr = o.getStckClpr();
                String volStr   = o.getAcmlVol();
                if (dateStr == null || dateStr.length() != 8) {
                    continue;
                }

                LocalDate date = LocalDate.parse(dateStr, DATE_YYYYMMDD);

                // 사용자가 요청한 [fromDate, toDate] 범위만 포함
                if (date.isBefore(startDate) || date.isAfter(endDate)) {
                    continue;
                }

                long epochMillis = date.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();

                double open   = StringUtil.parseDoubleSafe(openStr);
                double high   = StringUtil.parseDoubleSafe(highStr);
                double low    = StringUtil.parseDoubleSafe(lowStr);
                double close  = StringUtil.parseDoubleSafe(closeStr);
                double volume = StringUtil.parseDoubleSafe(volStr);

                StockDataVo vo = new StockDataVo(epochMillis, open, high, low, close, volume);
                // 같은 날짜가 여러 번 내려와도 마지막 값으로 덮어씀
                merged.put(epochMillis, vo);

                if (oldestDateInBatch == null || date.isBefore(oldestDateInBatch)) {
                    oldestDateInBatch = date;
                }
            }

            // 이번 호출에서 유효한 날짜가 하나도 없으면 종료
            if (oldestDateInBatch == null) {
                break;
            }

            // 이미 fromDate까지 다 채웠으면 종료
            if (!oldestDateInBatch.isAfter(startDate)) {
                break;
            }

            // 다음 루프에서는 더 과거 구간을 조회하도록 end를 하루 줄임
            currentEnd = oldestDateInBatch.minusDays(1);

            loopCount++;
            // 안전장치 (비정상 루프 방지)
            if (loopCount > 50) {
                System.out.println("InquireDailyItemchartprice loopCount over 50. stop loop (safety).");
                break;
            }
        }

        java.util.List<StockDataVo> list = new java.util.ArrayList<StockDataVo>(merged.values());

        java.util.Collections.sort(list, new java.util.Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        System.out.println("InquireDailyItemchartprice merged list size = " + list.size());
        return list;
    }

    private static final int MAX_INTRADAY_DAYS = 365;
    private static final int MAX_OVERSEAS_INTRADAY_DAYS = 30;

    private List<StockDataVo> getInquireTimeDailychartpriceNMin(String stockCode, LocalDate startDate, LocalDate endDate, int minuteInterval) throws Exception {
        KisClient client = KisClientFactory.getClient();
        List<StockDataVo> all = new ArrayList<StockDataVo>();

        LocalDate current = endDate;
        int days = 0;
        while (!current.isBefore(startDate) && days < MAX_INTRADAY_DAYS) {
            List<StockDataVo> oneDay = fetchNMinCandlesOneDay(client, stockCode, current, minuteInterval);
            if (oneDay != null && !oneDay.isEmpty()) {
                all.addAll(oneDay);
            }
            current = current.minusDays(1);
            days++;
        }

        Collections.sort(all, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        return all;
    }

    private static final int MAX_INTRADAY_PAGES = 60;

    private List<StockDataVo> fetchNMinCandlesOneDay(KisClient client, String stockCode, LocalDate date, int minuteInterval) throws Exception {
        String dateStr = date.format(DATE_YYYYMMDD);
        Map<Long, StockDataVo> minuteMap = new LinkedHashMap<Long, StockDataVo>();

        LocalTime cursor = LocalTime.of(15, 30);
        LocalTime earliestSeen = null;
        int safety = 0;

        while (safety++ < MAX_INTRADAY_PAGES) {
            InquireTimeDailychartpriceApi api = new InquireTimeDailychartpriceApi();
            api.setFidInputIscd(stockCode);
            api.setFidInputDate1(dateStr);
            api.setFidInputHour1(formatTimeForKis(cursor));
            api.setFidPwDataIncuYn("Y");
            api.setFidFakeTickIncuYn("N");

            InquireTimeDailychartpriceResult result = client.execute(api);
            if (result == null || !"0".equals(result.getRtCd())) {
                break;
            }

            InquireTimeDailychartpriceResult.Output2[] arr = result.getOutput2();
            if (arr == null || arr.length == 0) {
                break;
            }

            LocalTime batchEarliest = null;
            for (InquireTimeDailychartpriceResult.Output2 o : arr) {
                if (o == null) continue;

                String bsop = StringUtil.nvl(o.getStckBsopDate(), "");
                String hhmmss = StringUtil.nvl(o.getStckCntgHour(), "");
                if (bsop.length() != 8 || hhmmss.length() < 4) continue;

                LocalDate d = parseDateSafe(bsop);
                LocalTime t = parseTimeSafe(hhmmss);
                if (d == null || t == null) continue;

                if (batchEarliest == null || t.isBefore(batchEarliest)) {
                    batchEarliest = t;
                }

                long epoch = ZonedDateTime.of(d, t, KOREA_ZONE).toInstant().toEpochMilli();

                double open = StringUtil.parseDoubleSafe(o.getStckOprc());
                double high = StringUtil.parseDoubleSafe(o.getStckHgpr());
                double low = StringUtil.parseDoubleSafe(o.getStckLwpr());
                double close = StringUtil.parseDoubleSafe(o.getStckPrpr());
                double vol = StringUtil.parseDoubleSafe(o.getCntgVol());

                minuteMap.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));
            }

            if (batchEarliest == null) {
                break;
            }
            if (earliestSeen != null && !batchEarliest.isBefore(earliestSeen)) {
                break; // no progress
            }
            earliestSeen = batchEarliest;

            LocalTime nextCursor = batchEarliest.minusMinutes(1);
            if (nextCursor.equals(cursor) || nextCursor.isAfter(cursor)) {
                break;
            }
            cursor = nextCursor;
        }

        if (minuteMap.isEmpty()) {
            return new ArrayList<StockDataVo>();
        }

        return aggregateToNMin(new ArrayList<StockDataVo>(minuteMap.values()), minuteInterval);
    }

    private String formatTimeForKis(LocalTime time) {
        int hh = (time != null) ? time.getHour() : 0;
        int mm = (time != null) ? time.getMinute() : 0;
        return String.format("%02d%02d00", hh, mm);
    }

    private List<StockDataVo> aggregateToNMin(List<StockDataVo> mins, int minuteInterval) {
        if (mins == null || mins.isEmpty()) {
            return new ArrayList<StockDataVo>();
        }

        int bucketMinutes = minuteInterval;
        if (bucketMinutes <= 0) {
            bucketMinutes = 30;
        }

        Collections.sort(mins, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        class CandleAgg {
            long epoch;
            double open;
            double high;
            double low;
            double close;
            double volume;
        }

        Map<Long, CandleAgg> buckets = new LinkedHashMap<Long, CandleAgg>();

        for (StockDataVo m : mins) {
            ZonedDateTime z = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(m.getDate()), KOREA_ZONE);
            int totalMin = z.getHour() * 60 + z.getMinute();
            int bucketMin = (totalMin / bucketMinutes) * bucketMinutes;
            int bh = bucketMin / 60;
            int bm = bucketMin % 60;

            ZonedDateTime bucketTime = ZonedDateTime.of(z.toLocalDate(), LocalTime.of(bh, bm, 0), KOREA_ZONE);
            long bucketEpoch = bucketTime.toInstant().toEpochMilli();

            CandleAgg agg = buckets.get(bucketEpoch);
            if (agg == null) {
                agg = new CandleAgg();
                agg.epoch = bucketEpoch;
                agg.open = m.getOpen();
                agg.high = m.getHigh();
                agg.low = m.getLow();
                agg.close = m.getClose();
                agg.volume = m.getVolume();
                buckets.put(bucketEpoch, agg);
            } else {
                agg.high = Math.max(agg.high, m.getHigh());
                agg.low = Math.min(agg.low, m.getLow());
                agg.close = m.getClose();
                agg.volume = agg.volume + m.getVolume();
            }
        }

        List<StockDataVo> out = new ArrayList<StockDataVo>();
        for (CandleAgg a : buckets.values()) {
            out.add(new StockDataVo(a.epoch, a.open, a.high, a.low, a.close, a.volume));
        }

        Collections.sort(out, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });

        return out;
    }

    private List<StockDataVo> getOverseasDailyPriceData(String stockCode, String market, String country,
            LocalDate startDate, LocalDate endDate, String period) throws Exception {
        KisClient client = KisClientFactory.getClient();

        String excd = normalizeOverseasExcd(market, country);
        if (excd == null || excd.isEmpty()) {
            excd = "NAS";
        }

        Map<Long, StockDataVo> merged = new LinkedHashMap<Long, StockDataVo>();
        LocalDate currentEnd = endDate;
        String keyb = "";
        int loop = 0;

        while (!currentEnd.isBefore(startDate) && loop < 60) {
            InquireOverseasDailyPriceApi api = new InquireOverseasDailyPriceApi();
            api.setExcd(excd);
            api.setSymb(stockCode);
            api.setGubn(normalizeOverseasPeriodToGubn(period));
            api.setBymd(currentEnd.format(DATE_YYYYMMDD));
            api.setModp("0");
            api.setAuth("");
            api.setKeyb(keyb);

            InquireOverseasDailyPriceResult result = client.execute(api);
            if (result == null) {
                throw new IllegalStateException("KIS overseas dailyprice response is null.");
            }
            if (!"0".equals(result.getRtCd())) {
                String msg = "KIS overseas dailyprice error rtCd=" + result.getRtCd()
                        + ", msgCd=" + result.getMsgCd()
                        + ", msg1=" + result.getMsg1();
                throw new IllegalStateException(msg);
            }

            InquireOverseasDailyPriceResult.Output2[] outputs = result.getOutput2();
            if (outputs == null || outputs.length == 0) {
                break;
            }

            LocalDate oldestDateInBatch = null;
            for (InquireOverseasDailyPriceResult.Output2 o : outputs) {
                if (o == null) {
                    continue;
                }
                String dateStr = safeString(o.getXymd());
                if (dateStr.length() != 8) {
                    continue;
                }
                LocalDate date = LocalDate.parse(dateStr, DATE_YYYYMMDD);
                if (date.isBefore(startDate) || date.isAfter(endDate)) {
                    continue;
                }

                long epoch = date.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();
                double open = StringUtil.parseDoubleSafe(o.getOpen());
                double high = StringUtil.parseDoubleSafe(o.getHigh());
                double low = StringUtil.parseDoubleSafe(o.getLow());
                double close = StringUtil.parseDoubleSafe(o.getClos());
                double vol = StringUtil.parseDoubleSafe(o.getTvol());

                if (Double.isNaN(open) || Double.isNaN(high) || Double.isNaN(low) || Double.isNaN(close)) {
                    continue;
                }

                merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));

                if (oldestDateInBatch == null || date.isBefore(oldestDateInBatch)) {
                    oldestDateInBatch = date;
                }
            }

            if (oldestDateInBatch == null) {
                break;
            }
            if (!oldestDateInBatch.isAfter(startDate)) {
                break;
            }

            String nextKeyb = "";
            if (result.getOutput1() != null) {
                nextKeyb = safeString(result.getOutput1().getKeyb());
            }

            if (!nextKeyb.isEmpty() && !nextKeyb.equals(keyb)) {
                keyb = nextKeyb;
            } else {
                keyb = "";
                currentEnd = oldestDateInBatch.minusDays(1);
            }

            loop++;
        }

        List<StockDataVo> list = new ArrayList<StockDataVo>(merged.values());
        Collections.sort(list, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });
        return list;
    }

    private List<StockDataVo> getOverseasIndexDailyChartData(String stockCode, LocalDate startDate, LocalDate endDate,
            String period) throws Exception {
        return getOverseasDailyChartDataByMarketDiv("N", stockCode, startDate, endDate, period);
    }

    private List<StockDataVo> getOverseasFxDailyChartData(String stockCode, LocalDate startDate, LocalDate endDate,
            String period) throws Exception {
        String code = normalizeFxCode(stockCode);
        if (code.isEmpty()) {
            code = safeString(stockCode);
        }
        return getOverseasDailyChartDataByMarketDiv("X", code, startDate, endDate, period);
    }

    private List<StockDataVo> getOverseasDailyChartDataByMarketDiv(String marketDiv, String stockCode,
            LocalDate startDate, LocalDate endDate, String period) throws Exception {
        KisClient client = KisClientFactory.getClient();

        InquireOverseasDailyChartPriceApi api = new InquireOverseasDailyChartPriceApi();
        api.setFidCondMrktDivCode(marketDiv);
        api.setFidInputIscd(stockCode);
        api.setFidInputDate1(startDate.format(DATE_YYYYMMDD));
        api.setFidInputDate2(endDate.format(DATE_YYYYMMDD));
        api.setFidPeriodDivCode(normalizeOverseasIndexPeriod(period));

        InquireOverseasDailyChartPriceResult result = client.execute(api);
        if (result == null) {
            throw new IllegalStateException("KIS overseas chart response is null.");
        }
        if (!"0".equals(result.getRtCd())) {
            String msg = "KIS overseas chart error rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd()
                    + ", msg1=" + result.getMsg1();
            throw new IllegalStateException(msg);
        }

        java.util.List<InquireOverseasDailyChartPriceResult.Output2> outputs = result.getOutput2();
        if (outputs == null || outputs.isEmpty()) {
            return new ArrayList<StockDataVo>();
        }

        Map<Long, StockDataVo> merged = new LinkedHashMap<Long, StockDataVo>();
        for (InquireOverseasDailyChartPriceResult.Output2 o : outputs) {
            if (o == null) {
                continue;
            }

            String dateStr = pickOverseasChartField(o, "stck_bsop_date", "bsop_date", "xymd", "XYMD", "date", "DATE");
            dateStr = safeString(dateStr);
            if (dateStr.length() != 8) {
                continue;
            }
            LocalDate date = LocalDate.parse(dateStr, DATE_YYYYMMDD);
            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                continue;
            }

            long epoch = date.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();

            double open = StringUtil.parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_oprc", "open", "OPEN"));
            double high = StringUtil.parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_hgpr", "high", "HIGH"));
            double low = StringUtil.parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_lwpr", "low", "LOW"));
            double close = StringUtil.parseDoubleSafe(pickOverseasChartField(o, "ovrs_nmix_prpr", "clos", "CLOS", "close", "CLOSE"));
            double vol = StringUtil.parseDoubleSafe(pickOverseasChartField(o, "acml_vol", "tvol", "TVOL", "volume", "VOLUME"));

            if (Double.isNaN(open) || Double.isNaN(high) || Double.isNaN(low) || Double.isNaN(close)) {
                continue;
            }

            merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));
        }

        List<StockDataVo> list = new ArrayList<StockDataVo>(merged.values());
        Collections.sort(list, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });
        return list;
    }

    private List<StockDataVo> getDomesticIndexChartData(String stockCode, LocalDate startDate, LocalDate endDate,
            String period) throws Exception {
        KisClient client = KisClientFactory.getClient();
        Map<Long, StockDataVo> merged = new LinkedHashMap<Long, StockDataVo>();
        LocalDate currentEnd = endDate;
        int loopCount = 0;

        while (!currentEnd.isBefore(startDate)) {
            InquireDailyIndexchartpriceResult result = fetchDomesticIndexChartWithFallback(client, stockCode, startDate,
                    currentEnd, period);
            if (result == null || !"0".equals(result.getRtCd())) {
                String msg = (result == null) ? "KIS indexchart result is null."
                        : ("KIS indexchart error rtCd=" + result.getRtCd()
                        + ", msgCd=" + result.getMsgCd()
                        + ", msg1=" + result.getMsg1());
                if (merged.isEmpty()) {
                    throw new IllegalStateException(msg);
                }
                break;
            }

            List<InquireDailyIndexchartpriceResult.Output2> outputs = result.getOutput2();
            if (outputs == null || outputs.isEmpty()) {
                break;
            }

            LocalDate oldestDateInBatch = null;
            for (InquireDailyIndexchartpriceResult.Output2 o : outputs) {
                if (o == null) {
                    continue;
                }
                String dateStr = pickIndexField(o, "stck_bsop_date", "bsop_date", "date", "xymd");
                if (dateStr.length() != 8) {
                    continue;
                }
                LocalDate date = LocalDate.parse(dateStr, DATE_YYYYMMDD);
                if (date.isBefore(startDate) || date.isAfter(endDate)) {
                    continue;
                }

                long epoch = date.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();
                double close = StringUtil.parseDoubleSafe(pickIndexField(o, "bstp_nmix_prpr", "close", "clpr", "prpr"));
                double open = StringUtil.parseDoubleSafe(pickIndexField(o, "bstp_nmix_oprc", "open", "oprc"));
                double high = StringUtil.parseDoubleSafe(pickIndexField(o, "bstp_nmix_hgpr", "high", "hgpr"));
                double low = StringUtil.parseDoubleSafe(pickIndexField(o, "bstp_nmix_lwpr", "low", "lwpr"));
                double vol = StringUtil.parseDoubleSafe(pickIndexField(o, "acml_vol", "tvol", "volume", "vol"));

                if (Double.isNaN(close)) {
                    continue;
                }
                if (Double.isNaN(open)) open = close;
                if (Double.isNaN(high)) high = close;
                if (Double.isNaN(low)) low = close;

                merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));

                if (oldestDateInBatch == null || date.isBefore(oldestDateInBatch)) {
                    oldestDateInBatch = date;
                }
            }

            if (oldestDateInBatch == null) {
                break;
            }
            if (!oldestDateInBatch.isAfter(startDate)) {
                break;
            }

            currentEnd = oldestDateInBatch.minusDays(1);
            loopCount++;
            if (loopCount > 60) {
                break;
            }
        }

        List<StockDataVo> list = new ArrayList<StockDataVo>(merged.values());
        Collections.sort(list, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });
        return list;
    }

    private InquireDailyIndexchartpriceResult fetchDomesticIndexChartWithFallback(KisClient client, String stockCode,
            LocalDate startDate, LocalDate endDate, String period) {
        String p = normalizeOverseasIndexPeriod(period);

        InquireDailyIndexchartpriceResult r1 = fetchDomesticIndexChart(client, stockCode, startDate, endDate, p, "U");
        logIndexChartAttempt(stockCode, p, "U", r1);
        if (r1 != null && "0".equals(r1.getRtCd())) {
            return r1;
        }

        InquireDailyIndexchartpriceResult r2 = fetchDomesticIndexChart(client, stockCode, startDate, endDate, p, "UN");
        logIndexChartAttempt(stockCode, p, "UN", r2);
        if (r2 != null && "0".equals(r2.getRtCd())) {
            return r2;
        }

        InquireDailyIndexchartpriceResult r3 = fetchDomesticIndexChart(client, stockCode, startDate, endDate, p, "J");
        logIndexChartAttempt(stockCode, p, "J", r3);
        return r3;
    }

    private String pickOverseasChartField(InquireOverseasDailyChartPriceResult.Output2 o, String... keys) {
        if (o == null || keys == null) return "";
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            if (k == null) continue;
            Object v = o.getField(k);
            if (v == null) continue;
            String s = String.valueOf(v).trim();
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s;
        }
        return "";
    }

    private InquireDailyIndexchartpriceResult fetchDomesticIndexChart(KisClient client, String stockCode,
            LocalDate startDate, LocalDate endDate, String period, String marketDiv) {
        try {
            InquireDailyIndexchartpriceApi api = new InquireDailyIndexchartpriceApi();
            api.setFidCondMrktDivCode(marketDiv);
            api.setFidInputIscd(stockCode);
            api.setFidInputDate1(startDate.format(DATE_YYYYMMDD));
            api.setFidInputDate2(endDate.format(DATE_YYYYMMDD));
            api.setFidPeriodDivCode(period);
            return client.execute(api);
        } catch (Exception e) {
            return null;
        }
    }

    private String pickIndexField(InquireDailyIndexchartpriceResult.Output2 o, String... keys) {
        if (o == null || keys == null) return "";
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            if (k == null || k.isEmpty()) continue;
            Object v = o.getField(k);
            if (v == null) continue;
            String s = String.valueOf(v).trim();
            if (!s.isEmpty()) return s;
        }
        return "";
    }

    private void logIndexChartAttempt(String stockCode, String period, String marketDiv,
            InquireDailyIndexchartpriceResult res) {
        if (logger == null) return;
        if (res == null) {
            logger.warn("[KIS-INDEX-CHART] code={} period={} marketDiv={} result=null", stockCode, period, marketDiv);
            return;
        }
        int cnt = 0;
        try {
            List<InquireDailyIndexchartpriceResult.Output2> out = res.getOutput2();
            cnt = (out == null) ? 0 : out.size();
        } catch (Exception ignore) {
            cnt = -1;
        }
        logger.info("[KIS-INDEX-CHART] code={} period={} marketDiv={} rtCd={} msgCd={} msg1={} outputs={}",
                stockCode, period, marketDiv, res.getRtCd(), res.getMsgCd(), res.getMsg1(), cnt);
    }

    private List<StockDataVo> getDomesticIndexDailyPriceData(String stockCode, LocalDate startDate, LocalDate endDate,
            String period) throws Exception {
        KisClient client = KisClientFactory.getClient();

        InquireDailyPriceResult result = fetchDomesticDailyPriceWithFallback(client, stockCode, period);
        if (result == null || !"0".equals(result.getRtCd())) {
            String msg = (result == null) ? "KIS daily price result is null."
                    : ("KIS daily price error rtCd=" + result.getRtCd()
                    + ", msgCd=" + result.getMsgCd()
                    + ", msg1=" + result.getMsg1());
            throw new IllegalStateException(msg);
        }

        InquireDailyPriceResult.Output1[] outputs = result.getOutput1();
        if (outputs == null || outputs.length == 0) {
            return new ArrayList<StockDataVo>();
        }

        Map<Long, StockDataVo> merged = new LinkedHashMap<Long, StockDataVo>();
        for (InquireDailyPriceResult.Output1 o : outputs) {
            if (o == null) {
                continue;
            }
            String dateStr = safeString(o.getStckBsopDate());
            if (dateStr.length() != 8) {
                continue;
            }
            LocalDate date = LocalDate.parse(dateStr, DATE_YYYYMMDD);
            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                continue;
            }

            long epoch = date.atStartOfDay(KOREA_ZONE).toInstant().toEpochMilli();
            double open = StringUtil.parseDoubleSafe(o.getStckOprc());
            double high = StringUtil.parseDoubleSafe(o.getStckHgpr());
            double low = StringUtil.parseDoubleSafe(o.getStckLwpr());
            double close = StringUtil.parseDoubleSafe(o.getStckClpr());
            double vol = StringUtil.parseDoubleSafe(o.getAcmlVol());

            if (Double.isNaN(open) || Double.isNaN(high) || Double.isNaN(low) || Double.isNaN(close)) {
                continue;
            }

            merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));
        }

        List<StockDataVo> list = new ArrayList<StockDataVo>(merged.values());
        Collections.sort(list, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });
        return list;
    }

    private InquireDailyPriceResult fetchDomesticDailyPriceWithFallback(KisClient client, String stockCode,
            String period) {
        String p = safeString(period).toUpperCase();
        if (p.isEmpty()) {
            p = "D";
        }

        // 1) try index-like market code "U"
        InquireDailyPriceResult r1 = fetchDomesticDailyPrice(client, stockCode, p, "U");
        logIndexDailyAttempt(stockCode, p, "U", r1);
        if (r1 != null && "0".equals(r1.getRtCd())) {
            return r1;
        }

        // 2) fallback to default "UN"
        InquireDailyPriceResult r2 = fetchDomesticDailyPrice(client, stockCode, p, "UN");
        logIndexDailyAttempt(stockCode, p, "UN", r2);
        if (r2 != null && "0".equals(r2.getRtCd())) {
            return r2;
        }

        // 3) fallback to "J"
        InquireDailyPriceResult r3 = fetchDomesticDailyPrice(client, stockCode, p, "J");
        logIndexDailyAttempt(stockCode, p, "J", r3);
        return r3;
    }

    private InquireDailyPriceResult fetchDomesticDailyPrice(KisClient client, String stockCode, String period,
            String marketDiv) {
        try {
            InquireDailyPriceApi api = new InquireDailyPriceApi();
            api.setFidInputIscd(stockCode);
            api.setFidPeriodDivCode(period);
            if (marketDiv != null && !marketDiv.isEmpty()) {
                api.setFidCondMrktDivCode(marketDiv);
            }
            return client.execute(api);
        } catch (Exception e) {
            return null;
        }
    }

    private void logIndexDailyAttempt(String stockCode, String period, String marketDiv, InquireDailyPriceResult res) {
        if (logger == null) return;
        if (res == null) {
            logger.warn("[KIS-INDEX-DAILY] code={} period={} marketDiv={} result=null", stockCode, period, marketDiv);
            return;
        }
        int cnt = 0;
        try {
            InquireDailyPriceResult.Output1[] out = res.getOutput1();
            cnt = (out == null) ? 0 : out.length;
        } catch (Exception ignore) {
            cnt = -1;
        }
        logger.info("[KIS-INDEX-DAILY] code={} period={} marketDiv={} rtCd={} msgCd={} msg1={} outputs={}",
                stockCode, period, marketDiv, res.getRtCd(), res.getMsgCd(), res.getMsg1(), cnt);
    }

    private List<StockDataVo> getOverseasTimeItemchartpriceNMin(String stockCode, String market, String country,
            LocalDate startDate, LocalDate endDate, int minuteInterval) throws Exception {
        KisClient client = KisClientFactory.getClient();

        String excd = normalizeOverseasExcd(market, country);
        if (excd == null || excd.isEmpty()) {
            excd = "NAS";
        }

        Map<Long, StockDataVo> merged = new LinkedHashMap<Long, StockDataVo>();

        String next = "";
        String keyb = "";
        LocalDate earliest = null;
        int safety = 0;

        while (safety++ < 60) {
            InquireOverseasTimeItemchartpriceApi api = new InquireOverseasTimeItemchartpriceApi();
            api.setAuth("");
            api.setExcd(excd);
            api.setSymb(stockCode);
            int nmin = (minuteInterval <= 0) ? 30 : minuteInterval;
            api.setNmin(Integer.toString(nmin));
            api.setPinc("1");
            api.setNext(next);
            api.setNrec("120");
            api.setFill("");
            api.setKeyb(keyb);

            InquireOverseasTimeItemchartpriceResult result = client.execute(api);
            if (result == null) {
                break;
            }
            if (!"0".equals(result.getRtCd())) {
                String msg = "KIS overseas time chart error rtCd=" + result.getRtCd()
                        + ", msgCd=" + result.getMsgCd()
                        + ", msg1=" + result.getMsg1();
                throw new IllegalStateException(msg);
            }

            InquireOverseasTimeItemchartpriceResult.Output2[] outputs = result.getOutput2();
            if (outputs == null || outputs.length == 0) {
                break;
            }

            LocalDate batchEarliest = null;
            for (InquireOverseasTimeItemchartpriceResult.Output2 o : outputs) {
                if (o == null) {
                    continue;
                }
                String kymd = safeString(o.getKymd());
                String khms = safeString(o.getKhms());
                if (kymd.isEmpty()) {
                    kymd = safeString(o.getXymd());
                }
                if (khms.isEmpty()) {
                    khms = safeString(o.getXhms());
                }
                if (kymd.length() != 8 || khms.length() < 4) {
                    continue;
                }

                LocalDate date = parseDateSafe(kymd);
                if (date == null) {
                    continue;
                }
                if (date.isBefore(startDate) || date.isAfter(endDate)) {
                    continue;
                }

                LocalTime time = parseTimeSafe(khms);
                if (time == null) {
                    continue;
                }

                long epoch = ZonedDateTime.of(date, time, KOREA_ZONE).toInstant().toEpochMilli();
                double open = StringUtil.parseDoubleSafe(o.getOpen());
                double high = StringUtil.parseDoubleSafe(o.getHigh());
                double low = StringUtil.parseDoubleSafe(o.getLow());
                double close = StringUtil.parseDoubleSafe(o.getLast());
                double vol = StringUtil.parseDoubleSafe(o.getEvol());

                if (Double.isNaN(open) || Double.isNaN(high) || Double.isNaN(low) || Double.isNaN(close)) {
                    continue;
                }

                merged.put(epoch, new StockDataVo(epoch, open, high, low, close, vol));

                if (batchEarliest == null || date.isBefore(batchEarliest)) {
                    batchEarliest = date;
                }
            }

            if (batchEarliest == null) {
                break;
            }
            if (earliest == null || batchEarliest.isBefore(earliest)) {
                earliest = batchEarliest;
            }

            String nextKeyb = buildOverseasKeyb(outputs[outputs.length - 1]);
            if (nextKeyb == null || nextKeyb.isEmpty()) {
                break;
            }

            InquireOverseasTimeItemchartpriceResult.Output1 out1 = result.getOutput1();
            if (out1 != null) {
                String more = safeString(out1.getMore());
                String nextFlag = safeString(out1.getNext());
                if (!"1".equals(more) && !"1".equals(nextFlag)) {
                    break;
                }
            }

            if (earliest != null) {
                long days = ChronoUnit.DAYS.between(earliest, endDate) + 1;
                if (days >= MAX_OVERSEAS_INTRADAY_DAYS) {
                    break;
                }
                if (!earliest.isAfter(startDate)) {
                    break;
                }
            }

            keyb = nextKeyb;
            next = "1";
        }

        List<StockDataVo> list = new ArrayList<StockDataVo>(merged.values());
        Collections.sort(list, new Comparator<StockDataVo>() {
            @Override
            public int compare(StockDataVo o1, StockDataVo o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });
        return list;
    }

    private String buildOverseasKeyb(InquireOverseasTimeItemchartpriceResult.Output2 last) {
        if (last == null) {
            return "";
        }
        String ymd = safeString(last.getXymd());
        String hms = safeString(last.getXhms());
        if (ymd.isEmpty()) {
            ymd = safeString(last.getKymd());
        }
        if (hms.isEmpty()) {
            hms = safeString(last.getKhms());
        }
        if (ymd.length() != 8 || hms.length() < 4) {
            return "";
        }

        LocalDate date = parseDateSafe(ymd);
        LocalTime time = parseTimeSafe(hms);
        if (date == null || time == null) {
            return "";
        }

        LocalDateTime dt = LocalDateTime.of(date, time).minusMinutes(1);
        return dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private LocalDate parseDateSafe(String ymd) {
        try {
            if (ymd == null || ymd.trim().length() != 8) {
                return null;
            }
            return LocalDate.parse(ymd.trim(), DATE_YYYYMMDD);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTimeSafe(String hhmmss) {
        try {
            if (hhmmss == null) {
                return null;
            }
            String t = hhmmss.trim();
            if (t.length() == 4) {
                t = t + "00";
            }
            if (t.length() < 6) {
                return null;
            }
            int hh = parseIntSafe(t.substring(0, 2), -1);
            int mm = parseIntSafe(t.substring(2, 4), -1);
            int ss = parseIntSafe(t.substring(4, 6), 0);
            if (hh < 0 || mm < 0 || ss < 0) {
                return null;
            }
            return LocalTime.of(hh, mm, ss);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseMinuteInterval(String periodCode) {
        if (periodCode == null) {
            return null;
        }
        String p = periodCode.trim().toUpperCase();
        if (!p.startsWith("T")) {
            return null;
        }
        if (p.length() == 1) {
            return 30;
        }
        String num = p.substring(1);
        if (!num.matches("\\d+")) {
            return null;
        }
        int v = parseIntSafe(num, -1);
        if (v == 1 || v == 5 || v == 10 || v == 30) {
            return v;
        }
        return null;
    }

    private boolean isOverseasStock(String stockCode, String market, String country) {
        String c = safeString(country).toUpperCase();
        if (!c.isEmpty() && !c.equals("KR") && !c.equals("KOR")) {
            return true;
        }

        String m = safeString(market).toUpperCase();
        if (!m.isEmpty()) {
            if (m.contains("NASDAQ") || m.contains("NYSE") || m.contains("AMEX")
                    || m.equals("NAS") || m.equals("NYS") || m.equals("AMS")
                    || m.equals("HKS") || m.equals("SHS") || m.equals("SZS")
                    || m.equals("TSE") || m.equals("TYO") || m.equals("HSX")
                    || m.equals("HNX")) {
                return true;
            }
            if (m.contains("KOSPI") || m.contains("KOSDAQ") || m.contains("KRX")
                    || m.equals("KSE") || m.equals("KQ")) {
                return false;
            }
        }

        String code = safeString(stockCode);
        if (code.equalsIgnoreCase("KOSPI") || code.equalsIgnoreCase("KOSDAQ")) {
            return false;
        }
        if (!code.isEmpty() && !code.matches("\\\\d{6}")) {
            if (code.startsWith(".")) {
                return true;
            }
            if (code.matches("^[A-Za-z]{1,6}$")) {
                return true;
            }
        }

        return false;
    }

    private boolean isFxLike(String stockCode) {
        String code = normalizeFxCode(stockCode);
        return "USDKRW".equalsIgnoreCase(code);
    }

    private String normalizeFxCode(String stockCode) {
        String code = safeString(stockCode).toUpperCase();
        if (code.endsWith("=X")) {
            code = code.substring(0, code.length() - 2);
        }
        code = code.replace("/", "");
        return code;
    }

    private boolean isDomesticIndexCode(String stockCode) {
        String code = safeString(stockCode);
        return "0001".equals(code) || "2001".equals(code) || "1001".equals(code);
    }

    private boolean isIndexLike(String stockCode, String market) {
        String code = safeString(stockCode);
        if (code.startsWith(".")) {
            return true;
        }
        String m = safeString(market).toUpperCase();
        if (m.contains("INDEX") || m.contains("IDX")) {
            return true;
        }
        return false;
    }

    private String normalizeOverseasExcd(String market, String country) {
        String c = safeString(country).toUpperCase();
        String m = safeString(market).trim();
        if (m.isEmpty()) {
            return "US".equals(c) ? "NAS" : m;
        }

        String u = m.toUpperCase();
        if (u.equals("NAS") || u.equals("NYS") || u.equals("AMS") || u.equals("HKS") || u.equals("SHS")
                || u.equals("SZS") || u.equals("TSE") || u.equals("TYO") || u.equals("HSX") || u.equals("HNX")) {
            return u;
        }

        if ("US".equals(c)) {
            if (u.contains("NASDAQ") || u.contains("NAS")) {
                return "NAS";
            }
            if (u.contains("NYSE") || u.contains("NYS")) {
                return "NYS";
            }
            if (u.contains("AMEX") || u.contains("AMS")) {
                return "AMS";
            }
            return "NAS";
        }

        if ("HK".equals(c) || u.contains("HONG") || u.contains("HK")) {
            return "HKS";
        }

        if ("JP".equals(c) || u.contains("TOKYO") || u.contains("TSE") || u.contains("TYO")) {
            return "TSE";
        }

        if ("CN".equals(c) || u.contains("SHANG") || u.contains("SH")) {
            return "SHS";
        }
        if ("CN".equals(c) || u.contains("SHEN") || u.contains("SZ")) {
            return "SZS";
        }

        if ("VN".equals(c) || u.contains("HSX") || u.contains("HOCH")) {
            return "HSX";
        }
        if ("VN".equals(c) || u.contains("HNX") || u.contains("HANOI")) {
            return "HNX";
        }

        return u;
    }

    private String normalizeOverseasPeriodToGubn(String period) {
        String p = safeString(period).toUpperCase();
        if ("W".equals(p)) {
            return "1";
        }
        if ("M".equals(p) || "Y".equals(p)) {
            return "2";
        }
        return "0";
    }

    private String normalizeOverseasIndexPeriod(String period) {
        String p = safeString(period).toUpperCase();
        if ("D".equals(p) || "W".equals(p) || "M".equals(p) || "Y".equals(p)) {
            return p;
        }
        return "D";
    }

    private StockInfoVo lookupStockInfo(String stockCode) {
        try {
            if (stockCode == null || stockCode.trim().isEmpty()) {
                return null;
            }
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("in_stockCode", stockCode.trim());
            return (StockInfoVo) getSqlSession().selectOne(NS + "searchStockOne", params);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeString(String v) {
        return v == null ? "" : v.trim();
    }

    private int parseIntSafe(String s, int def) {
        try {
            if (s == null) return def;
            String t = s.trim();
            if (t.isEmpty()) return def;
            return Integer.parseInt(t);
        } catch (Exception e) {
            return def;
        }
    }
}
