(function () {
    var config = window.batchAdminConfig || {};
    var state = {
        tasks: [],
        jobs: [],
        selectedJobId: "",
        batchAdminTable: null,
        batchLogTable: null
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
        var rows = [];
        $("#param_rows_container .param-row").each(function () {
            var $row = $(this);
            var paramKey = $row.find(".param-key").val().trim();
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
        return [];
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
        $("#edit_cron_expr").val("0 0 2 ? * TUE-SAT");
        $("#edit_interval_sec").val("3600");
        $("#edit_misfire_policy").val("SKIP");
        $("#edit_schedule_enabled_yn").val("Y");
        renderParamRows([]);
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
            $("#edit_interval_sec").val(val(schedule, "interval_sec") || "3600");
            $("#edit_misfire_policy").val(val(schedule, "misfire_policy") || "SKIP");
            $("#edit_schedule_enabled_yn").val(val(schedule, "enabled_yn") || "Y");

            var paramsJson = [];
            for (var i = 0; i < params.length; i++) {
                paramsJson.push({
                    paramKey: val(params[i], "param_key"),
                    paramValue: val(params[i], "param_value"),
                    paramType: val(params[i], "param_type"),
                    requiredYn: val(params[i], "required_yn"),
                    maskedYn: val(params[i], "masked_yn")
                });
            }
            renderParamRows(paramsJson);

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

        var scheduleJson = {
            scheduleType: $("#edit_schedule_type").val(),
            cronExpr: $("#edit_cron_expr").val(),
            intervalSec: $("#edit_interval_sec").val(),
            misfirePolicy: $("#edit_misfire_policy").val(),
            enabledYn: $("#edit_schedule_enabled_yn").val()
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
            if (!$("#param_rows_container .param-row").length) {
                renderParamRows(defaultParamsByTask($(this).val()));
            }
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

        $("#modal_batch_admin_edit").on("shown.bs.modal", function () {
            initTooltips($(this));
        });
    });
})();
