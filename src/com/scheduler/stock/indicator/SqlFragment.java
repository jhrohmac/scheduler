package com.scheduler.stock.indicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 지표가 SQL WHERE 조각으로 표현 가능한 경우 반환하는 객체.
 * MyBatis 동적 쿼리에 통합하기 위한 키-값 형태로 변환된다.
 */
public class SqlFragment {

    /** WHERE 조각 (MyBatis OGNL 표현식 사용 가능). 예: "R.GOLDEN_YN = #{p_goldenYn}" */
    private String whereClause;

    /** 바인드 파라미터 (MyBatis 파라미터 맵에 머지됨) */
    private Map<String, Object> bindParams = new HashMap<String, Object>();

    public SqlFragment() {}

    public SqlFragment(String whereClause) {
        this.whereClause = whereClause;
    }

    public SqlFragment addParam(String key, Object value) {
        this.bindParams.put(key, value);
        return this;
    }

    public String getWhereClause() { return whereClause; }
    public void setWhereClause(String whereClause) { this.whereClause = whereClause; }

    public Map<String, Object> getBindParams() { return bindParams; }
    public void setBindParams(Map<String, Object> bindParams) {
        this.bindParams = bindParams != null ? bindParams : new HashMap<String, Object>();
    }

    /** 여러 조각을 AND로 병합 */
    public static SqlFragment merge(List<SqlFragment> fragments) {
        if (fragments == null || fragments.isEmpty()) {
            return null;
        }
        List<String> clauses = new ArrayList<String>();
        SqlFragment merged = new SqlFragment();
        for (SqlFragment f : fragments) {
            if (f == null) continue;
            if (f.getWhereClause() != null && !f.getWhereClause().trim().isEmpty()) {
                clauses.add("(" + f.getWhereClause() + ")");
            }
            if (f.getBindParams() != null) {
                merged.getBindParams().putAll(f.getBindParams());
            }
        }
        if (clauses.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clauses.size(); i++) {
            if (i > 0) sb.append(" AND ");
            sb.append(clauses.get(i));
        }
        merged.setWhereClause(sb.toString());
        return merged;
    }
}
