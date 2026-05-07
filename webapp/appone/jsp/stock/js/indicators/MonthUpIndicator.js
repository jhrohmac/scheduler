(function (registry) {
    "use strict";
    registry.register({
        id: "MONTH_UP",
        renderBadge: function () { return "당월상승"; },
        renderMatchDesc: function (stock, params) {
            var min = params && params.minRate != null ? Number(params.minRate) : 0;
            var rate = stock && stock.monChgRate != null ? Number(stock.monChgRate).toFixed(2) : "—";
            if (min <= 0) return "월 등락률 " + rate + "% > 0%";
            return "월 등락률 " + rate + "% ≥ " + min + "%";
        }
    });
})(window.IndicatorRegistry);
