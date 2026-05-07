(function (registry) {
    "use strict";
    registry.register({
        id: "MACD",
        isNew: true,
        renderBadge: function () { return "MACD↑"; },
        renderMatchDesc: function (stock, params) {
            var f = params && params.fast != null ? params.fast : 12;
            var s = params && params.slow != null ? params.slow : 26;
            var sig = params && params.signal != null ? params.signal : 9;
            var lb = params && params.lookbackDays != null ? params.lookbackDays : 5;
            return "MACD(" + f + "/" + s + "/" + sig + ") 골든크로스 (최근 " + lb + "일)";
        }
    });
})(window.IndicatorRegistry);
