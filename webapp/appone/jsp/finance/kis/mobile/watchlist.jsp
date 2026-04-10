<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>KIS Mobile Watchlist</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />
<link rel="stylesheet" href="<c:url value='/appone/jsp/finance/kis/mobile/css/mobile.css?v=20260220-6' />">
</head>
<body>
<div id="mobileProgress" class="mobile-progress"></div>
<header class="toolbar">
  <div class="stock-info">
    <div class="name-code">
      <h1 class="stock-name">관심종목</h1>
      <span class="stock-code" id="wlStatus">연결대기</span>
    </div>
  </div>
  <div class="actions">
    <button class="icon-btn search" aria-label="검색"><span class="mi material-symbols-outlined">search</span></button>
    <button class="icon-btn refresh" aria-label="새로고침"><span class="mi material-symbols-outlined">refresh</span></button>
    <button class="icon-btn menu" aria-label="메뉴"><span class="mi material-symbols-outlined">menu</span></button>
  </div>
</header>

<section class="market-switch" id="marketSwitch">
  <button class="market-btn active" id="marketToggle" data-market="KR">한국 <span class="dot"></span></button>
</section>

<section class="wl-controls">
  <select id="wlGroup" aria-label="관심그룹"></select>
</section>

<section class="wl-actionbar">
  <button class="action-btn" id="wlAdd"><span class="mi material-symbols-outlined">add</span><span>종목추가</span></button>
  <button class="action-btn" id="wlSort"><span class="mi material-symbols-outlined">swap_vert</span><span>정렬</span></button>
  <button class="action-btn" id="wlEdit"><span class="mi material-symbols-outlined">edit</span><span>편집</span></button>
  <button class="action-btn" id="wlSetting"><span class="mi material-symbols-outlined">settings</span><span>설정</span></button>
</section>

<section class="wl-list" id="wlList">
  <div class="mini-muted" style="padding:12px 16px; color:#888;">로딩중...</div>
</section>

<nav class="bottom-nav">
  <button class="nav-btn active" data-go="watchlist"><span class="icon mi material-symbols-outlined">grade</span><span>관심종목</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">monitoring</span><span>시장정보</span></button>
  <button class="nav-btn" data-go="analysis"><span class="icon mi material-symbols-outlined">manage_search</span><span>발굴분석</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">chat</span><span>커뮤니티</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">currency_exchange</span><span>트레이딩</span></button>
  <button class="nav-btn" data-go="chart"><span class="icon mi material-symbols-outlined">candlestick_chart</span><span>주식차트</span></button>
</nav>

<script>
window.__MOBILE = window.__MOBILE || {};
window.__MOBILE.urls = Object.assign({}, window.__MOBILE.urls || {}, {
  watchlistGroups: "<c:url value='/finance/selectWatchlistGroups.do'/>",
  watchlistItems: "<c:url value='/finance/selectWatchlistItems.do'/>",
  watchlistToggle: "<c:url value='/finance/toggleWatchlistItem.do'/>",
  wsWatchlist: "/finance/watchlistRealtime.ws",
  search: "<c:url value='/finance/searchStocksKeyword.do' />",
  mobileChart: "/scheduler/finance/mobile/chart.do",
  mobileWatchlist: "/scheduler/finance/mobile/watchlist.do",
  mobileAnalysis: "/scheduler/finance/mobile/analysis.do",
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
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/watchlist.js?v=20260220-2' />"></script>
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/navigation.js?v=20260218-6' />"></script>
</body>
</html>
