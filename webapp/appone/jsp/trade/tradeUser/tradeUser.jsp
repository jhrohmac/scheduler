<%@ page contentType="text/html; charset=utf-8" %>
<!-- bootstrap color picker -->
<script src="/scheduler/appone/plugins/bootstrap-colorpicker/js/bootstrap-colorpicker.min.js"></script>
<!-- Bootstrap Color Picker -->
<link rel="stylesheet" href="/scheduler/appone/plugins/bootstrap-colorpicker/css/bootstrap-colorpicker.min.css">

<div class="container-fluid" id="div_user_main">
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
				<label for="sel_userId" class="col-sm-1 col-form-label text-right">사용자 정보</label>
				<div class="col-sm-3">
					<input type="text" id="sel_userId" name="sel_userId" onkeyup="fn_tradeUserGridData();"
						class="form-control form-control-sm" placeholder="사용자 ID or 사용자명">
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label text-right">사용여부</label>
				<div class="col-sm-1 common select" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="sel_useYn"></select>
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
			<div class="card">
				<div class="card-body table-responsive p-2">
			    	<ul class="nav nav-tabs" id="custom-content-below-tab" role="tablist">
			        	<li class="pt-2 px-3">
							<button type="button" class="btn btn-xs" id="btn_main_insert">
								<i class="fas fa-user-plus right"></i>
							</button>
							<!-- <button type="button" class="btn btn-xs" id="btn_main_delete">
								<i class="fas fa-user-minus right"></i>
							</button> -->
						</li>
		            </ul>
	            	<table class="table table-hover table-sm" id="tradeUserGridData">
						<thead>
							<tr>
								<th></th>
								<th>아이디</th>
								<th>이름</th>
								<th>약칭</th>
								<th>부서</th>
								<th>직급</th>
								<th>구분</th>
								<th>권한</th>
								<th>이메일</th>
								<th>GOL ID</th>
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

<!-- 사용자 등록 / 수정 POPUP -->
<div class="modal fade" id="modal_user_info" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title user_info_title">사용자 등록</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" id="frmUserInfo" novalidate>
	    			<div class="form-group row" style="margin-bottom: 4px;">
						<label for="in_userId" class="col-sm-3 control-label text-right"><span>*</span>아이디</label>
						<div class="col-lg-8 mb-3">
							<div class="input-group">
								<input type="text" class="form-control form-control-sm" id="in_userId" onkeypress="fn_enter(fn_duplicate);">
								<div class="input-group-append">
									<span class="input-group-text" style="cursor: pointer;" id="btn_duplicate"><i class="fas fa-check"></i></span>
								</div>
							</div>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userName" class="col-sm-3 control-label text-right"><span>*</span>이름</label>
						<div class="col-lg-8">
							<input type="text" id="in_userName"  name="in_userName" class="form-control form-control-sm" placeholder="User Name">
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userShortName" class="col-sm-3 control-label text-right"><span>*</span>닉네임</label>
						<div class="col-lg-8">
							<input type="text" id="in_userShortName"  name="in_userShortName" class="form-control form-control-sm" placeholder="User Short Name">
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userEngName" class="col-sm-3 control-label text-right"><span>*</span>영문명</label>
						<div class="col-lg-8">
							<input type="text" id="in_userEngName"  name="in_userEngName" 
								class="form-control form-control-sm" placeholder="Eng User Name">
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userCmp" class="col-sm-3 control-label text-right"><span>*</span>회사코드</label>
						<div class="col-lg-8">
							<input type="text" id="in_userCmp"  name="in_userCmp" 
								class="form-control form-control-sm" placeholder="User Company">
						</div>
					</div>
					<div class="form-group row" id="form_pwd">
						<label for="in_userPwd" class="col-sm-3 control-label text-right">패스워드</label>
						<div class="col-lg-8">
							<input type="password" id="in_userPwd"  name="in_userPwd" class="form-control form-control-sm" placeholder="Password">
						</div>
					</div>
					<div class="form-group row" id="form_pwd_cfm">
						<label for="in_userPswConfirm" class="col-sm-3 control-label text-right">패스워드 확인</label>
						<div class="col-lg-8">
							<input type="password" id="in_userPswConfirm"  name="in_userPswConfirm" class="form-control form-control-sm" placeholder="Password">
						</div>
					</div>
					<div class="form-group row">
						<label for="in_position" class="col-sm-3 control-label text-right">직급</label>
						<div class="col-lg-3 common select-nonall" id="POSITION">
							<select id="in_position"  name="in_position" class="form-control form-control-sm"></select>
						</div>
						<label for="in_golId" class="col-sm-2 control-label text-right">GOL ID</label>
						<div class="col-lg-3">
							 <input type="text" id="in_golId" name="in_golId" 
							 	class="form-control form-control-sm" placeholder="GOL ID" />
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userDept" class="col-sm-3 control-label text-right">부서</label>
						<div class="col-lg-3 common select-nonall" id="DEPT">
							<select id="in_userDept"  name="in_userDept" class="form-control form-control-sm"></select>
						</div>
						<label for="in_userType" class="col-sm-2 control-label text-right">구분</label>
						<div class="col-lg-3">
							<select id="in_userType"  name="in_userType" class="form-control form-control-sm">
							<option value="B">Broker</option>
							<option value="D">Dealer</option>
							<option value="M">Manager</option>
							<option value="N">Normal</option>
							</select>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userEmail" class="col-sm-3 control-label text-right">Email</label>
						<div class="col-lg-8">
							 <input type="email" id="in_userEmail" name="in_userEmail" 
							 	class="form-control form-control-sm" placeholder="Email" />
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userPhone" class="col-sm-3 control-label text-right">M-blie</label>
						<div class="col-lg-8">
		                    <input type="text" id="in_userPhone"  name="in_userPhone" 
		                    	class="form-control form-control-sm" placeholder="M-blie"/>
		                  </div>
					</div>
					<div class="form-group row">
						<label for="in_modId" class="col-sm-3 control-label text-right">수정자</label>
						<div class="col-lg-8">
							 <input type="text" id="in_modId" name="in_modId" disabled="disabled"
							 	class="form-control form-control-sm" placeholder="수정자" />
						</div>
					</div>
					<div class="form-group row">
						<label for="in_modDate" class="col-sm-3 control-label text-right">수정일자</label>
						<div class="col-lg-8">
							 <input type="text" id="in_modDate" name="in_modDate" disabled="disabled"
							 	class="form-control form-control-sm" placeholder="수정일자" />
						</div>
					</div>
					<div class="form-group row">
						<label for="in_useYn" class="col-sm-3 control-label text-right">사용여부</label>
						<div class="col-lg-2">
							<label id="check_label">
			                  <input type="checkbox" id="in_useYn" class="flat-red" >
			                </label>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer  justify-content-between">
				<input type="hidden" id="in_eventDiv" value="I">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-danger"data-toggle="modal"
						 data-target="#modal_passChg" id="btn_submitPassChg" disabled="disabled">패스워드 변경</button>
					<button type="button" class="btn btn-sm btn-primary" id="btn_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>
<!-- 패스워드 변경  -->
<div class="modal fade" id="modal_passChg" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title" id="menu_psw_title">패스워드 변경</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<div class="tab-content">
					<form class="form-horizontal">
						<div class="form-group row">
							<label for="in_userNewPwd" class="col-sm-4 control-label text-right">New Password</label>
							<div class="col-sm-8">
								<input type="password" id="in_userNewPwd" class="form-control form-control-sm"autocomplete="off" placeholder="New Password">
							</div>
						</div>
						<div class="form-group row">
							<label for="in_NewPwdConfirmd" class="col-sm-4 control-label text-right">Confirm Password</label>
							<div class="col-sm-8" id="MENUGROUP">
								<input type="password" class="form-control form-control-sm" id="in_NewPwdConfirmd"autocomplete="off" placeholder="Confirm Password">
							</div>
						</div>
					</form>
			  	</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-primary" id="btn_newpwd_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>
<script>

	var modal_user_info = $('#modal_user_info');	//등록/수정 POPUP
	/* 아이디 중복확인 확인유무를 위한 변수 */
	var check_duplicate = "N";
	
	/* 각 역영들을 미리 선언 */
	var div_user_main = $('#div_user_main');
	var modal_passChg = $('#modal_passChg');

	/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
	div_user_main.on('click', '#btn_main_search', function() {
		fn_tradeUserGridData();
	});
	
	function fn_user_initEvent(){
		
		/* 유저 신규버튼 클릭시 */
		div_user_main.on('click', '#btn_main_insert', function() {
				/* 신규 등록 셋팅 */
				$('.modal-body form').each(function() {
		      		this.reset();
		  		});
				
				$("#in_Role option:eq(0)").prop("selected", true);
				$("#in_MailYn option:eq(0)").prop("checked", false);
				$("#in_SmsYn option:eq(0)").prop("checked", false);
				
				$("#in_MailYn").iCheck('uncheck');
				$("#in_SmsYn").iCheck('uncheck');
				 
				$('#btn_submitPassChg').hide();				//패스워드 변경 버튼 hide
				$('#form_pwd').show();						//패스워드 숨김
				$('#form_pwd_cfm').show();					//패스워드 확인 숨김
				
				$('.user_info_title').text('사용자 등록');
				check_duplicate = "N";
				$("#in_eventDiv").val("I");
				modal_user_info.find('#btn_duplicate').show();
				modal_user_info.find('#in_userId').attr("readonly", false);
				$("#modal_user_info").modal();
		});
		
		/* 유저 삭제버튼 클릭시 */
		div_user_main.on('click', '#btn_main_delete', function() {
			fn_userDelete();
		});
	
		/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
		modal_user_info.on('click', '#btn_save', function() {
			fn_userSave();
		});
		
		/* 유저 신규,수정  팝업창 아이디 중복 체크 버튼 클릭시 */
		modal_user_info.on('click', '#btn_duplicate',function() {
			fn_duplicate();
		});
		
		/* 유저 신규,수정  팝업창 아이디 중복 체크 버튼 클릭시 */
		$("#modal_passChg").on('click', '#btn_newpwd_save',function() {
			fn_newPwdSave();
		});
		
		/* 유저 사용여부 */
		div_user_main.on('change', '#sel_useYn',function() {
			fn_tradeUserGridData();
		});
	}

	/************************************************************************
	* user list
	*************************************************************************/
	function fn_tradeUserGridData(){
		// 호출 URL
		var url = "/scheduler/tradeUser/selectUserAll.do";
		//조회 조건
		var param = {
				 	"sel_userId" 	: $('#sel_userId').val(),
	                "sel_useYn"		: $('#sel_useYn').val()
		};
		
		//컬럼 옵션
		var columns = [
						{"data": "rnum"},
						{"data": "user_id",
							"render": function(data, type, row){
								data = '<a href="javascript:void(0)" class="hover-underline">'+data+'</a>'
								return data;
							}
						},
						{"data": "user_nm"},
						{"data": "user_short_nm"},
						{"data": "dept_cd"},
						{"data": "jik_nm"},
						{"data": "user_div"},
						{"data": "role_nm"},
						{"data": "e_mail"}, 
						{"data": "gol_id"}
		           ];
		
		var columnDefs = {
			    'targets': [1,2,3,4,6,7,8],
			    "className": "text-center"
		};
		
		var gridObj = {
		        'grid_id': "tradeUserGridData",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': true
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
		        'ordering': true,
		        'cursorCols': [2]
	    };
		dataTableGridNew(gridObj,gridOptions);
	};
	
 	$('#tradeUserGridData tbody').on( 'dblclick', 'tr', function () {
 		var table = $("#tradeUserGridData").dataTable();
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
				setCheckBox("N", modal_user_info, '#in_useYn');
	      		this.reset();
	  		});
			
			console.log(row_data);
			modal_user_info.find('#in_userId').val(row_data.user_id).attr("readonly", "readonly");
			modal_user_info.find('#in_userName').val(row_data.user_nm);
			modal_user_info.find('#in_userShortName').val(row_data.user_short_nm);
			modal_user_info.find('#in_userEngName').val(row_data.user_enm);
			modal_user_info.find('#in_position').val(row_data.jik_cd);
			modal_user_info.find('#in_userType').val(row_data.user_tp);
			modal_user_info.find('#in_userCmp').val(row_data.cmp_cd);
			modal_user_info.find('#in_userDept').val(row_data.dept_cd);
			modal_user_info.find('#in_golId').val(row_data.gol_id);
			modal_user_info.find('#in_userEmail').val(row_data.e_mail);
			modal_user_info.find('#in_userPhone').val(row_data.phone_no);
			modal_user_info.find('#in_modId').val(row_data.mod_id);
			modal_user_info.find('#in_modDate').val(row_data.mod_dt);
			setCheckBox(row_data.use_yn , modal_user_info, '#in_useYn');
			
			$("#modal_user_info").modal();				//사용자 수정 Popup Open
			$('#form_pwd').hide();						//패스워드 숨김
			$('#form_pwd_cfm').hide();					//패스워드 확인 숨김
			$('#btn_submitPassChg').show();				//패스워드 변경 버튼 show
			$('#btn_quick_UserInfo').hide();
			$('.user_info_title').text('사용자 수정');
			check_duplicate ="Y";
			$("#in_eventDiv").val("U");
		}
	});

	/* 유저 신규 등록 및 수정 */
	function fn_userSave(){

		var url = "/scheduler/tradeUser/userEvent.do";
		var param = getJQParams(modal_user_info);
		var type = "script";
		if (check_duplicate != 'Y') {
			alert('아이디 중복 체크를 확인해 주세요');
			return false;
		}
		ajaxCall(url, type, param, fn_userSaveResult);
		function fn_userSaveResult(data){
			$('#modal_user_info').modal("hide"); //사용자 수정 Popup Close
			fn_tradeUserGridData();
		}
	}
	
	/* 유저 삭제 */
	function fn_userDelete() {
		var userGridData = $("#userGridData").dataTable();
		var delList = fn_SelectGridList("삭제",userGridData);
		if(delList.length > 0){
			var url = "/scheduler/tradeUser/deleteUser.do";
			var param = "deleteList=" + delList;
			var type = "script";
			ajaxCall(url, type, param, fn_tradeUserGridData);
		}
	}

	/* 유저 패스워드 확인 */
	function fn_newPwdSave() {
		var modal_passChg = $("#modal_passChg");
		var in_userId = $('#in_userId').val();
		var in_userNewPwd = modal_passChg.find('#in_userNewPwd').val();
		var in_NewPwdConfirmd = modal_passChg.find('#in_NewPwdConfirmd').val();
		
		if (in_userNewPwd == "" || in_NewPwdConfirmd == "" || in_userNewPwd != in_NewPwdConfirmd) {
			alert("패스워드를 확인하세요.");
			return false;
		}
		
		var url = "/scheduler/tradeUser/updatePw.do";
		var param = getJQParams(modal_passChg)+"&in_userId="+in_userId;
		var type = "json";

		ajaxCall(url, type, param, fn_newPwdSaveResult);
		function fn_newPwdSaveResult(data) {
			alert(data.result_msg);
			if (data.result_code == "0000") {
				$('#in_userNewPwd').val("");
				$('#in_NewPwdConfirmd').val("");
				$('#modal_passChg').modal("hide"); //닫기
			}
		}
	};

	
	/* 유저 아이디 중복 확인 */
	function fn_duplicate(){
		var in_userId = modal_user_info.find('#in_userId').val();
		if (in_userId == "") {
			alert("아이디를 입력하세요.");
			return false;
		}
		
		check_duplicate = "Y";
		var url = "/scheduler/tradeUser/selectUserInfo.do";
		var param =  getJQParams(modal_user_info);
		var type = "json";

		ajaxCall(url, type, param, fn_duplicateResult);

		function fn_duplicateResult(data) {
			console.log(data);
			alert(data.result_msg);
			if (data.result_code != "0000") {
				check_duplicate = "N";
			} else {
				check_duplicate = "Y";
			}
		}
	}

	$(document).ready(function(){
		fn_tradeUserGridData();
		fn_user_initEvent();
	});
</script>
