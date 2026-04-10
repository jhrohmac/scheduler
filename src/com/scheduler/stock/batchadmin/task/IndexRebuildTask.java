package com.scheduler.stock.batchadmin.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scheduler.comm.system.IndexMaintenanceService;

/**
 * UNUSABLE 상태 Oracle 인덱스를 감지하여 자동 REBUILD 하는 배치 Task.
 *
 * TB_STK_BATCH_TASK_DEF 등록 정보
 *   TASK_KEY  : INDEX_REBUILD
 *   BEAN_NAME : indexRebuildTask
 *
 * 반환값 (result map)
 *   status          : SUCCESS | ERROR
 *   rebuilt_count   : 재빌드한 인덱스 수
 *   rebuilt_indexes : 재빌드한 인덱스 이름 목록 (쉼표 구분)
 *   message         : 결과 요약 메시지
 */
public class IndexRebuildTask implements StockBatchTask {

    private IndexMaintenanceService indexMaintenanceService;

    public void setIndexMaintenanceService(IndexMaintenanceService indexMaintenanceService) {
        this.indexMaintenanceService = indexMaintenanceService;
    }

    @Override
    public Map<String, Object> execute(HashMap<String, Object> jobDef,
                                       HashMap<String, String> params,
                                       StopSignal stopSignal) throws Exception {

        if (indexMaintenanceService == null) {
            throw new IllegalStateException("indexMaintenanceService 가 설정되지 않았습니다.");
        }

        if (stopSignal != null && stopSignal.shouldStop()) {
            throw new RuntimeException("STOP REQUESTED");
        }

        List<String> rebuilt = indexMaintenanceService.rebuildAllUnusableNow();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "SUCCESS");
        result.put("rebuilt_count", rebuilt.size());
        result.put("rebuilt_indexes", join(rebuilt));

        if (rebuilt.isEmpty()) {
            result.put("message", "UNUSABLE 인덱스 없음 - 모두 정상");
        } else {
            result.put("message", rebuilt.size() + "건 REBUILD 완료: " + join(rebuilt));
        }

        return result;
    }

    private String join(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
