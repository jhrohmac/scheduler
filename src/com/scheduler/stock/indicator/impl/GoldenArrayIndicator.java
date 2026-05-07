package com.scheduler.stock.indicator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scheduler.stock.indicator.Indicator;
import com.scheduler.stock.indicator.IndicatorContext;
import com.scheduler.stock.indicator.ParameterDef;
import com.scheduler.stock.indicator.SqlFragment;

/**
 * 정배열 필터 (GOLDEN_ARRAY)
 *  조건: MA5 > MA20 > MA60 > MA120 > MA240 (4구간 AND)
 *  → TB_REC_SIGNAL.GOLDEN_YN 사전계산값 활용 (SQL 단계)
 *
 *  파라미터:
 *   - strictMode: "FULL"(5종 모두) | "PARTIAL_4"(MA240 제외) | "PARTIAL_3"(MA60 이상)
 */
public class GoldenArrayIndicator implements Indicator {

    public String getId() { return "GOLDEN_ARRAY"; }
    public String getDisplayName() { return "정배열 필터"; }
    public String getCategory() { return "추세"; }

    public List<ParameterDef> getParameterDefs() {
        ParameterDef p = new ParameterDef("strictMode", "엄격도", "select", "FULL");
        List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
        options.add(buildOpt("FULL",       "5종 모두 (엄격)"));
        options.add(buildOpt("PARTIAL_4",  "4종 (MA240 제외)"));
        options.add(buildOpt("PARTIAL_3",  "3종 (MA60 이상)"));
        p.setOptions(options);
        return Arrays.asList(p);
    }

    public SqlFragment toSqlFragment(Map<String, Object> params) {
        String mode = strParam(params, "strictMode", "FULL");
        SqlFragment frag = new SqlFragment();
        if ("FULL".equals(mode)) {
            // 사전계산된 GOLDEN_YN 활용
            frag.setWhereClause("R.GOLDEN_YN = #{p_goldenYn}");
            frag.addParam("p_goldenYn", "Y");
        } else if ("PARTIAL_4".equals(mode)) {
            frag.setWhereClause("R.MA5 > R.MA20 AND R.MA20 > R.MA60 AND R.MA60 > R.MA120");
        } else { // PARTIAL_3
            frag.setWhereClause("R.MA5 > R.MA20 AND R.MA20 > R.MA60");
        }
        return frag;
    }

    public boolean evaluate(IndicatorContext ctx, Map<String, Object> params) {
        return true; // SQL에서 처리
    }

    public boolean requiresPriceList() { return false; }

    private static Map<String, Object> buildOpt(String value, String label) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("value", value);
        m.put("label", label);
        return m;
    }

    private static String strParam(Map<String, Object> params, String key, String def) {
        Object v = params == null ? null : params.get(key);
        return v == null ? def : String.valueOf(v);
    }
}
