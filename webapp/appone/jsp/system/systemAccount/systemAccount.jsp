<%@ page contentType="text/html; charset=utf-8" %>
<div class="container-fluid" id="div_systemAccount_main">
	<div class="card card-primary card-outline">
		<div class="card-header pb-2 pt-2">
			<h5 class="card-title">조회 조건</h5>
			<div class="card-tools">
				<button type="button" class="btn btn-tool" data-card-widget="collapse">
					<i class="fas fa-minus"></i>
				</button>
			</div>
		</div>
		<div class="card-body p-2">
			<div class="form-group search_row">
				<label for="sel_keyword" class="col-sm-1 col-form-label" style="text-align: right;">Search</label>
				<div class="col-sm-3">
					<input type="text" id="sel_keyword" name="sel_keyword" 
						class="form-control form-control-sm" placeholder="LIKE Search....">
				</div>
				<label for="sel_systemGroup" class="col-sm-1 col-form-label" style="text-align: right;">GROUP</label>
				<div class="col-sm-1 common select-all" id="SYSTEMGROUP">
					<select class="form-control custom-select custom-select-sm" id="sel_systemGroup" onchange="handleSearch()();">
					</select>
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="sel_useYn" onchange="handleSearch()();">
					</select>
				</div>
				<div class="col-sm-4">
					<button type="button" id="btn_main_search" class="btn btn-sm btn-primary float-right">
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
						<button type="button" class="btn btn-xs btplus" id="main">
							<i class="fa fa-fw fa-plus text-blue right"></i>
						</button>
					</div>
					<table class="table table-hover table-sm" id="systemGrid">
						<thead>
							<tr>
								<th>No.</th>
								<th>SYSTEM</th>
								<th>NAME</th>
								<th>ADDRESS</th>
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
			<div class="card card-info">
				<div class="card-body table-responsive p-2">
					<div class="card-controls"  style="text-align: right">
						<button type="button" class="btn btn-xs btplus" id="sub">
							<i class="fa fa-fw fa-plus text-blue right"></i>
						</button>
					</div>
					<table class="table table-hover table-sm" id="systemAccountGrid">
						<thead>
							<tr>
								<th>SYSTEM</th>
								<th>ACCOUNT</th>
								<th>PASSWORD</th>								
								<th>ADDRESS</th>
								<th>PORT</th>
								<th>DESC</th>
								<th>Y/N</th>
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

<div class="modal fade" id="modal_info" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title" id="modal_info_title">등록</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span>&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row" id="row_systemFlag">
							<label for="in_systemFlag" class="col-sm-4 col-form-label" style="text-align: right;">GROUP</label>
							<div class="col-sm-6 common select-notall" id="SYSTEMGROUP">
								<select class="form-control custom-select" id="in_systemFlag"></select>
							</div>
						</div>						
						<div class="form-group row"  id="row_systemName">
							<label for="in_systemName" class="col-sm-4 col-form-label" style="text-align: right;">SYSTEM NAME</label>
							<div class="col-sm-6">
								<input type="text" id="in_systemName" class="form-control input-sm" placeholder="NAME">
							</div>
						</div>
						<div class="form-group row"  id="row_systemAccount">
							<label for="in_systemAccount" class="col-sm-4 col-form-label" style="text-align: right;">Account</label>
							<div class="col-sm-7">
								<input type="text" id="in_systemAccount" class="form-control input-sm" placeholder="Account">
							</div>
						</div>
						<div class="form-group row"  id="row_systemPassword">
							<label for="in_systemPassword" class="col-sm-4 col-form-label" style="text-align: right;">Password</label>
							<div class="col-sm-7">
								<input type="text" id="in_systemPassword" class="form-control input-sm" placeholder="패스워드">
							</div>
						</div>
						<div class="form-group row" id="row_systemAddress">
							<label for="in_systemAddress" class="col-sm-4 col-form-label" style="text-align: right;">ADDRESS</label>
							<div class="col-sm-6">
								<div class="input-group">
									<input type="text" class="form-control input-sm" id="in_systemAddress" placeholder="Address">
								</div>
							</div>
							<div class="col-sm-2" id="row_systemPort">
								<div class="input-group">
									<input type="text" class="form-control input-sm" id="in_systemPort" placeholder="PORT" maxlength="4">
								</div>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_systemDesc" class="col-sm-4 col-form-label" style="text-align: right;">DESC</label>
							<div class="col-sm-8">
								<input type="text" id="in_systemDesc" class="form-control input-sm" placeholder="기타">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_useYn" class="col-sm-4 col-form-label" style="text-align: right;">Use YN</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useYn" class="flat-red">
							</div>
						</div>
						<input type="hidden" id="in_eventDiv" value="">
						<input type="hidden" id="in_parentId" value="">
						<input type="hidden" id="in_systemSeq" value="">
					</form>
			  	</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-danger" id="btn_del">삭제</button>
					<button type="button" class="btn btn-sm btn-primary" id="btn_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>
<input type="hidden" id="grid_div" value="">
<script>

	//각 영역들을 미리 선언
	var div_systemAccount_main = $('#div_systemAccount_main');
	var modal_info = $('#modal_info');
	var tempData ="";
	var duplicate = "N";
	
	$(function () {
	    /* 검색 키워드 입력 또는 조회 버튼 클릭 시 */
	    div_systemAccount_main.on('keyup', '#sel_keyword',  function(){
	    	tempData ="";
	    	$("#grid_div").val("");
	    	handleSearch();
	    });
	    div_systemAccount_main.on('click', '#btn_main_search', function(){
	    	tempData ="";
	    	$("#grid_div").val("");
	    	handleSearch();
	    });
		
		/* 등록버튼 클릭시 */
		modal_info.on('click', '#btn_save', function(){
			fn_saveSystemAccount();
		});
	
		/* 삭제 버튼 클릭시 */
		modal_info.on('click', '#btn_del', function(){
			fn_deleteSystemAccount();
		});
		
		$("#in_systemFlag").change(function(){
			fn_showHide(this.value);
		});
		
		$(".btplus").click(function(){
			$("#grid_div").val(this.id);
			fn_newSystemAccount(this.id);
		});
		
		$("#btn_id_duplicate").click(function(){
			fn_duplicate(this.id)
		});
	});

	function fn_systemGrid(){
		// 호출 URL
		var url = "/scheduler/systemAccount/selectSystemAccountList.do";
		//조회 조건
		var param = {
					"sel_grid" 			: "main",
	                "sel_keyword" 		: $('#sel_keyword').val().trim(),
	                "sel_systemGroup" 	: $('#sel_systemGroup').val(),
	                "sel_useYn" 		: $('#sel_useYn').val()
			};
		
		//컬럼 옵션
		var columns = [
						{"data": 'rnum'},
						{"data": "system_flag"},
						{"data": "system_name","className": "dt-ellipsis"},
						{"data": "system_address", "className": "dt-ellipsis"},
						{"data": "system_seq",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'						  
								return data;
							}
						}
		           ];
		
		var columnDefs =
			{
			    "targets": [0,1,3],
			    "className": "text-center"
			};
		
		var gridObj = {
		        'grid_id': "systemGrid",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': false
		};
		
		var gridOptions = {
		        'serverSide': true,
		        'searching': false,
		        'select': true,
		        'paging': true,
		        'button': false,
		        'lengthChange': false,
		        'info': true,
		        'autoWidth': false,
		        'responsive': true,
		        'bDestroy': true,
		        'processing': true,
		        'ordering': false,
		        'cursorCols': [1,2,3]
	    };
		dataTableGridNew(gridObj, gridOptions);
	};

 	$('#systemGrid tbody').on( 'click', 'tr', function (){
 		var table = $("#systemGrid").dataTable();
		// 현재 클릭한 행
	    var $row = $(this);
	    // 다른 행들의 선택 상태를 해제
	    table.$('tr.selected').not($row).removeClass('selected');
	    // 현재 행의 선택 상태를 토글
	    $row.toggleClass('selected');	    
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
	   	var row_position = table.fnGetPosition(this);
   		var row_data = table.fnGetData(row_position);
   		//선택한 RowData tempData Copy
   		tempData = row_data;
   		$("#in_parentId").val(row_data.system_seq);
   		$("#in_systemSeq").val(tempData.system_seq);
   		if(column_index == 4){
   	   		$("#in_systemFlag").val(row_data.system_flag);
   	   		$("#in_systemName").val(row_data.system_name);
   	   		$("#in_systemAddress").val((row_data.system_address =="-"?"":row_data.system_address));
			//$("#in_systemFlag").attr("disabled",true);
   			$("#in_systemDesc").val(row_data.system_desc);
   			setCheckBox(row_data.use_flag , modal_info, '#in_useYn');
   			
   			fn_showHide(row_data.system_flag);
   			
   			$("#row_systemPort").hide();   			
   			$("#row_systemAccount").hide();
			$("#row_systemPassword").hide();
			$("#grid_div").val("main");
			$("#in_eventDiv").val("update");
			$("#modal_info_title").text("수정");
			$("#modal_info").modal();
   		}else{
   			fn_systemAccountGrid();	
   		}
	});

	function fn_systemAccountGrid(){
		var sel_parentId ="";
		if(tempData.system_seq != undefined){
			sel_parentId=tempData.system_seq;
		}
		// 호출 URL
		var url = "/scheduler/systemAccount/selectSystemAccountList.do";
		//조회 조건
		var param = {
					"sel_grid" 			: "sub",
	                "sel_keyword" 		: $('#sel_keyword').val().trim(),
	                "sel_parentId" 		: sel_parentId,
	                "sel_useYn" 		: $('#sel_useYn').val()
			};
	    
		//컬럼 옵션
		var columns = [						
						{"data": "system_name","className": "dt-ellipsis"},
						{"data": "system_account"},
						{"data": "system_password"},
						{"data": "system_address","className": "dt-ellipsis"},
						{"data": "system_port"},
						{"data": "system_desc","className": "dt-ellipsis"},
						{"data": "use_flag"},
						{"data": "system_seq",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'						  
								return data;
							}
						}
		           ];
		
		var columnDefs =
			{
			    "targets": [2,4],
			    "className": "text-center"
			};
		
		var gridObj = {
		        'grid_id': "systemAccountGrid",
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
		        'ordering': false
	    };
		dataTableGridNew(gridObj, gridOptions);
	};

 	$('#systemAccountGrid tbody').on( 'click', 'tr', function () {
 		var table = $("#systemAccountGrid").dataTable();
		// 현재 클릭한 행
	    var $row = $(this);
	    // 다른 행들의 선택 상태를 해제
	    table.$('tr.selected').not($row).removeClass('selected');
	    // 현재 행의 선택 상태를 토글
	    $row.toggleClass('selected');
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == 7){
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
	   		
	   		/* Modal 초기화 */
	   		fn_resetForm(modal_info);
	   		
			//$("#in_systemFlag").attr("disabled",true);
			$("#in_systemSeq").val(row_data.system_seq);
			$("#in_systemFlag").val(row_data.system_flag);
			$("#in_systemAddress").val((row_data.system_address =="-"?"":row_data.system_address));
			$("#in_systemPort").val((row_data.system_port =="-"?"":row_data.system_port));
			$("#in_systemName").val(row_data.system_name);
			$("#in_systemAccount").val((row_data.system_account =="-"?"":row_data.system_account));		
			$("#in_systemPassword").val((row_data.system_password =="-"?"":row_data.system_password));
			setCheckBox(row_data.use_flag , modal_info, '#in_useYn');
			
			//$("#row_appName").show();
			$("#row_systemAccount").show();
			$("#row_systemPassword").show();
			$("#row_systemAddress").show();
			
			$("#grid_div").val("sub");
			$("#in_systemDesc").val(row_data.system_desc);
			$("#in_eventDiv").val("update");
			$("#modal_info_title").text("수정");
			$("#modal_info").modal();
		}
	});
 	
	//등록
	function fn_newSystemAccount(grid){
		//Modal 초기화
		fn_resetForm(modal_info);
		$("#in_systemFlag").removeAttr("disabled");
		$("#in_systemName").removeAttr("disabled");
		if(grid =="main"){
			$("#row_systemPort").hide();
			$("#row_systemAccount").hide();
			$("#row_systemPassword").hide();
			
		}else{
			if(tempData.system_seq == undefined){
				alert("Main System을 먼저 선택해주세요.");
				return false;
			};
			//Main Grid
			$("#in_parentId").val(tempData.system_seq);
   			$("#in_systemFlag").val(tempData.system_flag);
			$("#row_systemPort").show();
			$("#row_systemAccount").show();
			$("#row_systemPassword").show();
			fn_showHide(tempData.system_flag);		
		}
		
		setCheckBox("Y" , modal_info, '#in_useYn');
		$("#in_eventDiv").val("insert");
		$("#modal_info_title").text("등록");
		$("#modal_info").modal();
	}

	function fn_showHide(item){
		$("#row_systemName label[for='in_systemName']").text(item+" NAME");
		$("#in_systemName").attr("placeholder", item +" NAME");
		if(item =="APPLICATION"){
			
		}else if(item =="WEBSITE"){
			$("#row_systemPort").hide();
			$("#row_systemAddress label[for='in_systemAddress']").text("URL");
			$("#in_systemAddress").attr("placeholder", "URL");
		}else{
			$("#row_systemPort").show();
			$("#row_systemAddress label[for='in_systemAddress']").text("ADDRESS");
			$("#in_systemAddress").attr("placeholder", "Address");
		}
	}
	/* 서버 서버 중복 확인 */
	function fn_duplicate(){
		var in_systemSeq = modal_info.find('#in_systemSeq').val();
		if(in_systemSeq == ""){
			alert("시스템명을 입력하세요.");
			return false;
		}
		
		var url = "/scheduler/systemAccount/selectSystemAccountCheck.do";
		var param = "in_systemSeq="+in_systemSeq;
		var type = "json";
		ajaxCall(url, type, param, fn_duplicateResult);
		function fn_duplicateResult(data){
			alert(data.result_msg);
			duplicate = data.result_code;
		}
	}
	
	/* 서버 신규 등록 및 수정 */ 
	function fn_saveSystemAccount(){
		var param = getJQParams(modal_info);
		var type = "script";
		var url = "/scheduler/systemAccount/saveSystemAccount.do";
		ajaxCall(url, type, param, fn_saveSystemAccountResult);
		function fn_saveSystemAccountResult(){
			$("#modal_info").modal('hide');			//serverConfig 수정 Popup Open
			handleSearch();
		}
	}
	
	
	/* 서버 삭제 */ 
	function fn_deleteSystemAccount(){
		if(confirm("삭제 하시겠습니까?") == false){
			return false;
		}
		var param = getJQParams(modal_info);
		var type = "script";
		var url = "/scheduler/systemAccount/deleteSystemAccount.do";
		
		ajaxCall(url, type, param, fn_deleteSystemAccountResult);
		function fn_deleteSystemAccountResult(){
			$("#modal_info").modal('hide');			//serverConfig 수정 Popup Open
			handleSearch();
		}
	}
	function handleSearch(){
		var grid_div = $("#grid_div").val();
		fn_resetForm(modal_info);
		//Main Grid 조회
		if(grid_div == "main"){
        	fn_systemGrid();
		}else if(grid_div == "sub"){
        	fn_systemAccountGrid();
		}else{
        	fn_systemGrid();
        	fn_systemAccountGrid();
		}
    }
	handleSearch();
	
</script>
<style>

/* 한 줄로 자르고 끝에 … 표시 */
td.dt-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 20ch; /* 원하는 픽셀 너비 설정 */   
}
</style>