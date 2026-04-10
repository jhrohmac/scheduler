<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container-fluid" id="div_user_main">
	<div class="card card-primary card-outline">
		<div class="card-header pb-2 pt-2">
			<h3 class="card-title">조회 조건</h3>
			<div class="card-tools">
				<button type="button" class="btn btn-tool"
					data-card-widget="collapse" title="Collapse">
					<i class="fas fa-minus"></i>
				</button>
			</div>
		</div>
		<div class="card-body p-2">
			<div class="form-group search_row">
				<label for="in_stockCode" class="col-sm-1 col-form-label text-right">CODE 정보</label>
				<div class="col-sm-3">
					<input type="text" id="in_stockCode" name="in_stockCode" value="000270" class="form-control form-control-sm" placeholder="Code ID or Code명">
				</div>
				<div class="col-sm-1">
					<button type="button" id="btn_SearchInfo" class="btn btn-sm btn-primary float-right">
						기본
					</button>
				</div>
				<div class="col-sm-1">
					<button type="button" id="btn_SearchDetailInfo" class="btn btn-sm btn-primary float-right">
						상세
					</button>
				</div>
				<div class="col-sm-1">
					<button type="button" id="btn_InquirePrice" class="btn btn-sm btn-primary float-right">
						현재
					</button>
				</div>
				<div class="col-sm-5">
					<button type="button" id="btn_main_search" class="btn btn-sm btn-primary float-right">
						<i class="fa fa-search"></i>
					</button>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-12">
			<div class="card">
				<div class="card-body table-responsive p-2">
	            	<table class="table table-hover table-sm" id="tblDailyPriceGridData">
						<thead>
							<tr>
								<th>일자</th>
					            <th>시가</th>
					            <th>고가</th>
					            <th>저가</th>
					            <th>종가</th>
					            <th>거래량</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
</div>

<script>

$(document).ready(function () {
    $("#btn_main_search").on("click", function () {
    	fn_kisDailyPriceGridData();
    });
    $("#btn_SearchDetailInfo").on("click", function () {
    	fn_SearchDetailInfo();
    });
    $("#btn_InquirePrice").on("click", function () {
    	fn_InquirePrice();
    });
    $("#btn_SearchInfo").on("click", function () {
    	fn_SearchInfo();
    });

    // 엔터키로도 조회
    $("#in_stockCode").on("keydown", function (e) {
        if (e.keyCode === 13) {
        	fn_kisDailyPriceGridData();
        }
    });
    // 최초 진입 시 기본값으로 한 번 조회
    fn_kisDailyPriceGridData();
});

/************************************************************************
* user list
*************************************************************************/
function fn_kisDailyPriceGridData(){

    var ctx = $("#contextPath").val() || "";
    var in_stockCode = $.trim($("#in_stockCode").val());
    
    in_stockCode = "005930";
    if (in_stockCode === "") {
        alert("종목코드를 입력하세요.");
        $("#in_stockCode").focus();
        return;
    }
    
	// 호출 URL
	var url = "/scheduler/finance/kisDailyPriceData.do";
	//조회 조건
	var param = {
			 	"in_stockCode" 	: $('#in_stockCode').val()
	};
	
	//컬럼 옵션
	var columns = [
					{"data": "date"},
					{"data": "open"},
					{"data": "high"},
					{"data": "low"},
					{"data": "close"},
					{"data": "volume"}
	           ];
	
	var columnDefs = {
		    'targets': [1,2,3],
		    "className": "text-center"
	};
	
	var gridObj = {
	        'grid_id': "tblDailyPriceGridData",
	        'url': url,
	        'param': param,
	        'columns': columns,
	        'columnDefs': columnDefs,
	        'columnCheck': false
	};
	
	var gridOptions = {
	        'serverSide': false,
	        'searching': false,
	        'paging': true,
	        'button': false,
	        'lengthChange': false,
	        'info': true,
	        'autoWidth': false,
	        'responsive': true,
	        'bDestroy': true,
	        'processing': true,
	        'ordering': false
    };
	dataTableGridNew(gridObj,gridOptions);
};


/* 종목 기본정보 */
function fn_SearchInfo() {
    var in_stockCode = $.trim($("#in_stockCode").val());
	var url = "/scheduler/finance/getSearchInfo.do";
	var param = "in_stockCode=" + in_stockCode;
	var type = "script";
	ajaxCall(url, type, param, function(data){
		console.log(data);
	});
}


/* 종목 상세 정보 */
function fn_SearchDetailInfo() {
    var in_stockCode = $.trim($("#in_stockCode").val());
	var url = "/scheduler/finance/getSearchStockInfo.do";
	var param = "in_stockCode=" + in_stockCode;
	var type = "script";
	ajaxCall(url, type, param, function(data){
		console.log(data);
	});
}

/* 현재가 */
function fn_InquirePrice() {
    var in_stockCode = $.trim($("#in_stockCode").val());
	var url = "/scheduler/finance/getCurrentPriceByInquirePrice.do";
	var param = "in_stockCode=" + in_stockCode;
	var type = "script";
	ajaxCall(url, type, param, function(data){
		console.log(data);
	});
}

</script>
