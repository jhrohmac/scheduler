# 현재 진행 상태 (모바일 요약)

## 완료(핵심)
- 추천 생성 파이프라인(Batch→DB→추천)
- 추천 사유(reco_event_summary) 표시
- 시장 레짐(KR/US) 점수 반영 + 배너 표시
- 추천 UI 선택/적용 흐름
- **WF-2-4 보유종목 관리 백엔드 완료** (2026-02-13)
  - VO 3개, Dao/Impl, Controller 7개 API, MyBatis 매퍼, Spring Bean 등록
  - API: list/add/delete/averageDown/txnList/eventList
  - DB: TB_S_POSITION, TB_S_POSITION_TXN, TB_S_POSITION_EVENT + 시퀀스

## 진행중
- WF-1-3 보유종목 UI 액션 연결 (백엔드 완료, 프론트 미착수)
- 관심종목 상태 배지/상세 이동 UX 보강
- 보유종목 상태머신 확장 설계
- 차트 표시 항목 확장(이벤트 마커 등)
- WF-2-2c-BASE 지수 전용 베이스 모듈 UI 연동 (모듈 완료, 추천/차트 연동 필요)
- RecSignal 공용 Batch Admin 분리 구현 반영 (DDL/API/JSP CRUD/실행/정지, 스케줄 엔진 1차)

## 미착수(정확도 개선 핵심)
- W1 추천 스냅샷 저장
- W2 거래일 캘린더
- W3 Return/MFE/MAE 계산
- W4 분해 분석
- W5 튜닝 루프
- 선택 종목(내 픽) 승률/정확도 집계

## 최종 단계
- 실계좌 연동 자동매매
  - 페이퍼 → 소액 → 확장
  - 안전장치/감사로그 필수
