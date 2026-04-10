-- TB_TRADE_CALENDAR US 거래일 초기 데이터 (2024-01-01 ~ 2026-12-31)
-- NYSE/NASDAQ 기준: 월~금 중 미국 공휴일 제외
-- 실행 방법: sqlplus 접속 후 @SEED_TB_TRADE_CALENDAR_US.sql

MERGE INTO TB_TRADE_CALENDAR T
USING (
    SELECT dt AS TRADE_DT, 'US' AS MKT_CD, 'Y' AS TRADE_YN
    FROM (
        SELECT TO_DATE('2024-01-01','YYYY-MM-DD') + LEVEL - 1 AS dt
        FROM DUAL
        CONNECT BY LEVEL <= (TO_DATE('2026-12-31','YYYY-MM-DD') - TO_DATE('2024-01-01','YYYY-MM-DD') + 1)
    )
    WHERE TO_CHAR(dt, 'DY', 'NLS_DATE_LANGUAGE=ENGLISH') NOT IN ('SAT', 'SUN')
      AND dt NOT IN (
          -- 2024 US Market Holidays
          TO_DATE('2024-01-01','YYYY-MM-DD'), -- New Year's Day
          TO_DATE('2024-01-15','YYYY-MM-DD'), -- MLK Day
          TO_DATE('2024-02-19','YYYY-MM-DD'), -- Presidents' Day
          TO_DATE('2024-03-29','YYYY-MM-DD'), -- Good Friday
          TO_DATE('2024-05-27','YYYY-MM-DD'), -- Memorial Day
          TO_DATE('2024-06-19','YYYY-MM-DD'), -- Juneteenth
          TO_DATE('2024-07-04','YYYY-MM-DD'), -- Independence Day
          TO_DATE('2024-09-02','YYYY-MM-DD'), -- Labor Day
          TO_DATE('2024-11-28','YYYY-MM-DD'), -- Thanksgiving
          TO_DATE('2024-12-25','YYYY-MM-DD'), -- Christmas
          -- 2025 US Market Holidays
          TO_DATE('2025-01-01','YYYY-MM-DD'), -- New Year's Day
          TO_DATE('2025-01-09','YYYY-MM-DD'), -- National Day of Mourning (Jimmy Carter)
          TO_DATE('2025-01-20','YYYY-MM-DD'), -- MLK Day / Inauguration Day
          TO_DATE('2025-02-17','YYYY-MM-DD'), -- Presidents' Day
          TO_DATE('2025-04-18','YYYY-MM-DD'), -- Good Friday
          TO_DATE('2025-05-26','YYYY-MM-DD'), -- Memorial Day
          TO_DATE('2025-06-19','YYYY-MM-DD'), -- Juneteenth
          TO_DATE('2025-07-04','YYYY-MM-DD'), -- Independence Day
          TO_DATE('2025-09-01','YYYY-MM-DD'), -- Labor Day
          TO_DATE('2025-11-27','YYYY-MM-DD'), -- Thanksgiving
          TO_DATE('2025-12-25','YYYY-MM-DD'), -- Christmas
          -- 2026 US Market Holidays
          TO_DATE('2026-01-01','YYYY-MM-DD'), -- New Year's Day
          TO_DATE('2026-01-19','YYYY-MM-DD'), -- MLK Day
          TO_DATE('2026-02-16','YYYY-MM-DD'), -- Presidents' Day
          TO_DATE('2026-04-03','YYYY-MM-DD'), -- Good Friday
          TO_DATE('2026-05-25','YYYY-MM-DD'), -- Memorial Day
          TO_DATE('2026-06-19','YYYY-MM-DD'), -- Juneteenth
          TO_DATE('2026-07-03','YYYY-MM-DD'), -- Independence Day (observed, Jul 4 is Sat)
          TO_DATE('2026-09-07','YYYY-MM-DD'), -- Labor Day
          TO_DATE('2026-11-26','YYYY-MM-DD'), -- Thanksgiving
          TO_DATE('2026-12-25','YYYY-MM-DD')  -- Christmas
      )
) S
ON (T.TRADE_DT = S.TRADE_DT AND T.MKT_CD = S.MKT_CD)
WHEN MATCHED THEN
    UPDATE SET T.TRADE_YN = S.TRADE_YN
WHEN NOT MATCHED THEN
    INSERT (TRADE_DT, MKT_CD, TRADE_YN, CREATED_AT)
    VALUES (S.TRADE_DT, S.MKT_CD, S.TRADE_YN, SYSDATE);

COMMIT;

-- 결과 확인
SELECT MKT_CD,
       COUNT(*) AS TOTAL_CNT,
       MIN(TO_CHAR(TRADE_DT,'YYYY-MM-DD')) AS MIN_DT,
       MAX(TO_CHAR(TRADE_DT,'YYYY-MM-DD')) AS MAX_DT
  FROM TB_TRADE_CALENDAR
 WHERE MKT_CD = 'US'
   AND TRADE_YN = 'Y'
 GROUP BY MKT_CD;
