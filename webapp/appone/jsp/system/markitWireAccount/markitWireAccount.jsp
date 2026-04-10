<%@ page contentType="text/html; charset=utf-8" %>
<!-- bootstrap color picker -->
<div class="container-fluid" id="div_markitwire_main">
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
				<label for="sel_markitwireId" class="col-sm-1 col-form-label text-right">MarkitWire 정보</label>
				<div class="col-sm-3">
					<input type="text" id="sel_markitwireId" name="sel_markitwireId" class="form-control form-control-sm" placeholder="MarkitWire 정보">
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
							<button type="button" class="btn btn-xs" id="btn_main_all">
								<i class="fas fa-gear right"></i>
							</button>
						</li>
			            <li class="nav-item">
			            	<a class="nav-link active" id="custom-content-below-home-tab" 
			                	data-toggle="pill" href="#custom-content-below-home" 
			                	role="tab" aria-controls="custom-content-below-home" 
			                		aria-selected="true">UAT</a>
		              	</li>
		              	<li class="nav-item">
		                	<a class="nav-link" id="custom-content-below-home-tab" 
		    	            	data-toggle="pill" href="#custom-content-below-home" 
			                	role="tab" aria-controls="custom-content-below-home" 
			                		aria-selected="false">PROD</a>
		              	</li>
		            </ul>
		            <div class="tab-content">
		            	<div class="tab-pane fade show active" id="custom-content-below-home" role="tabpanel" aria-labelledby="custom-content-below-home-tab">
			            	<table class="table table-hover table-sm" id="mwGridData">
								<thead>
									<tr>
										<th></th>
										<th>시스템</th>
										<th>사용자</th>
										<th>계정명</th>
										<th>패스워드</th>
										<th>변경여부</th>
										<th>변경일자</th>
										<th>Next</th>
										<th>E-Mail</th>
										<th>태그</th>
										<th>용도</th>
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

<!-- 등록 / 수정 POPUP -->
<div class="modal fade" id="modal_markitwire" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title">등록</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<div class="form-group row">
					<label for="in_systemFlag" class="col-sm-3 control-label text-right">시스템</label>
					<div class="col-lg-8">
						<select id="in_systemFlag"  name="in_systemFlag" class="form-control form-control-sm">
							<option value="UAT">UAT</option>
							<option value="PROD">PROD</option>
						</select>
					</div>
				</div>
	   			<div class="form-group row" style="margin-bottom: 4px;">
					<label for="in_userId" class="col-sm-3 control-label text-right"><span>*</span>사용자</label>
					<div class="col-lg-8 mb-3">
						<div class="input-group">
							<input type="text" class="form-control form-control-sm" id="in_userId" onkeypress="fn_enter(fn_duplicate);">
						</div>
					</div>
				</div>
				<div class="form-group row">
					<label for="in_markitWireAccount" class="col-sm-3 control-label text-right"><span>*</span>계정명</label>
					<div class="col-lg-8">
						<input type="text" id="in_markitWireAccount"  name="in_markitWireAccount" class="form-control form-control-sm" placeholder="markitWire Account">
					</div>
				</div>
				<div class="form-group row">
					<label for="in_markitWirePassword" class="col-sm-3 control-label text-right">패스워드</label>
					<div class="col-lg-8">
						<input type="text" id="in_markitWirePassword"  name="in_markitWirePassword" class="form-control form-control-sm" placeholder="Password">
					</div>
				</div>
				<div class="form-group row">
					<label for="in_changeDate" class="col-sm-3 control-label text-right">Change Date</label>
					<div class="col-lg-4">
						<div class="input-group date" id="setChangeDate" data-target-input="nearest">
	                    <input type="text" class="form-control form-control-sm datetimepicker-input"
	                    	id="in_changeDate" style="text-align: center;" data-target="#setChangeDate"/>
		                    <div class="input-group-append" data-target="#setChangeDate" data-toggle="datetimepicker">
		                        <div class="input-group-text"><i class="fa fa-calendar"></i></div>
		                    </div>
	                	</div>
					</div>
				</div>
				<div class="form-group row">
					<label for="in_nextDate" class="col-sm-3 control-label text-right">Next Date</label>
					<div class="col-lg-4">
						<div class="input-group date" id="setEndDate" data-target-input="nearest">
	                    <input type="text" class="form-control form-control-sm datetimepicker-input"
	                    	id="in_nextDate" style="text-align: center;" data-target="#setNextDate"/>
		                    <div class="input-group-append" data-target="#setNextDate" data-toggle="datetimepicker">
		                        <div class="input-group-text"><i class="fa fa-calendar"></i></div>
		                    </div>
	                	</div>
					</div>
				</div>
				<div class="form-group row">
					<label for="in_markitWireEmail" class="col-sm-3 control-label text-right">E-mail</label>
					<div class="col-lg-8">
						<input type="email" id="in_markitWireEmail"  name="in_markitWireEmail" class="form-control form-control-sm" placeholder="E-mail">
					</div>
				</div>
				<div class="form-group row">
					<label for="in_markitWireTag" class="col-sm-3 control-label text-right">태그</label>
					<div class="col-lg-8">
						<input id="in_markitWireTag"  name="in_markitWireTag" class="form-control form-control-sm"/>
					</div>
				</div>
				<div class="form-group row">
					<label for="in_markitWireDesc" class="col-sm-3 control-label text-right">용도</label>
					<div class="col-lg-8">
						<input id="in_markitWireDesc"  name="in_markitWireDesc" class="form-control form-control-sm"/>
					</div>
				</div>
				<div class="form-group row">
					<label for="in_useFlag" class="col-sm-3 control-label text-right">Y/N</label>
					<div class="col-lg-2">
						<label id="check_label">
		                  <input type="checkbox" id="in_useFlag" class="flat-red" >
		                </label>
					</div>
				</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-primary" id="btn_save">Save&Changes</button>
				</div>
			</div>
		</div>
	</div>
</div>

<!-- 일괄 변경 POPUP -->
<div class="modal fade" id="modal_markitwire_all" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h6 class="modal-title" id="modal-title-all">일괄 변경</h6>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
				</button>
			</div>
			<div class="modal-body">
				<div class="form-group row">
					<label for="all_systemFlag" class="col-sm-3 control-label text-right">SYSTEM</label>
					<div class="col-lg-6">
						<select id="all_systemFlag"  name="all_systemFlag" class="form-control form-control-sm">
							<option value="UAT">UAT</option>
							<option value="PROD">PROD</option>
						</select>
					</div>
				</div>
				<div class="form-group row">
					<label for="all_markitWireEmail" class="col-sm-3 control-label text-right">Send E-mail</label>
					<div class="col-lg-7">
						<input type="email" id="all_markitWireEmail"  name="all_markitWireEmail" class="form-control form-control-sm" placeholder="E-mail">
					</div>
				</div>
				<div class="form-group row">
					<label for="all_markitWirePassword" class="col-sm-3 control-label text-right">Next Password</label>
					<div class="col-lg-8">
						<input type="text" id="all_markitWirePassword"  name="all_markitWirePassword" class="form-control form-control-sm" placeholder="Change Password">
					</div>
				</div>
				<div class="form-group row">
					<label for="all_nextDate" class="col-sm-3 control-label text-right">Next Date</label>
					<div class="col-lg-4">
						<h5 id="all_nextDateALL"></h5>
						<input type="hidden" id="all_nextDate"  name="all_nextDate">
					</div>
				</div>
				<div class="form-group row">
					<label for="all_markitWireURL" class="col-sm-3 control-label text-right">URL</label>
					<div class="col-lg-9">
						<select id="all_markitWireURL"  name="all_markitWireURL" class="form-control form-control-sm">
							<option value="https://opsw.uat.passwordreset.markit.com/#/login">https://opsw.uat.passwordreset.markit.com/#/login</option>
							<option value="https://opsw.passwordreset.markit.com/#/login">https://opsw.passwordreset.markit.com/#/login</option>
						</select>
					</div>
				</div>
			</div>
			<div class="modal-footer  justify-content-between">
				<button type="button" class="btn btn-sm btn-default" data-dismiss="modal">Close</button>
				<div class="form-group float-right">
					<button type="button" class="btn btn-sm btn-primary" id="btn_saveALL">Changes ALL</button>
				</div>
			</div>
		</div>
	</div>
</div>

<script>
	var systemFlag="UAT";
	var modal_markitwire = $('#modal_markitwire');			//등록/수정 POPUP
	var modal_markitwire_all = $('#modal_markitwire_all');	//등록/수정 POPUP
	/* 각 역영들을 미리 선언 */
	var div_markitwire_main = $('#div_markitwire_main');

	/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
	div_markitwire_main.on('click', '#custom-content-below-home-tab', function() {
		systemFlag = this.text;
		fn_mwGridData();
	});
	
	/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
	div_markitwire_main.on('click', '#btn_main_search', function() {
		fn_mwGridData();
	});
	
	
	modal_markitwire_all.on('click', '#btn_saveALL', function() {
		fn_markitWireAccountChangeALL();
	});
	
	div_markitwire_main.on('click', '#btn_main_all', function() {
		
		showConfirm(
				  'info',
				  '패스워드 변경을 진행하시겠습니까?',
				  'MarkitWire 패스워드 변경은 시스템에 즉시 적용됩니다.',
				  'Change',
				  'Cancel',
				  fn_modalMarkitwire_all
				);

	});
	function fn_initEvent(){
		/* 유저 삭제버튼 클릭시 */
		div_markitwire_main.on('click', '#btn_delete', function() {
			fn_MarkitWireAccountDelete();
		});
	
		/* 유저 신규 등록 및 수정 팝업창창 신청버튼 클릭시 - 팝업창 [N]ew:등록, [M]odify:수정 */
		modal_markitwire.on('click', '#btn_save', function() {
			fn_markitWireAccountChangeALL();
		});
		
		/* 사용여부 */
		div_markitwire_main.on('change', '#sel_useYn',function() {
			fn_mwGridData();
		});
	}

	// field 검증 & 에러 토글
	function validateRealtimeReg(fieldId, errorId, type) {
	  var val = $('#' + fieldId).val().trim();
	  var $err = $('#' + errorId);

	  switch(type) {
	    case 'text':  // username 빈값 체크
	      if (val === '') {
	        $err.text('이름을 입력해주세요.');
	        return false;
	      }
	      break;
	    case 'id':    // userId 영문만
	      if (!isAlphanumeric(val) || val === '') {
	        $err.text('아이디는 영문 대/소문자 및 숫자만 입력 가능합니다.');
	        return false;
	      }
	      break;
	  }
	  $err.text('');
	  return true;
	}
	
	function fn_modalMarkitwire_all(){
	
		const now = new Date();
		const currentHour = now.getHours();
		 // ─── 여기에 시간 검증이 필요하다면 넣으세요 ───
	   /*  if (systemFlag === "UAT" && currentHour < 18) {
	      Swal.fire('주의', 'UAT 계정 변경은 18시 이후에 진행해주세요.', 'warning');
	      return;
	    }
	    if (systemFlag === "PROD" && currentHour < 18) {
	      Swal.fire('주의', 'PROD 계정 변경은 18시 이후에만 가능합니다.', 'warning');
	      return;
	    } */
		
		//$("#all_systemFlag").removeAttr("disabled");
		
		// selectedIndex 사용
		if (systemFlag === 'UAT') {
		  // 첫 번째 옵션 선택
		  $('#all_markitWireURL').prop('selectedIndex', 0);
		  $('#all_markitWireURL').css('color', ''); 
		}
		else if (systemFlag === 'PROD') {
		  // 두 번째 옵션 선택
		  $('#all_markitWireURL').prop('selectedIndex', 1);
		  $('#all_markitWireURL').css('color', 'red');
		}
		// 선택 변경 이벤트가 필요하면
		$('#all_markitWireURL').change();

		// ▶ 오늘 기준 3개월 후 날짜 + 요일 계산
		const threeMonthsLater = new Date(now);
		threeMonthsLater.setMonth(now.getMonth() + 3);

		const year = threeMonthsLater.getFullYear();
		const month = String(threeMonthsLater.getMonth() + 1).padStart(2, '0');
		const day = String(threeMonthsLater.getDate()).padStart(2, '0');

		const weekDays = ['일', '월', '화', '수', '목', '금', '토'];
		const weekDayName = weekDays[threeMonthsLater.getDay()];
		const formattedDateWithDay = year+"-"+month+"-"+day+"("+weekDayName+")";
		$('#all_nextDateALL').text(formattedDateWithDay);
		$('#all_nextDate').val(formattedDateWithDay);
		
		modal_markitwire_all.find("#all_markitWireURL").attr("disabled",true);
		modal_markitwire_all.find("#all_systemFlag").val(systemFlag);
		modal_markitwire_all.find("#all_systemFlag").attr("disabled",true);
		$('#modal-title-all').text(systemFlag+" 일괄 변경");
		$('#modal_markitwire_all').modal();
	}
	
	/************************************************************************
	* user list
	*************************************************************************/
	function fn_mwGridData(){
		// 호출 URL
		var url = "/scheduler/markitWireAccount/selectMarkitWireAccountList.do";
		//조회 조건
		var param = {
					"sel_systemFlag" 	: systemFlag,
				 	"sel_markitwireId" 	: $('#sel_markitwireId').val(),
	                "sel_useYn"			: $('#sel_useYn').val()
		};
		//컬럼 옵션
		var columns = [
						{"data": "rnum"},
						{"data": "system_flag"},
						{"data": "user_id",
							"render": function(data, type, row){
								data = '<a href="javascript:void(0)" class="hover-underline">'+data+'</a>'
								return data;
							}
						},
						{"data": "markitwire_account"},
						{"data": "markitwire_password"},
						{"data": "change_yn"}, 
						{"data": "change_date"}, 
						{"data": "next_date"}, 
						{"data": "markitwire_email"},
						{"data": "markitwire_tag"},
						{"data": "markitwire_desc"},
						{"data": "use_flag"}
		           ];
		
		var columnDefs = {
			    'targets': [0,1,2,3,4,5,6,7,9,11],
			    "className": "text-center"
		};
		
		var gridObj = {
		        'grid_id': "mwGridData",
		        'url': url,
		        'param': param,
		        'columns': columns,
		        'columnDefs': columnDefs,
		        'columnCheck': false			//체크박스 유무
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

 	$('#mwGridData tbody').on( 'dblclick', 'tr', function (){
 		var table = $("#mwGridData").dataTable();
		// 현재 클릭한 행
	    var $row = $(this);
	    // 다른 행들의 선택 상태를 해제
	    table.$('tr.selected').not($row).removeClass('selected');
	    // 현재 행의 선택 상태를 토글
	    $row.toggleClass('selected');
		var column_index = $(this).children('td').index($(event.target).closest('td'));  // 클릭한 column index
		   	var row_position = table.fnGetPosition(this);
	   		var row_data = table.fnGetData(row_position);
	   		fn_resetForm(modal_markitwire);
			
			$('.modal-title').text('수정');
			$("#modal_markitwire").modal();		//사용자 수정 Popup Open
	});

	/* MarkitWire 패스워드 변경 저장 */
	function fn_markitWireAccountChangeALL(){
		var email = $("#all_markitWireEmail").val();
		var password = $("#all_markitWirePassword").val();
		if(email == ""){
			alert("E-Mail을 입력하세요.");
			return false;
		}
		if(password == ""){
			alert("Password를 입력하세요.");
			return false;
		}
		var url = "/scheduler/markitWireAccount/markitWireAccountChangeALL.do";
		var param = getJQParams(modal_markitwire_all);
		var type = "script";
		ajaxCall(url, type, param, fn_markitWireAccountSaveResult);
		function fn_markitWireAccountSaveResult(data){
			$('#modal_markitwire_all').modal("hide"); //사용자 수정 Popup Close
			fn_mwGridData();
		}
	}
	
	/* MarkitWire 삭제 */
	function fn_markitWireAccountDelete() {
		var mwGridData = $("#mwGridData").dataTable();
		var delList = fn_SelectGridList("삭제",mwGridData);
		if(delList.length > 0){
			var url = "/scheduler/markitWireAccount/deleteMarkitWireAccount.do";
			var param = "deleteList=" + delList;
			var type = "script";
			ajaxCall(url, type, param, fn_mwGridData);
		}
	}
	
	$(document).ready(function () {
		//변경 일자
	    $('#setChangeDate').datetimepicker({
	        format: 'YYYY-MM-DD'
	    });
		
		//만료 일자
	    $('#setNextDate').datetimepicker({
	        format: 'YYYY-MM-DD'
	    });
		
	    fn_mwGridData();
	});
	fn_initEvent();
</script>
