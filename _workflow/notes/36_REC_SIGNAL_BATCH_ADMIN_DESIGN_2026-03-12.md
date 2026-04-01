# 36. RecSignal 공용 배치관리 설계서 (2026-03-12)

## 1) 목적
- RecSignal 배치 운영을 화면에서 제어할 수 있는 전용 관리 체계를 구축한다.
- 기존 `BatchJobCtrl`과 완전히 분리하고, 향후 타 배치도 재사용 가능한 공용 구조로 설계한다.
- 스케줄/파라미터/실행제어를 DB 기반으로 구성해 소스 하드코딩을 제거한다.

## 2) 범위와 비범위
### 범위
- RecSignal 기반 배치관리 화면 신규 생성
- 공용 배치관리 모델(작업정의/스케줄/파라미터/런타임) 설계
- 실행 로그 조회, 오류 메시지 표시, 즉시실행/정지요청
- 화면 CRUD(추가/수정/삭제)

### 비범위
- 기존 `BatchJobCtrl` 즉시 이관
- 기존 RecSignal 배치 로직 전면 교체(점진 전환)

## 3) 핵심 요구사항 반영
1. RecSignal 기반 화면: `stock/batchAdmin` 신규 화면/엔드포인트로 제공
2. BatchJobCtrl와 별개: URL/Controller/DAO/테이블 분리
3. 하드코딩 제거: `TASK_KEY + DB 스케줄 + DB 파라미터` 구조 적용

## 4) 리스크 및 대응
| 리스크 | 대응 |
| --- | --- |
| 기존 `RecSignalMarketScheduler`와 중복 실행 | 전환 단계에서 feature flag로 신규/구방식 단일 활성화 |
| 잘못된 스케줄 입력(CRON/INTERVAL) | 저장 시 표현식 검증 + 다음 실행시간 미리보기 검증 |
| 실행 중 삭제/핫수정으로 인한 불일치 | `RUNNING_YN='Y'` 시 삭제 금지, 수정 제한 |
| 고아락(비정상 종료) | `heartbeat + stale timeout` 자동 복구 |
| 파라미터 타입 오류 | `PARAM_TYPE` 기반 서버 검증 |
| 장애 원인 추적 부족 | `TB_BATCH_EXEC_LOG`, `TB_BATCH_EXEC_ITEM_LOG` 연계 노출 |
| 무권한 조작 | 관리자 권한 체크 + 조작 감사 로그 |
| 시간대 혼선 | `TIMEZONE` 컬럼 필수, 계산/표시 동일 기준 |

## 5) 아키텍처
```text
BatchAdminController
  -> BatchAdminService
    -> BatchSchedulerEngine (1분 tick)
      -> JobRepository (JOB_DEF/SCHEDULE/PARAM/RUNTIME)
      -> TaskRegistry (TASK_KEY -> Bean)
      -> RecSignalBatchTask (초기 구현)
        -> RecSignalService.runBatch
      -> BatchExecLogDao / BatchExecItemLogDao
```

## 6) 데이터 모델(DDL)
```sql
CREATE TABLE TB_STK_BATCH_TASK_DEF (
    TASK_KEY            VARCHAR2(100)   NOT NULL,
    TASK_NAME           VARCHAR2(200)   NOT NULL,
    BEAN_NAME           VARCHAR2(200)   NOT NULL,
    ENABLED_YN          CHAR(1)         DEFAULT 'Y' NOT NULL,
    PARAM_SCHEMA_JSON   CLOB,
    DESCRIPTION         VARCHAR2(1000),
    REG_DT              DATE            DEFAULT SYSDATE NOT NULL,
    MOD_DT              DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_STK_BATCH_TASK_DEF PRIMARY KEY (TASK_KEY),
    CONSTRAINT CK_STK_BATCH_TASK_DEF_YN CHECK (ENABLED_YN IN ('Y','N'))
);

CREATE TABLE TB_STK_BATCH_JOB_DEF (
    JOB_ID               VARCHAR2(100)   NOT NULL,
    JOB_NAME             VARCHAR2(200)   NOT NULL,
    TASK_KEY             VARCHAR2(100)   NOT NULL,
    ENABLED_YN           CHAR(1)         DEFAULT 'Y' NOT NULL,
    TIMEZONE             VARCHAR2(60)    DEFAULT 'Asia/Seoul' NOT NULL,
    MAX_RUNTIME_SEC      NUMBER(10)      DEFAULT 7200 NOT NULL,
    ALLOW_MANUAL_RUN_YN  CHAR(1)         DEFAULT 'Y' NOT NULL,
    RUN_POLICY           VARCHAR2(20)    DEFAULT 'SINGLETON' NOT NULL,
    DESCRIPTION          VARCHAR2(1000),
    REG_DT               DATE            DEFAULT SYSDATE NOT NULL,
    MOD_DT               DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_STK_BATCH_JOB_DEF PRIMARY KEY (JOB_ID),
    CONSTRAINT FK_STK_BATCH_JOB_DEF_TASK FOREIGN KEY (TASK_KEY)
        REFERENCES TB_STK_BATCH_TASK_DEF (TASK_KEY),
    CONSTRAINT CK_STK_BATCH_JOB_DEF_YN1 CHECK (ENABLED_YN IN ('Y','N')),
    CONSTRAINT CK_STK_BATCH_JOB_DEF_YN2 CHECK (ALLOW_MANUAL_RUN_YN IN ('Y','N')),
    CONSTRAINT CK_STK_BATCH_JOB_DEF_POLICY CHECK (RUN_POLICY IN ('SINGLETON'))
);

CREATE TABLE TB_STK_BATCH_JOB_SCHEDULE (
    SCHEDULE_ID          NUMBER(18)      NOT NULL,
    JOB_ID               VARCHAR2(100)   NOT NULL,
    SCHEDULE_TYPE        VARCHAR2(20)    NOT NULL,
    CRON_EXPR            VARCHAR2(120),
    INTERVAL_SEC         NUMBER(10),
    WINDOW_START_HH24MI  VARCHAR2(5),
    WINDOW_END_HH24MI    VARCHAR2(5),
    MISFIRE_POLICY       VARCHAR2(20)    DEFAULT 'SKIP' NOT NULL,
    ENABLED_YN           CHAR(1)         DEFAULT 'Y' NOT NULL,
    NEXT_RUN_AT          DATE,
    LAST_EVAL_AT         DATE,
    REG_DT               DATE            DEFAULT SYSDATE NOT NULL,
    MOD_DT               DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_STK_BATCH_JOB_SCHEDULE PRIMARY KEY (SCHEDULE_ID),
    CONSTRAINT FK_STK_BATCH_JOB_SCH_JOB FOREIGN KEY (JOB_ID)
        REFERENCES TB_STK_BATCH_JOB_DEF (JOB_ID) ON DELETE CASCADE,
    CONSTRAINT CK_STK_BATCH_SCH_TYPE CHECK (SCHEDULE_TYPE IN ('CRON','INTERVAL')),
    CONSTRAINT CK_STK_BATCH_SCH_YN CHECK (ENABLED_YN IN ('Y','N')),
    CONSTRAINT CK_STK_BATCH_SCH_MISFIRE CHECK (MISFIRE_POLICY IN ('SKIP','RUN_ONCE')),
    CONSTRAINT CK_STK_BATCH_SCH_RULE CHECK (
        (SCHEDULE_TYPE = 'CRON' AND CRON_EXPR IS NOT NULL AND INTERVAL_SEC IS NULL)
        OR
        (SCHEDULE_TYPE = 'INTERVAL' AND INTERVAL_SEC IS NOT NULL AND CRON_EXPR IS NULL)
    )
);

CREATE TABLE TB_STK_BATCH_JOB_PARAM (
    PARAM_ID          NUMBER(18)      NOT NULL,
    JOB_ID            VARCHAR2(100)   NOT NULL,
    PARAM_KEY         VARCHAR2(100)   NOT NULL,
    PARAM_VALUE       CLOB,
    PARAM_TYPE        VARCHAR2(20)    DEFAULT 'STRING' NOT NULL,
    REQUIRED_YN       CHAR(1)         DEFAULT 'N' NOT NULL,
    MASKED_YN         CHAR(1)         DEFAULT 'N' NOT NULL,
    REG_DT            DATE            DEFAULT SYSDATE NOT NULL,
    MOD_DT            DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_STK_BATCH_JOB_PARAM PRIMARY KEY (PARAM_ID),
    CONSTRAINT UK_STK_BATCH_JOB_PARAM UNIQUE (JOB_ID, PARAM_KEY),
    CONSTRAINT FK_STK_BATCH_PARAM_JOB FOREIGN KEY (JOB_ID)
        REFERENCES TB_STK_BATCH_JOB_DEF (JOB_ID) ON DELETE CASCADE,
    CONSTRAINT CK_STK_BATCH_PARAM_TYPE CHECK (PARAM_TYPE IN ('STRING','NUMBER','BOOLEAN','DATE','JSON')),
    CONSTRAINT CK_STK_BATCH_PARAM_YN1 CHECK (REQUIRED_YN IN ('Y','N')),
    CONSTRAINT CK_STK_BATCH_PARAM_YN2 CHECK (MASKED_YN IN ('Y','N'))
);

CREATE TABLE TB_STK_BATCH_JOB_RUNTIME (
    JOB_ID             VARCHAR2(100)   NOT NULL,
    RUNNING_YN         CHAR(1)         DEFAULT 'N' NOT NULL,
    LOCK_OWNER         VARCHAR2(200),
    LOCK_AT            DATE,
    LAST_HEARTBEAT_AT  DATE,
    STOP_REQUEST_YN    CHAR(1)         DEFAULT 'N' NOT NULL,
    CURRENT_EXEC_ID    VARCHAR2(36),
    MOD_DT             DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_STK_BATCH_JOB_RUNTIME PRIMARY KEY (JOB_ID),
    CONSTRAINT FK_STK_BATCH_RUNTIME_JOB FOREIGN KEY (JOB_ID)
        REFERENCES TB_STK_BATCH_JOB_DEF (JOB_ID) ON DELETE CASCADE,
    CONSTRAINT CK_STK_BATCH_RUNTIME_YN1 CHECK (RUNNING_YN IN ('Y','N')),
    CONSTRAINT CK_STK_BATCH_RUNTIME_YN2 CHECK (STOP_REQUEST_YN IN ('Y','N'))
);
```

## 7) API 명세
### 화면/조회
- `GET /stock/batchAdmin/view.do` : 배치 관리 JSP
- `GET /stock/batchAdmin/taskCatalog.do` : TASK 목록
- `GET /stock/batchAdmin/jobList.do` : 배치 목록
- `GET /stock/batchAdmin/jobDetail.do` : 배치 상세(기본/스케줄/파라미터/런타임)
- `GET /stock/batchAdmin/jobLogList.do` : 실행 로그
- `GET /stock/batchAdmin/jobLogItemFailList.do` : 실패 종목 로그

### 관리(CRUD + 운영 제어)
- `POST /stock/batchAdmin/jobCreate.do` : 배치 추가
- `POST /stock/batchAdmin/jobUpdate.do` : 배치 수정
- `POST /stock/batchAdmin/jobDelete.do` : 배치 삭제
- `POST /stock/batchAdmin/jobRunNow.do` : 즉시 실행
- `POST /stock/batchAdmin/jobStop.do` : 정지 요청

### 요청 예시
```json
{
  "job_id": "REC_SIGNAL_KR_PRIMARY",
  "job_name": "RecSignal KR Primary",
  "task_key": "REC_SIGNAL_RUN",
  "enabled_yn": "Y",
  "timezone": "Asia/Seoul",
  "max_runtime_sec": 7200,
  "allow_manual_run_yn": "Y",
  "description": "KR 종가 배치",
  "schedule_json": {
    "scheduleType": "CRON",
    "cronExpr": "0 0 2 ? * TUE-SAT",
    "misfirePolicy": "SKIP",
    "enabledYn": "Y"
  },
  "params_json": [
    {"paramKey": "marketGroup", "paramValue": "KR", "paramType": "STRING"},
    {"paramKey": "retryOnly", "paramValue": "N", "paramType": "BOOLEAN"},
    {"paramKey": "days", "paramValue": "400", "paramType": "NUMBER"},
    {"paramKey": "requestIntervalMs", "paramValue": "1000", "paramType": "NUMBER"}
  ]
}
```

## 8) JSP 화면 와이어프레임
```text
[조회영역]
- JOB ID / TASK / ENABLE / RUNNING / RESULT 필터
- 버튼: 조회, 새로고침, 등록

[목록그리드]
- JOB ID | JOB NAME | TASK | ENABLE | RUNNING | NEXT RUN | LAST START | LAST END | RESULT | ERROR | ACTION
- ACTION: 설정, 실행, 정지, 삭제

[설정 모달]
- 기본 탭: JOB ID, JOB NAME, TASK, ENABLE, TIMEZONE, MAX_RUNTIME_SEC
- 스케줄 탭: SCHEDULE_TYPE(CRON/INTERVAL), CRON_EXPR, INTERVAL_SEC, MISFIRE_POLICY
- 파라미터 탭: KEY/VALUE/TYPE/REQUIRED/MASKED 행 편집
- 버튼: 저장, 삭제, 닫기

[로그 패널]
- EXEC_ID | BASE_DT | STATUS | TOTAL | SUCCESS | FAIL | START | END | ERROR
- 실패 건 상세 팝업 연동
```

## 9) 전환 시나리오
1. 신규 Batch Admin 모듈과 테이블을 먼저 배포
2. RecSignal Job 4개(KR/US primary/retry) seed 등록
3. 신규 방식 shadow-run 검증
4. 운영 스위치 전환 후 기존 `RecSignalMarketScheduler` 비활성화
5. 안정화 후 타 배치 확장

## 10) 완료 기준(Definition of Done)
- 화면에서 배치 `추가/수정/삭제` 가능
- 즉시 실행/정지 요청 가능
- 마지막 상태/에러 메시지 화면 표시
- 하드코딩 없이 DB 설정 변경만으로 스케줄/파라미터 조정 가능
- 중복 실행/고아락/입력 검증/권한 통제가 동작

## 11) 구현 반영 메모 (2026-03-12)
- `StockBatchAdminController`, `StockBatchAdminService`, `StockBatchAdminDao/Mapper` 1차 구현 완료
- `webapp/appone/jsp/stock/batchAdmin.jsp` 화면 추가 (CRUD/Run/Stop/로그 조회)
- `db/DDL_TB_STK_BATCH_ADMIN.sql` 추가
- `stockService.xml`, `oracle_mybatis-config.xml`에 신규 빈/매퍼 연결 반영
