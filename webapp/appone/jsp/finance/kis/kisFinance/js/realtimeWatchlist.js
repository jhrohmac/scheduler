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

  function normalizeSign(sign) {
    var s = (sign === null || sign === undefined) ? "" : String(sign).trim();
    if (s === "+" || s === "1" || s === "4") {
      return "+";
    }
    if (s === "-" || s === "2" || s === "5") {
      return "-";
    }
    if (s === "0" || s === "3") {
      return "0";
    }
    return "";
  }

  function signClass(sign) {
    var normalized = normalizeSign(sign);
    if (normalized === "+") {
      return "up";
    }
    if (normalized === "-") {
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

  function updateChartHeaderRealtime(msg) {
    if (!msg) return;

    var priceSign = signFromDiff(msg);
    var diffSigned = normalizeSignedValue(msg.diff, priceSign, 0);
    var rateSigned = normalizeSignedValue(msg.rate, priceSign, 2);

    var priceText = formatAuto(msg.price, 0);
    var diffText = diffSigned.raw;
    var rateText = rateSigned.raw;

    if (rateText && rateText.indexOf("%") < 0) {
      rateText = rateText + "%";
    }

    var priceCls = signClass(priceSign);
    var diffCls = signClass(diffSigned.sign);
    var rateCls = signClass(rateSigned.sign);

    var $now = $("#kisHdrNow");
    if ($now.length && priceText) {
      $now.text(priceText).removeClass("kis-up kis-down kis-flat up down flat");
      if (priceCls === "up") $now.addClass("kis-up");
      else if (priceCls === "down") $now.addClass("kis-down");
      else $now.addClass("kis-flat");
    }

    var $pct = $("#kisHdrPct");
    if ($pct.length && rateText) {
      $pct.text(rateText).removeClass("kis-up kis-down kis-flat up down flat");
      if (rateCls === "up") $pct.addClass("kis-up");
      else if (rateCls === "down") $pct.addClass("kis-down");
      else $pct.addClass("kis-flat");
    }

    var $diff = $("#kisHdrDiff");
    if ($diff.length && diffText) {
      $diff.text(diffText).removeClass("kis-up kis-down kis-flat up down flat");
      if (diffCls === "up") $diff.addClass("kis-up");
      else if (diffCls === "down") $diff.addClass("kis-down");
      else $diff.addClass("kis-flat");
    }

    var $compatPrice = $("#kisStockPrice");
    if ($compatPrice.length && priceText) {
      $compatPrice.text(priceText);
    }
  }

  function updateCanvasChartRealtime(msg) {
    var price;
    var diff;
    var prevClose;

    if (!msg || !global.KisDashboardChartRenderer ||
        typeof global.KisDashboardChartRenderer.updateRealtimePrice !== "function") {
      return;
    }

    price = toNumber(msg.price);
    if (!Number.isFinite(price)) {
      return;
    }

    diff = toNumber(msg.diff);
    prevClose = Number.isFinite(diff) ? (price - diff) : NaN;
    global.KisDashboardChartRenderer.updateRealtimePrice(price, prevClose);
  }

  function summaryTextFromFields(it) {
    if (!it) return "-";

    var price = formatAuto(it.price, 0);
    if (!price) price = (it.price === null || it.price === undefined) ? "" : String(it.price);
    if (!price) price = "-";

    var sign = signFromDiff(it);
    var diff = normalizeSignedValue(it.diff, sign, 0).raw;
    var rate = normalizeSignedValue(it.rate, sign, 2).raw;
    if (rate && rate.indexOf("%") < 0) {
      rate = rate + "%";
    }

    if (price === "-" || price === "") return "-";
    if (!diff && !rate) return price;
    if (diff && rate) return price + " (" + diff + " / " + rate + ")";
    return price + " (" + (diff || rate) + ")";
  }

  function signFromDiff(it) {
    var explicit = normalizeSign(it && it.sign);
    if (explicit) return explicit;

    var n = toNumber(it && it.diff);
    if (Number.isFinite(n)) {
      if (n > 0) return "+";
      if (n < 0) return "-";
      return "0";
    }

    n = toNumber(it && it.rate);
    if (Number.isFinite(n)) {
      if (n > 0) return "+";
      if (n < 0) return "-";
      return "0";
    }

    return "0";
  }

  function signFromValue(value, fallbackSign) {
    var n = toNumber(value);
    if (Number.isFinite(n)) {
      if (n > 0) return "+";
      if (n < 0) return "-";
      return "0";
    }
    return fallbackSign ? String(fallbackSign) : "0";
  }

  function hasExplicitSignedText(value) {
    if (value === null || value === undefined) return false;
    return /^[\s]*[+-]/.test(String(value));
  }

  function normalizeSignedValue(value, fallbackSign, defaultDecimals) {
    var raw = (value === null || value === undefined) ? "" : String(value).trim();
    var numeric = toNumber(raw);
    var decimals = decimalsHint(raw, defaultDecimals);

    if (!Number.isFinite(numeric)) {
      return {
        raw: raw,
        sign: fallbackSign ? String(fallbackSign) : "0"
      };
    }

    if (hasExplicitSignedText(raw)) {
      return {
        raw: numeric > 0 ? "+" + formatNumber(Math.abs(numeric), decimals) : (numeric < 0 ? "-" + formatNumber(Math.abs(numeric), decimals) : "0"),
        sign: signFromValue(numeric, fallbackSign)
      };
    }

    if (fallbackSign === "+" || fallbackSign === "-") {
      return {
        raw: fallbackSign + formatNumber(Math.abs(numeric), decimals),
        sign: fallbackSign
      };
    }

    return {
      raw: numeric > 0 ? "+" + formatNumber(Math.abs(numeric), decimals) : (numeric < 0 ? "-" + formatNumber(Math.abs(numeric), decimals) : "0"),
      sign: signFromValue(numeric, fallbackSign)
    };
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

    var priceSign = signFromDiff({ diff: diff, rate: rate, sign: sign });
    var diffSigned = normalizeSignedValue(diff, priceSign, 0);
    var rateSigned = normalizeSignedValue(rate, priceSign, 2);
    var priceCls = signClass(priceSign);
    var diffCls = signClass(diffSigned.sign);
    var rateCls = signClass(rateSigned.sign);

    var prevPrice = toNumber($row.data("lastPrice"));
    if (!Number.isFinite(prevPrice)) {
      prevPrice = toNumber($row.find(".wl-price").text());
    }
    var nextPrice = toNumber(price);
    var borderDir = "";
    if (Number.isFinite(nextPrice) && Number.isFinite(prevPrice) && nextPrice !== prevPrice) {
      borderDir = (nextPrice > prevPrice) ? "up" : "down";
    } else if (!Number.isFinite(prevPrice) && Number.isFinite(nextPrice)) {
      var fallbackDir = priceCls;
      if (fallbackDir === "up" || fallbackDir === "down") {
        borderDir = fallbackDir;
      }
    }

    var priceText = formatAuto(price, 0);
    var diffText = diffSigned.raw;
    var rateText = rateSigned.raw;
    if (rateText && rateText.indexOf("%") < 0) {
      rateText = rateText + "%";
    }

    $row.find(".wl-price")
      .text(priceText)
      .attr("title", diffText + " / " + rateText)
      .removeClass("up down flat")
      .addClass(priceCls);

    var $diffEl = $row.find(".wl-diff");
    if ($diffEl.length) {
      $diffEl.text(diffText).removeClass("up down flat").addClass(diffCls);
    }

    var $rateEl = $row.find(".wl-rate");
    if ($rateEl.length) {
      $rateEl.text(rateText).removeClass("up down flat").addClass(rateCls);
    }

    var $eventEl = $row.find(".wl-event");
    if ($eventEl.length) {
      $eventEl.removeClass("up down flat").addClass(priceCls);
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

  function queueWatchlistUpdate(msg) {
    applyWatchlistUpdate(msg);
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

    function applyTopTicker(key, valueSelector) {
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
    }

    apply("KOSPI", "#kospVal");
    apply("KOSDAQ", "#kosdVal");
    apply("USDKRW", "#fxVal");
    apply("DJI", "#djiVal");
    apply("IXIC", "#ixicVal");
    apply("SPX", "#spxVal");
    applyTopTicker("KOSPI", "#topKospiVal");
    applyTopTicker("KOSDAQ", "#topKosdVal");
    applyTopTicker("DJI", "#topDjiVal");
    applyTopTicker("SPX", "#topSpxVal");
    applyTopTicker("IXIC", "#topIxicVal");
  }

  function queueMarketSummaryUpdate(msg) {
    applyMarketSummaryUpdate(msg);
  }

  function clearReconnectTimer(target) {
    if (!target || !target.reconnectTimer) {
      return;
    }
    clearTimeout(target.reconnectTimer);
    target.reconnectTimer = null;
  }

  function markMessageReceived(target) {
    if (!target) {
      return;
    }
    target.lastMessageAt = Date.now();
    target.reconnectAttempts = 0;
    clearReconnectTimer(target);
  }

  function shouldReconnect(evt) {
    var code = evt && evt.code;
    if (!code || code === 1006) {
      return true;
    }
    if (code === 1000 || code === 1001 || code === 1008) {
      return false;
    }
    return true;
  }

  function nextReconnectDelay(target, baseDelay, maxDelay) {
    var attempt = target && typeof target.reconnectAttempts === "number"
      ? target.reconnectAttempts
      : 0;
    var base = Math.max(500, baseDelay || 1000);
    var max = Math.max(base, maxDelay || 30000);
    var delay = Math.min(base * Math.pow(2, attempt), max);
    var jitter = Math.floor(Math.random() * 700);

    if (target) {
      target.reconnectAttempts = attempt + 1;
    }

    return delay + jitter;
  }

  function scheduleReconnect(target, reconnectFn, evt, options) {
    if (!target || typeof reconnectFn !== "function" || !shouldReconnect(evt)) {
      return 0;
    }

    clearReconnectTimer(target);

    var delay = nextReconnectDelay(
      target,
      options && options.baseDelay,
      options && options.maxDelay
    );

    if (typeof document !== "undefined" && document.hidden) {
      delay = Math.max(delay, (options && options.hiddenDelay) || 5000);
    }

    target.reconnectTimer = setTimeout(function () {
      target.reconnectTimer = null;
      reconnectFn();
    }, delay);

    return delay;
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
    reconnectAttempts: 0,
    reconnectTimer: null,
    lastMessageAt: 0,
    currentConnectSeq: 0,

    close: function (silent) {
      clearReconnectTimer(this);
      this.currentConnectSeq += 1;
      this.reconnectAttempts = 0;
      this.lastMessageAt = 0;
      safeCloseWs(this.ws);
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

      tokens = Array.from(new Set(tokens)).sort();
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
      clearReconnectTimer(this);

      setWlStatus("연결중", "is-warn");

      var wsPathCandidate = global.__WS_WATCHLIST;
      var path = isValidWsPath(wsPathCandidate)
        ? wsPathCandidate
        : "/finance/watchlistRealtime.ws";
      var url = buildWsUrl(path, "codes=" + encodeURIComponent(key));
      var connectSeq = ++this.currentConnectSeq;

      if (global.__DEBUG_WL_WS && global.console) {
        console.log("[WL-WS] connect", { url: url, tokens: tokens.length });
      }

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onopen = function () {
        if (WatchlistRealtime.ws !== ws || WatchlistRealtime.currentConnectSeq !== connectSeq) {
          return;
        }
        clearReconnectTimer(WatchlistRealtime);
        setWlStatus("연결 확인중", "is-warn", "open");
      };

      ws.onmessage = function (evt) {
        if (WatchlistRealtime.ws !== ws || WatchlistRealtime.currentConnectSeq !== connectSeq) {
          return;
        }
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
            markMessageReceived(WatchlistRealtime);
            setWlStatus("연결됨", "is-on", "message");
            queueWatchlistUpdate(msg);
            return;
          }
        } catch (e) {}
      };

      ws.onerror = function () {
        if (WatchlistRealtime.ws !== ws || WatchlistRealtime.currentConnectSeq !== connectSeq) {
          return;
        }
        setWlStatus("오류", "is-off", "onerror");
      };

      ws.onclose = function (evt) {
        if (ws.__manualClose) return;
        if (WatchlistRealtime.ws !== ws || WatchlistRealtime.currentConnectSeq !== connectSeq) return;
        var detail = "close";
        if (evt && evt.code) {
          detail += " code=" + evt.code;
        }
        if (evt && evt.reason) {
          detail += " reason=" + evt.reason;
        }
        if (global.__DEBUG_WL_WS && global.console) {
          console.warn("[WL-WS] closed", evt && evt.code, evt && evt.reason);
        }
        var delay = scheduleReconnect(WatchlistRealtime, function () {
          WatchlistRealtime.connectFromDom();
        }, evt, { baseDelay: 1000, maxDelay: 30000, hiddenDelay: 5000 });

        if (delay > 0) {
          setWlStatus("재연결중", "is-warn", detail + " / " + delay + "ms");
        } else {
          setWlStatus("종료", "is-off", detail);
        }
      };
    }
  };

  // Realtime quote for current chart header
  function safeCloseWs(ws) {
    if (!ws) return;
    try {
      ws.__manualClose = true;
      ws.onopen = null;
      ws.onmessage = null;
      ws.onerror = null;
      ws.onclose = null;
      ws.close();
    } catch (e) {}
  }

  var ChartPriceRealtime = {
    ws: null,
    lastToken: "",
    reconnectAttempts: 0,
    reconnectTimer: null,
    lastMessageAt: 0,
    currentConnectSeq: 0,

    close: function () {
      clearReconnectTimer(this);
      this.currentConnectSeq += 1;
      this.reconnectAttempts = 0;
      this.lastMessageAt = 0;
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

      if (this.ws && (this.ws.readyState === 0 || this.ws.readyState === 1) && this.lastToken === token) {
        return;
      }

      clearReconnectTimer(this);
      safeCloseWs(this.ws);
      this.ws = null;
      this.lastToken = token;

      var wsPathCandidate = global.__WS_WATCHLIST;
      var path = isValidWsPath(wsPathCandidate)
        ? wsPathCandidate
        : "/finance/watchlistRealtime.ws";
      var url = buildWsUrl(path, "codes=" + encodeURIComponent(token));
      var connectSeq = ++this.currentConnectSeq;

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onopen = function () {
        if (ChartPriceRealtime.ws !== ws || ChartPriceRealtime.currentConnectSeq !== connectSeq) {
          return;
        }
        clearReconnectTimer(ChartPriceRealtime);
      };

      ws.onmessage = function (evt) {
        if (ChartPriceRealtime.ws !== ws || ChartPriceRealtime.currentConnectSeq !== connectSeq) {
          return;
        }
        try {
          var msg = JSON.parse(evt.data);
          if (!msg || msg.type !== "WL") {
            return;
          }
          markMessageReceived(ChartPriceRealtime);
          updateChartHeaderRealtime(msg);
          updateCanvasChartRealtime(msg);
          if (global.ChartScript && typeof global.ChartScript.applyRealtimeQuote === "function") {
            global.ChartScript.applyRealtimeQuote(msg);
          }
        } catch (e) {}
      };

      ws.onclose = function (evt) {
        if (ws.__manualClose) return;
        if (ChartPriceRealtime.ws !== ws || ChartPriceRealtime.currentConnectSeq !== connectSeq) return;
        scheduleReconnect(ChartPriceRealtime, function () {
          ChartPriceRealtime.connect(ChartPriceRealtime.lastToken);
        }, evt, { baseDelay: 1000, maxDelay: 30000, hiddenDelay: 5000 });
      };
    }
  };

  var MarketSummaryRealtime = {
    ws: null,
    reconnectAttempts: 0,
    reconnectTimer: null,
    lastMessageAt: 0,
    currentConnectSeq: 0,

    close: function () {
      clearReconnectTimer(this);
      this.currentConnectSeq += 1;
      this.reconnectAttempts = 0;
      this.lastMessageAt = 0;
      safeCloseWs(this.ws);
      this.ws = null;
    },

    connect: function () {
      if (this.ws && (this.ws.readyState === 0 || this.ws.readyState === 1)) {
        return;
      }

      clearReconnectTimer(this);
      this.close();

      var wsSummaryCandidate = global.__WS_MARKET_SUMMARY;
      var path = isValidWsPath(wsSummaryCandidate)
        ? wsSummaryCandidate
        : "/finance/marketSummaryRealtime.ws";
      var url = buildWsUrl(path, "");
      var connectSeq = ++this.currentConnectSeq;

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onopen = function () {
        if (MarketSummaryRealtime.ws !== ws || MarketSummaryRealtime.currentConnectSeq !== connectSeq) {
          return;
        }
        clearReconnectTimer(MarketSummaryRealtime);
      };

      ws.onmessage = function (evt) {
        if (MarketSummaryRealtime.ws !== ws || MarketSummaryRealtime.currentConnectSeq !== connectSeq) {
          return;
        }
        try {
          var msg = JSON.parse(evt.data);
          if (msg && msg.type === "SUMMARY") {
            markMessageReceived(MarketSummaryRealtime);
          }
          queueMarketSummaryUpdate(msg);
        } catch (e) {}
      };

      ws.onclose = function (evt) {
        if (ws.__manualClose) return;
        if (MarketSummaryRealtime.ws !== ws || MarketSummaryRealtime.currentConnectSeq !== connectSeq) return;
        scheduleReconnect(MarketSummaryRealtime, function () {
          MarketSummaryRealtime.connect();
        }, evt, { baseDelay: 1500, maxDelay: 30000, hiddenDelay: 5000 });
      };
    }
  };

  global.WatchlistRealtime = WatchlistRealtime;
  global.MarketSummaryRealtime = MarketSummaryRealtime;
  global.ChartPriceRealtime = ChartPriceRealtime;
})(window, window.jQuery);
