package com.scheduler.stock.batch;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * 추천 신호 + 추천 추적 + 매도 가이드 통합 스케줄러
 *
 * 실행 순서 (장 마감 기준, KR 기준):
 *   02:00 KR RecSignal Primary
 *   03:30 KR RecPick DailyTrack  ← 신규 추가
 *   04:00 KR PositionMonitor     ← 신규 추가
 *   05:00 KR RecSignal Retry
 *   16:00 US RecSignal Primary
 *   17:30 US RecPick DailyTrack  ← 신규 추가
 *   18:00 US PositionMonitor     ← 신규 추가
 *   20:00 US RecSignal Retry
 */
public class RecSignalMarketScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private RecSignalDailyBatch recSignalDailyBatch;
    private RecPickDailyBatch recPickDailyBatch;
    private PositionMonitorDailyBatch positionMonitorDailyBatch;

    public void setRecSignalDailyBatch(RecSignalDailyBatch recSignalDailyBatch) {
        this.recSignalDailyBatch = recSignalDailyBatch;
    }

    public void setRecPickDailyBatch(RecPickDailyBatch recPickDailyBatch) {
        this.recPickDailyBatch = recPickDailyBatch;
    }

    public void setPositionMonitorDailyBatch(PositionMonitorDailyBatch positionMonitorDailyBatch) {
        this.positionMonitorDailyBatch = positionMonitorDailyBatch;
    }

    /* ─────────────────────────────────────────────
       KR 구간
    ───────────────────────────────────────────── */

    @Scheduled(cron = "0 0 2 ? * TUE-SAT")
    public void runKrPrimary() throws Exception {
        recSignalDailyBatch.runScheduled("KR", false, resolvePreviousBaseDate());
    }

    /** RecSignal 이후 1.5시간 뒤 — KIS 일봉 데이터 수집 후 추적 */
    @Scheduled(cron = "0 30 3 ? * TUE-SAT")
    public void runKrRecPickTrack() throws Exception {
        if (recPickDailyBatch == null) return;
        java.util.HashMap<String, String> map = new java.util.HashMap<String, String>();
        map.put("mktCd", "KR");
        recPickDailyBatch.run(map);
    }

    /** RecPick 추적 30분 후 — 매도 가이드 갱신 */
    @Scheduled(cron = "0 0 4 ? * TUE-SAT")
    public void runKrPositionMonitor() throws Exception {
        if (positionMonitorDailyBatch == null) return;
        java.util.HashMap<String, String> map = new java.util.HashMap<String, String>();
        map.put("mktCd", "KR");
        positionMonitorDailyBatch.run(map);
    }

    @Scheduled(cron = "0 0 5 ? * TUE-SAT")
    public void runKrRetry() throws Exception {
        recSignalDailyBatch.runScheduled("KR", true, resolvePreviousBaseDate());
    }

    /* ─────────────────────────────────────────────
       US 구간
    ───────────────────────────────────────────── */

    @Scheduled(cron = "0 0 16 ? * TUE-SAT")
    public void runUsPrimary() throws Exception {
        recSignalDailyBatch.runScheduled("US", false, resolvePreviousBaseDate());
    }

    @Scheduled(cron = "0 30 17 ? * TUE-SAT")
    public void runUsRecPickTrack() throws Exception {
        if (recPickDailyBatch == null) return;
        java.util.HashMap<String, String> map = new java.util.HashMap<String, String>();
        map.put("mktCd", "US");
        recPickDailyBatch.run(map);
    }

    @Scheduled(cron = "0 0 18 ? * TUE-SAT")
    public void runUsPositionMonitor() throws Exception {
        if (positionMonitorDailyBatch == null) return;
        java.util.HashMap<String, String> map = new java.util.HashMap<String, String>();
        map.put("mktCd", "US");
        positionMonitorDailyBatch.run(map);
    }

    @Scheduled(cron = "0 0 20 ? * TUE-SAT")
    public void runUsRetry() throws Exception {
        recSignalDailyBatch.runScheduled("US", true, resolvePreviousBaseDate());
    }

    /* ─────────────────────────────────────────────
       유틸
    ───────────────────────────────────────────── */

    private String resolvePreviousBaseDate() {
        return LocalDate.now(KOREA_ZONE).minusDays(1).toString();
    }
}
