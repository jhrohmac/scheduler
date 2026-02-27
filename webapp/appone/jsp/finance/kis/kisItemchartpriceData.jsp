<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container-fluid" id="div_user_main">
	<input type="hidden" id="contextPath" value="${ctx}" />
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
				<div class="col-sm-1">
					<input type="text" id="in_stockCode" name="in_stockCode" class="form-control form-control-sm" placeholder="Code ID or Code명">
				</div>
				<label for="in_fromDate" class="col-sm-1 col-form-label text-right">기간</label>
				<div class="col-sm-1">
					<input type="text" id="in_fromDate" name="in_fromDate" class="form-control form-control-sm" placeholder="YYYYMMDD">
				</div>
				<div class="col-sm-1">
					<input type="text" id="in_toDate" name="in_toDate" class="form-control form-control-sm" placeholder="YYYYMMDD">
				</div>
				<label for="in_period" class="col-sm-1 col-form-label text-right">기간분류코드</label>
				<div class="col-sm-1">
					<select class="form-control form-control-sm custom-select" id="in_period">
			            <option value="D" selected="selected">D - 일봉</option>
			            <option value="W">W - 주봉</option>
			            <option value="M">M - 월봉</option>
			            <option value="Y">Y - 년봉</option>
			        </select>
				</div>
				<label for="in_orgAdj" class="col-sm-1 col-form-label text-right">수정/원주가</label>
				<div class="col-sm-1">
					<select class="form-control form-control-sm custom-select" id="in_orgAdj">
			            <option value="0" selected="selected">0 - 수정주가</option>
			            <option value="1">1 - 원주가</option>
			        </select>
				</div>
				<div class="col-sm-1">
					<button type="button" id="btn_today" class="btn btn-sm btn-primary float-right">60
					</button>
				</div>
				<div class="col-sm-1">
					<button type="button" id="btn_main_search" class="btn btn-sm btn-primary float-right">
						<i class="fa fa-search"></i>
					</button>
				</div>
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

<script type="text/javascript">
$(document).ready(function () {
    $("#btn_main_search").on("click", function () {
    	fn_kisDailyPriceGridData();
    });

    // 엔터키로도 조회
    $("#in_stockCode").on("keydown", function (e) {
        if (e.keyCode === 13) {
        	fn_kisDailyPriceGridData();
        }
    });
    // 페이지 로딩 시, 오늘 기준 60일 자동 세팅
    setDefaultDates();

    $("#btn_today").on("click", function () {
        setDefaultDates();
    });

    $("#btn_main_search").on("click", function () {
        requestItemchartprice();
    });
    $('#in_stockCode').val("005930");
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
	var url = "/scheduler/finance/kisItemchartpriceData.do";
	//조회 조건
	var param = {
			 	"in_stockCode" 		: $('#in_stockCode').val(),
			 	"in_fromDate" 		: $('#in_fromDate').val(),
			 	"in_toDate" 		: $('#in_toDate').val(),
			 	"in_periodDivCode" 	: $('#in_period').val(),
			 	"in_orgAdjPrc" 		: $('#in_orgAdj').val()
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

function setDefaultDates() {
    var today = new Date();
    var end = formatDateYYYYMMDD(today);

    var past = new Date();
    past.setDate(past.getDate() - 60);
    var start = formatDateYYYYMMDD(past);
    $("#in_fromDate").val(start);
    $("#in_toDate").val(end);
}

function formatDateYYYYMMDD(d) {
    var yyyy = d.getFullYear().toString();
    var mm = (d.getMonth() + 1).toString();
    var dd = d.getDate().toString();
    if (mm.length === 1) mm = "0" + mm;
    if (dd.length === 1) dd = "0" + dd;
    return yyyy + mm + dd;
}

</script>
