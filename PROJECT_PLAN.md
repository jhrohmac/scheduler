# Scheduler 프로젝트 설계/실행 플랜

## 목표(큰 맥락)

**배치 기반 분석 파이프라인을 강화**하여,
1) 여러 차트 모듈로 기술적 분석을 수행하고
2) 분석 결과/점수/신호를 DB에 저장한 뒤
3) 상승추세(또는 상승추세 진입) 종목을 골라 **추천 종목으로 노출**한다.

추가 목표(포지션 관리):
- 상승 시: **원금 회수(리스크 0화) → 추세 추종**
- 하락 시: **손실 최소화 또는 구조가 유지되면 반등까지 생존**

---

## 운영 기본값(결정됨)

- Universe(추천 대상): **혼합**
  - 1순위: 관심종목(워치리스트)
  - 2순위: 조건 충족 시 KOSPI/KOSDAQ로 확장(부하 제어)
- 배치 주기:
  - 일봉 분석: **하루 1회(장 마감 후)**
  - 30분봉 분석: **장중 30분마다** (워치리스트 우선)
- 추천 출력:
  - **점수 기반 Top N + 신호 이벤트 기반(둘 다)**

---

## 핵심 원칙

- 기존 운영 로직은 참고하되, "분석→DB 저장→추천"이 명확한 파이프라인이 되게 재설계
- **시장 레짐(지수: KOSPI/KOSDAQ)을 종목과 동일하게 분석/저장**하고, 추천 점수에 반영
  - 이유: 지수의 상승/하락 레짐이 개별 종목의 기대수익/성공확률을 크게 좌우
- 내부 상태(state)는 영문으로 유지하되, UI 표시는 한글로 노출
- 일봉(상위 프레임)으로 상승 추세 필터링(후보군 축소)
- 30분봉(하위 프레임)으로 진입 타이밍/리스크를 정밀화
- 먼저 **배치 분석 품질/저장 모델/추천 산출**을 완성하고, UI는 그 결과를 설명하는 방향으로 강화

---

## 현재 구현된 것 (2026-02-10 기준)

> 참고: 아래는 UI/검증 도구로도 유용하지만, 프로젝트 코어는 "배치 분석 → DB 저장 → 추천"을 중심으로 강화한다.

### 이미 존재하는 배치/추천 파이프라인(코드/SQL 스캔 결과)

1) 배치 스케줄러 틱
- `BatchJobCtrlController.batchSchedulerTick()`
  - 1분마다 실행
  - 실제 배치 실행/주기는 DB 테이블 `TB_S_BATCH_JOB_CTRL`로 제어

2) 배치 제어 테이블/락
- MyBatis: `oracle_BatchJobCtrl.xml`
  - `isDue`, `tryLock`, `finish` 등 제공

3) 일봉/30분봉 분석 저장(핵심)
- 구현: `BatchJobCtrlDaoImpl`
  - `refreshStockSmaAnalysis()` → `analysisTable=TB_S_STOCK_ANALYSIS` (일봉)
  - `refreshStock30MinAnalysis()` → `analysisTable=TB_S_STOCK_30MINANALYSIS` (30분)
- 대상 종목 선정 SQL: `oracle_BatchStock.xml`
  - `batchMarketStockList` (관심그룹 0000)
  - `batchGroupStockList` (관심그룹 전체)
  - `batchGroupOrderStockList` (전체 확장 + 가격 범위로 부하 제어)
- 분석 결과 저장 SQL: `oracle_BatchStock.xml`의 `mergeInterastStockAnalsis`
  - `${analysisTable}`에 SMA/MACD/거래량/라인넘버/종가 등을 MERGE

4) 추천 종목 리스트(이미 점수 기반)
- MyBatis: `oracle_StockAnalysis.xml`의 `selectRecommendStocks`
  - `TB_S_INTEREST_STOCK` + `TB_S_STOCK_ANALYSIS`(일봉) + `TB_S_STOCK_30MINANALYSIS`(30분) + `TB_S_STOCK_CONSENSUS` 조합
  - 점수(`SCORE_NUM`) 계산 후 등급(STRONG_BUY/BUY/WEAK_BUY/...) 및 Top N 반환

즉, 프로젝트는 이미 "분석→DB 저장→점수 추천"의 골격이 존재하며,
우리는 이 파이프라인을 **(1) 분석 품질 강화**하고 **(2) 저장 모델(사유/신호)을 표준화**하고
**(3) 상승추세/진입타이밍 로직을 더 정확히** 만드는 방향으로 확장하면 된다.

### Backend
- `POST /scheduler/finance/evaluatePositionPlan.do`
  - 입력: dailyChartData + m30ChartData
  - 출력: plan(state, entry/stop/R/tp1) + signals
- `PositionRuleEngine.evaluatePreEntry()` (MVP)
  - 일봉: close > MA60 필터
  - 30분봉: MA20 재진입(눌림반등) 힌트
  - stop: 최근 저점(lookback) 기반
  - TP1: +1R

### Frontend
- `kisFinancePage.js`
  - 차트 렌더 후 플랜 평가 호출 → ENTRY/STOP/TP1 PlotLine 오버레이
  - 상태 라벨 한글 매핑 추가

### Ops
- `run.sh` 안정화: daemon 모드 + `CATALINA_PID` 고정

---

## 로드맵(배치 강화 중심)

### Phase 1 — 배치 분석 파이프라인/저장 모델 정리 (최우선)
- [x] 배치 Job Inventory 정리(현재 어떤 배치가 무엇을 하는지)  *(코드/SQL 스캔으로 확인 완료)*
- [ ] 분석 결과 저장 모델 표준화
  - `TB_S_STOCK_ANALYSIS` / `TB_S_STOCK_30MINANALYSIS`는 "스냅샷(최신 상태)" 역할 유지
  - 추천의 설명가능성과 백테스트를 위해 **신호 이력 테이블을 추가**(결정됨)
    - `TB_S_SIGNAL_EVENT` (종목/타임프레임/신호타입/시각/파라미터/메시지)
- [ ] 추천 산출 기준(TrendFilter + EntryTiming) 점수 체계 정의
- [ ] 관심종목 우선 + 확장 유니버스 부하 제어 전략 확정

### Phase 2 — 기술적 분석 모듈 확장/재사용
- [ ] **시장 레짐(지수) 점수화/반영**
  - KOSPI/KOSDAQ 지수를 종목과 동일하게 분석/저장(일봉/30분)
  - 지수 점수(상승/중립/하락)를 종목 추천 점수에 가감점으로 반영
- [ ] 일봉 상승 추세 필터 강화(MA 구조/기울기/거래량/과열 방지)
- [ ] 30분봉 진입 타이밍 강화(눌림 반등 + 돌파 진입)
- [ ] stop/리스크 모델 고도화(스윙로우 + ATR 옵션)
- [ ] BullishMomentum 등 신규 신호를 배치 산출에 통합

### Phase 3 — UI 설명력 강화(차트/추천 이유)
- [ ] 추천 종목 리스트에 “추천 사유(한글)” 노출
- [ ] 차트에서 추천 사유와 일치하는 마킹(ENTRY/STOP/TP1 + 이벤트)
- [ ] 플랜 카드(우측 패널) 추가

### Phase 4 — 성과 측정/백테스트/튜닝
- [ ] 신호 후 N일 성과(승률/손익비/MAE/MFE)
- [ ] 파라미터 튜닝 루프(로그/리포트 자동화)

### Phase 5 — 포지션 관리(선택, 마지막)
- [ ] RISK_ON/RISK_OFF 상태 도입(실포지션 입력 또는 가상 포지션)
- [ ] +1R 도달 시: 50% 익절 + stop=entry
- [ ] 추세 추종: trailing stop(ATR) 또는 MA 이탈 규칙

---

## 상태 라벨 (UI 표기)

- NO_POSITION: 관망
- WATCH: 관심
- ENTRY_READY: 진입준비
- RISK_ON: 리스크ON
- RISK_OFF: 원금회수
- EXIT: 청산
- INVALIDATED: 무효(진입금지)
