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

$(document).ready(function(){
	var url = "/scheduler/finance/searchStocksKeyword.do";
	
	$(".select2").select2({
		placeholder: "종목명 & 코드",
		minimumInputLength: 2, // 최소 검색어 길이
		ajax: {
			url: url,
		    dataType: "json",
		    delay: 400,
		    data: function (params) {
		    		return {
		    			keyword : params.term
		        	};
		      	},
		      	processResults: function (data, params) {
		        	return { results: data.data // AJAX로 받은 결과
		        	};
		      	},		     
		      	cache: true
		    },
			templateResult: formatState,
			templateSelection: formatState
	});
	
	$(".select2").on("select2:select", function(e) {
		var code = e.params.data.code;
		//fn_StockDetails(code);		// 선택된 Stock 정보 DB에 저장
		fn_StockPrice(code); 			// 선택된 Stock 금일 금액 정보 조회
		$('.select2').val(null).trigger('change');	//select2 multiple 항목 삭제
	});

	function formatState(state) {
		if (state.id === undefined) {
			return state.text;
		}else{
			var	logo = "";
			if(state.logo != null){
				logo = "<img src='"+state.logo+"'class='img-size-32 img-circle'>";
			}
				var $container = $(
					"<div class='media'>"+logo+
					"	<div class='media-body'>"+
					"		<h3 class='dropdown-item-title p-0'>"+state.ko_name+"</h3>"+
					"		<span class='text-sm text-muted p-0'>"+state.code +" "+ state.market+"</span>"+
					"	</div>"+
					"</div>"
				);
			return $container;
		}
	};
		
	//국내/해외 구분 Switch
	$("#groupMarket").on('change', function() {
		
		var groupMarket = document.getElementById("groupMarket").checked;
		if (groupMarket == true) {
			groupMarket = "US";
		} else {
			groupMarket = "KR";
		}
		$("#groupMarket").val(groupMarket);
		
		fn_MarketInfo();
		fn_groupList();
		fn_transform(div_finance_main, '#STOCKGROUP_LIST', 'combobox');
		//fn_selectGroupStockList();//그룹 Stock List
		$("#modal_options *").remove();
	
		const activeNavLink = $(".nav-link.active");
		var activetab = activeNavLink.attr("id");
		fn_watchtab(activetab);
	});
	//fn_initPageHeight();
	fn_transform(div_finance_main,'#STOCKGROUP_LIST', 'combobox');
	fn_financeUserSetting();
});

// Stock 상세 정보
function fn_StockDetails(code){
	var url = "/scheduler/finance/selectStockDetails.do";
	var param = "stock_code=" + code;
	var type = "json";
	fn_sendAjax(url, type, param, fn_StockDetailsResult);
	function fn_StockDetailsResult(result){
		return result[code];
	}
}

$(document).ready(function(){
	const elementList = document.querySelectorAll('.btn-dynmicOptions');
	elementList.forEach((item) =>{
		var element_id = item.id;
		const elementCheck = document.querySelectorAll('#btn_'+element_id);
		if(empty.isNotEmpty(element_id) && elementCheck.length == 0){
			var div =
			"<button type='button' class='btn btn-light btn-sm' id='"+element_id+"' onclick='fn_dynmicOptions(this.id)'>"
			+"	<i class='fas fa-gear dynmicbutton' id='btn_"+element_id+"'></i>"
			+"</button>";
			$('#'+element_id).append(div);
			
		}
	});
	const refleshElementList = document.querySelectorAll('.btn-reflesh');
		refleshElementList.forEach((item) =>{
		var elementId = item.id;
		const elementCheck = document.querySelectorAll('.real_'+elementId);
		if(empty.isNotEmpty(elementId) && elementCheck.length == 0){
			var div = 
				"<button type='button' class='btn btn-light btn-sm' data-toggle='dropdown'>"
				+"	<i class='fas fa-ban' id='real_"+elementId+"'></i>"
				+"</button>"
				+"<div class='dropdown-menu'>"
				+"	<a class='btn p-2 dropdown-item real_"+elementId+"' onclick=fn_reflesh('"+elementId+"',this.text)><i class='text-red fas fa-ban'></i> </a>"
				+"	<a class='btn p-2 dropdown-item real_"+elementId+"' onclick=fn_reflesh('"+elementId+"',this.text)>5</a>"
				+"	<a class='btn p-2 dropdown-item real_"+elementId+"' onclick=fn_reflesh('"+elementId+"',this.text)>10</a>"
				+"	<a class='btn p-2 dropdown-item real_"+elementId+"' onclick=fn_reflesh('"+elementId+"',this.text)>30</a>"
				+"	<a class='btn p-2 dropdown-item real_"+elementId+"' onclick=fn_reflesh('"+elementId+"',this.text)>60</a>"
				+"  <input type='hidden' value='' id='time_"+elementId+"'/>";
				+"</div>";
				$('#'+elementId).append(div);
		}
	});
});

function fn_reflesh(_fnution,time){
	var time_id = $("#time_"+_fnution).val();

	if(time.trim()==""){
		$('#real_'+_fnution).removeClass('fa-solid fa-spinner fa-spin-pulse').addClass('fas fa-ban');
	}else{
		$('#real_'+_fnution).removeClass('fas fa-ban').addClass('fa-solid fa-spinner fa-spin-pulse');
	}
	if(time.trim()===""){
		stopTimer(time_id,time);
	}else{
		if(time_id != undefined){
			stopTimer(time_id,time);	
		}
		
		var timerVar = setInterval("fn_"+_fnution+"()", time*1000);
		console.log(time+" "+time_id+" "+_fnution+' 타이머가 시작되었습니다.!!');
		$("#time_"+_fnution).val(timerVar);
	}
}

function startTimer(_fnution, time){
	timerVar = setInterval(_fnution, time*1000);
	console.log(time+" "+timerVar+" "+_fnution+' 타이머가 시작되었습니다.!!');
}

function stopTimer(time_id, time){
	    clearInterval(time_id);
	    console.log(time+" "+time_id+'종료되었습니다.!!!');
}

function fn_dynmicOptions(id){
	const activeNavLink = $(".nav-link.active");
	var tab_id = activeNavLink.attr("id");
	if(id == ""){
		return false;
	}
	
	$("#modal_options *").remove();
	var url = "/scheduler/finance/selectModalOptions.do";
	var param = "modal_id=" + id+"&tab_id="+tab_id+"&groupMarket="+$("#groupMarket").val();
	var type = "html";
	fn_sendAjax(url, type, param, fn_OptionSettingResult);
	
	/* Option Btn Display */
	function fn_OptionSettingResult(data) {
		if(id == "WatchList") {
			switch (tab_id) {
			case "stockPortfolio":
				id = "StockPortfolio";
				break;
			case "marketChart":
				id = "MarketChart";
				break;
			case "stockAnalysis":
				id = "StockAnalysis";
				break;
			}
		}
		$('#modal_options').append(data);
		$('#modal_'+id).modal("show");
	}
};

$(".subCharts").click(function(){
	period = this.id;
	var chartObj = new Object();
	chartObj['period'] = this.id;
	chartObj['groupMarket'] = $("#groupMarket").val();
	chartObj['infoData'] = infoData;
	// chartObj를 문자열로 변환
	var chartObjStr = JSON.stringify(chartObj);
	// 자식 창을 열고 chartObjStr을 URL 쿼리 문자열로 전달
	var childWindow = window.open('/scheduler/finance/stock/pop/pop_SubChart.jsp?chartObj='
			+encodeURIComponent(chartObjStr), '_blank', "width=750, height=600");
		childWindow.focus();
	$("#modal_subChart").modal("hide");
});

function fn_delAnalysis(code, name){
	if(confirm(name+"는 더 이상 추천 목록에 올라오지 않습니다.\n진행하시겠습니까?") != false){
      	var url = "/scheduler/finance/delAnalysis.do";
      	var param = "code=" + code;
      	var type = "json";
      	fn_sendAjax(url, type, param, fn_delAnalysisResult);
      	function fn_delAnalysisResult(){
      		fn_watchtab();
      	}
	}
}