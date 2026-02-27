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
					<input type="text" id="sel_userId" name="sel_userId" class="form-control form-control-sm" placeholder="사용자 ID or 사용자명">
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label text-right">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
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
							<button type="button" class="btn btn-xs" id="btn_main_delete">
								<i class="fas fa-user-minus right"></i>
							</button>
						</li>
		            </ul>
		            <div class="tab-content" id="custom-content-below-tabContent">
		            	<div class="tab-pane fade show active" id="custom-content-below-home" role="tabpanel" aria-labelledby="custom-content-below-home-tab">
			            	<table class="table table-hover table-sm" id="userGridData">
								<thead>
									<tr>
										<th></th>
										<th>아이디</th>
										<th>이름</th>
										<th>직급</th>
										<th>부서</th>
										<th>권한</th>
										<th>이메일</th>
										<th>모바일</th>
										<th>메일</th>
										<th>SMS</th>
										<th>시작페이지</th>
										<th>Y/N</th>
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
						<label for="in_userDesk" class="col-sm-2 control-label text-right">부서</label>
						<div class="col-lg-3 common select-nonall" id="DEPT">
							<select id="in_userDesk"  name="in_userDept" class="form-control form-control-sm"></select>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userEmail" class="col-sm-3 control-label text-right">Email</label>
						<div class="col-lg-8">
							 <input type="email" id="in_userEmail" name="in_userEmail" 
							 	class="form-control form-control-sm"placeholder="Email"/>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_userPhone" class="col-sm-3 control-label text-right">M-blie</label>
						<div class="col-lg-8">
		                    <input type="text" id="in_userPhone"  name="in_userPhone" 
		                    	class="form-control form-control-sm" placeholder="M-blie"/>
		                  </div>
					</div>
					<div class="form-group row mb-1">
						<label for="in_MailYn" class="col-sm-3 control-label text-right">메일수신</label>
						<div class="col-lg-2">
							<label id="check_label">
			                  <input type="checkbox" id="in_MailYn" class="flat-red" >
			                </label>
						</div>
						<label class="col-sm-3 control-label text-right">Color</label>
						<div class="col-lg-2">
							<div class="user-color-picker">
  								<input type="color" id="in_userColor" value="#8888ff" />
							</div>
						</div>
					</div>
					<div class="form-group row mb-1">
						<label for="in_SmsYn" class="col-sm-3 control-label text-right">SMS수신</label>
						<div class="col-lg-2">
							<label id="check_label">
			                	<input type="checkbox" id="in_SmsYn" class="flat-red" >
			                </label>
						</div>
						<label class="col-sm-3 control-label text-right">Avatar</label>
						<div class="col-lg-2">
							<div class="btn-group">
		                    <button type="button" class="btn btn-sm dropdown-toggle pl-0" data-toggle="dropdown" data-offset="-7">
		                      <img src="/scheduler/appone/plugins/dist/img/avatar.png" id="userAvatar" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
		                    </button>
		                    <div class="dropdown-menu p-0 popAvatar" role="menu">
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/dist/img/avatar.png" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/dist/img/avatar2.png" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    	<a class="dropdown-avatarItem">
		                    		<img src="/scheduler/appone/plugins/dist/img/avatar3.png" 
	              						class="user-image img-circle user-img-bordered-sm" alt="Avatar Image">
								</a>
		                    </div>
		                  </div>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_EmploymentDate" class="col-sm-3 control-label text-right">입사일자</label>
						<div class="col-lg-4">
							<div class="input-group date" id="setEmploymentDate" data-target-input="nearest">
		                    <input type="text" class="form-control form-control-sm datetimepicker-input"
		                    	id="in_EmploymentDate" style="text-align: center;" data-target="#setEmploymentDate"/>
			                    <div class="input-group-append" data-target="#setEmploymentDate" data-toggle="datetimepicker">
			                        <div class="input-group-text"><i class="fa fa-calendar"></i></div>
			                    </div>
		                	</div>
						</div>
					</div>					
					<div class="form-group row">
						<label for="in_userStartPage" class="col-sm-3 control-label text-right">시작페이지</label>
						<div class="col-lg-8" id="STARTPAGE">
							<select id="in_userStartPage" name="in_userStartPage"
								class="form-control form-control-sm"></select>
						</div>
					</div>
					<div class="form-group row">
						<label for="in_Role" class="col-sm-3 control-label text-right">사용자권한</label>
						<div class="col-lg-8 common select-nonall" id="ROLE">
							<select id="in_Role"  name="in_Role" class="form-control form-control-sm"></select>
						</div>
					</div>
					<div class="form-group row mb-1">
						<label for="in_useYn" class="col-sm-3 control-label text-right">사용유무</label>
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
				<input type="hidden" id="in_userAvatar" value="">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-danger"data-toggle="modal" data-target="#modal_passChg" id="btn_submitPassChg">패스워드 변경</button>
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
		fn_userGridData();
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
			fn_userGridData();
		});
	}

	/************************************************************************
	* user list
	*************************************************************************/
	function fn_userGridData(){
		// 호출 URL
		var url = "/scheduler/user/selectUserAll.do";
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
						{"data": "position_desc"},
						{"data": "desk_div"},
						{"data": "role_desc"},
						{"data": "email"}, 
						{"data": "hp_no"},
						{"data": "mail_yn_desc"},
						{"data": "sms_yn_desc"},
						{"data": "defaultpagename"},
						{"data": "use_yn"}
		           ];
		
		var columnDefs = {
			    'targets': [1,2,3,4,6,7,8,11],
			    "className": "text-center"
		};
		
		var gridObj = {
		        'grid_id': "userGridData",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': true
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
	};
	
 	$('#userGridData tbody').on( 'dblclick', 'tr', function () {
 		var table = $("#userGridData").dataTable();
		// 현재 클릭한 행
	    var $row = $(this);
	    // 다른 행들의 선택 상태를 해제
	    table.$('tr.selected').not($row).removeClass('selected');
	    // 현재 행의 선택 상태를 토글
	    $row.toggleClass('selected');
	
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		if(column_index == '1'){
			//시작페이지 리스트
			fn_userStartPageList();
			
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
			
			$('.modal-body form').each(function() {
				/* checkBox setting */
				setCheckBox("N", modal_user_info, '#in_MailYn');
				setCheckBox("N", modal_user_info, '#in_SmsYn');
	      		this.reset();
	  		});
			
			modal_user_info.find('#in_userId').val(row_data.user_id).attr("readonly", "readonly");
			modal_user_info.find('#in_userName').val(row_data.user_nm);
			modal_user_info.find('#in_userDesk').val(row_data.desk_div);
			modal_user_info.find('#in_position').val(row_data.position);
			modal_user_info.find('#in_userEmail').val(row_data.email);
			modal_user_info.find('#in_userPhone').val(row_data.hp_no);
			modal_user_info.find('#in_userColor').val(row_data.user_color);
			modal_user_info.find('#in_userAvatar').val(row_data.user_avatar);
			$("#userAvatar").attr("src", row_data.user_avatar);
			modal_user_info.find('#in_userStartPage').val(row_data.defaultpage);
			modal_user_info.find('#in_userEndDate').val(row_data.use_end_date);
			modal_user_info.find('#in_ExtensionDate').val(row_data.extension_date);
			modal_user_info.find('#in_EmploymentDate').val(row_data.employment_date);
			modal_user_info.find('#btn_duplicate').hide();
			
			/* selectBox setting */
			$("#in_Lang").val(row_data.lang);
			$("#in_Role").val(row_data.role);
			
			/* checkBox setting */
			setCheckBox(row_data.mail_yn , modal_user_info, '#in_MailYn');
			setCheckBox(row_data.sms_yn, modal_user_info, '#in_SmsYn');
			
			setCheckBox(row_data.use_yn, modal_user_info, '#in_useYn');
			
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
	function fn_userSave() {

		modal_user_info.find('#in_Lang').val('KOR');

		setCheckBoxYN(modal_user_info, '#in_MailYn');
		setCheckBoxYN(modal_user_info, '#in_SmsYn');

		var url = "/scheduler/user/userEvent.do";
		var param = getJQParams(modal_user_info);
		var type = "script";
		if (check_duplicate != 'Y') {
			alert('아이디 중복 체크를 확인해 주세요');
			return false;
		}
		ajaxCall(url, type, param, fn_userSaveResult);
		function fn_userSaveResult(data){
			$('#modal_user_info').modal("hide"); //사용자 수정 Popup Close
			fn_userGridData();
		}
	}
	
	/* 유저 삭제 */
	function fn_userDelete() {
		var userGridData = $("#userGridData").dataTable();
		var delList = fn_SelectGridList("삭제",userGridData);
		if(delList.length > 0){
			var url = "/scheduler/user/deleteUser.do";
			var param = "deleteList=" + delList;
			var type = "script";
			ajaxCall(url, type, param, fn_userGridData);
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
		
		var url = "/scheduler/user/updatePw.do";
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
	
	/* 연장 승인*/	
	function fn_extensionSubmit(sel_userId,extension_date){
		if(confirm("연장 승인하겠습니까?")!=false){
			var url = "/scheduler/user/updateExtensionDate.do";
			var param =  getJQParams(modal_user_info);
			var type = "script";
			ajaxCall(url, type, param, fn_userGridData);
		}
	}
	
	/* 유저 아이디 중복 확인 */
	function fn_duplicate(){
		var in_userId = modal_user_info.find('#in_userId').val();
		if (in_userId == "") {
			alert("아이디를 입력하세요.");
			return false;
		}
		
		check_duplicate = "Y";
		var url = "/scheduler/user/selectUserInfo.do";
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
	
	$(document).ready(function () {

		//조회 일자
		$('#setSelectDate').datetimepicker({
	        locale: 'ko',          // 한국어 로케일
	        format: 'YYYY-MM',     // 연-월 형식
	        viewMode: 'months',    // 월 단위 뷰 (년도 선택 후 월 선택)
	        defaultDate: moment()
    	});
		
		//연장기간 일자
	    $('#setExtensionDate').datetimepicker({
	        format: 'YYYY-MM-DD'
	    });
		
		//사용기간 만료일자
	    $('#setEndDate').datetimepicker({
	        format: 'YYYY-MM-DD'
	    });
		
		//고용일자
	    $('#setEmploymentDate').datetimepicker({
	        format: 'YYYY-MM-DD'
	    });
	});

	/* page loading 시 검색 영역의 selectbox Setting */
	function fn_userStartPageList(){
		var url = "/scheduler/user/userStartPageList.do";
		var param = "";
		var type = "html";
		ajaxCall(url, type, param, fn_userStartPageListResult);
		function fn_userStartPageListResult(data) {
			modal_user_info.find('#STARTPAGE select').html(data);
		}
	}
	$(document).ready(function(){
		fn_userGridData();
		fn_user_initEvent();
		// 모든 아바타 아이템을 선택
	    $(".dropdown-avatarItem img").on("click", function () {
	        // 클릭한 이미지의 src를 mainAvatar의 src로 변경
	        $("#userAvatar").attr("src", $(this).attr("src"));
	        $("#in_userAvatar").val($(this).attr("src"));
	    });
	});
</script>
