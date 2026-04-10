var CrossSignalScript = (function () {
    function isValidNumber(n) {
        return n !== null && n !== undefined && !isNaN(parseFloat(n));
    }

    function buildSma(values, period) {
        var p = parseInt(period, 10);
        if (!p || p < 1) return [];

        var out = new Array(values.length);
        var sum = 0;
        var q = [];

        for (var i = 0; i < values.length; i++) {
            var v = parseFloat(values[i]);
            if (!isValidNumber(v)) {
                out[i] = null;
                continue;
            }

            q.push(v);
            sum += v;

            if (q.length > p) {
                sum -= q.shift();
            }

            if (q.length === p) {
                out[i] = sum / p;
            } else {
                out[i] = null;
            }
        }

        return out;
    }

    function buildCrossPoints(times, closes, shortPeriod, longPeriod) {
        if (!times || !times.length || !closes || !closes.length) return [];

        var sp = parseInt(shortPeriod, 10);
        var lp = parseInt(longPeriod, 10);
        if (!sp || !lp || sp < 1 || lp < 1 || sp === lp) return [];

        var smaS = buildSma(closes, sp);
        var smaL = buildSma(closes, lp);

        var points = [];
        for (var i = 1; i < times.length; i++) {
            var sPrev = smaS[i - 1];
            var lPrev = smaL[i - 1];
            var sCur = smaS[i];
            var lCur = smaL[i];

            if (!isValidNumber(sPrev) || !isValidNumber(lPrev) || !isValidNumber(sCur) || !isValidNumber(lCur)) {
                continue;
            }

            var diffPrev = sPrev - lPrev;
            var diffCur = sCur - lCur;

            if (diffPrev <= 0 && diffCur > 0) {
                points.push({
                    x: times[i],
                    title: "GC",
                    text: "골든크로스(" + sp + "/" + lp + ")",
                    color: "red",
                    fillColor: "rgba(255,0,0,0.18)"
                });
            } else if (diffPrev >= 0 && diffCur < 0) {
                points.push({
                    x: times[i],
                    title: "DC",
                    text: "데드크로스(" + sp + "/" + lp + ")",
                    color: "#3496ff",
                    fillColor: "rgba(52,150,255,0.18)"
                });
            }
        }
        return points;
    }

    function buildCrossFlagSeries(options, times, closes) {
        options = options || {};
        var enabled = options.crossEnabled === true;
        if (!enabled) return null;

        var sp = options.crossShortPeriod || 5;
        var lp = options.crossLongPeriod || 20;

        var pts = buildCrossPoints(times, closes, sp, lp);
        if (!pts || !pts.length) return null;

        return {
            type: "flags",
            name: "골/데 크로스",
            id: "crossSignals",
            onSeries: "price",
            shape: "circlepin",
            width: 18,
            y: -25,
            data: pts,
            zIndex: 9,
            states: { hover: { enabled: true } },
            tooltip: { pointFormat: "{point.text}" }
        };
    }

    return {
        buildCrossFlagSeries: buildCrossFlagSeries
    };
})();
