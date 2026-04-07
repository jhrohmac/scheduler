(function () {
    var config = window.batchAdminConfig || {};
    var DEFAULT_CRON_EXPR = "0 0 2 ? * TUE-SAT";
    var CRON_DAY_OPTIONS = [
        { value: "SUN", label: "일" },
        { value: "MON", label: "월" },
        { value: "TUE", label: "화" },
        { value: "WED", label: "수" },
        { value: "THU", label: "목" },
        { value: "FRI", label: "금" },
        { value: "SAT", label: "토" }
    ];
    var state = {
        tasks: [],
        jobs: [],
        selectedJobId: "",
        batchAdminTable: null,
        batchLogTable: null,
        batchItemFailTable: null
    };

    function initTooltips($scope) {
        var $targets;
        if (!$.fn || typeof $.fn.tooltip !== "function") {
            return;
        }
        $targets = ($scope && $scope.length ? $scope : $(document)).find('[data-toggle="tooltip"]');
        if (!$targets.length) {
            return;
        }
        $targets.tooltip("dispose");
        $targets.tooltip({
            trigger: "hover focus",
            container: "body"
        });
    }

    function val(obj, key) {
        if (!obj || !key) return "";
        var v = obj[key];
        if (v !== undefined && v !== null) return v;
        v = obj[key.toUpperCase()];
        if (v !== undefined && v !== null) return v;
        v = obj[key.toLowerCase()];
        if (v !== undefined && v !== null) return v;
        return "";
    }

    function intervalSecToMinutes(value) {
        var text = $.trim(value == null ? "" : String(value));
        var seconds;
        if (!text) {
            return "60";
        }
        seconds = Number(text);
        if (isNaN(seconds)) {
            return text;
        }
        return String(seconds / 60);
    }

    function intervalMinutesToSeconds(value) {
        var text = $.trim(value == null ? "" : String(value));
        var minutes;
        if (!text) {
            return "";
        }
        minutes = Number(text);
        if (isNaN(minutes)) {
            return text;
        }
        return String(Math.round(minutes * 60));
    }

    function pad2(value) {
        var n = parseInt(value, 10);
        if (isNaN(n)) return "00";
        return n < 10 ? "0" + n : String(n);
    }

    function findCronDayIndex(day) {
        var target = String(day || "").toUpperCase();
        for (var i = 0; i < CRON_DAY_OPTIONS.length; i++) {
            if (CRON_DAY_OPTIONS[i].value === target) {
                return i;
            }
        }
        return -1;
    }

    function sortCronDays(days) {
        var list = (days || []).slice(0);
        list.sort(function (a, b) {
            return findCronDayIndex(a) - findCronDayIndex(b);
        });
        return list;
    }

    function populateCronTimeOptions() {
        var hourHtml = "";
        var minuteHtml = "";
        var i;

        for (i = 0; i < 24; i++) {
            hourHtml += '<option value="' + pad2(i) + '">' + pad2(i) + "</option>";
        }
        for (i = 0; i < 60; i++) {
            minuteHtml += '<option value="' + pad2(i) + '">' + pad2(i) + "</option>";
        }

        $("#cron_simple_hour").html(hourHtml);
        $("#cron_simple_minute").html(minuteHtml);
    }

    function getCronEditorMode() {
        return $("#cron_schedule_panel").attr("data-cron-mode") || "simple";
    }

    function setCronEditorMode(mode) {
        var nextMode = mode === "advanced" ? "advanced" : "simple";
        var isSimple = nextMode === "simple";

        $("#cron_schedule_panel").attr("data-cron-mode", nextMode);
        $(".schedule-mode-switch [data-cron-mode]").removeClass("btn-primary active").addClass("btn-default");
        $('.schedule-mode-switch [data-cron-mode="' + nextMode + '"]').removeClass("btn-default").addClass("btn-primary active");
        $("#cron_simple_panel").toggle(isSimple);
        $("#cron_advanced_panel").toggle(!isSimple);

        if (isSimple) {
            updateCronPreview();
        } else {
            updateAdvancedCronHint();
        }
    }

    function selectedSimpleCronDays() {
        var days = [];
        $("#cron_simple_days_wrap input:checked").each(function () {
            days.push($(this).val());
        });
        return sortCronDays(days);
    }

    function setSimpleCronDays(days) {
        var sorted = sortCronDays(days);
        $("#cron_simple_days_wrap input").prop("checked", false);
        for (var i = 0; i < sorted.length; i++) {
            $('#cron_simple_days_wrap input[value="' + sorted[i] + '"]').prop("checked", true);
        }
    }

    function cronDayLabel(day) {
        var index = findCronDayIndex(day);
        return index > -1 ? CRON_DAY_OPTIONS[index].label : day;
    }

    function formatCronDayLabels(days) {
        var labels = [];
        var sorted = sortCronDays(days);
        for (var i = 0; i < sorted.length; i++) {
            labels.push(cronDayLabel(sorted[i]));
        }
        return labels.join(", ");
    }

    function updateCronSimpleDayVisibility() {
        $("#cron_simple_days_wrap").toggle($("#cron_simple_repeat").val() === "WEEKLY");
    }

    function buildSimpleCronExpr() {
        var repeatType = $("#cron_simple_repeat").val() || "DAILY";
        var hour = parseInt($("#cron_simple_hour").val(), 10);
        var minute = parseInt($("#cron_simple_minute").val(), 10);
        var days = selectedSimpleCronDays();

        if (isNaN(hour) || isNaN(minute)) {
            return "";
        }
        if (repeatType === "DAILY") {
            return "0 " + minute + " " + hour + " * * *";
        }
        if (repeatType === "WEEKDAY") {
            return "0 " + minute + " " + hour + " ? * MON-FRI";
        }
        if (!days.length) {
            return "";
        }
        return "0 " + minute + " " + hour + " ? * " + days.join(",");
    }

    function buildSimpleCronPreviewText() {
        var repeatType = $("#cron_simple_repeat").val() || "DAILY";
        var timezone = $.trim($("#edit_timezone").val()) || "Asia/Seoul";
        var hour = $("#cron_simple_hour").val() || "00";
        var minute = $("#cron_simple_minute").val() || "00";
        var timeText = pad2(hour) + ":" + pad2(minute);
        var days = selectedSimpleCronDays();

        if (repeatType === "DAILY") {
            return "매일 " + timeText + " (" + timezone + ")";
        }
        if (repeatType === "WEEKDAY") {
            return "매주 평일 " + timeText + " (" + timezone + ")";
        }
        if (!days.length) {
            return "요일을 선택하면 실행 일정을 미리 보여줍니다.";
        }
        return "매주 " + formatCronDayLabels(days) + " " + timeText + " (" + timezone + ")";
    }

    function parseCronDayToken(token) {
        var text = $.trim(String(token || "")).toUpperCase();
        var i;
        var parts;
        var start;
        var end;
        var days = [];

        if (!text) {
            return [];
        }
        if (text.indexOf(",") > -1) {
            parts = text.split(",");
            for (i = 0; i < parts.length; i++) {
                var subDays = parseCronDayToken(parts[i]);
                if (subDays == null) {
                    return null;
                }
                for (var j = 0; j < subDays.length; j++) {
                    if ($.inArray(subDays[j], days) === -1) {
                        days.push(subDays[j]);
                    }
                }
            }
            return sortCronDays(days);
        }
        if (text.indexOf("-") > -1) {
            parts = text.split("-");
            if (parts.length !== 2) {
                return null;
            }
            start = findCronDayIndex(parts[0]);
            end = findCronDayIndex(parts[1]);
            if (start < 0 || end < 0 || start > end) {
                return null;
            }
            for (i = start; i <= end; i++) {
                days.push(CRON_DAY_OPTIONS[i].value);
            }
            return days;
        }
        if (findCronDayIndex(text) < 0) {
            return null;
        }
        return [text];
    }

    function parseSimpleCronExpr(expr) {
        var text = $.trim(String(expr || "")).toUpperCase();
        var tokens;
        var minute;
        var hour;
        var days;

        if (!text) {
            return null;
        }
        tokens = text.split(/\s+/);
        if (tokens.length !== 6 || tokens[0] !== "0" || tokens[4] !== "*") {
            return null;
        }

        minute = parseInt(tokens[1], 10);
        hour = parseInt(tokens[2], 10);
        if (isNaN(minute) || isNaN(hour) || minute < 0 || minute > 59 || hour < 0 || hour > 23) {
            return null;
        }

        if ((tokens[3] === "*" || tokens[3] === "?") && tokens[5] === "*") {
            return { repeatType: "DAILY", hour: hour, minute: minute, days: [] };
        }
        if (tokens[3] !== "?" && tokens[3] !== "*") {
            return null;
        }
        if (tokens[5] === "MON-FRI") {
            return { repeatType: "WEEKDAY", hour: hour, minute: minute, days: ["MON", "TUE", "WED", "THU", "FRI"] };
        }

        days = parseCronDayToken(tokens[5]);
        if (!days || !days.length) {
            return null;
        }
        return { repeatType: "WEEKLY", hour: hour, minute: minute, days: days };
    }

    function applySimpleCronConfig(config) {
        if (!config) {
            return;
        }
        $("#cron_simple_repeat").val(config.repeatType || "DAILY");
        $("#cron_simple_hour").val(pad2(config.hour));
        $("#cron_simple_minute").val(pad2(config.minute));
        setSimpleCronDays(config.days || []);
        updateCronSimpleDayVisibility();
        updateCronPreview();
    }

    function updateAdvancedCronHint() {
        var expr = $.trim($("#edit_cron_expr").val());
        var parsed = parseSimpleCronExpr(expr);
        var message = "간편 설정으로 표현하기 어려운 식은 고급 CRON으로 그대로 저장됩니다.";

        if (!expr) {
            message = "예: 0 0 5 ? * SUN = 매주 일요일 05:00";
        } else if (parsed) {
            message = "현재 입력은 간편 설정으로도 표현할 수 있습니다. 필요하면 간편 설정 탭으로 전환하세요.";
        } else if (expr) {
            message = "현재 입력은 간편 설정으로 완전히 표현되지 않아 저장 시 그대로 사용됩니다.";
        }
        $("#cron_advanced_hint").text(message);
    }

    function updateCronPreview() {
        var expr = buildSimpleCronExpr();
        var previewExpr;
        var warning = "";

        updateCronSimpleDayVisibility();
        $("#cron_timezone_preview").text($.trim($("#edit_timezone").val()) || "Asia/Seoul");
        $("#cron_preview_text").text(buildSimpleCronPreviewText());

        if ($("#cron_simple_repeat").val() === "WEEKLY" && !selectedSimpleCronDays().length) {
            warning = "주간 반복은 최소 1개 요일을 선택해야 합니다.";
        }

        if (getCronEditorMode() === "simple" && expr) {
            $("#edit_cron_expr").val(expr);
        }
        previewExpr = getCronEditorMode() === "simple"
            ? (expr || "-")
            : ($.trim($("#edit_cron_expr").val()) || "-");
        $("#cron_preview_expr").text(previewExpr);

        if (warning) {
            $("#cron_simple_warning").text(warning).show();
        } else {
            $("#cron_simple_warning").hide();
        }
    }

    function syncCronEditorFromExpr(expr, preferSimple) {
        var parsed = parseSimpleCronExpr(expr);
        var defaultParsed = parseSimpleCronExpr(DEFAULT_CRON_EXPR);

        if (parsed) {
            applySimpleCronConfig(parsed);
            if (preferSimple !== false) {
                setCronEditorMode("simple");
            }
            updateAdvancedCronHint();
            return true;
        }

        if (!$.trim(expr) && defaultParsed) {
            applySimpleCronConfig(defaultParsed);
            $("#edit_cron_expr").val(DEFAULT_CRON_EXPR);
            if (preferSimple !== false) {
                setCronEditorMode("simple");
            }
            updateAdvancedCronHint();
            return true;
        }

        if (preferSimple !== false) {
            setCronEditorMode("advanced");
        }
        updateAdvancedCronHint();
        return false;
    }

    function updateIntervalPresetState() {
        var current = $.trim($("#edit_interval_sec").val());
        $(".btn-interval-preset").removeClass("btn-primary").addClass("btn-outline-secondary");
        $('.btn-interval-preset[data-minutes="' + current + '"]').removeClass("btn-outline-secondary").addClass("btn-primary");
    }

    function syncScheduleTypeUi() {
        var isCron = $("#edit_schedule_type").val() === "CRON";

        $("#cron_schedule_panel").toggle(isCron);
        $("#interval_schedule_panel").toggle(!isCron);
        $("#interval_field_wrap").toggleClass("schedule-disabled", isCron);
        $("#edit_interval_sec").prop("disabled", isCron);
        $(".btn-interval-preset").prop("disabled", isCron);
    }

    function refreshScheduleEditor(preferSimple) {
        syncScheduleTypeUi();
        updateIntervalPresetState();
        if ($("#edit_schedule_type").val() === "CRON") {
            if (!$.trim($("#edit_cron_expr").val())) {
                $("#edit_cron_expr").val(DEFAULT_CRON_EXPR);
            }
            syncCronEditorFromExpr($.trim($("#edit_cron_expr").val()), preferSimple);
            updateCronPreview();
        } else {
            updateAdvancedCronHint();
        }
    }

    function initScheduleEditor() {
        populateCronTimeOptions();
        $("#edit_cron_expr").val(DEFAULT_CRON_EXPR);
        applySimpleCronConfig(parseSimpleCronExpr(DEFAULT_CRON_EXPR));
        setCronEditorMode("simple");
        updateIntervalPresetState();
        syncScheduleTypeUi();
    }

    function escapeHtml(value) {
        return $("<div/>").text(value == null ? "" : String(value)).html();
    }

    function normalizeJobRow(row) {
        return {
            job_id: val(row, "job_id"),
            job_name: val(row, "job_name"),
            task_key: val(row, "task_key"),
            enabled_yn: val(row, "enabled_yn"),
            running_yn: val(row, "running_yn"),
            next_run_at: val(row, "next_run_at"),
            last_start_at: val(row, "last_start_at"),
            last_end_at: val(row, "last_end_at"),
            last_result_code: val(row, "last_result_code"),
            last_result_msg: val(row, "last_result_msg")
        };
    }

    function toBatchAdminRow(target) {
        var table = state.batchAdminTable;
        var $row;
        var row;

        if (!table) {
            return null;
        }

        $row = $(target).closest("tr");
        row = table.row($row);
        if (!row.data() && $row.prev().length) {
            row = table.row($row.prev());
        }
        return row.data() || null;
    }

    function normalizeLogRow(row) {
        return {
            exec_id: val(row, "exec_id"),
            base_dt: val(row, "base_dt"),
            status: val(row, "status"),
            total_cnt: val(row, "total_cnt"),
            success_cnt: val(row, "success_cnt"),
            fail_cnt: val(row, "fail_cnt"),
            start_at: val(row, "start_at"),
            end_at: val(row, "end_at"),
            error_msg: val(row, "error_msg")
        };
    }

    function ensureBatchAdminTable() {
        if (state.batchAdminTable) {
            return state.batchAdminTable;
        }

        state.batchAdminTable = $("#tblBatchAdmin").DataTable({
            data: [],
            pageLength: 10,
            lengthChange: false,
            searching: false,
            paging: true,
            info: true,
            autoWidth: false,
            responsive: true,
            processing: false,
            ordering: false,
            columns: [
                { data: "job_id", defaultContent: "" },
                { data: "job_name", defaultContent: "" },
                { data: "task_key", defaultContent: "" },
                { data: "enabled_yn", defaultContent: "" },
                {
                    data: "running_yn",
                    defaultContent: "",
                    render: function (data) {
                        return String(data || "N") === "Y"
                            ? '<span class="badge badge-info">Y</span>'
                            : '<span class="badge badge-secondary">N</span>';
                    }
                },
                { data: "next_run_at", defaultContent: "" },
                { data: "last_start_at", defaultContent: "" },
                { data: "last_end_at", defaultContent: "" },
                { data: "last_result_code", defaultContent: "" },
                {
                    data: "last_result_msg",
                    defaultContent: "",
                    render: function (data, type) {
                        var fullText = data || "";
                        var shortText = fullText;
                        if (type !== "display") {
                            return fullText;
                        }
                        if (shortText.length > 60) {
                            shortText = shortText.substring(0, 60) + "...";
                        }
                        return '<span title="' + escapeHtml(fullText) + '">' + escapeHtml(shortText) + "</span>";
                    }
                },
                {
                    data: null,
                    orderable: false,
                    searchable: false,
                    render: function (data, type, row) {
                        var jobId = escapeHtml(val(row, "job_id"));
                        var html = "";

                        if (type !== "display") {
                            return jobId;
                        }

                        html += '<button class="btn btn-xs btn-primary btn-edit" data-job-id="' + jobId + '">설정</button> ';
                        html += '<button class="btn btn-xs btn-success btn-run" data-job-id="' + jobId + '">실행</button> ';
                        html += '<button class="btn btn-xs btn-warning btn-stop" data-job-id="' + jobId + '">정지</button> ';
                        html += '<button class="btn btn-xs btn-danger btn-del" data-job-id="' + jobId + '">삭제</button>';
                        return html;
                    }
                }
            ],
            drawCallback: function () {
                initTooltips($(this.api().table().container()));
            }
        });

        return state.batchAdminTable;
    }

    function ensureBatchLogTable() {
        if (state.batchLogTable) {
            return state.batchLogTable;
        }

        state.batchLogTable = $("#tblBatchLog").DataTable({
            data: [],
            pageLength: 10,
            lengthChange: false,
            searching: false,
            paging: true,
            info: true,
            autoWidth: false,
            responsive: true,
            processing: false,
            ordering: false,
            columns: [
                { data: "exec_id", defaultContent: "" },
                { data: "base_dt", defaultContent: "" },
                { data: "status", defaultContent: "" },
                { data: "total_cnt", defaultContent: "0" },
                { data: "success_cnt", defaultContent: "0" },
                { data: "fail_cnt", defaultContent: "0" },
                { data: "start_at", defaultContent: "" },
                { data: "end_at", defaultContent: "" },
                {
                    data: "error_msg",
                    defaultContent: "",
                    render: function (data, type) {
                        var fullText = data || "";
                        var shortText = fullText;
                        if (type !== "display") {
                            return fullText;
                        }
                        if (shortText.length > 80) {
                            shortText = shortText.substring(0, 80) + "...";
                        }
                        return '<span title="' + escapeHtml(fullText) + '">' + escapeHtml(shortText) + "</span>";
                    }
                }
            ],
            drawCallback: function () {
                initTooltips($(this.api().table().container()));
            }
        });

        return state.batchLogTable;
    }

    function ensureBatchItemFailTable() {
        if (state.batchItemFailTable) {
            return state.batchItemFailTable;
        }

        state.batchItemFailTable = $("#tblBatchItemFailLog").DataTable({
            data: [],
            pageLength: 20,
            lengthChange: false,
            searching: false,
            paging: true,
            info: true,
            autoWidth: false,
            responsive: true,
            processing: false,
            ordering: false,
            columns: [
                { data: "stk_cd", defaultContent: "" },
                { data: "stk_nm", defaultContent: "" },
                { data: "mkt_cd", defaultContent: "" },
                {
                    data: "error_msg",
                    defaultContent: "",
                    render: function (data, type) {
                        var fullText = data || "";
                        if (type !== "display") return fullText;
                        var shortText = fullText.length > 120 ? fullText.substring(0, 120) + "..." : fullText;
                        return '<span title="' + escapeHtml(fullText) + '">' + escapeHtml(shortText) + "</span>";
                    }
                },
                { data: "created_at", defaultContent: "" }
            ]
        });

        return state.batchItemFailTable;
    }

    function loadItemFailLog(execId, jobId) {
        if (!execId && !jobId) return;
        var table = ensureBatchItemFailTable();
        var payload = {};
        if (execId) {
            payload.exec_id = execId;
            $("#lbl_item_fail_exec_id").text("EXEC: " + execId);
        }
        if (jobId) payload.job_id = jobId;

        api(config.jobLogItemFailListUrl || "/stock/batchAdmin/jobLogItemFailList.do", payload, function (res) {
            var list = rowsOf(res);
            var normalized = [];
            for (var i = 0; i < list.length; i++) {
                var r = list[i];
                normalized.push({
                    stk_cd: val(r, "stk_cd"),
                    stk_nm: val(r, "stk_nm"),
                    mkt_cd: val(r, "mkt_cd"),
                    error_msg: val(r, "error_msg"),
                    created_at: val(r, "created_at")
                });
            }
            table.clear();
            if (normalized.length) {
                table.rows.add(normalized);
                $("#div_item_fail_log").show();
            } else {
                $("#div_item_fail_log").hide();
            }
            table.draw(false);
        });
    }

    function api(url, data, onSuccess, onError) {
        $.ajax({
            url: url,
            method: "POST",
            data: data || {},
            dataType: "json"
        }).done(function (res) {
            var code = res && res.system_code != null ? String(res.system_code) : "";
            if (code === "0000" || code === "S") {
                onSuccess(res);
            } else {
                alert((res && res.system_msg) || "요청 실패");
                if (onError) onError(res);
            }
        }).fail(function (xhr) {
            alert("요청 실패: " + xhr.status);
            if (onError) onError(xhr);
        });
    }

    function rowsOf(res) {
        if (!res) return [];
        if (Array.isArray(res.data)) return res.data;
        if (res.data && Array.isArray(res.data.data)) return res.data.data;
        return [];
    }

    function singleOf(res) {
        if (!res) return {};
        if (res.singleData && typeof res.singleData === "object") return res.singleData;
        if (res.data && res.data.singleData && typeof res.data.singleData === "object") {
            return res.data.singleData;
        }
        return {};
    }

    function loadTasks(callback) {
        api(config.taskCatalogUrl || "/stock/batchAdmin/taskCatalog.do", {}, function (res) {
            var list = rowsOf(res);
            state.tasks = list;

            var html = '<option value="">ALL</option>';
            for (var i = 0; i < list.length; i++) {
                var taskKey = val(list[i], "task_key");
                if (!taskKey) continue;
                html += '<option value="' + taskKey + '">' + taskKey + "</option>";
            }
            $("#in_task_key").html(html);
            $("#edit_task_key").html(html.replace('value="">ALL', 'value="">선택'));

            if (callback) callback();
        }, function () {
            if (callback) callback();
        });
    }

    function renderJobs(list) {
        var table = ensureBatchAdminTable();
        var normalized = [];
        var i;

        list = list || [];
        for (i = 0; i < list.length; i++) {
            normalized.push(normalizeJobRow(list[i]));
        }

        state.jobs = normalized;
        table.clear();
        if (state.jobs.length) {
            table.rows.add(state.jobs);
        }
        table.draw(false);
    }

    function renderLogs(list) {
        var table = ensureBatchLogTable();
        var normalized = [];
        var i;

        list = list || [];
        for (i = 0; i < list.length; i++) {
            normalized.push(normalizeLogRow(list[i]));
        }

        table.clear();
        if (normalized.length) {
            table.rows.add(normalized);
        }
        table.draw(false);
    }

    function loadJobs() {
        api(config.jobListUrl || "/stock/batchAdmin/jobList.do", {
            in_job_id: $("#in_job_id").val(),
            in_task_key: $("#in_task_key").val(),
            in_enabled_yn: $("#in_enabled_yn").val(),
            in_running_yn: $("#in_running_yn").val()
        }, function (res) {
            renderJobs(rowsOf(res));
        });
    }

    function loadLogs(jobId) {
        if (!jobId) return;
        state.selectedJobId = jobId;
        var payload = { job_id: jobId };
        for (var i = 0; i < state.jobs.length; i++) {
            var row = state.jobs[i];
            if (String(val(row, "job_id")) === String(jobId)) {
                payload.task_key = val(row, "task_key");
                break;
            }
        }
        // 실패 종목 상세 패널 초기화
        $("#div_item_fail_log").hide();
        api(config.jobLogListUrl || "/stock/batchAdmin/jobLogList.do", payload, function (res) {
            renderLogs(rowsOf(res));
        });
    }

    function todayStr() {
        var d = new Date();
        var y = d.getFullYear();
        var m = String(d.getMonth() + 1);
        var dd = String(d.getDate());
        if (m.length < 2) m = "0" + m;
        if (dd.length < 2) dd = "0" + dd;
        return y + "-" + m + "-" + dd;
    }

    function buildParamRow(param) {
        param = param || {};
        var paramKey = param.paramKey || "";
        var paramValue = param.paramValue || "";
        var paramType = param.paramType || "STRING";
        var requiredYn = param.requiredYn || "N";
        var maskedYn = param.maskedYn || "N";
        var isSysdate = (paramType === "DATE" && paramValue === "SYSDATE");
        var isDate = (paramType === "DATE");

        var typeOpts = ["STRING", "NUMBER", "BOOLEAN", "DATE"].map(function (t) {
            return '<option value="' + t + '"' + (paramType === t ? " selected" : "") + ">" + t + "</option>";
        }).join("");
        var reqOpts = ["N", "Y"].map(function (v) {
            return '<option value="' + v + '"' + (requiredYn === v ? " selected" : "") + ">" + v + "</option>";
        }).join("");
        var maskOpts = ["N", "Y"].map(function (v) {
            return '<option value="' + v + '"' + (maskedYn === v ? " selected" : "") + ">" + v + "</option>";
        }).join("");

        var displayValue = isSysdate ? "" : escapeHtml(paramValue);
        var valuePlaceholder = isSysdate ? ("SYSDATE → " + todayStr()) : "paramValue";
        var valueDisabled = isSysdate ? " disabled" : "";
        var sysdateClass = isSysdate ? "btn-info" : "btn-outline-secondary";
        var sysdateDisplay = isDate ? "" : ' style="display:none"';

        return '<div class="param-row row align-items-center mb-1 px-2">' +
            '<div class="col-sm-3"><input type="text" class="form-control form-control-sm param-key" value="' + escapeHtml(paramKey) + '" placeholder="paramKey"></div>' +
            '<div class="col-sm-3 d-flex align-items-center">' +
                '<input type="text" class="form-control form-control-sm param-value" value="' + displayValue + '" placeholder="' + valuePlaceholder + '"' + valueDisabled + ' data-sysdate="' + (isSysdate ? "Y" : "N") + '">' +
                '<button type="button" class="btn btn-xs ' + sysdateClass + ' btn-sysdate ml-1 flex-shrink-0"' + sysdateDisplay + ' title="실행 시점 날짜 자동 적용">S</button>' +
            '</div>' +
            '<div class="col-sm-2"><select class="form-control form-control-sm param-type">' + typeOpts + '</select></div>' +
            '<div class="col-sm-1"><select class="form-control form-control-sm param-required">' + reqOpts + '</select></div>' +
            '<div class="col-sm-1"><select class="form-control form-control-sm param-masked">' + maskOpts + '</select></div>' +
            '<div class="col-sm-1 text-center"><button type="button" class="btn btn-xs btn-outline-danger btn-remove-param">×</button></div>' +
        '</div>';
    }

    function renderParamRows(paramsArr) {
        var $container = $("#param_rows_container");
        var $header = $container.find(".param-row-header").detach();
        $container.empty();
        $container.append($header);
        paramsArr = paramsArr || [];
        for (var i = 0; i < paramsArr.length; i++) {
            $container.append(buildParamRow(paramsArr[i]));
        }
    }

    function collectParamsJson() {
        var taskKey = $("#edit_task_key").val();
        var isRefreshTask = (taskKey === STK_MASTER_REFRESH_TASK);
        var rows = [];

        /* STK_MASTER_REFRESH: 전용 패널 값을 우선 수집 */
        if (isRefreshTask) {
            rows = collectStkMasterRefreshParams();
        }

        $("#param_rows_container .param-row").each(function () {
            var $row = $(this);
            var paramKey = $row.find(".param-key").val().trim();
            /* STK_MASTER_REFRESH 관리 키는 일반 파라미터 목록에서 제외 */
            if (isRefreshTask && isStkMasterRefreshManagedKey(paramKey)) {
                return;
            }
            var paramType = $row.find(".param-type").val();
            var $valInput = $row.find(".param-value");
            var isSysdate = $valInput.attr("data-sysdate") === "Y";
            var paramValue;
            if (paramType === "DATE" && isSysdate) {
                paramValue = "SYSDATE";
            } else {
                paramValue = $valInput.val();
            }
            var obj = { paramKey: paramKey, paramValue: paramValue, paramType: paramType };
            var req = $row.find(".param-required").val();
            var masked = $row.find(".param-masked").val();
            if (req) obj.requiredYn = req;
            if (masked) obj.maskedYn = masked;
            rows.push(obj);
        });
        return JSON.stringify(rows, null, 2);
    }

    var STK_MASTER_REFRESH_TASK = "STK_MASTER_REFRESH";

    function defaultParamsByTask(taskKey) {
        if (taskKey === "REC_SIGNAL_RUN") {
            return [
                { paramKey: "marketGroup", paramValue: "KR", paramType: "STRING" },
                { paramKey: "retryOnly", paramValue: "N", paramType: "BOOLEAN" },
                { paramKey: "days", paramValue: "400", paramType: "NUMBER" },
                { paramKey: "requestIntervalMs", paramValue: "1000", paramType: "NUMBER" },
                { paramKey: "stkCd", paramValue: "", paramType: "STRING" }
            ];
        }
        if (taskKey === "REC_PICK_DAILY") {
            return [
                { paramKey: "baseDt", paramValue: "SYSDATE", paramType: "DATE", requiredYn: "N", maskedYn: "N" }
            ];
        }
        /* STK_MASTER_REFRESH: 전용 패널에서 관리 — 일반 파라미터 목록 없음 */
        return [];
    }

    /* ── STK_MASTER_REFRESH 전용 패널 ─────────────────────────────── */

    function setStkBtnGroupValue(groupId, hiddenId, value) {
        $("#" + groupId + " button").removeClass("btn-primary active").addClass("btn-default");
        var $btn = $('#' + groupId + ' button[data-value="' + value + '"]');
        if ($btn.length) {
            $btn.removeClass("btn-default").addClass("btn-primary active");
        }
        $("#" + hiddenId).val(value);
    }

    function syncStkMasterRefreshPanel(taskKey, paramsArr) {
        var isRefreshTask = (taskKey === STK_MASTER_REFRESH_TASK);
        $("#stk_master_refresh_panel").toggle(isRefreshTask);
        if (!isRefreshTask) {
            return;
        }

        /* paramsArr에서 marketGroup / stockType 값을 읽어 패널에 반영 */
        var marketGroup = "ALL";
        var stockType   = "ALL";
        if (paramsArr && paramsArr.length) {
            for (var i = 0; i < paramsArr.length; i++) {
                var k = paramsArr[i].paramKey;
                var v = paramsArr[i].paramValue;
                if (k === "marketGroup" && v) { marketGroup = v; }
                if (k === "stockType"   && v) { stockType   = v; }
            }
        }
        setStkBtnGroupValue("stk_market_group_btn", "stk_market_group_val", marketGroup);
        setStkBtnGroupValue("stk_stock_type_btn",   "stk_stock_type_val",   stockType);
    }

    /** STK_MASTER_REFRESH 전용 params (패널 값 → 파라미터 배열) */
    function collectStkMasterRefreshParams() {
        return [
            { paramKey: "marketGroup", paramValue: $("#stk_market_group_val").val() || "ALL", paramType: "STRING" },
            { paramKey: "stockType",   paramValue: $("#stk_stock_type_val").val()   || "ALL", paramType: "STRING" }
        ];
    }

    /** 일반 파라미터 수집 시 STK_MASTER_REFRESH 전용 키는 제외 */
    function isStkMasterRefreshManagedKey(key) {
        return (key === "marketGroup" || key === "stockType");
    }

    function openCreate() {
        $("#edit_mode").val("I");
        $("#edit_job_id").val("").prop("readonly", false);
        $("#edit_job_name").val("");
        $("#edit_task_key").val("");
        $("#edit_enabled_yn").val("Y");
        $("#edit_timezone").val("Asia/Seoul");
        $("#edit_max_runtime_sec").val("7200");
        $("#edit_schedule_type").val("CRON");
        $("#edit_cron_expr").val(DEFAULT_CRON_EXPR);
        $("#edit_interval_sec").val("60");
        $("#edit_misfire_policy").val("SKIP");
        $("#edit_schedule_enabled_yn").val("Y");
        $("#edit_log_retention_days").val("30");
        renderParamRows([]);
        syncStkMasterRefreshPanel("", []);
        refreshScheduleEditor(true);
        $("#modal_batch_admin_edit").modal("show");
    }

    function openEdit(jobId) {
        api(config.jobDetailUrl || "/stock/batchAdmin/jobDetail.do", { job_id: jobId }, function (res) {
            var detail = singleOf(res);
            var job = detail.job || {};
            var schedule = detail.schedule || {};
            var params = detail.params || [];

            $("#edit_mode").val("U");
            $("#edit_job_id").val(val(job, "job_id") || "").prop("readonly", true);
            $("#edit_job_name").val(val(job, "job_name") || "");
            $("#edit_task_key").val(val(job, "task_key") || "");
            $("#edit_enabled_yn").val(val(job, "enabled_yn") || "Y");
            $("#edit_timezone").val(val(job, "timezone") || "Asia/Seoul");
            $("#edit_max_runtime_sec").val(val(job, "max_runtime_sec") || "7200");

            $("#edit_schedule_type").val(val(schedule, "schedule_type") || "CRON");
            $("#edit_cron_expr").val(val(schedule, "cron_expr") || "");
            $("#edit_interval_sec").val(intervalSecToMinutes(val(schedule, "interval_sec")));
            $("#edit_misfire_policy").val(val(schedule, "misfire_policy") || "SKIP");
            $("#edit_schedule_enabled_yn").val(val(schedule, "enabled_yn") || "Y");
            $("#edit_log_retention_days").val(val(schedule, "log_retention_days") || "30");

            var taskKey = val(job, "task_key") || "";
            var isRefreshTask = (taskKey === STK_MASTER_REFRESH_TASK);
            var paramsJson = [];
            for (var i = 0; i < params.length; i++) {
                var pKey = val(params[i], "param_key");
                /* STK_MASTER_REFRESH 관리 키는 일반 파라미터 목록에서 제외 */
                if (isRefreshTask && isStkMasterRefreshManagedKey(pKey)) {
                    continue;
                }
                paramsJson.push({
                    paramKey: pKey,
                    paramValue: val(params[i], "param_value"),
                    paramType: val(params[i], "param_type"),
                    requiredYn: val(params[i], "required_yn"),
                    maskedYn: val(params[i], "masked_yn")
                });
            }
            renderParamRows(paramsJson);
            syncStkMasterRefreshPanel(taskKey, params.map(function (p) {
                return { paramKey: val(p, "param_key"), paramValue: val(p, "param_value") };
            }));

            refreshScheduleEditor(true);
            $("#modal_batch_admin_edit").modal("show");
        });
    }

    function saveJob() {
        var mode = $("#edit_mode").val();
        var jobId = $.trim($("#edit_job_id").val()).toUpperCase();

        if (!jobId) {
            alert("JOB ID를 입력하세요.");
            return;
        }

        if ($("#edit_schedule_type").val() === "CRON") {
            if (getCronEditorMode() === "simple") {
                var simpleCronExpr = buildSimpleCronExpr();
                if (!simpleCronExpr) {
                    alert("주간 반복은 최소 1개 요일을 선택하세요.");
                    return;
                }
                $("#edit_cron_expr").val(simpleCronExpr);
            }
            if (!$.trim($("#edit_cron_expr").val())) {
                alert("CRON 실행 규칙을 입력하세요.");
                return;
            }
        } else {
            var intervalSeconds = parseInt(intervalMinutesToSeconds($("#edit_interval_sec").val()), 10);
            if (!intervalSeconds || intervalSeconds <= 0) {
                alert("실행 간격(분)을 입력하세요.");
                return;
            }
        }

        var scheduleJson = {
            scheduleType: $("#edit_schedule_type").val(),
            cronExpr: $.trim($("#edit_cron_expr").val()),
            intervalSec: intervalMinutesToSeconds($("#edit_interval_sec").val()),
            misfirePolicy: $("#edit_misfire_policy").val(),
            enabledYn: $("#edit_schedule_enabled_yn").val(),
            logRetentionDays: parseInt($("#edit_log_retention_days").val(), 10) || 0
        };

        var paramsText = collectParamsJson();

        var payload = {
            job_id: jobId,
            job_name: $("#edit_job_name").val(),
            task_key: $("#edit_task_key").val(),
            enabled_yn: $("#edit_enabled_yn").val(),
            timezone: $("#edit_timezone").val(),
            max_runtime_sec: $("#edit_max_runtime_sec").val(),
            allow_manual_run_yn: "Y",
            schedule_json: JSON.stringify(scheduleJson),
            params_json: paramsText
        };

        var url = mode === "I"
            ? (config.jobCreateUrl || "/stock/batchAdmin/jobCreate.do")
            : (config.jobUpdateUrl || "/stock/batchAdmin/jobUpdate.do");
        api(url, payload, function () {
            $("#modal_batch_admin_edit").modal("hide");
            loadJobs();
            if (state.selectedJobId) loadLogs(state.selectedJobId);
        });
    }

    function deleteJob(jobId) {
        if (!confirm(jobId + " 삭제하시겠습니까?")) return;
        api(config.jobDeleteUrl || "/stock/batchAdmin/jobDelete.do", { job_id: jobId }, function () {
            loadJobs();
            if (state.selectedJobId === jobId) {
                state.selectedJobId = "";
                renderLogs([]);
            }
            $("#modal_batch_admin_edit").modal("hide");
        });
    }

    function runJob(jobId) {
        api(config.jobRunNowUrl || "/stock/batchAdmin/jobRunNow.do", { job_id: jobId }, function () {
            loadJobs();
            loadLogs(jobId);
        });
    }

    function stopJob(jobId) {
        api(config.jobStopUrl || "/stock/batchAdmin/jobStop.do", { job_id: jobId }, function () {
            loadJobs();
        });
    }

    $(document).ready(function () {
        ensureBatchAdminTable();
        ensureBatchLogTable();
        ensureBatchItemFailTable();
        initScheduleEditor();
        loadTasks();
        loadJobs();
        initTooltips($(document));

        $("#btn_search, #btn_reload").on("click", function () {
            loadJobs();
        });

        $("#btn_add").on("click", function () {
            openCreate();
        });

        $("#edit_task_key").on("change", function () {
            var taskKey = $(this).val();
            if (!$("#param_rows_container .param-row").length) {
                renderParamRows(defaultParamsByTask(taskKey));
            }
            syncStkMasterRefreshPanel(taskKey, []);
        });

        $("#edit_schedule_type").on("change", function () {
            refreshScheduleEditor(true);
        });

        $(".schedule-mode-switch [data-cron-mode]").on("click", function () {
            var mode = $(this).data("cronMode");
            var currentExpr = $.trim($("#edit_cron_expr").val());
            var shouldWarnOnSimple = false;

            if (mode === "simple") {
                if (!syncCronEditorFromExpr(currentExpr, false) && currentExpr) {
                    shouldWarnOnSimple = true;
                }
            }
            setCronEditorMode(mode);
            if (shouldWarnOnSimple) {
                $("#cron_simple_warning").text("현재 고급 CRON은 간편 설정으로 표현되지 않아, 저장 시 현재 간편 설정 값으로 바뀝니다.").show();
            }
        });

        $("#cron_simple_repeat, #cron_simple_hour, #cron_simple_minute").on("change", function () {
            updateCronPreview();
        });

        $("#cron_simple_days_wrap").on("change", "input", function () {
            updateCronPreview();
        });

        $("#edit_cron_expr").on("input change", function () {
            syncCronEditorFromExpr($.trim($(this).val()), false);
            updateAdvancedCronHint();
        });

        $("#edit_timezone").on("input change", function () {
            updateCronPreview();
        });

        $("#edit_interval_sec").on("input change", function () {
            updateIntervalPresetState();
        });

        $(".btn-interval-preset").on("click", function () {
            $("#edit_interval_sec").val(String($(this).data("minutes")));
            updateIntervalPresetState();
        });

        /* STK_MASTER_REFRESH 전용 패널 — 버튼 그룹 토글 */
        $("#stk_master_refresh_panel").on("click", ".btn-group button", function () {
            var $btn    = $(this);
            var $group  = $btn.closest(".btn-group");
            var $hidden = $group.next("input[type=hidden]");
            $group.find("button").removeClass("btn-primary active").addClass("btn-default");
            $btn.removeClass("btn-default").addClass("btn-primary active");
            $hidden.val($btn.data("value"));
        });

        $("#btn_add_param").on("click", function () {
            $("#param_rows_container").append(buildParamRow({}));
        });

        $("#param_rows_container").on("click", ".btn-remove-param", function () {
            $(this).closest(".param-row").remove();
        });

        $("#param_rows_container").on("change", ".param-type", function () {
            var $row = $(this).closest(".param-row");
            var isDate = $(this).val() === "DATE";
            var $btn = $row.find(".btn-sysdate");
            if (isDate) {
                $btn.show();
            } else {
                $btn.hide();
                $btn.removeClass("btn-info").addClass("btn-outline-secondary");
                var $valInput = $row.find(".param-value");
                $valInput.attr("data-sysdate", "N").prop("disabled", false).attr("placeholder", "paramValue");
            }
        });

        $("#param_rows_container").on("click", ".btn-sysdate", function () {
            var $row = $(this).closest(".param-row");
            var $valInput = $row.find(".param-value");
            var isSysdate = $valInput.attr("data-sysdate") === "Y";
            if (isSysdate) {
                $valInput.attr("data-sysdate", "N").prop("disabled", false).val("").attr("placeholder", "paramValue");
                $(this).removeClass("btn-info").addClass("btn-outline-secondary");
            } else {
                $valInput.attr("data-sysdate", "Y").prop("disabled", true).val("").attr("placeholder", "SYSDATE → " + todayStr());
                $(this).removeClass("btn-outline-secondary").addClass("btn-info");
            }
        });

        $("#btn_save").on("click", function () {
            saveJob();
        });

        $("#btn_delete").on("click", function () {
            var mode = $("#edit_mode").val();
            if (mode !== "U") {
                alert("등록 전 삭제는 불가합니다.");
                return;
            }
            deleteJob($("#edit_job_id").val());
        });

        $("#tblBatchAdmin tbody").on("click", ".btn-edit", function () {
            var row = toBatchAdminRow(this);
            openEdit(row ? val(row, "job_id") : $(this).data("jobId"));
        });

        $("#tblBatchAdmin tbody").on("click", ".btn-run", function () {
            var row = toBatchAdminRow(this);
            runJob(row ? val(row, "job_id") : $(this).data("jobId"));
        });

        $("#tblBatchAdmin tbody").on("click", ".btn-stop", function () {
            var row = toBatchAdminRow(this);
            stopJob(row ? val(row, "job_id") : $(this).data("jobId"));
        });

        $("#tblBatchAdmin tbody").on("click", ".btn-del", function () {
            var row = toBatchAdminRow(this);
            deleteJob(row ? val(row, "job_id") : $(this).data("jobId"));
        });

        $("#tblBatchAdmin tbody").on("click", "tr", function () {
            var row = toBatchAdminRow(this);
            var jobId = row ? val(row, "job_id") : "";
            if (jobId) loadLogs(jobId);
        });

        // 실행 로그 행 클릭 시 FAIL 종목 상세 표시
        $("#tblBatchLog tbody").on("click", "tr", function () {
            var table = state.batchLogTable;
            if (!table) return;
            var $row = $(this).closest("tr");
            var rowData = table.row($row).data();
            if (!rowData) return;
            var status = val(rowData, "status");
            var execId = val(rowData, "exec_id");
            if (status === "FAIL" || status === "ERROR") {
                loadItemFailLog(execId, state.selectedJobId);
            } else {
                $("#div_item_fail_log").hide();
            }
        });

        $("#modal_batch_admin_edit").on("shown.bs.modal", function () {
            refreshScheduleEditor(false);
            initTooltips($(this));
        });
    });
})();
