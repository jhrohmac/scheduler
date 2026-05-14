# REC_SIGNAL_RUN 개발 계획 - 실행 요약

**작성**: 2026-04-16  
**목표**: 매매 신호 조건을 **하드코딩에서 구성 기반(Configuration-Based)** 으로 전환

---

## 📌 핵심 개선사항

### **현재 상태**
```
고정된 신호 조건 (하드코딩)
  ├─ Golden Cross (MA5>20>60>120>240): 필수
  └─ Month Up (월상승): 필수
  
→ 조건 추가/변경 시마다 코드 수정 필요
```

### **개선 후**
```
구성 가능한 신호 조건 (DB 기반)
  ├─ Condition Master (TB_REC_SIGNAL_CONDITION)
  ├─ Batch Config (TB_REC_SIGNAL_BATCH_CONFIG)
  ├─ Strategy Pattern Evaluators
  └─ UI 조건 선택
  
→ UI에서 선택 가능, 코드 수정 불필요
```

---

## 🎯 주요 개선 항목 (3가지)

### **(1) 조건 선택 UI**
```
batchAdmin.jsp에 "신호 조건 선택" 섹션 추가
┌──────────────────────────────┐
│ ☑ MA 정배열                   │
│ ☑ 월상승                      │
│ ☐ MACD+OSC (개발중)          │
│ ☐ 거래량 급증 (개발중)       │
│ ☑ 추세강도                    │
└──────────────────────────────┘

→ 선택된 조건만 배치에서 평가
→ 선택 내용 DB 저장 (재사용 가능)
```

### **(2) 신호 추적성 강화**
```
TB_REC_SIGNAL 테이블에 3개 컬럼 추가:
  ├─ EVAL_CONDS: 평가한 조건 목록
  │   예) "GOLDEN_CROSS,MONTH_UP,TREND_STRENGTH"
  │
  ├─ EVAL_RESULTS: 평가 결과 (JSON)
  │   예) {"GOLDEN_CROSS": true, "MONTH_UP": true, "TREND_STRENGTH": false}
  │
  └─ FAIL_REASON: 신호 실패 원인
      예) "TREND_STRENGTH" (이 조건만 미충족)

→ 신호별로 어떤 조건이 충족/미충족했는지 추적 가능
```

### **(3) 차트/테이블 시각화**
```
kisFinance.jsp 신호 리스트:
┌────────────────────────────────────────┐
│ 종목     │신호│ 정배열 │ 월상승 │ 추세강 │
├────────────────────────────────────────┤
│ 삼성전자  │ ✓ │   ✓   │   ✓   │   ✓  │
│ SK하이닉  │ ✓ │   ✓   │   ✓   │   ✓  │
│ LG디스플  │ ✗ │   ✓   │   ✓   │   ✗  │
└────────────────────────────────────────┘

→ 각 신호에 대해 조건별 충족 여부 시각화
→ 차트에 MA선, 월상승/월하강 구간 표시
```

---

## 📋 개발 범위 (5 Phase)

### **Phase 1: DB 스키마 (1-2일)**

**추가할 테이블:**
```sql
-- 신호 조건 마스터
CREATE TABLE TB_REC_SIGNAL_CONDITION (
    COND_ID VARCHAR2(50) PRIMARY KEY,
    COND_NM VARCHAR2(100),
    COND_TYPE VARCHAR2(20),        -- BASIC, EXTENDED, VOLUME, INDICATOR
    ENABLED_YN VARCHAR2(1)
);

-- 배치별 조건 선택 저장
CREATE TABLE TB_REC_SIGNAL_BATCH_CONFIG (
    CONFIG_ID VARCHAR2(50) PRIMARY KEY,
    BATCH_ID VARCHAR2(100),
    SELECTED_CONDS VARCHAR2(500)   -- JSON: ["GOLDEN_CROSS", "MONTH_UP", ...]
);
```

**기존 테이블 수정:**
```sql
ALTER TABLE TB_REC_SIGNAL ADD (
    EVAL_CONDS VARCHAR2(500),      -- 평가한 조건
    EVAL_RESULTS VARCHAR2(500),    -- 평가 결과 (JSON)
    FAIL_REASON VARCHAR2(500)      -- 실패 원인
);
```

**생성 SQ:**
- `DDL_TB_REC_SIGNAL_CONDITION.sql`
- `DDL_TB_REC_SIGNAL_BATCH_CONFIG.sql`

**DAO 수정:**
- `RecSignalDaoImpl.java`: insert/update/select 쿼리 수정
- `mapper/oracle_RecSignal.xml`: 새 컬럼 반영

---

### **Phase 2: 백엔드 로직 (2-3일)**

**신규 서비스:**
```java
// src/com/scheduler/stock/service/RecSignalConditionService.java
public class RecSignalConditionService {
    public List<RecSignalCondition> loadConditionList();
    public List<String> loadBatchConfig(String batchId);
    public void saveBatchConfig(String batchId, List<String> condIds);
    public Map<String, Boolean> evaluateConditions(
        String batchId, Stock stock, List<DlyPrice> prices);
}
```

**신규 인터페이스 & 구현체:**
```java
// src/com/scheduler/stock/service/condition/IConditionEvaluator.java
public interface IConditionEvaluator {
    boolean evaluate(RecSignalDto stock, List<DlyPriceDto> prices);
    String getConditionId();
}

// 구현체들:
// - GoldenCrossEvaluator
// - MonthUpEvaluator
// - TrendStrengthEvaluator
// - VolumeSurgeEvaluator (향후)
// - MacdOscEvaluator (향후)
```

**기존 서비스 수정:**
```java
// src/com/scheduler/stock/service/RecSignalService.java

// buildRecSignal() 메소드 변경
// BEFORE: if (isGoldenCross && isMonthUp) recYn = "Y"
// AFTER: 
//   List<String> selectedConds = conditionService.loadBatchConfig(batchId)
//   Map<String, Boolean> results = 
//       conditionService.evaluateConditions(batchId, stock, prices)
//   recYn = allConditionsMet(results) ? "Y" : "N"
//   evalConds = selectedConds.join(",")
//   evalResults = toJson(results)
```

**단위 테스트:**
- `RecSignalConditionServiceTest.java`
- 각 Evaluator별 테스트케이스

---

### **Phase 3: 프론트엔드 UI (1-2일)**

**batchAdmin.jsp 수정:**
```jsp
<!-- 신호 조건 선택 섹션 추가 -->
<div class="form-group">
  <label>신호 조건 선택</label>
  
  <!-- BASIC 카테고리 -->
  <fieldset>
    <legend>기본 조건</legend>
    <input type="checkbox" id="cond_golden_cross" class="condition-check" 
           data-cond-id="GOLDEN_CROSS">
    <label>MA 정배열</label>
    
    <input type="checkbox" id="cond_month_up" class="condition-check"
           data-cond-id="MONTH_UP">
    <label>월상승</label>
  </fieldset>
  
  <!-- 기타 카테고리 추가 -->
  ...
</div>
```

**신규 JS 모듈:**
```javascript
// webapp/appone/js/recSignalCondition.js
class RecSignalConditionManager {
  loadConditions()                        // DB에서 조건 목록 로드
  getSelectedConditions()                 // 선택된 조건 반환
  saveConditionConfig(batchId, condIds)  // 배치 조건 저장
  toggleCondition(condId)                // 체크박스 토글
}
```

**batchAdmin.js 수정:**
```javascript
// 배치 저장 시 조건 함께 저장
$("#btn_save_batch").click(function() {
  var batchId = $("#edit_job_id").val();
  var selectedConds = getSelectedConditions();
  saveConditionConfig(batchId, selectedConds);
  saveBatchConfig(...);
});
```

---

### **Phase 4: 차트/테이블 시각화 (1일)**

**kisFinance.jsp 수정:**
```jsp
<!-- 신호 조건 결과 테이블 -->
<table class="table signal-condition-table">
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
  </tbody>
</table>
```

**신규 JS 모듈:**
```javascript
// webapp/appone/js/recSignalConditionRenderer.js
class SignalConditionRenderer {
  renderConditionResults(signal) {
    // evalResults JSON 파싱
    // 조건별 ✓/✗ 아이콘 표시
  }
  
  renderChart(stock, prices, selectedConds) {
    // Highcharts에 조건별 표식 추가
    // - MA선 (5/20/60/120/240)
    // - 월상승/월하강 구간 배경색
    // - 신호 지점 플래그
  }
}
```

**Highcharts 차트 개선:**
```javascript
var chartOptions = {
  series: [
    { name: 'MA5', data: [...] },
    { name: 'MA20', data: [...] },
    // ...
  ],
  plotBands: [
    // 월상승 구간 (배경색 표시)
    { from: Date1, to: Date2, color: 'lightgreen', label: 'MonthUp' }
  ],
  flags: [
    // 신호 지점 (깃발 표시)
    { x: Date, title: 'SIGNAL: A', text: 'Golden+MonUp+Trend' }
  ]
};
```

---

### **Phase 5: 검증 & 최적화 (1일)**

**테스트:**
- 기존 신호 vs 신조건 신호 비교
  ```
  Before: recYn 기반으로 A등급 10개 종목 검출
  After: 동일한 조건 선택 시 결과 동일 확인
  ```
- 조건 조합별 신호 검증
  ```
  1. Golden + MonUp + Trend 선택 → 검증
  2. Golden + MonUp만 선택 → 다른 결과 확인
  3. 조건 선택 해제 → 신호 미생성
  ```
- 성능 테스트
  ```
  기존 배치 실행 시간 vs 신규 배치 실행 시간 비교
  (조건 평가 오버헤드 측정)
  ```

**문서화:**
- 신규 조건 추가 가이드 (개발자용)
- 배치 조건 선택 가이드 (운영자용)
- API 문서 업데이트

---

## 📊 영향도 분석

### **변경사항 요약**

| 항목 | 변경 | 영향 | 비고 |
|------|------|------|------|
| **DB** | 3개 테이블 추가/수정 | 낮음 | Migration 필요 (선택) |
| **Service** | RecSignal 로직 리팩토링 | 중간 | 기존 API 유지 |
| **Controller** | 조건 로드/저장 API 추가 | 낮음 | 신규 API |
| **JSP** | batchAdmin 수정 | 낮음 | UI만 변경 |
| **JS** | 신규 모듈 2개 추가 | 낮음 | 기존 코드 무영향 |
| **성능** | 조건 평가 오버헤드 | 중간 | 캐싱으로 완화 |

### **하위 호환성**
```
✓ 기존 recYn/recGrade 필드 유지
✓ 기존 API 호출 방식 변경 없음
✓ 기존 배치 설정 유효
  (defaultConditions로 기본값 설정)
```

---

## 🚀 추진 방안

### **1단계: 검토 & 승인 (즉시)**
- [ ] 이 문서 검토
- [ ] DB 스키마 확정
- [ ] 개발 순서 조정

### **2단계: Phase 1-2 (1주 ~ 10일)**
- [ ] DB 스키마 생성
- [ ] 백엔드 서비스 구현
- [ ] 단위 테스트

### **3단계: Phase 3-4 (1주)**
- [ ] 프론트엔드 UI 개발
- [ ] 차트 시각화 구현
- [ ] 통합 테스트

### **4단계: Phase 5 (2-3일)**
- [ ] 검증 & 성능 테스트
- [ ] 문서화
- [ ] 배포

---

## ❓ FAQ

### **Q1: 기존 신호는 어떻게 되나요?**
```
A: 신규 조건 구조는 별도 컬럼에 저장되므로,
   기존 recYn/recGrade는 유지됩니다.
   
   마이그레이션 예시:
   - 배치 실행 시 기본 조건으로 초기화
   - 기존 신호도 EVAL_CONDS/EVAL_RESULTS로 재계산 가능
   - 호환성 문제 없음
```

### **Q2: 성능이 저하되지 않을까요?**
```
A: 조건 평가는 추가 오버헤드가 생길 수 있습니다.
   
   완화 방안:
   - 조건 평가 결과 캐싱
   - 병렬 처리 (Future/ExecutorService)
   - 배치 최적화 (prefetch, bulk operation)
   
   예상 성능:
   - 기존: 종목당 평가 시간 ~10ms
   - 신규: 종목당 평가 시간 ~15-20ms (조건 3-5개 기준)
   - 차이: 1,000개 종목 배치 기준 10-20초 추가
```

### **Q3: 조건을 어떻게 추가하나요?**
```
A: 4단계 프로세스:

1. DB 등록
   INSERT INTO TB_REC_SIGNAL_CONDITION 
   VALUES ('CONDITION_ID', '조건명', '카테고리', ...);

2. Evaluator 구현
   public class NewConditionEvaluator 
       implements IConditionEvaluator { ... }

3. 빈 등록
   evaluatorMap.put('CONDITION_ID', new NewConditionEvaluator());

4. UI 추가
   <input type="checkbox" data-cond-id="CONDITION_ID">

자세한 가이드는 REC_SIGNAL_ANALYSIS.md의 "조건 추가 가이드"를 참고하세요.
```

### **Q4: 지금 바로 시작해야 하나요?**
```
A: 권장 추진 시기:

즉시 시작 추천 이유:
✓ 향후 조건 확장 예정 (MACD, 거래량, RSI 등)
✓ 신호 신뢰도 향상 필요
✓ 운영 유연성 증대 필요

만약 현재 바쁘다면:
→ Phase 1-2 (DB+BE)만 먼저 진행
→ Phase 3-4 (FE+Chart)는 이후 진행 가능
→ 단, Phase 2에서 기존 API 유지 필요
```

---

## 📚 참고 자료

| 문서 | 용도 | 대상 |
|------|------|------|
| `REC_SIGNAL_ANALYSIS.md` | 상세 설계 | 개발자 |
| `REC_SIGNAL_ARCHITECTURE.md` | 아키텍처 다이어그램 | 개발자/PO |
| `REC_SIGNAL_SUMMARY.md` | 실행 요약 | 모두 |
| `조건 추가 가이드` (TBD) | 신규 조건 추가 방법 | 개발자 |

---

## 🎓 핵심 메시지

### **왜 이 개선이 필요한가?**
```
현재:  조건 추가 = 코드 수정 → 재배포 (어려움)
개선:  조건 추가 = DB 등록 + 평가 클래스 (쉬움)

현재:  신호 추적 = recYn/Grade만 기록 (불명확)
개선:  신호 추적 = 조건별 성공/실패 기록 (명확)

현재:  다중 신호 조합 = 불가능
개선:  다중 신호 조합 = 자유로운 선택
```

### **누가 이점을 받는가?**

**개발자:**
- 신규 조건 추가 시 기존 로직 변경 불필요
- 테스트가 쉬워짐 (모듈화)
- 확장성 ⬆️

**운영자:**
- UI에서 조건 선택 가능 (배포 불필요)
- 신호 추적 명확함
- 유연한 신호 조합 테스트 가능

**사용자(거래자):**
- 신뢰도 높은 신호 (다양한 조건 조합)
- 실패 원인 파악 용이
- 맞춤형 신호 선택 가능

---

**최종 권장: 즉시 Phase 1 시작을 제안합니다.**

