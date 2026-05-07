(function (registry) {
    "use strict";
    registry.register({
        id: "VOLUME_FILTER",
        renderBadge: function () { return "거래대금"; },
        renderMatchDesc: function (stock, params) {
            var min = params && params.minTradeValue != null ? Number(params.minTradeValue) : 50;
            var v = stock && stock.avgTrdVal20 != null
                  ? (Number(stock.avgTrdVal20) / 1e8).toFixed(0) : "—";
            return "20일 평균 " + v + "억 ≥ " + min + "억";
        }
    });
})(window.IndicatorRegistry);
