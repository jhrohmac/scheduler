package com.scheduler.stock.indicator.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.ParameterDef;
import com.scheduler.stock.indicator.SqlFragment;

/**
 * 추세 강도 필터 (TREND_STRENGTH)
 *  조건: ratio(MA5/20)+ratio(MA20/60)+ratio(MA60/120)+ratio(MA120/240) ≥ min
 *  → TB_REC_SIGNAL.TREND_STRENGTH 사전계산값 활용
 *
 *  파라미터:
 *   - min: 최소 강도(%)
 */
public class TrendStrengthIndicator implements Indicator {

    public String getId() { return "TREND_STRENGTH"; }
    public String getDisplayName() { return "추세 강도 필터"; }
    public String getCategory() { return "추세"; }

    public List<ParameterDef> getParameterDefs() {
        ParameterDef p = new ParameterDef("min", "최소 강도", "range", 2.0);
        p.setMinValue(0.0).setMaxValue(20.0).setStep(0.5).setUnit("%");
        return Arrays.asList(p);
    }

    public SqlFragment toSqlFragment(Map<String, Object> params) {
        double min = numParam(params, "min", 2.0);
        SqlFragment frag = new SqlFragment("R.TREND_STRENGTH >= #{p_minTrend}");
        frag.addParam("p_minTrend", Double.valueOf(min));
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
