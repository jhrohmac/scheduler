# DD 데이터사전 (용어 표준)

## 목적
- 프로젝트 소스/DB 전반의 용어를 표준화해서 같은 의미의 중복 용어를 줄이고, 이후 로직 변경 시 일관성을 유지한다.

## 수집 범위 (1차)
- `src/com/scheduler/finance/**/*.java`
- `src/com/scheduler/finance/sql/oracle/**/*.xml`
- `webapp/appone/jsp/finance/kis/kisFinance/**/*.js`
- `webapp/appone/jsp/finance/kis/kisFinance/**/*.jsp`
- `db/**/*.sql`

## 표준 네이밍 원칙 (초안 v1)
1. **DB 컬럼/테이블**: `UPPER_SNAKE_CASE`
2. **Java 필드/메서드**: `camelCase`
3. **JSON API key**: 기본 `snake_case` (프론트 호환 위해 legacy 키는 읽기만 허용)
4. **UI 문구**: 한글 기준, 내부코드(영문)와 분리
5. **이벤트 코드**: `EV_*` 또는 `IDX_*` 접두어로 도메인 구분

---

## 1차 수집 결과 (동일/유사 용어군)

### A. 명백한 대소문자 중복 (표준 1개로 통일 필요)
- `STOCK_CODE` / `stock_code`
- `STOCK_MARKET` / `stock_market`
- `STOCK_COUNTRY_CODE` / `stock_country_code`
- `STOCK_KO_NAME` / `stock_ko_name`
- `EVENT_TYPE` / `event_type`
- `EVENT_TIME` / `event_time`
- `GROUP_ID` / `group_id`
- `GROUP_NAME` / `group_name`
- `GROUP_DESC` / `group_desc`
- `RECO_SIGNAL_CODE` / `reco_signal_code`
- `RECO_SIGNAL_NAME` / `reco_signal_name`
- `RECO_TREND_TEXT` / `reco_trend_text`
- `RECO_REASON_DETAIL` / `reco_reason_detail`
- `RECO_EVENT_SUMMARY` / `reco_event_summary`
- `REGIME_KR_SCORE` / `regime_kr_score`
- `REGIME_US_SCORE` / `regime_us_score`
- `AVG_PRICE` / `avg_price`
- `STATE_CODE` / `state_code`

### B. 의미 중복/근접 용어 (정의 통합 필요)
- 가격 관련
  - `purchase_price`, `avg_purchase_price`, `total_purchase_price`, `price_ref`, `target_price`
- 신호/상태 관련
  - `signal_type`, `signal_grade`, `state_code`, `last_event_type`
- 종목 분류 관련
  - `stock_type`, `stock_type_specs`, `stock_group`, `stock_sector`, `stock_market`
- 레짐/추세 관련
  - `regime_kr_score`, `regime_us_score`, (UI) 시장환경/추세 문구

### C. 즉시 정비 대상(우선순위 높음)
1) **시장환경/추세**
- 표준 키: `regime_kr_score`, `regime_us_score`
- UI 표준 용어: `시장환경(외부)`

2) **추천 신호 세트**
- 표준 키 6종 고정:
  - `reco_score`
  - `reco_signal_code`
  - `reco_signal_name`
  - `reco_trend_text`
  - `reco_reason_detail`
  - `reco_event_summary`

3) **차트/AI 검토 세트**
- 표준 키:
  - `state` (`ENTRY_READY` 등)
  - `suggestedEntryPrice`
  - `initialStopPrice`
  - `takeProfit1Price`
  - `riskR` (또는 `riskRewardRatio` 단일화 필요)

---

## DB 핵심 도메인 테이블 (용어 중심)
- `TB_S_STOCK_ANALYSIS`
- `TB_S_STOCK_30MINANALYSIS`
- `TB_S_SIGNAL_EVENT`
- `TB_S_POSITION`
- `TB_S_POSITION_TXN`
- `TB_S_POSITION_EVENT`
- `TB_S_INTEREST_STOCK`
- `TB_S_INTEREST_GROUP`
- `TB_S_INTEREST_WATCHLIST`
- `TB_S_BATCH_JOB_CTRL`

---

## 표준 적용 방식
1. **읽기 호환 유지**: 구 키/신 키 동시 수용 (파서 레벨)
2. **쓰기 표준화**: 저장/응답은 표준 키만 사용
3. **마이그레이션 로그**: 변경 키 목록을 `PROGRESS_LOG.md`에 누적
4. **WF 연동**: `WF-2-2c-BASE` 및 `31_MARKET_TREND_RULES.md` 기준으로 이벤트 용어 동기화

---

## 화면 표시용 증권 용어 표준 (UI 우선)
> 비즈니스 로직은 유지하고, **화면 노출 용어만 표준화**한다.

| 축약 | 한글 표준 | 영문 표준 |
|---|---|---|
| BUY | 매수 | Buy |
| SELL | 매도 | Sell |
| HOLD | 관망(보합) | Hold / Neutral |
| STRONG_BUY | 강한매수 | Strong Buy |
| WEAK_BUY | 관심매수 | Weak Buy |
| WEAK_SELL | 주의매도 | Weak Sell |
| STRONG_SELL | 강한매도 | Strong Sell |
| ENTRY | 진입 | Entry |
| EXIT | 청산 | Exit |
| ENTRY_READY | 진입준비 | Entry Ready |
| WAIT | 대기 | Wait |
| BREAKOUT | 돌파 | Breakout |
| PULLBACK | 눌림 | Pullback |
| TP1 | 1차목표가(일부매도) | Take Profit 1 |
| SL | 손절가 | Stop Loss |
| RR / R | 손익비 / 리스크단위 | Risk-Reward / R-multiple |
| SIDEWAYS | 횡보 | Sideways |
| UPTREND | 상승추세 | Uptrend |
| DOWNTREND | 하락추세 | Downtrend |
| DEAD_CROSS | 데드크로스 | Dead Cross |
| GOLDEN_CROSS | 골든크로스 | Golden Cross |
| NO_BUY_ZONE | 매수금지구간 | No-Buy Zone |
| REGIME | 시장환경(외부) | Market Regime |

### 사용자 가이드(괄호 용어) 축약 표준
| 이벤트 원문(가이드)  | 축약코드          | 한글 단축 라벨   | 영문 라벨                        | 설명                                          |
| ------------ | ------------- | ---------- | ---------------------------- | ------------------------------------------- |
| 5->20 하락 관망  | `M520_DW_W`   | 5-20 하락관망  | MA5→20 Down Watch            | 5일선이 20일선을 하향 이탈해 단기 약세 전환을 경계하는 구간         |
| 경고 5일선 역N자   | `M5_RN_WARN`  | 5일 역N 경고   | MA5 Reverse-N Warn           | 5일선 반등 후 20일선 부근에서 재하락하는 실패 반등(역N) 패턴       |
| 5-20 완만 관망   | `M520_GW`     | 5-20 완만관망  | MA5-20 Gentle Watch          | 5일선이 20일선 위/근처를 회복했지만 확정 상승 전까지 관망이 필요한 구간  |
| 5-20 완만 상승   | `M520_GU`     | 5-20 완만상승  | MA5-20 Gentle Up             | 완만 관망 이후 다음날 상승 확인으로 단기 추세 회복 신호            |
| 5->20->60 하락 | `M5260_DW`    | 5-20-60 하락 | MA5→20→60 Down               | 5일선이 20일선과 60일선을 연속 하향 이탈한 하락 가속 구간         |
| 20->60 경고    | `M2060_WARN`  | 20-60 경고   | MA20→60 Warn                 | 20일선이 60일선을 하향 이탈해 중기 추세 약화를 경고             |
| 경고 20일선 역N자  | `M20_RN_WARN` | 20일 역N 경고  | MA20 Reverse-N Warn          | 20일선 반등 후 60일선 부근에서 재하락하는 실패 반등(역N) 패턴      |
| 20-60 완만 관망  | `M2060_GW`    | 20-60 완만관망 | MA20-60 Gentle Watch         | 20일선이 60일선 위/근처를 회복했지만 추세 확정 전 관망 구간        |
| 상승추세         | `MA_BULL_M`   | 상승추세       | MA Bull Trend (Monthly Bull) | 5·20·60·120·240 정배열이며 월봉 양봉일 때 중장기 상승추세로 판정 |
| 데드크로스        | `DC_60_240`   | 데드크로스      | Dead Cross 60/240            | 60일선이 240일선을 하향 이탈한 장기 하락 전환 신호             |
| 매수 금지        | `NBZ_240`     | 매수금지       | No-Buy Zone (<MA240)         | 핵심 이동평균선/가격이 240일선 하단에 위치해 신규 매수를 제한하는 구간   |

### UI 표기 규칙
1. 기본은 **한글 표준**으로 표기
2. 필요 시 보조로 영문 병기: `매수(Buy)`
3. 화면에서 `보합`은 단독 사용보다 `관망(보합)` 우선
4. `레짐` 표현은 `시장환경(외부)`로 통일
5. 이벤트성 경고는 접두어 `경고` 유지 (`경고 5일선 역N자`)
6. 가이드 괄호 용어는 축약코드(`M520_DW_W` 등)와 1:1 매핑하여 관리

## 다음 액션 (실행 계획)
- [ ] DD-1: 추천/신호 UI 라벨을 위 표준 용어로 일괄 치환
- [ ] DD-2: `recommendStocks.js` 뱃지/필터 라벨 표준화
- [ ] DD-3: AI 검토 패널 라벨(진입/손절/목표/신호해석) 표준화
- [ ] DD-4: 영문 코드→한글 라벨 매핑 테이블 단일 소스화
- [ ] DD-5: 용어 변경 이력(버전) 관리 규칙 추가

버전: v0.2 (UI 용어 표준 추가)
기준일: 2026-02-12
