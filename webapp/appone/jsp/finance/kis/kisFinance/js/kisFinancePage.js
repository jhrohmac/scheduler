function fmtYmdToPlain(ymd) {
    if (!ymd) return "";
    return ("" + ymd).replaceAll("-", "");
  }

  function plainYmdToIso(plain) {
    if (!plain) return "";
    var s = ("" + plain).replace(/[^0-9]/g, "");
    if (s.length !== 8) return "";
    return s.substr(0, 4) + "-" + s.substr(4, 2) + "-" + s.substr(6, 2);
  }

  function todayPlainYmd() {
    var d = new Date();
    var y = d.getFullYear();
    var m = ("0" + (d.getMonth() + 1)).slice(-2);
    var dd = ("0" + d.getDate()).slice(-2);
    return "" + y + m + dd;
  }

  function formatIsoDate(d) {
    if (!d || isNaN(d.getTime())) return "";
    var y = d.getFullYear();
    var m = ("0" + (d.getMonth() + 1)).slice(-2);
    var dd = ("0" + d.getDate()).slice(-2);
    return y + "-" + m + "-" + dd;
  }

  function applyMinuteDefaultRange(days) {
    var span = (days && days > 0) ? days : 5;
    var to = new Date();
    var from = new Date(to.getTime());
    from.setDate(from.getDate() - (span - 1));
    $("#fromDate").val(formatIsoDate(from));
    $("#toDate").val(formatIsoDate(to));
  }

  function setDefaultDates() {
    var now = new Date();
    var to = new Date(now.getTime());
    var from = new Date(now.getTime());
    from.setMonth(from.getMonth() - 18);

    function toISO(d) {
      var y = d.getFullYear();
      var m = ("0" + (d.getMonth() + 1)).slice(-2);
      var dd = ("0" + d.getDate()).slice(-2);
      return y + "-" + m + "-" + dd;
    }

    $("#fromDate").val(toISO(from));
    $("#toDate").val(toISO(to));
  }

  function unwrapList(res) {
    if (!res) return { ok: false, list: [], msg: "" };

    if (res.result_code) {
      var ok1 = ("" + res.result_code) === "1";
      return { ok: ok1, list: res.data || [], msg: res.result_msg || "" };
    }

    if (res.system_code) {
      var ok2 = ("" + res.system_code) === "0000";
      return { ok: ok2, list: res.data || [], msg: res.system_msg || "" };
    }

    return { ok: true, list: res.data || [], msg: "" };
  }

  function unwrapSingle(res) {
    if (!res) return { ok: false, data: null, msg: "" };

    // 일부 API는 data 대신 singleData 키로 단건을 내려줍니다.
    // (예: selectMarketSummary.do)
    function pickSingleData(r) {
      if (!r) return null;
      if (r.singleData) return r.singleData;
      if (r.single_data) return r.single_data;
      // data가 배열이면 단건이 아니므로 제외
      if (r.data && !Array.isArray(r.data)) return r.data;
      return r.data || null;
    }

    if (res.result_code) {
      var ok1 = ("" + res.result_code) === "1";
      return { ok: ok1, data: pickSingleData(res), msg: res.result_msg || "" };
    }

    if (res.system_code) {
      var ok2 = ("" + res.system_code) === "0000";
      return { ok: ok2, data: pickSingleData(res), msg: res.system_msg || "" };
    }

    return { ok: true, data: pickSingleData(res), msg: "" };
  }


  /* =========================
     Highstock Lazy Loader
     - 차트 최초 조회 시점에만 Highstock/모듈을 로드하여 초기 체감 속도를 개선
     ========================= */
  var __highstockPromise = null;

  function loadScriptOnce(url, id) {
    return new Promise(function (resolve, reject) {
      if (!url) {
        resolve();
        return;
      }

      try {
        var exists = false;
        $("script").each(function () {
          var s = $(this).attr("src");
          if (s && s.indexOf(url) >= 0) {
            exists = true;
            return false;
          }
        });
        if (exists) {
          resolve();
          return;
        }
      } catch (e) {}

      if (id && document.getElementById(id)) {
        resolve();
        return;
      }

      var el = document.createElement("script");
      if (id) el.id = id;
      el.src = url;
      el.async = true;
      el.onload = function () { resolve(); };
      el.onerror = function () { reject(new Error("Failed to load: " + url)); };
      document.head.appendChild(el);
    });
  }

  function ensureHighstockLoaded() {
    if (typeof window.Highcharts !== "undefined" && window.Highcharts.stockChart) {
      return Promise.resolve();
    }
    if (__highstockPromise) {
      return __highstockPromise;
    }

    var src = window.__HIGHCHARTS_SRC || {};
    var ctx = window.__CTX_PATH || "";
    if (ctx && ctx.charAt(ctx.length - 1) === "/") {
      ctx = ctx.substring(0, ctx.length - 1);
    }
    var localBase = ctx + "/appone/plugins/Highcharts-Stock-11.1.0/code";
    var highstockUrl = src.highstock || (localBase + "/highstock.js");
    var dataUrl = src.data || (localBase + "/modules/data.js");
    var exportingUrl = src.exporting || (localBase + "/modules/exporting.js");

    __highstockPromise = loadScriptOnce(highstockUrl, "hc-highstock")
      .then(function () { return loadScriptOnce(dataUrl, "hc-data"); })
      .then(function () { return loadScriptOnce(exportingUrl, "hc-exporting"); })
      .catch(function (e) {
        __highstockPromise = null;
        throw e;
      });

    return __highstockPromise;
  }
  

  /* =========================
     ChartScript Lazy Loader
     - ChartScript(로컬 js)가 누락되거나 로딩 실패 시, doSearch에서 자동 재로딩 시도
     ========================= */
  var __chartScriptPromise = null;

  function getKisJsBase() {
    try {
      var scripts = document.getElementsByTagName("script");
      for (var i = scripts.length - 1; i >= 0; i--) {
        var src = scripts[i].getAttribute("src") || "";
        if (!src) continue;
        if (src.indexOf("kisFinancePage.js") >= 0) {
          return src.substring(0, src.lastIndexOf("/") + 1);
        }
      }
    } catch (e) {}
    return "";
  }

  function ensureChartScriptLoaded() {
    if (window.ChartScript && typeof ChartScript.loadKisItemchartprice === "function") {
      return Promise.resolve();
    }
    if (__chartScriptPromise) {
      return __chartScriptPromise;
    }

    var base = getKisJsBase();
    var src = window.__CHARTSCRIPT_SRC || {};

    // 기본은 kisFinancePage.js와 동일 폴더 기준으로 로드 시도
    var urls = [
      src.ma || (base ? (base + "maScript.js") : "maScript.js"),
      src.doubleMonth || (base ? (base + "doubleMonthChartScript.js") : "doubleMonthChartScript.js"),
      src.chart || (base ? (base + "chartScript.js") : "chartScript.js")
    ];

    __chartScriptPromise = Promise.resolve()
      .then(function () { return loadScriptOnce(urls[0], "kis-maScript"); })
      .then(function () { return loadScriptOnce(urls[1], "kis-doubleMonthChartScript"); })
      .then(function () { return loadScriptOnce(urls[2], "kis-chartScript"); })
      .then(function () {
        if (!window.ChartScript || typeof ChartScript.loadKisItemchartprice !== "function") {
          throw new Error("ChartScript not available after loading scripts.");
        }
      })
      .catch(function (e) {
        __chartScriptPromise = null;
        throw e;
      });

    return __chartScriptPromise;
  }

  /* =========================
     Watchlist - Last selected group persistence
     - 새로고침(F5) 시에도 직전 선택한 마켓/그룹을 유지
     ========================= */
  var __WL_STORE_KEY_MARKET = "KIS_WL_MARKET";
  var __WL_STORE_KEY_GROUP_PREFIX = "KIS_WL_GROUP_";
  var __WL_STORE_KEY_GROUP_DIV_PREFIX = "KIS_WL_GROUP_DIV_";
  var __LAST_VIEW_KEY = "KIS_LAST_VIEW";
  var __LAST_SEARCHED_STOCK_CODE = "";

  function wlStoreEnabled() {
    try {
      return !!window.localStorage;
    } catch (e) {
      return false;
    }
  }

  function wlGroupKeyByMarket(marketVal) {
    return __WL_STORE_KEY_GROUP_PREFIX + (marketVal || "");
  }

  function wlGroupDivKeyByMarket(marketVal) {
    return __WL_STORE_KEY_GROUP_DIV_PREFIX + (marketVal || "");
  }

  function normalizeWatchlistGroupDiv(groupDiv) {
    var v = String(groupDiv || "").trim().toLowerCase();
    if (v === "monthend") return "month";
    if (v === "month") return "month";
    if (v === "recommend") return "recommend";
    return "normal";
  }

  function wlSaveMarket(marketVal) {
    if (!wlStoreEnabled()) return;
    try {
      window.localStorage.setItem(__WL_STORE_KEY_MARKET, marketVal || "");
    } catch (e) {}
  }

  function wlLoadMarket() {
    if (!wlStoreEnabled()) return "";
    try {
      return window.localStorage.getItem(__WL_STORE_KEY_MARKET) || "";
    } catch (e) {
      return "";
    }
  }

  function wlSaveGroup(marketVal, groupId) {
    if (!wlStoreEnabled()) return;
    try {
      window.localStorage.setItem(wlGroupKeyByMarket(marketVal), groupId || "");
    } catch (e) {}
  }

  function wlLoadGroup(marketVal) {
    if (!wlStoreEnabled()) return "";
    try {
      return window.localStorage.getItem(wlGroupKeyByMarket(marketVal)) || "";
    } catch (e) {
      return "";
    }
  }

  function wlSaveGroupDiv(marketVal, groupDiv) {
    if (!wlStoreEnabled()) return;
    try {
      window.localStorage.setItem(
        wlGroupDivKeyByMarket(marketVal),
        normalizeWatchlistGroupDiv(groupDiv)
      );
    } catch (e) {}
  }

  function wlLoadGroupDiv(marketVal) {
    if (!wlStoreEnabled()) return "normal";
    try {
      return normalizeWatchlistGroupDiv(window.localStorage.getItem(wlGroupDivKeyByMarket(marketVal)) || "");
    } catch (e) {
      return "normal";
    }
  }

  function saveLastViewState(state) {
    if (!wlStoreEnabled()) return;
    try {
      window.localStorage.setItem(__LAST_VIEW_KEY, JSON.stringify(state || {}));
    } catch (e) {}
  }

  function loadLastViewState() {
    if (!wlStoreEnabled()) return null;
    try {
      var raw = window.localStorage.getItem(__LAST_VIEW_KEY);
      if (!raw) return null;
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  }

  function setWlMarketPillText(isOverseas) {
    var $txt = $("#wlMarketPillText");
    if ($txt.length) $txt.text(isOverseas ? "미국" : "한국");
    $("#wlMarketSwitchBtn").toggleClass("is-overseas", isOverseas);
  }

  function syncWlMarketSwitchFromSelect() {
    var v = $("#wlMarket").val();
    var isOverseas = (v === "A");
    setWlMarketPillText(isOverseas);
  }

  function toggleWlMarketFromPill() {
    var current = $("#wlMarket").val();
    var nextVal = (current === "A") ? "N" : "A";
    if ($("#wlMarket").val() !== nextVal) {
      $("#wlMarket").val(nextVal).trigger("change");
    } else {
      syncWlMarketSwitchFromSelect();
    }
  }

  function currentWatchlistGroupDiv() {
    return normalizeWatchlistGroupDiv($("#wlGroupDiv").val());
  }

  function syncWlGroupDivFromMarket() {
    var $sel = $("#wlGroupDiv");
    if (!$sel.length) return;
    var normalized = wlLoadGroupDiv($("#wlMarket").val());
    if (!$sel.find("option[value='" + normalized + "']").length) {
      normalized = "normal";
    }
    $sel.val(normalized);
  }

  var __watchlistItemsXhr = null;
  var __watchlistItemsRequestSeq = 0;

  function currentWatchlistQuery() {
    return {
      market: ($("#wlMarket").val() || "").trim(),
      groupId: ($("#wlGroup").val() || "").trim(),
      groupDiv: currentWatchlistGroupDiv()
    };
  }

  function isSameWatchlistQuery(query) {
    var current = currentWatchlistQuery();
    return current.market === (query.market || "")
      && current.groupId === (query.groupId || "")
      && current.groupDiv === normalizeWatchlistGroupDiv(query.groupDiv || "");
  }

  function abortPendingWatchlistItemsRequest() {
    try {
      if (__watchlistItemsXhr && __watchlistItemsXhr.readyState !== 4) {
        __watchlistItemsXhr.abort();
      }
    } catch (e) {}
    __watchlistItemsXhr = null;
  }

  function setWatchlistLoading(message) {
    var msg = message || "관심종목 불러오는 중...";
    $("#watchlist").html("<div class='mini-muted'>" + msg + "</div>");
  }

  function loadWatchlistGroups() {
    $("#wlError").hide().text("");
    $("#wlGroup").empty();

    var market = getGroupMarketForEvent();
    
    $.ajax({
      url: window.__URLS.selectWatchlistGroups,
      method: "GET",
      dataType: "json",
      data: { market: market },
      success: function (res) {
        var u = unwrapList(res);
        if (!u.ok) {
          $("#wlError").show().text(u.msg || "관심종목 그룹 조회 실패");
          $("#wlGroup").append("<option value=''>그룹 없음</option>");
          renderWatchlist([]);
          return;
        }

        var list = u.list || [];
        if (!list.length) {
          $("#wlGroup").append("<option value=''>그룹 없음</option>");
          renderWatchlist([]);
          syncGroupManageList([]);
          return;
        }

        for (var i = 0; i < list.length; i++) {
          var g = list[i] || {};
          var gid = g.GROUP_ID || g.groupId || g.group_id || "";
          var gnm = g.GROUP_NAME || g.groupName || g.group_name || "-";
          $("#wlGroup").append("<option value='" + gid + "'>" + gnm + "</option>");
        }

                // 새로고침 유지: 직전에 선택한 그룹 복원(마켓별)
        try {
          var savedGroupId = wlLoadGroup($("#wlMarket").val());
          if (savedGroupId && $("#wlGroup option[value='" + savedGroupId + "']").length) {
            $("#wlGroup").val(savedGroupId);
          }
        } catch (e) {}

        loadWatchlistItems();
        syncGroupManageList(list);
      },
      error: function () {
        $("#wlError").show().text("관심종목 그룹 조회 실패");
        $("#wlGroup").append("<option value=''>그룹 없음</option>");
        renderWatchlist([]);
        syncGroupManageList([]);
      }
    });
  }

  function watchlistEventLabel(it) {
    if (!it) return "이벤트 확인중";

    var ev = it.reco_event_summary || it.RECO_EVENT_SUMMARY || it.event_summary || it.EVENT_SUMMARY || "";
    if (ev) return String(ev);

    var sig = String(it.STOCK_MACD_SIGNAL || it.stock_macd_signal || it.reco_signal_code || it.RECO_SIGNAL_CODE || "").toUpperCase();
    if (sig.indexOf("STRONG_BUY") >= 0) return "강한매수";
    if (sig.indexOf("BUY") >= 0) return "매수";
    if (sig.indexOf("STRONG_SELL") >= 0) return "강한매도";
    if (sig.indexOf("SELL") >= 0) return "매도";
    if (sig.indexOf("HOLD") >= 0) return "관망(보합)";

    return "관망(보합)";
  }

  function renderWatchlist(items) {
    var $wrap = $("#watchlist");
    $wrap.empty();

    if (!items || !items.length) {
      $wrap.append("<div class='mini-muted'>등록된 관심종목이 없습니다.</div>");
      return;
    }
    console.log("========group LIST=============");
    //console.log(items);
    console.log("========group LIST=============");
    for (var i = 0; i < items.length; i++) {
      var it = items[i] || {};

      var code = it.STOCK_CODE || it.stockCode || it.stock_code || "";
      var name = it.STOCK_KO_NAME || it.stock_ko_name || it.STOCK_EN_NAME || it.stock_en_name || "-";
      var close = it.STOCK_CLOSE || it.stock_close || "-";
      var eventLabel = watchlistEventLabel(it);

      code = (code === null || code === undefined) ? "" : String(code).trim();
      name = (name === null || name === undefined) ? "-" : String(name);

      var country = it.STOCK_COUNTRY_CODE || it.stock_country_code || it.country || "";
      var marketCd = it.STOCK_MARKET || it.stock_market || it.market || "";
      country = (country === null || country === undefined) ? "" : String(country).trim();
      marketCd = (marketCd === null || marketCd === undefined) ? "" : String(marketCd).trim();
      if (!country) {
        country = "KR";
      }
      if (!marketCd) {
        marketCd = (country === "KR") ? "KRX" : "NAS";
      }
      var token = country + "|" + marketCd + "|" + code;

      var html = "";
      html += "<div class='wl-item' data-code='" + code + "' data-country='" + country + "' data-market='" + marketCd + "' data-token='" + token + "'>";
      html += "  <div class='wl-left'>";
      html += "    <div class='wl-name'>" + name + "</div>";
      html += "    <div class='wl-event' title='이벤트 원문'>" + eventLabel + "</div>";
      html += "    <div class='wl-code'>" + code + "</div>";
      html += "  </div>";
      html += "  <div class='wl-right'>";
      html += "    <div class='wl-price'>" + close + "</div>";
      html += "    <div class='wl-sub'>";
      html += "      <div class='wl-rate'>-</div>";
      html += "      <div class='wl-diff'>-</div>";
      html += "    </div>";
      html += "  </div>";
      html += "</div>";

      $wrap.append(html);
    }

    function closeMobilePanelsIfOpen() {
      if (!$("body").hasClass("mobile-open-left") && !$("body").hasClass("mobile-open-right")) {
        return;
      }
      $("body").removeClass("mobile-open-left mobile-open-right");
      $("#mobilePanelButtons").attr("aria-hidden", "false");
      $("#mobilePanelBackdrop").attr("aria-hidden", "true");
    }

    $(".wl-item").off("click").on("click", function () {
      var code = $(this).data("code");
      if (!code) return;
      $("#stockCode").val(code);
      doSearch();
    });

    // Mobile: double click on watchlist item closes the popup panel
    $(".wl-item").off("dblclick").on("dblclick", function () {
      closeMobilePanelsIfOpen();
    });
  }

  function loadWatchlistItems() {
    $("#wlError").hide().text("");
    var query = currentWatchlistQuery();
    var requestSeq = ++__watchlistItemsRequestSeq;

    abortPendingWatchlistItemsRequest();
    setWatchlistLoading();
    try {
      if (window.WatchlistRealtime && typeof window.WatchlistRealtime.close === "function") {
        window.WatchlistRealtime.close(true);
      }
    } catch (e) {}

    __watchlistItemsXhr = $.ajax({
      url: window.__URLS.selectWatchlistItems,
      method: "GET",
      dataType: "json",
      data: query,
      success: function (res) {
        if (requestSeq !== __watchlistItemsRequestSeq || !isSameWatchlistQuery(query)) {
          return;
        }
        var u = unwrapList(res);
        if (!u.ok) {
          $("#wlError").show().text(u.msg || "관심종목 조회 실패");
          renderWatchlist([]);
          return;
        }
        renderWatchlist(u.list || []);
        if (window.WatchlistRealtime) {
          window.WatchlistRealtime.connectFromDom();
        }
      },
      error: function (xhr, status) {
        if (status === "abort") {
          return;
        }
        if (requestSeq !== __watchlistItemsRequestSeq || !isSameWatchlistQuery(query)) {
          return;
        }
        $("#wlError").show().text("관심종목 조회 실패");
        renderWatchlist([]);
      },
      complete: function (xhr) {
        if (__watchlistItemsXhr === xhr) {
          __watchlistItemsXhr = null;
        }
      }
    });
  }

  window.loadWatchlistItems = loadWatchlistItems;
  window.loadWatchlistGroups = loadWatchlistGroups;

  /* =========================
     Market Summary
     ========================= */
  function setSummaryError(msg) {
    if (!msg) {
      $("#summaryError").hide().text("");
      return;
    }
    $("#summaryError").show().text(msg);
  }

  function loadMarketSummary() {
    setSummaryError("");

    $.ajax({
      url: window.__URLS.selectMarketSummary,
      method: "GET",
      dataType: "json",
      success: function (res) {
        var u = unwrapSingle(res);
        if (!u.ok) {
          setSummaryError(u.msg || "시장요약 조회 실패");
          return;
        }
        var d = u.data || {};
        // 기존(우측 탭) 지수는 제거되었지만, 상단 Ticker에서 값 표시는 유지합니다.
        // (남아있는 DOM이 있으면 함께 업데이트)
        var kospiTxt = (d.KOSPI || d.kospi || "-");
        var kosdaqTxt = (d.KOSDAQ || d.kosdaq || "-");
        var usdkrwTxt = (d.USDKRW || d.usdkrw || "-");
        var djiTxt = (d.DJI || d.dji || "-");
        var ixicTxt = (d.IXIC || d.ixic || "-");
        var spxTxt = (d.SPX || d.spx || "-");

        // legacy ids (혹시 남아있으면)
        $("#kospVal").text(kospiTxt);
        $("#kosdVal").text(kosdaqTxt);
        $("#fxVal").text(usdkrwTxt);
        $("#djiVal").text(djiTxt);
        $("#ixicVal").text(ixicTxt);
        $("#spxVal").text(spxTxt);
        $("#summaryAsOf").text(d.AS_OF ? ("기준: " + d.AS_OF) : "");

        // top ticker ids
        $("#topKospiVal").text(kospiTxt);
        $("#topKosdVal").text(kosdaqTxt);
        $("#topDjiVal").text(djiTxt);
        $("#topSpxVal").text(spxTxt);
        $("#topIxicVal").text(ixicTxt);

        // up/down 컬러는 realtime에서 주는 클래스(v.up/down) 규칙을 재사용
        function applyUpDown($el, raw) {
          if (!$el || !$el.length) return;
          $el.removeClass("up down flat");
          var s = String(raw || "");
          // 예: "1,106.08 (-19.91 / -1.77%)" / "6,836.17 (3.41 / 0.05%)" 형태
          var m = s.match(/\(.*?\/[\s]*([+-]?[0-9]+\.?[0-9]*)%\)/);
          if (m && m[1]) {
            var pct = String(m[1]).trim();
            if (pct.indexOf("-") === 0) $el.addClass("down");
            else if (pct.indexOf("+") === 0) $el.addClass("up");
            else {
              var n = parseFloat(pct);
              if (!isNaN(n) && n < 0) $el.addClass("down");
              else if (!isNaN(n) && n > 0) $el.addClass("up");
              else $el.addClass("flat");
            }
          } else {
            $el.addClass("flat");
          }
        }
        applyUpDown($("#topKospiVal"), kospiTxt);
        applyUpDown($("#topKosdVal"), kosdaqTxt);
        applyUpDown($("#topDjiVal"), djiTxt);
        applyUpDown($("#topSpxVal"), spxTxt);
        applyUpDown($("#topIxicVal"), ixicTxt);
        if (window.MarketSummaryRealtime) {
          window.MarketSummaryRealtime.connect();
        }
      },
      error: function () {
        setSummaryError("시장요약 조회 실패");
      }
    });
  }

  /* =========================
     Market Summary -> Chart Link
     - 시장요약 각 항목 클릭 시 차트 조회
     ========================= */
  function bindMarketSummaryChartClicks() {
    // 기존 summary-row(우측 카드) 클릭은 유지하되,
    // 지수 카드는 제거되었으므로 '있으면만' 바인딩.
    var items = [
      { selector: "#kospVal", code: "0001", label: "코스피" },
      { selector: "#kosdVal", code: "1001", label: "코스닥" },
      { selector: "#fxVal", code: "USDKRW", label: "USD/KRW" },
      { selector: "#djiVal", code: ".DJI", label: "DOW (.DJI)" },
      { selector: "#ixicVal", code: ".IXIC", label: "NASDAQ (.IXIC)" },
      { selector: "#spxVal", code: ".INX", label: "S&P 500 (.INX)" }
    ];

    for (var i = 0; i < items.length; i++) {
      var it = items[i] || {};
      if (!it.selector) continue;

      var $v = $(it.selector);
      if (!$v.length) continue;

      var $row = $v.closest(".summary-row");
      if (!$row.length) continue;

      $row.attr("data-code", it.code || "");
      $row.attr("data-label", it.label || "");
      $row.addClass("summary-click");
      $row.css("cursor", "pointer");
      if (!$row.attr("title")) {
        $row.attr("title", "클릭 시 차트 조회");
      }
    }

    $(document).off("click.summary", ".summary-row.summary-click");
    $(document).on("click.summary", ".summary-row.summary-click", function () {
      var code = $(this).attr("data-code") || "";
      var label = $(this).attr("data-label") || "";
      if (!code) return;

      if (label && $("#kisHdrName").length) {
        $("#kisHdrName").text(label);
      }

      $("#stockCode").val(code);
      doSearch();
    });

    // 상단 Ticker 클릭 => 차트 조회
    $(document).off("click.topmarket", ".tm-item");
    $(document).on("click.topmarket", ".tm-item", function () {
      var code = $(this).attr("data-code") || "";
      var label = $(this).attr("data-label") || $(this).find(".tm-name").text() || "";
      if (!code) return;
      if (label && $("#kisHdrName").length) {
        $("#kisHdrName").text(label);
      }
      $("#stockCode").val(code);
      doSearch();
    });
  }
  /* =========================
     Group Manage (DML)
     ========================= */
  function setWgStatus(msg, isError) {
    $("#wgStatus").text(msg || "").css("color", isError ? "#ef4444" : "#16a34a");
  }

  function clearWgForm() {
    $("#wg_group_id").val("");
    $("#wg_group_name").val("");
    $("#wg_group_desc").val("");
  }

  function getGroupMarketForEvent() {
    var market = $("#wlMarket").val();
    return (market === "N") ? "KR" : "US";
  }

  function syncGroupManageList(groups) {
    if (!$("#leftPanel").hasClass("wg-open")) return;
    renderGroupManageList(groups || []);
  }

  function renderGroupManageList(groups) {
    var $box = $("#wgList");
    $box.empty();

    if (!groups || !groups.length) {
      $box.append("<div class='mini-muted'>그룹이 없습니다.</div>");
      return;
    }

    for (var i = 0; i < groups.length; i++) {
      var g = groups[i] || {};
      var gid = g.GROUP_ID || g.groupId || g.group_id || "";
      var gnm = g.GROUP_NAME || g.groupName || g.group_name || ("그룹" + gid);
      var gdc = g.GROUP_DESC || g.groupDesc || g.group_desc || "";
      
      var cnt = g.STOCK_COUNT || g.stockCount || g.stock_count || "";
      var seq = g.GROUP_SEQ || g.groupSeq || g.group_seq || "";

      var html = "";
      html += "<div class='wg-row' data-id='" + gid + "' data-name='" + escapeHtml(gnm) + "' data-desc='" + escapeHtml(gdc) + "'>";
      html += "  <div>";
      html += "    <div class='wg-name'>" + gnm + "</div>";
      html += "    <div class='wg-meta'>ID:" + gid + (cnt ? (" / " + cnt) : "") + "</div>";
      html += "  </div>";
      html += "</div>";
      $box.append(html);
    }

    $box.find(".wg-row").off("click").on("click", function () {
      var $r = $(this);
      $("#wg_group_id").val($r.data("id") || "");
      $("#wg_group_name").val($r.data("name") || "");
      $("#wg_group_desc").val($r.data("desc") || "");
      setWgStatus("선택됨: " + ($("#wg_group_name").val() || ""), false);
    });
  }

  function escapeHtml(s) {
    s = "" + (s == null ? "" : s);
    return s
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll("\"", "&quot;")
      .replaceAll("'", "&#39;");
  }

  function callGroupEvent(eventCode, groupId, groupName, groupDesc, okMsg) {
    setWgStatus("처리 중...", false);

    $.ajax({
      url: window.__URLS.portfolioGroupEvent,
      method: "POST",
      dataType: "json",
      data: {
        eventCode: eventCode,
        group_id: groupId || "",
        group_name: groupName || "",
        group_desc: groupDesc || "",
        groupMarket: getGroupMarketForEvent()
      },
      success: function (res) {
        var u = unwrapSingle(res);
        if (!u.ok) {
          setWgStatus(u.msg || "처리 실패", true);
          return;
        }
        setWgStatus(okMsg || "완료", false);
        clearWgForm();
        // 새로고침(F5) 시 직전 선택한 관심 마켓/그룹 유지
    try {
      var savedMarket = wlLoadMarket();
      if (savedMarket && $("#wlMarket option[value='" + savedMarket + "']").length) {
        $("#wlMarket").val(savedMarket);
      }
    } catch (e) {}
    syncWlMarketSwitchFromSelect();
    syncWlGroupDivFromMarket();

        loadWatchlistGroups();
      },
      error: function () {
        setWgStatus("요청 실패", true);
      }
    });
  }

  function doGroupAdd() {
    var name = ($("#wg_group_name").val() || "").trim();
    var desc = ($("#wg_group_desc").val() || "").trim();

    if (!name) {
      setWgStatus("그룹명을 입력하세요.", true);
      return;
    }
    if (!desc) desc = name;

    callGroupEvent("I", "", name, desc, "그룹이 추가되었습니다.");
  }

  function doGroupUpdate() {
    var id = ($("#wg_group_id").val() || "").trim();
    var name = ($("#wg_group_name").val() || "").trim();
    var desc = ($("#wg_group_desc").val() || "").trim();

    if (!id) {
      setWgStatus("수정할 그룹을 선택하세요.", true);
      return;
    }
    if (!name) {
      setWgStatus("그룹명을 입력하세요.", true);
      return;
    }
    if (!desc) desc = name;

    callGroupEvent("U", id, name, desc, "그룹이 수정되었습니다.");
  }

  function doGroupDelete() {
    var id = ($("#wg_group_id").val() || "").trim();
    var name = ($("#wg_group_name").val() || "").trim();

    if (!id) {
      setWgStatus("삭제할 그룹을 선택하세요.", true);
      return;
    }

    if (!confirm("그룹을 삭제하면 해당 그룹의 관심종목도 함께 삭제됩니다.\n삭제하시겠습니까?\n\n[" + (name || id) + "]")) {
      return;
    }

    callGroupEvent("D", id, "", "", "그룹이 삭제되었습니다.");
  }

  /* =========================
     Holding (보유종목)
     WF-2-4: 보유종목 관리
     ========================= */
  var selectedHoldingPosition = null; // 선택된 보유종목 (삭제/물타기용)
  var holdingEventListVisible = false;

  function setHoldingError(msg, isError) {
    var $err = $("#holdingError");
    if (!msg) {
      $err.hide().text("");
      return;
    }
    $err.text(msg).css("color", isError ? "#d9534f" : "#5cb85c").show();
    if (!isError) {
      setTimeout(function() { $err.fadeOut(); }, 3000);
    }
  }

  function clearHoldingForm() {
    $("#holdingQty").val("1");
    $("#holdingPrice").val("");
    $("#holdingDesc").val("");
    $("#holdingAvgPreview").hide().html("");
    selectedHoldingPosition = null;
  }

  function loadHoldingList() {
    var groupMarket = window.__SELECTED_STOCK_COUNTRY || "KR";
    
    $.ajax({
      url: window.__URLS.positionList,
      method: "GET",
      dataType: "json",
      data: {
        marketCode: groupMarket,
        closeFlag: "N"
      },
      success: function(res) {
        var u = unwrapSingle(res);
        if (!u.ok) {
          setHoldingError(u.msg || "목록 조회 실패", true);
          return;
        }
        renderHoldingList(u.data || []);
        try { $("#holdingAsOf").text("업데이트: " + new Date().toLocaleTimeString("ko-KR")); } catch (e) {}
        setHoldingError("", false);
      },
      error: function() {
        setHoldingError("목록 조회 실패", true);
      }
    });
  }

  function renderHoldingList(list) {
    var $list = $("#holdingList");
    $list.empty();
    
    $("#holdingCount").text(list.length + "개");
    
    if (!list || !list.length) {
      $list.html('<div class="mini-muted">보유종목이 없습니다.</div>');
      return;
    }
    
    list.forEach(function(pos) {
      var code = pos.stockCode || "";
      var qty = pos.totalQty || 0;
      var avg = pos.avgPrice || 0;
      var state = pos.stateCode || "HOLD";
      var marketCode = pos.marketCode || "KR";
      
      var avgStr = marketCode === "KR" ? 
        Math.round(avg).toLocaleString() : 
        avg.toFixed(1);
      
      var $item = $('<div class="holding-item"></div>');
      $item.attr("data-position-id", pos.positionId);
      $item.attr("data-code", code);
      $item.attr("data-qty", qty);
      $item.attr("data-avg", avg);
      
      $item.html(
        '<div class="holding-item-code">' + code + '</div>' +
        '<div class="holding-item-info">' +
          '<span class="holding-qty">' + qty + '주</span>' +
          '<span class="holding-avg">@' + avgStr + '</span>' +
          '<span class="holding-state">' + state + '</span>' +
        '</div>'
      );
      
      $item.on("click", function() {
        $(".holding-item").removeClass("is-selected");
        $(this).addClass("is-selected");
        selectedHoldingPosition = pos;
        // 선택 종목을 입력창에도 동기화(실수 방지)
        if (code) {
          $("#stockCode").val(code);
        }
        updateAvgPreview();
      });
      
      $list.append($item);
    });
  }

  function updateAvgPreview() {
    var $preview = $("#holdingAvgPreview");
    
    if (!selectedHoldingPosition) {
      $preview.hide().html("");
      return;
    }
    
    var addQty = parseInt($("#holdingQty").val()) || 0;
    var addPrice = parseFloat($("#holdingPrice").val()) || 0;
    
    if (addQty <= 0 || addPrice <= 0) {
      $preview.hide().html("");
      return;
    }
    
    var beforeQty = selectedHoldingPosition.totalQty || 0;
    var beforeAvg = selectedHoldingPosition.avgPrice || 0;
    var afterQty = beforeQty + addQty;
    var afterAvg = ((beforeQty * beforeAvg) + (addQty * addPrice)) / afterQty;
    var marketCode = selectedHoldingPosition.marketCode || "KR";
    
    var afterAvgStr = marketCode === "KR" ? 
      Math.round(afterAvg).toLocaleString() : 
      afterAvg.toFixed(1);
    
    $preview.html(
      '<strong>물타기 미리보기:</strong> ' +
      beforeQty + '주 → ' + afterQty + '주, ' +
      '평단 ' + (marketCode === "KR" ? Math.round(beforeAvg).toLocaleString() : beforeAvg.toFixed(1)) +
      ' → <span class="highlight">' + afterAvgStr + '</span>'
    ).show();
  }

  function doHoldingAdd() {
    var code = $("#stockCode").val().trim();
    if (!code) {
      setHoldingError("종목코드를 입력하세요.", true);
      return;
    }
    
    var qty = parseInt($("#holdingQty").val()) || 0;
    var price = parseFloat($("#holdingPrice").val()) || 0;
    var desc = $("#holdingDesc").val().trim();
    
    if (qty <= 0) {
      setHoldingError("수량을 입력하세요.", true);
      return;
    }
    if (price <= 0) {
      setHoldingError("단가를 입력하세요.", true);
      return;
    }
    
    var marketCode = window.__SELECTED_STOCK_COUNTRY || "KR";
    var groupName = ($("#wlGroup").length ? (($("#wlGroup option:selected").text() || "").trim()) : "") || "기본";
    
    $.ajax({
      url: window.__URLS.positionAdd,
      method: "POST",
      dataType: "json",
      data: {
        stockGroup: groupName,
        stockCode: code,
        marketCode: marketCode,
        qty: qty,
        price: price,
        reasonText: desc
      },
      success: function(res) {
        var u = unwrapSingle(res);
        if (!u.ok) {
          setHoldingError(u.msg || "추가 실패", true);
          return;
        }
        setHoldingError("보유종목이 추가되었습니다.", false);
        clearHoldingForm();
        loadHoldingList();
      },
      error: function() {
        setHoldingError("추가 실패", true);
      }
    });
  }

  function doHoldingDelete() {
    if (!selectedHoldingPosition) {
      setHoldingError("삭제할 보유종목을 선택하세요.", true);
      return;
    }
    
    var code = selectedHoldingPosition.stockCode;
    var positionId = selectedHoldingPosition.positionId;
    
    if (!confirm("보유종목을 삭제하시겠습니까?\n\n[" + code + "]")) {
      return;
    }
    
    var desc = $("#holdingDesc").val().trim();
    
    $.ajax({
      url: window.__URLS.positionDelete,
      method: "POST",
      dataType: "json",
      data: {
        positionId: positionId,
        reasonText: desc || "삭제"
      },
      success: function(res) {
        var u = unwrapSingle(res);
        if (!u.ok) {
          setHoldingError(u.msg || "삭제 실패", true);
          return;
        }
        setHoldingError("보유종목이 삭제되었습니다.", false);
        clearHoldingForm();
        loadHoldingList();
      },
      error: function() {
        setHoldingError("삭제 실패", true);
      }
    });
  }

  function doHoldingAvgDown() {
    if (!selectedHoldingPosition) {
      setHoldingError("물타기할 보유종목을 선택하세요.", true);
      return;
    }
    
    var addQty = parseInt($("#holdingQty").val()) || 0;
    var addPrice = parseFloat($("#holdingPrice").val()) || 0;
    var desc = $("#holdingDesc").val().trim();
    
    if (addQty <= 0) {
      setHoldingError("추가 수량을 입력하세요.", true);
      return;
    }
    if (addPrice <= 0) {
      setHoldingError("추가 단가를 입력하세요.", true);
      return;
    }
    
    var positionId = selectedHoldingPosition.positionId;
    
    $.ajax({
      url: window.__URLS.positionAverageDown,
      method: "POST",
      dataType: "json",
      data: {
        positionId: positionId,
        addQty: addQty,
        addPrice: addPrice,
        reasonText: desc || "물타기"
      },
      success: function(res) {
        var u = unwrapSingle(res);
        if (!u.ok) {
          setHoldingError(u.msg || "물타기 실패", true);
          return;
        }
        setHoldingError("물타기가 완료되었습니다.", false);
        clearHoldingForm();
        loadHoldingList();
      },
      error: function() {
        setHoldingError("물타기 실패", true);
      }
    });
  }

  function toggleHoldingEvents() {
    holdingEventListVisible = !holdingEventListVisible;
    var $wrap = $("#holdingEventListWrap");
    
    if (holdingEventListVisible) {
      loadHoldingEvents();
      $wrap.show();
    } else {
      $wrap.hide();
    }
  }

  function loadHoldingEvents() {
    var filter = $(".holding-ev-filter.is-active").attr("data-evf") || "all";
    var positionId = selectedHoldingPosition ? selectedHoldingPosition.positionId : "";

    if (!positionId) {
      setHoldingError("이력을 보려면 보유종목을 먼저 선택하세요.", true);
      $("#holdingEventList").html('<div class="mini-muted">보유종목 선택 후 이력을 확인할 수 있습니다.</div>');
      return;
    }
    
    var data = {};
    if (positionId) data.positionId = positionId;
    
    $.ajax({
      url: window.__URLS.positionEventList,
      method: "GET",
      dataType: "json",
      data: data,
      success: function(res) {
        var u = unwrapSingle(res);
        if (!u.ok) {
          setHoldingError(u.msg || "이력 조회 실패", true);
          return;
        }
        var rows = u.data || [];
        if (rows.length) {
          renderHoldingEvents(rows, filter);
          return;
        }

        // 이벤트가 비어있으면 거래이력으로 fallback
        $.ajax({
          url: window.__URLS.positionTxnList,
          method: "GET",
          dataType: "json",
          data: data,
          success: function(res2) {
            var u2 = unwrapSingle(res2);
            if (!u2.ok) {
              setHoldingError(u2.msg || "거래이력 조회 실패", true);
              return;
            }
            renderHoldingTxnAsEvents(u2.data || [], filter);
          },
          error: function() {
            setHoldingError("거래이력 조회 실패", true);
          }
        });
      },
      error: function() {
        setHoldingError("이력 조회 실패", true);
      }
    });
  }

  function formatHoldingDate(v) {
    if (!v) return "";
    var d = new Date(v);
    if (!isNaN(d.getTime())) return d.toLocaleString("ko-KR");
    return String(v);
  }

  function renderHoldingTxnAsEvents(txns, filter) {
    var normalized = (txns || []).map(function(t) {
      var action = (t.actionType || t.ACTION_TYPE || "").toUpperCase();
      var type = action;
      if (action === "ADD") type = "ENTER";
      else if (action === "DELETE") type = "EXIT";
      else if (action === "AVERAGE_DOWN") type = "RISK_OFF";

      return {
        eventType: type,
        eventLevel: "INFO",
        message: (action || "TXN") + " | 수량 " + (t.qty || t.QTY || 0) + " | 단가 " + (t.price || t.PRICE || 0),
        eventTime: t.createDate || t.CREATE_DATE
      };
    });
    renderHoldingEvents(normalized, filter);
  }

  function renderHoldingEvents(events, filter) {
    var $list = $("#holdingEventList");
    $list.empty();
    
    if (!events || !events.length) {
      $list.html('<div class="mini-muted">이력이 없습니다.</div>');
      return;
    }
    
    var filtered = events;
    if (filter === "risk") {
      filtered = events.filter(function(e) { 
        return e.eventType === "RISK_OFF" || e.eventType === "ALERT"; 
      });
    } else if (filter === "trend") {
      filtered = events.filter(function(e) { 
        return e.eventType === "TREND_FOLLOW"; 
      });
    }
    
    filtered.forEach(function(ev) {
      var type = ev.eventType || "";
      var level = ev.eventLevel || "INFO";
      var msg = ev.message || "";
      var time = formatHoldingDate(ev.eventTime || ev.createDate || ev.EVENT_TIME || ev.CREATE_DATE);
      
      var levelClass = level === "ERROR" ? "ev-error" : (level === "WARN" ? "ev-warn" : "ev-info");
      
      var $item = $('<div class="holding-event-item ' + levelClass + '"></div>');
      $item.html(
        '<div class="ev-type">' + type + '</div>' +
        '<div class="ev-msg">' + msg + '</div>' +
        '<div class="ev-time">' + time + '</div>'
      );
      
      $list.append($item);
    });
  }

  /* =========================
     Chart
     ========================= */
  // MA 옵션 모달에서 설정한 값 (체크박스 MA 옵션과 별도로 관리)
  window.__MA_PERIODS_OVERRIDE = null;
  window.__MA_CONFIGS_OVERRIDE = null;

  function getMaPeriods() {
    if (window.__MA_PERIODS_OVERRIDE && $.isArray(window.__MA_PERIODS_OVERRIDE) && window.__MA_PERIODS_OVERRIDE.length) {
      return window.__MA_PERIODS_OVERRIDE.slice();
    }
  }

  function applyMaAndRerenderIfPossible() {
    if (!window.ChartScript) return;

    var maPeriods = getMaPeriods();
    var opt = { maPeriods: maPeriods };
    if (window.__MA_CONFIGS_OVERRIDE && $.isArray(window.__MA_CONFIGS_OVERRIDE)) {
      opt.maConfigs = window.__MA_CONFIGS_OVERRIDE.slice();
    }

    if (typeof ChartScript.setOptions === "function") {
      ChartScript.setOptions(opt);
    }

    if (ChartScript.lastData && ChartScript.lastData.length && typeof ChartScript.renderKisChart === "function") {
      var q = ChartScript.lastQuery || {};
      ChartScript.renderKisChart(ChartScript.lastData, q.periodDivCode || $("#periodDivCode").val());
    }
  }



  /* =========================
     Chart Header UI (차트 위 레이아웃)
     - 기존 기능 영향 없이, Highstock 차트 렌더 후 헤더만 갱신
     ========================= */
  function kisComma(n) {
    if (n === null || n === undefined || n === "" || isNaN(n)) return "-";
    var s = ("" + n);
    if (s.indexOf('.') >= 0) {
      var parts = s.split('.');
      parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
      return parts[0] + "." + parts[1];
    }
    return s.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  }

  function kisRound(n, d) {
    if (n === null || n === undefined || n === "" || isNaN(n)) return null;
    var p = Math.pow(10, d || 0);
    return Math.round(n * p) / p;
  }

  function kisFormatSigned(n, digits) {
    if (n === null || n === undefined || n === "" || isNaN(n)) return "-";
    var v = kisRound(Number(n), digits || 2);
    var s = (v > 0 ? "+" : (v < 0 ? "-" : ""));
    return s + kisComma(Math.abs(v));
  }

  function kisFormatPct(n, digits) {
    if (n === null || n === undefined || n === "" || isNaN(n)) return "-";
    var v = kisRound(Number(n), digits || 2);
    var s = (v > 0 ? "+" : (v < 0 ? "-" : ""));
    return s + kisComma(Math.abs(v)) + "%";
  }
  
  function kisFmtUnitKR(n) {
    // 10,000 = 만 / 100,000,000 = 억 / 1,000,000,000,000 = 조
    if (n === null || n === undefined || n === "" || isNaN(n)) return "-";

    var v = Number(n);
    var sign = v < 0 ? "-" : "";
    v = Math.abs(v);

    // 소숫점 입력이 와도 단위 계산은 정수 기준으로 처리
    v = Math.floor(v);

    var jo = Math.floor(v / 1e12);
    v = v % 1e12;

    var eok = Math.floor(v / 1e8);
    v = v % 1e8;

    // 만 단위는 무조건 정수(소숫점 금지)
    var man = parseInt(v / 1e4, 10);

    var out = "";
    if (jo) out += jo + "조 ";
    if (eok) out += eok + "억 ";
    if (man) out += man + "만";

    out = out.trim();
    if (!out) out = "0";

    return sign + out;
  }


  function kisWeekdayKor(i) {
    var arr = ["일", "월", "화", "수", "목", "금", "토"];
    return arr[i] || "";
  }

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

  function kisFmtDt(x, periodDivCode) {
    if (!x) return "-";
    var d = new Date(x);
    if (isNaN(d.getTime())) return "-";
    var y = d.getFullYear();
    var m = ("0" + (d.getMonth() + 1)).slice(-2);
    var dd = ("0" + d.getDate()).slice(-2);
    var w = kisWeekdayKor(d.getDay());

    // 30분봉(T) 같이 시간형이면 HH:mm 포함
    if (isMinuteDivCode(periodDivCode)) {
      var hh = ("0" + d.getHours()).slice(-2);
      var mm = ("0" + d.getMinutes()).slice(-2);
      return y + "." + m + "." + dd + "(" + w + ") " + hh + ":" + mm;
    }
    return y + "." + m + "." + dd + "(" + w + ")";
  }

  function kisPickKisChart() {
    if (!window.Highcharts || !Highcharts.charts) return null;
    for (var i = Highcharts.charts.length - 1; i >= 0; i--) {
      var c = Highcharts.charts[i];
      if (c && c.renderTo && c.renderTo.id === "kisChartContainer") {
        return c;
      }
    }
    return null;
  }

  function kisFindOhlcSeries(chart) {
    if (!chart || !chart.series) return null;
    for (var i = 0; i < chart.series.length; i++) {
      var s = chart.series[i];
      if (!s || !s.visible) continue;
      if (s.type === "candlestick" || s.type === "ohlc") return s;
    }
    // fallback
    return chart.series[0] || null;
  }

  function kisGetPointOhlc(pt) {
    if (!pt) return null;
    var o = (pt.open !== undefined) ? pt.open : (pt.y && pt.y.length ? pt.y[0] : null);
    var h = (pt.high !== undefined) ? pt.high : (pt.y && pt.y.length ? pt.y[1] : null);
    var l = (pt.low !== undefined) ? pt.low : (pt.y && pt.y.length ? pt.y[2] : null);
    var c = (pt.close !== undefined) ? pt.close : (pt.y && pt.y.length ? pt.y[3] : null);
    return { o: o, h: h, l: l, c: c, x: pt.x };
  }

  function kisPrevClose(series, idx) {
    if (!series) return null;
    var yData = series.yData;
    if (yData && idx > 0 && yData[idx - 1] && yData[idx - 1].length) {
      return yData[idx - 1][3];
    }
    if (series.points && idx > 0 && series.points[idx - 1]) {
      var p = series.points[idx - 1];
      return (p.close !== undefined) ? p.close : (p.y && p.y.length ? p.y[3] : null);
    }
    return null;
  }

  function kisSetUpDown($el, n) {
    $el.removeClass("kis-up kis-down");
    if (n === null || n === undefined || n === "" || isNaN(n)) return;
    var v = Number(n);
    if (v > 0) $el.addClass("kis-up");
    else if (v < 0) $el.addClass("kis-down");
  }

  function kisSetText($el, txt) {
    $el.text((txt === null || txt === undefined || txt === "") ? "-" : txt);
  }

  function kisUpdateMaBadge() {
    var active = getMaPeriods();
    $("#kisChartHeader .kis-ma").each(function () {
      var p = parseInt($(this).attr("data-ma"), 10);
      if (active.indexOf(p) >= 0) $(this).addClass("is-on");
      else $(this).removeClass("is-on");
    });
  }

  function kisUpdateHeaderByPoint(chart, series, pt, periodDivCode) {
    if (!chart || !series || !pt) return;

    var ohlc = kisGetPointOhlc(pt);
    if (!ohlc) return;

    var idx = (pt.index !== undefined && pt.index !== null) ? pt.index : -1;
    if (idx < 0 && series.xData && series.xData.length) {
      // fallback: find index
      for (var i = 0; i < series.xData.length; i++) {
        if (series.xData[i] === pt.x) { idx = i; break; }
      }
    }

    var prev = kisPrevClose(series, idx);

    // 날짜
    kisSetText($("#kisHdrDt"), kisFmtDt(ohlc.x, periodDivCode));

    function fmtOhlc(val) {
      if (val === null || val === undefined || val === "" || isNaN(val)) return "-";
      var base = prev;
      var pct = (base && !isNaN(base) && Number(base) !== 0) ? ((Number(val) - Number(base)) / Number(base) * 100) : null;
      var vtxt = kisComma(kisRound(Number(val), 2));
      if (pct === null) return vtxt;
      return vtxt + " (" + kisFormatPct(pct, 2) + ")";
    }

    var $o = $("#kisHdrO"), $h = $("#kisHdrH"), $l = $("#kisHdrL"), $c = $("#kisHdrC");
    kisSetText($o, fmtOhlc(ohlc.o));
    kisSetText($h, fmtOhlc(ohlc.h));
    kisSetText($l, fmtOhlc(ohlc.l));
    kisSetText($c, fmtOhlc(ohlc.c));

    kisSetUpDown($o, (prev ? (ohlc.o - prev) : 0));
    kisSetUpDown($h, (prev ? (ohlc.h - prev) : 0));
    kisSetUpDown($l, (prev ? (ohlc.l - prev) : 0));
    kisSetUpDown($c, (prev ? (ohlc.c - prev) : 0));
  }

  function kisUpdateTopLine(chart, series) {
    if (!chart || !series) return;

    var len = (series.yData && series.yData.length) ? series.yData.length : 0;
    if (!len) return;

    var last = series.yData[len - 1];
    var lastClose = (last && last.length) ? last[3] : null;
    var prevClose = (len > 1 && series.yData[len - 2] && series.yData[len - 2].length) ? series.yData[len - 2][3] : null;

    var diff = (lastClose !== null && prevClose !== null && !isNaN(lastClose) && !isNaN(prevClose)) ? (Number(lastClose) - Number(prevClose)) : null;
    var pct = (diff !== null && prevClose && Number(prevClose) !== 0) ? (diff / Number(prevClose) * 100) : null;

    kisSetText($("#kisHdrNow"), (lastClose === null ? "-" : kisComma(kisRound(Number(lastClose), 2))));
    kisSetText($("#kisHdrPct"), (pct === null ? "-" : kisFormatPct(pct, 2)));
    kisSetText($("#kisHdrDiff"), (diff === null ? "-" : kisFormatSigned(diff, 2)));

    kisSetUpDown($("#kisHdrNow"), diff);
    kisSetUpDown($("#kisHdrPct"), pct);
    kisSetUpDown($("#kisHdrDiff"), diff);

    // 거래량/대금: volume series가 있으면 표시
    var volSeries = null;
    for (var i = 0; i < chart.series.length; i++) {
      var s = chart.series[i];
      if (!s || !s.visible) continue;
      if (s.type === "column" || (s.name && ("" + s.name).toLowerCase().indexOf("volume") >= 0)) {
        volSeries = s;
        break;
      }
    }

    if (volSeries && volSeries.yData && volSeries.yData.length) {
      var v = volSeries.yData[volSeries.yData.length - 1];
      kisSetText($("#kisHdrVol"), "거래량 " + kisFmtUnitKR(v));
    } else {
      kisSetText($("#kisHdrVol"), "거래량 -");
    }
    kisSetText($("#kisHdrAmt"), "거래대금 -");

    // 이름/코드: 기존 숨김 영역이 채워져있으면 활용
    var nm = ($.trim($("#kisStockName").text()) || "");
    var code = ($.trim($("#stockCode").val()) || "-");

    if (!nm) nm = code;
    $("#kisHdrName").text(nm);
    $("#kisHdrCode").text(code);

    // 마켓(간단 기본값)
    var marketText = "";
    if (code === "KOSPI" || code === "KOSDAQ") marketText = "국내지수";
    else marketText = "";
    $("#kisHdrMarket").text(marketText);

    // ChartScript 호환용(기존 ID)
    $("#kisStockPrice").text($("#kisHdrNow").text());
    $("#kisStockDate").text($("#kisHdrDt").text());
  }

  function kisBindHeaderToChart(chart, periodDivCode) {
    var series = kisFindOhlcSeries(chart);
    if (!series) return;

    // 초기 표시
    kisUpdateMaBadge();
    kisUpdateTopLine(chart, series);

    // 마지막 캔들 기준으로 2번째 줄 세팅
    if (series.points && series.points.length) {
      kisUpdateHeaderByPoint(chart, series, series.points[series.points.length - 1], periodDivCode);
    } else if (series.xData && series.yData && series.xData.length) {
      // points가 아직 없을 때를 대비
      var idx = series.xData.length - 1;
      var fake = { x: series.xData[idx], y: series.yData[idx], index: idx };
      kisUpdateHeaderByPoint(chart, series, fake, periodDivCode);
    }

    // hover 시 갱신
    try {
      series.update({
        point: {
          events: {
            mouseOver: function () {
              kisUpdateHeaderByPoint(chart, series, this, periodDivCode);
            }
          }
        }
      }, false);
      chart.redraw(false);
    } catch (e) {
      // ignore
    }
  }

  function kisHookRenderForHeader() {
    if (!window.ChartScript || typeof ChartScript.renderKisChart !== "function") return;
    if (ChartScript.__kisHeaderHooked) return;

    var _orig = ChartScript.renderKisChart;
    ChartScript.renderKisChart = function () {
      var r = _orig.apply(this, arguments);
      setTimeout(function () {
        var c = kisPickKisChart();
        var q = (window.ChartScript && ChartScript.lastQuery) ? ChartScript.lastQuery : {};
        kisBindHeaderToChart(c, q.periodDivCode || $("#periodDivCode").val());
      }, 0);
      return r;
    };

    ChartScript.__kisHeaderHooked = true;
  }


  function doSearch() {

    var stockCode = ($("#stockCode").val() || "").trim();
    var periodDivCode = $("#periodDivCode").val();

    if (stockCode && __LAST_SEARCHED_STOCK_CODE && __LAST_SEARCHED_STOCK_CODE !== stockCode) {
      setDefaultDates(); // 종목 변경 시 기본 1년6개월
    }

    function inferLocaleByCode(code) {
      var c = (code || "").trim().toUpperCase();
      if (!c) return { country: "", market: "" };
      if (/^[A-Z][A-Z0-9\.\-]{0,9}$/.test(c) && !/^\d+$/.test(c)) {
        return { country: "US", market: "NAS" };
      }
      return { country: "KR", market: "KRX" };
    }

    var fromDate = fmtYmdToPlain($("#fromDate").val());
    var toDate = fmtYmdToPlain($("#toDate").val());

    if (isMinuteDivCode(periodDivCode)) {
      if (!fromDate || !toDate) {
        applyMinuteDefaultRange(5);
        fromDate = fmtYmdToPlain($("#fromDate").val());
        toDate = fmtYmdToPlain($("#toDate").val());
      }
    }

    if (!stockCode) {
      alert("종목코드를 입력하세요.");
      return;
    }
    if (!fromDate || !toDate) {
      alert("시작일/종료일을 입력하세요.");
      return;
    }

    __LAST_SEARCHED_STOCK_CODE = stockCode;

    var selectedCountry = (window.__SELECTED_STOCK_COUNTRY || "").trim();
    var selectedMarket = (window.__SELECTED_STOCK_MARKET || "").trim();
    var inf = inferLocaleByCode(stockCode);

    // 코드 기준 시장/국가와 현재 선택값이 충돌하면 코드 기준으로 보정
    // (예: 005930을 US/NAS로 들고가서 '조회된 차트 데이터가 없습니다' 발생)
    if (!selectedCountry || !selectedMarket || (inf.country && selectedCountry && inf.country !== selectedCountry)) {
      if (inf.country) selectedCountry = inf.country;
      if (inf.market) selectedMarket = inf.market;
      window.__SELECTED_STOCK_COUNTRY = selectedCountry;
      window.__SELECTED_STOCK_MARKET = selectedMarket;
    }

    try {
      saveLastViewState({
        stockCode: stockCode,
        periodDivCode: periodDivCode,
        fromDate: fromDate,
        toDate: toDate,
        stockCountryCode: selectedCountry,
        stockMarket: selectedMarket
      });
    } catch (e) {}

    function buildChartRealtimeToken() {
      var code = ($("#stockCode").val() || "").trim();
      if (!code) return "";
      // skip realtime socket for index/FX codes (no watchlist quote)
      var c = code.toUpperCase();
      if (c.charAt(0) == "." || c == "0001" || c == "1001" || c == "2001" || c == "KOSPI" || c == "KOSDAQ" || c == "KOSPI200" || c == "USDKRW") {
        return "";
      }

      var q = (window.ChartScript && ChartScript.lastQuery) ? ChartScript.lastQuery : {};
      var country = (q.stockCountryCode || window.__SELECTED_STOCK_COUNTRY || "").trim();
      var market = (q.stockMarket || window.__SELECTED_STOCK_MARKET || "").trim();

      if (!country) {
        return code;
      }

      if (!market) {
        market = (country === "KR") ? "KRX" : "NAS";
      }

      return country + "|" + market + "|" + code;
    }

    // ChartScript(로컬) + Highstock(CDN)은 필요 시 동적 로딩
    ensureChartScriptLoaded()
      .then(function () {

        // ChartScript가 로드된 이후에 훅을 걸어야 실제로 적용된다.
        try {
          kisHookRenderForHeader();
        } catch (e) {}

        if (typeof ChartScript.setOptions === "function") {
          var opt = { maPeriods: getMaPeriods() };
          if (window.__MA_CONFIGS_OVERRIDE && $.isArray(window.__MA_CONFIGS_OVERRIDE)) {
            opt.maConfigs = window.__MA_CONFIGS_OVERRIDE.slice();
          }
          ChartScript.setOptions(opt);
        }

        if (window.KisDashboardChartRenderer && typeof window.KisDashboardChartRenderer.render === "function") {
          return Promise.resolve();
        }
        return ensureHighstockLoaded();
      })
      .then(function () {
        ChartScript.loadKisItemchartprice({
          stockCode: stockCode,
          fromDate: fromDate,
          toDate: toDate,
          periodDivCode: periodDivCode,
          orgAdjPrc: "1",
          stockCountryCode: selectedCountry,
          stockMarket: selectedMarket
        });

        // Realtime current price for chart header
        try {
          if (window.ChartPriceRealtime && typeof ChartPriceRealtime.connect === "function") {
            ChartPriceRealtime.connect(buildChartRealtimeToken());
          }
        } catch (e) {}
      })
      .catch(function (e) {
        try { console.error(e); } catch (ex) {}
        alert("차트 모듈 로딩에 실패했습니다. 네트워크 상태를 확인해 주세요.");
      });
  }
  
  /* =========================
     Right panel toggle (responsive)
     ========================= */
  function toggleRightPanel() {
    $("#rightPanel").toggleClass("is-collapsed");
  }

  function toggleWgPanel(openYn) {
    var $lp = $("#leftPanel");
    var isOpen = $lp.hasClass("wg-open");
    var targetOpen = (typeof openYn === "boolean") ? openYn : !isOpen;

    if (targetOpen) {
      $lp.addClass("wg-open");
      // 슬라이드 오픈 시 그룹 관리영역 초기화 + 최신 그룹 로딩
      setWgStatus("", false);
      clearWgForm();
      renderGroupManageList([]);
      loadWatchlistGroups();
    } else {
      $lp.removeClass("wg-open");
    }
  }


  /* =========================
     Chart Option: MA 옵션 모달
     (chartTest_vs3.jsp 의 '이동평균선(MA) 옵션 설정' 기능만 이식)
     ========================= */
  var CHART_ID = "KIS_ITEMCHART";
  var SERIES_TYPE_MA = "";
  var MA_LINEWIDTH_DEFAULT = 2.5;
  var MA_LINEWIDTH_MIN = 0.5;
  var MA_LINEWIDTH_MAX = 6;

  function openChartOptionsPanel() {
    var $panel = $("#chartOptionsModal");
    var $backdrop = $("#chartOptBackdrop");
    if (!$panel.length) return;
    $panel.addClass("open").attr("aria-hidden", "false");
    $backdrop.addClass("open");
    $("body").addClass("chart-opt-open");
    $("#btnChartOptions").addClass("active");
    $panel.trigger("show.bs.modal");
  }

  function closeChartOptionsPanel() {
    var $panel = $("#chartOptionsModal");
    var $backdrop = $("#chartOptBackdrop");
    $panel.removeClass("open").attr("aria-hidden", "true");
    $backdrop.removeClass("open");
    $("body").removeClass("chart-opt-open");
    $("#btnChartOptions").removeClass("active");
  }

  function normalizeMaLineWidth(v) {
    var n = parseFloat(v);
    if (isNaN(n)) n = MA_LINEWIDTH_DEFAULT;
    if (n < MA_LINEWIDTH_MIN) n = MA_LINEWIDTH_MIN;
    if (n > MA_LINEWIDTH_MAX) n = MA_LINEWIDTH_MAX;
    return Math.round(n * 2) / 2;
  }


  function createMaRow2(opt, index) {
	  opt = opt || {};
	  var enabledYn = (opt.enabledYn === "Y" || opt.enabledYn === true);
	  var label = opt.seriesLabel || "";
	  var period = opt.seriesPeriod || "";
	  var color = opt.seriesColor || "#000000";
	  var lineWidth = (opt.lineWidth !== undefined && opt.lineWidth !== null) ? opt.lineWidth : MA_LINEWIDTH_DEFAULT;
	  lineWidth = normalizeMaLineWidth(lineWidth);
	  var seriesKey = opt.seriesKey || "";

	  var $row = $("<div class='ma-row2'/>").attr("data-series-key", seriesKey);

	  var $chk = $("<input type='checkbox' class='ma-enabled'/>").prop("checked", enabledYn);
	  var $chkWrap = $("<label class='ma-check' title='사용 여부'/>");
	  $chkWrap.append($chk).append("<span class='box'></span>");

	  var $order = $("<div class='ma-order'/>").text((index + 1) + "번");

	  var $labelInput = $("<input type='text' class='form-control input-sm ma-label'/>").val(label);
	  var $periodInput = $("<input type='number' class='form-control input-sm ma-period' min='1'/>").val(period);
	  var $widthInput = $("<input type='number' class='form-control input-sm ma-width' min='0.5' max='6' step='0.5' aria-label='Line width'/>").val(lineWidth);

	  var $colorInput = $("<input type='hidden' class='ma-color'/>").val(color);
	  var $colorPicker = $("<input type='color' class='ma-color-picker'/>").val(color);

	  var $colorBtn = $("<button type='button' class='ma-colorbtn' title='색상 선택'/>");
	  var $swatch = $("<span class='swatch'/>").css("background", color);

	  // 버튼 안에 컬러피커를 넣어서 "직접 클릭"되게 만듦 (가장 안정적)
	  $colorBtn.append($swatch);
	  $colorBtn.append($colorPicker);

	  $colorPicker.on("input change", function () {
	    var v = $(this).val() || "#000000";
	    $colorInput.val(v);
	    $swatch.css("background", v);
	  });

	  (function syncColor() {
	    var v = ($colorInput.val() || "").trim();
	    if (v && v.charAt(0) !== "#") v = "#" + v;
	    if (!v) v = "#000000";
	    $colorInput.val(v);
	    $colorPicker.val(v);
	    $swatch.css("background", v);
	  })();

	  var $delBtn = $("<button type='button' class='btn btn-default btn-xs btn-delete-ma'>삭제</button>").on("click", function () {
	    if (confirm("해당 MA 라인을 삭제하시겠습니까?")) {
	      $row.remove();
	    scheduleMaAutoSave();
	    }
	  });

	  $row.append($chkWrap);
	  $row.append($order);
	  $row.append($labelInput);
	  $row.append($periodInput);
	  $row.append($widthInput);
	  $row.append($colorInput);   // hidden
	  $row.append($colorBtn);     // visible + color input overlay
	  $row.append($delBtn);

	  return $row;
	}



  function renderMaOptions(list) {
    var $list = $("#maRowList");
    $list.empty();

    list = $.isArray(list) ? list.slice() : [];
    list.sort(function (a, b) {
      var oa = (a.displayOrder != null ? a.displayOrder : (a.seriesPeriod || 0));
      var ob = (b.displayOrder != null ? b.displayOrder : (b.seriesPeriod || 0));
      return oa - ob;
    });

    for (var i = 0; i < list.length; i++) {
      $list.append(createMaRow2(list[i], i));
    }
  }

  function collectMaOptionsFromUi() {
    var list = [];
    var hasInvalid = false;
    $("#maRowList .ma-row2").each(function (idx) {
      var $row = $(this);
      var label = $.trim($row.find(".ma-label").val());
      var period = parseInt($row.find(".ma-period").val(), 10);
      var color = $.trim($row.find(".ma-color").val()) || "#000000";
      var lineWidth = normalizeMaLineWidth($row.find(".ma-width").val());
      var enabledYn = $row.find(".ma-enabled").is(":checked") ? "Y" : "N";
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
        enabledYn: enabledYn,
        displayOrder: idx + 1
      });
    });
    return { list: list, hasInvalid: hasInvalid };
  }

  
  function hexToRgba(hex, alpha) {
    if (!hex) return "rgba(0,0,0," + alpha + ")";
    var h = ("" + hex).trim();
    if (h.charAt(0) === "#") h = h.substring(1);
    if (h.length === 3) {
      h = h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
    }
    if (h.length !== 6) return "rgba(0,0,0," + alpha + ")";
    var r = parseInt(h.substring(0, 2), 16);
    var g = parseInt(h.substring(2, 4), 16);
    var b = parseInt(h.substring(4, 6), 16);
    return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
  }

  function renderMaLegendFromOptions(list) {
    var $wrap = $("#kisMaLegend");
    if (!$wrap.length) return;

    // 기존 배지 제거(라벨 제외)
    $wrap.find(".kis-ma").remove();

    list = $.isArray(list) ? list : [];
    var enabledList = $.grep(list, function (opt) {
      return (opt.enabledYn === "Y" || opt.enabledYn === true);
    });

    enabledList.sort(function (a, b) {
      var oa = (a.displayOrder != null ? a.displayOrder : (a.seriesPeriod || 0));
      var ob = (b.displayOrder != null ? b.displayOrder : (b.seriesPeriod || 0));
      return oa - ob;
    });

    for (var i = 0; i < enabledList.length; i++) {
      var opt = enabledList[i] || {};
      var p = parseInt(opt.seriesPeriod || 0, 10);
      if (!p) continue;

      var c = (opt.seriesColor || "").toString().trim();
      if (!c) c = "#334155";

      var $b = $("<span class='kis-ma'></span>");
      $b.attr("data-ma", p);
      $b.text(p);
      $b.attr("title", (opt.seriesLabel ? opt.seriesLabel : ("MA" + p)));

      // 색상은 팝업 설정과 동일하게 반영
      $b.css({
        color: c,
        borderColor: c
      });

      $wrap.append($b);
    }

    // 현재 활성 상태(is-on) 갱신
    if (typeof kisUpdateMaBadge === "function") {
      kisUpdateMaBadge();
    }
  }


function applyMaOptionsToChart(list) {
    if (!$.isArray(list)) return;

    var enabledList = $.grep(list, function (opt) {
      return (opt.enabledYn === "Y" || opt.enabledYn === true);
    });

    for (var i = 0; i < enabledList.length; i++) {
      enabledList[i].lineWidth = normalizeMaLineWidth(enabledList[i].lineWidth);
    }

    enabledList.sort(function (a, b) {
      return (a.seriesPeriod || 0) - (b.seriesPeriod || 0);
    });

    var maPeriods = $.map(enabledList, function (opt) {
      return opt.seriesPeriod || 0;
    });

    // override 저장 (체크박스와 독립)
    window.__MA_PERIODS_OVERRIDE = maPeriods;
    window.__MA_CONFIGS_OVERRIDE = enabledList;

    // 헤더 MA 뱃지(색/기간)도 DB 옵션과 동일하게 동기화
    renderMaLegendFromOptions(list);

    if (window.ChartScript && typeof ChartScript.setOptions === "function") {
      ChartScript.setOptions({ maPeriods: maPeriods, maConfigs: enabledList });
    }

    if (window.ChartScript && ChartScript.lastData && ChartScript.lastData.length && typeof ChartScript.renderKisChart === "function") {
      var q = ChartScript.lastQuery || {};
      ChartScript.renderKisChart(ChartScript.lastData, q.periodDivCode || $("#periodDivCode").val());
    }
  }

  function loadMaOptionsFromDb(forceDefault, showPanel) {
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
        if (!res) {
          alert("차트 옵션 응답이 없습니다.");
          return;
        }
        if (res.system_code && ("" + res.system_code) !== "0000") {
          alert("차트 옵션 조회 오류: " + (res.system_msg || ""));
          return;
        }

        var list = $.isArray(res.data) ? res.data : [];
        renderMaOptions(list);
        if (window.ChartFeatureToggle && typeof ChartFeatureToggle.applyFromModal === "function") {
          ChartFeatureToggle.applyFromModal(false);
        }
        applyMaOptionsToChart(list);

        if (showPanel) {
          openChartOptionsPanel();
        }
      },
      error: function () {
        alert("차트 옵션 조회 중 오류가 발생했습니다.");
      }
    });
  }

  function saveMaOptions(closeAfter, isAuto) {
    var result = collectMaOptionsFromUi();
    var list = result.list || [];
    var hasInvalid = result.hasInvalid === true;

    if (hasInvalid) {
      if (isAuto === true) {
        return;
      }
      alert("MA 라인 입력값이 비어 있습니다. 라벨/기간을 모두 입력해 주세요.");
      return;
    }

  if (isAuto === true) {
    // 자동 저장은 임시 입력(비어있음)으로 전체 삭제되는 상황을 방지
    if (!list.length) {
      return;
    }
  } else {
    if (!list.length) {
      if (!confirm("모든 MA 라인을 삭제하시겠습니까?")) {
        return;
      }
      }
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
        if (res && res.system_code && ("" + res.system_code) !== "0000") {
        if (isAuto !== true) {
          alert("차트 옵션 저장 오류: " + (res.system_msg || ""));
        }
          return;
        }
        applyMaOptionsToChart(list);
      if (closeAfter === true) {
        closeChartOptionsPanel();
      }
      },
      error: function () {
      if (isAuto !== true) {
        alert("차트 옵션 저장 중 오류가 발생했습니다.");
      }
    }
  });
}

 var __maAutoTimer = null;
function scheduleMaAutoSave() {
    if (__maAutoTimer) {
      clearTimeout(__maAutoTimer);
      __maAutoTimer = null;
    }
    __maAutoTimer = setTimeout(function () {
      saveMaOptions(false, true);
      __maAutoTimer = null;
    }, 300);

    return;

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
        if (res && res.system_code && ("" + res.system_code) !== "0000") {
          alert("차트 옵션 저장 오류: " + (res.system_msg || ""));
          return;
        }
        applyMaOptionsToChart(list);
	        closeChartOptionsPanel();
	      },
      error: function () {
        alert("차트 옵션 저장 중 오류가 발생했습니다.");
      }
    });
  }



  // =========================================================
  // Mobile: 관심종목/시장요약 패널을 버튼으로 접고 펼치기
  // - 기능 변경 최소화를 위해 CSS 클래스만 토글
  // =========================================================
  function initMobilePanelToggle() {
    var $btnWatch = $("#btnMobileOpenWatch");
    var $btnSummary = $("#btnMobileOpenSummary");
    var $backdrop = $("#mobilePanelBackdrop");
    var $buttons = $("#mobilePanelButtons");

    if ($btnWatch.length === 0 || $btnSummary.length === 0 || $backdrop.length === 0 || $buttons.length === 0) {
      return;
    }

    function isMobileLayout() {
      return window.matchMedia && window.matchMedia("(max-width: 860px)").matches;
    }

    function closePanels() {
      $("body").removeClass("mobile-open-left mobile-open-right");
      $buttons.attr("aria-hidden", "false");
      $backdrop.attr("aria-hidden", "true");
    }

    function openPanel(type) {
      if (!isMobileLayout()) return;

      if (type === "left") {
        $("body").addClass("mobile-open-left").removeClass("mobile-open-right");
      } else if (type === "right") {
        $("body").addClass("mobile-open-right").removeClass("mobile-open-left");
      }
      $buttons.attr("aria-hidden", "false");
      $backdrop.attr("aria-hidden", "false");
    }

    $btnWatch.off("click.mobilePanel").on("click.mobilePanel", function () {
      if ($("body").hasClass("mobile-open-left")) closePanels();
      else openPanel("left");
    });

    $btnSummary.off("click.mobilePanel").on("click.mobilePanel", function () {
      if ($("body").hasClass("mobile-open-right")) closePanels();
      else openPanel("right");
    });

    $backdrop.off("click.mobilePanel").on("click.mobilePanel", function () {
      closePanels();
    });

    $(document).off("keydown.mobilePanel").on("keydown.mobilePanel", function (e) {
      if (e.key === "Escape") closePanels();
    });

    // 화면이 커지면(데스크탑) 열림 상태 해제
    $(window).off("resize.mobilePanel").on("resize.mobilePanel", function () {
      if (!isMobileLayout()) {
        closePanels();
      }
    });

    // 초기 상태: 모바일이면 닫힌 상태 유지 (버튼만 노출)
    if (isMobileLayout()) {
      closePanels();
    }
  }

  $(function () {
    if (window.ChartFeatureToggle && typeof ChartFeatureToggle.init === "function") {
      ChartFeatureToggle.init();
    }

    // Highstock 상단 옵션(기간 버튼/메뉴) 숨김
    if (window.Highcharts && typeof Highcharts.setOptions === 'function') {
      Highcharts.setOptions({
        rangeSelector: { enabled: false },
        exporting: {
          enabled: false,
          buttons: {
            contextButton: { enabled: false }
          }
        }
      });
    }

    // 차트 헤더(상단 레이아웃) 훅 등록
    kisHookRenderForHeader();

    initMobilePanelToggle();

    setDefaultDates();
		    
	    // 차트 기능 토글 - 골든/데드 크로스(기간 선택) [DB 저장]
var SERIES_TYPE_CROSS = "CROSS";
var __crossSaveTimer = null;
	
	function normalizeCrossPeriods() {
	  var sp = parseInt($("#optCrossShort").val(), 10);
	  var lp = parseInt($("#optCrossLong").val(), 10);
	
	  if (!sp || sp < 1) sp = 5;
	  if (!lp || lp < 1) lp = 20;
	
	  // 동일하면 long을 다음 후보로 밀기
	  if (sp === lp) {
	    var candidates = [5, 10, 20, 60, 120, 240];
	    var idx = candidates.indexOf(lp);
	    if (idx >= 0 && idx < candidates.length - 1) {
	      lp = candidates[idx + 1];
	    } else {
	      // 마지막이면 short를 이전 후보로 당기기
	      idx = candidates.indexOf(sp);
	      if (idx > 0) sp = candidates[idx - 1];
	    }
	  }
	
	  // short > long 이면 swap
	  if (sp > lp) {
	    var t = sp; sp = lp; lp = t;
	  }
	
	  $("#optCrossShort").val(String(sp));
	  $("#optCrossLong").val(String(lp));
	  $("#crossPairLabel").text("(" + sp + "/" + lp + ")");
	  return { sp: sp, lp: lp };
	}
	
	function setCrossUiEnabled(enabled) {
	  $("#optCrossShort").prop("disabled", !enabled);
	  $("#optCrossLong").prop("disabled", !enabled);
	  $("#crossPairLabel").css("opacity", enabled ? 0.7 : 0.35);
	}
	
function applyCrossToChart(enabled, sp, lp, shouldRender) {
	  if (window.ChartScript && typeof ChartScript.setOptions === "function") {
	    ChartScript.setOptions({
	      crossEnabled: enabled,
      crossShortPeriod: sp,
      crossLongPeriod: lp
    });

    if (shouldRender && ChartScript.lastData && ChartScript.lastQuery) {
      ChartScript.renderKisChart(ChartScript.lastData, ChartScript.lastQuery.periodDivCode);
    }
	  }
	}
	
function loadCrossOptionsFromDb(forceDefault, applyToChart) {
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
      if (!res || (res.system_code && ("" + res.system_code) !== "0000")) {
        return;
      }

      var list = $.isArray(res.data) ? res.data : [];
      var enabled = false;
      var sp = 5;
      var lp = 20;

      for (var i = 0; i < list.length; i++) {
        var it = list[i] || {};
        var k = (it.seriesKey || "").toString();
        if (k === "crossSignals") enabled = ((it.enabledYn || "") === "Y");
        if (k === "crossShort") sp = parseInt(it.seriesPeriod || 5, 10);
        if (k === "crossLong") lp = parseInt(it.seriesPeriod || 20, 10);
      }

      $("#optCrossSignals").prop("checked", enabled);
      $("#optCrossShort").val(String(sp));
      $("#optCrossLong").val(String(lp));

	  var p = normalizeCrossPeriods();
	  setCrossUiEnabled(enabled);
	
      if (applyToChart) {
        applyCrossToChart(enabled, p.sp, p.lp, false);
      }
    }
  });
}

function saveCrossOptionsToDb(enabled, sp, lp) {
  var list = [
    { seriesKey: "crossSignals", seriesLabel: "골든/데드 크로스", seriesPeriod: 0, seriesColor: null, enabledYn: enabled ? "Y" : "N", displayOrder: 1 },
    { seriesKey: "crossShort", seriesLabel: "단기선", seriesPeriod: sp, seriesColor: null, enabledYn: "Y", displayOrder: 2 },
    { seriesKey: "crossLong", seriesLabel: "장기선", seriesPeriod: lp, seriesColor: null, enabledYn: "Y", displayOrder: 3 }
  ];

  $.ajax({
    url: window.__URLS.kisItemchartpriceOptionSave,
    type: "POST",
    dataType: "json",
    data: {
      chartId: CHART_ID,
      seriesType: SERIES_TYPE_CROSS,
      optionsJson: JSON.stringify(list)
    }
  });
}

function scheduleCrossSave(enabled, sp, lp) {
  if (__crossSaveTimer) {
    clearTimeout(__crossSaveTimer);
    __crossSaveTimer = null;
  }
  __crossSaveTimer = setTimeout(function () {
    saveCrossOptionsToDb(enabled, sp, lp);
    __crossSaveTimer = null;
  }, 150);
}

function applyCrossFromUi(shouldRender) {
  var enabled = $("#optCrossSignals").is(":checked");
  var p = normalizeCrossPeriods();
  setCrossUiEnabled(enabled);
  applyCrossToChart(enabled, p.sp, p.lp, shouldRender === true);
  scheduleCrossSave(enabled, p.sp, p.lp);
	}
	
	$("#optCrossSignals").off("change.cross").on("change.cross", function () {
  applyCrossFromUi(true);
	});
	
	$("#optCrossShort, #optCrossLong").off("change.cross").on("change.cross", function () {
  applyCrossFromUi(true);
	});
	
// 페이지 로드시 DB 로드 -> 차트 옵션 적용
loadCrossOptionsFromDb(false, true);

// 옵션 패널 오픈 시 DB 기준으로 동기화
$("#chartOptionsModal").off("show.bs.modal.cross").on("show.bs.modal.cross", function () {
  loadCrossOptionsFromDb(false, false);
});


// 이동평균선(MA) 옵션 설정 패널
$("#btnChartOptions").on("click", function () {
  loadMaOptionsFromDb(false, true);
});

$("#btnChartOptClose, #chartOptBackdrop").on("click", function () {
  closeChartOptionsPanel();
});

$(document).off("keydown.chartOpt").on("keydown.chartOpt", function (e) {
  if (e.key === "Escape") closeChartOptionsPanel();
});

// 모달 내 버튼
    $("#btnAddMaLine").on("click", function () {
      var $list = $("#maRowList");
      var idx = $list.find(".ma-row2").length;
      var opt = {
        enabledYn: "Y",
        seriesLabel: (idx + 1) + "번MA",
        seriesPeriod: 5,
        lineWidth: MA_LINEWIDTH_DEFAULT,
        seriesColor: "#000000"
      };
      $list.append(createMaRow2(opt, idx));
    });

    $("#btnMaDefault").on("click", function () {
      if (confirm("이동평균선 옵션을 기본값으로 되돌리겠습니까?\n(저장은 '적용' 버튼을 눌러야 반영됩니다.)")) {
        loadMaOptionsFromDb(true, true);
      }
    });

    $("#btnApplyMaOptions").on("click", function () {
      saveMaOptions(true, false);
    });

    // MA 옵션 변경 즉시 DB 저장(디바운스)
    $(document).off("input.maAuto change.maAuto").on("input.maAuto change.maAuto", "#maRowList .ma-enabled, #maRowList .ma-label, #maRowList .ma-period, #maRowList .ma-width, #maRowList .ma-color-picker", function () {
      scheduleMaAutoSave();
    });

    //타임 프레임(일,주,월)
    $("#periodDivCode").on("change", function () {
      var p = $("#periodDivCode").val();
      if (isMinuteDivCode(p)) {
        applyMinuteDefaultRange(5);
      }
      doSearch();
    });    
	//시작날짜
    $("#fromDate").on("change", doSearch);
	//종료날짜
    $("#toDate").on("change", doSearch);



    $("#wlMarket").on("change", function () {
      try {
        var marketVal = $("#wlMarket").val();
        wlSaveMarket(marketVal);
      } catch (e) {}
      syncWlMarketSwitchFromSelect();
      syncWlGroupDivFromMarket();
      loadWatchlistGroups();
    });

    $("#wlMarketSwitchBtn").on("click", function () {
      toggleWlMarketFromPill();
    });

    $("#wlGroup").on("change", function () {
      try {
        var marketVal = $("#wlMarket").val();
        var groupVal = $("#wlGroup").val();
        wlSaveGroup(marketVal, groupVal);
      } catch (e) {}
      loadWatchlistItems();
    });

    $("#wlGroupDiv").on("change", function () {
      try {
        var marketVal = $("#wlMarket").val();
        wlSaveGroupDiv(marketVal, $("#wlGroupDiv").val());
      } catch (e) {}
      loadWatchlistItems();
    });

    $("#btnWlReload").on("click", function () {
      try {
        var marketVal = $("#wlMarket").val();
        var groupVal = $("#wlGroup").val();
        wlSaveMarket(marketVal);
        wlSaveGroup(marketVal, groupVal);
        wlSaveGroupDiv(marketVal, $("#wlGroupDiv").val());
      } catch (e) {}
      loadWatchlistGroups();
    });

    $("#btnSummaryReload").on("click", function () {
      loadMarketSummary();
    });

    $("#btnOpenRecSignal").on("click", function () {
      if (!window.__URLS || !window.__URLS.recSignalListView) {
        return;
      }
      window.open(window.__URLS.recSignalListView, "_blank");
    });

    $("#btnRightToggle").on("click", function () {
      toggleRightPanel();
    });

    $("#btnWgToggle").on("click", function () {
      toggleWgPanel();
    });

    $("#btnWgToggleBtn").on("click", function () {
      toggleWgPanel();
    });

    $("#wgBackdrop").on("click", function () {
      toggleWgPanel(false);
    });


    $("#btnWgClose").on("click", function () {
      toggleWgPanel(false);
    });

    $("#btnWgAdd").on("click", doGroupAdd);
    $("#btnWgUpdate").on("click", doGroupUpdate);
    $("#btnWgDelete").on("click", doGroupDelete);
    $("#btnWgClear").on("click", function () {
      clearWgForm();
      setWgStatus("초기화됨", false);
    });

    // WF-2-4: 보유종목 이벤트 바인딩
    $("#btnHoldingAdd").on("click", doHoldingAdd);
    $("#btnHoldingDelete").on("click", doHoldingDelete);
    $("#btnHoldingAvgDown").on("click", doHoldingAvgDown);
    $("#btnHoldingEvents").on("click", toggleHoldingEvents);
    $("#btnHoldingReload").on("click", loadHoldingList);
    // 보유종목 탭 클릭 시 목록 로드(초기 차트 로딩 영향 최소화)
    $(document).on("click", ".rt-tab[data-tab='holding']", function() {
      loadHoldingList();
    });
    
    // 수량/단가 입력 시 평단 미리보기 업데이트
    $("#holdingQty, #holdingPrice").on("input", updateAvgPreview);
    
    // 이력 필터 버튼 이벤트
    $(document).on("click", ".holding-ev-filter", function() {
      $(".holding-ev-filter").removeClass("is-active");
      $(this).addClass("is-active");
      if (holdingEventListVisible) {
        loadHoldingEvents();
      }
    });

    try {
      var savedMarket = wlLoadMarket();
      if (savedMarket && $("#wlMarket option[value='" + savedMarket + "']").length) {
        $("#wlMarket").val(savedMarket);
      }
    } catch (e) {}
    syncWlMarketSwitchFromSelect();
    syncWlGroupDivFromMarket();

    loadWatchlistGroups();
    loadMarketSummary();
    bindMarketSummaryChartClicks();

    // 초기 진입 시 DB MA 옵션을 차트에 반영 (모달은 띄우지 않음)
    loadMaOptionsFromDb(false, false);

    try {
      var lastView = loadLastViewState();
      if (lastView) {
        if (lastView.stockCode) $("#stockCode").val(lastView.stockCode);
        if (lastView.periodDivCode && $("#periodDivCode option[value='" + lastView.periodDivCode + "']").length) {
          $("#periodDivCode").val(lastView.periodDivCode);
        }
        window.__SELECTED_STOCK_COUNTRY = lastView.stockCountryCode || window.__SELECTED_STOCK_COUNTRY || "";
        window.__SELECTED_STOCK_MARKET = lastView.stockMarket || window.__SELECTED_STOCK_MARKET || "";
      }
    } catch (e) {}

    setDefaultDates(); // 페이지 새로고침/초기 진입 시 기본 1년6개월
    doSearch();
  });
