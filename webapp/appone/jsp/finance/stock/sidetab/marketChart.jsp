<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
	<div class="card my-0">
		<div class="card-header p-1">
			<div class="col-sm-12" id="stock_nav">
				<div class="row" style="font-size: 17.5px;" id="watch_info">
					<div class="col-sm-auto">
						<h5 class="text-muted col-form-label">
							<strong id="market_ko_name" class="review" style="cursor: pointer;"></strong>
						</h5>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label setcolor" id="market_close"></h6>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label setcolor" id="market_pricePercentage"></h6>
					</div>
					<div class="col-sm-auto">
						<h6 class="text-muted col-form-label setcolor">
						 	<span class="text-danger" id="market_priceDifference"></span>
							<i id="market_priceArrow"class='fas fa-arrow-up text-sm pl-1'></i>
						</h6>
					</div>
					<div class="col-sm-auto   d-flex align-items-center justify-content-center">
						<h6 class="text-muted col-form-label">
						 	<span class="text-${marketState}" id="marketState"><i class='fas fa-circle'></i></span>
						</h6>
					</div>
				</div>
			</div>
		</div>
		<!-- Market Chart -->
		<div class="card-body p-0">
			<div class="tab-content">
				<div id="marketContainer" class="chart d-flex align-items-center justify-content-center"></div>
			</div>
		</div>
	</div>
	<div class="card">
		<div class="card-body p-1">
			<ul class="products-list product-list-in-card col" id="market_list">
				<c:forEach var="listVo" items="${listVo}" varStatus="loopStatus">
					<c:set var="price_color" value="${listVo.getStock_flag()}" />		<!-- Price Color  -->
					<c:set var="avgcolor" value="${listVo.getStockAVG_flag()}" />		<!-- AVG Color  -->
					<c:set var="avgquantity" value="${listVo.getTotal_quantity()}" />	<!-- 평균갯수 -->
					<c:set var="smaAnalysis" value="${listVo.getStock_sma()}" />		<!-- 평균갯수 -->
					<c:if test="${loopStatus.first}">
						<c:set var="firstStock" value="'${listVo.getStock_code()}'" />
					</c:if>
					<li class="item p-1" id="accordion">
						<div class="product-img cursor mt-1">
							<span class="dot-badge" title="Module Update">
								<i class="fa fa-fw fa-circle ${listVo.getModify_state()}"></i>
							</span>
							<img class='img-circle img-bordered-sm' title="${listVo.getStock_code()}" src="${listVo.getStock_url()}">
						</div>
						<div class="product-info ml-0" onclick="fn_MarketDetails('${listVo.getStock_code()}')">
							<a class="product-title cursor ml-1">${listVo.getStock_ko_name()}
								<b class="sort_code text-xs text-gray">${listVo.getStock_code()}</b>
								<span class="float-right pr-2 ${price_color}">${listVo.getStock_close()}</span>
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
											href="#market_${listVo.getStock_code()}" title="더보기">
										<i class="fas fa-plus"></i>
			                  		</button>
								</span>
							</a>
						</div>
						<div id="market_${listVo.getStock_code()}" class="collapse" data-parent="#accordion">
		                    <hr class="my-1">
	                		<span class="progress-description">
		                 		<i class="fa-solid fa-clock-rotate-left"></i> ${listVo.getModify_date()}
		                	</span>
			            </div>
					</li>
				</c:forEach>
			</ul>
		</div>
	</div>

<script type="text/javascript">
var marketCode= ${firstStock};
fn_MarketDetails();
var setMarketChart;	//sma,flags,doublechart..을 넣기위해
var marketData;
//Stock 상세 정보
function fn_MarketDetails(stock_val){
	if(stock_val != undefined){
		marketCode= stock_val;
	}
	var priceObj = new Object();
		priceObj['stock_code'] = marketCode;
		priceObj['group_id'] = $("#interastGroup").val();
		priceObj['groupMarket'] = groupMarket;
	
	var url = "/scheduler/finance/selectStockPrice.do";
	var param = "priceObj=" + encodeURIComponent(JSON.stringify(priceObj))+"&market_div=true";
	var type = "json";
	fn_sendAjax(url, type, param, fn_MarketDetailsResult);
	function fn_MarketDetailsResult(stockInfo){
		marketData = stockInfo;
		fn_MarketStockInfo(stockInfo);
		fn_MarketChart(stockInfo);
	}
}
var marketPeriod;
var	marketObj;
function fn_MarketChart(stockInfo){
	var icon = '<h2><i class="fa-solid fa-spinner fa-spin-pulse" aria-hidden="true"></i></h2>';
	$("#marketContainer").html(icon);
	marketObj = new Object();
	marketObj['chartNm']	= "MarketChart";
	marketObj['period']	= "day";
	marketObj['limit']	= "";
	marketObj['infoData']	= stockInfo;
	marketPeriod = "day";
	var url = "/scheduler/finance/getChartData.do";
	var param = "chartObj=" + encodeURIComponent(JSON.stringify(marketObj));
	var type = "json";
	fn_sendAjax(url, type, param, fn_MarketChartResult);
	function fn_MarketChartResult(result) {
		var data = result["data"];
		var smaMarketFlag ="N";
		var macdMarketFlag ="N";
		var doubleChartMarketFlag ="N";
		var code = marketData.code;
		var name = marketData.name;
		//console.log(data.length);
		//console.log(data[data.length-1][4]);
		var close_price = marketData.close.replace(/,/gi, "");	//금일 종가 라인
		var avg_purchase_price = marketData.avg_purchase_price;	//평단가 라인
		
		var chartOptions = result["chartOptions"];
		for(var d=0; d < chartOptions.length; d++){
			var code_nm = chartOptions[d].code_nm;
			var code_value = chartOptions[d].code_value;
			if(code_nm == "F-SMA" && code_value == 'Y'){
				smaMarketFlag = "Y";
			}
			if(code_nm == "F-MACD" && code_value == 'Y'){
				macdMarketFlag = "Y";
			}
			if(code_nm == "F-DOUBLECHART" && code_value == 'Y'){
				doubleChartMarketFlag = "Y";
			}
		}
		
		// 호출된 page의 Chart ID를 가져와 표시
		setMarketChart = Highcharts.stockChart('marketContainer', {
			plotOptions: {
				candlestick: {
					upColor: 'red',		//Up Color
					upLineColor: 'red',	//Up Line Color
					color: '#3496ff',	//Down Color
					lineColor: '#3496ff',
					dataGrouping: {
						enabled: false // Disable automatic data grouping for candlestick series
					}
				},
				//flags 표시
				flags: {
	                accessibility: {
	                    exposeAsGroupOnly: true
	                }
            	}
			},
			// Chart Default 기간 Set 버튼 리스트
			rangeSelector: {
				enabled: true,
				inputEnabled: false,
				buttons: [{
					type:   marketPeriod === 'month' || marketPeriod === 'week' ? 'year' : (marketPeriod === 'day' ? 'month' : 'day'),
					count:  marketPeriod === 'month' ? '12' : marketPeriod === 'week' ? '4' : (marketPeriod === 'day' ? '3' : '20'),
					text:   marketPeriod === 'month' ? '12Y': marketPeriod === 'week' ? '4Y' : (marketPeriod === 'day' ? '3M' : '20D')
				}, {
					type: 'ytd',
					text: '올해'
				}],
				selected: 0
			},
			//네비게이트
			navigator: {
				enabled: false
			},
	        xAxis: 
	        [{
				// 크로스
				crosshair: {
					width: 1,
					color: 'black',
					dashStyle: 'dash'
				},
				labels: {
					formatter: function() {
						return Highcharts.dateFormat('%Y.%m.%d', this.value);
					}
				},
				//일자 구분선
	            plotLines: (function () {
	               	const plotLines = [];
       				let currentPeriod = -1;
			        for (let i = 0; i < data.length; i++) {
			            const currentDate = new Date(data[i][0]);
			            let periodValue;
			            
						if (marketPeriod.indexOf('minute') != -1) {
							//periodValue = currentDate.getUTCDate();
							periodValue = currentDate.getUTCMonth();
			            }else if (marketPeriod === 'day') {
			                periodValue = currentDate.getUTCMonth();
			            } else if (marketPeriod === 'month' || marketPeriod === 'week') {
			                periodValue = currentDate.getUTCFullYear();
			            }
			
			            if (periodValue !== currentPeriod) {
			                plotLines.push({
			                    color: 'rgba(0, 0, 0, 0.3)',
			                    width: 1,
			                    value: data[i][0],
			                    dashStyle: 'dash',
			                    zIndex: 5,
			                    label: {		//날짜 구분선
			                        text: Highcharts.dateFormat(marketPeriod === 'month' || marketPeriod === 'week' ? '%Y' : (marketPeriod === 'day' ? '%m' : '%m'), data[i][0]),
			                        rotation: 0,
			                        textAlign: 'left'/*,
			                        style: {
			                            fontStyle: 'italic'
			                        }*/
			                    }
			                });
			                currentPeriod = periodValue;
			            }
			        }
	                return plotLines;
	            })()
	        }],
			yAxis: [{
				// 크로스
				crosshair: {
					width: 1,
					color: 'black',
					dashStyle: 'dash'
				},
				labels: {
					align: 'left',
					formatter: function() {	//금액 포멧
						return Highcharts.numberFormat(this.value, 0, '.', ',');
					}
				},
				height: "100%",
				resize: {
					enabled: true
				},
				plotLines: [{//매수 평단가 라인
                             value: avg_purchase_price,
                             color: 'red',
                             dashStyle: 'shortdash',
                             width: 1.5,
                             label: {
                                 text:Highcharts.numberFormat(avg_purchase_price, 0, '.', ','),
                                 align: 'center'
                             }
                           },
                           {	//금일 종가 라인
                           	 value: close_price,
                             color: 'gray',
                             dashStyle: 'shortdash',
                             width: 1.5,
                             label: {
                                 text:Highcharts.numberFormat(close_price, 0, '.', ','),
                                 align: 'center'
                             }
                            }]
			}],
			series: [{
				type: 'candlestick',
				id: code,
				name: name, //$("#watch_ko_name").text(),
				data: data
			}],
        	tooltip: {
				shape: 'square',
	            headerShape: 'callout',
	            borderWidth: 0,
	            shadow: false,
				formatter: function() {
					if (this.points !== undefined) {
						var point = this.points[0]; // Assuming candlestick series is the first series
						if (point.x !== undefined && point.x !== null) {
							var date = Highcharts.dateFormat('%Y.%m.%d', point.x);
							var open = point.point.open !== undefined && point.point.open !== null ? point.point.open.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '';
							var high = point.point.high !== undefined && point.point.high !== null ? point.point.high.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '';
							var low = point.point.low !== undefined && point.point.low !== null ? point.point.low.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '';
							var close = point.point.close !== undefined && point.point.close !== null ? point.point.close.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') : '';
						
							var tooltipContent = '<div class="comment-text">' +
									'<br><span class="username">' + date + '</span></br>' +
									'종가    <span>' + close + '</span><br>' +
									'시가    <span>' + open + '</span><br>' +
									'고가    <span>' + high + '</span><br>' +
									'저가    <span>' + low + '</span><br>' +
									'</div>';
							return tooltipContent;
						}
					}else if(this.point !== undefined){
						var point = this.point;
						var date = Highcharts.dateFormat('%Y.%m.%d', point.x);
						var title = point.title;
						var text = point.text;
					
						var tooltipContent = '<div class="comment-text">' +
									'<br><span class="username">' + date + '</span></br>' +
									'TITLE    <span>' + title + '</span><br>' +
									'TEXT    <span>' + text + '</span><br>' +
									'</div>';
							return tooltipContent;
					}
				},
				positioner: function (width, height, point) {
	                var chart = this.chart,
	                    position;
	
	                if (point.isHeader) {
	                    position = {
	                        x: Math.max(
	                            // Left side limit
	                            chart.plotLeft,
	                            Math.min(
	                                point.plotX + chart.plotLeft - width / 2,
	                                // Right side limit
	                                chart.chartWidth - width - chart.marginRight
	                            )
	                        ),
	                        y: point.plotY
	                    };
	                } else {
	                    position = {
	                        x: (point.series.chart.plotLeft !== undefined)?point.series.chart.plotLeft : "",
	                        y: point.series.yAxis.top - chart.plotTop
	                    };
	                }
	                return position;
	            }
			}
		});
		
		if(smaMarketFlag == 'Y'){
			fn_addMarketSMA(data);
		}
		if(doubleChartMarketFlag == 'Y'){
			//일봉 차트 시작과 끝으로 월봉 갯수 구하기
			const monthsDifference = calculateMonthDifference(data[0][0], data[data.length-1][0]);
			fn_addMarketMonthChart(monthsDifference);
		}
		if(result["flag"] != undefined){
			fn_addMarketFLAGS(result["flag"])
		}
		if(result["macdosc"] != undefined ){
			fn_addMarketMACDOSC(result["macdosc"])
		}
	}
};

function fn_addMarketSMA(data){
	//var code = marketData.code;
	// Define SMA periods and their corresponding colors
    const smaPeriods = [5, 20, 60, 120, 240];
    const smaColors = ['black', 'red', 'green', 'skyblue', '#EB3CDA'];
    const smaLines = [2, 2, 2, 2, 2];
    // Loop through SMA periods and create series
    for (let i = 0; i < smaPeriods.length; i++) {
        const smaPeriod = smaPeriods[i];
        const smaColor = smaColors[i];
        const smaLine = smaLines[i];

        setMarketChart.addSeries({
            type: 'sma',
            linkedTo: marketData.code,
            yAxis: 0,
            name: `SMA ${smaPeriod}`,
            color: smaColor,
            data: [data], // Placeholder for actual data from the database
            params: {
                period: smaPeriod,
            },
            marker: {
                enabled: false,
            },
            lineWidth: smaLine,
            states: {
                hover: {
                    lineWidth: 2,
                },
            },
            dataGrouping: {
                enabled: (marketPeriod.includes('minute')) ? true : false,
            },
            enableMouseTracking: false,
            tooltip: {
                pointFormatter: function () {
                    return false;
                }
            }
        });
    }
};

//MACDOSC 매수 매도 구간 라인
function fn_addMarketMACDOSC(data){
	const item = data // three green lines should be plotted on x-axis
	for (let i = 0; i < item.length; i++) {
		setMarketChart.xAxis[0].addPlotLine({
			customTooltip: false,
			type: 'line',
			value: item[i][0],
			id: item[i][1],
			color: (item[i][1] == "BUY") ? '#e83e8c57' : '#3498db57',
			width: 3,
			zIndex: 2
		});
	}
};

function fn_addMarketFLAGS(data){
    setMarketChart.addSeries({
            type: 'flags',
            data: data,
            onSeries: marketData.code,
            width: 30,
            y: -100,
            lineWidth: 1.5,
            dashStyle: 'dash',
    		//shape: "url(http://www.highcharts.com/demo/gfx/sun.png)",
            shape: 'squarepin',
            enableMouseTracking: false,
            color: '#0263cb',		//Highcharts.getOptions().colors[0], // same as onSeries
            fillColor:'#0263cb',	// Highcharts.getOptions().colors[0],
            style: { 			
                color: 'white'		// text style
            },
            states: {
                hover: {
                    fillColor: '#0263cb' // darker
                }
            }
        }
    )
};

//월봉 더블 차트
function fn_addMarketMonthChart(monthsDifference){
	//월봉 더블 챠트 제거
	if(marketPeriod =="day"){
		marketObj['period']	= "month";
		marketObj['limit']	= monthsDifference;
		
		var url = "/scheduler/finance/getChartData.do";
		var param = "chartObj=" + encodeURIComponent(JSON.stringify(marketObj));
		var type = "json";
		fn_sendAjax(url, type, param, fn_addMonthChartResult);
		function fn_addMonthChartResult(result) {
			setMarketChart.addSeries({
				type: 'candlestick',
				linkedTo: marketData.code,		//CODE
				data: result["data"],
	        	upColor: 		'#e83e8c6e', 	//Up Color
	        	upLineColor: 	'#e83e8c6e',	//Up Line Color
				color: 			'#3498db6e', 	//Down Color
	        	lineColor: 		'#3498db6e', 	//Down Line Color
	        	pointWidth: 60		//CandleStick Width (point)
				,lineWidth: 2		//CandleStick Line Width (point)
			});
		}
	}
}

/* Link System Display */
function fn_MarketStockInfo(data) {
	var close = data.close;			//금일 종가
	var prevClose = data.prevClose;	//전일 종가
	$("#market_ko_name").text(data.name);
	// attr() 메서드를 사용하여 src 속성에 변수 값 설정
	$("#market_close").text(data.close);
	$("#market_pricePercentage").text(data.pricePercentage);
	$("#market_priceDifference").text(data.priceDifference);

	var color = "";
	var arrow = "";
	if (close > prevClose) {
		arrow = "fas fa-arrow-up text-sm pl-1";
		color = "text-danger col-form-label setcolor";
	} else if (close < prevClose) {
		arrow = "fas fa-arrow-down text-sm pl-1";
		color = "text-primary col-form-label setcolor";
	} else {
		color = "text-muted col-form-label setcolor";
	}
	$("#market_priceArrow").removeClass().addClass(arrow);
	$("#market_close").removeClass().addClass(color);
	$("#market_pricePercentage").removeClass().addClass(color);
	$("#market_priceDifference").removeClass().addClass(color);

	var priceArrow_color = "gray";
	if (close > prevClose) {
		priceArrow_color = "red";
	} else if (close < prevClose) {
		priceArrow_color = "blue";
	}
	$("#market_priceArrow").css("color",priceArrow_color);
}

//일봉 차트 시작과 끝으로 월봉 갯수 구하기
function calculateMonthDifference(fromTimestamp, endTimestamp) {
    const from = new Date(fromTimestamp);
    const end = new Date(endTimestamp);
    const monthsDifference = (end.getFullYear() - from.getFullYear()) * 12 + (end.getMonth() - from.getMonth());
    return monthsDifference;
}

$("#market_ko_name").click(function(){
	fn_StockPrice(marketCode);
});


</script>
