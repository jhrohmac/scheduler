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
			<div class="form-group search_row">
				<label for="sel_codename" class="col-sm-1 col-form-label" style="text-align: right;">코드 명</label>
				<div class="col-sm-3">
					<input type="text" id="sel_codename" name="sel_codename" onkeyup="fn_codegridOneData();"class="form-control input-sm" placeholder="코드 명">
				</div>
				<label for="sel_useyn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select" id="sel_useyn" onchange="fn_codegridOneData();">
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
							<button type="button" class="btn bg-default btn-sm bg-info" onclick="fn_pop_CodeInsert('main');">
								<i class="fa fa-fw fa-plus right" aria-hidden="true"></i>
							</button>
						</div>
						<table class="table table-sm" id="codegridOneData">
							<thead>
								<tr>
									<th>코드 순서</th>
									<th>코드 ID</th>
									<th>코드 명</th>
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
					<button type="button" class="btn btn-sm bg-info" onclick="fn_pop_CodeInsert('sub');">
						<i class="fa fa-fw fa-plus right" aria-hidden="true"></i>
					</button>
				</div>
					<table class="table table-sm" id="codegridTwoData" >
						<thead>
							<tr>
								<th>코드 순서</th>
								<th>코드 ID</th>
								<th>코드 명</th>
								<th>코드 값</th>
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

<div class="modal fade" id="modal_code_InfoPop">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title" id="code_info_title">코드 등록</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row">
							<label for="in_sortorder" class="col-sm-3 col-form-label" style="text-align: right;">코드 순서</label>
							<div class="col-sm-3">
								<input type="hidden" id="in_codeseq">
								<input type="text" id="in_sortorder" class="form-control input-sm" placeholder="코드 순서">
							</div>
						</div>
						<div class="form-group row" id="row_codeparent">
							<label for="in_codeparent" class="col-sm-3 col-form-label" style="text-align: right;">코드 그룹</label>
							<div class="col-sm-4" id="CODEGROUP">
								<select class="form-control custom-select" id="in_codeparent">
								</select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_codeid" class="col-sm-3 col-form-label" style="text-align: right;">코드 ID</label>
							<div class="col-sm-4">
								<input type="text" id="in_codeid" class="form-control input-sm" placeholder="코드 ID">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_codename" class="col-sm-3 col-form-label" style="text-align: right;">코드 명</label>
							<div class="col-sm-5">
								<input type="text" id="in_codename" class="form-control input-sm" placeholder="코드 명">
							</div>
						</div>
						<div class="form-group row" id="row_codevalue">
							<label for="in_codevalue" class="col-sm-3 col-form-label" style="text-align: right;">코드 값</label>
							<div class="col-sm-7">
								<input type="text" id="in_codevalue" class="form-control input-sm" placeholder="코드 값">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_codeaccess" class="col-sm-3 col-form-label" style="text-align: right;">코드 권한</label>
							<div class="col-sm-4 common select-all" id="ROLE">
								<select class="form-control custom-select" id="in_codeaccess"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_useyn" class="col-sm-3 col-form-label" style="text-align: right;">사용여부</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useyn" class="flat-red" placeholder="코드 명">
							</div>
						</div>
						<div class="form-group row" id="row_default">
							<label for="in_default" class="col-sm-3 col-form-label" style="text-align: right;">DEFAULT</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_default" class="flat-red" placeholder="DEFAULT">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_codedesc" class="col-sm-3 col-form-label" style="text-align: right;">코드 설명</label>
							<div class="col-sm-8">
								<input type="text" id="in_codedesc" class="form-control input-sm" placeholder="코드 설명">
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
var div_mgr_code_main = $('#div_mgr_code_main');
var div_code_InfoPop = $('#modal_code_InfoPop');
var grid_div ="";
function mgr_code_initEvent(){
	
	/* 조회버튼 클릭시 */
	div_mgr_code_main.on('click', '#btn_main_search', function(){
		fn_codegridOneData();
	});
	
	/* 등록버튼 클릭시 */
	div_code_InfoPop.on('click', '#btn_save', function(){
		fn_codeSave();
	});

	/* 삭제 버튼 클릭시 */
	div_code_InfoPop.on('click', '#btn_del', function(){
		fn_codeDelete();
	});
}

	function fn_codegridOneData() {
		// 호출 URL
		var url = "/scheduler/finance/selectCodeList.do";
		//조회 조건
		var param = {
	                "sel_codediv" 		: "main",
	                "sel_codename" 		: $('#sel_codename').val(),
	                "sel_useyn" 		: $('#sel_useyn').val()
				};
		//컬럼 옵션
		var columns = [
						
						{"data": "sort_order"},
						{"data": "code_id"},
						{"data": "code_nm"},
						{"data": "use_flag"},
						{"data": "code_seq",
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
		        'grid_id': "codegridOneData",
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
	var oneRowdata ="";
	// 마우스 클릭 이벤트 핸들러
	$('#codegridOneData tbody').on( 'click', 'tr', function () {
			var table = $("#codegridOneData").dataTable();
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
				fn_pop_CodeUpdate("main",row_data);
			}else{
				fn_codegridTwoData(row_data.code_seq);
				oneRowdata = row_data;
			}
	});
	
	function fn_codegridTwoData(data) {
		
		// 호출 URL
		var url = "/scheduler/finance/selectCodeList.do";
		//조회 조건
		var param = {
	                "sel_parentid" 	: data,
	                "sel_codediv" 	: "sub",
	                "sel_useyn" 	: $('#sel_useyn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": "sort_order"},
						{"data": "code_id"},
						{"data": "code_nm"},
						{"data": "code_value"},
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
		        'grid_id': "codegridTwoData",
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

	$('#codegridTwoData tbody').on( 'click', 'tr', function () {

		var codegridTwoData = $("#codegridTwoData").dataTable();
	   	var row_position = codegridTwoData.fnGetPosition(this);
   		var row_data = codegridTwoData.fnGetData(row_position);
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == '6'){
			fn_pop_CodeUpdate("sub",row_data);
		}
	});

	/* 코드 코드 중복 확인 */
	function fn_CodeCheck(){
		var code_id = div_code_InfoPop.find('#txt_insupd_code').val();
		if(code_id == ""){
			alert("코드를 입력하세요.");
			return false;
		}
	
		var url = "/scheduler/finance/selectCodeCheck.do";
		var param = "code_id="+code_id;
		var type = "json";
		
		ajaxCall(url, type, param, fn_duplicateUser);
		
		function fn_duplicateUser(data){
			if(data.result == "사용중인 코드 입니다. 코드명을 변경해주세요."){
				mgr_code_duplicateUser = "N";
			}else{
				mgr_code_duplicateUser = "Y";
			}
		}
	}
	
	function fn_pop_CodeUpdate(div,row_data){
		grid_div = div;
		/* 신규 등록 셋팅 */
		$('.modal-body form').each(function() {
      		this.reset();
  		});
		if(div =='main'){
			$('#row_codeparent').hide();
			$('#row_codevalue').hide();
			$('#row_default').hide();
			$("#in_codeparent").val("");
			$("#in_codevalue").val("");
			$("#in_default").val("");
		}else{
			$('#row_codeparent').show();
			$('#row_codevalue').show();
			$('#row_default').show();
			$("#in_codeparent").val(row_data.parent_id);
			$("#in_codevalue").val(row_data.code_value);
			//$("#in_default").val(row_data.default_yn);
			setCheckBox(row_data.default_yn , div_code_InfoPop, '#in_default');
		}
		
		$("#in_sortorder").val(row_data.sort_order);
		$("#in_codeseq").val(row_data.code_seq);
		$("#in_codeid").val(row_data.code_id);
		$("#in_codename").val(row_data.code_nm);
		$("#in_codeaccess").val(row_data.access_right);
		setCheckBox(row_data.use_flag , div_code_InfoPop, '#in_useyn');
		$("#in_codedesc").val(row_data.code_desc);
		$("#in_eventdiv").val("update");
		$("#code_info_title").text("코드 수정");
		$("#modal_code_InfoPop").modal();
	}
	
	function fn_pop_CodeInsert(div){
		if(div != "main" && oneRowdata == ""){
			alert("Main Code를 먼저 선택해주세요.");
			return false;
		}
		
		grid_div = div;
		/* 신규 등록 셋팅 */
		$('.modal-body form').each(function() {
      		this.reset();
  		});
		$("#in_eventdiv").val("insert");
		fn_codeSeq();
		if(div =='main'){
			$('#row_codeparent').hide();	
			$('#row_codevalue').hide();	
			$('#row_default').hide();	
			$("#in_codeparent").val("");
			$("#in_codevalue").val("");
			$("#in_default").val("");
		}else{
			$('#row_codeparent').show();
			$('#row_codevalue').show();
			$('#row_default').show();
			if(oneRowdata.code_seq != undefined){
				$("#in_codeparent").val(oneRowdata.code_seq).prop("selected", true);
			}
		}
		
		$("#code_info_title").text("코드 등록");
		$("#modal_code_InfoPop").modal();
	}
	/* 코드 SEQ */ 
	function fn_codeSeq(){
		var param = "parent_id="+oneRowdata.code_seq+"&grid_div="+grid_div;
		var type = "json";
		var url = "/scheduler/finance/selectCodeSeq.do";
		
		ajaxCall(url, type, param, fn_codeSeqResult);
		function fn_codeSeqResult(result){
			$("#in_sortorder").val(result.code_seq);
		}
	}
	/* 코드 신규 등록 및 수정 */ 
	function fn_codeSave(){
		var useyn =$("input:checkbox[id=in_useyn]").is(":checked");
		var defaultyn =$("input:checkbox[id=in_default]").is(":checked");
		var param = getJQParams(div_code_InfoPop)+"&useyn="+useyn+"&defaultyn="+defaultyn;
		var type = "script";
		var url = "/scheduler/finance/saveCode.do";
		ajaxCall(url, type, param, fn_code_save_result);
		function fn_code_save_result(){
			//$("#modal_code_InfoPop").modal('hide');			//code 수정 Popup Open
			if(grid_div =="main"){
				fn_codegridOneData();
			}else{
				if(oneRowdata.code_seq != undefined){
					fn_codegridTwoData(oneRowdata.code_seq);	
				}
			}
			setCheckBox("N" , div_code_InfoPop, '#in_default');
			setCheckBox("N" , div_code_InfoPop, '#in_useyn');
			fn_mgr_code_get_codegroup(div_code_InfoPop,'#CODEGROUP', 'SEARCH');
		}
	}
	/* 코드 삭제 */ 
	function fn_codeDelete(){
		var param = getJQParams(div_code_InfoPop);
		var type = "json";
		var url = "/scheduler/finance/deleteCode.do";
		ajaxCall(url, type, param, fn_code_delete_result);
		function fn_code_delete_result(){
			$("#modal_code_InfoPop").modal('hide');			//code 수정 Popup Open
			if(grid_div =="main"){
				fn_codegridOneData();
			}else{
				if(oneRowdata.code_seq != undefined){
					fn_codegridTwoData(oneRowdata.code_seq);	
				}
			}
		}
	}
	/* page loading 시 검색 영역의 selectbox Setting */
	function fn_mgr_code_get_codegroup(div, id, flag){
		$.ajax({
			type:"POST",
			url:"/scheduler/finance/codeGroupCode.do",
			dataType:"html",
			data : "flag=" + flag,
			success:function(msg){
				div.find(id+' select').html(msg);
			}
		});
	}
	
	function mgr_code_initProp(){
		fn_mgr_code_get_codegroup(div_code_InfoPop,'#CODEGROUP', 'SEARCH');
		fn_codegridOneData();
		fn_codegridTwoData('1');
	}

	function mgr_code_init(){
		mgr_code_initProp();
		mgr_code_initEvent();
	}
mgr_code_init();


</script>
<style>
tr.selected {
  background-color: #c8e6c9;
}
</style>

