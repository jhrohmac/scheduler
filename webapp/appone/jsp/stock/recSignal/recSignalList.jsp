<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>추천 종목 분석</title>
<link rel="stylesheet" href="<c:url value='/appone/jsp/finance/kis/kisFinance/css/common.css' />">
<link rel="stylesheet" href="<c:url value='/appone/jsp/finance/kis/kisFinance/css/kisDashboardChart.css?v=20260305-1' />">
<link rel="stylesheet" href="<c:url value='/appone/jsp/stock/recSignal/css/recSignalList.css?v=20260317-8' />">
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script>
window.__CTX_PATH = "${pageContext.request.contextPath}";
window.__HIGHCHARTS_SRC = {
  highstock: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/highstock.js' />",
  data: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/modules/data.js' />",
  exporting: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/modules/exporting.js' />"
};
</script>
<script defer src="<c:url value='/appone/jsp/finance/kis/kisFinance/js/maScript.js' />"></script>
<script defer src="<c:url value='/appone/jsp/finance/kis/kisFinance/js/doubleMonthChartScript.js?v=20260203-7' />"></script>
<script defer src="<c:url value='/appone/jsp/finance/kis/kisFinance/js/kisDashboardChartRenderer.js?v=20260305-1' />"></script>
<script defer src="<c:url value='/appone/jsp/finance/kis/kisFinance/js/chartScript.js?v=20260312-4' />"></script>
</head>
<body>
<div class="rec-shell">
  <div id="pageNotice" class="page-notice" hidden></div>
  <section id="batchStatusPanel" class="batch-status-panel" hidden></section>

  <section class="summary-grid" id="summaryGrid">
    <article class="summary-card accent">
      <span class="summary-label">추천 종목 수</span>
      <strong id="summaryRecommended">-</strong>
      <small>추천 조건을 충족한 종목</small>
    </article>
    <article class="summary-card">
      <span class="summary-label">A 등급</span>
      <strong id="summaryGradeA">-</strong>
      <small>이달 상승률 3% 이상</small>
    </article>
    <article class="summary-card">
      <span class="summary-label">B 등급</span>
      <strong id="summaryGradeB">-</strong>
      <small>이달 상승률 0% 초과 3% 미만</small>
    </article>
    <article class="summary-card">
      <span class="summary-label">C 등급</span>
      <strong id="summaryGradeC">-</strong>
      <small>추천 대상이지만 5일선 이하</small>
    </article>
  </section>

  <section class="toolbar-card">
    <div class="toolbar-left">
      <div class="toolbar-group">
        <label class="toolbar-label" for="countryFilter">국가</label>
        <div class="chip-group" id="countryFilter">
          <button type="button" class="filter-chip active" data-country="KR">한국</button>
          <button type="button" class="filter-chip" data-country="US">미국</button>
        </div>
      </div>
      <div class="toolbar-group">
        <label class="toolbar-label" for="marketFilter">시장</label>
        <div class="chip-group" id="marketFilter"></div>
      </div>
      <div class="toolbar-group">
        <label class="toolbar-label" for="gradeFilter">등급 필터</label>
        <div class="chip-group" id="gradeFilter">
          <button type="button" class="filter-chip active" data-grade="ALL">전체</button>
          <button type="button" class="filter-chip" data-grade="A">A</button>
          <button type="button" class="filter-chip" data-grade="B">B</button>
          <button type="button" class="filter-chip" data-grade="C">C</button>
        </div>
      </div>
    </div>
    <div class="toolbar-right">
      <span id="toolbarBaseDateText" class="toolbar-base-date">조회 기준일 -</span>
      <div class="toolbar-group">
        <label class="toolbar-label" for="sortFilter">랭킹 정렬</label>
        <select id="sortFilter" class="toolbar-select">
          <option value="rank">기본 순위</option>
          <option value="monthChange">월간 상승률</option>
          <option value="trend">추세 강도</option>
          <option value="tradeValue">평균 거래대금</option>
        </select>
      </div>
    </div>
  </section>

  <main class="analysis-grid">
    <section class="panel rank-panel">
      <div class="panel-head">
        <div>
          <span class="panel-kicker">추천 리스트</span>
          <h4 style="margin-bottom: 5px; margin-top: 5px;">추천 종목</h4>
        </div>
      </div>
      <div id="rankList" class="rank-list" style="padding-top: 5px;"></div>
    </section>

    <section class="detail-column">
      <section class="panel detail-panel">
        <div class="detail-head">
          <div>
            <span class="panel-kicker">선택 종목</span>
            <h2 id="detailTitle">-</h2>
            <p id="detailSubtitle" class="detail-subtitle">-</p>
          </div>
          <div class="detail-actions">
            <span class="detail-tab active">차트</span>
            <a id="btnChartOptions" class="detail-link" href="#"><span aria-hidden="true">⚙</span></a>
            <span id="detailGrade" class="grade-chip">-</span>
            <span id="detailRecommend" class="status-chip">추천 상태 확인 중</span>
            <button type="button" id="btnSaveToWatchlist" class="detail-link detail-link-button pick-btn" disabled>
              <span aria-hidden="true">★</span> 픽 등록
            </button>
            <a id="detailPageLink" class="detail-link" href="#">상세페이지</a>
          </div>
        </div>

        <div class="detail-chart-card">
          <div class="detail-chart-meta">
            <div class="kis-info-line">
              <div class="kis-info-left">
                <span id="kisHdrFlag" class="kis-flag">🇰🇷</span>
                <img id="kisHdrLogo" class="kis-logo" alt="" style="display:none;">
                <span id="kisHdrName" class="kis-name">-</span>
                <span id="kisHdrCode" class="kis-code">-</span>
                <span class="kis-dot">·</span>
                <span id="kisHdrMarket" class="kis-sub">-</span>
              </div>
              <div class="kis-info-right">
                <span id="kisHdrNow" class="kis-now">-</span>
                <span id="kisHdrPct" class="kis-pct">-</span>
                <span id="kisHdrDiff" class="kis-diff">-</span>
              </div>
            </div>
            <div class="kis-h-bottom">
              <div class="kis-b-left">
                <span id="kisHdrDt" class="kis-dt">-</span>
                <span class="kis-ohlc">
                  <span class="kis-k">시</span> <span id="kisHdrO" class="kis-v">-</span>
                  <span class="kis-k">고</span> <span id="kisHdrH" class="kis-v">-</span>
                  <span class="kis-k">저</span> <span id="kisHdrL" class="kis-v">-</span>
                  <span class="kis-k">종</span> <span id="kisHdrC" class="kis-v">-</span>
                </span>
                <span id="kisHdrVol" class="kis-meta">거래량 -</span>
              </div>
              <div class="kis-b-right">
                <span class="kis-ma-label">MA</span>
                <span id="kisMaLegend"></span>
              </div>
            </div>
          </div>

          <div class="detail-chart-stage">
            <input type="hidden" id="stockCode" value="">
            <input type="hidden" id="fromDate" value="">
            <input type="hidden" id="toDate" value="">
            <input type="hidden" id="periodDivCode" value="D">
            <div id="kisChartContainer" class="rec-chart-container"></div>
          </div>
        </div>

        <div class="metric-grid" id="metricGrid"></div>
      </section>

    </section>
  </main>

  <section class="panel table-panel">
    <div class="panel-head">
      <div>
        <span class="panel-kicker">전체 목록</span>
        <h2>추천 종목 테이블</h2>
      </div>      
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th scope="col"><button type="button" class="table-sort active asc" data-sort-key="rank">순위</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="name">종목</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="grade">등급</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="currentPrice">현재가</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="monthOpenPrice">월시가</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="monthChange">월간 상승률</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="ma5">5일 평균가</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="trendStrength">추세 강도</button></th>
            <th scope="col"><button type="button" class="table-sort" data-sort-key="avgTradeValue20">평균 거래대금(20)</button></th>
          </tr>
        </thead>
        <tbody id="comparisonBody"></tbody>
      </table>
    </div>
  </section>
</div>

<div class="chart-opt-backdrop" id="chartOptBackdrop"></div>
<div id="chartOptionsModal" class="chart-opt-panel" aria-hidden="true">
  <div class="opt-panel-hdr">
    <span class="opt-panel-title">차트 옵션</span>
    <button type="button" class="opt-close-btn" id="btnChartOptClose" aria-label="닫기">×</button>
  </div>

  <div class="opt-section">
    <div class="opt-sec-hdr">
      <span class="opt-sec-title">이동평균선</span>
      <button type="button" class="opt-add-btn" id="btnAddMaLine">+ 추가</button>
    </div>
    <div id="maRowList" class="opt-ma-list"></div>
  </div>

  <div class="opt-section">
    <div class="opt-toggle-row">
      <div class="opt-toggle-info">
        <div class="opt-toggle-lbl">전고 / 전저점</div>
        <div class="opt-toggle-sub">화면 범위의 최고가 · 최저가 수평선</div>
      </div>
      <label class="toggle-sw">
        <input type="checkbox" id="optHighLow">
        <span class="toggle-sl"></span>
      </label>
    </div>
  </div>

  <div class="opt-section">
    <div class="opt-toggle-row">
      <div class="opt-toggle-info">
        <div class="opt-toggle-lbl">거래량</div>
        <div class="opt-toggle-sub">하단 거래량 바 차트 표시</div>
      </div>
      <label class="toggle-sw">
        <input type="checkbox" id="optVolume">
        <span class="toggle-sl"></span>
      </label>
    </div>
  </div>

  <div class="opt-section">
    <div class="opt-toggle-row">
      <div class="opt-toggle-info">
        <div class="opt-toggle-lbl">틱푸시</div>
        <div class="opt-toggle-sub">실시간 틱 기반 차트 갱신</div>
      </div>
      <label class="toggle-sw">
        <input type="checkbox" id="optTickPushEnabled" disabled>
        <span class="toggle-sl"></span>
      </label>
    </div>
  </div>

  <div class="opt-section">
    <div class="opt-toggle-info" style="margin-bottom:8px;">
      <div class="opt-toggle-lbl">더블차트</div>
      <div class="opt-toggle-sub">일봉 위에 월봉 미니차트 표시</div>
    </div>
    <input type="checkbox" id="optDoubleChart" style="display:none;">
    <div class="opt-radio-row">
      <label><input type="radio" name="optDoubleChartMode" value="off"> off</label>
      <label><input type="radio" name="optDoubleChartMode" value="recent"> 최근월봉</label>
      <label><input type="radio" name="optDoubleChartMode" value="all"> 전체월봉</label>
    </div>
  </div>

  <div class="opt-section" style="display:none;">
    <div class="opt-toggle-row">
      <div class="opt-toggle-info">
        <div class="opt-toggle-lbl">월/년 구분선</div>
      </div>
      <label class="toggle-sw">
        <input type="checkbox" id="optMonthLines">
        <span class="toggle-sl"></span>
      </label>
    </div>
  </div>

  <div class="opt-section" style="display:none;">
    <div class="opt-toggle-row">
      <div class="opt-toggle-info">
        <div class="opt-toggle-lbl">MA 지지/저항</div>
      </div>
      <label class="toggle-sw">
        <input type="checkbox" id="optMaSr">
        <span class="toggle-sl"></span>
      </label>
    </div>
  </div>

  <div class="opt-section">
    <label class="checkbox-inline" style="margin-right: 5px;">
      <input type="checkbox" id="optCrossSignals"> 골든/데드
    </label>
    <span class="cross-pair-wrap" style="display:inline-flex; align-items:center; gap:6px;">
      <select id="optCrossShort" style="width:74px; display:inline-block;">
        <option value="5">5</option>
        <option value="10">10</option>
        <option value="20">20</option>
        <option value="60">60</option>
        <option value="120">120</option>
        <option value="240">240</option>
      </select>
      <span style="opacity:0.7;">/</span>
      <select id="optCrossLong" style="width:74px; display:inline-block;">
        <option value="5">5</option>
        <option value="10">10</option>
        <option value="20">20</option>
        <option value="60">60</option>
        <option value="120">120</option>
        <option value="240">240</option>
      </select>
      <span id="crossPairLabel" style="margin-left:6px; font-size:12px; opacity:0.7;">(5/20)</span>
    </span>
  </div>

  <div class="opt-panel-footer">
    <button type="button" class="btn" id="btnMaDefault">기본값</button>
    <button type="button" class="btn" id="btnApplyMaOptions">적용</button>
  </div>
</div>

<script>
window.recSignalConfig = {
  listUrl: "<c:url value='/stock/recSignal/list.do' />",
  detailUrl: "<c:url value='/stock/recSignal/detail.do' />",
  batchStatusUrl: "<c:url value='/stock/recSignal/batchStatus.do' />",
  detailViewUrl: "<c:url value='/stock/recSignal/detailView.do' />",
  saveToWatchlistUrl: "<c:url value='/stock/recPick/saveToWatchlist.do' />",
  baseDt: "<c:out value='${param.baseDt}' />",
  mktCd: "<c:out value='${param.mktCd}' />",
  marketFilter: "<c:out value='${param.marketFilter}' />"
};
</script>
<script>
window.__URLS = {
  kisItemchartpriceOptionData: "<c:url value='/finance/kisItemchartpriceOptionData.do' />",
  kisItemchartpriceOptionSave: "<c:url value='/finance/kisItemchartpriceOptionSave.do' />",
  selectChartEventMarkers: "<c:url value='/finance/selectChartEventMarkers.do' />",
  selectPositionState: "<c:url value='/finance/selectPositionState.do' />"
};
</script>
<script defer src="<c:url value='/appone/jsp/stock/recSignal/js/recSignalChartOptions.js?v=20260317-1' />"></script>
<script defer src="<c:url value='/appone/jsp/stock/recSignal/js/recSignalList.js?v=20260317-6' />"></script>
</body>
</html>
