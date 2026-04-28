<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>추천 종목 상세 분석</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/appone/jsp/stock/recSignal/css/recSignalList.css?v=20260317-8">
</head>
<body>
<div class="rec-shell">
  <main class="detail-column">
    <section class="panel detail-panel">
      <div class="detail-head">
        <div>
          <span class="panel-kicker">선택 종목</span>
          <h2 id="detailTitle">-</h2>
          <p id="detailSubtitle" class="detail-subtitle">-</p>
        </div>
        <div class="detail-badges">              
           <a id="listPageLink" class="detail-link" href="<c:url value='/stock/recSignal/listView.do' />">목록으로</a>    
          <span id="detailGrade" class="grade-chip">-</span>
          <span id="detailRecommend" class="status-chip">추천 상태 확인 중</span>
        </div>
      </div>

      <div class="detail-hero-grid">
        <article class="hero-price-card">
          <span class="metric-label">현재가</span>
          <strong id="detailCurrentPrice">-</strong>
          <div id="detailMonthChange" class="metric-delta">-</div>
          <p id="detailReason" class="reason-copy">-</p>
        </article>
        <article class="hero-score-card">
          <div class="score-row">
            <span>정배열</span>
            <strong id="detailGoldenYn">-</strong>
          </div>
          <div class="score-row">
            <span>해당월 상승</span>
            <strong id="detailMonthUpYn">-</strong>
          </div>
          <div class="score-row">
            <span>추세 강도</span>
            <strong id="detailTrendStrength">-</strong>
          </div>
          <div class="score-row">
            <span>평균 거래대금(20)</span>
            <strong id="detailTradeValue">-</strong>
          </div>
        </article>
      </div>

      <div id="metricGrid" class="metric-grid"></div>
    </section>

    <details class="panel decision-panel section-accordion">
      <summary class="panel-head panel-summary">
        <span class="panel-summary-main">
          <span class="panel-kicker">판정 근거</span>
          <strong class="panel-summary-title panel-summary-title-lg">추천 결정 사다리</strong>
        </span>
        <span class="panel-summary-side">
          <span class="panel-note">저장된 추천 결과를 기준으로 설명합니다</span>
          <span class="panel-summary-toggle" aria-hidden="true"></span>
        </span>
      </summary>
      <div class="panel-accordion-body">
        <div id="decisionList" class="decision-list"></div>
      </div>
    </details>

    <section class="lower-grid">
      <section class="panel">
        <div class="panel-head">
        <div>
          <span class="panel-kicker">이동평균</span>
          <h2>MA 상태 비교</h2>
        </div>
          <span class="panel-note">보정 종가 기준 이동평균</span>
        </div>
        <div id="maLadder" class="ma-ladder"></div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">종목 해석</span>
            <h2>분석 메모</h2>
          </div>
        </div>
        <ul id="analysisNoteList" class="rule-list">
          <li>상세 데이터를 불러오면 실제 적재 값 기준 해석 메모가 표시됩니다.</li>
        </ul>
      </section>
    </section>
  </main>
</div>

<script>
window.recSignalDetailConfig = {
  detailUrl: "<c:url value='/stock/recSignal/detail.do' />",
  listUrl: "<c:url value='/stock/recSignal/list.do' />",
  listViewUrl: "<c:url value='/stock/recSignal/listView.do' />",
  baseDt: "<c:out value='${baseDt}' />",
  stkCd: "<c:out value='${stkCd}' />",
  mktCd: "<c:out value='${mktCd}' />"
};
</script>
<script src="${pageContext.request.contextPath}/appone/jsp/stock/recSignal/js/recSignalDetail.js?v=20260317-5"></script>
</body>
</html>
