var ChartFeatureToggle = (function () {
    var CHART_ID = "KIS_ITEMCHART";
    var SERIES_TYPE = "FEATURE";

    var defaults = {
        volumeEnabled: true,
        doubleChartEnabled: true,
        doubleChartMode: "all",
        monthLinesEnabled: true,
        highLowEnabled: true,
        maSrEnabled: true
    };

    var lastState = null;
    var saveTimer = null;
    var loadInFlight = false;

    function toBoolYn(v) {
        return (v === true || v === "Y" || v === "y");
    }

    function safeAjaxGet(forceDefault, onDone) {
        if (!window.__URLS || !window.__URLS.kisItemchartpriceOptionData) {
            onDone && onDone(null);
            return;
        }

        loadInFlight = true;
        $.ajax({
            url: window.__URLS.kisItemchartpriceOptionData,
            type: "GET",
            dataType: "json",
            data: {
                chartId: CHART_ID,
                seriesType: SERIES_TYPE,
                forceDefault: forceDefault ? "Y" : "N"
            },
            success: function (res) {
                loadInFlight = false;
                if (!res || (res.system_code && ("" + res.system_code) !== "0000")) {
                    onDone && onDone(null);
                    return;
                }
                onDone && onDone($.isArray(res.data) ? res.data : []);
            },
            error: function () {
                loadInFlight = false;
                onDone && onDone(null);
            }
        });
    }

    function listToState(list) {
        var out = {
            volumeEnabled: defaults.volumeEnabled,
            doubleChartEnabled: defaults.doubleChartEnabled,
            doubleChartMode: defaults.doubleChartMode,
            monthLinesEnabled: defaults.monthLinesEnabled,
            highLowEnabled: defaults.highLowEnabled,
            maSrEnabled: defaults.maSrEnabled
        };

        if (!$.isArray(list)) return out;

        for (var i = 0; i < list.length; i++) {
            var it = list[i] || {};
            var k = (it.seriesKey || "").toString();
            var yn = (it.enabledYn || "").toString();

            if (k === "volumeEnabled") out.volumeEnabled = toBoolYn(yn);
            if (k === "doubleChartEnabled") {
                out.doubleChartEnabled = toBoolYn(yn);
                out.doubleChartMode = out.doubleChartEnabled ? "all" : "off";
            }
            if (k === "doubleChartMode") {
                var p = parseInt(it.seriesPeriod || 0, 10);
                out.doubleChartMode = (p === 1 ? "recent" : (p === 2 ? "all" : "off"));
                out.doubleChartEnabled = out.doubleChartMode !== "off";
            }
            if (k === "monthLinesEnabled") out.monthLinesEnabled = toBoolYn(yn);
            if (k === "highLowEnabled") out.highLowEnabled = toBoolYn(yn);
            if (k === "maSrEnabled") out.maSrEnabled = toBoolYn(yn);
        }
        return out;
    }

    function stateToList(state) {
        state = state || defaults;
        return [
            { seriesKey: "volumeEnabled", seriesLabel: "거래량 표시", seriesPeriod: 0, seriesColor: null, enabledYn: state.volumeEnabled ? "Y" : "N", displayOrder: 1 },
            { seriesKey: "doubleChartEnabled", seriesLabel: "월봉 오버레이(월봉차트)", seriesPeriod: 0, seriesColor: null, enabledYn: state.doubleChartMode === "off" ? "N" : "Y", displayOrder: 2 },
            { seriesKey: "doubleChartMode", seriesLabel: "더블차트 모드", seriesPeriod: state.doubleChartMode === "recent" ? 1 : (state.doubleChartMode === "all" ? 2 : 0), seriesColor: null, enabledYn: state.doubleChartMode === "off" ? "N" : "Y", displayOrder: 3 },
            { seriesKey: "monthLinesEnabled", seriesLabel: "월봉구분선", seriesPeriod: 0, seriesColor: null, enabledYn: state.monthLinesEnabled ? "Y" : "N", displayOrder: 3 },
            { seriesKey: "highLowEnabled", seriesLabel: "전고/전저 표시", seriesPeriod: 0, seriesColor: null, enabledYn: state.highLowEnabled ? "Y" : "N", displayOrder: 4 },
            { seriesKey: "maSrEnabled", seriesLabel: "MA 지지/저항 표시", seriesPeriod: 0, seriesColor: null, enabledYn: state.maSrEnabled ? "Y" : "N", displayOrder: 5 }
        ];
    }

    function apply(state, rerender) {
        if (!state) state = defaults;

        if (window.ChartScript && typeof ChartScript.setOptions === "function") {
            ChartScript.setOptions({
                volumeEnabled: !!state.volumeEnabled,
                doubleChartEnabled: !!state.doubleChartEnabled,
                doubleChartMode: state.doubleChartMode || (state.doubleChartEnabled ? "all" : "off"),
                monthLinesEnabled: !!state.monthLinesEnabled,
                highLowEnabled: !!state.highLowEnabled,
                maSrEnabled: !!state.maSrEnabled
            });
        }

        if (rerender && window.ChartScript && ChartScript.lastData && ChartScript.lastData.length && typeof ChartScript.renderKisChart === "function") {
            var q = ChartScript.lastQuery || {};
            ChartScript.renderKisChart(ChartScript.lastData, q.periodDivCode || ($("#periodDivCode").val() || "D"));
        }
    }

    function readModal() {
        return {
            volumeEnabled: $("#optVolume").is(":checked"),
            doubleChartEnabled: (($("input[name='optDoubleChartMode']:checked").val() || "off") !== "off"),
            doubleChartMode: ($("input[name='optDoubleChartMode']:checked").val() || ($("#optDoubleChart").is(":checked") ? "all" : "off")),
            monthLinesEnabled: $("#optMonthLines").is(":checked"),
            highLowEnabled: $("#optHighLow").is(":checked"),
            maSrEnabled: $("#optMaSr").is(":checked")
        };
    }

    function writeModal(state) {
        state = state || defaults;
        $("#optVolume").prop("checked", !!state.volumeEnabled);
        var mode = state.doubleChartMode || (state.doubleChartEnabled ? "all" : "off");
        if (mode !== "off" && mode !== "recent" && mode !== "all") mode = "all";
        $("#optDoubleChart").prop("checked", mode !== "off");
        $("input[name='optDoubleChartMode'][value='" + mode + "']").prop("checked", true);
        $("#optMonthLines").prop("checked", !!state.monthLinesEnabled);
        $("#optHighLow").prop("checked", !!state.highLowEnabled);
        $("#optMaSr").prop("checked", !!state.maSrEnabled);
    }

    function saveToDb(state) {
        if (!window.__URLS || !window.__URLS.kisItemchartpriceOptionSave) return;

        var list = stateToList(state);

        $.ajax({
            url: window.__URLS.kisItemchartpriceOptionSave,
            type: "POST",
            dataType: "json",
            data: {
                chartId: CHART_ID,
                seriesType: SERIES_TYPE,
                optionsJson: JSON.stringify(list)
            }
        });
    }

    function scheduleSave(state, delayMs) {
        if (saveTimer) {
            clearTimeout(saveTimer);
            saveTimer = null;
        }
        saveTimer = setTimeout(function () {
            saveToDb(state);
            saveTimer = null;
        }, (delayMs != null ? delayMs : 150));
    }

    function refreshFromDb(forceDefault, applyToChart, cb) {
        safeAjaxGet(forceDefault === true, function (list) {
            var state = listToState(list);
            lastState = state;
            writeModal(state);
            if (applyToChart) apply(state, false);
            cb && cb(state);
        });
    }

    function init() {
        // 珥덇린媛? DB?먯꽌 媛?몄? 李⑦듃???곸슜
        refreshFromDb(false, true);

        // 紐⑤떖 ?대┫ ??DB 湲곗??쇰줈 ?숆린??        
        $("#chartOptionsModal").on("show.bs.modal", function () {
            if (loadInFlight) return;
            refreshFromDb(false, false);
        });

        // 泥댄겕 蹂寃?利됱떆 李⑦듃 諛섏쁺 + DB ????붾컮?댁뒪)
        $(document).off("change.chartFeatureToggle").on("change.chartFeatureToggle", "#optVolume,#optDoubleChart,#optMonthLines,#optHighLow,#optMaSr,input[name='optDoubleChartMode']", function () {
            var state = readModal();
            lastState = state;
            apply(state, true);
            scheduleSave(state, 150);
        });
    }

    return {
        init: init,
        refreshFromDb: refreshFromDb,
        readModal: readModal,
        writeModal: writeModal,
        apply: apply
    };
})(); 
