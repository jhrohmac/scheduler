<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<ul class="products-list product-list-in-card col" id="stock_list">
	<c:forEach var="listVo" items="${listVo}" varStatus="loopStatus">
	<c:set var="price_color" value="${listVo.getStock_flag()}" />		<!-- Price Color  -->
	<c:set var="avgcolor" value="${listVo.getStockAVG_flag()}" />		<!-- AVG Color  -->
	<c:set var="avgquantity" value="${listVo.getTotal_quantity()}" />	<!-- 평균갯수 -->
	<c:set var="smaAnalysis" value="${listVo.getStock_sma()}" />		<!-- 평균갯수 -->
	
	<li class="item p-1" id="accordion">
		<div class="product-img cursor mt-1">
			<span class="dot-badge" title="Module Update"><!-- Analysis Update Time -->
				<i class="fa fa-fw fa-circle ${listVo.getModify_state()}"></i>
			</span>
              	<img class='img-circle img-bordered-sm' title="${listVo.getStock_code()}" src="${listVo.getStock_url()}">
		</div>
		<div class="product-info ml-0" onclick="fn_StockPrice('${listVo.getStock_code()}')" 
				data-toggle="collapse" href="#collapse_${listVo.getStock_code()}">
			<a class="product-title cursor ml-2">${listVo.getStock_ko_name()}
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
				<c:forTokens items="${smaAnalysis}" delims="," var="item">
					${item}
				</c:forTokens>
				${listVo.getStock_macd_signal()}
				<span class="float-right pr-0"> 
					<c:choose>
						<c:when test="${close gt preclose}">
							<i class="fas fa-arrow-up text-xs pr-1 ${price_color}" aria-hidden="true"></i>
						</c:when>
						<c:when test="${close lt preclose}">
							<i class="fas fa-arrow-down text-xs pr-1 ${price_color}" aria-hidden="true"></i>
						</c:when>
					</c:choose> 
					<b class="text-sm ${price_color}">${listVo.getPriceDifference()}</b> 
					<b class="text-sm pl-2 ${price_color}">${listVo.getPricePercentage()}%</b>
					<button type="button" class="btn btn-tool btn-xs" data-toggle="collapse"
							href="#collapse_${listVo.getStock_code()}" title="더보기">
						<i class="fas fa-plus"></i>
                 		</button>
				</span>
			</a>
		</div>
		<!-- 더보기 옵션 -->
		<div id="collapse_${listVo.getStock_code()}" class="collapse" data-parent="#accordion">
               <div class="card-body">
               	<hr class="my-1">
                <span class="market-info-box-number m-0">
            		<b class="text-sm ${price_color}" title="30MIN">30분</b>
                	${listVo.getStock_30minsma()}
            		<b class="text-sm ${price_color}" title="day">일</b>
                	${listVo.getStock_sma()}
					<b class="text-sm ${price_color}" title="Month">월</b>
					<b class="text-pink" title="Month"><i class="fas fa-arrow-${listVo.getStock_mon()} text-xxs" aria-hidden="true"></i></b>
               	</span>
               	<hr class="my-1">
               	<span class="float-left pr-2">
               		<b class="text-sm">등록일자</b>
               		<b class="text-sm" title="등록일자">${listVo.getWatch_date()}</b>
               		<fmt:formatNumber value="${listVo.getWatch_close()}" pattern="#,###.##" var="watchClose" />
               		<b class="text-sm" title="등록가격">${watchClose}</b>
               	</span>
               	<span class="float-right pr-2">
					<button type="button" class="btn btn-danger btn-sm" 
							onclick="fn_interastStockEvent('D','${listVo.getStock_code()}')">
                   		<i class="far fa-trash-alt"></i>
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
						<b class="pl-2 ${avgcolor}">${listVo.getAvg_pricePercentage()}%</b>	<!-- 손익률 -->
						<b class="pl-2 ${avgcolor}">${listVo.getAvg_purchase_price()}</b>	<!-- 평단가 -->
					</span>
					<b>보유</b>
					<span class="text-green"><!-- 보유 수량 -->
						<b class="pl-2">${listVo.getTotal_quantity()}</b>
					</span>
				</a>
			</div>
		</c:if>
	</li>
	</c:forEach>
</ul>

<script>
$("#stock_list").sortable({
	placeholder : 'sort-placeholder',
	forcePlaceholderSize : true,
	start : function(e, ui) {
		ui.item.data('start-pos', ui.item.index() + 1);
	},
	stop : function(e, ui) {
		var list = [];
		ui.item.parent().find('li.item').each(
			function(idx, el) {
				var $this = $(el);
				 $this.find('.sort_code').text();
				 var value = $this.find('.sort_code').text(); 	// Retrieve the text value
				 list.push(value); 								// Add the value to the list array
			});
		var group_id = $('#interastGroup').val();
		var url = "/scheduler/finance/interastStockEvent.do";
		var param = "stock_code="+list+"&eventCode="+"U"+"&group_id="+group_id;
		var type = "json";
		fn_sendAjax(url, type, param, fn_StockSortResult);
		function fn_StockSortResult(data){
			fn_selectGroupStockList(group_id);	//그룹 종목 조회
		}
	}
});
</script>
