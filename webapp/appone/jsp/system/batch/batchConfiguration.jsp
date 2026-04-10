<%@ page contentType="text/html; charset=utf-8" %>
<div class="container-fluid"  onload="실행될 코드" id="div_batch_main">
	<div class="card card-primary card-outline">
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
				<label for="in_usr_id" class="col-sm-1 col-form-label" style="text-align: right;">프로세스 정보</label>
				<div class="col-sm-3">
					<input type="text" id="in_usr_id" name="in_usr_id" onkeyup="fn_BatchGridData();"
					class="form-control form-control-sm" placeholder="프로세스 정보 입력">
				</div>
				<label for="in_useyn" class="col-sm-1 col-form-label" style="text-align: right;">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="in_useyn" onchange="fn_BatchGridData();">
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
		<div class="col-12">
			<div class="card card-info">
					<div class="card-body table-responsive p-2">
						<div class="card-controls"  style="text-align: left">
							<button type="button" class="btn bg-default btn-sm bg-info" id="btn_main_insert">
								<i class="fas fa-plus right"></i>
							</button>							
						</div>
						<table class="table table-hover table-sm" id="batchGridData">
							<thead>
								<tr>
									<th>No.</th>
									<th>PID</th>
									<th>프로세스명</th>
									<th>상태</th>
									<th>리플레쉬</th>
								</tr>
							</thead>
							<tbody></tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
</div>

<!-- 등록 / 수정 POPUP -->
<div class="modal fade" id="modal_Batch_InfoPop" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">배치 등록</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
	    			<div class="form-group row" style="margin-bottom: 4px;">
						<label for="txt_insUpd_id" class="col-sm-3 control-label"><span>*</span>프로세스명</label>
						<div class="col-lg-8 mb-3">
							<div class="input-group">
								<input type="text" class="form-control form-control-sm" id="txt_insUpd_id" onkeypress="fn_enter(fn_duplicate);">
								<div class="input-group-append">
									<span class="input-group-text" style="cursor: pointer;" id="btn_id__duplicate"><i class="fas fa-check"></i></span>
								</div>
							</div>
						</div>
					</div>
					<div class="form-group row">
						<label for="txt_insUpd_name" class="col-sm-3 control-label"><span>*</span>프로세스경로</label>
						<div class="col-lg-8">
							<input type="text" id="txt_insUpd_name"  name="txt_insUpd_name" class="form-control form-control-sm" placeholder="batch Name">
						</div>
					</div>					
					<div class="form-group row">
						<label for="txt_insUpd_system" class="col-sm-3 control-label">프로그램 등급</label>
						<div class="col-lg-8">
							<select id="sel_insUpd_role"  name="sel_insUpd_role" class="form-control form-control-sm"></select>
						</div>
					</div>
					<div class="form-group row">
						<label for="chk_insUpd_smsYn" class="col-sm-3 control-label">SMS수신</label>
						<div class="col-lg-8">
							<label id="check_label">
			                	<input type="checkbox" id="chk_insUpd_smsYn" class="flat-red" >
			                </label>
						</div>
					</div>
					<div class="form-group row">
						<label for="sel_insUpd_role" class="col-sm-3 control-label">사용자권한</label>
						<div class="col-lg-8 common select-all" id="ROLE">
							<select id="sel_insUpd_role"  name="sel_insUpd_role" class="form-control form-control-sm"></select>
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
<script type="text/javascript">
var modal_Batch_InfoPop = $('#modal_Batch_InfoPop');	//등록/수정 POPUP
/* 아이디 중복확인 확인유무를 위한 변수 */
var check_duplicatebatch = "N";
var eventInfo = "N";

/* 각 역영들을 미리 선언 */
var div_batch_main = $('#div_batch_main');
var modal_passChg_pop = $('#modal_passChg_pop');

function fn_batch_initEvent() {
	/* 신규버튼 클릭시 */
	div_batch_main.on('click', '#btn_insert', function() {
			/* 신규 등록 셋팅 */
			$('.modal-body form').each(function() {
	      		this.reset();
	  		});
			$("#modal_Batch_InfoPop").modal();
	});

	/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
	modal_Batch_InfoPop.on('click', '#btn_save', function() {
		fn_save_batch();
	});
	
	/* 유저 신규,수정  팝업창 아이디 중복 체크 버튼 클릭시 */
	modal_Batch_InfoPop.on('click', '#btn_id__duplicate',function() {
		fn_duplicate();
	});
}

	function fn_duplicate(){
		/* 유저 아이디 중복 확인 */
		var usr_id = modal_Batch_InfoPop.find('#txt_insUpd_id').val();
		if (usr_id == "") {
			alert("아이디를 입력하세요.");
			return false;
		}
		check_duplicatebatch = "Y";
		var url = "/scheduler/batch/selectbatchCheck.do";
		var param = "usr_id=" + usr_id;
		var type = "json";

		fn_sendAjax(url, type, param, fn_duplicatebatch);

		function fn_duplicatebatch(data) {
			alert(data.resultMsg);
			if (data.resultMsg == "사용중인 아이디 입니다.") {
				check_duplicatebatch = "N";
			} else {
				check_duplicatebatch = "Y";
			}
		}
	}
	/************************************************************************
	* batch list
	*************************************************************************/
	function fn_BatchGridData() {
		// 호출 URL
		var url = "/scheduler/batch/selectProcessList.do";
		//조회 조건
		var param = {
				 	"batch_id" 	: $('#batch_id').val(),  
	                "sel_useyn" : $('#sel_useyn').val()
				};
		//컬럼 옵션
		var columns = [
						{"data": "rnum"},
						{"data": "batch_id",
							"render": function(data, type, row){
					 			data="<a style='cursor: pointer;'>"+data+"</a>";
							return data;
							}
						},						
						{"data": "email"},
						{"data": "hp_no",
							"render": function(data, type, row){
								data = '<a class="btn btn-success btn-xs" href="#">'
									  +'<i class="fa-solid fa-play fa-beat"></i></a>'
								return data;
							}
						},
						{"data": "sms_yn",
							"render": function(data, type, row){
								data = '<a class="btn btn-info btn-xs" href="#">'
									  +'<i class="fa-solid fa-repeat"></i></a>'
								return data;
							}
						}
		           ];

		var gridObj = {
		        'grid_id': "batchGridData",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnCheck': true
		};
		
		var gridOptions = {
		        'serverSide'	: true,
		        'searching'		: false,
		        'paging'		: true,
		        'button'		: false,
		        'lengthChange'	: false,
		        'info'			: true,
		        'autoWidth'		: false,
		        'responsive'	: true,
		        'bDestroy'		: true,
		        'processing'	: true,
		        'ordering'		: false
	    };
		dataTableGridNew(gridObj,gridOptions);
	}
	
 	$('#batchGridData tbody').on( 'dblclick', 'tr', function () {
 		var table = $("#batchGridData").dataTable();
		// 현재 클릭한 행
	    var $row = $(this);
	    // 다른 행들의 선택 상태를 해제
	    table.$('tr.selected').not($row).removeClass('selected');
	    // 현재 행의 선택 상태를 토글
	    $row.toggleClass('selected');
	    
	
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == '1'){
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
			
			$('.modal-body form').each(function() {
				/* checkBox setting */
				setCheckBox("N", modal_Batch_InfoPop, '#chk_insUpd_mailYn');
				setCheckBox("N", modal_Batch_InfoPop, '#chk_insUpd_smsYn');
	      		this.reset();
	  		});
			
			modal_Batch_InfoPop.find('#txt_insUpd_id').val(row_data.batch_id).attr("readonly", "readonly");
			modal_Batch_InfoPop.find('#txt_insUpd_name').val(row_data.batch_nm);
			modal_Batch_InfoPop.find('#txt_insUpd_dept').val(row_data.desk_div);
			modal_Batch_InfoPop.find('#txt_insUpd_email').val(row_data.email);
			modal_Batch_InfoPop.find('#txt_insUpd_phone').val(row_data.hp_no);
			modal_Batch_InfoPop.find('#txt_insupd_startpage').val(row_data.defaultpage);
			modal_Batch_InfoPop.find('#txt_use_end_date').val(row_data.use_end_date);
			modal_Batch_InfoPop.find('#txt_extension_date').val(row_data.extension_date);
			modal_Batch_InfoPop.find('#btn_id__duplicate').hide();
			
			/* selectBox setting */
			$("#sel_insUpd_lang").val(row_data.lang);
			$("#sel_insUpd_role").val(row_data.role);
			
			/* checkBox setting */
			setCheckBox(row_data.mail_yn , modal_Batch_InfoPop, '#chk_insUpd_mailYn');
			setCheckBox(row_data.sms_yn, modal_Batch_InfoPop, '#chk_insUpd_smsYn');
			
			$("#modal_Batch_InfoPop").modal();			//사용자 수정 Popup Open
			$('#form_pwd').hide();						//패스워드 숨김
			$('#form_pwd_cfm').hide();					//패스워드 확인 숨김
			
			$('#btn_submitbatchConfigInfo').text('수정');	//등록 -> 수정
			$('#btn_submitPassChg').show();				//패스워드 변경 버튼 show
			
			$('#btn_quick_batchInfo').hide();
			$('#psw_new').hide();
			$('#psw_new2').hide();
			$('#batch_info_title').text('사용자 수정');
			check_duplicatebatch ="Y";
			eventInfo ="M";
		}
	});

	/* 유저 신규 등록 및 수정 */
	function fn_mgr_save_batch() {

		modal_Batch_InfoPop.find('#sel_insUpd_authority').val(1);
		modal_Batch_InfoPop.find('#sel_insUpd_lang').val('KOR');

		setCheckBoxYN(modal_Batch_InfoPop, '#chk_insUpd_mailYn');
		setCheckBoxYN(modal_Batch_InfoPop, '#chk_insUpd_smsYn');

		var url = "";
		var param = getJQParams(modal_Batch_InfoPop);
		var type = "script";
		if (eventInfo == "N") {
			if (check_duplicatebatch == 'Y') {
				url = "/scheduler/batch/insertbatch.do";
			} else {
				alert('아이디 중복체크를 확인해 주세요');
				return false;
			}
		} else if (eventInfo == "M") {
			url = "/scheduler/batch/updatebatch.do";
		}

		fn_sendAjax(url, type, param, fn_save_batch_Result);
		function fn_save_batch_Result(data){
			$('#modal_Batch_InfoPop').modal("hide"); //사용자 수정 Popup Close
			fn_BatchGridData();
		}
	}
	
	function fn_batch_initProp() {
		fn_BatchGridData();
	}
	
	fn_batch_initProp();
	fn_batch_initEvent();
</script>