<%@ page contentType="text/html; charset=utf-8" %>
<div class="container-fluid">
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
			<div class="form-group row">
				<label for="sel_optionname" class="col-sm-1 col-form-label" style="text-align: right;">옵션 명</label>
				<div class="col-sm-3">
					<input type="text" id="sel_optionname" name="sel_optionname" onkeyup="fn_OptionGridOneData();"class="form-control input-sm" placeholder="옵션 명">
				</div>
				<label for="sel_useyn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="sel_useYn" onchange="fn_OptionGridOneData();">
					</select>
				</div>
				<div class="col-sm-6">
					<button type="button" id="btn_main_search"
						class="btn btn-info float-right">
						<i class="fa fa-search"></i>
					</button>
				</div>
			</div>
		</div>
	</div>

	<div class="row">
		<div class="col-4">
			<div class="card card-info">
					<div class="card-body table-responsive p-2">
						<div class="card-controls"  style="text-align: right">
							<button type="button" class="btn bg-default btn-sm" onclick="fn_pop_OptionInsert('main');">
								<i class="fas fa-plus right"></i>
							</button>
						</div>
						<table class="table table-sm" id="optionGridOneData">
							<thead>
								<tr>
									<th>옵션 순서</th>
									<th>옵션 ID</th>
									<th>옵션 명</th>
									<th>사용 여부</th>
									<th></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div class="col-8">
			<div class="card">
				<div class="card-body table-responsive p-2">
				<div class="card-controls" style="text-align: right">
					<button type="button" class="btn bg-default btn-sm " onclick="fn_pop_OptionInsert('sub');">
						<i class="fas fa-plus"></i>
					</button>
				</div>
					<table class="table table-sm" id="optionGridTwoData" >
						<thead>
							<tr>
								<th>옵션 순서</th>
								<th>옵션 ID</th>
								<th>옵션 명</th>
								<th>옵션 값</th>
								<th>Default</th>
								<th>사용 여부</th>
								<th></th>
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

<div class="modal fade" id="modal_option_InfoPop" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title" id="option_info_title">옵션 등록</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row">
							<label for="in_sortorder" class="col-sm-3 col-form-label" style="text-align: right;">옵션 순서</label>
							<div class="col-sm-3">
								<input type="hidden" id="in_optionseq">
								<input type="text" id="in_sortorder" class="form-control input-sm" placeholder="옵션 순서">
							</div>
						</div>
						<div class="form-group row" id="row_optionparent">
							<label for="in_optionparent" class="col-sm-3 col-form-label" style="text-align: right;">옵션 그룹</label>
							<div class="col-sm-4" id="OPTIONGROUP">
								<select class="form-control custom-select" id="in_optionparent">
								</select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_optionid" class="col-sm-3 col-form-label" style="text-align: right;">옵션 ID</label>
							<div class="col-sm-4">
								<input type="text" id="in_optionid" class="form-control input-sm" placeholder="옵션 ID">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_optionname" class="col-sm-3 col-form-label" style="text-align: right;">옵션 명</label>
							<div class="col-sm-5">
								<input type="text" id="in_optionname" class="form-control input-sm" placeholder="옵션 명">
							</div>
						</div>
						<div class="form-group row" id="row_optionvalue">
							<label for="in_optionvalue" class="col-sm-3 col-form-label" style="text-align: right;">옵션 값</label>
							<div class="col-sm-7">
								<input type="text" id="in_optionvalue" class="form-control input-sm" placeholder="옵션 값">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_optionaccess" class="col-sm-3 col-form-label" style="text-align: right;">옵션 권한</label>
							<div class="col-sm-4" id="ROLE">
								<select class="form-control custom-select" id="in_optionaccess"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_useyn" class="col-sm-3 col-form-label" style="text-align: right;">사용여부</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useyn" class="flat-red" placeholder="옵션 명">
							</div>
						</div>
						<div class="form-group row" id="row_default">
							<label for="in_default" class="col-sm-3 col-form-label" style="text-align: right;">DEFAULT</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_default" class="flat-red" placeholder="DEFAULT">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_optiondesc" class="col-sm-3 col-form-label" style="text-align: right;">옵션 설명</label>
							<div class="col-sm-8">
								<input type="text" id="in_optiondesc" class="form-control input-sm" placeholder="옵션 설명">
							</div>
						</div>
					</form>
			  	</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_eventdiv" value="">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-danger" id="btn_del"data-dismiss="modal">삭제</button>
					<button type="button" class="btn btn-primary" id="btn_save"data-dismiss="modal">Save&Changes</button>
				</div>
			</div>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>
<!-- /.modal -->
<script>

//각 영역들을 미리 선언
var div_mgr_option_main = $('#div_mgr_option_main');
var div_option_InfoPop = $('#modal_option_InfoPop');
var grid_div ="";
function mgr_option_initEvent(){
	
	/* 조회버튼 클릭시 */
	div_mgr_option_main.on('click', '#btn_main_search', function(){
		fn_OptionGridOneData();
	});
	
	/* 등록버튼 클릭시 */
	div_option_InfoPop.on('click', '#btn_save', function(){
		fn_optionSave();
	});

	/* 삭제 버튼 클릭시 */
	div_option_InfoPop.on('click', '#btn_del', function(){
		fn_optionDelete();
	});
}

	function fn_OptionGridOneData() {
		// 호출 URL
		var url = "/scheduler/finance/selectOptionList.do";
		//조회 조건
		var param = {
	                "sel_optiondiv" 		: "main",
	                "sel_optionname" 		: $('#sel_optionname').val(),
	                "sel_useyn" 			: $('#sel_useyn').val()
				};
		//컬럼 옵션
		var columns = [
						
						{"data": "sort_order"},
						{"data": "option_id"},
						{"data": "option_nm"},
						{"data": "use_flag"},
						{"data": "option_seq",
							"render": function(data, type, row){
								data = '<a class="btn btn-info btn-xs" href="#">'
									  +'<i class="fas fa-pencil-alt"></i> Edit</a>'
								return data;
							}
						}
		           ];
		var columnDefs = [
							{
							    "targets": [0,3,4],
							    "className": "text-center",
							}
						];
		var order = [[1, 'desc']];
		var gridObj = {
		        'grid_id': "optionGridOneData",
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
		        'cursorCols': [2]
	    };
		dataTableGridNew(gridObj,gridOptions);
	}
	var oneRowdata ="";
	// 마우스 클릭 이벤트 핸들러
	$('#optionGridOneData tbody').on( 'click', 'tr', function () {
			var table = $("#optionGridOneData").dataTable();
			// 현재 클릭한 행
		    var $row = $(this);
		    // 다른 행들의 선택 상태를 해제
		    table.$('tr.selected').not($row).removeClass('selected');
		    // 현재 행의 선택 상태를 토글
		    $row.toggleClass('selected');
		    
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
			
			var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
			if(column_index == '4'){
				fn_pop_OptionUpdate("main",row_data);
			}else{
				fn_OptionGridTwoData(row_data.option_seq);
				oneRowdata = row_data;
			}
	});
	
	function fn_OptionGridTwoData(data) {
		
		// 호출 URL
		var url = "/scheduler/finance/selectOptionList.do";
		//조회 조건
		var param = {
	                "sel_parentid" 	: data,
	                "sel_optiondiv" : "sub",
	                "sel_useyn" 	: $('#sel_useyn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": "sort_order"},
						{"data": "option_id"},
						{"data": "option_nm"},
						{"data": "option_value"},
						{"data": "default_yn"},
						{"data": "use_flag"},
						{"data": "use_flag",
							"render": function(data, type, row){
								data = '<a class="btn btn-info btn-xs" href="#">'
								 +'<i class="fas fa-pencil-alt">'
	                             +'</i> Edit</a>'	
								return data;
							}
						}
						
		           ];
		var columnDefs = [
							{
							    "targets": [0,3,4,5],
							    "className": "text-center",
							}
						];
		var order = [[1, 'desc']];
		
		var gridObj = {
		        'grid_id': "optionGridTwoData",
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
		        'cursorCols': [2]
	    };
		dataTableGridNew(gridObj, gridOptions);
	}

	$('#optionGridTwoData tbody').on( 'click', 'tr', function () {

		var optionGridTwoData = $("#optionGridTwoData").dataTable();
	   	var row_position = optionGridTwoData.fnGetPosition(this);
   		var row_data = optionGridTwoData.fnGetData(row_position);
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == '6'){
			fn_pop_OptionUpdate("sub",row_data);
		}
	});

	/* 옵션 중복 확인 */
	function fn_OptionCheck(){
		var option_id = div_option_InfoPop.find('#txt_insupd_option').val();
		if(option_id == ""){
			alert("옵션를 입력하세요.");
			return false;
		}
	
		var url = "/scheduler/finance/selectOptionCheck.do";
		var param = "option_id="+option_id;
		var type = "json";
		
		ajaxCall(url, type, param, fn_duplicateUser);
		
		function fn_duplicateUser(data){
			if(data.result == "사용중인 옵션 입니다. 옵션명을 변경해주세요."){
				mgr_option_duplicateUser = "N";
			}else{
				mgr_option_duplicateUser = "Y";
			}
		}
	}
	
	function fn_pop_OptionUpdate(div,row_data){
		grid_div = div;
		/* 신규 등록 셋팅 */
		$('.modal-body form').each(function() {
      		this.reset();
  		});
		if(div =='main'){
			$('#row_optionparent').hide();
			$('#row_optionvalue').hide();
			$('#row_default').hide();
			$("#in_optionparent").val("");
			$("#in_optionvalue").val("");
			$("#in_default").val("");
		}else{
			$('#row_optionparent').show();
			$('#row_optionvalue').show();
			$('#row_default').show();
			$("#in_optionparent").val(row_data.parent_id);
			$("#in_optionvalue").val(row_data.option_value);
			//$("#in_default").val(row_data.default_yn);
			setCheckBox(row_data.default_yn , div_option_InfoPop, '#in_default');
		}
		
		$("#in_sortorder").val(row_data.sort_order);
		$("#in_optionseq").val(row_data.option_seq);
		$("#in_optionid").val(row_data.option_id);
		$("#in_optionname").val(row_data.option_nm);
		$("#in_optionaccess").val(row_data.access_right);
		setCheckBox(row_data.use_flag , div_option_InfoPop, '#in_useyn');
		$("#in_optiondesc").val(row_data.option_desc);
		$("#in_eventdiv").val("update");
		$("#option_info_title").text("옵션 수정");
		$("#modal_option_InfoPop").modal();
	}
	
	function fn_pop_OptionInsert(div){
		if(div != "main" && oneRowdata == ""){
			alert("Main Option를 먼저 선택해주세요.");
			return false;
		}
		
		grid_div = div;
		/* 신규 등록 셋팅 */
		$('.modal-body form').each(function() {
      		this.reset();
  		});
		$("#in_eventdiv").val("insert");
		fn_optionSeq();
		if(div =='main'){
			$('#row_optionparent').hide();	
			$('#row_optionvalue').hide();	
			$('#row_default').hide();	
			$("#in_optionparent").val("");
			$("#in_optionvalue").val("");
			$("#in_default").val("");
		}else{
			$('#row_optionparent').show();
			$('#row_optionvalue').show();
			$('#row_default').show();
			if(oneRowdata.option_seq != undefined){
				$("#in_optionparent").val(oneRowdata.option_seq).prop("selected", true);
			}
		}
		
		$("#option_info_title").text("옵션 등록");
		$("#modal_option_InfoPop").modal();
	}
	/* 옵션 SEQ */ 
	function fn_optionSeq(){
		var param = "parent_id="+oneRowdata.option_seq+"&grid_div="+grid_div;
		var type = "json";
		var url = "/scheduler/finance/selectOptionSeq.do";
		
		ajaxCall(url, type, param, fn_optionSeqResult);
		function fn_optionSeqResult(result){
			$("#in_sortorder").val(result.option_seq);
		}
	}
	/* 옵션 신규 등록 및 수정 */ 
	function fn_optionSave(){
		var useyn =$("input:checkbox[id=in_useyn]").is(":checked");
		var defaultyn =$("input:checkbox[id=in_default]").is(":checked");
		var param = getJQParams(div_option_InfoPop)+"&useyn="+useyn+"&defaultyn="+defaultyn;
		var type = "script";
		var url = "/scheduler/finance/saveOption.do";
		ajaxCall(url, type, param, fn_option_save_result);
		function fn_option_save_result(){
			//$("#modal_option_InfoPop").modal('hide');			//option 수정 Popup Open
			if(grid_div =="main"){
				fn_OptionGridOneData();
			}else{
				if(oneRowdata.option_seq != undefined){
					fn_OptionGridTwoData(oneRowdata.option_seq);	
				}
			}
			setCheckBox("N" , div_option_InfoPop, '#in_default');
			setCheckBox("N" , div_option_InfoPop, '#in_useyn');
			fn_optionGroupCode(div_option_InfoPop,'#OPTIONGROUP', 'SEARCH');
		}
	}
	/* 옵션 삭제 */ 
	function fn_optionDelete(){
		var param = getJQParams(div_option_InfoPop);
		var type = "json";
		var url = "/scheduler/finance/deleteOption.do";
		ajaxCall(url, type, param, fn_option_delete_result);
		function fn_option_delete_result(){
			$("#modal_option_InfoPop").modal('hide');			//option 수정 Popup Open
			if(grid_div =="main"){
				fn_OptionGridOneData();
			}else{
				if(oneRowdata.option_seq != undefined){
					fn_OptionGridTwoData(oneRowdata.option_seq);	
				}
			}
		}
	}
	/* page loading 시 검색 영역의 selectbox Setting */
	function fn_optionGroupCode(div, id, flag){
		$.ajax({
			type:"POST",
			url:"/scheduler/finance/selectOptionGroup.do",
			dataType:"html",
			data : "flag=" + flag,
			success:function(msg){
				div.find(id+' select').html(msg);
			}
		});
	}
	
	function mgr_option_initProp(){
		fn_optionGroupCode(div_option_InfoPop,'#OPTIONGROUP', 'SEARCH');
		//getItem_select(div_option_InfoPop, 'ROLE');
		fn_OptionGridOneData();
		fn_OptionGridTwoData('1');
	}

	function mgr_option_init(){
		mgr_option_initProp();
		mgr_option_initEvent();
	}
	
	$(function () {
	    //Flat red color scheme for iCheck
	    $('input[type="checkbox"].flat-red, input[type="radio"].flat-red').iCheck({
	      checkboxClass: 'icheckbox_flat-green',
	      radioClass   : 'iradio_flat-green'
	    })
		$('.select2').select2();
	});
	
mgr_option_init();
// Jquery draggable
$('.modal-dialog').draggable({
    handle: ".modal-header"
});
</script>
<style>
tr.selected {
  background-color: #c8e6c9;
}
</style>

