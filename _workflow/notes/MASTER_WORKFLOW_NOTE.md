# MASTER_WORKFLOW_NOTE

## 목적

이 문서는 `_workflow/canvas/MASTER_WORKFLOW.canvas`의 보조 설명 노트다.
프로젝트 운영 기준상 캔버스는 `MASTER_WORKFLOW.canvas` 하나만 공식 사용한다.

다음 정보는 이 문서로 흡수한다.

- 삭제된 보조 캔버스의 핵심 정보
- `MASTER_WORKFLOW` 와 실제 프로젝트 소스의 매핑 근거
- 1:1 싱크 점검 결과와 운영 원칙

## 공식 기준

- 공식 캔버스: `MASTER_WORKFLOW.canvas`
- 구조/연결 SoT: `MASTER_WORKFLOW.canvas`
- 구현 사실 SoT: `src/`, `webapp/`, `db/`
- 보조 설명/이관 메모: `MASTER_WORKFLOW_NOTE.md`

삭제 대상 캔버스:

- `CURRENT_RUNTIME_FLOW.canvas`
- `PROJECT_STRUCTURE_TREE.canvas`

## 이관 정보 1. 프로젝트 구조 요약

`PROJECT_STRUCTURE_TREE.canvas` 에 있던 핵심 구조 정보는 아래로 정리한다.

### 루트 축

```text
scheduler/
├── webapp/              화면, JSP, WEB-INF, 정적 리소스
├── src/com/scheduler/   Java 소스 루트
├── finance/             금융 도메인 소스/SQL 별도 루트
├── comm/                공통 프레임/유틸/공통 SQL
├── management/          사용자/메뉴/코드/캘린더 관리
├── login/               로그인/세션 관련
├── kis_api/             KIS API 스펙/모델
├── kis_client/          KIS 클라이언트 구현
├── db/                  DDL, 검증 SQL, 운영 스크립트, Java DB 툴
└── _workflow/           Obsidian 캔버스, 노트, 상태 문서
```

### 구조 해석 메모

- 화면 진입점은 `webapp/appone/jsp/` 와 `finance/controller/` 조합으로 본다.
- 금융 핵심 로직은 `finance/module/`, `finance/sql/oracle/`, `finance/dao/impl/` 에 집중돼 있다.
- 외부 시세/차트 원천은 `kis_api/`, `kis_client/`, `finance/kis/` 축을 따라간다.
- 운영성/적용 스크립트는 `db/` 아래를 먼저 본다.
- 워크플로우 구조/연결 기준은 `_workflow/canvas/MASTER_WORKFLOW.canvas` 하나만 사용한다.

## 이관 정보 2. 런타임 흐름 요약

`CURRENT_RUNTIME_FLOW.canvas` 에 있던 step by step 런타임 정리는 아래로 흡수한다.

### 주요 UI → 비즈니스 → DB 축

```text
관심종목 화면
  → InterestWatchlistController / WatchlistRealtimeEndpoint
  → TB_S_INTEREST_STOCK / 실시간 시세 반영

추천종목 화면
  → StockAnalysisController
  → oracle_StockAnalysis.xml
  → TB_S_INTEREST_STOCK + TB_S_STOCK_ANALYSIS + TB_S_STOCK_30MINANALYSIS + TB_S_SIGNAL_EVENT

차트 화면
  → StockCodeInfoController
  → StockAnalysisUtil / BullishMomentumDetector / PositionRuleEngine
  → 차트 렌더 JS + 시그널/오버레이 처리

보유종목 화면
  → PositionController
  → oracle_Position.xml
  → TB_S_POSITION / TB_S_POSITION_TXN / TB_S_POSITION_EVENT

배치 저장
  → BatchJobCtrlController
  → BatchJobCtrlDaoImpl
  → TB_S_BATCH_JOB_CTRL + 분석/이벤트 테이블 갱신
```

### 런타임 세부 흐름 메모

- 기존 추천은 `StockAnalysisController` 와 `oracle_StockAnalysis.xml` 중심의 점수형 추천 흐름이다.
- 기존 차트 분석은 `StockCodeInfoController`, `StockAnalysisUtil`, `BullishMomentumDetector`, `PositionRuleEngine` 쪽이 핵심이다.
- 기존 배치 제어는 `BatchJobCtrlController`, `BatchJobCtrlDaoImpl`, `TB_S_BATCH_JOB_CTRL` 체계다.
- 보유종목은 `PositionController` 와 `oracle_Position.xml` 기준으로 이미 별도 vertical slice가 있다.

## MASTER_WORKFLOW ↔ Source 심층 분석

## 분석 기준

- 대상 캔버스: `MASTER_WORKFLOW.canvas`
- 대상 소스: `src/`, `webapp/`, `db/`
- 판단 기준
  - `SYNCED` : 캔버스 노드가 실제 소스 앵커와 명확히 대응됨
  - `PARTIAL` : 실제 소스는 있으나 연결 상태 또는 범위가 부분 상태임

## 현재 공식 캔버스 운영 원칙

현재 `MASTER_WORKFLOW.canvas` 는 source-backed 노드만 남긴 상태로 운영한다.

- 실제 파일 또는 실제 사용 테이블이 있는 기능만 캔버스에 남긴다.
- 설계만 있는 planned 항목은 캔버스에 올리지 않는다.
- helper/checklist 성격의 설명은 이 문서에만 남긴다.
- 소스와 캔버스는 항상 같은 작업 단위에서 같이 수정한다.
- 상태 표기 의미는 `✅ 실제 구현`, `🟡 실제 소스는 있으나 연결 또는 범위가 부분 상태`, `🔴 에러·오류 점검 필요` 로 관리한다.

## 캔버스에서 NOTE로 내린 항목

다음 항목은 공식 캔버스에서 제거하고 이 문서로 이관했다.

### helper / checklist

- `ai_note`
- `ui_1_a`
- `ui_1_b`
- `ui_2_f`
- `ui_2_r`
- `ui_2_e`
- `ui_2_a`
- `chk_ui3`
- `chk_biz4`
- `chk_db8`

### planned / future scope

- `ui_pick`
- `ui_ai`
- `2-5 차트 표시 항목`
- `2-6 백테스트/튜닝 루프`
- `2-7 선택 종목 모니터링/정확도`
- `2-8 AI 예측/재검토 엔진`
- `2-9 실시간 신호 스트림`
- `WF-3-1B`
- `db_2`
- `db_3`
- `db_4`
- `db_5`
- `db_6`
- `db_7`
- `db_auto`
- `auto_hdr`
- `auto_1`
- `auto_2`
- `auto_3`
- `auto_4`
- `auto_5`

이 항목들은 필요할 때 `MASTER_WORKFLOW_NOTE.md` 와 관련 설계 문서에서만 관리한다.
실제 소스가 추가되기 전에는 `MASTER_WORKFLOW.canvas` 와 `92_SOURCE_SYNC_MATRIX.md` 에 올리지 않는다.

## 현재 캔버스 노드별 점검 결과

| MASTER_WORKFLOW 영역 | 상태 | 실제 소스 앵커 | 판단 |
| --- | --- | --- | --- |
| `1-1 관심종목 UI` | `SYNCED` | `webapp/appone/jsp/finance/kis/mobile/watchlist.jsp`, `src/com/scheduler/finance/controller/InterestWatchlistController.java`, `src/com/scheduler/finance/websocket/WatchlistRealtimeEndpoint.java` | 화면/조회/실시간 축이 실제 소스와 대응됨 |
| `WF-1-2A 추천신호 v1.10 UI` | `PARTIAL` | `webapp/appone/jsp/stock/recSignalList.jsp`, `webapp/appone/jsp/stock/recSignalList.css`, `webapp/appone/jsp/stock/recSignalList.js`, `webapp/appone/jsp/stock/recSignalDetail.jsp`, `webapp/appone/jsp/stock/recSignalDetail.js`, `src/com/scheduler/stock/web/RecSignalController.java`, `webapp/appone/jsp/finance/kis/kisFinance/kisFinance.jsp`, `webapp/appone/jsp/finance/kis/kisFinance/js/kisFinancePage.js`, `webapp/appone/jsp/finance/kis/mobile/analysis.jsp`, `webapp/appone/jsp/finance/kis/mobile/js/analysis.js` | 리스트/상세 화면과 조회 API 연동, kisFinance 우측 비교 테이블과 모바일 TOP5까지 같은 추천신호 API를 사용한다. 샘플 fallback은 제거했고 운영 데이터 없음/조회 실패 상태를 화면에서 직접 보여주도록 바꿨다. 운영 데이터 적재 완료와 메뉴 체계 정식 편입은 아직 미완료 |
| `1-3 보유종목 UI` | `SYNCED` | `src/com/scheduler/finance/controller/PositionController.java`, `src/com/scheduler/finance/dao/impl/PositionDaoImpl.java`, `src/com/scheduler/finance/sql/oracle/oracle_Position.xml`, `db/DDL_TB_S_POSITION.sql` | CRUD/물타기 축이 실제 소스와 대응됨 |
| `1-4 차트 UI` | `SYNCED` | `webapp/appone/jsp/finance/kis/mobile/chart.jsp`, `webapp/appone/jsp/finance/kis/kisFinance/kisFinance.jsp`, `src/com/scheduler/finance/controller/StockCodeInfoController.java` | 화면/컨트롤러/데이터 흐름이 존재 |
| `2-1 배치/분석 저장` | `SYNCED` | `src/com/scheduler/finance/controller/BatchJobCtrlController.java`, `src/com/scheduler/finance/dao/impl/BatchJobCtrlDaoImpl.java`, `src/com/scheduler/finance/sql/oracle/oracle_BatchJobCtrl.xml` | 기존 배치 저장 흐름과 대응되며, KOSPI200 동기화 전에 `TB_S_INTEREST_STOCK.STOCK_COUNTRY_CODE` null 값을 `KR`로 정규화하는 mapper와 `ORA-38104`를 피하도록 `MERGE` matched branch에서 ON절 컬럼 갱신을 제거한 보정이 포함된다. 또한 `BatchJobCtrlDaoImpl`은 MyBatis batch executor의 반환값 대신 예외 여부를 기준으로 KOSPI200 merge 성공/실패를 집계하고, `tryLock`은 DB update count 기준으로만 성공을 판단해 중복 실행을 막는다. `schedulerTick`은 `RUNNING_YN='Y'`인데 살아 있는 worker thread가 없는 고아 락을 다음 tick에서 즉시 `AUTO_RECOVER_ORPHAN_LOCK`로 정리한다. |
| `2-2 차트 분석 모듈` | `SYNCED` | `src/com/scheduler/finance/module/StockAnalysisUtil.java`, `src/com/scheduler/finance/module/BullishMomentumDetector.java`, `src/com/scheduler/finance/module/PositionRuleEngine.java`, `src/com/scheduler/finance/module/MovingAveragesLineNumber.java` | 모듈 축이 실제 소스와 대응됨 |
| `WF-2-2c-BASE 지수 베이스 모듈` | `PARTIAL` | `src/com/scheduler/finance/module/IndexTrendRuleModule.java`, `src/com/scheduler/finance/controller/StockAnalysisController.java` | 실제 모듈은 있으나 활용 범위는 부분 상태임 |
| `WF-2-3A 추천신호 조회/응답 런타임` | `PARTIAL` | `src/com/scheduler/stock/web/RecSignalController.java`, `src/com/scheduler/stock/service/RecSignalService.java`, `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`, `webapp/WEB-INF/resources/service/stockService.xml` | `RecSignalController` 의 `list.do`, `detail.do` 요청이 `RecSignalService` 와 `RecSignalMapper.xml` 을 거쳐 `TB_REC_SIGNAL` 조회 응답으로 이어진다. 결과 조회는 `MKT_CD` 기준으로 KR/US를 분리하고, 운영 데이터 없음/null 응답은 UI 빈 상태로 표출한다. 정식 메뉴 체계 편입과 운영 데이터 누적 검증은 아직 부분 상태임 |
| `WF-2-3A-1 조회 진입/UI 요청` | `PARTIAL` | `webapp/appone/jsp/stock/recSignalList.jsp`, `webapp/appone/jsp/stock/recSignalDetail.jsp`, `src/com/scheduler/stock/web/RecSignalController.java` | 추천신호 v1.10 UI 에서 list/detail 화면이 `list.do`, `detail.do` 조회를 호출하는 진입 단계다. `baseDt`, `stkCd`, `mktCd` 파라미터가 여기서 런타임으로 전달된다. |
| `WF-2-3A-2 조회조건/기준일 정규화` | `PARTIAL` | `src/com/scheduler/stock/service/RecSignalService.java`, `src/com/scheduler/stock/service/TradeDateService.java` | 조회 요청이 서비스로 들어오면 시장 필터를 정규화하고, 최신 기준일 조회 후 `resolveReadBaseDate` 로 실제 조회 기준일을 보정한다. 목록 조회는 `recYn` 기본값도 이 단계에서 `Y` 로 맞춘다. |
| `WF-2-3A-3 추천신호 조회 응답` | `PARTIAL` | `src/com/scheduler/stock/dao/RecSignalDao.java`, `src/com/scheduler/stock/dao/impl/RecSignalDaoImpl.java`, `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`, `src/com/scheduler/stock/web/RecSignalController.java` | 정규화된 조회 조건으로 `TB_REC_SIGNAL` 을 list/detail 형태로 읽어 `DataTableSettingVo` 응답으로 반환한다. 런타임 상 조회의 마지막 단계이며 결과 저장 스키마와 직접 연결된다. |
| `WF-2-3B 추천신호 수집/계산/적재 배치` | `PARTIAL` | `src/com/scheduler/stock/web/RecSignalController.java`, `src/com/scheduler/stock/batch/RecSignalBatch.java`, `src/com/scheduler/stock/batch/RecSignalDailyBatch.java`, `src/com/scheduler/stock/batch/RecSignalMarketScheduler.java`, `src/com/scheduler/stock/service/KisDlyPriceSyncService.java`, `src/com/scheduler/stock/service/TradeDateService.java`, `src/com/scheduler/stock/service/MaCalculateService.java`, `src/com/scheduler/stock/service/RecSignalService.java`, `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`, `src/com/scheduler/stock/sql/oracle/TradeDateMapper.xml`, `src/com/scheduler/stock/sql/oracle/BatchExecLogMapper.xml`, `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml` | `RecSignalDailyBatch` 와 `RecSignalMarketScheduler` 가 정기 실행을 담당하고, 수동 실행은 `RecSignalController.runBatch.do` 로 진입한다. 일봉 원천은 KIS API에서 조회한 뒤 메모리에서 계산해 `TB_REC_SIGNAL` 에 적재하고, 실행/종목 로그는 `TB_BATCH_EXEC_LOG`, `TB_BATCH_EXEC_ITEM_LOG` 로 남긴다. 시장별 스케줄은 KR 02:00/05:00, US 16:00/20:00 이고, 재시도는 실패 종목만 다시 처리한다. |
| `WF-2-3B-1 배치 진입/스케줄` | `PARTIAL` | `src/com/scheduler/stock/web/RecSignalController.java`, `src/com/scheduler/stock/batch/RecSignalDailyBatch.java`, `src/com/scheduler/stock/batch/RecSignalMarketScheduler.java` | 추천신호 배치의 시작점이다. 정기 실행은 `RecSignalMarketScheduler`, 수동 실행은 `runBatch.do` 로 들어오고, `RecSignalDailyBatch` 가 기본 파라미터를 채운 뒤 실제 배치 서비스로 넘긴다. |
| `WF-2-3B-2 배치 기준일/대상종목 결정` | `PARTIAL` | `src/com/scheduler/stock/service/RecSignalService.java`, `src/com/scheduler/stock/service/TradeDateService.java`, `src/com/scheduler/stock/dao/StkMasterDao.java`, `src/com/scheduler/stock/dao/BatchExecItemLogDao.java` | 시장 구분, 입력 기준일, 거래일 보정, 대상 종목 필터, 실패건 재대상 선별이 이 단계에서 결정된다. 실제 배치 순서상 수집 전에 어떤 날짜와 어떤 종목을 처리할지 확정하는 구간이다. |
| `WF-2-3B-3 KIS 일봉 수집` | `PARTIAL` | `src/com/scheduler/stock/service/KisDlyPriceSyncService.java`, `src/com/scheduler/stock/web/RecSignalController.java` | 기준일과 대상 종목이 정해지면 KIS API 에서 KR/US 일봉 원천을 가져온다. `syncDlyPrice.do` 는 이 수집 단계를 검증할 때 쓰는 보조 진입점이다. |
| `WF-2-3B-4 추천신호 메모리 계산` | `PARTIAL` | `src/com/scheduler/stock/service/RecSignalService.java`, `src/com/scheduler/stock/service/MaCalculateService.java` | 수집한 일봉을 메모리에서 계산해 월시가, 이동평균, 추세강도, 추천 여부와 등급/사유를 만든다. 결과 저장 전 추천신호 데이터를 완성하는 핵심 계산 단계다. |
| `WF-2-3B-5 결과/실행 로그 적재` | `ERROR_CHECK` | `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`, `src/com/scheduler/stock/sql/oracle/BatchExecLogMapper.xml`, `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml`, `src/com/scheduler/stock/dao/BatchExecLogDao.java`, `src/com/scheduler/stock/dao/BatchExecItemLogDao.java` | 계산이 끝난 뒤 `TB_REC_SIGNAL` 저장과 실행/종목 로그 적재가 순서대로 수행된다. 프로그램 동작상 배치 결과가 DB 에 남는 지점이며, 저장 오류와 로그 누락 여부를 운영 중 우선 점검해야 하는 단계라 캔버스에서는 `🔴` 로 관리한다. |
| `WF-2-3B-6 실패건 재시도` | `ERROR_CHECK` | `src/com/scheduler/stock/service/RecSignalService.java`, `src/com/scheduler/stock/dao/BatchExecItemLogDao.java`, `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml` | 이전 적재 단계에서 남긴 실패 종목 로그를 읽어 retryOnly 배치 대상을 다시 만든다. 본 실행 다음에 이어지는 후속 단계로, 실패건 재처리 여부를 계속 점검해야 하므로 캔버스에서는 `🔴` 로 관리한다. |
| `2-4 보유종목 관리` | `SYNCED` | `src/com/scheduler/finance/controller/PositionController.java`, `src/com/scheduler/finance/dao/impl/PositionDaoImpl.java`, `src/com/scheduler/finance/sql/oracle/oracle_Position.xml` | 실제 구현 있음 |
| `3-1 TB_S_INTEREST_STOCK` | `SYNCED` | `src/com/scheduler/finance/sql/oracle/oracle_BatchStock.xml`, `src/com/scheduler/finance/sql/oracle/oracle_BatchJobCtrl.xml`, `src/com/scheduler/finance/sql/oracle/oracle_StockAnalysis.xml`, `src/com/scheduler/finance/sql/oracle/oracle_StockManagement.xml`, `db/tools/DbUpsertMarketRegimeAssets.java` | 관심종목 기준 테이블로 실제 사용 중이며, 배치 제어 mapper에서도 국가코드 null 정규화를 수행한다. |
| `3-2 TB_S_BATCH_JOB_CTRL` | `SYNCED` | `src/com/scheduler/finance/sql/oracle/oracle_BatchJobCtrl.xml`, `src/com/scheduler/finance/controller/BatchJobCtrlController.java`, `src/com/scheduler/finance/dao/impl/BatchJobCtrlDaoImpl.java` | 기존 배치 제어 테이블과 대응되며, `tryLock`을 DB update count 기준으로 보정해 `RUNNING_YN` 기반 락 의미를 유지한다. 또한 다음 scheduler tick에서 고아 락을 즉시 해제하도록 보강했다. |
| `3-3 TB_S_STOCK_ANALYSIS` | `SYNCED` | `src/com/scheduler/finance/sql/oracle/oracle_StockAnalysis.xml`, `src/com/scheduler/finance/sql/oracle/oracle_StockCodeInfo.xml`, `src/com/scheduler/finance/dao/impl/BatchJobCtrlDaoImpl.java` | 일봉 분석 스냅샷 테이블과 대응됨 |
| `WF-3-1A 추천신호 저장구조/배치운영 스키마` | `PARTIAL` | `db/DDL_TB_STK_MASTER.sql`, `db/DDL_TB_REC_SIGNAL.sql`, `db/DDL_TB_TRADE_CALENDAR.sql`, `db/DDL_TB_BATCH_EXEC_LOG.sql`, `db/DDL_TB_BATCH_EXEC_ITEM_LOG.sql`, `db/MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql`, `db/DDL_DROP_TB_STK_PRICE_TABLES.sql`, `db/tools/DbRecSignalDdlRunner.java`, `db/apply_rec_signal_ddl.sh`, `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`, `src/com/scheduler/stock/sql/oracle/TradeDateMapper.xml`, `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml`, `src/com/scheduler/comm/system/InitializedSetting.java`, `src/com/scheduler/comm/system/GlobalVariablesFileRead.java`, `src/com/scheduler/comm/system/GlobalVariablesDBRead.java` | 추천신호 운영 경로는 입력 정본, 결과 저장, 운영 로그로 역할이 나뉜다. `TB_STK_MASTER`, `TB_TRADE_CALENDAR` 는 입력 정본이고, `TB_REC_SIGNAL` 은 결과 저장, `TB_BATCH_EXEC_LOG`, `TB_BATCH_EXEC_ITEM_LOG` 는 실행/재시도 로그를 담당한다. `TB_REC_SIGNAL` 은 `MKT_CD` 포함 PK 기준으로 KR/US 결과를 분리하고, `MIGRATE_REC_SIGNAL_MEMORY_MODEL.sql` 과 runbook을 통해 운영 DB 반영 순서를 분리한다. `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` 는 smoke test 후 drop 대상으로 관리한다. |
| `WF-3-1A-1 입력 정본 스키마` | `PARTIAL` | `db/DDL_TB_STK_MASTER.sql`, `db/DDL_TB_TRADE_CALENDAR.sql`, `src/com/scheduler/stock/sql/oracle/TradeDateMapper.xml`, `src/com/scheduler/stock/dao/StkMasterDao.java`, `src/com/scheduler/stock/dao/TradeDateDao.java` | 배치가 시작되면 대상 종목과 거래일 판단에 쓰는 입력 정본 스키마다. 프로그램 선후 관계상 기준일/대상종목 결정 단계가 먼저 참조하는 저장 구조다. |
| `WF-3-1A-2 결과 저장 스키마` | `PARTIAL` | `db/DDL_TB_REC_SIGNAL.sql`, `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`, `src/com/scheduler/stock/dao/RecSignalDao.java` | 계산 결과가 최종 저장되는 스키마다. 배치 적재 이후 조회 런타임이 읽는 공용 결과 저장소 역할을 한다. |
| `WF-3-1A-3 배치 로그/재시도 스키마` | `ERROR_CHECK` | `db/DDL_TB_BATCH_EXEC_LOG.sql`, `db/DDL_TB_BATCH_EXEC_ITEM_LOG.sql`, `src/com/scheduler/stock/sql/oracle/BatchExecLogMapper.xml`, `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml`, `src/com/scheduler/stock/dao/BatchExecLogDao.java`, `src/com/scheduler/stock/dao/BatchExecItemLogDao.java` | 배치 실행 이력과 종목별 실패 로그를 남기는 운영 스키마다. 적재 단계 이후 데이터가 쌓이고, 실패건 재시도 단계가 다시 이 구조를 참조하므로 운영 오류 점검 기준 노드로 보고 캔버스에서는 `🔴` 로 관리한다. |
| `3-4 TB_S_STOCK_30MINANALYSIS` | `SYNCED` | `src/com/scheduler/finance/sql/oracle/oracle_StockAnalysis.xml`, `src/com/scheduler/finance/sql/oracle/oracle_StockCodeInfo.xml`, `src/com/scheduler/finance/dao/impl/BatchJobCtrlDaoImpl.java` | 30분 분석 스냅샷 테이블과 대응됨 |
| `3-5 TB_S_SIGNAL_EVENT` | `SYNCED` | `db/DDL_TB_S_SIGNAL_EVENT.sql`, `src/com/scheduler/finance/sql/oracle/oracle_SignalEvent.xml`, `src/com/scheduler/finance/dao/impl/SignalEventDaoImpl.java` | 이벤트 저장 축이 실제 소스와 대응됨 |
| `3-8 TB_S_POSITION / TXN / EVENT` | `SYNCED` | `db/DDL_TB_S_POSITION.sql`, `src/com/scheduler/finance/controller/PositionController.java`, `src/com/scheduler/finance/sql/oracle/oracle_Position.xml` | 보유종목/거래/이벤트 DB 축과 대응됨 |

## 종합 판정

현재 `MASTER_WORKFLOW.canvas` 는 source-backed 노드만 남긴 공식 1:1 매핑 캔버스다.

- `SYNCED` 노드는 실제 소스와 직접 대응된다.
- `PARTIAL` 노드는 실제 소스는 있으나 연결 범위가 아직 부분 상태다.
- source가 없는 planned 기능은 캔버스에서 제거하고 이 문서로 내렸다.

즉, 현재 운영 기준은 다음처럼 정리된다.

- `MASTER_WORKFLOW.canvas` : 현재 존재하는 소스의 기능 맵
- `92_SOURCE_SYNC_MATRIX.md` : 현재 캔버스 노드의 실제 소스 앵커
- `MASTER_WORKFLOW_NOTE.md` : removed/planned/helper 설명과 심층 메모

## 1:1 싱크 운영 기준

1. 실제 소스가 없는 항목은 공식 캔버스에 올리지 않는다.
2. planned/helper/checklist 항목은 이 문서에만 남긴다.
3. 소스가 추가되거나 삭제되면 같은 작업 단위에서 `MASTER_WORKFLOW.canvas` 와 `92_SOURCE_SYNC_MATRIX.md` 를 같이 수정한다.
4. 노드 ID가 바뀌면 이 문서의 removed/planned 목록과 관련 준비 문서도 같이 정리한다.
5. 보조 캔버스는 새로 만들지 않는다.

## 후속 작업 우선순위

1. `WF-1-2A` 추천신호 UI는 list/detail 화면과 조회 API 연동, kisFinance 진입 버튼까지 반영된 상태다. 시장별 운영 데이터 누적과 정식 메뉴 편입이 완료되면 `PARTIAL` 문구를 다시 정리한다.
2. `WF-2-2c-BASE` 의 실제 사용 범위가 명확해지면 `PARTIAL` 문구를 더 좁힌다.
3. future scope 문서는 `MASTER_WORKFLOW_NOTE.md` 와 개별 설계서에서만 관리한다.

## 추가 설계 메모 (2026-03-12)

- RecSignal 배치 운영을 `BatchJobCtrl`과 분리한 공용 Batch Admin으로 전환하는 설계 초안을 완료했다.
- 설계 문서: `36_REC_SIGNAL_BATCH_ADMIN_DESIGN_2026-03-12.md`
- 핵심: 리스크 대응(중복 실행/고아락/권한/입력검증) + DB 스케줄/파라미터 기반 무하드코딩 구조
