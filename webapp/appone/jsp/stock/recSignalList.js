(function () {
  var config = window.recSignalConfig || {};
  var highstockPromise = null;
  var COUNTRY_KR = "KR";
  var COUNTRY_US = "US";
  var MARKET_FILTER_ALL = "ALL";
  var MARKET_FILTER_KOSPI = "KOSPI";
  var MARKET_FILTER_KOSDAQ = "KOSDAQ";
  var MARKET_FILTER_NASDAQ = "NASDAQ";
  var MARKET_FILTER_DOW = "DOW";
  var MARKET_OPTIONS = {
    KR: [
      { value: MARKET_FILTER_ALL, label: "전체" },
      { value: MARKET_FILTER_KOSPI, label: "코스피" },
      { value: MARKET_FILTER_KOSDAQ, label: "코스닥" }
    ],
    US: [
      { value: MARKET_FILTER_ALL, label: "전체" },
      { value: MARKET_FILTER_NASDAQ, label: "나스닥" },
      { value: MARKET_FILTER_DOW, label: "다우지수" }
    ]
  };
  var state = {
    country: resolveConfiguredCountry(config),
    marketFilter: resolveConfiguredMarketFilter(config),
    indexFilter: "ALL",
    grade: "ALL",
    sort: "rank",
    tableSortKey: "rank",
    tableSortDir: "asc",
    selectedCode: "",
    selectedMktCd: resolveConfiguredCountry(config),
    chartKey: "",
    baseDt: config.baseDt || "",
    priceMin: null,
    priceMax: null,
    allStocks: [],
    detailCache: {},
    batchStatus: null,
    notice: "추천신호 운영 데이터를 조회 중입니다.",
    usingFallback: false
  };
  var scrollCapFrameId = null;
  var MOBILE_SCROLL_BREAKPOINT = 768;
  var VISIBLE_ROW_LIMIT = 10;

  var els = {
    pageNotice: document.getElementById("pageNotice"),
    batchStatusPanel: document.getElementById("batchStatusPanel"),
    countryFilter: document.getElementById("countryFilter"),
    marketFilter: document.getElementById("marketFilter"),
    indexFilter: document.getElementById("indexFilter"),
    priceMin: document.getElementById("priceMin"),
    priceMax: document.getElementById("priceMax"),
    toolbarBaseDateText: document.getElementById("toolbarBaseDateText"),
    summaryRecommended: document.getElementById("summaryRecommended"),
    summaryGradeA: document.getElementById("summaryGradeA"),
    summaryGradeB: document.getElementById("summaryGradeB"),
    summaryGradeC: document.getElementById("summaryGradeC"),
    rankList: document.getElementById("rankList"),
    detailTitle: document.getElementById("detailTitle"),
    detailSubtitle: document.getElementById("detailSubtitle"),
    detailGrade: document.getElementById("detailGrade"),
    detailRecommend: document.getElementById("detailRecommend"),
    detailPageLink: document.getElementById("detailPageLink"),
    metricGrid: document.getElementById("metricGrid"),
    decisionList: document.getElementById("decisionList"),
    maLadder: document.getElementById("maLadder"),
    analysisNoteList: document.getElementById("analysisNoteList"),
    stockCode: document.getElementById("stockCode"),
    fromDate: document.getElementById("fromDate"),
    toDate: document.getElementById("toDate"),
    periodDivCode: document.getElementById("periodDivCode"),
    kisChartContainer: document.getElementById("kisChartContainer"),
    comparisonBody: document.getElementById("comparisonBody"),
    sortFilter: document.getElementById("sortFilter")
  };

  function normalizeCountryValue(value) {
    var upper = String(value || "").toUpperCase();
    if (upper === COUNTRY_US || upper === "NASDAQ" || upper === "NYSE" || upper === MARKET_FILTER_DOW) {
      return COUNTRY_US;
    }
    return COUNTRY_KR;
  }

  function normalizeMarketFilterValue(value) {
    var upper = String(value || "").toUpperCase();
    if (upper === MARKET_FILTER_KOSPI || upper === MARKET_FILTER_KOSDAQ
        || upper === MARKET_FILTER_NASDAQ || upper === MARKET_FILTER_DOW) {
      return upper;
    }
    return MARKET_FILTER_ALL;
  }

  function resolveConfiguredCountry(cfg) {
    var rawMktCd = String(cfg && cfg.mktCd || "");
    var rawMarketFilter = String(cfg && cfg.marketFilter || "");
    var promotedMarketFilter = rawMarketFilter || rawMktCd;

    if (normalizeMarketFilterValue(promotedMarketFilter) !== MARKET_FILTER_ALL) {
      return normalizeCountryValue(promotedMarketFilter);
    }
    return normalizeCountryValue(rawMktCd);
  }

  function resolveConfiguredMarketFilter(cfg) {
    var country = resolveConfiguredCountry(cfg);
    var rawMktCd = String(cfg && cfg.mktCd || "");
    var rawMarketFilter = String(cfg && cfg.marketFilter || "");
    var promoted = normalizeMarketFilterValue(rawMarketFilter || rawMktCd);

    return isValidMarketFilter(country, promoted) ? promoted : MARKET_FILTER_ALL;
  }

  function marketOptions(country) {
    return MARKET_OPTIONS[country] || MARKET_OPTIONS.KR;
  }

  function isValidMarketFilter(country, marketFilter) {
    var options = marketOptions(country);
    var i;

    for (i = 0; i < options.length; i += 1) {
      if (options[i].value === marketFilter) {
        return true;
      }
    }
    return false;
  }

  function currentMarketFilter() {
    return isValidMarketFilter(state.country, state.marketFilter) ? state.marketFilter : MARKET_FILTER_ALL;
  }

  function renderCountryFilterState() {
    Array.prototype.forEach.call(els.countryFilter ? els.countryFilter.querySelectorAll(".filter-chip") : [], function (button) {
      button.classList.toggle("active", button.getAttribute("data-country") === state.country);
    });
  }

  function renderMarketFilterOptions() {
    var options = marketOptions(state.country);

    if (!els.marketFilter) {
      return;
    }

    els.marketFilter.innerHTML = options.map(function (option) {
      var active = option.value === currentMarketFilter() ? " active" : "";
      return '<button type="button" class="filter-chip' + active + '" data-market-filter="' + option.value + '">' + option.label + "</button>";
    }).join("");
  }

  function renderIndexFilterOptions() {
    if (!els.indexFilter) {
      return;
    }

    var indexLabel = state.country === COUNTRY_US ? "S&P500" : "KOSPI200";
    var active = state.indexFilter === "ALL" ? " active" : "";
    var specificActive = state.indexFilter !== "ALL" ? " active" : "";

    els.indexFilter.innerHTML = [
      '<button type="button" class="filter-chip' + active + '" data-index-filter="ALL">전체</button>',
      '<button type="button" class="filter-chip' + specificActive + '" data-index-filter="SPECIFIC">' + indexLabel + '</button>'
    ].join("");
  }

  function loadScriptOnce(url, id) {
    return new Promise(function (resolve, reject) {
      var scripts;
      var i;

      if (!url) {
        resolve();
        return;
      }

      scripts = document.getElementsByTagName("script");
      for (i = 0; i < scripts.length; i += 1) {
        if ((scripts[i].src || "").indexOf(url) >= 0) {
          resolve();
          return;
        }
      }

      if (id && document.getElementById(id)) {
        resolve();
        return;
      }

      var el = document.createElement("script");
      if (id) {
        el.id = id;
      }
      el.src = url;
      el.async = true;
      el.onload = function () {
        resolve();
      };
      el.onerror = function () {
        reject(new Error("Failed to load script: " + url));
      };
      document.head.appendChild(el);
    });
  }

  function ensureHighstockLoaded() {
    var src;
    var ctx;
    var base;
    var highstockUrl;
    var dataUrl;
    var exportingUrl;

    if (typeof window.Highcharts !== "undefined" && window.Highcharts && typeof window.Highcharts.stockChart === "function") {
      return Promise.resolve();
    }

    if (highstockPromise) {
      return highstockPromise;
    }

    src = window.__HIGHCHARTS_SRC || {};
    ctx = window.__CTX_PATH || "";
    if (ctx && ctx.charAt(ctx.length - 1) === "/") {
      ctx = ctx.substring(0, ctx.length - 1);
    }
    base = ctx + "/appone/plugins/Highcharts-Stock-11.1.0/code";
    highstockUrl = src.highstock || (base + "/highstock.js");
    dataUrl = src.data || (base + "/modules/data.js");
    exportingUrl = src.exporting || (base + "/modules/exporting.js");

    highstockPromise = loadScriptOnce(highstockUrl, "hc-highstock")
      .then(function () {
        return loadScriptOnce(dataUrl, "hc-data");
      })
      .then(function () {
        return loadScriptOnce(exportingUrl, "hc-exporting");
      })
      .catch(function (error) {
        highstockPromise = null;
        throw error;
      });

    return highstockPromise;
  }

  function ensureChartRuntimeReady() {
    if (!window.ChartScript || typeof window.ChartScript.loadKisItemchartprice !== "function") {
      return Promise.reject(new Error("ChartScript is not available."));
    }

    if (window.KisDashboardChartRenderer && typeof window.KisDashboardChartRenderer.render === "function") {
      return Promise.resolve();
    }

    return ensureHighstockLoaded();
  }

  function fetchJson(url, params) {
    var query = params ? "?" + new URLSearchParams(params).toString() : "";
    return fetch(url + query, {
      headers: {
        "Accept": "application/json"
      },
      credentials: "same-origin"
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("HTTP " + response.status);
      }
      return response.json();
    });
  }

  function isSuccessResponse(json) {
    var code = json && json.system_code != null ? String(json.system_code) : "";
    return code === "0000" || code === "S";
  }

  function toNumber(value) {
    var num = Number(value);
    return isNaN(num) ? 0 : num;
  }

  function normalizeStock(raw, index) {
    return {
      rank: index + 1,
      code: String(raw.stkCd || raw.stk_cd || raw.code || ""),
      name: String(raw.stkNm || raw.stk_nm || raw.name || ""),
      mktCd: String(raw.mktCd || raw.mkt_cd || raw.marketGroup || "KR"),
      listingMarket: String(raw.listingMarket || raw.listing_market || raw.market || ""),
      indexCd: String(raw.indexCd || raw.index_cd || ""),
      grade: String(raw.recGrade || raw.rec_grade || raw.grade || ""),
      recYn: String(raw.recYn || raw.rec_yn || "N"),
      baseDt: String(raw.baseDt || raw.base_dt || state.baseDt || ""),
      currentPrice: toNumber(raw.curPrice || raw.cur_price || raw.currentPrice),
      monthOpenPrice: toNumber(raw.monOpenPrice || raw.mon_open_price || raw.monthOpenPrice),
      ma5: toNumber(raw.ma5),
      ma20: toNumber(raw.ma20),
      ma60: toNumber(raw.ma60),
      ma120: toNumber(raw.ma120),
      ma240: toNumber(raw.ma240),
      goldenYn: String(raw.goldenYn || raw.golden_yn || "N"),
      monthUpYn: String(raw.monUpYn || raw.mon_up_yn || raw.monthUpYn || "N"),
      trendStrength: toNumber(raw.trendStrength || raw.trend_strength),
      avgTradeValue20: toNumber(raw.avgTrdVal20 || raw.avg_trd_val_20 || raw.avgTradeValue20),
      reason: String(raw.recReason || raw.rec_reason || raw.reason || "")
    };
  }

  function monthChangeRate(stock) {
    if (!stock || !stock.monthOpenPrice) {
      return 0;
    }
    return ((stock.currentPrice - stock.monthOpenPrice) / stock.monthOpenPrice) * 100;
  }

  function filteredStocks() {
    return state.allStocks.filter(function (stock) {
      // 등급 필터
      if (state.grade !== "ALL" && stock.grade !== state.grade) {
        return false;
      }

      // 지수 필터
      if (state.indexFilter !== "ALL") {
        var expectedIndex = state.country === COUNTRY_US ? "SNP500" : "KOSPI200";
        if (stock.indexCd !== expectedIndex) {
          return false;
        }
      }

      // 가격 범위 필터
      var price = stock.currentPrice;
      if (state.priceMin !== null && price < state.priceMin) {
        return false;
      }
      if (state.priceMax !== null && price > state.priceMax) {
        return false;
      }

      return true;
    });
  }

  function rankedStocks() {
    var cloned = filteredStocks().slice();
    cloned.sort(function (a, b) {
      if (state.sort === "monthChange") {
        return monthChangeRate(b) - monthChangeRate(a) || b.trendStrength - a.trendStrength || b.avgTradeValue20 - a.avgTradeValue20 || a.code.localeCompare(b.code);
      }
      if (state.sort === "trend") {
        return b.trendStrength - a.trendStrength || monthChangeRate(b) - monthChangeRate(a) || b.avgTradeValue20 - a.avgTradeValue20 || a.code.localeCompare(b.code);
      }
      if (state.sort === "tradeValue") {
        return b.avgTradeValue20 - a.avgTradeValue20 || monthChangeRate(b) - monthChangeRate(a) || b.trendStrength - a.trendStrength || a.code.localeCompare(b.code);
      }
      return a.rank - b.rank;
    });
    return cloned;
  }

  function defaultTableSortDir(key) {
    if (key === "rank" || key === "name" || key === "grade") {
      return "asc";
    }
    return "desc";
  }

  function tableSortValue(stock, key) {
    var gradeOrder = { A: 1, B: 2, C: 3 };

    if (key === "name") {
      return stock.name || "";
    }
    if (key === "grade") {
      return gradeOrder[stock.grade] || 99;
    }
    if (key === "currentPrice") {
      return stock.currentPrice;
    }
    if (key === "monthOpenPrice") {
      return stock.monthOpenPrice;
    }
    if (key === "monthChange") {
      return monthChangeRate(stock);
    }
    if (key === "ma5") {
      return stock.ma5;
    }
    if (key === "trendStrength") {
      return stock.trendStrength;
    }
    if (key === "avgTradeValue20") {
      return stock.avgTradeValue20;
    }
    return stock.rank;
  }

  function tableSortedStocks() {
    var cloned = filteredStocks().slice();
    var key = state.tableSortKey;
    var direction = state.tableSortDir === "asc" ? 1 : -1;

    cloned.sort(function (a, b) {
      var av = tableSortValue(a, key);
      var bv = tableSortValue(b, key);
      var diff;

      if (typeof av === "string" || typeof bv === "string") {
        diff = String(av).localeCompare(String(bv), "ko-KR");
      } else {
        diff = Number(av || 0) - Number(bv || 0);
      }

      return diff * direction
        || a.rank - b.rank
        || a.code.localeCompare(b.code);
    });

    return cloned;
  }

  function selectedStock() {
    var list = rankedStocks();
    var i;

    for (i = 0; i < list.length; i += 1) {
      if (list[i].code === state.selectedCode && list[i].mktCd === state.selectedMktCd) {
        return list[i];
      }
    }
    return list[0] || state.allStocks[0] || null;
  }

  function setText(element, value) {
    if (element) {
      element.textContent = value;
    }
  }

  function setHtml(element, value) {
    if (element) {
      element.innerHTML = value;
    }
  }

  function setHref(element, value) {
    if (element) {
      element.href = value;
    }
  }

  function escapeHtml(value) {
    return String(value == null ? "" : value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function parseBaseDate(value) {
    var raw = String(value || "").replace(/[^0-9]/g, "");
    var parsed;

    if (raw.length === 8) {
      parsed = new Date(Number(raw.substring(0, 4)), Number(raw.substring(4, 6)) - 1, Number(raw.substring(6, 8)));
      if (!isNaN(parsed.getTime())) {
        return parsed;
      }
    }

    parsed = new Date(value);
    if (!isNaN(parsed.getTime())) {
      return parsed;
    }

    return new Date();
  }

  function formatInputDate(date) {
    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, "0");
    var day = String(date.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
  }

  function buildChartRange(stock) {
    var anchor = parseBaseDate(stock && (stock.baseDt || state.baseDt));
    var fromDate = new Date(anchor.getTime());

    fromDate.setMonth(fromDate.getMonth() - 18);
    fromDate.setDate(1);

    return {
      fromDate: formatInputDate(fromDate),
      toDate: formatInputDate(anchor)
    };
  }

  function formatNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
  }

  function formatCurrency(value, country) {
    var mktCd = country || state.country;
    if (String(mktCd || "").toUpperCase() === COUNTRY_US) {
      var num = Number(value || 0);
      return num.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + "$";
    }
    return formatNumber(value) + "원";
  }

  function formatBillion(value) {
    var billion = Number(value || 0) / 100000000;
    return billion.toLocaleString("ko-KR", { maximumFractionDigits: 0 }) + "억";
  }

  function formatPct(value) {
    var cls = value > 0 ? "positive" : "neutral";
    var sign = value > 0 ? "+" : "";
    return '<span class="' + cls + '">' + sign + value.toFixed(2) + '%</span>';
  }

  function gradeClass(grade) {
    return "grade-" + String((grade || "").toLowerCase());
  }

  function marketLabel(mktCd) {
    return String(mktCd || "").toUpperCase() === "US" ? "미국시장" : "국내시장";
  }

  function formatStockName(stock) {
    var name = stock && stock.name ? stock.name : "";
    if (stock && String(stock.listingMarket || "").toUpperCase() === MARKET_FILTER_KOSDAQ && name.indexOf("#") !== 0) {
      return "#" + name;
    }
    return name;
  }

  function yesNoText(value) {
    return value === "Y" ? "충족" : "미충족";
  }

  function recommendStatusText(stock) {
    return stock && stock.recYn === "Y" ? "추천 대상" : "추천 조건 미충족";
  }

  function formatSignedCurrency(value) {
    var num = Number(value || 0);
    var sign = num > 0 ? "+" : num < 0 ? "-" : "";
    return sign + formatNumber(Math.abs(num)) + "원";
  }

  function buildReasonText(stock) {
    if (!stock) {
      return "-";
    }

    if (stock.recYn !== "Y") {
      var misses = [];
      if (stock.goldenYn !== "Y") {
        misses.push("정배열 조건을 충족하지 못했습니다.");
      }
      if (stock.monthUpYn !== "Y") {
        misses.push("현재가가 월초 시가를 넘지 못했습니다.");
      }
      return misses.length ? misses.join(" ") : "추천 조건을 충족하지 못했습니다.";
    }

    if (stock.grade === "A") {
      return "정배열과 이달 상승을 모두 만족했고, 현재가가 5일 이동평균선 위에 있으며 이달 상승률이 3% 이상입니다.";
    }
    if (stock.grade === "B") {
      return "정배열과 이달 상승을 만족했고, 현재가가 5일 이동평균선 위에 있지만 이달 상승률은 3% 미만입니다.";
    }
    return "정배열과 이달 상승은 만족하지만 현재가가 5일 이동평균선 이하라 단기 탄력은 다소 약합니다.";
  }

  function buildAnalysisNotes(stock) {
    if (!stock) {
      return ["종목을 선택하면 실제 저장 데이터 기준 분석 메모가 표시됩니다."];
    }

    var rate = monthChangeRate(stock);
    var maGap = stock.currentPrice - stock.ma5;
    var notes = [
      marketLabel(stock.mktCd) + " " + formatStockName(stock) + "(" + stock.code + ")의 기준일은 " + (stock.baseDt || state.baseDt || "-") + "입니다.",
      "현재가는 " + formatCurrency(stock.currentPrice) + ", 월초 시가는 " + formatCurrency(stock.monthOpenPrice) + "로 이달 상승률은 " + rate.toFixed(2) + "%입니다.",
      "정배열 조건은 " + yesNoText(stock.goldenYn) + ", 이달 상승 조건은 " + yesNoText(stock.monthUpYn) + " 상태입니다.",
      "현재가는 5일 이동평균선 대비 " + formatSignedCurrency(maGap) + " 수준이며, 20일 평균 거래대금은 " + formatBillion(stock.avgTradeValue20) + "입니다."
    ];

    if (stock.recYn === "Y") {
      notes.splice(2, 0, buildReasonText(stock));
    } else {
      notes.splice(2, 0, "현재 시점에는 추천 대상이 아니며 추가 관찰이 필요한 종목입니다.");
    }

    return notes;
  }

  function renderAnalysisNotes(stock) {
    if (!els.analysisNoteList) {
      return;
    }
    els.analysisNoteList.innerHTML = buildAnalysisNotes(stock).map(function (note) {
      return "<li>" + note + "</li>";
    }).join("");
  }

  function renderNotice() {
    if (!state.notice) {
      els.pageNotice.hidden = true;
      els.pageNotice.textContent = "";
      return;
    }

    els.pageNotice.hidden = false;
    els.pageNotice.textContent = state.notice;
    els.pageNotice.className = "page-notice" + (state.usingFallback ? " fallback" : "");
  }

  function statusCssClass(status) {
    var value = String(status || "").toUpperCase();
    if (value === "RUNNING") {
      return "running";
    }
    if (value === "SUCCESS") {
      return "success";
    }
    if (value === "FAIL") {
      return "fail";
    }
    return "none";
  }

  function statusText(batch) {
    if (!batch) {
      return "실행 이력 없음";
    }
    if (batch.statusLabel) {
      return batch.statusLabel;
    }
    if (String(batch.status || "").toUpperCase() === "RUNNING") {
      return "실행 중";
    }
    if (String(batch.status || "").toUpperCase() === "SUCCESS") {
      return "성공";
    }
    if (String(batch.status || "").toUpperCase() === "FAIL") {
      return "실패";
    }
    return "실행 이력 없음";
  }

  function renderBatchState(css, text, extraClass) {
    var className = "batch-state " + (css || "none");
    if (extraClass) {
      className += " " + extraClass;
    }

    return [
      '<span class="' + className + '">',
      '  <span class="batch-state-dot"></span>',
      '  <span class="batch-state-text">' + escapeHtml(text || "실행 이력 없음") + "</span>",
      "</span>"
    ].join("");
  }

  function batchSummaryState(batchStatus) {
    var primaryCss = statusCssClass(batchStatus && batchStatus.primary ? batchStatus.primary.status : "");
    var retryCss = statusCssClass(batchStatus && batchStatus.retry ? batchStatus.retry.status : "");
    var hasError = batchStatus && String(batchStatus.hasError || "N").toUpperCase() === "Y";

    if (hasError || primaryCss === "fail" || retryCss === "fail") {
      return { css: "fail", text: "에러" };
    }
    if (primaryCss === "running" || retryCss === "running") {
      return { css: "running", text: "주의" };
    }
    if (primaryCss === "success" || retryCss === "success") {
      return { css: "success", text: "성공" };
    }
    return { css: "none", text: "이력 없음" };
  }

  function renderBatchCard(label, batch) {
    var safeBatch = batch || {};
    var status = String(safeBatch.status || "NONE").toUpperCase();
    var css = statusCssClass(status);
    var startAt = safeBatch.startAt || "-";
    var endAt = safeBatch.endAt || (status === "RUNNING" ? "진행중" : "-");
    var totalCnt = toNumber(safeBatch.totalCnt);
    var successCnt = toNumber(safeBatch.successCnt);
    var failCnt = toNumber(safeBatch.failCnt);
    var errorMsg = String(safeBatch.errorMsg || "");
    var hasError = status === "FAIL" && errorMsg.length > 0;

    return [
      '<article class="batch-card">',
      '  <div class="batch-card-top">',
      '    <strong class="batch-card-name">' + escapeHtml(label) + "</strong>",
      "    " + renderBatchState(css, statusText(safeBatch)),
      "  </div>",
      '  <p class="batch-kv">기준일: ' + escapeHtml(safeBatch.baseDt || "-") + "</p>",
      '  <p class="batch-kv">처리건수: total ' + totalCnt + ' / success ' + successCnt + ' / fail ' + failCnt + "</p>",
      '  <p class="batch-kv">실행시간: ' + escapeHtml(startAt) + " ~ " + escapeHtml(endAt) + "</p>",
      hasError ? [
        '  <details class="batch-error">',
        "    <summary>에러 내용</summary>",
        "    <pre>" + escapeHtml(errorMsg) + "</pre>",
        "  </details>"
      ].join("") : "",
      "</article>"
    ].join("");
  }

  function renderBatchStatus() {
    if (!els.batchStatusPanel) {
      return;
    }

    if (!state.batchStatus) {
      els.batchStatusPanel.hidden = true;
      els.batchStatusPanel.innerHTML = "";
      return;
    }

    var baseDt = state.batchStatus.baseDt || state.baseDt || "-";
    var hasError = String(state.batchStatus.hasError || "N").toUpperCase() === "Y";
    var marketGroup = String(state.batchStatus.marketGroup || state.country || COUNTRY_KR).toUpperCase();
    var summaryState = batchSummaryState(state.batchStatus);

    els.batchStatusPanel.hidden = false;
    els.batchStatusPanel.innerHTML = [
      '<details class="batch-status-accordion">',
      '  <summary class="batch-status-head">',
      '    <div class="batch-status-head-main">',
      '      <div class="batch-status-title-row">',
      '        <strong class="batch-status-title">추천 배치 상태</strong>',
      "        " + renderBatchState(summaryState.css, summaryState.text, "batch-status-summary-state"),
      "      </div>",
      '      <span class="batch-status-base">조회 기준일: ' + escapeHtml(baseDt) + "</span>",
      "    </div>",
      '    <span class="batch-status-chevron" aria-hidden="true"></span>',
      "  </summary>",
      '  <div class="batch-status-grid">',
      renderBatchCard(marketGroup + " Primary", state.batchStatus.primary),
      renderBatchCard(marketGroup + " Retry", state.batchStatus.retry),
      "  </div>",
      "</details>"
    ].join("");

    if (hasError) {
      els.batchStatusPanel.classList.add("has-error");
    } else {
      els.batchStatusPanel.classList.remove("has-error");
    }
  }

  function renderSummary() {
    var total = state.allStocks.length;
    var counts = state.allStocks.reduce(function (acc, stock) {
      acc[stock.grade] = (acc[stock.grade] || 0) + 1;
      return acc;
    }, {});

    setText(els.toolbarBaseDateText, "조회 기준일 " + (state.baseDt || "-"));
    els.summaryRecommended.textContent = total;
    els.summaryGradeA.textContent = counts.A || 0;
    els.summaryGradeB.textContent = counts.B || 0;
    els.summaryGradeC.textContent = counts.C || 0;
  }

  function resetChartHeader() {
    var logo = document.getElementById("kisHdrLogo");

    setText(document.getElementById("kisHdrName"), "-");
    setText(document.getElementById("kisHdrCode"), "-");
    setText(document.getElementById("kisHdrMarket"), "-");
    setText(document.getElementById("kisHdrFlag"), "🇰🇷");
    setText(document.getElementById("kisHdrNow"), "-");
    setText(document.getElementById("kisHdrPct"), "-");
    setText(document.getElementById("kisHdrDiff"), "-");
    setText(document.getElementById("kisHdrDt"), "-");
    setText(document.getElementById("kisHdrO"), "-");
    setText(document.getElementById("kisHdrH"), "-");
    setText(document.getElementById("kisHdrL"), "-");
    setText(document.getElementById("kisHdrC"), "-");
    setText(document.getElementById("kisHdrVol"), "거래량 -");
    setHtml(document.getElementById("kisMaLegend"), "");

    if (window.jQuery) {
      window.jQuery("#kisHdrFlag").show();
      window.jQuery(logo).hide();
    } else if (document.getElementById("kisHdrFlag")) {
      document.getElementById("kisHdrFlag").style.display = "";
    }
    if (logo) {
      logo.style.display = "none";
      logo.removeAttribute("src");
    }
  }

  function renderChartEmpty(message) {
    state.chartKey = "";
    resetChartHeader();

    if (els.stockCode) {
      els.stockCode.value = "";
    }
    if (els.fromDate) {
      els.fromDate.value = "";
    }
    if (els.toDate) {
      els.toDate.value = "";
    }
    if (els.periodDivCode) {
      els.periodDivCode.value = "D";
    }
    if (els.kisChartContainer) {
      els.kisChartContainer.innerHTML = '<div class="chart-empty">' + message + "</div>";
    }
  }

  function renderDetailChart(stock) {
    var range;
    var chartKey;
    var requestedChartKey;

    if (!stock || !els.kisChartContainer) {
      renderChartEmpty("차트를 표시할 추천 종목이 없습니다.");
      return;
    }

    range = buildChartRange(stock);
    chartKey = [stock.baseDt || state.baseDt || "", stock.mktCd || "KR", stock.code, range.fromDate, range.toDate].join(":");

    if (els.stockCode) {
      els.stockCode.value = stock.code;
    }
    if (els.fromDate) {
      els.fromDate.value = range.fromDate;
    }
    if (els.toDate) {
      els.toDate.value = range.toDate;
    }
    if (els.periodDivCode) {
      els.periodDivCode.value = "D";
    }

    if (state.chartKey === chartKey) {
      return;
    }

    state.chartKey = chartKey;
    requestedChartKey = chartKey;
    els.kisChartContainer.innerHTML = '<div class="chart-empty loading">차트 데이터를 불러오는 중입니다.</div>';

    ensureChartRuntimeReady()
      .then(function () {
        if (state.chartKey !== requestedChartKey) {
          return;
        }

        if (typeof window.ChartScript.setOptions === "function") {
          window.ChartScript.setOptions({
            volumeEnabled: false,
            doubleChartEnabled: true,
            doubleChartMode: "all",
            monthLinesEnabled: true,
            maSrEnabled: false
          });
        }

        window.ChartScript.loadKisItemchartprice({
          stockCode: stock.code,
          fromDate: range.fromDate,
          toDate: range.toDate,
          periodDivCode: "D",
          orgAdjPrc: "1",
          skipCurrentPrice: true,
          stockMarket: "",
          stockCountryCode: stock.mktCd || "KR"
        });
      })
      .catch(function (error) {
        if (state.chartKey !== requestedChartKey) {
          return;
        }
        if (window.console && typeof window.console.error === "function") {
          window.console.error("[recSignal] chart runtime load failed", error);
        }
        renderChartEmpty("차트 모듈을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
      });
  }

  function renderList() {
    var list = rankedStocks();
    if (!list.length) {
      els.rankList.innerHTML = [
        '<div class="rank-item">',
        '  <strong>표시할 추천 종목이 없습니다.</strong>',
        '  <div class="rank-caption">배치 미실행, 저장 실패, 또는 추천 조건 미충족 상태일 수 있습니다.</div>',
        '</div>'
      ].join("");
      queueScrollCapUpdate();
      return;
    }

    els.rankList.innerHTML = list.map(function (stock, index) {
      var rate = monthChangeRate(stock);
      var maGap = stock.currentPrice - stock.ma5;
      var active = stock.code === state.selectedCode && stock.mktCd === state.selectedMktCd ? "active" : "";
      return [
        '<button type="button" class="rank-item ' + active + '" data-code="' + stock.code + '" data-mkt-cd="' + stock.mktCd + '">',
        '  <div class="rank-top">',
        '    <span class="rank-order">' + (index + 1) + '</span>',
        '    <span class="grade-chip ' + gradeClass(stock.grade) + '">' + (stock.grade || "-") + '</span>',
        '  </div>',
        '  <div class="rank-main">',
        '    <div>',
        '      <div class="rank-name">' + formatStockName(stock) + '</div>',
        '      <div class="rank-code">' + stock.code + ' · 기본 순위 ' + stock.rank + '</div>',
        '    </div>',
        '    <div class="rank-price-box">',
        '      <span class="rank-price-label">현재가</span>',
        '      <strong class="rank-price mono">' + formatCurrency(stock.currentPrice, stock.mktCd) + '</strong>',
        '    </div>',
        '  </div>',
        '  <div class="rank-bottom">',
        '    <span class="rank-caption">월간 ' + formatPct(rate) + '</span>',
        '    <span class="rank-caption">5일선 대비 ' + formatSignedCurrency(maGap) + '</span>',
        '    <span class="rank-caption">추세 ' + stock.trendStrength.toFixed(1) + '</span>',
        '  </div>',
        '</button>'
      ].join("");
    }).join("");

    Array.prototype.forEach.call(els.rankList.querySelectorAll(".rank-item"), function (button) {
      button.addEventListener("click", function () {
        state.selectedCode = this.getAttribute("data-code");
        state.selectedMktCd = this.getAttribute("data-mkt-cd") || "KR";
        loadDetailAndRender();
      });
    });
    queueScrollCapUpdate();
  }

  function renderDetail(stock) {
    if (!stock) {
      renderEmptyDetail();
      return;
    }

    var rate = monthChangeRate(stock);

    setText(els.detailTitle, formatStockName(stock));
    setText(els.detailSubtitle, marketLabel(stock.mktCd) + " · " + stock.code + " · 기준일 " + (stock.baseDt || state.baseDt || "-"));
    if (els.detailGrade) {
      els.detailGrade.className = "grade-chip " + gradeClass(stock.grade);
      els.detailGrade.textContent = (stock.grade || "-") + "등급";
    }
    setText(els.detailRecommend, recommendStatusText(stock));
    setHref(els.detailPageLink, config.detailViewUrl
      + "?baseDt=" + encodeURIComponent(stock.baseDt || state.baseDt || "")
      + "&stkCd=" + encodeURIComponent(stock.code)
      + "&mktCd=" + encodeURIComponent(stock.mktCd || "KR"));

    var metrics = [
      { label: "현재가", value: formatCurrency(stock.currentPrice, stock.mktCd) },
      { label: "월초 시가", value: formatCurrency(stock.monthOpenPrice, stock.mktCd) },
      { label: "5일 평균가", value: formatCurrency(stock.ma5, stock.mktCd) },
      { label: "20일 평균가", value: formatCurrency(stock.ma20, stock.mktCd) },
      { label: "60일 평균가", value: formatCurrency(stock.ma60, stock.mktCd) },
      { label: "정배열", value: yesNoText(stock.goldenYn) },
      { label: "추천 사유", value: buildReasonText(stock), wide: true },
      { label: "5일선 대비", value: formatSignedCurrency(stock.currentPrice - stock.ma5) },
      { label: "월간 상승률", value: rate.toFixed(2) + "%" },
      { label: "추세 강도", value: stock.trendStrength.toFixed(1) },
      { label: "평균 거래대금(20)", value: formatBillion(stock.avgTradeValue20) }
    ];

    setHtml(els.metricGrid, metrics.map(function (metric) {
      var valueMarkup = metric.wide
        ? '<p class="metric-copy">' + metric.value + '</p>'
        : '<strong class="mono">' + metric.value + '</strong>';
      return [
        '<article class="metric-card' + (metric.wide ? " metric-card-wide" : "") + '">',
        '  <span class="metric-label">' + metric.label + '</span>',
        "  " + valueMarkup,
        '</article>'
      ].join("");
    }).join(""));

    renderDecision(stock);
    renderMaLadder(stock);
    renderAnalysisNotes(stock);
    renderDetailChart(stock);
  }

  function renderEmptyDetail() {
    setText(els.detailTitle, "운영 데이터 없음");
    setText(els.detailSubtitle, "추천신호 결과가 저장되지 않았거나 추천 종목이 없습니다.");
    if (els.detailGrade) {
      els.detailGrade.className = "grade-chip";
      els.detailGrade.textContent = "-";
    }
    setText(els.detailRecommend, "추천 상태 없음");
    setHref(els.detailPageLink, "#");
    setHtml(els.metricGrid, [
      '<article class="metric-card"><span class="metric-label">상태</span><strong class="mono">데이터 없음</strong></article>',
      '<article class="metric-card"><span class="metric-label">확인 대상</span><strong class="mono">배치 / 저장 데이터</strong></article>'
    ].join(""));
    if (els.decisionList) {
      setHtml(els.decisionList, [
        '<article class="decision-item fail">',
        '  <div class="decision-copy">',
        '    <strong>추천 결과 없음</strong>',
        '    <p>배치 실패, 저장 실패, 또는 추천 조건 미충족으로 인해 표시할 스냅샷이 없습니다.</p>',
        '  </div>',
        '  <span class="pill grade-a">데이터 없음</span>',
        '</article>'
      ].join(""));
    }
    if (els.maLadder) {
      els.maLadder.innerHTML = [
        '<div class="ma-row">',
        '  <div>',
        '    <div class="ma-label">상태</div>',
        '    <strong class="mono">MA 정보 없음</strong>',
        '  </div>',
        '  <div class="ma-bar"><span class="ma-fill" style="width:0%;"></span></div>',
        '</div>'
      ].join("");
    }
    renderAnalysisNotes(null);
    renderChartEmpty("추천 종목을 선택하면 KIS 차트가 표시됩니다.");
  }

  function renderDecision(stock) {
    var currentAboveMa5 = stock.currentPrice > stock.ma5;
    var items = [
      {
        title: "1. 정배열 조건",
        note: "5일선이 20일선, 60일선, 120일선, 240일선보다 모두 높아야 합니다.",
        result: stock.goldenYn === "Y" ? "충족" : "미충족",
        klass: stock.goldenYn === "Y" ? "pass" : "fail"
      },
      {
        title: "2. 해당월 상승 조건",
        note: "현재가가 월초 시가보다 높아야 합니다.",
        result: stock.monthUpYn === "Y" ? "충족" : "미충족",
        klass: stock.monthUpYn === "Y" ? "pass" : "fail"
      },
      {
        title: "3. 5일선 위치 판단",
        note: currentAboveMa5 ? "현재가가 5일 이동평균선 위에 있습니다." : "현재가가 5일 이동평균선 아래에 있습니다.",
        result: currentAboveMa5 ? "상회" : "하회",
        klass: currentAboveMa5 ? "pass" : "watch"
      },
      {
        title: "4. 등급 확정",
        note: stock.recYn !== "Y"
          ? "추천 조건을 충족하지 않아 등급이 부여되지 않았습니다."
          : stock.grade === "A"
          ? "이달 상승률이 3% 이상이라 A등급입니다."
          : stock.grade === "B"
          ? "이달 상승률이 0% 초과 3% 미만이라 B등급입니다."
          : "추천 대상이지만 현재가가 5일선 이하라 C등급입니다.",
        result: stock.recYn === "Y" ? (stock.grade || "-") + " 등급" : "등급 없음",
        klass: stock.recYn !== "Y" ? "fail" : stock.grade === "A" ? "pass" : "watch"
      }
    ];

    if (!els.decisionList) {
      return;
    }

    els.decisionList.innerHTML = items.map(function (item) {
      return [
        '<article class="decision-item ' + item.klass + '">',
        '  <div class="decision-copy">',
        '    <strong>' + item.title + '</strong>',
        '    <p>' + item.note + '</p>',
        '  </div>',
        '  <span class="pill ' + (item.klass === "pass" ? "grade-c" : item.klass === "watch" ? "grade-b" : "grade-a") + '">' + item.result + '</span>',
        '</article>'
      ].join("");
    }).join("");
  }

  function renderMaLadder(stock) {
    if (!els.maLadder) {
      return;
    }
    var items = [
      { label: "5일 평균가", value: stock.ma5 },
      { label: "20일 평균가", value: stock.ma20 },
      { label: "60일 평균가", value: stock.ma60 },
      { label: "120일 평균가", value: stock.ma120 },
      { label: "240일 평균가", value: stock.ma240 }
    ];
    var max = Math.max.apply(null, items.map(function (item) { return item.value; }));

    els.maLadder.innerHTML = items.map(function (item) {
      var pct = max ? (item.value / max) * 100 : 0;
      return [
        '<div class="ma-row">',
        '  <div>',
        '    <div class="ma-label">' + item.label + '</div>',
        '    <strong class="mono">' + formatCurrency(item.value, stock.mktCd) + '</strong>',
        '  </div>',
        '  <div class="ma-bar"><span class="ma-fill" style="width:' + pct.toFixed(2) + '%;"></span></div>',
        '</div>'
      ].join("");
    }).join("");
  }

  function renderTable() {
    var list = tableSortedStocks();
    if (!list.length) {
      els.comparisonBody.innerHTML = '<tr><td colspan="9" class="mono">추천 결과가 없습니다.</td></tr>';
      queueScrollCapUpdate();
      return;
    }
    els.comparisonBody.innerHTML = list.map(function (stock) {
      var rate = monthChangeRate(stock);
      var detailUrl = config.detailViewUrl
        + "?baseDt=" + encodeURIComponent(stock.baseDt || state.baseDt || "")
        + "&stkCd=" + encodeURIComponent(stock.code)
        + "&mktCd=" + encodeURIComponent(stock.mktCd || "KR");
      var active = stock.code === state.selectedCode && stock.mktCd === state.selectedMktCd ? ' class="table-row-selected"' : "";
      return [
        '<tr' + active + '>',
        '  <td class="mono">' + stock.rank + '</td>',
        '  <td><a class="detail-link inline" href="' + detailUrl + '"><strong>' + formatStockName(stock) + '</strong></a><div class="rank-code">' + stock.code + '</div></td>',
        '  <td><span class="grade-chip ' + gradeClass(stock.grade) + '">' + (stock.grade || "-") + '</span></td>',
        '  <td class="mono">' + formatCurrency(stock.currentPrice, stock.mktCd) + '</td>',
        '  <td class="mono">' + formatCurrency(stock.monthOpenPrice, stock.mktCd) + '</td>',
        '  <td class="mono">' + rate.toFixed(2) + '%</td>',
        '  <td class="mono">' + formatCurrency(stock.ma5, stock.mktCd) + '</td>',
        '  <td class="mono">' + stock.trendStrength.toFixed(1) + '</td>',
        '  <td class="mono">' + formatBillion(stock.avgTradeValue20) + '</td>',
        '</tr>'
      ].join("");
    }).join("");
    queueScrollCapUpdate();
  }

  function renderTableSortState() {
    Array.prototype.forEach.call(document.querySelectorAll(".table-sort"), function (button) {
      var key = button.getAttribute("data-sort-key");
      var active = key === state.tableSortKey;
      var sortValue = "none";

      button.classList.toggle("active", active);
      button.classList.toggle("asc", active && state.tableSortDir === "asc");
      button.classList.toggle("desc", active && state.tableSortDir === "desc");
      button.setAttribute("aria-pressed", active ? "true" : "false");

      if (active) {
        sortValue = state.tableSortDir === "asc" ? "ascending" : "descending";
      }
      if (button.parentElement) {
        button.parentElement.setAttribute("aria-sort", sortValue);
      }
    });
  }

  function isMobileViewport() {
    if (window.matchMedia) {
      return window.matchMedia("(max-width: " + MOBILE_SCROLL_BREAKPOINT + "px)").matches;
    }
    return window.innerWidth <= MOBILE_SCROLL_BREAKPOINT;
  }

  function resetScrollCap(element, extraClass) {
    if (!element) {
      return;
    }
    element.classList.remove("mobile-scroll-cap");
    if (extraClass) {
      element.classList.remove(extraClass);
    }
    element.style.maxHeight = "";
  }

  function measureVisibleHeight(elements, limit) {
    var list = Array.prototype.slice.call(elements || []).filter(function (element) {
      return element && element.getBoundingClientRect().height > 0;
    });
    var lastIndex;
    var firstRect;
    var lastRect;

    if (!list.length) {
      return 0;
    }

    lastIndex = Math.min(limit, list.length) - 1;
    firstRect = list[0].getBoundingClientRect();
    lastRect = list[lastIndex].getBoundingClientRect();

    return Math.max(0, Math.ceil(lastRect.bottom - firstRect.top));
  }

  function applyRankListScrollCap() {
    var items;
    var visibleHeight;

    if (!els.rankList) {
      return;
    }

    items = els.rankList.querySelectorAll(".rank-item[data-code]");
    if (!isMobileViewport() || items.length <= VISIBLE_ROW_LIMIT) {
      resetScrollCap(els.rankList);
      return;
    }

    visibleHeight = measureVisibleHeight(items, VISIBLE_ROW_LIMIT);
    if (!visibleHeight) {
      resetScrollCap(els.rankList);
      return;
    }

    els.rankList.classList.add("mobile-scroll-cap");
    els.rankList.style.maxHeight = visibleHeight + "px";
  }

  function applyTableScrollCap() {
    var wrapper = document.querySelector(".table-wrap");
    var table;
    var head;
    var rows;
    var headHeight;
    var bodyHeight;

    if (!wrapper || !els.comparisonBody) {
      return;
    }

    table = wrapper.querySelector("table");
    head = table ? table.querySelector("thead") : null;
    rows = els.comparisonBody.querySelectorAll("tr");

    if (rows.length <= VISIBLE_ROW_LIMIT) {
      resetScrollCap(wrapper, "table-scroll-cap");
      return;
    }

    headHeight = head ? Math.ceil(head.getBoundingClientRect().height) : 0;
    bodyHeight = measureVisibleHeight(rows, VISIBLE_ROW_LIMIT);

    if (!bodyHeight) {
      resetScrollCap(wrapper, "table-scroll-cap");
      return;
    }

    wrapper.classList.add("table-scroll-cap");
    wrapper.style.maxHeight = (headHeight + bodyHeight + 2) + "px";
  }

  function updateMobileScrollCaps() {
    applyRankListScrollCap();
    applyTableScrollCap();
  }

  function queueScrollCapUpdate() {
    if (scrollCapFrameId && window.cancelAnimationFrame) {
      window.cancelAnimationFrame(scrollCapFrameId);
    }

    if (window.requestAnimationFrame) {
      scrollCapFrameId = window.requestAnimationFrame(function () {
        scrollCapFrameId = null;
        updateMobileScrollCaps();
      });
      return;
    }

    updateMobileScrollCaps();
  }

  function ensureSelection() {
    var list = rankedStocks();
    var exists = false;
    var i;

    if (!list.length) {
      state.selectedCode = "";
      state.selectedMktCd = state.country || COUNTRY_KR;
      return;
    }

    for (i = 0; i < list.length; i += 1) {
      if (list[i].code === state.selectedCode && list[i].mktCd === state.selectedMktCd) {
        exists = true;
        break;
      }
    }

    if (!exists) {
      state.selectedCode = list[0].code;
      state.selectedMktCd = list[0].mktCd;
    }
  }

  function bindControls() {
    Array.prototype.forEach.call(document.querySelectorAll("#gradeFilter .filter-chip"), function (button) {
      button.addEventListener("click", function () {
        state.grade = this.getAttribute("data-grade");
        Array.prototype.forEach.call(document.querySelectorAll("#gradeFilter .filter-chip"), function (chip) {
          chip.classList.toggle("active", chip === button);
        });
        ensureSelection();
        loadDetailAndRender();
      });
    });

    if (els.countryFilter) {
      els.countryFilter.addEventListener("click", function (event) {
        var button = event.target;
        var nextCountry;

        if (!button || button.tagName !== "BUTTON") {
          return;
        }

        nextCountry = button.getAttribute("data-country");
        if (!nextCountry || nextCountry === state.country) {
          return;
        }

        state.country = normalizeCountryValue(nextCountry);
        state.marketFilter = MARKET_FILTER_ALL;
        state.indexFilter = "ALL";
        state.selectedCode = "";
        state.selectedMktCd = state.country;
        state.detailCache = {};
        state.allStocks = [];
        state.baseDt = config.baseDt || "";
        state.batchStatus = null;
        renderCountryFilterState();
        renderMarketFilterOptions();
        renderIndexFilterOptions();
        renderAll();
        loadList();
      });
    }

    if (els.marketFilter) {
      els.marketFilter.addEventListener("click", function (event) {
        var button = event.target;
        var nextMarketFilter;

        if (!button || button.tagName !== "BUTTON") {
          return;
        }

        nextMarketFilter = normalizeMarketFilterValue(button.getAttribute("data-market-filter"));
        if (nextMarketFilter === currentMarketFilter()) {
          return;
        }

        state.marketFilter = isValidMarketFilter(state.country, nextMarketFilter) ? nextMarketFilter : MARKET_FILTER_ALL;
        renderMarketFilterOptions();
        loadList();
      });
    }

    if (els.indexFilter) {
      els.indexFilter.addEventListener("click", function (event) {
        var button = event.target;
        var nextIndexFilter;

        if (!button || button.tagName !== "BUTTON") {
          return;
        }

        nextIndexFilter = button.getAttribute("data-index-filter");
        if (!nextIndexFilter || nextIndexFilter === state.indexFilter) {
          return;
        }

        state.indexFilter = nextIndexFilter;
        renderIndexFilterOptions();
        ensureSelection();
        loadDetailAndRender();
      });
    }

    if (els.priceMin || els.priceMax) {
      var updatePriceFilter = function () {
        state.priceMin = els.priceMin && els.priceMin.value ? Number(els.priceMin.value) : null;
        state.priceMax = els.priceMax && els.priceMax.value ? Number(els.priceMax.value) : null;
        ensureSelection();
        loadDetailAndRender();
      };
      if (els.priceMin) {
        els.priceMin.addEventListener("change", updatePriceFilter);
      }
      if (els.priceMax) {
        els.priceMax.addEventListener("change", updatePriceFilter);
      }
    }

    els.sortFilter.addEventListener("change", function () {
      state.sort = this.value;
      ensureSelection();
      loadDetailAndRender();
    });

    Array.prototype.forEach.call(document.querySelectorAll(".table-sort"), function (button) {
      button.addEventListener("click", function () {
        var key = this.getAttribute("data-sort-key") || "rank";
        if (state.tableSortKey === key) {
          state.tableSortDir = state.tableSortDir === "asc" ? "desc" : "asc";
        } else {
          state.tableSortKey = key;
          state.tableSortDir = defaultTableSortDir(key);
        }
        renderTableSortState();
        renderTable();
      });
    });

    window.addEventListener("resize", queueScrollCapUpdate);
  }

  function renderAll() {
    renderNotice();
    renderBatchStatus();
    renderSummary();
    renderCountryFilterState();
    renderMarketFilterOptions();
    renderIndexFilterOptions();
    renderList();
    renderDetail(selectedStock());
    renderTableSortState();
    renderTable();
  }

  function mergeDetail(detail) {
    var i;
    for (i = 0; i < state.allStocks.length; i += 1) {
      if (state.allStocks[i].code === detail.code && state.allStocks[i].mktCd === detail.mktCd) {
        detail.rank = state.allStocks[i].rank;
        state.allStocks[i] = detail;
        return;
      }
    }
  }

  function loadDetailAndRender() {
    var stock = selectedStock();
    var cacheKey;

    if (!stock) {
      renderAll();
      return;
    }

    cacheKey = (stock.baseDt || state.baseDt || "") + ":" + currentMarketFilter() + ":" + stock.mktCd + ":" + stock.code;

    if (state.detailCache[cacheKey]) {
      mergeDetail(state.detailCache[cacheKey]);
      renderAll();
      return;
    }

    if (!config.detailUrl) {
      renderAll();
      return;
    }

    fetchJson(config.detailUrl, { baseDt: stock.baseDt || state.baseDt, stkCd: stock.code, mktCd: stock.mktCd }).then(function (json) {
      if (isSuccessResponse(json) && json.singleData) {
        var detail = normalizeStock(json.singleData, stock.rank - 1);
        state.detailCache[cacheKey] = detail;
        mergeDetail(detail);
      }
      renderAll();
    }).catch(function () {
      renderAll();
    });
  }

  function loadBatchStatus(baseDt) {
    if (!config.batchStatusUrl) {
      return Promise.resolve();
    }

    var params = {};
    params.mktCd = state.country;
    if (baseDt) {
      params.baseDt = baseDt;
    }

    return fetchJson(config.batchStatusUrl, params).then(function (json) {
      if (isSuccessResponse(json) && json.singleData) {
        state.batchStatus = json.singleData;
      } else {
        state.batchStatus = null;
      }
      renderBatchStatus();
    }).catch(function () {
      state.batchStatus = null;
      renderBatchStatus();
    });
  }

  function loadList() {
    if (!config.listUrl) {
      renderAll();
      return;
    }

    var baseParams = {};
    if (config.baseDt) {
      baseParams.baseDt = config.baseDt;
    }
    baseParams.mktCd = state.country;
    if (currentMarketFilter() !== MARKET_FILTER_ALL) {
      baseParams.marketFilter = currentMarketFilter();
    }

    function rowsOf(json) {
      if (!json) return [];
      if (Array.isArray(json.data)) return json.data;
      if (json.data && Array.isArray(json.data.data)) return json.data.data;
      return [];
    }

    function applyRows(rows, fallbackMode) {
      state.allStocks = rows.map(normalizeStock);
      state.baseDt = state.allStocks[0] ? state.allStocks[0].baseDt : state.baseDt;
      state.selectedCode = state.allStocks[0] ? state.allStocks[0].code : state.selectedCode;
      state.selectedMktCd = state.allStocks[0] ? state.allStocks[0].mktCd : state.selectedMktCd;
      if (fallbackMode) {
        state.notice = "추천 조건 충족 종목이 없어 " + (state.baseDt || "기준일 미확인") + " 기준 전체 스냅샷 " + state.allStocks.length + "건을 표시합니다.";
      } else {
        state.notice = (state.baseDt || "기준일 미확인") + " 기준 추천 종목 " + state.allStocks.length + "건을 표시합니다.";
      }
      state.usingFallback = !!fallbackMode;
      renderCountryFilterState();
      renderMarketFilterOptions();
      loadBatchStatus(state.baseDt);
      renderAll();
      loadDetailAndRender();
    }

    var recommendedParams = Object.assign({}, baseParams, { recYn: "Y" });
    fetchJson(config.listUrl, recommendedParams).then(function (json) {
      if (isSuccessResponse(json)) {
        var recommendedRows = rowsOf(json);
        if (recommendedRows.length) {
          applyRows(recommendedRows, false);
          return;
        }

        var nonRecommendedParams = Object.assign({}, baseParams, { recYn: "N" });
        fetchJson(config.listUrl, nonRecommendedParams).then(function (allJson) {
          if (isSuccessResponse(allJson)) {
            var allRows = rowsOf(allJson);
            if (allRows.length) {
              applyRows(allRows, true);
              return;
            }
          }

          state.allStocks = [];
          state.detailCache = {};
          state.notice = "추천신호 운영 데이터가 없습니다. 배치 실행 결과와 TB_REC_SIGNAL 저장 상태를 확인하세요.";
          state.usingFallback = false;
          state.selectedCode = "";
          state.selectedMktCd = state.country;
          renderCountryFilterState();
          renderMarketFilterOptions();
          loadBatchStatus(config.baseDt || state.baseDt);
          renderAll();
        }).catch(function () {
          state.allStocks = [];
          state.detailCache = {};
          state.notice = "추천신호 조회 API 호출에 실패했습니다. 서버 로그와 배치 결과를 확인하세요.";
          state.usingFallback = true;
          state.selectedCode = "";
          state.selectedMktCd = state.country;
          renderCountryFilterState();
          renderMarketFilterOptions();
          loadBatchStatus(config.baseDt || state.baseDt);
          renderAll();
        });
        return;
      }

      state.allStocks = [];
      state.detailCache = {};
      state.notice = "추천신호 운영 데이터가 없습니다. 배치 실행 결과와 TB_REC_SIGNAL 저장 상태를 확인하세요.";
      state.usingFallback = false;
      state.selectedCode = "";
      state.selectedMktCd = state.country;
      renderCountryFilterState();
      renderMarketFilterOptions();
      loadBatchStatus(config.baseDt || state.baseDt);
      renderAll();
    }).catch(function () {
      state.allStocks = [];
      state.detailCache = {};
      state.notice = "추천신호 조회 API 호출에 실패했습니다. 서버 로그와 배치 결과를 확인하세요.";
      state.usingFallback = true;
      state.selectedCode = "";
      state.selectedMktCd = state.country;
      renderCountryFilterState();
      renderMarketFilterOptions();
      loadBatchStatus(config.baseDt || state.baseDt);
      renderAll();
    });
  }

  renderCountryFilterState();
  renderMarketFilterOptions();
  renderIndexFilterOptions();
  bindControls();
  loadBatchStatus(config.baseDt || state.baseDt);
  loadList();
})();
