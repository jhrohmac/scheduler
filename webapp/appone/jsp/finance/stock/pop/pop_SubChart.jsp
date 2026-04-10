<%@page import="org.apache.poi.util.SystemOutLogger"%>
<%@ page contentType="text/html; charset=utf-8" %>

<%@ page import="org.apache.taglibs.standard.tag.rt.core.IfTag" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="/appone/plugins/system/js/jsLink.jsp" %>
<%@ include file="/appone/plugins/system/css/cssLink.jsp" %>

<!-- Theme style -->
<link rel="stylesheet" type="text/css"  href="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/css/gui.css">
<link rel="stylesheet" type="text/css"  href="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/css/popup.css">
 

<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/highstock.js"></script>

<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/indicators/indicators-all.js"></script>
<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/drag-panes.js"></script>

<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/annotations-advanced.js"></script>
<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/price-indicator.js"></script>
<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/full-screen.js"></script>

<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/stock-tools.js"></script>

<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/heikinashi.js"></script>
<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/hollowcandlestick.js"></script>
<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/accessibility.js"></script>


<script src="/scheduler/finance/stock/sub/chartScript.js"></script>
<script src="/scheduler/finance/stock/sub/financeScript.js"></script>
<link rel="stylesheet" href="/scheduler/finance/stock/sub/chartStyle.css">

<div class="card">
	<div class="card-header p-2">
		<div class="row">
			<div class="col-sm-8" id="stock_nav">
				<div class="row" style="font-size: 17.5px;" id="pop_stockInfo">
					<div class="col-sm-auto d-flex align-items-center justify-content-center">
						<h6 class="my-1 mt-1">
							<img class="img-size-32 img-circle img-bordered-sm" id="stock_logo" src="">
						</h6>
					</div>
					<div class="col-sm-auto">
						<h5 class="text-muted col-form-label"><strong id="stock_ko_name" class="review"></strong></h5>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label" id="stock_code"></h6>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label" id="stock_market"></h6>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label" id="stock_close"></h6>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label" id="stock_priceDifference"></h6>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label" id="stock_pricePercentage"></h6>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label">
						 	<span class="text-danger" id="stock_priceDifference"></span>						
						</h6>
					</div>
				</div>
			</div>
			<div class="col-sm-4">
				<div class="row float-right" style="font-size: 17.5px;" >
					<div class="col-sm-auto">
						<div class="card-tools">
							<div class="btn-group btn-sm">
			                  <select class="custom-select form-control-border" id="chartTime">
			                    <option value="minute-30">30분</option>
			                    <option value="day">Day</option>
			                    <option value="week">Week</option>
			                    <option value="month">Month</option>
			                  </select>
			                </div>
							<div class="btn-group btn-reflesh" id="Chart"></div>
							<div class="btn-group btn-dynmicOptions" id="SubChart"></div>
							<button type="button" class="btn btn-light btn-sm" data-card-widget="collapse">
								<i class="fas fa-minus"></i>
							</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="card-body p-0">
		<div class="tab-content">
			<div id="container" class="chart d-flex align-items-center justify-content-center"></div>
		</div>
	</div>
	<div id="modal_options"></div>
</div>
<script>

var urlParams = new URLSearchParams(window.location.search);
var chartObjStr = urlParams.get('chartObj');
var parentObj = JSON.parse(chartObjStr);
var infoData = parentObj.infoData;
var period = parentObj.period;

//Page Init Functions
$(document).ready(function(){
	fn_init();
});

function fn_init(){
	
	var close = infoData.close;
	var prevClose = infoData.prevClose;
	
	$("#stock_logo").attr("src", infoData.url);
	$("#stock_ko_name").text(infoData.name);
	$("#stock_code").text(infoData.code);
	$("#stock_market").text(infoData.market);
	$("#stock_close").text(infoData.close);
	$("#stock_priceDifference").text(infoData.priceDifference.toLocaleString());
	$("#stock_pricePercentage").text(infoData.pricePercentage);
	$("#chartTime").val(period);
	
	var color = "";
	if (close > prevClose) {
		color = "text-danger col-form-label";
	} else if (close < prevClose) {
		color = "text-primary col-form-label";
	} else {
		color = "text-muted col-form-label";
	}
	
	$("#stock_close").removeClass().addClass(color);
	$("#stock_priceDifference").removeClass().addClass(color);
	$("#stock_pricePercentage").removeClass().addClass(color);

	var chartObj = new Object();
	chartObj['chartNm'] = "SubChart";
	chartObj['period'] 	= period;
	chartObj['limit'] 	= "";
	chartObj['infoData'] = infoData;
	fn_Chart(chartObj);
};

$("#chartTime").change(function(){
	period = this.value;
	fn_init();
});

var dir = true;
var interval;
$("#10sec").click(function(){
	console.log(dir);
	if(dir){
		//주기적으로 데이터 가져오기 (예: 1분마다)
		interval = setInterval(fn_realStockPrice, 10 * 1000);  // 1분마다 업데이트
		dir = false;
	}else{
		dir = true;
		 clearInterval(interval);
	}
});

/* 주식 오늘 날짜 금액 정보 */
function fn_realStockPrice(){
	var stock_code = $("#stock_code").text();
	if(stock_code == undefined || stock_code == "" ){
		return false;
	}
	var url = "/scheduler/finance/realStockPrice.do";
	var param = "stock_code=" + stock_code;
	var type = "json";
	fn_sendAjax(url, type, param, fn_realResult);

	function fn_realResult(result) {
		setChart.series[0].addPoint(result["obj"]);
	}
}
</script>

<style type="text/css">
/* GENERAL */
.chart {
    float: left;
    max-height: 100%;
    height: 95vh;
    position: relative;
    width: 100%;
}
</style>