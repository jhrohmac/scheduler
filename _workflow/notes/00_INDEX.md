# Scheduler Workflow Index

## 목적
- 추천 정확도 개선(백테스트/튜닝) 및 MFE/MAE 기반 검증 루프를 **워크플로우로 시각화**해서 운영.

## 바로가기
- 캔버스: `../canvas/MASTER_WORKFLOW.canvas`
- 보조 설명 노트: `MASTER_WORKFLOW_NOTE.md`
- 프로젝트 구성도: `01_PROJECT_STRUCTURE_TREE.md`
- 진행 로그(코드 저장소): `../PROGRESS_LOG.md`
- 프로젝트 플랜(코드 저장소): `../PROJECT_PLAN.md`
- 마켓 추세 기준 문서(사용자 정의): `31_MARKET_TREND_RULES.md`
- 웹소켓 재연결 방어: `32_WEBSOCKET_RECONNECT_HARDENING.md`
- RecSignal 공용 배치관리 설계: `36_REC_SIGNAL_BATCH_ADMIN_DESIGN_2026-03-12.md`
- 추천 종목 관리/추적 확장 설계: `52_RECOMMENDATION_TRACKING_DESIGN_2026-03-20.md`
- 추천 종목 관리/추적 구현 명세: `53_RECOMMENDATION_TRACKING_IMPLEMENTATION_SPEC_2026-03-20.md`
- DD 데이터사전(용어 표준): `DD_DATA_DICTIONARY.md`
- 동기화 규칙: `90_DEV_SYNC_RULE.md`
- 싱크 매트릭스: `92_SOURCE_SYNC_MATRIX.md`

## 상태 표기 규칙
- ✅ 완료
- 🟡 진행중/부분완료
- ⬜ 미착수
- ⚠️ 리스크/운영주의

## 현재 우선순위
1. W1 추천 스냅샷 저장(Reco Snapshot) ⬜
2. W2 거래일 캘린더 확정/생성 ⬜
3. W3 Return + MFE/MAE 계산 및 DB 저장 ⬜
4. W4 분해 분석(레짐/신호/이벤트 조합) ⬜
5. W5 튜닝 실험 프레임(파라미터/실험ID/성과 비교) ⬜
