<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Stock Market</title>
</head>
<body>
<div class="row">
    <div class="col-sm-1">
        <div class="description-block border-right m-0">
            <span class="description-text"><b>국내증시</b></span>
        </div>
    </div>
    <div class="col-sm-2 col-6">
        <div class="description-block border-right  m-0">
            <span class="description-text float-left">코스피</span>
            <span class="description-percentage ">2750</span>
            <span class="description-percentage ml-2">
                <i class="fas fa-caret-up"></i> 17%</span>
        </div>
    </div>
    <div class="col-sm-2 col-6">
        <div class="description-block border-right  m-0">
            <span class="description-text float-left">코스닥</span>
            <span class="description-percentage ">2750</span>
            <span class="description-percentage ml-2">
                <i class="fas fa-caret-down"></i> 17%</span>
        </div>
    </div>
    <div class="col-sm-1">
        <div class="description-block border-right m-0">
            <span class="description-text"><b>해외증시</b></span>
        </div>
    </div>
    <div class="col-sm-2 col-6">
        <div class="description-block border-right  m-0">
            <span class="description-text float-left">다우존스</span>
            <span class="description-percentage ">2750</span>
            <span class="description-percentage ml-2"><i class="fas fa-caret-up"></i> 17%</span>
        </div>
    </div>
    <div class="col-sm-2 col-6">
        <div class="description-block border-right  m-0">
            <span class="description-text float-left">나스닥</span>
            <span class="description-percentage ">2750</span>
            <span class="description-percentage ml-2"><i class="fas fa-caret-up"></i> 17%</span>
        </div>
    </div>
    <div class="col-sm-2 col-6">
        <div class="description-block   m-0">
            <span class="description-text float-left">S&P500</span>
            <span class="description-percentage ">2750</span>
            <span class="description-percentage ml-2"><i class="fas fa-caret-up"></i> 17%</span>
        </div>
    </div>
</div>

<ul class="products-list product-list-in-card col">
    <!-- Loop through your stock list here -->
</ul>

<!-- Add switch buttons here to toggle between domestic and overseas markets -->
<div class="text-center mt-4">
    <label><input type="radio" name="market" value="domestic" checked th:checked="${groupMarket == 'domestic'}" th:onclick="'/setMarket?market=domestic'"> 국내증시</label>
    <label><input type="radio" name="market" value="overseas" th:checked="${groupMarket == 'overseas'}" th:onclick="'/setMarket?market=overseas'"> 해외증시</label>
</div>
</body>
</html>
