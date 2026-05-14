# REC_SIGNAL_RUN 신호 분석 및 개발 계획

**작성일**: 2026-04-16  
**목표**: 매매 신호 조건의 구성/관리 체계 구축 및 시각화

---

## 1. 현재 시그널 조건 분석

### 1.1 현재 구현 상태 (RecSignalService.java:626~673)

#### **조건 구조 (하드코딩)**
```
추천 여부 (recYn)
├── Golden Cross (정배열) ✓ MA5 > MA20 > MA60 > MA120 > MA240
└── Month Up (월상승) ✓ CurrentPrice > MonthOpenPrice
    → 둘 다 만족 시만 recYn = "Y"

등급 (recGrade: A/B/C)
├── Grade A: Golden + MonUp + (CurPrice > MA5) + (MonChgRate >= 3%)
├── Grade B: Golden + MonUp + (CurPrice > MA5) + (0% < MonChgRate < 3%)
└── Grade C: Golden + MonUp만 만족
```

#### **사용 신호 매개변수**
| 필드 | 의미 | 계산처 |
|------|------|--------|
| `goldenYn` | 정배열 여부 | `MaCalculateService.isGoldenArray()` |
| `monUpYn` | 월상승 여부 | `currentPrice > monOpenPrice` |
| `monChgRate` | 월변화율(%) | `(currentPrice - monOpenPrice) / monOpenPrice * 100` |
| `trendStrength` | 추세강도 | `MaCalculateService.calculateTrendStrength()` |
| `avgTrdVal20` | 20일 평균 거래대금 | 자체 계산 |
| `curPrice` | 현재가 | KIS API |
| `ma5, ma20, ma60, ma120, ma240` | 이동평균선 | `MaCalculateService` |

---

## 2. 요청사항 분석

### 2.1 사용자 요구

#### **(1) 조건 항목 리스트 관리**
```
□ MACD + OSC 매수/매도 신호     [체크박스]
□ MA 정배열 (5>20>60>120>240) [체크박스]
□ 월상승 (MonUp)               [체크박스]
□ 추세강도 (TrendStrength)     [체크박스]
□ 거래대금 필터 (AvgTradVal20) [체크박스]
... 향후 조건 확장
```

#### **(2) 조건 기반 시각화**
- 신호 생성 시 **선택된 조건만 평가**
- 차트에서 **조건별 지표 표시** (Highcharts annotation)
- 신호 성공/실패 원인을 **조건별로 분류**

#### **(3) 배치 실행 범위**
- 조건 선택 UI → 배치 실행 요청
- 선택 조건 저장 → 재사용

---

## 3. 시스템 설계 (개발 범위)

### 3.1 DB 스키마 신규 / 변경

#### **신규: `TB_REC_SIGNAL_CONDITION` (신호 조건 마스터)**
```sql
CREATE TABLE TB_REC_SIGNAL_CONDITION (
    COND_ID        VARCHAR2(50) PRIMARY KEY,    -- GOLDEN_CROSS, MONTH_UP, MACD_OSC, etc.
    COND_NM        VARCHAR2(100) NOT NULL,      -- 정배열, 월상승, MACD+OSC 신호, etc.
    COND_TYPE      VARCHAR2(20) NOT NULL,       -- BASIC, EXTENDED, VOLUME, INDICATOR
    COND_DESC      VARCHAR2(500),                -- 조건 설명
    CALC_METHOD    VARCHAR2(200),                -- 계산 로직 (클래스 메소드 참조)
    ENABLED_YN     VARCHAR2(1) DEFAULT 'Y',     -- Y: 사용 가능, N: 비활성화
    CREATED_AT     TIMESTAMP DEFAULT SYSDATE,
    UPDATED_AT     TIMESTAMP DEFAULT SYSDATE
);

-- 초기 데이터
INSERT INTO TB_REC_SIGNAL_CONDITION VALUES ('GOLDEN_CROSS', 'MA 정배열', 'BASIC', 'MA5 > MA20 > MA60 > MA120 > MA240', 'MaCalculateService.isGoldenArray()', 'Y', SYSDATE, SYSDATE);
INSERT INTO TB_REC_SIGNAL_CONDITION VALUES ('MONTH_UP', '월상승', 'BASIC', 'CurrentPrice > MonthOpenPrice', 'calc.isMonthUp()', 'Y', SYSDATE, SYSDATE);
INSERT INTO TB_REC_SIGNAL_CONDITION VALUES ('MACD_OSC', 'MACD+OSC 신호', 'EXTENDED', 'MACD 0선 교차 + OSC 신호', 'MacdCalculateService.isSignal()', 'N', SYSDATE, SYSDATE);
INSERT INTO TB_REC_SIGNAL_CONDITION VALUES ('VOLUME_SURGE', '거래량 급증', 'VOLUME', '20일 평균 대비 거래량 > 150%', 'calc.isVolumeSurge()', 'N', SYSDATE, SYSDATE);
INSERT INTO TB_REC_SIGNAL_CONDITION VALUES ('TREND_STRENGTH', '추세강도', 'INDICATOR', 'TrendStrength > 0.7', 'calc.isTrendStrong()', 'Y', SYSDATE, SYSDATE);
```

#### **신규: `TB_REC_SIGNAL_BATCH_CONFIG` (배치별 조건 선택)**
```sql
CREATE TABLE TB_REC_SIGNAL_BATCH_CONFIG (
    CONFIG_ID       VARCHAR2(50) PRIMARY KEY,   -- BATCH_REC_SIGNAL_KR_PRIMARY, etc.
    BATCH_ID        VARCHAR2(100) NOT NULL,    -- BATCH_REC_SIGNAL_KR_PRIMARY
    MKT_CD          VARCHAR2(10) NOT NULL,     -- KR, US
    SELECTED_CONDS  VARCHAR2(500),              -- JSON: ["GOLDEN_CROSS", "MONTH_UP", ...]
    ENABLED_YN      VARCHAR2(1) DEFAULT 'Y',
    CREATED_AT      TIMESTAMP DEFAULT SYSDATE,
    UPDATED_AT      TIMESTAMP DEFAULT SYSDATE,
    CONSTRAINT FK_REC_SIG_BATCH_COND FOREIGN KEY (BATCH_ID) 
        REFERENCES TB_BATCH_MASTER(BATCH_ID)
);
```

#### **변경: `TB_REC_SIGNAL`**
```sql
-- 신규 컬럼 (신호 평가 추적용)
ALTER TABLE TB_REC_SIGNAL ADD (
    EVAL_CONDS      VARCHAR2(500),  -- 평가한 조건들 (JSON: ["GOLDEN_CROSS", "MONTH_UP"])
    EVAL_RESULTS    VARCHAR2(500),  -- 평가 결과 (JSON: {"GOLDEN_CROSS": true, "MONTH_UP": true})
    FAIL_REASON     VARCHAR2(500)   -- 신호 실패 원인 분류
);
```

---

### 3.2 백엔드 구조 변경

#### **A. 신조건 관리 서비스 (신규)**
```
src/com/scheduler/stock/service/RecSignalConditionService.java
├── loadConditionList(market)        // TB_REC_SIGNAL_CONDITION 로드
├── loadBatchConfig(batchId)         // TB_REC_SIGNAL_BATCH_CONFIG 조회
├── saveBatchConfig(batchId, condIds) // 배치별 조건 선택 저장
├── evaluateCondition(cond, stock, prices) // 개별 조건 평가
└── evaluateSignal(stock, conditions)     // 모든 조건 종합 평가
```

#### **B. RecSignalService 리팩토링**
```
buildRecSignal() 메소드 변경
  
BEFORE:
  if (isGolden && isMonthUp) recYn = "Y"
  
AFTER:
  List<String> selectedCondIds = loadBatchConfig(batchId)
  Map<String, Boolean> results = new HashMap<>();
  
  for (condId in selectedCondIds) {
    results.put(condId, evaluateCondition(condId, stock, prices))
  }
  
  recYn = allConditionsMet(results) ? "Y" : "N"
  evalResults = JSON.stringify(results)  // DB 저장
```

#### **C. RecSignalConditionEvaluator (조건 평가 전략 패턴)**
```
interface IConditionEvaluator {
  boolean evaluate(Stock stock, List<DlyPrice> prices);
}

class GoldenCrossEvaluator implements IConditionEvaluator { ... }
class MonthUpEvaluator implements IConditionEvaluator { ... }
class MacdOscEvaluator implements IConditionEvaluator { ... }
class VolumeSurgeEvaluator implements IConditionEvaluator { ... }

Map<String, IConditionEvaluator> evaluatorMap = new HashMap<>();
evaluatorMap.put("GOLDEN_CROSS", new GoldenCrossEvaluator());
evaluatorMap.put("MONTH_UP", new MonthUpEvaluator());
// ... 나머지
```

---

### 3.3 프론트엔드 변경

#### **1. 조건 선택 UI (batchAdmin.jsp)**
```html
<div class="modal-body">
  <!-- 기존: 배치 설정 -->
  
  <!-- 신규: 신호 조건 선택 -->
  <div class="form-group">
    <label>신호 생성 조건 선택</label>
    <div class="condition-list">
      <!-- BASIC 카테고리 -->
      <fieldset class="border p-2 mb-3">
        <legend class="w-auto">기본 조건 (BASIC)</legend>
        <div class="form-check">
          <input type="checkbox" id="cond_golden_cross" class="form-check-input condition-check" data-cond-id="GOLDEN_CROSS">
          <label class="form-check-label">MA 정배열 (MA5 > MA20 > MA60 > MA120 > MA240)</label>
          <small class="form-text text-muted d-block">추세가 명확한 상승추세를 나타냅니다.</small>
        </div>
        <div class="form-check">
          <input type="checkbox" id="cond_month_up" class="form-check-input condition-check" data-cond-id="MONTH_UP">
          <label class="form-check-label">월상승 (이번달 시가 대비 현재가 상승)</label>
          <small class="form-text text-muted d-block">기간 내 긍정적 흐름을 나타냅니다.</small>
        </div>
      </fieldset>
      
      <!-- EXTENDED 카테고리 -->
      <fieldset class="border p-2 mb-3">
        <legend class="w-auto">확장 조건 (EXTENDED)</legend>
        <div class="form-check">
          <input type="checkbox" id="cond_macd_osc" class="form-check-input condition-check" data-cond-id="MACD_OSC" disabled>
          <label class="form-check-label">(개발중) MACD+OSC 신호</label>
          <small class="form-text text-muted d-block">추가 모멘텀 확인용 (아직 미구현)</small>
        </div>
      </fieldset>
      
      <!-- VOLUME 카테고리 -->
      <fieldset class="border p-2">
        <legend class="w-auto">거래량 조건 (VOLUME)</legend>
        <div class="form-check">
          <input type="checkbox" id="cond_volume" class="form-check-input condition-check" data-cond-id="VOLUME_SURGE" disabled>
          <label class="form-check-label">(개발중) 거래량 급증</label>
          <small class="form-text text-muted d-block">20일 평균 대비 150% 이상 (아직 미구현)</small>
        </div>
      </fieldset>
    </div>
  </div>
</div>
```

#### **2. 신호 리스트 화면 개선 (kisFinance.jsp)**
```html
<!-- 신호 차트 영역 -->
<div class="chart-section">
  <div id="chart_signal"></div>
  
  <!-- 신호 조건 범례 (조건별 아이콘) -->
  <div class="signal-legend">
    <span class="badge badge-success">정배열</span>
    <span class="badge badge-warning">월상승</span>
    <span class="badge badge-info">추세강</span>
  </div>
  
  <!-- 신호별 조건 충족 현황 테이블 -->
  <table class="table table-sm signal-condition-table">
    <thead>
      <tr>
        <th>종목</th>
        <th>신호</th>
        <th>정배열</th>
        <th>월상승</th>
        <th>추세강</th>
        <th>등급</th>
      </tr>
    </thead>
    <tbody>
      <!-- 동적 생성 -->
      <tr>
        <td>삼성전자</td>
        <td><span class="badge badge-success">YES</span></td>
        <td><i class="fas fa-check text-success"></i></td>
        <td><i class="fas fa-check text-success"></i></td>
        <td><i class="fas fa-check text-success"></i></td>
        <td>A</td>
      </tr>
    </tbody>
  </table>
</div>
```

#### **3. JS 모듈 신규 (js/recSignalCondition.js)**
```javascript
// 조건 선택 관리
class RecSignalConditionManager {
  loadConditions() { /* 조건 목록 로드 */ }
  getSelectedConditions() { /* 선택된 조건 ID 배열 반환 */ }
  saveConditionConfig(batchId, condIds) { /* 배치별 조건 선택 저장 */ }
  toggleCondition(condId) { /* 조건 체크/언체크 */ }
}

// 신호 조건 표시
class SignalConditionRenderer {
  renderConditionResults(signal, condResults) {
    // TB_REC_SIGNAL.EVAL_RESULTS JSON 파싱
    // 조건별 ✓/✗ 표시
  }
}
```

---

## 4. 개발 로드맵

### **Phase 1: DB & 기초 구조 (1-2일)**
- [ ] TB_REC_SIGNAL_CONDITION 테이블 생성 (기본 5개 조건 초기화)
- [ ] TB_REC_SIGNAL_BATCH_CONFIG 테이블 생성
- [ ] TB_REC_SIGNAL 컬럼 추가 (EVAL_CONDS, EVAL_RESULTS, FAIL_REASON)
- [ ] DAO 변경 (insert/update 쿼리 수정)

### **Phase 2: 백엔드 로직 (2-3일)**
- [ ] RecSignalConditionService 생성 (조건 로드/저장/평가)
- [ ] IConditionEvaluator 인터페이스 + 구현체들 (GoldenCross, MonthUp, TrendStrength, VolumeCheck)
- [ ] RecSignalService.buildRecSignal() 리팩토링 (조건 기반 평가)
- [ ] 단위테스트 (각 조건별 평가 로직 검증)

### **Phase 3: 프론트엔드 UI (1-2일)**
- [ ] batchAdmin.jsp: 조건 선택 UI 추가
- [ ] batchAdmin.js: 조건 저장/로드 로직
- [ ] kisFinance.jsp: 신호 조건 결과 표시 테이블
- [ ] recSignalCondition.js: 렌더링 로직

### **Phase 4: 시각화 (1일)**
- [ ] Highcharts 차트에 조건별 annotation 추가
  - MA선 (5/20/60/120/240) 기본 표시
  - 월상승/월하강 구간 배경색 표시
  - 정배열/역배열 상태 표시
- [ ] 신호 리스트에서 조건 충족 현황을 아이콘으로 표시

### **Phase 5: 검증 & 최적화 (1일)**
- [ ] 배치 재실행 (기존 신호 vs 신조건 신호 비교)
- [ ] 성능 테스트 (대량 종목 처리 시간)
- [ ] 문서화 (조건 추가 가이드)

---

## 5. 조건 추가 가이드 (향후 확장)

### **신규 조건 추가 방법**

#### **Step 1: DB 등록**
```sql
INSERT INTO TB_REC_SIGNAL_CONDITION 
VALUES ('NEW_CONDITION_ID', '조건명', 'CATEGORY', '조건설명', 'ClassName.methodName', 'Y', SYSDATE, SYSDATE);
```

#### **Step 2: Evaluator 구현**
```java
@Component
public class NewConditionEvaluator implements IConditionEvaluator {
    @Override
    public boolean evaluate(Stock stock, List<DlyPrice> prices) {
        // 조건 평가 로직
        return result;
    }
}
```

#### **Step 3: RecSignalConditionService에 등록**
```java
evaluatorMap.put("NEW_CONDITION_ID", new NewConditionEvaluator());
```

#### **Step 4: UI에 추가**
```html
<div class="form-check">
  <input type="checkbox" id="cond_new" class="form-check-input condition-check" data-cond-id="NEW_CONDITION_ID">
  <label>신규조건명</label>
</div>
```

---

## 6. 예상 효과

| 항목 | 개선 | 설명 |
|------|------|------|
| **유연성** | ⬆️ 대폭 증가 | 조건을 자유롭게 조합하여 신호 생성 |
| **추적성** | ⬆️ 증가 | EVAL_RESULTS에서 각 신호의 조건 충족 추적 가능 |
| **확장성** | ⬆️ 증가 | 새 조건 추가 시 기존 로직 영향 최소화 |
| **운영성** | ⬆️ 증가 | 배치 실행 전 조건을 선택하여 신호 품질 제어 |
| **신호 검증** | ⬆️ 증가 | 차트/테이블에서 조건별 성공/실패 원인 시각화 |

---

## 7. 위험요소 및 완화 방안

| 위험 | 영향 | 완화 방안 |
|------|------|----------|
| DB 스키마 변경 | 기존 신호 재계산 필요 | Phase 1에서 별도 컬럼으로 추가 (migration 불필요) |
| 성능 저하 | 종목당 조건 평가 오버헤드 | 조건 평가 결과 캐싱, 배치 병렬화 |
| 기존 신호 호환성 | 기존 API 클라이언트 영향 | EVAL_RESULTS는 선택적(nullable), recYn/recGrade는 기존 유지 |

---

## 8. 추가 고려사항

### **MACD+OSC 신호 (향후)**
- MacdCalculateService 추가 필요
- Daily price에서 MACD(12,26,9) 계산
- OSC (Oscillator) 계산 및 신호선 교차 감지
- 독립적인 buy/sell 신호 생성 가능

### **거래량 급증 필터 (향후)**
- 20일 평균 거래량 계산
- 당일 거래량 > (평균 * 1.5) 조건 평가
- 신호 신뢰도 증가

### **통계 분석 (향후)**
- 조건 조합별 신호 성공률 분석
- backtesting: 과거 신호 vs 실제 주가 상승
- 조건 최적화 (선택적 조건 제거/추가)

---

