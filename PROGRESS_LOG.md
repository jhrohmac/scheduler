# 진행도 로그 (append-only)

## 2026-02-10
- 포지션 진입 전 평가 API 추가: `/finance/evaluatePositionPlan.do`
- PositionRuleEngine / PositionPlanVo / PositionSignalVo 추가
- KIS Finance 차트에 ENTRY/STOP/TP1 PlotLine 오버레이 연결
- PlotLine 라벨에 상태 한글 표기 추가
- ChartScript 로드 이후 훅이 걸리도록 보완(오버레이 안정화)
- run.sh 안정화: daemon + CATALINA_PID
- (설계 전환) 배치 기반 분석 강화(분석→DB저장→추천) 중심으로 로드맵 재정렬

- 코드/SQL 스캔으로 기존 배치/추천 파이프라인 파악 완료
  - TB_S_BATCH_JOB_CTRL 기반 스케줄/락
  - TB_S_STOCK_ANALYSIS(일봉) / TB_S_STOCK_30MINANALYSIS(30분) MERGE 저장
  - selectRecommendStocks 점수 기반 추천 쿼리 존재

- (설계 결정) 신호 이벤트 이력 테이블을 추가하기로 확정(추천 사유/백테스트/튜닝 목적)
  - DDL 초안 작성: `db/DDL_TB_S_SIGNAL_EVENT.sql`
- (구현) Oracle에 TB_S_SIGNAL_EVENT 생성 완료 + 인덱스 3개 생성
  - 실행 스크립트 추가: `db/apply_signal_event_ddl.sh` (JDBC로 create-if-missing)
- (구현) MyBatis mapper + DAO 추가
  - `oracle_SignalEvent.xml` / `SignalEventDao(Impl)`
- (구현) 배치 분석에 신호 이벤트 로깅 추가(최소 2개)
  - DAY: `DAILY_TREND_OK`
  - 30MIN: `PULLBACK_NEAR_MA20`

- (구현) 신호 중복 방지: TB_S_SIGNAL_EVENT insert를 MERGE(upsert)로 변경
  - 중복 기준: (STOCK_CODE, TIMEFRAME, SIGNAL_TYPE, EVENT_TIME)

- (구현) BullishMomentum를 일봉 배치에 통합하여 TB_S_SIGNAL_EVENT에 기록
  - `BULLISH_MOMENTUM_LONG_BULL_BODY`
  - `BULLISH_MOMENTUM_CONSECUTIVE_BULL`
  - 최근 봉(마지막 캔들)에서 발생한 신호만 저장하여 노이즈 최소화

- (요구사항) 국내장: KOSPI/KOSDAQ 지수를 종목과 동일하게 분석/저장하고, 추천 점수에 반영(시장 레짐)
- (확장 요구사항) 미국 시장도 동일하게 시장 레짐(지수) 점수 반영
- (탐색/정식화) KIS 국내지수 코드(0001/2001/1001)가 프로젝트 코드상 "국내지수"로 이미 지원됨
  - `StockCodeInfoDaoImpl.isDomesticIndexCode()`가 0001/2001/1001을 인식
- (DB 반영) market regime 자산을 interest_stock에 정식 등록 및 활성화
  - KR index: 0001(KOSPI), 2001(KOSDAQ), 1001(KOSPI200)
  - US index(선호): .DJI(다우), SPX(S&P500), COMP(나스닥 종합)
  - US proxy(fallback): SPY/QQQ/IWM도 유지
  - 적용 스크립트: `db/upsert_market_regime_assets.sh`
- (검증) KIS 해외지수 차트 API 코드 탐색 완료
  - .DJI, SPX, COMP, NDX 가 실제 데이터 반환 확인
  - probe 스크립트: `db/kis_probe_overseas_index.sh`
- (구현) 배치 일봉 캔들 fetch 확장
  - 국내지수(0001/2001/1001): indexchartprice fallback(U/UN/J)
  - 해외지수(.DJI/SPX/COMP/NDX): overseas chartprice(marketDiv=N)
- (구현) 추천 점수에 시장 레짐 가감점 반영(정식 코드 우선 + 다중 fallback)
  - KR: 0001+2001 평균 → 없으면 KRX300
  - US: .DJI+SPX+COMP 평균 → 없으면 SPY+QQQ+IWM → 없으면 SOX+RUT

- (구현) 배치 대상 포함 보장
  - market regime 자산을 watchlist 그룹 0000에 upsert: `db/upsert_watchlist_market_regime.sh`
  - US 배치 타겟 SQL에서 market_regime 태그 포함 허용
- (장애/복구) MyBatis XML 주석의 '&' 문자로 앱 기동 실패 → 'SP500'로 수정하여 복구
- (구현) 해외지수(KIS overseas daily chartprice) Output2 필드가 POJO에 매핑되지 않아 US 지수 분석 실패
  - 실제 필드키: stck_bsop_date, ovrs_nmix_prpr/oprc/hgpr/lwpr, acml_vol
  - Output2를 JsonAnySetter(Map)로 파싱 + 키 매핑 로직 추가
- (검증) US 3대 지수(.DJI/SPX/COMP) 일봉 분석 저장 성공 (TB_S_STOCK_ANALYSIS 최신 스냅샷 확인)

- (구현) 추천 사유 2단계 처리
  - 1) TB_S_SIGNAL_EVENT 최근 3개 메시지를 `reco_event_summary`로 노출
  - 2) 이벤트가 비어있을 때는 분석 필드 기반 요약(일봉/30분 MACD, 거래량, 정배열)을 fallback로 노출
- (구현) 배치가 핵심 구성요소 이벤트를 추가로 기록하여 추천 상위 종목의 이벤트가 빈 경우를 최소화
  - DAY: DAILY_SMA_LINES, DAILY_MACD_{BUY/SELL}, DAILY_VOLUME_{UP/DOWN}
  - 30MIN: M30_MACD_{BUY/SELL}, M30_VOLUME_{UP/DOWN}
- (구현) 프론트 UI에 `reco_event_summary` 표시(초록 박스)

(다음 작업) Phase1: 추천 리스트에 시장 레짐(지수) 점수/요약도 함께 표시 + 이벤트 메시지 한글 정리
