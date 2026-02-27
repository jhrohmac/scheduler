# 자동매매 DB 초안(TB_S_* 규칙)

> 목적: 실계좌 연동 자동 주문/체결/포지션을 **감사 가능(Audit)** 하게 운영하기 위한 최소 스키마 초안.
> 실제 DDL 적용은 **페이퍼/소액 단계 검증 후**.

## 1) 주문(Orders)
### TB_S_ORDER
- ORDER_ID (PK)
- ACCOUNT_ID (계좌 식별, 내부키)
- BROKER (예: KIS)
- STOCK_CODE / MARKET (KR/US)
- SIDE (BUY/SELL)
- ORDER_TYPE (MKT/LMT/STOP 등)
- QTY / PRICE
- STATUS (NEW/SENT/ACK/PARTIAL/FILLED/CANCELED/REJECTED/ERROR)
- EXT_ORDER_ID (증권사 주문번호)
- REQUEST_AT / LAST_UPDATE_AT
- SOURCE (AUTO|MANUAL)
- SOURCE_RUN_ID (TB_S_RECO_RUN 연계 옵션)
- SOURCE_PICK_ID (TB_S_RECO_PICK 연계 옵션)
- JSON_PARAMS (주문 파라미터 원문)

## 2) 체결(Fills/Executions)
### TB_S_TRADE
- TRADE_ID (PK)
- ORDER_ID (FK)
- EXT_TRADE_ID (증권사 체결ID)
- FILLED_QTY / FILLED_PRICE
- FEE / TAX (가능하면)
- TRADE_AT

## 3) 포지션(Positions)
### TB_S_POSITION
- POSITION_ID (PK)
- ACCOUNT_ID
- STOCK_CODE / MARKET
- QTY
- AVG_PRICE
- REALIZED_PNL
- UNREALIZED_PNL (계산/캐시)
- STATE (예: RISK_OFF, TREND_FOLLOW 등)
- UPDATE_AT

## 4) 포지션 이벤트(상태 머신 로그)
### TB_S_POSITION_EVENT
- EVENT_ID (PK)
- POSITION_ID
- EVENT_TYPE (ENTER/TP1/STOP_MOVE/EXIT/ALERT 등)
- EVENT_AT
- MESSAGE
- JSON_PARAMS

## 5) 브로커/연동 로그(Audit)
### TB_S_BROKER_API_LOG
- LOG_ID (PK)
- BROKER
- API_NAME
- REQUEST_AT / RESPONSE_AT
- HTTP_STATUS / RESULT_CODE
- REQUEST_JSON / RESPONSE_JSON (민감정보 마스킹 필수)

---

## 확인해야 할 것(체크리스트)
- 주문/체결/포지션의 **정합성**(부분체결/정정/취소 포함)
- 재시도/중복요청 방지 키(ORDER_ID ↔ EXT_ORDER_ID)
- 민감정보(토큰/계좌번호 등) **저장 금지/마스킹**
- ‘긴급정지 스위치’(AUTO OFF) 저장 위치/적용 범위
