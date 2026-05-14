
function downloadAll() {
  var btn = document.getElementById('btn-download');
  if (btn) { btn.textContent = '⏳ 저장 중...'; btn.disabled = true; }
  try {
    var html = '<!DOCTYPE html>\n' + document.documentElement.outerHTML;
    var blob = new Blob([html], { type: 'text/html;charset=utf-8' });
    var a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'kjc_42pattern_dashboard.html';
    document.body.appendChild(a); a.click(); document.body.removeChild(a);
    setTimeout(function(){ URL.revokeObjectURL(a.href); }, 3000);
    if (btn) {
      btn.textContent = '✅ 저장 완료'; btn.disabled = false;
      setTimeout(function(){ btn.textContent='📦 HTML 다운로드'; }, 3000);
    }
  } catch(e) {
    if (btn) { btn.textContent = '❌ 실패'; btn.disabled = false; }
    console.error(e);
  }
}

/* ─── patterns/patternUtils.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  patternUtils.js  —  KJC 패턴 공통 유틸리티
 *  모든 패턴 파일에서 import하여 사용
 * ═══════════════════════════════════════════════════════════════════
 */

// ── 타입 정의 (JSDoc) ──────────────────────────────────────────────
/**
 * @typedef {Object} StockBar
 * @property {number} idx        - 인덱스
 * @property {string} date       - 날짜 문자열
 * @property {number} open       - 시가
 * @property {number} high       - 고가
 * @property {number} low        - 저가
 * @property {number} close      - 종가
 * @property {number} volume     - 거래량
 * @property {number} change     - 전일 대비 변화량
 * @property {number} changePct  - 전일 대비 변화율(%)
 * @property {number|null} ma5   - 5일 이동평균
 * @property {number|null} ma20  - 20일 이동평균
 * @property {number|null} ma60  - 60일 이동평균
 * @property {number|null} ma120 - 120일 이동평균
 * @property {number|null} ma240 - 240일 이동평균
 * @property {number|null} bbU   - 볼린저밴드 상단
 * @property {number|null} bbM   - 볼린저밴드 중심
 * @property {number|null} bbL   - 볼린저밴드 하단
 */

/**
 * @typedef {Object} PatternSignal
 * @property {number}  idx        - 감지 인덱스
 * @property {number}  [idx2]     - 연결 인덱스 (이중바닥 등)
 * @property {number}  confidence - 신뢰도 (0.0 ~ 1.0)
 * @property {'BUY'|'SELL'|'WATCH'} type - 신호 종류
 * @property {string}  label      - 신호 레이블
 * @property {Object}  meta       - 추가 메타데이터
 */

// ── 공통 계산 함수 ─────────────────────────────────────────────────

/**
 * 특정 인덱스 이전 N봉의 평균 거래량 반환
 * @param {StockBar[]} data
 * @param {number} i     - 현재 인덱스
 * @param {number} [w=5] - 윈도우 크기
 * @returns {number}
 */
function avgVolume(data, i, w = 5) {
  const start = Math.max(0, i - w);
  const slice = data.slice(start, i);
  if (!slice.length) return data[i]?.volume || 1;
  return slice.reduce((s, d) => s + d.volume, 0) / slice.length;
}

/**
 * 현재 거래량 / 평균 거래량 비율
 * @param {StockBar[]} data
 * @param {number} i
 * @param {number} [w=5]
 * @returns {number}
 */
function volumeRatio(data, i, w = 5) {
  const avg = avgVolume(data, i, w);
  return avg > 0 ? data[i].volume / avg : 1;
}

/**
 * 골든크로스 여부 (MA20이 MA60을 하→상 돌파)
 * @param {StockBar[]} data
 * @param {number} i
 * @returns {boolean}
 */
function isGoldenCross(data, i) {
  if (i < 1) return false;
  const c = data[i], p = data[i - 1];
  return !!(c.ma20 && p.ma20 && c.ma60 && p.ma60
    && c.ma20 > c.ma60 && p.ma20 <= p.ma60);
}

/**
 * 데드크로스 여부 (MA20이 MA60을 상→하 이탈)
 * @param {StockBar[]} data
 * @param {number} i
 * @returns {boolean}
 */
function isDeadCross(data, i) {
  if (i < 1) return false;
  const c = data[i], p = data[i - 1];
  return !!(c.ma20 && p.ma20 && c.ma60 && p.ma60
    && c.ma20 < c.ma60 && p.ma20 >= p.ma60);
}

/**
 * 정배열 여부 (종가 > MA5 > MA20 > MA60)
 * @param {StockBar} d
 * @returns {boolean}
 */
function isBullAlign(d) {
  return !!(d.ma5 && d.ma20 && d.ma60
    && d.close > d.ma5 && d.ma5 > d.ma20 && d.ma20 > d.ma60);
}

/**
 * 역배열 여부 (종가 < MA5 < MA20 < MA60)
 * @param {StockBar} d
 * @returns {boolean}
 */
function isBearAlign(d) {
  return !!(d.ma5 && d.ma20 && d.ma60
    && d.close < d.ma5 && d.ma5 < d.ma20 && d.ma20 < d.ma60);
}

/**
 * 구간 내 고가
 * @param {StockBar[]} data
 * @param {number} from
 * @param {number} to
 * @returns {number}
 */
function periodHigh(data, from, to) {
  return Math.max(...data.slice(from, to).map(d => d.close));
}

/**
 * 구간 내 저가
 * @param {StockBar[]} data
 * @param {number} from
 * @param {number} to
 * @returns {number}
 */
function periodLow(data, from, to) {
  return Math.min(...data.slice(from, to).map(d => d.close));
}

/**
 * 극소점(트로프) 인덱스 배열 반환
 * @param {StockBar[]} data
 * @param {number} [wing=3] - 좌우 확인 봉 수
 * @returns {number[]}
 */
function findTroughs(data, wing = 3) {
  const out = [];
  for (let i = wing; i < data.length - wing; i++) {
    const cur = data[i].close;
    const leftOk  = data.slice(i - wing, i).every(d => d.close >= cur);
    const rightOk = data.slice(i + 1, i + wing + 1).every(d => d.close >= cur);
    if (leftOk && rightOk) out.push(i);
  }
  return out;
}

/**
 * 극대점(피크) 인덱스 배열 반환
 * @param {StockBar[]} data
 * @param {number} [wing=3]
 * @returns {number[]}
 */
function findPeaks(data, wing = 3) {
  const out = [];
  for (let i = wing; i < data.length - wing; i++) {
    const cur = data[i].close;
    const leftOk  = data.slice(i - wing, i).every(d => d.close <= cur);
    const rightOk = data.slice(i + 1, i + wing + 1).every(d => d.close <= cur);
    if (leftOk && rightOk) out.push(i);
  }
  return out;
}

/**
 * 아래꼬리 비율 (하락 압력 대비 반등력)
 * @param {StockBar} d
 * @returns {number} 0~1
 */
function lowerWickRatio(d) {
  const range = d.high - d.low;
  if (range <= 0) return 0;
  return (Math.min(d.open, d.close) - d.low) / range;
}

/**
 * 윗꼬리 비율
 * @param {StockBar} d
 * @returns {number} 0~1
 */
function upperWickRatio(d) {
  const range = d.high - d.low;
  if (range <= 0) return 0;
  return (d.high - Math.max(d.open, d.close)) / range;
}

/**
 * 도지 캔들 여부
 * @param {StockBar} d
 * @param {number} [threshold=0.15] - 몸통/전체 비율 임계값
 * @returns {boolean}
 */
function isDoji(d, threshold = 0.15) {
  const range = d.high - d.low;
  if (range <= 0) return false;
  return Math.abs(d.open - d.close) / range < threshold;
}

/**
 * MA 기울기 (n봉 전 대비)
 * @param {StockBar[]} data
 * @param {number} i
 * @param {number} maPeriod - 5|20|60|120|240
 * @param {number} [lookback=3]
 * @returns {number} 양수=상승, 음수=하락
 */
function maSlope(data, i, maPeriod, lookback = 3) {
  const key = `ma${maPeriod}`;
  const cur  = data[i]?.[key];
  const prev = data[Math.max(0, i - lookback)]?.[key];
  if (!cur || !prev) return 0;
  return cur - prev;
}

/**
 * 신뢰도 클램프 (0~1 범위)
 * @param {number} v
 * @returns {number}
 */
function clamp01(v) {
  return Math.min(1, Math.max(0, v));
}

/**
 * 중복 신호 제거 (최소 간격 봉 수)
 * @param {PatternSignal[]} signals
 * @param {number} [minGap=5]
 * @returns {PatternSignal[]}
 */
function deduplicateSignals(signals, minGap = 5) {
  const sorted = [...signals].sort((a, b) => a.idx - b.idx);
  const out = [];
  for (const sig of sorted) {
    if (!out.length || sig.idx - out[out.length - 1].idx >= minGap) {
      out.push(sig);
    }
  }
  return out;
}

/**
 * 볼린저밴드 폭 비율
 * @param {StockBar} d
 * @returns {number}
 */
function bbWidth(d) {
  if (!d.bbU || !d.bbL || !d.close) return 0.05;
  return (d.bbU - d.bbL) / d.close;
}


/* ─── patterns/moduleA/A01_jin_gold_pullback.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 패턴 01]  진골드 눌림목 패턴  (jin_gold_pullback)
 * ═══════════════════════════════════════════════════════════════════
 *  정의  : 정배열(MA20>MA60) 상태에서 전고점 대비 눌림 후 MA20에서 지지 반등
 *  이동평균: MA20 (빨간), MA60 (녹색)
 *  신호  : BUY
 *  그룹  : G1 - 눌림목/돌파 계열
 * ═══════════════════════════════════════════════════════════════════
 */

// ── 옵션 (조절 가능한 파라미터) ────────────────────────────────────
const OPTIONS_A01 = {
  /** 전고점 탐색 구간 (봉 수) */
  lookbackBars: 10,

  /** 눌림 최소 하락률 (예: 0.03 = 3%) */
  dropMin: 0.03,

  /** 눌림 최대 하락률 (예: 0.12 = 12%) */
  dropMax: 0.12,

  /** MA20 허용 오차 (±N%) - 이 범위 내에 있어야 지지로 판단 */
  ma20Tolerance: 0.015,

  /** 거래량 배율 (평균 대비 몇 배 이상이어야 유효) */
  volumeMultiplier: 1.3,

  /** 거래량 평균 산출 구간 (봉 수) */
  volumeWindow: 5,

  /** 신뢰도 기본값 */
  baseConfidence: 0.70,

  /** 중복 신호 최소 간격 (봉 수) */
  dedupeGap: 5,

  /** 감지 시작 최소 인덱스 (MA 수렴 최소 구간) */
  minIdx: 20,
};

// ── 감지 함수 ───────────────────────────────────────────────────────
/**
 * 진골드 눌림목 패턴 감지
 * @param {import('../patternUtils.js').StockBar[]} data
 * @param {Partial<typeof OPTIONS_A01>} [opts]
 * @returns {import('../patternUtils.js').PatternSignal[]}
 */
function detectA01(data, opts = {}) {
  const o = { ...OPTIONS_A01, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length - 3; i++) {
    const d = data[i];

    // 조건 1: MA20, MA60 존재 및 정배열
    if (!d.ma20 || !d.ma60 || d.ma20 <= d.ma60) continue;

    // 조건 2: 전고점 대비 눌림 폭 확인
    const prevHigh = Math.max(
      ...data.slice(i - o.lookbackBars, i).map(x => x.close)
    );
    const drop = (prevHigh - d.close) / prevHigh;
    if (drop < o.dropMin || drop > o.dropMax) continue;

    // 조건 3: 현재가가 MA20 근처에서 지지 확인
    const ma20Dist = Math.abs(d.close - d.ma20) / d.ma20;
    if (ma20Dist > o.ma20Tolerance) continue;

    // 조건 4: 거래량 확인
    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volumeMultiplier) continue;

    // 신뢰도 산출 (눌림폭 + 거래량 비율로 가중)
    const confidence = clamp01(
      o.baseConfidence + drop * 0.5 + (vr - 1) * 0.05
    );

    signals.push({
      idx: i,
      confidence,
      type: 'BUY',
      label: '진골드눌림목',
      meta: {
        drop: +(drop * 100).toFixed(2),
        prevHigh,
        ma20: d.ma20,
        ma60: d.ma60,
        volumeRatio: +vr.toFixed(2),
      },
    });
  }

  return deduplicateSignals(signals, o.dedupeGap);
}

// ── 패턴 메타데이터 ────────────────────────────────────────────────
const META_A01 = {
  id:      'jin_gold_pullback',
  no:      1,
  name:    '진골드 눌림목',
  module:  'A',
  group:   'G1-눌림목',
  color:   '#FFD700',
  icon:    '🥇',
  signal:  'BUY',
  ma:      [20, 60],
  desc:    '정배열(MA20>MA60) 상태에서 전고점 대비 3~12% 눌린 후 MA20 지지 반등. 거래량 130% 이상 필수.',
  buy:     'MA20 ±1.5% 이내 + 정배열 + 거래량↑',
  sell:    'MA20 하향 이탈 or -5% 손절',
};


/* ─── patterns/moduleA/A02_A05_group1.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 패턴 02]  분봉 상방추 패턴  (minute_upward)
 * ═══════════════════════════════════════════════════════════════════
 *  정의  : MA5 연속 우상향 + 고점·저점 동시 상향 → 단기 추세 방향성 예고
 *  이동평균: MA5 (검정), MA20 (빨간)
 *  신호  : BUY
 */
const OPTIONS_A02 = {
  /** MA5 연속 상승 확인 구간 (봉 수) */
  ma5RisingBars: 5,
  /** 저점 상향 확인 구간 (봉 수 전) */
  higherLowBars: 3,
  /** 거래량 배율 */
  volumeMultiplier: 1.1,
  volumeWindow: 5,
  baseConfidence: 0.68,
  dedupeGap: 5,
  minIdx: 5,
};

function detectA02(data, opts = {}) {
  const o = { ...OPTIONS_A02, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length - 2; i++) {
    const d = data[i];
    if (!d.ma5) continue;

    // MA5 연속 우상향
    const ma5Rising = data
      .slice(i - o.ma5RisingBars + 1, i + 1)
      .every((x, j, a) => j === 0 || (x.ma5 != null && a[j-1].ma5 != null && x.ma5 >= a[j-1].ma5));
    if (!ma5Rising) continue;

    // 저점 상향
    if (d.low <= data[i - o.higherLowBars].low) continue;

    // 현재가 MA5 위
    if (d.close <= d.ma5) continue;

    const vr = data[i].volume / (data.slice(Math.max(0,i-o.volumeWindow),i).reduce((s,x)=>s+x.volume,0)/o.volumeWindow||1);

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - 1) * 0.03),
      type: 'BUY',
      label: '분봉상방추',
      meta: { ma5: d.ma5, volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A02 = {
  id: 'minute_upward', no: 2, name: '분봉 상방추', module: 'A', group: 'G1-눌림목',
  color: '#FF9800', icon: '📐', signal: 'BUY', ma: [5, 20],
  desc: '일봉 횡보 중 MA5가 연속 우상향하며 방향성 예고. 고점·저점 동시 상향.',
  buy: '분봉 MA5 기울기>0 + 3봉 연속 고점 상향', sell: '분봉 MA5 하향 전환',
};


/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 패턴 03]  급등주 4대 종결가 패턴  (surge_4_close)
 * ═══════════════════════════════════════════════════════════════════
 *  정의  : 급등 이후 ①윗꼬리 장대음봉 ②도지 ③거래량 급감 ④갭상승 반납 중 하나 → 고점 신호
 *  이동평균: MA20 (빨간), MA60 (녹색)
 *  신호  : SELL (청산/매도)
 */
const OPTIONS_A03 = {
  /** 급등 판단 기준 — 최근 N봉 중 수익률 */
  surgeLookback: 5,
  /** 급등 최소 수익률 (예: 0.15 = 15%) */
  surgeMinReturn: 0.15,
  /** 윗꼬리 비율 임계값 */
  upperWickThreshold: 0.55,
  /** 도지 몸통 비율 임계값 */
  dojiThreshold: 0.15,
  /** 거래량 급감 비율 (평균 대비) */
  volDropRatio: 0.65,
  volumeWindow: 5,
  /** 당일 거래량이 N배 이상이어야 신뢰 */
  confirmVolMultiplier: 1.3,
  baseConfidence: 0.75,
  dedupeGap: 5,
  minIdx: 10,
};

function detectA03(data, opts = {}) {
  const o = { ...OPTIONS_A03, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    // 선행 급등 확인
    const prevMax = Math.max(...data.slice(i - o.surgeLookback, i).map(x => x.close));
    const isSurge = prevMax > data[i - o.surgeLookback].close * (1 + o.surgeMinReturn);
    if (!isSurge) continue;

    const avgVol = data.slice(Math.max(0,i-o.volumeWindow),i).reduce((s,x)=>s+x.volume,0)/o.volumeWindow||1;
    const vr = d.volume / avgVol;

    // 4가지 종결 조건 중 하나
    const cond1 = upperWickRatio(d) > o.upperWickThreshold && d.close < d.open;
    const cond2 = isDoji(d, o.dojiThreshold);
    const cond3 = d.volume < avgVol * o.volDropRatio;
    const cond4 = d.open > data[i-1].close * 1.01 && d.close < d.open * 0.98;

    if (!(cond1 || cond2 || cond3 || cond4)) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr > 1 ? 0.05 : 0)),
      type: 'SELL',
      label: '종결가신호',
      meta: { cond1, cond2, cond3, cond4, upperWick: +upperWickRatio(d).toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A03 = {
  id: 'surge_4_close', no: 3, name: '급등주 4대 종결가', module: 'A', group: 'G1-눌림목',
  color: '#FF5722', icon: '🔔', signal: 'SELL', ma: [20, 60],
  desc: '급등 이후 ①윗꼬리 장대음봉 ②도지 ③거래량 급감 ④갭상승 반납 중 하나 출현 시 고점 신호.',
  buy: '없음 (매도/청산 패턴)', sell: '4가지 종결 형태 중 1개 확인 즉시 청산',
};


/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 패턴 04]  빅 골드 매집형  (big_gold_acc)
 * ═══════════════════════════════════════════════════════════════════
 *  정의  : 박스권 내 거래량 비정상 급증 반복 → 세력 매집 구간
 *  이동평균: MA60 (녹색), MA120 (파란)
 *  신호  : BUY
 */
const OPTIONS_A04 = {
  /** 박스권 판단 구간 (봉 수) */
  boxLookback: 20,
  /** 박스권 최대 등락폭 (예: 0.06 = 6%) */
  boxRangeMax: 0.06,
  /** 거래량 폭증 배율 (평균 대비) */
  volSpikeMultiplier: 2.0,
  volumeWindow: 10,
  /** 폭증 일수 최소 횟수 */
  minSpikeDays: 3,
  baseConfidence: 0.72,
  dedupeGap: 5,
  minIdx: 20,
};

function detectA04(data, opts = {}) {
  const o = { ...OPTIONS_A04, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const sl = data.slice(i - o.boxLookback, i);
    const hi = Math.max(...sl.map(d => d.close));
    const lo = Math.min(...sl.map(d => d.close));
    const rng = (hi - lo) / lo;
    if (rng > o.boxRangeMax) continue;

    const avgVol = sl.reduce((s,d)=>s+d.volume,0) / sl.length;
    const spikeDays = sl.filter(d => d.volume > avgVol * o.volSpikeMultiplier).length;
    if (spikeDays < o.minSpikeDays) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + spikeDays * 0.02),
      type: 'BUY',
      label: '빅골드매집',
      meta: { boxRange: +(rng*100).toFixed(2), spikeDays, avgVolume: Math.round(avgVol) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A04 = {
  id: 'big_gold_acc', no: 4, name: '빅 골드 매집형', module: 'A', group: 'G1-눌림목',
  color: '#FFC107', icon: '💰', signal: 'BUY', ma: [60, 120],
  desc: '박스권 내 거래량 비정상 급증 반복. 세력 매집 구간으로 돌파 직전 포착.',
  buy: '박스권 ±3% + 거래량 2배 이상 일수 ≥3일', sell: '박스권 하단 이탈',
};


/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 패턴 05]  쌍매들 돌파형 (W패턴)  (twin_bottom_break)
 * ═══════════════════════════════════════════════════════════════════
 *  정의  : 이중바닥 W자 형성 후 넥라인 상향 돌파 + 거래량 폭증
 *  이동평균: MA20 (빨간), MA60 (녹색)
 *  신호  : BUY
 */
const OPTIONS_A05 = {
  /** 두 저점 간 최소 간격 (봉) */
  troughGapMin: 10,
  /** 두 저점 간 최대 간격 (봉) */
  troughGapMax: 40,
  /** 두 저점 가격 차이 허용 (비율) */
  troughPriceTolerance: 0.03,
  /** 넥라인 돌파 판단: 넥라인 대비 종가 초과 비율 */
  neckBreakThreshold: 0.005,
  /** 거래량 배율 */
  volMultiplier: 2.0,
  volumeWindow: 5,
  /** 트로프 탐색 wing 크기 */
  troughWing: 3,
  baseConfidence: 0.85,
  dedupeGap: 5,
};

function detectA05(data, opts = {}) {
  const o = { ...OPTIONS_A05, ...opts };
  const troughs = findTroughs(data, o.troughWing);
  const signals = [];

  for (let t = 1; t < troughs.length; t++) {
    const t1 = troughs[t - 1], t2 = troughs[t];
    const gap = t2 - t1;
    if (gap < o.troughGapMin || gap > o.troughGapMax) continue;

    const p1 = data[t1].close, p2 = data[t2].close;
    if (Math.abs(p1 - p2) / p1 > o.troughPriceTolerance) continue;

    // 넥라인: 두 저점 사이 최고가
    const neckline = Math.max(...data.slice(t1, t2 + 1).map(d => d.close));

    // 돌파 확인 (t2 이후 봉)
    const breakIdx = t2 + 1;
    if (breakIdx >= data.length) continue;
    const bd = data[breakIdx];
    if (bd.close <= neckline * (1 + o.neckBreakThreshold)) continue;

    const avgVol = data.slice(Math.max(0,breakIdx-o.volumeWindow),breakIdx).reduce((s,d)=>s+d.volume,0)/o.volumeWindow||1;
    const vr = bd.volume / avgVol;
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: breakIdx,
      idx2: t1,
      confidence: clamp01(o.baseConfidence + vr * 0.02),
      type: 'BUY',
      label: '쌍바닥돌파',
      meta: { t1, t2, neckline, volumeRatio: +vr.toFixed(2), gap },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A05 = {
  id: 'twin_bottom_break', no: 5, name: '쌍매들 돌파형', module: 'A', group: 'G1-눌림목',
  color: '#4CAF50', icon: '🅦', signal: 'BUY', ma: [20, 60],
  desc: '이중바닥 W자 형성 후 넥라인 상향 돌파. 두 저점 간격 10~40봉, 거래량 폭증 필수.',
  buy: '넥라인 돌파 + 거래량 200% 이상', sell: '넥라인 하향 복귀',
};


/* ─── patterns/moduleA/A06_A10_group2.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 그룹2]  골드 변형 계열  패턴 06~10
 * ═══════════════════════════════════════════════════════════════════
 */

// ─────────────────────────────────────────────
//  패턴 06 : 진골드 심층 눌림목  (jin_gold_deep)
// ─────────────────────────────────────────────
const OPTIONS_A06 = {
  /** MA60 허용 오차 (±N%) */
  ma60Tolerance: 0.02,
  /** 정배열 필수 여부 (MA20 > MA60) */
  requireBullAlign: true,
  /** 거래량 배율 */
  volMultiplier: 1.5,
  volumeWindow: 5,
  baseConfidence: 0.78,
  dedupeGap: 5,
  minIdx: 20,
};

function detectA06(data, opts = {}) {
  const o = { ...OPTIONS_A06, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length - 3; i++) {
    const d = data[i];
    if (!d.ma60 || !d.ma20) continue;
    if (o.requireBullAlign && d.ma20 <= d.ma60) continue;

    const dist = Math.abs(d.close - d.ma60) / d.ma60;
    if (dist > o.ma60Tolerance) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (o.ma60Tolerance - dist) * 5),
      type: 'BUY',
      label: '심층눌림목',
      meta: { ma60: d.ma60, ma60Dist: +(dist*100).toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A06 = {
  id:'jin_gold_deep', no:6, name:'진골드 심층 눌림목', module:'A', group:'G2-변형',
  color:'#E6B800', icon:'🥈', signal:'BUY', ma:[20,60],
  desc:'MA60까지 깊게 눌린 변형 눌림목. MA60 지지 확인 후 매수.',
  buy:'MA60 ±2% + 거래량 150% 이상', sell:'MA60 종가 이탈',
};


// ─────────────────────────────────────────────
//  패턴 07 : 분봉 에너지 집중형  (minute_energy)
// ─────────────────────────────────────────────
const OPTIONS_A07 = {
  /** BB 수축 판단: 현재 BB폭 / 과거 평균 BB폭 비율 임계값 */
  bbContractionRatio: 0.50,
  /** BB폭 비교 구간 (봉 수) */
  bbLookback: 10,
  /** 돌파 거래량 배율 */
  volMultiplier: 2.5,
  volumeWindow: 5,
  /** 상방 돌파: 종가 > BB상단 */
  requireUpperBreak: true,
  baseConfidence: 0.83,
  dedupeGap: 5,
  minIdx: 25,
};

function detectA07(data, opts = {}) {
  const o = { ...OPTIONS_A07, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.bbU || !d.bbL) continue;

    const curW = bbWidth(d);
    const prevWidths = data.slice(i - o.bbLookback, i)
      .map(x => bbWidth(x)).filter(w => w > 0);
    const avgW = prevWidths.length
      ? prevWidths.reduce((s,v)=>s+v,0) / prevWidths.length
      : curW;

    if (curW > avgW * o.bbContractionRatio) continue;
    if (o.requireUpperBreak && d.close <= d.bbU) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - o.volMultiplier) * 0.02),
      type: 'BUY',
      label: '에너지집중',
      meta: { bbWidth: +(curW*100).toFixed(2), avgBBWidth: +(avgW*100).toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A07 = {
  id:'minute_energy', no:7, name:'분봉 에너지 집중형', module:'A', group:'G2-변형',
  color:'#FF7043', icon:'⚡', signal:'BUY', ma:[5,20],
  desc:'볼린저밴드 극도 수축 후 거래량 폭발 동반 상방 돌파. 수축 기간이 길수록 폭발력 강.',
  buy:'BB폭 3% 이하 수축 + 돌파 거래량 3배', sell:'돌파 당일 종가 BB 중심선 하회',
};


// ─────────────────────────────────────────────
//  패턴 08 : 분봉 종결가 패턴  (surge_4_minute)
// ─────────────────────────────────────────────
const OPTIONS_A08 = {
  /** 최근 급등 확인 구간 */
  surgeLookback: 5,
  /** 급등 최소 수익률 */
  surgeMinReturn: 0.08,
  /** 연속 거래량 감소 확인 봉 수 */
  volDropConsecutive: 2,
  /** 거래량 감소 비율 (직전봉 대비) */
  volDropRatio: 0.5,
  baseConfidence: 0.70,
  dedupeGap: 5,
  minIdx: 5,
};

function detectA08(data, opts = {}) {
  const o = { ...OPTIONS_A08, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    const recentGain = (d.close - data[i - o.surgeLookback].close) / data[i - o.surgeLookback].close;
    if (recentGain < o.surgeMinReturn) continue;

    let volDropCount = 0;
    for (let j = 0; j < o.volDropConsecutive; j++) {
      if (i - j < 1) break;
      if (data[i - j].volume < data[i - j - 1].volume * o.volDropRatio) volDropCount++;
    }
    if (volDropCount < o.volDropConsecutive) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence),
      type: 'SELL',
      label: '분봉종결가',
      meta: { recentGain: +(recentGain*100).toFixed(2), volDropCount },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A08 = {
  id:'surge_4_minute', no:8, name:'분봉 종결가 패턴', module:'A', group:'G2-변형',
  color:'#EF5350', icon:'⏱', signal:'SELL', ma:[5,20],
  desc:'급등주 4대 종결 패턴의 분봉 적용 버전. 스캘핑/단타 고점 청산 타이밍.',
  buy:'없음 (단타 청산용)', sell:'분봉 윗꼬리 + 거래량 급감 2봉 연속',
};


// ─────────────────────────────────────────────
//  패턴 09 : 장기 매집 변형  (big_gold_long)
// ─────────────────────────────────────────────
const OPTIONS_A09 = {
  /** MA120 ~ MA240 사이 확인 (종가가 두 선 사이) */
  requireBetweenMA: true,
  /** 거래량 폭증 배율 */
  volMultiplier: 2.0,
  volumeWindow: 20,
  /** 주간 거래량 비교 구간 (봉) */
  weeklyLookback: 5,
  baseConfidence: 0.72,
  dedupeGap: 10,
  minIdx: 120,
};

function detectA09(data, opts = {}) {
  const o = { ...OPTIONS_A09, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.ma120 || !d.ma240) continue;

    if (o.requireBetweenMA) {
      const inRange = d.close >= Math.min(d.ma120, d.ma240)
                   && d.close <= Math.max(d.ma120, d.ma240);
      if (!inRange) continue;
    }

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - o.volMultiplier) * 0.03),
      type: 'BUY',
      label: '장기매집변형',
      meta: { ma120: d.ma120, ma240: d.ma240, volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A09 = {
  id:'big_gold_long', no:9, name:'장기 매집 변형', module:'A', group:'G2-변형',
  color:'#F9A825', icon:'🏦', signal:'BUY', ma:[120,240],
  desc:'MA120~MA240 사이 초장기 매집. 수개월 간 거래량 간헐적 급증 반복.',
  buy:'120~240MA 사이 + 주간 거래량 평균 2배', sell:'120MA 이탈 후 복귀 실패',
};


// ─────────────────────────────────────────────
//  패턴 10 : 역헤드앤숄더 변형  (twin_bottom_hns)
// ─────────────────────────────────────────────
const OPTIONS_A10 = {
  /** 좌어깨-머리 최소 간격 */
  shoulderGapMin: 8,
  /** 전체 패턴 최대 구간 */
  patternMaxSpan: 80,
  /** 좌우 어깨 가격 균형 (비율) */
  shoulderBalance: 0.04,
  /** 넥라인 돌파 확인 */
  neckBreakRequired: true,
  baseConfidence: 0.84,
  dedupeGap: 5,
  troughWing: 3,
};

function detectA10(data, opts = {}) {
  const o = { ...OPTIONS_A10, ...opts };
  const troughs = findTroughs(data, o.troughWing);
  const signals = [];

  for (let t = 2; t < troughs.length; t++) {
    const l = troughs[t-2], m = troughs[t-1], r = troughs[t];
    if (m - l < o.shoulderGapMin || r - m < o.shoulderGapMin) continue;
    if (r - l > o.patternMaxSpan) continue;

    const pl = data[l].close, pm = data[m].close, pr = data[r].close;
    if (pm >= pl || pm >= pr) continue; // 머리가 가장 낮아야 함

    const balance = Math.abs(pl - pr) / pl;
    if (balance > o.shoulderBalance) continue;

    if (o.neckBreakRequired) {
      const neckline = Math.max(...data.slice(l, r+1).map(d => d.close));
      const bi = r + 1;
      if (bi >= data.length || data[bi].close <= neckline) continue;
    }

    signals.push({
      idx: r + 1,
      confidence: clamp01(o.baseConfidence - balance * 3),
      type: 'BUY',
      label: '역HnS',
      meta: { leftShoulder: l, head: m, rightShoulder: r, balance: +(balance*100).toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A10 = {
  id:'twin_bottom_hns', no:10, name:'역헤드앤숄더 변형', module:'A', group:'G2-변형',
  color:'#43A047', icon:'👑', signal:'BUY', ma:[20,60],
  desc:'쌍바닥 + 역헤드앤숄더 복합. 가운데 저점이 가장 낮고 양쪽 저점이 균형.',
  buy:'오른쪽 어깨 확인 + 넥라인 돌파', sell:'오른쪽 어깨 저점 하향 이탈',
};


/* ─── patterns/moduleA/A11_A15_group3.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 그룹3]  N자형 / 파동 계열  패턴 11~15
 * ═══════════════════════════════════════════════════════════════════
 */

// ─────────────────────────────────────────────
//  패턴 11 : 이중 N자형 (더블N)  (double_n)
// ─────────────────────────────────────────────
const OPTIONS_A11 = {
  /** N자 탐색 구간 */
  lookback: 20,
  /** 극값 탐색 wing */
  extremaWing: 2,
  /** 2번째 저점이 1번째보다 높아야 함 */
  requireHigherLow: true,
  /** 거래량 배율 */
  volMultiplier: 1.2,
  volumeWindow: 5,
  baseConfidence: 0.75,
  dedupeGap: 8,
  minIdx: 20,
};

function detectA11(data, opts = {}) {
  const o = { ...OPTIONS_A11, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length - 5; i++) {
    const sl = data.slice(i - o.lookback, i + 1);

    // 극점 찾기
    const peaks = [], valleys = [];
    for (let j = o.extremaWing; j < sl.length - o.extremaWing; j++) {
      const isPeak = sl.slice(j - o.extremaWing, j).every(x => x.close <= sl[j].close)
                  && sl.slice(j + 1, j + o.extremaWing + 1).every(x => x.close <= sl[j].close);
      const isValley = sl.slice(j - o.extremaWing, j).every(x => x.close >= sl[j].close)
                     && sl.slice(j + 1, j + o.extremaWing + 1).every(x => x.close >= sl[j].close);
      if (isPeak) peaks.push(j);
      if (isValley) valleys.push(j);
    }

    if (peaks.length < 2 || valleys.length < 2) continue;

    const v1 = valleys[valleys.length - 2], v2 = valleys[valleys.length - 1];
    if (o.requireHigherLow && sl[v2].close <= sl[v1].close) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - 1) * 0.03),
      type: 'BUY',
      label: '이중N자',
      meta: { valley1Price: sl[v1].close, valley2Price: sl[v2].close, peakCount: peaks.length },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A11 = {
  id:'double_n', no:11, name:'이중 N자형 (더블N)', module:'A', group:'G3-파동',
  color:'#29B6F6', icon:'🌊', signal:'BUY', ma:[20,60],
  desc:'N자 파동 2회 연속 + 저점 상향. 두 번째 N자 초입이 최적 매수.',
  buy:'2번째 N자 상승 초입 + 이전 저점보다 높음', sell:'2번째 N자 저점 하향 이탈',
};


// ─────────────────────────────────────────────
//  패턴 12 : 반칙형 패턴  (foul_pattern)
// ─────────────────────────────────────────────
const OPTIONS_A12 = {
  /** 지지선 기준 MA (5|20|60) */
  supportMA: 20,
  /** 이전 N봉이 지지선 위에 있어야 함 */
  prevAboveBars: 5,
  /** 이탈 임계값 (지지선 대비 N% 이하) */
  breakThreshold: 0.015,
  /** 복귀 확인: 이탈 후 지지선 위로 복귀 */
  recoveryRequired: true,
  /** 이탈봉 거래량 제한 (평균 대비) — 소거래량 이탈이어야 속임수 */
  fakeBreakVolMax: 1.2,
  /** 복귀봉 거래량 최소 배율 */
  recoveryVolMin: 1.4,
  volumeWindow: 5,
  baseConfidence: 0.78,
  dedupeGap: 5,
  minIdx: 5,
};

function detectA12(data, opts = {}) {
  const o = { ...OPTIONS_A12, ...opts };
  const key = `ma${o.supportMA}`;
  const signals = [];

  for (let i = o.prevAboveBars + 1; i < data.length - 2; i++) {
    const d = data[i];
    const support = d[key];
    if (!support) continue;

    // 이전 N봉이 지지선 위에 있었는지
    const prevAbove = data.slice(i - o.prevAboveBars, i)
      .every(x => x[key] ? x.close > x[key] * (1 - o.breakThreshold) : true);
    if (!prevAbove) continue;

    // 직전봉이 이탈
    const prevBar = data[i - 1];
    const prevSupport = prevBar[key];
    if (!prevSupport || prevBar.close >= prevSupport * (1 - o.breakThreshold)) continue;

    // 이탈봉 거래량 낮아야 함 (속임수)
    const breakVR = volumeRatio(data, i - 1, o.volumeWindow);
    if (breakVR > o.fakeBreakVolMax) continue;

    // 현재봉이 지지선 위로 복귀
    if (o.recoveryRequired && d.close <= support) continue;

    // 복귀봉 거래량 증가
    const recoveryVR = volumeRatio(data, i, o.volumeWindow);
    if (recoveryVR < o.recoveryVolMin) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + recoveryVR * 0.02),
      type: 'BUY',
      label: '반칙형',
      meta: { support, breakVR: +breakVR.toFixed(2), recoveryVR: +recoveryVR.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A12 = {
  id:'foul_pattern', no:12, name:'반칙형 패턴', module:'A', group:'G3-파동',
  color:'#AB47BC', icon:'⚠️', signal:'BUY', ma:[20,60],
  desc:'지지선 일시 이탈(속임 하락) 후 빠른 복귀. 손절 유도 후 급반등.',
  buy:'지지선 이탈 당일 종가 복귀 확인', sell:'지지선 재이탈 or -3%',
};


// ─────────────────────────────────────────────
//  패턴 13 : 종형 세력 복귀형  (bell_return)
// ─────────────────────────────────────────────
const OPTIONS_A13 = {
  /** 이전 고점 탐색 구간 (봉) */
  peakLookback: 60,
  /** 고점 이후 무시 구간 (최근 N봉 제외) */
  peakIgnoreRecent: 20,
  /** 하락 폭 최소 */
  dropMin: 0.25,
  /** 하락 폭 최대 */
  dropMax: 0.55,
  /** 거래량 배율 (세력 재진입 신호) */
  volMultiplier: 2.0,
  volumeWindow: 20,
  /** MA60 근처 확인 */
  requireNearMA60: false,
  ma60Tolerance: 0.05,
  baseConfidence: 0.72,
  dedupeGap: 10,
  minIdx: 60,
};

function detectA13(data, opts = {}) {
  const o = { ...OPTIONS_A13, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    const prevHigh = Math.max(
      ...data.slice(i - o.peakLookback, i - o.peakIgnoreRecent).map(x => x.close)
    );
    const drop = (prevHigh - d.close) / prevHigh;
    if (drop < o.dropMin || drop > o.dropMax) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    if (o.requireNearMA60 && d.ma60) {
      if (Math.abs(d.close - d.ma60) / d.ma60 > o.ma60Tolerance) continue;
    }

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - o.volMultiplier) * 0.02),
      type: 'BUY',
      label: '세력복귀',
      meta: { prevHigh, drop: +(drop*100).toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A13 = {
  id:'bell_return', no:13, name:'종형 세력 복귀형', module:'A', group:'G3-파동',
  color:'#7E57C2', icon:'🔔', signal:'BUY', ma:[60,120],
  desc:'종(Bell) 모양 급등락 후 세력 재진입. 이전 고점 대비 30~50% 하락 구간에서 대량 거래 재출현.',
  buy:'이전 급등 거래량의 50% 이상 재출현 + 60MA 부근', sell:'60MA 하향 이탈',
};


// ─────────────────────────────────────────────
//  패턴 14 : 스마트머니 유입  (smart_money)
// ─────────────────────────────────────────────
const OPTIONS_A14 = {
  /** 하락 추세 확인 구간 (봉) */
  downTrendBars: 5,
  /** 아래꼬리 비율 최소값 */
  lowerWickMin: 0.35,
  /** 거래량 배율 */
  volMultiplier: 1.5,
  volumeWindow: 5,
  /** 하락 중 거래량 증가 확인 */
  volIncreasingInDrop: true,
  baseConfidence: 0.70,
  dedupeGap: 5,
  minIdx: 5,
};

function detectA14(data, opts = {}) {
  const o = { ...OPTIONS_A14, ...opts };
  const signals = [];

  for (let i = o.downTrendBars; i < data.length; i++) {
    const d = data[i];

    // 하락 추세 확인
    const downTrend = data
      .slice(i - o.downTrendBars, i)
      .every((x, j, a) => j === 0 || x.close <= a[j-1].close);
    if (!downTrend) continue;

    // 아래꼬리 확인
    if (lowerWickRatio(d) < o.lowerWickMin) continue;

    // 거래량 확인
    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    // 하락 중 거래량 증가 확인
    if (o.volIncreasingInDrop) {
      const volIncreasing = data.slice(i - o.downTrendBars + 1, i + 1)
        .every((x, j, a) => j === 0 || x.volume >= a[j-1].volume * 0.9);
      if (!volIncreasing) continue;
    }

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - 1) * 0.03 + lowerWickRatio(d) * 0.1),
      type: 'BUY',
      label: '스마트머니',
      meta: { lowerWick: +lowerWickRatio(d).toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A14 = {
  id:'smart_money', no:14, name:'스마트머니 유입', module:'A', group:'G3-파동',
  color:'#26C6DA', icon:'🧠', signal:'BUY', ma:[20,60],
  desc:'주가 하락 중 거래량 증가 + 아래꼬리 장대 음봉. 기관/외국인 누적 매수 신호.',
  buy:'하락 중 거래량 150% + 아래꼬리/몸통 비율>1', sell:'하락 연속 3봉 + 거래량 지속 증가',
};


// ─────────────────────────────────────────────
//  패턴 15 : 단기급등 N자형  (short_surge_n)
// ─────────────────────────────────────────────
const OPTIONS_A15 = {
  /** N자 완성 구간 최대 (봉) */
  nShapeMaxBars: 10,
  /** 눌림목 최소 반등률 */
  pullbackReboundMin: 0.03,
  /** 2차 상승 거래량이 1차보다 커야 함 */
  requireHigherVol2ndRun: false,
  /** 총 상승폭 최소 */
  totalGainMin: 0.08,
  volumeWindow: 5,
  baseConfidence: 0.72,
  dedupeGap: 8,
  minIdx: 10,
};

function detectA15(data, opts = {}) {
  const o = { ...OPTIONS_A15, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length - 3; i++) {
    const sl = data.slice(i - o.nShapeMaxBars, i + 1);
    if (sl.length < 5) continue;

    // 고점 인덱스
    const maxJ = sl.reduce((m, d, j) => d.close > sl[m].close ? j : m, 0);
    if (maxJ <= 0 || maxJ >= sl.length - 2) continue;

    // 고점 이후 저점 인덱스
    const afterPeak = sl.slice(maxJ);
    const minAfterJ = afterPeak.reduce((m, d, j) => d.close < afterPeak[m].close ? j : m, 0);
    if (minAfterJ <= 0) continue;

    const peakPrice = sl[maxJ].close;
    const valleyPrice = afterPeak[minAfterJ].close;
    const currentPrice = sl[sl.length - 1].close;

    // 반등 확인
    const rebound = (currentPrice - valleyPrice) / valleyPrice;
    if (rebound < o.pullbackReboundMin) continue;

    // 총 상승폭
    const totalGain = (currentPrice - sl[0].close) / sl[0].close;
    if (totalGain < o.totalGainMin) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + rebound * 0.5),
      type: 'BUY',
      label: '단기N자',
      meta: { peakPrice, valleyPrice, rebound: +(rebound*100).toFixed(2), totalGain: +(totalGain*100).toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A15 = {
  id:'short_surge_n', no:15, name:'단기급등 N자형', module:'A', group:'G3-파동',
  color:'#00BCD4', icon:'📈', signal:'BUY', ma:[5,20],
  desc:'5~10일 내 N자 완성 단기 급등. 두 번째 상승 거래량 > 첫 번째.',
  buy:'N자 중간 눌림목 저점 + 2차 상승 거래량 확인', sell:'N자 저점 하향 이탈',
};


/* ─── patterns/moduleA/A16_A20_group4.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 그룹4]  상한가 / 레벨업 계열  패턴 16~20
 * ═══════════════════════════════════════════════════════════════════
 */

// ─────────────────────────────────────────────
//  패턴 16 : 상한가 파라잡기 I  (upper_limit_1)
// ─────────────────────────────────────────────
const OPTIONS_A16 = {
  /** 상한가 최소 등락률 (한국 상한가 기준 +29.9% → 시뮬레이션 완화) */
  upperLimitMin: 0.20,
  /** 상한가 당일 거래량 배율 */
  limitDayVolMin: 5.0,
  volumeWindow: 10,
  /** 익일 시가가 전일 종가 대비 N% 이내 */
  nextOpenGapMax: 0.03,
  /** 익일 진입 여부 (true=익일, false=당일) */
  enterNextDay: true,
  baseConfidence: 0.80,
  dedupeGap: 5,
  minIdx: 2,
};

function detectA16(data, opts = {}) {
  const o = { ...OPTIONS_A16, ...opts };
  const signals = [];

  for (let i = 2; i < data.length; i++) {
    const prev = data[i - 1];
    const cur  = data[i];

    // 전일 상한가 판단
    const prevGain = i >= 2
      ? (prev.close - data[i-2].close) / data[i-2].close
      : 0;
    if (prevGain < o.upperLimitMin) continue;

    // 전일 거래량 폭증
    const avgVol = volumeRatio(data, i - 1, o.volumeWindow);
    if (avgVol < o.limitDayVolMin) continue;

    // 익일 시가 갭 확인
    const openGap = Math.abs(cur.open - prev.close) / prev.close;
    if (openGap > o.nextOpenGapMax) continue;

    const enterIdx = o.enterNextDay ? i : i - 1;
    signals.push({
      idx: enterIdx,
      confidence: clamp01(o.baseConfidence - openGap * 5),
      type: 'BUY',
      label: '상한가파라I',
      meta: { prevGain: +(prevGain*100).toFixed(2), openGap: +(openGap*100).toFixed(2), prevClose: prev.close },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A16 = {
  id:'upper_limit_1', no:16, name:'상한가 파라잡기 I', module:'A', group:'G4-레벨',
  color:'#FF1744', icon:'🚀', signal:'BUY', ma:[5,20],
  desc:'상한가 다음날 -3% 이내 눌림목 추격 매수. 상한가 거래량 5배 이상 필수.',
  buy:'상한가 익일 시가 -3% 이내 + 전일 거래량 5배', sell:'상한가 종가 -5% 이탈',
};


// ─────────────────────────────────────────────
//  패턴 17 : 상한가 파라잡기 II  (upper_limit_2)
// ─────────────────────────────────────────────
const OPTIONS_A17 = {
  /** BB 수축 판단 구간 */
  bbContractionBars: 8,
  /** BB 수축 최대폭 (종가 대비 비율) */
  bbWidthMax: 0.03,
  /** 상한가 최소 등락률 */
  upperLimitMin: 0.20,
  /** 거래량 배율 */
  volMultiplier: 4.0,
  volumeWindow: 5,
  baseConfidence: 0.78,
  dedupeGap: 5,
  minIdx: 10,
};

function detectA17(data, opts = {}) {
  const o = { ...OPTIONS_A17, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.bbU || !d.bbL) continue;

    // BB 수축 구간 확인
    const recentBBW = data.slice(i - o.bbContractionBars, i)
      .map(x => bbWidth(x));
    const minBBW = Math.min(...recentBBW);
    if (minBBW > o.bbWidthMax) continue;

    // 상한가 확인
    const gain = i > 0 ? (d.close - data[i-1].close) / data[i-1].close : 0;
    if (gain < o.upperLimitMin) continue;

    // 거래량 확인
    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - o.volMultiplier) * 0.01),
      type: 'BUY',
      label: '상한가파라II',
      meta: { minBBWidth: +(minBBW*100).toFixed(2), gain: +(gain*100).toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A17 = {
  id:'upper_limit_2', no:17, name:'상한가 파라잡기 II', module:'A', group:'G4-레벨',
  color:'#D50000', icon:'💥', signal:'BUY', ma:[5,20],
  desc:'BB 수축 구간 이후 상한가 에너지 분출. 수축 기간이 길수록 지속성 강.',
  buy:'BB폭 수축 5일 이상 + 상한가 확인 후 익일 진입', sell:'BB 중심선(MA20) 이탈',
};


// ─────────────────────────────────────────────
//  패턴 18 : 레벨업 / SD패턴  (level_up_sd)
// ─────────────────────────────────────────────
const OPTIONS_A18 = {
  /** 이전 고점 탐색 구간 */
  prevHighLookback: 20,
  /** 이전 고점 근접 허용 오차 */
  prevHighTolerance: 0.015,
  /** BB 상단 돌파 확인 */
  requireBBBreak: true,
  /** 거래량 배율 */
  volMultiplier: 1.5,
  volumeWindow: 5,
  baseConfidence: 0.76,
  dedupeGap: 5,
  minIdx: 20,
};

function detectA18(data, opts = {}) {
  const o = { ...OPTIONS_A18, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (o.requireBBBreak && (!d.bbU || d.close <= d.bbU)) continue;

    const prevHigh = Math.max(
      ...data.slice(i - o.prevHighLookback, i).map(x => x.close)
    );
    const nearPrevHigh = Math.abs(d.close - prevHigh) / prevHigh < o.prevHighTolerance;
    if (!nearPrevHigh) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - 1) * 0.02),
      type: 'BUY',
      label: '레벨업SD',
      meta: { prevHigh, volumeRatio: +vr.toFixed(2), bbUpper: d.bbU },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A18 = {
  id:'level_up_sd', no:18, name:'레벨업 / SD패턴', module:'A', group:'G4-레벨',
  color:'#FF6F00', icon:'📊', signal:'BUY', ma:[20,60],
  desc:'이전 고점을 새 지지선으로 전환하며 계단식 레벨 상승. BB 상단 돌파가 레벨 전환 신호.',
  buy:'이전 고점 지지 확인 + BB 상단 재돌파', sell:'레벨 지지선(이전 고점) 하향 이탈',
};


// ─────────────────────────────────────────────
//  패턴 19 : 볼라올 GC (BB하단 + 골든크로스)  (bb_golden_cross)
// ─────────────────────────────────────────────
const OPTIONS_A19 = {
  /** BB 하단 터치 확인 구간 (현재봉 기준 최근 N봉 내) */
  bbTouchLookback: 5,
  /** 골든크로스 최대 허용 지연 (봉) */
  gcMaxDelay: 5,
  volumeWindow: 5,
  baseConfidence: 0.88,
  dedupeGap: 5,
  minIdx: 20,
};

function detectA19(data, opts = {}) {
  const o = { ...OPTIONS_A19, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    // 골든크로스 확인
    if (!isGoldenCross(data, i)) continue;

    // 최근 N봉 내 BB 하단 터치 확인
    const recentBBTouch = data
      .slice(Math.max(0, i - o.bbTouchLookback), i + 1)
      .some(d => d.bbL != null && d.low <= d.bbL);
    if (!recentBBTouch) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence),
      type: 'BUY',
      label: '볼라올GC',
      meta: { ma20: data[i].ma20, ma60: data[i].ma60, bbLower: data[i].bbL },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A19 = {
  id:'bb_golden_cross', no:19, name:'볼라올 GC (BB+골든크로스)', module:'A', group:'G4-레벨',
  color:'#FFD600', icon:'⭐', signal:'BUY', ma:[20,60],
  desc:'BB 하단 터치 + MA20/MA60 골든크로스 동시/연속 발생. 복합 신호로 신뢰도 최상.',
  buy:'BB 하단 터치 후 5봉 이내 골든크로스', sell:'골든크로스 실패(재데드크로스)',
};


// ─────────────────────────────────────────────
//  패턴 20 : 더블 쌍바닥형  (double_double_bottom)
// ─────────────────────────────────────────────
const OPTIONS_A20 = {
  /** 전체 패턴 최대 구간 */
  patternMaxSpan: 80,
  /** 대형 쌍바닥 가격 오차 */
  bigBottomTolerance: 0.04,
  /** 소형 쌍바닥 가격 오차 */
  smallBottomTolerance: 0.03,
  /** 트로프 탐색 wing */
  troughWing: 3,
  baseConfidence: 0.88,
  dedupeGap: 8,
};

function detectA20(data, opts = {}) {
  const o = { ...OPTIONS_A20, ...opts };
  const troughs = findTroughs(data, o.troughWing);
  const signals = [];

  for (let t = 3; t < troughs.length; t++) {
    const [t1, t2, t3, t4] = [troughs[t-3], troughs[t-2], troughs[t-1], troughs[t]];
    if (t4 - t1 > o.patternMaxSpan) continue;

    const bigDiff = Math.abs(data[t1].close - data[t2].close) / data[t1].close;
    const smallDiff = Math.abs(data[t3].close - data[t4].close) / data[t3].close;

    if (bigDiff > o.bigBottomTolerance || smallDiff > o.smallBottomTolerance) continue;

    signals.push({
      idx: t4,
      confidence: clamp01(o.baseConfidence - bigDiff - smallDiff),
      type: 'BUY',
      label: '더블쌍바닥',
      meta: { t1, t2, t3, t4, bigDiff: +(bigDiff*100).toFixed(2), smallDiff: +(smallDiff*100).toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A20 = {
  id:'double_double_bottom', no:20, name:'더블 쌍바닥형', module:'A', group:'G4-레벨',
  color:'#00E676', icon:'🔁', signal:'BUY', ma:[20,60],
  desc:'대형 이중바닥 안에 소형 이중바닥 내포. 지지력 이중 확인으로 신뢰도 최상.',
  buy:'소형 이중바닥 완성 + 대형 넥라인 돌파', sell:'소형 이중바닥 저점 하향 이탈',
};


/* ─── patterns/moduleA/A21_A25_group5.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 그룹5]  속임수 / 볼록 계열  패턴 21~25
 * ═══════════════════════════════════════════════════════════════════
 */

// ─────────────────────────────────────────────
//  패턴 21 : 속임수 패턴형  (fake_out)
// ─────────────────────────────────────────────
const OPTIONS_A21 = {
  /** 지지선 기준 MA */
  supportMA: 20,
  /** 이탈 임계값 (지지선 대비 하회 %) */
  breakThreshold: 0.015,
  /** 이탈봉 거래량 최대 배율 (소거래량 이탈 = 속임수) */
  fakeVolMax: 1.2,
  /** 복귀봉 거래량 최소 배율 */
  recoveryVolMin: 1.6,
  volumeWindow: 5,
  baseConfidence: 0.80,
  dedupeGap: 5,
  minIdx: 5,
};

function detectA21(data, opts = {}) {
  const o = { ...OPTIONS_A21, ...opts };
  const key = `ma${o.supportMA}`;
  const signals = [];

  for (let i = o.minIdx; i < data.length - 2; i++) {
    const d = data[i];
    const support = d[key];
    if (!support) continue;

    // 직전봉 이탈
    const prev = data[i - 1];
    const prevSupport = prev[key];
    if (!prevSupport || prev.close >= prevSupport * (1 - o.breakThreshold)) continue;

    // 이탈봉 소거래량
    const breakVR = volumeRatio(data, i - 1, o.volumeWindow);
    if (breakVR > o.fakeVolMax) continue;

    // 현재봉 복귀
    if (d.close <= support) continue;

    // 복귀봉 대거래량
    const recovVR = volumeRatio(data, i, o.volumeWindow);
    if (recovVR < o.recoveryVolMin) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + recovVR * 0.02),
      type: 'BUY',
      label: '속임수반등',
      meta: { support, breakVR: +breakVR.toFixed(2), recovVR: +recovVR.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A21 = {
  id:'fake_out', no:21, name:'속임수 패턴형', module:'A', group:'G5-속임수',
  color:'#F06292', icon:'🎭', signal:'BUY', ma:[20,60],
  desc:'지지선 이탈 속임 후 급반등. 이탈 캔들 거래량이 적고, 당일/익일 강한 양봉 복귀.',
  buy:'이탈 캔들 소거래량 + 익일 강봉 복귀', sell:'이탈 후 2봉 연속 지지선 하회',
};


// ─────────────────────────────────────────────
//  패턴 22 : 분봉 볼록봉자  (convex_candle)
// ─────────────────────────────────────────────
const OPTIONS_A22 = {
  /** 연속 양봉 확인 수 */
  consecutiveUp: 3,
  /** 몸통 확대 비율 (이전봉 대비 최소 유지 비율) */
  bodyGrowthRatio: 0.90,
  /** 거래량 배율 */
  volMultiplier: 1.3,
  volumeWindow: 5,
  baseConfidence: 0.68,
  dedupeGap: 4,
  minIdx: 3,
};

function detectA22(data, opts = {}) {
  const o = { ...OPTIONS_A22, ...opts };
  const signals = [];

  for (let i = o.consecutiveUp; i < data.length; i++) {
    const sl = data.slice(i - o.consecutiveUp + 1, i + 1);

    // 모두 양봉
    const allUp = sl.every(d => d.close > d.open);
    if (!allUp) continue;

    // 몸통 확대 확인
    const growing = sl.every((d, j, a) => {
      if (j === 0) return true;
      const body = Math.abs(d.close - d.open);
      const prevBody = Math.abs(a[j-1].close - a[j-1].open);
      return body >= prevBody * o.bodyGrowthRatio;
    });
    if (!growing) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - 1) * 0.03),
      type: 'BUY',
      label: '볼록봉자',
      meta: { consecutiveUp: o.consecutiveUp, volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A22 = {
  id:'convex_candle', no:22, name:'분봉 볼록봉자', module:'A', group:'G5-속임수',
  color:'#CE93D8', icon:'📊', signal:'BUY', ma:[5,20],
  desc:'분봉 연속 볼록 양봉(각 몸통이 이전보다 크거나 동일, 방향 일치). 추세 가속 신호.',
  buy:'분봉 3봉 연속 양봉 + 몸통 점진적 확대', sell:'볼록봉 다음 도지 or 음봉 출현',
};


// ─────────────────────────────────────────────
//  패턴 23 : 줄헌 매집형  (line_accumulation)
// ─────────────────────────────────────────────
const OPTIONS_A23 = {
  /** 박스권 탐색 구간 */
  boxLookback: 25,
  /** 박스권 최대 등락폭 */
  boxRangeMax: 0.05,
  /** 거래량 증가 추세 확인: 후반 5봉 vs 전반 5봉 비율 */
  volTrendRatio: 1.2,
  /** 상단 돌파 임계값 */
  breakThreshold: 0.005,
  volumeWindow: 5,
  baseConfidence: 0.74,
  dedupeGap: 5,
  minIdx: 25,
};

function detectA23(data, opts = {}) {
  const o = { ...OPTIONS_A23, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const sl = data.slice(i - o.boxLookback, i);
    const hi = Math.max(...sl.map(d => d.close));
    const lo = Math.min(...sl.map(d => d.close));
    const rng = (hi - lo) / lo;
    if (rng > o.boxRangeMax) continue;

    // 거래량 증가 추세
    const half = Math.floor(sl.length / 2);
    const earlyVol = sl.slice(0, half).reduce((s,d)=>s+d.volume,0) / half;
    const lateVol  = sl.slice(half).reduce((s,d)=>s+d.volume,0) / (sl.length - half);
    if (lateVol < earlyVol * o.volTrendRatio) continue;

    // 상단 돌파
    if (data[i].close < hi * (1 + o.breakThreshold)) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (1 - rng / o.boxRangeMax) * 0.1),
      type: 'BUY',
      label: '줄헌매집',
      meta: { boxRange: +(rng*100).toFixed(2), earlyVol: Math.round(earlyVol), lateVol: Math.round(lateVol) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A23 = {
  id:'line_accumulation', no:23, name:'줄헌 매집형', module:'A', group:'G5-속임수',
  color:'#80DEEA', icon:'📏', signal:'BUY', ma:[60,120],
  desc:'수평 밴드 장기 횡보 + 점진적 거래량 증가. 상단 돌파 방향으로 포지션 결정.',
  buy:'20일 박스권 ±2% + 거래량 점진 증가 + 상단 돌파', sell:'박스권 하단 이탈',
};


// ─────────────────────────────────────────────
//  패턴 24 : 볼라올 SD (BB하단 반등)  (bb_lower_bounce)
// ─────────────────────────────────────────────
const OPTIONS_A24 = {
  /** 저가가 BB하단에 닿아야 함 (low <= bbL) */
  requireLowTouch: true,
  /** 종가가 BB하단 위에 있어야 함 */
  requireCloseAbove: true,
  /** 아래꼬리 최소 비율 */
  lowerWickMin: 0.30,
  /** 거래량 배율 */
  volMultiplier: 1.4,
  volumeWindow: 5,
  baseConfidence: 0.65,
  dedupeGap: 5,
  minIdx: 20,
};

function detectA24(data, opts = {}) {
  const o = { ...OPTIONS_A24, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.bbL) continue;

    if (o.requireLowTouch && d.low > d.bbL) continue;
    if (o.requireCloseAbove && d.close <= d.bbL) continue;

    // 아래꼬리 확인
    const range = d.high - d.low;
    const lw = range > 0 ? (Math.min(d.open, d.close) - d.low) / range : 0;

    const vr = volumeRatio(data, i, o.volumeWindow);
    const hasSignal = lw >= o.lowerWickMin || vr >= o.volMultiplier;
    if (!hasSignal) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + lw * 0.2 + (vr - 1) * 0.05),
      type: 'BUY',
      label: 'BB하단반등',
      meta: { bbLower: d.bbL, lowerWick: +lw.toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A24 = {
  id:'bb_lower_bounce', no:24, name:'볼라올 SD (BB하단 반등)', module:'A', group:'G5-속임수',
  color:'#A78BFA', icon:'🔮', signal:'BUY', ma:[20],
  desc:'BB 하단 2σ 터치 후 아래꼬리 양봉 마감. 과매도 기술적 반등 포착.',
  buy:'BB 하단 터치 + 아래꼬리>몸통 + 거래량 증가', sell:'BB 하단 종가 이탈',
};


// ─────────────────────────────────────────────
//  패턴 25 : 1년 매집형 (240MA)  (yearly_acc)
// ─────────────────────────────────────────────
const OPTIONS_A25 = {
  /** 240MA 하단에 있다가 돌파 */
  requirePrevBelow: true,
  /** 돌파 확인 임계값 (240MA 대비 % 초과) */
  breakThreshold: 0.005,
  /** 직전봉이 240MA 아래 있어야 함 */
  prevBelowTolerance: 0.005,
  /** 거래량 배율 */
  volMultiplier: 1.8,
  volumeWindow: 20,
  baseConfidence: 0.86,
  dedupeGap: 10,
  minIdx: 240,
};

function detectA25(data, opts = {}) {
  const o = { ...OPTIONS_A25, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.ma240) continue;

    // 직전봉 240MA 아래
    if (o.requirePrevBelow) {
      const prev = data[i - 1];
      if (!prev.ma240 || prev.close > prev.ma240 * (1 + o.prevBelowTolerance)) continue;
    }

    // 현재봉 240MA 돌파
    if (d.close < d.ma240 * (1 + o.breakThreshold)) continue;

    // 거래량 확인
    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - o.volMultiplier) * 0.02),
      type: 'BUY',
      label: '1년매집돌파',
      meta: { ma240: d.ma240, volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A25 = {
  id:'yearly_acc', no:25, name:'1년 매집형 (240MA)', module:'A', group:'G5-속임수',
  color:'#F97316', icon:'📅', signal:'BUY', ma:[120,240],
  desc:'240MA 하단 1년 초장기 매집. 분기별 거래량 증가. 240MA 상향 돌파가 완성 신호.',
  buy:'240MA 하단 + 분기 거래량 증가 + 240MA 돌파', sell:'240MA 재이탈',
};


/* ─── patterns/moduleA/A26_A33_group6_7.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module A - 그룹6~7]  날개/전략/최종 계열  패턴 26~33
 * ═══════════════════════════════════════════════════════════════════
 */

// ─────────────────────────────────────────────
//  패턴 26 : 새의 날개기형  (bird_wing)
// ─────────────────────────────────────────────
const OPTIONS_A26 = {
  /** 전체 패턴 구간 */
  lookback: 20,
  /** 중심 분할 비율 (0.5 = 정중앙) */
  splitRatio: 0.5,
  /** 좌우 날개 대칭 허용 오차 */
  symmetryTolerance: 0.20,
  /** 상단 돌파 확인 */
  requireBreakout: true,
  baseConfidence: 0.70,
  dedupeGap: 8,
  minIdx: 20,
};

function detectA26(data, opts = {}) {
  const o = { ...OPTIONS_A26, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const sl = data.slice(i - o.lookback, i + 1);
    const mid = Math.floor(sl.length * o.splitRatio);

    const leftRange = Math.max(...sl.slice(0,mid).map(d=>d.close))
                    - Math.min(...sl.slice(0,mid).map(d=>d.close));
    const rightRange= Math.max(...sl.slice(mid).map(d=>d.close))
                    - Math.min(...sl.slice(mid).map(d=>d.close));

    if (leftRange <= 0) continue;
    const asymmetry = Math.abs(leftRange - rightRange) / leftRange;
    if (asymmetry > o.symmetryTolerance) continue;

    const leftHigh = Math.max(...sl.slice(0, mid).map(d => d.close));
    if (o.requireBreakout && sl[sl.length-1].close <= leftHigh) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (o.symmetryTolerance - asymmetry) * 0.5),
      type: 'BUY',
      label: '날개기형',
      meta: { leftRange, rightRange, asymmetry: +(asymmetry*100).toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A26 = {
  id:'bird_wing', no:26, name:'새의 날개기형', module:'A', group:'G6-전략',
  color:'#4DB6AC', icon:'🦅', signal:'BUY', ma:[20,60],
  desc:'중심축 기준 대칭 날개형 차트. 균형이 깨지는 방향으로 추세 결정.',
  buy:'좌우 저점 균형 확인 + 중심축 상향 이탈', sell:'중심축 하향 이탈',
};


// ─────────────────────────────────────────────
//  패턴 27 : STT 투런 선공략형  (stt_two_run)
// ─────────────────────────────────────────────
const OPTIONS_A27 = {
  /** 저항선 탐색 구간 */
  resistanceLookback: 20,
  /** 저항선 탐색 제외 최근 구간 */
  resistanceIgnoreRecent: 5,
  /** 1차 시도 판단 구간 */
  firstAttemptRange: [10, 3], // [from, to] (i-10 ~ i-3)
  /** 1차 시도 저항선 접근 임계값 */
  firstAttemptThreshold: 0.02,
  /** 2차 돌파 거래량 배율 */
  breakVolMultiplier: 2.0,
  volumeWindow: 5,
  baseConfidence: 0.80,
  dedupeGap: 5,
  minIdx: 20,
};

function detectA27(data, opts = {}) {
  const o = { ...OPTIONS_A27, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    const rStart = i - o.resistanceLookback;
    const rEnd   = i - o.resistanceIgnoreRecent;
    if (rStart < 0) continue;

    const resistance = Math.max(...data.slice(rStart, rEnd).map(x => x.close));

    // 1차 시도 확인
    const [fa, fb] = o.firstAttemptRange;
    const firstAttempt = data
      .slice(Math.max(0, i - fa), i - fb + 1)
      .some(x => x.close >= resistance * (1 - o.firstAttemptThreshold));
    if (!firstAttempt) continue;

    // 2차 돌파 확인
    if (d.close <= resistance) continue;
    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.breakVolMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - o.breakVolMultiplier) * 0.02),
      type: 'BUY',
      label: 'STT투런',
      meta: { resistance, volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A27 = {
  id:'stt_two_run', no:27, name:'STT 투런 선공략형', module:'A', group:'G6-전략',
  color:'#26A69A', icon:'🎯', signal:'BUY', ma:[20,60],
  desc:'두 번의 저항선 공략 패턴. 2차 도전 거래량>1차 시 돌파 확률 상승.',
  buy:'2차 도전 거래량>1차 + 저항선 종가 돌파', sell:'저항선 재이탈 후 지지 실패',
};


// ─────────────────────────────────────────────
//  패턴 28 : -7% 공략형  (minus_7pct)
// ─────────────────────────────────────────────
const OPTIONS_A28 = {
  /** 하락 최소 % */
  dropMin: 0.065,
  /** 하락 최대 % (너무 크면 추세 붕괴) */
  dropMax: 0.09,
  /** 정배열 필수 (MA20 > MA60) */
  requireBullAlign: true,
  /** 거래량 폭증 없어도 됨 */
  volRequired: false,
  volMultiplier: 1.0,
  volumeWindow: 5,
  baseConfidence: 0.73,
  dedupeGap: 5,
  minIdx: 1,
};

function detectA28(data, opts = {}) {
  const o = { ...OPTIONS_A28, ...opts };
  const signals = [];

  for (let i = 1; i < data.length; i++) {
    const d = data[i];
    const drop = (data[i-1].close - d.close) / data[i-1].close;
    if (drop < o.dropMin || drop > o.dropMax) continue;

    if (o.requireBullAlign && d.ma20 && d.ma60 && d.ma20 <= d.ma60) continue;

    if (o.volRequired) {
      const vr = volumeRatio(data, i, o.volumeWindow);
      if (vr < o.volMultiplier) continue;
    }

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence - (drop - o.dropMin) * 5),
      type: 'BUY',
      label: '-7%공략',
      meta: { drop: +(drop*100).toFixed(2), ma20: d.ma20, ma60: d.ma60 },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A28 = {
  id:'minus_7pct', no:28, name:'-7% 공략형', module:'A', group:'G6-전략',
  color:'#66BB6A', icon:'📉', signal:'BUY', ma:[20,60],
  desc:'전일 대비 7% 하락 구간 역발상 매수. 정배열 유지 시 반등 확률 통계적 우세.',
  buy:'전일 대비 -7% + 정배열(MA20>MA60) 유지', sell:'-10% 추가 하락 or 정배열 붕괴',
};


// ─────────────────────────────────────────────
//  패턴 29 : 누적매물 돌파형  (accumulated_break)
// ─────────────────────────────────────────────
const OPTIONS_A29 = {
  /** 매물대 탐색 구간 */
  resistanceLookback: 30,
  /** 저항선 도전 횟수 최소 */
  minAttempts: 2,
  /** 저항선 접근 임계값 */
  attemptThreshold: 0.03,
  /** 확인 제외 최근 구간 */
  recentExclude: 5,
  /** 돌파 거래량 배율 */
  volMultiplier: 2.5,
  volumeWindow: 5,
  baseConfidence: 0.85,
  dedupeGap: 5,
  minIdx: 30,
};

function detectA29(data, opts = {}) {
  const o = { ...OPTIONS_A29, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const resistance = Math.max(
      ...data.slice(i - o.resistanceLookback, i - o.recentExclude).map(d=>d.close)
    );
    // 저항선 도전 횟수 계산
    const attempts = data
      .slice(i - o.resistanceLookback + o.recentExclude, i - 1)
      .filter(d => d.close >= resistance * (1 - o.attemptThreshold)).length;
    if (attempts < o.minAttempts) continue;

    // 돌파 확인
    if (data[i].close <= resistance) continue;
    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + attempts * 0.01 + (vr - o.volMultiplier) * 0.01),
      type: 'BUY',
      label: '누적매물돌파',
      meta: { resistance, attempts, volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A29 = {
  id:'accumulated_break', no:29, name:'누적매물 돌파형', module:'A', group:'G6-전략',
  color:'#8BC34A', icon:'🏔', signal:'BUY', ma:[20,60],
  desc:'장기 누적 매물대 한 번에 돌파. 매물대 여러 번 도전 후 마지막 돌파 + 대량 거래량.',
  buy:'매물대 2회 이상 도전 후 3차 돌파 + 거래량 250%', sell:'매물대 하향 재진입',
};


// ─────────────────────────────────────────────
//  패턴 30 : 6020패턴 (KJC 핵심)  (ma60_ma20)
// ─────────────────────────────────────────────
const OPTIONS_A30 = {
  /** 골든크로스 후 최대 허용 지연 (봉) */
  gcMaxDelay: 3,
  /** MA60 기울기 양수 필요 */
  requireMA60Rising: true,
  /** MA60 기울기 확인 구간 */
  ma60SlopeLookback: 3,
  /** 현가 > MA20 필수 */
  requireAboveMA20: true,
  volumeWindow: 5,
  baseConfidence: 0.90,
  dedupeGap: 5,
  minIdx: 1,
};

function detectA30(data, opts = {}) {
  const o = { ...OPTIONS_A30, ...opts };
  const signals = [];

  for (let i = 1; i < data.length; i++) {
    if (!isGoldenCross(data, i)) continue;

    const d = data[i];
    if (o.requireAboveMA20 && d.ma20 && d.close < d.ma20) continue;
    if (o.requireMA60Rising && maSlope(data, i, 60, o.ma60SlopeLookback) <= 0) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence),
      type: 'BUY',
      label: '6020패턴',
      meta: { ma20: d.ma20, ma60: d.ma60, ma60Slope: +maSlope(data, i, 60, o.ma60SlopeLookback).toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A30 = {
  id:'ma60_ma20', no:30, name:'6020패턴 (KJC 핵심)', module:'A', group:'G6-전략',
  color:'#CDDC39', icon:'✨', signal:'BUY', ma:[20,60],
  desc:'MA20이 MA60 골든크로스 + 현가>MA20>MA60 정배열 + MA60 우상향. KJC 핵심 패턴.',
  buy:'골든크로스 3봉 이내 + 정배열 + MA60 기울기>0', sell:'데드크로스 발생',
};


// ─────────────────────────────────────────────
//  패턴 31 : 마지막 속임수요착형  (final_fake_trap)
// ─────────────────────────────────────────────
const OPTIONS_A31 = {
  /** 선행 강봉 연속 수 */
  strongCandleStreak: 3,
  /** 강봉 판단: 양봉 필수 */
  requireBullCandles: true,
  /** 출회봉 하락률 최소 */
  exitDropMin: 0.025,
  /** 출회봉 거래량 배율 */
  exitVolMultiplier: 3.0,
  volumeWindow: 5,
  baseConfidence: 0.83,
  dedupeGap: 5,
  minIdx: 5,
};

function detectA31(data, opts = {}) {
  const o = { ...OPTIONS_A31, ...opts };
  const signals = [];

  for (let i = o.strongCandleStreak; i < data.length; i++) {
    const d = data[i];
    const prevBars = data.slice(i - o.strongCandleStreak, i);

    if (o.requireBullCandles && !prevBars.every(x => x.close > x.open)) continue;

    const exitDrop = d.open > 0 ? (d.open - d.close) / d.open : 0;
    if (exitDrop < o.exitDropMin) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.exitVolMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (vr - o.exitVolMultiplier) * 0.01),
      type: 'SELL',
      label: '요착형',
      meta: { exitDrop: +(exitDrop*100).toFixed(2), volumeRatio: +vr.toFixed(2), streak: o.strongCandleStreak },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A31 = {
  id:'final_fake_trap', no:31, name:'마지막 속임수요착형', module:'A', group:'G7-최종',
  color:'#FF7043', icon:'🪤', signal:'SELL', ma:[20,60],
  desc:'상승 마지막 단계 대량 음봉 세력 출회. 연속 강봉 후 갑작스러운 대량음봉 = 고점 신호.',
  buy:'없음 (매도/청산 패턴)', sell:'강봉 연속 3개 이후 대량음봉 거래량 300% 즉시 청산',
};


// ─────────────────────────────────────────────
//  패턴 32 : 피라미드형 (계단상승)  (pyramid)
// ─────────────────────────────────────────────
const OPTIONS_A32 = {
  /** 분석 구간 */
  lookback: 20,
  /** 고점 상향 최소 횟수 */
  higherHighsMin: 12,
  /** 저점 상향 최소 횟수 */
  higherLowsMin: 10,
  /** 현재가 MA20 근처 허용 오차 */
  ma20Tolerance: 0.025,
  requireNearMA20: true,
  volumeWindow: 5,
  baseConfidence: 0.74,
  dedupeGap: 8,
  minIdx: 20,
};

function detectA32(data, opts = {}) {
  const o = { ...OPTIONS_A32, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const sl = data.slice(i - o.lookback, i + 1);
    let hh = 0, hl = 0;
    for (let j = 1; j < sl.length; j++) {
      if (sl[j].high > sl[j-1].high) hh++;
      if (sl[j].low  > sl[j-1].low)  hl++;
    }
    if (hh < o.higherHighsMin || hl < o.higherLowsMin) continue;

    const d = data[i];
    if (o.requireNearMA20 && d.ma20) {
      if (Math.abs(d.close - d.ma20) / d.ma20 > o.ma20Tolerance) continue;
    }

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (hh - o.higherHighsMin) * 0.01),
      type: 'BUY',
      label: '피라미드',
      meta: { higherHighs: hh, higherLows: hl, ma20: d.ma20 },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A32 = {
  id:'pyramid', no:32, name:'피라미드형 (계단상승)', module:'A', group:'G7-최종',
  color:'#FFCC02', icon:'🔺', signal:'BUY', ma:[20,60],
  desc:'고점·저점 모두 계단식 상승. 각 계단 눌림목이 매수 타이밍.',
  buy:'저점 상향 + 고점 상향 + 눌림목 MA20 부근', sell:'저점 하향 이탈(계단 붕괴)',
};


// ─────────────────────────────────────────────
//  패턴 33 : 도리도 패턴 (상하 진동 후 방향 결정)  (dorido)
// ─────────────────────────────────────────────
const OPTIONS_A33 = {
  /** 분석 구간 */
  lookback: 25,
  /** 박스권 최대 등락폭 */
  boxRangeMax: 0.06,
  /** 중심선 교차 최소 횟수 */
  minCrosses: 4,
  /** 상단 돌파 거래량 배율 */
  volMultiplier: 1.8,
  /** 상단 돌파 임계값 */
  breakThreshold: 0.002,
  volumeWindow: 5,
  baseConfidence: 0.78,
  dedupeGap: 5,
  minIdx: 25,
};

function detectA33(data, opts = {}) {
  const o = { ...OPTIONS_A33, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const sl = data.slice(i - o.lookback, i);
    const hi = Math.max(...sl.map(d=>d.close));
    const lo = Math.min(...sl.map(d=>d.close));
    const rng = (hi - lo) / lo;
    if (rng > o.boxRangeMax) continue;

    const mid = (hi + lo) / 2;
    let crosses = 0;
    for (let j = 1; j < sl.length; j++) {
      if ((sl[j-1].close - mid) * (sl[j].close - mid) < 0) crosses++;
    }
    if (crosses < o.minCrosses) continue;

    // 상단 돌파
    if (data[i].close < hi * (1 + o.breakThreshold)) continue;
    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + crosses * 0.01),
      type: 'BUY',
      label: '도리도',
      meta: { boxRange: +(rng*100).toFixed(2), crosses, volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_A33 = {
  id:'dorido', no:33, name:'도리도 패턴', module:'A', group:'G7-최종',
  color:'#00BFA5', icon:'🔄', signal:'BUY', ma:[20,60],
  desc:'일정 구간 상하 반복 진동 → 방향 결정 패턴. 진동 횟수가 많을수록 방향 결정 후 강도 큼.',
  buy:'박스권 상단 돌파 + 거래량 증가', sell:'박스권 하단 이탈',
};


/* ─── patterns/moduleB/B01_B09_leverage.js ─── */
/**
 * ═══════════════════════════════════════════════════════════════════
 *  [Module B]  레버리지 / 인버스 ETF 9가지 패턴  B01~B09
 * ═══════════════════════════════════════════════════════════════════
 *  MA 색상 (KJC 원본):
 *    MA20 = 빨간(#FF3B3B),  MA60 = 녹색(#22C55E)
 * ═══════════════════════════════════════════════════════════════════
 */

// ─────────────────────────────────────────────
//  B01 : 20상하빗  (lev_20_osc)
//  정의: 20MA/60MA 반복 교차 → 마지막 방향으로 포지션
// ─────────────────────────────────────────────
const OPTIONS_B01 = {
  /** 교차 감지 구간 (봉) */
  lookback: 20,
  /** 최소 교차 횟수 */
  minCrosses: 2,
  /** 마지막 교차가 골든이면 레버리지, 데드면 인버스 */
  volumeWindow: 5,
  baseConfidence: 0.70,
  dedupeGap: 5,
  minIdx: 5,
};

function detectB01(data, opts = {}) {
  const o = { ...OPTIONS_B01, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    let crossCount = 0, lastType = '';
    for (let j = Math.max(1, i - o.lookback); j <= i; j++) {
      if (isGoldenCross(data, j)) { crossCount++; lastType = 'gc'; }
      else if (isDeadCross(data, j)) { crossCount++; lastType = 'dc'; }
    }
    if (crossCount < o.minCrosses || !lastType) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + Math.min(crossCount * 0.02, 0.15)),
      type: lastType === 'gc' ? 'BUY' : 'SELL',
      label: '20상하빗',
      meta: { crossCount, lastType, signal: lastType === 'gc' ? '레버리지' : '인버스' },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B01 = {
  id:'lev_20_osc', no:'B1', name:'20상하빗 (레버리지)', module:'B', group:'GB-레버',
  color:'#FF9800', icon:'⚖️', signal:'BUY', ma:[20,60],
  desc:'20MA/60MA 반복 교차(20일 내 2회↑). 마지막 교차 방향 = 포지션 방향.',
  buy:'골든크로스 확인 → 레버리지 ETF', sell:'데드크로스 확인 → 인버스 ETF',
};


// ─────────────────────────────────────────────
//  B02 : 60TT (60MA 쌍봉 하락)  (lev_60tt)
//  정의: 60MA가 두 개의 봉우리(Twin Top) 형성 → 인버스 신호
// ─────────────────────────────────────────────
const OPTIONS_B02 = {
  /** 피크 탐색 wing */
  peakWing: 5,
  /** 두 피크 최소 간격 */
  peakGapMin: 10,
  /** 2번째 피크가 1번째보다 낮아야 함 */
  requireLowerSecondPeak: true,
  /** 2번째 피크가 1번째보다 낮은 비율 최소 */
  secondPeakDropMin: 0.001,
  baseConfidence: 0.76,
  dedupeGap: 5,
  minIdx: 20,
};

function detectB02(data, opts = {}) {
  const o = { ...OPTIONS_B02, ...opts };
  const signals = [];

  // MA60 피크 탐색
  const ma60Peaks = [];
  for (let i = o.peakWing; i < data.length - o.peakWing; i++) {
    const m = data[i].ma60;
    if (!m) continue;
    const leftOk  = data.slice(i - o.peakWing, i).every(d => !d.ma60 || d.ma60 <= m);
    const rightOk = data.slice(i + 1, i + o.peakWing + 1).every(d => !d.ma60 || d.ma60 <= m);
    if (leftOk && rightOk) ma60Peaks.push({ idx: i, val: m });
  }

  for (let p = 1; p < ma60Peaks.length; p++) {
    const p1 = ma60Peaks[p - 1], p2 = ma60Peaks[p];
    if (p2.idx - p1.idx < o.peakGapMin) continue;
    if (o.requireLowerSecondPeak && p2.val >= p1.val - o.secondPeakDropMin) continue;

    signals.push({
      idx: p2.idx,
      confidence: clamp01(o.baseConfidence + (p1.val - p2.val) / p1.val * 5),
      type: 'SELL',
      label: '60TT',
      meta: { peak1: p1, peak2: p2, dropPct: +((p1.val - p2.val) / p1.val * 100).toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B02 = {
  id:'lev_60tt', no:'B2', name:'60TT (쌍봉 하락)', module:'B', group:'GB-레버',
  color:'#E91E63', icon:'📉', signal:'SELL', ma:[60],
  desc:'60MA 쌍봉 형성(2차 봉우리 < 1차). 레버리지 청산 및 인버스 진입 타이밍.',
  buy:'없음', sell:'2차 봉우리 확인 → 인버스 ETF 진입',
};


// ─────────────────────────────────────────────
//  B03 : 60상하빗 (박스권 교차)  (lev_60box)
//  정의: 60MA와 수평 박스권 교차 → 방향에 따라 레버리지/인버스
// ─────────────────────────────────────────────
const OPTIONS_B03 = {
  /** 박스권 탐색 구간 */
  boxLookback: 25,
  /** 박스권 최대 등락폭 */
  boxRangeMax: 0.06,
  /** 돌파/이탈 임계값 */
  breakThreshold: 0.005,
  /** 거래량 배율 */
  volMultiplier: 1.6,
  volumeWindow: 5,
  baseConfidence: 0.72,
  dedupeGap: 5,
  minIdx: 25,
};

function detectB03(data, opts = {}) {
  const o = { ...OPTIONS_B03, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.ma60) continue;

    const sl = data.slice(i - o.boxLookback, i);
    const hi = Math.max(...sl.map(x => x.close));
    const lo = Math.min(...sl.map(x => x.close));
    const rng = (hi - lo) / lo;
    if (rng > o.boxRangeMax) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    if (d.close > hi * (1 + o.breakThreshold)) {
      signals.push({ idx:i, confidence:clamp01(o.baseConfidence+(vr-1)*0.02), type:'BUY', label:'60상하빗↑',
        meta:{boxHi:hi,boxLo:lo,rng:+(rng*100).toFixed(2),volumeRatio:+vr.toFixed(2),dir:'레버리지'} });
    } else if (d.close < lo * (1 - o.breakThreshold)) {
      signals.push({ idx:i, confidence:clamp01(o.baseConfidence+(vr-1)*0.02), type:'SELL', label:'60상하빗↓',
        meta:{boxHi:hi,boxLo:lo,rng:+(rng*100).toFixed(2),volumeRatio:+vr.toFixed(2),dir:'인버스'} });
    }
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B03 = {
  id:'lev_60box', no:'B3', name:'60상하빗 (박스교차)', module:'B', group:'GB-레버',
  color:'#9C27B0', icon:'📦', signal:'BUY', ma:[60],
  desc:'60MA와 수평 박스권 교차. 박스 상단 돌파=레버리지, 하단 이탈=인버스.',
  buy:'박스 상단 돌파 → 레버리지', sell:'박스 하단 이탈 → 인버스',
};


// ─────────────────────────────────────────────
//  B04 : 인서트패턴 (다중 MA 수렴)  (lev_insert)
//  정의: 20/60/120MA 한 점 수렴 → 발산 방향으로 포지션
// ─────────────────────────────────────────────
const OPTIONS_B04 = {
  /** 수렴 확인 MA 목록 */
  convergenceMAs: [20, 60, 120],
  /** 수렴 임계값 (종가 대비 MA 간 최대 스프레드 %) */
  convergeThresholdPct: 0.02,
  /** 과거 스프레드 비교 구간 */
  spreadLookback: 20,
  /** 과거 대비 수렴 비율 */
  convergeRatio: 0.35,
  /** 거래량 배율 */
  volMultiplier: 1.8,
  volumeWindow: 5,
  baseConfidence: 0.80,
  dedupeGap: 10,
  minIdx: 60,
};

function detectB04(data, opts = {}) {
  const o = { ...OPTIONS_B04, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    const maVals = o.convergenceMAs.map(p => d[`ma${p}`]).filter(Boolean);
    if (maVals.length < 2) continue;

    const spread = Math.max(...maVals) - Math.min(...maVals);
    const spreadRatio = spread / d.close;

    // 과거 스프레드
    const past = data[i - o.spreadLookback];
    const pastVals = past ? o.convergenceMAs.map(p => past[`ma${p}`]).filter(Boolean) : [];
    const pastSpread = pastVals.length >= 2
      ? (Math.max(...pastVals) - Math.min(...pastVals)) / past.close
      : spreadRatio;

    if (spreadRatio > pastSpread * o.convergeRatio) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    // 방향: MA20 > MA60 이면 상향
    const dir = d.ma20 && d.ma60 ? (d.ma20 > d.ma60 ? 'BUY' : 'SELL') : 'BUY';

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + (o.convergeThresholdPct - spreadRatio) * 5),
      type: dir,
      label: '인서트패턴',
      meta: { spread: +(spreadRatio*100).toFixed(2), pastSpread: +(pastSpread*100).toFixed(2), dir:dir==='BUY'?'레버리지':'인버스', volumeRatio:+vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B04 = {
  id:'lev_insert', no:'B4', name:'인서트패턴 (수렴)', module:'B', group:'GB-레버',
  color:'#673AB7', icon:'🎯', signal:'BUY', ma:[20,60,120,240],
  desc:'4개 MA 한 점 수렴 후 발산. 수렴 기간이 길수록 발산 후 추세 강도 큼.',
  buy:'수렴 후 상향 발산 + 거래량 급증', sell:'수렴 후 하향 발산 + 거래량 급증',
};


// ─────────────────────────────────────────────
//  B05 : 5파동 (엘리어트 변형)  (lev_5wave)
//  정의: MA20/MA60 교차 5회 → 5번째 방향으로 포지션
// ─────────────────────────────────────────────
const OPTIONS_B05 = {
  /** 파동 탐색 구간 */
  lookback: 50,
  /** 목표 교차 횟수 */
  targetCrosses: 5,
  /** 교차 허용 오차 (±N회) */
  crossTolerance: 0,
  volumeWindow: 5,
  baseConfidence: 0.76,
  dedupeGap: 10,
  minIdx: 20,
};

function detectB05(data, opts = {}) {
  const o = { ...OPTIONS_B05, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    let cnt = 0, lastType = '';
    for (let j = Math.max(1, i - o.lookback); j <= i; j++) {
      if (isGoldenCross(data, j)) { cnt++; lastType = 'gc'; }
      else if (isDeadCross(data, j)) { cnt++; lastType = 'dc'; }
    }
    if (Math.abs(cnt - o.targetCrosses) > o.crossTolerance) continue;
    if (!lastType) continue;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence),
      type: lastType === 'gc' ? 'BUY' : 'SELL',
      label: '5파동',
      meta: { crossCount: cnt, lastType, dir: lastType==='gc'?'레버리지':'인버스' },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B05 = {
  id:'lev_5wave', no:'B5', name:'5파동 (엘리어트)', module:'B', group:'GB-레버',
  color:'#3F51B5', icon:'🌊', signal:'BUY', ma:[20,60],
  desc:'MA20/MA60 교차 5회 완성. 5번째 파동 방향 = 주요 추세 방향.',
  buy:'5번째 골든크로스 → 레버리지', sell:'5번째 데드크로스 → 인버스',
};


// ─────────────────────────────────────────────
//  B06 : 60중빙 (MA60 중간선 교차)  (lev_60mid)
//  정의: MA60이 중기 수평선을 X자 교차 → 방향으로 포지션
// ─────────────────────────────────────────────
const OPTIONS_B06 = {
  /** 피봇(수평선) 산출 구간 */
  pivotLookback: 30,
  /** MA60이 피봇을 교차 허용 오차 */
  crossThreshold: 0.005,
  /** 거래량 배율 */
  volMultiplier: 1.5,
  volumeWindow: 5,
  baseConfidence: 0.70,
  dedupeGap: 5,
  minIdx: 30,
};

function detectB06(data, opts = {}) {
  const o = { ...OPTIONS_B06, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.ma60) continue;

    const pivot = data.slice(i - o.pivotLookback, i)
      .reduce((s, x) => s + x.close, 0) / o.pivotLookback;

    const prev = data[i - 1];
    if (!prev.ma60) continue;

    const vr = volumeRatio(data, i, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    // 상향 교차
    if (prev.ma60 < pivot && d.ma60 > pivot) {
      signals.push({ idx:i, confidence:clamp01(o.baseConfidence+(vr-1)*0.03), type:'BUY', label:'60중빙↑',
        meta:{pivot:+pivot.toFixed(0),ma60:d.ma60,dir:'레버리지',volumeRatio:+vr.toFixed(2)} });
    }
    // 하향 교차
    else if (prev.ma60 > pivot && d.ma60 < pivot) {
      signals.push({ idx:i, confidence:clamp01(o.baseConfidence+(vr-1)*0.03), type:'SELL', label:'60중빙↓',
        meta:{pivot:+pivot.toFixed(0),ma60:d.ma60,dir:'인버스',volumeRatio:+vr.toFixed(2)} });
    }
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B06 = {
  id:'lev_60mid', no:'B6', name:'60중빙 (중간선 교차)', module:'B', group:'GB-레버',
  color:'#2196F3', icon:'✂️', signal:'BUY', ma:[60],
  desc:'60MA와 중기 수평선 X자 교차. 교차 후 벌어지는 방향으로 포지션.',
  buy:'60MA 중간선 상향 돌파', sell:'60MA 중간선 하향 이탈',
};


// ─────────────────────────────────────────────
//  B07 : 롱핑 쌩봉 쌍바닥  (lev_longping)
//  정의: 횡보(롱핑) → 강봉(쌩봉) → 이중바닥 3단계 복합
// ─────────────────────────────────────────────
const OPTIONS_B07 = {
  /** 롱핑 구간 탐색 */
  longpingLookback: 30,
  /** 롱핑 최대 등락폭 */
  longpingRangeMax: 0.06,
  /** 쌩봉 최소 상승률 */
  saengbongMinGain: 0.04,
  /** 쌩봉 탐색 구간 */
  saengbongLookback: 5,
  /** 이중바닥 저점 가격 오차 */
  doublebottomTolerance: 0.03,
  /** 이중바닥 저점 간격 */
  doublebottomGapMin: 8,
  doublebottomGapMax: 30,
  /** 거래량 배율 */
  volMultiplier: 1.5,
  volumeWindow: 5,
  troughWing: 3,
  baseConfidence: 0.82,
  dedupeGap: 8,
};

function detectB07(data, opts = {}) {
  const o = { ...OPTIONS_B07, ...opts };
  const troughs = findTroughs(data, o.troughWing);
  const signals = [];

  for (let t = 1; t < troughs.length; t++) {
    const t1 = troughs[t-1], t2 = troughs[t];
    const gap = t2 - t1;
    if (gap < o.doublebottomGapMin || gap > o.doublebottomGapMax) continue;
    if (Math.abs(data[t1].close - data[t2].close) / data[t1].close > o.doublebottomTolerance) continue;

    // 롱핑 확인 (t1 이전)
    const lpStart = Math.max(0, t1 - o.longpingLookback);
    const lpSlice = data.slice(lpStart, t1);
    if (lpSlice.length < 5) continue;
    const lpHi = Math.max(...lpSlice.map(d=>d.close));
    const lpLo = Math.min(...lpSlice.map(d=>d.close));
    if ((lpHi - lpLo) / lpLo > o.longpingRangeMax) continue;

    // 쌩봉 확인 (t1 직전)
    const sbSlice = data.slice(Math.max(0, t1 - o.saengbongLookback), t1);
    const hasSaengbong = sbSlice.some(d =>
      (d.close - d.open) / d.open > o.saengbongMinGain
    );
    if (!hasSaengbong) continue;

    // t2 거래량
    const vr = volumeRatio(data, t2, o.volumeWindow);
    if (vr < o.volMultiplier) continue;

    signals.push({
      idx: t2, idx2: t1,
      confidence: clamp01(o.baseConfidence + (vr - 1) * 0.02),
      type: 'BUY',
      label: '롱핑쌩봉',
      meta: { t1, t2, lpRange: +((lpHi-lpLo)/lpLo*100).toFixed(2), volumeRatio: +vr.toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B07 = {
  id:'lev_longping', no:'B7', name:'롱핑 쌩봉 쌍바닥', module:'B', group:'GB-레버',
  color:'#00BCD4', icon:'🎸', signal:'BUY', ma:[20,60],
  desc:'장기횡보(롱핑) → 강봉(쌩봉) → 이중바닥 3단계 복합. 2번째 저점 거래량 증가 시 레버리지.',
  buy:'이중바닥 2번째 저점 거래량↑ → 레버리지', sell:'이중바닥 저점 하향 이탈',
};


// ─────────────────────────────────────────────
//  B08 : 갈매기패턴 (대칭 수렴)  (lev_seagull)
//  정의: 이동평균 날개형 수렴 → 저점 수렴=레버리지, 고점 수렴=인버스
// ─────────────────────────────────────────────
const OPTIONS_B08 = {
  /** 트로프 탐색 wing */
  troughWing: 3,
  /** 두 트로프 간격 */
  troughGapMin: 10,
  troughGapMax: 40,
  /** 날개 대칭 허용 오차 */
  wingSymmetryTolerance: 0.025,
  /** 날개 최소 상승률 */
  wingMinRise: 0.02,
  /** 날개 확인 구간 */
  wingBars: 5,
  volumeWindow: 5,
  baseConfidence: 0.74,
  dedupeGap: 8,
};

function detectB08(data, opts = {}) {
  const o = { ...OPTIONS_B08, ...opts };
  const troughs = findTroughs(data, o.troughWing);
  const signals = [];

  for (let t = 1; t < troughs.length; t++) {
    const t1 = troughs[t-1], t2 = troughs[t];
    if (t2 - t1 < o.troughGapMin || t2 - t1 > o.troughGapMax) continue;

    const leftPre  = Math.max(0, t1 - o.wingBars);
    const rightPost = Math.min(data.length - 1, t2 + o.wingBars);
    const leftWing  = (data[t1].close - data[leftPre].close) / data[t1].close;
    const rightWing = (data[Math.min(rightPost, data.length-1)].close - data[t2].close) / data[t2].close;

    if (leftWing < o.wingMinRise || rightWing < o.wingMinRise) continue;

    const symmetry = Math.abs(leftWing - rightWing);
    if (symmetry > o.wingSymmetryTolerance) continue;

    signals.push({
      idx: t2, idx2: t1,
      confidence: clamp01(o.baseConfidence + (o.wingSymmetryTolerance - symmetry) * 5),
      type: 'BUY',
      label: '갈매기',
      meta: { t1, t2, leftWing: +(leftWing*100).toFixed(2), rightWing: +(rightWing*100).toFixed(2), symmetry: +(symmetry*100).toFixed(2) },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B08 = {
  id:'lev_seagull', no:'B8', name:'갈매기패턴 (수렴)', module:'B', group:'GB-레버',
  color:'#009688', icon:'🐦', signal:'BUY', ma:[60],
  desc:'MA 갈매기 날개형 수렴. 저점 수렴=레버리지, 고점 수렴=인버스 진입.',
  buy:'저점 수렴 확인 → 레버리지', sell:'고점 수렴 확인 → 인버스',
};


// ─────────────────────────────────────────────
//  B09 : 포캐스팅 20런60런  (lev_forecast)
//  정의: MA20 상승런(1단계) + MA60 상승런(2단계) 순차 완성 → 레버리지
// ─────────────────────────────────────────────
const OPTIONS_B09 = {
  /** MA20 상승 런 확인 구간 */
  ma20RunBars: 5,
  /** MA60 상승 런 확인 구간 */
  ma60RunBars: 10,
  /** MA 상승 기울기 최소값 */
  minSlope: 0,
  /** 240MA 돌파 확인 여부 */
  require240MABreak: false,
  /** 현가 > MA20 > MA60 정배열 필수 */
  requireBullAlign: true,
  volumeWindow: 5,
  baseConfidence: 0.81,
  dedupeGap: 8,
  minIdx: 10,
};

function detectB09(data, opts = {}) {
  const o = { ...OPTIONS_B09, ...opts };
  const signals = [];

  for (let i = o.minIdx; i < data.length; i++) {
    const d = data[i];
    if (!d.ma20 || !d.ma60) continue;

    // MA20 상승 런
    const ma20Rising = data.slice(i - o.ma20RunBars, i + 1)
      .every((x, j, a) => j === 0 || (!a[j-1].ma20 || (x.ma20 != null && x.ma20 >= a[j-1].ma20)));

    // MA60 상승 런
    const ma60Rising = data.slice(Math.max(0, i - o.ma60RunBars), i + 1)
      .every((x, j, a) => j === 0 || (!a[j-1].ma60 || (x.ma60 != null && x.ma60 >= a[j-1].ma60)));

    if (!ma20Rising || !ma60Rising) continue;

    // 정배열 확인
    if (o.requireBullAlign && !(d.close > d.ma20 && d.ma20 > d.ma60)) continue;

    // 240MA 돌파 확인 (선택)
    if (o.require240MABreak && d.ma240 && d.close <= d.ma240) continue;

    // 런 단계 판단
    const stage = (ma20Rising && ma60Rising) ? 2 : 1;

    signals.push({
      idx: i,
      confidence: clamp01(o.baseConfidence + stage * 0.03),
      type: 'BUY',
      label: `포캐스팅런${stage}`,
      meta: { stage, ma20: d.ma20, ma60: d.ma60, ma240: d.ma240, dir:'레버리지' },
    });
  }
  return deduplicateSignals(signals, o.dedupeGap);
}

const META_B09 = {
  id:'lev_forecast', no:'B9', name:'포캐스팅 20런60런', module:'B', group:'GB-레버',
  color:'#4CAF50', icon:'🔭', signal:'BUY', ma:[20,60,240],
  desc:'MA20 상승런(1단계) + MA60 상승런(2단계) 순차 완성. 240MA 돌파가 최종 확인.',
  buy:'MA20런 완성=1차 레버리지, MA60런 완성=추가 매수', sell:'MA60 기울기 꺾임 + 240MA 이탈',
};


/* ─── PATTERN_REGISTRY ─── */
var PATTERN_REGISTRY = [
  { meta: META_A01, detect: detectA01, defaultOpts: OPTIONS_A01 },
  { meta: META_A02, detect: detectA02, defaultOpts: OPTIONS_A02 },
  { meta: META_A03, detect: detectA03, defaultOpts: OPTIONS_A03 },
  { meta: META_A04, detect: detectA04, defaultOpts: OPTIONS_A04 },
  { meta: META_A05, detect: detectA05, defaultOpts: OPTIONS_A05 },
  { meta: META_A06, detect: detectA06, defaultOpts: OPTIONS_A06 },
  { meta: META_A07, detect: detectA07, defaultOpts: OPTIONS_A07 },
  { meta: META_A08, detect: detectA08, defaultOpts: OPTIONS_A08 },
  { meta: META_A09, detect: detectA09, defaultOpts: OPTIONS_A09 },
  { meta: META_A10, detect: detectA10, defaultOpts: OPTIONS_A10 },
  { meta: META_A11, detect: detectA11, defaultOpts: OPTIONS_A11 },
  { meta: META_A12, detect: detectA12, defaultOpts: OPTIONS_A12 },
  { meta: META_A13, detect: detectA13, defaultOpts: OPTIONS_A13 },
  { meta: META_A14, detect: detectA14, defaultOpts: OPTIONS_A14 },
  { meta: META_A15, detect: detectA15, defaultOpts: OPTIONS_A15 },
  { meta: META_A16, detect: detectA16, defaultOpts: OPTIONS_A16 },
  { meta: META_A17, detect: detectA17, defaultOpts: OPTIONS_A17 },
  { meta: META_A18, detect: detectA18, defaultOpts: OPTIONS_A18 },
  { meta: META_A19, detect: detectA19, defaultOpts: OPTIONS_A19 },
  { meta: META_A20, detect: detectA20, defaultOpts: OPTIONS_A20 },
  { meta: META_A21, detect: detectA21, defaultOpts: OPTIONS_A21 },
  { meta: META_A22, detect: detectA22, defaultOpts: OPTIONS_A22 },
  { meta: META_A23, detect: detectA23, defaultOpts: OPTIONS_A23 },
  { meta: META_A24, detect: detectA24, defaultOpts: OPTIONS_A24 },
  { meta: META_A25, detect: detectA25, defaultOpts: OPTIONS_A25 },
  { meta: META_A26, detect: detectA26, defaultOpts: OPTIONS_A26 },
  { meta: META_A27, detect: detectA27, defaultOpts: OPTIONS_A27 },
  { meta: META_A28, detect: detectA28, defaultOpts: OPTIONS_A28 },
  { meta: META_A29, detect: detectA29, defaultOpts: OPTIONS_A29 },
  { meta: META_A30, detect: detectA30, defaultOpts: OPTIONS_A30 },
  { meta: META_A31, detect: detectA31, defaultOpts: OPTIONS_A31 },
  { meta: META_A32, detect: detectA32, defaultOpts: OPTIONS_A32 },
  { meta: META_A33, detect: detectA33, defaultOpts: OPTIONS_A33 },
  { meta: META_B01, detect: detectB01, defaultOpts: OPTIONS_B01 },
  { meta: META_B02, detect: detectB02, defaultOpts: OPTIONS_B02 },
  { meta: META_B03, detect: detectB03, defaultOpts: OPTIONS_B03 },
  { meta: META_B04, detect: detectB04, defaultOpts: OPTIONS_B04 },
  { meta: META_B05, detect: detectB05, defaultOpts: OPTIONS_B05 },
  { meta: META_B06, detect: detectB06, defaultOpts: OPTIONS_B06 },
  { meta: META_B07, detect: detectB07, defaultOpts: OPTIONS_B07 },
  { meta: META_B08, detect: detectB08, defaultOpts: OPTIONS_B08 },
  { meta: META_B09, detect: detectB09, defaultOpts: OPTIONS_B09 },
];

/* ─── v2 수정/신규 패턴 functions ─── */

// ✅ FIX: findTroughsTrue — close 대신 low 기준
function findTroughsTrue(data, wing=3){
  const out=[];
  for(let i=wing;i<data.length-wing;i++){
    const cur=data[i].low;
    if(data.slice(i-wing,i).every(d=>d.low>=cur)&&data.slice(i+1,i+wing+1).every(d=>d.low>=cur))out.push(i);
  }
  return out;
}
// ✅ FIX: periodHighTrue — close 대신 high 기준
function periodHighTrue(data,from,to){return Math.max(...data.slice(from,to).map(d=>d.high));}
// ✅ NEW: 양봉 반등 캔들 여부
function isBullCandle(d){const r=d.high-d.low;if(r<=0)return false;const lw=(Math.min(d.open,d.close)-d.low)/r;return d.close>d.open||lw>0.5;}
// ✅ NEW: RSI 과매도 여부
function isOversold(d,thr=35){return d.rsi!=null&&d.rsi<thr;}

// ── v2-FIX 1: 진골드 눌림목 v2 (양봉캔들 + high 기준) ──────────────
function detectA01v2(data,opts={}){
  const o={lookbackBars:10,dropMin:0.03,dropMax:0.12,ma20Tolerance:0.015,volumeMultiplier:1.3,volumeWindow:5,dedupeGap:5,minIdx:20,...opts};
  const sigs=[];
  for(let i=o.minIdx;i<data.length-3;i++){
    const d=data[i];
    if(!d.ma20||!d.ma60||d.ma20<=d.ma60)continue;
    const prevHigh=periodHighTrue(data,i-o.lookbackBars,i);   // ✅ high 기준
    const drop=(prevHigh-d.low)/prevHigh;                      // ✅ low 기준 낙폭
    if(drop<o.dropMin||drop>o.dropMax)continue;
    if(Math.abs(d.close-d.ma20)/d.ma20>o.ma20Tolerance)continue;
    if(!isBullCandle(d))continue;                              // ✅ 양봉 캔들 필수
    const vr=volumeRatio(data,i,o.volumeWindow);
    if(vr<o.volumeMultiplier)continue;
    sigs.push({idx:i,confidence:clamp01(0.70+drop*0.5+(vr-1)*0.05),type:'BUY',label:'진골드v2',meta:{drop:+(drop*100).toFixed(2),rsi:d.rsi,pp:d.pp}});
  }
  return deduplicateSignals(sigs,o.dedupeGap);
}
const META_A01v2={id:'A01v2',no:'v2-1',name:'진골드 눌림목 v2',module:'A',group:'G1-눌림목',color:'#FFD700',icon:'🔧',signal:'BUY',ma:[20,60],desc:'v1 수정: 전고점 high 기준 + 양봉 반등 캔들 필수',buy:'MA20 ±1.5% + 정배열 + 양봉 + 거래량↑',sell:'MA20 이탈'};

// ── v2-FIX 2: W쌍바닥 v2 (low 저점 + high 넥라인) ──────────────────
function detectA05v2(data,opts={}){
  const o={troughGapMin:10,troughGapMax:40,troughPriceTol:0.03,neckBreakThr:0.005,volMul:2.0,volWindow:5,troughWing:3,dedupeGap:5,...opts};
  const troughs=findTroughsTrue(data,o.troughWing);          // ✅ low 기준 저점
  const sigs=[];
  for(let t=1;t<troughs.length;t++){
    const t1=troughs[t-1],t2=troughs[t];
    if(t2-t1<o.troughGapMin||t2-t1>o.troughGapMax)continue;
    const p1=data[t1].low,p2=data[t2].low;                  // ✅ low 기준
    if(Math.abs(p1-p2)/p1>o.troughPriceTol)continue;
    const neckline=periodHighTrue(data,t1,t2+1);             // ✅ high 기준 넥라인
    const bi=t2+1;if(bi>=data.length)continue;
    if(data[bi].close<=neckline*(1+o.neckBreakThr))continue;
    const avgVol=data.slice(Math.max(0,bi-o.volWindow),bi).reduce((s,d)=>s+d.volume,0)/o.volWindow||1;
    const vr=data[bi].volume/avgVol;if(vr<o.volMul)continue;
    sigs.push({idx:bi,idx2:t1,confidence:clamp01(0.85+vr*0.02),type:'BUY',label:'쌍바닥v2',meta:{neckline,vr:+vr.toFixed(2),rsi:data[bi].rsi,pp:data[bi].pp}});
  }
  return deduplicateSignals(sigs,o.dedupeGap);
}
const META_A05v2={id:'A05v2',no:'v2-2',name:'W쌍바닥 v2',module:'A',group:'G1-눌림목',color:'#4CAF50',icon:'🔧',signal:'BUY',ma:[20,60],desc:'v1 수정: 저점 low 기준 + 넥라인 high 기준',buy:'넥라인(high) 돌파 + 거래량 2배',sell:'넥라인 하향 복귀'};

// ── v2-FIX 3: -7% 공략 v2 (RSI 35 이하 추가) ──────────────────────
function detectA28v2(data,opts={}){
  const o={dropMin:0.065,dropMax:0.09,rsiMax:35,dedupeGap:5,...opts};
  const sigs=[];
  for(let i=15;i<data.length;i++){
    const d=data[i];
    const drop=(data[i-1].close-d.close)/data[i-1].close;
    if(drop<o.dropMin||drop>o.dropMax)continue;
    if(d.ma20&&d.ma60&&d.ma20<=d.ma60)continue;
    if(!isOversold(d,o.rsiMax))continue;                     // ✅ RSI 35 이하
    sigs.push({idx:i,confidence:clamp01(0.73-(drop-o.dropMin)*5+(d.rsi?(o.rsiMax-d.rsi)/50:0)),type:'BUY',label:'-7%RSI',meta:{drop:+(drop*100).toFixed(2),rsi:d.rsi,pp:d.pp}});
  }
  return deduplicateSignals(sigs,o.dedupeGap);
}
const META_A28v2={id:'A28v2',no:'v2-3',name:'-7% 공략 v2 (RSI)',module:'A',group:'G6-전략',color:'#66BB6A',icon:'🔧',signal:'BUY',ma:[20,60],desc:'v1 수정: RSI(14) < 35 과매도 조건 추가 (설계서 필수지표)',buy:'-7% 하락 + 정배열 + RSI<35',sell:'-10% 추가 하락'};

// ── v2-NEW 1: 블랙홀 SD — SELL (설계서 #19 원래 의도) ──────────────
function detectBlackholeSD(data,opts={}){
  const o={volMul:1.5,volWindow:5,dedupeGap:5,...opts};
  const sigs=[];
  for(let i=1;i<data.length;i++){
    if(!isDeadCross(data,i))continue;
    const d=data[i];
    const vr=volumeRatio(data,i,o.volWindow);if(vr<o.volMul)continue;
    if(d.ma20&&d.close>d.ma20*0.98)continue;
    sigs.push({idx:i,confidence:clamp01(0.82+(vr-1.5)*0.03),type:'SELL',label:'블랙홀SD',meta:{ma20:d.ma20,vr:+vr.toFixed(2),rsi:d.rsi}});
  }
  return deduplicateSignals(sigs,o.dedupeGap);
}
const META_BhSD={id:'blackhole_sd',no:'v2-N1',name:'블랙홀 SD (SELL)',module:'A',group:'G6-전략',color:'#F43F5E',icon:'✨',signal:'SELL',ma:[20,60],desc:'설계서 #19 신규구현: MA20/MA60 데드크로스 + 거래량 급증',buy:'없음',sell:'데드크로스 + 거래량 1.5배 + 이평선 하방이탈'};

// ── v2-NEW 2: 피벗도형 BUY (설계서 #27 원래 의도) ──────────────────
function detectPivotBreak(data,opts={}){
  const o={volMul:1.3,volWindow:5,dedupeGap:5,...opts};
  const sigs=[];
  for(let i=2;i<data.length;i++){
    const d=data[i];if(!d.pp)continue;
    if(data[i-1].close>d.pp)continue;
    if(d.close<=d.pp)continue;
    if(!isBullCandle(d))continue;
    const vr=volumeRatio(data,i,o.volWindow);if(vr<o.volMul)continue;
    sigs.push({idx:i,confidence:clamp01(0.75+(vr-1)*0.04),type:'BUY',label:'피벗돌파',meta:{pp:d.pp,r1:d.r1,s1:d.s1,rsi:d.rsi,vr:+vr.toFixed(2)}});
  }
  return deduplicateSignals(sigs,o.dedupeGap);
}
const META_PivotBrk={id:'pivot_break',no:'v2-N2',name:'피벗도형 (PP돌파)',module:'A',group:'G6-전략',color:'#A78BFA',icon:'✨',signal:'BUY',ma:[20,60],desc:'설계서 #27 신규구현: Pivot Point = (고+저+종)/3 하향→상향 돌파',buy:'PP 상향 돌파 + 양봉 + 거래량↑',sell:'PP 재이탈'};

// ── v2-NEW 3: 60TT BUY v2 (설계서 L2: 가격이 MA60 터치 2회) ────────
function detectB02v2(data,opts={}){
  const o={lookback:40,touchTol:0.03,minGap:5,volMul:1.3,volWindow:5,dedupeGap:8,...opts};
  const sigs=[];
  for(let i=30;i<data.length;i++){
    const touches=[];
    for(let j=Math.max(0,i-o.lookback);j<i;j++){
      const dd=data[j];if(!dd.ma60)continue;
      if(Math.abs(dd.low-dd.ma60)/dd.ma60<o.touchTol&&dd.low>dd.ma60*(1-o.touchTol))touches.push({j,low:dd.low,vol:dd.volume});
    }
    if(touches.length<2)continue;
    const t1=touches[touches.length-2],t2=touches[touches.length-1];
    if(t2.j-t1.j<o.minGap)continue;
    if(data[t2.j].volume>data[t1.j].volume)continue;       // 2차 거래량 감소
    const d=data[i];if(!d.ma60||d.close<d.ma60)continue;
    if(!isBullCandle(d))continue;
    const vr=volumeRatio(data,i,o.volWindow);if(vr<o.volMul)continue;
    sigs.push({idx:i,confidence:clamp01(0.80+(vr-1)*0.03),type:'BUY',label:'60TT_BUY',meta:{t1:t1.j,t2:t2.j,rsi:d.rsi,vr:+vr.toFixed(2)}});
  }
  return deduplicateSignals(sigs,o.dedupeGap);
}
const META_B02v2={id:'lev_60tt_buy',no:'v2-N3',name:'60TT BUY v2',module:'B',group:'GB-레버',color:'#38BDF8',icon:'✨',signal:'BUY',ma:[60],desc:'설계서 L2 원래 BUY 신호 복원: 가격이 MA60 두번 터치 후 반등',buy:'MA60 2차 터치 + 거래량 감소 + 반등 양봉',sell:'MA60 이탈'};

// ── v2 패턴들을 PATTERN_REGISTRY에 동적 등록 ─────────────────────────
(function registerV2Patterns(){
  const v2List=[
    {meta:META_A01v2,detect:detectA01v2,defaultOpts:{}},
    {meta:META_A05v2,detect:detectA05v2,defaultOpts:{}},
    {meta:META_A28v2,detect:detectA28v2,defaultOpts:{}},
    {meta:META_BhSD, detect:detectBlackholeSD,defaultOpts:{}},
    {meta:META_PivotBrk,detect:detectPivotBreak,defaultOpts:{}},
    {meta:META_B02v2,detect:detectB02v2,defaultOpts:{}},
  ];
  v2List.forEach(p=>PATTERN_REGISTRY.push(p));
})();

function runAllPatterns(data, customOpts) {
  customOpts = customOpts || {};
  var results = {};
  for (var i = 0; i < PATTERN_REGISTRY.length; i++) {
    var entry = PATTERN_REGISTRY[i];
    var id = entry.meta.id;
    var opts = Object.assign({}, entry.defaultOpts, customOpts[id] || {});
    try { results[id] = entry.detect(data, opts); }
    catch(e) { console.warn('[패턴오류]', id, e.message); results[id] = []; }
  }
  return results;
}

/* ─── app.js ─── */
/* ═══════════════════════════════════════════════════════════════════
   KJC 42패턴 주식 분석 대시보드 — app.js
   삼성전자 (005930.KS) · 2024.01 ~ 2025.12 · 504 거래일
   ※ ES Module — <script type="module" src="app.js"> 로 로드
   ═══════════════════════════════════════════════════════════════════ */

// ── 0. 패턴 레지스트리 import ─────────────────────────────────────
// ── 1. KIS 데이터 연동 ─────────────────────────────────────────────
const APP_CTX = (window.PatternBootstrap && window.PatternBootstrap.appCtx) || '';
const API_CONFIG = {
  search: "/finance/searchStocksKeyword.do",
  chart: "/finance/kisItemchartpriceData.do",
  currentPrice: "/finance/getCurrentPriceByInquirePrice.do",
  watchWs: "/finance/watchlistRealtime.ws",
  chartOptionData: "/finance/kisItemchartpriceOptionData.do",
  chartOptionSave: "/finance/kisItemchartpriceOptionSave.do"
};
const THEME_STORAGE_KEY = "patternDashboardTheme";
const THEME_MEDIA_QUERY = typeof window !== "undefined" && typeof window.matchMedia === "function"
  ? window.matchMedia("(prefers-color-scheme: dark)")
  : null;
const DEFAULT_STOCK = {
  stock_code: "005930",
  stock_ko_name: "삼성전자",
  stock_market: "KOSPI",
  stock_country_code: "KR"
};

function resolveSystemTheme() {
  return THEME_MEDIA_QUERY && THEME_MEDIA_QUERY.matches ? "dark" : "light";
}

function readStoredTheme() {
  try {
    const saved = window.localStorage.getItem(THEME_STORAGE_KEY);
    return saved === "dark" || saved === "light" ? saved : "";
  } catch (error) {
    return "";
  }
}

function persistTheme(theme) {
  try {
    if (!theme) {
      window.localStorage.removeItem(THEME_STORAGE_KEY);
      return;
    }
    window.localStorage.setItem(THEME_STORAGE_KEY, theme);
  } catch (error) {}
}

function isDarkTheme() {
  return state.currentTheme !== "light";
}

function getThemePalette() {
  if (isDarkTheme()) {
    return {
      chartBg: "#050A14",
      grid: "#1B2E4B",
      axis: "#475569",
      axisDim: "#334155",
      yearMark: "#38BDF860",
      yearLine: "#38BDF820",
      monthLine: "#1B2E4B",
      crosshair: "#38BDF8",
      crosshairBg: "#132240",
      volLabel: "#475569",
      monthLabel: "#334155"
    };
  }
  return {
    chartBg: "#FFFFFF",
    grid: "#D7E2EF",
    axis: "#5F6F82",
    axisDim: "#94A3B8",
    yearMark: "#0F6ADF80",
    yearLine: "#0F6ADF26",
    monthLine: "#D7E2EF",
    crosshair: "#0F6ADF",
    crosshairBg: "#EAF2FC",
    volLabel: "#7B8794",
    monthLabel: "#7B8794"
  };
}

function applyTheme(theme) {
  const body = document.body;
  if (!body) return;
  const nextTheme = theme === "dark" ? "dark" : "light";
  body.classList.remove("theme-light", "theme-dark");
  body.classList.add(nextTheme === "dark" ? "theme-dark" : "theme-light");
  state.currentTheme = nextTheme;

  const indicator = document.getElementById("theme-mode-indicator");
  if (indicator) {
    const sourceLabel = state.themeSource === "manual" ? "수동" : "시스템";
    const currentLabel = nextTheme === "dark" ? "다크 모드" : "라이트 모드";
    const nextLabel = nextTheme === "dark" ? "라이트 모드" : "다크 모드";
    const tooltip = sourceLabel + " " + currentLabel + " 사용 중 · 클릭 시 " + nextLabel + "로 변경";
    indicator.setAttribute("title", tooltip);
    indicator.setAttribute("aria-label", tooltip);
    indicator.setAttribute("aria-pressed", nextTheme === "dark" ? "true" : "false");
  }
}

function rerenderThemeSensitiveUi() {
  updateHeader();
  renderSidebar();
  showTab(state.activeTab);
}

function bindSystemTheme() {
  const storedTheme = readStoredTheme();
  if (storedTheme) {
    state.themeSource = "manual";
    applyTheme(storedTheme);
  } else {
    state.themeSource = "system";
    applyTheme(resolveSystemTheme());
  }

  const indicator = document.getElementById("theme-mode-indicator");
  if (indicator) {
    indicator.addEventListener("click", function () {
      const nextTheme = state.currentTheme === "dark" ? "light" : "dark";
      state.themeSource = "manual";
      persistTheme(nextTheme);
      applyTheme(nextTheme);
      rerenderThemeSensitiveUi();
    });
  }

  if (!THEME_MEDIA_QUERY) return;

  const onThemeChange = function () {
    if (readStoredTheme()) return;
    state.themeSource = "system";
    applyTheme(resolveSystemTheme());
    rerenderThemeSensitiveUi();
  };

  if (typeof THEME_MEDIA_QUERY.addEventListener === "function") {
    THEME_MEDIA_QUERY.addEventListener("change", onThemeChange);
  } else if (typeof THEME_MEDIA_QUERY.addListener === "function") {
    THEME_MEDIA_QUERY.addListener(onThemeChange);
  }
}

function pad2(n) { return String(n).padStart(2, "0"); }

function formatYmd(date) {
  return date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate());
}

function getDefaultChartRange() {
  const end = new Date();
  const start = new Date(end.getTime());
  start.setFullYear(end.getFullYear() - 2);
  return { from: formatYmd(start), to: formatYmd(end) };
}

function buildAppUrl(path) {
  if (!path) return APP_CTX || "";
  if (/^https?:\/\//i.test(path)) return path;
  return (APP_CTX || "") + path;
}

function buildWsUrl(path, query) {
  const proto = location.protocol === "https:" ? "wss://" : "ws://";
  let url = proto + location.host + buildAppUrl(path);
  if (query) {
    url += (url.indexOf("?") >= 0 ? "&" : "?") + query;
  }
  return url;
}

function escapeHtml(value) {
  return String(value == null ? "" : value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function toNumber(value) {
  if (value == null || value === "") return NaN;
  if (typeof value === "number") return value;
  const normalized = String(value).replace(/,/g, "");
  const parsed = parseFloat(normalized);
  return Number.isFinite(parsed) ? parsed : NaN;
}

function toEpochMillis(value) {
  if (value == null || value === "") return NaN;
  if (typeof value === "number") {
    return value < 1000000000000 ? value * 1000 : value;
  }
  const raw = String(value).trim();
  if (/^\d+$/.test(raw)) {
    if (raw.length === 8) {
      const year = parseInt(raw.slice(0, 4), 10);
      const month = parseInt(raw.slice(4, 6), 10) - 1;
      const day = parseInt(raw.slice(6, 8), 10);
      return new Date(year, month, day).getTime();
    }
    const numeric = parseInt(raw, 10);
    return numeric < 1000000000000 ? numeric * 1000 : numeric;
  }
  const parsed = Date.parse(raw);
  return Number.isFinite(parsed) ? parsed : NaN;
}

function unwrapListResponse(payload) {
  if (!payload) return [];
  if (Array.isArray(payload.data)) return payload.data;
  if (payload.data && Array.isArray(payload.data.data)) return payload.data.data;
  if (Array.isArray(payload.list)) return payload.list;
  return [];
}

function unwrapSingleResponse(payload) {
  if (!payload) return null;
  if (payload.data && payload.data.singleData) return payload.data.singleData;
  if (payload.data && payload.data.data && payload.data.data.singleData) return payload.data.data.singleData;
  if (payload.singleData) return payload.singleData;
  return null;
}

function normalizeStock(raw) {
  const stock = raw || {};
  return {
    stock_code: String(stock.stock_code || stock.stockCode || stock.code || DEFAULT_STOCK.stock_code).trim().toUpperCase(),
    stock_ko_name: String(stock.stock_ko_name || stock.stockKoName || stock.stock_name || stock.name || DEFAULT_STOCK.stock_ko_name).trim(),
    stock_market: String(stock.stock_market || stock.stockMarket || stock.market || stock.market_section || DEFAULT_STOCK.stock_market).trim(),
    stock_country_code: String(stock.stock_country_code || stock.stockCountryCode || stock.country || stock.country_code || DEFAULT_STOCK.stock_country_code).trim().toUpperCase()
  };
}

function getMarketLabel(stock) {
  const market = String((stock && stock.stock_market) || "").trim().toUpperCase();
  if (market === "KOSPI") return "코스피";
  if (market === "KOSDAQ") return "코스닥";
  if (market === "KRX") return "KRX";
  if (market === "NASDAQ" || market === "NAS") return "나스닥";
  if (market === "NYSE" || market === "NYS") return "NYSE";
  if (market === "AMEX" || market === "AMS") return "AMEX";
  return market || "-";
}

function getSelectedStockName() {
  return (state.selectedStock && state.selectedStock.stock_ko_name) || DEFAULT_STOCK.stock_ko_name;
}

function getSelectedStockCode() {
  return (state.selectedStock && state.selectedStock.stock_code) || DEFAULT_STOCK.stock_code;
}

function getRangeSummary() {
  if (!ALL_DATA.length) return "KIS 차트 데이터 대기";
  const first = ALL_DATA[0].ts;
  const last = ALL_DATA[ALL_DATA.length - 1].ts;
  const firstDate = new Date(first);
  const lastDate = new Date(last);
  return firstDate.getFullYear() + "." + pad2(firstDate.getMonth() + 1) + "~" +
    lastDate.getFullYear() + "." + pad2(lastDate.getMonth() + 1) + " · " +
    ALL_DATA.length + "거래일";
}

function getWsStatusMarkup() {
  if (state.wsStatus === "connected") {
    return "<span style='color:#4ADE80'>실시간 WS 연결됨</span>";
  }
  if (state.wsStatus === "connecting") {
    return "<span style='color:#F59E0B'>실시간 WS 연결 중</span>";
  }
  if (state.wsStatus === "reconnecting") {
    return "<span style='color:#F59E0B'>실시간 WS 재연결 중</span>";
  }
  if (state.wsStatus === "error") {
    return "<span style='color:#F87171'>실시간 WS 연결 실패</span>";
  }
  return "<span style='color:#64748B'>실시간 WS 대기</span>";
}

function updateHeaderSubline() {
  const el = document.getElementById("header-subline");
  if (!el) return;
  const name = escapeHtml(getSelectedStockName());
  const code = escapeHtml(getSelectedStockCode());
  let summary = getRangeSummary();
  if (state.dataStatus === "loading") {
    summary = "KIS 차트 데이터 로딩 중";
  } else if (state.dataStatus === "error" && state.errorMessage) {
    summary = state.errorMessage;
  }
  el.innerHTML = name + " (" + code + ") · " + escapeHtml(summary) + " · " + getWsStatusMarkup();
}

async function fetchJson(path, params) {
  const query = new URLSearchParams();
  Object.keys(params || {}).forEach(function (key) {
    const value = params[key];
    if (value == null || value === "") return;
    query.set(key, value);
  });
  const requestUrl = buildAppUrl(path) + (query.toString() ? ("?" + query.toString()) : "");
  const response = await fetch(requestUrl, {
    method: "GET",
    credentials: "same-origin",
    headers: { "Accept": "application/json" }
  });
  if (!response.ok) {
    throw new Error("HTTP " + response.status + " 응답 오류");
  }
  return response.json();
}

function normalizeChartBars(list) {
  const rawItems = Array.isArray(list) ? list : [];
  const data = rawItems.map(function (item) {
    const ts = toEpochMillis(item.date || item.stock_date || item.trade_date || item.datetime || item.bsop_date);
    const open = toNumber(item.open || item.stock_open || item.stck_oprc);
    const high = toNumber(item.high || item.stock_high || item.stck_hgpr);
    const low = toNumber(item.low || item.stock_low || item.stck_lwpr);
    const close = toNumber(item.close || item.stock_close || item.stck_clpr);
    const volume = toNumber(item.volume || item.stock_volume || item.acml_vol || item.acmlVol);
    if (!Number.isFinite(ts) || !Number.isFinite(open) || !Number.isFinite(high) || !Number.isFinite(low) || !Number.isFinite(close)) {
      return null;
    }
    return {
      ts: ts,
      open: Math.round(open),
      high: Math.round(high),
      low: Math.round(low),
      close: Math.round(close),
      volume: Number.isFinite(volume) ? Math.round(volume) : 0
    };
  }).filter(Boolean).sort(function (a, b) {
    return a.ts - b.ts;
  });

  data.forEach(function (bar, index) {
    const date = new Date(bar.ts);
    const prevClose = index > 0 ? data[index - 1].close : bar.close;
    bar.idx = index;
    bar.date = (date.getMonth() + 1) + "/" + date.getDate();
    bar.monthLabel = date.getFullYear() + "." + pad2(date.getMonth() + 1);
    bar.yearLabel = String(date.getFullYear());
    bar.change = index > 0 ? bar.close - prevClose : 0;
    bar.changePct = index > 0 && prevClose ? (bar.change / prevClose) * 100 : 0;
  });

  const closes = data.map(function (item) { return item.close; });
  recomputeChartMaSeries(data);

  for (let i = 19; i < data.length; i++) {
    const slice = data.slice(i - 19, i + 1);
    const mean = slice.reduce(function (sum, item) { return sum + item.close; }, 0) / 20;
    const variance = slice.reduce(function (sum, item) { return sum + Math.pow(item.close - mean, 2); }, 0) / 20;
    const std = Math.sqrt(variance);
    data[i].bbU = Math.round(mean + 2 * std);
    data[i].bbL = Math.round(mean - 2 * std);
    data[i].bbM = Math.round(mean);
  }

  if (closes.length > 14) {
    let avgGain = 0;
    let avgLoss = 0;
    for (let i = 1; i <= 14; i++) {
      const diff = closes[i] - closes[i - 1];
      if (diff > 0) avgGain += diff;
      else avgLoss += Math.abs(diff);
    }
    avgGain /= 14;
    avgLoss /= 14;
    data[14].rsi = avgLoss === 0 ? 100 : +(100 - 100 / (1 + avgGain / avgLoss)).toFixed(2);
    for (let i = 15; i < data.length; i++) {
      const diff = closes[i] - closes[i - 1];
      avgGain = (avgGain * 13 + Math.max(diff, 0)) / 14;
      avgLoss = (avgLoss * 13 + Math.max(-diff, 0)) / 14;
      data[i].rsi = avgLoss === 0 ? 100 : +(100 - 100 / (1 + avgGain / avgLoss)).toFixed(2);
    }
  }

  for (let i = 1; i < data.length; i++) {
    const prev = data[i - 1];
    const pp = (prev.high + prev.low + prev.close) / 3;
    data[i].pp = Math.round(pp);
    data[i].r1 = Math.round(2 * pp - prev.low);
    data[i].r2 = Math.round(pp + (prev.high - prev.low));
    data[i].s1 = Math.round(2 * pp - prev.high);
    data[i].s2 = Math.round(pp - (prev.high - prev.low));
  }

  return data;
}

function getRealtimeToken(stock) {
  const item = normalizeStock(stock);
  if (item.stock_country_code === "KR") {
    const market = item.stock_market || "KRX";
    return [item.stock_country_code, market, item.stock_code].join("|");
  }
  const market = item.stock_market || "NAS";
  return [item.stock_country_code, market, item.stock_code].join("|");
}

function setDashboardError(message) {
  ALL_DATA = [];
  state.liveQuote = null;
  state.detected = detectAll(ALL_DATA, state.customOpts);
  state.stats = calcStats();
  state.zoomRange = [0, 0];
  state.activeZoom = DEFAULT_ZOOM_PRESET.label;
  state.hoverIdx = null;
  state.dataStatus = "error";
  state.errorMessage = message || "차트 데이터를 불러오지 못했습니다.";
  updateHeader();
  renderSidebar();
  showTab(state.activeTab);
}

function applyLiveQuote(payload) {
  if (!payload) return;
  const price = toNumber(payload.price);
  const diff = toNumber(payload.diff);
  const rate = toNumber(payload.rate);
  state.liveQuote = {
    price: Number.isFinite(price) ? price : NaN,
    diff: Number.isFinite(diff) ? diff : NaN,
    rate: Number.isFinite(rate) ? rate : NaN
  };
  updateHeader();
  if (state.activeTab === "chart" && ALL_DATA.length) {
    refreshHover();
  }
}

function disconnectRealtime() {
  if (state.wsRetryTimer) {
    clearTimeout(state.wsRetryTimer);
    state.wsRetryTimer = null;
  }
  const socket = state.ws;
  state.ws = null;
  if (socket) {
    try { socket.close(); } catch (e) {}
  }
  state.wsStatus = "idle";
  updateHeaderSubline();
}

function connectRealtime() {
  disconnectRealtime();
  if (!state.selectedStock || !state.selectedStock.stock_code) return;

  state.wsStatus = "connecting";
  updateHeaderSubline();

  const url = buildWsUrl(API_CONFIG.watchWs, "codes=" + encodeURIComponent(getRealtimeToken(state.selectedStock)));
  let socket;
  try {
    socket = new WebSocket(url);
  } catch (error) {
    state.wsStatus = "error";
    updateHeaderSubline();
    return;
  }

  state.ws = socket;
  socket.onopen = function () {
    if (state.ws !== socket) return;
    state.wsStatus = "connected";
    updateHeaderSubline();
  };
  socket.onmessage = function (event) {
    if (state.ws !== socket) return;
    try {
      const payload = JSON.parse(event.data);
      if (!payload) return;
      if (payload.type === "WL" && String(payload.code || "").toUpperCase() === getSelectedStockCode().toUpperCase()) {
        applyLiveQuote(payload);
      } else if (payload.type === "ERR") {
        state.wsStatus = "error";
        updateHeaderSubline();
      }
    } catch (error) {}
  };
  socket.onerror = function () {
    if (state.ws !== socket) return;
    state.wsStatus = "error";
    updateHeaderSubline();
  };
  socket.onclose = function () {
    if (state.ws !== socket) return;
    state.ws = null;
    state.wsStatus = "reconnecting";
    updateHeaderSubline();
    state.wsRetryTimer = setTimeout(function () {
      connectRealtime();
    }, 1500);
  };
}

async function loadSelectedStock(stock) {
  state.selectedStock = normalizeStock(stock);
  const searchInput = document.getElementById("stock-search-input");
  if (searchInput) {
    searchInput.value = state.selectedStock.stock_ko_name || state.selectedStock.stock_code;
  }
  state.dataStatus = "loading";
  state.errorMessage = "";
  state.liveQuote = null;
  ALL_DATA = [];
  state.detected = detectAll(ALL_DATA, state.customOpts);
  state.stats = calcStats();
  state.zoomRange = [0, 0];
  state.activeZoom = DEFAULT_ZOOM_PRESET.label;
  state.hoverIdx = null;
  updateHeader();
  renderSidebar();
  showTab(state.activeTab);

  try {
    const range = getDefaultChartRange();
    const chartPayload = await fetchJson(API_CONFIG.chart, {
      in_stockCode: state.selectedStock.stock_code,
      in_fromDate: range.from,
      in_toDate: range.to,
      in_periodDivCode: "D",
      in_orgAdjPrc: "1",
      in_stockMarket: state.selectedStock.stock_market,
      in_stockCountryCode: state.selectedStock.stock_country_code
    });
    const chartRows = normalizeChartBars(unwrapListResponse(chartPayload));
    if (!chartRows.length) {
      throw new Error("KIS 차트 데이터가 비어 있습니다.");
    }

    ALL_DATA = chartRows;
    resetVisibleRange();
    state.detected = detectAll(ALL_DATA, state.customOpts);
    state.stats = calcStats();
    state.dataStatus = "ready";
    state.errorMessage = "";

    if (state.selectedStock.stock_country_code === "KR") {
      try {
        const pricePayload = await fetchJson(API_CONFIG.currentPrice, {
          in_stockCode: state.selectedStock.stock_code
        });
        const single = unwrapSingleResponse(pricePayload);
        const output = (single && (single.output || single.out || (single.data && single.data.output))) || null;
        if (output) {
          applyLiveQuote({
            price: output.stckPrpr || output.stck_prpr,
            diff: output.prdyVrss || output.prdy_vrss,
            rate: output.prdyCtrt || output.prdy_ctrt
          });
        }
      } catch (error) {}
    }

    updateHeader();
    renderSidebar();
    showTab(state.activeTab);
    connectRealtime();
  } catch (error) {
    disconnectRealtime();
    setDashboardError(error && error.message ? error.message : "KIS 차트 데이터를 불러오지 못했습니다.");
  }
}

async function searchStockList(keyword) {
  const payload = await fetchJson(API_CONFIG.search, { in_stockCode: keyword });
  return unwrapListResponse(payload).map(normalizeStock);
}

function closeSearchResults() {
  const box = document.getElementById("stock-search-results");
  if (!box) return;
  box.classList.remove("open");
  box.innerHTML = "";
  state.searchResults = [];
  state.searchIndex = -1;
}

function renderSearchResults(items) {
  const box = document.getElementById("stock-search-results");
  if (!box) return;

  state.searchResults = Array.isArray(items) ? items : [];
  state.searchIndex = -1;

  if (!state.searchResults.length) {
    box.innerHTML = "<div class='search-result-empty'>검색 결과가 없습니다.</div>";
    box.classList.add("open");
    return;
  }

  box.innerHTML = state.searchResults.slice(0, 12).map(function (item, index) {
    return "<div class='search-result-item' data-search-index='" + index + "'>" +
      "<div class='search-result-main'>" +
      "<div class='search-result-name'>" + escapeHtml(item.stock_ko_name || item.stock_code) + "</div>" +
      "<div class='search-result-sub'>" + escapeHtml(item.stock_code) + " · " + escapeHtml(item.stock_country_code) + "</div>" +
      "</div>" +
      "<div class='search-result-meta'>" + escapeHtml(getMarketLabel(item)) + "</div>" +
      "</div>";
  }).join("");
  box.classList.add("open");

  box.querySelectorAll("[data-search-index]").forEach(function (row) {
    row.addEventListener("mousedown", function (event) {
      event.preventDefault();
      const index = parseInt(row.getAttribute("data-search-index"), 10);
      const item = state.searchResults[index];
      if (!item) return;
      const input = document.getElementById("stock-search-input");
      if (input) input.value = item.stock_ko_name || item.stock_code;
      closeSearchResults();
      loadSelectedStock(item);
    });
  });
}

function bindSearchUi() {
  const input = document.getElementById("stock-search-input");
  const clearButton = document.getElementById("stock-search-clear");
  const results = document.getElementById("stock-search-results");
  if (!input || !clearButton || !results) return;

  input.value = DEFAULT_STOCK.stock_ko_name;

  input.addEventListener("input", function () {
    const keyword = input.value.trim();
    if (state.searchTimer) {
      clearTimeout(state.searchTimer);
      state.searchTimer = null;
    }
    if (!keyword) {
      closeSearchResults();
      return;
    }
    state.searchTimer = setTimeout(async function () {
      try {
        const items = await searchStockList(keyword);
        renderSearchResults(items);
      } catch (error) {
        results.innerHTML = "<div class='search-result-empty'>종목 검색 중 오류가 발생했습니다.</div>";
        results.classList.add("open");
      }
    }, 200);
  });

  input.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      closeSearchResults();
      return;
    }
    if (event.key === "Enter" && state.searchResults.length) {
      event.preventDefault();
      const item = state.searchResults[0];
      input.value = item.stock_ko_name || item.stock_code;
      closeSearchResults();
      loadSelectedStock(item);
    }
  });

  clearButton.addEventListener("click", function () {
    input.value = "";
    closeSearchResults();
    input.focus();
  });

  document.addEventListener("click", function (event) {
    if (!results.contains(event.target) && event.target !== input) {
      closeSearchResults();
    }
  });
}

// ── 2. 패턴 메타데이터 ─── registry.js에서 자동 구성 ─────────────────
const PATTERNS = PATTERN_REGISTRY.map(r => r.meta);

// ── 3. 옵션 정의 ─────────────────────────────────────────────────────
const PATTERN_OPTIONS = {
  jin_gold_pullback:{lookbackBars:{v:10,min:5,max:20,step:1,label:'전고점 탐색 구간(봉)'},dropMin:{v:0.03,min:0.01,max:0.08,step:0.005,label:'눌림 최소(%)'},dropMax:{v:0.12,min:0.05,max:0.20,step:0.01,label:'눌림 최대(%)'},ma20Tolerance:{v:0.015,min:0.005,max:0.04,step:0.005,label:'MA20 허용오차(%)'},volumeMultiplier:{v:1.3,min:1.0,max:2.5,step:0.1,label:'거래량 배율'}},
  twin_bottom_break:{troughGapMin:{v:10,min:5,max:20,step:1,label:'저점 최소 간격'},troughGapMax:{v:40,min:20,max:60,step:5,label:'저점 최대 간격'},troughPriceTolerance:{v:0.03,min:0.01,max:0.06,step:0.005,label:'저점 가격 오차(%)'},volMultiplier:{v:2.0,min:1.0,max:3.5,step:0.1,label:'거래량 배율'}},
  minute_energy:{bbContractionRatio:{v:0.5,min:0.2,max:0.8,step:0.05,label:'BB 수축 비율'},volMultiplier:{v:2.5,min:1.0,max:4.0,step:0.1,label:'거래량 배율'},bbLookback:{v:10,min:5,max:20,step:1,label:'BB 비교 구간'}},
  surge_4_close:{surgeLookback:{v:5,min:3,max:10,step:1,label:'급등 탐색 구간'},surgeMinReturn:{v:0.15,min:0.05,max:0.30,step:0.01,label:'급등 최소(%)'},upperWickThreshold:{v:0.55,min:0.3,max:0.8,step:0.05,label:'윗꼬리 임계값'}},
  big_gold_acc:{boxLookback:{v:20,min:10,max:30,step:1,label:'박스 탐색 구간'},boxRangeMax:{v:0.06,min:0.02,max:0.12,step:0.01,label:'박스 최대폭(%)'},volSpikeMultiplier:{v:2.0,min:1.2,max:3.5,step:0.1,label:'거래량 폭증 배율'},minSpikeDays:{v:3,min:1,max:6,step:1,label:'폭증 최소 일수'}},
  jin_gold_deep:{ma60Tolerance:{v:0.02,min:0.005,max:0.05,step:0.005,label:'MA60 허용오차(%)'},volMultiplier:{v:1.5,min:1.0,max:3.0,step:0.1,label:'거래량 배율'}},
  double_n:{lookback:{v:20,min:10,max:30,step:1,label:'N자 탐색 구간'},volMultiplier:{v:1.2,min:1.0,max:2.0,step:0.1,label:'거래량 배율'}},
  smart_money:{downTrendBars:{v:5,min:3,max:8,step:1,label:'하락 확인 구간'},lowerWickMin:{v:0.35,min:0.1,max:0.6,step:0.05,label:'아래꼬리 최소'},volMultiplier:{v:1.5,min:1.0,max:2.5,step:0.1,label:'거래량 배율'}},
  foul_pattern:{breakThreshold:{v:0.015,min:0.005,max:0.03,step:0.005,label:'이탈 임계값(%)'},fakeBreakVolMax:{v:1.2,min:0.8,max:1.8,step:0.1,label:'이탈봉 거래량 상한'},recoveryVolMin:{v:1.4,min:1.0,max:2.5,step:0.1,label:'복귀봉 거래량 하한'}},
  bell_return:{peakLookback:{v:60,min:30,max:120,step:5,label:'고점 탐색 구간'},dropMin:{v:0.25,min:0.1,max:0.4,step:0.05,label:'하락 최소(%)'},dropMax:{v:0.55,min:0.3,max:0.7,step:0.05,label:'하락 최대(%)'},volMultiplier:{v:2.0,min:1.0,max:3.5,step:0.1,label:'거래량 배율'}},
  bb_golden_cross:{bbTouchLookback:{v:5,min:2,max:10,step:1,label:'BB터치 탐색 구간'}},
  bb_lower_bounce:{lowerWickMin:{v:0.30,min:0.1,max:0.6,step:0.05,label:'아래꼬리 최소'},volMultiplier:{v:1.4,min:1.0,max:2.5,step:0.1,label:'거래량 배율'}},
  fake_out:{breakThreshold:{v:0.015,min:0.005,max:0.04,step:0.005,label:'이탈 임계값'},fakeVolMax:{v:1.2,min:0.8,max:1.8,step:0.1,label:'이탈 거래량 상한'},recoveryVolMin:{v:1.6,min:1.0,max:2.5,step:0.1,label:'복귀 거래량 하한'}},
  minus_7pct:{dropMin:{v:0.065,min:0.04,max:0.09,step:0.005,label:'하락 최소(%)'},dropMax:{v:0.09,min:0.07,max:0.15,step:0.005,label:'하락 최대(%)'}},
  accumulated_break:{minAttempts:{v:2,min:1,max:4,step:1,label:'최소 도전 횟수'},volMultiplier:{v:2.5,min:1.0,max:4.0,step:0.1,label:'거래량 배율'},resistanceLookback:{v:30,min:15,max:60,step:5,label:'매물대 구간'}},
  ma60_ma20:{ma60SlopeLookback:{v:3,min:1,max:8,step:1,label:'MA60 기울기 구간'}},
  final_fake_trap:{strongCandleStreak:{v:3,min:2,max:5,step:1,label:'선행 강봉 수'},exitDropMin:{v:0.025,min:0.01,max:0.06,step:0.005,label:'출회 하락 최소(%)'},exitVolMultiplier:{v:3.0,min:1.5,max:5.0,step:0.1,label:'거래량 배율'}},
  dorido:{lookback:{v:25,min:15,max:35,step:1,label:'탐색 구간'},boxRangeMax:{v:0.06,min:0.02,max:0.10,step:0.01,label:'박스 최대폭(%)'},minCrosses:{v:4,min:2,max:8,step:1,label:'최소 교차 수'},volMultiplier:{v:1.8,min:1.0,max:3.0,step:0.1,label:'거래량 배율'}},
  lev_20_osc:{lookback:{v:20,min:10,max:40,step:2,label:'교차 탐색 구간'},minCrosses:{v:2,min:1,max:5,step:1,label:'최소 교차 횟수'}},
  lev_60tt:{peakWing:{v:5,min:2,max:8,step:1,label:'피크 탐색 Wing'},peakGapMin:{v:10,min:5,max:20,step:1,label:'피크 최소 간격'}},
  lev_insert:{convergeRatio:{v:0.35,min:0.15,max:0.60,step:0.05,label:'수렴 비율'},volMultiplier:{v:1.8,min:1.0,max:3.0,step:0.1,label:'거래량 배율'}},
  lev_forecast:{ma20RunBars:{v:5,min:3,max:10,step:1,label:'MA20 런 구간'},ma60RunBars:{v:10,min:5,max:20,step:1,label:'MA60 런 구간'}},
};

// ── 4. MA / 그룹 색상 ─────────────────────────────────────────────────
const MA_CFG = {
  5:  {color:'#D4D4D4',label:'MA5',  width:1.0,dash:''},
  20: {color:'#FF3B3B',label:'MA20', width:1.4,dash:''},
  60: {color:'#22C55E',label:'MA60', width:1.6,dash:''},
  120:{color:'#3B82F6',label:'MA120',width:1.7,dash:'5,3'},
  240:{color:'#F472B6',label:'MA240',width:2.0,dash:'8,4'},
};
const PATTERN_CHART_OPTION_ID = 'PATTERN_ITEMCHART';
const CHART_MA_LINE_WIDTH_DEFAULT = 2.5;
const CHART_MA_LINE_WIDTH_MIN = 0.5;
const CHART_MA_LINE_WIDTH_MAX = 6;
const GROUP_COLORS = {'G1-눌림목':'#FFD700','G2-변형':'#FF9800','G3-파동':'#29B6F6','G4-레벨':'#FF4081','G5-속임수':'#CE93D8','G6-전략':'#4ADE80','G7-최종':'#F97316','GB-레버':'#E91E63'};
const ZOOM_PRESETS = [{label:'1M',days:22},{label:'3M',days:63},{label:'6M',days:126},{label:'1Y',days:252},{label:'2Y',days:504}];
const DEFAULT_ZOOM_PRESET = ZOOM_PRESETS[ZOOM_PRESETS.length - 1];
const MIN_ZOOM_POINTS = 24;
const ZOOM_STEP_RATIO = 0.75;
const CHART_RISE_COLOR = '#E53935';
const CHART_FALL_COLOR = '#2563EB';
const DOUBLE_CHART_MODE_LABELS = { off: 'OFF', recent: '최근월봉', all: '전체월봉' };

function normalizeChartMaLineWidth(value) {
  let width = parseFloat(value);
  if (!Number.isFinite(width)) width = CHART_MA_LINE_WIDTH_DEFAULT;
  width = clampValue(width, CHART_MA_LINE_WIDTH_MIN, CHART_MA_LINE_WIDTH_MAX);
  return Math.round(width * 2) / 2;
}

function cloneChartMaOptions(list) {
  return (Array.isArray(list) ? list : []).map(function (item) {
    return {
      seriesKey: String(item.seriesKey || ''),
      seriesLabel: String(item.seriesLabel || ''),
      seriesPeriod: parseInt(item.seriesPeriod, 10) || 0,
      seriesColor: String(item.seriesColor || '#94A3B8'),
      lineWidth: normalizeChartMaLineWidth(item.lineWidth),
      enabledYn: item.enabledYn === 'N' || item.enabledYn === false ? 'N' : 'Y',
      displayOrder: parseInt(item.displayOrder, 10) || 0
    };
  });
}

function getSortedChartMaOptions(list) {
  const deduped = new Map();
  (Array.isArray(list) ? list : []).forEach(function (item, index) {
    const period = parseInt(item.seriesPeriod, 10);
    if (!Number.isFinite(period) || period <= 0) return;
    const base = MA_CFG[period] || {};
    deduped.set(period, {
      seriesKey: String(item.seriesKey || ('ma' + period)),
      seriesLabel: String(item.seriesLabel || base.label || ('MA' + period)),
      seriesPeriod: period,
      seriesColor: String(item.seriesColor || base.color || '#94A3B8'),
      lineWidth: normalizeChartMaLineWidth(item.lineWidth != null ? item.lineWidth : base.width),
      enabledYn: item.enabledYn === 'N' || item.enabledYn === false ? 'N' : 'Y',
      displayOrder: parseInt(item.displayOrder, 10) || (index + 1)
    });
  });
  return Array.from(deduped.values())
    .sort(function (left, right) {
      const leftOrder = left.displayOrder || left.seriesPeriod;
      const rightOrder = right.displayOrder || right.seriesPeriod;
      if (leftOrder !== rightOrder) return leftOrder - rightOrder;
      return left.seriesPeriod - right.seriesPeriod;
    })
    .map(function (item, index) {
      item.displayOrder = index + 1;
      return item;
    });
}

function getDefaultChartMaOptions() {
  return getSortedChartMaOptions(Object.keys(MA_CFG).map(function (period, index) {
    const cfg = MA_CFG[period];
    return {
      seriesKey: 'ma' + period,
      seriesLabel: cfg.label,
      seriesPeriod: parseInt(period, 10),
      seriesColor: cfg.color,
      lineWidth: cfg.width,
      enabledYn: 'Y',
      displayOrder: index + 1
    };
  }));
}

const DEFAULT_CHART_MA_OPTIONS = getDefaultChartMaOptions();

function getChartMaOptions() {
  return cloneChartMaOptions(state.chartMaOptions || DEFAULT_CHART_MA_OPTIONS);
}

function getEnabledChartMaOptions() {
  return getChartMaOptions().filter(function (item) {
    return item.enabledYn === 'Y';
  });
}

function getVisibleChartMaPeriods() {
  const enabledPeriods = getEnabledChartMaOptions().map(function (item) { return item.seriesPeriod; });
  return (Array.isArray(state.visibleMAs) ? state.visibleMAs : [])
    .filter(function (period, index, list) {
      return enabledPeriods.indexOf(period) !== -1 && list.indexOf(period) === index;
    })
    .sort(function (left, right) { return left - right; });
}

function getChartMaConfig(period) {
  const key = parseInt(period, 10);
  const base = MA_CFG[key] || { color: '#94A3B8', label: 'MA' + key, width: 1.6, dash: '' };
  const option = (state.chartMaOptions || []).find(function (item) {
    return item.seriesPeriod === key;
  });
  return {
    color: option && option.seriesColor ? option.seriesColor : base.color,
    label: option && option.seriesLabel ? option.seriesLabel : base.label,
    width: normalizeChartMaLineWidth(option && option.lineWidth != null ? option.lineWidth : base.width),
    dash: base.dash || ''
  };
}

function getChartMaPeriodsForCalc() {
  const periods = new Set([5, 20, 60, 120, 240]);
  getChartMaOptions().forEach(function (item) {
    if (item.seriesPeriod > 0) periods.add(item.seriesPeriod);
  });
  return Array.from(periods).sort(function (left, right) { return left - right; });
}

function recomputeChartMaSeries(data) {
  const rows = Array.isArray(data) ? data : [];
  const periods = getChartMaPeriodsForCalc();
  periods.forEach(function (period) {
    let sum = 0;
    for (let i = 0; i < rows.length; i++) {
      sum += rows[i].close;
      if (i >= period) sum -= rows[i - period].close;
      rows[i]['ma' + period] = i >= period - 1
        ? Math.round(sum / period * 10) / 10
        : null;
    }
  });
  return rows;
}

function syncVisibleChartMaPeriods(resetVisible) {
  const enabledPeriods = getEnabledChartMaOptions().map(function (item) { return item.seriesPeriod; });
  const currentVisible = Array.isArray(state.visibleMAs) ? state.visibleMAs.slice() : [];
  let nextVisible = resetVisible
    ? []
    : currentVisible.filter(function (period) { return enabledPeriods.indexOf(period) !== -1; });

  enabledPeriods.forEach(function (period) {
    if (resetVisible || currentVisible.indexOf(period) === -1) {
      if (nextVisible.indexOf(period) === -1) nextVisible.push(period);
    }
  });

  if (!nextVisible.length) {
    nextVisible = enabledPeriods.slice();
  }

  state.visibleMAs = nextVisible.sort(function (left, right) { return left - right; });
}

function setChartMaOptions(list, options) {
  const nextOptions = getSortedChartMaOptions(Array.isArray(list) && list.length ? list : getDefaultChartMaOptions());
  state.chartMaOptions = cloneChartMaOptions(nextOptions);
  syncVisibleChartMaPeriods(!!(options && options.resetVisible));
  if (ALL_DATA.length) {
    recomputeChartMaSeries(ALL_DATA);
  }
  return getChartMaOptions();
}

function toggleVisibleMa(period) {
  const target = parseInt(period, 10);
  if (!Number.isFinite(target)) return false;
  const enabledPeriods = getEnabledChartMaOptions().map(function (item) { return item.seriesPeriod; });
  if (enabledPeriods.indexOf(target) === -1) return false;
  const current = Array.isArray(state.visibleMAs) ? state.visibleMAs.slice() : [];
  const next = current.filter(function (value, index, list) {
    return list.indexOf(value) === index && enabledPeriods.indexOf(value) !== -1;
  });
  const foundIndex = next.indexOf(target);
  if (foundIndex > -1) next.splice(foundIndex, 1);
  else next.push(target);
  state.visibleMAs = next.sort(function (left, right) { return left - right; });
  return true;
}

// ── 5. 패턴 감지 ─── registry.js의 각 패턴 detect 함수 사용 ──────────
function detectAll(data, customOpts) {
  return runAllPatterns(data, customOpts || {});
}


// ── 6. 앱 상태 ───────────────────────────────────────────────────────
let ALL_DATA = [];
const state = {
  activeTab: 'chart',
  selectedPats: ['jin_gold_pullback','ma60_ma20','bb_lower_bounce','twin_bottom_break'],
  visibleMAs: DEFAULT_CHART_MA_OPTIONS.map(function (item) { return item.seriesPeriod; }),
  showBB: false,
  showVolume: true,
  showPivot: true,
  showRSI: true,
  doubleChartMode: 'off',
  hoverIdx: null,
  zoomRange: [0,0],
  activeZoom: '2Y',
  filterMod: 'ALL',
  filterGrp: 'ALL',
  optPatId: 'jin_gold_pullback',
  localOpts: {},
  customOpts: {},
  detected: {},
  stats: {},
  selectedStock: normalizeStock(DEFAULT_STOCK),
  dataStatus: "loading",
  errorMessage: "",
  liveQuote: null,
  ws: null,
  wsStatus: "idle",
  wsRetryTimer: null,
  searchResults: [],
  searchIndex: -1,
  searchTimer: null,
  currentTheme: "dark",
  themeSource: "system",
  sidebarCollapsed: false,
  sidebarAccordion: {
    patterns: true,
    indicators: true
  },
  chartMaOptions: cloneChartMaOptions(DEFAULT_CHART_MA_OPTIONS),
  chartPan: {
    active: false,
    startClientX: 0,
    startRange: [0, 0],
    plotWidth: 1,
    pendingRange: null,
    frameId: 0
  }
};
state.detected = detectAll(ALL_DATA, state.customOpts);
state.stats = calcStats();

function calcStats() {
  const s = {};
  for(const p of PATTERNS){
    const matches = state.detected[p.id]||[];
    let hit=0;
    for(const m of matches){
      if(m.idx+5<ALL_DATA.length){
        const fwd=ALL_DATA[m.idx+5].close,cur=ALL_DATA[m.idx].close;
        if((m.type==='BUY'&&fwd>cur)||(m.type==='SELL'&&fwd<cur))hit++;
      }
    }
    s[p.id]={count:matches.length,winRate:matches.length>0?Math.round(hit/matches.length*100):0};
  }
  return s;
}

function syncSidebarChrome() {
  const sidebar = document.getElementById('sidebar');
  if (!sidebar) return;

  const collapsed = !!state.sidebarCollapsed;
  sidebar.classList.toggle('sidebar-collapsed', collapsed);

  const sidebarToggle = document.getElementById('sidebar-toggle-btn');
  if (sidebarToggle) {
    const label = collapsed ? '사이드바 열기' : '사이드바 닫기';
    sidebarToggle.setAttribute('aria-label', label);
    sidebarToggle.setAttribute('title', label);
    sidebarToggle.setAttribute('data-collapsed', collapsed ? 'true' : 'false');
  }

  const collapsedNav = document.getElementById('sidebar-collapsed-nav');
  if (collapsedNav) {
    collapsedNav.setAttribute('aria-hidden', collapsed ? 'false' : 'true');
  }

  [
    { key: 'patterns', sectionId: 'sidebar-pattern-section', bodyId: 'sidebar-pattern-body', toggleId: 'sidebar-pattern-toggle' },
    { key: 'indicators', sectionId: 'sidebar-indicator-section', bodyId: 'sidebar-indicator-body', toggleId: 'sidebar-indicator-toggle' }
  ].forEach(function (item) {
    const section = document.getElementById(item.sectionId);
    const body = document.getElementById(item.bodyId);
    const toggle = document.getElementById(item.toggleId);
    const expanded = !!state.sidebarAccordion[item.key];

    if (section) section.classList.toggle('is-collapsed', !expanded);
    if (body) body.hidden = !expanded;
    if (toggle) {
      toggle.setAttribute('aria-expanded', expanded ? 'true' : 'false');
      const icon = toggle.querySelector('.sidebar-accordion-icon');
      if (icon) icon.textContent = expanded ? '▾' : '▸';
    }
  });
}

function openSidebarPanel(target) {
  state.sidebarCollapsed = false;
  if (target === 'patterns') {
    state.sidebarAccordion.patterns = true;
  } else if (target === 'indicators') {
    state.sidebarAccordion.indicators = true;
  }
  syncSidebarChrome();
  requestAnimationFrame(function () {
    const focusMap = {
      patterns: 'sidebar-pattern-section',
      indicators: 'sidebar-indicator-section',
      summary: 'sidebar-summary'
    };
    const el = document.getElementById(focusMap[target] || focusMap.patterns);
    if (el && typeof el.scrollIntoView === 'function') {
      el.scrollIntoView({ block: 'nearest' });
    }
  });
}

function bindSidebarChrome() {
  const sidebarToggle = document.getElementById('sidebar-toggle-btn');
  if (sidebarToggle && !sidebarToggle.dataset.bound) {
    sidebarToggle.dataset.bound = 'Y';
    sidebarToggle.addEventListener('click', function () {
      state.sidebarCollapsed = !state.sidebarCollapsed;
      syncSidebarChrome();
    });
  }

  [
    { key: 'patterns', toggleId: 'sidebar-pattern-toggle' },
    { key: 'indicators', toggleId: 'sidebar-indicator-toggle' }
  ].forEach(function (item) {
    const toggle = document.getElementById(item.toggleId);
    if (!toggle || toggle.dataset.bound) return;
    toggle.dataset.bound = 'Y';
    toggle.addEventListener('click', function () {
      state.sidebarAccordion[item.key] = !state.sidebarAccordion[item.key];
      syncSidebarChrome();
    });
  });

  [
    { id: 'sidebar-collapsed-patterns', target: 'patterns' },
    { id: 'sidebar-collapsed-indicators', target: 'indicators' },
    { id: 'sidebar-collapsed-summary', target: 'summary' }
  ].forEach(function (item) {
    const button = document.getElementById(item.id);
    if (!button || button.dataset.bound) return;
    button.dataset.bound = 'Y';
    button.addEventListener('click', function () {
      openSidebarPanel(item.target);
    });
  });

  syncSidebarChrome();
}

function clampValue(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

function getZoomSpan() {
  return Math.max(0, state.zoomRange[1] - state.zoomRange[0]);
}

function getDoubleChartModeLabel(mode) {
  return DOUBLE_CHART_MODE_LABELS[mode] || DOUBLE_CHART_MODE_LABELS.off;
}

function getNextDoubleChartMode(mode) {
  if (mode === 'recent') return 'all';
  if (mode === 'all') return 'off';
  return 'recent';
}

function getMinZoomSpan() {
  return ALL_DATA.length ? Math.min(ALL_DATA.length, MIN_ZOOM_POINTS) : 0;
}

function getDefaultVisibleRange() {
  const span = Math.min(ALL_DATA.length, DEFAULT_ZOOM_PRESET.days);
  return [Math.max(0, ALL_DATA.length - span), ALL_DATA.length];
}

function setVisibleRange(start, end, zoomLabel) {
  if (!ALL_DATA.length) {
    state.zoomRange = [0, 0];
    state.activeZoom = zoomLabel || DEFAULT_ZOOM_PRESET.label;
    state.hoverIdx = null;
    return false;
  }

  const total = ALL_DATA.length;
  const minSpan = getMinZoomSpan();
  const nextSpan = clampValue(Math.round(end - start), minSpan, total);
  const maxStart = Math.max(0, total - nextSpan);
  const nextStart = clampValue(Math.round(start), 0, maxStart);
  const nextEnd = nextStart + nextSpan;
  const nextLabel = zoomLabel || 'CUSTOM';

  if (nextStart === state.zoomRange[0] && nextEnd === state.zoomRange[1] && nextLabel === state.activeZoom) {
    return false;
  }

  state.zoomRange = [nextStart, nextEnd];
  state.activeZoom = nextLabel;
  state.hoverIdx = null;
  return true;
}

function applyPresetZoom(days, label) {
  if (!ALL_DATA.length) {
    state.zoomRange = [0, 0];
    state.activeZoom = label;
    state.hoverIdx = null;
    return false;
  }
  const span = Math.min(ALL_DATA.length, days);
  return setVisibleRange(ALL_DATA.length - span, ALL_DATA.length, label);
}

function resetVisibleRange() {
  if (!ALL_DATA.length) {
    state.zoomRange = [0, 0];
    state.activeZoom = DEFAULT_ZOOM_PRESET.label;
    state.hoverIdx = null;
    return false;
  }
  const range = getDefaultVisibleRange();
  return setVisibleRange(range[0], range[1], DEFAULT_ZOOM_PRESET.label);
}

function zoomVisibleRange(multiplier, focusRatio) {
  if (!ALL_DATA.length) return false;

  const currentSpan = getZoomSpan() || ALL_DATA.length;
  const total = ALL_DATA.length;
  const minSpan = getMinZoomSpan();
  const nextSpan = clampValue(Math.round(currentSpan * multiplier), minSpan, total);
  if (nextSpan === currentSpan) return false;

  const ratio = clampValue(Number.isFinite(focusRatio) ? focusRatio : 0.5, 0, 1);
  const focusIndex = state.zoomRange[0] + ratio * Math.max(0, currentSpan - 1);
  const nextStart = focusIndex - ratio * Math.max(0, nextSpan - 1);
  return setVisibleRange(nextStart, nextStart + nextSpan, 'CUSTOM');
}

function scheduleChartPan(startIndex) {
  const pan = state.chartPan;
  if (!pan.active || !ALL_DATA.length) return;

  const span = pan.startRange[1] - pan.startRange[0];
  if (!span || span >= ALL_DATA.length) return;

  const maxStart = Math.max(0, ALL_DATA.length - span);
  const nextStart = clampValue(Math.round(startIndex), 0, maxStart);
  const nextEnd = nextStart + span;

  if (nextStart === state.zoomRange[0] && nextEnd === state.zoomRange[1]) {
    pan.pendingRange = null;
    return;
  }

  pan.pendingRange = [nextStart, nextEnd];
  if (pan.frameId) return;

  pan.frameId = requestAnimationFrame(function () {
    pan.frameId = 0;
    const pendingRange = pan.pendingRange;
    pan.pendingRange = null;
    if (pendingRange && setVisibleRange(pendingRange[0], pendingRange[1], 'CUSTOM')) {
      renderChartTab();
    }
  });
}

function finishChartPan() {
  const pan = state.chartPan;
  if (pan.frameId) {
    cancelAnimationFrame(pan.frameId);
    pan.frameId = 0;
  }

  const pendingRange = pan.pendingRange;
  pan.pendingRange = null;
  pan.active = false;
  pan.startClientX = 0;
  pan.startRange = [0, 0];
  pan.plotWidth = 1;
  document.body.classList.remove('chart-pan-active');

  if (pendingRange && setVisibleRange(pendingRange[0], pendingRange[1], 'CUSTOM')) {
    renderChartTab();
  }
}

function beginChartPan(event, svg) {
  if (event.button !== 0 || !ALL_DATA.length) return;

  const span = getZoomSpan();
  if (!span || span >= ALL_DATA.length) return;

  const rect = svg.getBoundingClientRect();
  const pan = state.chartPan;
  pan.active = true;
  pan.startClientX = event.clientX;
  pan.startRange = [state.zoomRange[0], state.zoomRange[1]];
  pan.plotWidth = Math.max(rect.width * (IW / CW), 1);
  pan.pendingRange = null;
  if (pan.frameId) {
    cancelAnimationFrame(pan.frameId);
    pan.frameId = 0;
  }

  document.body.classList.add('chart-pan-active');
  event.preventDefault();

  const handleMove = function (moveEvent) {
    if (!state.chartPan.active) return;
    const visibleSpan = state.chartPan.startRange[1] - state.chartPan.startRange[0];
    const deltaBars = Math.round(((moveEvent.clientX - state.chartPan.startClientX) / state.chartPan.plotWidth) * Math.max(visibleSpan - 1, 1));
    scheduleChartPan(state.chartPan.startRange[0] - deltaBars);
  };

  const handleEnd = function () {
    window.removeEventListener('mousemove', handleMove);
    window.removeEventListener('mouseup', handleEnd);
    window.removeEventListener('blur', handleEnd);
    finishChartPan();
  };

  window.addEventListener('mousemove', handleMove);
  window.addEventListener('mouseup', handleEnd);
  window.addEventListener('blur', handleEnd);
}

function getSliceData() { return ALL_DATA.slice(state.zoomRange[0], state.zoomRange[1]); }
function hasCustom(id) { return !!(state.customOpts[id] && Object.keys(state.customOpts[id]).length>0); }

// ── 7. 차트 SVG 렌더링 ────────────────────────────────────────────────
const CW=900,CH=360,PL=70,PR=64,PT=16,PB=32;
const PRICE_AXIS_LABEL_X = CW - PR + 6;
const PRICE_BOX_WIDTH = 58;
const PRICE_BOX_HEIGHT = 16;
const PRICE_BOX_X = CW - PRICE_BOX_WIDTH - 2;
const PRICE_BOX_TEXT_X = PRICE_BOX_X + (PRICE_BOX_WIDTH / 2);
function VH(){ return state.showVolume?68:0; }
const IW=CW-PL-PR, IH=CH-PT-PB;

function calcPriceRange(data) {
  const vs=data.flatMap(d=>{
    const a=[d.high,d.low];
    if(state.showBB){if(d.bbU)a.push(d.bbU);if(d.bbL)a.push(d.bbL);}
    if(state.showPivot){if(d.r1)a.push(d.r1);if(d.s1)a.push(d.s1);}
    getVisibleChartMaPeriods().forEach(p=>{if(d[`ma${p}`])a.push(d[`ma${p}`]);});
    return a;
  }).filter(Boolean);
  if (state.liveQuote && Number.isFinite(state.liveQuote.price)) {
    vs.push(state.liveQuote.price);
  }
  const lo=Math.min(...vs),hi=Math.max(...vs),pad=(hi-lo)*0.06;
  return {minP:lo-pad,maxP:hi+pad};
}

function xS(i,len){ return PL+(i/(len-1||1))*IW; }
function yS(v,minP,maxP){ return PT+IH-((v-minP)/(maxP-minP))*IH; }

function renderDoubleMonthOverlay(data, xs, ys, baseBarWidth) {
  if (state.doubleChartMode === 'off' || !Array.isArray(data) || !data.length) return '';
  const helper = window.DoubleMonthChartScript;
  if (!helper || typeof helper.buildMonthlyOverlayFromDaily !== 'function') return '';

  const monthlyInfo = helper.buildMonthlyOverlayFromDaily(data.map(function (bar) {
    return [bar.ts, bar.open, bar.high, bar.low, bar.close];
  }));
  const overlays = Array.isArray(monthlyInfo.overlay) ? monthlyInfo.overlay : [];
  const boundaries = Array.isArray(monthlyInfo.boundaries) ? monthlyInfo.boundaries : [];
  if (!overlays.length || !boundaries.length) return '';

  const indexByTs = new Map();
  data.forEach(function (bar, index) {
    indexByTs.set(bar.ts, index);
  });

  const startOverlayIndex = state.doubleChartMode === 'recent' ? Math.max(0, overlays.length - 1) : 0;
  let svg = '';

  for (let overlayIndex = startOverlayIndex; overlayIndex < overlays.length; overlayIndex++) {
    const candle = overlays[overlayIndex];
    if (!Array.isArray(candle) || candle.length < 5) continue;

    const startTs = boundaries[overlayIndex];
    const startIndex = indexByTs.get(startTs);
    if (!Number.isFinite(startIndex)) continue;

    const nextBoundaryTs = overlayIndex + 1 < boundaries.length ? boundaries[overlayIndex + 1] : null;
    let endIndex = data.length - 1;
    if (nextBoundaryTs != null && indexByTs.has(nextBoundaryTs)) {
      endIndex = Math.max(startIndex, indexByTs.get(nextBoundaryTs) - 1);
    }

    const monthOpen = toNumber(candle[1]);
    const monthHigh = toNumber(candle[2]);
    const monthLow = toNumber(candle[3]);
    const monthClose = toNumber(candle[4]);
    if (![monthOpen, monthHigh, monthLow, monthClose].every(Number.isFinite)) continue;

    const leftX = xs(startIndex) - baseBarWidth * 0.7;
    const rightX = xs(endIndex) + baseBarWidth * 0.7;
    const centerX = (leftX + rightX) / 2;
    const width = Math.max(12, (rightX - leftX) * 0.92);
    const bodyTop = ys(Math.max(monthOpen, monthClose));
    const bodyBottom = ys(Math.min(monthOpen, monthClose));
    const wickTop = ys(monthHigh);
    const wickBottom = ys(monthLow);
    const isUp = monthClose >= monthOpen;
    const stroke = isUp ? 'rgba(229,57,53,0.52)' : 'rgba(37,99,235,0.52)';
    const fill = isUp ? 'rgba(229,57,53,0.22)' : 'rgba(37,99,235,0.22)';

    svg += `<g class="double-month-candle">
      <line x1="${centerX.toFixed(1)}" y1="${wickTop.toFixed(1)}" x2="${centerX.toFixed(1)}" y2="${wickBottom.toFixed(1)}" stroke="${stroke}" stroke-width="1.2"/>
      <rect x="${(centerX - width / 2).toFixed(1)}" y="${bodyTop.toFixed(1)}" width="${width.toFixed(1)}" height="${Math.max(2, bodyBottom - bodyTop).toFixed(1)}" fill="${fill}" stroke="${stroke}" stroke-width="1"/>
    </g>`;
  }

  return svg ? `<g clip-path="url(#cc2)">${svg}</g>` : '';
}

function renderChart() {
  const data = getSliceData();
  const palette = getThemePalette();
  const {minP,maxP} = calcPriceRange(data);
  const maxVol = Math.max(...data.map(d=>d.volume));
  const barW = Math.max(1.0, IW/data.length*0.65);
  const vHeight = VH();
  const xs = (i)=>xS(i,data.length);
  const ys = (v)=>yS(v,minP,maxP);
  const doubleChartSvg = renderDoubleMonthOverlay(data, xs, ys, barW);

  // price grid
  const step = Math.ceil((maxP-minP)/6/2000)*2000||1000;
  const gridStart = Math.ceil(minP/step)*step;
  let gridLines='';
  for(let v=gridStart;v<=maxP;v+=step){const y=ys(v);if(y<PT||y>PT+IH)continue;gridLines+=`<line x1="${PL}" y1="${y.toFixed(1)}" x2="${CW-PR}" y2="${y.toFixed(1)}" stroke="${palette.grid}" stroke-width="0.5" stroke-dasharray="3,4"/><text x="${PRICE_AXIS_LABEL_X}" y="${(y+3.5).toFixed(1)}" text-anchor="start" font-size="9" fill="${palette.axis}" font-weight="600">${Math.round(v).toLocaleString()}</text>`;}

  // month/year marks
  let monthMarks='';const seenMonth=new Set();
  data.forEach((d,i)=>{
    if(!seenMonth.has(d.monthLabel)){seenMonth.add(d.monthLabel);const x=xs(i);const isYear=d.monthLabel.endsWith('.01');
    if(isYear){monthMarks+=`<line x1="${x}" y1="${PT}" x2="${x}" y2="${CH}" stroke="${palette.yearLine}" stroke-width="1.5"/><text x="${x+4}" y="${PT+14}" font-size="10" fill="${palette.yearMark}" font-weight="700">${d.yearLabel}</text>`;}
    else{const label=d.monthLabel.slice(5)+'월';monthMarks+=`<line x1="${x}" y1="${PT}" x2="${x}" y2="${PT+IH}" stroke="${palette.monthLine}" stroke-width="0.5"/><text x="${x+2}" y="${(PT+IH+12).toFixed(1)}" font-size="7.5" fill="${palette.monthLabel}" font-weight="600">${label}</text>`;}}
  });

  // BB
  let bbSvg='';
  if(state.showBB){
    const up=data.map((d,i)=>d.bbU?`${xs(i).toFixed(1)},${ys(d.bbU).toFixed(1)}`:null).filter(Boolean);
    const lo=data.map((d,i)=>d.bbL?`${xs(i).toFixed(1)},${ys(d.bbL).toFixed(1)}`:null).filter(Boolean);
    if(up.length){bbSvg=`<g clip-path="url(#cc2)"><polygon points="${[...up,...[...lo].reverse()].join(' ')}" fill="rgba(167,139,250,0.06)"/><path d="M${up.join('L')}" fill="none" stroke="#A78BFA" stroke-width="0.7" stroke-dasharray="4,3" opacity="0.5"/><path d="M${lo.join('L')}" fill="none" stroke="#A78BFA" stroke-width="0.7" stroke-dasharray="4,3" opacity="0.5"/></g>`;}
  }

  // Pivot Lines
  let pivotSvg='';
  if(state.showPivot){
    [{k:'pp',c:'#A78BFA',w:0.8,d:'4,3'},{k:'r1',c:'#F87171',w:0.7,d:'3,4'},{k:'s1',c:'#4ADE80',w:0.7,d:'3,4'}].forEach(({k,c,w,d})=>{
      const pts=data.map((dd,i)=>dd[k]?`${xs(i).toFixed(1)},${ys(dd[k]).toFixed(1)}`:null).filter(Boolean);
      if(pts.length>2){
        // draw as segmented lines
        let path='',inL=false;
        data.forEach((dd,i)=>{const v=dd[k];if(!v){inL=false;return;}const pt=`${xs(i).toFixed(1)},${ys(v).toFixed(1)}`;path+=inL?`L${pt}`:`M${pt}`;inL=true;});
        if(path)pivotSvg+=`<path d="${path}" fill="none" stroke="${c}" stroke-width="${w}" stroke-dasharray="${d}" clip-path="url(#cc2)" opacity="0.65"/>`;
      }
    });
    // Labels
    if(data.length>0){
      const lastD2=data[data.length-1];
      const lx=CW-PR+2;
      if(lastD2.pp){pivotSvg+=`<text x="${lx}" y="${(ys(lastD2.pp)+3).toFixed(1)}" font-size="7" fill="#A78BFA" font-weight="700">PP</text>`;}
      if(lastD2.r1){pivotSvg+=`<text x="${lx}" y="${(ys(lastD2.r1)+3).toFixed(1)}" font-size="7" fill="#F87171" font-weight="700">R1</text>`;}
      if(lastD2.s1){pivotSvg+=`<text x="${lx}" y="${(ys(lastD2.s1)+3).toFixed(1)}" font-size="7" fill="#4ADE80" font-weight="700">S1</text>`;}
    }
  }
  let maLines='';
  for(const p of getVisibleChartMaPeriods().slice().sort((left, right)=>right-left)){
    const cfg=getChartMaConfig(p);
    let path='',inL=false;
    data.forEach((d,i)=>{const v=d[`ma${p}`];if(!v){inL=false;return;}const pt=`${xs(i).toFixed(1)},${ys(v).toFixed(1)}`;path+=inL?`L${pt}`:`M${pt}`;inL=true;});
    if(path)maLines+=`<path d="${path}" fill="none" stroke="${cfg.color}" stroke-width="${cfg.width}" ${cfg.dash?`stroke-dasharray="${cfg.dash}"`:''}  clip-path="url(#cc2)" opacity="0.9"/>`;
  }

  // Candlesticks
  let candles='';
  data.forEach((d,i)=>{
    const x=xs(i);const isUp=d.close>=d.open;const uc=CHART_RISE_COLOR,dc=CHART_FALL_COLOR;
    const bT=Math.min(ys(d.open),ys(d.close));const bH=Math.max(1,Math.abs(ys(d.open)-ys(d.close)));
    const hov=state.hoverIdx===i;
    candles+=`<g opacity="${hov?1:0.87}"><line x1="${x.toFixed(1)}" y1="${ys(d.high).toFixed(1)}" x2="${x.toFixed(1)}" y2="${ys(d.low).toFixed(1)}" stroke="${isUp?uc:dc}" stroke-width="${barW>3?1:0.6}"/><rect x="${(x-barW/2).toFixed(1)}" y="${bT.toFixed(1)}" width="${barW.toFixed(1)}" height="${bH.toFixed(1)}" fill="${isUp?uc:dc}" ${hov?'stroke="#FFF" stroke-width="0.5"':''}/></g>`;
  });

  // Signals
  let sigSvg='';
  const visibleSigs=[];
  for(const pid of state.selectedPats){
    const pat=PATTERNS.find(p=>p.id===pid);if(!pat)continue;
    for(const sig of (state.detected[pid]||[])){
      if(sig.idx>=state.zoomRange[0]&&sig.idx<state.zoomRange[1])visibleSigs.push({...sig,pid,color:pat.color,name:pat.name});
    }
  }
  visibleSigs.forEach((sig)=>{
    const li=sig.idx-state.zoomRange[0];if(li<0||li>=data.length)return;
    const x=xs(li),d=data[li];const isBuy=sig.type==='BUY';
    const arY=isBuy?ys(d.low)+4:ys(d.high)-4;const lbY=isBuy?arY+2:arY-14;
    sigSvg+=`<g><line x1="${x.toFixed(1)}" y1="${PT}" x2="${x.toFixed(1)}" y2="${PT+IH}" stroke="${sig.color}" stroke-width="0.4" stroke-dasharray="3,3" opacity="0.3"/><text x="${x.toFixed(1)}" y="${arY.toFixed(1)}" text-anchor="middle" font-size="9" fill="${sig.color}">${isBuy?'▲':'▼'}</text><rect x="${(x-22).toFixed(1)}" y="${lbY.toFixed(1)}" width="44" height="12" fill="${sig.color}" opacity="0.85" rx="2"/><text x="${x.toFixed(1)}" y="${(lbY+9).toFixed(1)}" text-anchor="middle" font-size="7" fill="#000" font-weight="800">${sig.label}</text></g>`;
    if(sig.idx2!=null){const i1=sig.idx2-state.zoomRange[0],i2=li;if(i1>=0&&i1<data.length)sigSvg+=`<line x1="${xs(i1).toFixed(1)}" y1="${(ys(data[i1].low)+4).toFixed(1)}" x2="${x.toFixed(1)}" y2="${(ys(d.low)+4).toFixed(1)}" stroke="${sig.color}" stroke-width="1.2" stroke-dasharray="4,3" opacity="0.5"/>`;}
  });

  // Current price line
  const lastD=data[data.length-1];
  const livePrice=state.liveQuote&&Number.isFinite(state.liveQuote.price)?state.liveQuote.price:lastD.close;
  const liveDiff=state.liveQuote&&Number.isFinite(state.liveQuote.diff)?state.liveQuote.diff:lastD.change;
  const ly=ys(livePrice);const uc=liveDiff>=0;
  const priceLine=`<line x1="${PL}" y1="${ly.toFixed(1)}" x2="${CW-PR}" y2="${ly.toFixed(1)}" stroke="${uc?CHART_RISE_COLOR:CHART_FALL_COLOR}" stroke-width="0.7" stroke-dasharray="4,3" opacity="0.5"/><rect x="${PRICE_BOX_X}" y="${(ly-(PRICE_BOX_HEIGHT/2)).toFixed(1)}" width="${PRICE_BOX_WIDTH}" height="${PRICE_BOX_HEIGHT}" fill="${uc?CHART_RISE_COLOR:CHART_FALL_COLOR}" rx="2"/><text x="${PRICE_BOX_TEXT_X}" y="${(ly+4).toFixed(1)}" text-anchor="middle" font-size="7.5" fill="#000" font-weight="800">${Math.round(livePrice).toLocaleString()}</text>`;

  // Crosshair
  let crosshair='';
  if(state.hoverIdx!==null&&state.hoverIdx>=0&&state.hoverIdx<data.length){
    const hd=data[state.hoverIdx];const hx=xs(state.hoverIdx);const hy=ys(hd.close);
    crosshair=`<line x1="${hx.toFixed(1)}" y1="${PT}" x2="${hx.toFixed(1)}" y2="${PT+IH}" stroke="${palette.crosshair}" stroke-width="0.6" stroke-dasharray="2,3" opacity="0.5"/><line x1="${PL}" y1="${hy.toFixed(1)}" x2="${CW-PR}" y2="${hy.toFixed(1)}" stroke="${palette.crosshair}" stroke-width="0.5" stroke-dasharray="2,3" opacity="0.35"/><rect x="${PRICE_BOX_X}" y="${(hy-(PRICE_BOX_HEIGHT/2)).toFixed(1)}" width="${PRICE_BOX_WIDTH}" height="${PRICE_BOX_HEIGHT}" fill="${palette.crosshairBg}" rx="2"/><text x="${PRICE_BOX_TEXT_X}" y="${(hy+4).toFixed(1)}" text-anchor="middle" font-size="8" fill="${palette.crosshair}" font-weight="700">${hd.close.toLocaleString()}</text>`;
  }

  // Volume bars
  let volBars='';
  if(state.showVolume){
    volBars=`<line x1="${PL}" y1="${CH+1}" x2="${CW-PR}" y2="${CH+1}" stroke="${palette.grid}" stroke-width="1"/>`;
    data.forEach((d,i)=>{
      const x=xs(i);const isUp=d.close>=d.open;const h=Math.max(1,(d.volume/maxVol)*(vHeight-10));
      const col=isUp?'url(#gu2)':'url(#gd2)';
      volBars+=`<rect x="${(x-barW/2).toFixed(1)}" y="${(CH+vHeight-4-h).toFixed(1)}" width="${barW.toFixed(1)}" height="${h.toFixed(1)}" fill="${col}" opacity="${state.hoverIdx===i?1:0.65}"/>`;
    });
    volBars+=`<text x="${PRICE_AXIS_LABEL_X}" y="${CH+13}" text-anchor="start" font-size="8" fill="${palette.volLabel}" font-weight="600">VOL</text>`;
  }

  return `<svg id="chart-svg" viewBox="0 0 ${CW} ${CH+vHeight+4}" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="gu2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="${CHART_RISE_COLOR}" stop-opacity="0.55"/><stop offset="100%" stop-color="${CHART_RISE_COLOR}" stop-opacity="0.05"/></linearGradient>
    <linearGradient id="gd2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="${CHART_FALL_COLOR}" stop-opacity="0.55"/><stop offset="100%" stop-color="${CHART_FALL_COLOR}" stop-opacity="0.05"/></linearGradient>
    <clipPath id="cc2"><rect x="${PL}" y="${PT}" width="${IW}" height="${IH}"/></clipPath>
  </defs>
  <rect x="${PL}" y="${PT}" width="${IW}" height="${IH}" fill="${palette.chartBg}" rx="4"/>
  ${gridLines}${monthMarks}${doubleChartSvg}${bbSvg}${pivotSvg}${maLines}
  <g clip-path="url(#cc2)">${candles}</g>
  ${sigSvg}${priceLine}${crosshair}${volBars}
</svg>`;
}

// ── 7-B. RSI 서브 차트 렌더링 ─────────────────────────────────────────
function renderRSIChart() {
  const data = getSliceData();
  const palette = getThemePalette();
  const W=CW, H=70, PL2=70, PR2=14, PT2=8, PB2=16;
  const IW2=W-PL2-PR2, IH2=H-PT2-PB2;
  const xs2=(i)=>PL2+(i/(data.length-1||1))*IW2;
  const ys2=(v)=>PT2+IH2-((v/100))*IH2;
  let linePath='',prevUp=null;
  let coloredPaths={red:'',amber:'',green:''};
  data.forEach((d,i)=>{if(d.rsi==null)return;const x=xs2(i).toFixed(1),y=ys2(d.rsi).toFixed(1);const key=d.rsi>70?'red':d.rsi<30?'green':'amber';if(coloredPaths[key]==='')coloredPaths[key]+=`M${x},${y}`;else coloredPaths[key]+=`L${x},${y}`;});
  const segments=coloredPaths;
  const overBuyY=ys2(70).toFixed(1),overSellY=ys2(30).toFixed(1),midY=ys2(50).toFixed(1);
  return `<svg viewBox="0 0 ${W} ${H}" width="100%" style="display:block">
    <rect x="${PL2}" y="${PT2}" width="${IW2}" height="${IH2}" fill="${palette.chartBg}"/>
    <rect x="${PL2}" y="${PT2}" width="${IW2}" height="${parseFloat(overBuyY)-PT2}" fill="rgba(248,113,113,0.06)"/>
    <rect x="${PL2}" y="${overSellY}" width="${IW2}" height="${IH2-(parseFloat(overSellY)-PT2)}" fill="rgba(74,222,128,0.06)"/>
    <line x1="${PL2}" y1="${overBuyY}" x2="${W-PR2}" y2="${overBuyY}" stroke="#F87171" stroke-width="0.5" stroke-dasharray="3,4" opacity="0.5"/>
    <line x1="${PL2}" y1="${midY}" x2="${W-PR2}" y2="${midY}" stroke="${palette.axis}" stroke-width="0.4" stroke-dasharray="2,5" opacity="0.4"/>
    <line x1="${PL2}" y1="${overSellY}" x2="${W-PR2}" y2="${overSellY}" stroke="#4ADE80" stroke-width="0.5" stroke-dasharray="3,4" opacity="0.5"/>
    ${segments.red?`<path d="${segments.red}" fill="none" stroke="#F87171" stroke-width="1.2" clip-path="url(#rcc)"/>`:''}
    ${segments.amber?`<path d="${segments.amber}" fill="none" stroke="#F59E0B" stroke-width="1.2" clip-path="url(#rcc)"/>`:''}
    ${segments.green?`<path d="${segments.green}" fill="none" stroke="#4ADE80" stroke-width="1.2" clip-path="url(#rcc)"/>`:''}
    <defs><clipPath id="rcc"><rect x="${PL2}" y="${PT2}" width="${IW2}" height="${IH2}"/></clipPath></defs>
    <text x="${PL2-5}" y="${parseFloat(overBuyY)+3}" text-anchor="end" font-size="8" fill="#F87171" font-weight="600">70</text>
    <text x="${PL2-5}" y="${parseFloat(overSellY)+3}" text-anchor="end" font-size="8" fill="#4ADE80" font-weight="600">30</text>
    <text x="${PL2-5}" y="${H-3}" text-anchor="end" font-size="8" fill="${palette.axis}" font-weight="700">RSI14</text>
    ${data[data.length-1]?.rsi!=null?`<text x="${W-PR2+2}" y="${ys2(data[data.length-1].rsi).toFixed(1)}" font-size="7.5" fill="${data[data.length-1].rsi>70?'#F87171':data[data.length-1].rsi<30?'#4ADE80':'#F59E0B'}" font-weight="700">${data[data.length-1].rsi.toFixed(1)}</text>`:''}
  </svg>`;
}

// ── 7-C. 6-Factor 강도 계산기 (설계서 PatternStrengthCalculator) ──────
function calcStrength6F(idx) {
  if(idx==null||idx<0||idx>=ALL_DATA.length)return 0;
  const d=ALL_DATA[idx]; let s=0;
  // F1: MA 정배열 +20
  const perf=d.ma5&&d.ma20&&d.ma60&&d.ma120&&d.close>d.ma5&&d.ma5>d.ma20&&d.ma20>d.ma60&&d.ma60>d.ma120;
  s+=perf?20:(d.ma20&&d.ma60&&d.ma20>d.ma60?12:0);
  // F2: 거래량 급증 +20
  const avg20=ALL_DATA.slice(Math.max(0,idx-20),idx).reduce((a,x)=>a+x.volume,0)/20||1;
  const vr=d.volume/avg20;s+=vr>=2?20:vr>=1.5?14:vr>=1.2?8:0;
  // F3: 추세 방향 +15
  const slope=idx>=5&&d.ma20&&ALL_DATA[idx-5].ma20?d.ma20-ALL_DATA[idx-5].ma20:0;
  s+=slope>0?15:slope>-200?7:0;
  // F4: RSI 과매도 +15
  s+=d.rsi!=null?(d.rsi<30?15:d.rsi<40?12:d.rsi<50?6:0):0;
  // F5: Pivot 근접 +15
  if(d.pp){const dists=[Math.abs(d.close-d.pp),d.s1?Math.abs(d.close-d.s1):999,d.r1?Math.abs(d.close-d.r1):999].map(x=>x/d.close);s+=Math.min(...dists)<0.005?15:Math.min(...dists)<0.01?10:Math.min(...dists)<0.02?5:0;}
  // F6: 시장방향 +15
  s+=idx>=20&&d.close>ALL_DATA[idx-20].close?15:5;
  return Math.min(s,100);
}

function updateHoverFromChartEvent(event, svg) {
  if (state.chartPan.active) return;
  const data = getSliceData();
  if (!data.length) return;
  const rect = svg.getBoundingClientRect();
  const x = (event.clientX - rect.left) / rect.width * CW;
  const idx = Math.round((x - PL) / IW * (data.length - 1));
  const nextHoverIdx = idx >= 0 && idx < data.length ? idx : null;
  if (nextHoverIdx !== state.hoverIdx) {
    state.hoverIdx = nextHoverIdx;
    refreshHover();
  }
}

function bindChartSvgEvents() {
  const svg = document.getElementById('chart-svg');
  if (!svg) return;

  svg.addEventListener('mousemove', function (event) {
    updateHoverFromChartEvent(event, svg);
  });
  svg.addEventListener('mousedown', function (event) {
    beginChartPan(event, svg);
  });
  svg.addEventListener('mouseleave', function () {
    if (state.chartPan.active) return;
    if (state.hoverIdx !== null) {
      state.hoverIdx = null;
      refreshHover();
    }
  });
  svg.addEventListener('wheel', function (event) {
    if (!ALL_DATA.length) return;
    const rect = svg.getBoundingClientRect();
    const svgX = (event.clientX - rect.left) / rect.width * CW;
    const focusRatio = clampValue((svgX - PL) / IW, 0, 1);
    const changed = event.deltaY < 0
      ? zoomVisibleRange(ZOOM_STEP_RATIO, focusRatio)
      : zoomVisibleRange(1 / ZOOM_STEP_RATIO, focusRatio);
    if (changed) {
      event.preventDefault();
      renderChartTab();
    }
  }, { passive: false });
}

// ── 8. 탭별 콘텐츠 렌더러 ───────────────────────────────────────────
function renderChartTab() {
  const data = getSliceData();
  if (!data.length) {
    const title = state.dataStatus === "loading" ? "KIS 차트 로딩 중" : (state.dataStatus === "error" ? "차트 로드 실패" : "차트 데이터 없음");
    const desc = state.dataStatus === "error"
      ? escapeHtml(state.errorMessage || "차트 데이터를 다시 조회해 주세요.")
      : "상단 종목 조회에서 원하는 종목을 선택하면 KIS 차트 데이터와 실시간 시세를 연결합니다.";
    document.getElementById('tab-chart').innerHTML = `
      <div class="chart-empty">
        <div class="chart-empty-title">${title}</div>
        <div class="chart-empty-desc ${state.dataStatus === "error" ? "chart-empty-error" : ""}">${desc}</div>
      </div>`;
    return;
  }
  const lastD = data[data.length-1];
  const firstD = data[0];
  const totalRet = ((lastD.close-firstD.close)/firstD.close*100).toFixed(2);
  const zoomSpan = getZoomSpan();
  const minZoomSpan = getMinZoomSpan();
  const canZoomIn = zoomSpan > minZoomSpan;
  const canZoomOut = zoomSpan < ALL_DATA.length;
  const isDefaultZoom = state.activeZoom === DEFAULT_ZOOM_PRESET.label && state.zoomRange[0] === getDefaultVisibleRange()[0] && state.zoomRange[1] === getDefaultVisibleRange()[1];
  const doubleChartMode = state.doubleChartMode || 'off';

  // Zoom buttons
  const zoomBtns = ZOOM_PRESETS.map(z=>`<button class="zoom-btn" style="border:1px solid ${state.activeZoom===z.label?'#38BDF8':'#1B2E4B'};background:${state.activeZoom===z.label?'#1E3A5F':'transparent'};color:${state.activeZoom===z.label?'#38BDF8':'#64748B'}" data-zoom="${z.label}" data-days="${z.days}">${z.label}</button>`).join('');
  const zoomActionBtns = `<div class="zoom-actions"><button type="button" class="zoom-step-btn" data-zoom-action="in" ${canZoomIn?'':'disabled'}>확대 +</button><button type="button" class="zoom-step-btn" data-zoom-action="out" ${canZoomOut?'':'disabled'}>축소 -</button><button type="button" class="zoom-step-btn" data-zoom-action="reset" ${isDefaultZoom?'disabled':''}>초기화</button><button type="button" class="zoom-step-btn double-chart-btn ${doubleChartMode !== 'off' ? 'is-active' : ''}" data-zoom-action="double-chart" data-double-mode="${doubleChartMode}" title="더블차트 ${getDoubleChartModeLabel(doubleChartMode)}"><span class="double-chart-btn-label">더블차트</span><span class="double-chart-btn-mode">${getDoubleChartModeLabel(doubleChartMode)}</span><span class="double-chart-dots" aria-hidden="true"><span class="double-chart-dot ${doubleChartMode==='recent'?'is-active':''}"></span><span class="double-chart-dot ${doubleChartMode==='all'?'is-active':''}"></span><span class="double-chart-dot ${doubleChartMode==='off'?'is-active':''}"></span></span></button></div>`;
  const zoomLabel = state.activeZoom === 'CUSTOM' ? '사용자 줌' : state.activeZoom;

  // MA chip buttons
  const maChips = getEnabledChartMaOptions().map(option=>{
    const p=option.seriesPeriod;const cfg=getChartMaConfig(p);const on=getVisibleChartMaPeriods().includes(p);
    return `<button class="ma-chip-btn" style="border:1.5px solid ${on?cfg.color:'#1B2E4B'};background:${on?cfg.color+'15':'transparent'};color:${on?cfg.color:'#475569'}" data-ma="${p}"><span class="ma-chip-line" style="background:${on?cfg.color:'#334155'}"></span>${cfg.label}</button>`;
  }).join('');

  // Hover bar
  const hd = state.hoverIdx!==null ? data[state.hoverIdx] : null;
  let hoverContent;
  if(hd){
    hoverContent=`<span style="color:#38BDF8;font-weight:700">${hd.date}</span>`;
    [['시',hd.open,'#94A3B8'],['고',hd.high,'#F87171'],['저',hd.low,'#60A5FA'],['종',hd.close,hd.change>=0?CHART_RISE_COLOR:CHART_FALL_COLOR],['등락',`${hd.change>=0?'+':''}${hd.change}(${hd.changePct.toFixed(1)}%)`,hd.change>=0?CHART_RISE_COLOR:CHART_FALL_COLOR],['거래량',`${(hd.volume/1e6).toFixed(1)}M`,'#64748B']].forEach(([l,v,c])=>{hoverContent+=`<span><span style="color:#334155">${l} </span><span style="color:${c};font-weight:700">${typeof v==='number'?v.toLocaleString():v}</span></span>`;});
    getVisibleChartMaPeriods().forEach(p=>{const cfg=getChartMaConfig(p);if(hd[`ma${p}`])hoverContent+=`<span style="color:${cfg.color};font-weight:700">${escapeHtml(cfg.label)}:${hd[`ma${p}`].toLocaleString()}</span>`;});
  } else {
    hoverContent=`<span style="color:#334155">차트 위에 마우스를 올리면 상세 정보가 표시됩니다 (${escapeHtml(getSelectedStockName())} · ${escapeHtml(getRangeSummary())})</span>`;
  }

  // MA cards
  const maCards = getEnabledChartMaOptions().map(option=>{
    const p=option.seriesPeriod;const cfg=getChartMaConfig(p);const maV=lastD[`ma${p}`];const diff=maV?lastD.close-maV:null;const on=getVisibleChartMaPeriods().includes(p);
    return `<div class="ma-card" style="background:${on?cfg.color+'0C':'#0B1220'};border:1px solid ${on?cfg.color+'50':'#1B2E4B'};opacity:${on?1:0.4}" data-ma="${p}">
      <div class="ma-card-header"><div class="ma-card-bar" style="background:${cfg.color}"></div><span class="ma-card-label" style="color:${cfg.color}">${cfg.label}</span></div>
      <div class="ma-card-value">${maV?maV.toLocaleString():'—'}<span class="ma-card-unit">원</span></div>
      ${diff!==null?`<div class="ma-card-diff" style="color:${diff>=0?'#4ADE80':'#F87171'}">${diff>=0?'▲':'▼'} ${Math.abs(diff).toLocaleString()}(${diff>=0?'+':''}${(diff/maV*100).toFixed(2)}%)</div>`:''}
      <div class="ma-card-period">${p<=5?'초단기':p<=20?'단기':p<=60?'중기':p<=120?'중장기':'장기'}</div>
    </div>`;
  }).join('');

  // Alignment status
  const ord=[5,20,60,120,240];const vals=ord.map(p=>({p,v:lastD[`ma${p}`]}));
  const isBull=vals.every((m,i)=>!m.v?true:i===0?(lastD.close>m.v):(vals[i-1].v>m.v))&&vals.filter(v=>v.v).length>=3;
  const isBear=vals.every((m,i)=>!m.v?true:i===0?(lastD.close<m.v):(vals[i-1].v<m.v))&&vals.filter(v=>v.v).length>=3;
  const alignDots = ord.map((p,idx)=>{const cur=lastD[`ma${p}`];const prev=idx===0?lastD.close:lastD[`ma${ord[idx-1]}`];const above=prev&&cur?prev>cur:null;const cfg=getChartMaConfig(p);return `<div class="align-dot-group"><div class="align-dot" style="background:${above===null?'#475569':above?'#4ADE80':'#F87171'}"></div><span style="font-size:9px;color:${cfg.color};font-weight:700">${escapeHtml(cfg.label)}</span>${idx<4?'<span class="align-sep">›</span>':''}</div>`;}).join('');
  const alignBadge=`<div class="align-badge" style="color:${isBull?'#4ADE80':isBear?'#F87171':'#FFD700'};background:${isBull?'#4ADE8015':isBear?'#F8717115':'#FFD70015'};border:1px solid ${isBull?'#4ADE8040':isBear?'#F8717140':'#FFD70040'}">${isBull?'✅ 정배열':isBear?'🔴 역배열':'⚡ 혼재'}</div>`;

  // Signal summary
  const visSigs=[];
  for(const pid of state.selectedPats){const pat=PATTERNS.find(p=>p.id===pid);if(!pat)continue;for(const sig of(state.detected[pid]||[])){if(sig.idx>=state.zoomRange[0]&&sig.idx<state.zoomRange[1])visSigs.push({...sig,pid,color:pat.color,name:pat.name});}}
  const sigChips = visSigs.map((sig,i)=>{const p=PATTERNS.find(x=>x.id===sig.pid);return `<div class="signal-chip" style="background:${sig.color}12;border:1px solid ${sig.color}40"><span style="color:${sig.color}">${p?.icon} ${sig.name}</span><span style="color:#64748B;margin-left:4px">D${sig.idx+1}</span><span style="color:${sig.type==='BUY'?'#4ADE80':'#F87171'};margin-left:3px;font-weight:700">${sig.type==='BUY'?'매수':'매도'}</span><span style="color:#475569;margin-left:3px">${(sig.confidence*100).toFixed(0)}%</span></div>`;}).join('');

  const visibleCount = visSigs.length;
  document.getElementById('tab-chart').innerHTML = `
    <div class="chart-controls">
      <div class="zoom-btns">${zoomBtns}</div>
      ${zoomActionBtns}
      <div class="divider"></div>
      ${maChips}
      <label style="display:flex;align-items:center;gap:4px;font-size:10px;color:#64748B;cursor:pointer"><input type="checkbox" id="bb-toggle" ${state.showBB?'checked':''}> BB</label>
      <label style="display:flex;align-items:center;gap:4px;font-size:10px;color:#A78BFA;cursor:pointer"><input type="checkbox" id="pivot-toggle" ${state.showPivot?'checked':''}> Pivot</label>
      <label style="display:flex;align-items:center;gap:4px;font-size:10px;color:#F59E0B;cursor:pointer"><input type="checkbox" id="rsi-toggle" ${state.showRSI?'checked':''}> RSI</label>
      <label style="display:flex;align-items:center;gap:4px;font-size:10px;color:#64748B;cursor:pointer"><input type="checkbox" id="vol-toggle" ${state.showVolume?'checked':''}> 거래량</label>
      <span class="chart-info">줌: <b style="color:#38BDF8">${zoomLabel}</b> · 더블차트: <b style="color:${doubleChartMode==='off'?'#64748B':'#38BDF8'}">${getDoubleChartModeLabel(doubleChartMode)}</b> · 표시: <b style="color:#FFD700">${data.length}</b>일 · 신호: <b style="color:#4ADE80">${visibleCount}</b>개 · 휠 확대/축소 · 좌클릭 드래그 이동</span>
    </div>
    <div id="hover-bar">${hoverContent}</div>
    <div id="chart-wrap">${renderChart()}</div>
    ${state.showRSI ? `<div id="rsi-wrap" style="background:#0B1220;border-radius:8px;border:1px solid #1B2E4B;overflow:hidden;flex-shrink:0">${renderRSIChart()}</div>` : ''}
    <div class="ma-cards">${maCards}</div>
    <div class="align-bar" style="border:1px solid ${isBull?'#4ADE8040':isBear?'#F8717140':'#1B2E4B'}">
      <span style="font-size:10px;font-weight:700;color:#475569">배열 상태</span>
      <div class="align-dots">${alignDots}</div>
      ${alignBadge}
    </div>
    ${visSigs.length>0?`<div class="signal-summary"><div class="signal-title">📍 현재 구간 감지 신호</div><div class="signal-chips">${sigChips}</div></div>`:''}
  `;
  bindChartSvgEvents();
  bindChartEvents();
}

function refreshHover(){
  // Update just the hover bar and crosshair without full re-render
  const data=getSliceData();
  if(!data.length)return;
  const hd=state.hoverIdx!==null?data[state.hoverIdx]:null;
  const bar=document.getElementById('hover-bar');
  if(bar){
    let hoverContent;
    if(hd){
      hoverContent=`<span style="color:#38BDF8;font-weight:700">${hd.date}</span>`;
      [['시',hd.open,'#94A3B8'],['고',hd.high,'#F87171'],['저',hd.low,'#60A5FA'],['종',hd.close,hd.change>=0?CHART_RISE_COLOR:CHART_FALL_COLOR],['등락',`${hd.change>=0?'+':''}${hd.change}(${hd.changePct.toFixed(1)}%)`,hd.change>=0?CHART_RISE_COLOR:CHART_FALL_COLOR],['거래량',`${(hd.volume/1e6).toFixed(1)}M`,'#64748B']].forEach(([l,v,c])=>{hoverContent+=`<span><span style="color:#334155">${l} </span><span style="color:${c};font-weight:700">${typeof v==='number'?v.toLocaleString():v}</span></span>`;});
      getVisibleChartMaPeriods().forEach(p=>{const cfg=getChartMaConfig(p);if(hd[`ma${p}`])hoverContent+=`<span style="color:${cfg.color};font-weight:700">${escapeHtml(cfg.label)}:${hd[`ma${p}`].toLocaleString()}</span>`;});
      if(hd.rsi!=null){const rc=hd.rsi>70?'#F87171':hd.rsi<30?'#4ADE80':'#F59E0B';hoverContent+=`<span><span style="color:#334155">RSI </span><span style="color:${rc};font-weight:700">${hd.rsi.toFixed(1)}</span></span>`;}
      if(hd.pp){hoverContent+=`<span><span style="color:#334155">PP </span><span style="color:#A78BFA;font-weight:700">${hd.pp.toLocaleString()}</span></span><span><span style="color:#334155">R1 </span><span style="color:#F87171;font-weight:700">${hd.r1?.toLocaleString()}</span></span><span><span style="color:#334155">S1 </span><span style="color:#4ADE80;font-weight:700">${hd.s1?.toLocaleString()}</span></span>`;}
    } else {
      hoverContent=`<span style="color:#334155">차트 위에 마우스를 올리면 상세 정보가 표시됩니다 (${escapeHtml(getSelectedStockName())} · ${escapeHtml(getRangeSummary())})</span>`;
    }
    bar.innerHTML=hoverContent;
  }
  const chartWrap=document.getElementById('chart-wrap');
  if(chartWrap)chartWrap.innerHTML=renderChart();
  bindChartSvgEvents();
}

function bindChartEvents(){
  const chartTab = document.getElementById('tab-chart');
  if (!chartTab) return;

  chartTab.querySelectorAll('.zoom-btn').forEach(b=>b.addEventListener('click',()=>{
    const days=parseInt(b.dataset.days, 10);
    if (!Number.isFinite(days)) return;
    applyPresetZoom(days, b.dataset.zoom || DEFAULT_ZOOM_PRESET.label);
    renderChartTab();
  }));
  chartTab.querySelectorAll('.zoom-step-btn').forEach(b=>b.addEventListener('click',()=>{
    const action = b.dataset.zoomAction;
    let changed = false;
    if (action === 'in') changed = zoomVisibleRange(ZOOM_STEP_RATIO, 0.5);
    else if (action === 'out') changed = zoomVisibleRange(1 / ZOOM_STEP_RATIO, 0.5);
    else if (action === 'reset') changed = resetVisibleRange();
    else if (action === 'double-chart') {
      state.doubleChartMode = getNextDoubleChartMode(state.doubleChartMode);
      changed = true;
    }
    if (changed) renderChartTab();
  }));
  chartTab.querySelectorAll('.ma-chip-btn').forEach(b=>b.addEventListener('click',()=>{if(toggleVisibleMa(b.dataset.ma)){renderSidebar();renderChartTab();}}));
  chartTab.querySelectorAll('.ma-card').forEach(c=>c.addEventListener('click',()=>{if(toggleVisibleMa(c.dataset.ma)){renderSidebar();renderChartTab();}}));
  const bb=document.getElementById('bb-toggle');if(bb)bb.addEventListener('change',()=>{state.showBB=bb.checked;renderChartTab();});
  const pivotTgl=document.getElementById('pivot-toggle');if(pivotTgl)pivotTgl.addEventListener('change',()=>{state.showPivot=pivotTgl.checked;renderSidebar();renderChartTab();});
  const rsiTgl=document.getElementById('rsi-toggle');if(rsiTgl)rsiTgl.addEventListener('change',()=>{state.showRSI=rsiTgl.checked;renderSidebar();renderChartTab();});
  const vol=document.getElementById('vol-toggle');if(vol)vol.addEventListener('change',()=>{state.showVolume=vol.checked;renderChartTab();});
}

function renderLibraryTab(){
  const filteredPats=PATTERNS.filter(p=>{
    if(state.filterMod!=='ALL'&&p.module!==state.filterMod)return false;
    if(state.filterGrp!=='ALL'&&p.group!==state.filterGrp)return false;
    return true;
  });
  const groups=['ALL',...new Set(PATTERNS.map(p=>p.group))];
  const modBtns=[['ALL','전체'],['A','대박주'],['B','레버리지']].map(([k,l])=>`<button class="zoom-btn" style="border:1px solid ${state.filterMod===k?'#38BDF8':'#1B2E4B'};background:${state.filterMod===k?'#1E3A5F':'transparent'};color:${state.filterMod===k?'#38BDF8':'#64748B'}" data-fmod="${k}">${l}</button>`).join('');
  const grpBtns=groups.map(g=>{const on=state.filterGrp===g;const gc=g==='ALL'?'#64748B':(GROUP_COLORS[g]||'#64748B');return`<button class="group-btn" style="border:1px solid ${on?gc:'#1B2E4B'};background:${on?gc+'20':'transparent'};color:${on?gc:'#475569'}" data-fgrp="${g}">${g==='ALL'?'전체 그룹':g.split('-')[1]||g}</button>`;}).join('');
  const cards=filteredPats.map(p=>{
    const on=state.selectedPats.includes(p.id);const st=state.stats[p.id];const gc=GROUP_COLORS[p.group]||'#64748B';const hc=hasCustom(p.id);
    return`<div class="pat-card ${on?'selected':''}" style="--card-bg:${p.color}0F;${on?`background:${p.color}0F;border-color:${p.color}`:''}" data-pid="${p.id}">
      <div class="pat-card-top"><span class="pat-card-icon">${p.icon}</span><div class="pat-card-badges"><span class="mod-badge" style="background:${p.module==='A'?'#1565C0':'#6A1B9A'}">Module ${p.module}</span><span class="grp-badge" style="background:${gc}30;color:${gc}">${p.group.split('-')[1]||p.group}</span>${hc?`<span style="font-size:8px;padding:1px 4px;border-radius:3px;background:#38BDF815;color:#38BDF8;font-weight:700">⚙커스텀</span>`:''}</div></div>
      <div class="pat-card-name" style="color:${on?p.color:'#E0E6F0'}"><span class="pat-card-no">#${p.no}</span>${p.name}</div>
      <div class="pat-card-desc">${p.desc}</div>
      <div class="pat-card-cond"><span style="color:#4ADE80">매수: </span>${p.buy}</div>
      <div class="pat-card-cond"><span style="color:#F87171">매도: </span>${p.sell}</div>
      <div class="pat-card-footer"><span>MA: <span class="pat-card-ma">${p.ma.map(x=>x+'일').join(', ')}</span></span><span class="pat-card-stats" style="color:${st?.winRate>=60?'#4ADE80':'#F87171'}">감지 ${st?.count||0}회 · 승률 ${st?.winRate||0}%</span></div>
      ${on?`<div class="pat-active-mark" style="color:${p.color}">✓ 차트 표시 중</div>`:''}
    </div>`;
  }).join('');
  document.getElementById('tab-library').innerHTML=`
    <div class="library-header"><span class="library-title">KJC 42패턴 라이브러리</span><span class="library-sub">Module A(33) + Module B(9)</span><div style="margin-left:auto;display:flex;gap:4px">${modBtns}</div></div>
    <div class="group-filters">${grpBtns}</div>
    <div class="pat-grid">${cards}</div>`;
  document.querySelectorAll('[data-fmod]').forEach(b=>b.addEventListener('click',()=>{state.filterMod=b.dataset.fmod;state.filterGrp='ALL';renderLibraryTab();}));
  document.querySelectorAll('[data-fgrp]').forEach(b=>b.addEventListener('click',()=>{state.filterGrp=b.dataset.fgrp;renderLibraryTab();}));
  document.querySelectorAll('.pat-card').forEach(c=>c.addEventListener('click',()=>{const pid=c.dataset.pid;const idx=state.selectedPats.indexOf(pid);if(idx>-1)state.selectedPats.splice(idx,1);else state.selectedPats.push(pid);renderSidebar();renderLibraryTab();if(state.activeTab==='chart')renderChartTab();}));
}

function renderOptionsTab(){
  const optPat=PATTERNS.find(p=>p.id===state.optPatId)||PATTERNS[0];
  const optDefs=PATTERN_OPTIONS[state.optPatId]||{};
  const curLocalOpts=state.localOpts[state.optPatId]||{};
  const customCount=Object.keys(state.customOpts).filter(k=>Object.keys(state.customOpts[k]||{}).length>0).length;

  const patBtns=PATTERNS.filter(p=>PATTERN_OPTIONS[p.id]).map(p=>{
    const isSel=state.optPatId===p.id;const hc=hasCustom(p.id);
    return`<button class="pat-sel-btn" style="border:1px solid ${isSel?p.color:hc?'#38BDF840':'#1B2E4B'};background:${isSel?p.color+'20':hc?'#38BDF810':'transparent'};color:${isSel?p.color:hc?'#38BDF8':'#64748B'}" data-optpat="${p.id}">${p.icon} ${p.no}. ${p.name}${hc?'<span style="color:#38BDF8;margin-left:3px">●</span>':''}</button>`;
  }).join('');

  const sliders=Object.entries(optDefs).map(([key,def])=>{
    const cur=curLocalOpts[key]!=null?curLocalOpts[key]:def.v;
    const isCustom=state.customOpts[state.optPatId]?.[key]!=null;
    const dispVal=cur<1&&cur>0?cur.toFixed(3):cur.toFixed(1);
    return`<div class="opt-row ${isCustom?'has-custom':''}">
      <div class="opt-row-header"><span class="opt-label">${def.label}</span><span class="opt-val ${isCustom?'is-custom':''}" id="oval-${key}">${dispVal}</span></div>
      <input type="range" min="${def.min}" max="${def.max}" step="${def.step}" value="${cur}" data-optkey="${key}" style="--accent:${optPat.color}">
      <div class="opt-range-labels"><span>min ${def.min}</span><span class="opt-default">기본: ${def.v<1&&def.v>0?def.v.toFixed(3):def.v}</span><span>max ${def.max}</span></div>
    </div>`;
  }).join('');

  const sigs=state.detected[optPat.id]||[];
  const sigRows=sigs.slice(0,20).map(s=>{
    const d=ALL_DATA[s.idx];const pct=Math.round(s.confidence*100);const col=s.type==='BUY'?'#4ADE80':'#F87171';
    return`<div class="sig-row"><span class="sig-idx">${s.idx}</span><span class="sig-date">${d?.date||''}</span><span class="sig-type" style="color:${col}">${s.type}</span><div class="sig-conf-wrap"><div class="sig-conf-bar" style="width:${pct*0.5}px;background:${col}"></div><span class="sig-conf-pct">${pct}%</span></div></div>`;
  }).join('');

  const topStats=PATTERNS.filter(p=>state.stats[p.id]?.count>0).sort((a,b)=>(state.stats[b.id]?.count||0)-(state.stats[a.id]?.count||0)).slice(0,15).map(p=>{const st=state.stats[p.id];return`<div class="stat-row"><span class="stat-icon">${p.icon}</span><span class="stat-name">${p.name}</span><span class="stat-cnt">${st.count}</span><div class="stat-bar-wrap"><div class="stat-bar" style="width:${st.winRate}%;background:${st.winRate>=60?'#4ADE80':'#F87171'}"></div></div><span class="stat-pct" style="color:${st.winRate>=60?'#4ADE80':'#F87171'}">${st.winRate}%</span></div>`;}).join('');

  document.getElementById('tab-options').innerHTML=`
    <div class="opts-header"><span class="opts-title">⚙️ 패턴 파라미터 설정</span><span class="opts-sub">각 조건값을 조절하면 감지 결과가 즉시 재계산됩니다</span>${customCount>0?`<span class="custom-count-badge">${customCount}개 패턴 커스텀 적용 중</span>`:''}</div>
    <div class="pat-selector-card"><div class="pat-selector">${patBtns}</div></div>
    <div class="opts-body">
      <div class="opts-panel">
        <div class="opts-panel-header">
          <span class="opts-pat-icon">${optPat.icon}</span>
          <div><div class="opts-pat-name" style="color:${optPat.color}">${optPat.no}. ${optPat.name}</div><div class="opts-pat-desc">${optPat.desc}</div></div>
          <div class="opts-actions"><button class="btn-apply" id="btn-apply">▶ 적용 후 실행</button><button class="btn-reset" id="btn-reset">기본값</button></div>
        </div>
        ${Object.keys(optDefs).length===0?'<div style="color:#475569;font-size:12px;padding:20px 0;text-align:center">이 패턴은 기본 옵션을 사용합니다</div>':`<div class="opts-grid">${sliders}</div>`}
        <div class="opts-result">
          <div class="opts-result-title">현재 감지 결과</div>
          ${sigs.length===0?'<div style="font-size:10px;color:#334155">감지된 신호 없음</div>':
          `<div class="opts-result-count">총 <b style="color:${optPat.color}">${sigs.length}</b>건 감지 <span style="font-size:9px;color:#475569">승률 <b style="color:${state.stats[optPat.id]?.winRate>=60?'#4ADE80':'#F87171'}">${state.stats[optPat.id]?.winRate||0}%</b></span></div><div class="sig-list">${sigRows}</div>`}
        </div>
      </div>
      <div class="opts-right"><div class="opts-stats-panel"><div class="opts-stats-title">전체 패턴 감지 현황</div>${topStats}</div></div>
    </div>
    <div id="pattern-chart-option-section"></div>`;

  document.querySelectorAll('[data-optpat]').forEach(b=>b.addEventListener('click',()=>{state.optPatId=b.dataset.optpat;state.localOpts[state.optPatId]=Object.assign({},state.customOpts[state.optPatId]);renderOptionsTab();}));
  document.querySelectorAll('input[type=range][data-optkey]').forEach(inp=>{
    inp.addEventListener('input',()=>{
      const key=inp.dataset.optkey;const val=parseFloat(inp.value);
      if(!state.localOpts[state.optPatId])state.localOpts[state.optPatId]={};
      state.localOpts[state.optPatId][key]=val;
      const el=document.getElementById('oval-'+key);
      if(el)el.textContent=val<1&&val>0?val.toFixed(3):val.toFixed(1);
    });
  });
  document.getElementById('btn-apply')?.addEventListener('click',()=>{
    state.customOpts[state.optPatId]=Object.assign({},state.localOpts[state.optPatId]||{});
    state.detected=detectAll(ALL_DATA,state.customOpts);
    state.stats=calcStats();
    updateHeader();renderSidebar();renderOptionsTab();
  });
  document.getElementById('btn-reset')?.addEventListener('click',()=>{
    delete state.localOpts[state.optPatId];delete state.customOpts[state.optPatId];
    state.detected=detectAll(ALL_DATA,state.customOpts);
    state.stats=calcStats();
    updateHeader();renderSidebar();renderOptionsTab();
  });
  mountPatternChartOptions();
}

function renderVerifyTab(){
  const verifyData=[
    ['MA5/20/60/120/240 계산','✅ 정확','SMA 종가 기준, HTS 동일. 2년(504일) 데이터 전구간 검증',100],
    ['RSI(14) — Wilder Smoothing','✅ v2 신규','avgGain/avgLoss Wilder 방식 완전 구현. 504봉 전체 계산',100],
    ['Pivot Points (PP/R1/R2/S1/S2)','✅ v2 신규','전일 (H+L+C)/3 기준 일별 산출. 차트 오버레이 + 호버바 표시',100],
    ['6-Factor 강도 계산기','✅ v2 신규','MA정배열+거래량+추세+RSI+SR근접+시장방향 6요소 100점제',100],
    ['고가/저가 필드 수정','✅ v2 수정','전고점·저점·넥라인 close→high/low 수정. 13개+ 패턴 반영',95],
    ['W쌍바닥 넥라인','✅ v2 수정','low 기준 저점 + high 기준 넥라인. 설계서 Python 로직과 일치',95],
    ['진골드 눌림목 양봉필수','✅ v2 수정','반등 캔들(양봉/아래꼬리) 확인 조건 추가. 음봉 오신호 차단',90],
    ['-7% 공략 RSI 추가','✅ v2 수정','RSI(14) < 35 과매도 조건 추가. 설계서 필수지표 반영',90],
    ['블랙홀 SD (SELL)','✅ v2 신규','설계서 #19 원래 의도 복원. DC + 거래량 + 이평선 하방이탈',88],
    ['피벗도형 신규','✅ v2 신규','설계서 #27 PP=(H+L+C)/3 돌파 패턴 완전 구현',88],
    ['60TT BUY 반전 수정','✅ v2 수정','설계서 L2 BUY 신호 복원. 가격이 MA60 2회 터치 후 반등',87],
    ['볼린저밴드 (BB) 패턴','✅ 정확','20일 SMA ± 2σ 표준. BB하단반등, 볼라올GC, 에너지집중 3패턴',100],
    ['골든크로스/데드크로스','✅ 정확','1봉 지연 없는 정확한 교차 포착. 6020패턴 핵심 조건',98],
    ['패턴 중복 제거','✅ 해결','5봉 간격 deduplication 적용. 인접 중복 신호 제거',95],
    ['옵션 시스템','✅ 신규','22개 패턴 파라미터 실시간 조절. 즉시 재감지 반영',100],
  ];
  const vRows=verifyData.map(([item,status,detail,score])=>`<div class="verify-row"><div class="verify-item">${item}</div><div class="verify-status">${status}</div><div class="verify-detail">${detail}</div><div class="verify-score" style="color:${score>=90?'#4ADE80':score>=75?'#FFD700':'#F87171'}">${score}</div><div class="verify-bar-wrap"><div class="verify-bar" style="width:${score}%;background:${score>=90?'#4ADE80':score>=75?'#FFD700':'#F87171'}"></div></div></div>`).join('');

  const detectRows=PATTERNS.filter(p=>state.stats[p.id]?.count>0).slice(0,15).map(p=>{const st=state.stats[p.id];return`<div class="detect-row"><span style="font-size:10px">${p.icon}</span><span class="detect-name">${p.name}</span><span class="detect-cnt">${st.count}회</span><div class="detect-bar-wrap"><div class="detect-bar" style="width:${st.winRate}%;background:${st.winRate>=60?'#4ADE80':'#F87171'}"></div></div><span class="detect-wr" style="color:${st.winRate>=60?'#4ADE80':'#F87171'}">${st.winRate}%</span></div>`;}).join('');

  const issueItems=[['이슈#1','분봉 패턴 미구현','일봉 데이터로 근사 처리. 실제 분봉 API 연동 시 별도 구현 필요','#FF9800'],['이슈#2','이중바닥 세밀화','넥라인 정확도 개선. 볼륨 프로파일 분석 추가 권장','#FF9800'],['이슈#3','실시간 시세 반영 범위','KIS WS는 헤더 현재가 중심으로 연결됨. 캔들 intraday 보정은 후속 개선 대상','#FFD700'],['이슈#4','백테스트 미실시','현재 단순 5일 후 수익률만 계산. 전체 백테스트 프레임워크 필요','#64748B']].map(([id,title,desc,color])=>`<div class="issue-item" style="background:${color}10;border-color:${color}"><div class="issue-name" style="color:${color}">${id}: ${title}</div><div class="issue-desc">${desc}</div></div>`).join('');

  const ptRows=PATTERNS.map(p=>{const st=state.stats[p.id];const hc=hasCustom(p.id);const gc=GROUP_COLORS[p.group]||'#1B2E4B';return`<div class="pt-row" style="border-color:${gc}20"><span class="pt-icon">${p.icon}</span><span class="pt-no">${p.no}.</span><span class="pt-name" style="color:${p.color}">${p.name}</span>${hc?'<span class="pt-custom">⚙</span>':''}<span class="pt-cnt">${st?.count||0}건</span><span class="pt-wr" style="color:${st?.winRate>=60?'#4ADE80':'#F87171'}">${st?.winRate||0}%</span></div>`;}).join('');

  document.getElementById('tab-verify').innerHTML=`
    <div class="verify-wrap">
      <div class="verify-panel">
        <div class="verify-title">✅ 전문가 검증 리포트 v2.0 — RSI·Pivot·6F강도·수정패턴 적용</div>
        ${vRows}
      </div>
      <div class="verify-grid">
        <div class="verify-panel"><div class="detect-title">📊 ${escapeHtml(getSelectedStockName())} 패턴 감지 결과</div>${detectRows}</div>
        <div class="verify-panel"><div class="issues-title">⚠️ 잔여 이슈 및 개선 계획</div>${issueItems}</div>
      </div>
      <div class="verify-panel"><div class="pattern-table-title">📋 42패턴 전체 일람표</div><div class="pattern-table-grid">${ptRows}</div></div>
    </div>`;
}

// ── 9. 사이드바 렌더링 ────────────────────────────────────────────────
function renderSidebar(){
  // Module filter buttons
  const mf=document.getElementById('module-filters');
  if(mf){mf.innerHTML=[['ALL','전체'],['A','대박주'],['B','레버']].map(([k,l])=>`<button class="module-btn" style="border:1px solid ${state.filterMod===k?'#38BDF8':'#1B2E4B'};background:${state.filterMod===k?'#1E3A5F':'transparent'};color:${state.filterMod===k?'#38BDF8':'#64748B'}" data-smod="${k}">${l}</button>`).join('');
  mf.querySelectorAll('[data-smod]').forEach(b=>b.addEventListener('click',()=>{state.filterMod=b.dataset.smod;renderSidebar();}));}

  // Pattern list
  const filteredPats=PATTERNS.filter(p=>{if(state.filterMod!=='ALL'&&p.module!==state.filterMod)return false;return true;});
  const pl=document.getElementById('pattern-list');
  if(pl){pl.innerHTML=filteredPats.map(p=>{
    const on=state.selectedPats.includes(p.id);const cnt=state.detected[p.id]?.length||0;const wr=state.stats[p.id]?.winRate||0;const gc=GROUP_COLORS[p.group]||'#64748B';const hc=hasCustom(p.id);
    return`<div class="pat-item" style="border-left-color:${on?p.color:'transparent'};background:${on?p.color+'0A':'transparent'}" data-pid="${p.id}"><div class="pat-item-row"><span class="pat-item-icon">${p.icon}</span><span class="pat-item-name ${on?'active':''}" style="--pat-color:${p.color}">${p.no}. ${p.name}</span>${hc?'<span class="custom-badge">커스텀</span>':''}${cnt>0?`<span class="pat-badge" style="background:${on?p.color:'#1E3A5F'};color:${on?'#000':'#78909C'}">${cnt}</span>`:''}</div><div class="pat-item-meta"><span style="color:${gc};font-weight:600">${p.group}</span>${cnt>0?`<span style="color:${wr>=60?'#4ADE80':'#F87171'}">승률${wr}%</span>`:''}</div></div>`;
  }).join('');
  pl.querySelectorAll('.pat-item').forEach(el=>el.addEventListener('click',()=>{const pid=el.dataset.pid;const idx=state.selectedPats.indexOf(pid);if(idx>-1)state.selectedPats.splice(idx,1);else state.selectedPats.push(pid);renderSidebar();if(state.activeTab==='chart')renderChartTab();}));}

  // MA toggles
  const mt=document.getElementById('ma-toggles');
  if(mt){mt.innerHTML=getEnabledChartMaOptions().map(option=>{const p=option.seriesPeriod;const cfg=getChartMaConfig(p);const on=getVisibleChartMaPeriods().includes(p);return`<div class="ma-toggle-row" style="background:${on?cfg.color+'07':'transparent'}" data-mab="${p}"><div class="ma-line-icon" style="background:${on?cfg.color:'#334155'}"></div><span class="ma-label" style="color:${on?cfg.color:'#64748B'}">${escapeHtml(cfg.label)}</span><input type="checkbox" ${on?'checked':''} style="margin-left:auto;accent-color:${cfg.color}" readonly></div>`;}).join('');
  mt.querySelectorAll('[data-mab]').forEach(el=>el.addEventListener('click',()=>{if(toggleVisibleMa(el.dataset.mab)){renderSidebar();if(state.activeTab==='chart')renderChartTab();}}));}

  const it=document.getElementById('indicator-toggles');
  if(it){it.innerHTML=`<div class="ma-toggle-row" id="bb-side-toggle"><div class="ma-line-icon" style="background:${state.showBB?'#A78BFA':'#334155'}"></div><span class="ma-label" style="color:${state.showBB?'#A78BFA':'#64748B'}">볼린저밴드</span><input type="checkbox" ${state.showBB?'checked':''} style="margin-left:auto" readonly></div><div class="ma-toggle-row" id="pivot-side-toggle"><div class="ma-line-icon" style="background:${state.showPivot?'#A78BFA':'#334155'}"></div><span class="ma-label" style="color:${state.showPivot?'#A78BFA':'#64748B'}">Pivot Points</span><input type="checkbox" ${state.showPivot?'checked':''} style="margin-left:auto" readonly></div><div class="ma-toggle-row" id="rsi-side-toggle"><div class="ma-line-icon" style="background:${state.showRSI?'#F59E0B':'#334155'}"></div><span class="ma-label" style="color:${state.showRSI?'#F59E0B':'#64748B'}">RSI(14)</span><input type="checkbox" ${state.showRSI?'checked':''} style="margin-left:auto" readonly></div><div class="ma-toggle-row" id="vol-side-toggle"><div class="ma-line-icon" style="background:${state.showVolume?'#64748B':'#334155'}"></div><span class="ma-label" style="color:${state.showVolume?'#94A3B8':'#64748B'}">거래량</span><input type="checkbox" ${state.showVolume?'checked':''} style="margin-left:auto" readonly></div>`;
  document.getElementById('bb-side-toggle')?.addEventListener('click',()=>{state.showBB=!state.showBB;renderSidebar();if(state.activeTab==='chart')renderChartTab();});
  document.getElementById('pivot-side-toggle')?.addEventListener('click',()=>{state.showPivot=!state.showPivot;renderSidebar();if(state.activeTab==='chart')renderChartTab();});
  document.getElementById('rsi-side-toggle')?.addEventListener('click',()=>{state.showRSI=!state.showRSI;renderSidebar();if(state.activeTab==='chart')renderChartTab();});
  document.getElementById('vol-side-toggle')?.addEventListener('click',()=>{state.showVolume=!state.showVolume;renderSidebar();if(state.activeTab==='chart')renderChartTab();});}

  // Selected chips
  const sc=document.getElementById('sel-count');if(sc)sc.textContent=state.selectedPats.length;
  const chips=document.getElementById('selected-chips');
  if(chips){chips.innerHTML=state.selectedPats.map(id=>{const p=PATTERNS.find(x=>x.id===id);return p?`<span class="pat-chip" style="background:${p.color}20;color:${p.color}">${p.icon}</span>`:''}).join('');}
  syncSidebarChrome();
}

// ── 10. 헤더 업데이트 ────────────────────────────────────────────────
function updateHeader(){
  updateHeaderSubline();

  const priceEl = document.getElementById('h-price');
  const retEl = document.getElementById('h-ret');
  const totalEl = document.getElementById('h-total');
  const rsiEl = document.getElementById('h-rsi');
  const pivotEl = document.getElementById('h-pivot');
  const strengthEl = document.getElementById('h-strength');

  if (!ALL_DATA.length) {
    if (priceEl) priceEl.innerHTML = '—<span class="stat-unit">원</span>';
    if (retEl) { retEl.textContent = '—'; retEl.style.color = '#64748B'; }
    if (totalEl) totalEl.textContent = '0건';
    if (rsiEl) { rsiEl.textContent = '—'; rsiEl.style.color = '#C8D6E5'; }
    if (pivotEl) pivotEl.textContent = '—';
    if (strengthEl) { strengthEl.textContent = '—'; strengthEl.style.color = '#C8D6E5'; }
    return;
  }

  const lastD = ALL_DATA[ALL_DATA.length - 1];
  const firstD = ALL_DATA[0];
  const totalRet = ((lastD.close - firstD.close) / firstD.close * 100).toFixed(2);
  const isUp = parseFloat(totalRet) >= 0;
  const priceValue = state.liveQuote && Number.isFinite(state.liveQuote.price) ? state.liveQuote.price : lastD.close;
  const diffValue = state.liveQuote && Number.isFinite(state.liveQuote.diff) ? state.liveQuote.diff : lastD.change;

  if (priceEl) {
    priceEl.innerHTML = Math.round(priceValue).toLocaleString() + '<span class="stat-unit">원</span>';
    priceEl.style.color = diffValue >= 0 ? '#4ADE80' : '#F87171';
  }
  if (retEl) {
    retEl.textContent = `${isUp?'+':''}${totalRet}%`;
    retEl.style.color = isUp ? '#4ADE80' : '#F87171';
  }
  if (totalEl) totalEl.textContent = `${Object.values(state.detected).flat().length}건`;
  if (rsiEl) {
    if (lastD.rsi != null) {
      rsiEl.textContent = lastD.rsi.toFixed(1);
      rsiEl.style.color = lastD.rsi > 70 ? '#F87171' : lastD.rsi < 30 ? '#4ADE80' : '#F59E0B';
    } else {
      rsiEl.textContent = '—';
      rsiEl.style.color = '#C8D6E5';
    }
  }
  if (pivotEl) {
    pivotEl.textContent = lastD.pp ? lastD.pp.toLocaleString() + '원' : '—';
  }
  if (strengthEl) {
    const score = calcStrength6F(ALL_DATA.length - 1);
    strengthEl.textContent = score + '점';
    strengthEl.style.color = score >= 70 ? '#4ADE80' : score >= 50 ? '#F59E0B' : '#F87171';
  }
}

// ── 11. 탭 전환 ──────────────────────────────────────────────────────
function showTab(tab){
  state.activeTab=tab;
  document.querySelectorAll('.tab-btn').forEach(b=>{b.classList.toggle('active',b.dataset.tab===tab);});
  ['chart','library','options','verify'].forEach(t=>{const el=document.getElementById('tab-'+t);if(el)el.style.display=t===tab?'':'none';});
  const main=document.getElementById('main');
  if(main){main.style.overflowY=tab==='chart'?'hidden':'auto';}
  if(tab==='chart')renderChartTab();
  else if(tab==='library')renderLibraryTab();
  else if(tab==='options')renderOptionsTab();
  else if(tab==='verify')renderVerifyTab();
}

function mountPatternChartOptions() {
  if (window.PatternChartOptions && typeof window.PatternChartOptions.mount === 'function') {
    window.PatternChartOptions.mount();
  }
}

function refreshChartMaViews() {
  renderSidebar();
  if (state.activeTab === 'chart') renderChartTab();
  else if (state.activeTab === 'options') renderOptionsTab();
}

window.PatternAppBridge = {
  apiConfig: Object.assign({}, API_CONFIG),
  chartOptionChartId: PATTERN_CHART_OPTION_ID,
  buildAppUrl: buildAppUrl,
  getActiveTab: function () {
    return state.activeTab;
  },
  getDefaultChartMaOptions: function () {
    return cloneChartMaOptions(DEFAULT_CHART_MA_OPTIONS);
  },
  getChartMaOptions: function () {
    return getChartMaOptions();
  },
  setChartMaOptions: function (list, options) {
    return setChartMaOptions(list, options);
  },
  refreshChartMaViews: refreshChartMaViews,
  mountChartOptions: mountPatternChartOptions
};

// ── 12. 초기화 ───────────────────────────────────────────────────────
(function init(){
  document.querySelectorAll('.tab-btn').forEach(b=>b.addEventListener('click',()=>showTab(b.dataset.tab)));
  bindSystemTheme();
  bindSearchUi();
  bindSidebarChrome();
  updateHeader();
  renderSidebar();
  showTab('chart');
  loadSelectedStock(DEFAULT_STOCK);
})();
