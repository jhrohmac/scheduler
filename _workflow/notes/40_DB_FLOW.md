# 3. DB 단위 플로우

## 3-1 DB 테이블 리스트(핵심)
- `TB_S_BATCH_JOB_CTRL` : 기존 배치 제어/락/완료
- `TB_S_INTEREST_STOCK` : 관심종목(레짐 자산 포함)
- `TB_S_STOCK_ANALYSIS` : 일봉 분석 스냅샷
- `TB_S_STOCK_30MINANALYSIS` : 30분 분석 스냅샷
- `TB_S_SIGNAL_EVENT` : 신호 이벤트 이력
- `TB_S_POSITION` : 보유 포지션 헤더
- `TB_S_POSITION_TXN` : 보유종목 거래 이력
- `TB_S_POSITION_EVENT` : 보유종목 이벤트 이력
- `TB_S_RECO_PICK` : 추천 저장 이력
- `TB_S_RECO_PICK_DAILY` : 추천 일별 추적 스냅샷
- `TB_S_RECO_PICK_EVAL` : 추천 평가 결과
- `VW_S_RECO_PICK_STATS` : 추천 성과/확률 집계 뷰
- `TB_STK_MASTER` : 추천신호 대상 종목 정본
- `TB_TRADE_CALENDAR` : 추천신호 거래일 정본
- `TB_REC_SIGNAL` : 추천신호 결과 저장
- `TB_BATCH_EXEC_LOG` : 추천신호 배치 실행 헤더 로그
- `TB_BATCH_EXEC_ITEM_LOG` : 추천신호 배치 종목 로그
- `TB_STK_BATCH_TASK_DEF` : Batch Admin Task catalog
- `TB_STK_BATCH_JOB_DEF` : Batch Admin Job 정의
- `TB_STK_BATCH_JOB_SCHEDULE` : Batch Admin 스케줄 정의
- `TB_STK_BATCH_JOB_PARAM` : Batch Admin Job 파라미터
- `TB_STK_BATCH_JOB_RUNTIME` : Batch Admin 런타임/락 상태

## WF-3-1A 추천신호 저장구조/배치운영 스키마
- 입력 정본, 결과 저장, 운영 로그, Batch Admin 테이블을 하위 카드로 분리해 관리한다.
- 현재 DDL 소스:
  - `db/DDL_TB_STK_MASTER.sql`
  - `db/MIGRATE_TB_STK_MASTER_MARKET_SEGMENT.sql`
  - `db/DDL_TB_TRADE_CALENDAR.sql`
  - `db/DDL_TB_REC_SIGNAL.sql`
  - `db/DDL_TB_BATCH_EXEC_LOG.sql`
  - `db/DDL_TB_BATCH_EXEC_ITEM_LOG.sql`
  - `db/DDL_TB_STK_BATCH_ADMIN.sql`
  - `db/MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql`
  - `db/MIGRATE_TB_REC_SIGNAL_LIST_PARTITION.sql`
  - `db/DDL_DROP_TB_STK_PRICE_TABLES.sql`
  - `db/tools/DbRecSignalDdlRunner.java`
  - `db/apply_rec_signal_ddl.sh`

### WF-3-1A-1 입력 정본 테이블 묶음

#### WF-3-1A-1A `TB_STK_MASTER`
- 추천신호 배치 대상 종목 정본
- `MKT_CD` 로 상장시장을 구분한다.
- `DOW_MEMBER_YN` 으로 다우 편입 여부를 보존한다.

#### WF-3-1A-1B `TB_TRADE_CALENDAR`
- 거래일 / 휴장일 / 기준일 판단 정본
- `TradeDateService` 가 조회 기준일 보정 시 참조한다.

### WF-3-1A-2 결과 저장 스키마
- `TB_REC_SIGNAL`
- `BASE_DT + MKT_CD + STK_CD` PK
- 조회 런타임과 배치 런타임이 공통으로 참조하는 결과 저장소
- `TB_STK_MASTER` 조인으로 `listingMarket`, `dowMemberYn` 메타를 확장한다.
- 파티션 전환 설계안:
  - `MKT_CD` 기준 `LIST PARTITION`
  - 권장 파티션: `P_KR`, `P_US`, `P_ETC(DEFAULT)`
  - full refresh primary 는 시장 partition 만 `TRUNCATE PARTITION`
  - retry / 부분 실행은 기존 `MERGE` 유지
  - 상세 설계: `54_REC_SIGNAL_PARTITION_DESIGN_2026-04-06.md`

### WF-3-1A-3 배치 로그 / Batch Admin 테이블 묶음

#### WF-3-1A-3A `TB_BATCH_EXEC_LOG`
- 추천신호 배치 실행 헤더 로그
- `execId`, `batchId`, `status`, `count` 집계를 저장한다.

#### WF-3-1A-3B `TB_BATCH_EXEC_ITEM_LOG`
- 종목 단위 실행/실패 로그
- `retryOnly` 재대상 구성 기준 테이블
- `ERROR_MSG` 2000 보정 migration 이 반영되어 있다.

#### WF-3-1A-3C `TB_STK_BATCH_TASK_DEF`
- Batch Admin 에서 실행 가능한 Task catalog
- `task_key`, `bean_name`, `param_schema_json` 을 관리한다.

#### WF-3-1A-3D `TB_STK_BATCH_JOB_DEF`
- Batch Admin Job 정의
- `task_key`, 활성화 여부, 최대 실행시간, 수동실행 허용 여부를 관리한다.

#### WF-3-1A-3E `TB_STK_BATCH_JOB_SCHEDULE`
- CRON / INTERVAL 스케줄 정의
- `next_run_at`, `misfire_policy` 를 관리한다.

#### WF-3-1A-3F `TB_STK_BATCH_JOB_PARAM`
- Job 파라미터 저장
- `key / value / type / required / masked` 속성을 관리한다.

#### WF-3-1A-3G `TB_STK_BATCH_JOB_RUNTIME`
- 런타임 락/실행 상태 저장
- `RUNNING_YN`, `LOCK_AT`, `LAST_HEARTBEAT_AT`, `STOP_REQUEST_YN`, `CURRENT_EXEC_ID` 를 관리한다.
- stale lock 회수와 heartbeat 기준 판정의 정본이다.

## 3-1B 추천신호 v1.10 정본 범위(확정)
- 정본(Source of Truth)
  - `TB_STK_MASTER`
  - `TB_TRADE_CALENDAR`
- 결과/로그
  - `TB_REC_SIGNAL`
  - `TB_BATCH_EXEC_LOG`
  - `TB_BATCH_EXEC_ITEM_LOG`
- 운영 제어
  - `TB_STK_BATCH_TASK_DEF`
  - `TB_STK_BATCH_JOB_DEF`
  - `TB_STK_BATCH_JOB_SCHEDULE`
  - `TB_STK_BATCH_JOB_PARAM`
  - `TB_STK_BATCH_JOB_RUNTIME`
- legacy 분리
  - `TB_S_INTEREST_STOCK`, `TB_S_STOCK_ANALYSIS`, `TB_S_BATCH_JOB_CTRL` 는 기존 추천 체계 유지용
  - v1.10 추천신호 시스템의 계산 정본으로 사용하지 않음

## 3-2 로직에 필요한 테이블(기능별)
- 추천: `TB_S_STOCK_ANALYSIS` + `TB_S_STOCK_30MINANALYSIS` + `TB_S_SIGNAL_EVENT`
- 레짐: `TB_S_STOCK_ANALYSIS`(지수 자산)
- 차트: 분석 테이블 + 이벤트 테이블(마커)
- 추천신호 조회/배치: `TB_STK_MASTER` + `TB_TRADE_CALENDAR` + `TB_REC_SIGNAL`
- 공용 배치 운영: `TB_STK_BATCH_*` 5종 + `TB_BATCH_EXEC_*` 2종

## 3-3 기존 테이블 컬럼 추가/커스텀(원칙)
- 운영 리스크 최소화를 위해: 먼저 MERGE/로직으로 대응 → 안정화 후 제약/인덱스 강화
- 시크릿 파일(`kis.properties`)은 절대 커밋 금지

## 3-3A 추천신호 배치 제어 원칙(확정)
- v1.10 추천신호 시스템은 `TB_S_BATCH_JOB_CTRL` 를 재사용하지 않는다.
- 실행 이력은 `TB_BATCH_EXEC_LOG`, 종목 단위 실패 이력은 `TB_BATCH_EXEC_ITEM_LOG` 로 관리한다.
- 공용 Batch Admin 운영 제어는 `TB_STK_BATCH_TASK_DEF`, `TB_STK_BATCH_JOB_DEF`, `TB_STK_BATCH_JOB_SCHEDULE`, `TB_STK_BATCH_JOB_PARAM`, `TB_STK_BATCH_JOB_RUNTIME` 로 관리한다.
- 운영 스케줄은 `RecSignalDailyBatch` 고정 크론과 `StockBatchAdminController.schedulerTick()` 기반 공용 Batch Admin 이 공존한다.
- KR 02:00 / 05:00, US 16:00 / 20:00 스케줄을 기본 운용 기준으로 둔다.
- 수동 재실행 API와 운영 UI는 이미 `StockBatchAdminController`, `batchAdmin.jsp`, `batchAdmin.js` 로 연결돼 있다.

## 3-5 사용자 선택(픽) 추적 테이블

### 3-9A `TB_S_RECO_PICK`
- 사용자가 저장한 추천 종목 헤더
- `saveToWatchlist` / 매수 연계의 기준 테이블

### 3-9B `TB_S_RECO_PICK_DAILY`
- 추천 종목 일별 추적 스냅샷
- 5D / 20D / 60D 평가 입력 원천

### 3-9C `TB_S_RECO_PICK_EVAL`
- 추천 종목 평가 결과
- 수익률 / MFE / MAE 저장

### 3-9D `VW_S_RECO_PICK_STATS`
- 조건별 성공확률 / 평균 MFE / 평균 MAE 집계 뷰
- UI/확률 서비스에서 읽기 전용으로 사용

## 3-8 보유종목/물타기 관리

### 3-8A `TB_S_POSITION`
- 보유 포지션 헤더
- 수량 / 평단 / 상태 / 추천 연계 필드 저장

### 3-8B `TB_S_POSITION_TXN`
- `ADD`, `DELETE`, `AVERAGE_DOWN` 거래 이력
- before/after 수량·평단 추적

### 3-8C `TB_S_POSITION_EVENT`
- 상태전이 / 리스크 이벤트 로그
- 이벤트가 비어 있으면 UI 에서 거래이력 fallback 으로 참조 가능

## (추가) 3-6 AI 예측 결과/로그(예정)
- `TB_S_AI_PREDICT_REQ`
  - `req_id`, `stock_code`, `horizon`, `risk_mode`, `req_at`, `req_user`
- `TB_S_AI_PREDICT_RESULT`
  - `req_id`, `prob_up`, `prob_neutral`, `prob_down`, `confidence`, `summary`
  - `top_reasons`, `top_risks`, `as_of_time`
- `TB_S_AI_PREDICT_FEATURE_SNAP` (옵션)
  - `req_id` 기준 입력 피처 스냅샷

## (추가) 3-7 실시간 신호/차트 이벤트(예정)
- `TB_S_RT_SIGNAL_EVENT`
  - `stock_code`, `signal_type`, `signal_at`, `price_ref`, `source_ws`, `confidence`
  - `event_seq`, `event_time`
- `TB_S_WS_STREAM_LOG` (옵션)
  - ws 수신 품질 / 지연 / 재연결 로그
  - `lag_ms`, `reconnect_count`, `stale_rate`
