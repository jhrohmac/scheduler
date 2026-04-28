(function () {
  var config = window.recSignalDetailConfig || {};
  var state = {
    baseDt: config.baseDt || "",
    stkCd: config.stkCd || "",
    mktCd: config.mktCd || ""
  };

  var els = {
    detailTitle: document.getElementById("detailTitle"),
    detailSubtitle: document.getElementById("detailSubtitle"),
    detailGrade: document.getElementById("detailGrade"),
    detailRecommend: document.getElementById("detailRecommend"),
    detailCurrentPrice: document.getElementById("detailCurrentPrice"),
    detailMonthChange: document.getElementById("detailMonthChange"),
    detailReason: document.getElementById("detailReason"),
    detailGoldenYn: document.getElementById("detailGoldenYn"),
    detailMonthUpYn: document.getElementById("detailMonthUpYn"),
    detailTrendStrength: document.getElementById("detailTrendStrength"),
    detailTradeValue: document.getElementById("detailTradeValue"),
    metricGrid: document.getElementById("metricGrid"),
    decisionList: document.getElementById("decisionList"),
    maLadder: document.getElementById("maLadder"),
    analysisNoteList: document.getElementById("analysisNoteList"),
    listPageLink: document.getElementById("listPageLink")
  };

  function fetchJson(url, params) {
    var query = params ? "?" + new URLSearchParams(params).toString() : "";
    return fetch(url + query, {
      headers: { "Accept": "application/json" },
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

  function normalizeStock(raw) {
    return {
      code: String(raw.stkCd || raw.stk_cd || raw.code || config.stkCd || ""),
      name: String(raw.stkNm || raw.stk_nm || raw.name || ""),
      mktCd: String(raw.mktCd || raw.mkt_cd || config.mktCd || "KR"),
      grade: String(raw.recGrade || raw.rec_grade || raw.grade || ""),
      recYn: String(raw.recYn || raw.rec_yn || "N"),
      baseDt: String(raw.baseDt || raw.base_dt || config.baseDt || ""),
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

  function normalizeList(list) {
    return Array.isArray(list) ? list.map(normalizeStock) : [];
  }

  function monthChangeRate(stock) {
    if (!stock || !stock.monthOpenPrice) {
      return 0;
    }
    return ((stock.currentPrice - stock.monthOpenPrice) / stock.monthOpenPrice) * 100;
  }

  function formatNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
  }

  function formatCurrency(value) {
    return formatNumber(value) + "원";
  }

  function formatBillion(value) {
    var billion = Number(value || 0) / 100000000;
    return billion.toLocaleString("ko-KR", { maximumFractionDigits: 0 }) + "억";
  }

  function gradeClass(grade) {
    return "grade-" + String((grade || "").toLowerCase());
  }

  function marketLabel(mktCd) {
    return String(mktCd || "").toUpperCase() === "US" ? "미국시장" : "국내시장";
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
      return ["상세 데이터를 불러오면 실제 저장 데이터 기준 해석 메모가 표시됩니다."];
    }

    var rate = monthChangeRate(stock);
    var maGap = stock.currentPrice - stock.ma5;
    var notes = [
      marketLabel(stock.mktCd) + " " + stock.name + "(" + stock.code + ")의 기준일은 " + (stock.baseDt || config.baseDt || "-") + "입니다.",
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

  function formatPct(value) {
    var cls = value > 0 ? "positive" : "neutral";
    var sign = value > 0 ? "+" : "";
    return '<span class="' + cls + '">' + sign + value.toFixed(2) + '%</span>';
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
        '    <strong class="mono">' + formatCurrency(item.value) + '</strong>',
        '  </div>',
        '  <div class="ma-bar"><span class="ma-fill" style="width:' + pct.toFixed(2) + '%;"></span></div>',
        '</div>'
      ].join("");
    }).join("");
  }

  function renderStock(stock) {
    var rate = monthChangeRate(stock);
    var metrics = [
      { label: "월초 시가", value: formatCurrency(stock.monthOpenPrice) },
      { label: "5일 평균가", value: formatCurrency(stock.ma5) },
      { label: "20일 평균가", value: formatCurrency(stock.ma20) },
      { label: "60일 평균가", value: formatCurrency(stock.ma60) },
      { label: "120일 평균가", value: formatCurrency(stock.ma120) },
      { label: "240일 평균가", value: formatCurrency(stock.ma240) },
      { label: "5일선 대비", value: formatSignedCurrency(stock.currentPrice - stock.ma5) },
      { label: "월간 상승률", value: rate.toFixed(2) + "%" }
    ];

    state.baseDt = stock.baseDt || state.baseDt || "";
    state.stkCd = stock.code || state.stkCd || "";
    state.mktCd = stock.mktCd || state.mktCd || "";

    els.detailTitle.textContent = stock.name;
    els.detailSubtitle.textContent = marketLabel(stock.mktCd) + " · " + stock.code + " · 기준일 " + (stock.baseDt || "-");
    els.detailGrade.className = "grade-chip " + gradeClass(stock.grade);
    els.detailGrade.textContent = (stock.grade || "-") + " 등급";
    els.detailRecommend.textContent = recommendStatusText(stock);
    els.detailCurrentPrice.textContent = formatCurrency(stock.currentPrice);
    els.detailMonthChange.innerHTML = "월시가 " + formatCurrency(stock.monthOpenPrice) + " 대비 " + formatPct(rate);
    els.detailReason.textContent = buildReasonText(stock);
    els.detailGoldenYn.textContent = yesNoText(stock.goldenYn);
    els.detailMonthUpYn.textContent = yesNoText(stock.monthUpYn);
    els.detailTrendStrength.textContent = stock.trendStrength.toFixed(1);
    els.detailTradeValue.textContent = formatBillion(stock.avgTradeValue20);
    els.metricGrid.innerHTML = metrics.map(function (metric) {
      return [
        '<article class="metric-card">',
        '  <span class="metric-label">' + metric.label + '</span>',
        '  <strong class="mono">' + metric.value + '</strong>',
        '</article>'
      ].join("");
    }).join("");

    if (els.listPageLink) {
      var params = [];
      if (state.baseDt) {
        params.push("baseDt=" + encodeURIComponent(state.baseDt));
      }
      if (state.mktCd) {
        params.push("mktCd=" + encodeURIComponent(state.mktCd));
      }
      els.listPageLink.href = config.listViewUrl + (params.length ? "?" + params.join("&") : "");
    }

    syncCurrentUrl();
    renderDecision(stock);
    renderMaLadder(stock);
    renderAnalysisNotes(stock);
  }

  function renderLoadingState(message) {
    els.detailTitle.textContent = "상세 데이터 조회 중";
    els.detailSubtitle.textContent = message;
    els.detailGrade.className = "grade-chip";
    els.detailGrade.textContent = "-";
    els.detailRecommend.textContent = "조회 중";
    els.detailCurrentPrice.textContent = "조회 중";
    els.detailMonthChange.textContent = "운영 데이터를 불러오는 중입니다.";
    els.detailReason.textContent = "잠시만 기다려 주세요.";
    els.detailGoldenYn.textContent = "-";
    els.detailMonthUpYn.textContent = "-";
    els.detailTrendStrength.textContent = "-";
    els.detailTradeValue.textContent = "-";
    els.metricGrid.innerHTML = [
      '<article class="metric-card"><span class="metric-label">상태</span><strong class="mono">조회 중</strong></article>',
      '<article class="metric-card"><span class="metric-label">대상</span><strong class="mono">' + (state.stkCd || "최신 추천 종목 탐색") + '</strong></article>'
    ].join("");
    els.decisionList.innerHTML = [
      '<article class="decision-item watch">',
      '  <div class="decision-copy">',
      '    <strong>상세 데이터 조회 중</strong>',
      '    <p>' + message + '</p>',
      '  </div>',
      '  <span class="pill grade-b">조회 중</span>',
      '</article>'
    ].join("");
    els.maLadder.innerHTML = [
      '<div class="ma-row">',
      '  <div>',
      '    <div class="ma-label">상태</div>',
      '    <strong class="mono">조회 중</strong>',
      '  </div>',
      '  <div class="ma-bar"><span class="ma-fill" style="width:0%;"></span></div>',
        '</div>'
      ].join("");
    renderAnalysisNotes(null);
  }

  function loadDetailByParams(baseDt, stkCd, mktCd) {
    return fetchJson(config.detailUrl, { baseDt: baseDt, stkCd: stkCd, mktCd: mktCd }).then(function (json) {
      if (isSuccessResponse(json) && json.singleData) {
        var stock = normalizeStock(json.singleData);
        renderStock(stock);
        return;
      }

      renderEmptyState("TB_REC_SIGNAL 에 저장된 상세 스냅샷이 없습니다.");
    }).catch(function () {
      renderEmptyState("상세 조회 API 실패로 운영 데이터를 불러오지 못했습니다.");
    });
  }

  function pickDefaultStock(list) {
    return list && list.length ? list[0] : null;
  }

  function resolveInitialStock() {
    if (!config.listUrl) {
      return Promise.resolve(null);
    }

    var params = {};
    if (state.baseDt) {
      params.baseDt = state.baseDt;
    }
    if (state.mktCd) {
      params.mktCd = state.mktCd;
    }

    return fetchJson(config.listUrl, params).then(function (json) {
      if (!isSuccessResponse(json)) {
        return null;
      }
      return pickDefaultStock(normalizeList(json.data));
    }).catch(function () {
      return null;
    });
  }

  function syncCurrentUrl() {
    if (!window.history || typeof window.history.replaceState !== "function" || !state.stkCd) {
      return;
    }

    var params = new URLSearchParams();
    if (state.baseDt) {
      params.set("baseDt", state.baseDt);
    }
    params.set("stkCd", state.stkCd);
    if (state.mktCd) {
      params.set("mktCd", state.mktCd);
    }

    window.history.replaceState(null, document.title, window.location.pathname + "?" + params.toString());
  }

  function loadDetail() {
    if (!config.detailUrl) {
      renderEmptyState("상세 조회 API 설정이 없습니다.");
      return;
    }

    if (state.stkCd) {
      renderLoadingState("요청한 추천 종목 상세 데이터를 조회하는 중입니다.");
      loadDetailByParams(state.baseDt, state.stkCd, state.mktCd);
      return;
    }

    renderLoadingState("기본 진입 경로라 최신 추천 종목을 찾는 중입니다.");
    resolveInitialStock().then(function (stock) {
      if (!stock || !stock.code) {
        renderEmptyState("상세 조회 파라미터가 없고 기본 추천 종목도 찾지 못했습니다.");
        return;
      }

      loadDetailByParams(stock.baseDt, stock.code, stock.mktCd);
    });
  }

  function renderEmptyState(message) {
    els.detailTitle.textContent = "운영 데이터 없음";
    els.detailSubtitle.textContent = message;
    els.detailGrade.className = "grade-chip";
    els.detailGrade.textContent = "-";
    els.detailRecommend.textContent = "추천 상태 없음";
    els.detailCurrentPrice.textContent = "-";
    els.detailMonthChange.textContent = "운영 데이터가 없습니다.";
    els.detailReason.textContent = "배치 실행 결과와 TB_REC_SIGNAL 저장 상태를 확인하세요.";
    els.detailGoldenYn.textContent = "-";
    els.detailMonthUpYn.textContent = "-";
    els.detailTrendStrength.textContent = "-";
    els.detailTradeValue.textContent = "-";
    els.metricGrid.innerHTML = [
      '<article class="metric-card"><span class="metric-label">상태</span><strong class="mono">데이터 없음</strong></article>',
      '<article class="metric-card"><span class="metric-label">확인 대상</span><strong class="mono">배치 / 저장 데이터</strong></article>'
    ].join("");
    els.decisionList.innerHTML = [
      '<article class="decision-item fail">',
      '  <div class="decision-copy">',
      '    <strong>상세 스냅샷 없음</strong>',
      '    <p>' + message + '</p>',
      '  </div>',
      '  <span class="pill grade-a">데이터 없음</span>',
      '</article>'
    ].join("");
    els.maLadder.innerHTML = [
      '<div class="ma-row">',
      '  <div>',
      '    <div class="ma-label">상태</div>',
      '    <strong class="mono">MA 정보 없음</strong>',
      '  </div>',
      '  <div class="ma-bar"><span class="ma-fill" style="width:0%;"></span></div>',
        '</div>'
      ].join("");
    renderAnalysisNotes(null);
  }

  loadDetail();
})();
