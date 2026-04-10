/*
 * marketSummaryRealtime.js
 * - 시장요약(국내지수/해외지수/환율) 실시간 반영
 * - 서버 WebSocket: /scheduler/finance/watchlistRealtime.ws?market=S
 */

(function (window) {
  var WS = null;
  var reconnectTimer = null;
  var closedByUser = false;

  function log() {
    if (window && window.console && console.log) {
      console.log.apply(console, arguments);
    }
  }

  function getWsUrl() {
    var proto = (location.protocol === "https:") ? "wss://" : "ws://";
    var base = proto + location.host;
    return base + "/scheduler/finance/watchlistRealtime.ws?market=S";
  }

  function disconnect() {
    closedByUser = true;

    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }

    if (WS) {
      try { WS.close(); } catch (e) {}
      WS = null;
    }
  }

  function connect() {
    // 동일 연결 유지
    if (WS && WS.readyState === 1) return;

    disconnect();
    closedByUser = false;

    var url = getWsUrl();
    try {
      WS = new WebSocket(url);
    } catch (e) {
      WS = null;
      scheduleReconnect();
      return;
    }

    WS.onopen = function () {
      log("[marketSummaryRealtime] open");
    };

    WS.onmessage = function (evt) {
      if (!evt || !evt.data) return;
      var msg;
      try {
        msg = JSON.parse(evt.data);
      } catch (e) {
        return;
      }

      if (!msg || msg.type !== "summary") return;
      applySummary(msg.data || {});
    };

    WS.onclose = function () {
      WS = null;
      if (!closedByUser) scheduleReconnect();
    };

    WS.onerror = function () {
      // close 이벤트로 이어지도록 둔다.
    };
  }

  function scheduleReconnect() {
    if (reconnectTimer) return;
    reconnectTimer = setTimeout(function () {
      reconnectTimer = null;
      connect();
    }, 2000);
  }

  function applySummary(d) {
    // 기존 화면 id와 매핑
    var mapping = {
      KOSPI: "#kospVal",
      KOSDAQ: "#kosdVal",
      USDKRW: "#fxVal",
      DJI: "#djiVal",
      IXIC: "#ixicVal",
      SPX: "#spxVal",
      AS_OF: "#summaryAsOf"
    };

    Object.keys(mapping).forEach(function (k) {
      var sel = mapping[k];
      if (!sel) return;

      if (k === "AS_OF") {
        if ($(sel).length) {
          $(sel).text(d.AS_OF ? ("기준: " + d.AS_OF) : "");
        }
        return;
      }

      if (!$(sel).length) return;
      var val = d[k] || d[k.toLowerCase()] || "-";
      $(sel).text(val);

      // 등락 색상 적용 (문자열 안의 + / - 를 기준)
      var dir = parseDir(val);
      $(sel).removeClass("up down flat").addClass(dir);
    });
  }

  function parseDir(text) {
    var s = (text === null || text === undefined) ? "" : ("" + text);
    // 괄호 안에 - 가 있으면 down, + 있으면 up
    var inParen = s;
    var m = s.match(/\(([^)]+)\)/);
    if (m && m[1]) inParen = m[1];

    if (inParen.indexOf("-") >= 0) return "down";
    if (inParen.indexOf("+") >= 0) return "up";
    return "flat";
  }

  window.MarketSummaryRealtime = {
    connect: connect,
    disconnect: disconnect
  };

})(window);
