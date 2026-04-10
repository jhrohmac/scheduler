/************************************************************************
*  Title        : 공통 자바스크립트 모음 ( 취합 )

*  Description  :  구성
*       1)팝업 관련 Method
*       2)폼 관련 Method
*       3)Util Method( Date, String, Etc...)
*  Copyright    : Copyright (c) 2003.03.02
*  Company      : Soft on mobile
*  사용환경       : Explorer 5.5 이상 최적
*  @author	    : feelhouse@orgio.net
*  @version     : 0.9
*************************************************************************/

//전역변수 리스트
var period;
var infoData;	//Stock 기준정보
var setChart;	//sma,flags,doublechart..을 넣기위해

/* 주식 오늘 날짜 금액 정보 */
function fn_StockPrice(stock_code){
	if(stock_code == undefined){
		return false;
	}
	period = "day";
	var priceObj = new Object();
		priceObj['stock_code'] = stock_code;
		priceObj['group_id'] = $("#interastGroup").val();
		priceObj['groupMarket'] = groupMarket;
	
	var url = "/scheduler/finance/selectStockPrice.do";
	var param = "priceObj=" + encodeURIComponent(JSON.stringify(priceObj));
	var type = "json";
	fn_sendAjax(url, type, param, fn_StockPriceResult);
	function fn_StockPriceResult(result) {
		infoData = result;		
		fn_setStockInfo(result);
		fn_Chart();
		
		// 열려있는 tab 조회
		const activeNavLink = $(".nav-link.active");
		var activetab = activeNavLink.attr("id");
		// Find the active nav-link
		if(activetab == "stockCurrent"){
	  		fn_watchtab(activetab);
		}
	}
};
var	chartObj;
function fn_Chart(variableObj){
	var icon = '<h2><i class="fa-solid fa-spinner fa-spin-pulse" aria-hidden="true"></i></h2>';
	$("#container").html(icon);
	chartObj = new Object();
	if(variableObj == undefined){
		chartObj['chartNm']		= "MainChart";
		chartObj['period']		= period;
		chartObj['limit']		= "";
		chartObj['infoData']	= infoData;
	}else{
		chartObj = variableObj;
		period = chartObj.period;
		infoData = variableObj.infoData;
	}
	
	var url = "/scheduler/finance/getChartData.do";
	var param = "chartObj=" + encodeURIComponent(JSON.stringify(chartObj));
	var type = "json";
	fn_sendAjax(url, type, param, fn_ChartResult);
	function fn_ChartResult(result) {
		var data = result["data"];
		var smaFlag ="N";
		var macdFlag ="N";
		var doubleChartFlag ="N";
		var code = infoData.code;
		var name = infoData.name;
		var close_price = infoData.close.replace(/,/gi, "");	//금일 종가
		var open_price = infoData.open.replace(/,/gi, "");		//금일 시가
		
		var ctz_price = infoData.stock_sma5_mon_price;			//ctz(월5일선)라인가격
		var avg_purchase_price = infoData.avg_purchase_price;	//평단가 
		var chartOptions = result["chartOptions"];
		var macdOption=""; 
		if(macdFlag == 'Y'){
			macdOption ="";
		}
		
		for(var d=0; d < chartOptions.length; d++){
			var code_nm = chartOptions[d].code_nm;
			var code_value = chartOptions[d].code_value;
			if(code_nm == "F-SMA" && code_value == 'Y'){
				smaFlag = "Y";
			}
			if(code_nm == "F-MACD" && code_value == 'Y'){
				macdFlag = "Y";
			}
			if(code_nm == "F-DOUBLECHART" && code_value == 'Y'){
				doubleChartFlag = "Y";
			}
		}
		
		// 호출된 page의 Chart ID를 가져와 표시
		setChart = Highcharts.stockChart('container', {
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
				inputEnabled: false,//달력
				buttons: [{
					type:   period === 'month' || period === 'week' ? 'year' : (period === 'day' ? 'month' : 'day'),
					count:  period === 'month' ? '12' : period === 'week' ? '4' : (period === 'day' ? '6' : '20'),
					text:   period === 'month' ? '12Y': period === 'week' ? '4Y' : (period === 'day' ? '6M' : '20D')
				}, {
					type: 'ytd',
					text: '올해'
				}/*,{
			        type: 'custom',
			        text: 'Custom',
			        events: {
			            click: function () {
			            	커스텀하게 변경할때
			            }
			        }
			    }*/
			    ],
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
			            
						if (period.indexOf('minute') != -1) {
							//periodValue = currentDate.getUTCDate();
							periodValue = currentDate.getUTCMonth();
			            }else if (period === 'day') {
			                periodValue = currentDate.getUTCMonth();
			            } else if (period === 'month' || period === 'week') {
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
			                        text: Highcharts.dateFormat(period === 'month' || period === 'week' ? '%Y' : (period === 'day' ? '%m' : '%m'), data[i][0]),
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
				height: (macdFlag == "Y")?"65%":"100%",
				resize: {
					enabled: true
				},
				plotLines: [{//매수 평단가 라인
                             value: avg_purchase_price,
                             color: 'gray',
                             dashStyle: 'shortdash',
                             width: 1.5,
                             label: {
                                 text:'AVG '+Highcharts.numberFormat(avg_purchase_price, 0, '.', ','),
                                 align: 'center'
                             }
                           },
                           {	//금일 종가 라인
                           	 value: close_price,
                             color: (open_price > close_price) ? 'blue' : 'red',
                             dashStyle: 'shortdash',
                             width: 2,
                             label: {
                                 text:'Close '+Highcharts.numberFormat(close_price, 0, '.', ','),
                                 align: 'center'
                             }
                            },
                         	{	//CTZ
                           	 value: ctz_price,
                             color: 'green',
                             dashStyle: 'shortdash',
                             width: 2,
                             label: {
                                 text:'CTZ '+Highcharts.numberFormat(ctz_price, 0, '.', ','),
                                 align: 'left'
                             }
                        }]
					}
					,{
						/* MACD Setting */
						top: (macdFlag == "Y")?"65%":"0",
						height: (macdFlag == "Y")?"35%":"",
						labels: {
							align: 'left',
							x: -1,
							formatter: function() {	//금액 포멧
								return Highcharts.numberFormat(this.value, 0, '.', ',');
							}
						},
						offset: 0
					}
			],
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
		
		if(smaFlag == 'Y'){
			fn_addSMA(data);
		}
		if(macdFlag == 'Y'){
			fn_addMACD(data);
		}
		if(result["flag"] != undefined){
			fn_addFLAGS(result["flag"])
		}
		if(result["macdosc"] != undefined ){
			fn_addMACDOSC(result["macdosc"])
		}
		if(doubleChartFlag == 'Y'){
			//일봉 차트 시작과 끝으로 월봉 갯수 구하기
			fn_addMonthChart(result["monthData"]);
		}
	}
};
function fn_addSMA(data){
	//var code = infoData.code;
	// Define SMA periods and their corresponding colors
    const smaPeriods = [5, 20, 60, 120, 240];
    const smaColors = ['black', 'red', 'green', 'skyblue', '#EB3CDA'];
    const smaLines = [2, 2, 2, 2, 2];
    // Loop through SMA periods and create series
    for (let i = 0; i < smaPeriods.length; i++) {
        const smaPeriod = smaPeriods[i];
        const smaColor = smaColors[i];
        const smaLine = smaLines[i];

        setChart.addSeries({
            type: 'sma',
            linkedTo: infoData.code,
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
                enabled: (period.includes('minute')) ? true : false,
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

function fn_addMACD(data){
    setChart.addSeries({
		type: 'macd',
		yAxis: 1,
		data:[data],
		linkedTo: infoData.code,
		dataGrouping: {
			enabled: false // Disable automatic data grouping for candlestick series
		},
		params: {
			shortPeriod: 12,
			longPeriod: 26,
			signalPeriod: 9
		},
		macdLine: {
			styles: {
				lineColor: 'red',
				lineWidth: 2
			}
		},
		signalLine: {
			styles: {
				lineColor: '#3496ff',
				lineWidth: 2
			}
		},
		zones: [
			{
				value: 0,
				color: '#3496ff'
			},
			{
				color: 'red'
			}
		],
		// tooltip hide
		/*tooltip: {
			pointFormatter: function() {
				return false; // Exclude tooltip
			}
		}*/
    });
};

//MACDOSC 매수 매도 구간 라인
function fn_addMACDOSC(data){
	const item = data // three green lines should be plotted on x-axis
	for (let i = 0; i < item.length; i++) {
		setChart.xAxis[0].addPlotLine({
			customTooltip: false,
			type: 'line',
			value: item[i][0],
			id: item[i][1],
			color: (item[i][1] == "BUY") ? '#e83e8c57' : '#3498db57',
			width: 2,
			zIndex: 0.3
		});
	}
};

function fn_addFLAGS(data){
    setChart.addSeries({
            type: 'flags',
            data: data,
            onSeries: infoData.code,
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
// 월봉 더블 차트 추가 함수
function fn_addMonthChart(data) {
    // 새로운 시리즈 추가
    var series = setChart.addSeries({
        type: 'candlestick',
        linkedTo: infoData.code,    // CODE
        data: data,
        upColor: '#e83e8c6e',      // Up Color
        upLineColor: '#e83e8c6e',  // Up Line Color
        color: '#3498db6e',        // Down Color
        lineColor: '#3498db6e',    // Down Line Color
        pointWidth: 60,            // CandleStick Width (point)
        lineWidth: 2               // CandleStick Line Width (point)
    });

    // 차트 리사이즈 이벤트 핸들러 추가
    setChart.update({
        chart: {
            events: {
                resize: function () {
                    // 차트가 리사이즈 될 때마다 새로운 차트의 너비를 가져옴
                    var newChartWidth = this.chartWidth;
                    // 예를 들어, 차트의 촛대 너비를 다시 계산하여 조정
                    // 여기서는 1/10로 축소하는 예시 코드를 제공
                    var newCandleWidth = newChartWidth / 10;

                    // 모든 촛대 데이터 포인트에 대해 너비를 업데이트
                    series.points.forEach(function (point) {
                        point.update({
                            pointWidth: newCandleWidth
                        }, false); // false로 설정하여 차트를 다시 그리지 않음
                    });

                    // 변경 사항을 적용하여 차트를 다시 그림
                    this.redraw();
                }
            }
        }
    });
};

/* Link System Display */
function fn_setStockInfo(data) {

	$("#watch_logo").attr("src", data.url);
	$("#watch_ko_name").text(data.name);
	$("#watch_code").text(data.code);
	$("#watch_id").val(data.id);
	$("#watch_market").text(data.market);
	
	// attr() 메서드를 사용하여 src 속성에 변수 값 설정
	$("#watch_close").text(data.close);
	$("#watch_pricePercentage").text(data.pricePercentage+"%");
	$("#watch_priceDifference").text(data.priceDifference);
    
    //connectWebSocket(data.code);
    
	//$("#watch_volume").text(data.volume);
	//$("#watch_volumeValued").text(data.volumeValued);
	//$("#watch_smaAnalysis").text(data.volumeValued);
	
	$("#totime").text(data.realTime);
	$("#watch_info").show();
	
	var close = data.close.replace(/,/gi, "");			//금일 종가
	var prevClose = data.prevClose.replace(/,/gi, "");	//전일 종가
	
	var price_flag = ""
	var price_color = "gray"
	if (parseInt(close) > parseInt(prevClose)) {
		price_flag = "up";
		price_color = "red";
	}else if (parseInt(prevClose) > parseInt(close)) {
		price_flag = "down";
		price_color = "blue";
	}

	var color = "text-"+price_color+" col-form-label setcolor";
	var arrow = (price_flag == "")? "" : "fas fa-arrow-"+price_flag+" text-sm pl-1";
	
	$("#watch_priceArrow").removeClass().addClass(arrow);
	$("#watch_close").removeClass().addClass(color);
	$("#watch_pricePercentage").removeClass().addClass(color);
	$("#watch_priceDifference").removeClass().addClass(color);
	$("#watch_priceArrow").css("color", price_color);
	
	const activeNavLink = $(".nav-link.active").attr("id");
	if(activeNavLink == "stockSet"){
		fn_StockCurrentPrice();
	}
}
