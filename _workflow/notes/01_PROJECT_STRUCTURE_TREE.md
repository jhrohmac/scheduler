# Scheduler 프로젝트 구성도

## 개요

현재 `scheduler` 프로젝트는 전통적인 Java 웹 애플리케이션 구조 위에
금융 분석, 추천, 포지션 관리, KIS 연동, 워크플로우 문서를 함께 올려 둔 형태다.

큰 맥락에서 보면 아래 6개 축으로 보면 이해가 쉽다.

1. 웹 진입점과 화면
2. 공통 프레임/공통 기능
3. 금융 도메인 로직
4. 외부 API 연동
5. DB/운영 스크립트
6. 워크플로우/문서화

## 트리형 구성도

```text
scheduler/
├── webapp/
│   ├── index.html
│   ├── META-INF/
│   ├── WEB-INF/
│   │   ├── web.xml
│   │   ├── classes/
│   │   ├── lib/
│   │   └── resources/
│   └── appone/
│       ├── jsp/
│       └── plugins/
│
├── finance/
│   ├── controller/        # 금융/차트/추천/포지션 웹 진입점
│   ├── dao/              # 금융 도메인 DAO 인터페이스
│   ├── dao/impl/         # DAO 구현체
│   ├── sql/
│   │   └── oracle/       # Oracle SQL/MyBatis 매퍼
│   ├── vo/               # 분석/추천/포지션 관련 VO
│   ├── module/           # 차트 분석, 시그널, 룰 엔진
│   ├── websocket/        # 실시간 시세/이벤트 WebSocket
│   └── kis/              # KIS 연동용 금융 설정/헬퍼
│
├── comm/
│   ├── controller/       # 공통 화면/공통 설정 진입점
│   ├── dao/
│   ├── dao/impl/
│   ├── sql/
│   │   └── oracle/
│   ├── vo/
│   ├── util/             # 날짜/문자열/다운로드/세션 등 공통 유틸
│   ├── system/           # 전역 설정, 세션, 필터
│   └── exp/              # 공통 예외
│
├── management/
│   ├── controller/       # 사용자/메뉴/코드/캘린더 관리
│   ├── dao/
│   ├── dao/impl/
│   ├── sql/
│   │   └── oracle/
│   └── vo/
│
├── login/
│   ├── controller/       # 로그인 진입점
│   ├── dao/
│   ├── dao/impl/
│   └── service/
│
├── kis_api/
│   └── api/
│       ├── rest/         # KIS REST API 스펙/모델
│       └── realtime/     # KIS 실시간 API 스펙/모델
│
├── kis_client/
│   ├── api/              # API 추상화
│   ├── client/           # HTTP/Socket 클라이언트
│   ├── config/           # 인증/설정/키 선택 전략
│   ├── context/          # 요청 컨텍스트
│   ├── middleware/       # 인증/레이트리밋/페이지 처리
│   ├── util/
│   ├── exception/
│   └── test/             # KIS 클라이언트 테스트/샘플
│
├── db/
│   ├── DDL_*.sql         # 핵심 테이블 DDL
│   ├── verify_*.sql      # 검증 SQL
│   ├── analyze_*.sql     # 분석/튜닝 SQL
│   ├── seed_*.sql        # 데모/샘플 적재 SQL
│   ├── cleanup_*.sql     # 샘플 정리 SQL
│   ├── *.sh              # DDL 적용/업서트/프로브 스크립트
│   └── tools/            # DB 실행용 Java 툴
│
├── data/
│   └── market-issues.json # 시장 이슈/브리핑 데이터
│
├── src/
│   └── com/scheduler/    # 별도 소스 루트(현재는 비중이 낮아 보임)
│
├── util/
│   └── handler/          # 요청/응답/세션 핸들러
│
├── build/
│   └── classes/          # 빌드 산출물
│
├── _workflow/
│   ├── canvas/           # Obsidian 캔버스
│   ├── notes/            # 설계/흐름/동기화 문서
│   ├── 00_START_HERE.md
│   ├── 03_STATUS.md
│   └── README.md
│
├── PROJECT.md
├── PROJECT_PLAN.md
├── PROGRESS_LOG.md
├── WORKFLOW.md
├── build.sh
├── run.sh
└── start-work.sh / end-work.sh
```

## 큰 맥락 해석

### 1. 웹/화면 계층

- `webapp/`
- `finance/controller/`
- `management/controller/`
- `login/controller/`

사용자 요청이 들어오는 시작점이다.
JSP와 컨트롤러 기준으로 화면 흐름을 추적할 때 먼저 본다.

### 2. 공통 기반 계층

- `comm/`
- `util/`

세션, 공통 DAO, 공통 SQL, 공통 유틸, 전역 설정이 모여 있다.
여기서 숨은 의존성이 생기기 쉽다.

### 3. 핵심 금융 도메인 계층

- `finance/module/`
- `finance/vo/`
- `finance/dao/`
- `finance/sql/oracle/`

추천, 분석, 차트, 포지션, 시그널 핵심 로직은 대부분 여기 있다.
프로젝트의 핵심 가치가 모여 있는 중심 축이다.

### 4. 외부 연동 계층

- `kis_api/`
- `kis_client/`
- `finance/websocket/`

한국투자 API와 실시간 시세/이벤트 처리 영역이다.
실시간, 인증, 레이트리밋, 요청 포맷 이슈는 주로 이 축에서 발생한다.

### 5. 데이터/운영 계층

- `db/`
- `data/`

DDL, 검증 SQL, 운영 스크립트, 샘플 데이터가 모여 있다.
DB 변경이나 운영성 이슈는 이 축에서 먼저 정리한다.

### 6. 프로젝트 관리 계층

- `_workflow/`
- 루트의 `PROJECT*.md`, `PROGRESS_LOG.md`, `WORKFLOW.md`

실제 구현과 별도로 설계/상태/흐름을 관리하는 영역이다.
지금은 Obsidian과 Notion을 같이 운영하는 기준점이다.

## 이 프로젝트를 볼 때 추천 시작 순서

1. `_workflow/canvas/MASTER_WORKFLOW.canvas`
2. `_workflow/03_STATUS.md`
3. `finance/controller/`
4. `finance/module/`
5. `finance/sql/oracle/`
6. `db/`

## 현재 기준 해석 포인트

- `finance/` 가 메인 도메인
- `comm/`, `management/`, `login/` 은 공통/관리 기능
- `kis_api/`, `kis_client/` 는 외부 증권 API 연동 계층
- `_workflow/` 는 프로젝트 운영 문서 계층
- `build/`, 여러 `.class` 파일은 이미 빌드된 결과물이 함께 존재하는 구조

즉, 큰 맥락으로 보면 이 프로젝트는
`Java 웹앱 + 금융 분석/추천 엔진 + KIS 연동 + DB 운영 스크립트 + Obsidian/Notion 워크플로우`
로 이해하면 된다.
