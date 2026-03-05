<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>StockSignal Dashboard</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/appone/jsp/finance/stockSignal/dashboard.css"/>
</head>
<body>
  <input type="hidden" id="ctx" value="${pageContext.request.contextPath}"/>

  <header class="hdr">
    <div>
      <div class="logo">MA SIGNAL</div>
      <div class="title-wrap">
        <span id="stockName" class="stock-name">삼성전자</span>
        <span id="stockCode" class="stock-code">005930</span>
      </div>
    </div>
    <div class="search-wrap">
      <input id="symbolInput" type="text" value="005930" placeholder="종목코드"/>
      <button id="btnLoad" type="button">조회</button>
    </div>
  </header>

  <section class="summary">
    <div class="price-box">
      <span id="currentPrice" class="price">-</span>
      <span id="priceChange" class="change">-</span>
      <span id="priceTime" class="time">-</span>
    </div>
    <div class="controls">
      <select id="periodDivCode">
        <option value="D">일</option>
        <option value="W">주</option>
        <option value="M">월</option>
      </select>
      <label><input id="showVolume" type="checkbox"/> 거래량</label>
    </div>
  </section>

  <main class="main">
    <div class="chart-card">
      <canvas id="mainChart"></canvas>
    </div>
  </main>

  <script src="${pageContext.request.contextPath}/appone/jsp/finance/stockSignal/dashboard.js"></script>
</body>
</html>
