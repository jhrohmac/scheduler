PROMPT === REC SIGNAL SPACE RECLAIM START ===
SET SERVEROUTPUT ON

PROMPT [BEFORE] TARGET SEGMENT SIZE (MB)
SELECT SEGMENT_NAME,
       SEGMENT_TYPE,
       ROUND(BYTES / 1024 / 1024, 2) AS MB
  FROM USER_SEGMENTS
 WHERE SEGMENT_NAME IN (
       'TB_REC_SIGNAL',
       'TB_BATCH_EXEC_LOG',
       'TB_BATCH_EXEC_ITEM_LOG',
       'PK_REC_SIGNAL',
       'IDX_REC_SIGNAL_YN',
       'PK_BATCH_EXEC_LOG',
       'IDX_BATCH_EXEC_LOG_1',
       'PK_BATCH_EXEC_ITEM_LOG',
       'IDX_BATCH_EXEC_ITEM_LOG_1'
 )
 ORDER BY BYTES DESC;

DECLARE
    PROCEDURE exec_table_move(p_table_name IN VARCHAR2) IS
        v_cnt NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_cnt
          FROM USER_TABLES
         WHERE TABLE_NAME = p_table_name;

        IF v_cnt = 0 THEN
            DBMS_OUTPUT.PUT_LINE('SKIP: ALTER TABLE ' || p_table_name || ' MOVE (table not found)');
            RETURN;
        END IF;

        BEGIN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table_name || ' MOVE';
            DBMS_OUTPUT.PUT_LINE('OK  : ALTER TABLE ' || p_table_name || ' MOVE');
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('FAIL: ALTER TABLE ' || p_table_name || ' MOVE => ' || SQLERRM);
        END;
    END;

    PROCEDURE exec_index_rebuild(p_index_name IN VARCHAR2) IS
        v_cnt NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_cnt
          FROM USER_INDEXES
         WHERE INDEX_NAME = p_index_name;

        IF v_cnt = 0 THEN
            DBMS_OUTPUT.PUT_LINE('SKIP: ALTER INDEX ' || p_index_name || ' REBUILD (index not found)');
            RETURN;
        END IF;

        BEGIN
            EXECUTE IMMEDIATE 'ALTER INDEX ' || p_index_name || ' REBUILD';
            DBMS_OUTPUT.PUT_LINE('OK  : ALTER INDEX ' || p_index_name || ' REBUILD');
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('FAIL: ALTER INDEX ' || p_index_name || ' REBUILD => ' || SQLERRM);
        END;
    END;

    PROCEDURE gather_table_stats_safe(p_table_name IN VARCHAR2) IS
    BEGIN
        DBMS_STATS.GATHER_TABLE_STATS(OWNNAME => USER, TABNAME => p_table_name);
        DBMS_OUTPUT.PUT_LINE('OK  : DBMS_STATS.GATHER_TABLE_STATS(' || p_table_name || ')');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('WARN: DBMS_STATS.GATHER_TABLE_STATS(' || p_table_name || ') => ' || SQLERRM);
    END;
BEGIN
    exec_table_move('TB_REC_SIGNAL');
    exec_index_rebuild('PK_REC_SIGNAL');
    exec_index_rebuild('IDX_REC_SIGNAL_YN');
    gather_table_stats_safe('TB_REC_SIGNAL');

    exec_table_move('TB_BATCH_EXEC_LOG');
    exec_index_rebuild('PK_BATCH_EXEC_LOG');
    exec_index_rebuild('IDX_BATCH_EXEC_LOG_1');
    gather_table_stats_safe('TB_BATCH_EXEC_LOG');

    exec_table_move('TB_BATCH_EXEC_ITEM_LOG');
    exec_index_rebuild('PK_BATCH_EXEC_ITEM_LOG');
    exec_index_rebuild('IDX_BATCH_EXEC_ITEM_LOG_1');
    gather_table_stats_safe('TB_BATCH_EXEC_ITEM_LOG');
END;
/

PROMPT [OPTION] REMOVE RECYCLEBIN OBJECTS
PURGE RECYCLEBIN;

PROMPT [AFTER] TARGET SEGMENT SIZE (MB)
SELECT SEGMENT_NAME,
       SEGMENT_TYPE,
       ROUND(BYTES / 1024 / 1024, 2) AS MB
  FROM USER_SEGMENTS
 WHERE SEGMENT_NAME IN (
       'TB_REC_SIGNAL',
       'TB_BATCH_EXEC_LOG',
       'TB_BATCH_EXEC_ITEM_LOG',
       'PK_REC_SIGNAL',
       'IDX_REC_SIGNAL_YN',
       'PK_BATCH_EXEC_LOG',
       'IDX_BATCH_EXEC_LOG_1',
       'PK_BATCH_EXEC_ITEM_LOG',
       'IDX_BATCH_EXEC_ITEM_LOG_1'
 )
 ORDER BY BYTES DESC;

PROMPT [AFTER] TOTAL USER SEGMENT (MB)
SELECT ROUND(NVL(SUM(BYTES), 0) / 1024 / 1024, 2) AS TOTAL_MB
  FROM USER_SEGMENTS;

PROMPT === REC SIGNAL SPACE RECLAIM DONE ===
