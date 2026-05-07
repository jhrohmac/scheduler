# 동적 추천 종목 선별 화면 — 프로그램 설계서

> **문서 버전**: v1.0
> **작성일**: 2026-05-07
> **대상 화면**: `/stock/recPickDynamic/listView.do` (가칭)
> **관련 기존 화면**: `/stock/recSignal/listView.do` (배치 기반 정적 추천)

---

## 1. 설계 목표

### 1.1 배경

현재 `RecSignalService`의 추천 로직은 **2개 조건 AND** (정배열 + 당월 상승) 로 고정되어 있다.
TREND_STRENGTH, AVG_TRD_VAL_20 등 계산만 되고 사용되지 않는 지표가 있고, 사용자가 조건을 변경할 수 없다.

### 1.2 핵심 요구사항

| # | 요구사항 | 설계 키워드 |
|---|---------|-------------|
| 1 | 지표의 핵심 설정값을 사용자가 화면에서 변경 | **지표별 파라미터 패널** |
| 2 | 선택한 지표 조합으로 종목 리스트 동적 생성 | **AJAX 필터 + 서버 동적 평가** |
| 3 | 신규 지표를 모듈처럼 독립 개발하여 추가 | **Plugin Pattern (Strategy + Registry)** |
| 4 | 추천 종목을 관심종목(픽)에 추가 | **기존 `/stock/recPick/saveToWatchlist.do` 재활용** |
| 5 | 종목 선택 시 슬라이드 패널로 상세 표시 | **Off-canvas slide drawer** |
| 6 | 기존 프로젝트 컨벤션 준수 | **JSP/JS/CSS 분리, MyBatis, DataTable 그리드** |

### 1.3 비기능 요구사항

- 응답 속도: 4,000+ 종목 대상 동적 평가 → **5초 이내**
- 신규 지표 추가 시 **기존 지표/화면 영향 0** (소스 독립성)
- 모바일 화면 미지원 (PC 전용 — `kisFinanceMobile`은 별도)

---

## 2. 전체 아키텍처

### 2.1 계층 구조

```
┌─────────────────────────────────────────────────────────────┐
│  [Frontend]  recPickDynamic.jsp / .js / .css                │
│   ├ 지표 패널 (좌)        ── IndicatorPanelRenderer.js       │
│   ├ 결과 그리드 (중)       ── dataTableGridNew (common)       │
│   └ 상세 슬라이드 (우)     ── DetailDrawer.js                 │
│                                                              │
│  [Indicator JS Modules]   js/indicators/*.js (플러그인)       │
│   ├ IndicatorRegistry     모든 지표 등록/조회                 │
│   ├ IndicatorBase         기본 인터페이스 (render, validate)  │
│   └ {Indicator}.js        지표별 파일 (1지표 = 1파일)         │
└─────────────────────────────────────────────────────────────┘
                          │ HTTP POST (JSON)
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  [Controller]  RecPickDynamicController.java                 │
│   POST /stock/recPickDynamic/list.do                         │
│   POST /stock/recPickDynamic/preset/save.do                  │
│   GET  /stock/recPickDynamic/preset/list.do                  │
│                                                              │
│  [Service]  RecPickDynamicService.java                       │
│   - selectStockList(filterRequest)                           │
│   - 등록된 Indicator 들을 순회하며 평가                       │
│                                                              │
│  [Indicator Plugins]  com.scheduler.stock.indicator.impl.*  │
│   ├ Indicator (interface)                                    │
│   ├ IndicatorRegistry (Spring bean, @PostConstruct 자동수집)  │
│   └ {Indicator}.java (1지표 = 1파일)                          │
│                                                              │
│  [DAO]  RecPickDynamicDao.java                               │
│   - selectBaseUniverse(market filter)                        │
│   - TB_REC_SIGNAL 최신 BASE_DT 기준 조회                      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  [DB]  Oracle                                                │
│   ├ TB_REC_SIGNAL          (기존)                            │
│   ├ TB_STK_MASTER          (기존)                            │
│   └ TB_USER_FILTER_PRESET  (신규 - 옵션)                      │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 데이터 흐름

```
[사용자]  지표 선택/파라미터 조정
   │
   ▼
[JS]  IndicatorPanel.collectFilterRequest()
   │   → { market: "KR", indicators: [{id:"GOLDEN", params:{...}}, ...] }
   ▼
[Ajax]  POST /stock/recPickDynamic/list.do
   │
   ▼
[Controller]  파라미터 파싱 → Service 호출
   │
   ▼
[Service]
   1. RecPickDynamicDao.selectBaseUniverse() — 후보 종목 조회 (TB_REC_SIGNAL 최신본)
   2. for each 종목:
        for each 사용자 선택 지표:
          IndicatorRegistry.get(id).evaluate(stock, params)
        모든 지표 통과 → 결과 리스트 추가
   3. 정렬/페이지 처리
   ▼
[Response]  DataTableSettingVo 형식 반환
   ▼
[JS]  그리드 렌더링 + 상세 슬라이드 활성화
```

---

## 3. 지표 모듈화 설계 (핵심)

### 3.1 Backend — Plugin Pattern

#### 3.1.1 지표 인터페이스

```java
// src/com/scheduler/stock/indicator/Indicator.java (신규)
public interface Indicator {

    /** 지표 고유 ID (예: "GOLDEN_ARRAY", "RSI_14") */
    String getId();

    /** UI 표시명 (예: "정배열 필터") */
    String getDisplayName();

    /** 지표가 속한 카테고리 (예: "추세", "모멘텀", "거래량") */
    String getCategory();

    /** 사용자 파라미터 정의 (UI 자동 생성용 메타데이터) */
    List<ParameterDef> getParameterDefs();

    /**
     * 종목이 이 지표 조건을 통과하는지 평가
     * @param ctx  종목 데이터(TB_REC_SIGNAL row + 보조 데이터)
     * @param params  사용자가 화면에서 입력한 파라미터값
     * @return 통과=true, 탈락=false
     */
    boolean evaluate(IndicatorContext ctx, Map<String, Object> params);
}
```

#### 3.1.2 ParameterDef (UI 메타데이터)

```java
public class ParameterDef {
    private String key;          // "minTrendStrength"
    private String label;        // "최소 추세 강도(%)"
    private String inputType;    // "number" | "range" | "checkbox" | "select"
    private Object defaultValue; // 2.0
    private Object minValue;     // 0
    private Object maxValue;     // 50
    private List<Option> options; // select 타입일 때
}
```

#### 3.1.3 IndicatorRegistry

```java
@Component
public class IndicatorRegistry {
    private final Map<String, Indicator> registry = new HashMap<>();

    @Autowired
    public IndicatorRegistry(List<Indicator> indicators) {
        // Spring이 자동으로 모든 Indicator 빈을 수집 → 등록
        for (Indicator ind : indicators) {
            registry.put(ind.getId(), ind);
        }
    }

    public Indicator get(String id) { return registry.get(id); }
    public List<Indicator> listAll() { return new ArrayList<>(registry.values()); }
}
```

#### 3.1.4 신규 지표 추가 절차 (3단계)

```
1. com.scheduler.stock.indicator.impl 패키지에 클래스 1개 추가
   예: RsiIndicator.java
   - implements Indicator
   - @Component 어노테이션 부착 (Spring이 자동 수집)

2. evaluate() 로직 작성
   public boolean evaluate(IndicatorContext ctx, Map<String,Object> params) {
       int period = (Integer) params.get("period");      // 사용자 입력
       double maxRsi = (Double) params.get("maxRsi");
       double rsi = calculateRsi(ctx.getPriceList(), period);
       return rsi <= maxRsi;
   }

3. webapp/appone/jsp/stock/js/indicators/RsiIndicator.js 추가
   - IndicatorRegistry.register("RSI", { id, label, paramDefs, render })

→ 기존 코드 수정 0줄, 화면 영향 0
```

### 3.2 Frontend — Plugin Pattern

#### 3.2.1 디렉토리 구조 (신규)

```
webapp/appone/jsp/stock/
├── recPickDynamic.jsp                    (신규)
├── recPickDynamic.js                     (신규 - 메인 페이지 컨트롤러)
├── recPickDynamic.css                    (신규)
└── js/indicators/                        (신규 - 지표 모듈)
    ├── IndicatorRegistry.js              (전역 레지스트리)
    ├── IndicatorBase.js                  (공통 베이스)
    ├── GoldenArrayIndicator.js           (정배열)
    ├── MonthUpIndicator.js               (당월 상승)
    ├── TrendStrengthIndicator.js         (추세 강도)
    ├── VolumeFilterIndicator.js          (거래대금 필터)
    ├── PriceRangeIndicator.js            (가격대 필터)
    ├── RsiIndicator.js                   (RSI - 신규)
    └── MacdIndicator.js                  (MACD - 신규)
```

#### 3.2.2 JS 지표 모듈 표준 형식

```javascript
// js/indicators/RsiIndicator.js
(function (registry) {
    "use strict";

    registry.register({
        id: "RSI",
        label: "RSI 과매수 필터",
        category: "모멘텀",

        // UI 파라미터 정의 (자동으로 입력 컨트롤 생성됨)
        params: [
            { key: "period",  label: "기간(일)", type: "number", default: 14, min: 5, max: 30 },
            { key: "maxRsi",  label: "최대 RSI",  type: "range",  default: 70, min: 0, max: 100 }
        ],

        // 라벨 렌더링 (그리드 매칭 표시용 - 옵션)
        renderBadge: function (params) {
            return "RSI(" + params.period + ") ≤ " + params.maxRsi;
        }
    });
})(window.IndicatorRegistry);
```

JS 파일은 단순히 `<script>` 태그로 추가만 하면 자동 등록됨 → **메인 코드 수정 불필요**.

---

## 4. 화면 설계

### 4.1 레이아웃 (PC 1920×1080 기준)

```
┌──────────────────────────────────────────────────────────────────────────┐
│  [헤더] 동적 추천 종목 선별                                                │
├──────────────┬───────────────────────────────────────────┬───────────────┤
│              │                                            │               │
│  지표 패널    │   결과 그리드                              │  상세 슬라이드 │
│   (300px)    │                                            │   (450px)     │
│              │                                            │   ※ 평소 hidden│
│ □ 정배열      │  No │ 종목 │ 시장 │ 현재가 │ 매칭지표 │... │   클릭시 슬라이드│
│   ▶ 4구간     │  1  │ 삼성 │ KOSPI│ 70k   │ 정배열✓  │    │               │
│ □ 추세강도    │  2  │ 셀트리│KOSPI │ 180k  │ 정배열✓  │    │   [차트]      │
│   ▶ ≥ 2.0%   │  ...                                      │   [지표 상세]  │
│ □ 거래대금    │                                            │   [매칭조건]  │
│   ▶ 50억 이상 │                                            │   ★ 픽 등록   │
│ □ RSI(신규)  │                                            │               │
│ □ MACD(신규) │                                            │               │
│              │                                            │               │
│ [+ 지표 추가] │  현재 1-50 / 전체 234건  [페이지네이션]      │               │
│              │                                            │               │
│ [🔄 적용]    │                                            │               │
│ [💾 프리셋]  │                                            │               │
│              │                                            │               │
└──────────────┴───────────────────────────────────────────┴───────────────┘
```

### 4.2 컴포넌트 상세

#### A. 지표 패널 (좌)

- **시장 필터** (탭): 국내 / 해외 (스크린샷 참고)
- **세부 필터**: 시장(전체/코스피/코스닥), 지수(KOSPI200/KOSDAQ150), 종목 유형(주식/ETF/...)
- **지표 체크리스트**:
  - 각 지표는 카드 형태
  - 체크박스로 ON/OFF
  - 체크 시 파라미터 입력 컨트롤 자동 표시 (`ParameterDef.inputType` 기준)
- **하단 액션**:
  - `🔄 적용` → AJAX 호출 (디바운스 300ms)
  - `💾 프리셋 저장` → 현재 설정 저장
  - `📋 프리셋 불러오기` → 저장된 설정 목록

#### B. 결과 그리드 (중)

| 컬럼 | 설명 |
|------|------|
| No | 순번 |
| 종목코드 / 종목명 | TB_STK_MASTER 기반 |
| 시장 | KOSPI / KOSDAQ / NASDAQ / NYSE |
| 현재가 | curPrice |
| 등락률 | monChgRate (%) |
| 매칭지표 | 통과한 지표 뱃지 (예: `정배열✓ 추세✓ RSI✓`) |
| 픽 등록 | ★ 버튼 (클릭 시 즉시 등록) |

- 정렬: 시가총액/등락률/추세강도 등
- 페이징: `dataTableGridNew` 활용
- **행 클릭 시 → 우측 슬라이드 패널 오픈**

#### C. 상세 슬라이드 (우)

평소 hidden (translateX(100%)). 종목 클릭 시 슬라이드 인.

```
┌─────────────────────────────────┐
│  ← 닫기              종목명 (코드)│
├─────────────────────────────────┤
│  [Highcharts 차트 영역]          │
│  - 일봉 + MA5/20/60/120/240      │
│  - 골든크로스 flag                │
├─────────────────────────────────┤
│  📊 지표 상세                    │
│  ┌─────────┬─────────────────┐  │
│  │ MA5     │ 71,200          │  │
│  │ MA20    │ 69,500          │  │
│  │ ...                        │  │
│  │ TREND   │ 4.8% 🟢 강한 추세│  │
│  │ AVG_VOL │ 850억 (상위 10%)│  │
│  └────────────────────────────┘  │
├─────────────────────────────────┤
│  ✅ 매칭 조건                    │
│  • 정배열: MA5>20>60>120>240 ✓  │
│  • 추세강도: 4.8% ≥ 2.0% ✓      │
│  • 거래대금: 850억 ≥ 50억 ✓     │
│  • RSI(14): 65 ≤ 70 ✓          │
├─────────────────────────────────┤
│  [★ 관심종목에 추가]              │
└─────────────────────────────────┘
```

### 4.3 사용자 흐름 (Wireframe)

1. 화면 진입 → 디폴트 지표 (정배열 + 당월상승) 적용된 결과 표시
2. 사용자가 좌측 패널에서 RSI 추가 체크 → 파라미터 슬라이더 노출
3. 슬라이더 조정 → 디바운스 300ms 후 자동 갱신 (또는 `🔄 적용` 버튼)
4. 그리드에서 종목 클릭 → 우측 슬라이드 패널 오픈, Highcharts + 상세
5. `★ 픽 등록` 클릭 → 기존 `/stock/recPick/saveToWatchlist.do` 호출
6. 토스트 알림 "픽 등록 완료" 표시

---

## 5. API 설계

### 5.1 종목 조회

```
POST /stock/recPickDynamic/list.do
Content-Type: application/json

Request:
{
  "market": "KR",                          // KR | US
  "filters": {
    "marketType": "ALL",                   // ALL | KOSPI | KOSDAQ
    "indexType": "ALL",                    // ALL | KOSPI200 | KOSDAQ150
    "stockType": "STOCK"                   // STOCK | ETF | ETN | ELW
  },
  "indicators": [
    { "id": "GOLDEN_ARRAY", "params": {} },
    { "id": "TREND_STRENGTH", "params": { "min": 2.0 } },
    { "id": "VOLUME_FILTER", "params": { "minTradeValue": 5000000000 } },
    { "id": "RSI", "params": { "period": 14, "maxRsi": 70 } }
  ],
  "sort": "trendStrength",                 // 정렬 컬럼
  "sortDir": "desc",
  "start": 0,
  "length": 50
}

Response:
{
  "result_code": "SUCCESS",
  "result_msg": "조회 성공",
  "result_vo": {
    "data": [ ... 종목 리스트 ... ],
    "draw": 1,
    "start_no": 1,
    "page_length": 50,
    "recordsFiltered": 234,
    "recordsTotal": 4343,
    "matchedIndicatorMap": {              // 종목별 매칭 지표 ID
      "005930": ["GOLDEN_ARRAY","TREND_STRENGTH","RSI"]
    }
  }
}
```

### 5.2 종목 상세

```
GET /stock/recPickDynamic/detail.do?stkCd=005930&baseDt=20260507

Response: TB_REC_SIGNAL row + 일봉 200일치 (차트용)
```

### 5.3 지표 메타데이터 조회

```
GET /stock/recPickDynamic/indicatorMeta.do

Response:
{
  "result_code": "SUCCESS",
  "result_vo": [
    {
      "id": "GOLDEN_ARRAY",
      "label": "정배열 필터",
      "category": "추세",
      "params": [...]
    },
    ...
  ]
}
```

→ 화면 진입 시 1회 호출, 지표 패널 자동 렌더링

### 5.4 픽 등록 (기존 API 재활용)

```
POST /stock/recPick/saveToWatchlist.do
Body: { baseDt, mktCd, stkCd }
```

---

## 6. DB 설계

### 6.1 신규 테이블 (옵션 - 프리셋 저장 기능 시)

```sql
-- 사용자 필터 프리셋 (DDL_TB_USER_FILTER_PRESET.sql)
CREATE TABLE TB_USER_FILTER_PRESET (
    PRESET_ID       NUMBER          PRIMARY KEY,
    USER_ID         VARCHAR2(50)    NOT NULL,
    PRESET_NAME     VARCHAR2(100)   NOT NULL,
    PRESET_TYPE     VARCHAR2(20)    DEFAULT 'REC_PICK',  -- 확장 대비
    FILTER_JSON     CLOB            NOT NULL,            -- 전체 필터 JSON
    USE_COUNT       NUMBER          DEFAULT 0,
    LAST_USED_AT    DATE,
    REG_DT          DATE            DEFAULT SYSDATE,
    UPD_DT          DATE
);

CREATE INDEX IX_USER_FILTER_PRESET_USER ON TB_USER_FILTER_PRESET(USER_ID, PRESET_TYPE);
```

### 6.2 기존 테이블 활용

- `TB_REC_SIGNAL` — 1일 1회 배치로 지표값 사전 계산 (현재대로)
- `TB_STK_MASTER` — 종목 마스터
- `TB_S_RECO_PICK` — 픽 등록 (기존 재활용)

> ⚠️ **주의**: 동적 평가는 TB_REC_SIGNAL의 사전 계산값을 활용한다.
> 실시간 가격이 필요한 신규 지표(예: 분봉 RSI)는 KIS API 추가 호출이 필요하므로 별도 검토.

---

## 7. 구현 로드맵

### Phase 1 — 기반 구축 (3일)

- [ ] `Indicator` 인터페이스 + `IndicatorRegistry` 구현
- [ ] 기존 4개 지표 (정배열/당월상승/추세/거래대금)를 `Indicator` 구현체로 이식
- [ ] `RecPickDynamicController` + `Service` 골격
- [ ] `recPickDynamic.jsp` + 기본 화면 (지표 패널, 그리드)

### Phase 2 — 화면 완성 (2일)

- [ ] 슬라이드 상세 패널 + Highcharts 통합
- [ ] 픽 등록 버튼 (기존 API 재활용)
- [ ] 매칭 지표 뱃지 표시
- [ ] 디바운스 자동 갱신

### Phase 3 — 신규 지표 (지속)

- [ ] RSI 지표
- [ ] MACD 지표
- [ ] 가격대/시가총액 필터
- [ ] 52주 신고가 근접 필터

### Phase 4 — 프리셋 (옵션, 1일)

- [ ] `TB_USER_FILTER_PRESET` 생성
- [ ] 저장/불러오기 UI

---

## 8. 기존 시스템과의 관계

| 화면 | URL | 역할 | 변경 |
|------|-----|------|------|
| **기존 추천 시그널** | `/stock/recSignal/listView.do` | 배치 결과 보기 (정적) | 변경 없음 ✅ |
| **신규 동적 선별** | `/stock/recPickDynamic/listView.do` | 사용자 동적 필터 | 신규 |
| 픽 등록 화면 | `/stock/recPick/listView.do` (예정) | 등록된 픽 추적 | 별도 |

- **공존 전략**: 두 화면 모두 메뉴에 노출. 사용자가 선택.
- **데이터 공유**: `TB_REC_SIGNAL` 일배치 결과를 양쪽 다 활용.

---

## 9. 컨벤션 준수 확인

- ✅ JSP/JS/CSS 분리 (CLAUDE.md 규칙)
- ✅ `dataTableGridNew` + `DataTableSettingVo` 활용
- ✅ `ResponseHandler.sendResponse` 응답 패턴
- ✅ `ajaxRequest` Ajax 호출 패턴
- ✅ MyBatis XML mapper (`oracle_RecPickDynamic.xml`)
- ✅ `*.do` URL 패턴
- ✅ Korean 주석/UI

---

## 10. 위험 요소 및 대응

| 위험 | 대응 |
|------|------|
| 4,000 종목 × 5+ 지표 평가 시 응답 지연 | TB_REC_SIGNAL 사전계산값 활용, JOIN 최소화 |
| 신규 지표 추가 시 기존 지표 영향 | Plugin Pattern + 단위 테스트 |
| 사용자 파라미터 검증 누락 | `ParameterDef.minValue/maxValue` 서버 재검증 |
| TB_REC_SIGNAL 신규 지표 컬럼 부재 | DDL ALTER 필요 → **사전 사용자 승인** (CLAUDE.md 규칙) |

---

**문서 끝.**
다음 단계: 목업 페이지(`recPickDynamic_mockup.html`)에서 시각적 확인 → 사용자 승인 → Phase 1 구현 진입.
