package com.scheduler.stock.recpickdynamic.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 동적 추천 화면 필터 요청 VO.
 */
public class FilterRequestVo {

    /** "KR" | "US" */
    private String mktGroup;

    /** "ALL" | "KOSPI" | "KOSDAQ" | "NASDAQ" | "NYSE" | "KOSPI200" | "KOSDAQ150" | "DOW" | "SNP500" | "NDX100" */
    private String marketFilter = "ALL";

    /** "ALL" | "STOCK" | "ETF" | "ETN" | "ELW" */
    private String stockType = "ALL";

    /** YYYY-MM-DD (없으면 latest) */
    private String baseDt;

    /** 사용자가 선택한 지표 목록 */
    private List<IndicatorRequest> indicators = new ArrayList<IndicatorRequest>();

    private String sortColumn = "trendStrength";
    private String sortDir = "desc";
    private Integer limit = 200;

    public static class IndicatorRequest {
        private String id;
        private Map<String, Object> params = new HashMap<String, Object>();
        public IndicatorRequest() {}
        public IndicatorRequest(String id, Map<String, Object> params) {
            this.id = id;
            this.params = params == null ? new HashMap<String, Object>() : params;
        }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Map<String, Object> getParams() { return params; }
        public void setParams(Map<String, Object> params) {
            this.params = params == null ? new HashMap<String, Object>() : params;
        }
    }

    public String getMktGroup() { return mktGroup; }
    public void setMktGroup(String mktGroup) { this.mktGroup = mktGroup; }
    public String getMarketFilter() { return marketFilter; }
    public void setMarketFilter(String marketFilter) { this.marketFilter = marketFilter; }
    public String getStockType() { return stockType; }
    public void setStockType(String stockType) { this.stockType = stockType; }
    public String getBaseDt() { return baseDt; }
    public void setBaseDt(String baseDt) { this.baseDt = baseDt; }
    public List<IndicatorRequest> getIndicators() { return indicators; }
    public void setIndicators(List<IndicatorRequest> indicators) {
        this.indicators = indicators == null ? new ArrayList<IndicatorRequest>() : indicators;
    }
    public String getSortColumn() { return sortColumn; }
    public void setSortColumn(String sortColumn) { this.sortColumn = sortColumn; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}
