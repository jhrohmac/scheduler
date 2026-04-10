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
    var p = String(location.pathname || "");
    if (p.charAt(0) !== "/") p = "/" + p;
    var seg = p.split("/")[1] || "";
    return seg ? ("/" + seg) : "";
  }

  function resolveToggleUrl() {
    try {
      if (global.__URLS && global.__URLS.toggleWatchlistItem) {
        return global.__URLS.toggleWatchlistItem;
      }
    } catch (e) {}
    return detectContextPath() + "/finance/toggleWatchlistItem.do";
  }

  function resolveCheckUrl() {
    try {
      if (global.__URLS && global.__URLS.selectWatchlistItemYn) {
        return global.__URLS.selectWatchlistItemYn;
      }
    } catch (e) {}
    return detectContextPath() + "/finance/selectWatchlistItemYn.do";
  }

  function unwrapOk(res) {
    if (!res) return { ok: false, msg: "" };

    if (res.result_code) {
      return { ok: ("" + res.result_code) === "1", msg: res.result_msg || "" };
    }

    if (res.system_code) {
      return { ok: ("" + res.system_code) === "0000", msg: res.system_msg || "" };
    }

    return { ok: true, msg: "" };
  }

  function extractSingleData(res) {
    try {
      if (res && res.singleData) {
        return res.singleData;
      }
    } catch (e) {}
    return null;
  }

  function parseNumberText(s) {
    if (s === null || s === undefined) return "";
    var t = String(s).trim();
    if (!t) return "";
    t = t.replace(/,/g, "");
    t = t.replace(/[^0-9.\-]/g, "");
    return t;
  }

  function currentStockCode() {
    var v = ($("#stockCode").val() || "").trim();
    if (v) return v;
    v = ($("#kisHdrCode").text() || "").trim();
    return v;
  }

  function selectedGroupId() {
    var v = ($("#wlGroup").val() || "").trim();
    return v;
  }

  function selectedGroupDiv() {
    var v = ($("#wlGroupDiv").val() || "").trim().toLowerCase();
    if (v === "monthend") return "month";
    if (v === "month") return "month";
    if (v === "recommend") return "recommend";
    return "normal";
  }

  function currentClosePrice() {
    return parseNumberText($("#kisHdrNow").text() || "");
  }

  function refreshWatchlist() {
    try {
      if (typeof global.loadWatchlistItems === "function") {
        global.loadWatchlistItems();
        return;
      }
    } catch (e) {}

    var $btn = $("#btnWlReload");
    if ($btn.length) {
      $btn.trigger("click");
    }
  }

  function markFav(on) {
    var $b = $("#kisHdrFav");
    if (!$b.length) return;

    $b.toggleClass("is-on", !!on);
    //var $sp = $b.find("span");
    //if ($sp.length) {
      //$sp.text(on ? "관심됨" : "관심");
    //}

    $b.contents().filter(function () {
      return this.nodeType === 3;
    }).remove();
    $b.prepend(on ? "★ " : "☆ ");
  }

  function debounce(fn, wait) {
    var t = null;
    return function () {
      var args = arguments;
      clearTimeout(t);
      t = setTimeout(function () {
        fn.apply(null, args);
      }, wait);
    };
  }

  var _lastKey = "";
  var _lastCheckedKey = "";
  var _lastCheckedAt = 0;
  var _xhrCheck = null;
  var CHECK_THROTTLE_MS = 3000;

  function syncFavState() {
    var gid = selectedGroupId();
    var groupDiv = selectedGroupDiv();
    var code = currentStockCode();

    if (!gid || !code) {
      markFav(false);
      _lastKey = "";
      _lastCheckedKey = "";
      _lastCheckedAt = 0;
      return;
    }

    var key = gid + "|" + groupDiv + "|" + code;
    if (key === _lastKey && _xhrCheck) {
      return;
    }
    var now = Date.now();
    if (key === _lastCheckedKey && (now - _lastCheckedAt) < CHECK_THROTTLE_MS) {
      return;
    }
    _lastKey = key;
    _lastCheckedKey = key;
    _lastCheckedAt = now;

    try {
      if (_xhrCheck && _xhrCheck.readyState !== 4) {
        _xhrCheck.abort();
      }
    } catch (e) {}

    _xhrCheck = $.ajax({
      url: resolveCheckUrl(),
      type: "GET",
      dataType: "json",
      data: { groupId: gid, groupDiv: groupDiv, stockCode: code },
      success: function (res) {
        var u = unwrapOk(res);
        if (!u.ok) {
          markFav(false);
          return;
        }

        var sd = extractSingleData(res) || {};
        var favYn = (sd.favYn !== undefined && sd.favYn !== null) ? String(sd.favYn) : "";
        markFav(favYn === "Y");
      },
      error: function () {
        markFav(false);
      },
      complete: function () {
        _xhrCheck = null;
        _lastCheckedAt = Date.now();
      }
    });
  }

  var syncFavStateDebounced = debounce(syncFavState, 250);

  function bindAutoSync() {
    $(document).on("change", "#wlGroup, #wlGroupDiv", function () {
      syncFavStateDebounced();
    });

    var el = document.getElementById("kisHdrCode");
    if (el && global.MutationObserver) {
      var obs = new MutationObserver(function () {
        syncFavStateDebounced();
      });
      obs.observe(el, { childList: true, characterData: true, subtree: true });
    }

    $(document).on("change", "#stockCode", function () {
      syncFavStateDebounced();
    });

    setTimeout(function () {
      syncFavStateDebounced();
    }, 300);
  }

  function bindToggle() {
    $(document).on("click", "#kisHdrFav", function () {
      var code = currentStockCode();
      var gid = selectedGroupId();
      var groupDiv = selectedGroupDiv();

      if (!gid) {
        alert("관심그룹을 먼저 선택해 주세요.");
        return;
      }
      if (!code) {
        alert("종목 코드가 없습니다.");
        return;
      }

      var $btn = $(this);
      if ($btn.data("busy")) {
        return;
      }
      $btn.data("busy", true);

      $.ajax({
        url: resolveToggleUrl(),
        type: "POST",
        dataType: "json",
        data: {
          groupId: gid,
          groupDiv: groupDiv,
          stockCode: code,
          stockClose: currentClosePrice()
        },
        success: function (res) {
          var u = unwrapOk(res);
          if (!u.ok) {
            alert(u.msg || "관심 처리에 실패했습니다.");
            return;
          }

          var sd = extractSingleData(res) || {};
          var favYn = (sd.favYn !== undefined && sd.favYn !== null) ? String(sd.favYn) : "";

          if (favYn === "Y") {
            markFav(true);
          } else if (favYn === "N") {
            markFav(false);
          } else {
            markFav(!$btn.hasClass("is-on"));
          }

          refreshWatchlist();
          syncFavStateDebounced();
        },
        error: function () {
          alert("관심 처리에 실패했습니다.");
        },
        complete: function () {
          $btn.data("busy", false);
        }
      });
    });
  }

  global.FavoriteAction = {
    syncFavState: syncFavState,
    markFav: markFav
  };

  $(function () {
    bindToggle();
    bindAutoSync();
  });
})(window, window.jQuery);
