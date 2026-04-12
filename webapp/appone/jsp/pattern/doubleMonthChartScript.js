var DoubleMonthChartScript = (function () {

    function isMinuteDivCode(code) {
        if (!code) return false;
        var p = ("" + code).toUpperCase();
        if (p === "T") return true;
        if (p.length > 1 && p.charAt(0) === "T") {
            var n = p.substring(1);
            return /^[0-9]+$/.test(n);
        }
        return false;
    }

    /**
     * 일봉 데이터(ohlc: [time,o,h,l,c])에서
     *  - 월 경계선 시간(boundaries)
     *  - 월봉 오버레이 캔들(overlay)
     * 를 생성한다. (기존 ChartScript 로직 그대로)
     */
    function buildMonthlyOverlayFromDaily(ohlc) {
        var result = {
            overlay: [],
            boundaries: []
        };
        if (!ohlc || !ohlc.length) return result;

        function pickMiddleTime(arr, startIdx, endIdx) {
            if (!arr || startIdx == null || endIdx == null) return null;
            if (startIdx < 0) startIdx = 0;
            if (endIdx < 0) endIdx = 0;
            if (endIdx < startIdx) endIdx = startIdx;
            var mid = Math.floor((startIdx + endIdx) / 2);
            var row = arr[mid];
            return row ? row[0] : null;
        }

        var currentMonthKey = null;
        var agg = null;

        for (var i = 0; i < ohlc.length; i++) {
            var row = ohlc[i];
            var time = row[0];
            var op = row[1];
            var hi = row[2];
            var lo = row[3];
            var cl = row[4];

            var d = new Date(time);
            if (isNaN(d.getTime())) continue;

            var y = d.getFullYear();
            var m = d.getMonth();
            var key = y * 12 + m;

            if (currentMonthKey === null || key !== currentMonthKey) {
                if (agg) {
                    agg.lastTime = ohlc[i - 1][0];
                    agg.lastIndex = i - 1;
                    agg.centerTime = pickMiddleTime(ohlc, agg.startIndex, agg.lastIndex);
                    result.overlay.push(agg);
                }
                currentMonthKey = key;
                result.boundaries.push(time);
                agg = {
                    open: op,
                    high: hi,
                    low: lo,
                    close: cl,
                    firstTime: time,
                    lastTime: time,
                    startIndex: i,
                    lastIndex: i,
                    centerTime: time
                };
            } else {
                if (hi > agg.high) agg.high = hi;
                if (lo < agg.low) agg.low = lo;
                agg.close = cl;
                agg.lastTime = time;
                agg.lastIndex = i;
            }
        }

        if (agg) {
            agg.lastIndex = (agg.lastIndex != null ? agg.lastIndex : (ohlc.length - 1));
            agg.centerTime = pickMiddleTime(ohlc, agg.startIndex, agg.lastIndex);
            result.overlay.push(agg);
        }

        var overlayCandles = [];
        var b = result.boundaries;

        for (var j = 0; j < result.overlay.length; j++) {
            var m = result.overlay[j];
            var start = b[j];
            var center = (m.centerTime != null ? m.centerTime : start);

            overlayCandles.push([
                center,
                m.open,
                m.high,
                m.low,
                m.close
            ]);
        }

        result.overlay = overlayCandles;
        return result;
    }

    function updateMonthOverlayPointWidth(chart, monthBoundaryTimes, axis, options, periodDivCodeVal) {
        if (!chart) return;
        if (!options.doubleChartEnabled) return;

        var divCode = (periodDivCodeVal || "D").toUpperCase();
        if (divCode !== "D") return;

        var monthSeries = chart.get("monthOverlay");
        if (!monthSeries || !monthSeries.points || !monthSeries.points.length) {
            return;
        }
        if (!monthBoundaryTimes || monthBoundaryTimes.length < 2) {
            return;
        }

        var points = monthSeries.points;
        var lineWidth = monthSeries.options.lineWidth || 2;
        var crispCorr = (monthSeries.options.crisp !== false)
            ? (Math.round(lineWidth) % 2) / 2 : 0;

        for (var i = 0; i < points.length; i++) {
            var point = points[i];
            if (!point || !point.graphic) continue;
            if (point.plotX == null || point.plotY == null) continue;
            if (i >= monthBoundaryTimes.length) continue;

            var leftB = monthBoundaryTimes[i];
            var rightB = (i + 1 < monthBoundaryTimes.length)
                ? monthBoundaryTimes[i + 1]
                : null;

            if (rightB === null) {
                if (i > 0) {
                    rightB = leftB + (monthBoundaryTimes[i] - monthBoundaryTimes[i - 1]);
                } else {
                    continue;
                }
            }

            var leftPx = axis.toPixels(leftB);
            var rightPx = axis.toPixels(rightB);
            var widthPx = Math.abs(rightPx - leftPx);

            if (!isFinite(widthPx) || widthPx <= 0) continue;

            widthPx = widthPx * 0.96;
            if (widthPx < 4) widthPx = 4;

            var halfWidth = Math.round(widthPx / 2);
            var crispX = Math.round(point.plotX) - crispCorr;

            var plotOpen = point.plotOpen;
            var plotClose = point.plotClose;
            var plotHigh = point.plotHigh;
            var plotLow = point.plotLow;

            if (plotOpen == null || plotClose == null) continue;
            if (plotHigh == null) plotHigh = Math.min(plotOpen, plotClose);
            if (plotLow == null) plotLow = Math.max(plotOpen, plotClose);

            var topBox = Math.min(plotOpen, plotClose);
            var bottomBox = Math.max(plotOpen, plotClose);

            var path = [
                ['M', crispX, Math.round(topBox)],
                ['L', crispX, Math.round(plotHigh)],
                ['M', crispX, Math.round(bottomBox)],
                ['L', crispX, Math.round(plotLow)],
                ['M', crispX - halfWidth, Math.round(topBox)],
                ['L', crispX + halfWidth, Math.round(topBox)],
                ['L', crispX + halfWidth, Math.round(bottomBox)],
                ['L', crispX - halfWidth, Math.round(bottomBox)],
                ['Z']
            ];

            point.graphic.attr({ d: path });
        }
    }

    function buildMonthPlotLines(times, periodDivCodeVal, monthBoundaryTimes) {
        var plotLines = [];
        var currentPeriod = -1;

        var div = (periodDivCodeVal || "D").toUpperCase();
        var period;
        if (isMinuteDivCode(div)) {
            period = "minute";
        } else if (div === "D") {
            period = "day";
        } else if (div === "W") {
            period = "week";
        } else if (div === "M") {
            period = "month";
        } else if (div === "Y") {
            period = "year";
        } else {
            period = "day";
        }

        var source = times;
        if (period === "day" && monthBoundaryTimes && monthBoundaryTimes.length) {
            source = monthBoundaryTimes;
        }

        for (var i = 0; i < source.length; i++) {
            var t = source[i];
            if (t == null) continue;

            var currentDate = new Date(t);
            if (isNaN(currentDate.getTime())) continue;

            var periodValue;
            if (period.indexOf("minute") !== -1) {
                periodValue = currentDate.getFullYear() * 10000 + (currentDate.getMonth() + 1) * 100 + currentDate.getDate();
            } else if (period === "day") {
                periodValue = currentDate.getMonth();
            } else if (period === "month" || period === "week" || period === "year") {
                periodValue = currentDate.getFullYear();
            } else {
                periodValue = currentDate.getMonth();
            }

            if (periodValue !== currentPeriod) {
                plotLines.push({
                    color: "rgba(0, 0, 0, 0.3)",
                    width: 1,
                    value: t,
                    dashStyle: "dash",
                    zIndex: 5,
                    label: {
                        enabled: false
                    }
                });
                currentPeriod = periodValue;
            }
        }

        return plotLines;
    }

    return {
        buildMonthlyOverlayFromDaily: buildMonthlyOverlayFromDaily,
        updateMonthOverlayPointWidth: updateMonthOverlayPointWidth,
        buildMonthPlotLines: buildMonthPlotLines
    };
})();
