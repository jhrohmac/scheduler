# 추천신호 메모리 계산형 DB 적용 절차서

작성일: 2026-03-11  
대상: `scheduler` 추천신호 v1.10

## 1. 목적

추천신호를 `KIS API 메모리 계산 -> TB_REC_SIGNAL 저장` 구조로 전환하기 위해,
운영 DB에 필요한 스키마 변경과 검증 순서를 정리한다.

## 2. 적용 범위

### 유지

- `TB_STK_MASTER`
- `TB_TRADE_CALENDAR`
- `TB_REC_SIGNAL`
- `TB_BATCH_EXEC_LOG`

### 신규

- `TB_BATCH_EXEC_ITEM_LOG`

### 후속 drop 대상

- `TB_STK_DLY_PRICE`
- `TB_STK_MON_PRICE`

## 3. 적용 전 체크

1. 현재 서버가 새 소스로 배포 가능한 상태인지 확인
2. `webapp/WEB-INF/classes` 와 `src` 가 동기화돼 있는지 확인
3. 기존 `TB_REC_SIGNAL` 데이터 보존 필요 여부 확인
4. 운영 시간 중 drop이 가능한지 확인

## 4. 1차 적용

### 4.1 실행 SQL

- 실행 파일
  - [MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql](/Users/jinhyun/projects/scheduler/db/MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql)

### 4.2 실행 내용

- `TB_REC_SIGNAL.MKT_CD` 컬럼 보강
- 기존 `NULL MKT_CD` 값을 `KR`로 일괄 보정
- `TB_REC_SIGNAL` PK를 `(BASE_DT, MKT_CD, STK_CD)` 로 재구성
- `IDX_REC_SIGNAL_YN` 재생성
- `TB_BATCH_EXEC_ITEM_LOG` 생성

### 4.3 실행 예시

```sql
@/Users/jinhyun/projects/scheduler/db/MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql
```

## 5. 1차 적용 후 검증

### 5.1 데이터 확인

```sql
SELECT COUNT(*)
  FROM TB_REC_SIGNAL
 WHERE MKT_CD IS NULL;

SELECT CONSTRAINT_NAME
  FROM USER_CONSTRAINTS
 WHERE TABLE_NAME = 'TB_REC_SIGNAL'
   AND CONSTRAINT_TYPE = 'P';

SELECT INDEX_NAME
  FROM USER_INDEXES
 WHERE TABLE_NAME = 'TB_REC_SIGNAL';

SELECT TABLE_NAME
  FROM USER_TABLES
 WHERE TABLE_NAME = 'TB_BATCH_EXEC_ITEM_LOG';
```

### 5.2 애플리케이션 확인

1. 서버 재기동
2. `/stock/recSignal/list.do?mktCd=KR`
3. `/stock/recSignal/list.do?mktCd=US`
4. `/stock/recSignal/runBatch.do?marketGroup=KR`
5. `/stock/recSignal/runBatch.do?marketGroup=US`

### 5.3 실패 재시도 확인

1. 일부 실패 종목이 생기도록 제한 실행
2. `/stock/recSignal/runBatch.do?marketGroup=KR&retryOnly=Y`
3. `TB_BATCH_EXEC_ITEM_LOG` 에서 `ITEM_STATUS='FAIL'` 대상만 재시도되는지 확인

## 6. 2차 적용

### 6.1 drop 실행 조건

아래가 모두 만족될 때만 drop 한다.

1. 새 배치가 정상 동작
2. KR/US 조회 API 정상 응답
3. 재시도 경로 정상 동작
4. 더 이상 `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` 를 참조하는 서버가 없음

### 6.2 실행 파일

- [DDL_DROP_TB_STK_PRICE_TABLES.sql](/Users/jinhyun/projects/scheduler/db/DDL_DROP_TB_STK_PRICE_TABLES.sql)

### 6.3 실행 예시

```sql
@/Users/jinhyun/projects/scheduler/db/DDL_DROP_TB_STK_PRICE_TABLES.sql
```

## 7. 적용 순서 요약

1. 새 소스 배포
2. `MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql` 실행
3. 서버 재기동
4. KR/US 수동 배치 검증
5. 조회 API / 상세 화면 검증
6. 실패 재시도 검증
7. 문제 없으면 `DDL_DROP_TB_STK_PRICE_TABLES.sql` 실행

## 8. 롤백 기준

아래 상황이면 drop 전에 즉시 중단한다.

1. `TB_REC_SIGNAL` PK 재구성 실패
2. `MKT_CD` NULL 데이터 잔존
3. KR/US batch 결과가 모두 실패
4. 조회 API가 `mktCd` 기준으로 결과를 못 읽음

drop 전 단계라면 rollback은 단순하다.

- 애플리케이션을 이전 build로 되돌림
- `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` 는 아직 남아 있으므로 기존 구조로 복귀 가능

## 9. 관련 파일

- [34_REC_SIGNAL_MEMORY_BATCH_REPORT_2026-03-11.md](/Users/jinhyun/projects/scheduler/_workflow/notes/34_REC_SIGNAL_MEMORY_BATCH_REPORT_2026-03-11.md)
- [MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql](/Users/jinhyun/projects/scheduler/db/MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql)
- [DDL_DROP_TB_STK_PRICE_TABLES.sql](/Users/jinhyun/projects/scheduler/db/DDL_DROP_TB_STK_PRICE_TABLES.sql)
