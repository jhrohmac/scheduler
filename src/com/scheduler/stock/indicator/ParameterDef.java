package com.scheduler.stock.indicator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 지표 파라미터 메타데이터.
 * 화면에서 자동으로 입력 컨트롤을 렌더링하기 위해 사용된다.
 */
public class ParameterDef {

    private String key;            // 예: "min", "period", "maxRsi"
    private String label;          // 화면 표시명
    private String inputType;      // "number" | "range" | "checkbox" | "select"
    private Object defaultValue;
    private Object minValue;
    private Object maxValue;
    private Object step;
    private String unit;           // 단위 표시 (예: "%", "억원", "일")
    private List<Map<String, Object>> options; // select 타입용

    public ParameterDef() {}

    public ParameterDef(String key, String label, String inputType, Object defaultValue) {
        this.key = key;
        this.label = label;
        this.inputType = inputType;
        this.defaultValue = defaultValue;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("key", key);
        m.put("label", label);
        m.put("inputType", inputType);
        m.put("defaultValue", defaultValue);
        if (minValue != null) m.put("minValue", minValue);
        if (maxValue != null) m.put("maxValue", maxValue);
        if (step != null) m.put("step", step);
        if (unit != null) m.put("unit", unit);
        if (options != null) m.put("options", options);
        return m;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }
    public Object getDefaultValue() { return defaultValue; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
    public Object getMinValue() { return minValue; }
    public ParameterDef setMinValue(Object v) { this.minValue = v; return this; }
    public Object getMaxValue() { return maxValue; }
    public ParameterDef setMaxValue(Object v) { this.maxValue = v; return this; }
    public Object getStep() { return step; }
    public ParameterDef setStep(Object v) { this.step = v; return this; }
    public String getUnit() { return unit; }
    public ParameterDef setUnit(String unit) { this.unit = unit; return this; }
    public List<Map<String, Object>> getOptions() { return options; }
    public ParameterDef setOptions(List<Map<String, Object>> options) { this.options = options; return this; }
}
