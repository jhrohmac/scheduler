var StockSearch = (function () {
  var apiUrl = "/scheduler/finance/searchStocksKeyword.do";

  var $input;
  var $clear;
  var $box;

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

  function showBox() {
    if (!$box || !$box.length) return;
    $box.show();
  }

  function hideBox() {
    if (!$box || !$box.length) return;
    $box.hide();
    activeIndex = -1;
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
    var rowTop = $row.position().top;
    var rowBottom = rowTop + $row.outerHeight();
    var boxScrollTop = $box.scrollTop();
    var boxH = $box.innerHeight();

    if (rowTop < 0) {
      $box.scrollTop(boxScrollTop + rowTop);
    } else if (rowBottom > boxH) {
      $box.scrollTop(boxScrollTop + (rowBottom - boxH));
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
      }
    }

    // UX: 입력창에는 이름을 보여주고, 드롭다운은 닫음
    if (name) {
      $input.val(name);
    }

    hideBox();
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
    var keyword = String($input.val() || "").trim();

    if (keyword.length > 0) {
      $clear.css("display", "inline-flex");
    } else {
      $clear.hide();
    }

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
        $input.focus();
        $input.select();
      }
    });

    $input.on("input", onInput);

    $input.on("keydown", function (e) {
      if (!$box.is(":visible")) return;

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
      } else if (e.key === "Escape") {
        hideBox();
      }
    });

    $clear.on("click", function () {
      $input.val("");
      $input.focus();
      $clear.hide();
      hideBox();
    });

    // 클릭 선택
    $box.on("click", ".s-item", function () {
      var idx = parseInt($(this).attr("data-idx"), 10);
      if (isNaN(idx)) return;
      applySelection(idx);
    });

    // 바깥 클릭 => 닫기
    $(document).on("mousedown", function (e) {
      if (!$box.is(":visible")) return;
      if ($(e.target).closest(".top-search-wrap").length) return;
      hideBox();
    });

    // 포커스 시, 값이 있으면 재조회
    $input.on("focus", function () {
      var keyword = String($input.val() || "").trim();
      if (keyword) {
        onInput();
      }
    });
  }

  function init() {
    $input = $("#topSearchInput");
    $clear = $("#topSearchClear");
    $box = $("#topSuggest");

    if (!$input.length || !$box.length) return;

    bindEvents();
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
