-- =====================================================================
-- REC_SIGNAL batch runtime state repair
-- Date: 2026-05-13
-- Purpose:
--   1. REC_SIGNAL_RUN_KR/US batches normally take several hours, so the
--      1200-second runtime setting is too short for stale-lock decisions.
--   2. Clear orphan RUNNING rows whose heartbeat is already stale.
-- =====================================================================

UPDATE TB_STK_BATCH_JOB_DEF
   SET MAX_RUNTIME_SEC = 28800,
       MOD_DT = CURRENT_TIMESTAMP
 WHERE JOB_ID IN ('REC_SIGNAL_RUN_KR', 'REC_SIGNAL_RUN_US')
   AND NVL(MAX_RUNTIME_SEC, 0) < 28800;

UPDATE TB_BATCH_EXEC_LOG L
   SET END_AT = CURRENT_TIMESTAMP,
       STATUS = 'FAIL',
       ERROR_MSG = SUBSTR(
           CASE
               WHEN L.ERROR_MSG IS NULL OR TRIM(L.ERROR_MSG) = '' THEN 'AUTO_ABORT_STALE_RUNTIME'
               ELSE L.ERROR_MSG || ' / AUTO_ABORT_STALE_RUNTIME'
           END,
           1,
           2000
       )
 WHERE L.STATUS = 'RUNNING'
   AND EXISTS (
        SELECT 1
          FROM TB_STK_BATCH_JOB_RUNTIME R
          JOIN TB_STK_BATCH_JOB_DEF J
            ON J.JOB_ID = R.JOB_ID
          LEFT JOIN (
                SELECT JOB_ID,
                       MAX(
                           CASE
                               WHEN UPPER(PARAM_KEY) IN ('MARKETGROUP', 'MKTCD', 'SOURCEMKTCD')
                               THEN UPPER(DBMS_LOB.SUBSTR(PARAM_VALUE, 100, 1))
                           END
                       ) AS MARKET_GROUP
                  FROM TB_STK_BATCH_JOB_PARAM
                 GROUP BY JOB_ID
          ) P
            ON P.JOB_ID = J.JOB_ID
         WHERE R.RUNNING_YN = 'Y'
           AND J.JOB_ID IN ('REC_SIGNAL_RUN_KR', 'REC_SIGNAL_RUN_US')
           AND (
                NVL(R.LAST_HEARTBEAT_AT, R.LOCK_AT) < (CURRENT_TIMESTAMP - (10 / (24*60)))
                OR R.LOCK_AT < (CURRENT_TIMESTAMP - (J.MAX_RUNTIME_SEC / (24*60*60)))
           )
           AND (
                L.EXEC_ID = R.CURRENT_EXEC_ID
                OR (
                    J.TASK_KEY = 'REC_SIGNAL_RUN'
                    AND L.BATCH_ID LIKE 'BATCH_' || REPLACE(UPPER(J.TASK_KEY), '_RUN', '') || '_%'
                    AND L.START_AT >= NVL(J.LAST_START_AT, R.LOCK_AT)
                    AND (
                        P.MARKET_GROUP IS NULL
                        OR INSTR(UPPER(L.BATCH_ID), '_' || P.MARKET_GROUP || '_') > 0
                    )
                )
           )
   );

UPDATE TB_STK_BATCH_JOB_RUNTIME R
   SET RUNNING_YN = 'N',
       STOP_REQUEST_YN = 'N',
       CURRENT_EXEC_ID = NULL,
       LAST_HEARTBEAT_AT = NULL,
       MOD_DT = CURRENT_TIMESTAMP
 WHERE R.JOB_ID IN ('REC_SIGNAL_RUN_KR', 'REC_SIGNAL_RUN_US')
   AND R.RUNNING_YN = 'Y'
   AND EXISTS (
        SELECT 1
          FROM TB_STK_BATCH_JOB_DEF J
         WHERE J.JOB_ID = R.JOB_ID
           AND (
                NVL(R.LAST_HEARTBEAT_AT, R.LOCK_AT) < (CURRENT_TIMESTAMP - (10 / (24*60)))
                OR R.LOCK_AT < (CURRENT_TIMESTAMP - (J.MAX_RUNTIME_SEC / (24*60*60)))
           )
   );

UPDATE TB_BATCH_EXEC_LOG L
   SET END_AT = CURRENT_TIMESTAMP,
       STATUS = 'FAIL',
       ERROR_MSG = SUBSTR(
           CASE
               WHEN L.ERROR_MSG IS NULL OR TRIM(L.ERROR_MSG) = '' THEN 'AUTO_ABORT_ORPHAN_RUNNING_LOG'
               ELSE L.ERROR_MSG || ' / AUTO_ABORT_ORPHAN_RUNNING_LOG'
           END,
           1,
           2000
       )
 WHERE L.STATUS = 'RUNNING'
   AND L.START_AT < (CURRENT_TIMESTAMP - 1)
   AND NOT EXISTS (
        SELECT 1
          FROM TB_STK_BATCH_JOB_RUNTIME R
         WHERE R.RUNNING_YN = 'Y'
           AND R.CURRENT_EXEC_ID = L.EXEC_ID
   );

UPDATE TB_STK_BATCH_JOB_DEF J
   SET LAST_RESULT_CODE = 'ABORTED',
       LAST_RESULT_MSG = 'AUTO_ABORT_STALE_RUNTIME',
       LAST_END_AT = CURRENT_TIMESTAMP,
       MOD_DT = CURRENT_TIMESTAMP
 WHERE J.JOB_ID IN ('REC_SIGNAL_RUN_KR', 'REC_SIGNAL_RUN_US')
   AND J.LAST_RESULT_CODE = 'RUNNING'
   AND NOT EXISTS (
        SELECT 1
          FROM TB_STK_BATCH_JOB_RUNTIME R
         WHERE R.JOB_ID = J.JOB_ID
           AND R.RUNNING_YN = 'Y'
   );

COMMIT;
