# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**scheduler** is a stock market monitoring and analysis web application focused on the Korean market via KIS (Korea Investment & Securities) OpenAPI integration. It provides real-time stock tracking, technical charting, watchlist management, and batch data processing.

## Tech Stack

- **Backend**: Java 8, Spring Framework 3.0.7 (+ Spring WebFlux 6.1.5 for reactive streams), MyBatis 3.2.0
- **Database**: Oracle RDBMS (dual datasource: primary + DEV), Apache Commons DBCP
- **Frontend**: JSP/JSTL, jQuery 3.7.1, Bootstrap 5, AdminLTE 3, Highcharts (financial charts)
- **Real-time**: KIS WebSocket API + Java-WebSocket 1.5.6
- **Server**: Apache Tomcat 9.0
- **Build**: Eclipse-based (no Maven/Gradle) — JARs live in `webapp/WEB-INF/lib/`

## Project Mission

이 프로젝트의 핵심 목표는 **주식 차트 분석을 통한 매수/매도 시점 파악 도구**를 만드는 것이다.

### Claude의 역할
- **금융 전문가 + 프로그래머**: 단순 코딩이 아니라, 금융 도메인 지식을 기반으로 기술적 분석(Technical Analysis) 도구를 설계하고 구현한다.
- **어드바이스 제공**: 사용자 요청을 파악한 뒤, 더 나은 분석 방법이나 지표 조합이 있으면 적극 제안한다.
- **차트 시각화 중심**: 분석 결과는 반드시 화면의 차트(Highcharts)에 시각적으로 표현해야 한다.

### 개발 방향
- 기술적 지표(이동평균, MACD, RSI, 볼린저밴드 등)를 활용한 매매 신호 생성
- 차트에 매수/매도 포인트를 시각적으로 마킹
- 분석 도구를 지속적으로 추가/개선하며 정확도를 높여간다
- 새로운 분석 기능 구현 시, 해당 전략의 장단점과 적용 시나리오를 함께 설명한다

## Build & Deploy

- **Compile**: `./build.sh` — JDK 11(javac)로 src/ 컴파일 → `webapp/WEB-INF/classes/` 출력, 리소스 파일 복사
- **Run**: `./run.sh` — Tomcat 9 시작 (기존 프로세스 자동 종료 후 재시작)
- **Deploy**: `webapp/` 디렉토리를 Tomcat Context로 직접 배포 (WAR 패킹 없음)
- **JDK**: Zulu JDK 11 (Apple Silicon, `java.net.http` API 필요)
- 의존성은 `webapp/WEB-INF/lib/`에 커밋된 JAR 파일들

## Architecture

### URL Routing
All requests go through Spring's `DispatcherServlet` mapped to `*.do` URLs. The view resolver maps to `/appone/jsp/*.jsp`.

### Spring Configuration (XML-based, not annotation-driven)
Beans are defined in XML, imported via chain:
```
scheduler-servlet.xml
  ├── mainService.xml      (login, menu, user settings)
  ├── mngtService.xml      (user/menu/code/calendar/profile management)
  └── financeService.xml   (stock, chart, watchlist, batch controllers + DAOs)
```
Located in: `webapp/WEB-INF/resources/`

### Source Packages (`src/com/scheduler/`)

| Package | Purpose |
|---------|---------|
| `login` | Authentication and session management |
| `comm` | Shared controllers (menu, item code, settings), utilities, system init |
| `management` | Admin features: user/menu/code/calendar/profile CRUD |
| `finance` | Stock monitoring, charting, watchlists, batch jobs, market data |
| `kis_api` | KIS OpenAPI wrappers — REST and real-time data classes (40+ response types) |
| `kis_client` | Reusable KIS client library: HTTP/WebSocket clients, rate limiting, auth middleware |
| `util` | Request/response handlers, session validation |

### DAO Pattern
Controllers → Service/DAO interfaces → DaoImpl (MyBatis `SqlSessionTemplate`). MyBatis mapper XMLs live alongside Java source in `*/sql/oracle/` directories.

### MyBatis Configuration
- Config: `webapp/WEB-INF/resources/config/mybatis/oracle/oracle_mybatis-config.xml` (19 mappers registered)
- Context: `oracle_mybatis-context.xml` (datasource + transaction manager)
- DB credentials: `oracle.db.properties`

### Frontend Structure (`webapp/appone/`)

- **JSP pages**: `appone/jsp/{module}/` — login, main, finance, management, calendar, batch
- **Primary finance UI**: `appone/jsp/finance/kis/kisFinance/` — the main dashboard
- **JavaScript modules**: `appone/jsp/finance/kis/kisFinance/js/` — 13 JS files handling charts, watchlists, recommendations, real-time data
- **CSS**: `appone/jsp/finance/kis/kisFinance/css/`
- **Plugins**: `appone/plugins/` — AdminLTE, Bootstrap, jQuery, Highcharts, Select2, etc.

### KIS API Integration
- **Credentials**: `webapp/WEB-INF/resources/kis/kis.properties`
- **REST API**: `kis_api/api/rest/` (quotations, trading)
- **Real-time WebSocket**: `kis_api/api/realtime/` — 40+ handler classes for different market data types (H0STCNT0 = stock tick, H0STNAV0 = NAV, H0STMKO0 = market overview, etc.)
- **Client library**: `kis_client/` — includes rate limiting middleware, auth middleware, connection management

## Key Conventions

- **Language**: Code comments and UI text are in Korean
- **Encoding**: UTF-8 everywhere (`.editorconfig`, servlet filter, view resolver)
- **Controller URLs**: `*.do` pattern
- **SQL**: Oracle-specific SQL in MyBatis XML mappers (prefixed `oracle_`)
- **Dual datasource**: Primary Oracle + DEV Oracle configured in `oracle_mybatis-context.xml`
