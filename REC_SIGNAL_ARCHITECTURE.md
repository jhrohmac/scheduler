# REC_SIGNAL_RUN 아키텍처 설계 (시각화)

---

## 📊 현재 구조 vs 개선 구조

### **BEFORE (현재 - 하드코딩)**
```
┌─────────────────────────────────────────┐
│       RecSignalRunTask (배치 시작)      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│   RecSignalService.runBatch()           │
│   └─ buildRecSignal(stock, prices)      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  신호 판단 (고정 로직)                   │
│  ├─ if (isGoldenCross && isMonthUp)     │
│  │   recYn = "Y"                        │
│  └─ grade = calcGrade(...)              │
│                                          │
│  [문제점]                                 │
│  ✗ 조건 추가/수정 시 코드 변경 필수     │
│  ✗ 조건별 성공/실패 추적 어려움         │
│  ✗ 다양한 신호 조합 테스트 불가        │
└─────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│   TB_REC_SIGNAL 저장                    │
│   recYn, recGrade, recReason만 저장    │
└─────────────────────────────────────────┘
```

---

### **AFTER (개선 - 구성 기반)**
```
┌──────────────────────────────────────────────────────┐
│           RecSignalRunTask (배치 시작)                │
│           ├─ marketGroup (KR/US)                      │
│           └─ params (days, interval, etc.)            │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
      ╔══════════════════════════════╗
      ║  RecSignalConditionService   ║  ◄─ [신규]
      ║  └─ loadBatchConfig(batchId) ║
      ║    (조건 선택 조회)            ║
      ╚════────┬─────────────────────╝
               │
               ▼ selectedCondIds
┌──────────────────────────────────────────────────────┐
│   RecSignalService.runBatch()                        │
│   └─ buildRecSignal(stock, prices, selectedConds)   │
└────────────────┬─────────────────────────────────────┘
                 │
                 ▼
      ╔══════════════════════════════════════════════╗
      ║   신호 판단 (동적 로직)                       ║
      ║                                               ║
      ║   selectedConds = ["GOLDEN_CROSS", "MONTH_UP"] │
      ║                                               ║
      ║   for each condId in selectedConds:          ║
      ║     ├─ evaluator = getEvaluator(condId)      ║
      ║     ├─ result = evaluator.evaluate(...)      ║
      ║     └─ results[condId] = result              ║
      ║                                               ║
      ║   recYn = allConditionsMet(results) ?        ║
      ║           "Y" : "N"                          ║
      ║   evalResults = JSON.stringify(results)      ║
      ║                                               ║
      ║   [개선점]                                    ║
      ║   ✓ UI에서 조건 선택 가능                     ║
      ║   ✓ 조건별 성공/실패 추적 (JSON)             ║
      ║   ✓ 다양한 신호 조합 테스트 가능             ║
      ║   ✓ 신규 조건 추가 시 코드 변경 최소화      ║
      ╚════────┬─────────────────────────────────────╝
               │
               ▼
┌──────────────────────────────────────────────────────┐
│   TB_REC_SIGNAL 저장                                 │
│   ├─ recYn, recGrade, recReason (기존)             │
│   ├─ evalConds (평가한 조건 ID 목록)                │
│   ├─ evalResults ({"GOLDEN_CROSS": true, ...})    │
│   └─ failReason (신호 실패 원인 분류)              │
└──────────────────────────────────────────────────────┘
```

---

## 🔧 조건 평가 아키텍처

### **Strategy Pattern: IConditionEvaluator**

```
IConditionEvaluator (인터페이스)
├── boolean evaluate(Stock, List<DlyPrice>)
└── String getConditionId()

        △
        │ implements
        ├──────────────────────────────────────┐
        │                                      │
    ┌───┴─────────────────┐    ┌──────────────┴──────┐
    │                     │    │                     │
GoldenCrossEvaluator   MonthUpEvaluator   TrendStrengthEvaluator
(MA5>20>60>120>240)    (Price>MonOpen)    (TrendStrength > 0.7)
    │                     │                     │
    └─────────────────────┴─────────────────────┘
           △ 저장: evaluatorMap
           │
    ┌──────┴──────────────┐
    │                     │
  loadBatchConfig()    evaluateCondition()
  (DB 조회)            (동적 평가)
```

### **평가 흐름**

```
┌────────────────────────────────────────┐
│   Stock, List<DlyPrice> 입력            │
└─────────────┬──────────────────────────┘
              │
              ▼
    ╔═════════════════════════════╗
    ║ Batch Config 조회            ║
    ║ ("BATCH_REC_SIGNAL_KR_PRIMARY")  ║
    ║ → selectedConds = [          ║
    ║     "GOLDEN_CROSS",          ║
    ║     "MONTH_UP",              ║
    ║     "TREND_STRENGTH"         ║
    ║   ]                          ║
    ╚════────┬────────────────────╝
             │
             ▼
    ╔════════════════════════════════════╗
    ║  for each condId in selectedConds  ║
    ║  (병렬/순차 처리)                   ║
    ║                                    ║
    ║  ┌─ GOLDEN_CROSS                  ║
    ║  │  Evaluator.evaluate() → true   ║
    ║  │                                ║
    ║  ├─ MONTH_UP                      ║
    ║  │  Evaluator.evaluate() → true   ║
    ║  │                                ║
    ║  └─ TREND_STRENGTH                ║
    ║     Evaluator.evaluate() → false  ║
    ╚════────┬──────────────────────────╝
             │
             ▼
    ┌──────────────────────────────────┐
    │  evalResults = {                 │
    │    "GOLDEN_CROSS": true,         │
    │    "MONTH_UP": true,             │
    │    "TREND_STRENGTH": false       │
    │  }                               │
    │                                  │
    │  recYn = (true && true && false) │
    │         = false (모두 만족 안함) │
    └────────┬───────────────────────┘
             │
             ▼
    ┌──────────────────────────────────┐
    │  RecSignal 레코드 생성/저장       │
    │  ├─ recYn: "N"                   │
    │  ├─ evalConds: "GOLDEN_CROSS,   │
    │  │             MONTH_UP,         │
    │  │             TREND_STRENGTH"   │
    │  ├─ evalResults: JSON (위)      │
    │  └─ failReason: "TREND_STRENGTH" │
    └──────────────────────────────────┘
```

---

## 📋 조건 목록 및 분류

### **기본 조건 (BASIC)**
```
┌─────────────────────────────────────────────────────┐
│ ID: GOLDEN_CROSS                                    │
│ 이름: MA 정배열 (지수이동평균 정렬)                  │
├─────────────────────────────────────────────────────┤
│ 조건: MA5 > MA20 > MA60 > MA120 > MA240            │
│ 의미: 명확한 상승 추세                               │
│ 계산: MaCalculateService.isGoldenArray(maResult)   │
│ 신호: STRONG (상향 신호 강함)                       │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ ID: MONTH_UP                                        │
│ 이름: 월상승 (기간 내 양의 변화)                    │
├─────────────────────────────────────────────────────┤
│ 조건: CurrentPrice > MonthOpenPrice                │
│ 의미: 이번 달 시가 대비 현재가 상승                 │
│ 계산: (currentPrice > monOpenPrice)                │
│ 신호: MOMENTUM (상향 모멘텀)                        │
└─────────────────────────────────────────────────────┘
```

### **확장 조건 (EXTENDED)** [개발 예정]
```
┌─────────────────────────────────────────────────────┐
│ ID: MACD_OSC                                        │
│ 이름: MACD+OSC 신호                                 │
├─────────────────────────────────────────────────────┤
│ 조건: MACD > Signal Line (0선 위) &                │
│      OSC > 0                                        │
│ 의미: 추가 모멘텀 확인                               │
│ 계산: MacdCalculateService.isSignal(...)          │
│ 신호: CONFIRMATION (신호 확인)                      │
│ 상태: 개발중 (UI 체크박스 Disabled)                │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ ID: VOLUME_SURGE                                    │
│ 이름: 거래량 급증                                    │
├─────────────────────────────────────────────────────┤
│ 조건: CurrentVolume > (Avg20Day * 1.5)             │
│ 의미: 거래량 급증 = 강한 수급 신호                 │
│ 계산: VolumeAnalyzer.isSurgeDetected(...)         │
│ 신호: STRENGTH (신호 강도)                          │
│ 상태: 개발중                                        │
└─────────────────────────────────────────────────────┘
```

### **지표 조건 (INDICATOR)**
```
┌─────────────────────────────────────────────────────┐
│ ID: TREND_STRENGTH                                  │
│ 이름: 추세강도 (추세의 진전도)                       │
├─────────────────────────────────────────────────────┤
│ 조건: TrendStrength > 0.7 (70% 이상)               │
│ 의미: 강한 추세가 진행 중                           │
│ 계산: MaCalculateService.calculateTrendStrength()│
│ 신호: QUALITY (신호 품질)                           │
│ 상태: 사용 가능 (Y)                                 │
└─────────────────────────────────────────────────────┘
```

---

## 🎯 배치 조건 선택 예시

### **KR 시장 - Primary Batch**
```
Batch ID: BATCH_REC_SIGNAL_KR_PRIMARY
Market: KR

선택 조건 (checkboxes):
┌─────────────────────────────────────┐
│ [✓] GOLDEN_CROSS  MA 정배열          │
│ [✓] MONTH_UP      월상승             │
│ [✗] MACD_OSC      MACD+OSC (개발중) │
│ [✗] VOLUME_SURGE  거래량 급증        │
│ [✓] TREND_STRENGTH 추세강도          │
└─────────────────────────────────────┘

결과:
  → 3개 조건 선택
  → 3개 모두 만족 시에만 recYn = "Y"
  → 조건 조합: (Golden AND MonUp AND TrendStrong)
```

### **US 시장 - Primary Batch**
```
Batch ID: BATCH_REC_SIGNAL_US_PRIMARY
Market: US

선택 조건:
┌─────────────────────────────────────┐
│ [✓] GOLDEN_CROSS  MA 정배열          │
│ [✓] MONTH_UP      월상승             │
│ [✗] MACD_OSC      MACD+OSC          │
│ [✓] VOLUME_SURGE  거래량 급증        │
│ [✗] TREND_STRENGTH 추세강도          │
└─────────────────────────────────────┘

결과:
  → 3개 조건 선택
  → 3개 모두 만족 시에만 recYn = "Y"
  → 조건 조합: (Golden AND MonUp AND VolumeSurge)
```

---

## 📊 신호 생성 결과 시각화 (UI)

### **배치 조건 선택 화면 (batchAdmin.jsp)**
```
┌─────────────────────────────────────────────────┐
│  Stock Batch Admin - REC_SIGNAL_RUN             │
├─────────────────────────────────────────────────┤
│                                                  │
│  📋 신호 생성 조건 선택 (배치: KR_PRIMARY)      │
│                                                  │
│  ┌─ 기본 조건 (BASIC)                           │
│  │  ☑ MA 정배열 (MA5>20>60>120>240)             │
│  │    └─ 명확한 상승 추세를 나타냅니다.          │
│  │  ☑ 월상승 (이번달 시가 대비 현재가 상승)    │
│  │    └─ 기간 내 긍정적 흐름을 나타냅니다.      │
│  │                                              │
│  ├─ 확장 조건 (EXTENDED)                        │
│  │  ☐ (개발중) MACD+OSC 신호      [Disabled]   │
│  │    └─ 추가 모멘텀 확인용                     │
│  │                                              │
│  ├─ 거래량 조건 (VOLUME)                        │
│  │  ☐ (개발중) 거래량 급증        [Disabled]   │
│  │    └─ 20일 평균 대비 150% 이상              │
│  │                                              │
│  └─ 지표 조건 (INDICATOR)                       │
│     ☑ 추세강도 (TrendStrength > 0.7)           │
│       └─ 강한 추세가 진행 중                    │
│                                                  │
│  ✓ 선택된 조건: 3개 (GOLDEN_CROSS, MONTH_UP, TREND_STRENGTH)
│                                                  │
│  [저장]  [취소]  [즉시 실행]                     │
└─────────────────────────────────────────────────┘
```

### **신호 리스트 조건 충족 현황 (kisFinance.jsp)**
```
┌──────────────────────────────────────────────────────┐
│ REC_SIGNAL List (Base Date: 2026-04-15)             │
├──────────────────────────────────────────────────────┤
│                                                      │
│ 종목명     │ 신호 │ 정배열 │ 월상승 │ 추세강 │ 등급 │
├──────────────────────────────────────────────────────┤
│ 삼성전자   │  ✓  │   ✓   │   ✓   │   ✓   │  A  │
│ SK하이닉스 │  ✓  │   ✓   │   ✓   │   ✓   │  B  │
│ LG디스플레 │  ✗  │   ✓   │   ✓   │   ✗   │  -  │
│ 현대차     │  ✗  │   ✗   │   ✓   │   ✓   │  -  │
│ 셀트리온   │  ✗  │   ✗   │   ✗   │   ✓   │  -  │
│                                                      │
│ ✓ = 조건 충족, ✗ = 조건 미충족                     │
│ 신호 기준: 선택 조건 모두 만족                      │
└──────────────────────────────────────────────────────┘
```

### **신호 상세 차트 (조건 표시)**
```
차트 영역:
  ┌────────────────────────────────────────┐
  │         Price Chart (with MA lines)     │
  │                                          │
  │      MA240                               │
  │       MA120                              │
  │        MA60           ◢ Current Price   │
  │         MA20      ◢◢◢                   │
  │          MA5  ◢◢◢                       │
  │       ◢◢◢◢                              │
  │  ◢◢◢◢                                   │
  │ │◢◢◢◢ Golden Cross: YES (정배열)       │
  │ │║                                      │
  │ │║  Month Up: YES (월상승)              │
  │ │║                                      │
  │ │║  Trend Strength: 0.82 (강함)        │
  │ │║                                      │
  └─┼────────────────────────────────────┘
    └─ Base Date: 2026-04-15
       Signal: ✓ YES (A등급)
       
  범례:
  ■ Golden Cross Period (정배열)
  ■ Month Up Period (월상승 구간 배경 칠하기)
  ⚡ Signal Generated (깃발 표시)
```

---

## 🔄 신호 평가 로직 (Java Pseudo Code)

```java
// Phase 1: 배치 조건 로드
String batchId = "BATCH_REC_SIGNAL_KR_PRIMARY";
List<String> selectedCondIds = 
    recSignalConditionService.loadBatchConfig(batchId);
    // → ["GOLDEN_CROSS", "MONTH_UP", "TREND_STRENGTH"]

// Phase 2: 각 종목별 신호 생성
for (RecSignalDto stock : stockList) {
    List<DlyPriceDto> prices = 
        kisDlyPriceSyncService.fetchAdjustedDailyPrices(...);
    
    // Phase 3: 조건별 평가
    Map<String, Boolean> evalResults = new HashMap<>();
    for (String condId : selectedCondIds) {
        IConditionEvaluator evaluator = 
            evaluatorMap.get(condId);
        boolean result = evaluator.evaluate(stock, prices);
        evalResults.put(condId, result);
    }
    
    // Phase 4: 최종 신호 결정
    boolean allConditionsMet = 
        evalResults.values().stream()
            .allMatch(r -> r == true);
    
    String recYn = allConditionsMet ? "Y" : "N";
    String evalCondsCsv = String.join(",", selectedCondIds);
    String evalResultsJson = toJson(evalResults);
    // evalResults = {
    //   "GOLDEN_CROSS": true,
    //   "MONTH_UP": true,
    //   "TREND_STRENGTH": true
    // }
    
    // Phase 5: DB에 저장
    RecSignalDto signal = new RecSignalDto();
    signal.setRecYn(recYn);
    signal.setEvalConds(evalCondsCsv);
    signal.setEvalResults(evalResultsJson);
    signal.setFailReason(recYn.equals("Y") ? null : 
        findFailCondition(evalResults)); // "TREND_STRENGTH"
    
    recSignalDao.mergeRecSignal(signal);
}
```

---

## 📈 기대효과 비교

| 항목 | 현재 (하드코딩) | 개선 (동적) |
|------|-----------------|-----------|
| **조건 변경** | 코드 수정 + 재배포 | UI에서 선택 + 즉시 반영 |
| **조건 추가** | 코드 + 테스트 + 배포 | DB + Evaluator 추가 |
| **신호 추적** | recYn/Grade만 저장 | 각 조건별 성공/실패 추적 |
| **다중 조합** | 불가능 | 자유로운 조합 가능 |
| **운영 유연성** | 낮음 | 높음 |
| **성능** | 빠름 (평가 조건 최소) | 조금 느림 (평가 조건 증가 시) |

---

## 🚀 추진 일정 (5 Phase)

```
Week 1       Week 2       Week 3
├─ Phase 1   ├─ Phase 2   ├─ Phase 3   ├─ Phase 4   ├─ Phase 5
│ (DB)       │ (BE)       │ (FE)       │ (Chart)    │ (Test)
│ 1-2일      │ 2-3일      │ 1-2일      │ 1일        │ 1일
│
└─────────────────────────────────────────────────────────
  Total: 6-8일 예상
```

---

