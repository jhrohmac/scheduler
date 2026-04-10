(function (global, $) {
  "use strict";

  function buildWsUrl(path, query) {
    var proto = (location.protocol === "https:") ? "wss://" : "ws://";
    var base = proto + location.host;

    var ctx = "";
    if (global.__CTX_PATH) {
      ctx = global.__CTX_PATH;
    } else {
      try {
        ctx = (global.__APP_CTX || "");
      } catch (e) {
        ctx = "";
      }
    }

    var url = base + ctx + path;
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

  var WatchlistRealtime = {
    ws: null,
    lastCodesKey: "",

    close: function () {
      try {
        if (this.ws) {
          this.ws.close();
        }
      } catch (e) {}
      this.ws = null;
      this.lastCodesKey = "";
    },

    connectFromDom: function () {
      var codes = [];
      $("#watchlist .wl-item").each(function () {
        var c = $(this).attr("data-code") || "";
        c = $.trim(c);
        if (c) {
          codes.push(c);
        }
      });

      codes = Array.from(new Set(codes));
      var key = codes.join(",");
      if (!key) {
        this.close();
        return;
      }
      if (this.ws && this.ws.readyState === 1 && this.lastCodesKey === key) {
        return;
      }

      this.close();
      this.lastCodesKey = key;

      var path = global.__WS_WATCHLIST || "/finance/watchlistRealtime.ws";
      var url = buildWsUrl(path, "codes=" + encodeURIComponent(key));

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onmessage = function (evt) {
        try {
          var msg = JSON.parse(evt.data);
          if (!msg || !msg.type) {
            return;
          }
          if (msg.type === "WL") {
            var code = msg.code;
            var price = msg.price;
            var diff = msg.diff;
            var rate = msg.rate;
            var sign = msg.sign;

            var $row = $("#watchlist .wl-item[data-code='" + code + "']");
            if ($row.length === 0) {
              return;
            }

            var cls = signClass(sign);
            $row.find(".wl-price").text(price).attr("title", (sign === "+" ? "+" : (sign === "-" ? "-" : "")) + diff + " / " + rate + "%").removeClass("up down flat").addClass(cls);
            return;
          }
        } catch (e) {}
      };

      ws.onclose = function () {
        // 자동 재연결 (DOM이 존재하면)
        setTimeout(function () {
          WatchlistRealtime.connectFromDom();
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

      var path = global.__WS_MARKET_SUMMARY || "/finance/marketSummaryRealtime.ws";
      var url = buildWsUrl(path, "");

      var ws = new WebSocket(url);
      this.ws = ws;

      ws.onmessage = function (evt) {
        try {
          var msg = JSON.parse(evt.data);
          if (!msg || msg.type !== "SUMMARY" || !msg.items) {
            return;
          }

          if (msg.asOf) {
            $("#summaryAsOf").text(msg.asOf);
          }

          function apply(key, valueSelector) {
            var it = msg.items[key];
            if (!it) {
              return;
            }
            var cls = signClass(it.sign);
            var $el = $(valueSelector);
            if ($el.length === 0) {
              return;
            }
            $el.text(it.text).removeClass("up down flat").addClass(cls);
          }

          apply("KOSPI", "#kospVal");
          apply("KOSDAQ", "#kosdVal");
          apply("USDKRW", "#fxVal");
          apply("DJI", "#djiVal");

          // (선택) 화면에 있으면 자동 반영
          apply("IXIC", "#ixicVal");
          apply("SPX", "#spxVal");
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
})(window, window.jQuery);
