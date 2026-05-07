package com.scheduler.stock.indicator.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.ParameterDef;
import com.scheduler.stock.indicator.SqlFragment;

/**
 * 거래대금 필터 (VOLUME_FILTER)
 *  조건: 20일 평균 거래대금 ≥ 사용자 지정 (억원 단위)
 *  → TB_REC_SIGNAL.AVG_TRD_VAL_20 사전계산값 활용
 *
 *  파라미터:
 *   - minTradeValue: 최소 평균 거래대금 (억원)
 */
public class VolumeFilterIndicator implements Indicator {

    private static final long EOK = 100_000_000L;

    public String getId() { return "VOLUME_FILTER"; }
    public String getDisplayName() { return "거래대금 필터"; }
    public String getCategory() { return "유동성"; }

    public List<ParameterDef> getParameterDefs() {
        ParameterDef p = new ParameterDef("minTradeValue", "20일 평균 ≥", "number", 50);
        p.setMinValue(0).setMaxValue(100000).setStep(10).setUnit("억원");
        return Arrays.asList(p);
    }

    public SqlFragment toSqlFragment(Map<String, Object> params) {
        double minEok = numParam(params, "minTradeValue", 50);
        long minWon = (long) (minEok * EOK);
        SqlFragment frag = new SqlFragment("R.AVG_TRD_VAL_20 >= #{p_minTrdVal}");
        frag.addParam("p_minTrdVal", Long.valueOf(minWon));
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
