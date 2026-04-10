package com.scheduler.stock.batch;

/**
 * Legacy fixed scheduler holder.
 *
 * Batch execution is now driven only by TB_STK_BATCH_JOB_SCHEDULE through
 * StockBatchAdminController.stockBatchAdminTick().
 */
@Deprecated
public class RecSignalMarketScheduler {

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

    public boolean isEnabled() {
        return false;
    }
}
