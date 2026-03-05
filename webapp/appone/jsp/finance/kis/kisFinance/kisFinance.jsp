<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>KIS 차트 테스트 (vs1)</title>

<!-- Font Awesome -->
<link rel="stylesheet" href="/scheduler/appone/plugins/fontawesome/css/all.min.css">
<script src="https://kit.fontawesome.com/d3b23ad756.js" crossorigin="anonymous"></script>

<link rel="stylesheet" href="/scheduler/appone/plugins/login/css/bootstrap.min.css">
<script defer src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script defer src="/scheduler/appone/plugins/bootstrap/js/bootstrap.min.js"></script>

<script>
  // Highstock는 차트 최초 조회 시점에 동적 로딩합니다.
  window.__HIGHCHARTS_SRC = {
    highstock: "https://code.highcharts.com/stock/highstock.js",
    data: "https://code.highcharts.com/stock/modules/data.js",
    exporting: "https://code.highcharts.com/stock/modules/exporting.js"
  };
</script>

<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisFinance.css?v=20260214-1" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/common.css" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisFinance.mobileFix.css" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisDashboardChart.css?v=20260305-1" />

<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/maScript.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/doubleMonthChartScript.js?v=20260203-7"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/kisDashboardChartRenderer.js?v=20260305-1"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartScript.js?v=20260305-1"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartFeatureToggle.js?v=20260305-1"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartCrossSignals.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/stockSearch.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/recommendStocks.js?v=20260212-1"></script>

<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartPrevHighLowFlags.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/realtimeWatchlist.js?v=20260212-1"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/favoriteAction.js"></script>

<script>
  window.__CTX_PATH = "${pageContext.request.contextPath}";
  window.__WS_WATCHLIST = "<c:url value='/finance/watchlistRealtime.ws'/>";
  window.__WS_MARKET_SUMMARY = "<c:url value='/finance/marketSummaryRealtime.ws'/>";

  window.__URLS = {
    selectWatchlistGroups: "<c:url value='/finance/selectWatchlistGroups.do'/>",
    selectWatchlistItems: "<c:url value='/finance/selectWatchlistItems.do'/>",
    insertWatchlistItem: "<c:url value='/finance/insertWatchlistItem.do'/>",
    selectWatchlistItemYn: "<c:url value='/finance/selectWatchlistItemYn.do'/>",    
    selectMarketSummary: "<c:url value='/finance/selectMarketSummary.do'/>",
    selectMarketIssues: "<c:url value='/finance/selectMarketIssues.do'/>",
    portfolioGroupEvent: "<c:url value='/finance/portfolioGroupEvent.do'/>",
    kisItemchartpriceOptionData: "<c:url value='/finance/kisItemchartpriceOptionData.do' />",
    kisItemchartpriceOptionSave: "<c:url value='/finance/kisItemchartpriceOptionSave.do' />",
    selectRecommendStocks: "<c:url value='/finance/selectRecommendStocks.do'/>",
    // WF-2-4: 보유종목 API
    positionList: "<c:url value='/position/list.do'/>",
    positionDetail: "<c:url value='/position/detail.do'/>",
    positionAdd: "<c:url value='/position/add.do'/>",
    positionDelete: "<c:url value='/position/delete.do'/>",
    positionAverageDown: "<c:url value='/position/averageDown.do'/>",
    positionTxnList: "<c:url value='/position/txnList.do'/>",
    positionEventList: "<c:url value='/position/eventList.do'/>",
    selectChartEventMarkers: "<c:url value='/finance/selectChartEventMarkers.do'/>",
    selectPositionState: "<c:url value='/finance/selectPositionState.do'/>"
  };
</script>

</head>

<body>
	<div class="dash-topbar">
		<div class="brand">Finance</div>

		<!-- 1) 종목검색 영역(배치 변경): 상단 좌측에 고정, 입력 폭은 CSS에서 제어 -->
		<div class="search">
			<div class="top-search-wrap">
				<input type="text" id="topSearchInput" placeholder="종목 검색 (Alt + S)" autocomplete="off" />
				<button type="button" class="top-search-clear" id="topSearchClear" title="Clear">×</button>
				<div class="top-suggest" id="topSuggest" style="display: none;"></div>
			</div>
		</div>

		<!-- 2) Market 지수 상단 배치 (AlphaSquare 스타일의 상단 Ticker) -->
		<div class="top-market" id="topMarketBar" aria-label="시장 지수">
			<!-- 순서: 코스피, 코스닥, 다우, S&P500, 나스닥 -->
			<button type="button" class="tm-item" data-code="0001" data-label="코스피">
				<span class="tm-name">코스피</span>
				<span class="tm-val" id="topKospiVal">-</span>
			</button>
			<button type="button" class="tm-item" data-code="1001" data-label="코스닥">
				<span class="tm-name">코스닥</span>
				<span class="tm-val" id="topKosdVal">-</span>
			</button>
			<span class="tm-sep" aria-hidden="true"></span>
			<button type="button" class="tm-item" data-code=".DJI" data-label="DOW (.DJI)">
				<span class="tm-flag">🇺🇸</span>
				<span class="tm-name">다우</span>
				<span class="tm-val" id="topDjiVal">-</span>
			</button>
			<button type="button" class="tm-item" data-code=".INX" data-label="S&amp;P 500 (.INX)">
				<span class="tm-flag">🇺🇸</span>
				<span class="tm-name">S&amp;P500</span>
				<span class="tm-val" id="topSpxVal">-</span>
			</button>
			<button type="button" class="tm-item" data-code=".IXIC" data-label="NASDAQ (.IXIC)">
				<span class="tm-flag">🇺🇸</span>
				<span class="tm-name">나스닥</span>
				<span class="tm-val" id="topIxicVal">-</span>
			</button>
		</div>

		<div class="tools">
			<!-- 추천 팝업 제거: 매매신호 탭에서 직접 검색 -->
		</div>
	</div>
	<!-- Mobile: side panels toggle buttons -->
	<div class="mobile-panel-buttons" id="mobilePanelButtons" aria-hidden="true">
		<button type="button" class="mpb-btn" id="btnMobileOpenWatch">관심종목</button>
		<button type="button" class="mpb-btn" id="btnMobileOpenSummary">매매신호</button>
	</div>
	
	<div class="dash-shell">
		<div class="mobile-panel-backdrop" id="mobilePanelBackdrop" aria-hidden="true"></div>
		<!-- LEFT: Watchlist -->
		<div class="panel left-panel" id="leftPanel">
			<div class="panel-head">
				<div class="title" id="btnWgToggle" style="cursor: pointer;">관심종목</div>
				<div class="tools">
					<span class="wl-rt-status is-warn" id="wlRealtimeStatus" title="실시간 상태">실시간 대기</span>
					<button type="button" id="btnWlReload" class="wg-btn" title="새로고침">↻</button>
					<button type="button" class="btn btn-tool wg-toggle-btn"
						title="Contacts" data-widget="chat-pane-toggle"
						id="btnWgToggleBtn">
						<span class="wg-icon">💬</span>
					</button>
				</div>
			</div>

			<div class="left-body">
				<div class="left-row">
					<button type="button" class="wl-market-pill" id="wlMarketSwitchBtn" aria-label="관심종목 시장 전환">
						<span class="wl-market-pill-text" id="wlMarketPillText">한국</span>
						<span class="wl-market-pill-dot" aria-hidden="true"></span>
					</button>
					<select id="wlMarket" class="wl-market-select" aria-hidden="true" tabindex="-1">
						<option value="N">국내</option>
						<option value="A">해외</option>
					</select> <select id="wlGroup"></select>
				</div>

				<div id="wlError" class="err" style="display: none;"></div>
				<div id="watchlist" class="watchlist"></div>

				<!-- 관심그룹 관리 (슬라이드) : AdminLTE chat-pane-toggle 스타일 -->
				<div class="wg-backdrop" id="wgBackdrop"></div>
				<div class="wg-drawer" id="wgDrawer">
					<div class="wg-head">
						<div class="wg-title">관심그룹 관리</div>
						<button type="button" class="wg-btn" id="btnWgClose">닫기</button>
					</div>

					<div class="wg-list" id="wgList">
						<div class="mini-muted">불러오는 중...</div>
					</div>

					<div class="wg-form">
						<input type="hidden" id="wg_group_id" value="" /> 
						<input type="text" id="wg_group_name" placeholder="그룹명" /> 
						<input type="text" id="wg_group_desc" placeholder="설명(선택)" />
						<div class="wg-actions">
							<button type="button" class="wg-btn" id="btnWgAdd">추가</button>
							<button type="button" class="wg-btn" id="btnWgUpdate">수정</button>
							<button type="button" class="wg-btn" id="btnWgDelete">삭제</button>
							<button type="button" class="wg-btn" id="btnWgClear">초기화</button>
						</div>
						<div class="wg-status" id="wgStatus"></div>
					</div>
				</div>
			</div>
		</div>

		<!-- CENTER: Chart -->
		<div class="panel center-panel">
			<div class="center-body">
				<div class="kis-toolbar">
					<div class="kis-toolbar-left">
						<input type="hidden" id="stockCode" value="005930"
							style="width: 120px;" />
						<input type="date" id="fromDate" />
						<input type="date" id="toDate" />						 
						<select id="periodDivCode">
							<option value="D" selected>일</option>
							<option value="W">주</option>
							<option value="M">월</option>
							<option value="Y">년</option>
							<option value="T">30분</option>
							<option value="T1">1m</option>
							<option value="T5">5m</option>
							<option value="T10">10m</option>
						</select>
					</div>

					<div class="kis-toolbar-right">
						<button type="button" id="btnChartOptions"
							class="btn btn-default btn-sm"><i class="fa-solid fa-gear"></i></button>
						<button type="button" id="btnAiRecheck" class="btn btn-default btn-sm ai-recheck-btn" title="AI 검토" aria-label="AI 검토"><i class="fa-solid fa-robot"></i></button>
						<button type="button" class="kis-ghost" id="kisHdrFav" title="관심">☆</button>
						<!-- <button type="button" class="kis-ghost" id="kisHdrMemo" title="메모"
							style="display: none;">
							📝 <span>메모</span>
						</button> -->
					</div>
				</div>
				<div class="kis-info-line">
					<div class="kis-info-left">		
						<img id="kisHdrLogo" class="kis-logo" alt="logo" style="display:none;" />
						<span class="kis-flag" id="kisHdrFlag" aria-hidden="true">🇰🇷</span> 
						<span class="kis-name" id="kisHdrName">-</span> 
						<span class="kis-code" id="kisHdrCode">-</span> 
						<span class="kis-sub" id="kisHdrMarket">-</span>
						<span class="kis-dot">·</span> 
						<span class="kis-now" id="kisHdrNow">-</span> 
						<span class="kis-pct" id="kisHdrPct">-</span>
						<span class="kis-diff" id="kisHdrDiff">-</span> 
					</div>
				</div>
				<div class="chart-wrap">
					<div id="kisChartHeader" class="kis-h">
						<div class="kis-info-line">
							<div class="kis-info-left">								
								<span class="kis-dt" id="kisHdrDt">-</span>
								<!-- <span class="kis-dot">·</span>  -->
								<span class="kis-ohlc"> 
									<span class="kis-k">시</span> 
									<span class="kis-v" id="kisHdrO">-</span>
									<span class="kis-k">고</span> 
									<span class="kis-v" id="kisHdrH">-</span>
									<span class="kis-k">저</span> 
									<span class="kis-v" id="kisHdrL">-</span>
									<span class="kis-k">종</span> 
									<span class="kis-v" id="kisHdrC">-</span>
								</span> 
								<!-- <span class="kis-dot">·</span> -->
								<!-- <span class="kis-meta" id="kisHdrVol">거래량 -</span> -->
							</div>

							<div class="kis-info-right" id="kisMaLegend">
								<span class="kis-ma-label">단순이동평균</span>
								<!-- MA 뱃지는 DB 옵션 기반으로 JS에서 렌더링 -->
							</div>
						</div>
						<div class="kis-sr-list" id="kisMaSrList"></div>
						<div class="kis-explain-panel" id="kisExplainPanel">설명 준비중...</div>
                        </div>

						<!-- 기존 ChartScript 호환용(숨김) -->
						<div class="kis-compat" aria-hidden="true">
							<div id="kisStockName"></div>
							<div id="kisStockPrice"></div>
							<div id="kisStockDate"></div>
						</div>
					</div>
					<div id="kisChartContainer"></div>
					<div class="ai-slide-backdrop" id="aiSlideBackdrop"></div>
					<aside class="ai-slide-panel" id="aiSlidePanel" aria-hidden="true">
						<div class="ai-slide-head">
							<strong>AI 검토</strong>
							<button type="button" class="ai-slide-close" id="aiSlideClose" aria-label="닫기">×</button>
						</div>
						<div class="ai-slide-body" id="aiSlideBody">
							<div class="ai-card" id="aiSlideSummary">분석 준비중...</div>
							<div class="ai-card" id="aiSlideIndicators">지표 분석 준비중...</div>
							<div class="ai-card" id="aiSlideStockAnalysis">종목 분석 준비중...</div>
						</div>
					</aside>
				</div>
			</div>
			
		<!-- RIGHT: Trading Signal -->
		<div class="panel right-panel is-collapsed" id="rightPanel">
			<div class="panel-head">
				<div class="title">매매신호</div>
				<div class="tools">
					<button type="button" id="btnSignalReload" class="wg-btn"
						title="새로고침">↻</button>
				</div>
			</div>

			<div class="right-body">
				<div class="right-tabs" id="rightTabs">
					<button type="button" class="rt-tab is-active" data-tab="signal">매매신호</button>
                    <button type="button" class="rt-tab" data-tab="holding">보유종목</button>
					<button type="button" class="rt-tab" data-tab="theme">테마종목</button>
					<!-- 3) 시장요약 → 시장이슈 로 명칭 변경 -->
					<button type="button" class="rt-tab" data-tab="summary">시장이슈</button>
				</div>

				<div class="rt-pane is-active" data-pane="signal">
					<div class="signal-controls">
						<div class="signal-market">
							<button type="button" class="sig-chip is-active" data-market="N">국내주식</button>
							<button type="button" class="sig-chip" data-market="A">미국주식</button>
							<button type="button" class="sig-chip is-disabled" data-market="C" title="준비중">가상화폐</button>
						</div>
						<div class="signal-filter">
							<button type="button" class="sig-pill is-active" data-filter="all">전체</button>
							<button type="button" class="sig-pill" data-filter="buy">매수</button>
							<button type="button" class="sig-pill" data-filter="sell">매도</button>
						</div>
						<div class="signal-search">
							<input type="text" id="signalKeyword" class="holding-input" placeholder="매매신호 검색 (종목명/코드/이벤트)" />
						</div>
						<div class="signal-type-filter">
							<span class="signal-type-label">상태필터:</span>
							<button type="button" class="sig-type-chip is-active" data-type="all">전체</button>
							<button type="button" class="sig-type-chip is-active" data-type="buy">매수</button>
							<button type="button" class="sig-type-chip is-active" data-type="sell">매도</button>
							<button type="button" class="sig-type-chip is-active" data-type="entry_ready">진입가능</button>
							<button type="button" class="sig-type-chip is-active" data-type="caution">주의</button>
							<button type="button" class="sig-type-chip is-active" data-type="long_uptrend">장기상승</button>
						</div>
						<div class="signal-meta">
							<span id="signalAsOf" class="mini-muted"></span>
							<span id="signalCount" class="signal-count"></span>
						</div>
					</div>
					<div id="signalError" class="err" style="display: none;"></div>
					<div id="signalList" class="signal-list"></div>
				</div>
                <div class="rt-pane" data-pane="holding">
                    <div class="holding-controls">
                        <div class="holding-meta">
                            <span id="holdingAsOf" class="mini-muted"></span>
                            <span id="holdingCount" class="holding-count"></span>
                        </div>
                        <div class="holding-actions">
                            <button type="button" id="btnHoldingAdd" class="wg-btn" title="보유 추가">추가</button>
                            <button type="button" id="btnHoldingAvgDown" class="wg-btn" title="물타기">물타기</button>
                            <button type="button" id="btnHoldingDelete" class="wg-btn" title="보유 삭제">삭제</button>
                            <button type="button" id="btnHoldingEvents" class="wg-btn" title="최근 이력">이력</button>
                            <button type="button" id="btnHoldingReload" class="wg-btn" title="새로고침">&#8635;</button>
                        </div>
                    </div>
                    <div class="holding-form" id="holdingActionForm">
                        <input type="number" id="holdingQty" class="holding-input" min="1" step="1" placeholder="수량" value="1" />
                        <input type="number" id="holdingPrice" class="holding-input" min="0" step="0.01" placeholder="단가" />
                        <input type="text" id="holdingDesc" class="holding-input holding-input-desc" placeholder="메모(선택)" />
                    </div>
                    <div id="holdingAvgPreview" class="holding-avg-preview" style="display:none;"></div>
                    <div id="holdingError" class="err" style="display: none;"></div>
                    <div id="holdingEventHint" class="holding-event-hint" style="display:none;"></div>
                    <div id="holdingEventListWrap" class="holding-event-list-wrap" style="display:none;">
                        <div class="holding-event-filters">
                            <button type="button" class="holding-ev-filter is-active" data-evf="all">전체</button>
                            <button type="button" class="holding-ev-filter" data-evf="risk">위험</button>
                            <button type="button" class="holding-ev-filter" data-evf="trend">추세</button>
                        </div>
                        <div id="holdingEventList" class="holding-event-list"></div>
                    </div>
                    <div id="holdingList" class="holding-list"></div>
                </div>


				<div class="rt-pane" data-pane="theme">
					<div class="mini-muted">테마 종목은 준비 중입니다.</div>
				</div>

				<div class="rt-pane" data-pane="summary">
					<div id="summaryError" class="err" style="display: none;"></div>

					<!-- 3) 시장요약 → 시장이슈 (지수는 상단 Ticker로 이동했으므로, 이 탭은 '이슈'만 표시) -->
					<div class="summary-card market-issues-card" id="marketIssuesCard" style="display:none;">
						<div class="h">📋 시장이슈 <span class="market-issues-date" id="issueDate"></span></div>
						<div class="market-issues-section" id="usMarketIssue">
							<div class="issue-title" id="usIssueTitle"></div>
							<div class="issue-indices" id="usIssueIndices"></div>
							<ul class="issue-list" id="usIssueList"></ul>
							<div class="issue-outlook" id="usIssueOutlook"></div>
						</div>
						<div class="market-issues-divider"></div>
						<div class="market-issues-section" id="krMarketIssue">
							<div class="issue-title" id="krIssueTitle"></div>
							<div class="issue-indices" id="krIssueIndices"></div>
							<ul class="issue-list" id="krIssueList"></ul>
							<div class="issue-outlook" id="krIssueOutlook"></div>
						</div>
						<div class="issue-updated" id="issueUpdated"></div>
					</div>
				</div>
			</div>
		</div>
		</div>


		<div class="chart-opt-backdrop" id="chartOptBackdrop"></div>
		<div id="chartOptionsModal" class="chart-opt-panel" aria-hidden="true">
			<div class="opt-panel-hdr">
				<span class="opt-panel-title">⚙ 차트 옵션</span>
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
					<input type="checkbox" id="optCrossSignals" /> 골든/데드
				</label>
				<span class="cross-pair-wrap" style="display:inline-flex; align-items:center; gap:6px;">
					<select id="optCrossShort" class="form-control input-sm" style="width:74px; display:inline-block;">
						<option value="5">5</option>
						<option value="10">10</option>
						<option value="20">20</option>
						<option value="60">60</option>
						<option value="120">120</option>
						<option value="240">240</option>
					</select>
					<span style="opacity:0.7;">/</span>
					<select id="optCrossLong" class="form-control input-sm" style="width:74px; display:inline-block;">
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
				<button type="button" class="btn btn-default btn-sm" id="btnMaDefault">기본값</button>
				<button type="button" class="btn btn-primary btn-sm" id="btnApplyMaOptions">적용</button>
			</div>
		</div>

	<script src="/scheduler/appone/jsp/finance/kis/kisFinance/js/kisFinancePage.js?v=20260305-1" defer></script>
</body>
</html>
