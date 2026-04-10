<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!-- Finance Main Page 지수 리스트 -->
<div class="row">
<c:forEach var="listVo" items="${listVo}" varStatus="loopStatus">
	<c:set var="price_color" value="${listVo.getStock_flag()}" />		<!-- Price Color  -->
	<c:set var="daysmaAnalysis" value="${listVo.getStock_sma()}" />		<!-- 평균갯수 -->	
	<c:set var="minsmaAnalysis" value="${listVo.getStock_30minsma()}" />	<!-- 평균갯수 -->	
	<div class="col-12 col-sm-6 col-md-3">
       	<div class="market-info-box">
      		<span class="dot-badge" title="Module Update"><!-- Analysis Update Time -->
				<i class="fa fa-fw fa-circle ${listVo.getModify_state()}"></i>
			</span>
           	<span class="market-info-box-icon cursor ${price_color}">
           		<strong onclick="fn_StockPrice('${listVo.getStock_code()}');">${listVo.getStock_ko_name()}</strong>
           	</span>
            <div class="market-info-box-content">
            	<span class="market-info-box-text">
            		<span class="pr-2 ${price_color}"><b>${listVo.getStock_close()}</b></span>
					<b class="text-sm ${price_color}">${listVo.getPriceDifference()}</b> 
					<b class="text-sm pl-2 ${price_color}">${listVo.getPricePercentage()}%</b>
            	</span>
            	<span class="market-info-box-number">
					${listVo.getStock_macd_signal()}
                </span>
                <span class="market-info-box-number mb-1">
					<b class="text-sm ${price_color}" title="30MIN">30분</b>
                	${listVo.getStock_30minsma()}
            		<b class="text-sm ${price_color}" title="day">일</b>
                	${listVo.getStock_sma()}
					<b class="text-sm ${price_color}" title="Month">월</b>
					<b class="text-pink pr-1" title="Month">
						<i class="fas fa-arrow-${listVo.getStock_mon()} text-xxs" aria-hidden="true"></i>
					</b>
					<b class="text-sm text-gray pr-1" title="CTZ">CTZ </b>
					<fmt:formatNumber value="${listVo.getStock_sma5_mon_price()}" pattern="#,###" var="formattedStockClose" />
					<span class="text-sm text-gray float-right">
					    <c:out value="${formattedStockClose}" />
					</span>
                </span>
            </div>
       </div>
    </div>
</c:forEach>
</div>