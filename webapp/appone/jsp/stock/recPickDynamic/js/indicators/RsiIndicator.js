(function (registry) {
    "use strict";
    registry.register({
        id: "RSI",
        isNew: true,
        renderBadge: function () { return "RSI"; },
        renderMatchDesc: function (stock, params) {
            var period = params && params.period != null ? params.period : 14;
            var maxRsi = params && params.maxRsi != null ? params.maxRsi : 70;
            return "RSI(" + period + ") ≤ " + maxRsi;
        }
    });
})(window.IndicatorRegistry);
