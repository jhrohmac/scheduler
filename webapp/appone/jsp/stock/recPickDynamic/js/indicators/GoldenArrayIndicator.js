(function (registry) {
    "use strict";
    registry.register({
        id: "GOLDEN_ARRAY",
        renderBadge: function () { return "정배열"; },
        renderMatchDesc: function (stock, params) {
            var mode = params && params.strictMode ? params.strictMode : "FULL";
            if (mode === "FULL")      return "MA5 > MA20 > MA60 > MA120 > MA240 (전구간)";
            if (mode === "PARTIAL_4") return "MA5 > MA20 > MA60 > MA120 (4구간)";
            return "MA5 > MA20 > MA60 (3구간)";
        }
    });
})(window.IndicatorRegistry);
