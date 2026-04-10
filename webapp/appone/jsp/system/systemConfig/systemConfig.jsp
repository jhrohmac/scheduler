<%@ page contentType="text/html; charset=utf-8" %>
<div class="container-fluid" id="div_system_main">
	<div class="card card-primary card-outline">
		<div class="card-header pb-2 pt-2">
			<h5 class="card-title">조회 조건</h5>
			<div class="card-tools">
				<button type="button" class="btn btn-tool"
					data-card-widget="collapse" title="Collapse">
					<i class="fas fa-minus"></i>
				</button>
			</div>
		</div>
		<div class="card-body p-2">
			<div class="form-group search_row">
				<label for="sel_systemName" class="col-sm-1 col-form-label" style="text-align: right;">System Name</label>
				<div class="col-sm-3">
					<input type="text" id="sel_systemName" name="sel_systemName" onkeyup="fn_systemGridOneData();"
						class="form-control form-control-sm" placeholder="시스템 명">
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="sel_useYn" onchange="fn_systemGridOneData();">
					</select>
				</div>
				<div class="col-sm-6">
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
						<table class="table table-hover table-sm" id="systemGridOneData">
							<thead>
								<tr>
									<th>No.</th>
									<th>System ID</th>
									<th>System Name</th>
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
			<div class="col-8">
			<div class="card">
				<div class="card-body table-responsive p-2">
				<div class="card-controls" style="text-align: right">
					<button type="button" class="btn btn-xs btplus" id="sub">
						<i class="fa fa-fw fa-plus text-blue"></i>
					</button>
				</div>
					<table class="table table-hover table-sm" id="systemGridTwoData">
						<thead>
							<tr>
								<th>No.</th>
								<th>System ID</th>
								<th>System Value</th>
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

<div class="modal fade" id="modal_system_info" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title" id="modal_system_info_title">System 등록</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span>&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row">
							<label for="in_sortOrder" class="col-sm-4 col-form-label" style="text-align: right;">System No.</label>
							<div class="col-sm-3">
								<input type="text" id="in_sortOrder" class="form-control input-sm" placeholder="시스템 순서">
							</div>
						</div>
						<div class="form-group row" id="row_systemParent">
							<label for="in_systemParent" class="col-sm-4 col-form-label" style="text-align: right;">System Group</label>
							<div class="col-sm-6" id="SYSTEMGROUP">
								<select class="form-control custom-select" id="in_systemParent"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_systemId" class="col-sm-4 col-form-label" style="text-align: right;">System ID</label>
							<div class="col-sm-7">
								<div class="input-group">
									<input type="text" class="form-control input-sm" id="in_systemId" placeholder="시스템 ID">
									<div class="input-group-append" id="row_systemCheck">
										<span class="input-group-text" style="cursor: pointer;" id="btn_id_duplicate">
											<i class="fas fa-check"></i>
										</span>
									</div>
								</div>								
							</div>
						</div>
						<div class="form-group row" id="row_systemValue">
							<label for="in_systemValue" class="col-sm-4 col-form-label" style="text-align: right;">System Value</label>
							<div class="col-sm-7">
								<input type="text" id="in_systemValue" class="form-control input-sm" placeholder="시스템 ID">
							</div>
						</div>
						<div class="form-group row"  id="row_systemName">
							<label for="in_systemName" class="col-sm-4 col-form-label" style="text-align: right;">System Name</label>
							<div class="col-sm-5">
								<input type="text" id="in_systemName" class="form-control input-sm" placeholder="시스템 명">
							</div>
						</div>
						<div class="form-group row"  id="row_systemNameEng">
							<label for="in_systemNameEng" class="col-sm-4 col-form-label" style="text-align: right;">System Name Eng</label>
							<div class="col-sm-7">
								<input type="text" id="in_systemNameEng" class="form-control input-sm" placeholder="시스템 영문명">
							</div>
						</div>
						<div class="form-group row"  id="row_systemDir">
							<label for="in_systemDir" class="col-sm-4 col-form-label" style="text-align: right;">System Dir</label>
							<div class="col-sm-7">
								<input type="text" id="in_systemDir" class="form-control input-sm" placeholder="시스템 경로">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_systemAccess" class="col-sm-4 col-form-label" style="text-align: right;">System Access</label>
							<div class="col-sm-4 common select-notall" id="ROLE">
								<select class="form-control custom-select" id="in_systemAccess"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_useYn" class="col-sm-4 col-form-label" style="text-align: right;">Use YN</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useYn" class="flat-red">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_systemDesc" class="col-sm-4 col-form-label" style="text-align: right;">System DESC</label>
							<div class="col-sm-8">
								<input type="text" id="in_systemDesc" class="form-control input-sm" placeholder="시스템 설명">
							</div>
						</div>
					</form>
			  	</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_eventDiv" value="">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-danger" id="btn_del">삭제</button>
					<button type="button" class="btn btn-sm btn-primary" id="btn_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>
<script>

	//각 영역들을 미리 선언
	var div_system_main = $('#div_system_main');
	var modal_system_info = $('#modal_system_info');
	var grid_div ="";
	var oneRowdata ="";
	var system_duplicate = "N";
	function fn_system_init(){
		
		/* 조회버튼 클릭시 */
		div_system_main.on('click', '#btn_main_search', function(){
			fn_systemGridOneData();
		});
		
		/* 등록버튼 클릭시 */
		modal_system_info.on('click', '#btn_save', function(){
			fn_systemSave();
		});
	
		/* 삭제 버튼 클릭시 */
		modal_system_info.on('click', '#btn_del', function(){
			fn_systemDelete();
		});
		
		$(".btplus").click(function(){
			fn_systemInsert(this.id)
		});
		
		$("#btn_id_duplicate").click(function(){
			fn_duplicate(this.id)
		});
	}
	
	function fn_systemGridOneData() {
		// 호출 URL
		var url = "/scheduler/systemConfig/selectSystemList.do";
		//조회 조건
		var param = {
	                "sel_systemDiv" 	: "main",
	                "sel_parentId" 		: "",
	                "sel_systemName" 	: $('#sel_systemName').val(),
	                "sel_useYn" 		: $('#sel_useYn').val()
			};
		
		//컬럼 옵션
		var columns = [
						{"data": 'rnum'},
						{"data": "system_id"},
						{"data": "system_nm"},
						{"data": "use_flag"},
						{"data": "system_id",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'
								return data;
							}
						}
		           ];
		
		var columnDefs = 
			{
			    "targets": [0,1,3,4],
			    "className": "text-center",
			};
		
		var gridObj = {
		        'grid_id': "systemGridOneData",
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
		dataTableGridNew(gridObj, gridOptions);
	}
	
	// 마우스 클릭 이벤트 핸들러
	$('#systemGridOneData tbody').on( 'click', 'tr', function () {
			var table = $("#systemGridOneData").dataTable();
			// 현재 클릭한 행
		    var $row = $(this);
		    // 다른 행들의 선택 상태를 해제
		    table.$('tr.selected').not($row).removeClass('selected');
		    // 현재 행의 선택 상태를 토글
		    $row.toggleClass('selected');
		    
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
			var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
			oneRowdata = row_data;
			if(column_index == '4'){
				fn_systemUpdate("main",row_data);
			}else{
				fn_systemGridTwoData(row_data.system_id);
			}
	});
	
	function fn_systemGridTwoData(parentid) {
		// 호출 URL
		var url = "/scheduler/systemConfig/selectSystemList.do";
		//조회 조건
		var param = {
	                "sel_systemDiv" : "sub",
	                "sel_parentId" 	: parentid,
	                "sel_systemName": "",
	                "sel_useYn" 	: $('#sel_useYn').val()
				};

		//컬럼 옵션
		var columns = [
						{"data": "rnum"},
						{"data": "system_id"},
						{"data": "system_value"},						
						{"data": "use_flag"},
						{"data": "system_id",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'
								return data;
							}
						}
		           ];
		
		var columnDefs = 
			{
			    "targets": [0,1,2,3,4],
			    "className": "text-center",
			};
		
		var gridObj = {
		        'grid_id': "systemGridTwoData",
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
		dataTableGridNew(gridObj,gridOptions);
	}

	$('#systemGridTwoData tbody').on( 'click', 'tr', function () {
		var gridData = $('#systemGridTwoData').dataTable();
	   	var row_position = gridData.fnGetPosition(this);
   		var row_data = gridData.fnGetData(row_position);
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == '4'){
			fn_systemUpdate("sub",row_data);
		}
	});
	
	//수정
	function fn_systemUpdate(div,row_data){
		fn_showHide(div);
		grid_div = div;
		/* 셋팅 초기화 */
		$('.modal-body form').each(function() {
			setCheckBox("N", modal_system_info, '#in_useYn');
      		this.reset();
  		});
		system_duplicate ="Y";
		// disabled 처리
		$("#in_systemId").attr("disabled",true);
		$('#row_systemCheck').hide();
		
		$("#in_systemParent").val(row_data.parent_id);
		$("#in_sortOrder").val(row_data.sort_order);
		$("#in_systemId").val(row_data.system_id);
		$("#in_systemValue").val(row_data.system_value);
		$("#in_systemName").val(row_data.system_nm);
		$("#in_systemNameEng").val(row_data.system_nm_eng);		
		$("#in_systemDir").val(row_data.system_dir);		
		$("#in_systemAccess").val(row_data.access_right);
		
		setCheckBox(row_data.use_flag , modal_system_info, '#in_useYn');
		
		$("#in_systemDesc").val(row_data.system_desc);
		$("#in_eventDiv").val("update");
		$("#modal_system_info_title").text("시스템 수정");
		$("#modal_system_info").modal();
	}
	
	//등록
	function fn_systemInsert(div){
		if(div == "sub" && oneRowdata == ""){
			alert("Main System를 먼저 선택해주세요.");
			return false;
		}
		
		fn_showHide(div);
		grid_div = div;
		
		/* 신규 등록 셋팅 */
		$('.modal-body form').each(function() {
			system_duplicate ="N";
			setCheckBox("N", modal_system_info, '#in_useYn');
      		this.reset();
  		});
		//중복체크 버튼 Show
		$('#row_systemCheck').show();
		
		/* System SortOrder */
		fn_systemSortOrder();

		// disabled 삭제
		$("#in_systemId").removeAttr("disabled"); 
		if(div =="main"){
			$('#row_systemParent').hide();
			$('#in_systemParent').val("");
		}else{
			$('#row_systemParent').show();
			if(oneRowdata.system_id != undefined){
				$("#in_systemParent").val(oneRowdata.system_id).prop("selected", true);
			}
		}
		$("#in_eventDiv").val("insert");
		$("#modal_system_info_title").text("시스템 등록");
		$("#modal_system_info").modal();
	}
	
	function fn_showHide(div){
		if(div =='main'){
			$('#row_systemParent').hide();
			$('#row_systemValue').hide();
			$('#row_systemName').show();
			$('#row_systemNameEng').show();
		}else{
			fn_systemGroupList();
			// disabled 처리
			$("#in_systemParent").attr("disabled",false);
			$('#row_systemName').hide();
			$('#row_systemNameEng').hide();
			
			$('#row_systemParent').show();
			$('#row_systemValue').show();
		}
	}
	/* 시스템 시스템 중복 확인 */
	function fn_duplicate(){
		var in_systemId = modal_system_info.find('#in_systemId').val();
	
		if(in_systemId == ""){
			alert("시스템명을 입력하세요.");
			return false;
		}
		var url = "/scheduler/systemConfig/selectSystemCheck.do";
		var param = "in_systemId="+in_systemId;
		var type = "json";
		ajaxCall(url, type, param, fn_duplicateResult);
		function fn_duplicateResult(data){
			alert(data.result_msg);
			system_duplicate = data.result_code;
		}
	}
	/* 시스템 SEQ */ 
	function fn_systemSortOrder(){
		var param = "in_parentId="+oneRowdata.system_id+"&in_gridDiv="+grid_div;
		var type = "json";
		var url = "/scheduler/systemConfig/selectSortOrder.do";
		
		ajaxCall(url, type, param, fn_systemSortOrderResult);
		function fn_systemSortOrderResult(data){
			$("#in_sortOrder").val(data.singleData);
		}
	};
	
	/* 시스템 신규 등록 및 수정 */ 
	function fn_systemSave(){
		if(system_duplicate =="N"){
			alert("시스템 체크를 해주세요.");
			return false;
		};
		var param = getJQParams(modal_system_info);
		var type = "script";
		var url = "/scheduler/systemConfig/saveSystem.do";
		ajaxCall(url, type, param, fn_systemSaveResult);
		function fn_systemSaveResult(){
			$("#modal_system_info").modal('hide');			//systemConfig 수정 Popup Open
			if(grid_div =="main"){
				fn_systemGridOneData();
			}else{
				if(oneRowdata.system_id != undefined){
					fn_systemGridTwoData(oneRowdata.system_id );
				}
			}
		}
	}
	
	/* 시스템 삭제 */ 
	function fn_systemDelete(){
		if(confirm("Main 목록 삭제시 Sub 목록도 삭제됩니다. \n진행하시겠습니까?") == false){
			return false;
		}
		var param = getJQParams(modal_system_info);
		var type = "script";
		var url = "/scheduler/systemConfig/deleteSystem.do";
		
		ajaxCall(url, type, param, fn_systemDeleteResult);
		function fn_systemDeleteResult(){
			$("#modal_system_info").modal('hide');			//systemConfig 수정 Popup Open
			if(grid_div =="main"){
				fn_systemGridOneData();
			}else{
				if(oneRowdata.system_id != undefined){
					fn_systemGridTwoData(oneRowdata.system_id);	
				}
			}
		}
	}
	
	/* page loading 시 검색 영역의 selectbox Setting */
	function fn_systemGroupList(){
		var param;
		var type = "html";
		var url = "/scheduler/systemConfig/selectSystemGroupList.do";
		ajaxCall(url, type, param, fn_systemGroupListResult);
		function fn_systemGroupListResult(data){
			modal_system_info.find('#SYSTEMGROUP select').html(data);
		}
	};
	
	$(document).ready(function(){
		fn_systemGridOneData();
		fn_systemGridTwoData('1');
		fn_system_init();
	});
</script>
