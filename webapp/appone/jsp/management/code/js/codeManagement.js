//각 영역들을 미리 선언
var div_code_main 	= $('#div_code_main');
var div_code_info 	= $('#modal_code_info');

var oneRowdata ="";
var code_duplicate = "N";
function mgr_code_initEvent(){

	/* 조회버튼 클릭시 */
	div_code_main.on('click', '#btn_main_search', function(){
		fn_codeGridOneData();
	});

	/* 등록버튼 클릭시 */
	div_code_info.on('click', '#btn_save', function(){
		fn_codeSave();
	});

	/* 삭제 버튼 클릭시 */
	div_code_info.on('click', '#btn_del', function(){
		fn_codeDelete();
	});

	$(".btPlus").click(function(){
		$("#in_gridDiv").val(this.id);
		fn_codeInsert(this.id)
	});

	$("#btn_duplicate").click(function(){
		fn_duplicate(this.id)
	});
}

function fn_codeGridOneData() {
	// 호출 URL
	var url = "/scheduler/code/selectCodeList.do";
	//조회 조건
	var param = {
				"sel_parentId" 		: "",
                "sel_codeDiv" 		: "main",
                "sel_codeName" 		: $('#sel_codeName').val(),
                "sel_useYn" 		: $('#sel_useYn').val()
			};
	//컬럼 옵션
	var columns = [
					{"data": 'rnum'},
					{"data": "code_id"},
					{"data": "code_nm"},
					{"data": "use_flag"},
					{"data": "code_id",
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
	        'grid_id': "codeGridOneData",
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
$('#codeGridOneData tbody').on( 'click', 'tr', function () {
	var table = $("#codeGridOneData").dataTable();
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
		$("#in_gridDiv").val("main");
		code_duplicate="Y";
		fn_codeGroup();
		fn_codeUpdate(row_data);
	}else{
		fn_codeGridTwoData(row_data.code_id);
	}
});

function fn_codeGridTwoData(parentId) {
	// 호출 URL
	var url = "/scheduler/code/selectCodeList.do";
	//조회 조건
	var param = {
                "sel_parentId" 	: parentId,
                "sel_codeDiv" 	: "sub",
                "sel_codeName" 	: "",
                "sel_useYn" 	: $('#sel_useYn').val()
			};
	//컬럼 옵션
	var columns = [
					{"data": "rnum"},
					{"data": "code_id"},
					{"data": "code_value"},
					{"data": "use_flag"},
					{"data": "code_id",
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
	        'grid_id': "codeGridTwoData",
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

$('#codeGridTwoData tbody').on( 'click', 'tr', function () {
	var gridData = $('#codeGridTwoData').dataTable();
	var row_position = gridData.fnGetPosition(this);
	var row_data = gridData.fnGetData(row_position);
	var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
	if(column_index == '4'){
		$("#in_gridDiv").val("sub");
		fn_codeGroup();
		fn_codeUpdate(row_data);
		code_duplicate="Y";
	}
});

//수정
function fn_codeUpdate(row_data){
	fn_showHide();

	/* 셋팅 초기화 */
	$('.modal-body form').each(function() {
	  	this.reset();
	});

	setCheckBox(row_data.use_flag 		, div_code_info, '#in_useYn');
	setCheckBox(row_data.default_flag 	, div_code_info, '#in_defaultYn');

	// disabled 처리
	$("#in_codeId").attr("disabled",true);
	$("#in_codeParent").val(row_data.parent_id);
	$("#in_sortOrder").val(row_data.sort_order);
	$("#in_codeId").val(row_data.code_id);
	$("#in_codeValue").val(row_data.code_value);
	$("#in_codeName").val(row_data.code_nm);
	$("#in_codeNameEng").val(row_data.code_nm_eng);
	$("#in_codeDesc").val(row_data.code_desc);

	$("#in_eventDiv").val("update");
	$("#code_info_title").text("코드 수정");
	$("#modal_code_info").modal();
}

//등록
function fn_codeInsert(grid_div){

	var grid_div = $("#in_gridDiv").val();
	if(grid_div == "sub" && oneRowdata == ""){
		alert("Main Code를 먼저 선택해주세요.");
		return false;
	}

	/* 신규 등록 셋팅 */
	$('.modal-body form').each(function() {
	  	this.reset();
	});

	setCheckBox("N", div_code_info, '#in_useyn');
	setCheckBox("N", div_code_info, '#in_defaultYn');

	fn_showHide();
	if(grid_div =="main"){
		$('#row_codeParent').hide();
		$('#in_codeParent').val("");
	}else{
		$('#row_codeParent').show();
		if(oneRowdata.code_id != undefined){
			console.log(oneRowdata.code_id);
			$("#in_codeParent").val(oneRowdata.code_id);
		}
	}

	/* Code No. */
	fn_codeSortOrder();

	// disabled 삭제
	$("#in_codeId").removeAttr("disabled");
	$("#in_eventDiv").val("insert");
	$("#code_info_title").text("코드 등록");
	$("#modal_code_info").modal();
}

function fn_showHide(){
	var grid_div = $("#in_gridDiv").val();
	if(grid_div =='main'){
		$('#row_codeParent').hide();
		$('#row_codeValue').hide();
		$('#row_codeName').show();
		$('#row_codeNameEng').show();
	}else{
		//Main Code
		fn_codeGroup();
		// disabled 처리
		$('#row_codeParent').show();
		//$("#in_codeParent").attr("disabled",true);
		$('#row_codeValue').show();
		$('#row_codeName').hide();
		$('#row_codeNameEng').hide();
	}
}
/* 코드 코드 중복 확인 */
function fn_duplicate(){
	var in_codeId = div_code_info.find('#in_codeId').val();
	if(in_codeId == ""){
		alert("코드 코드를 입력하세요.");
		return false;
	}
	var param = getJQParams(div_code_info);
	var url = "/scheduler/code/selectCodeCheck.do";
	var type = "json";

	ajaxCall(url, type, param, fn_duplicateResult);
	function fn_duplicateResult(data){
		alert(data.result_msg);
		code_duplicate = data.result_code;
		if(data.result_code ="N"){
			return false;
		}
	}
}

/* 코드 No. */
function fn_codeSortOrder(){
	var param = getJQParams(div_code_info);
	var type = "json";
	var url = "/scheduler/code/selectSortOrder.do";
	ajaxCall(url, type, param, fn_codeSortOrderResult);
	function fn_codeSortOrderResult(data){
		$("#in_sortOrder").val(data.singleData);
	}
}

/* 코드 신규 등록 및 수정 */
function fn_codeSave(){
	var grid_div = $("#in_gridDiv").val();
	if(code_duplicate =="N"){
		alert("코드 체크를 해주세요.");
		return false;
	};

	var param = getJQParams(div_code_info);
	var type = "script";
	var url = "/scheduler/code/saveCode.do";
	ajaxCall(url, type, param, fn_codeSaveResult);
	function fn_codeSaveResult(){
		$("#modal_code_info").modal('hide');			//code 수정 Popup Open
		if(grid_div =="main"){
			fn_codeGridOneData();
		}else{
			if(oneRowdata.code_id != undefined){
				fn_codeGridTwoData(oneRowdata.code_id );
			}
		}
	}
}

/* 코드 삭제 */
function fn_codeDelete(){
	var grid_div = $("#in_gridDiv").val();
	if(grid_div =="main"){
		 if(confirm("Main을 삭제 할 경우 하위 리스트도 삭제됩니다. \n삭제하시겠습니까?") == false){
			 return false;
		 }
	}
	var param = getJQParams(div_code_info);
	var type = "script";
	var url = "/scheduler/code/deleteCode.do";
	ajaxCall(url, type, param, code_delete_result);
	function code_delete_result(){
		$("#modal_code_info").modal('hide');			//code 수정 Popup Open
		if(grid_div =="main"){
			fn_codeGridOneData();
		}else{
			if(oneRowdata.code_id != undefined){
				fn_codeGridTwoData(oneRowdata.code_id);
			}
		}
	}
}

/* page loading 시 검색 영역의 selectbox Setting */
function fn_codeGroup(){
	var param = getJQParams(div_code_info);
	var type = "html";
	var url = "/scheduler/code/selectCodeGroupList.do";
	ajaxCall(url, type, param, fn_codeGroupResult);
	function fn_codeGroupResult(data){
		div_code_info.find('#CODEGROUP select').html(data);
	}
};

$(document).ready(function(){
	fn_codeGridOneData();
	fn_codeGridTwoData('1');
	mgr_code_initEvent();
});
