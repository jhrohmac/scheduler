# 선택 종목 모니터링/정확도(Probability) 플로우

## 목표
- 추천 리스트에서 사용자가 “선택한 종목”이 실제로 맞았는지(수익/손실) 추적
- 조건별(레짐/신호/이벤트 조합) 성공확률을 누적해서 튜닝 근거로 사용

---

## P1. 선택 이벤트 기록(TB_S_RECO_PICK) ⬜
- 언제/어떤 추천(run_id)에서/몇 위(rank)/어떤 종목(stock_code)을 선택했는지 저장
- 선택 당시 스냅샷도 함께 저장
  - pick_anchor_close(종가)
  - pick_regime_kr/us_score
  - pick_event_summary(텍스트)

## P2. 성과 업데이트(TB_S_RECO_PICK_EVAL) ⬜
- 거래일 기준 5D/20D/60D에 대해
  - Return/MFE/MAE 계산하여 pick_id에 매핑
- 매일 배치로 “평가가 완성된 pick”만 업데이트

## P3. 확률/정확도 집계(VW_S_RECO_PICK_STATS) ⬜
- 성공 기준 예시(초기):
  - WIN = Return_20D > 0
  - 또는 MFE_20D >= +X% (목표 도달)
- 조건별 승률/평균 MFE/평균 MAE/표본수 산출
  - (레짐 상/중/하)×(등급)×(이벤트 조합)

## P4. UI(성적표) ⬜
- 내 선택 종목 리스트
  - 당시 추천정보(점수/등급/이벤트요약/레짐)
  - 현재까지 성과(5/20/60D Return, MFE/MAE)
- 요약 카드
  - 최근 30개 pick 승률
  - 조건별 TOP/BOTTOM
