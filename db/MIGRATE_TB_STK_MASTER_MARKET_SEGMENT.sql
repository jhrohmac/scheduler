PROMPT === TB_STK_MASTER market segment migration start ===

DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_cnt
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'TB_STK_MASTER'
       AND COLUMN_NAME = 'DOW_MEMBER_YN';

    IF v_cnt = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE TB_STK_MASTER ADD (DOW_MEMBER_YN CHAR(1) DEFAULT ''N'' NOT NULL)';
    END IF;
END;
/

UPDATE TB_STK_MASTER
   SET DOW_MEMBER_YN = 'N'
 WHERE DOW_MEMBER_YN IS NULL;

COMMIT;

COMMENT ON COLUMN TB_STK_MASTER.MKT_CD IS '상장 시장/거래소: KOSPI, KOSDAQ, NASDAQ, NYSE';
COMMENT ON COLUMN TB_STK_MASTER.DOW_MEMBER_YN IS '다우지수 편입 여부: Y/N';

PROMPT === manual backfill required ===
PROMPT - TB_STK_MASTER.MKT_CD values must be backfilled to KOSPI/KOSDAQ/NASDAQ/NYSE.
PROMPT - Dow constituent stocks must be marked with DOW_MEMBER_YN = 'Y'.

PROMPT === TB_STK_MASTER market segment migration done ===
