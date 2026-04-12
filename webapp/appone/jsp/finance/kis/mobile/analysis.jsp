<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>KIS Mobile Analysis</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />
<link rel="stylesheet" href="<c:url value='/appone/jsp/finance/kis/mobile/css/mobile.css?v=20260220-6' />">
</head>
<body>
<div id="mobileProgress" class="mobile-progress"></div>
<header class="toolbar">
  <div class="stock-info">
    <div class="name-code">
      <h1 class="stock-name">발굴분석</h1>
      <span class="stock-code">기능별 바로가기</span>
    </div>
  </div>
  <div class="actions">
    <button class="icon-btn search" aria-label="검색"><span class="mi material-symbols-outlined">search</span></button>
    <button class="icon-btn alarm" aria-label="알림"><span class="mi material-symbols-outlined">notifications</span><span class="alarm-dot"></span></button>
    <button class="icon-btn menu" aria-label="메뉴"><span class="mi material-symbols-outlined">menu</span></button>
  </div>
</header>

<section class="market-switch" id="marketSwitch">
  <button class="market-btn active" id="marketToggle" data-market="KR">한국 <span class="dot"></span></button>
</section>

<section class="analysis-tabs">
  <button class="analysis-tab active" data-tab="ai">AI예측</button>
  <button class="analysis-tab" data-tab="signal">추천신호</button>
  <button class="analysis-tab" data-tab="indicator">지표분석</button>
  <button class="analysis-tab" data-tab="theme">테마종목</button>
</section>

<section class="analysis-panel active" data-panel="ai">
  <article class="analysis-card">
    <div class="card-head"><span class="mi material-symbols-outlined">psychology</span><strong>AI예측</strong></div>
    <div class="ai-stock-box">
      <div class="ai-stock-title" id="aiStockName">종목 로딩중...</div>
      <div class="ai-price" id="aiStockPrice">-</div>
      <div class="ai-price-sub" id="aiStockChange">-</div>
    </div>
    <div class="ai-opinion">
      <span>투자의견</span>
      <strong id="aiOpinionBadge">중립</strong>
      <span class="yield" id="aiYield">-</span>
    </div>
    <button class="cta" data-focus="ai">AI예측 열기</button>
  </article>
</section>

<section class="analysis-panel" data-panel="signal">
  <article class="analysis-card buy">
    <div class="card-head"><span class="mi material-symbols-outlined">trending_up</span><strong>추천신호 TOP5</strong></div>
    <div id="signalListMini" class="signal-mini-list">
      <div class="mini-empty">추천신호 로딩중...</div>
    </div>
    <button class="cta" data-focus="signal">추천신호 전체보기</button>
  </article>
</section>

<section class="analysis-panel" data-panel="indicator">
  <article class="analysis-card">
    <div class="card-head"><span class="mi material-symbols-outlined">query_stats</span><strong>지표분석</strong></div>
    <p>RSI, MACD, 이동평균 등 핵심 보조지표를 점검합니다.</p>
    <button class="cta" data-focus="indicator">지표분석 열기</button>
  </article>
</section>

<section class="analysis-panel" data-panel="theme">
  <article class="analysis-card">
    <div class="card-head"><span class="mi material-symbols-outlined">grid_view</span><strong>테마종목</strong></div>
    <p>테마/이슈별 종목군을 빠르게 탐색합니다.</p>
    <button class="cta" data-focus="theme">테마종목 열기</button>
  </article>
</section>

<nav class="bottom-nav">
  <button class="nav-btn" data-go="watchlist"><span class="icon mi material-symbols-outlined">grade</span><span>관심종목</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">monitoring</span><span>시장정보</span></button>
  <button class="nav-btn active" data-go="analysis"><span class="icon mi material-symbols-outlined">manage_search</span><span>발굴분석</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">chat</span><span>커뮤니티</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">currency_exchange</span><span>트레이딩</span></button>
  <button class="nav-btn" data-go="chart"><span class="icon mi material-symbols-outlined">candlestick_chart</span><span>주식차트</span></button>
</nav>

<script>
window.__MOBILE = window.__MOBILE || {};
window.__MOBILE.urls = Object.assign({}, window.__MOBILE.urls || {}, {
  search: "<c:url value='/finance/searchStocksKeyword.do' />",
  mobileChart: "/scheduler/finance/mobile/chart.do",
  mobileWatchlist: "/scheduler/finance/mobile/watchlist.do",
  mobileAnalysis: "/scheduler/finance/mobile/analysis.do",
  recSignalList: "<c:url value='/stock/recSignal/list.do' />",
  legacyHome: "/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp"
});
</script>
<script>
(function(){
  function applyTheme(){
    var dark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    document.body.classList.toggle('theme-dark', !!dark);
  }
  applyTheme();
  if (window.matchMedia) {
    var mq = window.matchMedia('(prefers-color-scheme: dark)');
    if (mq.addEventListener) mq.addEventListener('change', applyTheme);
    else if (mq.addListener) mq.addListener(applyTheme);
  }
})();
</script>
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/toolbar.js?v=20260220-2' />"></script>
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/analysis.js?v=20260220-2' />"></script>
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/navigation.js?v=20260218-6' />"></script>
</body>
</html>
