<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>KIS Mobile Chart</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />
<link rel="stylesheet" href="<c:url value='/appone/jsp/finance/kis/mobile/css/mobile.css?v=20260404-1' />">
</head>
<body class="chart-page">
<div id="mobileProgress" class="mobile-progress"></div>
<!-- 상단 툴바 -->
<header class="toolbar">
  <div class="stock-info">
    <div class="logo logo-placeholder" id="stockLogo">-</div>
    <div class="name-code">
      <h1 class="stock-name">-</h1>
      <span class="stock-code">-</span>
    </div>
  </div>
  <div class="actions">
    <button class="icon-btn search" aria-label="검색"><span class="mi material-symbols-outlined">search</span></button>
    <button class="icon-btn alarm" aria-label="알림"><span class="mi material-symbols-outlined">notifications</span><span class="alarm-dot"></span></button>
    <button class="icon-btn menu" aria-label="메뉴"><span class="mi material-symbols-outlined">menu</span></button>
  </div>
</header>

<section class="market-switch" id="marketSwitch">  
  <span id="marketPriceInline" class="market-price-inline">-</span>
  <span id="marketChangeInline" class="market-change-inline">-% · -</span>
</section>

<!-- 현재가 영역(하위 호환용, 화면 표시 없음) -->
<section class="price-area" style="display:none;">
  <div class="price-main">
    <div class="change">
      <span class="percent">-%</span>
      <span class="diff">-</span>
    </div>
  </div>
</section>

<!-- 기능 버튼 -->
<section class="features">
  <button class="feature-btn" data-action="indicator" title="지표분석" aria-label="지표분석">
    <span class="mi material-symbols-outlined">query_stats</span>
    <span class="label">지표분석</span>
  </button>
  <button class="feature-btn" data-action="ai" title="AI예측" aria-label="AI예측">
    <span class="mi material-symbols-outlined">memory</span>
    <span class="label">AI예측</span>
  </button>
</section>

<!-- 이평선 표시 -->
<section class="ma-info">
  <span class="ma-label">단순이동평균</span>
  <span class="ma ma5">5</span>
  <span class="ma ma20">20</span>
  <span class="ma ma60">60</span>
  <span class="ma ma120">120</span>
  <span class="ma ma240">240</span>
</section>

<!-- 차트 영역 -->
<section class="chart-container">
  <div id="priceChart" class="chart"></div>
  <div id="volumeChart" class="chart volume"></div>
</section>

<!-- 타임프레임 + 차트 옵션 -->
<section class="timeframe">
  <button class="tf-btn" data-tf="m" aria-label="분봉">분</button>
  <button class="tf-btn active" data-tf="d" aria-label="일봉">일</button>
  <button class="tf-btn" data-tf="w" aria-label="주봉">주</button>
  <button class="tf-btn" data-tf="M" aria-label="월봉">월</button>
  <button class="tf-btn" data-tf="y" aria-label="년봉">년</button>
  <button class="tf-btn settings" aria-label="차트 옵션"><span class="mi material-symbols-outlined">settings</span></button>
</section>

<!-- 하단 네비게이션 -->
<nav class="bottom-nav">
  <button class="nav-btn" data-go="watchlist"><span class="icon mi material-symbols-outlined">grade</span><span>관심종목</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">monitoring</span><span>시장정보</span></button>
  <button class="nav-btn" data-go="analysis"><span class="icon mi material-symbols-outlined">manage_search</span><span>발굴분석</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">chat</span><span>커뮤니티</span></button>
  <button class="nav-btn" data-go="legacy"><span class="icon mi material-symbols-outlined">currency_exchange</span><span>트레이딩</span></button>
  <button class="nav-btn active" data-go="chart"><span class="icon mi material-symbols-outlined">candlestick_chart</span><span>주식차트</span></button>
</nav>

<!-- 차트 옵션 패널 (숨김) -->
<aside id="chartOptions" class="options-panel hidden">
  <header>
    <h3>이동평균선(MA) 옵션 설정</h3>
    <button class="close">✕</button>
  </header>

  <!-- 1. Feature Toggles -->
  <div class="opt-block">
    <div class="opt-row">
      <span>거래량</span>
      <label class="opt-switch"><input type="checkbox" id="optVolume"><span class="opt-slider"></span></label>
    </div>
    <div class="opt-row">
      <span>월봉 오버레이(더블차트)</span>
      <label class="opt-switch"><input type="checkbox" id="optDoubleChart"><span class="opt-slider"></span></label>
    </div>
    <div class="opt-row">
      <span>월/년 구분선</span>
      <label class="opt-switch"><input type="checkbox" id="optMonthLines"><span class="opt-slider"></span></label>
    </div>
    <div class="opt-row">
      <span>전고/전저</span>
      <label class="opt-switch"><input type="checkbox" id="optHighLow"><span class="opt-slider"></span></label>
    </div>
    <div class="opt-row">
      <span>MA 지지/저항</span>
      <label class="opt-switch"><input type="checkbox" id="optMaSr"><span class="opt-slider"></span></label>
    </div>
    <p class="opt-hint">체크 상태는 DB에 즉시 저장되어 유지됩니다.</p>
  </div>

  <!-- 2. 골든/데드 크로스 -->
  <div class="opt-block">
    <div class="opt-row">
      <label class="opt-cross-label">
        <input type="checkbox" id="optCrossSignals"> 골든/데드
      </label>
      <div class="opt-cross-pair">
        <select id="optCrossShort" class="opt-select">
          <option value="5">5</option><option value="10">10</option><option value="20">20</option>
          <option value="60">60</option><option value="120">120</option><option value="240">240</option>
        </select>
        <span>/</span>
        <select id="optCrossLong" class="opt-select">
          <option value="5">5</option><option value="10">10</option><option value="20">20</option>
          <option value="60">60</option><option value="120">120</option><option value="240">240</option>
        </select>
        <span id="crossPairLabel" class="opt-cross-hint">(60/240)</span>
      </div>
    </div>
  </div>

  <!-- 3. MA 라인 목록 (JS 동적 삽입) -->
  <div id="maRowList"></div>
  <button type="button" id="btnAddMaLine" class="btn-add-ma">MA 라인 추가</button>

  <!-- 4. 기간 설정 -->
  <div class="range-content">
    <label>fromDate <input type="date" id="fromDate"></label>
    <label>endDate <input type="date" id="endDate"></label>
    <button type="button" id="applyDateRange" class="range-apply-btn">기간 적용</button>
  </div>

  <!-- 5. 하단 버튼 -->
  <div class="opt-footer">
    <button type="button" id="btnMaDefault" class="opt-btn">기본값</button>
    <button type="button" id="btnCancelOptions" class="opt-btn">취소</button>
    <button type="button" id="btnApplyMaOptions" class="opt-btn opt-btn-primary">적용</button>
  </div>
</aside>

<script>
window.__MOBILE = {
  code: "${param.code}" || "005930",
  urls: {
    chartData: "<c:url value='/finance/kisItemchartpriceData.do' />",
    currentPrice: "<c:url value='/finance/getCurrentPriceByInquirePrice.do' />",
    search: "<c:url value='/finance/searchStocksKeyword.do' />",
    chartOptionData: "<c:url value='/finance/kisItemchartpriceOptionData.do' />",
    chartOptionSave: "<c:url value='/finance/kisItemchartpriceOptionSave.do' />",
    mobileChart: "/scheduler/finance/mobile/chart.do",
    mobileWatchlist: "/scheduler/finance/mobile/watchlist.do",
    mobileAnalysis: "/scheduler/finance/mobile/analysis.do",
    legacyHome: "/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp"
  }
};
</script>
<script>
(function(){
  function applyTheme(){
    var dark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    document.body.classList.toggle('theme-dark', !!dark);
    window.dispatchEvent(new Event('mobileThemeChanged'));
  }
  applyTheme();
  if (window.matchMedia) {
    var mq = window.matchMedia('(prefers-color-scheme: dark)');
    if (mq.addEventListener) mq.addEventListener('change', applyTheme);
    else if (mq.addListener) mq.addListener(applyTheme);
  }
})();
</script>
<script src="<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/highstock.js' />"></script>
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/toolbar.js?v=20260220-2' />"></script>
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/chart.js?v=20260404-1' />"></script>
<script src="<c:url value='/appone/jsp/finance/kis/mobile/js/navigation.js?v=20260218-6' />"></script>
</body>
</html>
