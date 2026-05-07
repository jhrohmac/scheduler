package com.scheduler.stock.indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 모든 Indicator 구현체를 등록·조회하는 중앙 레지스트리.
 * Spring XML에서 indicators 프로퍼티로 모든 지표 빈을 주입받아 자동 등록한다.
 */
public class IndicatorRegistry {

    private final Map<String, Indicator> registry = new LinkedHashMap<String, Indicator>();

    /** Spring XML로 모든 Indicator 빈 주입 */
    public void setIndicators(List<Indicator> indicators) {
        registry.clear();
        if (indicators != null) {
            for (Indicator ind : indicators) {
                if (ind == null) continue;
                registry.put(ind.getId(), ind);
            }
        }
    }

    public Indicator get(String id) {
        return registry.get(id);
    }

    public List<Indicator> listAll() {
        return new ArrayList<Indicator>(registry.values());
    }

    /**
     * UI 자동 렌더링용 메타데이터 목록 반환.
     * 화면 진입 시 1회 호출되어 좌측 패널을 구성한다.
     */
    public List<Map<String, Object>> getMetadataList() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Indicator ind : registry.values()) {
            Map<String, Object> meta = new LinkedHashMap<String, Object>();
            meta.put("id", ind.getId());
            meta.put("label", ind.getDisplayName());
            meta.put("category", ind.getCategory());
            List<Map<String, Object>> paramMaps = new ArrayList<Map<String, Object>>();
            for (ParameterDef p : ind.getParameterDefs()) {
                paramMaps.add(p.toMap());
            }
            meta.put("params", paramMaps);
            meta.put("requiresPriceList", ind.requiresPriceList());
            result.add(meta);
        }
        return result;
    }

    /** 특정 카테고리 지표만 반환 */
    public List<Indicator> listByCategory(String category) {
        List<Indicator> list = new ArrayList<Indicator>();
        for (Indicator ind : registry.values()) {
            if (category == null || category.equals(ind.getCategory())) {
                list.add(ind);
            }
        }
        return list;
    }

    /** 디버그/모니터링용 ID 목록 */
    public List<String> getRegisteredIds() {
        return Collections.unmodifiableList(new ArrayList<String>(registry.keySet()));
    }

    @SuppressWarnings("unused")
    private Map<String, Object> emptyMap() {
        return new HashMap<String, Object>();
    }
}
