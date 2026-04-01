(function () {
  "use strict";

  var urls = window.__URLS || {};
  var COUNTRY_KR = "KR";
  var COUNTRY_US = "US";
  var WATCH_GROUP_DIV_RECOMMEND = "recommend";
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
    country: resolveInitialCountry(),
    marketFilter: MARKET_FILTER_ALL,
    grade: "ALL",
    sort: "rank",
    cache: {},
    allStocks: [],
    baseDt: "",
    usingFallback: false,
    selectedCode: "",
    selectedMarket: resolveInitialCountry(),
    detailCache: {},
    detailStock: null,
    detailRequestSeq: 0,
    detailTrigger: null,
    savedWatchKeys: {},
    saveBusyKeys: {}
  };
  var detailRefs = null;

  window.refreshRecSignalPanel = function (forceReload) {
    syncCountryFromWatchlist();
    fetchList(!!forceReload);
  };

  function $(id) {
    return document.getElementById(id);
  }

  function safeStr(value) {
    if (value === null || value === undefined) {
      return "";
    }
    return String(value);
  }

  function toNumber(value) {
    var num = Number(value);
    return isNaN(num) ? 0 : num;
  }

  function escapeHtml(value) {
    return safeStr(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function isSuccessResponse(json) {
    var code = json && json.system_code != null ? String(json.system_code) : "";
    return code === "0000" || code === "S";
  }

  function extractList(json) {
    if (!json) {
      return [];
    }
    if (Array.isArray(json.data)) {
      return json.data;
    }
    if (json.data && Array.isArray(json.data.data)) {
      return json.data.data;
    }
    return [];
  }

  function extractSingleData(json) {
    if (!json) {
      return null;
    }
    if (json.singleData) {
      return json.singleData;
    }
    if (json.data && json.data.singleData) {
      return json.data.singleData;
    }
    return null;
  }

  function fetchJson(url, params) {
    var query = params ? "?" + new URLSearchParams(params).toString() : "";
    return fetch(url + query, {
      headers: {
        Accept: "application/json"
      },
      credentials: "same-origin"
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("HTTP " + response.status);
      }
      return response.json();
    });
  }

  function normalizeStock(raw, index) {
    return {
      rank: index + 1,
      code: safeStr(raw.stkCd || raw.stk_cd || raw.code),
      name: safeStr(raw.stkNm || raw.stk_nm || raw.name),
      market: safeStr(raw.mktCd || raw.mkt_cd || state.country || COUNTRY_KR).toUpperCase(),
      listingMarket: safeStr(raw.listingMarket || raw.listing_market || raw.market),
      baseDt: safeStr(raw.baseDt || raw.base_dt),
      grade: safeStr(raw.recGrade || raw.rec_grade),
      currentPrice: toNumber(raw.curPrice || raw.cur_price),
      monthOpenPrice: toNumber(raw.monOpenPrice || raw.mon_open_price),
      monthChangeRate: toNumber(raw.monChgRate || raw.mon_chg_rate),
      ma5: toNumber(raw.ma5),
      trendStrength: toNumber(raw.trendStrength || raw.trend_strength),
      avgTradeValue20: toNumber(raw.avgTrdVal20 || raw.avg_trd_val_20),
      recReason: safeStr(raw.recReason || raw.rec_reason)
    };
  }

  function resolveCountry(value) {
    return safeStr(value).toUpperCase() === COUNTRY_US ? COUNTRY_US : COUNTRY_KR;
  }

  function resolveInitialCountry() {
    var wlMarket = $("wlMarket");
    if (wlMarket && safeStr(wlMarket.value).toUpperCase() === "A") {
      return COUNTRY_US;
    }
    return COUNTRY_KR;
  }

  function syncCountryFromWatchlist() {
    var nextCountry = resolveInitialCountry();

    if (nextCountry === state.country) {
      return false;
    }

    state.country = nextCountry;
    state.marketFilter = MARKET_FILTER_ALL;
    state.selectedCode = "";
    state.selectedMarket = nextCountry;
    renderCountryFilterState();
    renderMarketFilterOptions();
    return true;
  }

  function normalizeMarketFilterValue(value) {
    var upper = safeStr(value).toUpperCase();
    if (upper === MARKET_FILTER_KOSPI || upper === MARKET_FILTER_KOSDAQ
        || upper === MARKET_FILTER_NASDAQ || upper === MARKET_FILTER_DOW) {
      return upper;
    }
    return MARKET_FILTER_ALL;
  }

  function isValidMarketFilter(country, marketFilter) {
    var options = MARKET_OPTIONS[country] || MARKET_OPTIONS.KR;
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

  function currentKeyword() {
    var input = $("signalKeyword");
    return input ? safeStr(input.value).trim().toUpperCase() : "";
  }

  function monthChangeRate(stock) {
    if (!stock) {
      return 0;
    }
    if (stock.monthChangeRate) {
      return stock.monthChangeRate;
    }
    if (!stock.monthOpenPrice) {
      return 0;
    }
    return ((stock.currentPrice - stock.monthOpenPrice) / stock.monthOpenPrice) * 100;
  }

  function formatCurrencyValue(value, market, dashWhenZero) {
    var number = Number(value);

    if (!isFinite(number)) {
      return "-";
    }
    if (!number && dashWhenZero !== false) {
      return "-";
    }
    if (safeStr(market).toUpperCase() === COUNTRY_US) {
      return "$" + number.toLocaleString("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    }
    return number.toLocaleString("ko-KR") + "원";
  }

  function formatPrice(value, market) {
    return formatCurrencyValue(value, market, true);
  }

  function formatCurrency(value, market) {
    return formatCurrencyValue(value, market, false);
  }

  function formatTradeValue(value, market) {
    var number = Number(value);

    if (!isFinite(number) || !number) {
      return "-";
    }

    if (safeStr(market).toUpperCase() === COUNTRY_US) {
      return "$" + number.toLocaleString("en-US", {
        maximumFractionDigits: 0
      });
    }

    return (number / 100000000).toLocaleString("ko-KR", {
      maximumFractionDigits: 0
    }) + "억";
  }

  function formatSignedCurrency(value, market) {
    var number = Number(value);
    var sign = "";

    if (!isFinite(number)) {
      return "-";
    }

    if (number > 0) {
      sign = "+";
    } else if (number < 0) {
      sign = "-";
    }

    return sign + formatCurrency(Math.abs(number), market);
  }

  function formatSignedRateHtml(value) {
    var number = Number(value);
    var cls = "is-neutral";
    var sign = "";

    if (!isFinite(number)) {
      return '<span class="signal-rec-detail-change is-neutral">0.00%</span>';
    }

    if (number > 0) {
      cls = "is-positive";
      sign = "+";
    } else if (number < 0) {
      cls = "is-negative";
    }

    return '<span class="signal-rec-detail-change ' + cls + '">' + sign + number.toFixed(2) + "%</span>";
  }

  function gradeClass(grade) {
    var normalized = safeStr(grade).toUpperCase();
    if (normalized === "A" || normalized === "B" || normalized === "C") {
      return "grade-" + normalized.toLowerCase();
    }
    return "";
  }

  function formatStockName(stock) {
    var name = safeStr(stock && stock.name);
    if (safeStr(stock && stock.listingMarket).toUpperCase() === MARKET_FILTER_KOSDAQ && name.indexOf("#") !== 0) {
      return "#" + name;
    }
    return name;
  }

  function marketLabel(market) {
    return resolveCountry(market) === COUNTRY_US ? "미국시장" : "국내시장";
  }

  function yesNoText(value) {
    return safeStr(value).toUpperCase() === "Y" ? "충족" : "미충족";
  }

  function recommendStatusText(stock) {
    return stock && safeStr(stock.recYn).toUpperCase() === "Y" ? "추천 대상" : "관찰 대상";
  }

  function buildReasonText(stock) {
    var misses = [];

    if (!stock) {
      return "상세 추천 이유를 확인할 수 없습니다.";
    }

    if (safeStr(stock.recReason)) {
      return stock.recReason;
    }

    if (safeStr(stock.recYn).toUpperCase() !== "Y") {
      if (safeStr(stock.goldenYn).toUpperCase() !== "Y") {
        misses.push("정배열 조건을 충족하지 못했습니다.");
      }
      if (safeStr(stock.monthUpYn).toUpperCase() !== "Y") {
        misses.push("현재가가 월초 시가를 넘지 못했습니다.");
      }
      return misses.length ? misses.join(" ") : "추천 조건을 충족하지 못했습니다.";
    }

    if (safeStr(stock.grade).toUpperCase() === "A") {
      return "정배열과 이달 상승을 모두 만족했고 현재가가 5일 이동평균선 위에 있어 우선 관찰 대상입니다.";
    }
    if (safeStr(stock.grade).toUpperCase() === "B") {
      return "정배열과 이달 상승은 만족했지만 단기 탄력은 A등급보다 약한 구간입니다.";
    }
    return "추천 대상이지만 현재가가 5일 이동평균선 이하라 단기 추세 강도는 보수적으로 해석해야 합니다.";
  }

  function buildAnalysisNotes(stock) {
    var rate;
    var maGap;

    if (!stock) {
      return ["상세 데이터를 불러오면 실제 저장 값 기준 해석이 표시됩니다."];
    }

    rate = monthChangeRate(stock);
    maGap = stock.currentPrice - stock.ma5;

    return [
      marketLabel(stock.market) + " " + formatStockName(stock) + "(" + stock.code + ") 기준일은 " + (stock.baseDt || "-") + "입니다.",
      "현재가는 " + formatCurrency(stock.currentPrice, stock.market) + ", 월초 시가는 " + formatCurrency(stock.monthOpenPrice, stock.market) + "로 월간 상승률은 " + rate.toFixed(2) + "%입니다.",
      buildReasonText(stock),
      "정배열 조건은 " + yesNoText(stock.goldenYn) + ", 해당월 상승 조건은 " + yesNoText(stock.monthUpYn) + " 상태입니다.",
      "현재가는 5일 이동평균선 대비 " + formatSignedCurrency(maGap, stock.market) + " 수준이며, 평균 거래대금(20)은 " + formatTradeValue(stock.avgTradeValue20, stock.market) + "입니다."
    ];
  }

  function normalizeDetailStock(raw, fallbackStock) {
    fallbackStock = fallbackStock || {};

    return {
      code: safeStr(raw.stkCd || raw.stk_cd || raw.code || fallbackStock.code),
      name: safeStr(raw.stkNm || raw.stk_nm || raw.name || fallbackStock.name),
      market: resolveCountry(raw.mktCd || raw.mkt_cd || raw.market || fallbackStock.market || state.country),
      listingMarket: safeStr(raw.listingMarket || raw.listing_market || fallbackStock.listingMarket),
      baseDt: safeStr(raw.baseDt || raw.base_dt || fallbackStock.baseDt),
      grade: safeStr(raw.recGrade || raw.rec_grade || raw.grade || fallbackStock.grade),
      recYn: safeStr(raw.recYn || raw.rec_yn || fallbackStock.recYn || "N"),
      currentPrice: toNumber(raw.curPrice || raw.cur_price || raw.currentPrice || fallbackStock.currentPrice),
      monthOpenPrice: toNumber(raw.monOpenPrice || raw.mon_open_price || raw.monthOpenPrice || fallbackStock.monthOpenPrice),
      monthChangeRate: toNumber(raw.monChgRate || raw.mon_chg_rate || raw.monthChangeRate || fallbackStock.monthChangeRate),
      ma5: toNumber(raw.ma5 || fallbackStock.ma5),
      ma20: toNumber(raw.ma20),
      ma60: toNumber(raw.ma60),
      ma120: toNumber(raw.ma120),
      ma240: toNumber(raw.ma240),
      goldenYn: safeStr(raw.goldenYn || raw.golden_yn || "N"),
      monthUpYn: safeStr(raw.monUpYn || raw.mon_up_yn || raw.monthUpYn || "N"),
      trendStrength: toNumber(raw.trendStrength || raw.trend_strength || fallbackStock.trendStrength),
      avgTradeValue20: toNumber(raw.avgTrdVal20 || raw.avg_trd_val_20 || raw.avgTradeValue20 || fallbackStock.avgTradeValue20),
      recReason: safeStr(raw.recReason || raw.rec_reason || raw.reason || fallbackStock.recReason)
    };
  }

  function detailCacheKey(stock) {
    if (!stock) {
      return "";
    }
    return [safeStr(stock.baseDt), safeStr(stock.market), safeStr(stock.code)].join(":");
  }

  function currentWatchGroupId() {
    var el = $("wlGroup");
    return el ? safeStr(el.value).trim() : "";
  }

  function watchSaveKey(stock, watchGroupId, watchGroupDiv) {
    if (!stock) {
      return "";
    }
    return [
      safeStr(watchGroupId || currentWatchGroupId()),
      safeStr(watchGroupDiv || WATCH_GROUP_DIV_RECOMMEND),
      safeStr(stock.baseDt),
      safeStr(stock.market),
      safeStr(stock.code)
    ].join(":");
  }

  function isSavedWatch(stock, watchGroupId, watchGroupDiv) {
    var key = watchSaveKey(stock, watchGroupId, watchGroupDiv);
    return key ? !!state.savedWatchKeys[key] : false;
  }

  function buildDetailViewUrl(stock) {
    var detailViewUrl = urls.recSignalDetailView || "";

    if (!detailViewUrl || !stock || !stock.code) {
      return "";
    }

    return detailViewUrl
      + "?baseDt=" + encodeURIComponent(stock.baseDt || "")
      + "&stkCd=" + encodeURIComponent(stock.code)
      + "&mktCd=" + encodeURIComponent(stock.market || state.country);
  }

  function findStockInList(list, code, market, baseDt) {
    var rows = Array.isArray(list) ? list : [];
    var i;

    for (i = 0; i < rows.length; i += 1) {
      if (rows[i].code === code && rows[i].market === market) {
        if (!baseDt || rows[i].baseDt === baseDt) {
          return rows[i];
        }
      }
    }

    return null;
  }

  function getDetailRefs() {
    if (detailRefs) {
      return detailRefs;
    }

    detailRefs = {
      overlay: $("signalRecDetailOverlay"),
      dialog: document.querySelector("#signalRecDetailOverlay .signal-rec-detail-dialog"),
      closeButton: $("signalRecDetailCloseBtn"),
      applyButton: $("signalRecDetailApplyBtn"),
      pageLink: $("signalRecDetailPageLink"),
      title: $("signalRecDetailTitle"),
      subtitle: $("signalRecDetailSubtitle"),
      grade: $("signalRecDetailGrade"),
      recommend: $("signalRecDetailRecommend"),
      currentPrice: $("signalRecDetailCurrentPrice"),
      monthChange: $("signalRecDetailMonthChange"),
      reason: $("signalRecDetailReason"),
      goldenYn: $("signalRecDetailGoldenYn"),
      monthUpYn: $("signalRecDetailMonthUpYn"),
      trendStrength: $("signalRecDetailTrendStrength"),
      tradeValue: $("signalRecDetailTradeValue"),
      metricGrid: $("signalRecDetailMetricGrid"),
      decisionList: $("signalRecDetailDecisionList"),
      maLadder: $("signalRecDetailMaLadder"),
      analysisList: $("signalRecDetailAnalysisList")
    };

    return detailRefs;
  }

  function setDetailPageLink(stock) {
    var refs = getDetailRefs();
    var url = buildDetailViewUrl(stock);

    if (!refs.pageLink) {
      return;
    }

    if (!url) {
      refs.pageLink.setAttribute("href", "#");
      refs.pageLink.setAttribute("aria-disabled", "true");
      refs.pageLink.classList.add("is-disabled");
      return;
    }

    refs.pageLink.setAttribute("href", url);
    refs.pageLink.removeAttribute("aria-disabled");
    refs.pageLink.classList.remove("is-disabled");
  }

  function renderDetailAnalysis(stock) {
    var refs = getDetailRefs();
    var notes = buildAnalysisNotes(stock);

    if (!refs.analysisList) {
      return;
    }

    refs.analysisList.innerHTML = notes.map(function (note) {
      return "<li>" + escapeHtml(note) + "</li>";
    }).join("");
  }

  function renderDetailDecision(stock) {
    var refs = getDetailRefs();
    var currentAboveMa5 = stock.currentPrice > stock.ma5;
    var items = [
      {
        title: "1. 정배열 조건",
        note: "5일선이 20일선, 60일선, 120일선, 240일선보다 모두 높아야 합니다.",
        result: safeStr(stock.goldenYn).toUpperCase() === "Y" ? "충족" : "미충족",
        klass: safeStr(stock.goldenYn).toUpperCase() === "Y" ? "is-pass" : "is-fail"
      },
      {
        title: "2. 해당월 상승 조건",
        note: "현재가가 월초 시가보다 높아야 합니다.",
        result: safeStr(stock.monthUpYn).toUpperCase() === "Y" ? "충족" : "미충족",
        klass: safeStr(stock.monthUpYn).toUpperCase() === "Y" ? "is-pass" : "is-fail"
      },
      {
        title: "3. 5일선 위치 판단",
        note: currentAboveMa5 ? "현재가가 5일 이동평균선 위에 있습니다." : "현재가가 5일 이동평균선 아래에 있습니다.",
        result: currentAboveMa5 ? "상회" : "하회",
        klass: currentAboveMa5 ? "is-pass" : "is-watch"
      },
      {
        title: "4. 등급 확정",
        note: safeStr(stock.recYn).toUpperCase() !== "Y"
          ? "추천 조건을 충족하지 않아 등급이 부여되지 않았습니다."
          : safeStr(stock.grade).toUpperCase() === "A"
          ? "이달 상승률이 3% 이상이라 A등급입니다."
          : safeStr(stock.grade).toUpperCase() === "B"
          ? "이달 상승률이 0% 초과 3% 미만이라 B등급입니다."
          : "추천 대상이지만 현재가가 5일선 이하라 C등급입니다.",
        result: safeStr(stock.recYn).toUpperCase() === "Y" ? (stock.grade || "-") + " 등급" : "등급 없음",
        klass: safeStr(stock.recYn).toUpperCase() !== "Y"
          ? "is-fail"
          : safeStr(stock.grade).toUpperCase() === "A"
          ? "is-pass"
          : "is-watch"
      }
    ];

    if (!refs.decisionList) {
      return;
    }

    refs.decisionList.innerHTML = items.map(function (item) {
      return ""
        + '<article class="signal-rec-detail-decision-item">'
        + '  <div class="signal-rec-detail-decision-copy">'
        + "    <strong>" + escapeHtml(item.title) + "</strong>"
        + "    <p>" + escapeHtml(item.note) + "</p>"
        + "  </div>"
        + '  <span class="signal-rec-detail-pill ' + item.klass + '">' + escapeHtml(item.result) + "</span>"
        + "</article>";
    }).join("");
  }

  function renderDetailMaLadder(stock) {
    var refs = getDetailRefs();
    var items = [
      { label: "5일 평균가", value: stock.ma5 },
      { label: "20일 평균가", value: stock.ma20 },
      { label: "60일 평균가", value: stock.ma60 },
      { label: "120일 평균가", value: stock.ma120 },
      { label: "240일 평균가", value: stock.ma240 }
    ];
    var max = Math.max.apply(null, items.map(function (item) {
      return item.value || 0;
    }));

    if (!refs.maLadder) {
      return;
    }

    refs.maLadder.innerHTML = items.map(function (item) {
      var pct = max ? (item.value / max) * 100 : 0;
      return ""
        + '<div class="signal-rec-detail-ma-row">'
        + '  <div class="signal-rec-detail-ma-copy">'
        + "    <span>" + escapeHtml(item.label) + "</span>"
        + "    <strong>" + escapeHtml(formatCurrency(item.value, stock.market)) + "</strong>"
        + "  </div>"
        + '  <div class="signal-rec-detail-ma-bar"><span class="signal-rec-detail-ma-fill" style="width:' + pct.toFixed(2) + '%;"></span></div>'
        + "</div>";
    }).join("");
  }

  function renderDetailMetrics(stock) {
    var refs = getDetailRefs();
    var rate = monthChangeRate(stock);
    var metrics = [
      { label: "월초 시가", value: formatCurrency(stock.monthOpenPrice, stock.market) },
      { label: "5일 평균가", value: formatCurrency(stock.ma5, stock.market) },
      { label: "20일 평균가", value: formatCurrency(stock.ma20, stock.market) },
      { label: "60일 평균가", value: formatCurrency(stock.ma60, stock.market) },
      { label: "120일 평균가", value: formatCurrency(stock.ma120, stock.market) },
      { label: "240일 평균가", value: formatCurrency(stock.ma240, stock.market) },
      { label: "5일선 대비", value: formatSignedCurrency(stock.currentPrice - stock.ma5, stock.market) },
      { label: "월간 상승률", value: rate.toFixed(2) + "%" }
    ];

    if (!refs.metricGrid) {
      return;
    }

    refs.metricGrid.innerHTML = metrics.map(function (metric) {
      return ""
        + '<article class="signal-rec-detail-metric-card">'
        + "  <span class=\"signal-rec-detail-label\">" + escapeHtml(metric.label) + "</span>"
        + "  <strong>" + escapeHtml(metric.value) + "</strong>"
        + "</article>";
    }).join("");
  }

  function renderDetailLoading(stock) {
    var refs = getDetailRefs();
    var name = stock ? formatStockName(stock) : "";
    var code = stock ? stock.code : "";
    var market = stock ? stock.market : state.country;

    if (!refs.overlay) {
      return;
    }

    refs.title.textContent = name || "상세 데이터 조회 중";
    refs.subtitle.textContent = name ? marketLabel(market) + " · " + code + " · 상세 데이터를 준비하고 있습니다." : "요청한 추천 종목 상세 데이터를 준비하고 있습니다.";
    refs.grade.className = "signal-rec-detail-grade is-empty";
    refs.grade.textContent = "-";
    refs.recommend.className = "signal-rec-detail-status is-neutral";
    refs.recommend.textContent = "조회 중";
    refs.currentPrice.textContent = "조회 중";
    refs.monthChange.textContent = "운영 데이터를 불러오는 중입니다.";
    refs.reason.textContent = "잠시만 기다려 주세요.";
    refs.goldenYn.textContent = "-";
    refs.monthUpYn.textContent = "-";
    refs.trendStrength.textContent = "-";
    refs.tradeValue.textContent = "-";

    renderDetailMetrics({
      currentPrice: 0,
      monthOpenPrice: 0,
      ma5: 0,
      ma20: 0,
      ma60: 0,
      ma120: 0,
      ma240: 0,
      market: market
    });
    refs.decisionList.innerHTML = '<article class="signal-rec-detail-decision-item"><div class="signal-rec-detail-decision-copy"><strong>상세 데이터 조회 중</strong><p>요청한 추천 종목 상세 데이터를 불러오는 중입니다.</p></div><span class="signal-rec-detail-pill is-watch">조회 중</span></article>';
    refs.maLadder.innerHTML = '<div class="signal-rec-detail-ma-row"><div class="signal-rec-detail-ma-copy"><span>상태</span><strong>조회 중</strong></div><div class="signal-rec-detail-ma-bar"><span class="signal-rec-detail-ma-fill" style="width:0%;"></span></div></div>';
    renderDetailAnalysis(null);
  }

  function renderDetailEmpty(message, fallbackStock) {
    var refs = getDetailRefs();
    var title = fallbackStock && fallbackStock.code ? formatStockName(fallbackStock) + " (" + fallbackStock.code + ")" : "운영 데이터 없음";
    var market = fallbackStock ? fallbackStock.market : state.country;
    var subtitle = fallbackStock && fallbackStock.baseDt
      ? marketLabel(market) + " · 기준일 " + fallbackStock.baseDt
      : "상세 스냅샷을 확인하지 못했습니다.";

    refs.title.textContent = title;
    refs.subtitle.textContent = subtitle;
    refs.grade.className = "signal-rec-detail-grade is-empty";
    refs.grade.textContent = "-";
    refs.recommend.className = "signal-rec-detail-status is-neutral";
    refs.recommend.textContent = "데이터 없음";
    refs.currentPrice.textContent = "-";
    refs.monthChange.textContent = "운영 데이터가 없습니다.";
    refs.reason.textContent = message;
    refs.goldenYn.textContent = "-";
    refs.monthUpYn.textContent = "-";
    refs.trendStrength.textContent = "-";
    refs.tradeValue.textContent = "-";
    refs.metricGrid.innerHTML = '<article class="signal-rec-detail-metric-card"><span class="signal-rec-detail-label">상태</span><strong>데이터 없음</strong></article><article class="signal-rec-detail-metric-card"><span class="signal-rec-detail-label">확인 대상</span><strong>TB_REC_SIGNAL</strong></article>';
    refs.decisionList.innerHTML = '<article class="signal-rec-detail-decision-item"><div class="signal-rec-detail-decision-copy"><strong>상세 스냅샷 없음</strong><p>' + escapeHtml(message) + '</p></div><span class="signal-rec-detail-pill is-fail">데이터 없음</span></article>';
    refs.maLadder.innerHTML = '<div class="signal-rec-detail-ma-row"><div class="signal-rec-detail-ma-copy"><span>상태</span><strong>MA 정보 없음</strong></div><div class="signal-rec-detail-ma-bar"><span class="signal-rec-detail-ma-fill" style="width:0%;"></span></div></div>';
    renderDetailAnalysis(null);
  }

  function renderDetailStock(stock) {
    var refs = getDetailRefs();
    var rate = monthChangeRate(stock);
    var gradeCls = gradeClass(stock.grade);

    state.detailStock = stock;
    refs.title.textContent = formatStockName(stock);
    refs.subtitle.textContent = marketLabel(stock.market) + " · " + stock.code + " · 기준일 " + (stock.baseDt || "-");
    refs.grade.className = "signal-rec-detail-grade " + (gradeCls || "is-empty");
    refs.grade.textContent = stock.grade ? stock.grade + " 등급" : "등급 없음";
    refs.recommend.className = "signal-rec-detail-status " + (safeStr(stock.recYn).toUpperCase() === "Y" ? "is-positive" : "is-neutral");
    refs.recommend.textContent = recommendStatusText(stock);
    refs.currentPrice.textContent = formatCurrency(stock.currentPrice, stock.market);
    refs.monthChange.innerHTML = "월시가 " + escapeHtml(formatCurrency(stock.monthOpenPrice, stock.market)) + " 대비 " + formatSignedRateHtml(rate);
    refs.reason.textContent = buildReasonText(stock);
    refs.goldenYn.textContent = yesNoText(stock.goldenYn);
    refs.monthUpYn.textContent = yesNoText(stock.monthUpYn);
    refs.trendStrength.textContent = stock.trendStrength.toFixed(1);
    refs.tradeValue.textContent = formatTradeValue(stock.avgTradeValue20, stock.market);

    renderDetailMetrics(stock);
    renderDetailDecision(stock);
    renderDetailMaLadder(stock);
    renderDetailAnalysis(stock);
  }

  function openDetailModal(stock, triggerEl) {
    var refs = getDetailRefs();
    var cacheKey;
    var requestSeq;

    if (!refs.overlay || !stock || !stock.code) {
      return;
    }

    state.detailTrigger = triggerEl || document.activeElement;
    state.detailStock = stock;
    setDetailPageLink(stock);
    renderDetailLoading(stock);
    refs.overlay.classList.add("is-open");
    refs.overlay.setAttribute("aria-hidden", "false");
    document.body.classList.add("signal-rec-detail-open");
    state.detailRequestSeq += 1;
    requestSeq = state.detailRequestSeq;

    cacheKey = detailCacheKey(stock);
    if (cacheKey && state.detailCache[cacheKey]) {
      renderDetailStock(state.detailCache[cacheKey]);
      return;
    }

    if (!urls.recSignalDetail) {
      renderDetailEmpty("추천 상세 API 경로가 설정되지 않았습니다.", stock);
      return;
    }

    fetchJson(urls.recSignalDetail, {
      baseDt: stock.baseDt || "",
      stkCd: stock.code,
      mktCd: stock.market || state.country
    }).then(function (json) {
      var raw = extractSingleData(json);
      var detailStock;

      if (requestSeq !== state.detailRequestSeq) {
        return;
      }

      if (!isSuccessResponse(json) || !raw) {
        renderDetailEmpty("TB_REC_SIGNAL 에 저장된 상세 스냅샷이 없습니다.", stock);
        return;
      }

      detailStock = normalizeDetailStock(raw, stock);
      if (cacheKey) {
        state.detailCache[cacheKey] = detailStock;
      }
      setDetailPageLink(detailStock);
      renderDetailStock(detailStock);
    }).catch(function () {
      if (requestSeq !== state.detailRequestSeq) {
        return;
      }
      renderDetailEmpty("상세 조회 API 실패로 운영 데이터를 불러오지 못했습니다.", stock);
    });
  }

  function closeDetailModal() {
    var refs = getDetailRefs();

    if (!refs.overlay) {
      return;
    }

    refs.overlay.classList.remove("is-open");
    refs.overlay.setAttribute("aria-hidden", "true");
    document.body.classList.remove("signal-rec-detail-open");
    state.detailRequestSeq += 1;

    if (state.detailTrigger && typeof state.detailTrigger.focus === "function" && document.contains(state.detailTrigger)) {
      state.detailTrigger.focus();
    }
  }

  function bindDetailModal() {
    var refs = getDetailRefs();

    if (!refs.overlay || refs.overlay.getAttribute("data-bound") === "Y") {
      return;
    }

    refs.overlay.setAttribute("data-bound", "Y");
    refs.overlay.addEventListener("click", function (event) {
      if (event.target === refs.overlay || safeStr(event.target.getAttribute("data-detail-close")).toUpperCase() === "TRUE") {
        closeDetailModal();
      }
    });
    if (refs.dialog) {
      refs.dialog.addEventListener("click", function (event) {
        event.stopPropagation();
      });
    }
    if (refs.closeButton) {
      refs.closeButton.addEventListener("click", function () {
        closeDetailModal();
      });
    }
    if (refs.applyButton) {
      refs.applyButton.addEventListener("click", function () {
        if (!state.detailStock || !state.detailStock.code) {
          return;
        }
        applyStockSelection(state.detailStock);
        closeDetailModal();
      });
    }
    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape" && refs.overlay.classList.contains("is-open")) {
        closeDetailModal();
      }
    });
  }

  function filteredList() {
    var list = state.allStocks || [];
    var keyword = currentKeyword();

    return list.filter(function (stock) {
      var matchedGrade = state.grade === "ALL" || safeStr(stock.grade).toUpperCase() === state.grade;
      if (!matchedGrade) {
        return false;
      }

      if (!keyword) {
        return true;
      }

      var haystack = [
        safeStr(stock.name).toUpperCase(),
        safeStr(stock.code).toUpperCase(),
        safeStr(stock.recReason).toUpperCase(),
        safeStr(stock.grade).toUpperCase()
      ].join(" ");
      return haystack.indexOf(keyword) >= 0;
    });
  }

  function sortedList() {
    var list = filteredList().slice();

    list.sort(function (a, b) {
      if (state.sort === "monthChange") {
        return monthChangeRate(b) - monthChangeRate(a)
          || b.trendStrength - a.trendStrength
          || b.avgTradeValue20 - a.avgTradeValue20
          || a.code.localeCompare(b.code);
      }
      if (state.sort === "trend") {
        return b.trendStrength - a.trendStrength
          || monthChangeRate(b) - monthChangeRate(a)
          || b.avgTradeValue20 - a.avgTradeValue20
          || a.code.localeCompare(b.code);
      }
      if (state.sort === "tradeValue") {
        return b.avgTradeValue20 - a.avgTradeValue20
          || monthChangeRate(b) - monthChangeRate(a)
          || b.trendStrength - a.trendStrength
          || a.code.localeCompare(b.code);
      }
      return a.rank - b.rank || a.code.localeCompare(b.code);
    });

    return list;
  }

  function ensureSelection(list) {
    var found = false;
    var i;

    if (!Array.isArray(list) || list.length === 0) {
      state.selectedCode = "";
      state.selectedMarket = state.country;
      return;
    }

    for (i = 0; i < list.length; i += 1) {
      if (list[i].code === state.selectedCode && list[i].market === state.selectedMarket) {
        found = true;
        break;
      }
    }

    if (!found) {
      state.selectedCode = list[0].code;
      state.selectedMarket = list[0].market;
    }
  }

  function setMeta(list, totalCount) {
    var asOf = $("signalRecAsOf");
    var count = $("signalRecCount");
    var sourceList = list && list.length ? list : state.allStocks;
    var baseDt = sourceList && sourceList[0] ? sourceList[0].baseDt : "";

    if (asOf) {
      if (baseDt) {
        asOf.textContent = "조회 기준일 " + baseDt;
      } else {
        asOf.textContent = "조회 기준일 -";
      }
    }

    if (count) {
      if (!totalCount) {
        count.textContent = "";
      } else if (list && list.length !== totalCount) {
        count.textContent = "표시 " + list.length + " / 총 " + totalCount + "건";
      } else {
        count.textContent = "총 " + totalCount + "건";
      }
    }
  }

  function renderCountryFilterState() {
    var button = $("signalCountrySwitchBtn");
    var text = $("signalCountryPillText");
    var isUs = state.country === COUNTRY_US;

    if (text) {
      text.textContent = isUs ? "미국" : "한국";
    }

    if (button) {
      button.classList.toggle("is-overseas", isUs);
      button.setAttribute("title", isUs ? "미국" : "한국");
    }
  }

  function renderGradeFilterState() {
    Array.prototype.forEach.call(document.querySelectorAll("#signalGradeFilter .signal-rec-chip"), function (button) {
      button.classList.toggle("is-active", safeStr(button.getAttribute("data-grade")).toUpperCase() === state.grade);
    });
  }

  function renderMarketFilterOptions() {
    var options = MARKET_OPTIONS[state.country] || MARKET_OPTIONS.KR;
    var target = $("signalMarketFilter");

    if (!target) {
      return;
    }

    target.innerHTML = options.map(function (option) {
      var active = option.value === currentMarketFilter() ? " is-active" : "";
      return '<button type="button" class="signal-rec-chip' + active + '" data-market-filter="' + option.value + '">' + option.label + "</button>";
    }).join("");
  }

  function setError(message, kind) {
    var el = $("signalRecError");
    if (!el) {
      return;
    }
    el.classList.remove("is-success");
    if (!message) {
      el.style.display = "none";
      el.textContent = "";
      return;
    }
    if (kind === "success") {
      el.classList.add("is-success");
    }
    el.style.display = "block";
    el.textContent = message;
  }

  function applyStockSelection(stock) {
    var input = $("topSearchInput");
    var codeInput = $("stockCode");
    var fromEl = $("fromDate");
    var toEl = $("toDate");

    if (!stock || !stock.code) {
      return;
    }

    if (input) {
      input.value = stock.code;
    }
    if (codeInput) {
      codeInput.value = stock.code;
    }
    if ((fromEl && !fromEl.value) || (toEl && !toEl.value)) {
      if (typeof window.setDefaultDates === "function") {
        window.setDefaultDates();
      }
    }

    try {
      window.__SELECTED_STOCK_COUNTRY = stock.market === "US" ? "US" : "KR";
      window.__SELECTED_STOCK_MARKET = "";
    } catch (ignore) {}

    if (typeof window.doSearch === "function") {
      window.doSearch();
    } else if (typeof window.selectStockByCode === "function") {
      window.selectStockByCode(stock.code);
    } else if (typeof window.loadStockChartByCode === "function") {
      window.loadStockChartByCode(stock.code);
    }
  }

  function saveToWatchlist(stock, triggerEl) {
    var saveUrl = urls.recPickSaveToWatchlist;
    var watchGroupId = currentWatchGroupId();
    var watchGroupDiv = WATCH_GROUP_DIV_RECOMMEND;
    var saveKey = watchSaveKey(stock, watchGroupId, watchGroupDiv);
    var body;

    if (!saveUrl) {
      setError("추천 저장 API 경로가 설정되지 않았습니다.");
      return;
    }

    if (!watchGroupId) {
      setError("관심그룹을 먼저 선택해 주세요.");
      return;
    }

    if (!stock || !stock.code || !stock.baseDt) {
      setError("추천 저장 파라미터가 올바르지 않습니다.");
      return;
    }

    if (isSavedWatch(stock, watchGroupId, watchGroupDiv) || state.saveBusyKeys[saveKey]) {
      return;
    }

    state.saveBusyKeys[saveKey] = true;
    if (triggerEl) {
      triggerEl.disabled = true;
      triggerEl.classList.add("is-busy");
    }

    body = new URLSearchParams();
    body.set("baseDt", stock.baseDt);
    body.set("mktCd", stock.market || state.country);
    body.set("stkCd", stock.code);
    body.set("watchGroupId", watchGroupId);
    body.set("groupDiv", watchGroupDiv);
    body.set("recRank", String(stock.rank || ""));

    function finishSave() {
      delete state.saveBusyKeys[saveKey];
      if (triggerEl) {
        triggerEl.classList.remove("is-busy");
      }
    }

    fetch(saveUrl, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
      },
      credentials: "same-origin",
      body: body.toString()
    }).then(function (response) {
      if (!response.ok) {
        throw new Error("HTTP " + response.status);
      }
      return response.json();
    }).then(function (json) {
      if (!isSuccessResponse(json)) {
        throw new Error(safeStr(json && (json.system_msg || json.result_msg)) || "관심종목 등록에 실패했습니다.");
      }

      state.savedWatchKeys[saveKey] = true;
      setError(formatStockName(stock) + " 관심종목 등록 완료", "success");
      renderTable();

      if (typeof window.loadWatchlistItems === "function") {
        window.loadWatchlistItems();
      }
    }).catch(function (error) {
      setError(error && error.message ? error.message : "관심종목 등록에 실패했습니다.");
      if (triggerEl) {
        triggerEl.disabled = false;
      }
    }).then(function () {
      finishSave();
    });
  }

  function renderTable() {
    var tbody = $("signalRecComparisonBody");
    var list = sortedList();
    var totalCount = state.allStocks.length;
    var detailViewUrl = urls.recSignalDetailView || "";
    var hasDetailAction = !!(detailViewUrl || urls.recSignalDetail);
    var hasSaveAction = !!urls.recPickSaveToWatchlist;
    var html = "";
    var i;

    if (!tbody) {
      return;
    }

    ensureSelection(list);
    setMeta(list, totalCount);

    if (!list.length) {
      tbody.innerHTML = '<tr><td colspan="5" class="signal-rec-empty">' + (totalCount
        ? "선택한 조건에 맞는 추천신호가 없습니다."
        : "표시할 추천신호 비교 데이터가 없습니다.") + "</td></tr>";
      return;
    }

    for (i = 0; i < list.length; i += 1) {
      var stock = list[i];
      var rowClass = "signal-rec-row";
      var detailUrl = "";
      var savedWatch = isSavedWatch(stock);
      var detailButtonHtml = "";
      var saveButtonHtml = "";

      if (stock.code === state.selectedCode && stock.market === state.selectedMarket) {
        rowClass += " is-selected";
      }

      if (detailViewUrl) {
        detailUrl = detailViewUrl
          + "?baseDt=" + encodeURIComponent(stock.baseDt || "")
          + "&stkCd=" + encodeURIComponent(stock.code)
          + "&mktCd=" + encodeURIComponent(stock.market);
      }

      detailButtonHtml = hasDetailAction
        ? '<a class="signal-rec-detail-btn" href="' + escapeHtml(detailUrl || "#") + '" data-code="' + escapeHtml(stock.code) + '" data-market="' + escapeHtml(stock.market) + '" data-base-dt="' + escapeHtml(stock.baseDt || "") + '" aria-label="' + escapeHtml(stock.name) + ' 상세 보기">상세</a>'
        : '<span class="signal-rec-detail-btn is-disabled" aria-disabled="true">상세</span>';

      saveButtonHtml = hasSaveAction
        ? '<button type="button" class="signal-rec-detail-btn signal-rec-save-btn' + (savedWatch ? ' is-saved' : '') + '" data-code="' + escapeHtml(stock.code) + '" data-market="' + escapeHtml(stock.market) + '" data-base-dt="' + escapeHtml(stock.baseDt || "") + '"' + (savedWatch ? ' disabled aria-disabled="true"' : '') + '>' + (savedWatch ? '등록됨' : '등록') + '</button>'
        : '<span class="signal-rec-detail-btn is-disabled" aria-disabled="true">등록</span>';

      html += ""
        + '<tr class="' + rowClass + '" data-code="' + stock.code + '" data-market="' + stock.market + '" title="더블클릭 시 차트에 반영됩니다.">'
        + '  <td class="signal-rec-rank">' + stock.rank + "</td>"
        + '  <td>'
        + '    <div class="signal-rec-stock">'
        + '      <span class="signal-rec-name">' + escapeHtml(formatStockName(stock)) + "</span>"
        + '      <span class="signal-rec-code">' + escapeHtml(stock.code) + "</span>"
        + "    </div>"
        + "  </td>"
        + '  <td><span class="signal-rec-grade ' + gradeClass(stock.grade) + '">' + escapeHtml(stock.grade || "-") + "</span></td>"
        + '  <td class="signal-rec-num">' + formatPrice(stock.currentPrice, stock.market) + "</td>"
        + '  <td class="signal-rec-action"><div class="signal-rec-action-stack">' + detailButtonHtml + saveButtonHtml + "</div></td>"
        + "</tr>";
    }

    tbody.innerHTML = html;

    Array.prototype.forEach.call(tbody.querySelectorAll(".signal-rec-detail-btn"), function (button) {
      button.addEventListener("click", function (event) {
        if (this.classList.contains("signal-rec-save-btn")) {
          return;
        }
        var code = safeStr(this.getAttribute("data-code"));
        var market = safeStr(this.getAttribute("data-market")) || state.country;
        var baseDt = safeStr(this.getAttribute("data-base-dt"));
        var selected = findStockInList(list, code, market, baseDt) || findStockInList(state.allStocks, code, market, baseDt);

        if (!selected) {
          return;
        }

        event.preventDefault();
        event.stopPropagation();
        state.selectedCode = code;
        state.selectedMarket = market;
        renderTable();
        openDetailModal(selected, this);
      });
      button.addEventListener("dblclick", function (event) {
        event.preventDefault();
        event.stopPropagation();
      });
    });

    Array.prototype.forEach.call(tbody.querySelectorAll(".signal-rec-save-btn"), function (button) {
      button.addEventListener("click", function (event) {
        var code = safeStr(this.getAttribute("data-code"));
        var market = safeStr(this.getAttribute("data-market")) || state.country;
        var baseDt = safeStr(this.getAttribute("data-base-dt"));
        var selected = findStockInList(list, code, market, baseDt) || findStockInList(state.allStocks, code, market, baseDt);

        event.preventDefault();
        event.stopPropagation();
        saveToWatchlist(selected, this);
      });
      button.addEventListener("dblclick", function (event) {
        event.preventDefault();
        event.stopPropagation();
      });
    });

    Array.prototype.forEach.call(tbody.querySelectorAll(".signal-rec-row"), function (row) {
      row.addEventListener("click", function () {
        state.selectedCode = safeStr(this.getAttribute("data-code"));
        state.selectedMarket = safeStr(this.getAttribute("data-market")) || state.country;
        renderTable();
      });
      row.addEventListener("dblclick", function () {
        var code = safeStr(this.getAttribute("data-code"));
        var market = safeStr(this.getAttribute("data-market")) || state.country;
        var selected = null;
        var j;

        state.selectedCode = code;
        state.selectedMarket = market;

        for (j = 0; j < list.length; j += 1) {
          if (list[j].code === code && list[j].market === market) {
            selected = list[j];
            break;
          }
        }

        renderTable();
        applyStockSelection(selected);
      });
    });
  }

  function renderAll() {
    renderTable();
  }

  function cloneStocks(list) {
    return (list || []).map(function (stock) {
      return Object.assign({}, stock);
    });
  }

  function applyLoadedRows(rows, fallbackMode) {
    state.allStocks = rows.map(normalizeStock);
    state.baseDt = state.allStocks[0] ? state.allStocks[0].baseDt : "";
    state.usingFallback = !!fallbackMode;
    ensureSelection(state.allStocks);
    renderAll();
  }

  function cacheKey() {
    return state.country + ":" + currentMarketFilter();
  }

  function applyCacheEntry(entry) {
    if (!entry) {
      return false;
    }

    state.allStocks = cloneStocks(entry.rows);
    state.baseDt = safeStr(entry.baseDt);
    state.usingFallback = !!entry.usingFallback;
    ensureSelection(state.allStocks);
    setError("");
    renderAll();
    return true;
  }

  function setLoadingState(message) {
    var tbody = $("signalRecComparisonBody");

    state.allStocks = [];
    state.baseDt = "";
    state.usingFallback = false;
    setMeta([], 0);
    setError("");

    if (tbody) {
      tbody.innerHTML = '<tr><td colspan="5" class="signal-rec-empty">' + escapeHtml(message) + "</td></tr>";
    }
  }

  function fetchList(forceReload) {
    var listUrl = urls.recSignalList;
    var params = {
      mktCd: state.country
    };
    var key = cacheKey();

    if (currentMarketFilter() !== MARKET_FILTER_ALL) {
      params.marketFilter = currentMarketFilter();
    }

    if (!listUrl) {
      setError("추천신호 비교 API 경로가 설정되지 않았습니다.");
      setMeta([], 0);
      return;
    }

    if (!forceReload && applyCacheEntry(state.cache[key])) {
      return;
    }

    setLoadingState("추천신호 비교 데이터를 불러오는 중입니다.");

    function requestRows(recYn) {
      var query = new URLSearchParams(Object.assign({}, params, { recYn: recYn }));
      return fetch(listUrl + "?" + query.toString(), {
        headers: {
          Accept: "application/json"
        },
        credentials: "same-origin"
      }).then(function (response) {
        if (!response.ok) {
          throw new Error("HTTP " + response.status);
        }
        return response.json();
      }).then(function (json) {
        if (!isSuccessResponse(json)) {
          throw new Error("API_ERROR");
        }
        return extractList(json);
      });
    }

    requestRows("Y").then(function (rows) {
      if (rows.length) {
        state.cache[key] = {
          rows: rows.map(normalizeStock),
          baseDt: rows[0] ? safeStr(rows[0].baseDt || rows[0].base_dt) : "",
          usingFallback: false
        };
        applyCacheEntry(state.cache[key]);
        return;
      }

      return requestRows("N").then(function (fallbackRows) {
        if (fallbackRows.length) {
          state.cache[key] = {
            rows: fallbackRows.map(normalizeStock),
            baseDt: fallbackRows[0] ? safeStr(fallbackRows[0].baseDt || fallbackRows[0].base_dt) : "",
            usingFallback: true
          };
          applyCacheEntry(state.cache[key]);
          return;
        }

        state.cache[key] = {
          rows: [],
          baseDt: "",
          usingFallback: false
        };
        setError("");
        applyLoadedRows([], false);
      });
    }).catch(function () {
      state.cache[key] = {
        rows: [],
        baseDt: "",
        usingFallback: false
      };
      setError("추천신호 비교 데이터를 불러오지 못했습니다.");
      applyLoadedRows([], false);
    });
  }

  function scheduleFetch(forceReload) {
    window.setTimeout(function () {
      fetchList(forceReload);
    }, 0);
  }

  function bind() {
    var keyword = $("signalKeyword");
    var reload = $("btnSignalReload");
    var watchMarket = $("wlMarket");
    var countrySwitch = $("signalCountrySwitchBtn");
    var marketFilter = $("signalMarketFilter");
    var gradeFilter = $("signalGradeFilter");
    var sortFilter = $("signalSortFilter");

    if (!$("signalRecComparisonBody")) {
      return;
    }

    bindDetailModal();

    if (countrySwitch) {
      countrySwitch.addEventListener("click", function () {
        state.country = state.country === COUNTRY_US ? COUNTRY_KR : COUNTRY_US;
        state.marketFilter = MARKET_FILTER_ALL;
        state.selectedCode = "";
        state.selectedMarket = state.country;
        renderCountryFilterState();
        renderMarketFilterOptions();
        scheduleFetch(false);
      });
    }

    if (marketFilter) {
      marketFilter.addEventListener("click", function (event) {
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
        state.selectedCode = "";
        state.selectedMarket = state.country;
        renderMarketFilterOptions();
        scheduleFetch(false);
      });
    }

    if (gradeFilter) {
      gradeFilter.addEventListener("click", function (event) {
        var button = event.target;

        if (!button || button.tagName !== "BUTTON") {
          return;
        }

        state.grade = safeStr(button.getAttribute("data-grade")).toUpperCase() || "ALL";
        renderGradeFilterState();
        renderTable();
      });
    }

    if (keyword) {
      keyword.addEventListener("input", function () {
        renderTable();
      });
    }

    if (sortFilter) {
      sortFilter.addEventListener("change", function () {
        state.sort = safeStr(this.value) || "rank";
        renderTable();
      });
    }

    if (reload) {
      reload.addEventListener("click", function () {
        fetchList(true);
      });
    }

    if (watchMarket) {
      watchMarket.addEventListener("change", function () {
        syncCountryFromWatchlist();
      });
    }

    renderCountryFilterState();
    renderGradeFilterState();
    renderMarketFilterOptions();
    if (sortFilter) {
      sortFilter.value = state.sort;
    }
    fetchList(false);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bind);
  } else {
    bind();
  }
})();
