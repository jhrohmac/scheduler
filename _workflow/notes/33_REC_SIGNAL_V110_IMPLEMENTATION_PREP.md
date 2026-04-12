# 추천신호 v1.10 실제 반영 준비

## 목적

추천종목 선별 시스템 설계서 v1.10 기준으로 scheduler 프로젝트에 실제 반영하기 전에,
추가되는 프론트, 백엔드, DB, 캔버스 대상을 정리하고 기존 로직 영향 범위를 분리한다.

## 전제

- 기존 추천 로직은 유지한다.
- 신규 기능은 별도 vertical slice 로 추가한다.
- 기존 `finance` 추천 소스는 참고만 하고 직접 재사용하지 않는다.
- 시세 및 차트 원천 데이터는 프로젝트 전역 지침에 따라 KIS API 기반으로 수집한다.

## 확정 결정

### 1. DB 정본 범위 확정

- v1.10 신규 시스템의 정본(Source of Truth)은 아래 2개로 고정한다.
  - `TB_STK_MASTER`
  - `TB_TRADE_CALENDAR`
- 일봉 원천은 KIS API 응답을 메모리에서 계산하고 DB에는 적재하지 않는다.
- `TB_REC_SIGNAL` 은 EOD 추천 결과 스냅샷 테이블이다.
- `TB_BATCH_EXEC_LOG` 는 배치 실행 헤더 로그 테이블이다.
- `TB_BATCH_EXEC_ITEM_LOG` 는 실패 종목 재시도 기준 테이블이다.
- `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` 는 운영 경로에서 제거하고 drop 대상으로 관리한다.
- 기존 `TB_S_INTEREST_STOCK`, `TB_S_STOCK_ANALYSIS`, `TB_S_BATCH_JOB_CTRL` 는 legacy 체계로 유지하며,
  v1.10 추천신호 시스템의 계산 정본으로 사용하지 않는다.

### 2. 배치 제어 재사용 여부 확정

- v1.10 추천신호 시스템은 기존 `BatchJobCtrlController`, `BatchJobCtrlDaoImpl`,
  `TB_S_BATCH_JOB_CTRL` 를 재사용하지 않는다.
- 신규 배치는 `RecSignalDailyBatch` 독립 스케줄로 구현한다.
- 실행 이력은 `TB_BATCH_EXEC_LOG`, 실패건 재실행 추적은 `TB_BATCH_EXEC_ITEM_LOG` 로 처리한다.
- 수동 실행 API 또는 운영 제어 UI가 필요하면, v1.10 2차 작업에서 별도 추가한다.

## 변경 사항

- 추천신호 v1.10 전용 UI 추가
- 추천신호 v1.10 전용 조회 API 추가
- 추천신호 v1.10 전용 EOD 배치 추가
- 추천신호 v1.10 전용 DB 스키마 추가
- 추천신호 v1.10 관련 캔버스/노트 동기화 추가

## 변경 대상

- 캔버스
  - `WF-1-2A` : 추천신호 리스트/상세 UI
  - `WF-2-3A` : 추천신호 조회 API
  - `WF-2-3B` : 추천신호 EOD 배치
  - `WF-3-1A` : 추천신호 DB 스키마

- 프로젝트소스
  - 프론트
    - `/Users/jinhyun/projects/scheduler/webapp/appone/jsp/stock/recSignalList.jsp`
    - `/Users/jinhyun/projects/scheduler/webapp/appone/jsp/stock/recSignalList.css`
    - `/Users/jinhyun/projects/scheduler/webapp/appone/jsp/stock/recSignalList.js`
    - `/Users/jinhyun/projects/scheduler/webapp/appone/jsp/stock/recSignalDetail.jsp`
    - `/Users/jinhyun/projects/scheduler/webapp/appone/jsp/stock/recSignalDetail.js`
  - 백엔드
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/web/RecSignalController.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/batch/RecSignalBatch.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/batch/RecSignalDailyBatch.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/batch/RecSignalMarketScheduler.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/service/RecSignalService.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/service/KisDlyPriceSyncService.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/service/MaCalculateService.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/service/TradeDateService.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dao/BatchExecItemLogDao.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dao/RecSignalDao.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dao/StkMasterDao.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dao/impl/BatchExecItemLogDaoImpl.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dao/impl/RecSignalDaoImpl.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dao/impl/StkMasterDaoImpl.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dto/RecSignalDto.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dto/DlyPriceDto.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/dto/MaResultDto.java`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/sql/oracle/StkMasterMapper.xml`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/sql/oracle/TradeDateMapper.xml`
    - `/Users/jinhyun/projects/scheduler/src/com/scheduler/stock/sql/oracle/BatchExecLogMapper.xml`
    - `/Users/jinhyun/projects/scheduler/webapp/WEB-INF/resources/service/stockService.xml`
    - `/Users/jinhyun/projects/scheduler/webapp/WEB-INF/resources/servlet/scheduler-servlet.xml`
    - `/Users/jinhyun/projects/scheduler/webapp/WEB-INF/resources/config/mybatis/oracle/oracle_mybatis-config.xml`
  - DB
    - `/Users/jinhyun/projects/scheduler/db/DDL_TB_STK_MASTER.sql`
    - `/Users/jinhyun/projects/scheduler/db/DDL_TB_REC_SIGNAL.sql`
    - `/Users/jinhyun/projects/scheduler/db/DDL_TB_TRADE_CALENDAR.sql`
    - `/Users/jinhyun/projects/scheduler/db/DDL_TB_BATCH_EXEC_LOG.sql`
    - `/Users/jinhyun/projects/scheduler/db/DDL_TB_BATCH_EXEC_ITEM_LOG.sql`
    - `/Users/jinhyun/projects/scheduler/db/DDL_DROP_TB_STK_PRICE_TABLES.sql`
    - `/Users/jinhyun/projects/scheduler/db/apply_rec_signal_ddl.sh`
    - `/Users/jinhyun/projects/scheduler/db/tools/DbRecSignalDdlRunner.java`
  - 캔버스/노트
    - `/Users/jinhyun/projects/scheduler/_workflow/canvas/MASTER_WORKFLOW.canvas`
    - `/Users/jinhyun/projects/scheduler/_workflow/notes/92_SOURCE_SYNC_MATRIX.md`
    - `/Users/jinhyun/projects/scheduler/_workflow/notes/21_UI_RECOMMEND_DETAIL.md`
    - `/Users/jinhyun/projects/scheduler/_workflow/notes/30_BUSINESS_LOGIC_FLOW.md`
    - `/Users/jinhyun/projects/scheduler/_workflow/notes/40_DB_FLOW.md`

## 추가 대상 상세

### 1. 프론트

- 목표
  - `TB_REC_SIGNAL` 기준 추천 결과를 리스트/상세 화면으로 노출
  - 설계서 필드인 `BASE_DT`, `REC_GRADE`, `MON_CHG_RATE`, `TREND_STRENGTH`, `AVG_TRD_VAL_20`, `REC_REASON`를 직접 확인 가능하게 구성
- 신규 화면
  - `stock/recSignalList`
  - `stock/recSignalDetail`
- 현재 상태
  - `recSignalList.jsp/css/js`, `recSignalDetail.jsp/js` 가 생성됨
  - `RecSignalController` 의 `listView.do`, `detailView.do`, `list.do`, `detail.do` 와 연결됨
  - `kisFinance.jsp` 우측 매매신호 패널에서 `추천신호` 버튼으로 새 화면 진입 가능
  - 운영 데이터가 없으면 샘플 데이터를 fallback 표시함
- 기존 영향
  - 기존 `finance/stockSignal/dashboard.jsp` 와 직접 충돌 없음
  - 메뉴 연결 전까지 기존 화면 영향 없음
  - 메뉴 연결 시 `kisFinance.jsp` 또는 모바일 분석 화면의 진입 링크만 검토 필요

### 2. 백엔드

- 목표
  - 설계서 v1.10 기준의 추천 로직을 별도 패키지로 구현
  - 기존 `StockAnalysisController` 와 `oracle_StockAnalysis.xml` 의 점수형 추천과 분리
- 신규 축
  - Controller: `RecSignalController`
  - Batch: `RecSignalBatch`, `RecSignalDailyBatch`, `RecSignalMarketScheduler`
  - Service: `RecSignalService`, `KisDlyPriceSyncService`, `MaCalculateService`, `TradeDateService`
  - DAO/Mapper: `RecSignal`, `StkMaster`, `TradeDate`, `BatchExecLog`, `BatchExecItemLog`
- 공용 설정 접점
  - `scheduler-servlet.xml` 에 `stockService.xml` import 추가 필요
  - `oracle_mybatis-config.xml` 에 typeAlias / mapper 등록 필요
- 기존 영향
  - `financeService.xml` 는 직접 수정하지 않고 신규 `stockService.xml` 분리 권장
  - 기존 `StockAnalysisController` 의 `/finance/selectRecommendStocks.do` 와 URL 분리 시 충돌 없음
  - `BatchJobCtrlController` 기반 공용 배치 제어는 재사용하지 않음

### 3. DB

- 목표
  - 설계서 v1.10의 신규 테이블군으로 추천신호 저장
- 신규 테이블
  - `TB_STK_MASTER`
  - `TB_REC_SIGNAL`
  - `TB_TRADE_CALENDAR`
  - `TB_BATCH_EXEC_LOG`
  - `TB_BATCH_EXEC_ITEM_LOG`
- 기존 영향
  - 기존 `TB_S_*` 계열과 직접 충돌 없음
  - 다만 종목 마스터, 거래일 캘린더, 배치 이력의 중복 관리 가능성 존재
  - 운영 시점에는 `TB_S_INTEREST_STOCK`, `TB_S_STOCK_ANALYSIS`, `TB_S_BATCH_JOB_CTRL` 와 책임 분리를 명확히 해야 함
- 주의
  - 원천 데이터는 반드시 KIS API 사용
  - 설계서 조건대로 `MONTH_OPEN_PRICE` 는 KIS 일봉 응답에서 계산
  - 정본은 `TB_STK_MASTER`, `TB_TRADE_CALENDAR` 로 고정

### 4. 캔버스

- 목표
  - 공식 캔버스에는 실제 소스가 존재하는 추천신호 UI만 반영
  - API, 배치, DB, KIS 연계는 구현 전까지 준비 문서와 설계서에서만 관리
- 현재 반영 대상
  - `MASTER_WORKFLOW.canvas` 에 `WF-1-2A` UI 노드 유지
  - `92_SOURCE_SYNC_MATRIX.md` 에 `WF-1-2A` 소스 앵커 유지
  - `MASTER_WORKFLOW.canvas` 에 `WF-2-3A`, `WF-2-3B`, `WF-3-1A` 노드 추가
  - `92_SOURCE_SYNC_MATRIX.md` 에 신규 백엔드/DB 소스 앵커 등록
  - `WF-2-3B` 는 KIS API fetch 후 메모리 계산, 시장별 스케줄, 실패건 재시도까지 포함
  - `21_UI_RECOMMEND_DETAIL.md` 에 신규 추천신호 UI 경로 추가
  - `30_BUSINESS_LOGIC_FLOW.md`, `40_DB_FLOW.md` 에는 planned 설계와 실제 구현 시작점을 함께 기록
- 기존 영향
  - 기존 `WF-2-3 추천 종목 선별` 설명은 유지 가능
  - 다만 기존 추천과 신규 추천신호를 같은 노드로 섞으면 해석 충돌 발생
  - source가 생성된 신규 API/배치/DB만 공식 캔버스에 올린다

## 영향 범위

### 영향도 낮음

- 기존 추천 API `/finance/selectRecommendStocks.do`
- 기존 추천 SQL `/src/com/scheduler/finance/sql/oracle/oracle_StockAnalysis.xml`
- 기존 추천 화면 `/webapp/appone/jsp/finance/stockSignal/dashboard.jsp`
- 기존 모바일 추천 노출 `/webapp/appone/jsp/finance/kis/mobile/analysis.jsp`

설명:
URL, 패키지, 테이블을 분리하면 기존 추천 흐름은 수정 없이 공존 가능하다.

### 영향도 중간

- `/Users/jinhyun/projects/scheduler/webapp/WEB-INF/resources/servlet/scheduler-servlet.xml`
- `/Users/jinhyun/projects/scheduler/webapp/WEB-INF/resources/config/mybatis/oracle/oracle_mybatis-config.xml`
- 메뉴 진입 링크가 있는 기존 금융 화면

설명:
신규 Controller/Mapper/Service 등록 때문에 공용 설정 파일은 반드시 수정된다.
메뉴를 어디에 연결하느냐에 따라 기존 화면 진입 동선에도 변화가 생긴다.

### 영향도 높음

- 종목 마스터 관리 기준
- 배치 스케줄/실행 이력 관리 기준

설명:
신규 추천신호 시스템은 `TB_STK_MASTER`, `TB_TRADE_CALENDAR`, `TB_BATCH_EXEC_LOG`, `TB_BATCH_EXEC_ITEM_LOG` 를 사용하므로,
기존 `TB_S_*` 계열과 책임이 겹칠 수 있다.
특히 시장별 스케줄과 실패건 재시도 기준을 먼저 고정해야 한다.

## 기존 로직 영향도 요약

| 구분 | 기존 대상 | 영향도 | 판단 |
| --- | --- | --- | --- |
| 프론트 | `finance/stockSignal`, `finance/kis/mobile` | 낮음 | 신규 화면 추가 후 메뉴 연결만 검토 |
| 컨트롤러 | `StockAnalysisController` | 낮음 | URL 분리 시 직접 수정 불필요 |
| 배치 | `BatchJobCtrlController`, `BatchJobCtrlDaoImpl` | 낮음 | v1.10 에서는 재사용하지 않음 |
| SQL/Mapper | `oracle_StockAnalysis.xml` | 낮음 | 신규 Mapper 분리 시 충돌 없음 |
| 설정 | `scheduler-servlet.xml`, `oracle_mybatis-config.xml` | 중간 | 신규 bean / mapper 등록 필요 |
| DB | `TB_S_*` 기존 추천/분석 테이블 | 중간 | legacy 유지, 신규 시스템 정본과 분리 운영 |
| 캔버스 | 기존 추천 노드 | 중간 | 신규 노드를 분리하지 않으면 의미 충돌 |

## 권장 구현 순서

1. `DDL 및 실행 로그 스크립트` 추가
2. `KIS API fetch + 메모리 계산 경로` 구현
3. `RecSignalBatch / Service / Scheduler / DAO` 구현
4. `RecSignalController / JSP` 연결
5. `MASTER_WORKFLOW.canvas` 와 `92_SOURCE_SYNC_MATRIX.md` 동기화

## 결론

가장 안전한 반영 방식은 아래와 같다.

- 기존 `finance` 추천 흐름은 유지
- 신규 `stock` 패키지와 신규 `TB_*` 테이블군을 별도 추가
- 공용 설정 접점만 최소 수정
- 캔버스는 신규 노드로 분리

즉, 이번 작업은 `기존 추천 수정`이 아니라 `신규 추천신호 시스템 추가`로 다루는 것이 맞다.
