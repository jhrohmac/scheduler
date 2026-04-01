(function (window) {
  'use strict';

  var MA_LINE_WIDTH_DEFAULT = 2.5;
  var MA_LINE_WIDTH_MIN = 0.5;
  var MA_LINE_WIDTH_MAX = 6;
  var SERIES_TYPE = 'MA';
  var SERVER_DEFAULTS = [
    { period: 5, label: '5일선', color: '#26a69a', lineWidth: 2.5 },
    { period: 20, label: '20일선', color: '#4caf50', lineWidth: 2.5 },
    { period: 60, label: '60일선', color: '#ff9800', lineWidth: 2.5 },
    { period: 120, label: '120일선', color: '#9c27b0', lineWidth: 2.5 },
    { period: 240, label: '240일선', color: '#607d8b', lineWidth: 2.5 }
  ];
  var viewState = {
    loaded: false,
    loading: false,
    saving: false,
    statusText: '저장된 차트 옵션을 조회 중입니다.',
    statusType: '',
    rows: []
  };

  function getBridge() {
    return window.PatternAppBridge || null;
  }

  function getRoot() {
    return document.getElementById('pattern-chart-option-section');
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function normalizeLineWidth(value) {
    var width = parseFloat(value);
    if (!isFinite(width)) {
      width = MA_LINE_WIDTH_DEFAULT;
    }
    if (width < MA_LINE_WIDTH_MIN) {
      width = MA_LINE_WIDTH_MIN;
    }
    if (width > MA_LINE_WIDTH_MAX) {
      width = MA_LINE_WIDTH_MAX;
    }
    return Math.round(width * 2) / 2;
  }

  function cloneRows(list) {
    return (Array.isArray(list) ? list : []).map(function (item, index) {
      return {
        seriesKey: String(item.seriesKey || ''),
        seriesLabel: String(item.seriesLabel || ''),
        seriesPeriod: parseInt(item.seriesPeriod, 10) || 0,
        seriesColor: String(item.seriesColor || '#94A3B8'),
        lineWidth: normalizeLineWidth(item.lineWidth),
        enabledYn: item.enabledYn === 'N' || item.enabledYn === false ? 'N' : 'Y',
        displayOrder: parseInt(item.displayOrder, 10) || (index + 1)
      };
    });
  }

  function normalizeRows(list) {
    var deduped = new Map();
    (Array.isArray(list) ? list : []).forEach(function (item, index) {
      var period = parseInt(item.seriesPeriod, 10);
      if (!period || period <= 0) {
        return;
      }
      deduped.set(period, {
        seriesKey: String(item.seriesKey || ('ma' + period)),
        seriesLabel: String(item.seriesLabel || ('MA' + period)),
        seriesPeriod: period,
        seriesColor: String(item.seriesColor || '#94A3B8'),
        lineWidth: normalizeLineWidth(item.lineWidth),
        enabledYn: item.enabledYn === 'N' || item.enabledYn === false ? 'N' : 'Y',
        displayOrder: parseInt(item.displayOrder, 10) || (index + 1)
      });
    });
    return Array.from(deduped.values()).sort(function (left, right) {
      if (left.seriesPeriod !== right.seriesPeriod) {
        return left.seriesPeriod - right.seriesPeriod;
      }
      return (left.displayOrder || 0) - (right.displayOrder || 0);
    }).map(function (item, index) {
      item.displayOrder = index + 1;
      return item;
    });
  }

  function getDefaultRows() {
    var bridge = getBridge();
    return bridge && typeof bridge.getDefaultChartMaOptions === 'function'
      ? cloneRows(bridge.getDefaultChartMaOptions())
      : [];
  }

  function getInitialRows() {
    var bridge = getBridge();
    return bridge && typeof bridge.getChartMaOptions === 'function'
      ? cloneRows(bridge.getChartMaOptions())
      : getDefaultRows();
  }

  function setStatus(message, type) {
    viewState.statusText = message || '';
    viewState.statusType = type || '';
  }

  function clearValidationState() {
    var root = getRoot();
    if (!root) {
      return;
    }
    root.querySelectorAll('.chart-ma-row').forEach(function (row) {
      row.classList.remove('chart-ma-invalid');
    });
  }

  function markInvalidRows(indexes) {
    var root = getRoot();
    if (!root) {
      return;
    }
    clearValidationState();
    (indexes || []).forEach(function (index) {
      var row = root.querySelector('.chart-ma-row[data-index="' + index + '"]');
      if (row) {
        row.classList.add('chart-ma-invalid');
      }
    });
  }

  function syncRowsFromDom() {
    var root = getRoot();
    var nextRows = [];
    if (!root) {
      return viewState.rows;
    }
    root.querySelectorAll('.chart-ma-row').forEach(function (row, index) {
      var periodInput = row.querySelector('[data-field="seriesPeriod"]');
      var labelInput = row.querySelector('[data-field="seriesLabel"]');
      var colorInput = row.querySelector('[data-field="seriesColor"]');
      var lineWidthInput = row.querySelector('[data-field="lineWidth"]');
      var enabledInput = row.querySelector('[data-field="enabledYn"]');
      var periodValue = parseInt(periodInput && periodInput.value, 10);
      nextRows.push({
        seriesKey: row.getAttribute('data-series-key') || ('ma' + (periodValue > 0 ? periodValue : (index + 1))),
        seriesLabel: labelInput ? labelInput.value : '',
        seriesPeriod: periodValue > 0 ? periodValue : 0,
        seriesColor: colorInput ? colorInput.value : '#94A3B8',
        lineWidth: normalizeLineWidth(lineWidthInput ? lineWidthInput.value : MA_LINE_WIDTH_DEFAULT),
        enabledYn: enabledInput && enabledInput.checked ? 'Y' : 'N',
        displayOrder: index + 1
      });
    });
    viewState.rows = cloneRows(nextRows);
    return viewState.rows;
  }

  function nextPeriod(rows) {
    var periods = (rows || []).map(function (item) {
      return parseInt(item.seriesPeriod, 10) || 0;
    }).filter(function (period) {
      return period > 0;
    });
    var last = periods.length ? Math.max.apply(Math, periods) : 20;
    var candidate = last < 20 ? last + 5 : last + 20;
    while (periods.indexOf(candidate) !== -1) {
      candidate += 5;
    }
    return candidate;
  }

  function isServerDefaultList(list) {
    var normalized = normalizeRows(list);
    return normalized.length === SERVER_DEFAULTS.length && normalized.every(function (item, index) {
      var expected = SERVER_DEFAULTS[index];
      return item.seriesPeriod === expected.period &&
        item.seriesLabel === expected.label &&
        item.seriesColor.toLowerCase() === expected.color &&
        normalizeLineWidth(item.lineWidth) === expected.lineWidth;
    });
  }

  function buildUrl(path, params) {
    var bridge = getBridge();
    var query = new URLSearchParams();
    Object.keys(params || {}).forEach(function (key) {
      if (params[key] == null || params[key] === '') {
        return;
      }
      query.set(key, params[key]);
    });
    return bridge.buildAppUrl(path) + (query.toString() ? ('?' + query.toString()) : '');
  }

  function validateRows(rows) {
    var errors = [];
    var invalidIndexes = [];
    var seenPeriods = new Set();
    var normalized = [];

    if (!rows.length) {
      return {
        ok: false,
        invalidIndexes: [],
        message: '최소 1개 이상의 이동평균선을 유지해야 합니다.',
        list: []
      };
    }

    rows.forEach(function (row, index) {
      var label = String(row.seriesLabel || '').trim();
      var period = parseInt(row.seriesPeriod, 10);
      var color = String(row.seriesColor || '#94A3B8').trim();
      if (!label) {
        invalidIndexes.push(index);
        errors.push('라벨을 입력해 주세요.');
        return;
      }
      if (!period || period <= 0) {
        invalidIndexes.push(index);
        errors.push('기간은 1 이상의 숫자여야 합니다.');
        return;
      }
      if (seenPeriods.has(period)) {
        invalidIndexes.push(index);
        errors.push('기간은 중복될 수 없습니다.');
        return;
      }
      seenPeriods.add(period);
      if (!/^#[0-9a-fA-F]{6}$/.test(color)) {
        color = '#94A3B8';
      }
      normalized.push({
        seriesKey: 'ma' + period,
        seriesLabel: label,
        seriesPeriod: period,
        seriesColor: color,
        lineWidth: normalizeLineWidth(row.lineWidth),
        enabledYn: row.enabledYn === 'N' || row.enabledYn === false ? 'N' : 'Y',
        displayOrder: index + 1
      });
    });

    return {
      ok: !errors.length,
      invalidIndexes: invalidIndexes,
      message: errors[0] || '',
      list: normalizeRows(normalized)
    };
  }

  function render() {
    var root = getRoot();
    var statusClass = viewState.statusType ? (' chart-opt-status ' + viewState.statusType) : ' chart-opt-status';
    var rowsHtml = '';

    if (!root) {
      return;
    }

    if (!viewState.rows.length) {
      rowsHtml = '<div class="chart-ma-empty">표시할 이동평균선이 없습니다. 추가 버튼으로 새 라인을 만들 수 있습니다.</div>';
    } else {
      rowsHtml = '<div class="chart-ma-head"><span>표시</span><span>이름</span><span>기간</span><span>굵기</span><span>색상</span><span>작업</span></div>' +
        viewState.rows.map(function (row, index) {
          return '' +
            '<div class="chart-ma-row" data-index="' + index + '" data-series-key="' + escapeHtml(row.seriesKey || ('ma' + row.seriesPeriod)) + '">' +
              '<label class="chart-ma-enable"><input type="checkbox" data-field="enabledYn" ' + (row.enabledYn !== 'N' ? 'checked' : '') + '>표시</label>' +
              '<input type="text" data-field="seriesLabel" value="' + escapeHtml(row.seriesLabel) + '" maxlength="20" placeholder="예: MA20">' +
              '<input type="number" data-field="seriesPeriod" min="1" step="1" value="' + escapeHtml(row.seriesPeriod || '') + '" placeholder="20">' +
              '<input type="number" data-field="lineWidth" min="' + MA_LINE_WIDTH_MIN + '" max="' + MA_LINE_WIDTH_MAX + '" step="0.5" value="' + escapeHtml(row.lineWidth) + '">' +
              '<input type="color" data-field="seriesColor" value="' + escapeHtml(row.seriesColor || '#94A3B8') + '">' +
              '<button type="button" class="chart-ma-remove" data-action="remove" data-index="' + index + '">삭제</button>' +
            '</div>';
        }).join('');
    }

    root.innerHTML = '' +
      '<div class="chart-opt-section">' +
        '<div class="chart-opt-header">' +
          '<div>' +
            '<div class="chart-opt-title">⚙️ 차트 옵션 설정</div>' +
            '<div class="chart-opt-sub">StockChartOptionController의 차트 옵션 API로 이동평균선 구성을 저장합니다.</div>' +
          '</div>' +
          '<div class="chart-opt-actions">' +
            '<button type="button" class="chart-opt-btn add" data-action="add">+ 추가</button>' +
            '<button type="button" class="chart-opt-btn" data-action="default">기본값</button>' +
            '<button type="button" class="chart-opt-btn primary" data-action="save" ' + (viewState.saving ? 'disabled' : '') + '>적용</button>' +
          '</div>' +
        '</div>' +
        '<div class="chart-opt-note">이동평균선은 기간 기준으로 정렬되어 저장됩니다. 사이드바 체크박스는 화면 표시만 제어하고, 여기서는 저장 가능한 기준 구성을 편집합니다.</div>' +
        '<div class="' + statusClass + '">' + escapeHtml(viewState.statusText || ' ') + '</div>' +
        '<div class="chart-ma-list">' + rowsHtml + '</div>' +
      '</div>';
  }

  function applyRowsToChart(list, resetVisible) {
    var bridge = getBridge();
    if (!bridge) {
      return;
    }
    if (typeof bridge.setChartMaOptions === 'function') {
      bridge.setChartMaOptions(list, { resetVisible: !!resetVisible });
    }
    if (typeof bridge.refreshChartMaViews === 'function') {
      bridge.refreshChartMaViews();
    }
  }

  function loadOptions(forceDefault) {
    var bridge = getBridge();
    if (!bridge || viewState.loading) {
      return Promise.resolve();
    }

    viewState.loading = true;
    setStatus('저장된 차트 옵션을 조회 중입니다.', '');
    render();

    return fetch(buildUrl(bridge.apiConfig.chartOptionData, {
      chartId: bridge.chartOptionChartId,
      seriesType: SERIES_TYPE,
      forceDefault: forceDefault ? 'Y' : 'N'
    }), {
      method: 'GET',
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    }).then(function (response) {
      if (!response.ok) {
        throw new Error('HTTP ' + response.status);
      }
      return response.json();
    }).then(function (payload) {
      var serverRows = payload && Array.isArray(payload.data) ? payload.data : [];
      var nextRows = isServerDefaultList(serverRows) ? getDefaultRows() : normalizeRows(serverRows);
      if (!nextRows.length) {
        nextRows = getDefaultRows();
      }
      viewState.rows = cloneRows(nextRows);
      viewState.loaded = true;
      setStatus('차트 옵션을 불러왔습니다.', 'success');
      clearValidationState();
      applyRowsToChart(nextRows, false);
      render();
    }).catch(function (error) {
      console.error('[patternChartOptions] load failed', error);
      if (!viewState.rows.length) {
        viewState.rows = getInitialRows();
      }
      setStatus('차트 옵션 조회 중 오류가 발생했습니다.', 'error');
      render();
    }).finally(function () {
      viewState.loading = false;
    });
  }

  function saveOptions() {
    var bridge = getBridge();
    var rows = syncRowsFromDom();
    var result = validateRows(rows);

    if (!bridge) {
      return;
    }

    if (!result.ok) {
      markInvalidRows(result.invalidIndexes);
      setStatus(result.message || '입력값을 확인해 주세요.', 'error');
      render();
      markInvalidRows(result.invalidIndexes);
      return;
    }

    clearValidationState();
    viewState.saving = true;
    viewState.rows = cloneRows(result.list);
    setStatus('차트 옵션을 저장 중입니다.', '');
    render();

    fetch(bridge.buildAppUrl(bridge.apiConfig.chartOptionSave), {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', 'Accept': 'application/json' },
      body: new URLSearchParams({
        chartId: bridge.chartOptionChartId,
        seriesType: SERIES_TYPE,
        optionsJson: JSON.stringify(result.list)
      }).toString()
    }).then(function (response) {
      if (!response.ok) {
        throw new Error('HTTP ' + response.status);
      }
      return response.json();
    }).then(function (payload) {
      if (payload && payload.system_code && String(payload.system_code) !== '0000') {
        throw new Error(payload.system_msg || '저장 응답 오류');
      }
      viewState.saving = false;
      viewState.rows = cloneRows(result.list);
      viewState.loaded = true;
      setStatus('차트 옵션이 저장되었습니다.', 'success');
      applyRowsToChart(result.list, false);
      render();
    }).catch(function (error) {
      console.error('[patternChartOptions] save failed', error);
      viewState.saving = false;
      setStatus('차트 옵션 저장 중 오류가 발생했습니다.', 'error');
      render();
    });
  }

  function handleClick(event) {
    var target = event.target.closest('[data-action]');
    var index;

    if (!target) {
      return;
    }

    event.preventDefault();
    syncRowsFromDom();

    if (target.getAttribute('data-action') === 'add') {
      viewState.rows.push({
        seriesKey: 'ma' + nextPeriod(viewState.rows),
        seriesLabel: 'MA' + nextPeriod(viewState.rows),
        seriesPeriod: nextPeriod(viewState.rows),
        seriesColor: '#94A3B8',
        lineWidth: MA_LINE_WIDTH_DEFAULT,
        enabledYn: 'Y',
        displayOrder: viewState.rows.length + 1
      });
      setStatus('새 이동평균선을 추가했습니다. 적용을 누르면 저장됩니다.', '');
      render();
      return;
    }

    if (target.getAttribute('data-action') === 'default') {
      viewState.rows = getDefaultRows();
      clearValidationState();
      setStatus('기본 이동평균선 구성을 불러왔습니다. 적용을 누르면 저장됩니다.', '');
      render();
      return;
    }

    if (target.getAttribute('data-action') === 'remove') {
      index = parseInt(target.getAttribute('data-index'), 10);
      if (isFinite(index)) {
        viewState.rows.splice(index, 1);
      }
      setStatus('이동평균선을 제거했습니다. 적용을 누르면 저장됩니다.', '');
      render();
      return;
    }

    if (target.getAttribute('data-action') === 'save') {
      saveOptions();
    }
  }

  function handleInput(event) {
    var row = event.target.closest('.chart-ma-row');
    var field = event.target.getAttribute('data-field');
    var index;

    if (!row || !field) {
      return;
    }

    index = parseInt(row.getAttribute('data-index'), 10);
    if (!isFinite(index) || !viewState.rows[index]) {
      return;
    }

    if (field === 'enabledYn') {
      viewState.rows[index].enabledYn = event.target.checked ? 'Y' : 'N';
    } else if (field === 'seriesPeriod') {
      viewState.rows[index].seriesPeriod = parseInt(event.target.value, 10) || 0;
      row.setAttribute('data-series-key', viewState.rows[index].seriesPeriod > 0 ? ('ma' + viewState.rows[index].seriesPeriod) : '');
    } else if (field === 'lineWidth') {
      viewState.rows[index].lineWidth = normalizeLineWidth(event.target.value);
    } else {
      viewState.rows[index][field] = event.target.value;
    }
    clearValidationState();
  }

  function bindEvents() {
    var root = getRoot();
    if (!root || root.__patternChartOptionsBound) {
      return;
    }
    root.addEventListener('click', handleClick);
    root.addEventListener('input', handleInput);
    root.addEventListener('change', handleInput);
    root.__patternChartOptionsBound = true;
  }

  function mount() {
    var root = getRoot();
    if (!root) {
      return;
    }
    if (!viewState.rows.length) {
      viewState.rows = getInitialRows();
    }
    bindEvents();
    render();
    if (!viewState.loaded && !viewState.loading) {
      loadOptions(false);
    }
  }

  function bootstrap() {
    if (!getBridge()) {
      return Promise.resolve();
    }
    if (!viewState.rows.length) {
      viewState.rows = getInitialRows();
    }
    if (viewState.loaded || viewState.loading) {
      return Promise.resolve();
    }
    return loadOptions(false);
  }

  window.PatternChartOptions = {
    mount: mount,
    bootstrap: bootstrap,
    reload: function () {
      return loadOptions(false);
    }
  };

  if (getBridge()) {
    bootstrap();
    if (getBridge().getActiveTab && getBridge().getActiveTab() === 'options') {
      mount();
    }
  }
})(window);
