<%@ page contentType="text/html; charset=utf-8" %>
<div class="container-fluid" id="div_stock_main">
	<div class="card card-info">
		<div class="card-header">
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
				<label for="in_usr_id" class="col-sm-1 col-form-label" style="text-align: right;">종목 정보</label>
				<div class="col-sm-2">
					<input type="text" id="in_stock_code" name="in_stock_code" onkeyup="fn_stockgridData();"class="form-control input-sm" placeholder="종목명 or 종목코드">
				</div>
				<label class="col-sm-1 col-form-label" for="in_useyn"  style="text-align: right;">국가</label>
				<div class="col-sm-1" id="">
					<select class="form-control custom-select" id="in_country" onchange="fn_stockgridData();">
						<option value="">ALL</option>
						<option value="KR">KR</option>
						<option value="US">US</option>
					</select>
				</div>
				<label class="col-sm-1 col-form-label" for="in_useyn"  style="text-align: right;">ORDER</label>
				<div class="col-sm-1" id="">
					<select class="form-control custom-select" id="in_column" onchange="fn_stockgridData();">
						<option value="STOCK_ID" selected="selected">종목ID</option>
						<option value="STOCK_CODE">종목코드</option>
						<option value="STOCK_KO_NAME">종목명</option>
						<option value="STOCK_EN_NAME">종목영문명</option>
						<option value="STOCK_COUNTRY_CODE">국가</option>
						<option value="STOCK_MARKET">Market</option>
						<option value="STOCK_TYPE">종목타입</option>
						<option value="STOCK_TYPE_SPECS">종목스팩</option>
						<option value="STOCK_SECTOR">종목섹터</option>
						<option value="STOCK_ALIASES">Aliases</option>
					</select>
				</div>
				<div class="col-sm-1 d-flex align-items-center">
					<input type="checkbox" id="in_order" name="in_order" value="ASC" checked data-bootstrap-switch>
				</div>
				<label for="in_useyn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1" id="USE_YN">
					<select class="form-control custom-select" id="in_useyn" onchange="fn_stockgridData();">
					</select>
				</div>
				<div class="col-sm-1">
					<button type="button" id="btn_main_search" class="btn btn-info float-right">
						<i class="fa fa-search"></i>
					</button>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-12">
			<div class="card card-info">
					<div class="card-body table-responsive p-2">
						<div class="card-controls"  style="text-align: right">
							<button type="button" class="btn btn-sm bg-info"
								data-toggle="modal" data-target="#modal_stock_refresh" id="btn_main_refresh">
								<i class="fa fa-fw fa-refresh" aria-hidden="true"></i>
							</button>
							<button type="button" class="btn btn-sm bg-danger" id="btn_main_delete">
								<i class="fa fa-fw fa-trash-o"></i>
							</button>
						</div>
						<table class="table table-sm" id="stockgridData">
							<thead>
								<tr class="review">
									<th></th>
									<th>종목ID</th>
									<th>종목코드</th>
									<th>종목명</th>
									<th>종목영문명</th>
									<th>국가</th>
									<th>Market</th>
									<th>종목타입</th>
									<th>종목스펙</th>
									<th>종목섹터</th>
									<th>Aliases</th>
									<th>종목설명</th>
									<th>사용여부</th>
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

<!-- 목록갱신 POPUP -->
<div class="modal fade" id="modal_stock_refresh">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Symbol Refresh</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
					<div class="form-group row">
						<label for="in_stockGlobal" class="col-sm-3 control-label"><span>*</span>GLOBAL</label>
						<div class="col-lg-8">
							<select class="form-control custom-select" id="in_stockGlobal">
								<option value="korea">KR</option>
								<option value="us">US</option>
							</select>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_stockMarket" class="col-sm-3 control-label"><span>*</span>Market</label>
						<div class="col-lg-8">
							<select class="form-control custom-select" id="in_stockMarket">
								<option value="kospi">kospi</option>
								<option value="kosdaq">kosdaq</option>
								<option value="snp500">S&P500</option>
							</select>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer  justify-content-between">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				<button type="button" class="btn btn-primary" id="btn_main_Send" onclick="fn_stockEquitiesALL();">Send</button>
			</div>
		</div>
	</div>
</div>

<!-- 수정 POPUP -->
<div class="modal fade" id="modal_stock_InfoPop">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">수정</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
	    			<div class="form-group row" style="margin-bottom: 4px;">
						<label for="txt_stock_code" class="col-sm-3 control-label"><span>*</span>종목 코드</label>
						<div class="col-lg-8 mb-3">
							<div class="input-group">
								<input type="text" class="form-control form-control-sm" id="txt_stock_code" onkeypress="fn_enter(fn_duplicate);">
								<div class="input-group-append">
									<span class="input-group-text" style="cursor: pointer;" id="btn_id__duplicate"><i class="fas fa-check"></i></span>
								</div>
							</div>
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_ko_name" class="col-sm-3 control-label"><span>*</span>코드명</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_ko_name"  name="txt_stock_ko_name" class="form-control form-control-sm" placeholder="Code Ko Name">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_en_name" class="col-sm-3 control-label"><span>*</span>코드영문명</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_en_name"  name="txt_stock_en_name" class="form-control form-control-sm" placeholder="Code En Name">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_country_code" class="col-sm-3 control-label">국가</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_country_code"  name="txt_stock_country_code" class="form-control form-control-sm" placeholder="국가">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_market" class="col-sm-3 control-label">Market</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_market"  name="txt_stock_market" class="form-control form-control-sm" placeholder="Market">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_aliases" class="col-sm-3 control-label">Aliases</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_aliases"  name="txt_stock_aliases" class="form-control form-control-sm" placeholder="Market Section">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_type" class="col-sm-3 control-label">코드 타입</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_type"  name="txt_stock_type" class="form-control form-control-sm" placeholder="코드 타입">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_type_specs" class="col-sm-3 control-label">코드 스펙</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_type_specs"  name="txt_stock_type_specs" class="form-control form-control-sm" placeholder="코드 스펙">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_sector" class="col-sm-3 control-label">코드 섹터</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_sector"  name="txt_stock_sector" class="form-control form-control-sm" placeholder="코드 섹터">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_stock_desc" class="col-sm-3 control-label">종목 설명</label>
						<div class="col-lg-8">
							<input type="text" id="txt_stock_desc"  name="txt_stock_desc" class="form-control form-control-sm" placeholder="종목 설명">
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_modify_date" class="col-sm-3 control-label">수정날짜</label>
						<div class="col-lg-8">
							<input type="text" id="txt_modify_date"  name="txt_modify_date" class="form-control form-control-sm" placeholder="수정 날짜">
						</div>
					</div>
					<div class="form-group row">
						<label for="chk_useYn" class="col-sm-3 control-label">사용여부</label>
						<div class="col-lg-8">
							<label id="check_label">
			                	<input type="checkbox" id="chk_useYn" class="flat-red" >
			                </label>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_eventdiv" value="">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-primary" id="btn_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>
<script>
	
	var modal_stock_InfoPop = $('#modal_stock_InfoPop');	//등록/수정 POPUP
	/* 아이디 중복확인 확인유무를 위한 변수 */
	var eventInfo = "N";
	
	/* 각 역영들을 미리 선언 */
	var div_stock_main = $('#div_stock_main');
	
	function fn_stock_initEvent() {
	
		/* 조회 버튼 */
		div_stock_main.on('click', '#btn_main_search', function() {
			fn_stockgridData();
		});
		
		/* 삭제버튼 */
		div_stock_main.on('click', '#btn_main_delete', function() {
			fn_mgr_stockdelete();
		});
	
		/* 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
		modal_stock_InfoPop.on('click', '#btn_save', function() {
			fn_updateStockCode();
		});
	}
	/************************************************************************
	* stock list
	*************************************************************************/
	function fn_stockgridData() {
		// 호출 URL
		var url = "/scheduler/stockManagement/selectStockList.do";
		//조회 조건
		var param = {
				 	"in_stock_code"	: $('#in_stock_code').val(),
				 	"in_country"	: $('#in_country').val(),
				 	"in_column"	: $('#in_column').val(),
				 	"in_order"	: $('#in_order').val(),
				 	"in_useyn"	: $('#in_useyn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": "rnum"},
						{"data": "stock_id"},
						{"data": "stock_code"},
						{"data": "stock_ko_name"},
						{"data": "stock_en_name"},
						{"data": "stock_country_code"}, 
						{"data": "stock_market"},
						{"data": "stock_type"},
						{"data": "stock_type_specs"},
						{"data": "stock_sector"},
						{"data": "stock_aliases"},
						{"data": "stock_desc"},
						{"data": "use_flag"}
		           ];
		var columnDefs = [
							{
							    'targets': 0,
							    "orderable": false,  // 0번 컬럼을 정렬 불가능하게 설정
							    "className": "text-center",
							    'checkboxes': {
							       'selectRow': true,
							       'selectCallback': function(nodes, selected){
							          $('input[type="checkbox"]', nodes).iCheck('update');
							       },
							       'selectAllCallback': function(nodes, selected, indeterminate){
							          $('input[type="checkbox"]', nodes).iCheck('update');
							       }
							    }
							 },
							 {
							    'targets': [1,2,5,6,7,8,12],
							    "className": "text-center"
							 },
							 {
							    'targets': [3,4,11],
					      		"render": function ( data, type, row ) {
					      			if(empty.isEmpty(data) == false){
					      				return type === 'display' && data.length > 20 ? data.substr( 0, 20 ) +'…' : data;	
					      			}
					      			return data;
				      		  	}
						    }
						];

		var gridObj = {
		        'grid_id': "stockgridData",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs
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
			    'rowReorder': true,
			    'cursorCols': [2, 3, 4]
		};
		
		dataTableGrid(gridObj, gridOptions);
	}
	
 	$('#stockgridData tbody').on( 'dblclick', 'tr', function () {
 		var table = $("#stockgridData").dataTable();
		// 현재 클릭한 행
	    var $row = $(this);
	    // 다른 행들의 선택 상태를 해제
	    table.$('tr.selected').not($row).removeClass('selected');
	    // 현재 행의 선택 상태를 토글
	    $row.toggleClass('selected');	    
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index <= 4){
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
			
			$('.modal-body form').each(function() {
				/* checkBox setting */
				setCheckBox("N", modal_stock_InfoPop, '#chk_useYn');
	      		this.reset();
	  		});
			
			modal_stock_InfoPop.find('#txt_stock_code').val(row_data.stock_code).attr("readonly", "readonly");
			modal_stock_InfoPop.find('#txt_stock_ko_name').val(row_data.stock_ko_name);
			modal_stock_InfoPop.find('#txt_stock_en_name').val(row_data.stock_en_name);
			modal_stock_InfoPop.find('#txt_stock_country_code').val(row_data.stock_country_code);
			modal_stock_InfoPop.find('#txt_stock_market').val(row_data.stock_market);
			modal_stock_InfoPop.find('#txt_stock_aliases').val(row_data.stock_aliases);
			/* modal_stock_InfoPop.find('#txt_market_section').val(row_data.market_section); */
			modal_stock_InfoPop.find('#txt_stock_type').val(row_data.stock_type);
			modal_stock_InfoPop.find('#txt_stock_type_specs').val(row_data.stock_type_specs);
			modal_stock_InfoPop.find('#txt_stock_sector').val(row_data.stock_sector);
			modal_stock_InfoPop.find('#txt_stock_desc').val(row_data.stock_desc);
			modal_stock_InfoPop.find('#txt_modify_date').val(row_data.modify_date);
			
			/* checkBox setting */
			setCheckBox(row_data.use_flag , modal_stock_InfoPop, '#chk_useYn');
			
			$("#modal_stock_InfoPop").modal();			//사용자 수정 Popup Open
			eventInfo ="M";
		}
	});

	/*종목 수정 */
	function fn_updateStockCode(){

		setCheckBoxYN(modal_stock_InfoPop, '#chk_useYn');
		
		var param = getJQParams(modal_stock_InfoPop);
		var type = "script";
		var url = "/scheduler/stockManagement/updateStockCode.do";
		ajaxCall(url, type, param, fn_save_stock_Result);
		function fn_save_stock_Result(data){
			$('#modal_stock_InfoPop').modal("hide"); //Popup Close
			fn_stockgridData();
		}
	}

	/* stock Market Crawler 종목 갱신및가져오기 */
	function fn_stockEquitiesALL() {
		if(confirm("종목 갱신하시겠습니까?")== false){
			return false;
		};
		$('#modal_stock_refresh').modal("hide"); //Popup Close
		var in_stockGlobal = $("#in_stockGlobal").val();
		var in_stockMarket = $("#in_stockMarket").val();
		var url = "/scheduler/stockManagement/stockEquitiesALL.do";
		var param = "global=" + in_stockGlobal +"&market=" + in_stockMarket;
		var type = "json";
		ajaxCall(url, type, param, fn_stockEquitiesALLResult);
		function fn_stockEquitiesALLResult(data){
			console.log(data);
		}
	}
	/* 종목 삭제 */
	function fn_mgr_stockdelete() {
		var stockgridData = $("#stockgridData").dataTable();
		var dellist = fn_SelectGridList("삭제",stockgridData);
		if(dellist.length > 0){
			var url = "/scheduler/stockManagement/deleteStockCode.do";
			var param = "deleteList=" + dellist;
			var type = "script";
			ajaxCall(url, type, param, fn_stockgridData);
		}else{
			alert("삭제 목록이 없습니다.")
		}
	}
	
	function fn_stock_initProp() {
		getItem('USE_YN');
		fn_stockgridData();
	}
	function fn_stock_init() {		
		fn_stock_initProp();
		fn_stock_initEvent();
	}
	fn_stock_init();
	
	$(function () {
	    //Flat red color scheme for iCheck
	    $('input[type="checkbox"].flat-red, input[type="radio"].flat-red').iCheck({
	      checkboxClass: 'icheckbox_flat-green',
	      radioClass   : 'iradio_flat-green'
	    })
		$('.select2').select2();
	});
	$('.modal-dialog').draggable({
	    handle: ".modal-header"
	});
	
	$("input[data-bootstrap-switch]").each(function(){
		$("[name='in_order']").bootstrapSwitch({
	        onText: 'ASC',
	        offText: 'DESC'
	    });
      $(this).bootstrapSwitch('state', $(this).prop('checked'));
    });
	   // 스위치의 값(value)을 받아오는 방법
    $("[name='in_order']").on('switchChange.bootstrapSwitch', function(event, state) {
        let value = state ? 'ASC' : 'DESC';
        $("#in_order").val(value);
        fn_stockgridData();
    });
    
</script>
