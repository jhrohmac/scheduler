# Workflow ↔ Source Sync Matrix

## 규칙
- 워크플로우 노드 ID를 소스 주석/로그 키로 같이 사용
- 예: `WF-2-2`(차트 분석 모듈), `WF-2-4`(보유종목 관리)

## 1차 매핑 + 싱크 상태
- WF-2-1 배치/분석 저장 → `BatchJobCtrlDaoImpl` ✅ (WF 주석 반영)
- WF-2-2 차트 분석 모듈 → `PositionRuleEngine`, `BullishMomentumDetector`, `StockAnalysisUtil` ✅
  - DAY/30MIN CrossDown(5/20 vs 60) 이벤트 로직 반영 ✅
- WF-2-2c-BASE 지수 전용 베이스 모듈 → `IndexTrendRuleModule`(신규), `31_MARKET_TREND_RULES.md` 기준 연동 🟡
  - 사용자 정의 이벤트명/판정식을 단일 기준 문서로 관리
  - 소스 반영 대상: 지수 레짐 산출부 + 차트/AI 검토 문구 매핑
- WF-2-3 추천 선별 → `oracle_StockAnalysis.xml#selectRecommendStocks`, `StockRecommendVo` ✅
- WF-1-2 추천 UI → `recommendStocks.js`, `kisFinance.css`, `kisFinance.jsp` ✅
- WF-1-4 차트 오버레이 UI → `kisFinancePage.js` ✅ (WF 주석 반영)
- WF-1-5 시장정보/시장이슈 UI → `kisFinance.jsp`, `kisFinancePage.js`, `kisFinance.css`, `MarketSummaryController#selectMarketIssues` ✅
  - 파일기반 SoT: `data/market-issues.json`
  - OpenClaw cron(평일 07:30)로 일일 브리핑 생성/갱신
- WF-2-9 WS 실시간 신호 → 설계/워크플로우 반영 ✅, 소스 확장 ⬜
- WF-2-4 보유종목 관리(ADD/DELETE/AVERAGE_DOWN) → 설계 반영 ✅, 소스 완료 ✅
  - `PositionVo/TxnVo/EventVo` + `PositionDao/DaoImpl` + `PositionController` 생성
  - `oracle_Position.xml` 매퍼 + Spring Bean 등록 완료
  - API: `/position/list.do`, `/position/add.do`, `/position/delete.do`, `/position/averageDown.do`, `/position/txnList.do`, `/position/eventList.do`

## 정리 완료 항목(삭제)
- 캔버스 중복 파일 삭제 완료
- 일회성/보조 DB tools 삭제 완료(승인 반영)

## 다음 구현 순서(합의)
1) WF-2-4 보유종목 로직(ADD/DELETE/AVERAGE_DOWN) ✅
2) WF-3-8 보유종목 DB 테이블/매퍼 ✅(테이블/매핑/검증쿼리)
3) WF-1-3 보유 UI 액션 연결 ✅

## 후속 고도화(튜닝)
- 상태머신 임계값(-3%, +1%) 데이터 기반 튜닝
- 이벤트 타입별 성능 분석 쿼리 운영 (`db/analyze_position_event_thresholds.sql`)
- 데모 샘플 적재/정리 스크립트 추가
  - 적재: `db/seed_position_events_demo.sql`
  - 정리: `db/cleanup_position_events_demo.sql`
- 보유 UI에서 최근 포지션 이벤트 힌트 조회 연동 (`selectPositionEventList.do`)
