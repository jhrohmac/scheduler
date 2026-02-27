# Backtest Workflow (Trading-day 기준 / 종가 anchor)

## 확정 의사결정
- 평가 기간: 20D 메인 + 5D/60D 보조
- 기준(anchor): 추천일 종가(daily_close)
- 기준일 계산: 거래일 기준(영업일 N개)
- 결과 확인: DB 저장 + 화면 조회(A)

---

## W1. 추천 스냅샷 저장 (Reco Snapshot) ⬜
### 목표
- `selectRecommendStocks` 결과(TopN)를 run_id/run_at과 함께 저장해서 **재현 가능**하게 만들기.

### 산출물(예)
- RUN 테이블(헤더)
- RUN_ITEM 테이블(종목 리스트)

### 검증
- 특정 날짜/시간의 TopN이 DB에서 동일하게 조회된다.

---

## W2. 거래일 캘린더 (Trading Calendar) ⬜
### 목표
- T0(추천일) 기준으로 T+5/T+20/T+60 거래일을 계산할 수 있어야 함.

### 검증
- 휴장/주말 포함해도 정확히 거래일로 이동한다.

---

## W3. 성과 계산 (Return + MFE/MAE) ⬜
### 공식
- Return_N = close_N / anchor_close - 1
- MFE_N = max_high_N / anchor_close - 1
- MAE_N = min_low_N / anchor_close - 1

### 검증
- 레짐 하(비우호) 구간에서 평균 MAE 상승 패턴이 관측된다.

---

## W4. 분해 분석(Attribution) ⬜
- 레짐(KR/US) 상/중/하
- 등급(STRONG_BUY/BUY/WEAK_BUY...)
- 이벤트 조합(예: 일봉 MACD BUY & 30분 MACD BUY)

---

## W5. 튜닝 실험 루프 ⬜
- 파라미터화(가중치/임계값)
- 실험ID 저장
- 동일 검증기간 성과 비교
