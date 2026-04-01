(function (window, $) {
  if (!$) {
    return;
  }

  var CHART_ID = "KIS_ITEMCHART";
  var SERIES_TYPE_FEATURE = "FEATURE";
  var SERIES_TYPE_MA = "";
  var SERIES_TYPE_CROSS = "CROSS";
  var MA_LINEWIDTH_DEFAULT = 2.5;
  var MA_LINEWIDTH_MIN = 0.5;
  var MA_LINEWIDTH_MAX = 6;
  var FEATURE_DEFAULTS = {
    volumeEnabled: true,
    doubleChartEnabled: true,
    doubleChartMode: "all",
    monthLinesEnabled: true,
    highLowEnabled: true,
    maSrEnabled: true
  };
  var featureState = $.extend({}, FEATURE_DEFAULTS);
  var maOptionList = [];
  var crossState = {
    crossEnabled: false,
    crossShortPeriod: 5,
    crossLongPeriod: 20
  };
  var featureSaveTimer = null;
  var maSaveTimer = null;
  var crossSaveTimer = null;

  function hasOptionApi() {
    return !!(window.__URLS && window.__URLS.kisItemchartpriceOptionData && window.__URLS.kisItemchartpriceOptionSave);
  }

  function normalizeMaLineWidth(value) {
    var width = parseFloat(value);
    if (isNaN(width)) {
      width = MA_LINEWIDTH_DEFAULT;
    }
    if (width < MA_LINEWIDTH_MIN) {
      width = MA_LINEWIDTH_MIN;
    }
    if (width > MA_LINEWIDTH_MAX) {
      width = MA_LINEWIDTH_MAX;
    }
    return Math.round(width * 2) / 2;
  }

  function enabledMaList() {
    return $.grep(maOptionList || [], function (option) {
      return option && (option.enabledYn === "Y" || option.enabledYn === true);
    }).sort(function (left, right) {
      return (left.seriesPeriod || 0) - (right.seriesPeriod || 0);
    });
  }

  function persistedChartOptions() {
    var enabledList = enabledMaList();
    return {
      volumeEnabled: !!featureState.volumeEnabled,
      doubleChartEnabled: !!featureState.doubleChartEnabled,
      doubleChartMode: featureState.doubleChartMode || (featureState.doubleChartEnabled ? "all" : "off"),
      monthLinesEnabled: !!featureState.monthLinesEnabled,
      highLowEnabled: !!featureState.highLowEnabled,
      maSrEnabled: !!featureState.maSrEnabled,
      maPeriods: $.map(enabledList, function (option) { return option.seriesPeriod || 0; }),
      maConfigs: enabledList.slice(),
      crossEnabled: !!crossState.crossEnabled,
      crossShortPeriod: crossState.crossShortPeriod || 5,
      crossLongPeriod: crossState.crossLongPeriod || 20
    };
  }

  function renderMaLegendFromOptions(list) {
    var $wrap = $("#kisMaLegend");
    var sorted;

    if (!$wrap.length) {
      return;
    }

    $wrap.find(".kis-ma").remove();
    sorted = $.grep(list || [], function (option) {
      return option && (option.enabledYn === "Y" || option.enabledYn === true);
    }).sort(function (left, right) {
      var leftOrder = left.displayOrder != null ? left.displayOrder : (left.seriesPeriod || 0);
      var rightOrder = right.displayOrder != null ? right.displayOrder : (right.seriesPeriod || 0);
      return leftOrder - rightOrder;
    });

    $.each(sorted, function (_, option) {
      var period = parseInt(option.seriesPeriod || 0, 10);
      var color = String(option.seriesColor || "#334155").trim();
      var $badge;

      if (!period) {
        return;
      }

      $badge = $("<span class='kis-ma'></span>");
      $badge.attr("data-ma", period);
      $badge.attr("title", option.seriesLabel || ("MA" + period));
      $badge.text(period);
      $badge.css({
        color: color,
        borderColor: color
      });
      $wrap.append($badge);
    });
  }

  function rerenderChartIfReady() {
    var query;
    if (!window.ChartScript || !window.ChartScript.lastData || !window.ChartScript.lastData.length || typeof window.ChartScript.renderKisChart !== "function") {
      return;
    }

    query = window.ChartScript.lastQuery || {};
    window.ChartScript.renderKisChart(window.ChartScript.lastData, query.periodDivCode || ($("#periodDivCode").val() || "D"));
  }

  function installChartOptionGuard() {
    var originalSetOptions;
    var originalRender;

    if (!window.ChartScript) {
      return;
    }

    if (!window.ChartScript.__recSignalOptionPatched && typeof window.ChartScript.setOptions === "function") {
      originalSetOptions = window.ChartScript.setOptions.bind(window.ChartScript);
      window.ChartScript.setOptions = function (next) {
        var merged = $.extend({}, next || {}, persistedChartOptions());
        return originalSetOptions(merged);
      };
      window.ChartScript.__recSignalOptionPatched = true;
    }

    if (!window.ChartScript.__recSignalRenderPatched && typeof window.ChartScript.renderKisChart === "function") {
      originalRender = window.ChartScript.renderKisChart.bind(window.ChartScript);
      window.ChartScript.renderKisChart = function (list, periodDivCode) {
        var result = originalRender(list, periodDivCode);
        renderMaLegendFromOptions(maOptionList);
        return result;
      };
      window.ChartScript.__recSignalRenderPatched = true;
    }
  }

  function applyPersistedOptions(rerender) {
    installChartOptionGuard();

    if (window.ChartScript && typeof window.ChartScript.setOptions === "function") {
      window.ChartScript.setOptions(persistedChartOptions());
    }

    renderMaLegendFromOptions(maOptionList);

    if (rerender === true) {
      rerenderChartIfReady();
    }
  }

  function openChartOptionsPanel() {
    var $panel = $("#chartOptionsModal");
    var $backdrop = $("#chartOptBackdrop");

    if (!$panel.length) {
      return;
    }

    $panel.addClass("open").attr("aria-hidden", "false");
    $backdrop.addClass("open");
    $("body").addClass("chart-opt-open");
    $("#btnChartOptions").addClass("active");
    $panel.trigger("show.bs.modal");
  }

  function closeChartOptionsPanel() {
    $("#chartOptionsModal").removeClass("open").attr("aria-hidden", "true");
    $("#chartOptBackdrop").removeClass("open");
    $("body").removeClass("chart-opt-open");
    $("#btnChartOptions").removeClass("active");
  }

  function featureStateFromList(list) {
    var state = $.extend({}, FEATURE_DEFAULTS);

    $.each(list || [], function (_, item) {
      var key = String(item.seriesKey || "");
      var enabled = (item.enabledYn === "Y" || item.enabledYn === true);
      var modeValue;

      if (key === "volumeEnabled") {
        state.volumeEnabled = enabled;
      } else if (key === "doubleChartEnabled") {
        state.doubleChartEnabled = enabled;
        state.doubleChartMode = enabled ? "all" : "off";
      } else if (key === "doubleChartMode") {
        modeValue = parseInt(item.seriesPeriod || 0, 10);
        state.doubleChartMode = modeValue === 1 ? "recent" : (modeValue === 2 ? "all" : "off");
        state.doubleChartEnabled = state.doubleChartMode !== "off";
      } else if (key === "monthLinesEnabled") {
        state.monthLinesEnabled = enabled;
      } else if (key === "highLowEnabled") {
        state.highLowEnabled = enabled;
      } else if (key === "maSrEnabled") {
        state.maSrEnabled = enabled;
      }
    });

    return state;
  }

  function featureStateToList(state) {
    return [
      { seriesKey: "volumeEnabled", seriesLabel: "거래량 표시", seriesPeriod: 0, seriesColor: null, enabledYn: state.volumeEnabled ? "Y" : "N", displayOrder: 1 },
      { seriesKey: "doubleChartEnabled", seriesLabel: "월봉 오버레이", seriesPeriod: 0, seriesColor: null, enabledYn: state.doubleChartMode === "off" ? "N" : "Y", displayOrder: 2 },
      { seriesKey: "doubleChartMode", seriesLabel: "더블차트 모드", seriesPeriod: state.doubleChartMode === "recent" ? 1 : (state.doubleChartMode === "all" ? 2 : 0), seriesColor: null, enabledYn: state.doubleChartMode === "off" ? "N" : "Y", displayOrder: 3 },
      { seriesKey: "monthLinesEnabled", seriesLabel: "월/년 구분선", seriesPeriod: 0, seriesColor: null, enabledYn: state.monthLinesEnabled ? "Y" : "N", displayOrder: 4 },
      { seriesKey: "highLowEnabled", seriesLabel: "전고/전저", seriesPeriod: 0, seriesColor: null, enabledYn: state.highLowEnabled ? "Y" : "N", displayOrder: 5 },
      { seriesKey: "maSrEnabled", seriesLabel: "MA 지지/저항", seriesPeriod: 0, seriesColor: null, enabledYn: state.maSrEnabled ? "Y" : "N", displayOrder: 6 }
    ];
  }

  function readFeatureStateFromUi() {
    var mode = $("input[name='optDoubleChartMode']:checked").val() || ($("#optDoubleChart").is(":checked") ? "all" : "off");
    if (mode !== "off" && mode !== "recent" && mode !== "all") {
      mode = "all";
    }

    return {
      volumeEnabled: $("#optVolume").is(":checked"),
      doubleChartEnabled: mode !== "off",
      doubleChartMode: mode,
      monthLinesEnabled: $("#optMonthLines").is(":checked"),
      highLowEnabled: $("#optHighLow").is(":checked"),
      maSrEnabled: $("#optMaSr").is(":checked")
    };
  }

  function writeFeatureStateToUi(state) {
    $("#optVolume").prop("checked", !!state.volumeEnabled);
    $("#optDoubleChart").prop("checked", state.doubleChartMode !== "off");
    $("input[name='optDoubleChartMode'][value='" + (state.doubleChartMode || "all") + "']").prop("checked", true);
    $("#optMonthLines").prop("checked", !!state.monthLinesEnabled);
    $("#optHighLow").prop("checked", !!state.highLowEnabled);
    $("#optMaSr").prop("checked", !!state.maSrEnabled);
  }

  function refreshFeatureOptions(forceDefault, applyToChart) {
    if (!hasOptionApi()) {
      return;
    }

    $.ajax({
      url: window.__URLS.kisItemchartpriceOptionData,
      type: "GET",
      dataType: "json",
      data: {
        chartId: CHART_ID,
        seriesType: SERIES_TYPE_FEATURE,
        forceDefault: forceDefault ? "Y" : "N"
      },
      success: function (res) {
        if (!res || (res.system_code && String(res.system_code) !== "0000")) {
          return;
        }

        featureState = featureStateFromList($.isArray(res.data) ? res.data : []);
        writeFeatureStateToUi(featureState);
        applyPersistedOptions(applyToChart === true);
      }
    });
  }

  function saveFeatureOptions(state) {
    if (!hasOptionApi()) {
      return;
    }

    $.ajax({
      url: window.__URLS.kisItemchartpriceOptionSave,
      type: "POST",
      dataType: "json",
      data: {
        chartId: CHART_ID,
        seriesType: SERIES_TYPE_FEATURE,
        optionsJson: JSON.stringify(featureStateToList(state))
      }
    });
  }

  function scheduleFeatureSave(state) {
    if (featureSaveTimer) {
      clearTimeout(featureSaveTimer);
    }
    featureSaveTimer = setTimeout(function () {
      saveFeatureOptions(state);
      featureSaveTimer = null;
    }, 150);
  }

  function createMaRow(option, index) {
    var enabled = option.enabledYn === "Y" || option.enabledYn === true;
    var lineWidth = normalizeMaLineWidth(option.lineWidth);
    var color = option.seriesColor || "#000000";
    var $row = $("<div class='ma-row2'/>").attr("data-series-key", option.seriesKey || "");
    var $colorInput = $("<input type='hidden' class='ma-color'/>").val(color);
    var $colorPicker = $("<input type='color' class='ma-color-picker'/>").val(color);
    var $colorButton = $("<button type='button' class='ma-colorbtn' title='색상 선택'><span class='swatch'></span></button>");

    $row.append($("<label class='ma-check' title='사용 여부'><input type='checkbox' class='ma-enabled'><span class='box'></span></label>").find(".ma-enabled").prop("checked", enabled).end());
    $row.append($("<div class='ma-order'/>").text((index + 1) + "번"));
    $row.append($("<input type='text' class='ma-label'/>").val(option.seriesLabel || ""));
    $row.append($("<input type='number' class='ma-period' min='1'/>").val(option.seriesPeriod || ""));
    $row.append($("<input type='number' class='ma-width' min='0.5' max='6' step='0.5'/>").val(lineWidth));
    $row.append($colorInput);
    $colorButton.find(".swatch").css("background", color);
    $colorButton.append($colorPicker);
    $row.append($colorButton);
    $row.append($("<button type='button' class='btn-delete-ma'>삭제</button>").on("click", function () {
      if (window.confirm("해당 MA 라인을 삭제하시겠습니까?")) {
        $row.remove();
        scheduleMaSave();
      }
    }));

    $colorPicker.on("input change", function () {
      var nextColor = $(this).val() || "#000000";
      $colorInput.val(nextColor);
      $colorButton.find(".swatch").css("background", nextColor);
      scheduleMaSave();
    });

    return $row;
  }

  function renderMaOptions(list) {
    var $list = $("#maRowList");
    var sorted = (list || []).slice();

    $list.empty();
    sorted.sort(function (left, right) {
      var leftOrder = left.displayOrder != null ? left.displayOrder : (left.seriesPeriod || 0);
      var rightOrder = right.displayOrder != null ? right.displayOrder : (right.seriesPeriod || 0);
      return leftOrder - rightOrder;
    });

    $.each(sorted, function (index, option) {
      $list.append(createMaRow(option || {}, index));
    });
  }

  function collectMaOptionsFromUi() {
    var list = [];
    var hasInvalid = false;

    $("#maRowList .ma-row2").each(function (index) {
      var $row = $(this);
      var label = $.trim($row.find(".ma-label").val());
      var period = parseInt($row.find(".ma-period").val(), 10);
      var color = $.trim($row.find(".ma-color").val()) || "#000000";
      var lineWidth = normalizeMaLineWidth($row.find(".ma-width").val());
      var seriesKey = $.trim($row.attr("data-series-key") || "");

      if (!label || !period || period <= 0) {
        hasInvalid = true;
        return;
      }

      if (!seriesKey) {
        seriesKey = "ma" + period;
        $row.attr("data-series-key", seriesKey);
      }

      list.push({
        seriesKey: seriesKey,
        seriesLabel: label,
        seriesPeriod: period,
        seriesColor: color,
        lineWidth: lineWidth,
        enabledYn: $row.find(".ma-enabled").is(":checked") ? "Y" : "N",
        displayOrder: index + 1
      });
    });

    return {
      list: list,
      hasInvalid: hasInvalid
    };
  }

  function refreshMaOptions(forceDefault, showPanel, applyToChart) {
    if (!hasOptionApi()) {
      return;
    }

    $.ajax({
      url: window.__URLS.kisItemchartpriceOptionData,
      type: "GET",
      dataType: "json",
      data: {
        chartId: CHART_ID,
        seriesType: SERIES_TYPE_MA,
        forceDefault: forceDefault ? "Y" : "N"
      },
      success: function (res) {
        var list;

        if (!res) {
          window.alert("차트 옵션 응답이 없습니다.");
          return;
        }
        if (res.system_code && String(res.system_code) !== "0000") {
          window.alert("차트 옵션 조회 오류: " + (res.system_msg || ""));
          return;
        }

        list = $.isArray(res.data) ? res.data : [];
        maOptionList = list.slice();
        renderMaOptions(list);
        renderMaLegendFromOptions(list);
        applyPersistedOptions(applyToChart === true);

        if (showPanel === true) {
          openChartOptionsPanel();
        }
      },
      error: function () {
        window.alert("차트 옵션 조회 중 오류가 발생했습니다.");
      }
    });
  }

  function saveMaOptions(closeAfter, isAuto) {
    var result = collectMaOptionsFromUi();
    var list = result.list || [];

    if (result.hasInvalid) {
      if (isAuto !== true) {
        window.alert("MA 라인 입력값이 비어 있습니다. 라벨/기간을 모두 입력해 주세요.");
      }
      return;
    }

    if (isAuto === true && !list.length) {
      return;
    }

    if (isAuto !== true && !list.length && !window.confirm("모든 MA 라인을 삭제하시겠습니까?")) {
      return;
    }

    $.ajax({
      url: window.__URLS.kisItemchartpriceOptionSave,
      type: "POST",
      dataType: "json",
      data: {
        chartId: CHART_ID,
        seriesType: SERIES_TYPE_MA,
        optionsJson: JSON.stringify(list)
      },
      success: function (res) {
        if (res && res.system_code && String(res.system_code) !== "0000") {
          if (isAuto !== true) {
            window.alert("차트 옵션 저장 오류: " + (res.system_msg || ""));
          }
          return;
        }

        maOptionList = list.slice();
        renderMaLegendFromOptions(list);
        applyPersistedOptions(true);

        if (closeAfter === true) {
          closeChartOptionsPanel();
        }
      },
      error: function () {
        if (isAuto !== true) {
          window.alert("차트 옵션 저장 중 오류가 발생했습니다.");
        }
      }
    });
  }

  function scheduleMaSave() {
    if (maSaveTimer) {
      clearTimeout(maSaveTimer);
    }
    maSaveTimer = setTimeout(function () {
      saveMaOptions(false, true);
      maSaveTimer = null;
    }, 300);
  }

  function normalizeCrossPeriods() {
    var shortPeriod = parseInt($("#optCrossShort").val(), 10);
    var longPeriod = parseInt($("#optCrossLong").val(), 10);
    var candidates = [5, 10, 20, 60, 120, 240];
    var shortIndex;
    var longIndex;
    var swap;

    if (!shortPeriod || shortPeriod < 1) {
      shortPeriod = 5;
    }
    if (!longPeriod || longPeriod < 1) {
      longPeriod = 20;
    }

    if (shortPeriod === longPeriod) {
      longIndex = $.inArray(longPeriod, candidates);
      if (longIndex >= 0 && longIndex < candidates.length - 1) {
        longPeriod = candidates[longIndex + 1];
      } else {
        shortIndex = $.inArray(shortPeriod, candidates);
        if (shortIndex > 0) {
          shortPeriod = candidates[shortIndex - 1];
        }
      }
    }

    if (shortPeriod > longPeriod) {
      swap = shortPeriod;
      shortPeriod = longPeriod;
      longPeriod = swap;
    }

    $("#optCrossShort").val(String(shortPeriod));
    $("#optCrossLong").val(String(longPeriod));
    $("#crossPairLabel").text("(" + shortPeriod + "/" + longPeriod + ")");

    return {
      shortPeriod: shortPeriod,
      longPeriod: longPeriod
    };
  }

  function setCrossUiEnabled(enabled) {
    $("#optCrossShort").prop("disabled", !enabled);
    $("#optCrossLong").prop("disabled", !enabled);
    $("#crossPairLabel").css("opacity", enabled ? 0.7 : 0.35);
  }

  function refreshCrossOptions(forceDefault, applyToChart) {
    if (!hasOptionApi()) {
      return;
    }

    $.ajax({
      url: window.__URLS.kisItemchartpriceOptionData,
      type: "GET",
      dataType: "json",
      data: {
        chartId: CHART_ID,
        seriesType: SERIES_TYPE_CROSS,
        forceDefault: forceDefault ? "Y" : "N"
      },
      success: function (res) {
        var normalized;
        var shortPeriod = 5;
        var longPeriod = 20;

        if (!res || (res.system_code && String(res.system_code) !== "0000")) {
          return;
        }

        crossState.crossEnabled = false;
        $.each($.isArray(res.data) ? res.data : [], function (_, item) {
          var key = String(item.seriesKey || "");
          if (key === "crossSignals") {
            crossState.crossEnabled = item.enabledYn === "Y";
          } else if (key === "crossShort") {
            shortPeriod = parseInt(item.seriesPeriod || 5, 10);
          } else if (key === "crossLong") {
            longPeriod = parseInt(item.seriesPeriod || 20, 10);
          }
        });

        $("#optCrossSignals").prop("checked", crossState.crossEnabled);
        $("#optCrossShort").val(String(shortPeriod));
        $("#optCrossLong").val(String(longPeriod));

        normalized = normalizeCrossPeriods();
        crossState.crossShortPeriod = normalized.shortPeriod;
        crossState.crossLongPeriod = normalized.longPeriod;
        setCrossUiEnabled(crossState.crossEnabled);

        applyPersistedOptions(applyToChart === true);
      }
    });
  }

  function saveCrossOptions() {
    if (!hasOptionApi()) {
      return;
    }

    $.ajax({
      url: window.__URLS.kisItemchartpriceOptionSave,
      type: "POST",
      dataType: "json",
      data: {
        chartId: CHART_ID,
        seriesType: SERIES_TYPE_CROSS,
        optionsJson: JSON.stringify([
          { seriesKey: "crossSignals", seriesLabel: "골든/데드 크로스", seriesPeriod: 0, seriesColor: null, enabledYn: crossState.crossEnabled ? "Y" : "N", displayOrder: 1 },
          { seriesKey: "crossShort", seriesLabel: "단기선", seriesPeriod: crossState.crossShortPeriod, seriesColor: null, enabledYn: "Y", displayOrder: 2 },
          { seriesKey: "crossLong", seriesLabel: "장기선", seriesPeriod: crossState.crossLongPeriod, seriesColor: null, enabledYn: "Y", displayOrder: 3 }
        ])
      }
    });
  }

  function scheduleCrossSave() {
    if (crossSaveTimer) {
      clearTimeout(crossSaveTimer);
    }
    crossSaveTimer = setTimeout(function () {
      saveCrossOptions();
      crossSaveTimer = null;
    }, 150);
  }

  function bindEvents() {
    $("#btnChartOptions").on("click", function () {
      refreshMaOptions(false, true, true);
      refreshCrossOptions(false, false);
      refreshFeatureOptions(false, false);
    });

    $("#btnChartOptClose, #chartOptBackdrop").on("click", function () {
      closeChartOptionsPanel();
    });

    $(document).on("keydown.recSignalChartOptions", function (event) {
      if (event.key === "Escape") {
        closeChartOptionsPanel();
      }
    });

    $("#chartOptionsModal").on("show.bs.modal.recSignalChartOptions", function () {
      refreshFeatureOptions(false, false);
      refreshCrossOptions(false, false);
    });

    $("#btnAddMaLine").on("click", function () {
      var nextIndex = $("#maRowList .ma-row2").length;
      $("#maRowList").append(createMaRow({
        enabledYn: "Y",
        seriesLabel: (nextIndex + 1) + "번MA",
        seriesPeriod: 5,
        lineWidth: MA_LINEWIDTH_DEFAULT,
        seriesColor: "#000000"
      }, nextIndex));
    });

    $("#btnMaDefault").on("click", function () {
      if (window.confirm("이동평균선 옵션을 기본값으로 되돌리겠습니까?\n(저장은 '적용' 버튼을 눌러야 반영됩니다.)")) {
        refreshMaOptions(true, true, true);
      }
    });

    $("#btnApplyMaOptions").on("click", function () {
      saveMaOptions(true, false);
    });

    $(document).on("input.recSignalMa change.recSignalMa", "#maRowList .ma-enabled, #maRowList .ma-label, #maRowList .ma-period, #maRowList .ma-width", function () {
      scheduleMaSave();
    });

    $(document).on("change.recSignalFeature", "#optVolume,#optDoubleChart,#optMonthLines,#optHighLow,#optMaSr,input[name='optDoubleChartMode']", function () {
      featureState = readFeatureStateFromUi();
      applyPersistedOptions(true);
      scheduleFeatureSave(featureState);
    });

    $("#optCrossSignals").on("change", function () {
      crossState.crossEnabled = $(this).is(":checked");
      setCrossUiEnabled(crossState.crossEnabled);
      normalizeCrossPeriods();
      crossState.crossShortPeriod = parseInt($("#optCrossShort").val(), 10) || 5;
      crossState.crossLongPeriod = parseInt($("#optCrossLong").val(), 10) || 20;
      applyPersistedOptions(true);
      scheduleCrossSave();
    });

    $("#optCrossShort, #optCrossLong").on("change", function () {
      var normalized = normalizeCrossPeriods();
      crossState.crossShortPeriod = normalized.shortPeriod;
      crossState.crossLongPeriod = normalized.longPeriod;
      applyPersistedOptions(true);
      scheduleCrossSave();
    });
  }

  $(function () {
    installChartOptionGuard();
    bindEvents();
    refreshFeatureOptions(false, true);
    refreshMaOptions(false, false, true);
    refreshCrossOptions(false, true);
  });
})(window, window.jQuery);
