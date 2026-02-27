# 1-2 추천종목(Recommend) UI 세부 플로우

## 사용자 플로우(요약)
1) 추천 버튼 클릭 → 모달 오픈
2) 필터 설정(국내/해외, 등급, 코스피/코스닥, 가격범위, limit, 원픽, 현재가)
3) 조회(↻) → 추천 리스트 렌더
4) 레짐 배너 확인(KR/US)
5) 종목 카드 확인
   - 점수/등급
   - 이벤트 요약(reco_event_summary)
   - (옵션) 현재가/등락
6) 선택(라디오) → 적용(선택) / 더블클릭 즉시 적용

## 화면 요소(현재 구현)
- 레짐 배너: KR/US 레짐(상/중/하) + 점수
- 이벤트 요약 박스: reco_event_summary(최근 이벤트 3개 + fallback)

## 연관 API
- /finance/selectRecommendStocks.do

## 연관 로직/DB
- 로직: 2-3 추천 종목 선별
- DB: TB_S_STOCK_ANALYSIS, TB_S_STOCK_30MINANALYSIS, TB_S_SIGNAL_EVENT

## 다음 개선 후보(UX)
- 시장 전환(N/A)에 따라 레짐 강조(국내면 KR 강조)
- 이벤트 요약 문구 표준화(한글 템플릿)
- 이벤트 상세(최근 N개) expand/tooltip
