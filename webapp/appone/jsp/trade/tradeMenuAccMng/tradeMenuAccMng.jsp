<%@ page contentType="text/html; charset=utf-8" %>
<!-- Tree view -->
<script src="/scheduler/appone/plugins/treejs-master/dist/tree.min.js"></script>
<div class="container-fluid" id="div_menu_main">
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
				<label for="sel_menuName" class="col-sm-1 col-form-label" style="text-align: right;">TRADE 메뉴 명</label>
				<div class="col-sm-2">
					<input type="text" id="sel_menuName" name="sel_menuName" onkeyup="fn_searchKey();"
						class="form-control form-control-sm" placeholder="메뉴 명">
				</div>
				<label for="sel_user" class="col-sm-1 col-form-label" style="text-align: right;">사용자 검색</label>
				<div class="col-sm-2">
					<input type="text" id="sel_user" name="sel_user" onkeyup="fn_menuGridOneData();"
						class="form-control form-control-sm" placeholder="사용자 명 & 사용자 ID">
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="sel_useYn" onchange="fn_searchKey();">
					</select>
				</div>
				<div class="col-sm-4">
					<button type="button" id="btn_main_search" class="btn btn-sm btn-primary float-right"> <i class="fa fa-search"></i>
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
							<h3 class="card-title">Main Menu</h3>
							<button type="button" class="btn btn-xs" id="bn_treeView">
								<i class="fa fa-fw fa-indent "></i>
							</button>
							<button type="button" class="btn btn-xs btPlus" id="main">
								<i class="fa fa-fw fa-plus right"></i>
							</button>
						</div>
						<table class="table table-hover table-sm" id="menuGridOneData">
							<thead>
								<tr>
									<th>메뉴 순서</th>
									<th>메뉴 ID</th>
									<th>메뉴 명</th>
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
						<h3 class="card-title">Sub Menu</h3>
						<button type="button" class="btn btn-xs btPlus" id="sub">
							<i class="fa fa-fw fa-plus"></i>
						</button>
					</div>
					<table class="table table-hover table-sm" id="menuGridTwoData" >
						<thead>
							<tr>
								<th>메뉴 순서</th>
								<th>메뉴 ID</th>
								<th>메뉴 명</th>
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

<div class="modal fade" id="modal_menu_info" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title" id="menu_info_title">Menu 등록</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row">
							<label for="in_sortOrder" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 순서</label>
							<div class="col-sm-3">
								<input type="hidden" id="in_menuCategory">								
								<input type="hidden" id="in_menuSeq">
								<input type="text" id="in_sortOrder" class="form-control input-sm" placeholder="메뉴 순서">
							</div>
						</div>
						<div class="form-group row" id="row_menuParent">
							<label for="in_menuParent" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 그룹</label>
							<div class="col-sm-4" id="MENUGROUP">
								<select class="form-control custom-select" id="in_menuParent"></select>
							</div>
						</div>
						<div class="form-group row">
							<label for="in_menuId" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 ID</label>
							<div class="col-sm-4">
								<input type="text" id="in_menuId" class="form-control input-sm" placeholder="메뉴 ID">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_menuName" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 명</label>
							<div class="col-sm-5">
								<input type="text" id="in_menuName" class="form-control input-sm" placeholder="메뉴 명">
							</div>
						</div>						
						<div class="form-group row" id="row_viewName">
							<label for="in_viewName" class="col-sm-3 col-form-label" style="text-align: right;">View Name</label>
							<div class="col-sm-5">
								<input type="text" id="in_viewName" class="form-control input-sm" placeholder="View Name">
							</div>
						</div>						
						<div class="form-group row" id="row_dllName">
							<label for="in_dllName" class="col-sm-3 col-form-label" style="text-align: right;">DLL Name</label>
							<div class="col-sm-5">
								<input type="text" id="in_dllName" class="form-control input-sm" placeholder="DLL Name">
							</div>
						</div>						
						<div class="form-group row" id="row_multiDiv">
							<label for="in_multiDiv" class="col-sm-3 col-form-label" style="text-align: right;">Multi Div</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_multiDiv" class="flat-red">
							</div>
						</div>
						<div class="form-group row" id="row_resizeDiv">
							<label for="in_resizeDiv" class="col-sm-3 col-form-label" style="text-align: right;">ReSize Div</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_resizeDiv" class="flat-red">
							</div>
						</div>	
						<div class="form-group row">
							<label for="btn_subscribe" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 권한</label>
							<div class="col-sm-3" id="row_subscribe">
								<button type="button" class="btn btn-sm" data-toggle="modal" id="btn_subscribe" data-target="#modal_subscribe">
									<i class="fa-solid fa-users"></i>
								</button>
							</div>
						</div>						
						<div class="form-group row">
							<label for="in_useYn" class="col-sm-3 col-form-label" style="text-align: right;">사용여부</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useYn" class="flat-red">
							</div>
						</div>
					</form>
			  	</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_eventDiv" value="">
				<input type="hidden" id="in_gridDiv" value="">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-danger" id="btn_del" disabled="disabled">삭제</button>
					<button type="button" class="btn btn-sm btn-primary" id="btn_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>

<!-- 메뉴 권한 -->
<div class="modal fade" id="modal_subscribe" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title" id="title_accessList"></h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body p-1">
				<select class="duallistbox" multiple="multiple" style="height: 200px;" id="pop_user_List" name="pop_user_List"></select>
				<div class="form-group row" id="row_accessAll">
					<label for="in_accessAll" class="col-sm-2 control-label">
						<i class="fa fa-fw fa-check-square-o"></i> 하위메뉴반영
					</label>
					<div class="col-lg-2">
						<input type="checkbox" id="in_accessAll" class="flat-red">
					</div>
				</div>
			</div>
			<div class="modal-footer p-1 justify-content-between">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-primary" id="btn_save_subscribe">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>

<!-- TRADE 메뉴 권한 : 팝업 -->
<div class="modal fade" id="modal_treeView" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header py-2">
        <h5 class="modal-title">TRADE 메뉴 권한</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span>&times;</span></button>
      </div>
      <div class="modal-body pt-3">
        <div class="form-row">
          <div class="col-md-4 mb-2">
            <label class="mb-1">사용자</label>
            <select id="sel_treeViewUsers" class="form-control select2bs4"></select>
          </div>
          <div class="col-md-8">
            <label class="mb-1 d-block">MenuList</label>
            <div class="manuContainer border rounded" style="height:520px;overflow:auto;"></div>
          </div>
        </div>
      </div>
      <div class="modal-footer p-1 justify-content-between">
        <button type="button" class="btn btn-sm btn-secondary" data-dismiss="modal">Close</button>
        <div class="form-group float-right">
        	<button type="button" id="btn_menuAuth_save" class="btn btn-sm btn-primary">Save&Changes</button>
        </div>
      </div>
    </div>
  </div>
</div>

<script>

	//각 영역들을 미리 선언
	var div_menu_main = $('#div_menu_main');
	var modal_menu_Info = $('#modal_menu_info');
	var modal_subscribe = $('#modal_subscribe');
	var oneRowdata ="";
	// jQuery
	$(document).on('click', '#btn_subscribe', function () {
		
		var $box1 = $('#bootstrap-duallistbox-nonselected-list_pop_user_List').closest('.box1');
		
		$("#title_accessList").text($("#in_menuName").val());
		setCheckBox("N" , modal_subscribe, '#in_accessAll');
		
	  	// 플러그인의 초기화 버튼이 있으면 그것을 사용
	  	var $clearBtn = $box1.find('button.clear1');
	  	if ($clearBtn.length) {
	    	$clearBtn.trigger('click');
	    	return;
	  	}
	  	// 없으면 직접 입력값 초기화 + 필터 갱신 이벤트 트리거
	  	var $filter = $box1.find('input.form-control.filter');
	  	$filter.val('');
	  	$filter.trigger('input').trigger('keyup').trigger('change');
	});

	
	function fn_menu_init(){
		
		/* 조회버튼 클릭시 */
		div_menu_main.on('click', '#btn_main_search', function(){
			fn_searchKey();
		});
		
		/* 등록버튼 클릭시 */
		modal_menu_Info.on('click', '#btn_save', function(){
			fn_menuSave();
		});
	
		/* 삭제 버튼 클릭시 */
		modal_menu_Info.on('click', '#btn_del', function(){
			fn_menuDelete();
		});
		
		$(".btPlus").click(function(){
			$("#in_gridDiv").val(this.id);
			fn_menuInsert(this.id);
		});
	}
	
	function fn_searchKey(){
		fn_menuGridOneData();
		fn_menuGridTwoData();
	};

	function fn_menuGridOneData() {
		// 호출 URL
		var url = "/scheduler/tradeMenuAccMng/selectMenuList.do";
		//조회 조건
		var param = {
                "sel_menuDiv" 	: "main",
                "sel_user" 		: $('#sel_user').val(),
                "sel_menuName" 	: $('#sel_menuName').val(),
                "sel_useYn"		: $('#sel_useYn').val()
		};
		//컬럼 옵션
		var columns = [
			{"data": "sort_order"},
			{"data": "menu_id"},
			{"data": "description"},
			{"data": "use_flag"},
			{"data": "menu_seq",
				"render": function(data, type, row){
					data = '<i class="fas fas fa-edit grid-edit"></i>'
					return data;
				}
			}
        ];
		
		var columnDefs = {
			    "targets": [0,1,3,4],
			    "className": "text-center",
		};
		
		var gridObj = {
		        'grid_id': "menuGridOneData",
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
		        'rowReorder': {
		            selector: 'td:first-child', // 행 이동을 허용하는 열 선택자
		            update: false 				// 테이블 정렬 자동 업데이트 비활성화
		        },
		        'cursorCols': [1,2,3,4]
	    };
		dataTableGridNew(gridObj,gridOptions);
	}

	// 마우스 클릭 이벤트 핸들러
	$('#menuGridOneData tbody').on( 'click', 'tr', function () {
			var table = $("#menuGridOneData").dataTable();
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
				fn_menuUpdate("main",row_data);
			}else{
				fn_menuGridTwoData(row_data.menu_id);
				oneRowdata = row_data;
			}
	});
	
	function fn_menuGridTwoData(data) {
		// 호출 URL
		var url = "/scheduler/tradeMenuAccMng/selectMenuList.do";
		//조회 조건
		var param = {
	                "sel_parentId" 	: (data == undefined)? "" : data,
	                "sel_menuDiv" 	: "sub",
	                "sel_user" 		: $('#sel_user').val(),
	                "sel_menuName" 	: $('#sel_menuName').val(),
	                "sel_useYn" 	: $('#sel_useYn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": "sort_order"},
						{"data": "menu_id"},
						{"data": "description"},
						{"data": "use_flag"},
						{"data": "use_flag",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'
								return data;
							}
						}
		           ];
		var columnDefs = {
			"targets": [0,1,3,4],
			"className": "text-center"
		};

		var gridObj = {
		        'grid_id': "menuGridTwoData",
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
		        'cursorCols': [1],
		        'rowReorder': {
		            selector: 'td:first-child', // 행 이동을 허용하는 열 선택자
		            update: false 				// 테이블 정렬 자동 업데이트 비활성화
		        },
		        'ordering': false
        };
		dataTableGridNew(gridObj,gridOptions);
	}
	
	$('#menuGridTwoData tbody').on( 'click', 'tr', function () {
		var menuGridTwoData = $("#menuGridTwoData").dataTable();
	   	var row_position = menuGridTwoData.fnGetPosition(this);
   		var row_data = menuGridTwoData.fnGetData(row_position);
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == '4'){
			fn_menuUpdate("sub",row_data);
		}
	});
	
	/* 메뉴 코드 중복 확인 */
	function fn_menuDuplicate(){
		var in_menuId = modal_menu_Info.find('#txt_insupd_code').val();
	
		if(in_menuId == ""){
			alert("메뉴 ID를 입력하세요.");
			return false;
		}
	
		var url = "/scheduler/tradeMenuAccMng/selectMenuCheck.do";
		var param = "in_menuId="+in_menuId;
		var type = "json";
		
		ajaxCall(url, type, param, fn_menuDuplicateResult);
		
		function fn_menuDuplicateResult(data){
			
			if(data.result == "사용중인 메뉴 코드 입니다. 코드명을 변경해주세요."){
				menu_duplicate = "N";
			}else{
				menu_duplicate = "Y";
			}
		}
	}
	
	function fn_menuUpdate(div,row_data){
		fn_menuGroup();
		//Modal Form Reset
		fn_resetForm(modal_menu_Info);
		// disabled 삭제
		if(div =="main"){
			$('#row_viewName').hide();
			$('#row_dllName').hide();
			$('#row_multiDiv').hide();
			$('#row_resizeDiv').hide();
			$('#row_accessAll').show();
		}else{
			$('#row_viewName').show();
			$('#row_dllName').show();
			$('#row_multiDiv').show();
			$('#row_resizeDiv').show();
			
			$('#row_accessAll').hide();
			setCheckBox("N", modal_menu_Info, '#in_accessAll');
		}
		
		$("#in_menuParent").val(row_data.parent_id).prop("selected", true);
		$("#in_sortOrder").val(row_data.sort_order);
		$("#in_menuSeq").val(row_data.menu_seq);
		$("#in_menuId").val(row_data.menu_id);		
		$("#in_menuName").val(row_data.description);
		
		$("#in_viewName").val(row_data.view_name);
		$("#in_dllName").val(row_data.dll_name);
		
		var multiYn = "N";
		if(row_data.multi_div == "1"){
			multiYn = "Y";	
		}
		var resizeYn = "N";
		if(row_data.resize_div == "1"){
			resizeYn= "Y";	
		}
		
		setCheckBox(multiYn , modal_menu_Info, '#in_multiDiv');
		setCheckBox(resizeYn , modal_menu_Info, '#in_resizeDiv');
		setCheckBox(row_data.use_flag , modal_menu_Info, '#in_useYn');
		
		$("#in_eventDiv").val("update");
		$("#menu_info_title").text("메뉴 수정");
		$("#modal_menu_info").modal();
		
		$('#row_subscribe').show();
		fn_menuAccessUsersSetting(row_data.menu_id);
		$("#in_gridDiv").val(div);
	}
	
	function fn_menuInsert(div){
		if(div != "main" && oneRowdata == ""){
			alert("Main Menu를 먼저 선택해주세요.");
			return false;
		}
		
		/* 신규 등록 셋팅 */
		$('.modal-body form').each(function() {
      		this.reset();
      		menu_duplicate = "N";
  		});
		
		fn_MenuSeq();
		fn_menuGroup();
		if(div =="main"){
			//$('#row_menuParent').hide();	
			$("#in_menuParent").val("");
			$("#in_menuCategory").val("P");
			$("#in_menuType").val("0");
		}else{
			
			//$('#row_menuParent').show();
			$('#row_subscribe').show();
			if(oneRowdata.menu_seq != undefined){
				$("#in_menuParent").val(oneRowdata.menu_id).prop("selected", true);
			}
			$("#in_menuCategory").val("S");
			$("#in_menuType").val("2");
		}		
		setCheckBox("N", modal_menu_Info, '#in_useYn');		
		$("#in_eventDiv").val("insert");
		$("#menu_info_title").text("메뉴 등록");
		$("#modal_menu_info").modal();
	}
	
	/* 코드 SEQ */ 
	function fn_MenuSeq(){
		var param = "in_parentId="+oneRowdata.menu_id+"&in_gridDiv="+$("#in_gridDiv").val();
		var type = "json";
		var url = "/scheduler/tradeMenuAccMng/selectMenuSeq.do";
		ajaxCall(url, type, param, fn_MenuSeqResult);
		function fn_MenuSeqResult(data){
			$("#in_sortOrder").val(data.singleData);
		}
	}
	
	/* 메뉴 신규 등록 및 수정 */ 
	function fn_menuSave(){
		
		var in_menuId = $("#in_menuId").val();
		$("#in_menuId").val(in_menuId.trim());
		
		var param = getJQParams(modal_menu_Info);
		var type = "script";
		var url = "/scheduler/tradeMenuAccMng/saveMenu.do";
		ajaxCall(url, type, param, fn_menuSaveResult);
		function fn_menuSaveResult(){
			$("#modal_menu_info").modal('hide');			//menu 수정 Popup Open
			var grid_div = $("#in_gridDiv").val();
			if(grid_div =="main"){
				fn_menuGridOneData();
			}else{
				if(oneRowdata.menu_id != undefined){
					fn_menuGridTwoData(oneRowdata.menu_id);
				}
			}
		}
	}
	
	/* 메뉴 삭제 */ 
	function fn_menuDelete(){
		var grid_div = $("#in_gridDiv").val();
		if(grid_div =="main"){
			 if(confirm("Main 메뉴 삭제를 할 경우 하위 메뉴리스트도 삭제됩니다. \n삭제하시겠습니까?") == false){
				 return false;
			 }
		}
		var param = getJQParams(modal_menu_Info);
		var type = "script";
		var url = "/scheduler/tradeMenuAccMng/deleteMenu.do";
		
		ajaxCall(url, type, param, fn_menuDeleteResult);
		function fn_menuDeleteResult(){
			$("#modal_menu_info").modal('hide');			//menu 수정 Popup Open
			var grid_div = $("#in_gridDiv").val();
			if(grid_div =="main"){
				fn_menuGridOneData();
			}else{
				if(oneRowdata.menu_id != undefined){
					fn_menuGridTwoData(oneRowdata.menu_id);	
				}
			}
		}
	}
	
	/* page loading 시 검색 영역의 selectbox Setting */
	function fn_menuGroup(){
		var param = "in_useYn="+$("#sel_useYn").val();
		var type = "html";
		var url = "/scheduler/tradeMenuAccMng/selectMenuGroupList.do";
		ajaxCall(url, type, param, fn_menuGroupResult);
		function fn_menuGroupResult(data){
	        var $sel = modal_menu_Info.find('#MENUGROUP select');
	        var prependNone = '<option value="">    -    </option>';
	        $sel.html(prependNone + data);
		}
	};

	/*	사용자 리스트 세팅	*/
	function fn_menuAccessUsers(){
		var url = "/scheduler/tradeMenuAccMng/selectMenuAccessUsers.do";
		var param= "in_menuId=";
		var type = "json";
		ajaxCall(url, type, param, fn_menuAccessUsersResult);
		function fn_menuAccessUsersResult(data) {
			$('[name=pop_user_List]').bootstrapDualListbox('refresh', true);
			var options = "";
			data.data.forEach(function(option) {
				var value = option.user_id;
                var user_nm = option.user_nm;
                options += '<option value="' + value + '">' + user_nm + '</option>';
			});
			$('#pop_user_List').append(options);
			$('[name=pop_user_List]').bootstrapDualListbox();
		}
	}
	
	function fn_menuAccessUsersSetting(in_menuId){
		var url = "/scheduler/tradeMenuAccMng/selectMenuAccessUsers.do";
		var param= "in_menuId="+in_menuId;
		var type = "json";
		ajaxCall(url, type, param, fn_menuAccessUsersSettingResult);
		function fn_menuAccessUsersSettingResult(data){
			$('[name=pop_user_List] option').prop('selected', false);
		    data.data.forEach(function(option) {
		    	var pageAccess = option.pageAccess;
		    	var value = option.user_id;
		      if(pageAccess == "2"){
		    	  $('[name=pop_user_List] option[value="'+value+'"]').prop('selected', true);
		      }
		    });
		    $('[name=pop_user_List]').bootstrapDualListbox('refresh', true);
		}
	}
	$("#btn_save_subscribe").click(function(){
		fn_saveMenuAccessUsers();
	});
	
	/* 시스템 코드 정보 등록 / 수정	*/
	function fn_saveMenuAccessUsers(){
		var in_userList =$('[name="pop_user_List"]').val();
/* 		if(in_userList.length == 0){
			alert("시스템 사용자를 등록해주세요.");
			return false;
		} */
		var in_menuId = $("#in_menuId").val();
		var in_accessAll = $("#in_accessAll").val();
		var url = "/scheduler/tradeMenuAccMng/saveMenuAccessUsers.do";
		var param = getJQParams($("#modal_subscribe"))+"&in_menuId=" + in_menuId+"&in_accessAll=" + in_accessAll;
		var type = "script";
		ajaxCall(url, type, param, fn_saveMenuAccessUsersResult);
		function fn_saveMenuAccessUsersResult(data){
			$('#modal_subscribe').modal("hide"); //닫기
			setCheckBox("N" , modal_subscribe, '#in_accessAll');
		}
	}
	
	/* 시스템 코드 정보 등록 / 수정	*/
	function fn_treeViewUsers(){
		var in_menuId = $("#in_menuId").val();
		var in_accessAll = $("#in_accessAll").val();
		var url = "/scheduler/tradeMenuAccMng/saveMenuAccessUsers.do";
		var param = getJQParams($("#modal_subscribe"))+"&in_menuId=" + in_menuId+"&in_accessAll=" + in_accessAll;
		var type = "script";
		ajaxCall(url, type, param, fn_saveMenuAccessUsersResult);
		function fn_saveMenuAccessUsersResult(data){
			
			$("#treeViewUsers").append("");
		}
	}

	$(document).ready(function(){
		fn_menuGridOneData();
		fn_menuGridTwoData('70000');
		fn_menu_init();
		fn_menuAccessUsers();
	});
	

	(function($){
	  // === Endpoints (서버 소스는 기존 그대로 사용) ===
	  const EP = {
	    users: "/scheduler/tradeMenuAccMng/selectTreeViewUsers.do",
	    menu : "/scheduler/tradeMenuAccMng/selectMenuTree.do",
	    auth : "/scheduler/tradeMenuAccMng/selectUserMenuAuth.do",
	    save : "/scheduler/tradeMenuAccMng/saveUserMenuAuth.do"
	  };

	  let treeRef = null;        // Tree 인스턴스
	  let menuRows = [];         // XZ06M00 전체
	  let leafIdSet = new Set(); // leaf 메뉴 id 집합
	  let allIdSet  = new Set(); // 전체 메뉴 id 집합

	  // 외부 버튼(id: bn_treeView)로 팝업 오픈
	  $(document).on('click', '#bn_treeView', function(){
	    $('#modal_treeView').modal('show');
	  });

	  // 모달 표시 시 초기화/바인딩 (한 번만)
	  let _inited = false;
	  $('#modal_treeView').on('shown.bs.modal', function(){
	    if(!_inited){
	      initUsers();
	      initMenuTree();
	      $('#sel_treeViewUsers').on('change', function(){
	        const uid = $(this).val();
	        if(uid) loadUserAuth(uid);
	        else if(treeRef){ treeRef.values = []; }
	      });
	      $('#btn_menuAuth_save').on('click', saveAuth);
	      _inited = true;
	    }
	  });

	  // 사용자 목록 (XZ01M00)
	  function initUsers(){
	    ajaxCall(EP.users, "json", "", function(resp){
	      const $sel = $('#sel_treeViewUsers').empty().append('<option value="">— 사용자 선택 —</option>');
	      (resp.data || []).forEach(function(u){
	        // USER_ID/USER_NM/ROLE_CD 필드명은 기존 호환
	        const id  = (u.user_id || '').toString();
	        const nm  = (u.user_nm || '');
	        const rcd = (u.role_cd || '');
	        $sel.append('<option value="'+id+'">'+ nm +'</option>');
	      });
	      // select2 재적용
	      if($sel.data('select2')) $sel.select2('destroy');
	      //$sel.select2({ theme:'bootstrap4', width:'100%' });
	    });
	  }
	  
	  let parentMap = new Map();     // menu_id -> parent_id

	  // 메뉴 트리 (XZ06M00 -> 트리)
	  function initMenuTree(){
	    ajaxCall(EP.menu, "json", "", function(resp){
	      menuRows = (resp.data || []).map(normalizeRow);
	      allIdSet = new Set(menuRows.map(r => r.menu_id));
	      leafIdSet = calcLeafIds(menuRows);
	
		   // ▼ 추가: 부모 맵 구성
		   parentMap.clear();
		   menuRows.forEach(r => { parentMap.set(r.menu_id, r.parent_id || null); });
	   
	      const treeData = buildTree(menuRows);
	      $('.manuContainer').empty();
	      treeRef = new Tree('.manuContainer', {
	        data: [{ id:'root', text:'메뉴', children: treeData }],
	        closeDepth: 2
	      });

	      // 팝업 첫 진입 시 선택된 사용자가 있으면 동기화
	      const uid = $('#sel_treeViewUsers').val();
	      if(uid) loadUserAuth(uid);
	    });
	  }

	  // Row 정규화 (숫자/문자 혼용 방지)
	  function normalizeRow(r){
	    return {
	      menu_id     : (r.menu_id     != null ? String(r.menu_id)     : ''),
	      parent_id   : (r.parent_id   != null ? String(r.parent_id)   : null),
	      description : (r.description || r.menu_nm || ''), // 컬럼 명칭 호환
	      menu_category: r.menu_category || r.menu_gb || '',
	      sort_order  : (r.sort_order != null ? Number(r.sort_order) : 0),
	      use_flag    : (r.use_flag || r.use_yn || 'Y')
	    };
	  }

	  // leaf 식별: 어떤 행의 menu_id가 다른 행의 parent_id로 한번도 등장하지 않으면 leaf
	  function calcLeafIds(rows){
	    const parentSet = new Set(rows.map(r => r.parent_id).filter(v => v != null));
	    return new Set(rows.map(r => r.menu_id).filter(id => !parentSet.has(id)));
	  }

	  // 평면 -> 트리
	  function buildTree(rows){
	    const map = {};
	    rows.forEach(function(r){
	      if(r.use_flag !== 'Y') return; // 사용 안함 제외
	      map[r.menu_id] = { id: r.menu_id, text: r.description, children: [] };
	    });

	    const roots = [];
	    rows.forEach(function(r){
	      if(r.use_flag !== 'Y') return;
	      const cur = map[r.menu_id];
	      const pid = r.parent_id;
	      // 최상위: parent_id null 또는 상위가 존재하지 않는 경우
	      if(!pid || !map[pid]){
	        roots.push(cur);
	      }else{
	        map[pid].children.push(cur);
	      }
	    });

	    // 정렬
	    const bySort = (a,b)=>{
	      const ra = rows.find(x => x.menu_id === a.id);
	      const rb = rows.find(x => x.menu_id === b.id);
	      const sa = ra ? ra.sort_order : 0;
	      const sb = rb ? rb.sort_order : 0;
	      return sa - sb;
	    };
	    (function dfsSort(list){
	      list.sort(bySort);
	      list.forEach(n => { if(n.children && n.children.length) dfsSort(n.children); });
	    })(roots);

	    return roots;
	  }

	  // 사용자 권한(XZ07M00) → 트리에 **leaf 교집합만** 체크 (DB와 화면 1:1)
	  function loadUserAuth(userId){
	    ajaxCall(EP.auth, "json", "in_userId="+encodeURIComponent(userId), function(resp){
	      const raw = (resp.data || []).map(r => String(r.menu_id));
	      const strict = raw.filter(id => leafIdSet.has(id) && allIdSet.has(id));
	      if(treeRef){ treeRef.values = strict; }
	    });
	  }
	  
	  
	// === 체크된 모든 노드 + 그 조상까지 저장 ===
	  function saveAuth(){
	    const uid = $('#sel_treeViewUsers').val();
	    if(!uid){ alert('사용자를 선택하세요.'); return; }

	    // 1) 체크 수집: treeRef.values(leaf) + DOM(부모 포함)을 합쳐서 수집
	    const chosen = collectCheckedIds();            // 화면에서 실제 체크된 모든 id
	    const chosenWithAnc = addAncestors(chosen);    // 조상 id 자동 포함

	    // 2) 정리: root 제거 + 실존 메뉴만 + 중복 제거
	    const finalIds = Array.from(new Set(
	      chosenWithAnc.filter(id => id && id !== 'root' && allIdSet.has(id))
	    ));

	    const param = "in_userId="+encodeURIComponent(uid)+"&menuIds="+encodeURIComponent(finalIds.join(','));
	    ajaxCall(EP.save, "script", param, function(){
	      showAlert('success','저장되었습니다.',1000);
	      loadUserAuth(uid); // 재조회로 화면=DB 동기화
	    });
	  }

	  /* 실제 트리 화면에서 체크된 모든 노드 수집 (부모/자식 모두) */
	  function collectCheckedIds(){
	    const ids = new Set();

	    // A) 라이브러리 values(대개 leaf 위주)
	    if(treeRef && Array.isArray(treeRef.values)){
	      treeRef.values.forEach(v => { if(v) ids.add(String(v)); });
	    }

	    // B) DOM 스캔(부모 포함) — 다양한 트리 플러그인 패턴 대응
	    const $ctr = $('.manuContainer');

	    // (1) li[data-id]에 체크 상태가 class/aria로 표기되는 경우
	    $ctr.find('li[data-id]').each(function(){
	      const $li = $(this);
	      const id  = String($li.data('id') || '');
	      if(!id || id === 'root') return;

	      const isChecked =
	        $li.hasClass('treejs-checked') ||                                  // 일부 라이브러리
	        $li.attr('aria-checked') === 'true' ||                              // aria true
	        $li.attr('aria-checked') === 'mixed' ||                             // 부분 체크도 저장하려면 허용
	        $li.find('> .treejs-checkbox.treejs-checked').length > 0 ||         // checkbox span에 클래스 부여
	        $li.find('> input[type=checkbox]:checked').length > 0 ||            // 직접 input 존재
	        $li.find('> .treejs-label input[type=checkbox]:checked').length>0;  // 라벨 내부 input

	      if(isChecked) ids.add(id);
	    });

	    // (2) fallback: 일반 체크박스가 흩어져 있는 경우
	    $ctr.find('input[type=checkbox]:checked').each(function(){
	      const id = String(
	        $(this).val() ||
	        $(this).data('id') ||
	        $(this).closest('[data-id]').data('id') || ''
	      );
	      if(id) ids.add(id);
	    });

	    return Array.from(ids);
	  }

	  /* 선택된 노드들의 모든 조상(ancestor) id 자동 포함 */
	  function addAncestors(idList){
	    const out = new Set(idList.map(String));
	    idList.forEach(id => {
	      let p = parentMap.get(String(id));
	      while(p && p !== 'root' && allIdSet.has(p)){
	        out.add(p);
	        p = parentMap.get(p);
	      }
	    });
	    return Array.from(out);
	  }


	  // 저장: 트리 체크값 중 **leaf만** 저장 → XZ07M00과 정확히 동일
	  function saveAuth2(){
	    const uid = $('#sel_treeViewUsers').val();
	    if(!uid){ alert('사용자를 선택하세요.'); return; }

	    let chosen = (treeRef && treeRef.values) ? treeRef.values.slice() : [];
	    console.log(chosen);
	    chosen = chosen.filter(id => id !== 'root' && leafIdSet.has(id)); // leaf만

	    const param = "in_userId="+encodeURIComponent(uid)+"&menuIds="+encodeURIComponent(chosen.join(','));
	    ajaxCall(EP.save, "script", param, function(){
	      showAlert('success', '저장되었습니다.', 1000);
	      // 저장 후 재조회하여 화면=DB 일치 검증
	      loadUserAuth(uid);
	    });
	  }
	})(jQuery);
</script>
<!-- tradeMenuAccMng.jsp 등 모달이 랜더링되는 페이지의 <style> 영역 -->
<style>
  .manuContainer{
    height: 300px;          /* 고정 높이 */
    overflow-y: auto;       /* 세로 스크롤 자동 */
    overflow-x: hidden;     /* 가로 스크롤 숨김 */
    padding-right: 8px;     /* 스크롤바에 내용 가리지 않도록 여백 (선택) */
    border: 1px solid #dee2e6;  /* 시각적 구분을 원하면 사용 (선택) */
    border-radius: .25rem;      /* 선택 */
    background: #fff;           /* 선택 */
  }

  /* 트리 플러그인이 내부에서 높이를 강제로 주는 경우 대비 (선택) */
  .manuContainer .jstree,
  .manuContainer .treeview,
  .manuContainer .list-group {
    max-height: none !important;
  }
</style>