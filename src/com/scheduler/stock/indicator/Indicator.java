package com.scheduler.stock.indicator;

import java.util.List;
import java.util.Map;

/**
 * 동적 추천 지표 인터페이스 (플러그인 모듈).
 *
 * 신규 지표 추가 절차:
 *   1. 본 인터페이스를 구현하는 클래스 작성 (impl/ 패키지)
 *   2. stockService.xml에 빈 등록
 *   3. recPickDynamicService 빈의 indicators 프로퍼티에 추가
 *   4. webapp/appone/jsp/stock/js/indicators/ 에 같은 ID의 JS 모듈 1개 추가
 *
 * → 기존 지표 / 기존 화면 영향 0
 */
public interface Indicator {

    /** 지표 고유 ID (영문 대문자_언더스코어). 예: "GOLDEN_ARRAY", "RSI" */
    String getId();

    /** 화면 표시명 (한국어). 예: "정배열 필터" */
    String getDisplayName();

    /** 지표 카테고리. "추세" | "모멘텀" | "유동성" | "기본" */
    String getCategory();

    /**
     * 사용자 입력 파라미터 메타데이터.
     * 화면이 이 정의를 받아 자동으로 입력 컨트롤을 렌더링한다.
     */
    List<ParameterDef> getParameterDefs();

    /**
     * SQL 단계에서 처리 가능한 경우 SQL 조각을 반환.
     * 사전계산 컬럼(TB_REC_SIGNAL)을 활용하는 지표는 여기서 WHERE 조각 생성.
     *
     * @return null이면 SQL로 표현 불가 → Java 단계에서 evaluate() 호출됨
     */
    SqlFragment toSqlFragment(Map<String, Object> params);

    /**
     * Java 메모리에서 직접 평가 (실시간 계산 지표).
     * SQL 단계에서 이미 통과한 경우 일반적으로 true 반환.
     *
     * @param ctx 종목 컨텍스트 (RecSignalDto + 필요시 priceList)
     * @param params 사용자 파라미터값
     * @return 통과 여부
     */
    boolean evaluate(IndicatorContext ctx, Map<String, Object> params);

    /**
     * 이 지표가 일봉 데이터(priceList)를 필요로 하는가?
     * true 반환 시 Service가 일괄 prefetch 함.
     */
    boolean requiresPriceList();
}
