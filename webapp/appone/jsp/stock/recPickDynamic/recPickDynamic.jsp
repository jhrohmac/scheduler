<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/appone/jsp/stock/recPickDynamic/css/recPickDynamic.css?v=20260508-3">

<div class="rpd-app" id="rpdApp">

    <!-- 상단 바 -->
    <div class="rpd-topbar">
        <div class="rpd-topbar-left">
            <h2 class="rpd-title">동적 추천 종목 선별</h2>
            <span class="rpd-breadcrumb">Stock &gt; 추천 &gt; 동적 선별</span>
        </div>
        <div class="rpd-topbar-right">
            <span class="rpd-basedt-label">기준일:</span>
            <span class="rpd-basedt-value" id="rpdBaseDt">—</span>
            <span class="rpd-basedt-source">TB_REC_SIGNAL 최신본</span>
        </div>
    </div>

    <!-- 메인 레이아웃 -->
    <div class="rpd-layout">

        <!-- ════════ 좌: 지표 패널 ════════ -->
        <aside class="rpd-panel rpd-side">
            <div class="rpd-panel-header">🔍 필터 / 지표 설정</div>
            <div class="rpd-panel-body" id="rpdFilterBody">

                <!-- 시장 그룹 탭 -->
                <div class="rpd-market-tabs" id="rpdMarketGroupTabs">
                    <button type="button" class="rpd-tab active" data-mkt-group="KR">국내</button>
                    <button type="button" class="rpd-tab" data-mkt-group="US">해외 (미국)</button>
                </div>

                <!-- 시장 -->
                <div class="rpd-filter-row">
                    <label>시장</label>
                    <div class="rpd-chips" id="rpdMarketChips"></div>
                </div>
                <!-- 지수 -->
                <div class="rpd-filter-row">
                    <label>지수</label>
                    <div class="rpd-chips" id="rpdIndexChips"></div>
                </div>
                <!-- 유형 -->
                <div class="rpd-filter-row">
                    <label>유형</label>
                    <div class="rpd-chips" id="rpdTypeChips"></div>
                </div>

                <!-- 지표 카드 영역 (카테고리별 그룹, 동적 렌더링) -->
                <div id="rpdIndicatorSections"></div>

                <button type="button" class="rpd-add-indicator-btn" id="rpdBtnAddInfo">
                    + 지표 추가하기
                </button>
            </div>
            <div class="rpd-panel-footer">
                <button type="button" class="rpd-btn-apply" id="rpdBtnApply">🔄 적용</button>
                <button type="button" class="rpd-btn-reset" id="rpdBtnReset" title="초기화">⟲</button>
            </div>
        </aside>

        <!-- ════════ 우: 결과 영역 ════════ -->
        <main class="rpd-main">

            <!-- 요약 바 -->
            <div class="rpd-summary-bar">
                <div class="rpd-summary-stat">
                    <span class="rpd-stat-num" id="rpdCntTotal">0</span>
                    <span class="rpd-stat-label">건 매칭</span>
                </div>
                <div class="rpd-divider"></div>
                <div class="rpd-summary-stat">
                    <span class="rpd-stat-num" id="rpdCntIndicators">0</span>
                    <span class="rpd-stat-label">지표 적용</span>
                </div>
                <div class="rpd-divider"></div>
                <div class="rpd-summary-stat rpd-grade-stat">
                    <span class="rpd-grade-a">A <span id="rpdGradeA">0</span></span>
                    <span class="rpd-grade-b">B <span id="rpdGradeB">0</span></span>
                    <span class="rpd-grade-c">C <span id="rpdGradeC">0</span></span>
                </div>
                <div class="rpd-summary-spacer"></div>
                <div class="rpd-summary-hint">💡 행 클릭 → 우측 상세 슬라이드</div>
            </div>

            <!-- 그리드 -->
            <div class="rpd-panel rpd-grid-panel">
                <div class="rpd-panel-header rpd-grid-header">
                    <span>📋 매칭 종목</span>
                    <div class="rpd-grid-tools">
                        <select id="rpdSortSelect">
                            <option value="trendStrength">정렬: 추세강도 ↓</option>
                            <option value="monChgRate">정렬: 등락률 ↓</option>
                            <option value="avgTrdVal20">정렬: 거래대금 ↓</option>
                            <option value="curPrice">정렬: 현재가 ↓</option>
                            <option value="stkNm">정렬: 종목명</option>
                        </select>
                    </div>
                </div>
                <div class="rpd-grid-wrap" id="rpdGridWrap">
                    <table class="rpd-grid">
                        <thead>
                            <tr>
                                <th class="rpd-col-no">No</th>
                                <th>종목코드</th>
                                <th>종목명</th>
                                <th>시장</th>
                                <th class="text-right">현재가</th>
                                <th class="text-right">등락률</th>
                                <th class="text-right">추세강도</th>
                                <th class="text-right">거래대금(억)</th>
                                <th>등급</th>
                                <th>매칭 지표</th>
                                <th>분석일</th>
                                <th class="rpd-col-action">픽 등록</th>
                            </tr>
                        </thead>
                        <tbody id="rpdGridBody">
                            <tr><td colspan="12" class="rpd-empty-row">조회 중...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- ════════ 슬라이드 상세 ════════ -->
    <div class="rpd-drawer-overlay" id="rpdDrawerOverlay"></div>
    <aside class="rpd-drawer" id="rpdDrawer">
        <div class="rpd-drawer-header">
            <button type="button" class="rpd-drawer-close" id="rpdDrawerClose">✕</button>
            <span class="rpd-drawer-name" id="rpdDName">—</span>
            <span class="rpd-drawer-code" id="rpdDCode">—</span>
        </div>
        <div class="rpd-drawer-body" id="rpdDrawerBody">

            <div class="rpd-drawer-section">
                <h6>📈 일봉 차트 + MA <button type="button" id="rpdBtnRefreshChart" class="rpd-chart-refresh-btn" title="실시간 가격 새로고침">🔄</button></h6>

                <!-- 차트 옵션 토글 -->
                <div class="rpd-chart-controls">
                    <div class="rpd-chart-controls-row">
                        <span class="rpd-chart-ctrl-label">MA</span>
                        <label class="rpd-chip-toggle"><input type="checkbox" data-ma="5"   checked> MA5</label>
                        <label class="rpd-chip-toggle"><input type="checkbox" data-ma="20"  checked> MA20</label>
                        <label class="rpd-chip-toggle"><input type="checkbox" data-ma="60"  checked> MA60</label>
                        <label class="rpd-chip-toggle"><input type="checkbox" data-ma="120"> MA120</label>
                        <label class="rpd-chip-toggle"><input type="checkbox" data-ma="240"> MA240</label>
                    </div>
                    <div class="rpd-chart-controls-row">
                        <span class="rpd-chart-ctrl-label">표시</span>
                        <label class="rpd-chip-toggle"><input type="checkbox" id="rpdToggleVolume" checked> 거래량</label>
                        <label class="rpd-chip-toggle"><input type="checkbox" id="rpdToggleCandle"> 캔들(OHLC)</label>
                    </div>
                </div>

                <div id="rpdChartContainer" class="rpd-chart"></div>
            </div>

            <div class="rpd-drawer-section">
                <h6>📊 지표 상세</h6>
                <table class="rpd-indicator-table" id="rpdIndicatorTable">
                    <tr><td>현재가</td>            <td id="rpdDPrice">—</td></tr>
                    <tr><td>당월 시가</td>         <td id="rpdDMonOpen">—</td></tr>
                    <tr><td>월 등락률</td>         <td id="rpdDChgRate">—</td></tr>
                    <tr><td>MA5 / MA20</td>        <td id="rpdDMa1">—</td></tr>
                    <tr><td>MA60 / MA120</td>      <td id="rpdDMa2">—</td></tr>
                    <tr><td>MA240</td>             <td id="rpdDMa240">—</td></tr>
                    <tr><td>추세 강도</td>         <td id="rpdDTrend">—</td></tr>
                    <tr><td>20일 평균 거래대금</td>  <td id="rpdDTrdVal">—</td></tr>
                    <tr><td>RSI(14)</td>           <td id="rpdDRsi">—</td></tr>
                </table>
            </div>

            <div class="rpd-drawer-section">
                <h6>✅ 매칭 조건</h6>
                <ul class="rpd-match-list" id="rpdMatchList"></ul>
            </div>
        </div>
        <div class="rpd-drawer-footer">
            <button type="button" class="rpd-pick-big" id="rpdPickBtnBig">★ 관심종목에 추가</button>
        </div>
    </aside>

    <!-- 토스트 -->
    <div class="rpd-toast" id="rpdToast">알림</div>

</div>

<script>
window.recPickDynamicConfig = {
    indicatorMetaUrl: "<c:url value='/stock/recPickDynamic/indicatorMeta.do' />",
    listUrl:          "<c:url value='/stock/recPickDynamic/list.do' />",
    detailUrl:        "<c:url value='/stock/recPickDynamic/detail.do' />",
    refreshPriceUrl:  "<c:url value='/stock/recPickDynamic/refreshPrice.do' />",
    saveToWatchlistUrl: "<c:url value='/stock/recPick/saveToWatchlist.do' />",
    userId:           "<c:out value='${userId}' default='anonymous' />"
};

/* ────────────────────────────────────────────────────────────
 * 의존성 부트스트래퍼
 * - 메인 layout 통한 ajax 임베드 진입: jQuery 등 이미 로드됨 → skip
 * - 직접 URL 접근: jQuery / Highcharts 가 없으면 자체 로드 후 컨트롤러 시작
 * ──────────────────────────────────────────────────────────── */
(function () {
    var ctxPath = "${pageContext.request.contextPath}";
    var deps = [];
    if (typeof window.jQuery === "undefined") {
        deps.push("https://code.jquery.com/jquery-3.6.0.min.js");
    }
    if (typeof window.Highcharts === "undefined") {
        // Highcharts Stock — rangeSelector / navigator / 더블차트 지원
        deps.push("https://code.highcharts.com/stock/highstock.js");
        deps.push("https://code.highcharts.com/stock/indicators/indicators.js");
    } else if (typeof window.Highcharts.stockChart === "undefined") {
        // Highcharts 는 있지만 Stock 모듈이 없으면 추가
        deps.push("https://code.highcharts.com/stock/modules/stock.js");
    }
    var modules = [
        "/appone/jsp/stock/recPickDynamic/js/indicators/IndicatorRegistry.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/indicators/GoldenArrayIndicator.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/indicators/MonthUpIndicator.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/indicators/TrendStrengthIndicator.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/indicators/VolumeFilterIndicator.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/indicators/PriceRangeIndicator.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/indicators/RsiIndicator.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/indicators/MacdIndicator.js?v=20260508-3",
        "/appone/jsp/stock/recPickDynamic/js/recPickDynamic.js?v=20260508-3"
    ].map(function (p) { return ctxPath + p; });

    function loadSequential(urls, done) {
        var i = 0;
        function next() {
            if (i >= urls.length) { done(); return; }
            var s = document.createElement("script");
            s.src = urls[i++];
            s.async = false;
            s.onload = next;
            s.onerror = function () {
                console.error("[recPickDynamic] script load failed:", s.src);
                next();
            };
            document.head.appendChild(s);
        }
        next();
    }

    // 외부 deps → 내부 모듈 → init
    loadSequential(deps, function () {
        loadSequential(modules, function () {
            if (window.recPickDynamicBootstrap) {
                window.recPickDynamicBootstrap();
            }
        });
    });
})();
</script>
