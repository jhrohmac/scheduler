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
				<label for="sel_menuName" class="col-sm-1 col-form-label" >메뉴 명</label>
				<div class="col-sm-3">
					<input type="text" id="sel_menuName" name="sel_menuName" onkeyup="fn_menuGridOneData();"
						class="form-control form-control-sm" placeholder="메뉴 명">
				</div>
				<label for="sel_user" class="col-sm-1 col-form-label" style="text-align: right;">사용자 검색</label>
				<div class="col-sm-2">
					<input type="text" id="sel_user" name="sel_user" onkeyup="fn_menuGridOneData();"
						class="form-control form-control-sm" placeholder="사용자 명 & 사용자 ID">
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="sel_useYn" onchange="fn_menuGridOneData();">
					</select>
				</div>
				<div class="col-sm-3">
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
							<button type="button" class="btn btn-sm" id="btn_menuAuthTree" 
							data-toggle="modal" data-target="#modal_treeView" title="Tree 권한">
								<i class="fa fa-fw fa-indent "></i>
							</button>
							<!-- <button type="button" class="btn btn-xs" id="bn_treeView">
								<i class="fa fa-fw fa-indent "></i>
							</button> -->
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

<div id="modal_menu_info" class="modal fade" tabindex="-1" role="dialog" aria-modal="true" aria-hidden="true">
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
							<div class="col-sm-4">
								<input type="text" id="in_menuIcon" class="form-control input-sm" placeholder="icon Code">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_menuName" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 명</label>
							<div class="col-sm-5">
								<input type="text" id="in_menuName" class="form-control input-sm" placeholder="메뉴 명">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_menuNameEng" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 영문명</label>
							<div class="col-sm-7">
								<input type="text" id="in_menuNameEng" class="form-control input-sm" placeholder="메뉴 영문명">
							</div>
						</div>
						<div class="form-group row">
							<label for="row_subscribe" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 권한</label>
							<div class="col-sm-3" id="row_subscribe">
								<button type="button" class="btn btn-sm" data-toggle="modal" id="btn_subscribe" data-target="#modal_subscribe">
									<i class="fa-solid fa-users"></i>
								</button>
							</div>
						</div>
						<div class="form-group row" id="row_menuurl">
							<label for="in_menuUrl" class="col-sm-3 col-form-label" style="text-align: right;">URL</label>
							<div class="col-sm-9">
								<input type="text" id="in_menuUrl" class="form-control input-sm" placeholder="메뉴 URL">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_useYn" class="col-sm-3 col-form-label" style="text-align: right;">사용여부</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_useYn" class="flat-red" placeholder="메뉴 명">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_popUp" class="col-sm-3 col-form-label" style="text-align: right;">POP UP</label>
							<div class="col-sm-3">
								<input type="checkbox" id="in_popUp" class="flat-red" placeholder="popup">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_menuDesc" class="col-sm-3 col-form-label" style="text-align: right;">메뉴 설명</label>
							<div class="col-sm-8">
								<input type="text" id="in_menuDesc" class="form-control input-sm" placeholder="메뉴 설명">
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
					<button type="button" class="btn btn-sm btn-danger" id="btn_del">삭제</button>
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
			<div class="modal-body p-1">
				<h3 id="title_accessList"><i class="fa fa-fw fa-bookmark-o"></i></h3>
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


<!-- MENU 권한 (Tree) : popup -->
<div class="modal fade" id="modal_treeView" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-lg modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header py-2">
        <h5 class="modal-title">메뉴 권한 (Tree)</h5>
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
	var originalMenuId = "";
	var menu_duplicate = "N";
	var menuSaveInProgress = false;
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
			fn_menuGridOneData();
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

	function fn_menuGridOneData() {
		// 호출 URL
		var url = "/scheduler/menu/selectMenuList.do";
		//조회 조건
		var param = {
                "sel_menuDiv" 	: "main",
                "sel_menuName" 	: $('#sel_menuName').val(),
                "sel_user"		: $('#sel_user').val(),
                "sel_useYn"		: $('#sel_useYn').val()
		};
		//컬럼 옵션
		var columns = [
			{"data": "sort_order"},
			{"data": "menu_id"},
			{"data": "menu_nm"},
			{"data": "use_flag"},
			{"data": "menu_seq",
				"render": function(data, type, row){
					data = '<i class="fas fas fa-edit grid-edit"></i>'
					return data;
				}
			}
        ];
		
		var columnDefs = {
			    "targets": [0,1,2,3,4],
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
				fn_menuGridTwoData(row_data.menu_seq);
				oneRowdata = row_data;
			}
	});
	
	function fn_menuGridTwoData(data) {
		// 호출 URL
		var url = "/scheduler/menu/selectMenuList.do";
		//조회 조건
		var param = {
	                "sel_parentId" 	: data,
	                "sel_menuDiv" 	: "sub",
	                "sel_menuName" 	: $('#sel_menuName').val(),
	                "sel_user" 		: $('#sel_user').val(),
	                "sel_useYn" 	: $('#sel_useYn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": "sort_order"},
						{"data": "menu_id"},
						{"data": "menu_nm"},
						{"data": "use_flag"},
						{"data": "menu_seq",
							"render": function(data, type, row){
								data = '<i class="fas fas fa-edit grid-edit"></i>'
								return data;
							}
						}
		           ];
		var columnDefs = {
			"targets": [0,1,2,3,4],
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
		var in_menuId = $.trim(modal_menu_Info.find('#in_menuId').val());
		var eventDiv = $("#in_eventDiv").val();

		if(in_menuId == ""){
			alert("메뉴 ID를 입력하세요.");
			return false;
		}

		modal_menu_Info.find('#in_menuId').val(in_menuId);

		if(eventDiv == "update" && originalMenuId != "" && in_menuId == originalMenuId){
			menu_duplicate = "Y";
			return true;
		}

		var isAvailable = false;
		var url = "/scheduler/menu/selectMenuCheck.do";
		var param = getJQParams(modal_menu_Info);
		var type = "json";

		ajaxCall(url, type, param, function(data){
			if(data.result_code == "Y"){
				menu_duplicate = "Y";
				isAvailable = true;
			}else{
				menu_duplicate = "N";
				alert(data.result_msg || "이미 사용 중인 메뉴 ID입니다. 메뉴 ID를 변경해주세요.");
			}
		});

		return isAvailable;
	}
	
	function fn_menuUpdate(div,row_data){
		$("#in_gridDiv").val(div);
		//Modal Form Reset
		fn_resetForm(modal_menu_Info);
		
		// disabled 삭제
		if(div =="main"){
			$('#row_menuParent').hide();
			$("#in_menuParent").val("");
		}else{
			fn_menuGroup();
			$('#row_menuParent').show();
			$("#in_menuParent").val(row_data.parent_id).prop("selected", true);
			if(oneRowdata.access_right ==""){
				// disabled 처리
				$("#in_menuId").attr("disabled",true);
				//alert("Main Menu가 전체 공개입니다.");
			}
		}
		
		$("#in_sortOrder").val(row_data.sort_order);
		$("#in_menuSeq").val(row_data.menu_seq);
		$("#in_menuId").val(row_data.menu_id);
		originalMenuId = $.trim(row_data.menu_id || "");
		$("#in_menuIcon").val(row_data.menu_icon);
		$("#in_menuName").val(row_data.menu_nm);
		$("#in_menuNameEng").val(row_data.menu_nm_eng);
		
		$("#in_menuUrl").val(row_data.url);
		setCheckBox(row_data.use_flag , modal_menu_Info, '#in_useYn');
		setCheckBox(row_data.popup_flag , modal_menu_Info, '#in_popUp');
		$("#in_menuDesc").val(row_data.menu_desc);
		$("#in_eventDiv").val("update");
		$("#menu_info_title").text("메뉴 수정");
		$("#modal_menu_info").modal();
		
		$('#row_subscribe').show();
		fn_menuAccessUsersSetting(row_data.menu_seq);
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
		originalMenuId = "";
		
		fn_MenuSeq();
		if(div =="main"){
			$('#row_menuParent').hide();	
			$("#in_menuParent").val("");
		}else{
			fn_menuGroup();
			$('#row_menuParent').show();
			$('#row_subscribe').show();
			if(oneRowdata.menu_seq != undefined){
				$("#in_menuParent").val(oneRowdata.menu_seq).prop("selected", true);
			}
		}
		
		setCheckBox("N" , modal_menu_Info, '#in_popUp');
		$('#row_subscribe').hide();	
		$("#in_eventDiv").val("insert");
		$("#menu_info_title").text("메뉴 등록");
		$("#modal_menu_info").modal();
	}
	
	/* 코드 SEQ */ 
	function fn_MenuSeq(){
		var param = "in_parentId="+oneRowdata.menu_seq+"&in_gridDiv="+$("#in_gridDiv").val();
		var type = "json";
		var url = "/scheduler/menu/selectMenuSeq.do";
		ajaxCall(url, type, param, fn_MenuSeqResult);
		function fn_MenuSeqResult(data){
			$("#in_sortOrder").val(data.singleData);
		}
	}
	
	/* 메뉴 신규 등록 및 수정 */ 
	function fn_menuSave(){
		if (menuSaveInProgress) {
			return false;
		}

		var grid_div = $("#in_gridDiv").val();
		var in_menuId = $.trim($("#in_menuId").val());

		if (in_menuId == "") {
			alert("메뉴 ID를 입력하세요.");
			return false;
		}

		$("#in_menuId").val(in_menuId.trim());

		if (!fn_menuDuplicate()) {
			return false;
		}
		
		var param = getJQParams(modal_menu_Info);
		var type = "script";
		var url = "/scheduler/menu/saveMenu.do";
		var $saveBtn = modal_menu_Info.find('#btn_save');

		menuSaveInProgress = true;
		$saveBtn.prop('disabled', true);
		setTimeout(function(){
			if(menuSaveInProgress){
				menuSaveInProgress = false;
				$saveBtn.prop('disabled', false);
			}
		}, 3000);

		ajaxCall(url, type, param, fn_menuSaveResult);
		function fn_menuSaveResult(){
			menuSaveInProgress = false;
			$saveBtn.prop('disabled', false);
			$("#modal_menu_info").modal('hide');			//menu 수정 Popup Open
			
			if(grid_div =="main"){
				fn_menuGridOneData();
			}else{
				if(oneRowdata.menu_seq != undefined){
					fn_menuGridTwoData(oneRowdata.menu_seq);
				}
			}
			//사이드 메뉴 갱신
			fn_slide_menu();
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
		var url = "/scheduler/menu/deleteMenu.do";
		
		ajaxCall(url, type, param, fn_menuDeleteResult);
		function fn_menuDeleteResult(){
			$("#modal_menu_info").modal('hide');			//menu 수정 Popup Open
			
			if(grid_div =="main"){
				fn_menuGridOneData();
			}else{
				if(oneRowdata.menu_seq != undefined){
					fn_menuGridTwoData(oneRowdata.menu_seq);	
				}
			}
			//사이드 메뉴 갱신
			fn_slide_menu();
		}
	}
	
	/* page loading 시 검색 영역의 selectbox Setting */
	function fn_menuGroup(){
		var param = "in_useYn="+$("#sel_useYn").val();
		var type = "html";
		var url = "/scheduler/menu/selectMenuGroupList.do";
		ajaxCall(url, type, param, fn_menuGroupResult);
		function fn_menuGroupResult(data){
			modal_menu_Info.find('#MENUGROUP select').html(data);
		}
	};

	/*	사용자 리스트 세팅	*/
	function fn_menuAccessUsers(){
		var url = "/scheduler/menu/selectMenuAccessUsers.do";
		var param= "in_menuSeq=";
		var type = "json";
		ajaxCall(url, type, param, fn_menuAccessUsersResult);
		function fn_menuAccessUsersResult(data) {
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
	
	function fn_menuAccessUsersSetting(in_menuSeq){
		var url = "/scheduler/menu/selectMenuAccessUsers.do";
		var param= "in_menuSeq="+in_menuSeq;
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
		/* if(in_userList.length == 0){
			alert("시스템 사용자를 등록해주세요.");
			return false;
		} */
		var in_accessAll 	= $("#in_accessAll").val();
		var in_menuSeq 		= $("#in_menuSeq").val();
		var url = "/scheduler/menu/saveMenuAccessUsers.do";
		var param = getJQParams($("#modal_subscribe"))+"&in_menuSeq=" + in_menuSeq+"&in_accessAll=" + $("#in_accessAll").val();
		var type = "script";
		ajaxCall(url, type, param, fn_saveMenuAccessUsersResult);
		function fn_saveMenuAccessUsersResult(data){
			$('#modal_subscribe').modal("hide"); //닫기
			setCheckBox("N" , modal_subscribe, '#in_accessAll');
			//사이드 메뉴 갱신
			fn_slide_menu();
		}
	}

	$(document).ready(function(){
		fn_menuGridOneData();
		fn_menuGridTwoData('1');
		fn_menu_init();
		fn_menuAccessUsers();
	});
	
	(function($){
	  // ===== TreeView endpoints (MenuManagement) =====
	  const EP = {
	    users: "/scheduler/menu/selectTreeViewUsers.do",
	    tree : "/scheduler/menu/selectMenuTree.do",
	    auth : "/scheduler/menu/selectUserMenuAuth.do",
	    save : "/scheduler/menu/saveUserMenuAuth.do"
	  };

	  let treeRef = null;
	  let menuRows = [];
	  let allIdSet = new Set();
	  let leafIdSet = new Set();
	  let parentMap = new Map();

	  // 모달 표시 시 초기화/바인딩 (한 번만)
	  let _inited = false;
	  $('#modal_treeView').on('shown.bs.modal', function(){
	    if(!_inited){
	      initUsers();
	      loadMenuTree();
	      $('#sel_treeViewUsers').on('change', function(){
	        const uid = $(this).val();
	        if(uid) loadUserAuth(uid);
	        else if(treeRef){ treeRef.values = []; }
	      });
	      $('#btn_menuAuth_save').on('click', saveAuth);
	      _inited = true;
	    }
	  });

	  function initUsers(){
	    ajaxCall(EP.users, "json", "", function(resp){
	      const $sel = $('#sel_treeViewUsers').empty().append('<option value="">— 사용자 선택 —</option>');
	      (resp.data || []).forEach(function(u){
	        const id  = (u.user_id || '').toString();
	        const nm  = (u.user_nm || '');
	        $sel.append('<option value="'+id+'">'+ nm +'</option>');
	      });
	      if($sel.data('select2')) $sel.select2('destroy');
	      //$sel.select2({ theme:'bootstrap4', width:'100%' });
	    });
	  }

	  function normalizeRow(r){
	    return {
	      id         : (r.menu_seq != null ? String(r.menu_seq) : ''),
	      parent_id  : (r.parent_id != null ? String(r.parent_id) : null),
	      text       : (r.menu_nm || r.description || ''),
	      sort_order : (r.sort_order != null ? Number(r.sort_order) : 0),
	      use_flag   : (r.use_flag || 'Y')
	    };
	  }

	  function calcLeafIds(rows){
	    const parentSet = new Set(rows.map(r => r.parent_id).filter(v => v != null));
	    return new Set(rows.map(r => r.id).filter(id => !parentSet.has(id)));
	  }

	  function buildTree(rows){
	    const map = new Map();
	    const roots = [];
	    rows.forEach(r => {
	      map.set(r.id, { id:r.id, text:r.text, children:[] });
	    });
	    rows.forEach(r => {
	      if(r.parent_id && map.has(r.parent_id)){
	        map.get(r.parent_id).children.push(map.get(r.id));
	      }else{
	        roots.push(map.get(r.id));
	      }
	    });
	    // sort by sort_order
	    const order = new Map(rows.map(r => [r.id, r.sort_order]));
	    function dfsSort(list){
	      list.sort((a,b) => (order.get(a.id)||0) - (order.get(b.id)||0));
	      list.forEach(n => { if(n.children && n.children.length) dfsSort(n.children); });
	    }
	    dfsSort(roots);
	    return roots;
	  }

	  function loadMenuTree(){
	    ajaxCall(EP.tree, "json", "", function(resp){
	    	console.log(resp);
	      menuRows = (resp.data || []).map(normalizeRow);
	      allIdSet = new Set(menuRows.map(r => r.id));
	      leafIdSet = calcLeafIds(menuRows);

	      parentMap.clear();
	      menuRows.forEach(r => { parentMap.set(r.id, r.parent_id || null); });

	      const treeData = buildTree(menuRows);
	      $('.manuContainer').empty();
	      treeRef = new Tree('.manuContainer', {
	        data: [{ id:'root', text:'메뉴', children: treeData }],
	        closeDepth: 2
	      });

	      const uid = $('#sel_treeViewUsers').val();
	      if(uid) loadUserAuth(uid);
	    });
	  }

	  function loadUserAuth(userId){
	    ajaxCall(EP.auth, "json", "in_userId="+encodeURIComponent(userId), function(resp){
	      const raw = (resp.data || []).map(r => String(r.menu_seq || r.menu_id));
	      const strict = raw.filter(id => leafIdSet.has(id) && allIdSet.has(id));
	      if(treeRef){ treeRef.values = strict; }
	    });
	  }

	  function collectCheckedIds(){
	    const ids = new Set();
	    if(treeRef && Array.isArray(treeRef.values)){
	      treeRef.values.forEach(v => ids.add(String(v)));
	    }
	    // 부모 포함 체크: DOM 기반
	    $('.manuContainer input[type="checkbox"]:checked').each(function(){
	      const id = $(this).val() || $(this).attr('value');
	      if(id) ids.add(String(id));
	    });
	    return Array.from(ids);
	  }

	  function addAncestors(idList){
	    const out = new Set(idList);
	    idList.forEach(id => {
	      let p = parentMap.get(String(id));
	      while(p && p !== 'root' && allIdSet.has(p)){
	        out.add(p);
	        p = parentMap.get(p);
	      }
	    });
	    return Array.from(out);
	  }

	  function saveAuth(){
	    const uid = $('#sel_treeViewUsers').val();
	    if(!uid){ alert('사용자를 선택하세요.'); return; }

	    const chosen = collectCheckedIds();
	    const chosenWithAnc = addAncestors(chosen);
	    const finalIds = Array.from(new Set(
	      chosenWithAnc.filter(id => id && id !== 'root' && allIdSet.has(id))
	    ));

	    const param = "in_userId="+encodeURIComponent(uid)+"&menuIds="+encodeURIComponent(finalIds.join(','));
	    ajaxCall(EP.save, "script", param, function(){
	      showAlert('success','저장되었습니다.',1000);
	      loadUserAuth(uid);
	    	//사이드 메뉴 갱신
			fn_slide_menu();
	    });
	  }
	})(jQuery);
</script>

<style>
  .manuContainer{
    height: 520px;          /* 고정 높이 */
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
