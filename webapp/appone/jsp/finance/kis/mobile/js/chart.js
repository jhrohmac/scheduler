// KIS Mobile Chart - 차트설정 DB연동 버전

(function() {
  'use strict';

  // ─── 상태 ───────────────────────────────────────────────────────────────
  const state = {
    code: window.__MOBILE.code,
    timeframe: 'd',
    chart: null,
    volumeChart: null,
    chartCache: Object.create(null),
    chartReq: null,
    priceReq: null,
    dbOptions: [],           // MA 타입
    featureDbOptions: [],    // FEATURE 타입
    crossDbOptions: [],      // CROSS 타입
    fromDate: '',
    endDate: '',
    lastChartError: ''
  };

  // ─── 기본값 상수 ─────────────────────────────────────────────────────────
  const DEFAULT_MA = [
    { seriesKey:'ma5',   seriesLabel:'5일선',   seriesPeriod:5,   displayOrder:1, enabledYn:'Y', lineWidth:2.5, seriesColor:'#26a69a' },
    { seriesKey:'ma20',  seriesLabel:'20일선',  seriesPeriod:20,  displayOrder:2, enabledYn:'Y', lineWidth:2.5, seriesColor:'#4caf50' },
    { seriesKey:'ma60',  seriesLabel:'60일선',  seriesPeriod:60,  displayOrder:3, enabledYn:'Y', lineWidth:2.5, seriesColor:'#ff9800' },
    { seriesKey:'ma120', seriesLabel:'120일선', seriesPeriod:120, displayOrder:4, enabledYn:'Y', lineWidth:2.5, seriesColor:'#9c27b0' },
    { seriesKey:'ma240', seriesLabel:'240일선', seriesPeriod:240, displayOrder:5, enabledYn:'Y', lineWidth:2.5, seriesColor:'#607d8b' }
  ];

  const DEFAULT_FEATURE = [
    { seriesKey:'volumeEnabled',      seriesLabel:'거래량',               enabledYn:'Y' },
    { seriesKey:'doubleChartEnabled', seriesLabel:'월봉 오버레이(더블차트)', enabledYn:'N' },
    { seriesKey:'monthLinesEnabled',  seriesLabel:'월/년 구분선',          enabledYn:'N' },
    { seriesKey:'highLowEnabled',     seriesLabel:'전고/전저',             enabledYn:'N' },
    { seriesKey:'maSrEnabled',        seriesLabel:'MA 지지/저항',          enabledYn:'N' }
  ];

  const DEFAULT_CROSS = [
    { seriesKey:'crossSignals', seriesLabel:'골든/데드', enabledYn:'N' },
    { seriesKey:'crossShort',   seriesLabel:'단기선', seriesPeriod:60,  enabledYn:'Y' },
    { seriesKey:'crossLong',    seriesLabel:'장기선', seriesPeriod:240, enabledYn:'Y' }
  ];

  // MA 라인 두께 정규화
  function normalizeMaLineWidth(v) {
    var n = parseFloat(v);
    if (!isFinite(n) || n <= 0) return 2.5;
    n = Math.round(n * 2) / 2; // 0.5 단위
    return Math.min(Math.max(n, 0.5), 6);
  }

  // ─── DB Save ─────────────────────────────────────────────────────────────
  function saveOptionsToDb(seriesType, list) {
    var saveUrl = window.__MOBILE && window.__MOBILE.urls && window.__MOBILE.urls.chartOptionSave;
    if (!saveUrl) return;
    var payload = new URLSearchParams();
    payload.set('chartId', 'KIS_ITEMCHART');
    payload.set('seriesType', seriesType);
    payload.set('optionsJson', JSON.stringify(list));
    fetch(saveUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: payload.toString(),
      credentials: 'include'
    }).catch(function(){});
  }

  var maAutoTimer = null;
  function scheduleMaAutoSave() {
    if (maAutoTimer) clearTimeout(maAutoTimer);
    maAutoTimer = setTimeout(function() {
      var result = collectMaRows();
      if (!result.hasInvalid) {
        state.dbOptions = result.list;
        saveOptionsToDb('MA', result.list);
      }
    }, 300);
  }

  // ─── DB Load ─────────────────────────────────────────────────────────────
  function loadOptionsFromDb(seriesType) {
    var optionUrl = window.__MOBILE && window.__MOBILE.urls && window.__MOBILE.urls.chartOptionData;
    if (!optionUrl) return Promise.resolve([]);
    var url = optionUrl + '?chartId=KIS_ITEMCHART&seriesType=' + encodeURIComponent(seriesType);
    return fetch(url, { credentials: 'include' })
      .then(function(r){ return r.json(); })
      .then(function(json){
        return (json && Array.isArray(json.data)) ? json.data : [];
      })
      .catch(function(){ return []; });
  }

  function loadAllOptions() {
    Promise.all([
      loadOptionsFromDb('MA'),
      loadOptionsFromDb('FEATURE'),
      loadOptionsFromDb('CROSS')
    ]).then(function(results) {
      var maList      = results[0].length ? results[0] : DEFAULT_MA.slice();
      var featureList = results[1].length ? results[1] : DEFAULT_FEATURE.slice();
      var crossList   = results[2].length ? results[2] : DEFAULT_CROSS.slice();

      state.dbOptions        = maList;
      state.featureDbOptions = featureList;
      state.crossDbOptions   = crossList;

      renderMaRows(maList);
      renderFeatureToggles(featureList);
      renderCrossSignals(crossList);
      renderMaBadges(maList);

      // 이미 캐시된 차트 데이터가 있으면 재렌더
      var cacheKey = state.code + '|' + state.timeframe;
      var cached = state.chartCache[cacheKey];
      if (cached && cached.length) renderChart(cached);
    });
  }

  // ─── Feature Toggle UI ───────────────────────────────────────────────────
  function getFeatureEnabled(key, defaultValue) {
    var list = state.featureDbOptions.length ? state.featureDbOptions : DEFAULT_FEATURE;
    var found = list.filter(function(o){ return o.seriesKey === key; })[0];
    if (!found) return defaultValue !== undefined ? defaultValue : false;
    return String(found.enabledYn).toUpperCase() === 'Y';
  }

  function renderFeatureToggles(list) {
    var map = {};
    list.forEach(function(o){ map[o.seriesKey] = String(o.enabledYn).toUpperCase() === 'Y'; });

    var keys = {
      'optVolume':      'volumeEnabled',
      'optDoubleChart': 'doubleChartEnabled',
      'optMonthLines':  'monthLinesEnabled',
      'optHighLow':     'highLowEnabled',
      'optMaSr':        'maSrEnabled'
    };
    Object.keys(keys).forEach(function(elId) {
      var el = document.getElementById(elId);
      if (el) {
        var k = keys[elId];
        el.checked = (map[k] !== undefined) ? map[k] : false;
      }
    });
  }

  function bindFeatureToggles() {
    var keys = {
      'optVolume':      'volumeEnabled',
      'optDoubleChart': 'doubleChartEnabled',
      'optMonthLines':  'monthLinesEnabled',
      'optHighLow':     'highLowEnabled',
      'optMaSr':        'maSrEnabled'
    };
    Object.keys(keys).forEach(function(elId) {
      var el = document.getElementById(elId);
      if (!el) return;
      el.addEventListener('change', function() {
        var seriesKey = keys[elId];
        var enabled = this.checked;
        // state 업데이트
        var found = false;
        state.featureDbOptions = state.featureDbOptions.map(function(o) {
          if (o.seriesKey === seriesKey) { found = true; return Object.assign({}, o, { enabledYn: enabled ? 'Y' : 'N' }); }
          return o;
        });
        if (!found) {
          state.featureDbOptions.push({ seriesKey: seriesKey, enabledYn: enabled ? 'Y' : 'N' });
        }
        // 즉시 저장
        saveOptionsToDb('FEATURE', state.featureDbOptions.length ? state.featureDbOptions : DEFAULT_FEATURE.slice());
        // 차트 재렌더 (거래량 등 즉시 반영)
        var cacheKey = state.code + '|' + state.timeframe;
        var list = state.chartCache[cacheKey];
        if (list && list.length) renderChart(list);
      });
    });
  }

  // ─── Cross Signal UI ─────────────────────────────────────────────────────
  function updateCrossLabel() {
    var shortEl = document.getElementById('optCrossShort');
    var longEl  = document.getElementById('optCrossLong');
    var labelEl = document.getElementById('crossPairLabel');
    if (shortEl && longEl && labelEl) {
      labelEl.textContent = '(' + shortEl.value + '/' + longEl.value + ')';
    }
  }

  function getCrossOptionList() {
    var signalEl = document.getElementById('optCrossSignals');
    var shortEl  = document.getElementById('optCrossShort');
    var longEl   = document.getElementById('optCrossLong');
    return [
      { seriesKey:'crossSignals', seriesLabel:'골든/데드', enabledYn: (signalEl && signalEl.checked) ? 'Y' : 'N' },
      { seriesKey:'crossShort',   seriesLabel:'단기선', seriesPeriod: shortEl ? Number(shortEl.value) : 60,  enabledYn:'Y' },
      { seriesKey:'crossLong',    seriesLabel:'장기선', seriesPeriod: longEl  ? Number(longEl.value)  : 240, enabledYn:'Y' }
    ];
  }

  function renderCrossSignals(list) {
    var map = {};
    list.forEach(function(o){ map[o.seriesKey] = o; });

    var signalEl = document.getElementById('optCrossSignals');
    var shortEl  = document.getElementById('optCrossShort');
    var longEl   = document.getElementById('optCrossLong');

    if (signalEl && map.crossSignals) {
      signalEl.checked = String(map.crossSignals.enabledYn).toUpperCase() === 'Y';
    }
    if (shortEl && map.crossShort && map.crossShort.seriesPeriod) {
      shortEl.value = String(map.crossShort.seriesPeriod);
    }
    if (longEl && map.crossLong && map.crossLong.seriesPeriod) {
      longEl.value = String(map.crossLong.seriesPeriod);
    }
    updateCrossLabel();
  }

  function bindCrossSignals() {
    var signalEl = document.getElementById('optCrossSignals');
    var shortEl  = document.getElementById('optCrossShort');
    var longEl   = document.getElementById('optCrossLong');

    function saveCross() {
      var list = getCrossOptionList();
      state.crossDbOptions = list;
      saveOptionsToDb('CROSS', list);
    }

    if (signalEl) signalEl.addEventListener('change', saveCross);
    if (shortEl)  shortEl.addEventListener('change',  function(){ updateCrossLabel(); saveCross(); });
    if (longEl)   longEl.addEventListener('change',   function(){ updateCrossLabel(); saveCross(); });
  }

  // ─── MA Row UI ───────────────────────────────────────────────────────────
  function createMaRow(opt, idx) {
    opt = opt || {};
    var enabledYn  = String(opt.enabledYn || 'Y').toUpperCase() === 'Y';
    var label      = opt.seriesLabel  || '';
    var period     = opt.seriesPeriod || '';
    var color      = opt.seriesColor  || '#999999';
    var lineWidth  = normalizeMaLineWidth(opt.lineWidth);
    var seriesKey  = opt.seriesKey    || '';

    // color 정규화
    if (color && color.charAt(0) !== '#') color = '#' + color;
    if (!/^#[0-9a-fA-F]{6}$/.test(color)) color = '#999999';

    var row = document.createElement('div');
    row.className = 'ma-row';
    row.dataset.seriesKey = seriesKey;

    // 체크박스
    var chk = document.createElement('input');
    chk.type = 'checkbox';
    chk.className = 'ma-enabled';
    chk.checked = enabledYn;

    // 순번
    var order = document.createElement('span');
    order.className = 'ma-order';
    order.textContent = (idx + 1) + '번';

    // 라벨 입력
    var labelInput = document.createElement('input');
    labelInput.type = 'text';
    labelInput.className = 'ma-label';
    labelInput.value = label;
    labelInput.placeholder = '이름';

    // 기간 입력
    var periodInput = document.createElement('input');
    periodInput.type = 'number';
    periodInput.className = 'ma-period';
    periodInput.value = period;
    periodInput.min = '1';
    periodInput.placeholder = '기간';

    // 두께 입력
    var widthInput = document.createElement('input');
    widthInput.type = 'number';
    widthInput.className = 'ma-width';
    widthInput.value = lineWidth;
    widthInput.min = '0.5';
    widthInput.max = '6';
    widthInput.step = '0.5';

    // 색상 (hidden)
    var colorHidden = document.createElement('input');
    colorHidden.type = 'hidden';
    colorHidden.className = 'ma-color';
    colorHidden.value = color;

    // 색상 버튼 (swatch + color picker)
    var colorBtn = document.createElement('button');
    colorBtn.type = 'button';
    colorBtn.className = 'ma-colorbtn';
    colorBtn.title = '색상 선택';

    var swatch = document.createElement('span');
    swatch.className = 'ma-swatch';
    swatch.style.background = color;

    var picker = document.createElement('input');
    picker.type = 'color';
    picker.className = 'ma-color-picker';
    picker.value = color;
    picker.addEventListener('input', function() {
      var v = picker.value || '#999999';
      colorHidden.value = v;
      swatch.style.background = v;
      scheduleMaAutoSave();
    });
    picker.addEventListener('change', function() {
      var v = picker.value || '#999999';
      colorHidden.value = v;
      swatch.style.background = v;
      scheduleMaAutoSave();
    });

    colorBtn.appendChild(swatch);
    colorBtn.appendChild(picker);

    // 삭제 버튼
    var delBtn = document.createElement('button');
    delBtn.type = 'button';
    delBtn.className = 'btn-delete-ma';
    delBtn.textContent = '삭제';
    delBtn.addEventListener('click', function() {
      if (confirm('해당 MA 라인을 삭제하시겠습니까?')) {
        row.parentNode && row.parentNode.removeChild(row);
        reindexMaRows();
        scheduleMaAutoSave();
      }
    });

    // 자동 저장 이벤트 (label/period/width/enabled 변경)
    [chk, labelInput, periodInput, widthInput].forEach(function(el) {
      el.addEventListener('change', scheduleMaAutoSave);
    });
    labelInput.addEventListener('input', scheduleMaAutoSave);
    periodInput.addEventListener('input', scheduleMaAutoSave);
    widthInput.addEventListener('input', scheduleMaAutoSave);

    row.appendChild(chk);
    row.appendChild(order);
    row.appendChild(labelInput);
    row.appendChild(periodInput);
    row.appendChild(widthInput);
    row.appendChild(colorHidden);
    row.appendChild(colorBtn);
    row.appendChild(delBtn);

    return row;
  }

  function reindexMaRows() {
    var rows = document.querySelectorAll('#maRowList .ma-row');
    rows.forEach(function(row, i) {
      var order = row.querySelector('.ma-order');
      if (order) order.textContent = (i + 1) + '번';
    });
  }

  function renderMaRows(list) {
    var container = document.getElementById('maRowList');
    if (!container) return;
    container.innerHTML = '';

    var sorted = (Array.isArray(list) ? list.slice() : []).sort(function(a, b) {
      var oa = a.displayOrder != null ? a.displayOrder : (a.seriesPeriod || 0);
      var ob = b.displayOrder != null ? b.displayOrder : (b.seriesPeriod || 0);
      return oa - ob;
    });

    sorted.forEach(function(opt, i) {
      container.appendChild(createMaRow(opt, i));
    });
  }

  function collectMaRows() {
    var rows = document.querySelectorAll('#maRowList .ma-row');
    var list = [];
    var hasInvalid = false;

    rows.forEach(function(row, idx) {
      var label     = (row.querySelector('.ma-label')   || {}).value || '';
      var periodRaw = (row.querySelector('.ma-period')  || {}).value || '';
      var widthRaw  = (row.querySelector('.ma-width')   || {}).value || '';
      var colorRaw  = (row.querySelector('.ma-color')   || {}).value || '#999999';
      var enabled   = !!(row.querySelector('.ma-enabled') || {}).checked;
      var seriesKey = row.dataset.seriesKey || '';

      label    = label.trim();
      var period = parseInt(periodRaw, 10);

      if (!label || !period || period <= 0) { hasInvalid = true; return; }

      var color = colorRaw.trim();
      if (!color || color.charAt(0) !== '#') color = '#' + color;
      if (!/^#[0-9a-fA-F]{6}$/.test(color)) color = '#999999';

      if (!seriesKey) {
        seriesKey = 'ma' + period;
        row.dataset.seriesKey = seriesKey;
      }

      list.push({
        seriesKey:    seriesKey,
        seriesLabel:  label,
        seriesPeriod: period,
        seriesColor:  color,
        lineWidth:    normalizeMaLineWidth(widthRaw),
        enabledYn:    enabled ? 'Y' : 'N',
        displayOrder: idx + 1
      });
    });

    return { list: list, hasInvalid: hasInvalid };
  }

  function addMaRow() {
    var container = document.getElementById('maRowList');
    if (!container) return;
    var count = container.querySelectorAll('.ma-row').length;
    var newOpt = { seriesKey:'', seriesLabel:'', seriesPeriod:'', lineWidth:2.5, seriesColor:'#999999', enabledYn:'Y' };
    container.appendChild(createMaRow(newOpt, count));
  }

  // ─── MA 뱃지 업데이트 ─────────────────────────────────────────────────────
  function renderMaBadges(list) {
    var maInfo = document.querySelector('.ma-info');
    if (!maInfo) return;

    // 기존 MA 뱃지만 제거 (label은 유지)
    var oldBadges = maInfo.querySelectorAll('.ma');
    oldBadges.forEach(function(el) { el.parentNode.removeChild(el); });

    var enabled = (Array.isArray(list) ? list : []).filter(function(o) {
      return String(o.enabledYn).toUpperCase() === 'Y';
    }).sort(function(a, b) {
      return (a.displayOrder || a.seriesPeriod || 0) - (b.displayOrder || b.seriesPeriod || 0);
    });

    enabled.forEach(function(opt) {
      var span = document.createElement('span');
      span.className = 'ma';
      span.textContent = String(opt.seriesPeriod || '');
      var c = opt.seriesColor || '#999';
      // 배경색: 색상의 투명 버전
      span.style.color = c;
      span.style.background = c + '22';
      maInfo.appendChild(span);
    });
  }

  // ─── 이벤트 바인딩 ────────────────────────────────────────────────────────
  function bindRangeOptions() {
    var fromEl  = document.getElementById('fromDate');
    var endEl   = document.getElementById('endDate');
    var applyBtn = document.getElementById('applyDateRange');
    if (!fromEl || !endEl || !applyBtn) return;

    applyBtn.addEventListener('click', function() {
      state.fromDate = (fromEl.value || '').trim();
      state.endDate  = (endEl.value  || '').trim();
      var cacheKey = state.code + '|' + state.timeframe;
      var list = state.chartCache[cacheKey];
      if (list && list.length) renderChart(list);
      // 날짜 범위는 MA 저장과 별개로 처리 (필요 시 추가)
    });
  }

  function bindActionButtons() {
    // 적용 버튼
    var applyBtn = document.getElementById('btnApplyMaOptions');
    if (applyBtn) {
      applyBtn.addEventListener('click', function() {
        var result = collectMaRows();
        if (result.hasInvalid) {
          alert('라벨과 기간을 모두 입력해주세요.');
          return;
        }
        state.dbOptions = result.list;
        saveOptionsToDb('MA', result.list);
        renderMaBadges(result.list);
        var cacheKey = state.code + '|' + state.timeframe;
        var cached = state.chartCache[cacheKey];
        if (cached && cached.length) renderChart(cached);
        // 패널 닫기
        var panel = document.getElementById('chartOptions');
        if (panel) panel.classList.add('hidden');
      });
    }

    // 기본값 버튼 (UI만 교체, 저장 없음)
    var defaultBtn = document.getElementById('btnMaDefault');
    if (defaultBtn) {
      defaultBtn.addEventListener('click', function() {
        renderMaRows(DEFAULT_MA);
      });
    }

    // 취소 버튼
    var cancelBtn = document.getElementById('btnCancelOptions');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', function() {
        var panel = document.getElementById('chartOptions');
        if (panel) panel.classList.add('hidden');
      });
    }

    // MA 라인 추가 버튼
    var addBtn = document.getElementById('btnAddMaLine');
    if (addBtn) {
      addBtn.addEventListener('click', addMaRow);
    }
  }

  // ─── 초기화 ───────────────────────────────────────────────────────────────
  function init() {
    bindResize();
    bindFeatureToggles();
    bindCrossSignals();
    bindRangeOptions();
    bindActionButtons();
    loadAllOptions();
    loadAll();
  }

  // ─── 로딩 유틸 ───────────────────────────────────────────────────────────
  function setLoading(on) {
    var el = document.querySelector('.current-price');
    if (!el) return;
    if (on) el.textContent = '로딩...';
  }

  function loadAll() {
    setLoading(true);
    loadCurrentPrice();
    resolveNameFromSearch(state.code);
    loadChartData();
  }

  function yyyymmdd(d) {
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    return y + '' + m + '' + day;
  }

  function periodCode(tf) {
    var t = tf || 'd';
    if (t === 'm') return 'T';
    if (t === 'M') return 'M';
    if (t === 'w') return 'W';
    if (t === 'y') return 'Y';
    return 'D';
  }

  function fromDateByTf(now, tf) {
    var d = new Date(now.getTime());
    var t = tf || 'd';
    if (t === 'm')      d.setDate(d.getDate() - 30);
    else if (t === 'd') d.setFullYear(d.getFullYear() - 10);
    else if (t === 'w') d.setFullYear(d.getFullYear() - 15);
    else if (t === 'M') d.setFullYear(d.getFullYear() - 15);
    else if (t === 'y') d.setFullYear(d.getFullYear() - 20);
    else                d.setFullYear(d.getFullYear() - 10);
    return d;
  }

  // 차트 영역 동적 높이 계산 (화면 꽉 채우기)
  function getChartHeights() {
    var totalH = window.innerHeight;
    var usedTop = 0;
    ['.toolbar', '.market-switch', '.features', '.ma-info'].forEach(function(sel) {
      var el = document.querySelector(sel);
      if (el && el.offsetParent !== null) usedTop += el.getBoundingClientRect().height;
    });
    // .timeframe(fixed bottom:62px 위치, ~38px) + .bottom-nav(~62px)
    var fixedBottom = 100;
    var available = totalH - usedTop - fixedBottom;
    available = Math.max(available, 260);
    var volH = Math.round(available * 0.22);
    volH = Math.max(volH, 60);
    var priceH = available - volH;
    return { price: priceH, volume: volH };
  }

  function resetChartArea() {
    if (state.chart) {
      try { state.chart.destroy(); } catch (e) {}
      state.chart = null;
    }
    if (state.volumeChart) {
      try { state.volumeChart.destroy(); } catch (e) {}
      state.volumeChart = null;
    }
    var priceEl = document.getElementById('priceChart');
    if (priceEl) priceEl.innerHTML = '';
    var volumeEl = document.getElementById('volumeChart');
    if (volumeEl) {
      volumeEl.innerHTML = '';
      volumeEl.style.display = 'none';
    }
  }

  function showChartError(message) {
    var text = (message || '차트 데이터를 불러오지 못했습니다.').trim();
    resetChartArea();
    if (state.lastChartError !== text) {
      state.lastChartError = text;
      alert(text);
    }
  }

  function loadChartData() {
    var cacheKey = state.code + '|' + state.timeframe;
    if (state.chartCache[cacheKey]) {
      var cachedList = state.chartCache[cacheKey];
      state.lastChartError = '';
      patchPriceFromChart(cachedList);
      if (!cachedList.length) {
        resetChartArea();
      } else {
        renderChart(cachedList);
      }
      setLoading(false);
      return;
    }

    var now = new Date();
    var toDate   = yyyymmdd(now);
    var fromDate = yyyymmdd(fromDateByTf(now, state.timeframe));

    var params = new URLSearchParams({
      in_stockCode:    state.code,
      in_fromDate:     fromDate,
      in_toDate:       toDate,
      in_periodDivCode: periodCode(state.timeframe),
      in_orgAdjPrc:    '0'
    });

    var url = window.__MOBILE.urls.chartData + '?' + params.toString();

    if (state.chartReq) state.chartReq.abort();
    state.chartReq = new AbortController();

    if (window.mobileProgress) window.mobileProgress.start();
    fetch(url, { signal: state.chartReq.signal })
      .then(function(r){ return r.json(); })
      .then(function(json) {
        if (!json || String(json.system_code || '') !== '0000') {
          throw new Error((json && (json.system_msg || json.result_msg)) || '차트 데이터를 불러오지 못했습니다.');
        }
        var list = (json && Array.isArray(json.data)) ? json.data : [];
        state.lastChartError = '';
        state.chartCache[cacheKey] = list;

        if (list.length) {
          var firstRaw = Number(list[0].date || list[0].DATE || 0);
          var lastRaw  = Number(list[list.length - 1].date || list[list.length - 1].DATE || 0);
          var lastTs   = lastRaw  > 2000000000000 ? lastRaw  : lastRaw  > 0 ? lastRaw  : 0;
          var fromEl   = document.getElementById('fromDate');
          var endEl    = document.getElementById('endDate');
          if (endEl && !endEl.value && lastTs) {
            endEl.value = new Date(lastTs).toISOString().slice(0, 10);
            state.endDate = endEl.value;
          }
          if (fromEl && !fromEl.value && lastTs) {
            var d = new Date(lastTs);
            d.setFullYear(d.getFullYear() - 1);
            fromEl.value = d.toISOString().slice(0, 10);
            state.fromDate = fromEl.value;
          }
        }

        patchPriceFromChart(list);
        if (!list.length) {
          resetChartArea();
          return;
        }
        renderChart(list);
      })
      .catch(function(err) {
        if (err && err.name === 'AbortError') return;
        console.error('차트 로드 실패', err);
        showChartError((err && err.message) ? err.message : '차트 데이터를 불러오지 못했습니다.');
      })
      .finally(function() {
        state.chartReq = null;
        setLoading(false);
        if (window.mobileProgress) window.mobileProgress.done();
      });
  }

  function resolveNameFromSearch(code) {
    if (!window.__MOBILE || !window.__MOBILE.urls || !window.__MOBILE.urls.search) return;
    var url = window.__MOBILE.urls.search + '?in_stockCode=' + encodeURIComponent(code || state.code);
    if (window.mobileProgress) window.mobileProgress.start();
    fetch(url, { credentials: 'include' })
      .then(function(r){ return r.json(); })
      .then(function(json) {
        var list = (json && Array.isArray(json.data)) ? json.data : [];
        if (!list.length) return;
        var it = list[0] || {};
        var name   = it.stockKoName || it.stock_ko_name || it.stockName || it.stock_name || '-';
        var market = it.stockMarket || it.stock_market || '';
        var nameEl = document.querySelector('.stock-name');
        var codeEl = document.querySelector('.stock-code');
        var logoEl = document.getElementById('stockLogo');
        if (nameEl && (nameEl.textContent || '').trim() === '-') nameEl.textContent = name;
        if (codeEl) codeEl.textContent = (code || state.code) + (market ? (' ' + marketLabel(market)) : '');
        if (logoEl) {
          logoEl.textContent = (name || code || '-').substring(0, 1);
          logoEl.classList.add('logo-placeholder');
        }
      })
      .catch(function(){})
      .finally(function() {
        if (window.mobileProgress) window.mobileProgress.done();
      });
  }

  function marketLabel(v) {
    var m = String(v || '').toUpperCase();
    if (m === 'STK' || m === 'KOSPI' || m === 'KS') return '코스피';
    if (m === 'KSQ' || m === 'KOSDAQ' || m === 'KQ') return '코스닥';
    if (m === 'NASDAQ') return '나스닥';
    if (m === 'NYSE') return '뉴욕';
    return v || '';
  }

  function loadCurrentPrice() {
    var params = new URLSearchParams({ in_stockCode: state.code });
    var url = window.__MOBILE.urls.currentPrice + '?' + params.toString();

    if (state.priceReq) state.priceReq.abort();
    state.priceReq = new AbortController();

    if (window.mobileProgress) window.mobileProgress.start();
    fetch(url, { signal: state.priceReq.signal })
      .then(function(r){ return r.json(); })
      .then(function(json) {
        var sd = (json && (json.singleData || json.single_data)) || null;
        if (!sd) return;

        var name   = sd.stockName || sd.stock_name || sd.hnm || '-';
        var code   = sd.stockCode || sd.stock_code || state.code;
        var market = sd.market || sd.mrkt || '';
        var nameEl = document.querySelector('.stock-name');
        var codeEl = document.querySelector('.stock-code');
        var logoEl = document.getElementById('stockLogo');
        if (nameEl) nameEl.textContent = name && name !== '-' ? name : '-';
        if (codeEl) codeEl.textContent = code + (market ? (' ' + marketLabel(market)) : '');
        if (logoEl) {
          logoEl.textContent = (name && name !== '-' ? name : code).substring(0, 1);
          logoEl.classList.add('logo-placeholder');
        }

        if (!name || name === '-') resolveNameFromSearch(code);

        var price = parseFloat(sd.currentPrice || sd.current_price || 0) || 0;
        var diff  = parseFloat(sd.diff || sd.priceChangeAmt || sd.price_change_amt || 0) || 0;
        var pct   = parseFloat(sd.diffRate || sd.priceChangeRate || sd.price_change_rate || 0) || 0;
        updatePriceUI(price, diff, pct);
      })
      .catch(function(e) {
        if (e && e.name === 'AbortError') return;
      })
      .finally(function() {
        state.priceReq = null;
        if (window.mobileProgress) window.mobileProgress.done();
      });
  }

  function patchPriceFromChart(list) {
    if (!list || list.length < 2) return;
    var last      = list[list.length - 1];
    var prev      = list[list.length - 2];
    var price     = Number(last.close || last.CLOSE || 0);
    var prevClose = Number(prev.close || prev.CLOSE || 0);
    if (!price || !prevClose) return;
    var diff = price - prevClose;
    var pct  = (prevClose > 0) ? (diff / prevClose * 100.0) : 0;
    updatePriceUI(price, diff, pct);
  }

  function updatePriceUI(price, diff, pct) {
    var inlinePriceEl  = document.getElementById('marketPriceInline');
    var inlineChangeEl = document.getElementById('marketChangeInline');
    var priceEl        = document.querySelector('.current-price');
    var changeEl       = document.querySelector('.change');

    var p = Math.round(price || 0);
    var d = Math.round(diff  || 0);
    var priceText = p > 0 ? p.toLocaleString() : '-';
    var pctText   = (pct >= 0 ? '+' : '') + (Number(pct || 0)).toFixed(2) + '%';
    var diffText  = (d >= 0 ? '+' : '') + d.toLocaleString();

    if (inlinePriceEl)  inlinePriceEl.textContent  = priceText;
    if (priceEl)        priceEl.textContent         = priceText;

    if (inlineChangeEl) {
      inlineChangeEl.textContent = pctText + ' · ' + diffText;
      inlineChangeEl.className   = 'market-change-inline ' + (d >= 0 ? 'up' : 'down');
    }

    if (changeEl) {
      var percentEl = changeEl.querySelector('.percent');
      var diffEl    = changeEl.querySelector('.diff');
      if (percentEl) percentEl.textContent = pctText;
      if (diffEl)    diffEl.textContent    = diffText;
      changeEl.className = 'change ' + (d >= 0 ? 'up' : 'down');
    }

    if (inlinePriceEl) inlinePriceEl.style.color = d >= 0 ? '#e53935' : '#1e88e5';
    if (priceEl)       priceEl.style.color        = d >= 0 ? '#e53935' : '#1e88e5';
  }

  // ─── MA 계산 ─────────────────────────────────────────────────────────────
  function buildMA(ohlc, period) {
    var out = [], sum = 0, q = [];
    for (var i = 0; i < ohlc.length; i++) {
      var p = Number(ohlc[i][4]);
      sum += p;
      q.push(p);
      if (q.length > period) sum -= q.shift();
      if (q.length === period) out.push([ohlc[i][0], +(sum / period).toFixed(2)]);
    }
    return out;
  }

  // ─── 리사이즈 ─────────────────────────────────────────────────────────────
  function bindResize() {
    var timer = null;
    window.addEventListener('resize', function() {
      if (timer) clearTimeout(timer);
      timer = setTimeout(function() {
        try {
          if (state.chart)       state.chart.reflow();
          if (state.volumeChart) state.volumeChart.reflow();
        } catch (e) {}
      }, 120);
    });
  }

  // ─── 데이터 정규화 ────────────────────────────────────────────────────────
  function normalizeSeries(list) {
    var ohlc = [], volume = [];
    list.forEach(function(item) {
      var raw = Number(item.date || item.DATE || 0);
      var ts  = raw > 2000000000000 ? raw : raw > 0 ? raw : 0;
      if (!ts) return;
      var o = Number(item.open   || item.OPEN   || 0);
      var h = Number(item.high   || item.HIGH   || 0);
      var l = Number(item.low    || item.LOW    || 0);
      var c = Number(item.close  || item.CLOSE  || 0);
      var v = Number(item.volume || item.VOLUME || 0);
      ohlc.push([ts, o, h, l, c]);
      // 종가 >= 시가: 상승(빨간), 종가 < 시가: 하락(파란)
      volume.push({ x: ts, y: v, color: c >= o ? '#e53935' : '#1e88e5' });
    });
    return { ohlc: ohlc, volume: volume };
  }

  function toDayTs(dateStr, end) {
    if (!dateStr) return NaN;
    var t = new Date(dateStr + (end ? 'T23:59:59' : 'T00:00:00')).getTime();
    return Number.isFinite(t) ? t : NaN;
  }

  function filterByDateRange(list) {
    return list || [];
  }

  function applyRangeExtremes(chart, fromDate, endDate) {
    if (!chart || !chart.xAxis || !chart.xAxis[0]) return;
    var min = toDayTs(fromDate, false);
    var max = toDayTs(endDate, true);
    if (!Number.isFinite(min) && !Number.isFinite(max)) return;
    chart.xAxis[0].setExtremes(
      Number.isFinite(min) ? min : null,
      Number.isFinite(max) ? max : null,
      true, false
    );
  }

  // ─── 월봉 오버레이 빌드 ──────────────────────────────────────────────────
  // { data: [[centerTs,o,h,l,c],...], boundaries: [{firstTs,lastTs},...] } 반환
  function buildMonthlyOverlay(ohlc) {
    if (!ohlc || !ohlc.length) return { data: [], boundaries: [] };
    var months = [];
    var currentKey = null;
    var agg = null;

    ohlc.forEach(function(row) {
      var d = new Date(row[0]);
      var key = d.getFullYear() * 100 + d.getMonth();
      if (key !== currentKey) {
        if (agg) {
          agg.centerTs = Math.round((agg.firstTs + agg.lastTs) / 2);
          months.push(agg);
        }
        currentKey = key;
        agg = { open: row[1], high: row[2], low: row[3], close: row[4],
                firstTs: row[0], lastTs: row[0] };
      } else {
        if (row[2] > agg.high) agg.high = row[2];
        if (row[3] < agg.low)  agg.low  = row[3];
        agg.close  = row[4];
        agg.lastTs = row[0];
      }
    });
    if (agg) { agg.centerTs = Math.round((agg.firstTs + agg.lastTs) / 2); months.push(agg); }

    return {
      data: months.map(function(m) {
        return [m.centerTs, m.open, m.high, m.low, m.close];
      }),
      boundaries: months.map(function(m) {
        return { firstTs: m.firstTs, lastTs: m.lastTs };
      })
    };
  }

  // ─── 월봉 오버레이 폭 업데이트 ──────────────────────────────────────────
  // 각 월봉 캔들의 SVG path를 월 시작~다음달 시작 픽셀 폭으로 재드로잉
  function updateMonthOverlayWidth(chart, boundaries) {
    if (!chart || !boundaries || !boundaries.length) return;
    var ms = chart.get('monthOverlay');
    if (!ms || !ms.points || !ms.points.length) return;
    var xAxis = chart.xAxis && chart.xAxis[0];
    if (!xAxis) return;

    var lw = 2;
    var crisp = (Math.round(lw) % 2) / 2;

    for (var i = 0; i < ms.points.length; i++) {
      var pt = ms.points[i];
      if (!pt || !pt.graphic) continue;
      var b = boundaries[i];
      if (!b) continue;

      // 오른쪽 경계: 다음 달 첫 거래일 타임스탬프
      var rightTs;
      if (i + 1 < boundaries.length) {
        rightTs = boundaries[i + 1].firstTs;
      } else if (i > 0) {
        rightTs = b.firstTs + (b.firstTs - boundaries[i - 1].firstTs);
      } else {
        rightTs = b.lastTs + 86400000;
      }

      var leftPx  = xAxis.toPixels(b.firstTs, false);
      var rightPx = xAxis.toPixels(rightTs,   false);
      var wPx = Math.abs(rightPx - leftPx) * 0.96;
      if (wPx < 3) wPx = 3;
      var hw = Math.round(wPx / 2);
      var cx = Math.round(pt.plotX) - crisp;

      var pO = pt.plotOpen,  pC = pt.plotClose;
      var pH = pt.plotHigh,  pL = pt.plotLow;
      if (pO == null || pC == null) continue;
      if (pH == null) pH = Math.min(pO, pC);
      if (pL == null) pL = Math.max(pO, pC);

      var top = Math.min(pO, pC), bot = Math.max(pO, pC);

      pt.graphic.attr({ d: [
        ['M', cx,      Math.round(top)],
        ['L', cx,      Math.round(pH)],
        ['M', cx,      Math.round(bot)],
        ['L', cx,      Math.round(pL)],
        ['M', cx - hw, Math.round(top)],
        ['L', cx + hw, Math.round(top)],
        ['L', cx + hw, Math.round(bot)],
        ['L', cx - hw, Math.round(bot)],
        ['Z']
      ]});
    }
  }

  // ─── 월/년 구분선 빌드 ────────────────────────────────────────────────────
  function buildMonthPlotLines(ohlc, timeframe, isDark) {
    if (!ohlc || !ohlc.length) return [];
    var plotLines  = [];
    var currentPeriod = -1;
    var tf = (timeframe || 'd').toLowerCase();
    // 일봉 → 월 경계 / 주봉·월봉·년봉 → 년 경계
    var useYear = (tf === 'w' || tf === 'm' || tf === 'y');
    var lineColor = isDark ? 'rgba(255,255,255,0.18)' : 'rgba(0,0,0,0.18)';

    ohlc.forEach(function(row) {
      var d = new Date(row[0]);
      var pv = useYear
        ? d.getFullYear()
        : d.getFullYear() * 100 + d.getMonth();
      if (pv !== currentPeriod) {
        plotLines.push({ color: lineColor, width: 1, value: row[0],
                         dashStyle: 'Dash', zIndex: 5 });
        currentPeriod = pv;
      }
    });
    return plotLines;
  }

  // ─── 전고/전저 계산 ───────────────────────────────────────────────────────
  function findPrevHighLow(ohlc) {
    if (!ohlc || ohlc.length < 2) return null;
    var lastClose = Number(ohlc[ohlc.length - 1][4]);
    var prevHigh = null, prevHighTs = null;
    var prevLow  = null, prevLowTs  = null;

    for (var i = 0; i < ohlc.length - 1; i++) {
      var h = Number(ohlc[i][2]);
      var l = Number(ohlc[i][3]);
      var t = ohlc[i][0];
      if (h >= lastClose && (prevHigh === null || h > prevHigh)) { prevHigh = h; prevHighTs = t; }
      if (l <= lastClose && (prevLow  === null || l < prevLow))  { prevLow  = l; prevLowTs  = t; }
    }
    return { lastClose: lastClose,
             high: prevHigh, highTs: prevHighTs,
             low:  prevLow,  lowTs:  prevLowTs };
  }

  function applyPrevHighLow(chart, ohlc) {
    // 기존 레이블 제거
    ['__hlHigh', '__hlLow'].forEach(function(k) {
      if (chart[k]) { try { chart[k].destroy(); } catch(e){} chart[k] = null; }
    });

    var hl = findPrevHighLow(ohlc);
    if (!hl) return;

    function drawLabel(storeKey, xTs, yPrice, label, color) {
      var xAxis = chart.xAxis && chart.xAxis[0];
      var yAxis = chart.yAxis && chart.yAxis[0];
      if (!xAxis || !yAxis || !chart.renderer) return;
      var x = xAxis.toPixels(xTs, false);
      var y = yAxis.toPixels(yPrice, false);
      var left = chart.plotLeft, right = chart.plotLeft + chart.plotWidth;
      var top  = chart.plotTop,  bottom = chart.plotTop + chart.plotHeight;
      if (y < top - 3 || y > bottom + 3) return;

      var placeLeft = x > left + chart.plotWidth * 0.6;
      var txt = placeLeft ? label + ' →' : '← ' + label;
      var t = chart.renderer.text(txt, 0, 0)
        .css({ color: color, fontWeight: 'bold', fontSize: '11px' }).add();
      var bbox = t.getBBox();
      var xText = placeLeft ? x - 6 - bbox.width : x + 6;
      var yText = y + 4;
      if (xText < left + 2) xText = left + 2;
      if (xText + bbox.width > right - 2) xText = right - 2 - bbox.width;
      if (yText < top + 12) yText = top + 12;
      if (yText > bottom - 2) yText = bottom - 2;
      t.attr({ x: xText, y: yText });
      chart[storeKey] = t;
    }

    var lc = hl.lastClose;
    if (hl.high !== null && hl.highTs !== null) {
      var hp = ((lc - hl.high) / hl.high * 100);
      drawLabel('__hlHigh', hl.highTs, hl.high,
        '전고 ' + Math.round(hl.high).toLocaleString() +
        ' (' + (hp >= 0 ? '+' : '') + hp.toFixed(1) + '%)',
        hp >= 0 ? '#d32f2f' : '#1976d2');
    }
    if (hl.low !== null && hl.lowTs !== null) {
      var lp = ((lc - hl.low) / hl.low * 100);
      drawLabel('__hlLow', hl.lowTs, hl.low,
        '전저 ' + Math.round(hl.low).toLocaleString() +
        ' (' + (lp >= 0 ? '+' : '') + lp.toFixed(1) + '%)',
        lp >= 0 ? '#d32f2f' : '#1976d2');
    }
  }

  // ─── 차트 렌더링 ──────────────────────────────────────────────────────────
  function renderChart(list) {
    if (!list || !list.length || typeof Highcharts === 'undefined') return;

    var ranged = filterByDateRange(list);
    if (!ranged || !ranged.length) return;

    var series  = normalizeSeries(ranged);
    var isDark  = document.body.classList.contains('theme-dark');
    var chartBg = isDark ? '#0b0c0f' : '#fff';
    var axisColor = isDark ? '#9aa0aa' : '#666';
    var gridColor = isDark ? '#222733' : '#e9ecef';

    // ── Feature 옵션 ──
    var doubleEnabled  = getFeatureEnabled('doubleChartEnabled', false) && state.timeframe === 'd';
    var monthLinesOn   = getFeatureEnabled('monthLinesEnabled',  false);
    var highLowOn      = getFeatureEnabled('highLowEnabled',     false);
    var monthlyResult  = doubleEnabled ? buildMonthlyOverlay(series.ohlc) : null;
    var monthlyCandles = monthlyResult ? monthlyResult.data       : [];
    var monthlyBounds  = monthlyResult ? monthlyResult.boundaries : [];
    var monthPlotLines = monthLinesOn  ? buildMonthPlotLines(series.ohlc, state.timeframe, isDark) : [];

    if (state.chart) {
      try { state.chart.destroy(); } catch (e) {}
      state.chart = null;
    }
    if (state.volumeChart) {
      try { state.volumeChart.destroy(); } catch (e) {}
      state.volumeChart = null;
    }

    var lastClose = series.ohlc.length ? Number(series.ohlc[series.ohlc.length - 1][4]) : NaN;

    var priceSeries = [{
      type: 'candlestick',
      name: state.code || '',
      data: series.ohlc,
      color:       '#1e88e5',
      upColor:     '#e53935',
      lineColor:   '#1e88e5',
      upLineColor: '#e53935',
      lineWidth: 1,
      pointWidth: 1,
      dataGrouping: { enabled: false }
    }];

    // DB 옵션 기반 MA 라인 (동적 색상/두께/기간)
    var enabledMas = (state.dbOptions.length ? state.dbOptions : DEFAULT_MA).filter(function(o) {
      return String(o.enabledYn).toUpperCase() === 'Y';
    });

    enabledMas.forEach(function(opt) {
      var period = Number(opt.seriesPeriod);
      if (!period) return;
      var data = buildMA(series.ohlc, period);
      if (!data.length) return;
      var color = opt.seriesColor || '#999';
      if (color && color.charAt(0) !== '#') color = '#' + color;
      priceSeries.push({
        type: 'line',
        name: opt.seriesLabel || ('MA' + period),
        data: data,
        color: color,
        lineWidth: normalizeMaLineWidth(opt.lineWidth),
        marker: { enabled: false },
        tooltip: { valueDecimals: 0 },
        dataGrouping: { enabled: false }
      });
    });

    // 월봉 오버레이 (더블차트, 일봉에서만)
    if (doubleEnabled && monthlyCandles.length) {
      priceSeries.push({
        type: 'candlestick',
        name: '월봉',
        id: 'monthOverlay',
        data: monthlyCandles,
        yAxis: 0,
        zIndex: 1,
        upColor:     'rgba(232,62,140,0.28)',
        upLineColor: 'rgba(232,62,140,0.75)',
        color:       'rgba(52,152,219,0.28)',
        lineColor:   'rgba(52,152,219,0.75)',
        lineWidth: 2,
        dataGrouping: { enabled: false }
      });
    }

    var chartHeights = getChartHeights();

    state.chart = Highcharts.stockChart('priceChart', {
      chart: {
        height: chartHeights.price,
        backgroundColor: chartBg,
        animation: false,
        zoomType: undefined,
        pinchType: undefined,
        zooming: { type: null },
        panning: { enabled: true, type: 'x' },
        events: {
          load: function() {
            try {
              if (this.container) this.container.style.touchAction = 'pan-x';
            } catch (e) {}
            if (doubleEnabled && monthlyBounds.length) {
              try { updateMonthOverlayWidth(this, monthlyBounds); } catch(e) {}
            }
          },
          render: function() {
            if (doubleEnabled && monthlyBounds.length) {
              try { updateMonthOverlayWidth(this, monthlyBounds); } catch(e) {}
            }
          }
        }
      },
      rangeSelector: { enabled: false },
      navigator:     { enabled: false },
      scrollbar:     { enabled: false },
      credits:       { enabled: false },
      legend:        { enabled: false },
      xAxis: {
        type: 'datetime',
        crosshair: false,
        lineColor: gridColor,
        tickColor: gridColor,
        labels: { style: { fontSize: '10px', color: axisColor } },
        plotLines: monthPlotLines,
        events: {
          setExtremes: function(e) {
            if (!state.volumeChart || !state.volumeChart.xAxis || !state.volumeChart.xAxis[0]) return;
            if (e && e.trigger === 'sync-volume') return;
            state.volumeChart.xAxis[0].setExtremes(e.min, e.max, true, false, { trigger: 'sync-price' });
          }
        }
      },
      yAxis: {
        opposite: false,
        gridLineColor: gridColor,
        labels: {
          align: 'left',
          x: 5,
          formatter: function() { return Number(this.value).toLocaleString(); },
          style: { fontSize: '10px', color: axisColor }
        },
        plotLines: Number.isFinite(lastClose) ? [{
          value:     lastClose,
          color:     isDark ? '#3b82f6' : '#1976d2',
          width:     1,
          dashStyle: 'Dash',
          zIndex:    5,
          label: {
            text:  Number(lastClose).toLocaleString(),
            align: 'right',
            x: -4, y: -4,
            style: { color: isDark ? '#8ec0ff' : '#1976d2', fontSize: '11px', fontWeight: '700' }
          }
        }] : []
      },
      plotOptions: {
        series: {
          animation: false,
          turboThreshold: 0,
          states: { hover: { enabled: false }, inactive: { enabled: false } },
          stickyTracking: false,
          dataGrouping: { enabled: false }
        }
      },
      series:  priceSeries,
      tooltip: { enabled: false }
    });

    // 전고/전저 (highLowEnabled)
    if (highLowOn && series.ohlc.length > 1) {
      try {
        applyPrevHighLow(state.chart, series.ohlc);
        if (!state.chart.__hlAutoBound) {
          state.chart.__hlAutoBound = true;
          var _capturedOhlc = series.ohlc;
          Highcharts.addEvent(state.chart, 'redraw', function() {
            if (getFeatureEnabled('highLowEnabled', false)) {
              try { applyPrevHighLow(state.chart, _capturedOhlc); } catch(e) {}
            }
          });
        }
      } catch(e) {}
    }

    // 거래량 차트 (Feature: volumeEnabled)
    var volEnabled = getFeatureEnabled('volumeEnabled', true);
    var volEl = document.getElementById('volumeChart');
    if (volEl) volEl.style.display = volEnabled ? '' : 'none';

    if (volEnabled) {
      state.volumeChart = Highcharts.stockChart('volumeChart', {
        chart: { height: chartHeights.volume, backgroundColor: chartBg, animation: false },
        rangeSelector: { enabled: false },
        navigator:     { enabled: false },
        scrollbar:     { enabled: false },
        credits:       { enabled: false },
        legend:        { enabled: false },
        xAxis: {
          type: 'datetime',
          lineColor: gridColor,
          tickColor: gridColor,
          labels: { enabled: false },
          events: {
            setExtremes: function(e) {
              if (!state.chart || !state.chart.xAxis || !state.chart.xAxis[0]) return;
              if (e && e.trigger === 'sync-price') return;
              state.chart.xAxis[0].setExtremes(e.min, e.max, true, false, { trigger: 'sync-volume' });
            }
          }
        },
        yAxis: {
          opposite: false,
          gridLineColor: gridColor,
          labels: { align: 'left', x: 5, style: { fontSize: '10px', color: axisColor } }
        },
        plotOptions: {
          series: {
            animation: false,
            turboThreshold: 0,
            states: { hover: { enabled: false }, inactive: { enabled: false } },
            stickyTracking: false,
            dataGrouping: { enabled: false }
          }
        },
        series: [{ type: 'column', name: '거래량', data: series.volume, dataGrouping: { enabled: false } }],
        tooltip: { enabled: false }
      });
    }

    applyRangeExtremes(state.chart,       state.fromDate, state.endDate);
    applyRangeExtremes(state.volumeChart, state.fromDate, state.endDate);
  }

  // ─── 타임프레임 / 마켓 / 테마 ─────────────────────────────────────────────
  window.changeTF = function(tf) {
    if (!tf || state.timeframe === tf) return;
    state.timeframe = tf;
    document.querySelectorAll('.tf-btn[data-tf]').forEach(function(btn) {
      btn.classList.toggle('active', btn.dataset.tf === tf);
    });
    loadChartData();
  };

  window.onMarketChanged = function(market) {
    var nextCode = (market === 'US') ? 'AAPL' : '005930';
    if (state.code === nextCode) return;
    state.code = nextCode;
    var q = new URLSearchParams(location.search);
    q.set('code', nextCode);
    history.replaceState(null, '', location.pathname + '?' + q.toString());
    loadAll();
  };

  window.addEventListener('mobileThemeChanged', function() {
    var cacheKey = state.code + '|' + state.timeframe;
    var list = state.chartCache[cacheKey];
    if (list && list.length) renderChart(list);
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
