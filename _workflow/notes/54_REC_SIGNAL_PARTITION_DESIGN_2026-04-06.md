# 추천신호 `TB_REC_SIGNAL` 파티션 전환 설계서

기준일: 2026-04-06  
프로젝트: `scheduler`  
대상 스키마: `SCHHOME`

## 1. 문서 목적

`TB_REC_SIGNAL` 은 현재 row 수가 매우 작아도 세그먼트가 크게 남는 문제가 있었다.  
원인은 시장 단위 full refresh 시 `DELETE` 후 재적재하는 구조이고, Oracle heap table 특성상 세그먼트가 자동으로 줄지 않기 때문이다.

이 문서는 `TB_REC_SIGNAL` 을 파티션 테이블로 전환해

1. 시장별 refresh 시 공간 회수를 쉽게 만들고
2. KR/US 데이터를 서로 간섭 없이 운영하고
3. `TRUNCATE PARTITION` 기반 운영이 가능한 구조를 정의한다.

## 2. 현재 확인된 사실

### 2-1. 현재 문제

- 현재 배치는 full refresh 시 시장 단위로 기존 데이터를 먼저 삭제한 뒤 다시 적재한다.
- 이 과정은 데이터 정합성은 유지하지만 세그먼트 공간은 반납하지 않는다.
- 실제 운영 확인 결과 `TB_REC_SIGNAL` 은 row 수 수백 건이어도 1GB 이상으로 커질 수 있었다.

### 2-2. 무료 티어 가능 여부 확인

현재 사용 중인 Oracle Cloud 무료 티어 Autonomous DB 에서 직접 아래를 검증했다.

- `LIST PARTITION` 테이블 생성 성공
- `USER_TABLES.PARTITIONED = YES` 확인
- `TRUNCATE PARTITION ... UPDATE INDEXES` 실행 성공
- 파티션 truncate 후 다른 파티션 데이터가 유지되는 것 확인

따라서 현재 환경에서는 파티션 사용이 가능하다고 판단한다.

## 3. 설계 결정

### 3-1. 파티션 개수

권장 파티션 개수는 **3개**다.

1. `P_KR`
   - `MKT_CD = 'KR'`
   - 국내 시장 추천신호 저장
2. `P_US`
   - `MKT_CD = 'US'`
   - 미국 시장 추천신호 저장
3. `P_ETC`
   - `DEFAULT`
   - 예기치 않은 `MKT_CD` 유입 방지용 안전 파티션

### 3-2. 왜 3개로 가는가

- 실운영 대상 시장은 현재 `KR`, `US` 두 개뿐이다.
- 하지만 DB 레벨에서 `MKT_CD` 체크 제약이 강하지 않은 상태라, 잘못된 값이 들어오면 2개 파티션만 둘 경우 insert 실패가 발생한다.
- `DEFAULT` 파티션을 두면 운영 장애 대신 이상 데이터 감지로 처리할 수 있다.
- `P_ETC` 는 정상 운영 시 항상 비어 있어야 하며, row 가 생기면 운영 경고 대상으로 본다.

## 4. 목표 물리 구조

### 4-1. 파티션 키

- 파티션 키: `MKT_CD`
- 파티션 방식: `LIST PARTITION`
- 이유:
  - 현재 배치 refresh 범위가 `KR`, `US` 시장 단위다.
  - full refresh 시 필요한 작업도 시장 단위 partition truncate 와 정확히 맞아떨어진다.

### 4-2. 권장 DDL 방향

```sql
CREATE TABLE TB_REC_SIGNAL (
    BASE_DT          DATE           NOT NULL,
    MKT_CD           VARCHAR2(10)   NOT NULL,
    STK_CD           VARCHAR2(20)   NOT NULL,
    STK_NM           VARCHAR2(100),
    CUR_PRICE        NUMBER(18,4),
    MON_OPEN_PRICE   NUMBER(18,4),
    MA5              NUMBER(18,4),
    MA20             NUMBER(18,4),
    MA60             NUMBER(18,4),
    MA120            NUMBER(18,4),
    MA240            NUMBER(18,4),
    GOLDEN_YN        CHAR(1),
    MON_UP_YN        CHAR(1),
    MON_CHG_RATE     NUMBER(18,4),
    TREND_STRENGTH   NUMBER(18,4),
    AVG_TRD_VAL_20   NUMBER(18,0),
    REC_YN           CHAR(1),
    REC_GRADE        VARCHAR2(1),
    REC_REASON       VARCHAR2(400),
    CREATED_AT       DATE DEFAULT SYSDATE,
    UPDATED_AT       DATE DEFAULT SYSDATE,
    CONSTRAINT PK_REC_SIGNAL PRIMARY KEY (BASE_DT, MKT_CD, STK_CD)
)
PARTITION BY LIST (MKT_CD)
(
    PARTITION P_KR VALUES ('KR'),
    PARTITION P_US VALUES ('US'),
    PARTITION P_ETC VALUES (DEFAULT)
);

CREATE INDEX IDX_REC_SIGNAL_YN
    ON TB_REC_SIGNAL (BASE_DT, MKT_CD, REC_YN)
    LOCAL;
```

### 4-3. 인덱스 방향

- `PK_REC_SIGNAL` 은 partition key `MKT_CD` 를 포함하므로 local unique index 기반 운영이 가능하다.
- `IDX_REC_SIGNAL_YN` 도 `LOCAL` 로 두는 방향을 권장한다.
- 이유:
  - `TRUNCATE PARTITION ... UPDATE INDEXES` 와 궁합이 좋다.
  - 시장별 partition truncate 후 인덱스 유지/복구 범위가 작다.

## 5. 운영 방식

### 5-1. 기본 원칙

- `retryOnly = Y` 인 경우에는 파티션 truncate 를 하지 않는다.
- `stkCd`, `limit` 등 부분 실행도 파티션 truncate 를 하지 않는다.
- 오직 `full refresh + primary run` 조합에서만 시장 partition truncate 를 수행한다.

### 5-2. KR 운영

#### KR Primary

1. KR 대상 종목 전체 계산을 먼저 메모리에서 완료한다.
2. 계산 성공 후 짧은 write window 에서 `P_KR` 만 truncate 한다.
3. 계산 결과를 `P_KR` 에 적재한다.
4. `P_US` 는 전혀 건드리지 않는다.

#### KR Retry

1. 실패 종목만 다시 계산한다.
2. `P_KR` truncate 는 하지 않는다.
3. 실패 종목만 `MERGE` 로 보정한다.

### 5-3. US 운영

US 도 KR 과 동일하게 동작한다.

#### US Primary

1. US 대상 종목 전체 계산 완료
2. `P_US` truncate
3. US 결과 적재
4. `P_KR` 유지

#### US Retry

1. 실패 종목만 재계산
2. truncate 없음
3. `MERGE` 보정만 수행

### 5-4. `P_ETC` 운영

- 정상 경로에서는 항상 row 수가 0 이어야 한다.
- `P_ETC` row 발생은 입력값 오류 또는 신규 시장코드 유입 신호로 본다.
- 운영 점검 SQL:

```sql
SELECT COUNT(*)
  FROM TB_REC_SIGNAL PARTITION (P_ETC);
```

## 6. 장애/실패 시 정책

### 6-1. 최소 수정 기준 운영 정책

최소 수정 방향에서는 staging table 까지 도입하지 않는다.  
대신 아래 순서를 지킨다.

1. 전체 계산 완료
2. 시장 partition truncate
3. 결과 적재

이렇게 하면 계산 단계 실패 시 기존 partition 은 유지된다.

### 6-2. 남는 리스크

- truncate 이후 적재 중간에 실패하면 해당 시장 partition 이 부분 적재 상태가 될 수 있다.
- 이 리스크는 현재 `DELETE` 구조에서도 본질적으로 존재한다.
- 다만 partition 구조에서는 영향 범위가 시장 단위로만 제한된다.

### 6-3. 후속 고도화 가능성

더 안전하게 가려면 이후 단계에서 아래를 검토할 수 있다.

- `TB_REC_SIGNAL_STG` staging table 도입
- 시장별 staging 적재 완료 후 `EXCHANGE PARTITION`
- 또는 배치 성공 시점에만 활성 partition 교체

하지만 이는 최소 수정 범위를 넘기므로 본 설계의 1차 범위에서는 제외한다.

## 7. 애플리케이션 변경 범위

### 7-1. 최소 수정 방향

최소 수정으로는 아래만 바꾼다.

- `RecSignalService.runBatch`
  - full refresh primary 인 경우 `deleteRecSignalByMarket()` 대신 시장 partition truncate 호출
- `RecSignalDao`
  - `truncateRecSignalPartition(mktCd)` 추가
- `RecSignalMapper.xml`
  - partition name 을 직접 외부 입력으로 받지 않고 Java 에서 `KR -> P_KR`, `US -> P_US`, 기타 -> P_ETC` 로 고정 매핑

### 7-2. 조회 로직 영향

- 조회 SQL 은 그대로 `TB_REC_SIGNAL` 을 읽는다.
- list/detail API 는 변경 없이 동작한다.
- `selectLatestBaseDtByMarket()` 도 기존 그대로 유지 가능하다.

## 8. 이행 절차

### 8-1. 전환 방식

관련 파일:

- `db/DDL_TB_REC_SIGNAL.sql`
- `db/MIGRATE_TB_REC_SIGNAL_LIST_PARTITION.sql`

기존 heap table 을 바로 partition table 로 바꾸기보다, 운영 점검이 쉬운 방식으로 새 테이블 생성 후 교체를 권장한다.

권장 절차:

1. `TB_REC_SIGNAL_NEW` 를 partition table 로 생성
2. 기존 `TB_REC_SIGNAL` 데이터 복사
3. row count / base date / market count 검증
4. maintenance window 에서 old/new rename 교체
5. 인덱스/제약/통계 재확인
6. KR, US 수동 배치 점검

### 8-2. 검증 SQL

```sql
SELECT PARTITIONED
  FROM USER_TABLES
 WHERE TABLE_NAME = 'TB_REC_SIGNAL';

SELECT PARTITION_NAME, NUM_ROWS
  FROM USER_TAB_PARTITIONS
 WHERE TABLE_NAME = 'TB_REC_SIGNAL'
 ORDER BY PARTITION_POSITION;

SELECT TO_CHAR(BASE_DT, 'YYYY-MM-DD') AS BASE_DT,
       MKT_CD,
       COUNT(*)
  FROM TB_REC_SIGNAL
 GROUP BY BASE_DT, MKT_CD
 ORDER BY BASE_DT DESC, MKT_CD;
```

## 9. 최종 권장안

### 9-1. 설계 결론

- 파티션 수: **3개**
- 파티션 방식: `LIST PARTITION BY MKT_CD`
- 파티션명:
  - `P_KR`
  - `P_US`
  - `P_ETC`
- full refresh 운영:
  - KR primary: `TRUNCATE PARTITION P_KR`
  - US primary: `TRUNCATE PARTITION P_US`
- retry / 부분실행:
  - truncate 없이 `MERGE`

### 9-2. 기대 효과

- KR/US 상호 간섭 제거
- 시장 단위 공간 회수 가능
- `ALTER TABLE ... MOVE` 같은 수동 reclaim 작업 빈도 감소
- 무료 티어 환경에서도 운영 가능한 구조 확보

## 10. 승인 필요 항목

다음 3가지를 승인받으면 구현으로 넘어갈 수 있다.

1. `TB_REC_SIGNAL` 을 `MKT_CD` 기준 3개 list partition 으로 전환할지
2. `P_ETC` default partition 을 안전장치로 둘지
3. 1차 구현은 최소 수정 원칙에 따라 staging 없이 `계산 완료 후 partition truncate` 로 갈지
