<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="org.apache.taglibs.standard.tag.rt.core.IfTag" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="/appone/plugins/system/js/jsLink.jsp" %>
<%@ include file="/appone/plugins/system/css/cssLink.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" type="text/css" href="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/css/stocktools/gui.css">
<link rel="stylesheet" type="text/css" href="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/css/annotations/popup.css">

<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/highstock.js"></script>
<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/modules/accessibility.js"></script>
<script src="/scheduler/appone/plugins/Highcharts-Stock-11.1.0/code/indicators/indicators-all.js"></script>

<script src="/scheduler/finance/stock/sub/chartScript.js"></script>
<!-- <script src="/scheduler/finance/stock/sub/websocketScript.js"></script> -->
<script src="/scheduler/finance/stock/sub/financeScript.js"></script>
<link rel="stylesheet" href="/scheduler/finance/stock/sub/chartStyle.css">

<!-- ChartJS -->
<div id="div_finance_main" class="p-1">
	<div class="row" id="toptitle">
		<div class="col-lg-12">
			<div class="row">
				<!-- 종목 검색 -->
				<div class="col-lg-2">
					<div class="row">
						<div class="market-info-box">
							<select class="form-control select2" multiple="multiple" style="width: 100%;"></select>
						</div>
					</div>
		       	</div>
				<!-- 지수 조회 -->
				<div class="col-lg-9 subMarketInfo" ></div>
			</div>
		</div>
	</div>
	<div class="row" id="financeMainCardbody">
		<!-- 관심목록 -->
		<section class="col-lg-2 pr-0 connectedSortable"  id="portfolio_card">
			<div class="card card-primary card-outline direct-chat direct-chat-primary mb-1 h-95">
				<div class="card-header p-1" >
					<div class="card-tools float-left">
	                	<ul class="nav">
	                  		<li class="nav-item">
	                    		<a class="nav-link p-1" style="color: #464b51; cursor: pointer;" onclick="fn_groupList();" data-widget="chat-pane-toggle">
	                      			<i class="fas fa-bars"></i><span class="text">관심그룹</span>
                      			</a>
	                    	</li>
		                    <li class="nav-item p-1">
		                    	<div class="custom-control custom-switch custom-switch-on-success custom-switch-off-gray">
		                      		<input type="checkbox" class="custom-control-input" id="groupMarket" value="KR">
		                      		<label class="custom-control-label" for="groupMarket">
		                      			<span class="text-gray">국내</span>/<span class="text-success">해외</span>
	                      			</label>
		                    	</div>
		                    </li>
		                    <!-- icon  -->
							<li class="nav-item p-1">
								<div class="btn-group btn-reflesh" id="selectGroupStockList"></div>
							</li>
							<!-- icon  -->
							<li class="nav-item p-1">
								<div class="btn-group btn-dynmicOptions" id="StockList"></div>
							</li>
						</ul>
	                </div>
            	</div>
            	<!-- 그룹 리스트 콤보 -->
				<div class="card m-0">
					<!-- SELECT GROUP LIST -->
					<ul class="nav">
						<li class="nav-item" id="STOCKGROUP_LIST">
                    		<select class="custom-select form-control-border border-width-2 col-sm-12" id="interastGroup"></select>
                    	</li>
                    	<!-- icon  -->
						<li class="nav-item p-1">
							<button type="button" class="btn btn-block btn-default btn-sm stockGroupDiv"
							 	id="StockGroupNormal" value="normal">일반</button>
						</li>
                    	<!-- icon  -->
						<li class="nav-item p-1">
							<button type="button" class="btn btn-block btn-default btn-sm stockGroupDiv" 
								id="StockGroupClose" value="close">종가</button>
						</li>
                    	<!-- icon  -->
						<li class="nav-item p-1">
							<button type="button" class="btn btn-block btn-default btn-sm stockGroupDiv" 
							id="StockGroupMonth" value="month" >월말</button>
						</li>
					</ul>
				</div>
				<!-- 그룹 리스트 -->
				<div class="card-body table-responsive p-0 scrollOff" id="heigh_size" >
					<div class="card card-row card-secondary">
						<div class="card-body" id="portfolioBody"></div>
					</div>
					<!-- 관심그룹 수정 -->
					<div class="direct-chat-contacts h-100 scrollOff" style="background-color: #fff;">
						<div class="card mb-1" style="color: black;">
							<div class="card-header mr-2 p-1">
								<div class="card-tools">
									<b>관심목록</b>
									<button type="button" class="btn btn-sm btn-success " onclick="fn_portfolioGroupEvent('I')">
										<i class="fas fa-plus"></i>
									</button>
								</div>
							</div>
							<!-- 관심그룹 수정 End-->
							<!-- Stock List -->
							<div class="card-body p-0" id="portfolioGroupList"></div>
						</div>
					</div>
				</div>
			</div>
		</section>
		<!-- 종목 챠트 -->
		<section class="col-lg-7 pr-0 connectedSortable" id="chart_card">
			<div class="card card-primary card-outline mb-1 h-95">
				<div class="card-body p-1">
					<div class="card-tools float-left">
						<div class="row" style="font-size: 17.5px;" id="watch_info">
							<div class="col-sm-auto d-flex align-items-center justify-content-center">
								<h6 class="my-1 mt-1">
									<img class="img-size-32 img-circle img-bordered-sm" id="watch_logo"
										src="" onerror="this.onerror='Img'; this.src=''">
								</h6>
							</div>
							<div class="col-sm-auto pl-0 pr-0">
								<h5 class="text-muted col-form-label"><strong id="watch_ko_name" class="review"></strong></h5>
							</div>
							<div class="col-sm-auto pl-1 pr-0">
								<h6 class="text-muted col-form-label" id="watch_code"></h6>
								<input type="hidden" class="text-muted col-form-label" id="watch_id" value=""/>
							</div>
							<div class="col-sm-auto pl-1 pr-0">
								<h6 class="text-muted col-form-label" id="watch_market"></h6>
							</div>
							<div class="col-sm-auto">
								<h6 class="text-muted col-form-label setcolor" id="watch_close"></h6>
							</div>
							<div class="col-sm-auto pl-1 pr-0">
								<h6 class="text-muted col-form-label setcolor">
								 	<span class="text-danger" id="watch_priceDifference"></span>
									<i class='fas fa-arrow-up text-sm pl-1' id="watch_priceArrow"></i>
								</h6>
							</div>
							<div class="col-sm-auto pl-1 pr-0">
								<h6 class="text-muted col-form-label setcolor" id="watch_pricePercentage"></h6>
							</div>
							<div class="col-sm-auto d-flex align-items-center">
								<label for="watch_priceVolume" class="text-muted col-form-label"></label>
								<a class="text-muted" id="watch_smaAnalysis"></a>
							</div>
							
	                       	<div class="col-sm-auto d-flex align-items-center">
								<div class="form-group row my-0">
									<div class="btn-group">
			                       		<button type="button" class="btn btn-light btn-xs dropdown-toggle dropdown-icon" data-toggle="dropdown">
			                       			<i class="fa fa-heart text-warning" aria-hidden="true"></i>
			                       		</button>
			                        	<div class="dropdown-menu p-1">
											<a class="btn dropdown-item text-sm p-1 custom_favorite" id="normal">일반</a>												
											<a class="btn dropdown-item text-sm p-1 custom_favorite"  id="close">종가</a>
											<a class="btn dropdown-item text-sm p-1 custom_favorite"  id="month">월말</a>
			                        	</div>
			                       	</div>
								</div>
							</div>
						</div>
					</div>
					<div class="card-tools float-right">
						<div class="btn-group" id="bt_chartTime">
                       		<button type="button" class="btn btn-default btn-sm dropdown-toggle dropdown-icon" 
                       			id="bt_minute" data-toggle="dropdown">분</button>
                         		<div class="dropdown-menu p-1">
                         			<a class="btn p-2 dropdown-item chartTime" id="minute-1">1</a>
	                           		<a class="btn p-2 dropdown-item chartTime" id="minute-5">5</a>
	                           		<a class="btn p-2 dropdown-item chartTime" id="minute-10">10</a>
	                           		<a class="btn p-2 dropdown-item chartTime" id="minute-15">15</a>
	                           		<a class="btn p-2 dropdown-item chartTime" id="minute-30">30</a>
	                           		<a class="btn p-2 dropdown-item chartTime" id="minute-60">60</a>
                         		</div>
                       	</div>
						<button type="button" class="btn btn-default btn-sm chartTime" id="minute-30">30</button>
						<button type="button" class="btn btn-default btn-sm chartTime" id="day">일</button>
						<button type="button" class="btn btn-default btn-sm chartTime" id="week">주</button>
						<button type="button" class="btn btn-default btn-sm chartTime" id="month">월</button>
						
						<div class="btn-group btn-reflesh" id="Chart"></div>
                       	<button type="button" class="btn btn-default btn-sm"
                       				data-toggle="modal" data-target="#modal_subChart">
             				<i class="fas fa-th-large"></i>
              			</button>
              			<div class="btn-group btn-dynmicOptions" id="MainChart"></div>
						<button type="button" class="btn btn-default btn-sm" data-card-widget="maximize">
							<i class="fas fa-expand"></i>
                		</button>
					</div>
					<div id="container" class="chart d-flex align-items-center justify-content-center h-95"></div>
				</div>
			</div>
		</section>
		<!-- Sub Tab -->
		<section class="col-lg-3 connectedSortable" id="watch_card">
			<div class="card card-primary card-outline card-outline-tabs mb-1 h-95" >
            	<div class="card-header p-0 border-bottom-0">
              		<ul class="nav nav-tabs" id="custom-tabs-four-tab" role="tablist">
                  		<li class="nav-item tab">
                    		<a class="nav-link p-1 active" id="marketChart" data-toggle="pill" href="#marketChart_tab" role="tab" aria-controls="marketChart" aria-selected="true">시장정보</a>
                  		</li>
                  		<li class="nav-item tab">
                    		<a class="nav-link p-1" id="stockCurrent" data-toggle="pill" href="#stockCurrent_tab" role="tab" aria-controls="stockCurrent" aria-selected="false">Stock주문</a>
                  		</li>
                  		<li class="nav-item tab">
                    		<a class="nav-link p-1" id="stockPortfolio" data-toggle="pill" href="#stockPortfolio_tab" role="tab" aria-controls="stockPortfolio" aria-selected="false">보유종목</a>
                  		</li>
                  		<li class="nav-item tab">
                    		<a class="nav-link p-1" id="stockAnalysis" data-toggle="pill" href="#stockAnalysis_tab" role="tab" aria-controls="stockAnalysis" aria-selected="false">추천종목</a>
                  		</li>
                  		<li class="nav-item tab">
                    		<a class="nav-link p-1" id="stockAnalysisDetail" data-toggle="pill" href="#stockAnalysisDetail_tab" role="tab" aria-controls="stockAnalysisDetail" aria-selected="false">종목상세</a>
                  		</li>
               		</ul>
              	</div>
              	<div class="mb-0 p-1">
             		<div class="form-group row mb-0 pr-1">
						<div class="col-sm-10 float-left">
							<i class="fas fa-bars"></i><span class="text"><a id="watch_title"></a></span>
						</div>
						<div class="col-sm-1">
							<div class="btn-group btn-reflesh" id="watchtab"></div>
						</div>
						<div class="col-sm-1">
							<div class="btn-group btn-dynmicOptions" id="WatchList"></div>
						</div>
					</div>
           		</div>
              	<div class="card card-row card-secondary mb-0 h-100">
					<div class="card-body table-responsive p-0 scrollOff subWatch_tab" id="watch_tab" style="height:700px;"></div>
				</div>
            </div>
		</section>
	</div>
	<!-- Modal -->
	<div class="modal fade" id="modal_subChart">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header p-2">
					<h4 class="modal-title text-sm">Sub Chart</h4>
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
				</div>
				<div class="modal-body p-2">
					<div class="card m-0">
						<div class="card-body row p-1">
							<div class="col-md-6">
								<button type="button" id="minute-30" class="btn btn-outline-info text-sm btn-block btn-sm subCharts">
									<i class="fa fa-bar-chart"></i> 30Minute
								</button>
								<button type="button" id="week" class="btn btn-outline-info text-sm btn-block btn-sm subCharts">
									<i class="fa fa-bar-chart"></i> Weekly
								</button>
							</div>
							<div class="col-md-6">
								<button type="button" id="day" class="btn btn-outline-info text-sm btn-block btn-sm subCharts">
									<i class="fa fa-bar-chart"></i> Day
								</button>
								<button type="button" id="month" class="btn btn-outline-info text-sm btn-block btn-sm subCharts">
									<i class="fa fa-bar-chart"></i> Month
								</button>
							</div>
						</div>
					</div>
				</div>
				<div class="modal-footer justify-content-end p-1">
					<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				</div>
			</div>
		</div>
	</div>
	<div id="modal_options"></div>
</div>
<script>
	//각 영역들을 미리 선언
	var div_finance_main = $('#div_finance_main');
	
	function fn_initPageHeight(){
		var windowHeight = $(window).height();
		var toptitle = $("#toptitle").outerHeight();	//top 타이틀
		$("#heigh_size").height(windowHeight-toptitle);
	}
	
	
	//Chart 관심 그룹 등록
	$('.custom_favorite').click(function() {
		var group_id = $('#interastGroup').val();
		var stock_code = $("#watch_code").text();
		var group_div = this.id;
		
		if(group_div == undefined){
			return false;
		}
		if(stock_code == undefined){
			return false;
		}
		var url = "/scheduler/finance/selectGroupStockInfo.do";
		var param = "group_id="+group_id+"&stock_code="+stock_code+"&group_div="+group_div;
		var type = "json";
		fn_sendAjax(url, type, param, fn_groupStockInfoResult);
			
		/* Link System Display */
		function fn_groupStockInfoResult(data) {
			var groupname = $('#interastGroup').find("option:selected").text();
			//그룹에 해당 종목이 있으면 삭제/ 없으면 등록 
			if(data.size > 0){
				if (confirm(groupname+" 삭제하시겠습니까?") != false) {
					fn_interastStockEvent("D",stock_code,group_div);
				}
			}else{
				fn_interastStockEvent("I",stock_code,group_div);
			}
		}
    });

	/* 관심 종목 생성,수정,삭제 */
	function fn_interastStockEvent(eventCode,stock_code,group_div){
		
		var group_id = $('#interastGroup').val();
		
		var param = "stock_code="+stock_code+"&eventCode="+eventCode+"&group_id="+group_id+"&group_div="+group_div;
		var url = "/scheduler/finance/interastStockEvent.do";
		var type = "json";
		fn_sendAjax(url, type, param, fn_StockEventResult);
		function fn_StockEventResult(data){
			showAlert(data.result_code, data.result_msg,"3000")
			fn_selectGroupStockList(group_id);	//그룹 조회
		}
	};
	
	/* 새 관심목록 클릭 */
	function fn_groupList(){
		fn_transform(div_finance_main,'#portfolioGroupList', 'contactsList');
	}
	
	/* 관심그룹 combobox Setting */
	function fn_transform(div, id, flag){
		var use_flag ="Y";
		
		var url ="/scheduler/finance/selectPortfolioGroupList.do";
		var param =getJQParams(div_finance_main)+"&flag=" + flag+"&use_flag="+use_flag;
		var type = "html";
		fn_sendAjax(url, type, param, fn_transformResult);
			
		function fn_transformResult(data){
			if(flag =="combobox"){
				div.find(id+' select').html(data);
			}else{
				div.find(id).html(data);
			}
		}
	}
    
    //그룹 리스트
    $("#interastGroup").change(function(){
    	fn_selectGroupStockList(this.value);
    });
    
    var group_div = "normal"; //그룹 구분(일반, 종가, 월말)
    //그룹 종가, 월말 리스트
    $('.stockGroupDiv').click(function() {
    	group_div = this.value;
    	var group_id = $("#interastGroup").val();
    	fn_selectGroupStockList(group_id);
    });
    
	/*그룹별 STOCK 리스트*/
	function fn_selectGroupStockList(group_id){
		if(group_id == undefined){
			group_id = $("#interastGroup").val();	
		}
		
		$('.chat-pane').hide();	// 슬라이드 hide
		$("#interastGroup").val(group_id);
		var url = "/scheduler/finance/selectGroupStockList.do";
		var param = getJQParams(div_finance_main)+"&group_div="+group_div;
		var type = "html";
		fn_sendAjax(url, type, param, fn_selectGroupStockListResult);
		/* Link System Display */
		function fn_selectGroupStockListResult(data) {
			div_finance_main.find("#portfolioBody").html(data);
		}
	};
	
    /* 관심그룹 생성,수정,삭제 */
    function fn_portfolioGroupEvent(eventCode,group_id,group_name){
    	var title ="";
    	var text ="";
    	var input ="";
    	var inputValue ="";
    	switch(eventCode) {
			case 'I': 
				title = "새 관심그룹 생성";
				text = "새로운 관심그룹의 이름을 입력해주세요.";
				input = "text";
				break;
			case 'U':
				title = "관심그룹 수정";
				text = "새롭게 변경할 관심그룹의 이름을 입력해 주세요.";
				input = "text";
				inputValue = group_name;
				break;
			case 'C':
				title = "관심그룹 복사";
				text = "관심그룹의 이름을 입력해 주세요.";
				input = "text";
				//inputValue = group_name;
				break;
			case 'D':
				title = "관심그룹 삭제";
				text = " 관심그룹을 삭제하시겠습니까?";
				input = "text";
				inputValue = group_name;
				break;
    	}
    	
   	$('.chat-pane').hide();
	Swal.fire({
		  title: title,
		  text: text,
		  input: input,
		  inputValue: inputValue,
		  //icon: 'warning',
		  showCancelButton: true,
		  confirmButtonColor: '#3085d6',
		  cancelButtonColor: '#d33',
		  confirmButtonText: '확인',
		  cancelButtonText: '취소'
		}).then((result) => {
		  if (result.value) {
			  group_name = result.value;
			  if(eventCode == "I" || eventCode == "C" ){
				  group_seq = "";
			  }
				$.ajax({
					type:"POST",
					url:"/scheduler/finance/portfolioGroupEvent.do",
					dataType:"json",
					data : param = getJQParams(div_finance_main)+"&group_id="+group_id+"&group_name="+group_name+"&eventCode="+eventCode,
					success:function(msg){
						fn_transform(div_finance_main,'#STOCKGROUP_LIST', 'combobox');
						fn_groupList();
					}
				});
		  	}else if (result.value == ""){
		  		alert("값을 입력해주세요.");
		  	}
		})
    };
    
    //Chart Data Time
    $(".chartTime").click(function() {
    	var buttonText = $(this).text();
    	period = this.id;
    	var mintext = "분";
    	if(buttonText <= 60){
    		mintext = buttonText+mintext;	
    	}
    	$("#bt_minute").text(mintext);
    	fn_Chart();
    });
	
	// TAB
    $(".tab > a").click(function(event){
    	fn_watchtab(this.id);
    });
    
 	// TAB
	function fn_watchtab(id){
		const activeNavLink = $(".nav-link.active");
		if(empty.isEmpty(id)){
			id = activeNavLink.attr("id");
		}
		var modal_id = $("#modal_id").val();
		var modal_param ="";
		if(modal_id != undefined){
			modal_param = getJQParams($("#modal_" + modal_id))+"&";
		}
		$("#watch_title").text($("#"+id).text());
    	//$("subWatch_tab *").remove();
      	var url = "/scheduler/finance/sidetabCallPage.do";
      	var param = modal_param+"tab_id=" + id+"&groupMarket="+$("#groupMarket").val();
      	var type = "html";
      	fn_sendAjax(url, type, param, fn_callPageResult);
      	function fn_callPageResult(data) {
      		div_finance_main.find(".subWatch_tab").html(data);
      	}
    }
	
	// USER DEFAULT STOCK SETTING 
 	function fn_financeUserSetting(){
     	var url = "/scheduler/finance/financeUserSetting.do";
     	var param =getJQParams(div_finance_main);
     	var type = "json";
     	fn_sendAjax(url, type, param, fn_financeUserSettingResult);
     	function fn_financeUserSettingResult(result) {
     		fn_StockPrice(result.stock_code);
     	}
    }
	// USER DEFAULT STOCK SETTING 
 	function fn_MarketInfo(){
     	var url = "/scheduler/finance/marketInfo.do";
     	var param = getJQParams(div_finance_main);
     	var type = "html";
     	fn_sendAjax(url, type, param, fn_MainMarketResult);
     	function fn_MainMarketResult(data) {
     		$(".subMarketInfo").html(data);
     	}
    }

	window.onload = function(){
		const activeNavLink = $(".nav-link.active");
		var activetab = activeNavLink.attr("id");
	  	fn_watchtab(activetab);
    }
	
	$("#modal_subChart").click(function(){
		$(".modal-title").text(infoData.name);
	});
	
	fn_initPageHeight();// 페이지 크기 조정
	
	fn_reflesh("MarketInfo","5");
	fn_MarketInfo();
</script>
