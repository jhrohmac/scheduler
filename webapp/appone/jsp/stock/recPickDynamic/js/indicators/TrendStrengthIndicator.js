(function (registry) {
    "use strict";
    registry.register({
        id: "TREND_STRENGTH",
        renderBadge: function () { return "추세강도"; },
        renderMatchDesc: function (stock, params) {
            var min = params && params.min != null ? Number(params.min) : 2.0;
            var v = stock && stock.trendStrength != null ? Number(stock.trendStrength).toFixed(2) : "—";
            return "추세강도 " + v + "% ≥ " + min + "%";
        }
    });
})(window.IndicatorRegistry);
