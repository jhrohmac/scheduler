-- =====================================================================
-- 종목 마스터 갱신 배치 잡 등록
-- 실행 전 조건: MIGRATE_TB_STK_MASTER_INDEX_CD.sql 완료 후 실행
-- =====================================================================

-- =====================================================================
-- SEED_STK_MASTER_REFRESH_JOB
-- =====================================================================

-- 1. Task 등록
MERGE INTO TB_STK_BATCH_TASK_DEF T
USING (SELECT 'STK_MASTER_REFRESH' AS TASK_KEY FROM DUAL) S
ON (T.TASK_KEY = S.TASK_KEY)
WHEN MATCHED THEN
    UPDATE SET
        T.TASK_NAME  = '종목 마스터 갱신',
        T.BEAN_NAME  = 'stkMasterRefreshTask',
        T.ENABLED_YN = 'Y',
        T.DESCRIPTION = 'KIS 파일 다운로드 → KOSPI/KOSDAQ/NASDAQ/NYSE 파싱 → TB_STK_MASTER MERGE + 상장폐지 처리',
        T.MOD_DT     = SYSDATE
WHEN NOT MATCHED THEN
    INSERT (TASK_KEY, TASK_NAME, BEAN_NAME, ENABLED_YN, DESCRIPTION, REG_DT, MOD_DT)
    VALUES ('STK_MASTER_REFRESH', '종목 마스터 갱신', 'stkMasterRefreshTask', 'Y',
            'KIS 파일 다운로드 → KOSPI/KOSDAQ/NASDAQ/NYSE 파싱 → TB_STK_MASTER MERGE + 상장폐지 처리',
            SYSDATE, SYSDATE);

-- 2. Job 등록 (KR+US 전체)
MERGE INTO TB_STK_BATCH_JOB_DEF T
USING (SELECT 'STK_MASTER_REFRESH_ALL' AS JOB_ID FROM DUAL) S
ON (T.JOB_ID = S.JOB_ID)
WHEN MATCHED THEN
    UPDATE SET
        T.JOB_NAME          = '종목 마스터 갱신 (KR+US)',
        T.TASK_KEY          = 'STK_MASTER_REFRESH',
        T.ENABLED_YN        = 'Y',
        T.MAX_RUNTIME_SEC   = 1800,
        T.DESCRIPTION       = '매일 01:00 KR+US 전체 종목 마스터 갱신',
        T.MOD_DT            = SYSDATE
WHEN NOT MATCHED THEN
    INSERT (JOB_ID, JOB_NAME, TASK_KEY, ENABLED_YN, TIMEZONE, MAX_RUNTIME_SEC,
            ALLOW_MANUAL_RUN_YN, RUN_POLICY, DESCRIPTION, REG_DT, MOD_DT)
    VALUES ('STK_MASTER_REFRESH_ALL', '종목 마스터 갱신 (KR+US)', 'STK_MASTER_REFRESH',
            'Y', 'Asia/Seoul', 1800, 'Y', 'SINGLETON',
            '매일 01:00 KR+US 전체 종목 마스터 갱신', SYSDATE, SYSDATE);

-- 3. Job Runtime 초기화
MERGE INTO TB_STK_BATCH_JOB_RUNTIME T
USING (SELECT 'STK_MASTER_REFRESH_ALL' AS JOB_ID FROM DUAL) S
ON (T.JOB_ID = S.JOB_ID)
WHEN NOT MATCHED THEN
    INSERT (JOB_ID, RUNNING_YN, STOP_REQUEST_YN, MOD_DT)
    VALUES ('STK_MASTER_REFRESH_ALL', 'N', 'N', SYSDATE);

-- 4. 기본 파라미터
MERGE INTO TB_STK_BATCH_JOB_PARAM T
USING (SELECT 'STK_MASTER_REFRESH_ALL' AS JOB_ID, 'marketGroup' AS PARAM_KEY FROM DUAL) S
ON (T.JOB_ID = S.JOB_ID AND T.PARAM_KEY = S.PARAM_KEY)
WHEN MATCHED THEN
    UPDATE SET T.PARAM_VALUE = 'ALL', T.MOD_DT = SYSDATE
WHEN NOT MATCHED THEN
    INSERT (PARAM_ID, JOB_ID, PARAM_KEY, PARAM_VALUE, PARAM_TYPE, REQUIRED_YN, MASKED_YN, REG_DT, MOD_DT)
    VALUES (SEQ_STK_BATCH_JOB_PARAM.NEXTVAL, 'STK_MASTER_REFRESH_ALL', 'marketGroup', 'ALL',
            'STRING', 'N', 'N', SYSDATE, SYSDATE);

COMMIT;

-- DONE
-- TASK_KEY : STK_MASTER_REFRESH  (bean: stkMasterRefreshTask)
-- JOB_ID   : STK_MASTER_REFRESH_ALL
-- 수동 실행 가능 (batchAdmin UI → 잡 목록에서 확인)
