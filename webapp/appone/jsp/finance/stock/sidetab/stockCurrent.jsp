<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card" id="priceSetting">
	<div class="card-body p-1">
		<div class="row">
			<div class="col-4">
				<div class="form-group">
                	<label>수량</label>
	                <input type="text" class="form-control is-warning" id="priceCount" placeholder="수량(주)">
	            </div>
			</div>
			<div class="col-8">
				<div class="form-group">
					<label>주문금액</label>
                	<input type="text" class="form-control is-warning" id="totalOrderPrice" value="" placeholder="주문금액">
	            </div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-6">
				<!-- text input -->
				<div class="form-group">
					<label>보통가격</label> 
					<input type="text" class="form-control" id="startprice" value="" placeholder="보통가격">
				</div>
			</div>
			<div class="col-sm-6">
				<div class="form-group">
				<c:set var="switch" value="${groupMarket}"></c:set>
				<c:choose>
					<c:when test="${groupMarket eq true}">
						<label>환율가격</label> 
						<input type="text" class="form-control" id="bidRate" value="" placeholder="환율가격">
					</c:when>
					<c:when test="${groupMarket eq false}">
						<label>목표가격</label> 
						<input type="text" class="form-control" id="endprice" value="" placeholder="목표가격">
					</c:when>
				</c:choose>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<div class="form-group">
                	<label>Description</label>
                  	<textarea class="form-control" rows="1" id="price_description" placeholder="Stock Desc.."></textarea>
            	</div>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<div id="slider-range"></div>
			</div>
		</div>
	</div>
	<div class="card-footer p-1">
		<button type="button" id="BUY" class="btn btn-danger submit">BUY</button>
		<button type="button" id="SELL"class="btn btn-primary submit">SELL</button>
		<button type="button" id="MIX"class="btn btn-primary submit">물타기</button>
	</div>
</div>
<div class="card">
	<div class="card-header border-0 p-1 ">
		<h3 class="card-title text-lg ">
			<span title="손익" class="badge badge-gray">손익</span>
			<span title="stock price roi" class="badge badge-primary" id="stock_price_roi"></span>
			<span title="avg_pricePercentage" class="badge badge-primary" id="avg_pricePercentage"></span>
			<i id="avg_price_priceArrow"class='fas fa-arrow-up text-sm'></i>
			<span title="총수량" class="badge badge-gray">수량</span>
			<span title="Total Quantity" class="text-green" id="total_quantity"></span>
			<span title="평가금액" class="badge badge-gray">평가</span>
			<span title="Total Purchase Price" class="badge badge-primary" id="total_purchase_price"></span>
		</h3>
		<div class="card-tools p-1 mr-1">
			<button type="button" class="btn btn-light btn-sm c_close" title="close">
				<i class="fas fa-angle-double-down"></i>
			</button>
			<button type="button" class="btn btn-light btn-sm" data-card-widget="collapse">
				<i class="fas fa-minus"></i>
			</button>
		</div>
	</div> 
	<div class="card-body p-1">
		<table class="table table-sm" id="priceGridTable">
			<colgroup>
			<col width="10%">
			<col width="20%">
			<col width="10%">
			<col width="30%">
			<col width="10%">
			<col width="10%">
			<col width="10%">
			</colgroup>
			<thead>
				<tr>
					<th>No.</th>
					<th>DATE</th>
					<th>수량</th>
					<th>주문금액</th>
					<th>구분</th>
					<th></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</div>
</div> 
<script>
	fn_StockCurrentPriceSet();
	function fn_StockCurrentPriceSet(){
		var price = 0;
		var step = 100;
		let minPrice;
		let maxPrice;
		
		if(infoData != undefined ){
			$("#price_name").text(infoData.name);
			if(infoData.country_code == "KR"){
				step = 100;
			}else{
				step = 1;
			}
			price = infoData.close.replace(",", "");
		}
		// 주어진 가격을 숫자로 변환
		var numericPrice = parseFloat(price);
		$("#priceCount").val("1");
		$("#totalOrderPrice").val(numericPrice.toLocaleString());
		
		//30% 하락한 가격 계산
		var decrease30Percent = numericPrice - (numericPrice * 0.3);
	//	minPrice = parseFloat(decrease30Percent.toFixed(0)); // 숫자를 문자열로 변환하고 천 단위로 콤마를 추가하여 저장
		minPrice = (Math.floor(decrease30Percent / 100) * 100); // 숫자를 100 단위로 내림하고 천 단위로 콤마를 추가하여 저장
	
		//30% 상승한 가격 계산
		var increase30Percent = numericPrice + (numericPrice * 0.3);
		//maxPrice = parseFloat(increase30Percent.toFixed(0)); // 숫자를 문자열로 변환하고 천 단위로 콤마를 추가하여 저장
		maxPrice = (Math.floor(increase30Percent / 100) * 100); // 숫자를 100 단위로 내림하고 천 단위로 콤마를 추가하여 저장
	
		//10% minPrice 가격 계산
		var startprice = parseFloat(minPrice - (minPrice * 0.1));
		startprice = (Math.floor(startprice / 100) * 100); 
		//10% maxPrice 가격 계산
		var endprice = parseFloat(maxPrice + (maxPrice * 0.1));
		endprice = (Math.floor(endprice / 100) * 100);
		
		$("#slider-range").slider({
			range : true,
			min : startprice,
			max : endprice,
			step: step,
			values : [ price , maxPrice ],
			slide : function(event, ui) {
				$("#startprice").val(ui.values[0].toLocaleString());
				$("#endprice").val(ui.values[1].toLocaleString());
				fn_totalOrderPrice();
			}
		});
		$("#startprice").val($("#slider-range").slider("values", 0).toLocaleString());
		$("#endprice").val($("#slider-range").slider("values", 1).toLocaleString());
		fn_priceGridTable();
	}
	
	$("#priceCount,#startprice").change(function() {
		fn_totalOrderPrice();
	});
	function fn_totalOrderPrice(){
		var pricecount = parseFloat($("#priceCount").val());
		var startprice = parseFloat($("#startprice").val().replace(",", ""));
		var totalOrderPrice = (startprice * pricecount).toLocaleString();
		$("#totalOrderPrice").val(totalOrderPrice);	
	}
	
	function fn_priceGridTable() {
		var close =""
		var code =""
		var group_id =""
		if(infoData != undefined ){
			close = infoData.close
			code = infoData.code
			group_id = infoData.group_id
		}
		// 호출 URL
		var url = "/scheduler/finance/selectPriceHistory.do";
		//조회 조건
		var param = {
	                "price" 		: close,
	                "stock_code" 	: code,
	                "group_id" 		: group_id
				};
		//컬럼 옵션
		var columns = [
						{"data": "stock_seq"},
						{"data": "create_date"},
						{"data": "quantity",
							"render": function(data, type, row){
								return data.toLocaleString(); // 기본 로케일에 따라 천 단위;
							}
						},
						{"data": "purchase_price",
							"render": function(data, type, row){
								return data.toLocaleString(); // 기본 로케일에 따라 천 단위;
							}
						},
						{"data": "transaction_type",
							"render": function(data, type, row){
								var color ="bg-red";
								if(data =="SELL"){
									color ="bg-primary";
								}
								data ='<span class="badge '+color+'">'+data+'</span>';
								return data;
							}
						},{"data": "transaction_type",
								"render": function(data, type, row){
									data = '<span class="btn btn-sm bg-red"><i class="fas fa-pencil"></i></span>';
								return data;
							}
						},{"data": "transaction_type",
							"render": function(data, type, row){
								data = '<span class="btn btn-sm bg-red"><i class="fas fa-trash"></i></span>';
							return data;
						}
						}
		           ];
		var columnDefs = [
							{
							    "targets": [0,1,2],
							    "className": "text-center",
							}
						];
		var order = [[1, 'desc']];
		
		var gridObj = {
		        'grid_id': "priceGridTable",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': false
		};
	
		var gridOptions = {
		        'serverSide': true,
		        'searching': false,
		        'paging': true,
		        'button': false,
		        'lengthChange': false,
		        'info': true,
		        'autoWidth': false,
		        'responsive': true,
		        'bDestroy': true,
		        'processing': true,
		        'ordering': false,
		        'cursorCols': [2,3,4]
	    };
		dataTableGrid(gridObj, gridOptions);
	}
	
	var eventCode = "I";
	$('#priceGridTable tbody').on( 'click', 'tr', function () {
		var priceGridTable = $("#priceGridTable").dataTable();
	   	var row_position = priceGridTable.fnGetPosition(this);
   		var row_data = priceGridTable.fnGetData(row_position);
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		console.log(row_data);
		//수정버튼
		if(column_index == '5'){
			eventCode = "U";
			$("#BUY").text("수정");
			$("#SELL").text("삭제");
		}else if(column_index == '6'){
			eventCode = "D";
		}else{
			eventCode = "I";
			$("#BUY").text("BUY");
			$("#SELL").text("SELL");
		}
		
		var chartObj = new Object();
			chartObj['containerName']	= "container";
			chartObj['stock_seq']	= row_data.stock_seq;
			chartObj['quantity']	= row_data.quantity;
			chartObj['stock_code']	= row_data.stock_code;
			chartObj['create_date']	= row_data.create_date;
			chartObj['stock_group']	= row_data.stock_group;
			chartObj['purchase_price']	= row_data.purchase_price;
			chartObj['eventCode']	= eventCode;
			chartObj['infoData']	= infoData;
			if(eventCode=="D"){
				fn_priceHistoryEvent(chartObj);
			}
	});
	//Stock 구매 종료
	$(".c_close").click(function(){
	   if (!confirm("구매 완료하시겠습니까?")) {
	       return;
	   };
	   
		var chartObj = new Object();
		chartObj['eventCode']	= "C";
		chartObj['stock_seq']	= "";
		chartObj['create_date']	= "";
		chartObj['purchase_price']	= "";
		chartObj['infoData']	= infoData;
		fn_priceHistoryEvent(chartObj);
	});
	
	function fn_priceHistoryEvent(chartObj){
		var url = "/scheduler/finance/priceHistoryEvent.do";
		var param = "chartObj=" + encodeURIComponent(JSON.stringify(chartObj));
		var type = "json";
		fn_sendAjax(url, type, param, fn_priceGridTable);
	}
	
	$(".submit").click(function(event) {
		var priceSetting = $("#priceSetting");
		var group_id = $("#interastGroup").val();
		var code = infoData.code;
		if(group_id == null){
			alert("그룹 ID가 잘못되었습니다.");
			return false;
		}
		if(code == null){
			alert("코드가 잘못되었습니다.");
			return false;
		}
		var url = "/scheduler/finance/priceSettingEvent.do";
		var param = getJQParams(priceSetting) + "&eventCode="+eventCode+  "&order_div=" + this.id+ "&group_id=" + group_id+ "&stock_code=" + code;
		var type = "json";
		fn_sendAjax(url, type, param, fn_priceGridTable);
	});
	
	fn_PriceCalculate();
	function fn_PriceCalculate(){
		var chartObj = new Object();
		chartObj['infoData']	= infoData;
		var url = "/scheduler/finance/selectPriceCalculate.do";
		var param = "chartObj=" + encodeURIComponent(JSON.stringify(chartObj));
		var type = "json";
		fn_sendAjax(url, type, param, fn_PriceCalculateResult);
		function fn_PriceCalculateResult(data){
			var avg_purchase_price = data.avg_purchase_price;
			var avg_pricePercentage = data.avg_pricePercentage;
			var stock_price_roi = data.stock_price_roi;
			var quantity = data.total_quantity;
			var purchase_price = data.total_purchase_price;

			var color = "";
			var arrow = "";
			var close = infoData.close;
			if (close > avg_purchase_price) {
				arrow = "fas fa-arrow-up text-sm text-danger";
				color = "text-danger";
			} else if (close < avg_purchase_price) {
				arrow = "fas fa-arrow-down text-sm text-primary";
				color = "text-primary";
			} else {
				arrow = "text-sm  text-gray";
				color = "text-muted";
			}
			$("#avg_price_priceArrow").removeClass().addClass(arrow);
			$("#avg_pricePercentage").text(avg_pricePercentage).removeClass().addClass(color);
			$("#stock_price_roi").text(stock_price_roi).removeClass().addClass(color);
			$("#total_quantity").text(quantity);
			$("#total_purchase_price").text(purchase_price).removeClass().addClass(color);
		}
	}

</script>