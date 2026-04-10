<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<ul class="products-list product-list-in-card col">
	<c:forEach var="listVo" items="${listVo}" varStatus="loopStatus">
		<c:set var="price_color" value="${listVo.getStock_flag()}" />		<!-- Price Color  -->
		<c:set var="avgcolor" value="${listVo.getStockAVG_flag()}" />		<!-- AVG Color  -->
		<c:set var="avgquantity" value="${listVo.getTotal_quantity()}" />	<!-- 평균갯수 -->
		
		<li class="item p-1 cursor" id="accordion"  onclick="fn_StockPrice('${listVo.getStock_code()}')">
			<div class="product-img mt-1">
				<span class="dot-badge" title="Module Update">
					<i class="fa fa-fw fa-circle ${listVo.getModify_state()}"></i>
				</span>
				<img class='img-circle img-bordered-sm' title="${listVo.getStock_code()}" src="${listVo.getStock_url()}">
			</div>
			<div class="product-info ml-0">
				<a class="product-title cursor ml-1">${listVo.getStock_ko_name()}
					<b class="sort_code text-xs text-gray">${listVo.getStock_code()}</b>
					<c:set var="stockClose" value="${listVo.getStock_close()}" />
					<fmt:formatNumber value="${stockClose}" pattern="#,###.##" var="formattedStockClose" />
					<span class="float-right pr-2 ${price_color}">
					    <c:out value="${formattedStockClose}" />
					</span>
				</a>
			</div>
			<div class="product-info ml-1">
				<a class="product-description">
					${listVo.getStock_macd_signal()}
					<span class="float-right pr-2"> 
						<c:choose>
							<c:when test="${close gt preclose}">
								<i class="fas fa-arrow-up text-sm pr-1 ${price_color}" aria-hidden="true"></i>
							</c:when>
							<c:when test="${close lt preclose}">
								<i class="fas fa-arrow-down text-sm pr-1 ${price_color}" aria-hidden="true"></i>
							</c:when>
						</c:choose> 
						<b class="text-sm ${price_color}">${listVo.getPriceDifference()}</b> 
						<b class="text-sm pl-2 ${price_color}">${listVo.getPricePercentage()}</b>
						<button type="button" class="btn btn-tool btn-xs" data-toggle="collapse"
								href="#portfolio_${listVo.getStock_code()}" title="더보기">
							<i class="fas fa-plus"></i>
                  		</button>
					</span>
				</a>
			</div>
			<div id="portfolio_${listVo.getStock_code()}" class="collapse" data-parent="#accordion">
                <div class="card-body p-1">
					<hr class="my-1">
					<span class="float-right pr-2">
						<button type="button" class="btn btn-default btn-sm" 
								onclick="fn_interastStockEvent('I','${listVo.getStock_code()}')">
                    		<i class="fa fa-heart text-warning" aria-hidden="true"></i>
                  		</button>
					</span>
                </div>
            </div>
			<c:if test="${avgquantity gt 0}">
	            <hr class="my-1">
				<div class="product-info ml-1 d-flex align-items-center">
					<!-- 구매 가격이 있으면 view  -->
	            	<a class="product-description text-sm">
	            		<b>수익</b>
	            		<span class="pr-2"> 
							<b class="pl-2 ${avgcolor}">${listVo.getStock_price_roi()}</b>		<!-- 손익 금액 -->
							<b class="pl-2 ${avgcolor}">${listVo.getAvg_pricePercentage()}%</b> <!-- 손익률 -->
							<b class="pl-2 ${avgcolor}">${listVo.getAvg_purchase_price()}</b>	<!-- 평단가 -->
						</span>
						<b>보유</b>
						<span class="text-green"><!-- 보유 수량 -->
							<b class="pl-2">${listVo.getTotal_quantity()}</b>
						</span>
						<b>총금액</b>
						<span class="${avgcolor}"><!-- 총금액 -->
							<b class="pl-2">${listVo.getTotal_purchase_price()}</b>
						</span>
					</a>
				</div>
			</c:if>
		</li>
	</c:forEach>
</ul>
