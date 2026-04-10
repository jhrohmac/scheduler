var StockSearch = (function () {
  var apiUrl = "/scheduler/finance/searchStocksKeyword.do";

  var $input;
  var $clear;
  var $box;
  var $wrap;
  var $trigger;
  var $close;

  var timer = null;
  var items = [];
  var activeIndex = -1;
  var lastKeyword = "";

  function esc(s) {
    if (s === null || s === undefined) return "";
    return String(s)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/\'/g, "&#39;");
  }

  function highlight(text, keyword) {
    var t = esc(text);
    if (!keyword) return t;
    var k = String(keyword).trim();
    if (!k) return t;

    // 대소문자 무시 + 특수문자 이스케이프
    var re = new RegExp(k.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), "ig");
    return t.replace(re, function (m) {
      return "<span class='s-hl'>" + esc(m) + "</span>";
    });
  }

  function unwrapList(res) {
    if (!res) return [];

    // ResponseHandler 포맷 (system_code / data)
    if (res.data && Array.isArray(res.data)) {
      return res.data;
    }

    // DataTableSettingVo 형태 (result_code / data)
    if (res.data && res.data.data && Array.isArray(res.data.data)) {
      return res.data.data;
    }
    if (Array.isArray(res.list)) {
      return res.list;
    }

    return [];
  }

  function getMarketLabel(item) {
    var m = (item.stock_market || item.groupMarket || item.market || item.market_section || "");
    if (!m) return "";
    if (m === "KOSPI" || m === "kospi") return "코스피";
    if (m === "KOSDAQ" || m === "kosdaq") return "코스닥";
    if (m === "ETF" || m === "etf") return "ETF";
    return String(m);
  }

  function getIconText(item) {
    var t = (item.stock_type || item.stockType || item.type || "");
    var m = getMarketLabel(item);
    if (String(t).toUpperCase().indexOf("ETF") !== -1 || String(m).toUpperCase().indexOf("ETF") !== -1) {
      return "ETF";
    }
    var name = item.stock_ko_name || item.stockKoName || item.stock_name || item.name || "";
    name = String(name).trim();
    if (!name) return "S";
    // 한글이면 첫 글자, 영문이면 첫 알파벳
    return name.substring(0, 1).toUpperCase();
  }

  
  function getLogoUrl(item) {
    var u =
      (item.logo_url || item.logoUrl ||
        item.stock_url || item.stockLogoUrl ||
        item.stock_logo || item.stockLogo ||
        item.stock_logo_path || item.stockLogoPath ||
        item.logo_path || item.logoPath ||
        item.img_url || item.imgUrl ||
        item.image_url || item.imageUrl || "");
    u = String(u || "").trim();
    if (!u) return "";
    return u;
  }

  function renderIconHtml(item) {
    var iconText = getIconText(item);
    var logoUrl = getLogoUrl(item);

    if (logoUrl) {
      return "<div class='s-ico has-logo'>"
        + "<img class='s-logo' src='" + esc(logoUrl) + "' alt='' "
        + "onerror='this.style.display='none'; this.parentNode.classList.remove('has-logo'); "
		+ "var t=this.parentNode.querySelector('.s-ico-txt'); if(t){t.style.display='flex';}'/>"
        + "<span class='s-ico-txt' style='display:none;'>" + esc(iconText) + "</span>"
        + "</div>";
    }

    return "<div class='s-ico'><span class='s-ico-txt'>" + esc(iconText) + "</span></div>";
  }

  function render(keyword) {
    if (!items || !items.length) {
      $box.html("<div class='s-empty'>검색 결과가 없습니다.</div>");
      showBox();
      return;
    }

    var html = "<div class='s-scroll'>";
    for (var i = 0; i < items.length; i++) {
      var it = items[i] || {};
      var code = (it.stock_code || it.stockCode || it.code || "");
      var name = (it.stock_ko_name || it.stockKoName || it.stock_name || it.name || "");
      var market = getMarketLabel(it);
      var iconHtml = renderIconHtml(it);
      html += "<div class='s-item' data-idx='" + i + "' data-code='" + esc(code) + "' data-name='" + esc(name) + "'>";
      html += "  " + iconHtml;
      html += "  <div class='s-main'>";
      html += "    <div class='s-name'>" + highlight(name, keyword) + "</div>";
      html += "    <div class='s-sub'>" + highlight(code, keyword);
      if (market) {
        html += "<span class='s-dot'>·</span>" + esc(market);
      }
      html += "    </div>";
      html += "  </div>";
      html += "</div>";
    }

    html += "</div>";

    $box.html(html);
    activeIndex = -1;
    showBox();
  }

  function getKeyword() {
    return String(($input && $input.length ? $input.val() : "") || "").trim();
  }

  function isExpanded() {
    return !!($wrap && $wrap.length && $wrap.hasClass("is-expanded"));
  }

  function isBoxOpen() {
    return !!($box && $box.length && $box.hasClass("is-open"));
  }

  function syncWrapState() {
    var keyword = getKeyword();
    var isFocused = !!($input && $input.length && document.activeElement === $input[0]);

    if ($wrap && $wrap.length) {
      $wrap.toggleClass("has-value", !!keyword);
      $wrap.toggleClass("is-focused", isFocused);
      $wrap.toggleClass("is-open", isBoxOpen());
    }

    if ($trigger && $trigger.length) {
      $trigger.attr("aria-expanded", isExpanded() ? "true" : "false");
    }
  }

  function focusInput(selectAll) {
    var run = function () {
      if (!$input || !$input.length) return;
      $input.trigger("focus");
      if (selectAll) {
        $input.select();
      }
      syncWrapState();
    };

    if (window.requestAnimationFrame) {
      window.requestAnimationFrame(run);
    } else {
      window.setTimeout(run, 16);
    }
  }

  function openSearch(options) {
    var opts = options || {};
    if ($wrap && $wrap.length) {
      $wrap.addClass("is-expanded");
    }
    if (opts.focus !== false) {
      focusInput(!!opts.select);
    } else {
      syncWrapState();
    }
  }

  function collapseSearch(options) {
    var opts = options || {};

    if (timer) {
      clearTimeout(timer);
      timer = null;
    }

    hideBox();

    if (opts.clear && $input && $input.length) {
      $input.val("");
    }

    if ($wrap && $wrap.length) {
      $wrap.removeClass("is-expanded");
    }

    if ($input && $input.length && document.activeElement === $input[0]) {
      $input.trigger("blur");
    }

    syncWrapState();
  }

  function showBox() {
    if (!$box || !$box.length) return;
    if (!isExpanded()) {
      openSearch({ focus: false });
    }
    $box.addClass("is-open").attr("aria-hidden", "false");
    syncWrapState();
  }

  function hideBox() {
    if (!$box || !$box.length) return;
    $box.removeClass("is-open").attr("aria-hidden", "true");
    activeIndex = -1;
    syncWrapState();
  }

  function setActive(idx) {
    if (!items || !items.length) return;
    if (idx < 0) idx = 0;
    if (idx >= items.length) idx = items.length - 1;
    activeIndex = idx;

    $box.find(".s-item").removeClass("is-active");
    var $row = $box.find(".s-item[data-idx='" + idx + "']");
    $row.addClass("is-active");

    // 스크롤 가시영역 보정
    var $scroller = $box.find(".s-scroll");
    if (!$scroller.length) {
      $scroller = $box;
    }

    var rowTop = $row.position().top;
    var rowBottom = rowTop + $row.outerHeight();
    var boxScrollTop = $scroller.scrollTop();
    var boxH = $scroller.innerHeight();

    if (rowTop < 0) {
      $scroller.scrollTop(boxScrollTop + rowTop);
    } else if (rowBottom > boxH) {
      $scroller.scrollTop(boxScrollTop + (rowBottom - boxH));
    }
  }

  function applySelection(idx) {
    if (!items || !items.length) return;
    if (idx < 0 || idx >= items.length) return;

    var it = items[idx] || {};
    var code = (it.stock_code || it.stockCode || it.code || "");
    var name = (it.stock_ko_name || it.stockKoName || it.stock_name || it.name || "");

    if (code) {
      var $stockCode = $("#stockCode");
      if ($stockCode.length) {
        $stockCode.val(code);
      }

      // 선택 직후 조회 실행 (버튼이 있으면 클릭, 없으면 ChartScript 직접 호출)
      var $btn = $("#btnSearch");
      if ($btn.length) {
        $btn.trigger("click");
      } else if (typeof ChartScript !== "undefined" && ChartScript.loadKisItemchartprice) {
        var params = {
          stockCode: code,
          periodDivCode: $("#periodDivCode").val() || "D",
          fromDate: ("" + ($("#fromDate").val() || "")).replace(/-/g, ""),
          toDate: ("" + ($("#toDate").val() || "")).replace(/-/g, ""),
          orgAdjPrc: "1",
          stockMarket: (it.stock_market || it.groupMarket || it.market || it.market_section || ""),
          stockCountryCode: (it.stock_country_code || it.country_code || it.country || "")
        };
        ChartScript.loadKisItemchartprice(params);

        // 실시간 헤더(현재가/등락률) 갱신: 새 종목으로 ChartPriceRealtime 재연결
        try {
          if (window.ChartPriceRealtime && typeof window.ChartPriceRealtime.connect === "function") {
            var q = (window.ChartScript && window.ChartScript.lastQuery) ? window.ChartScript.lastQuery : {};
            var rtCode = (q.stockCode || code || "").trim();
            var rtUpper = rtCode.toUpperCase();
            var isIndexOrFx = (rtUpper.charAt(0) === "." || rtUpper === "0001" || rtUpper === "1001" || rtUpper === "2001"
              || rtUpper === "KOSPI" || rtUpper === "KOSDAQ" || rtUpper === "KOSPI200" || rtUpper === "USDKRW");
            if (!isIndexOrFx && rtCode) {
              var rtCountry = (q.stockCountryCode || "").trim();
              var rtMarket = (q.stockMarket || "").trim();
              if (!rtMarket && rtCountry) {
                rtMarket = (rtCountry === "KR") ? "KRX" : "NAS";
              }
              var rtToken = rtCountry ? (rtCountry + "|" + rtMarket + "|" + rtCode) : rtCode;
              window.ChartPriceRealtime.connect(rtToken);
            }
          }
        } catch (e) {}
      }
    }

    // UX: 입력창에는 이름을 보여주고, 드롭다운은 닫음
    if (name) {
      $input.val(name);
    }

    hideBox();
    syncWrapState();
  }

  function fetch(keyword) {
    lastKeyword = keyword;
    $.ajax({
      url: apiUrl,
      type: "GET",
      dataType: "json",
      data: {
        in_stockCode: keyword
      },
      success: function (res) {
        items = unwrapList(res);

        // 너무 많으면 상위 12개만
        if (items.length > 12) {
          items = items.slice(0, 12);
        }

        // 현재 입력값이 달라졌으면 렌더하지 않음
        var nowVal = String($input.val() || "").trim();
        if (nowVal !== lastKeyword) {
          return;
        }

        render(keyword);
      },
      error: function () {
        items = [];
        render(keyword);
      }
    });
  }

  function onInput() {
    var keyword = getKeyword();
    syncWrapState();

    if (!keyword) {
      hideBox();
      return;
    }

    if (timer) {
      clearTimeout(timer);
      timer = null;
    }

    timer = setTimeout(function () {
      fetch(keyword);
    }, 180);
  }

  function bindEvents() {
    // Alt+S => 검색 포커스
    $(document).on("keydown", function (e) {
      if (e.altKey && (e.key === "s" || e.key === "S")) {
        e.preventDefault();
        openSearch({ focus: true, select: true });
      }
    });

    $input.on("input", onInput);

    $input.on("keydown", function (e) {
      if (e.key === "Escape") {
        e.preventDefault();
        if (isBoxOpen()) {
          hideBox();
        } else {
          collapseSearch({ clear: true });
        }
        return;
      }

      if (!isBoxOpen()) return;

      if (e.key === "ArrowDown") {
        e.preventDefault();
        setActive(activeIndex + 1);
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        setActive(activeIndex - 1);
      } else if (e.key === "Enter") {
        e.preventDefault();
        if (activeIndex < 0 && items.length) {
          applySelection(0);
        } else {
          applySelection(activeIndex);
        }
      }
    });

    $trigger.on("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      openSearch({ focus: true, select: !getKeyword() });
      if (getKeyword()) {
        onInput();
      }
    });

    $clear.on("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      $input.val("");
      hideBox();
      openSearch({ focus: true });
      syncWrapState();
    });

    $close.on("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      collapseSearch({ clear: true });
    });

    // 클릭 선택
    $box.on("click", ".s-item", function () {
      var idx = parseInt($(this).attr("data-idx"), 10);
      if (isNaN(idx)) return;
      applySelection(idx);
    });

    // 바깥 클릭 => 닫기
    $(document).on("mousedown", function (e) {
      var keyword;

      if (!isExpanded() && !isBoxOpen()) return;
      if ($(e.target).closest(".top-search-wrap").length) return;

      keyword = getKeyword();
      hideBox();

      if (!keyword) {
        collapseSearch({ clear: false });
      }
    });

    // 포커스 시, 값이 있으면 재조회
    $input.on("focus", function () {
      var keyword = getKeyword();
      openSearch({ focus: false });
      if (keyword) {
        onInput();
      }
    }).on("blur", function () {
      window.setTimeout(function () {
        if (!isBoxOpen() && !getKeyword()) {
          collapseSearch({ clear: false });
        } else {
          syncWrapState();
        }
      }, 120);
    });
  }

  function init() {
    $wrap = $("#topSearchWrap");
    $input = $("#topSearchInput");
    $clear = $("#topSearchClear");
    $box = $("#topSuggest");
    $trigger = $("#topSearchTrigger");
    $close = $("#topSearchClose");

    if (!$input.length || !$box.length) return;

    bindEvents();
    syncWrapState();
  }

  return {
    init: init
  };
})();

$(function () {
  if (typeof StockSearch !== "undefined" && StockSearch.init) {
    StockSearch.init();
  }
});
