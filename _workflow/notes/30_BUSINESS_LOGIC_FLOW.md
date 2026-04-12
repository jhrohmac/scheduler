# 2. 비즈니스 로직 단위 플로우

## 2-1 배치/분석 저장
- 캔들 수집 → 지표 계산(SMA/MACD/거래량 등) → 분석테이블 MERGE 저장
- 신호 이벤트 로깅(TB_S_SIGNAL_EVENT)

## 2-2 차트 분석 모듈
- 차트에 표시할 계산(ENTRY/STOP/TP, 상태 머신 기반 시그널)

### 현재 구현된 모듈/구성요소
- PositionRuleEngine (포지션 플랜/시그널 평가)
- StockAnalysisUtil (SMA/MACD/거래량 등 분석 유틸)
- BullishMomentumDetector (장대양봉/연속양봉 신호)
- MovingAveragesLineNumber (정배열/역배열 라인 순서 계산)
- CrossDown5v60 / CrossDown20v60 (DAY/30MIN 하향이탈 감지)
- Chart 오버레이 렌더(ENTRY/STOP/TP1 PlotLine)
- evaluatePositionPlan API (진입 전 규칙 평가)

### 2-2c 베이스 모듈 (신규 노드)
- **노드 ID:** `WF-2-2c-BASE`
- **모듈명:** `IndexTrendRuleModule`
- 목적: 마켓 지수 전용 추세/이벤트 판정 엔진을 2-2 차트분석 모듈 하위 베이스 모듈로 분리
- 기준 소스(SoT): `31_MARKET_TREND_RULES.md` (사용자 정의 기준)
- 판정 범위:
  - 우상향 조건(정배열+지그재그+120/240 간격 확대)
  - 5/20, 20/60, 60/240 이벤트
  - 매수 금지 규칙(240 하단)
- 출력:
  - 내부 이벤트 코드(`IDX_*`) + UI 한글 이벤트명 동시 제공
  - 차트 상단 배지/AI 검토/추천 필터 공통 사용
  - UI 용어/축약/영문 매핑은 `DD_DATA_DICTIONARY.md`의 "화면 표시용 증권 용어 표준" 및 "사용자 가이드(괄호 용어) 축약 표준" 준수

### 추가 필요 모듈(등록)
- ChartEventMarkerRenderer (TB_S_SIGNAL_EVENT를 차트 마커로 시각화)
- RegimeOverlayModule (KR/US 레짐 상태/점수 차트 상단 오버레이)
- PositionStateMachineModule (원금회수→추세추종→실패대응 상태 전환)
- RiskGuardModule (급락/갭/레짐 붕괴 시 경고/자동 방어 규칙)
- ExplainabilityPanelModule (현재 봉 기준 근거 TOP3/리스크 TOP2 패널)
- AiRecheckModule (선택 종목 AI 재검토 결과 카드 연동)

## 2-3 추천 종목 선별
- 분석 테이블 기반 점수화(0~100) + 등급
- 시장 레짐(지수) 가감점
- 설명가능성: 이벤트 요약(reco_event_summary)

## WF-2-3A 추천신호 조회/응답 런타임
- 설계서 v1.10 기준 추천신호 시스템 전용 조회/응답 런타임
- 현재 소스:
  - `src/com/scheduler/stock/web/RecSignalController.java`
  - `src/com/scheduler/stock/service/RecSignalService.java`
  - `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`
- 진입점:
  - `RecSignalController.list.do`
  - `RecSignalController.detail.do`
- 출력 기준:
  - `TB_REC_SIGNAL` 에 저장된 EOD 결과
  - `MKT_CD` 기준 KR / US 결과 분리
  - `BASE_DT`, `REC_GRADE`, `MON_CHG_RATE`, `TREND_STRENGTH`, `AVG_TRD_VAL_20`, `REC_REASON`
- 기존 `/finance/selectRecommendStocks.do` 와 분리 운영
- UI 연결 대상:
  - `stock/recSignalList`
  - `stock/recSignalDetail`

### WF-2-3A-1 조회 진입/UI 요청
- 화면 진입 후 `list.do`, `detail.do` 를 호출하는 시작 단계
- 전달 파라미터: `baseDt`, `stkCd`, `mktCd`

### WF-2-3A-2 조회조건/기준일 정규화
- 시장 필터 정규화
- 최신 기준일 조회 후 실제 읽을 기준일 보정
- 목록 조회 기본 조건 `recYn=Y` 적용

### WF-2-3A-3 추천신호 조회 응답
- `TB_REC_SIGNAL` list/detail 조회
- `DataTableSettingVo` 로 응답 변환
- 조회 런타임 마지막 단계

## WF-2-3B 추천신호 수집/계산/적재 배치
- 설계서 v1.10 기준 추천신호 배치 런타임
- 현재 소스:
  - `src/com/scheduler/stock/web/RecSignalController.java`
  - `src/com/scheduler/stock/batch/RecSignalBatch.java`
  - `src/com/scheduler/stock/batch/RecSignalDailyBatch.java`
  - `src/com/scheduler/stock/batch/RecSignalMarketScheduler.java`
  - `src/com/scheduler/stock/service/KisDlyPriceSyncService.java`
  - `src/com/scheduler/stock/service/RecSignalService.java`
  - `src/com/scheduler/stock/service/TradeDateService.java`
  - `src/com/scheduler/stock/service/MaCalculateService.java`
  - `src/com/scheduler/stock/sql/oracle/BatchExecLogMapper.xml`
  - `src/com/scheduler/stock/sql/oracle/BatchExecItemLogMapper.xml`
- 입력 정본:
  - `TB_STK_MASTER`
  - `TB_TRADE_CALENDAR`
- 진입점:
  - 정기 실행: `RecSignalMarketScheduler`
  - 수동 실행: `RecSignalController.runBatch.do`
- 출력:
  - `TB_REC_SIGNAL`
  - `TB_BATCH_EXEC_LOG`
  - `TB_BATCH_EXEC_ITEM_LOG`
- 원칙:
  - KIS API 응답을 메모리에서 계산하고 결과만 DB에 저장
  - `MONTH_OPEN_PRICE` 는 KIS 일봉 응답 중 해당월 첫 거래일 시가로 계산
  - 기존 `BatchJobCtrlController`, `TB_S_BATCH_JOB_CTRL` 는 재사용하지 않음
  - `RecSignalDailyBatch` 독립 스케줄로 운영
  - KR 02:00 / 05:00, US 16:00 / 20:00
  - 재시도는 실패 종목만 다시 실행
  - `RecSignalController.syncDlyPrice.do` 는 KIS fetch 검증용 보조 진입점으로 둔다

### WF-2-3B-1 배치 진입/스케줄
- `RecSignalMarketScheduler` 정기 실행
- `RecSignalDailyBatch` 기본 파라미터 주입
- `runBatch.do` 수동 실행 진입 포함

### WF-2-3B-2 배치 기준일/대상종목 결정
- `marketGroup`, `retryOnly`, `baseDt` 해석
- 거래일 보정 후 실제 기준일 확정
- 대상 종목 또는 실패건 재대상 구성

### WF-2-3B-3 KIS 일봉 수집
- KIS API 기준 KR/US 일봉 원천 조회
- `days`, `requestIntervalMs` 조건 반영

### WF-2-3B-4 추천신호 메모리 계산
- 월시가, 이평, 추세강도 계산
- `REC_YN`, `REC_GRADE`, `REC_REASON` 결정

### WF-2-3B-5 결과/실행 로그 적재
- `TB_REC_SIGNAL` 결과 저장
- `TB_BATCH_EXEC_LOG`, `TB_BATCH_EXEC_ITEM_LOG` 적재

### WF-2-3B-6 실패건 재시도
- `TB_BATCH_EXEC_ITEM_LOG` 의 FAIL 기준 재대상 선정
- `retryOnly=Y` 로 후속 재실행

## 2-4 보유 종목 관리
- (현재) 보유종목 조회 UI 존재
- (추가 필수) 보유종목 CRUD + 물타기(추가매수) 처리
  - ADD: 신규 보유 포지션 등록
  - DELETE: 보유 포지션 제거(청산/오입력 정리)
  - AVERAGE_DOWN: 추가매수 후 평단/수량/리스크 재계산
- (향후) 포지션 상태 머신(원금회수→추세추종→실패 대응) + 이벤트화

## 2-5 차트에 표시해줄 항목
- ENTRY/STOP/TP1 (완료)
- (확장) 신호 이벤트 마커(최근 3~10개), 레짐/점수 요약, 상태 머신 단계
- (추가) 실시간 매수/매도 시그널 배지(웹소켓 수신값 반영)

## (추가) 2-6 백테스트/튜닝 루프 (정확도 개선 핵심)
- 추천 스냅샷 저장(W1) → 거래일 캘린더(W2) → Return/MFE/MAE 계산(W3)
- 분해 분석(W4) → 파라미터 튜닝 실험(W5)

## (추가) 2-7 선택 종목 모니터링/정확도(Probability) 관리
- 목적: 사용자가 “선택한 추천 종목”이 실제로 맞았는지(이익/손실, MFE/MAE) 추적
- 입력: 사용자 선택 이벤트(어떤 run_id의 몇 rank/어떤 stock_code를 선택했는지)
- 처리:
  - 선택 시점(또는 선택 당시 anchor) 확정
  - 이후 5D/20D/60D 성과(Return/MFE/MAE) 업데이트
  - 신호/레짐/이벤트 조합별 성공확률 집계(예: 수익률>0 비율)
- 출력(UI): “내가 고른 종목 성적표(승률/평균 MFE/평균 MAE)” + 근거(당시 추천 사유)

## (추가) 2-8 AI 예측/재검토 엔진(On-demand)
- 목적: 사용자가 특정 종목을 고르면, 저장된 분석/이벤트/레짐 데이터를 다시 해석해 확률형 결과 제공
- 입력: stock_code + 옵션(5D/20D/60D, 보수/중립/공격)
- 출력: 상승/중립/하락 확률, 근거 TOP3, 리스크 TOP2, 권장 액션
- 원칙: 초기에는 자동매매 직접 트리거 금지(의사결정 보조)

## (추가) 2-9 실시간 신호 스트림(WebSocket)
- 목적: 실시간 시세/체결 데이터를 받아 차트에 즉시 매수/매도 신호 표시
- 입력: 웹소켓 tick/호가/체결 + 최근 분석 스냅샷
- 처리:
  - WS 기반 비동기 푸시를 기본 경로로 사용(동기 REST 대체)
  - 재연결 정책은 고정 주기 무한 재시도 대신 종료 코드 기반 + 지수 백오프 + 지터를 적용
  - 연결 성공은 `onopen` 이 아니라 실제 첫 데이터 수신 기준으로 확인
  - 동일 종목 집합은 정렬된 stable key 기준으로만 구독 변경 여부를 판단
  - invalid token / malformed codes 는 Endpoint 에서 에러 후 종료 정책 검토
  - 이벤트 최신성 보장: event_time/seq 기반으로 out-of-order 폐기
  - UI 반영은 스로틀(100~300ms)로 렌더 부하 제어
- 출력: 차트 신호 오버레이(매수/매도), 알림 이벤트, 상태 업데이트
- REST 역할: 초기 스냅샷/복구(fallback) 전용
- 상세 수정안: `32_WEBSOCKET_RECONNECT_HARDENING.md`
