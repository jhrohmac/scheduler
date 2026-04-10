<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<ul class="products-list product-list-in-card col" id="analysis_list">
	<c:forEach var="listVo" items="${listVo}" varStatus="loopStatus">
		<c:set var="price_color" value="${listVo.getStock_flag()}" />		<!-- Price Color  -->
		<c:set var="avgcolor" value="${listVo.getStockAVG_flag()}" />		<!-- AVG Color  -->
		<c:set var="avgquantity" value="${listVo.getTotal_quantity()}" />	<!-- 평균갯수 -->
		<c:set var="smaAnalysis" value="${listVo.getStock_sma()}" />		<!-- 평균갯수 -->
		<li class="item p-1" id="accordion">
			<div class="product-img cursor mt-1">
				<span class="dot-badge" title="Module Update">
					<i class="fa fa-fw fa-circle ${listVo.getModify_state()}"></i>
				</span>
				<img class='img-circle img-bordered-sm' title="${listVo.getStock_code()}" src="${listVo.getStock_url()}">
			</div>
			<div class="product-info ml-0" onclick="fn_StockPrice('${listVo.getStock_code()}')"
				data-toggle="collapse" href="#analysis_${listVo.getStock_code()}">
				<a class="product-title cursor ml-1">${listVo.getStock_ko_name()}
					<b class="sort_code text-xs text-gray">${listVo.getStock_code()}</b>
					<c:set var="stockClose" value="${listVo.getStock_close()}" />
					<fmt:formatNumber value="${stockClose}" pattern="#,###.##" var="formattedStockClose" />
					<span class="float-right pr-2 ${price_color}">
					    <c:out value="${formattedStockClose}" />
					</span>
					<%-- <span class="float-right pr-2 ${price_color}">${listVo.getStock_close()}</span> --%>
				</a> 
			</div>
			<div class="product-info ml-1">
				<a class="product-description">
					<c:forTokens items="${smaAnalysis}" delims="," var="item">
						${item}
					</c:forTokens>
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
						<b class="text-sm pl-2 ${price_color}">${listVo.getPricePercentage()}%</b>
						<button type="button" class="btn btn-tool btn-xs" data-toggle="collapse"
								href="#analysis_${listVo.getStock_code()}" title="더보기">
							<i class="fas fa-plus"></i>
                  		</button>
					</span>
				</a>
			</div>
			
			<div id="analysis_${listVo.getStock_code()}" class="collapse" data-parent="#accordion">
                <div class="card-body p-1">
					<hr class="my-1">
	                 <span class="market-info-box-number m-0">
	            		<b class="text-sm ${price_color}" title="30MIN">30분</b>
	                	${listVo.getStock_30minsma()}
	            		<b class="text-sm ${price_color}" title="day">일</b>
	                	${listVo.getStock_sma()}
						<b class="text-sm ${price_color}" title="Month">월</b>
						<b class="text-pink" title="Month"><i class="fas fa-arrow-${listVo.getStock_mon()} text-xxs" aria-hidden="true"></i></b>
                	</span>
                	<div class="float-right pr-2">
						<div class="btn-group">
                       		<button type="button" class="btn btn-default btn-xs dropdown-toggle dropdown-icon" data-toggle="dropdown">
                       			<i class="fa fa-heart text-warning" aria-hidden="true"></i>
                       		</button>
                        	<div class="dropdown-menu p-1">
								<a class="btn dropdown-item text-sm p-1"
									onclick="fn_interastStockEvent('I','${listVo.getStock_code()}','')">일반</a>
								<a class="btn dropdown-item text-sm p-1"
									onclick="fn_interastStockEvent('I','${listVo.getStock_code()}','close')">종가</a>
								<a class="btn dropdown-item text-sm p-1"
									onclick="fn_interastStockEvent('I','${listVo.getStock_code()}','month')">월말</a>
                        	</div>
                       	</div>
                        <button type="button" class="btn btn-danger btn-sm" 
							onclick="fn_delAnalysis('${listVo.getStock_code()}','${listVo.getStock_ko_name()}')">
                    		<i class="far fa-trash-alt"></i>
                  		</button>
                 	</div>
                </div>
            </div>
		</li>
	</c:forEach>
</ul>
