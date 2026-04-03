<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<link rel="stylesheet" href="<c:url value='/appone/jsp/stock/batchAdmin.css?v=20260403-2' />">

<div class="container-fluid" id="div_stock_batch_admin">
    <div class="card card-info">
        <div class="card-header">
            <h3 class="card-title">Stock Batch Admin</h3>
        </div>
        <div class="card-body p-2">
            <div class="form-group search_row">
                <label class="col-sm-1 col-form-label text-right">JOB ID</label>
                <div class="col-sm-2"><input type="text" id="in_job_id" class="form-control form-control-sm"></div>

                <label class="col-sm-1 col-form-label text-right">TASK</label>
                <div class="col-sm-2"><select id="in_task_key" class="form-control form-control-sm"><option value="">ALL</option></select></div>

                <label class="col-sm-1 col-form-label text-right">ENABLE</label>
                <div class="col-sm-1">
                    <select id="in_enabled_yn" class="form-control form-control-sm">
                        <option value="">ALL</option><option value="Y">Y</option><option value="N">N</option>
                    </select>
                </div>

                <label class="col-sm-1 col-form-label text-right">RUNNING</label>
                <div class="col-sm-1">
                    <select id="in_running_yn" class="form-control form-control-sm">
                        <option value="">ALL</option><option value="Y">Y</option><option value="N">N</option>
                    </select>
                </div>

                <div class="col-sm-2 text-right">
                    <button type="button" id="btn_search" class="btn btn-info btn-sm"><i class="fa fa-search"></i></button>
                    <button type="button" id="btn_reload" class="btn btn-default btn-sm"><i class="fa fa-sync"></i></button>
                    <button type="button" id="btn_add" class="btn btn-primary btn-sm"><i class="fa fa-plus"></i> 등록</button>
                </div>
            </div>
        </div>
    </div>

    <div class="card card-info">
        <div class="card-body table-responsive p-2">
            <table class="table table-hover table-sm w-100" id="tblBatchAdmin">
                <thead>
                <tr>
                    <th>JOB ID</th>
                    <th>JOB NAME</th>
                    <th>TASK</th>
                    <th>ENABLE</th>
                    <th>RUNNING</th>
                    <th>NEXT RUN</th>
                    <th>LAST START</th>
                    <th>LAST END</th>
                    <th>RESULT</th>
                    <th>ERROR</th>
                    <th style="width:260px;">ACTION</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>

    <div class="card card-secondary">
        <div class="card-header"><h3 class="card-title">실행 로그</h3></div>
        <div class="card-body table-responsive p-2">
            <table class="table table-sm w-100" id="tblBatchLog">
                <thead>
                <tr>
                    <th>EXEC ID</th>
                    <th>BASE DT</th>
                    <th>STATUS</th>
                    <th>TOTAL</th>
                    <th>SUCCESS</th>
                    <th>FAIL</th>
                    <th>START</th>
                    <th>END</th>
                    <th>ERROR</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>

    <div class="card card-danger" id="div_item_fail_log" style="display:none;">
        <div class="card-header">
            <h3 class="card-title">실패 종목 상세 <small id="lbl_item_fail_exec_id" class="text-light ml-2"></small></h3>
        </div>
        <div class="card-body table-responsive p-2">
            <table class="table table-sm w-100" id="tblBatchItemFailLog">
                <thead>
                <tr>
                    <th>종목코드</th>
                    <th>종목명</th>
                    <th>시장</th>
                    <th>에러 내용</th>
                    <th>발생시각</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="modal_batch_admin_edit" tabindex="-1" data-keyboard="true" data-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">Batch 설정</h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="edit_mode" value="I">

                <div class="form-group row">
                    <label class="col-sm-2 col-form-label text-right">
                        JOB ID
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="배치 작업의 고유 식별자입니다. 영문 대문자/숫자/언더스코어 형태를 권장합니다.">?</span>
                    </label>
                    <div class="col-sm-4"><input id="edit_job_id" class="form-control form-control-sm"></div>
                    <label class="col-sm-2 col-form-label text-right">
                        JOB NAME
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="화면에 표시되는 배치 이름입니다. 운영자가 구분하기 쉬운 설명형 이름으로 입력하세요.">?</span>
                    </label>
                    <div class="col-sm-4"><input id="edit_job_name" class="form-control form-control-sm"></div>
                </div>

                <div class="form-group row">
                    <label class="col-sm-2 col-form-label text-right">
                        TASK KEY
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="실행할 서버 작업(bean)을 선택합니다. REC_SIGNAL_RUN은 추천신호 계산 배치를 실행합니다.">?</span>
                    </label>
                    <div class="col-sm-4"><select id="edit_task_key" class="form-control form-control-sm"></select></div>
                    <label class="col-sm-2 col-form-label text-right">
                        ENABLE
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="Y: 스케줄/수동 실행 가능, N: 비활성화(실행 안됨)입니다.">?</span>
                    </label>
                    <div class="col-sm-4">
                        <select id="edit_enabled_yn" class="form-control form-control-sm"><option value="Y">Y</option><option value="N">N</option></select>
                    </div>
                </div>

                <div class="form-group row">
                    <label class="col-sm-2 col-form-label text-right">
                        TIMEZONE
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="스케줄 계산 기준 시간대입니다. 한국 운영은 Asia/Seoul을 사용하세요.">?</span>
                    </label>
                    <div class="col-sm-4"><input id="edit_timezone" class="form-control form-control-sm" value="Asia/Seoul"></div>
                    <label class="col-sm-2 col-form-label text-right">
                        MAX_RUNTIME_SEC
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="최대 실행 허용 시간(초)입니다. 초과 시 stale lock 정리 대상이 될 수 있습니다.">?</span>
                    </label>
                    <div class="col-sm-4"><input id="edit_max_runtime_sec" class="form-control form-control-sm" value="7200"></div>
                </div>

                <hr>
                <h6>
                    Schedule
                    <span class="batch-tip" data-toggle="tooltip" data-container="body" title="배치 실행 주기 설정입니다. CRON 또는 INTERVAL 중 하나를 사용합니다.">?</span>
                </h6>
                <div class="form-group row">
                    <label class="col-sm-2 col-form-label text-right">
                        TYPE
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="CRON: 지정 시간 실행, INTERVAL: N초 간격 반복 실행">?</span>
                    </label>
                    <div class="col-sm-2">
                        <select id="edit_schedule_type" class="form-control form-control-sm">
                            <option value="CRON">CRON</option>
                            <option value="INTERVAL">INTERVAL</option>
                        </select>
                    </div>
                    <label class="col-sm-2 col-form-label text-right">
                        CRON_EXPR
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="CRON 타입일 때 사용합니다. 예: 0 0 2 ? * TUE-SAT (화~토 02:00 실행)">?</span>
                    </label>
                    <div class="col-sm-6"><input id="edit_cron_expr" class="form-control form-control-sm" placeholder="0 0 2 ? * TUE-SAT"></div>
                </div>

                <div class="form-group row">
                    <label class="col-sm-2 col-form-label text-right">
                        INTERVAL_SEC
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="INTERVAL 타입일 때 실행 간격(초)입니다. 예: 1800=30분, 3600=1시간">?</span>
                    </label>
                    <div class="col-sm-2"><input id="edit_interval_sec" class="form-control form-control-sm" value="3600"></div>
                    <label class="col-sm-2 col-form-label text-right">
                        MISFIRE
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="SKIP: 놓친 실행 건너뜀, RUN_ONCE: 다음 tick에서 1회 보정 실행">?</span>
                    </label>
                    <div class="col-sm-2">
                        <select id="edit_misfire_policy" class="form-control form-control-sm">
                            <option value="SKIP">SKIP</option>
                            <option value="RUN_ONCE">RUN_ONCE</option>
                        </select>
                    </div>
                    <label class="col-sm-2 col-form-label text-right">
                        SCHEDULE ENABLE
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="Y: 스케줄러 자동 실행 사용, N: 자동 실행 중지(수동 실행만 가능)">?</span>
                    </label>
                    <div class="col-sm-2">
                        <select id="edit_schedule_enabled_yn" class="form-control form-control-sm"><option value="Y">Y</option><option value="N">N</option></select>
                    </div>
                </div>

                <div class="form-group row">
                    <label class="col-sm-2 col-form-label text-right">
                        LOG 보관(일)
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="TB_BATCH_EXEC_LOG 보관 일수입니다. 설정한 일수보다 오래된 로그는 스케줄러 tick 실행 시 자동 삭제됩니다. 0이면 자동 삭제하지 않습니다.">?</span>
                    </label>
                    <div class="col-sm-2"><input id="edit_log_retention_days" class="form-control form-control-sm" value="30" placeholder="0=삭제안함"></div>
                </div>

                <hr>
                <div class="d-flex align-items-center mb-2">
                    <h6 class="mb-0">
                        Params
                        <span class="batch-tip" data-toggle="tooltip" data-container="body" title="작업 파라미터입니다. 항목을 추가하고 KEY/VALUE/TYPE을 선택하세요. DATE 타입은 SYSDATE 버튼으로 실행 시점 날짜를 자동 적용할 수 있습니다.">?</span>
                    </h6>
                    <button type="button" id="btn_add_param" class="btn btn-xs btn-outline-secondary ml-2"><i class="fa fa-plus"></i> 파라미터 추가</button>
                </div>
                <div id="param_rows_container">
                    <div class="param-row-header row small text-muted mb-1 px-2">
                        <div class="col-sm-3">KEY</div>
                        <div class="col-sm-3">VALUE</div>
                        <div class="col-sm-2">TYPE</div>
                        <div class="col-sm-1 text-center">REQ</div>
                        <div class="col-sm-1 text-center">MASK</div>
                        <div class="col-sm-1"></div>
                    </div>
                </div>
                <small class="text-muted">`stkCd`는 비우면 전체 종목 실행, 값을 넣으면 해당 종목만 실행합니다.</small><br>
                <small class="text-muted text-info"><i class="fa fa-info-circle"></i> DATE 타입 파라미터에서 <strong>SYSDATE</strong> 활성화 시, 배치 실행 시점의 날짜가 자동 적용됩니다.</small>
            </div>
            <div class="modal-footer justify-content-between">
                <button type="button" class="btn btn-default btn-sm" data-dismiss="modal">Close</button>
                <div>
                    <button type="button" class="btn btn-danger btn-sm" id="btn_delete">Delete</button>
                    <button type="button" class="btn btn-primary btn-sm" id="btn_save">Save</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
window.batchAdminConfig = {
    taskCatalogUrl: "<c:url value='/stock/batchAdmin/taskCatalog.do' />",
    jobListUrl: "<c:url value='/stock/batchAdmin/jobList.do' />",
    jobDetailUrl: "<c:url value='/stock/batchAdmin/jobDetail.do' />",
    jobLogListUrl: "<c:url value='/stock/batchAdmin/jobLogList.do' />",
    jobCreateUrl: "<c:url value='/stock/batchAdmin/jobCreate.do' />",
    jobUpdateUrl: "<c:url value='/stock/batchAdmin/jobUpdate.do' />",
    jobDeleteUrl: "<c:url value='/stock/batchAdmin/jobDelete.do' />",
    jobRunNowUrl: "<c:url value='/stock/batchAdmin/jobRunNow.do' />",
    jobStopUrl: "<c:url value='/stock/batchAdmin/jobStop.do' />",
    jobLogItemFailListUrl: "<c:url value='/stock/batchAdmin/jobLogItemFailList.do' />"
};
</script>
<script src="<c:url value='/appone/jsp/stock/batchAdmin.js?v=20260403-2' />"></script>
