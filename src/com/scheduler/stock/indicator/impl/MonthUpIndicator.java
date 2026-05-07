package com.scheduler.stock.indicator.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.ParameterDef;
import com.scheduler.stock.indicator.SqlFragment;

/**
 * 당월 상승 필터 (MONTH_UP)
 *  조건: curPrice > monthOpenPrice + 사용자 지정 최소 등락률
 *  → TB_REC_SIGNAL.MON_UP_YN, MON_CHG_RATE 사전계산값 활용
 *
 *  파라미터:
 *   - minRate: 최소 등락률(%). 0이면 단순 양봉
 */
public class MonthUpIndicator implements Indicator {

    public String getId() { return "MONTH_UP"; }
    public String getDisplayName() { return "당월 상승 필터"; }
    public String getCategory() { return "모멘텀"; }

    public List<ParameterDef> getParameterDefs() {
        ParameterDef p = new ParameterDef("minRate", "최소 등락률", "number", 0.0);
        p.setMinValue(-50.0).setMaxValue(50.0).setStep(0.5).setUnit("%");
        return Arrays.asList(p);
    }

    public SqlFragment toSqlFragment(Map<String, Object> params) {
        double minRate = numParam(params, "minRate", 0.0);
        SqlFragment frag = new SqlFragment();
        if (minRate <= 0.0) {
            frag.setWhereClause("R.MON_UP_YN = #{p_monUpYn}");
            frag.addParam("p_monUpYn", "Y");
        } else {
            frag.setWhereClause("R.MON_UP_YN = #{p_monUpYn} AND R.MON_CHG_RATE >= #{p_minMonRate}");
            frag.addParam("p_monUpYn", "Y");
            frag.addParam("p_minMonRate", Double.valueOf(minRate));
        }
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
