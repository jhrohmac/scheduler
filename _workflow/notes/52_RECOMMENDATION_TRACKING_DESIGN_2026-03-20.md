# 추천 종목 관리/추적 확장 설계서

기준일: 2026-03-20  
프로젝트: `scheduler`  
원천 데이터 원칙: KIS API 정본 기준

## 1. 문서 목적

현재 프로젝트의 추천신호, 관심종목, 보유종목 구조를 기준으로 아래 6개 요구를 안전하게 확장하기 위한 프로그램 설계안을 정의한다.

1. 추천 종목을 관심종목으로 저장
2. 추천 종목의 정보 저장
3. 추천 종목의 정보를 토대로 추적 확인
4. 추천 종목을 관심종목에 등록한 날짜 이후 상승/하락 추적
5. 추천 종목 정보를 토대로 매수 했을때 일자별 성공 확률 계산
6. 매수한 종목을 매일 추적 업데이트 하여 매도 시점 표시

이 문서는 소스 구조와 금융 데이터셋 관점의 제약을 함께 반영한다. 핵심 원칙은 "화면용 관심종목"과 "분석용 추천 이력/성과 이력"을 분리하고, 모든 성과 계산의 정본은 KIS 일봉 데이터와 거래일 캘린더를 사용한다는 점이다.

## 2. 현재 소스 기준 현황 분석

### 2-1. 추천신호 생성/조회

현재 추천신호 v1.10은 `stock` 패키지에서 독립적으로 운영 중이다.

- 추천 계산 진입점: `src/com/scheduler/stock/web/RecSignalController.java`
- 추천 계산 서비스: `src/com/scheduler/stock/service/RecSignalService.java`
- KIS 일봉 수집: `src/com/scheduler/stock/service/KisDlyPriceSyncService.java`
- 거래일 처리: `src/com/scheduler/stock/service/TradeDateService.java`
- 결과 저장: `db/DDL_TB_REC_SIGNAL.sql`
- 조회 SQL: `src/com/scheduler/stock/sql/oracle/RecSignalMapper.xml`

현재 구조는 다음과 같다.

`KIS 일봉 -> MA/월상승/추세강도 계산 -> TB_REC_SIGNAL 저장 -> 추천신호 리스트/상세 조회`

즉, 추천 결과 자체는 이미 KIS 기반으로 잘 분리되어 있다. 다만 추천 결과를 사용자가 선택한 뒤 어떻게 추적할지에 대한 영속 구조는 아직 없다.

### 2-2. 관심종목 관리

관심종목은 `finance` 패키지에서 관리한다.

- 컨트롤러: `src/com/scheduler/finance/controller/MarketSummaryController.java`
- 보조 조회: `src/com/scheduler/finance/controller/InterestWatchlistController.java`
- DAO 구현: `src/com/scheduler/finance/dao/impl/MarketSummaryDaoImpl.java`
- SQL: `src/com/scheduler/finance/sql/oracle/oracle_MarketSummary.xml`

현재 `TB_S_INTEREST_WATCHLIST` 는 사실상 "좌측 패널 표시용 목록" 역할이다.

현재 저장되는 핵심 정보:

- `STOCK_GROUP`
- `STOCK_CODE`
- `STOCK_CLOSE`
- 화면용 정렬/표시 컬럼

현재 부족한 점:

- 어떤 추천신호에서 저장되었는지 없음
- 저장 당시 추천등급/사유/기준가 없음
- 이후 성과 추적용 anchor 정보 없음
- 사용자 선택 이력 누적 구조 없음

즉, 현재 관심종목 테이블만으로는 요구사항 2~5를 만족할 수 없다.

### 2-3. 보유종목/매수 이력 관리

보유종목은 이미 별도 구조가 존재한다.

- 컨트롤러: `src/com/scheduler/finance/controller/PositionController.java`
- SQL: `src/com/scheduler/finance/sql/oracle/oracle_Position.xml`
- 스키마: `db/DDL_TB_S_POSITION.sql`

현재 테이블:

- `TB_S_POSITION`
- `TB_S_POSITION_TXN`
- `TB_S_POSITION_EVENT`

현재 가능한 기능:

- 보유종목 등록
- 삭제(청산)
- 물타기(추가매수)
- 이벤트 로그 적재

현재 부족한 점:

- 추천신호와 포지션이 연결되어 있지 않음
- 매수 당시 추천정보 스냅샷이 없음
- 일자별 성과 테이블이 없음
- 자동 매도 시점 판정 로직이 없음

### 2-4. 실시간/현재가 경로

현재가 갱신은 이미 KIS API 기반 경로가 있다.

- 관심종목 현재가 조회: `src/com/scheduler/finance/dao/impl/MarketSummaryDaoImpl.java`
- 관심종목 실시간 폴링/WS 허브: `src/com/scheduler/finance/websocket/WatchlistQuoteHub.java`

따라서 실시간 현재가와 EOD 성과 계산을 완전히 새로 만들 필요는 없다. 다만 "공식 성과 계산"은 실시간 현재가가 아니라 KIS 일봉 종가/고가/저가 기준으로 정산해야 한다.

## 3. 요구사항별 갭 분석

| 요구사항 | 현재 가능 여부 | 부족한 점 | 설계 방향 |
|---|---|---|---|
| 추천 종목을 관심종목으로 저장 | 부분 가능 | 추천 맥락이 사라짐 | 저장 시 추천 스냅샷 동시 적재 |
| 추천 종목의 정보 저장 | 불가 | 추천등급/사유/기준가 이력 없음 | 별도 Pick/Snapshot 테이블 도입 |
| 추천 종목 정보 기반 추적 확인 | 불가 | 추적용 일별 테이블 없음 | Pick Daily Track 배치 도입 |
| 등록일 이후 상승/하락 추적 | 불가 | watch anchor 없음 | 등록 시점 가격 anchor 저장 |
| 매수했을 때 일자별 성공 확률 계산 | 불가 | buy anchor, cohort stats 없음 | buy anchor + 일별 확률 view 도입 |
| 매수 종목 매일 추적/매도 시점 표시 | 부분 가능 | 포지션 연결/exit rule 부재 | Position 연계 + exit state machine 추가 |

## 4. 금융 데이터셋 관점의 핵심 설계 원칙

### 4-1. 원천 데이터는 KIS API로 고정

정본 소스는 아래 두 계층으로 분리한다.

- EOD 평가 정본: `KisDlyPriceSyncService` 가 수집하는 KIS 일봉(보정 종가/고가/저가 포함)
- 실시간 참고값: `MarketSummaryDaoImpl`, `WatchlistQuoteHub` 가 수집하는 KIS 현재가

원칙:

- 승률, Return, MFE, MAE, 목표가 도달 여부는 KIS 일봉 기준으로 계산
- 실시간 현재가는 "임시 상태 표시" 용도로만 사용
- 장 마감 후 EOD 배치가 돌고 나서 최종 수치 확정

### 4-2. Look-ahead bias 방지

추천 저장 시점에 아래 정보를 얼려서 보관해야 한다.

- 추천 기준일
- 추천 등급
- 추천 사유
- 당시 가격
- 당시 추세/월간 상승/거래대금 지표

이후 `TB_REC_SIGNAL` 이 다음 날 덮여도 과거 추천의 성과는 변하면 안 된다.

### 4-3. 거래일 기준 계산

이미 `TB_TRADE_CALENDAR` 와 `TradeDateService` 가 있으므로 모든 N일 계산은 거래일 기준으로 처리한다.

- D+1, D+5, D+20, D+60 은 달력일이 아니라 거래일 기준
- 국내/해외 휴장일이 다르므로 `MKT_CD` 별로 계산

### 4-4. Anchor를 3개로 분리

요구사항을 정확히 만족하려면 기준 가격을 하나로 두면 안 된다.

1. 추천 anchor
   - 추천 시스템 품질 측정용
   - 보통 `TB_REC_SIGNAL.CUR_PRICE` 또는 `BASE_DT` 기준 종가
2. 관심종목 등록 anchor
   - "등록한 날짜 이후 상승/하락" 추적용
   - 저장 시점 현재가 또는 당일 종가
3. 실제 매수 anchor
   - 실제 포지션 성과/매도 시점 판단용
   - `TB_S_POSITION` 최초 BUY 기준 가격

### 4-5. 확률은 룰 기반 집계로 시작

초기 버전의 "성공 확률"은 모델 예측값이 아니라 과거 cohort 집계값으로 시작하는 것이 안전하다.

예시:

- 같은 시장
- 같은 추천등급
- 유사한 추세 강도 구간
- 같은 buy timing rule
- 같은 보유일수

이 조합의 과거 표본에서

- 수익률 > 0 비율
- TP1 도달 비율
- 손절 먼저 도달한 비율

을 계산해 확률로 제시한다.

## 5. 목표 아키텍처

### 5-1. 전체 흐름

1. 추천신호 배치가 `TB_REC_SIGNAL` 을 생성한다.
2. 사용자가 추천종목을 관심종목으로 저장한다.
3. 저장 시 `TB_S_INTEREST_WATCHLIST` 와 별도로 추천 스냅샷 이력을 적재한다.
4. 일별 추적 배치가 KIS 일봉과 거래일 캘린더를 이용해 상승/하락, Return, MFE, MAE 를 누적 계산한다.
5. 사용자가 매수하면 기존 `TB_S_POSITION` 에 포지션을 만들고 추천 스냅샷과 연결한다.
6. 포지션 추적 배치가 매일 매도 시점 후보를 계산하고 이벤트를 남긴다.
7. 확률 조회 API 는 누적된 pick/eval 데이터를 cohort 기준으로 집계해 반환한다.

### 5-2. 패키지 권장 분리

권장 패키지:

- 추천 선택/추적 도메인: `src/com/scheduler/stock/...`
- 관심종목/포지션 기존 도메인 연동: `src/com/scheduler/finance/...`

권장 신규 구성:

- `src/com/scheduler/stock/web/RecPickController.java`
- `src/com/scheduler/stock/service/RecPickService.java`
- `src/com/scheduler/stock/service/RecPickTrackService.java`
- `src/com/scheduler/stock/service/RecProbabilityService.java`
- `src/com/scheduler/stock/service/PositionExitSignalService.java`
- `src/com/scheduler/stock/dao/RecPickDao.java`
- `src/com/scheduler/stock/dao/impl/RecPickDaoImpl.java`
- `src/com/scheduler/stock/sql/oracle/RecPickMapper.xml`
- `src/com/scheduler/stock/batch/RecPickDailyBatch.java`
- `src/com/scheduler/stock/batch/PositionMonitorDailyBatch.java`

이유:

- 추천 정본과 추적 정본은 `stock` 패키지에 두는 것이 자연스럽다.
- 관심종목과 포지션은 기존 `finance` DAO 를 재사용한다.
- 추천 로직과 포지션 표시 로직을 한 서비스에 섞지 않는다.

## 6. DB 설계

## 6-1. 신규 테이블: `TB_S_RECO_PICK`

목적:

- "사용자가 추천종목을 선택/저장한 사실"과 "선택 당시 추천 스냅샷"을 저장

권장 핵심 컬럼:

- `PICK_ID`
- `SOURCE_TYPE` : `REC_SIGNAL_V110`
- `SOURCE_BASE_DT`
- `SOURCE_MKT_CD`
- `STK_CD`
- `STK_NM`
- `LISTING_MARKET`
- `WATCH_GROUP_ID`
- `PICK_STATUS` : `WATCH`, `BOUGHT`, `CLOSED`, `CANCELLED`
- `REC_RANK`
- `REC_YN`
- `REC_GRADE`
- `REC_REASON`
- `GOLDEN_YN`
- `MON_UP_YN`
- `MON_CHG_RATE`
- `TREND_STRENGTH`
- `AVG_TRD_VAL_20`
- `REC_ANCHOR_PRICE`
- `WATCH_ANCHOR_PRICE`
- `WATCH_SAVED_AT`
- `FIRST_BUY_DATE`
- `FIRST_BUY_PRICE`
- `FIRST_BUY_QTY`
- `POSITION_ID`
- `ENTRY_RULE_CODE`
- `SNAPSHOT_JSON`
- `LAST_TRACK_DT`
- `CREATE_USER`
- `CREATE_DATE`
- `MODIFY_DATE`

설계 포인트:

- `TB_S_INTEREST_WATCHLIST` 는 화면용 목록으로 유지한다.
- 분석/통계/복기용 데이터는 전부 `TB_S_RECO_PICK` 에 저장한다.
- `SNAPSHOT_JSON` 에는 당시 추천 DTO 전체를 보관해 향후 컬럼이 늘어나도 이력 재현이 가능하도록 한다.

## 6-2. 신규 테이블: `TB_S_RECO_PICK_DAILY`

목적:

- pick 단위의 일별 추적 결과 저장

권장 핵심 컬럼:

- `PICK_ID`
- `TRADE_DT`
- `MKT_CD`
- `STK_CD`
- `OPEN_PRICE`
- `HIGH_PRICE`
- `LOW_PRICE`
- `CLOSE_PRICE`
- `HOLD_DAY_NO_FROM_REC`
- `HOLD_DAY_NO_FROM_WATCH`
- `HOLD_DAY_NO_FROM_BUY`
- `RETURN_FROM_REC_PCT`
- `RETURN_FROM_WATCH_PCT`
- `RETURN_FROM_BUY_PCT`
- `MFE_FROM_REC_PCT`
- `MAE_FROM_REC_PCT`
- `MFE_FROM_BUY_PCT`
- `MAE_FROM_BUY_PCT`
- `UP_DOWN_FLAG`
- `TP1_HIT_YN`
- `STOP_HIT_YN`
- `SELL_SIGNAL_CODE`
- `SELL_SIGNAL_TEXT`
- `PRICE_SOURCE` : `KIS_DAILY`
- `CREATE_DATE`
- `MODIFY_DATE`

설계 포인트:

- 이 테이블이 요구사항 3, 4, 5, 6의 핵심 정본이다.
- 등록 추적과 매수 추적을 동시에 지원하려면 return/mfe/mae 를 anchor 별로 분리 저장하는 것이 가장 안전하다.

## 6-3. 신규 테이블: `TB_S_RECO_PICK_EVAL`

목적:

- 자주 쓰는 horizon 결과를 확정 저장

권장 PK:

- `PICK_ID + EVAL_TYPE + HOLD_DAYS`

권장 컬럼:

- `PICK_ID`
- `EVAL_TYPE` : `REC`, `WATCH`, `BUY`
- `HOLD_DAYS`
- `ANCHOR_DATE`
- `TARGET_DATE`
- `RETURN_PCT`
- `MFE_PCT`
- `MAE_PCT`
- `WIN_YN`
- `TP1_HIT_YN`
- `STOP_HIT_YN`
- `CALC_STATUS` : `READY`, `PENDING`
- `CREATE_DATE`
- `MODIFY_DATE`

사용 이유:

- 화면에서 5/20/60일 성과를 빠르게 조회 가능
- 배치 재계산/검증이 쉬움
- 확률 집계의 원본으로 활용 가능

## 6-4. 신규 View 또는 집계 테이블: `VW_S_RECO_PICK_STATS`

목적:

- 일자별 성공 확률 조회

초기 집계 차원:

- `EVAL_TYPE`
- `HOLD_DAY_NO`
- `SOURCE_MKT_CD`
- `LISTING_MARKET`
- `REC_GRADE`
- `ENTRY_RULE_CODE`
- `TREND_BUCKET`
- `MONTH_BUCKET`

초기 집계 지표:

- `SAMPLE_CNT`
- `WIN_RATE`
- `TP1_RATE`
- `STOP_RATE`
- `AVG_RETURN_PCT`
- `AVG_MFE_PCT`
- `AVG_MAE_PCT`

권장 사항:

- 초기에는 View 로 시작
- 성능 문제 시 야간 집계 테이블로 승격
- `TREND_BUCKET` 은 고정 임계값보다 누적 데이터의 분위수 기준으로 나누는 것이 바람직

## 6-5. 기존 테이블 변경: `TB_S_POSITION`

현재 포지션 구조를 유지하되 추천 연결 정보를 추가한다.

권장 추가 컬럼:

- `SOURCE_PICK_ID`
- `BUY_DATE`
- `TARGET_PRICE`
- `STOP_PRICE`
- `TP1_PRICE`
- `SELL_GUIDE_STATE`
- `LAST_SELL_SIGNAL_CODE`
- `LAST_TRACK_DATE`

권장 상태값:

- `HOLD`
- `TP1_DONE`
- `TRAILING`
- `SELL_READY`
- `STOP_LOSS`
- `CLOSED`

## 6-6. 기존 테이블 유지 원칙

### `TB_S_INTEREST_WATCHLIST`

- 유지
- 화면 렌더링/실시간 현재가 표시용
- 추천 이력 정본으로 사용하지 않음

### `TB_REC_SIGNAL`

- 유지
- 추천 결과 스냅샷 정본
- 사용자가 저장한 이후의 사용자 액션 이력은 별도 테이블에서 관리

### `TB_TRADE_CALENDAR`

- 재사용
- D+N 계산의 기준 테이블

## 7. 서비스/배치 설계

## 7-1. 추천 저장 서비스

서비스명 예시:

- `RecPickService.saveToWatchlistFromRecommendation()`

처리 순서:

1. `TB_REC_SIGNAL` 에서 `baseDt + mktCd + stkCd` 기준 추천 스냅샷 조회
2. 현재 KIS 현재가 또는 당일 종가 확보
3. `TB_S_INTEREST_WATCHLIST` 에 종목 저장
4. `TB_S_RECO_PICK` 에 추천 스냅샷 저장
5. 응답으로 `pickId`, `watchSavedAt`, `watchAnchorPrice` 반환

중요 포인트:

- 관심종목 추가 실패 시 pick 저장도 rollback
- pick 저장 성공 후 중복 저장 방지를 위해 `WATCH_GROUP_ID + SOURCE_BASE_DT + STK_CD + PICK_STATUS in WATCH/BOUGHT` 유니크 전략 고려

## 7-2. 일별 추적 배치

배치명 예시:

- `RecPickDailyBatch`

대상:

- `TB_S_RECO_PICK` 중 `PICK_STATUS in (WATCH, BOUGHT)`

처리 순서:

1. `LAST_TRACK_DT` 이후 미계산 거래일 목록 계산
2. KIS 일봉 조회
3. `TB_TRADE_CALENDAR` 보강
4. `TB_S_RECO_PICK_DAILY` upsert
5. 5/20/60일 등 주요 horizon 은 `TB_S_RECO_PICK_EVAL` 갱신
6. `LAST_TRACK_DT` 업데이트

주의:

- 동일 종목을 여러 pick 이 공유하므로 KIS 조회는 종목 단위 캐시 후 pick 별로 계산
- 국내/해외 시장을 분리해 배치 수행

## 7-3. 매수 등록 서비스

서비스명 예시:

- `RecPickService.registerBuyFromPick()`

처리 순서:

1. `pickId` 조회
2. 기존 `PositionController` 의 ADD 흐름과 동일하게 `TB_S_POSITION` 생성
3. `TB_S_POSITION_TXN` 에 BUY 트랜잭션 적재
4. `TB_S_RECO_PICK` 에 `FIRST_BUY_DATE`, `FIRST_BUY_PRICE`, `POSITION_ID`, `PICK_STATUS=BOUGHT` 저장
5. 초기 `TARGET_PRICE`, `STOP_PRICE`, `TP1_PRICE` 는 `PositionRuleEngine` 또는 별도 buy plan rule 로 계산

## 7-4. 포지션 추적/매도 시점 배치

배치명 예시:

- `PositionMonitorDailyBatch`

대상:

- `TB_S_POSITION.CLOSE_FLAG = 'N'`

처리 순서:

1. 연결된 `SOURCE_PICK_ID` 기준 추천 스냅샷 조회
2. KIS 일봉 기준 최신 성과 계산
3. 매도 룰 평가
4. `TB_S_POSITION_EVENT` 에 이벤트 적재
5. `TB_S_RECO_PICK_DAILY` 의 `SELL_SIGNAL_CODE/TEXT` 갱신
6. 필요 시 `TB_S_POSITION.SELL_GUIDE_STATE` 갱신

## 8. 성공 확률/성과 계산 로직

## 8-1. 기본 공식

거래일 기준 N일 성과:

- `Return_N = close_N / anchor_price - 1`
- `MFE_N = max(high_1..N) / anchor_price - 1`
- `MAE_N = min(low_1..N) / anchor_price - 1`

anchor 는 `REC`, `WATCH`, `BUY` 3종을 분리한다.

## 8-2. 등록일 이후 상승/하락 추적

요구사항 4의 의미는 "추천 저장 후 내가 watchlist 에 넣은 시점부터 얼마나 올랐는가/내렸는가" 이다.

따라서 공식 기준은 다음으로 둔다.

- anchor: `WATCH_ANCHOR_PRICE`
- 기준일: `WATCH_SAVED_AT` 의 시장별 유효 거래일
- 화면 지표:
  - 현재 수익률
  - 최고 상승률(MFE)
  - 최대 하락률(MAE)
  - 연속 상승/하락 일수

## 8-3. 매수 시 일자별 성공 확률

요구사항 5는 "이 추천 조합으로 매수했을 때 D+1, D+2, D+3 ... 시점의 성공 확률" 로 해석하는 것이 적절하다.

초기 성공 정의:

- `WIN`: `RETURN_FROM_BUY_PCT > 0`
- `TP1_SUCCESS`: `MFE_FROM_BUY_PCT >= risk_r`
- `FAIL_FAST`: `STOP_HIT_YN = 'Y'`

집계 기준 예시:

- `hold_day_no = 1, 2, 3, ..., 60`
- `market + rec_grade + entry_rule_code + trend_bucket`

예시 응답:

- D+1 승률 54.2%
- D+5 승률 61.8%
- D+20 TP1 도달 확률 43.1%

## 8-4. 매도 시점 표시 룰

초기 버전은 확률형보다 규칙형이 안전하다.

권장 1차 룰:

1. `STOP_LOSS`
   - 종가 또는 저가가 `STOP_PRICE` 하향 이탈
2. `TP1_REACHED`
   - 고가가 `TP1_PRICE` 이상 도달
3. `TRAILING_EXIT`
   - TP1 이후 종가가 `MA20` 하향 이탈
4. `TREND_BREAK`
   - 종가가 `MA60` 하향 이탈하거나 추천 상태가 약세로 전환
5. `TIME_EXIT`
   - 보유일수 초과 + 확률 급감 구간 진입

UI 표시용 상태:

- `보유`
- `부분매도 고려`
- `매도 준비`
- `손절 우선`
- `청산 완료`

## 9. API 설계

권장 신규 API:

- `/stock/recPick/saveToWatchlist.do`
- `/stock/recPick/list.do`
- `/stock/recPick/detail.do`
- `/stock/recPick/registerBuy.do`
- `/stock/recPick/dailyTrack.do`
- `/stock/recPick/probability.do`
- `/stock/position/sellGuide.do`

### 9-1. `saveToWatchlist.do`

입력:

- `baseDt`
- `mktCd`
- `stkCd`
- `watchGroupId`
- `userId`

출력:

- `pickId`
- `watchSavedAt`
- `watchAnchorPrice`
- `favYn`

### 9-2. `registerBuy.do`

입력:

- `pickId`
- `qty`
- `buyPrice`
- `buyDate`
- `userId`

출력:

- `positionId`
- `pickStatus`
- `targetPrice`
- `stopPrice`
- `tp1Price`

### 9-3. `probability.do`

입력:

- `pickId` 또는
- `mktCd + recGrade + entryRuleCode + holdDayNo`

출력:

- `sampleCnt`
- `winRate`
- `tp1Rate`
- `stopRate`
- `avgReturnPct`
- `avgMfePct`
- `avgMaePct`

## 10. UI 설계 포인트

### 10-1. 추천신호 리스트/상세

추가 버튼:

- `관심종목 저장`
- `매수 등록`
- `추적 보기`

### 10-2. 관심종목 화면

기존 watchlist 는 유지하되 추천 저장 종목에는 아래를 추가 노출한다.

- 추천등급
- 저장일 수익률
- 현재 추적 상태
- 매수 여부
- 매도 가이드 배지

### 10-3. 보유종목 화면

추가 노출:

- 연결된 추천등급/사유
- D+N 누적 수익률
- TP1/손절/추세이탈 상태
- 매도 시점 배지

## 11. 구현 단계 제안

### Phase 1. 이력 저장 기반 확보

- `TB_S_RECO_PICK` 생성
- 추천 저장 API 구현
- watchlist 저장과 snapshot 저장 연결

### Phase 2. 일별 추적 정본 구축

- `TB_S_RECO_PICK_DAILY`
- `TB_S_RECO_PICK_EVAL`
- `RecPickDailyBatch`

### Phase 3. 포지션 연결

- `TB_S_POSITION` 확장
- 매수 등록 API
- pick-position 연결

### Phase 4. 매도 가이드

- `PositionMonitorDailyBatch`
- exit rule
- 화면 배지/이벤트 표시

### Phase 5. 확률 고도화

- `VW_S_RECO_PICK_STATS`
- cohort 세분화
- 표본 수 부족 구간 신뢰도 표시

## 12. 권장 구현 우선순위

실무적으로는 아래 순서가 가장 안전하다.

1. 추천 저장 시 snapshot 을 잃지 않게 만드는 것
2. 등록일/매수일 기준 일별 성과를 계산하는 것
3. 포지션과 추천을 연결하는 것
4. 매도 시점 룰을 추가하는 것
5. 확률 지표를 노출하는 것

즉, 먼저 "데이터를 쌓는 구조"를 만들고 그 위에 "판단 로직"을 얹어야 한다.

## 13. 최종 결론

현재 프로젝트는 이미

- KIS 기반 추천신호 정본
- 관심종목 UI/실시간 시세
- 보유종목 CRUD

를 각각 가지고 있다.

이번 요구사항의 본질은 새 추천 엔진을 만드는 것이 아니라, 이 세 축 사이에 "사용자 선택 이력"과 "거래일 기반 성과 추적"을 연결하는 것이다.

가장 중요한 설계 결정은 아래 두 가지다.

1. `TB_S_INTEREST_WATCHLIST` 를 확장해서 모든 것을 해결하지 않는다.
2. `TB_S_RECO_PICK` 계열의 별도 추적 정본을 만들고, KIS 일봉과 거래일 캘린더를 기준으로 성과를 계산한다.

이 방식으로 가면 요구사항 1~6을 모두 수용하면서도, 이후 백테스트/튜닝/AI 보조 판단까지 같은 데이터셋을 재사용할 수 있다.
