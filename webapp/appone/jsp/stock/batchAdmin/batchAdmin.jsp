<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/appone/jsp/stock/batchAdmin/css/batchAdmin.css?v=20260417-1">

<div class="container-fluid" id="div_stock_batch_admin">

    <!-- 조회 조건 -->
    <div class="card card-primary card-outline">
        <div class="card-header">
            <h3 class="card-title"><i class="fas fa-search mr-1"></i>조회 조건</h3>
            <div class="card-tools">
                <button type="button" class="btn btn-tool" data-card-widget="collapse"><i class="fas fa-minus"></i></button>
            </div>
        </div>
        <div class="card-body p-2">
            <div class="form-group search_row">
                <label class="col-sm-1 col-form-label text-right">JOB ID</label>
                <div class="col-sm-2"><input type="text" id="in_job_id" class="form-control form-control-sm" placeholder="JOB ID"></div>

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
                    <button type="button" id="btn_search" class="btn btn-primary btn-sm"><i class="fa fa-search"></i> 조회</button>
                    <button type="button" id="btn_reload" class="btn btn-secondary btn-sm"><i class="fa fa-sync"></i></button>
                </div>
            </div>
        </div>
    </div>

    <!-- JOB 목록 -->
    <div class="card card-info">
        <div class="card-header">
            <h3 class="card-title"><i class="fas fa-list mr-1"></i>배치 JOB 목록</h3>
        </div>
        <div class="card-body table-responsive p-2">
            <div class="mb-2">
                <button type="button" class="btn btn-sm btn-warning" id="btn_task_def_open">
                    <i class="fas fa-puzzle-piece"></i> TASK 등록
                </button>
                <button type="button" class="btn btn-sm btn-primary ml-1" id="btn_add">
                    <i class="fas fa-plus"></i> 배치 추가
                </button>
                <button type="button" class="btn btn-sm btn-secondary float-right" id="btn_reload2">
                    <i class="fas fa-sync-alt"></i> 새로고침
                </button>
            </div>
            <table class="table table-hover table-sm table-bordered w-100" id="tblBatchAdmin">
                <thead class="thead-light">
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
                    <th>MESSAGE</th>
                    <th>ACTION</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>

    <!-- 실행 로그 -->
    <div class="card card-secondary" id="div_batch_exec_log" style="display:none;">
        <div class="card-header">
            <h3 class="card-title">
                <i class="fas fa-history mr-1"></i>실행 로그
                <span id="lbl_exec_job_id" class="badge badge-info ml-2"></span>
            </h3>
            <div class="card-tools">
                <button type="button" class="btn btn-tool" data-card-widget="collapse"><i class="fas fa-minus"></i></button>
            </div>
        </div>
        <div class="card-body table-responsive p-2">
            <table class="table table-sm table-bordered w-100" id="tblBatchLog">
                <thead class="thead-light">
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

    <!-- 실패 종목 상세 -->
    <div class="card batch-fail-card" id="div_item_fail_log" style="display:none;">
        <div class="card-header batch-fail-header">
            <h3 class="card-title">
                <i class="fas fa-exclamation-triangle mr-1"></i>실패 종목 상세
                <small id="lbl_item_fail_exec_id" class="text-light ml-2"></small>
            </h3>
            <div class="card-tools">
                <button type="button" class="btn btn-tool text-white" onclick="$('#div_item_fail_log').hide()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
        <div class="card-body table-responsive p-2">
            <table class="table table-sm table-bordered w-100" id="tblBatchItemFailLog">
                <thead class="thead-light">
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


<!-- ═══════════════════════════════════════════
     TASK KEY 관리 모달
═══════════════════════════════════════════ -->
<div class="modal fade" id="modal_task_def" tabindex="-1" data-keyboard="true" data-backdrop="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-warning">
                <h4 class="modal-title text-white"><i class="fas fa-puzzle-piece mr-1"></i>TASK KEY 관리</h4>
                <button type="button" class="close text-white" data-dismiss="modal"><span>&times;</span></button>
            </div>
            <div class="modal-body">
                <div class="card card-outline card-warning mb-3">
                    <div class="card-header py-2">
                        <h6 class="card-title mb-0"><i class="fas fa-plus-circle mr-1"></i>신규 TASK KEY 등록</h6>
                    </div>
                    <div class="card-body p-2">
                        <div class="form-group row mb-2">
                            <label class="col-sm-2 col-form-label col-form-label-sm">TASK KEY <span class="text-danger">*</span></label>
                            <div class="col-sm-4">
                                <input type="text" id="in_new_task_key" class="form-control form-control-sm text-uppercase"
                                       placeholder="예) MY_BATCH_TASK"
                                       oninput="this.value=this.value.toUpperCase();">
                            </div>
                            <label class="col-sm-2 col-form-label col-form-label-sm">설명</label>
                            <div class="col-sm-4">
                                <input type="text" id="in_new_task_desc" class="form-control form-control-sm" placeholder="TASK 설명">
                            </div>
                        </div>
                        <div class="form-group row mb-2">
                            <label class="col-sm-2 col-form-label col-form-label-sm">BEAN NAME <span class="text-danger">*</span></label>
                            <div class="col-sm-10">
                                <input type="text" id="in_new_bean_name" class="form-control form-control-sm"
                                       placeholder="예) myBatchTask  (Spring XML &lt;bean id=&quot;...&quot;&gt; 값)">
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-12">
                                <button type="button" class="btn btn-sm btn-warning" id="btn_insert_task_def">
                                    <i class="fas fa-plus mr-1"></i>등록
                                </button>
                                <small class="text-muted ml-2">
                                    <i class="fas fa-info-circle"></i>
                                    TASK KEY는 Java 구현체(<code>BatchTask.getTaskKey()</code>) 반환값과 일치해야 실행됩니다.
                                </small>
                            </div>
                        </div>
                    </div>
                </div>
                <table class="table table-sm table-bordered table-hover" id="tblTaskDef">
                    <thead class="thead-light">
                        <tr>
                            <th>TASK KEY</th>
                            <th>설명</th>
                            <th style="width:70px;">사용</th>
                            <th style="width:80px;">등록자</th>
                            <th style="width:145px;">등록일</th>
                            <th style="width:55px;">삭제</th>
                        </tr>
                    </thead>
                    <tbody id="tblTaskDefBody">
                        <tr><td colspan="6" class="text-center text-muted">로딩 중...</td></tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    <i class="fas fa-times mr-1"></i>닫기
                </button>
            </div>
        </div>
    </div>
</div>


<!-- ═══════════════════════════════════════════
     Batch 설정 모달
═══════════════════════════════════════════ -->
<div class="modal fade" id="modal_batch_admin_edit" tabindex="-1" data-keyboard="true" data-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-primary">
                <h4 class="modal-title text-white"><i class="fas fa-cog mr-1"></i>Batch 설정</h4>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="edit_mode" value="I">

                <!-- JOB ID | JOB NAME -->
                <div class="form-group row mb-2">
                    <label class="col-sm-2 col-form-label col-form-label-sm text-right">
                        JOB ID <span class="batch-tip" data-toggle="tooltip" data-container="body" title="배치 작업의 고유 식별자입니다. 영문 대문자/숫자/언더스코어 형태를 권장합니다.">?</span>
                    </label>
                    <div class="col-sm-4">
                        <div class="input-group input-group-sm">
                            <input id="edit_job_id" class="form-control form-control-sm text-uppercase"
                                   placeholder="예) MY_BATCH_JOB"
                                   oninput="this.value=this.value.toUpperCase()">
                            <div class="input-group-append" id="div_job_id_check_btn">
                                <button type="button" class="btn btn-outline-secondary" id="btn_job_id_check"
                                        data-toggle="tooltip" title="JOB ID 중복 체크">
                                    <i class="fas fa-check"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                    <label class="col-sm-2 col-form-label col-form-label-sm text-right">
                        JOB NAME <span class="batch-tip" data-toggle="tooltip" data-container="body" title="화면에 표시되는 배치 이름입니다.">?</span>
                    </label>
                    <div class="col-sm-4">
                        <input id="edit_job_name" class="form-control form-control-sm" placeholder="배치 명칭">
                    </div>
                </div>

                <!-- TASK KEY | ENABLE -->
                <div class="form-group row mb-2">
                    <label class="col-sm-2 col-form-label col-form-label-sm text-right">
                        TASK KEY <span class="batch-tip" data-toggle="tooltip" data-container="body" title="실행할 서버 작업(bean)을 선택합니다. REC_SIGNAL_RUN은 추천신호 계산 배치를 실행합니다.">?</span>
                    </label>
                    <div class="col-sm-4"><select id="edit_task_key" class="form-control form-control-sm"></select></div>
                    <label class="col-sm-2 col-form-label col-form-label-sm text-right">
                        ENABLE <span class="batch-tip" data-toggle="tooltip" data-container="body" title="Y: 스케줄/수동 실행 가능, N: 비활성화(실행 안됨)입니다.">?</span>
                    </label>
                    <div class="col-sm-4">
                        <select id="edit_enabled_yn" class="form-control form-control-sm"><option value="Y">Y</option><option value="N">N</option></select>
                    </div>
                </div>

                <!-- TIMEZONE | MAX_RUNTIME_SEC -->
                <div class="form-group row mb-2">
                    <label class="col-sm-2 col-form-label col-form-label-sm text-right">TIMEZONE</label>
                    <div class="col-sm-4"><input id="edit_timezone" class="form-control form-control-sm" value="Asia/Seoul"></div>
                    <label class="col-sm-2 col-form-label col-form-label-sm text-right">
                        MAX_RUNTIME_SEC <span class="batch-tip" data-toggle="tooltip" data-container="body" title="최대 실행 허용 시간(초)입니다. 초과 시 stale lock 정리 대상이 될 수 있습니다.">?</span>
                    </label>
                    <div class="col-sm-4"><input id="edit_max_runtime_sec" class="form-control form-control-sm" value="3600"></div>
                </div>

                <!-- ── SCHEDULE 카드 ── -->
                <div class="card card-outline card-primary mb-2">
                    <div class="card-header py-1">
                        <h6 class="card-title mb-0">
                            SCHEDULE
                            <span class="batch-tip" data-toggle="tooltip" data-container="body" title="배치 실행 주기 설정입니다. CRON 또는 INTERVAL 중 하나를 사용합니다.">?</span>
                        </h6>
                    </div>
                    <div class="card-body p-2">

                        <!-- TYPE -->
                        <div class="form-group row mb-2">
                            <label class="col-sm-2 col-form-label col-form-label-sm">
                                TYPE <span class="batch-tip" data-toggle="tooltip" data-container="body" title="CRON: 지정 시간 실행, INTERVAL: N분 간격 반복 실행">?</span>
                            </label>
                            <div class="col-sm-3">
                                <select id="edit_schedule_type" class="form-control form-control-sm">
                                    <option value="CRON">정해진 시간</option>
                                    <option value="INTERVAL">간격 실행</option>
                                </select>
                            </div>
                        </div>

                        <!-- CRON 편집기 -->
                        <div id="cron_schedule_panel">
                            <div class="btn-group btn-group-sm schedule-mode-switch mb-2" role="group" aria-label="CRON editor mode">
                                <button type="button" class="btn btn-primary active" data-cron-mode="simple">간편 설정</button>
                                <button type="button" class="btn btn-default" data-cron-mode="advanced">고급 CRON</button>
                            </div>

                            <div id="cron_simple_panel" class="schedule-builder-card">
                                <div class="form-row align-items-end">
                                    <div class="col-sm-4 mb-2">
                                        <label class="schedule-field-label">
                                            반복 <span class="batch-tip" data-toggle="tooltip" data-container="body" title="매일, 평일, 주간 반복 중 하나를 선택합니다.">?</span>
                                        </label>
                                        <select id="cron_simple_repeat" class="form-control form-control-sm">
                                            <option value="DAILY">매일</option>
                                            <option value="WEEKDAY">평일</option>
                                            <option value="WEEKLY">주간</option>
                                        </select>
                                    </div>
                                    <div class="col-sm-4 mb-2">
                                        <label class="schedule-field-label">
                                            실행 시간 <span class="batch-tip" data-toggle="tooltip" data-container="body" title="실행할 시각을 24시간 기준으로 선택합니다.">?</span>
                                        </label>
                                        <div class="d-flex align-items-center">
                                            <select id="cron_simple_hour" class="form-control form-control-sm schedule-time-select"></select>
                                            <span class="schedule-time-separator">:</span>
                                            <select id="cron_simple_minute" class="form-control form-control-sm schedule-time-select"></select>
                                        </div>
                                    </div>
                                    <div class="col-sm-4 mb-2">
                                        <label class="schedule-field-label">시간대</label>
                                        <div class="schedule-readonly-pill" id="cron_timezone_preview">Asia/Seoul</div>
                                    </div>
                                </div>

                                <div id="cron_simple_days_wrap" class="mb-2" style="display:none;">
                                    <label class="schedule-field-label">
                                        요일 <span class="batch-tip" data-toggle="tooltip" data-container="body" title="주간 반복일 때 실행할 요일을 하나 이상 선택하세요.">?</span>
                                    </label>
                                    <div class="schedule-day-group">
                                        <label class="schedule-day-chip"><input type="checkbox" value="SUN"><span>일</span></label>
                                        <label class="schedule-day-chip"><input type="checkbox" value="MON"><span>월</span></label>
                                        <label class="schedule-day-chip"><input type="checkbox" value="TUE"><span>화</span></label>
                                        <label class="schedule-day-chip"><input type="checkbox" value="WED"><span>수</span></label>
                                        <label class="schedule-day-chip"><input type="checkbox" value="THU"><span>목</span></label>
                                        <label class="schedule-day-chip"><input type="checkbox" value="FRI"><span>금</span></label>
                                        <label class="schedule-day-chip"><input type="checkbox" value="SAT"><span>토</span></label>
                                    </div>
                                </div>

                                <div class="cron-preview-box">
                                    <div class="schedule-field-label mb-1">미리보기</div>
                                    <div id="cron_preview_text" class="cron-preview-text">매주 화, 수, 목, 금, 토 02:00 (Asia/Seoul)</div>
                                    <div class="cron-preview-expr" id="cron_preview_expr_wrap">저장될 CRON_EXPR: <span id="cron_preview_expr">0 0 2 ? * TUE-SAT</span></div>
                                    <div id="cron_simple_warning" class="cron-preview-warning" style="display:none;"></div>
                                </div>
                            </div>

                            <div id="cron_advanced_panel" class="schedule-builder-card" style="display:none;">
                                <label class="schedule-field-label">
                                    CRON_EXPR <span class="batch-tip" data-toggle="tooltip" data-container="body" title="간편 설정으로 표현하기 어려운 식은 직접 입력합니다.">?</span>
                                </label>
                                <input id="edit_cron_expr" class="form-control form-control-sm" placeholder="0 0 2 ? * TUE-SAT" autocomplete="off">
                                <small class="text-muted d-block mt-2">예: 0 0 5 ? * SUN = 매주 일요일 05:00, 0 30 8 ? * MON-FRI = 평일 08:30</small>
                                <small id="cron_advanced_hint" class="text-muted d-block mt-1">간편 설정으로 표현하기 어려운 식은 고급 CRON으로 그대로 저장됩니다.</small>
                            </div>
                        </div>

                        <!-- INTERVAL 힌트 -->
                        <div id="interval_schedule_panel" class="schedule-type-hint" style="display:none;">
                            INTERVAL 타입에서는 분 단위 실행 간격만 설정하면 됩니다. 정해진 시간 실행이 필요하면 TYPE을 정해진 시간으로 변경하세요.
                        </div>

                        <!-- INTERVAL_MIN (INTERVAL 타입일 때만 표시) -->
                        <div id="row_interval_min" style="display:none;" class="mt-2">
                            <div class="form-group row mb-0">
                                <label class="col-sm-2 col-form-label col-form-label-sm">
                                    INTERVAL_MIN <span class="batch-tip" data-toggle="tooltip" data-container="body" title="실행 간격(분)입니다. 예: 30=30분, 60=1시간">?</span>
                                </label>
                                <div class="col-sm-4">
                                    <div id="interval_field_wrap">
                                        <div class="input-group input-group-sm">
                                            <input id="edit_interval_sec" class="form-control form-control-sm" value="60">
                                            <div class="input-group-append"><span class="input-group-text">분</span></div>
                                        </div>
                                        <div class="interval-quick-group mt-1">
                                            <button type="button" class="btn btn-xs btn-outline-secondary btn-interval-preset" data-minutes="10">10분</button>
                                            <button type="button" class="btn btn-xs btn-outline-secondary btn-interval-preset" data-minutes="30">30분</button>
                                            <button type="button" class="btn btn-xs btn-outline-secondary btn-interval-preset" data-minutes="60">60분</button>
                                            <button type="button" class="btn btn-xs btn-outline-secondary btn-interval-preset" data-minutes="120">120분</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div><!-- /SCHEDULE 카드 -->

                <!-- MISFIRE | SCHEDULE ENABLE | LOG 보관(일) -->
                <div class="row mb-2">
                    <div class="col-md-4">
                        <div class="form-group row align-items-center mb-0">
                            <label class="col-5 col-form-label col-form-label-sm">
                                MISFIRE <span class="batch-tip" data-toggle="tooltip" data-container="body" title="SKIP: 놓친 실행 건너뜀, RUN_ONCE: 다음 tick에서 1회 보정 실행">?</span>
                            </label>
                            <div class="col-7">
                                <select id="edit_misfire_policy" class="form-control form-control-sm">
                                    <option value="SKIP">SKIP</option>
                                    <option value="RUN_ONCE">RUN_ONCE</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="form-group row align-items-center mb-0">
                            <label class="col-6 col-form-label col-form-label-sm">
                                SCHEDULE ENABLE <span class="batch-tip" data-toggle="tooltip" data-container="body" title="Y: 스케줄러 자동 실행 사용, N: 자동 실행 중지(수동 실행만 가능)">?</span>
                            </label>
                            <div class="col-6">
                                <select id="edit_schedule_enabled_yn" class="form-control form-control-sm"><option value="Y">Y</option><option value="N">N</option></select>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="form-group row align-items-center mb-0">
                            <label class="col-5 col-form-label col-form-label-sm">
                                LOG 보관(일) <span class="batch-tip" data-toggle="tooltip" data-container="body" title="TB_BATCH_EXEC_LOG 보관 일수입니다. 0이면 자동 삭제하지 않습니다.">?</span>
                            </label>
                            <div class="col-7">
                                <input id="edit_log_retention_days" class="form-control form-control-sm" value="30" placeholder="0=삭제안함">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- REC_SIGNAL_RUN 전용 설정 패널 -->
                <div id="rec_signal_run_panel" style="display:none;">
                    <hr class="mt-1 mb-2">
                    <h6 class="mb-2">시그널 종목 필터</h6>
                    <div class="form-group row mb-2">
                        <label class="col-sm-2 col-form-label col-form-label-sm text-right">시장</label>
                        <div class="col-sm-10 d-flex align-items-center">
                            <div class="btn-group btn-group-sm" id="rec_listing_mkt_btn" role="group">
                                <button type="button" class="btn btn-primary active" data-value="ALL">전체</button>
                                <button type="button" class="btn btn-default" data-value="KOSPI">코스피</button>
                                <button type="button" class="btn btn-default" data-value="KOSDAQ">코스닥</button>
                                <button type="button" class="btn btn-default" data-value="NAS">NASDAQ</button>
                                <button type="button" class="btn btn-default" data-value="NYS">NYSE</button>
                            </div>
                            <input type="hidden" id="rec_listing_mkt_val" value="ALL">
                        </div>
                    </div>
                    <div class="form-group row mb-2">
                        <label class="col-sm-2 col-form-label col-form-label-sm text-right">지수</label>
                        <div class="col-sm-10 d-flex align-items-center">
                            <div class="btn-group btn-group-sm" id="rec_index_filter_btn" role="group">
                                <button type="button" class="btn btn-primary active" data-value="ALL">전체</button>
                                <button type="button" class="btn btn-default" data-value="KOSPI200">KOSPI200</button>
                                <button type="button" class="btn btn-default" data-value="KOSDAQ150">KOSDAQ150</button>
                            </div>
                            <input type="hidden" id="rec_index_filter_val" value="ALL">
                        </div>
                    </div>
                    <div class="form-group row mb-2">
                        <label class="col-sm-2 col-form-label col-form-label-sm text-right">유형</label>
                        <div class="col-sm-10 d-flex align-items-center">
                            <div class="btn-group btn-group-sm" id="rec_stk_type_btn" role="group">
                                <button type="button" class="btn btn-primary active" data-value="ALL">전체</button>
                                <button type="button" class="btn btn-default" data-value="STOCK">주식</button>
                                <button type="button" class="btn btn-default" data-value="ETF">ETF</button>
                                <button type="button" class="btn btn-default" data-value="ETN">ETN</button>
                                <button type="button" class="btn btn-default" data-value="ELW">ELW</button>
                            </div>
                            <input type="hidden" id="rec_stk_type_val" value="ALL">
                        </div>
                    </div>
                </div>

                <!-- STK_MASTER_REFRESH 전용 설정 패널 -->
                <div id="stk_master_refresh_panel" style="display:none;">
                    <hr class="mt-1 mb-2">
                    <h6 class="mb-2">종목 마스터 갱신 설정</h6>
                    <div class="form-group row mb-2">
                        <label class="col-sm-2 col-form-label col-form-label-sm text-right">시장 구분</label>
                        <div class="col-sm-10 d-flex align-items-center">
                            <div class="btn-group btn-group-sm" id="stk_market_group_btn" role="group">
                                <button type="button" class="btn btn-primary active" data-value="ALL">전체 (KR+US)</button>
                                <button type="button" class="btn btn-default" data-value="KR">국내 (KR)</button>
                                <button type="button" class="btn btn-default" data-value="US">해외 (US)</button>
                            </div>
                            <input type="hidden" id="stk_market_group_val" value="ALL">
                        </div>
                    </div>
                    <div class="form-group row mb-2">
                        <label class="col-sm-2 col-form-label col-form-label-sm text-right">종목 구분</label>
                        <div class="col-sm-10 d-flex align-items-center">
                            <div class="btn-group btn-group-sm" id="stk_stock_type_btn" role="group">
                                <button type="button" class="btn btn-primary active" data-value="ALL">전체</button>
                                <button type="button" class="btn btn-default" data-value="STOCK">일반종목</button>
                                <button type="button" class="btn btn-default" data-value="ETF">ETF</button>
                            </div>
                            <input type="hidden" id="stk_stock_type_val" value="ALL">
                        </div>
                    </div>
                </div>

                <hr class="mt-1 mb-2">
                <!-- 파라미터 요약 + 팝업 버튼 -->
                <div class="d-flex align-items-center mb-2">
                    <h6 class="mb-0">
                        Params <span class="batch-tip" data-toggle="tooltip" data-container="body" title="작업 파라미터입니다. 파라미터 설정 버튼을 눌러 편집하세요.">?</span>
                    </h6>
                    <span id="lbl_param_count" class="badge badge-secondary ml-2">0개</span>
                    <button type="button" id="btn_open_params" class="btn btn-sm btn-outline-info ml-2">
                        <i class="fas fa-sliders-h mr-1"></i>파라미터 설정
                    </button>
                </div>
                <div id="param_summary_preview" class="param-summary-box">
                    파라미터가 없습니다.
                </div>

            </div>
            <div class="modal-footer justify-content-between">
                <button type="button" class="btn btn-default btn-sm" data-dismiss="modal">Close</button>
                <div>
                    <button type="button" class="btn btn-danger btn-sm" id="btn_delete">Delete</button>
                    <button type="button" class="btn btn-success btn-sm d-none" id="btn_save_and_run">저장 후 실행</button>
                    <button type="button" class="btn btn-primary btn-sm" id="btn_save">Save</button>
                </div>
            </div>
        </div>
    </div>
</div>


<!-- ═══════════════════════════════════════════
     파라미터 설정 모달 (서브 팝업)
═══════════════════════════════════════════ -->
<div class="modal fade" id="modal_batch_params" tabindex="-1" data-keyboard="true" data-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-info">
                <h4 class="modal-title text-white"><i class="fas fa-sliders-h mr-1"></i>파라미터 설정</h4>
                <button type="button" class="close text-white" id="btn_params_close"><span>&times;</span></button>
            </div>
            <div class="modal-body">
                <div class="d-flex align-items-center mb-2">
                    <small class="text-muted mr-auto">
                        <i class="fa fa-info-circle text-info"></i>
                        KEY/VALUE/TYPE을 설정하세요.
                        DATE 타입은 <strong>S</strong> 버튼으로 실행 시점 날짜를 자동 적용할 수 있습니다.
                    </small>
                    <button type="button" id="btn_add_param" class="btn btn-xs btn-outline-secondary ml-2">
                        <i class="fa fa-plus"></i> 파라미터 추가
                    </button>
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
                <small class="text-muted d-block mt-2">
                    <code>stkCd</code>는 비우면 전체 종목 실행, 값을 넣으면 해당 종목만 실행합니다.
                </small>
                <small class="text-muted text-info d-block">
                    <i class="fa fa-info-circle"></i>
                    DATE 타입 파라미터에서 <strong>SYSDATE</strong> 활성화 시, 배치 실행 시점의 날짜가 자동 적용됩니다.
                </small>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary btn-sm" id="btn_params_done">
                    <i class="fas fa-check mr-1"></i>확인
                </button>
            </div>
        </div>
    </div>
</div>


<script>
window.batchAdminConfig = {
    taskCatalogUrl:       "<c:url value='/stock/batchAdmin/taskCatalog.do' />",
    taskDefListUrl:       "<c:url value='/stock/batchAdmin/taskDefList.do' />",
    taskDefInsertUrl:     "<c:url value='/stock/batchAdmin/taskDefInsert.do' />",
    taskDefDeleteUrl:     "<c:url value='/stock/batchAdmin/taskDefDelete.do' />",
    jobListUrl:           "<c:url value='/stock/batchAdmin/jobList.do' />",
    jobDetailUrl:         "<c:url value='/stock/batchAdmin/jobDetail.do' />",
    jobLogListUrl:        "<c:url value='/stock/batchAdmin/jobLogList.do' />",
    jobCreateUrl:         "<c:url value='/stock/batchAdmin/jobCreate.do' />",
    jobUpdateUrl:         "<c:url value='/stock/batchAdmin/jobUpdate.do' />",
    jobDeleteUrl:         "<c:url value='/stock/batchAdmin/jobDelete.do' />",
    jobRunNowUrl:         "<c:url value='/stock/batchAdmin/jobRunNow.do' />",
    jobStopUrl:           "<c:url value='/stock/batchAdmin/jobStop.do' />",
    jobLogItemFailListUrl: "<c:url value='/stock/batchAdmin/jobLogItemFailList.do' />"
};
</script>
<script src="${pageContext.request.contextPath}/appone/jsp/stock/batchAdmin/js/batchAdmin.js?v=20260417-1"></script>
