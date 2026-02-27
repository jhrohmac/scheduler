/************************************************************************
 *  Title        : 전고점 / 전저점 플래그 공통 스크립트
 *  Description  :
 *      - 차트 데이터(OHLC 배열)에서 "마지막 종가 기준 전고점/전저점" 계산
 *      - Highcharts Stock 차트에 전고점/전저점 PlotLine + Flag 표시
 *
 *  chartData 형식:
 *      [
 *          [time(ms), open, high, low, close],
 *          ...
 *      ]
 ************************************************************************/

/* 내부 유틸 함수들 */
function hl_toDouble(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }
    var n = Number(value);
    return isNaN(n) ? null : n;
}

function hl_toLong(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }
    var n = Number(value);
    return isNaN(n) ? null : n;
}

function hl_safeGetCandle(chartData, index) {
    if (!chartData || index < 0 || index >= chartData.length) {
        return null;
    }
    var row = chartData[index];
    if (!row || !row.length || row.length < 5) {
        return null;
    }
    return row;
}

/**
 * time(ms)에 해당하는 캔들을 chartData에서 찾기
 *  - time이 완전히 동일한 첫 번째 캔들을 반환
 */
function hl_findCandleByTime(chartData, time) {
    if (!chartData || !chartData.length || time === null || time === undefined) {
        return null;
    }
    var t = Number(time);
    for (var i = 0; i < chartData.length; i++) {
        var row = chartData[i];
        if (!row || row.length < 5) {
            continue;
        }
        var rowTime = hl_toLong(row[0]);
        if (rowTime !== null && rowTime === t) {
            return row;
        }
    }
    return null;
}

/**
 * 날짜 포맷 (Highcharts.dateFormat 사용)
 */
function hl_formatDate(time) {
    if (typeof Highcharts !== "undefined" && Highcharts.dateFormat) {
        // 필요시 시간까지 보고 싶으면 '%Y-%m-%d %H:%M' 등으로 변경
        return Highcharts.dateFormat('%Y-%m-%d', time);
    }
    var d = new Date(time);
    if (isNaN(d.getTime())) {
        return "";
    }
    var yyyy = d.getFullYear();
    var mm = ('0' + (d.getMonth() + 1)).slice(-2);
    var dd = ('0' + d.getDate()).slice(-2);
    return yyyy + '-' + mm + '-' + dd;
}

/**
 * 전고/전저 "라인" 대신, 오른쪽 영역에 화살표 텍스트로 표시
 *  - plotLine을 사용하지 않고 renderer.text로 표시한다.
 */
function hl_removeArrowText(chart, storeKey) {
    try {
        if (chart && storeKey && chart[storeKey] && chart[storeKey].destroy) {
            chart[storeKey].destroy();
        }
    } catch (e) {
        // ignore
    }
    try {
        if (chart && storeKey) {
            chart[storeKey] = null;
        }
    } catch (e2) {
        // ignore
    }
}

function hl_drawCandleArrowText(chart, storeKey, xTime, yValue, labelText, color) {
    if (!chart || !chart.renderer || !chart.yAxis || !chart.yAxis[0] || !chart.xAxis || !chart.xAxis[0]) {
        return;
    }

    var yAxis = chart.yAxis[0];
    var xAxis = chart.xAxis[0];

    var x = xAxis.toPixels(xTime, false);
    var y = yAxis.toPixels(yValue, false);

    var left = chart.plotLeft;
    var right = chart.plotLeft + chart.plotWidth;
    var top = chart.plotTop;
    var bottom = chart.plotTop + chart.plotHeight;

    if (y < top - 3 || y > bottom + 3 || x < left - 50 || x > right + 50) {
        hl_removeArrowText(chart, storeKey);
        return;
    }

    hl_removeArrowText(chart, storeKey);

    var txt;
    var placeLeft = (x > left + (chart.plotWidth * 0.65)); // 오른쪽에 가까우면 텍스트를 왼쪽에 두고 → 로 표시
    if (placeLeft) {
        txt = (labelText || "") + " →";
    } else {
        txt = "← " + (labelText || "");
    }

    var t = chart.renderer.text(txt, 0, 0)
        .css({
            color: color || "#333333",
            fontWeight: "bold",
            fontSize: "12px",
            textOutline: "none"
        })
        .add();

    var bbox = t.getBBox();

    var xText = placeLeft ? (x - 8 - bbox.width) : (x + 8);
    var yText = y + 4;

    // plot 영역 밖으로 튀지 않도록 보정
    if (xText < left + 2) {
        xText = left + 2;
    }
    if (xText + bbox.width > right - 2) {
        xText = right - 2 - bbox.width;
    }

    if (yText < top + 12) {
        yText = top + 12;
    }
    if (yText > bottom - 2) {
        yText = bottom - 2;
    }

    t.attr({ x: xText, y: yText });

    chart[storeKey] = t;
}

function hl_drawLevelArrowText(chart, storeKey, yValue, labelText, color) {
    // 하위 호환용: 시간축 위치를 알 수 없을 때는 오른쪽 영역에 표시
    if (!chart || !chart.renderer || !chart.yAxis || !chart.yAxis[0]) {
        return;
    }
    var yAxis = chart.yAxis[0];
    var y = yAxis.toPixels(yValue, false);

    var top = chart.plotTop;
    var bottom = chart.plotTop + chart.plotHeight;
    if (y < top - 3 || y > bottom + 3) {
        hl_removeArrowText(chart, storeKey);
        return;
    }

    hl_removeArrowText(chart, storeKey);

    var txt = (labelText || "") + " →";
    var t = chart.renderer.text(txt, 0, 0)
        .css({
            color: color || "#333333",
            fontWeight: "bold",
            fontSize: "12px",
            textOutline: "none"
        })
        .add();

    var bbox = t.getBBox();
    var xRight = chart.plotLeft + chart.plotWidth - 6;
    var x = xRight - bbox.width;
    var yText = y + 4;

    t.attr({ x: x, y: yText });

    chart[storeKey] = t;
}


/**
 * 마지막 종가 기준 전고점 계산
 *  - 이전 캔들(0 ~ lastIndex-1)에서 high >= lastClose 인 지점들 중
 *    가장 높은 high (동일 high 인 경우 더 나중의 time)
 */
function hl_findPreviousHighForLastClose(chartData) {

    var vo = {
        lastTime: null,
        lastClose: null,
        previousHighFound: false,
        previousHighPrice: null,
        previousHighTime: null
    };

    if (!chartData || chartData.length === 0) {
        return vo;
    }

    var lastIndex = chartData.length - 1;
    var lastCandle = hl_safeGetCandle(chartData, lastIndex);
    if (!lastCandle) {
        return vo;
    }

    var lastTime = hl_toLong(lastCandle[0]);
    var lastClose = hl_toDouble(lastCandle[4]);

    vo.lastTime = lastTime;
    vo.lastClose = lastClose;

    if (lastClose === null) {
        return vo;
    }

    var bestHigh = null;
    var bestTime = null;

    for (var i = 0; i < lastIndex; i++) {
        var candle = hl_safeGetCandle(chartData, i);
        if (!candle) {
            continue;
        }

        var high = hl_toDouble(candle[2]);
        var time = hl_toLong(candle[0]);

        if (high === null) {
            continue;
        }

        if (high >= lastClose) {
            if (bestHigh === null) {
                bestHigh = high;
                bestTime = time;
            } else {
                if (high > bestHigh) {
                    bestHigh = high;
                    bestTime = time;
                } else if (high === bestHigh && time !== null && bestTime !== null && time > bestTime) {
                    bestTime = time;
                }
            }
        }
    }

    if (bestHigh !== null && bestTime !== null) {
        vo.previousHighFound = true;
        vo.previousHighPrice = bestHigh;
        vo.previousHighTime = bestTime;
    }

    return vo;
}

/**
 * 마지막 종가 기준 전저점 계산
 *  - 이전 캔들(0 ~ lastIndex-1)에서 low <= lastClose 인 지점들 중
 *    가장 낮은 low (동일 low 인 경우 더 나중의 time)
 */
function hl_findPreviousLowForLastClose(chartData) {

    var vo = {
        lastTime: null,
        lastClose: null,
        previousLowFound: false,
        previousLowPrice: null,
        previousLowTime: null
    };

    if (!chartData || chartData.length === 0) {
        return vo;
    }

    var lastIndex = chartData.length - 1;
    var lastCandle = hl_safeGetCandle(chartData, lastIndex);
    if (!lastCandle) {
        return vo;
    }

    var lastTime = hl_toLong(lastCandle[0]);
    var lastClose = hl_toDouble(lastCandle[4]);

    vo.lastTime = lastTime;
    vo.lastClose = lastClose;

    if (lastClose === null) {
        return vo;
    }

    var bestLow = null;
    var bestTime = null;

    for (var i = 0; i < lastIndex; i++) {
        var candle = hl_safeGetCandle(chartData, i);
        if (!candle) {
            continue;
        }

        var low = hl_toDouble(candle[3]);
        var time = hl_toLong(candle[0]);

        if (low === null) {
            continue;
        }

        if (low <= lastClose) {
            if (bestLow === null) {
                bestLow = low;
                bestTime = time;
            } else {
                if (low < bestLow) {
                    bestLow = low;
                    bestTime = time;
                } else if (low === bestLow && time !== null && bestTime !== null && time > bestTime) {
                    bestTime = time;
                }
            }
        }
    }

    if (bestLow !== null && bestTime !== null) {
        vo.previousLowFound = true;
        vo.previousLowPrice = bestLow;
        vo.previousLowTime = bestTime;
    }

    return vo;
}

/**
 * high / low 둘 다 계산해서 묶어서 반환
 */
function findPreviousHighLowForLastClose(chartData) {
    return {
        high: hl_findPreviousHighForLastClose(chartData),
        low: hl_findPreviousLowForLastClose(chartData)
    };
}

/**
 * 퍼센트 문자열 포맷 (+/-포함)
 */
function hl_formatPct(pct, decimalPlaces) {
    if (pct === null || pct === undefined || isNaN(pct)) {
        return "";
    }
    var d = (decimalPlaces === null || decimalPlaces === undefined) ? 2 : decimalPlaces;
    var sign = pct > 0 ? "+" : "";
    return sign + pct.toFixed(d) + "%";
}

/**
 * Highcharts 차트에 전고점 / 전저점 PlotLine + Flag 적용
 *
 * @param {Highcharts.Chart} chart    생성된 StockChart 인스턴스
 * @param {Object} infoData           종목 기본 정보 (code 등)
 * @param {Array}  chartData          OHLC 배열
 * @param {Object} options            { noRedraw: true/false } 등 선택 옵션
 */
function applyHighLowFlagsFromData(chart, infoData, chartData, options) {

    if (!chart || !chartData || !chartData.length) {
        return;
    }

    var calc = findPreviousHighLowForLastClose(chartData) || {};
    var highPoint = calc.high;
    var lowPoint = calc.low;

    // 마지막 종가 (high/low 계산에서 동일 기준이므로 하나만 사용)
    var lastClose = null;
    if (highPoint && highPoint.lastClose != null) {
        lastClose = hl_toDouble(highPoint.lastClose);
    } else if (lowPoint && lowPoint.lastClose != null) {
        lastClose = hl_toDouble(lowPoint.lastClose);
    }

    var seriesId = null;
    if (infoData) {
        seriesId = infoData.code || infoData.stock_code || infoData.id || null;
    }
    if (!seriesId && chart.series && chart.series.length > 0) {
        seriesId = chart.series[0].options.id || chart.series[0].name;
    }

    var yAxis = (chart.yAxis && chart.yAxis[0]) ? chart.yAxis[0] : null;

    // 기존 라인/플래그 제거
    try {
        if (yAxis && yAxis.removePlotLine) {
            yAxis.removePlotLine('previousHighLine');
            yAxis.removePlotLine('previousLowLine');
        }
        var oldHighFlag = chart.get('previousHighFlag');
        if (oldHighFlag) {
            oldHighFlag.remove(false);
        }
        var oldLowFlag = chart.get('previousLowFlag');
        if (oldLowFlag) {
            oldLowFlag.remove(false);
        }
            hl_removeArrowText(chart, '__hlPrevHighText');
        hl_removeArrowText(chart, '__hlPrevLowText');
} catch (e) {
        console.log('applyHighLowFlagsFromData remove error:', e);
    }

    // ============================================================
    // 전고점 처리
    // ============================================================
    if (highPoint && highPoint.previousHighFound &&
        highPoint.previousHighPrice !== null &&
        highPoint.previousHighTime !== null) {

        var prevHighPrice = hl_toDouble(highPoint.previousHighPrice);
        var prevHighTime  = hl_toLong(highPoint.previousHighTime);

        var changeHighPct = null;
        if (lastClose != null && prevHighPrice != null && prevHighPrice !== 0) {
            changeHighPct = ((lastClose - prevHighPrice) / prevHighPrice) * 100;
        }
        //var highPctShortStr = changeHighPct != null ? hl_formatPct(changeHighPct, 1) : "";
        var highPctLongStr  = changeHighPct != null ? hl_formatPct(changeHighPct, 2) : "";

        // 상승/하락에 따라 색상 변경 (상승: 빨강, 하락: 파랑)
        var isHighUp = (changeHighPct != null && changeHighPct >= 0);
        var highColor = isHighUp ? '#d32f2f' : '#1976d2';

        // 전고점 라벨: 라인 대신 '화살표 텍스트'로 표시
        if (yAxis && prevHighPrice !== null) {
            var highPriceStr = Highcharts.numberFormat(prevHighPrice, 0, '.', ',');
            var highLabelText = '전고점 ' + highPriceStr;
            if (highPctLongStr) {
                highLabelText += ' (현재가 ' + highPctLongStr + ')';
            }
            hl_drawCandleArrowText(chart, '__hlPrevHighText', prevHighTime, prevHighPrice, highLabelText, highColor);
        }
    }

    // ============================================================
    // 전저점 처리
    // ============================================================
    if (lowPoint && lowPoint.previousLowFound &&
        lowPoint.previousLowPrice !== null &&
        lowPoint.previousLowTime !== null) {

        var prevLowPrice = hl_toDouble(lowPoint.previousLowPrice);
        var prevLowTime  = hl_toLong(lowPoint.previousLowTime);

        var changeLowPct = null;
        if (lastClose != null && prevLowPrice != null && prevLowPrice !== 0) {
            changeLowPct = ((lastClose - prevLowPrice) / prevLowPrice) * 100;
        }
        //var lowPctShortStr = changeLowPct != null ? hl_formatPct(changeLowPct, 1) : "";
        var lowPctLongStr  = changeLowPct != null ? hl_formatPct(changeLowPct, 2) : "";

        // 상승/하락에 따라 색상 변경 (상승: 빨강, 하락: 파랑)
        var isLowUp = (changeLowPct != null && changeLowPct >= 0);
        var lowColor = isLowUp ? '#d32f2f' : '#1976d2';

        // 전저점 라벨: 라인 대신 '화살표 텍스트'로 표시
        if (yAxis && prevLowPrice !== null) {
            var lowPriceStr = Highcharts.numberFormat(prevLowPrice, 0, '.', ',');
            var lowLabelText = '전저점 ' + lowPriceStr;
            if (lowPctLongStr) {
                lowLabelText += ' (현재가 ' + lowPctLongStr + ')';
            }
            hl_drawCandleArrowText(chart, '__hlPrevLowText', prevLowTime, prevLowPrice, lowLabelText, lowColor);
        }
    }

    // redraw 제어 (실시간 auto-update에서 무한루프 방지용 옵션)
    if (!options || !options.noRedraw) {
        chart.redraw();
    }
}

/**
 * 실시간 업데이트 자동 반영용 헬퍼
 *
 * 사용 예)
 *   var chart = Highcharts.stockChart(...);
 *   var ohlcData = ...;   // 최초 데이터
 *   applyHighLowFlagsFromData(chart, infoData, ohlcData);
 *   bindHighLowAutoUpdate(chart, infoData, function () {
 *       return latestOhlcData;    // 항상 최신 OHLC 배열을 리턴하는 함수
 *   });
 *
 * - chart가 redraw 될 때마다 최신 데이터 기준으로 전고/전저 플래그를 다시 그림
 *   (options.noRedraw = true 로 무한 루프 방지)
 */
function bindHighLowAutoUpdate(chart, infoData, getChartDataFunc) {
    if (!chart || typeof Highcharts === "undefined" || !Highcharts.addEvent) {
        return;
    }
    if (chart.__hlAutoBinded) {
        return;
    }
    chart.__hlAutoBinded = true;

    Highcharts.addEvent(chart, 'redraw', function () {
        try {
            var data = null;

            if (typeof getChartDataFunc === "function") {
                data = getChartDataFunc();
            } else if (chart.series && chart.series.length > 0) {
                // 기본: 첫 번째 시리즈의 data를 사용 (OHLC 배열이어야 함)
                data = chart.series[0].options.data || chart.series[0].yData;
            }

            if (data && data.length) {
                applyHighLowFlagsFromData(chart, infoData, data, { noRedraw: true });
            }
        } catch (e) {
            console.log('bindHighLowAutoUpdate redraw handler error:', e);
        }
    });
}
