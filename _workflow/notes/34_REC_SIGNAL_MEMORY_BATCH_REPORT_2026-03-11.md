# 추천신호 메모리 계산형 전환 보고

작성일: 2026-03-11  
대상 프로젝트: `scheduler`

## 1. 개요

추천신호 v1.10 경로를 `KIS API 일봉 응답 -> 메모리 계산 -> TB_REC_SIGNAL 저장` 구조로 전환했다.  
기존 운영 경로에서 `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` 적재/조회 의존을 제거했고, KR/US 시장별 배치와 실패 종목 재시도 구조를 추가했다.

## 2. 변경 배경

- 기존 구조는 `TB_STK_DLY_PRICE` 적재량이 커지면서 Oracle 공간 사용량 문제를 유발했다.
- 추천신호 계산에 필요한 최종 값은 `TB_REC_SIGNAL`에만 저장하면 되므로, 원천 일봉을 DB에 누적 저장하지 않는 방향으로 전환했다.
- KR/US 시장별 결과를 분리 저장하고, 실패 종목만 재실행할 수 있는 운영 구조가 필요했다.

## 3. 핵심 변경 사항

### 3.1 계산 구조 전환

- 변경 전
  - `KIS API -> TB_STK_DLY_PRICE / TB_TRADE_CALENDAR 적재 -> DlyPriceDao 조회 -> TB_REC_SIGNAL 저장`
- 변경 후
  - `KIS API -> 메모리 계산(MA/월시가/현재가/20일 평균 거래대금) -> TB_REC_SIGNAL 저장`

### 3.2 배치 구조 전환

- 시장별 배치를 분리했다.
  - KR 본배치: `02:00`
  - KR 실패 재시도: `05:00`
  - US 본배치: `16:00`
  - US 실패 재시도: `20:00`
- 실패 재시도는 전체 종목 재실행이 아니라 직전 본배치에서 실패한 종목만 다시 처리한다.

### 3.3 DB 구조 변경

- 유지 테이블
  - `TB_STK_MASTER`
  - `TB_TRADE_CALENDAR`
  - `TB_REC_SIGNAL`
  - `TB_BATCH_EXEC_LOG`
- 신규 테이블
  - `TB_BATCH_EXEC_ITEM_LOG`
- 운영 경로 제거 대상
  - `TB_STK_DLY_PRICE`
  - `TB_STK_MON_PRICE`
- `TB_REC_SIGNAL`는 `MKT_CD` 포함 PK 기준으로 KR/US 결과를 분리 저장하도록 변경했다.

## 4. 주요 반영 파일

### 4.1 백엔드

- `src/com/scheduler/stock/service/KisDlyPriceSyncService.java`
  - KIS fetch 전용 서비스로 전환
  - 국내/해외 일봉 응답을 `List<DlyPriceDto>`로 반환
- `src/com/scheduler/stock/service/RecSignalService.java`
  - DLY/MON 테이블 조회 제거
  - 메모리 계산 기반 추천신호 생성
  - 실패 종목 item log 기록
- `src/com/scheduler/stock/service/MaCalculateService.java`
  - `List<DlyPriceDto>` 기반 MA 계산 추가
- `src/com/scheduler/stock/service/TradeDateService.java`
  - 시장별 `EFFECTIVE_BASE_DATE` 조회 지원
- `src/com/scheduler/stock/batch/RecSignalDailyBatch.java`
  - 메모리 계산형 배치 실행 구조로 전환
- `src/com/scheduler/stock/batch/RecSignalMarketScheduler.java`
  - KR/US 본배치 및 재시도 스케줄 추가
- `src/com/scheduler/stock/web/RecSignalController.java`
  - 상세 조회 시 `mktCd` 전달 보강

### 4.2 DAO / Mapper

- 추가
  - `src/com/scheduler/stock/dao/BatchExecItemLogDao.java`
  - `src/com/scheduler/stock/dao/impl/BatchExecItemLogDaoImpl.java`
  - `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml`
- 수정
  - `src/com/scheduler/stock/dao/RecSignalDao.java`
  - `src/com/scheduler/stock/dao/impl/RecSignalDaoImpl.java`
  - `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`
  - `src/com/scheduler/stock/sql/oracle/TradeDateMapper.xml`
  - `src/com/scheduler/stock/sql/oracle/StkMasterMapper.xml`
- 제거
  - `src/com/scheduler/stock/dao/DlyPriceDao.java`
  - `src/com/scheduler/stock/dao/MonPriceDao.java`
  - `src/com/scheduler/stock/dao/impl/DlyPriceDaoImpl.java`
  - `src/com/scheduler/stock/dao/impl/MonPriceDaoImpl.java`
  - `src/com/scheduler/stock/sql/oracle/DlyPriceMapper.xml`
  - `src/com/scheduler/stock/sql/oracle/MonPriceMapper.xml`

### 4.3 설정

- `webapp/WEB-INF/resources/service/stockService.xml`
  - DLY/MON DAO bean 제거
  - item log DAO, 시장별 scheduler bean 추가
- `webapp/WEB-INF/resources/config/mybatis/oracle/oracle_mybatis-config.xml`
  - DLY/MON mapper 제거
  - item log mapper 추가

### 4.4 프론트

- `webapp/appone/jsp/stock/recSignalList.js`
  - KR/US 구분을 위한 `mktCd` 전달 추가
- `webapp/appone/jsp/stock/recSignalDetail.js`
  - 상세 조회 시 `mktCd` 반영
- `webapp/appone/jsp/stock/recSignalDetail.jsp`
  - `mktCd` config 전달 추가
- `webapp/appone/jsp/stock/recSignalList.jsp`
  - `baseDt`, `mktCd` query param 전달 보강

### 4.5 DB / 스크립트

- `db/DDL_TB_REC_SIGNAL.sql`
  - `MKT_CD` NOT NULL + PK 포함
- `db/DDL_TB_TRADE_CALENDAR.sql`
  - KR/US 시장코드 설명으로 수정
- `db/DDL_TB_BATCH_EXEC_ITEM_LOG.sql`
  - 실패 종목 재시도용 item log 신규
- `db/DDL_DROP_TB_STK_PRICE_TABLES.sql`
  - `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` drop 스크립트 추가
- `db/tools/DbRecSignalDdlRunner.java`
  - 결과 저장 중심 DDL만 적용하도록 정리

## 5. 운영 기준

### 5.1 배치 실행 기준

- KR
  - 본배치: `02:00`
  - 실패 재시도: `05:00`
- US
  - 본배치: `16:00`
  - 실패 재시도: `20:00`

### 5.2 호출 간격

- 본배치 기본 간격: `1초`
- 재시도 기본 간격: `2초`
- 병렬 호출 없이 종목 단위 순차 처리

### 5.3 저장 기준

- KIS 일봉 응답은 DB에 누적 저장하지 않는다.
- 계산 결과만 `TB_REC_SIGNAL`에 저장한다.
- 거래일 캘린더는 `TB_TRADE_CALENDAR`에 유지한다.
- 실패 종목은 `TB_BATCH_EXEC_ITEM_LOG`에 남겨 재시도 대상으로 사용한다.

## 6. 영향도

### 6.1 낮음

- 기존 `finance` 추천 로직
- 기존 `StockAnalysisController`
- 기존 `oracle_StockAnalysis.xml`

### 6.2 중간

- `stock` 신규 추천신호 조회 화면
- `TB_REC_SIGNAL` 조회 경로
- 시장별 배치 스케줄 운영

### 6.3 높음

- 추천신호 배치 구조 전체
- 결과 저장 스키마
- 실패 재시도 운영 기준
- 기존 `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` 제거 시 DB migration 절차

## 7. 검증 결과

다음 검증을 수행했다.

- `javac`
  - `src/com/scheduler/stock` 전체 컴파일 확인
- `node -c`
  - `webapp/appone/jsp/stock/recSignalList.js`
  - `webapp/appone/jsp/stock/recSignalDetail.js`
- `xmllint`
  - `stockService.xml`
  - `oracle_mybatis-config.xml`
  - stock mapper XML
- `jq`
  - `_workflow/canvas/MASTER_WORKFLOW.canvas`
- 추가 확인
  - `src`, `resources`, `WEB-INF/classes` 기준으로 `DlyPriceDao`, `MonPriceDao`, `DlyPriceMapper`, `MonPriceMapper` 참조 제거 확인

## 8. 남은 작업

### 8.1 DB 적용

- `TB_REC_SIGNAL` PK 변경 적용
- `TB_BATCH_EXEC_ITEM_LOG` 생성
- 필요 시 `TB_STK_DLY_PRICE`, `TB_STK_MON_PRICE` drop 실행

### 8.2 운영 검증

- 서버 재기동
- KR/US 스케줄 실제 트리거 확인
- 실패 종목 재시도 경로 확인
- KR/US 상세 조회 화면에서 `mktCd` 기준 응답 확인

## 9. 공식 동기화 반영

다음 공식 문서를 같이 갱신했다.

- `_workflow/canvas/MASTER_WORKFLOW.canvas`
- `_workflow/notes/92_SOURCE_SYNC_MATRIX.md`
- `_workflow/notes/MASTER_WORKFLOW_NOTE.md`
- `_workflow/notes/30_BUSINESS_LOGIC_FLOW.md`
- `_workflow/notes/40_DB_FLOW.md`
- `_workflow/notes/33_REC_SIGNAL_V110_IMPLEMENTATION_PREP.md`

## 10. 변경 보고

- Canvas ID `WF-2-3A`
  - Change Type `MODIFY`
  - Source Files
    - `src/com/scheduler/stock/web/RecSignalController.java`
    - `src/com/scheduler/stock/service/RecSignalService.java`
    - `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`
    - `webapp/appone/jsp/stock/recSignalList.js`
    - `webapp/appone/jsp/stock/recSignalDetail.js`
    - `webapp/appone/jsp/stock/recSignalDetail.jsp`
    - `_workflow/canvas/MASTER_WORKFLOW.canvas`
    - `_workflow/notes/92_SOURCE_SYNC_MATRIX.md`
    - `_workflow/notes/MASTER_WORKFLOW_NOTE.md`

- Canvas ID `WF-2-3B`
  - Change Type `MODIFY`
  - Source Files
    - `src/com/scheduler/stock/batch/RecSignalBatch.java`
    - `src/com/scheduler/stock/batch/RecSignalDailyBatch.java`
    - `src/com/scheduler/stock/batch/RecSignalMarketScheduler.java`
    - `src/com/scheduler/stock/service/KisDlyPriceSyncService.java`
    - `src/com/scheduler/stock/service/RecSignalService.java`
    - `src/com/scheduler/stock/service/TradeDateService.java`
    - `src/com/scheduler/stock/service/MaCalculateService.java`
    - `src/com/scheduler/stock/dao/BatchExecItemLogDao.java`
    - `src/com/scheduler/stock/dao/impl/BatchExecItemLogDaoImpl.java`
    - `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml`
    - `src/com/scheduler/stock/sql/oracle/BatchExecLogMapper.xml`
    - `src/com/scheduler/stock/sql/oracle/TradeDateMapper.xml`
    - `webapp/WEB-INF/resources/service/stockService.xml`
    - `_workflow/canvas/MASTER_WORKFLOW.canvas`
    - `_workflow/notes/92_SOURCE_SYNC_MATRIX.md`
    - `_workflow/notes/MASTER_WORKFLOW_NOTE.md`

- Canvas ID `WF-3-1A`
  - Change Type `MODIFY`
  - Source Files
    - `db/DDL_TB_REC_SIGNAL.sql`
    - `db/DDL_TB_TRADE_CALENDAR.sql`
    - `db/DDL_TB_BATCH_EXEC_ITEM_LOG.sql`
    - `db/DDL_DROP_TB_STK_PRICE_TABLES.sql`
    - `db/tools/DbRecSignalDdlRunner.java`
    - `webapp/WEB-INF/resources/config/mybatis/oracle/oracle_mybatis-config.xml`
    - `_workflow/canvas/MASTER_WORKFLOW.canvas`
    - `_workflow/notes/92_SOURCE_SYNC_MATRIX.md`
    - `_workflow/notes/MASTER_WORKFLOW_NOTE.md`
    - `_workflow/notes/30_BUSINESS_LOGIC_FLOW.md`
    - `_workflow/notes/40_DB_FLOW.md`
    - `_workflow/notes/33_REC_SIGNAL_V110_IMPLEMENTATION_PREP.md`
