package com.scheduler.stock.batchadmin.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.support.CronSequenceGenerator;

import com.scheduler.stock.batchadmin.dao.StockBatchAdminDao;
import com.scheduler.stock.batchadmin.task.StockBatchTask;

public class StockBatchAdminService implements ApplicationContextAware {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long HEARTBEAT_INTERVAL_MS = 30000L;

    private static final Map<String, Thread> RUNNING_THREADS = new ConcurrentHashMap<String, Thread>();
    private static final Map<String, AtomicBoolean> STOP_FLAGS = new ConcurrentHashMap<String, AtomicBoolean>();

    private StockBatchAdminDao stockBatchAdminDao;
    private ApplicationContext applicationContext;

    public void setStockBatchAdminDao(StockBatchAdminDao stockBatchAdminDao) {
        this.stockBatchAdminDao = stockBatchAdminDao;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public List<HashMap<String, Object>> selectTaskCatalog(HashMap<String, String> map) throws Exception {
        return stockBatchAdminDao.selectTaskCatalog(map);
    }

    public List<HashMap<String, Object>> selectJobList(HashMap<String, String> map) throws Exception {
        return stockBatchAdminDao.selectJobList(map);
    }

    public HashMap<String, Object> selectJobDetail(HashMap<String, String> map) throws Exception {
        String jobId = nvl(map.get("job_id"), "");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);

        HashMap<String, Object> out = new HashMap<String, Object>();
        out.put("job", stockBatchAdminDao.selectJobOne(q));
        out.put("schedule", stockBatchAdminDao.selectJobSchedule(q));
        out.put("params", stockBatchAdminDao.selectJobParams(q));
        return out;
    }

    public List<HashMap<String, Object>> selectJobLogList(HashMap<String, String> map) throws Exception {
        return stockBatchAdminDao.selectJobLogList(enrichJobLogQuery(map));
    }

    public List<HashMap<String, Object>> selectJobLogItemFailList(HashMap<String, String> map) throws Exception {
        return stockBatchAdminDao.selectJobLogItemFailList(enrichJobLogQuery(map));
    }

    public Map<String, Object> createJob(HashMap<String, String> map) throws Exception {
        String jobId = upperId(map.get("job_id"));
        String jobName = nvl(map.get("job_name"), "");
        String taskKey = upperId(map.get("task_key"));

        if (jobId.isEmpty()) throw new IllegalArgumentException("job_id is required");
        if (jobName.isEmpty()) throw new IllegalArgumentException("job_name is required");
        if (taskKey.isEmpty()) throw new IllegalArgumentException("task_key is required");

        HashMap<String, String> up = new HashMap<String, String>();
        up.put("job_id", jobId);
        up.put("job_name", jobName);
        up.put("task_key", taskKey);
        up.put("enabled_yn", yn(map.get("enabled_yn"), "Y"));
        up.put("timezone", nvl(map.get("timezone"), "Asia/Seoul"));
        up.put("max_runtime_sec", String.valueOf(parsePositiveInt(map.get("max_runtime_sec"), 7200)));
        up.put("allow_manual_run_yn", yn(map.get("allow_manual_run_yn"), "Y"));
        up.put("description", nvl(map.get("description"), ""));

        stockBatchAdminDao.insertJobDef(up);
        stockBatchAdminDao.ensureJobRuntime(up);

        HashMap<String, String> sch = parseScheduleMap(map.get("schedule_json"), up.get("timezone"));
        sch.put("job_id", jobId);
        stockBatchAdminDao.upsertJobSchedule(sch);

        replaceJobParams(jobId, map.get("params_json"));

        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("affected", 1);
        out.put("job_id", jobId);
        out.put("message", "CREATED");
        return out;
    }

    public Map<String, Object> updateJob(HashMap<String, String> map) throws Exception {
        String jobId = upperId(map.get("job_id"));
        if (jobId.isEmpty()) throw new IllegalArgumentException("job_id is required");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);

        HashMap<String, Object> current = stockBatchAdminDao.selectJobOne(q);
        if (current == null) {
            throw new IllegalArgumentException("JOB NOT FOUND");
        }

        String jobName = nvl(map.get("job_name"), mapVal(current, "job_name"));
        String taskKey = upperId(nvl(map.get("task_key"), mapVal(current, "task_key")));
        String timezone = nvl(map.get("timezone"), mapVal(current, "timezone"));

        HashMap<String, String> up = new HashMap<String, String>();
        up.put("job_id", jobId);
        up.put("job_name", jobName);
        up.put("task_key", taskKey);
        up.put("enabled_yn", yn(nvl(map.get("enabled_yn"), mapVal(current, "enabled_yn")), "Y"));
        up.put("timezone", nvl(timezone, "Asia/Seoul"));
        up.put("max_runtime_sec", String.valueOf(parsePositiveInt(nvl(map.get("max_runtime_sec"), mapVal(current, "max_runtime_sec")), 7200)));
        up.put("allow_manual_run_yn", yn(nvl(map.get("allow_manual_run_yn"), mapVal(current, "allow_manual_run_yn")), "Y"));
        up.put("description", nvl(map.get("description"), mapVal(current, "description")));

        stockBatchAdminDao.updateJobDef(up);
        stockBatchAdminDao.ensureJobRuntime(up);

        if (!isBlank(map.get("schedule_json"))) {
            HashMap<String, String> sch = parseScheduleMap(map.get("schedule_json"), up.get("timezone"));
            sch.put("job_id", jobId);
            stockBatchAdminDao.upsertJobSchedule(sch);
        }

        if (map.containsKey("params_json")) {
            replaceJobParams(jobId, map.get("params_json"));
        }

        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("affected", 1);
        out.put("job_id", jobId);
        out.put("message", "UPDATED");
        return out;
    }

    public Map<String, Object> deleteJob(HashMap<String, String> map) throws Exception {
        String jobId = upperId(map.get("job_id"));
        if (jobId.isEmpty()) throw new IllegalArgumentException("job_id is required");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);
        HashMap<String, Object> job = stockBatchAdminDao.selectJobOne(q);
        if (job == null) {
            Map<String, Object> out = new LinkedHashMap<String, Object>();
            out.put("affected", 0);
            out.put("job_id", jobId);
            out.put("message", "JOB NOT FOUND");
            return out;
        }

        if ("Y".equalsIgnoreCase(mapVal(job, "running_yn"))) {
            throw new IllegalStateException("RUNNING=Y JOB CANNOT DELETE");
        }

        int deleted = stockBatchAdminDao.deleteJobDef(q);
        STOP_FLAGS.remove(jobId);
        RUNNING_THREADS.remove(jobId);

        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("affected", deleted);
        out.put("job_id", jobId);
        out.put("message", deleted > 0 ? "DELETED" : "NOT DELETED");
        return out;
    }

    public Map<String, Object> runJobNow(HashMap<String, String> map) throws Exception {
        String jobId = upperId(map.get("job_id"));
        if (jobId.isEmpty()) throw new IllegalArgumentException("job_id is required");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);

        HashMap<String, Object> job = stockBatchAdminDao.selectJobOne(q);
        if (job == null) {
            return out("ERROR", "JOB NOT FOUND", null);
        }

        if (!"Y".equalsIgnoreCase(mapVal(job, "allow_manual_run_yn"))) {
            return out("ERROR", "ALLOW_MANUAL_RUN_YN=N", null);
        }

        boolean queued = triggerJob(job, false, "MANUAL");
        if (!queued) {
            return out("SKIP", "RUNNING_YN=Y", null);
        }
        return out("QUEUED", "RUN REQUESTED", null);
    }

    public Map<String, Object> stopJob(HashMap<String, String> map) throws Exception {
        String jobId = upperId(map.get("job_id"));
        if (jobId.isEmpty()) throw new IllegalArgumentException("job_id is required");

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);

        stockBatchAdminDao.requestStop(q);

        AtomicBoolean stopFlag = STOP_FLAGS.get(jobId);
        if (stopFlag == null) {
            stopFlag = new AtomicBoolean(true);
            STOP_FLAGS.put(jobId, stopFlag);
        } else {
            stopFlag.set(true);
        }

        Thread t = RUNNING_THREADS.get(jobId);
        if (t != null) {
            t.interrupt();
        }

        return out("SUCCESS", "STOP REQUESTED", null);
    }

    public Map<String, Object> schedulerTick() throws Exception {
        HashMap<String, String> empty = new HashMap<String, String>();

        List<String> staleJobIds = stockBatchAdminDao.selectStaleJobIds(empty);
        abortLocalStaleJobs(staleJobIds);

        int recoveredLogCnt = normalizeUpdateCount(stockBatchAdminDao.clearStaleBatchExecLogs(empty));
        int releasedRuntimeCnt = normalizeUpdateCount(stockBatchAdminDao.clearStaleRuntimeLocks(empty));

        List<HashMap<String, Object>> dueJobs = stockBatchAdminDao.selectDueJobs(empty);
        int triggered = 0;
        int skipped = 0;

        for (HashMap<String, Object> job : dueJobs) {
            boolean queued = triggerJob(job, true, "SCHEDULER");
            if (queued) {
                triggered++;
            } else {
                skipped++;
            }
        }

        Map<String, Object> meta = new LinkedHashMap<String, Object>();
        meta.put("triggered", triggered);
        meta.put("skipped", skipped);
        meta.put("due", dueJobs == null ? 0 : dueJobs.size());
        meta.put("recoveredLogCnt", recoveredLogCnt);
        meta.put("releasedRuntimeCnt", releasedRuntimeCnt);
        return out("SUCCESS", "TICK", meta);
    }

    private boolean triggerJob(HashMap<String, Object> job,
                               boolean schedulerRun,
                               String triggerType) throws Exception {
        if (job == null) return false;

        final String jobId = upperId(mapVal(job, "job_id"));
        if (jobId.isEmpty()) return false;

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);

        stockBatchAdminDao.ensureJobRuntime(q);

        HashMap<String, String> lock = new HashMap<String, String>();
        lock.put("job_id", jobId);
        lock.put("lock_owner", "STOCK_BATCH_ADMIN");
        lock.put("exec_id", java.util.UUID.randomUUID().toString());
        final String execId = lock.get("exec_id");

        int locked = stockBatchAdminDao.tryAcquireRuntimeLock(lock);
        HashMap<String, Object> runtimeAfterLock = stockBatchAdminDao.selectJobOne(q);
        boolean lockOwned = "Y".equalsIgnoreCase(mapVal(runtimeAfterLock, "running_yn"))
                && execId.equalsIgnoreCase(mapVal(runtimeAfterLock, "current_exec_id"));
        if (!lockOwned) {
            System.out.println("[StockBatchAdminService] triggerJob skip (already running) jobId=" + jobId + ", triggerType=" + triggerType);
            return false;
        }
        if (locked <= 0) {
            System.out.println("[StockBatchAdminService] triggerJob lock acquired by state-check (executorType=batch) jobId=" + jobId + ", execId=" + execId + ", triggerType=" + triggerType + ", updateCount=" + locked);
        } else {
            System.out.println("[StockBatchAdminService] triggerJob lock acquired jobId=" + jobId + ", execId=" + execId + ", triggerType=" + triggerType + ", updateCount=" + locked);
        }
        boolean started = false;
        try {
            if (schedulerRun) {
                updateNextRun(job);
            }

            stockBatchAdminDao.clearStopRequest(q);
            clearStopFlag(jobId);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    String resultCode = "SUCCESS";
                    String resultMsg = "OK";
                    AtomicBoolean heartbeatRunning = new AtomicBoolean(false);
                    Thread heartbeatThread = null;

                    HashMap<String, String> startMap = new HashMap<String, String>();
                    startMap.put("job_id", jobId);

                    try {
                        stockBatchAdminDao.markJobStart(startMap);
                        heartbeatRunning.set(true);
                        heartbeatThread = startHeartbeatThread(jobId, heartbeatRunning);
                        System.out.println("[StockBatchAdminService] job thread started jobId=" + jobId + ", execId=" + execId + ", triggerType=" + triggerType);
                        Map<String, Object> result = executeTask(jobId, execId, triggerType);
                        if (result != null) {
                            String status = toStr(result.get("status"));
                            if ("FAIL".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status)) {
                                resultCode = "ERROR";
                            } else if ("STOP".equalsIgnoreCase(status)) {
                                resultCode = "STOP";
                            }
                            resultMsg = buildResultMessage(result);
                        }
                    } catch (Exception e) {
                        resultCode = "ERROR";
                        resultMsg = safeMsg(e.getLocalizedMessage());
                        System.out.println("[StockBatchAdminService] job thread error jobId=" + jobId + ", execId=" + execId + ", error=" + resultMsg);
                    } finally {
                        stopHeartbeatThread(heartbeatRunning, heartbeatThread);
                        try {
                            HashMap<String, String> finishMap = new HashMap<String, String>();
                            finishMap.put("job_id", jobId);
                            finishMap.put("last_result_code", resultCode);
                            finishMap.put("last_result_msg", safeMsg(resultMsg));
                            stockBatchAdminDao.markJobFinish(finishMap);

                            HashMap<String, String> releaseMap = new HashMap<String, String>();
                            releaseMap.put("job_id", jobId);
                            stockBatchAdminDao.releaseRuntime(releaseMap);
                            System.out.println("[StockBatchAdminService] job thread finished jobId=" + jobId + ", execId=" + execId + ", resultCode=" + resultCode);
                        } catch (Exception ignore) {
                        }
                        RUNNING_THREADS.remove(jobId);
                        STOP_FLAGS.remove(jobId);
                    }
                }
            });

            t.setName("stock-batch-admin-" + jobId);
            t.setDaemon(true);
            RUNNING_THREADS.put(jobId, t);
            t.start();
            started = true;
            return true;
        } catch (Exception e) {
            rollbackTriggerFailure(jobId, safeMsg(e.getLocalizedMessage()));
            System.out.println("[StockBatchAdminService] triggerJob failed after lock jobId=" + jobId + ", execId=" + execId + ", error=" + safeMsg(e.getLocalizedMessage()));
            throw e;
        } catch (Throwable t) {
            rollbackTriggerFailure(jobId, safeMsg(t.getMessage()));
            System.out.println("[StockBatchAdminService] triggerJob throwable after lock jobId=" + jobId + ", execId=" + execId + ", error=" + safeMsg(t.getMessage()));
            throw t;
        } finally {
            if (!started) {
                RUNNING_THREADS.remove(jobId);
                STOP_FLAGS.remove(jobId);
            }
        }
    }

    private void rollbackTriggerFailure(String jobId, String message) {
        String msg = safeMsg("TRIGGER_FAIL: " + nvl(message, ""));
        try {
            HashMap<String, String> finishMap = new HashMap<String, String>();
            finishMap.put("job_id", jobId);
            finishMap.put("last_result_code", "ERROR");
            finishMap.put("last_result_msg", msg);
            stockBatchAdminDao.markJobFinish(finishMap);
        } catch (Exception ignore) {
        }
        try {
            HashMap<String, String> releaseMap = new HashMap<String, String>();
            releaseMap.put("job_id", jobId);
            stockBatchAdminDao.releaseRuntime(releaseMap);
        } catch (Exception ignore) {
        }
    }

    private Map<String, Object> executeTask(String jobId,
                                            String execId,
                                            String triggerType) throws Exception {

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);

        HashMap<String, Object> jobDef = stockBatchAdminDao.selectJobOne(q);
        if (jobDef == null) throw new IllegalStateException("JOB NOT FOUND");

        String taskKey = upperId(mapVal(jobDef, "task_key"));
        if (taskKey.isEmpty()) throw new IllegalStateException("TASK_KEY REQUIRED");

        HashMap<String, String> tq = new HashMap<String, String>();
        tq.put("task_key", taskKey);
        HashMap<String, Object> taskDef = stockBatchAdminDao.selectTaskDef(tq);
        if (taskDef == null) throw new IllegalStateException("TASK DEF NOT FOUND: " + taskKey);

        String beanName = mapVal(taskDef, "bean_name");
        if (beanName.isEmpty()) throw new IllegalStateException("BEAN_NAME REQUIRED");

        Object bean = applicationContext.getBean(beanName);
        if (!(bean instanceof StockBatchTask)) {
            throw new IllegalStateException("BEAN IS NOT StockBatchTask: " + beanName);
        }

        List<HashMap<String, Object>> paramRows = stockBatchAdminDao.selectJobParams(q);
        HashMap<String, String> params = new HashMap<String, String>();
        for (HashMap<String, Object> row : paramRows) {
            String k = nvl(mapVal(row, "param_key"), "");
            String v = mapVal(row, "param_value");
            if (!k.isEmpty()) params.put(k, v);
        }

        params.put("jobId", jobId);
        params.put("execId", execId);
        params.put("triggerType", triggerType);

        StockBatchTask task = (StockBatchTask) bean;
        return task.execute(jobDef, params, new StockBatchTask.StopSignal() {
            @Override
            public boolean shouldStop() {
                return isStopRequested(jobId);
            }
        });
    }

    private void replaceJobParams(String jobId, String paramsJson) throws Exception {
        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);

        stockBatchAdminDao.deleteJobParams(q);

        List<HashMap<String, String>> params = parseParams(paramsJson);
        for (HashMap<String, String> p : params) {
            HashMap<String, String> ins = new HashMap<String, String>();
            ins.put("job_id", jobId);
            ins.put("param_key", nvl(p.get("paramKey"), ""));
            ins.put("param_value", nvl(p.get("paramValue"), ""));
            ins.put("param_type", nvl(p.get("paramType"), "STRING").toUpperCase());
            ins.put("required_yn", yn(p.get("requiredYn"), "N"));
            ins.put("masked_yn", yn(p.get("maskedYn"), "N"));
            if (!isBlank(ins.get("param_key"))) {
                stockBatchAdminDao.insertJobParam(ins);
            }
        }
    }

    private HashMap<String, String> parseScheduleMap(String scheduleJson, String timezone) {
        HashMap<String, String> out = new HashMap<String, String>();

        JSONObject obj;
        if (isBlank(scheduleJson)) {
            obj = new JSONObject();
            obj.put("scheduleType", "INTERVAL");
            obj.put("intervalSec", 3600);
            obj.put("enabledYn", "Y");
            obj.put("misfirePolicy", "SKIP");
        } else {
            obj = new JSONObject(scheduleJson);
        }

        String scheduleType = nvl(obj.optString("scheduleType"), "INTERVAL").toUpperCase();
        String cronExpr = nvl(obj.optString("cronExpr"), "");
        int intervalSec = parsePositiveInt(String.valueOf(obj.opt("intervalSec")), 0);
        String enabledYn = yn(obj.optString("enabledYn"), "Y");
        String misfirePolicy = nvl(obj.optString("misfirePolicy"), "SKIP").toUpperCase();
        String windowStart = nvl(obj.optString("windowStart"), "");
        String windowEnd = nvl(obj.optString("windowEnd"), "");

        ZoneId zone = safeZone(timezone);

        if ("CRON".equals(scheduleType)) {
            if (cronExpr.isEmpty()) {
                throw new IllegalArgumentException("cronExpr is required");
            }
            new CronSequenceGenerator(cronExpr, TimeZone.getTimeZone(zone));
            intervalSec = 0;
        } else {
            scheduleType = "INTERVAL";
            if (intervalSec <= 0) {
                intervalSec = 3600;
            }
            cronExpr = "";
        }

        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextRun = computeNextRun(scheduleType, cronExpr, intervalSec, zone, now);

        out.put("schedule_type", scheduleType);
        out.put("cron_expr", cronExpr);
        out.put("interval_sec", String.valueOf(intervalSec));
        out.put("window_start_hh24mi", windowStart);
        out.put("window_end_hh24mi", windowEnd);
        out.put("misfire_policy", misfirePolicy);
        out.put("enabled_yn", enabledYn);
        out.put("next_run_at", toDateTime(nextRun));
        out.put("last_eval_at", toDateTime(now));

        return out;
    }

    private List<HashMap<String, String>> parseParams(String paramsJson) {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (isBlank(paramsJson)) return list;

        JSONArray arr = new JSONArray(paramsJson);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            HashMap<String, String> m = new HashMap<String, String>();
            m.put("paramKey", obj.optString("paramKey"));
            m.put("paramValue", obj.optString("paramValue"));
            m.put("paramType", obj.optString("paramType"));
            m.put("requiredYn", obj.optString("requiredYn"));
            m.put("maskedYn", obj.optString("maskedYn"));
            list.add(m);
        }
        return list;
    }

    private void updateNextRun(HashMap<String, Object> job) throws Exception {
        String jobId = upperId(mapVal(job, "job_id"));
        String scheduleType = nvl(mapVal(job, "schedule_type"), "INTERVAL").toUpperCase();
        String cronExpr = nvl(mapVal(job, "cron_expr"), "");
        int intervalSec = parsePositiveInt(mapVal(job, "interval_sec"), 0);
        String timezone = nvl(mapVal(job, "timezone"), "Asia/Seoul");

        ZoneId zone = safeZone(timezone);
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime next = computeNextRun(scheduleType, cronExpr, intervalSec, zone, now);

        HashMap<String, String> up = new HashMap<String, String>();
        up.put("job_id", jobId);
        up.put("next_run_at", toDateTime(next));
        up.put("last_eval_at", toDateTime(now));

        stockBatchAdminDao.updateScheduleNextRun(up);
    }

    private ZonedDateTime computeNextRun(String scheduleType,
                                         String cronExpr,
                                         int intervalSec,
                                         ZoneId zone,
                                         ZonedDateTime baseTime) {

        ZonedDateTime now = baseTime == null ? ZonedDateTime.now(zone) : baseTime;
        if ("CRON".equalsIgnoreCase(scheduleType)) {
            CronSequenceGenerator gen = new CronSequenceGenerator(cronExpr, TimeZone.getTimeZone(zone));
            java.util.Date next = gen.next(java.util.Date.from(now.toInstant()));
            return ZonedDateTime.ofInstant(next.toInstant(), zone);
        }

        int sec = intervalSec > 0 ? intervalSec : 3600;
        return now.plusSeconds(sec);
    }

    private boolean isStopRequested(String jobId) {
        AtomicBoolean stopFlag = STOP_FLAGS.get(jobId);
        if (stopFlag != null && stopFlag.get()) {
            return true;
        }
        try {
            HashMap<String, String> q = new HashMap<String, String>();
            q.put("job_id", jobId);
            String dbYn = stockBatchAdminDao.selectStopRequestYn(q);
            return "Y".equalsIgnoreCase(dbYn);
        } catch (Exception ignore) {
            return false;
        }
    }

    private void clearStopFlag(String jobId) {
        AtomicBoolean stopFlag = STOP_FLAGS.get(jobId);
        if (stopFlag == null) {
            stopFlag = new AtomicBoolean(false);
            STOP_FLAGS.put(jobId, stopFlag);
        } else {
            stopFlag.set(false);
        }
    }

    private Thread startHeartbeatThread(final String jobId, final AtomicBoolean heartbeatRunning) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (heartbeatRunning.get()) {
                    try {
                        Thread.sleep(HEARTBEAT_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    if (!heartbeatRunning.get()) {
                        break;
                    }

                    try {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("job_id", jobId);
                        stockBatchAdminDao.heartbeatRuntime(map);
                    } catch (Exception e) {
                        System.out.println("[StockBatchAdminService] heartbeatRuntime failed jobId=" + jobId + ", error=" + safeMsg(e.getLocalizedMessage()));
                    }
                }
            }
        });

        t.setName("stock-batch-heartbeat-" + jobId);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void stopHeartbeatThread(AtomicBoolean heartbeatRunning, Thread heartbeatThread) {
        if (heartbeatRunning != null) {
            heartbeatRunning.set(false);
        }
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
        }
    }

    private void abortLocalStaleJobs(List<String> staleJobIds) {
        for (int i = 0; staleJobIds != null && i < staleJobIds.size(); i++) {
            String jobId = upperId(staleJobIds.get(i));
            if (jobId.isEmpty()) {
                continue;
            }

            AtomicBoolean stopFlag = STOP_FLAGS.get(jobId);
            if (stopFlag == null) {
                stopFlag = new AtomicBoolean(true);
                STOP_FLAGS.put(jobId, stopFlag);
            } else {
                stopFlag.set(true);
            }

            Thread runningThread = RUNNING_THREADS.get(jobId);
            if (runningThread != null) {
                runningThread.interrupt();
                System.out.println("[StockBatchAdminService] stale runtime interrupt requested jobId=" + jobId);
            }
        }
    }

    private int normalizeUpdateCount(int updateCount) {
        return updateCount < 0 ? 0 : updateCount;
    }

    private Map<String, Object> out(String code, String message, Object data) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("code", code);
        out.put("message", message);
        out.put("data", data);
        out.put("asOf", LocalDateTime.now(DEFAULT_ZONE).format(DATETIME_FMT));
        return out;
    }

    private HashMap<String, String> enrichJobLogQuery(HashMap<String, String> map) throws Exception {
        HashMap<String, String> queryMap = new HashMap<String, String>();
        if (map != null) {
            queryMap.putAll(map);
        }

        String jobId = upperId(queryMap.get("job_id"));
        if (jobId.isEmpty()) {
            return queryMap;
        }

        if (isBlank(queryMap.get("task_key"))) {
            HashMap<String, String> q = new HashMap<String, String>();
            q.put("job_id", jobId);
            HashMap<String, Object> job = stockBatchAdminDao.selectJobOne(q);
            if (job != null) {
                queryMap.put("task_key", upperId(mapVal(job, "task_key")));
            }
        }

        if (isBlank(queryMap.get("market_group"))) {
            String marketGroup = resolveJobMarketGroup(jobId);
            if (!isBlank(marketGroup)) {
                queryMap.put("market_group", marketGroup);
            }
        }

        return queryMap;
    }

    private String resolveJobMarketGroup(String jobId) throws Exception {
        if (isBlank(jobId)) {
            return null;
        }

        HashMap<String, String> q = new HashMap<String, String>();
        q.put("job_id", jobId);
        List<HashMap<String, Object>> paramRows = stockBatchAdminDao.selectJobParams(q);
        for (int i = 0; paramRows != null && i < paramRows.size(); i++) {
            HashMap<String, Object> row = paramRows.get(i);
            String key = upperId(mapVal(row, "param_key"));
            if (!"MARKETGROUP".equals(key) && !"MKTCD".equals(key) && !"SOURCEMKTCD".equals(key)) {
                continue;
            }

            String marketGroup = normalizeMarketGroup(mapVal(row, "param_value"));
            if (!isBlank(marketGroup)) {
                return marketGroup;
            }
        }

        if (jobId.endsWith("_US")) {
            return "US";
        }
        if (jobId.endsWith("_KR")) {
            return "KR";
        }
        return null;
    }

    private String normalizeMarketGroup(String marketGroup) {
        String value = upperId(marketGroup);
        if ("US".equals(value) || value.endsWith("_US")) {
            return "US";
        }
        if ("KR".equals(value) || value.endsWith("_KR")) {
            return "KR";
        }
        return null;
    }

    private ZoneId safeZone(String timezone) {
        try {
            return ZoneId.of(nvl(timezone, "Asia/Seoul"));
        } catch (Exception e) {
            return DEFAULT_ZONE;
        }
    }

    private String toDateTime(ZonedDateTime zdt) {
        if (zdt == null) return "";
        return DATETIME_FMT.format(zdt);
    }

    private String buildResultMessage(Map<String, Object> result) {
        if (result == null || result.isEmpty()) return "OK";

        String msg = nvl(toStr(result.get("message")), "");
        if (!msg.isEmpty()) return safeMsg(msg);

        StringBuilder sb = new StringBuilder();
        if (result.containsKey("totalCnt")) sb.append("total=").append(toStr(result.get("totalCnt"))).append(' ');
        if (result.containsKey("successCnt")) sb.append("success=").append(toStr(result.get("successCnt"))).append(' ');
        if (result.containsKey("failCnt")) sb.append("fail=").append(toStr(result.get("failCnt"))).append(' ');
        if (result.containsKey("excludedCnt")) sb.append("excluded=").append(toStr(result.get("excludedCnt"))).append(' ');

        String t = sb.toString().trim();
        return safeMsg(t.isEmpty() ? "OK" : t);
    }

    private String safeMsg(String msg) {
        String m = nvl(msg, "");
        if (m.length() > 1000) return m.substring(0, 1000);
        return m;
    }

    private String yn(String value, String dft) {
        String v = nvl(value, dft).toUpperCase();
        return "Y".equals(v) ? "Y" : "N";
    }

    private int parsePositiveInt(String value, int dft) {
        try {
            int n = Integer.parseInt(nvl(value, "").trim());
            return n > 0 ? n : dft;
        } catch (Exception e) {
            return dft;
        }
    }

    private String upperId(String value) {
        return nvl(value, "").trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    private String nvl(String value, String dft) {
        if (value == null) return dft;
        String v = value.trim();
        return v.length() == 0 ? dft : v;
    }

    private String toStr(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String mapVal(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object v = map.get(key);
        if (v == null) v = map.get(key.toUpperCase());
        if (v == null) v = map.get(key.toLowerCase());
        return toStr(v);
    }
}
