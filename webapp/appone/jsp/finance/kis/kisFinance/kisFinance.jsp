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
    highstock: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/highstock.js' />",
    data: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/modules/data.js' />",
    exporting: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/modules/exporting.js' />"
  };
</script>

<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisFinance.css?v=20260320-2" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/common.css" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisFinance.mobileFix.css?v=20260311-2" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisDashboardChart.css?v=20260305-1" />
<style>
  .kis-toolbar-left .double-chart-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 0 10px;
    min-width: auto;
  }
  .kis-toolbar-left .double-chart-btn.is-active {
    border-color: #1d4ed8;
    background: #eff6ff;
    color: #1d4ed8;
  }
  .kis-toolbar-left .double-chart-btn-label {
    white-space: nowrap;
    font-weight: 700;
  }
  .kis-toolbar-left .double-chart-btn-mode {
    font-size: 9px;
    font-weight: 800;
    color: #64748b;
  }
  .kis-toolbar-left .double-chart-dots {
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }
  .kis-toolbar-left .double-chart-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: #cbd5e1;
    opacity: .45;
  }
  .kis-toolbar-left .double-chart-dot.is-active {
    opacity: 1;
  }
  .kis-toolbar-left .double-chart-btn[data-double-mode="recent"] .double-chart-btn-mode {
    color: #f59e0b;
  }
  .kis-toolbar-left .double-chart-btn[data-double-mode="recent"] .double-chart-dot.is-active {
    background: #f59e0b;
  }
  .kis-toolbar-left .double-chart-btn[data-double-mode="all"] .double-chart-btn-mode {
    color: #38bdf8;
  }
  .kis-toolbar-left .double-chart-btn[data-double-mode="all"] .double-chart-dot.is-active {
    background: #38bdf8;
  }
  .kis-toolbar-left .double-chart-btn[data-double-mode="off"] .double-chart-btn-mode {
    color: #64748b;
  }
  .kis-toolbar-left .double-chart-btn[data-double-mode="off"] .double-chart-dot.is-active {
    background: #64748b;
  }
</style>

<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/maScript.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/doubleMonthChartScript.js?v=20260203-7"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/kisDashboardChartRenderer.js?v=20260305-1"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartScript.js?v=20260312-4"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartFeatureToggle.js?v=20260305-1"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartCrossSignals.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/stockSearch.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/recommendStocks.js?v=20260212-1"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/recSignalPanel.js?v=20260320-1"></script>

<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartPrevHighLowFlags.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/realtimeWatchlist.js?v=20260331-1"></script>
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
    portfolioGroupEvent: "<c:url value='/finance/portfolioGroupEvent.do'/>",
    kisItemchartpriceOptionData: "<c:url value='/finance/kisItemchartpriceOptionData.do' />",
    kisItemchartpriceOptionSave: "<c:url value='/finance/kisItemchartpriceOptionSave.do' />",
    recSignalList: "<c:url value='/stock/recSignal/list.do'/>",
    recSignalDetail: "<c:url value='/stock/recSignal/detail.do'/>",
    recSignalListView: "<c:url value='/stock/recSignal/listView.do'/>",
    recSignalDetailView: "<c:url value='/stock/recSignal/detailView.do'/>",
    recPickSaveToWatchlist: "<c:url value='/stock/recPick/saveToWatchlist.do'/>",
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
				<span class="tm-val" id="topDjiVal"></span>
			</button>
			<button type="button" class="tm-item" data-code=".INX" data-label="S&amp;P 500 (.INX)">
				<span class="tm-flag">🇺🇸</span>
				<span class="tm-name">S&amp;P500</span>
				<span class="tm-val" id="topSpxVal"></span>
			</button>
			<button type="button" class="tm-item" data-code=".IXIC" data-label="NASDAQ (.IXIC)">
				<span class="tm-flag">🇺🇸</span>
				<span class="tm-name">나스닥</span>
				<span class="tm-val" id="topIxicVal"></span>
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
					</select>
					<select id="wlGroup"></select>
					<select id="wlGroupDiv" aria-label="관심그룹 분류">
						<option value="normal">노멀</option>
						<option value="month">월말</option>
						<option value="recommend">추천</option>
					</select>
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
					<input type="hidden" id="stockCode" value="005930"
						style="width: 120px;" />
					<div class="kis-toolbar-left">
						<div class="kis-toolbar-row kis-toolbar-row-dates">
							<input type="date" id="fromDate" />
							<input type="date" id="toDate" />
						</div>
						<div class="kis-toolbar-row kis-toolbar-row-controls">
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
							<button type="button" class="zoom-step-btn double-chart-btn" id="kisDoubleChartBtn" data-zoom-action="double-chart" data-double-mode="off" title="더블차트 OFF">
								<span class="double-chart-btn-label">더블차트</span>
								<span class="double-chart-btn-mode">OFF</span>
								<span class="double-chart-dots" aria-hidden="true">
									<span class="double-chart-dot"></span>
									<span class="double-chart-dot"></span>
									<span class="double-chart-dot is-active"></span>
								</span>
							</button>							
							<button type="button" id="btnChartOptions"
								class="btn btn-default btn-sm"><i class="fa-solid fa-gear"></i></button>
							<button type="button" class="kis-ghost" id="kisHdrFav" title="관심">☆</button>						
						</div>
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
                        </div>

						<!-- 기존 ChartScript 호환용(숨김) -->
						<div class="kis-compat" aria-hidden="true">
							<div id="kisStockName"></div>
							<div id="kisStockPrice"></div>
							<div id="kisStockDate"></div>
						</div>
					</div>
					<div id="kisChartContainer"></div>
				</div>
			</div>
			
		<!-- RIGHT: Trading Signal -->
		<div class="panel right-panel is-collapsed" id="rightPanel">
			<div class="panel-head">
				<div class="title">추천 종목</div>
				<div class="tools">
					<button type="button" id="btnOpenRecSignal" class="wg-btn"
						title="추천신호 v1.10 화면">추천신호</button>
					<button type="button" id="btnSignalReload" class="wg-btn"
						title="새로고침">↻</button>
				</div>
			</div>

			<div class="right-body">
				<div class="right-tabs" id="rightTabs">
					<button type="button" class="rt-tab is-active" data-tab="signal">추천 종목</button>
                    <button type="button" class="rt-tab" data-tab="holding">보유종목</button>
						<button type="button" class="rt-tab" data-tab="theme">테마종목</button>
				</div>

				<div class="rt-pane is-active" data-pane="signal">
					<section class="signal-rec-panel">
						<div class="signal-rec-head">
							<div>
								<div class="signal-rec-title">추천 종목 비교</div>
								<span id="signalRecAsOf" class="signal-rec-base-date">조회 기준일 -</span>
							</div>
						</div>
						<div class="signal-rec-toolbar">
							<div class="signal-rec-toolbar-left">
								<div class="signal-rec-toolbar-group">
									<span class="signal-rec-toolbar-label">국가</span>
									<button type="button" class="wl-market-pill signal-country-pill" id="signalCountrySwitchBtn" aria-label="시장 전환">
										<span class="wl-market-pill-text" id="signalCountryPillText">한국</span>
										<span class="wl-market-pill-dot" aria-hidden="true"></span>
									</button>
								</div>
								<div class="signal-rec-toolbar-group">
									<span class="signal-rec-toolbar-label">시장</span>
									<div id="signalMarketFilter" class="signal-rec-chip-row"></div>
								</div>
								
								<div class="signal-rec-toolbar-group">
									<span class="signal-rec-toolbar-label">등급 필터</span>
									<div id="signalGradeFilter" class="signal-rec-chip-row">
										<button type="button" class="signal-rec-chip is-active" data-grade="ALL">전체</button>
										<button type="button" class="signal-rec-chip" data-grade="A">A</button>
										<button type="button" class="signal-rec-chip" data-grade="B">B</button>
										<button type="button" class="signal-rec-chip" data-grade="C">C</button>
									</div>
								</div>
							</div>
							<div class="signal-rec-toolbar-right">
								<div class="signal-rec-sort">
									<span class="signal-rec-toolbar-label">랭킹 정렬</span>
									<select id="signalSortFilter" class="signal-rec-select">
										<option value="rank">기본 순위</option>
										<option value="monthChange">월간 상승률</option>
										<option value="trend">추세 강도</option>
										<option value="tradeValue">평균 거래대금</option>
									</select>
								</div>
							</div>
						</div>
						<div class="signal-rec-meta">
							<span id="signalRecCount" class="signal-count"></span>
							<div class="signal-search signal-rec-search">
								<input type="text" id="signalKeyword" class="holding-input" placeholder="추천신호 검색 (종목명/코드/사유)" />
							</div>
						</div>
						<div id="signalRecError" class="err" style="display: none;"></div>
						<div class="signal-rec-table-wrap">
							<table class="signal-rec-table">
								<thead>
									<tr>
										<th>순위</th>
										<th>종목</th>
										<th>등급</th>
										<th>현재가</th>
										<th>관리</th>
									</tr>
								</thead>
								<tbody id="signalRecComparisonBody">
									<tr>
										<td colspan="5" class="signal-rec-empty">추천신호 비교 데이터를 불러오는 중입니다.</td>
									</tr>
								</tbody>
							</table>
						</div>
					</section>
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
			</div>
		</div>
		</div>

		<div id="signalRecDetailOverlay" class="signal-rec-detail-overlay" aria-hidden="true">
			<div class="signal-rec-detail-backdrop" data-detail-close="true"></div>
			<section class="signal-rec-detail-dialog" role="dialog" aria-modal="true" aria-labelledby="signalRecDetailTitle">
				<header class="signal-rec-detail-head">
					<div class="signal-rec-detail-heading">
						<span class="signal-rec-detail-kicker">추천 종목 상세</span>
						<h3 id="signalRecDetailTitle" class="signal-rec-detail-title">상세 데이터를 불러오는 중입니다.</h3>
						<p id="signalRecDetailSubtitle" class="signal-rec-detail-subtitle">요청한 종목 상세 데이터를 준비하고 있습니다.</p>
					</div>
					<div class="signal-rec-detail-head-actions">
						<button type="button" id="signalRecDetailApplyBtn" class="signal-rec-detail-util">차트 반영</button>
						<a id="signalRecDetailPageLink" class="signal-rec-detail-util is-link" href="#" target="_blank" rel="noopener">상세 페이지</a>
						<button type="button" id="signalRecDetailCloseBtn" class="signal-rec-detail-close" aria-label="추천 종목 상세 팝업 닫기">닫기</button>
					</div>
				</header>
				<div class="signal-rec-detail-scroll">
					<div class="signal-rec-detail-summary">
						<article class="signal-rec-detail-card signal-rec-detail-card-primary">
							<div class="signal-rec-detail-chip-row">
								<span id="signalRecDetailGrade" class="signal-rec-detail-grade">-</span>
								<span id="signalRecDetailRecommend" class="signal-rec-detail-status">조회 중</span>
							</div>
							<span class="signal-rec-detail-label">현재가</span>
							<strong id="signalRecDetailCurrentPrice" class="signal-rec-detail-price">조회 중</strong>
							<div id="signalRecDetailMonthChange" class="signal-rec-detail-month-change">운영 데이터를 불러오는 중입니다.</div>
							<p id="signalRecDetailReason" class="signal-rec-detail-reason">잠시만 기다려 주세요.</p>
						</article>
						<article class="signal-rec-detail-card">
							<div class="signal-rec-detail-stat-grid">
								<div class="signal-rec-detail-stat">
									<span>정배열</span>
									<strong id="signalRecDetailGoldenYn">-</strong>
								</div>
								<div class="signal-rec-detail-stat">
									<span>해당월 상승</span>
									<strong id="signalRecDetailMonthUpYn">-</strong>
								</div>
								<div class="signal-rec-detail-stat">
									<span>추세 강도</span>
									<strong id="signalRecDetailTrendStrength">-</strong>
								</div>
								<div class="signal-rec-detail-stat">
									<span>평균 거래대금(20)</span>
									<strong id="signalRecDetailTradeValue">-</strong>
								</div>
							</div>
						</article>
					</div>
					<div id="signalRecDetailMetricGrid" class="signal-rec-detail-metric-grid"></div>
					<div class="signal-rec-detail-content-grid">
						<section class="signal-rec-detail-card">
							<div class="signal-rec-detail-card-head">
								<span class="signal-rec-detail-label">판정 근거</span>
								<strong>추천 결정 사다리</strong>
							</div>
							<div id="signalRecDetailDecisionList" class="signal-rec-detail-decision-list"></div>
						</section>
						<section class="signal-rec-detail-card">
							<div class="signal-rec-detail-card-head">
								<span class="signal-rec-detail-label">이동평균</span>
								<strong>MA 상태 비교</strong>
							</div>
							<div id="signalRecDetailMaLadder" class="signal-rec-detail-ma-ladder"></div>
						</section>
						<section class="signal-rec-detail-card signal-rec-detail-card-wide">
							<div class="signal-rec-detail-card-head">
								<span class="signal-rec-detail-label">분석 메모</span>
								<strong>추천 해석</strong>
							</div>
							<ul id="signalRecDetailAnalysisList" class="signal-rec-detail-analysis-list">
								<li>상세 데이터를 불러오면 실제 저장 값 기준 해석이 표시됩니다.</li>
							</ul>
						</section>
					</div>
				</div>
			</section>
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

		<div id="kisDoubleChartHiddenControls" style="display:none;" aria-hidden="true">
			<input type="checkbox" id="optDoubleChart" />
			<input type="radio" name="optDoubleChartMode" value="off" checked />
			<input type="radio" name="optDoubleChartMode" value="recent" />
			<input type="radio" name="optDoubleChartMode" value="all" />
		</div>

	<script>
	(function () {
		var doubleChartStateResolved = false;

		function normalizeDoubleChartMode(mode) {
			mode = String(mode || "").toLowerCase();
			if (mode !== "recent" && mode !== "all" && mode !== "off") {
				return "";
			}
			return mode;
		}

		function resolveModeFromState(state) {
			var mode;

			if (!state) {
				return "";
			}

			mode = normalizeDoubleChartMode(state.doubleChartMode || "");
			if (mode) {
				return mode;
			}

			if (typeof state.doubleChartEnabled === "boolean") {
				return state.doubleChartEnabled ? "all" : "off";
			}

			return "";
		}

		function extractDoubleChartModeFromOptionList(list) {
			var mode = "";
			var i;
			var item;
			var key;
			var enabled;
			var period;

			if (!Array.isArray(list)) {
				return "";
			}

			for (i = 0; i < list.length; i += 1) {
				item = list[i] || {};
				key = String(item.seriesKey || "");

				if (key === "doubleChartEnabled") {
					enabled = String(item.enabledYn || "").toUpperCase() === "Y";
					mode = enabled ? "all" : "off";
				}

				if (key === "doubleChartMode") {
					period = parseInt(item.seriesPeriod || 0, 10);
					mode = (period === 1 ? "recent" : (period === 2 ? "all" : "off"));
				}
			}

			return normalizeDoubleChartMode(mode);
		}

		function nextDoubleChartMode(mode) {
			mode = normalizeDoubleChartMode(mode) || "off";
			if (mode === "recent") return "all";
			if (mode === "all") return "off";
			return "recent";
		}

		function doubleChartModeLabel(mode) {
			if (mode === "recent") return "최근월봉";
			if (mode === "all") return "전체월봉";
			return "OFF";
		}

		function readChartScriptMode() {
			var options = window.ChartScript && window.ChartScript.options;
			var mode;

			if (!options) {
				return "";
			}

			mode = normalizeDoubleChartMode(options.doubleChartMode || "");
			if (mode) {
				return mode;
			}

			if (typeof options.doubleChartEnabled === "boolean") {
				return options.doubleChartEnabled ? "all" : "off";
			}

			return "";
		}

		function readHiddenDoubleChartMode() {
			var checked = document.querySelector("input[name='optDoubleChartMode']:checked");
			if (checked && checked.value) {
				return normalizeDoubleChartMode(checked.value) || "";
			}

			if (document.getElementById("optDoubleChart")) {
				return document.getElementById("optDoubleChart").checked ? "all" : "off";
			}

			return "";
		}

		function writeHiddenDoubleChartMode(mode) {
			var checkbox = document.getElementById("optDoubleChart");
			var radio;

			mode = normalizeDoubleChartMode(mode) || "off";
			radio = document.querySelector("input[name='optDoubleChartMode'][value='" + mode + "']");

			if (!checkbox || !radio) {
				return;
			}

			checkbox.checked = mode !== "off";
			radio.checked = true;
		}

		function markDoubleChartModeResolved(mode) {
			mode = normalizeDoubleChartMode(mode);
			if (!mode) {
				return "";
			}

			doubleChartStateResolved = true;
			writeHiddenDoubleChartMode(mode);
			return mode;
		}

		function resolveDoubleChartMode() {
			var chartMode = readChartScriptMode();
			var hiddenMode;

			if (doubleChartStateResolved && chartMode) {
				writeHiddenDoubleChartMode(chartMode);
				return chartMode;
			}

			hiddenMode = readHiddenDoubleChartMode();
			if (doubleChartStateResolved && hiddenMode) {
				return hiddenMode;
			}

			return "";
		}

		function applyDoubleChartMode(mode) {
			var checkbox = document.getElementById("optDoubleChart");
			var radio = document.querySelector("input[name='optDoubleChartMode'][value='" + mode + "']");

			if (!checkbox || !radio) {
				return;
			}

			mode = markDoubleChartModeResolved(mode) || "off";
			writeHiddenDoubleChartMode(mode);
			radio.dispatchEvent(new Event("change", { bubbles: true }));
		}

		function syncDoubleChartButton(mode) {
			var button = document.getElementById("kisDoubleChartBtn");
			var modeEl;
			var dots;

			if (!button) {
				return;
			}

			if (mode && typeof mode === "object" && typeof mode.type === "string") {
				mode = readHiddenDoubleChartMode() || readChartScriptMode();
			}

			mode = normalizeDoubleChartMode(mode) || resolveDoubleChartMode();
			modeEl = button.querySelector(".double-chart-btn-mode");
			dots = button.querySelectorAll(".double-chart-dot");

			if (!mode) {
				button.dataset.doubleMode = "";
				button.title = "더블차트 설정 확인중";
				button.classList.remove("is-active");

				if (modeEl) {
					modeEl.textContent = "-";
				}

				if (dots.length === 3) {
					dots[0].classList.remove("is-active");
					dots[1].classList.remove("is-active");
					dots[2].classList.remove("is-active");
				}
				return;
			}

			button.dataset.doubleMode = mode;
			button.title = "더블차트 " + doubleChartModeLabel(mode);
			button.classList.toggle("is-active", mode !== "off");

			if (modeEl) {
				modeEl.textContent = doubleChartModeLabel(mode);
			}

			if (dots.length === 3) {
				dots[0].classList.toggle("is-active", mode === "recent");
				dots[1].classList.toggle("is-active", mode === "all");
				dots[2].classList.toggle("is-active", mode === "off");
			}
		}

		function requestHasValue(requestData, key, expectedValue) {
			var pair;

			if (requestData == null) {
				return false;
			}

			if (typeof requestData === "string") {
				pair = key + "=" + encodeURIComponent(expectedValue);
				if (requestData.indexOf(pair) >= 0) {
					return true;
				}

				pair = key + "=" + expectedValue;
				return requestData.indexOf(pair) >= 0;
			}

			return String(requestData[key] || "") === expectedValue;
		}

		function bindDoubleChartAjaxSync() {
			if (!window.jQuery || window.__kisDoubleChartAjaxBound) {
				return;
			}

			window.__kisDoubleChartAjaxBound = true;
			$(document).off("ajaxSuccess.kisDoubleChartSync").on("ajaxSuccess.kisDoubleChartSync", function (event, xhr, settings, data) {
				var dataUrl = window.__URLS && window.__URLS.kisItemchartpriceOptionData;
				var mode;

				if (!dataUrl || !settings || String(settings.url || "").indexOf(dataUrl) === -1) {
					return;
				}

				if (!requestHasValue(settings.data, "chartId", "KIS_ITEMCHART") || !requestHasValue(settings.data, "seriesType", "FEATURE")) {
					return;
				}

				mode = extractDoubleChartModeFromOptionList(data && data.data);
				if (!mode) {
					return;
				}

				syncDoubleChartButton(markDoubleChartModeResolved(mode));
			});
		}

		function patchChartScriptSync() {
			var chartScript = window.ChartScript;
			var originalSetOptions;

			if (!chartScript || typeof chartScript.setOptions !== "function" || chartScript.__kisDoubleChartPatched) {
				return;
			}

			originalSetOptions = chartScript.setOptions;
			chartScript.setOptions = function () {
				var result = originalSetOptions.apply(this, arguments);
				var next = arguments[0] || {};
				var mode = normalizeDoubleChartMode(next.doubleChartMode || "");

				if (!mode && typeof next.doubleChartEnabled === "boolean") {
					mode = next.doubleChartEnabled ? "all" : "off";
				}

				if (mode) {
					mode = markDoubleChartModeResolved(mode);
				}

				syncDoubleChartButton(mode);
				return result;
			};
			chartScript.__kisDoubleChartPatched = true;
		}

		function patchChartFeatureToggleSync() {
			var toggle = window.ChartFeatureToggle;
			var originalWriteModal;
			var originalApply;

			if (!toggle || toggle.__kisDoubleChartPatched) {
				return;
			}

			if (typeof toggle.writeModal === "function") {
				originalWriteModal = toggle.writeModal;
				toggle.writeModal = function () {
					var result = originalWriteModal.apply(this, arguments);
					var mode = resolveModeFromState(arguments[0] || {});
					if (mode) {
						mode = markDoubleChartModeResolved(mode);
					}
					syncDoubleChartButton(mode);
					return result;
				};
			}

			if (typeof toggle.apply === "function") {
				originalApply = toggle.apply;
				toggle.apply = function () {
					var result = originalApply.apply(this, arguments);
					var mode = resolveModeFromState(arguments[0] || {});
					if (mode) {
						mode = markDoubleChartModeResolved(mode);
					}
					syncDoubleChartButton(mode);
					return result;
				};
			}

			toggle.__kisDoubleChartPatched = true;
		}

		function installDoubleChartSync() {
			bindDoubleChartAjaxSync();
			patchChartScriptSync();
			patchChartFeatureToggleSync();
			syncDoubleChartButton();
		}

		function scheduleInitialDoubleChartSync() {
			[0, 100, 300, 700, 1500].forEach(function (delay) {
				window.setTimeout(function () {
					installDoubleChartSync();
				}, delay);
			});
		}

		function bindDoubleChartButton() {
			var button = document.getElementById("kisDoubleChartBtn");
			var checkbox = document.getElementById("optDoubleChart");
			var radios = document.querySelectorAll("input[name='optDoubleChartMode']");

			if (!button || !checkbox || !radios.length) {
				return;
			}

			if (button.__kisDoubleChartBound) {
				installDoubleChartSync();
				return;
			}

			button.__kisDoubleChartBound = true;
			button.addEventListener("click", function () {
				applyDoubleChartMode(nextDoubleChartMode(resolveDoubleChartMode()));
			});

			checkbox.addEventListener("change", syncDoubleChartButton);
			Array.prototype.forEach.call(radios, function (radio) {
				radio.addEventListener("change", syncDoubleChartButton);
			});

			installDoubleChartSync();
			scheduleInitialDoubleChartSync();
		}

		if (document.readyState === "loading") {
			document.addEventListener("DOMContentLoaded", bindDoubleChartButton);
		} else {
			bindDoubleChartButton();
		}
	})();
	</script>
	<script>
	(function () {
		var watchPriceCache = {};
		var watchPriceInflight = {};
		var watchPriceTimers = {};
		var WATCH_PRICE_CACHE_MS = 2500;

		function toNumber(value) {
			var text;

			if (value === null || value === undefined) {
				return NaN;
			}

			text = String(value).replace(/,/g, "").trim();
			if (!text) {
				return NaN;
			}

			if (text.charAt(0) === "+") {
				text = text.substring(1);
			}

			return parseFloat(text);
		}

		function decimalsHint(value, fallback) {
			var text;
			var dot;
			var decimals;

			if (value === null || value === undefined) {
				return fallback;
			}

			text = String(value);
			dot = text.indexOf(".");
			if (dot < 0) {
				return fallback;
			}

			decimals = text.length - dot - 1;
			if (decimals < 0) {
				return fallback;
			}

			return decimals > 4 ? 4 : decimals;
		}

		function formatNumber(value, decimals) {
			if (!Number.isFinite(value)) {
				return "-";
			}

			return value.toLocaleString("en-US", {
				useGrouping: true,
				minimumFractionDigits: decimals,
				maximumFractionDigits: decimals
			});
		}

		function formatSignedNumber(value, decimals) {
			if (!Number.isFinite(value)) {
				return "-";
			}

			if (value > 0) {
				return "+" + formatNumber(Math.abs(value), decimals);
			}
			if (value < 0) {
				return "-" + formatNumber(Math.abs(value), decimals);
			}
			return "0";
		}

		function formatSignedPct(value) {
			if (!Number.isFinite(value)) {
				return "-";
			}

			if (value > 0) {
				return "+" + Math.abs(value).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + "%";
			}
			if (value < 0) {
				return "-" + Math.abs(value).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + "%";
			}
			return "0.00%";
		}

		function unwrapSingle(res) {
			if (!res) {
				return null;
			}

			if (res.data) {
				if (res.data.singleData) {
					return res.data.singleData;
				}
				if (res.data.data && res.data.data.singleData) {
					return res.data.data.singleData;
				}
			}

			if (res.singleData) {
				return res.singleData;
			}

			return null;
		}

		function extractPriceOutput(single) {
			if (!single) {
				return null;
			}
			if (single.output) {
				return single.output;
			}
			if (single.out) {
				return single.out;
			}
			if (single.data && single.data.output) {
				return single.data.output;
			}
			return null;
		}

		function pickNumber(source, keys) {
			var i;
			var value;
			var numeric;

			if (!source || !keys || !keys.length) {
				return NaN;
			}

			for (i = 0; i < keys.length; i += 1) {
				value = source[keys[i]];
				numeric = toNumber(value);
				if (Number.isFinite(numeric)) {
					return numeric;
				}
			}

			return NaN;
		}

		function buildWatchPriceState(single) {
			var out = extractPriceOutput(single);
			var current;
			var diff;
			var prevClose;
			var pct;
			var diffDecimals;
			var priceDecimals;
			var dir;

			if (!out) {
				return null;
			}

			current = pickNumber(out, ["stckPrpr", "stck_prpr", "price", "last"]);
			diff = pickNumber(out, ["prdyVrss", "prdy_vrss", "diff", "change"]);
			prevClose = pickNumber(out, ["stckSdpr", "stck_sdpr", "prevClose", "base"]);

			if (!Number.isFinite(diff) && Number.isFinite(current) && Number.isFinite(prevClose)) {
				diff = current - prevClose;
			}
			if (!Number.isFinite(prevClose) && Number.isFinite(current) && Number.isFinite(diff)) {
				prevClose = current - diff;
			}
			if (!Number.isFinite(current)) {
				return null;
			}

			pct = (Number.isFinite(prevClose) && prevClose !== 0 && Number.isFinite(diff))
				? (diff / prevClose * 100)
				: NaN;

			priceDecimals = decimalsHint(out.stckPrpr || out.stck_prpr || out.price || out.last, 0);
			diffDecimals = decimalsHint(out.prdyVrss || out.prdy_vrss || out.diff || out.change, 0);
			dir = Number.isFinite(diff) ? (diff > 0 ? "up" : (diff < 0 ? "down" : "flat")) : "flat";

			return {
				priceText: formatNumber(current, priceDecimals),
				diffText: formatSignedNumber(diff, diffDecimals),
				rateText: formatSignedPct(pct),
				dir: dir
			};
		}

		function applyWatchPriceStateToRow(row, state) {
			var priceEl;
			var diffEl;
			var rateEl;
			var eventEl;

			if (!row || !state) {
				return;
			}

			priceEl = row.querySelector(".wl-price");
			diffEl = row.querySelector(".wl-diff");
			rateEl = row.querySelector(".wl-rate");
			eventEl = row.querySelector(".wl-event");

			if (priceEl) {
				priceEl.textContent = state.priceText;
				priceEl.title = state.diffText + " / " + state.rateText;
				priceEl.classList.remove("up", "down", "flat");
				priceEl.classList.add(state.dir);
			}

			if (diffEl) {
				diffEl.textContent = state.diffText;
				diffEl.classList.remove("up", "down", "flat");
				diffEl.classList.add(state.dir);
			}

			if (rateEl) {
				rateEl.textContent = state.rateText;
				rateEl.classList.remove("up", "down", "flat");
				rateEl.classList.add(state.dir);
			}

			if (eventEl) {
				eventEl.classList.remove("up", "down", "flat");
				eventEl.classList.add(state.dir);
			}
		}

		function applyWatchPriceStateByCode(code, state) {
			var rows = document.querySelectorAll("#watchlist .wl-item");
			var i;
			var row;

			if (!code || !state) {
				return;
			}

			for (i = 0; i < rows.length; i += 1) {
				row = rows[i];
				if (String(row.getAttribute("data-code") || "").trim() === code) {
					applyWatchPriceStateToRow(row, state);
				}
			}
		}

		function isDomesticWatchRow(row) {
			var country = String(row && row.getAttribute("data-country") || "").trim().toUpperCase();
			return !country || country === "KR";
		}

		function getWatchCurrentPriceUrl() {
			return (window.__CTX_PATH || "") + "/finance/getCurrentPriceByInquirePrice.do";
		}

		function syncWatchRowFromApi(row, force) {
			var code;
			var cached;
			var nowTs;

			if (!row || !window.jQuery || !isDomesticWatchRow(row)) {
				return;
			}

			code = String(row.getAttribute("data-code") || "").trim();
			if (!code) {
				return;
			}

			nowTs = Date.now();
			cached = watchPriceCache[code];
			if (!force && cached && (nowTs - cached.ts) < WATCH_PRICE_CACHE_MS) {
				applyWatchPriceStateByCode(code, cached.state);
				return;
			}

			if (watchPriceInflight[code]) {
				return;
			}

			watchPriceInflight[code] = true;
			$.ajax({
				url: getWatchCurrentPriceUrl(),
				type: "GET",
				dataType: "json",
				data: { in_stockCode: code },
				success: function (res) {
					var single = unwrapSingle(res);
					var state = buildWatchPriceState(single);

					if (!state) {
						return;
					}

					watchPriceCache[code] = {
						ts: Date.now(),
						state: state
					};
					applyWatchPriceStateByCode(code, state);
				},
				complete: function () {
					delete watchPriceInflight[code];
				}
			});
		}

		function scheduleWatchRowSync(row, force) {
			var code;

			if (!row || !isDomesticWatchRow(row)) {
				return;
			}

			code = String(row.getAttribute("data-code") || "").trim();
			if (!code) {
				return;
			}

			if (watchPriceTimers[code]) {
				clearTimeout(watchPriceTimers[code]);
			}

			watchPriceTimers[code] = window.setTimeout(function () {
				delete watchPriceTimers[code];
				syncWatchRowFromApi(row, force === true);
			}, force === true ? 0 : 120);
		}

		function collectWatchRows(node, rows) {
			var row;
			var found;
			var i;

			if (!node) {
				return;
			}

			if (node.nodeType === 3) {
				row = node.parentElement ? node.parentElement.closest(".wl-item") : null;
				if (row) {
					rows.add(row);
				}
				return;
			}

			if (node.nodeType !== 1) {
				return;
			}

			row = node.closest(".wl-item");
			if (row) {
				rows.add(row);
			}

			if (node.matches(".wl-item")) {
				rows.add(node);
			}

			if (typeof node.querySelectorAll !== "function") {
				return;
			}

			found = node.querySelectorAll(".wl-item");
			for (i = 0; i < found.length; i += 1) {
				rows.add(found[i]);
			}
		}

		function bindWatchlistQuoteSync() {
			var wrap = document.getElementById("watchlist");
			var observer;

			if (!wrap || wrap.__watchlistQuoteSyncBound) {
				return;
			}

			wrap.__watchlistQuoteSyncBound = true;

			Array.prototype.forEach.call(wrap.querySelectorAll(".wl-item"), function (row) {
				scheduleWatchRowSync(row, true);
			});

			observer = new MutationObserver(function (mutations) {
				var rows = new Set();

				mutations.forEach(function (mutation) {
					var i;

					collectWatchRows(mutation.target, rows);

					if (mutation.addedNodes && mutation.addedNodes.length) {
						for (i = 0; i < mutation.addedNodes.length; i += 1) {
							collectWatchRows(mutation.addedNodes[i], rows);
						}
					}
				});

				rows.forEach(function (row) {
					scheduleWatchRowSync(row, false);
				});
			});

			observer.observe(wrap, {
				childList: true,
				characterData: true,
				subtree: true
			});
		}

		if (document.readyState === "loading") {
			document.addEventListener("DOMContentLoaded", bindWatchlistQuoteSync);
		} else {
			bindWatchlistQuoteSync();
		}
	})();
	</script>
	<script src="/scheduler/appone/jsp/finance/kis/kisFinance/js/kisFinancePage.js?v=20260312-2" defer></script>
</body>
</html>
