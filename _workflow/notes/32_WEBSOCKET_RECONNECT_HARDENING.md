# 2-11 웹소켓 재연결 방어 / KIS 차단 방지

## 목적
- KIS 공지의 `무한 연결 접속`, `구독 후 수신 검증 없는 반복 등록/해제` 리스크를 현재 `scheduler` 프로젝트 소스 기준으로 차단한다.
- 브라우저 WebSocket, 서버 Endpoint, 내부 Hub의 동작을 분리해서 보고, 어디서 무한 reconnect 루프가 생길 수 있는지 명확히 추적한다.
- 수정 후에는 `화면 -> 브라우저 WS -> 서버 Endpoint -> Hub -> KIS` 흐름이 step by step으로 검증 가능해야 한다.

## 현재 진단 요약

### 현재 실제 로드 경로
- KIS 메인 화면에서 실제 로드되는 파일은 `realtimeWatchlist.js`
- `watchlistRealtime.js`, `marketSummaryRealtime.js` 는 현재 `kisFinance.jsp` 기준 활성 진입점이 아님
- 따라서 수정 1순위는 `webapp/appone/jsp/finance/kis/kisFinance/js/realtimeWatchlist.js`

### 현재 리스크
- `onclose -> setTimeout -> reconnect` 고정 재시도
- 종료 코드 구분 없음
- 지수 백오프 없음
- 지터 없음
- 최초 `onopen` 만으로 연결 성공 판단
- 실제 데이터 수신(`lastMessageAt`) 확인 없음
- 같은 종목 집합이어도 DOM 순서가 달라지면 다른 key로 판단
- 서버 `onError` 에서 cleanup 보장 약함
- `invalid codes` 에 대해 Endpoint 레벨 명확한 종료 정책 없음

## 실제 수정안

## A. 클라이언트 수정안 (`realtimeWatchlist.js`)

### A-1. 공통 재연결 정책 함수 추가
- 목적: `WatchlistRealtime`, `ChartPriceRealtime`, `MarketSummaryRealtime` 가 제각각 재연결하지 않도록 공통 정책 적용
- 추가 상태값
  - `reconnectAttempts`
  - `reconnectTimer`
  - `lastOpenAt`
  - `lastMessageAt`
  - `manualClose`
  - `currentConnectSeq`
- 정책
  - 정상 종료(`1000`) 또는 정책상 재연결 금지 코드면 재연결 중지
  - 비정상 종료만 재연결
  - 재연결 지연 = `min(base * 2^attempt, maxDelay)` + `jitter`
  - 예시
    - base: `1000ms`
    - maxDelay: `30000ms`
    - jitter: `0~700ms`

### A-2. `WatchlistRealtime.connectFromDom()` 수정
- 현재 문제
  - DOM 순서 기반 `tokens.join(",")` 로 key 생성
  - 순서만 바뀌어도 기존 소켓 close 후 재연결
- 수정
  - `tokens = Array.from(new Set(tokens)).sort()`
  - 정렬된 key 기준으로만 reconnect 판단
  - `OPEN/CONNECTING` 상태이고 동일 key 이면 재연결 금지
  - `manualClose` 와 `ws 동일성 체크` 모두 적용

### A-3. `WatchlistRealtime` 수신 검증 추가
- `onopen` 은 “TCP/WebSocket 연결 성공”으로만 본다
- 실제 성공 판정은 첫 `WL` 메시지 수신 시점으로 본다
- 추가 규칙
  - `onmessage` 에서 `lastMessageAt = Date.now()` 저장
  - 일정 시간 내 첫 메시지 미수신 시 강제 close 대신 `재시도 가능 상태`로만 전환
  - 짧은 시간 안에 `open -> close -> open -> close` 반복되면 reconnect attempt 증가
  - 최근 `N`초 내 실수신이 없으면 재구독 횟수 제한

### A-4. `ChartPriceRealtime` 동일 정책 적용
- 현재도 단일 토큰에 대해 재연결 반복 가능
- 수정 포인트
  - `manualClose`
  - `ws 동일성 체크`
  - 종료 코드 기반 재연결
  - 지수 백오프 + 지터
  - `lastMessageAt` 기록

### A-5. `MarketSummaryRealtime` 동일 정책 적용
- 현재는 `onclose -> 2초 후 무조건 재연결`
- 수정 포인트
  - `manualClose` 추가
  - `ws 동일성 체크` 추가
  - 종료 코드 기반 재연결
  - 지수 백오프 + 지터
  - 첫 `SUMMARY` 수신 전까지는 성공으로 보지 않음

### A-6. 브라우저 상태 기반 차단
- 선택 적용 권장
- 페이지가 hidden 상태면 재연결 시도 지연
- `beforeunload/pagehide` 에서는 무조건 `manualClose=true`
- 모바일 백그라운드 전환 시 소켓 흔들림 방지

## B. 서버 수정안

## B-1. `WatchlistRealtimeEndpoint` 보완
- 대상: `src/com/scheduler/finance/websocket/WatchlistRealtimeEndpoint.java`
- 수정 포인트
  - `codes` 파싱 후 토큰 검증 결과를 명확히 분리
  - 전부 invalid:
    - `ERR` 전송
    - `session.close()` 수행 검토
  - 일부 invalid:
    - valid token만 유지
    - 필요 시 warning payload 추가
  - `@OnError` 에서 `quoteHub.unsubscribe(session)` 호출
  - `@OnError` 후 세션 정리 로그 남김

### invalid 판단 기준
- 빈 코드
- `country|market|code` 분해 후 code 없음
- 구분자만 있고 실코드 없음
- 허용되지 않은 market/country 조합

## B-2. `MarketSummaryRealtimeEndpoint` 보완
- 대상: `src/com/scheduler/finance/websocket/MarketSummaryRealtimeEndpoint.java`
- 수정 포인트
  - `@OnError` 에서 `cancel(session)` 수행
  - `@OnClose` / `@OnError` 중복 호출에도 안전하게 idempotent 하게 유지
  - 클라이언트 초기화 실패를 계속 push 하지 않도록 현재 1회 제한 유지

## B-3. `WatchlistQuoteHub` 보완
- 대상: `src/com/scheduler/finance/websocket/WatchlistQuoteHub.java`
- 현재 강점
  - closed session cleanup 있음
  - 중복 no-change push 억제 있음
  - timeout 예외 방어 있음
- 추가 보완
  - invalid token 샘플 로그 강화
  - subscribe 결과를 Endpoint 에 반환 가능하게 확장 검토
  - `all invalid` 인 경우 Endpoint 가 즉시 종료할 수 있도록 검증 결과 노출 검토

## Step By Step 패치 순서

1. `realtimeWatchlist.js` 에 공통 reconnect helper 추가
   - 종료 코드 판단
   - 지수 백오프
   - 지터
   - `manualClose`
   - `ws 동일성 체크`

2. `WatchlistRealtime` 적용
   - 토큰 정렬
   - stable key 생성
   - `lastMessageAt` 도입
   - 첫 수신 전 reconnect 제한

3. `ChartPriceRealtime` 적용
   - 단일 토큰 재연결도 같은 정책으로 통일

4. `MarketSummaryRealtime` 적용
   - 무조건 2초 재연결 제거
   - 동일 정책 적용

5. `WatchlistRealtimeEndpoint` 보완
   - invalid token 정책
   - `ERR + close` 처리
   - `@OnError` cleanup

6. `MarketSummaryRealtimeEndpoint` 보완
   - `@OnError` cleanup

7. 로그 검증 포인트 추가
   - `open`
   - `first_message`
   - `close(code, reason)`
   - `reconnect_scheduled(delay, attempt)`
   - `manual_close`
   - `invalid_codes`

## 검증 체크리스트

### 브라우저
- 같은 관심종목 목록을 다시 렌더해도 재연결되지 않는지
- 탭 전환/화면 숨김에서 reconnect 폭주가 없는지
- 네트워크 끊김 후 reconnect 간격이 증가하는지
- 첫 메시지 수신 전에는 성공 상태로 오판하지 않는지

### 서버
- invalid codes 요청 시 `ERR` 후 종료되는지
- `@OnError` 이후 세션 잔존 구독이 남지 않는지
- close/error 로그가 세션 단위로 추적 가능한지

### 운영
- KIS upstream 장애 시 1초 고정 재시도 대신 완만히 증가하는지
- 동일 사용자가 짧은 시간에 반복 구독/종료하지 않는지

## 워크플로우 반영 메모
- 현재 이 이슈는 `2-9 실시간 신호 스트림(WebSocket)` 하위 운영 안정화 작업으로 본다.
- 캔버스에는 별도 기능 노드보다 `운영주의` 또는 `실시간 연결 안정화` 체크포인트로 관리하는 편이 적절하다.
- 상태는 아직 코드 미반영이므로 `⬜ 미착수` 기준.

## 노션 반영 권장 형식
- 설계 결정
  - 제목: `KIS 웹소켓 재연결 방어 정책`
  - 핵심 결정: 종료 코드 기반 재연결, 지수 백오프, 수신 확인 전 reconnect 제한
- 구현 작업
  - 제목: `realtimeWatchlist.js 재연결 방어 적용`
  - 범위: `WatchlistRealtime`, `ChartPriceRealtime`, `MarketSummaryRealtime`, Endpoint 2종
- 이슈 및 메모
  - 제목: `KIS 무한 연결/종료 반복 차단 대응`
  - 근거: KIS 공지 + 현재 코드 분석 결과
