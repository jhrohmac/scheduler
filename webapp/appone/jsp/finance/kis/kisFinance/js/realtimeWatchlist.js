(function (global, $) {
  "use strict";

  function normalizeSlash(s) {
    if (!s) return "";
    s = String(s);
    if (s === "/") return "";
    if (s.charAt(0) !== "/") s = "/" + s;
    if (s.length > 1 && s.charAt(s.length - 1) === "/") {
      s = s.substring(0, s.length - 1);
    }
    return s;
  }

  function detectContextPath() {
    if (global.__CTX_PATH) {
      return normalizeSlash(global.__CTX_PATH);
    }
    try {
      if (global.__APP_CTX) {
        return normalizeSlash(global.__APP_CTX);
      }
    } catch (e) {}

    var p = String(location.pathname || "");
    if (p.charAt(0) !== "/") p = "/" + p;
    var seg = p.split("/")[1] || "";
    return seg ? ("/" + seg) : "";
  }

  function isValidWsPath(p) {
    if (!p) return false;
    p = String(p);
    if (p.indexOf("/jsp/") >= 0) return false;
    if (p.indexOf("/js/") >= 0) return false;
    if (p.indexOf(".ws") < 0) return false;
    return true;
  }

  function buildWsUrl(path, query) {
    var proto = (location.protocol === "https:") ? "wss://" : "ws://";
    var base = proto + location.host;
    var ctx = detectContextPath();

    path = String(path || "");
    if (path.charAt(0) !== "/") path = "/" + path;

    while (ctx && path.indexOf(ctx + ctx + "/") === 0) {
      path = path.substring(ctx.length);
    }

    if (ctx && path.indexOf(ctx + "/") !== 0) {
      path = ctx + path;
    }

    var url = base + path;
    if (query) {
      url += (url.indexOf("?") >= 0 ? "&" : "?") + query;
    }
    return url;
  }

  function signClass(sign) {
    if (sign === "+" || sign === "1" || sign === "4") {
      return "up";
    }
    if (sign === "-" || sign === "2" || sign === "5") {
      return "down";
    }
    return "flat";
  }

  var __wlStatusText = "";
  var __wlStatusEl = null;

  function wlStatusEl() {
    if (!__wlStatusEl || __wlStatusEl.length === 0) {
      __wlStatusEl = $("#wlRealtimeStatus");
    }
    return __wlStatusEl;
  }

  function setWlStatus(text, cls, detail) {
    var $el = wlStatusEl();
    if (!$el || $el.length === 0) {
      return;
    }
    var nextText = text || "";
    if (__wlStatusText !== nextText) {
      __wlStatusText = nextText;
      $el.text(nextText);
    }
    if (detail !== undefined) {
      var title = detail ? String(detail) : "실시간 상태";
      $el.attr("title", title);
    }
    $el.removeClass("is-on is-off is-warn");
    if (cls) {
      $el.addClass(cls);
    }
  }

  function toNumber(v) {
    if (v === null || v === undefined) return NaN;
    var s = String(v).trim();
    if (!s) return NaN;
    s = s.replace(/,/g, "");
    s = s.replace(/\+\+/g, "+").replace(/--/g, "-");
    if (s.charAt(0) === "+") s = s.substring(1);
    return parseFloat(s);
  }

  function decimalsHint(src, fallback) {
    if (src === null || src === undefined) return fallback;
    var s = String(src);
    var dot = s.indexOf(".");
    if (dot < 0) return fallback;
    var dec = s.length - dot - 1;
    if (dec < 0) dec = 0;
    if (dec > 4) dec = 4;
    return dec;
  }

  function formatNumber(num, decimals) {
    if (!Number.isFinite(num)) return "";
    var opts = { useGrouping: true, minimumFractionDigits: decimals, maximumFractionDigits: decimals };
    try {
      return num.toLocaleString("en-US", opts);
    } catch (e) {
      return String(num);
    }
  }

  function formatAuto(src, defaultDecimals) {
    var n = toNumber(src);
    if (!Number.isFinite(n)) return (src === null || src === undefined) ? "" : String(src);
    var d = decimalsHint(src, defaultDecimals);
    return formatNumber(n, d);
  }

  function formatSigned(src, defaultDecimals) {
    var n = toNumber(src);
    if (!Number.isFinite(n)) {
      var raw = (src === null || src === undefined) ? "" : String(src);
      raw = raw.replace(/--/g, "-");
      return raw;
    }
    var d = decimalsHint(src, defaultDecimals);
    if (n > 0) return "+" + formatNumber(Math.abs(n), d);
    if (n < 0) return "-" + formatNumber(Math.abs(n), d);
    return "0";
  }

  function summaryTextFromFields(it) {
    if (!it) return "-";

    var price = formatAuto(it.price, 0);
    if (!price) price = (it.price === null || it.price === undefined) ? "" : String(it.price);
    if (!price) price = "-";

    var diff = formatSigned(it.diff, 0);
    var rate = formatSigned(it.rate, 2);

    if (price === "-" || price === "") return "-";
    if (!diff && !rate) return price;

    return price + " (" + diff + " / " + rate + "%)";
  }

  function signFromDiff(it) {
    var n = toNumber(it && it.diff);
    if (!Number.isFinite(n)) return (it && it.sign) ? String(it.sign) : "0";
    if (n > 0) return "+";
    if (n < 0) return "-";
    return "0";
  }

  function safeTokenFromRow($row) {
    if (!$row || $row.length === 0) return "";

    var token = ($row.attr("data-token") || "").trim();
    if (token) return token;

    var code = ($row.attr("data-code") || "").trim();
    if (!code) return "";

    var country = ($row.attr("data-country") || "").trim();
    var market = ($row.attr("data-market") || "").trim();

    if (!country) country = "KR";
    if (!market) market = (country === "KR") ? "KRX" : "NAS";

    return country + "|" + market + "|" + code;
  }

  /* =========================
     Realtime UI Throttle
     - tick마다 DOM을 직접 변경하지 않고, 짧게 배치 반영하여 렌더링 부담 완화
     ========================= */
  var __wlPending = {};
  var __wlFlushTimer = null;
  var __wlFlushMs = 250;

  var __summaryPending = null;
  var __summaryFlushTimer = null;
  var __summaryFlushMs = 500;

  function findWatchlistRow(token, code) {
    var $rows = $("#watchlist .wl-item");
    if (!$rows.length) return null;

    if (token) {
      var t = String(token).trim();
      if (t) {
        var $byToken = $rows.filter(function () {
          return String($(this).attr("data-token") || "").trim() === t;
        });
        if ($byToken.length) return $byToken;
      }
    }

    if (code) {
      var c = String(code).trim();
      if (c) {
        var $byCode = $rows.filter(function () {
          return String($(this).attr("data-code") || "").trim() === c;
        });
        if ($byCode.length) return $byCode;
      }
    }

    return null;
  }

  function applyWatchlistUpdate(msg) {
    if (!msg) return;

    var token = (msg.token || "").trim();
    var code = (msg.code || "").trim();

    var price = msg.price;
    var diff = msg.diff;
    var rate = msg.rate;
    var sign = msg.sign;

    var $row = findWatchlistRow(token, code);
    if (!$row || $row.length === 0) {
      return;
    }

    var cls = signClass(signFromDiff({ diff: diff, sign: sign }));

    var prevPrice = toNumber($row.data("lastPrice"));
    if (!Number.isFinite(prevPrice)) {
      prevPrice = toNumber($row.find(".wl-price").text());
    }
    var nextPrice = toNumber(price);
    var borderDir = "";
    if (Number.isFinite(nextPrice) && Number.isFinite(prevPrice) && nextPrice !== prevPrice) {
      borderDir = (nextPrice > prevPrice) ? "up" : "down";
    } else if (!Number.isFinite(prevPrice) && Number.isFinite(nextPrice)) {
      var fallbackDir = signClass(signFromDiff({ diff: diff, sign: sign }));
      if (fallbackDir === "up" || fallbackDir === "down") {
        borderDir = fallbackDir;
      }
    }

    var priceText = formatAuto(price, 0);
    var diffText = formatSigned(diff, 0);
    var rateText = formatSigned(rate, 2);
    if (rateText && rateText.indexOf("%") < 0) {
      rateText = rateText + "%";
    }

    $row.find(".wl-price")
      .text(priceText)
      .attr("title", diffText + " / " + rateText)
      .removeClass("up down flat")
      .addClass(cls);

    var $diffEl = $row.find(".wl-diff");
    if ($diffEl.length) {
      $diffEl.text(diffText).removeClass("up down flat").addClass(cls);
    }

    var $rateEl = $row.find(".wl-rate");
    if ($rateEl.length) {
      $rateEl.text(rateText).removeClass("up down flat").addClass(cls);
    }

    var $eventEl = $row.find(".wl-event");
    if ($eventEl.length) {
      $eventEl.removeClass("up down flat").addClass(cls);
    }

    var $rowEl = $row[0];
    if (borderDir) {
      var prevTimer = $row.data("borderTimer");
      if (prevTimer) {
        clearTimeout(prevTimer);
        $row.removeData("borderTimer");
      }

      $row.removeClass("wl-border-up wl-border-down");
      if ($rowEl) {
        $rowEl.offsetWidth; // restart animation
      }
      $row.addClass("wl-border-" + borderDir);

      var timer = setTimeout(function () {
        $row.removeClass("wl-border-up wl-border-down");
        $row.removeData("borderTimer");
      }, 900);
      $row.data("borderTimer", timer);
    } else {
      $row.removeClass("wl-border-up wl-border-down");
    }
    if (Number.isFinite(nextPrice)) {
      $row.data("lastPrice", nextPrice);
    }
  }

  function flushWatchlistPending() {
    __wlFlushTimer = null;

    var pending = __wlPending;
    __wlPending = {};

    var keys = Object.keys(pending);
    if (!keys.length) return;

    for (var i = 0; i < keys.length; i++) {
      applyWatchlistUpdate(pending[keys[i]]);
    }
  }

  function queueWatchlistUpdate(msg) {
    if (!msg) return;

    var key = (msg.token || "").trim();
    if (!key) {
      key = (msg.code || "").trim();
    }
    if (!key) return;

    __wlPending[key] = msg;

    if (!__wlFlushTimer) {
      __wlFlushTimer = setTimeout(flushWatchlistPending, __wlFlushMs);
    }
  }

  function applyMarketSummaryUpdate(msg) {
    if (!msg || msg.type !== "SUMMARY" || !msg.items) {
      return;
    }

    if (msg.asOf) {
      $("#summaryAsOf").text(msg.asOf);
    }

    function summaryPriceFromItem(it, $el) {
      var p = toNumber(it && it.price);
      if (Number.isFinite(p)) return p;
      var t = "";
      if (it && it.text !== null && it.text !== undefined) {
        t = String(it.text);
      }
      if (!t && $el && $el.length) {
        t = String($el.text() || "");
      }
      t = t.trim();
      if (!t || t === "-") return NaN;
      var m = t.match(/([+-]?[0-9][0-9,]*\.?[0-9]*)/);
      if (m && m[1]) return toNumber(m[1]);
      return NaN;
    }

    function applySummaryBorder($row, dir) {
      if (!$row || $row.length === 0 || !dir) return;
      var clsUp = "summary-border-up";
      var clsDown = "summary-border-down";

      var prevTimer = $row.data("borderTimer");
      if (prevTimer) {
        clearTimeout(prevTimer);
        $row.removeData("borderTimer");
      }

      $row.removeClass(clsUp + " " + clsDown);
      if ($row[0]) {
        $row[0].offsetWidth; // restart animation
      }
      $row.addClass(dir === "up" ? clsUp : clsDown);

      var timer = setTimeout(function () {
        $row.removeClass(clsUp + " " + clsDown);
        $row.removeData("borderTimer");
      }, 900);
      $row.data("borderTimer", timer);
    }

    function apply(key, valueSelector) {
      var it = msg.items[key];
      if (!it) {
        return;
      }

      var sign = signFromDiff(it);
      var cls = signClass(sign);
      var text = "";
      if (it.text !== null && it.text !== undefined && String(it.text).trim() !== "") {
        text = String(it.text);
      } else {
        text = summaryTextFromFields(it);
      }

      var $el = $(valueSelector);
      if ($el.length === 0) {
        return;
      }
      $el.text(text).removeClass("up down flat").addClass(cls);

      var $row = $el.closest(".summary-row");
      if ($row.length) {
        var nextPrice = summaryPriceFromItem(it, $el);
        var prevPrice = toNumber($row.data("lastPrice"));
        var borderDir = "";
        if (Number.isFinite(nextPrice) && Number.isFinite(prevPrice) && nextPrice !== prevPrice) {
          borderDir = (nextPrice > prevPrice) ? "up" : "down";
        } else if (!Number.isFinite(prevPrice) && Number.isFinite(nextPrice)) {
          if (cls === "up" || cls === "down") {
            borderDir = cls;
          }
        }
        if (borderDir) {
          applySummaryBorder($row, borderDir);
        }
        if (Number.isFinite(nextPrice)) {
          $row.data("lastPrice", nextPrice);
        }
      }
    }

    apply("KOSPI", "#kospVal");
    apply("KOSDAQ", "#kosdVal");
    apply("USDKRW", "#fxVal");
    apply("DJI", "#djiVal");

    apply("IXIC", "#ixicVal");
    apply("SPX", "#spxVal");
  }

  function flushSummaryPending() {
    __summaryFlushTimer = null;
    var msg = __summaryPending;
    __summaryPending = null;
    applyMarketSummaryUpdate(msg);
  }

  function queueMarketSummaryUpdate(msg) {
    if (!msg) return;

    __summaryPending = msg;

    if (!__summaryFlushTimer) {
      __summaryFlushTimer = setTimeout(flushSummaryPending, __summaryFlushMs);
    }
  }

  function markManualClose(ws) {
    if (!ws) return;
    try {
      ws.__manualClose = true;
    } catch (e) {}
  }

  var WatchlistRealtime = {
    ws: null,
    lastCodesKey: "",

    close: function (silent) {
      try {
        if (this.ws) {
          markManualClose(this.ws);
          this.ws.close();
        }
      } catch (e) {}
      this.ws = null;
      this.lastCodesKey = "";
      if (!silent) {
        setWlStatus("대기", "is-warn");
      }
    },

    connectFromDom: function () {
      var tokens = [];

      $("#watchlist .wl-item").each(function () {
        var t = safeTokenFromRow($(this));
        if (t) {
          tokens.push(t);
        }
      });

      tokens = Array.from(new Set(tokens));
      var key = tokens.join(",");

      if (!key) {
        this.close(false);
        return;
      }
      if (this.ws && (this.ws.readyState === 0 || this.ws.readyState === 1) && this.lastCodesKey === key) {
        return;
      }

      this.close(true);
      this.lastCodesKey = key;

      setWlStatus("연결중", "is-warn");

      var wsPathCandidate = global.__WS_WATCHLIST;
      var path = isValidWsPath(wsPathCandidate)
        ? wsPathCandidate
        : "/finance/watchlistRealtime.ws";
      var url = buildWsUrl(path, "codes=" + encodeURIComponent(key));

      if (global.__DEBUG_WL_WS && global.console) {
        console.log("[WL-WS] connect", { url: url, tokens: tokens.length });
      }

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onopen = function () {
        setWlStatus("연결됨", "is-on", "open");
      };

      ws.onmessage = function (evt) {
        try {
          var msg = JSON.parse(evt.data);
          if (!msg || !msg.type) {
            return;
          }

          if (msg.type === "ERR") {
            setWlStatus("오류", "is-off", msg.message || "server error");
            if (global.__DEBUG_WL_WS && global.console) {
              console.warn("[WL-WS] server error", msg.message);
            }
            return;
          }

          if (msg.type === "WL") {
            queueWatchlistUpdate(msg);
            return;
          }
        } catch (e) {}
      };

      ws.onerror = function () {
        setWlStatus("오류", "is-off", "onerror");
      };

      ws.onclose = function (evt) {
        if (ws.__manualClose) return;
        if (WatchlistRealtime.ws !== ws) return;
        var detail = "close";
        if (evt && evt.code) {
          detail += " code=" + evt.code;
        }
        if (evt && evt.reason) {
          detail += " reason=" + evt.reason;
        }
        setWlStatus("재연결중", "is-warn", detail);
        if (global.__DEBUG_WL_WS && global.console) {
          console.warn("[WL-WS] closed", evt && evt.code, evt && evt.reason);
        }
        setTimeout(function () {
          WatchlistRealtime.connectFromDom();
        }, 1500);
      };
    }
  };

  // Realtime quote for current chart header
  function safeCloseWs(ws) {
    if (!ws) return;
    try {
      ws.__manualClose = true;
      ws.onclose = null;
      ws.close();
    } catch (e) {}
  }

  var ChartPriceRealtime = {
    ws: null,
    lastToken: "",

    close: function () {
      safeCloseWs(this.ws);
      this.ws = null;
      this.lastToken = "";
    },

    connect: function (token) {
      token = (token || "").trim();
      if (!token) {
        this.close();
        return;
      }

      if (this.ws && this.ws.readyState === 1 && this.lastToken === token) {
        return;
      }

      safeCloseWs(this.ws);
      this.ws = null;
      this.lastToken = token;

      var wsPathCandidate = global.__WS_WATCHLIST;
      var path = isValidWsPath(wsPathCandidate)
        ? wsPathCandidate
        : "/finance/watchlistRealtime.ws";
      var url = buildWsUrl(path, "codes=" + encodeURIComponent(token));

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onmessage = function (evt) {
        try {
          var msg = JSON.parse(evt.data);
          if (!msg || msg.type !== "WL") {
            return;
          }
          if (global.ChartScript && typeof global.ChartScript.applyRealtimeQuote === "function") {
            global.ChartScript.applyRealtimeQuote(msg);
          }
        } catch (e) {}
      };

      ws.onclose = function () {
        if (ws.__manualClose) return;
        if (ChartPriceRealtime.ws !== ws) return;
        setTimeout(function () {
          ChartPriceRealtime.connect(ChartPriceRealtime.lastToken);
        }, 1500);
      };
    }
  };

  var MarketSummaryRealtime = {
    ws: null,

    close: function () {
      try {
        if (this.ws) {
          this.ws.close();
        }
      } catch (e) {}
      this.ws = null;
    },

    connect: function () {
      if (this.ws && this.ws.readyState === 1) {
        return;
      }

      this.close();

      var wsSummaryCandidate = global.__WS_MARKET_SUMMARY;
      var path = isValidWsPath(wsSummaryCandidate)
        ? wsSummaryCandidate
        : "/finance/marketSummaryRealtime.ws";
      var url = buildWsUrl(path, "");

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onmessage = function (evt) {
        try {
          var msg = JSON.parse(evt.data);
          queueMarketSummaryUpdate(msg);
        } catch (e) {}
      };

      ws.onclose = function () {
        setTimeout(function () {
          MarketSummaryRealtime.connect();
        }, 2000);
      };
    }
  };

  global.WatchlistRealtime = WatchlistRealtime;
  global.MarketSummaryRealtime = MarketSummaryRealtime;
  global.ChartPriceRealtime = ChartPriceRealtime;
})(window, window.jQuery);
