var MaScript = (function () {

    function calcSMA(times, closes, period) {
        var out = [];
        var sum = 0;
        for (var i = 0; i < times.length; i++) {
            var p = parseFloat(closes[i]);
            if (isNaN(p)) p = 0;
            sum += p;
            if (i >= period) {
                var old = parseFloat(closes[i - period]);
                if (isNaN(old)) old = 0;
                sum -= old;
            }
            if (i >= period - 1) {
                out.push([times[i], sum / period]);
            }
        }
        return out;
    }

    /**
     * options, times, closes 를 받아 MA 시리즈 배열을 만들어 돌려준다.
     * - 기존 ChartScript 의 MA 생성 로직을 그대로 옮김
     */
    function buildMaSeries(options, times, closes) {
        var result = [];

        var maConfigs = Array.isArray(options.maConfigs) && options.maConfigs.length
            ? options.maConfigs.slice()
            : null;

        if (maConfigs) {
            maConfigs = maConfigs
                .filter(function (c) {
                    if (!c) return false;
                    if (c.enabledYn && c.enabledYn !== "Y") return false;
                    return typeof c.seriesPeriod === "number" && c.seriesPeriod > 0;
                })
                .sort(function (a, b) {
                    return a.seriesPeriod - b.seriesPeriod;
                });

            for (var mIndex = 0; mIndex < maConfigs.length; mIndex++) {
                var conf = maConfigs[mIndex];
                var n = conf.seriesPeriod;
                var label = conf.seriesLabel || ("MA" + n);
                var color = conf.seriesColor || undefined;
                var id = conf.seriesKey || ("ma" + n);
                var lineWidth = (conf.lineWidth !== undefined && conf.lineWidth !== null) ? parseFloat(conf.lineWidth) : null;

                var maDataConf = calcSMA(times, closes, n);
                if (!maDataConf.length) continue;

                var series = {
                    type: "line",
                    name: label,
                    id: id,
                    data: maDataConf,
                    maPeriod: n,
                    maLabel: label,
                    maColor: color,
                    yAxis: 0,
                    tooltip: { valueDecimals: 2 },
                    dataGrouping: { enabled: false },
                    marker: { enabled: false },
                    color: color
                };
                if (!isNaN(lineWidth) && lineWidth > 0) {
                    series.lineWidth = lineWidth;
                }
                result.push(series);
            }
        } else {
            var periods = Array.isArray(options.maPeriods) ? options.maPeriods.slice() : [];
            periods = periods
                .filter(function (n) {
                    return typeof n === "number" && n > 0;
                })
                .sort(function (a, b) {
                    return a - b;
                });

            for (var p = 0; p < periods.length; p++) {
                var n2 = periods[p];
                var maData = calcSMA(times, closes, n2);
                if (!maData.length) continue;
                result.push({
                    type: "line",
                    name: "MA" + n2,
                    id: "ma" + n2,
                    data: maData,
                    maPeriod: n2,
                    maLabel: "MA" + n2,
                    yAxis: 0,
                    tooltip: { valueDecimals: 2 },
                    dataGrouping: { enabled: false },
                    marker: { enabled: false }
                });
            }
        }

        return result;
    }

    return {
        calcSMA: calcSMA,
        buildMaSeries: buildMaSeries
    };
})();
