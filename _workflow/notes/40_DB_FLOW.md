# 3. DB 단위 플로우

## 3-1 DB 테이블 리스트(핵심)
- TB_S_BATCH_JOB_CTRL : 배치 제어/락/완료
- TB_S_INTEREST_STOCK : 관심종목(레짐 자산 포함)
- TB_S_STOCK_ANALYSIS : 일봉 분석 스냅샷
- TB_S_STOCK_30MINANALYSIS : 30분 분석 스냅샷
- TB_S_SIGNAL_EVENT : 신호 이벤트 이력(설명가능/백테스트/튜닝)
- (예정) TB_S_RECO_* : 추천/평가/픽(백테스트/튜닝)

## 3-2 로직에 필요한 테이블(기능별)
- 추천: TB_S_STOCK_ANALYSIS + TB_S_STOCK_30MINANALYSIS + TB_S_SIGNAL_EVENT (+ 컨센서스 테이블)
- 레짐: TB_S_STOCK_ANALYSIS(지수 자산)
- 차트: 분석 테이블 + (향후) 이벤트 테이블(마커)

## 3-3 기존 테이블 컬럼 추가/커스텀(원칙)
- 운영 리스크 최소화를 위해: 먼저 MERGE/로직으로 대응 → 안정화 후 제약/인덱스 강화
- 시크릿 파일(kis.properties)은 절대 커밋 금지

## (추가) 3-4 백테스트용 신규 테이블(예정) — 명명 규칙: TB_S_*
- TB_S_RECO_RUN (헤더)
- TB_S_RECO_RUN_ITEM (종목)
- TB_S_RECO_EVAL (성과: Return/MFE/MAE)
- TB_S_TRADING_CALENDAR (거래일)

## (추가) 3-5 사용자 선택(픽) 추적 테이블(예정) — 명명 규칙: TB_S_*
- TB_S_RECO_PICK (사용자 선택 기록)
  - pick_id, user_id(옵션), run_id, stock_code, rank, pick_at
  - pick_anchor_close(선택 당시 기준가), pick_reason_snapshot(당시 이벤트요약 등)
- TB_S_RECO_PICK_EVAL (선택 종목 성과)
  - pick_id, eval_5d/20d/60d(Return/MFE/MAE), updated_at
- (집계) VW_S_RECO_PICK_STATS (권장: VIEW)
  - 조건별 성공확률(승률), 평균 MFE/MAE, 표본 수

## (추가) 3-8 보유종목/물타기 관리(예정)
- TB_S_POSITION
  - 보유 수량/평단/상태
- TB_S_POSITION_TXN
  - ADD/DELETE/AVERAGE_DOWN 트랜잭션 이력
- TB_S_POSITION_EVENT
  - 상태전이/리스크 이벤트 로그

## (추가) 3-6 AI 예측 결과/로그(예정)
- TB_S_AI_PREDICT_REQ
  - req_id, stock_code, horizon(5/20/60), risk_mode, req_at, req_user
- TB_S_AI_PREDICT_RESULT
  - req_id, prob_up, prob_neutral, prob_down, confidence, summary
  - top_reasons, top_risks, as_of_time
- TB_S_AI_PREDICT_FEATURE_SNAP (옵션)
  - req_id 기준 입력 피처 스냅샷(재현성)

## (추가) 3-7 실시간 신호/차트 이벤트(예정)
- TB_S_RT_SIGNAL_EVENT
  - stock_code, signal_type(BUY/SELL), signal_at, price_ref, source_ws, confidence
  - event_seq, event_time(최신성 보장용)
- TB_S_WS_STREAM_LOG (옵션)
  - ws 수신 품질/지연/재연결 로그(운영 모니터링)
  - lag_ms, reconnect_count, stale_rate
