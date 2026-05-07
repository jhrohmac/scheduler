/* ============================================================
 * IndicatorRegistry — JS 지표 모듈 레지스트리
 *
 * 신규 지표 추가:
 *   같은 디렉토리에 새 JS 파일을 만들고
 *   IndicatorRegistry.register({ id, label, category, params, ... }) 만 호출
 *   → JSP <script> 태그 1줄 추가하면 자동 등록
 * ============================================================ */
(function (global) {
    "use strict";

    var modules = {};

    var Registry = {
        /**
         * 지표 모듈 등록.
         * @param {object} mod
         *   mod.id           : 백엔드 Indicator.getId() 와 일치 (예: "RSI")
         *   mod.label        : 화면 표시명 (생략 시 백엔드 메타데이터 사용)
         *   mod.category     : 카테고리 (생략 가능)
         *   mod.isNew        : true 시 "신규" 라벨 표시
         *   mod.renderBadge  : function(params) → 그리드 매칭 뱃지 텍스트 (선택)
         *   mod.renderMatchDesc : function(stock, params) → 매칭 조건 한줄 설명 (선택)
         */
        register: function (mod) {
            if (!mod || !mod.id) return;
            modules[mod.id] = mod;
        },

        get: function (id) { return modules[id]; },

        all: function () { return modules; },

        /** 백엔드 메타와 JS 모듈을 머지하여 최종 지표 정의 반환 */
        merge: function (backendMetaList) {
            var result = [];
            (backendMetaList || []).forEach(function (meta) {
                var jsMod = modules[meta.id] || {};
                result.push({
                    id: meta.id,
                    label: jsMod.label || meta.label,
                    category: jsMod.category || meta.category,
                    params: meta.params || [],
                    isNew: !!jsMod.isNew,
                    renderBadge: jsMod.renderBadge || function () { return null; },
                    renderMatchDesc: jsMod.renderMatchDesc || function () { return null; }
                });
            });
            return result;
        }
    };

    global.IndicatorRegistry = Registry;

})(window);
