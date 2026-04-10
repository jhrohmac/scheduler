# CLAUDE.md

Guidance for Claude Code in this repository.

## Project Overview

**scheduler** — KIS (Korea Investment & Securities) OpenAPI-based domestic stock monitoring and analysis web app.
Core goal: **TA (Technical Analysis) indicator tool for identifying buy/sell timing**.

## Claude's Role

- **Finance expert + developer**: Design and implement TA tools. Proactively suggest better indicator combinations.
- **Visualization preferred**: Represent analysis results with Highcharts charts where possible (not mandatory).
- **YOU MUST NOT commit**: `kis.properties`, `oracle.db.properties` (API keys and DB secrets)

## Development Direction

### Trading Signal Generation
- Generate TA-based buy/sell signals using MA(5/20/60/120/240), MACD, RSI, Bollinger Bands.
- When implementing a signal, always specify all 4:
  - **Buy condition**: Entry criteria (e.g. MA golden cross + volume surge)
  - **Sell condition**: Exit criteria (e.g. RSI > 70 + MACD dead cross)
  - **Market environment**: Trending / sideways / high-volatility
  - **False signal cases**: When the signal typically fails
- Mark buy/sell points visually on charts (Highcharts annotation/flag).

### Stock Performance Tracking Batch (Core Feature)
- Track watchlist / recommended / held stocks from registration date.
- Daily batch (`RecPickDailyBatch`) records price changes and cumulative returns → `TB_S_RECO_PICK_DAILY`
- Calculate **recommendation success rate**: target price reached, period return stats → `TB_S_RECO_PICK_EVAL`
- Buy/sell timing alerts: link TA signals (`TB_REC_SIGNAL`) with positions (`TB_S_POSITION`)

## Tech Stack

- **Backend**: Java 8, Spring 3.0.7 (+ WebFlux 6.1.5), MyBatis 3.2.0
- **Database**: Oracle RDBMS (dual datasource: primary + DEV), Apache Commons DBCP
- **Frontend**: JSP/JSTL, jQuery 3.7.1, Bootstrap 5, AdminLTE 3, Highcharts
- **Real-time**: KIS WebSocket API + Java-WebSocket 1.5.6
- **Server**: Apache Tomcat 9.0
- **Build**: Eclipse-based (no Maven/Gradle) — JARs in `webapp/WEB-INF/lib/`

## Build & Run

```bash
./build.sh                  # javac compile → webapp/WEB-INF/classes/
./run.sh                    # Restart Tomcat
./start-work.sh             # Show git status + NEXT_ACTION
./end-work.sh "summary"     # Update NEXT_ACTION.md + commit guide
```

- App URL: `http://scheduler.iptime.org:8080/scheduler/`
- Mobile: `/finance/mobile/watchlist.do`

## Source Packages (`src/com/scheduler/`)

| Package | Role |
|---|---|
| `login` | Auth / session |
| `comm` | Common controllers, utils, system init |
| `management` | User / menu / code / calendar management |
| `finance` | Stock monitoring, charts, watchlist, batch controllers & DAO |
| `stock` | Core business logic — batch/, RecSignal/RecPick/Position/MA services |
| `kis_api` | KIS OpenAPI wrapper (REST + realtime, 40+ response types) |
| `kis_client` | HTTP/WebSocket client, rate limiter, auth middleware |
| `util` | Request/response handlers, session validation |

Follow this package structure when adding new features.

## KIS API

- **Auth config**: `webapp/WEB-INF/resources/kis/kis.properties`
- **REST prod**: `https://openapi.koreainvestment.com:9443`
- **REST mock**: `https://openapivts.koreainvestment.com:29443`
- **Access token**: `POST /oauth2/tokenP` (valid 1 day)
- **WebSocket key**: `POST /oauth2/Approval` (no re-issue needed while session active)

> For new API integrations, always check TR_ID · Request/Response fields · domain in `한국투자증권_오픈API_전체_가이드_문서.xlsx`.

### KIS 종목 마스터 파일 다운로드

KIS는 전체 종목 목록을 REST API로 제공하지 않고 **정적 파일 다운로드** 방식으로 제공한다.
샘플 코드 참조: `/Users/jinhyun/projects/open-trading-api-main/stocks_info/`

| 시장 | 다운로드 URL | 파싱 참조 |
|---|---|---|
| KOSPI | `https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip` | `kis_kospi_code_mst.py` |
| KOSDAQ | `https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip` | `kis_kosdaq_code_mst.py` |
| NASDAQ | `https://new.real.download.dws.co.kr/common/master/nasmst.cod.zip` | `overseas_stock_code.py` |
| NYSE | `https://new.real.download.dws.co.kr/common/master/nysmst.cod.zip` | `overseas_stock_code.py` |

**파일 포맷 요약:**
- KOSPI `.mst`: cp949, 고정폭(Part1: 단축코드9+표준코드12+한글명, Part2: 228바이트 65필드)
  - 주요필드: `거래정지`(35번), `관리종목`(37번), `상장일자`(50번), `KOSPI200섹터업종`(9번)
- KOSDAQ `.mst`: cp949, 고정폭(Part2: 222바이트 60필드)
  - 주요필드: `거래정지여부`(30번), `관리종목여부`(32번), `상장일자`(45번), `KOSDAQ150지수여부`(26번, Y/N)
- 해외 `.cod`: cp949, **탭구분(TSV)**, 24컬럼
  - 주요필드: `Symbol`(5번), `Security type`(9번, 2=주식), `Korea name`(7번), `English name`(8번)
- 인증 불필요 — 공개 URL, 매일 갱신됨

## DB Key Tables

`TB_STK_MASTER`(stock master) · `TB_STK_DLY_PRICE`(daily) · `TB_STK_MON_PRICE`(monthly)
`TB_REC_SIGNAL`(buy signal) · `TB_S_RECO_PICK`/`_DAILY`/`_EVAL`(recommendations & performance)
`TB_S_POSITION`(position) · `TB_S_SIGNAL_EVENT`(signal events)
`TB_BATCH_EXEC_LOG`/`_ITEM_LOG`(batch logs) · `TB_TRADE_CALENDAR`(trading calendar)

DDL: `db/DDL_TB_*.sql`, `db/DDL_VW_*.sql`

## Architecture

**Request flow**: `DispatcherServlet (*.do)` → Controller → Service/DAO → MyBatis → Oracle

**Spring XML beans** (`webapp/WEB-INF/resources/`): `scheduler-servlet.xml` imports `mainService.xml`, `mngtService.xml`, `financeService.xml`.

**MyBatis**: mapper XMLs at `*/sql/oracle/oracle_*.xml` — 19 mappers registered in `oracle_mybatis-config.xml`.

## Frontend (`webapp/appone/`)

- **Main dashboard**: `jsp/finance/kis/kisFinance/`
- **JS modules**: `webapp/appone/js/` (16 modules — chartScript, maScript, kisFinancePage, recSignalPanel, etc.)
- **Common JS**: `appone/plugins/system/js/common.js`
- **Common includes**: `system/css/cssLink.jsp`, `system/js/jsLink.jsp`

### File Separation Rule

New screens must separate JSP / JS / CSS into distinct files:

```
jsp/{module}/feature.jsp      ← HTML structure only
jsp/{module}/js/feature.js    ← Scripts only
jsp/{module}/css/feature.css  ← Styles only
```

No inline `<script>` or `<style>` blocks inside JSP files.

## Project Conventions

**Grid**: Use `dataTableGridNew(gridObj, options)` from `common.js`. Set `grid_id`, `url`, `param`, `columns`, `columnDefs` on `gridObj`.

**Backend Response**: Set `DataTableSettingVo` fields (`data`, `draw`, `start_no`, `page_length`, `recordsFiltered`, `recordsTotal`), then return via `ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, msg, resultVo)`. Use `ERROR_CODE` in catch.

**Ajax**: Simple calls use `ajaxCall(url, "json", param, callback)`. Option-controlled calls use `ajaxRequest({ url, data, onSuccess, onError })`.

## Source Protection Rules

### Before Modifying Any File
- Always identify **change scope and affected files** before editing.
- Explicitly notify if an existing screen will be impacted.

### Common File Rules
- `common.js`: add new functions / fix bugs only — never change existing function signatures
- `cssLink.jsp` / `jsLink.jsp`: add libraries only — never remove or reorder existing entries
- Spring XML beans: add new beans/mappers only — never change existing bean id/class/properties

### Controller · DAO · SQL Mapper Rules
- Fixing bugs, improving performance, or refactoring logic in existing methods is allowed.
- **Never change** an existing method's parameter or return type — it breaks callers.
- Need different behavior? **Add a new method**, keep the original.

### Commit Rules
- Work in single-feature units — don't mix multiple features in one commit.
- Check `git status` before starting; commit immediately after finishing (`feat:` / `fix:` / `ops:`).
- **YOU MUST get user approval before**: DB schema changes (ALTER TABLE), common function behavior changes, existing API signature changes.

## Key Conventions

- Code comments and UI text: **Korean**
- Encoding: UTF-8
- Controller URL pattern: `*.do`
- SQL: Oracle-specific, MyBatis XML (`oracle_` prefix)
- Dual datasource: primary + DEV (`oracle_mybatis-context.xml`)

## Workflow Docs

- `NEXT_ACTION.md` — current / next tasks (always check at session start)
- `WORKFLOW.md` — branch rules (`feat/`, `fix/`, `ops/`, `docs/`), commit conventions
- `PROGRESS_LOG.md` — completion history
- `PROJECT_PLAN.md` — full roadmap
