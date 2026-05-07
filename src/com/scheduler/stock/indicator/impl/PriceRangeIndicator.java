package com.scheduler.stock.indicator.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.ParameterDef;
import com.scheduler.stock.indicator.SqlFragment;

/**
 * 가격대 필터 (PRICE_RANGE)
 *  조건: minPrice ≤ curPrice ≤ maxPrice
 *  → TB_REC_SIGNAL.CUR_PRICE 활용
 */
public class PriceRangeIndicator implements Indicator {

    public String getId() { return "PRICE_RANGE"; }
    public String getDisplayName() { return "가격대 필터"; }
    public String getCategory() { return "기본"; }

    public List<ParameterDef> getParameterDefs() {
        ParameterDef p1 = new ParameterDef("minPrice", "최소", "number", 1000);
        p1.setMinValue(0).setStep(100).setUnit("원");
        ParameterDef p2 = new ParameterDef("maxPrice", "최대", "number", 500000);
        p2.setMinValue(0).setStep(1000).setUnit("원");
        return Arrays.asList(p1, p2);
    }

    public SqlFragment toSqlFragment(Map<String, Object> params) {
        double minP = numParam(params, "minPrice", 0);
        double maxP = numParam(params, "maxPrice", Double.MAX_VALUE);
        SqlFragment frag = new SqlFragment("R.CUR_PRICE BETWEEN #{p_minPrice} AND #{p_maxPrice}");
        frag.addParam("p_minPrice", Double.valueOf(minP));
        frag.addParam("p_maxPrice", Double.valueOf(maxP));
        return frag;
    }

    public boolean evaluate(IndicatorContext ctx, Map<String, Object> params) {
        return true;
    }

    public boolean requiresPriceList() { return false; }

    private static double numParam(Map<String, Object> params, String key, double def) {
        Object v = params == null ? null : params.get(key);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
}
