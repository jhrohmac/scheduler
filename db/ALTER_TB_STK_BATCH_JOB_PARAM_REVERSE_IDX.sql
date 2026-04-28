-- ORA-12860 (sibling row lock deadlock) 근본 해결
-- 원인: SEQ_STK_BATCH_JOB_PARAM 순차 값이 PK 인덱스 우단 리프 블록에 집중 → 동시 INSERT 시 교착 상태
--
-- 수정 1: PK 인덱스 → REVERSE KEY 변환 (순차 값을 인덱스 전체에 분산)
-- 수정 2: 시퀀스 CACHE 적용 (NOCACHE는 NEXTVAL마다 데이터 딕셔너리 경합 유발)

-- 1. PK 인덱스 REVERSE KEY 변환 (온라인, 락 없음)
ALTER INDEX PK_STK_BATCH_JOB_PARAM REBUILD REVERSE ONLINE;

-- 2. 시퀀스 캐시 설정 (기본 20개 선할당)
ALTER SEQUENCE SEQ_STK_BATCH_JOB_PARAM CACHE 20;

