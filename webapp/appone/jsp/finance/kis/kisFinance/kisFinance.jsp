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
<script defer src="/scheduler/appone/plugins/system/js/common.js"></script>

<script>
  // Highstock는 차트 최초 조회 시점에 동적 로딩합니다.
  window.__HIGHCHARTS_SRC = {
    highstock: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/highstock.js' />",
    data: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/modules/data.js' />",
    exporting: "<c:url value='/appone/plugins/Highcharts-Stock-11.1.0/code/modules/exporting.js' />"
  };
</script>

<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisFinance.css?v=20260404-1" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/common.css" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisFinance.mobileFix.css?v=20260406-7" />
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/kis/kisFinance/css/kisDashboardChart.css?v=20260305-1" />

<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/maScript.js"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/doubleMonthChartScript.js?v=20260203-7"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/kisDashboardChartRenderer.js?v=20260406-2"></script>
<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/chartScript.js?v=20260404-1"></script>
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
    recPickRegisterBuy: "<c:url value='/stock/recPick/registerBuy.do'/>",
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
			<div class="top-search-wrap search-wrapper" id="topSearchWrap">
				<div class="input-holder">
					<input type="text" class="search-input" id="topSearchInput" placeholder="Type to search" autocomplete="off" />
					<button type="button" class="search-icon" id="topSearchTrigger" aria-label="종목 검색 열기">
						<span></span>
					</button>
					<button type="button" class="top-search-clear" id="topSearchClear" title="Clear" aria-label="입력 지우기">×</button>
				</div>
				<button type="button" class="btn-close" id="topSearchClose" aria-label="검색 닫기"></button>
				<div class="top-suggest" id="topSuggest" aria-hidden="true"></div>
			</div>
			<div class="mobile-panel-buttons" id="mobilePanelButtons" aria-hidden="true">
				<button type="button" class="mpb-btn is-watch" id="btnMobileOpenWatch" title="관심종목" aria-label="관심종목">
					<i class="fa-solid fa-bookmark" aria-hidden="true"></i>
				</button>
				<button type="button" class="mpb-btn" id="btnMobileOpenSummary" title="매매신호" aria-label="매매신호">
					<i class="fa-solid fa-thumbs-up" aria-hidden="true"></i>
				</button>
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
	<div class="dash-shell">
		<div class="mobile-panel-backdrop" id="mobilePanelBackdrop" aria-hidden="true"></div>
		<!-- LEFT: Watchlist -->
		<div class="panel left-panel is-collapsed" id="leftPanel">
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
						<!-- <div class="kis-toolbar-row kis-toolbar-row-controls">
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
							</div> -->
						</div>
						<div id="kisIntradayHint" class="mini-muted" style="display:none; padding-top:4px;"></div>
					</div>				
					<div class="chart-wrap">
					<div id="kisChartHeader" class="kis-h">
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
					</div>
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
								<div id="kisMaLegend">
								<!-- MA 뱃지는 DB 옵션 기반으로 JS에서 렌더링 -->
								</div>
							</div>						
						</div>
						<div class="kis-sr-list" id="kisMaSrList"></div>
                        </div>
						<div id="kisChartContainer"></div>
					</div>
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
				<div class="right-tabs p-0" id="rightTabs">
					<button type="button" class="rt-tab is-active" data-tab="signal">추천 종목</button>
                    <button type="button" class="rt-tab" data-tab="holding">보유종목</button>
						<button type="button" class="rt-tab" data-tab="theme">테마종목</button>
				</div>

				<div class="rt-pane is-active" data-pane="signal">
					<section class="signal-rec-panel">					
						<div class="signal-rec-toolbar">
							<div class="signal-rec-toolbar-left">
								<div class="signal-rec-toolbar-group">
									<button type="button" class="wl-market-pill signal-country-pill" id="signalCountrySwitchBtn" aria-label="시장 전환">
										<span class="wl-market-pill-text" id="signalCountryPillText">한국</span>
										<span class="wl-market-pill-dot" aria-hidden="true"></span>
									</button>
								</div>
								<div class="signal-rec-toolbar-group">
									<button type="button" class="signal-cycle-btn" id="signalMarketCycleBtn" data-market-mode="ALL" title="시장 전체">
										<span class="signal-cycle-btn-mode">A</span>
										<span class="signal-cycle-dots" aria-hidden="true">
											<span class="signal-cycle-dot is-active"></span>
											<span class="signal-cycle-dot"></span>
											<span class="signal-cycle-dot"></span>
										</span>
									</button>
								</div>
								<div class="signal-rec-toolbar-group">
									<button type="button" class="signal-cycle-btn" id="signalGradeCycleBtn" data-grade-mode="ALL" title="등급 전체">
										<span class="signal-cycle-btn-mode">A</span>
										<span class="signal-cycle-dots" aria-hidden="true">
											<span class="signal-cycle-dot is-active"></span>
											<span class="signal-cycle-dot"></span>
											<span class="signal-cycle-dot"></span>
											<span class="signal-cycle-dot"></span>
										</span>
									</button>
								</div>
							</div>
							<div class="signal-rec-toolbar-right">
								<div class="signal-rec-sort">
									<select id="signalSortFilter" class="signal-rec-select">
										<option value="rank">기본 순위</option>
										<option value="monthChange">월간 상승률</option>
										<option value="trend">추세 강도</option>
										<option value="tradeValue">평균 거래대금</option>
									</select>
								</div>
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

	<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/doubleChartSync.js?v=20260404-1"></script>
	<script defer src="/scheduler/appone/jsp/finance/kis/kisFinance/js/watchlistQuoteSync.js?v=20260404-1"></script>
	<script src="/scheduler/appone/jsp/finance/kis/kisFinance/js/kisFinancePage.js?v=20260406-1" defer></script>
</body>
</html>
