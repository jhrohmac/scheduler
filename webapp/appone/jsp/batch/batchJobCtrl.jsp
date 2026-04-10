<%@ page contentType="text/html; charset=utf-8" %>
<div class="container-fluid" id="div_batch_job_ctrl">

	<div class="card card-info">
		<div class="card-header">
			<h3 class="card-title">조회 조건</h3>
			<div class="card-tools">
				<button type="button" class="btn btn-tool" data-card-widget="collapse" title="Collapse">
					<i class="fas fa-minus"></i>
				</button>
			</div>
		</div>

		<div class="card-body p-2">
			<div class="form-group search_row">
				<label class="col-sm-1 col-form-label text-right">JOB ID</label>
				<div class="col-sm-2">
					<input type="text" id="in_job_id" class="form-control form-control-sm"
						   placeholder="예) INTEREST_STOCK_MASTER_REFRESH"
						   title="JOB ID로 부분 검색합니다. 예) INTEREST_STOCK"
						   data-toggle="tooltip" data-placement="top">
				</div>

				<label class="col-sm-1 col-form-label text-right">사용여부</label>
				<div class="col-sm-1 common select-all" id="USE_YN">
					<select class="form-control custom-select custom-select-sm" id="in_enable_yn"
							title="ENABLE(Y/N)로 필터링합니다."
							data-toggle="tooltip" data-placement="top"></select>
				</div>

				<label class="col-sm-1 col-form-label text-right">RUNNING</label>
				<div class="col-sm-1">
					<select class="form-control custom-select custom-select-sm" id="in_running_yn"
							title="실행 중(Y) / 미실행(N) 필터"
							data-toggle="tooltip" data-placement="top">
						<option value="">ALL</option>
						<option value="Y">Y</option>
						<option value="N">N</option>
					</select>
				</div>

				<label class="col-sm-1 col-form-label text-right">RESULT</label>
				<div class="col-sm-1">
					<select class="form-control custom-select custom-select-sm" id="in_result_code"
							title="마지막 실행 결과 코드로 필터"
							data-toggle="tooltip" data-placement="top">
						<option value="">ALL</option>
						<option value="SUCCESS">SUCCESS</option>
						<option value="ERROR">ERROR</option>
						<option value="SKIP">SKIP</option>
					</select>
				</div>

				<div class="col-sm-3 text-right">
					<button type="button" id="btn_batch_add" class="btn btn-primary btn-sm"
							title="새 배치 작업을 등록합니다."
							data-toggle="tooltip" data-placement="top">
						<i class="fa fa-plus"></i> 등록
					</button>
					<button type="button" id="btn_batch_search" class="btn btn-info btn-sm"
							title="조건에 맞게 목록을 조회합니다."
							data-toggle="tooltip" data-placement="top">
						<i class="fa fa-search"></i>
					</button>
					<button type="button" id="btn_batch_reload" class="btn btn-default btn-sm"
							title="목록을 새로고침합니다."
							data-toggle="tooltip" data-placement="top">
						<i class="fa fa-sync"></i>
					</button>
				</div>
			</div>

			<div class="small text-muted pl-1">
				배치는 서버 스케줄러가 수행합니다. 이 화면을 켜둘 필요는 없고, 설정(ENABLE/주기/건수)을 저장하면 즉시 반영됩니다.
			</div>
		</div>
	</div>

	<div class="row">
		<div class="col-12">
			<div class="card card-info">
				<div class="card-body table-responsive p-2">
					<table class="table table-hover table-sm" id="tblBatchJobCtrl">
						<thead>
							<tr>
								<th>JOB ID</th>
								<th>JOB NAME</th>
								<th>ENABLE</th>
								<th>INTERVAL(M)</th>
								<th>LIMIT</th>
								<th>STALE(D)</th>
								<th>COUNTRY</th>
								<th>RUNNING</th>
								<th>LAST START</th>
								<th>LAST END</th>
								<th>LAST CNT</th>
								<th>RESULT</th>
								<th>MESSAGE</th>
								<th style="width: 230px;">ACTION</th>
							</tr>
						</thead>
						<tbody></tbody>
					</table>

				</div>
			</div>
		</div>
	</div>
</div>

<!-- 등록/수정 POPUP -->
<div class="modal fade" id="modal_batch_job_edit" tabindex="-1" data-keyboard="true" data-backdrop="static">
	<div class="modal-dialog">
		<div class="modal-content">

			<div class="modal-header">
				<h4 class="modal-title">배치 설정</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>

			<div class="modal-body">
				<form class="form-horizontal">

					<input type="hidden" id="edit_mode" value="U">

					<div class="form-group row">
						<label class="col-sm-4 col-form-label text-right">JOB ID</label>
						<div class="col-sm-8">
							<input type="text" id="edit_job_id_view" class="form-control form-control-sm"
								   placeholder="예) INTEREST_STOCK_MASTER_REFRESH"
								   title="고유 식별자입니다. 대문자/언더스코어 권장. 등록 후 변경하지 않는 것을 권장합니다."
								   data-toggle="tooltip" data-placement="top">
							<small class="form-text text-muted">
								예) INTEREST_STOCK_MASTER_REFRESH (대문자+언더스코어, 공백/한글/특수문자 지양)
							</small>
						</div>
					</div>

					<div class="form-group row">
						<label class="col-sm-4 col-form-label text-right">JOB NAME</label>
						<div class="col-sm-8">
							<input type="text" id="edit_job_name" class="form-control form-control-sm"
								   placeholder="예) 관심종목 마스터 갱신(KIS search-stock-info)"
								   title="운영자가 알아보기 쉬운 설명 이름입니다. 데이터 출처/Endpoint를 함께 적으면 좋습니다."
								   data-toggle="tooltip" data-placement="top">
							<small class="form-text text-muted">
								예) 관심종목 마스터 갱신(KIS search-stock-info)
							</small>
						</div>
					</div>

					<div class="form-group row">
						<label class="col-sm-4 col-form-label text-right">ENABLE</label>
						<div class="col-sm-8">
							<div class="input-group input-group-sm">
								<select id="edit_enable_yn" class="form-control custom-select custom-select-sm"
										title="Y면 스케줄러가 자동 실행합니다. N이면 자동 실행하지 않고 수동 실행만 가능합니다."
										data-toggle="tooltip" data-placement="top">
									<option value="Y">Y</option>
									<option value="N">N</option>
								</select>
								<div class="input-group-append">
									<button type="button" class="btn btn-outline-secondary" id="btn_enable_toggle"
											title="자동 실행 ON/OFF"
											data-toggle="tooltip" data-placement="top">ON/OFF</button>
								</div>
							</div>
							<small class="form-text text-muted">
								Y: 주기 실행 / N: 주기 실행 중지(필요 시 수동 실행만)
							</small>
</div>
					</div>

					<div class="form-group row">
						<label class="col-sm-4 col-form-label text-right">INTERVAL(M)</label>
						<div class="col-sm-8">
							<input type="text" id="edit_interval_min" class="form-control form-control-sm"
								   inputmode="numeric" pattern="[0-9]*"
								   placeholder="예) 60(1시간), 360(6시간), 1440(1일)"
								   title="실행 주기(분)입니다. LAST_START 기준으로 INTERVAL이 경과하면 실행됩니다."
								   data-toggle="tooltip" data-placement="top">
							<small class="form-text text-muted">
								예) 360 = 6시간마다 실행
							</small>
						</div>
					</div>

					<div class="form-group row">
						<label class="col-sm-4 col-form-label text-right">LIMIT</label>
						<div class="col-sm-8">
							<input type="text" id="edit_limit_cnt" class="form-control form-control-sm"
								   inputmode="numeric" pattern="[0-9]*"
								   placeholder="예) 200"
								   title="한 번 실행 시 처리할 최대 건수입니다. KIS 호출/DB 작업량이 LIMIT에 비례합니다."
								   data-toggle="tooltip" data-placement="top">
							<small class="form-text text-muted">
								서버/호출량을 고려해 100~500 범위에서 조절 권장
							</small>
						</div>
					</div>

					<div class="form-group row">
						<label class="col-sm-4 col-form-label text-right">STALE(D)</label>
						<div class="col-sm-8">
							<input type="text" id="edit_stale_days" class="form-control form-control-sm"
								   inputmode="numeric" pattern="[0-9]*"
								   placeholder="예) 7"
								   title="마지막 갱신일(MODIFY_DATE)이 N일보다 오래된 종목만 대상으로 실행합니다."
								   data-toggle="tooltip" data-placement="top">
							<small class="form-text text-muted">
								예) 7 = 일주일 이상 미갱신 종목만 갱신(리소스 절약)
							</small>
						</div>
					</div>

					<div class="form-group row">
						<label class="col-sm-4 col-form-label text-right">COUNTRY</label>
						<div class="col-sm-8">
							<input type="text" id="edit_country_code" class="form-control form-control-sm"
								   placeholder="예) KR"
								   title="국가 코드입니다. 현재 국내만 지원하면 KR로 고정 사용을 권장합니다."
								   data-toggle="tooltip" data-placement="top">
							<small class="form-text text-muted">
								예) KR (현재 국내만 지원 시 KR 고정 권장)
							</small>
						</div>
					</div>

					<div class="form-group row mb-0">
						<label class="col-sm-4 col-form-label text-right">MESSAGE</label>
						<div class="col-sm-8">
							<textarea id="edit_last_msg" class="form-control form-control-sm" rows="4" readonly
									  title="마지막 실행 결과 요약/에러 메시지입니다(읽기 전용)."
									  data-toggle="tooltip" data-placement="top"></textarea>
							<small class="form-text text-muted">
								읽기 전용: 마지막 실행 결과/에러 확인용
							</small>
						</div>
					</div>

				</form>
			</div>

			<div class="modal-footer justify-content-between">
				<button type="button" class="btn btn-default btn-sm" data-dismiss="modal">Close</button>
				<button type="button" class="btn btn-primary btn-sm" id="btn_batch_save">Save</button>
			</div>

		</div>
	</div>
</div>

<script>
$(document).ready(function () {

	// Bootstrap tooltip
	$('[data-toggle="tooltip"]').tooltip();

	$("#btn_batch_add").on("click", function(){
		fn_openBatchCreate();
	});

	$("#btn_batch_search").on("click", function () {
		fn_batchJobCtrlGridData();
	});
	
	$("#btn_batch_reload").on("click", function () {
		fn_batchJobCtrlGridData();
	});

	$("#in_job_id").on("keydown", function (e) {
		if (e.keyCode === 13) fn_batchJobCtrlGridData();
	});

	$("#in_enable_yn, #in_running_yn, #in_result_code").on("change", function(){
		fn_batchJobCtrlGridData();
	});

	$("#btn_batch_save").on("click", function(){
		fn_batchJobCtrlSave();
	});


	$("#btn_enable_toggle").on("click", function(){
		var cur = ($("#edit_enable_yn").val() || "N").toUpperCase();
		$("#edit_enable_yn").val(cur === "Y" ? "N" : "Y");
	});
	
	$('#tblBatchJobCtrl').off('click', '.btn-job-stop');

	$('#tblBatchJobCtrl').on('click', '.btn-job-stop', function(){
	    var table = $('#tblBatchJobCtrl').DataTable();
	    var rowData = table.row($(this).closest('tr')).data();
	    fn_batchJobStop(rowData);
	});
	fn_batchJobCtrlGridData();
});

/************************************************************************
* Grid
*************************************************************************/
function fn_batchJobCtrlGridData(){

	var url = "/scheduler/finance/batchJobCtrlList.do";

	var param = {
		"in_job_id" : $("#in_job_id").val(),
		"in_enable_yn" : $("#in_enable_yn").val(),
		"in_running_yn" : $("#in_running_yn").val(),
		"in_result_code" : $("#in_result_code").val()
	};

	var columns = [
		{"data":"job_id"},
		{"data":"job_name"},
		{"data":"enable_yn"},
		{"data":"interval_min"},
		{"data":"limit_cnt"},
		{"data":"stale_days"},
		{"data":"country_code"},
		{"data":"running_yn"},
		{"data":"last_start_dt"},
		{"data":"last_end_dt"},
		{"data":"last_run_cnt"},
		{"data":"last_result_code"},
		{"data":"last_result_msg"},
		{"data":null}
	];

	var columnDefs = {
		"targets": [2,7,11,12,13],
		"className": "text-center",
		"render": function(data, type, row, meta){

			if (meta.col === 2) {
				var v = (data || "").toUpperCase();
				if (v === "Y") return '<span class="badge badge-success" title="자동 실행 ON" data-toggle="tooltip">ON</span>';
				return '<span class="badge badge-secondary" title="자동 실행 OFF" data-toggle="tooltip">OFF</span>';
			}

			if (meta.col === 7) {
				var r = (data || "").toUpperCase();
				if (r === "Y") return '<span class="badge badge-info" title="실행 중" data-toggle="tooltip">Y</span>';
				return '<span class="badge badge-blue" title="미실행" data-toggle="tooltip">N</span>';
			}

			if (meta.col === 11) {
				var rc = (data || "");
				if (rc === "SUCCESS") return '<span class="badge badge-success" title="정상 완료" data-toggle="tooltip">SUCCESS</span>';
				if (rc === "ERROR") return '<span class="badge badge-danger" title="오류" data-toggle="tooltip">ERROR</span>';
				if (rc === "SKIP") return '<span class="badge badge-warning" title="스킵(조건/락)" data-toggle="tooltip">SKIP</span>';
				if (rc) return '<span class="badge badge-info" data-toggle="tooltip">' + rc + '</span>';
				return '';
			}

			if (meta.col === 12) {
				var msg = (data || "");
				var safe = $('<div/>').text(msg).html();
				var shortMsg = safe;
				if (shortMsg.length > 80) shortMsg = shortMsg.substring(0, 80) + "...";
				return '<span title="' + safe + '" data-toggle="tooltip">' + shortMsg + '</span>';
			}

			if (meta.col === 13) {
			    var btns = '';
			    var isRunning = ((row.running_yn || "N").toUpperCase() === "Y");

			    btns += '<button type="button" class="btn btn-xs btn-primary mr-1 btn-job-edit" title="설정(수정)" data-toggle="tooltip">설정</button>';

			    if (isRunning) {
			        btns += '<button type="button" class="btn btn-xs btn-secondary mr-1 btn-job-run" disabled title="실행 중에는 실행 불가" data-toggle="tooltip">실행</button>';
			        btns += '<button type="button" class="btn btn-xs btn-warning mr-1 btn-job-stop" title="즉시 정지" data-toggle="tooltip">정지</button>';
			    } else {
			        btns += '<button type="button" class="btn btn-xs btn-success mr-1 btn-job-run" title="즉시 실행" data-toggle="tooltip">실행</button>';
			        btns += '<button type="button" class="btn btn-xs btn-secondary mr-1 btn-job-stop" disabled title="미실행 상태" data-toggle="tooltip">정지</button>';
			    }

			    btns += '<button type="button" class="btn btn-xs btn-danger btn-job-del" title="삭제(RUNNING=Y면 불가)" data-toggle="tooltip">삭제</button>';
			    return btns;
			}

			return data;
		}
	};

	var gridObj = {
		'grid_id': "tblBatchJobCtrl",
		'url': url,
		'param': param,
		'columns': columns,
		'columnDefs': columnDefs,
		'columnCheck': false
	};

	var gridOptions = {
		'serverSide': false,
		'searching': false,
		'paging': false,
		'lengthChange': false,
		'info': true,
		'autoWidth': false,
		'responsive': true,
		'bDestroy': true,
		'processing': true,
		'ordering': false
	};

	dataTableGridNew(gridObj, gridOptions);

	// tooltips after draw
	setTimeout(function(){
		$('[data-toggle="tooltip"]').tooltip();
	}, 50);

	$('#tblBatchJobCtrl').off('click', '.btn-job-edit');	$('#tblBatchJobCtrl').off('click', '.btn-job-run');
	$('#tblBatchJobCtrl').off('click', '.btn-job-del');

	$('#tblBatchJobCtrl').on('click', '.btn-job-edit', function(){
		var table = $('#tblBatchJobCtrl').DataTable();
		var rowData = table.row($(this).closest('tr')).data();
		fn_openBatchEdit(rowData);
	});
	$('#tblBatchJobCtrl').on('click', '.btn-job-run', function(){
		var table = $('#tblBatchJobCtrl').DataTable();
		var rowData = table.row($(this).closest('tr')).data();
		fn_batchJobRunNow(rowData);
	});

	$('#tblBatchJobCtrl').on('click', '.btn-job-del', function(){
		var table = $('#tblBatchJobCtrl').DataTable();
		var rowData = table.row($(this).closest('tr')).data();
		fn_batchJobDelete(rowData);
	});
}

/************************************************************************
* Modal Open (Create)
*************************************************************************/
function fn_openBatchCreate(){

	$("#edit_mode").val("I");
	$("#edit_job_id_view").val("").prop("readonly", false);
	$("#edit_job_name").val("");

	$("#edit_enable_yn").val("Y");
	$("#edit_interval_min").val("360");
	$("#edit_limit_cnt").val("200");
	$("#edit_stale_days").val("7");
	$("#edit_country_code").val("KR");
	$("#edit_last_msg").val("");

	$("#modal_batch_job_edit").modal("show");
}

/************************************************************************
* Modal Open (Edit)
*************************************************************************/
function fn_openBatchEdit(row){
	if (!row) return;

	$("#edit_mode").val("U");
	$("#edit_job_id_view").val(row.job_id || "").prop("readonly", true);

	$("#edit_job_name").val(row.job_name || "");
	$("#edit_enable_yn").val((row.enable_yn || "N").toUpperCase());
	$("#edit_interval_min").val(row.interval_min || "60");
	$("#edit_limit_cnt").val(row.limit_cnt || "200");
	$("#edit_stale_days").val(row.stale_days || "7");
	$("#edit_country_code").val(row.country_code || "KR");
	$("#edit_last_msg").val(row.last_result_msg || "");

	$("#modal_batch_job_edit").modal("show");
}

/************************************************************************
* Validation
*************************************************************************/
function fn_isPositiveInt(v){
	if (v === null || v === undefined) return false;
	v = $.trim(String(v));
	if (v === "") return false;
	return /^[0-9]+$/.test(v);
}

function fn_validateBatchForm(){
	var mode = $("#edit_mode").val();
	var jobId = $.trim($("#edit_job_id_view").val());
	var jobName = $.trim($("#edit_job_name").val());

	if (jobId === "") {
		showAlert("error", "JOB ID를 입력하세요.", 2000);
		return false;
	}
	if (!/^[A-Z0-9_]+$/.test(jobId)) {
		showAlert("error", "JOB ID는 대문자/숫자/언더스코어만 권장합니다.", 2500);
		return false;
	}
	if (jobName === "") {
		showAlert("error", "JOB NAME을 입력하세요.", 2000);
		return false;
	}

	var intervalMin = $("#edit_interval_min").val();
	var limitCnt = $("#edit_limit_cnt").val();
	var staleDays = $("#edit_stale_days").val();

	if (!fn_isPositiveInt(intervalMin)) {
		showAlert("error", "INTERVAL(M)은 숫자(분)로 입력하세요.", 2500);
		return false;
	}
	if (!fn_isPositiveInt(limitCnt)) {
		showAlert("error", "LIMIT은 숫자로 입력하세요.", 2500);
		return false;
	}
	if (!fn_isPositiveInt(staleDays)) {
		showAlert("error", "STALE(D)는 숫자(일)로 입력하세요.", 2500);
		return false;
	}

	var country = $.trim($("#edit_country_code").val());
	if (country === "") {
		showAlert("error", "COUNTRY를 입력하세요. (예: KR)", 2500);
		return false;
	}

	return true;
}

/************************************************************************
* Save (Insert/Update)
*************************************************************************/
function fn_batchJobCtrlSave(){

	if (!fn_validateBatchForm()) return;

	var mode = $("#edit_mode").val();
	var jobId = $.trim($("#edit_job_id_view").val());
	var jobName = $.trim($("#edit_job_name").val());

	var url = "/scheduler/finance/batchJobCtrlSave.do";
	if (mode === "I") url = "/scheduler/finance/batchJobCtrlInsert.do";

	var param =
		"job_id=" + encodeURIComponent(jobId)
		+ "&job_name=" + encodeURIComponent(jobName)
		+ "&enable_yn=" + encodeURIComponent($("#edit_enable_yn").val())
		+ "&interval_min=" + encodeURIComponent($("#edit_interval_min").val())
		+ "&limit_cnt=" + encodeURIComponent($("#edit_limit_cnt").val())
		+ "&stale_days=" + encodeURIComponent($("#edit_stale_days").val())
		+ "&country_code=" + encodeURIComponent($("#edit_country_code").val());

	ajaxCall(url, "json", param, function(res){
		showAlert("success", "저장되었습니다.", 1200);
		$("#modal_batch_job_edit").modal("hide");
		fn_batchJobCtrlGridData();
	});
}

/************************************************************************
* Toggle
*************************************************************************/
function fn_batchJobToggle(row){
	if (!row) return;

	var next = ((row.enable_yn || "N").toUpperCase() === "Y") ? "N" : "Y";

	showConfirm(
		"question",
		"사용여부를 변경하시겠습니까?",
		(row.job_id || "") + " : " + (next === "Y" ? "ON" : "OFF"),
		"Change",
		"Cancel",
		function(){
			var url = "/scheduler/finance/batchJobCtrlSave.do";
			var param =
				"job_id=" + encodeURIComponent(row.job_id || "")
				+ "&job_name=" + encodeURIComponent(row.job_name || "")
				+ "&enable_yn=" + encodeURIComponent(next)
				+ "&interval_min=" + encodeURIComponent(row.interval_min || "60")
				+ "&limit_cnt=" + encodeURIComponent(row.limit_cnt || "200")
				+ "&stale_days=" + encodeURIComponent(row.stale_days || "7")
				+ "&country_code=" + encodeURIComponent(row.country_code || "KR");

			ajaxCall(url, "json", param, function(){
				showAlert("success", "변경되었습니다.", 1200);
				fn_batchJobCtrlGridData();
			});
		}
	);
}

/************************************************************************
* Run Now
*************************************************************************/
function fn_batchJobRunNow(row){
	if (!row) return;

	showConfirm(
		"warning",
		"지금 실행하시겠습니까?",
		(row.job_id || "") + " 배치를 즉시 실행합니다.",
		"Run",
		"Cancel",
		function(){
			var url = "/scheduler/finance/batchJobRunNow.do";
			var param = "job_id=" + encodeURIComponent(row.job_id || "");
			ajaxCall(url, "json", param, function(res){
				showAlert("success", "실행 요청 완료", 1200);
				fn_batchJobCtrlGridData();
			});
		}
	);
}

function fn_batchJobStop(row){
    if (!row) return;

    if ((row.running_yn || "N").toUpperCase() !== "Y") {
        showAlert("error", "RUNNING=Y 상태에서만 정지할 수 있습니다.", 2000);
        return;
    }

    showConfirm(
        "warning",
        "지금 정지하시겠습니까?",
        (row.job_id || "") + " 배치를 정지 요청합니다.",
        "Stop",
        "Cancel",
        function(){
            var url = "/scheduler/finance/batchJobStop.do";
            var param = "job_id=" + encodeURIComponent(row.job_id || "");
            ajaxCall(url, "json", param, function(res){
                showAlert("success", "정지 요청 완료", 1200);
                fn_batchJobCtrlGridData();
            });
        }
    );
}

/************************************************************************
* Delete
*************************************************************************/
function fn_batchJobDelete(row){
	if (!row) return;

	if ((row.running_yn || "N").toUpperCase() === "Y") {
		showAlert("error", "RUNNING=Y 상태에서는 삭제할 수 없습니다.", 2000);
		return;
	}

	showConfirm(
		"warning",
		"삭제하시겠습니까?",
		(row.job_id || "") + " 작업을 삭제합니다.",
		"Delete",
		"Cancel",
		function(){
			var url = "/scheduler/finance/batchJobCtrlDelete.do";
			var param = "job_id=" + encodeURIComponent(row.job_id || "");
			ajaxCall(url, "json", param, function(res){
				var deleted = 0;
				try {
					deleted = (res && res.data && res.data.singleData) ? parseInt(res.data.singleData, 10) : 0;
				} catch(e) {}

				if (deleted > 0) {
					showAlert("success", "삭제되었습니다.", 1200);
				} else {
					showAlert("error", "삭제되지 않았습니다. (RUNNING 상태 확인)", 2200);
				}
				fn_batchJobCtrlGridData();
			});
		}
	);
}
</script>
