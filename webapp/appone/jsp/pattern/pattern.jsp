<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>KJC 패턴 분석 대시보드</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/appone/jsp/pattern/pattern.css?v=20260319-3">
<link rel="stylesheet" href="<%= request.getContextPath() %>/appone/jsp/pattern/pattern-chart.css?v=20260319-1">
<link rel="stylesheet" href="<%= request.getContextPath() %>/appone/jsp/pattern/pattern-library.css?v=20260319-1">
<link rel="stylesheet" href="<%= request.getContextPath() %>/appone/jsp/pattern/pattern-options.css?v=20260319-1">
<link rel="stylesheet" href="<%= request.getContextPath() %>/appone/jsp/pattern/pattern-verify.css?v=20260319-1">
</head>
<body>

<div id="header">
  <div>
    <div class="header-title">📊 주식 패턴 분석 대시보드 <span style="font-size:11px;background:#059669;color:#fff;padding:1px 7px;border-radius:4px;margin-left:6px;font-weight:700">v2.0</span></div>
    <div class="header-sub" id="header-subline">KIS 차트 데이터 로딩 준비 중</div>
  </div>
  <div class="header-search">
    <div class="header-search-label">종목 조회</div>
    <div class="header-search-box">
      <input type="text" id="stock-search-input" placeholder="종목명 또는 코드 검색" autocomplete="off">
      <button type="button" class="header-search-clear" id="stock-search-clear" aria-label="검색어 지우기">×</button>
    </div>
    <div class="header-search-results" id="stock-search-results"></div>
  </div>
  <div class="header-stats">
    <div class="stat-item"><div class="stat-label">현재가</div><div class="stat-value" id="h-price" style="color:#38BDF8">—<span class="stat-unit">원</span></div></div>
    <div class="stat-item"><div class="stat-label">RSI(14)</div><div class="stat-value" id="h-rsi">—</div></div>
    <div class="stat-item"><div class="stat-label">Pivot(PP)</div><div class="stat-value" id="h-pivot" style="color:#A78BFA">—</div></div>
    <div class="stat-item"><div class="stat-label">6F강도</div><div class="stat-value" id="h-strength">—</div></div>
    <div class="stat-item"><div class="stat-label">2년수익</div><div class="stat-value" id="h-ret">—</div></div>
    <div class="stat-item"><div class="stat-label">전체감지</div><div class="stat-value" id="h-total" style="color:#FFD700">—건</div></div>
    <div class="stat-item"><div class="stat-label">패턴수</div><div class="stat-value" style="color:#A78BFA">42+8개</div></div>
    <button id="btn-download" onclick="downloadAll()" style="margin-left:16px;padding:6px 16px;font-size:11px;font-weight:700;background:#1E3A5F;color:#38BDF8;border:1px solid #38BDF8;border-radius:6px;cursor:pointer;font-family:inherit;white-space:nowrap">📦 전체 다운로드</button>
  </div>
  <button type="button" class="theme-mode-indicator" id="theme-mode-indicator" aria-label="시스템 테마 아이콘" title="시스템 테마 감지 중">
    <span class="theme-icon theme-icon-sun" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="4.2" stroke="currentColor" stroke-width="1.8"/>
        <path d="M12 2.8V5.2M12 18.8v2.4M21.2 12h-2.4M5.2 12H2.8M18.5 5.5 16.8 7.2M7.2 16.8l-1.7 1.7M18.5 18.5l-1.7-1.7M7.2 7.2 5.5 5.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
      </svg>
    </span>
    <span class="theme-icon theme-icon-moon" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none">
        <path d="M15.6 3.4a8.8 8.8 0 1 0 5 15.7A9.4 9.4 0 1 1 15.6 3.4Z" fill="currentColor"/>
      </svg>
    </span>
  </button>
</div>

<div id="tabbar">
  <button class="tab-btn active" data-tab="chart">📈 차트분석</button>
  <button class="tab-btn" data-tab="library">🔍 패턴라이브러리 (42개)</button>
  <button class="tab-btn" data-tab="options">⚙️ 옵션설정</button>
  <button class="tab-btn" data-tab="verify">✅ 전문가검증</button>
</div>

<div id="body">
  <div id="sidebar">
    <div class="sidebar-toolbar">
      <button type="button" class="sidebar-toggle-btn" id="sidebar-toggle-btn" aria-label="사이드바 닫기" title="사이드바 닫기">
        <span class="sidebar-toggle-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <rect class="sidebar-toggle-frame" x="4.5" y="6" width="15" height="12" rx="2.5" stroke="currentColor" stroke-width="1.6"/>
            <rect class="sidebar-toggle-pane sidebar-toggle-pane-left" x="6.3" y="7.8" width="4.1" height="8.4" rx="1.2" fill="currentColor"/>
            <rect class="sidebar-toggle-pane sidebar-toggle-pane-right" x="11.8" y="7.8" width="5.9" height="8.4" rx="1.2" stroke="currentColor" stroke-width="1.2"/>
          </svg>
        </span>
      </button>
    </div>
    <div class="sidebar-collapsed-nav" id="sidebar-collapsed-nav" aria-hidden="true">
      <button type="button" class="sidebar-icon-btn" id="sidebar-collapsed-patterns" aria-label="패턴 선택 열기" title="패턴 선택 열기">
        <span class="sidebar-icon-svg" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M7 4.8h10a2.2 2.2 0 0 1 2.2 2.2v10.2A2.2 2.2 0 0 1 17 19.4H7a2.2 2.2 0 0 1-2.2-2.2V7A2.2 2.2 0 0 1 7 4.8Z" fill="currentColor" opacity=".14"/>
            <path d="M8.2 3.8v2.3M15.8 3.8v2.3M6 8.1h12" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
            <rect x="5.2" y="5.3" width="13.6" height="13.5" rx="2.4" stroke="currentColor" stroke-width="1.7"/>
          </svg>
        </span>
      </button>
      <button type="button" class="sidebar-icon-btn" id="sidebar-collapsed-indicators" aria-label="이동평균선 열기" title="이동평균선 열기">
        <span class="sidebar-icon-svg" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <ellipse cx="12" cy="6.9" rx="5.8" ry="2.8" fill="currentColor" opacity=".18"/>
            <ellipse cx="12" cy="6.9" rx="5.8" ry="2.8" stroke="currentColor" stroke-width="1.6"/>
            <path d="M6.2 6.9v3.8c0 1.5 2.6 2.8 5.8 2.8s5.8-1.3 5.8-2.8V6.9M6.2 10.7v3.7c0 1.5 2.6 2.8 5.8 2.8s5.8-1.3 5.8-2.8v-3.7" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
          </svg>
        </span>
      </button>
    </div>
    <div class="sidebar-scroll">
      <div class="sidebar-section-card" id="sidebar-pattern-section">
        <button type="button" class="sidebar-accordion-toggle" id="sidebar-pattern-toggle" aria-expanded="true" aria-controls="sidebar-pattern-body">
          <div class="sidebar-section-title">패턴 선택 (차트 오버레이)</div>
          <span class="sidebar-accordion-icon" aria-hidden="true">▾</span>
        </button>
        <div class="sidebar-accordion-body" id="sidebar-pattern-body">
          <div class="sidebar-pattern-group">
            <div class="module-filters" id="module-filters"></div>
            <div class="pattern-list" id="pattern-list"></div>
          </div>
        </div>
      </div>
      <div class="sidebar-section-card ma-section" id="sidebar-indicator-section">
        <button type="button" class="sidebar-accordion-toggle" id="sidebar-indicator-toggle" aria-expanded="true" aria-controls="sidebar-indicator-body">
          <div class="sidebar-section-title">이동평균선</div>
          <span class="sidebar-accordion-icon" aria-hidden="true">▾</span>
        </button>
        <div class="sidebar-accordion-body" id="sidebar-indicator-body">
          <div class="sidebar-indicator-group">
            <div id="ma-toggles"></div>
            <div id="indicator-toggles"></div>
          </div>
        </div>
      </div>
      <div class="sidebar-summary" id="sidebar-summary">
        <div style="font-size:9px;color:#475569;margin-bottom:4px">선택된 패턴 (<span id="sel-count">0</span>개)</div>
        <div class="selected-pats-chips" id="selected-chips"></div>
      </div>
    </div>
  </div>

  <div id="main">
    <div id="tab-chart"></div>
    <div id="tab-library" style="display:none"></div>
    <div id="tab-options" style="display:none"></div>
    <div id="tab-verify" style="display:none"></div>
  </div>
</div>

<script>
window.PatternBootstrap = {
  appCtx: "<%= request.getContextPath() %>"
};
</script>
<script src="<%= request.getContextPath() %>/appone/jsp/pattern/doubleMonthChartScript.js?v=20260319-2"></script>
<script src="<%= request.getContextPath() %>/appone/jsp/pattern/pattern.js?v=20260319-2"></script>
<script src="<%= request.getContextPath() %>/appone/jsp/pattern/patternChartOptions.js?v=20260319-1"></script>
</body>
</html>
