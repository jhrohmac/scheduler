(function (registry) {
    "use strict";
    registry.register({
        id: "PRICE_RANGE",
        renderBadge: function () { return "가격대"; },
        renderMatchDesc: function (stock, params) {
            var min = params && params.minPrice != null ? Number(params.minPrice) : 0;
            var max = params && params.maxPrice != null ? Number(params.maxPrice) : 0;
            return min.toLocaleString() + " ~ " + max.toLocaleString() + "원 범위";
        }
    });
})(window.IndicatorRegistry);
