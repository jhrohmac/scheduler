package com.scheduler.comm.system;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 사용 불가(UNUSABLE) 상태의 Oracle 인덱스를 주기적으로 감지하여 자동 재빌드하는 서비스.
 *
 * ORA-01502 오류(인덱스 사용 불가 상태) 발생 시 수동 조치 없이 자동 복구합니다.
 * - 체크 주기 : 5분마다 (fixedDelay = 300_000 ms)
 * - 대상      : USER_INDEXES.STATUS = 'UNUSABLE' 인 모든 인덱스
 * - 조치      : ALTER INDEX {name} REBUILD ONLINE (실패 시 REBUILD로 재시도)
 */
public class IndexMaintenanceService {

    private static final Log log = LogFactory.getLog(IndexMaintenanceService.class);

    private static final String SQL_UNUSABLE =
        "SELECT INDEX_NAME FROM USER_INDEXES WHERE STATUS = 'UNUSABLE' ORDER BY INDEX_NAME";

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 수동 호출용 (컨트롤러 또는 테스트에서 즉시 실행).
     */
    public List<String> rebuildAllUnusableNow() {
        List<String> targets = findUnusableIndexes();
        for (String indexName : targets) {
            rebuildIndex(indexName);
        }
        return targets;
    }

    // -------------------------------------------------------------------------

    private List<String> findUnusableIndexes() {
        List<String> list = new ArrayList<String>();
        Connection conn = null;
        Statement  stmt = null;
        ResultSet  rs   = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            stmt = conn.createStatement();
            rs   = stmt.executeQuery(SQL_UNUSABLE);
            while (rs.next()) {
                list.add(rs.getString("INDEX_NAME"));
            }
        } catch (Exception e) {
            log.error("[IndexMaintenance] UNUSABLE 인덱스 조회 중 오류: " + e.getMessage(), e);
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    private void rebuildIndex(String indexName) {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 1차 시도: REBUILD ONLINE (DML 허용, 운영 환경 권장)
        if (tryRebuild(indexName, "ALTER INDEX " + indexName + " REBUILD ONLINE")) {
            log.info("[IndexMaintenance] [" + ts + "] REBUILD ONLINE 성공: " + indexName);
            return;
        }
        // 2차 시도: REBUILD (ONLINE 미지원 인덱스 대비)
        if (tryRebuild(indexName, "ALTER INDEX " + indexName + " REBUILD")) {
            log.info("[IndexMaintenance] [" + ts + "] REBUILD 성공: " + indexName);
            return;
        }
        log.error("[IndexMaintenance] [" + ts + "] REBUILD 실패 (수동 조치 필요): " + indexName);
    }

    private boolean tryRebuild(String indexName, String sql) {
        Connection conn = null;
        Statement  stmt = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            stmt = conn.createStatement();
            stmt.execute(sql);
            return true;
        } catch (Exception e) {
            log.warn("[IndexMaintenance] 실패 [" + sql + "]: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(null, stmt, conn);
        }
    }

    private void closeQuietly(ResultSet rs, Statement stmt, Connection conn) {
        if (rs   != null) try { rs.close();   } catch (Exception ignored) {}
        if (stmt != null) try { stmt.close();  } catch (Exception ignored) {}
        if (conn != null) try { conn.close();  } catch (Exception ignored) {}
    }
}
