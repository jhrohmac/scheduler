# CLAUDE.md

## Project
**scheduler** — KIS API 기반 주식 모니터링·분석 웹앱.
목표: **TA 지표를 통한 매수/매도 시점 파악 도구** 구현.

## Claude Role
- 금융 전문가 + 개발자: TA 도구 설계·구현. 더 나은 지표 조합 적극 제안.
- 분석 결과는 가능하면 Highcharts 차트로 시각화 (필수 아님).
- DO NOT commit: `kis.properties`, `oracle.db.properties`

## Tech Stack
- Backend: Java 8, Spring 3.0.7 (+WebFlux 6.1.5), MyBatis 3.2.0
- DB: Oracle (dual: primary + DEV), Apache Commons DBCP
- Frontend: JSP/JSTL, jQuery 3.7.1, Bootstrap 5, AdminLTE 3, Highcharts
- Real-time: KIS WebSocket + Java-WebSocket 1.5.6
- Server: Tomcat 9.0 | Build: Eclipse (JARs in `webapp/WEB-INF/lib/`)

## Build & Run
```bash
./build.sh   # javac → webapp/WEB-INF/classes/
./run.sh     # Tomcat restart
```
App: `http://scheduler.iptime.org:8080/scheduler/`

## Source Packages (`src/com/scheduler/`)
| Package | Purpose |
|---|---|
| `login` | Auth & session |
| `comm` | Shared controllers, utilities, system init |
| `management` | User/menu/code/calendar CRUD |
| `finance` | Stock monitoring, charts, watchlist, batch |
| `stock` | Core: batch, RecSignal, RecPick, Position, MA calc |
| `kis_api` | KIS OpenAPI wrappers (REST + realtime 40+ types) |
| `kis_client` | HTTP/WebSocket client, rate limit, auth middleware |
| `util` | Request/response handlers, session validation |

## Architecture
Request flow: `DispatcherServlet (*.do)` → Controller → DAO → MyBatis → Oracle

Spring XML (`webapp/WEB-INF/resources/`):
```
scheduler-servlet.xml → mainService.xml | mngtService.xml | financeService.xml
```
MyBatis mapper XMLs: `*/sql/oracle/oracle_*.xml`

## KIS API
- Credentials: `webapp/WEB-INF/resources/kis/kis.properties`
- 신규 API 연동 시 `한국투자증권_오픈API_전체_가이드_문서.xlsx`에서 TR_ID·필드 확인.

## TA Signal (매매 신호 구현 필수)
신호마다 **매수 조건 / 매도 조건 / 적합 시장 환경 / 오신호 케이스** 4가지 명시.
차트에 매수·매도 포인트 Highcharts annotation/flag로 마킹.

## Conventions
- Comments & UI: Korean | Encoding: UTF-8 | URLs: `*.do`
- SQL: Oracle, MyBatis XML (`oracle_` prefix)
- **New screen**: JSP / JS / CSS 반드시 별도 파일 분리 (inline `<script>/<style>` 금지)
- Grid: `dataTableGridNew(gridObj, options)` in `common.js`
- Ajax: `ajaxCall()` or `ajaxRequest()`
- Response: `DataTableSettingVo` + `ResponseHandler.sendResponse()`

## Source Protection
1. Read before modify — 변경 범위·영향 파일 먼저 파악·보고
2. 기존 메서드 파라미터·반환타입 변경 금지 (추가만 허용)
3. `common.js`: 신규 함수 추가만 허용, 기존 동작 변경 금지
4. Spring XML: 신규 빈/매퍼 추가만 허용, 기존 빈 변경 금지
5. **사용자 승인 필수**: DB 스키마 변경 / 공통 함수 동작 변경 / 기존 API 시그니처 변경
6. 기능 단위 분리 작업 → 즉시 커밋 (`feat:`/`fix:`/`ops:`/`docs:`)

## Workflow
- `NEXT_ACTION.md` — 현재 할 일 (세션 시작 시 반드시 확인)
- `WORKFLOW.md` — 브랜치 규칙·커밋 컨벤션
