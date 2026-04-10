<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
<title>오늘의 주가</title>
<body>
<div class="container">
	<div th:replace="/nav.jsp :: fragment-nav"></div>
	
	<div class="h4 pb-2 mb-4 text-danger border-bottom border-info"></div>
	<div><span>조회시간: </span><span th:text="${jobDate}" ></span></div>
	
	<div class="card">
  		<div class="card-header">
    		<h2 th:text="${equity.stck_shrn_iscd}"></h2>
  		</div>
  		<div class="card-body">
			<div>
				<span class="fs-1" th:text="${equity.stck_prpr}"></span>원
				<span th:text="${equity.prdy_vrss}" th:style="${equity.prdy_vrss_sign<'3'?'color:red':(equity.prdy_vrss_sign>'3'?'color:blue':'color:black')}"></span>
				<span th:text="${equity.prdy_ctrt}" th:style="${equity.prdy_vrss_sign<'3'?'color:red':(equity.prdy_vrss_sign>'3'?'color:blue':'color:black')}"></span>
			</div>
  		</div>
	</div>
	
	
	<div class="h4 pb-2 mb-4 text-danger border-bottom border-info"></div>
		
	<div class="card">
  		<div class="card-body">
			<table class="table">
			  <tbody>
			    <tr>
			      <td>PER</td>
			      <td th:text="${equity.per}"></td>
			      <td>EPS</td>
			      <td th:text="${equity.eps}"></td>
			    </tr>
			    <tr>
			      <td>PBR</td>
			      <td th:text="${equity.pbr}"></td>
			      <td>BPS</td>
			      <td th:text="${equity.bps}"></td>
			    </tr>
			  </tbody>
			</table>
  		</div>
	</div>
	
</div>
</body>
</html>