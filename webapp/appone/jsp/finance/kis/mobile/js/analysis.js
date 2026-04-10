(function() {
  'use strict';

  const tabs = Array.from(document.querySelectorAll('.analysis-tab'));
  const panels = Array.from(document.querySelectorAll('.analysis-panel'));

  const signalListEl = document.getElementById('signalListMini');
  const aiStockNameEl = document.getElementById('aiStockName');
  const aiStockPriceEl = document.getElementById('aiStockPrice');
  const aiStockChangeEl = document.getElementById('aiStockChange');

  const state = {
    market: (localStorage.getItem('mobile.market') === 'US') ? 'A' : 'N',
    pickedCode: (new URLSearchParams(location.search).get('code') || '005930').trim(),
    pickedName: '-',
    pickedSignal: '중립',
    pickedYield: '-'
  };

  function setTab(name) {
    tabs.forEach(t => t.classList.toggle('active', t.dataset.tab === name));
    panels.forEach(p => p.classList.toggle('active', p.dataset.panel === name));
  }

  function num(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n : NaN;
  }

  function signClass(v) {
    const n = num(v);
    if (!Number.isFinite(n) || n === 0) return 'flat';
    return n > 0 ? 'up' : 'down';
  }

  function fmtPrice(v) {
    const n = num(v);
    if (!Number.isFinite(n)) return '-';
    return Math.round(n).toLocaleString();
  }

  function fmtPct(v) {
    const n = num(v);
    if (!Number.isFinite(n)) return '-';
    return (n > 0 ? '+' : '') + n.toFixed(2) + '%';
  }

  function getRowField(row, keys, fallback) {
    for (let i = 0; i < keys.length; i++) {
      const k = keys[i];
      if (row && row[k] !== undefined && row[k] !== null && String(row[k]).trim() !== '') {
        return row[k];
      }
    }
    return fallback;
  }

  function currentMarketCode() {
    return state.market === 'A' ? 'US' : 'KR';
  }

  function loadSignalMini() {
    if (!window.__MOBILE || !window.__MOBILE.urls || !window.__MOBILE.urls.recSignalList) return;
    const url = window.__MOBILE.urls.recSignalList
      + '?recYn=Y&mktCd=' + encodeURIComponent(currentMarketCode());
    fetch(url, { credentials: 'include' })
      .then(r => r.json())
      .then(resp => {
        const list = (resp && Array.isArray(resp.data)) ? resp.data : (resp && resp.data && Array.isArray(resp.data.data) ? resp.data.data : []);
        renderSignalMini(list);
        pickAiStock(list);
      })
      .catch(() => {
        if (signalListEl) signalListEl.innerHTML = '<div class="mini-empty">추천신호 로딩 실패</div>';
      });
  }

  function renderSignalMini(list) {
    if (!signalListEl) return;
    if (!list.length) {
      signalListEl.innerHTML = '<div class="mini-empty">표시할 추천신호가 없습니다.</div>';
      return;
    }

    const top = list.slice(0, 5);
    signalListEl.innerHTML = top.map((row, idx) => {
      const code = getRowField(row, ['stkCd','stk_cd','code'], '');
      const name = getRowField(row, ['stkNm','stk_nm','name'], code);
      const grade = String(getRowField(row, ['recGrade','rec_grade'], 'C')).toUpperCase();
      const close = getRowField(row, ['curPrice','cur_price','currentPrice'], null);
      const rate = getRowField(row, ['monChgRate','mon_chg_rate'], null);
      const badge = grade ? (grade + '등급') : '추천';
      const cls = grade === 'A' ? 'buy' : grade === 'B' ? 'hold' : 'sell';
      return `
        <button class="mini-row ${cls}" data-code="${code}" data-focus="signal">
          <span class="rank">${idx + 1}</span>
          <span class="name">${name}</span>
          <span class="price">${fmtPrice(close)}</span>
          <span class="rate ${signClass(rate)}">${fmtPct(rate)}</span>
          <span class="badge">${badge}</span>
        </button>
      `;
    }).join('');

    signalListEl.querySelectorAll('.mini-row').forEach(btn => {
      btn.addEventListener('click', function() {
        const code = this.dataset.code || '';
        location.href = (window.__MOBILE.urls.mobileChart || '/scheduler/finance/mobile/chart.do') + '?code=' + encodeURIComponent(code);
      });
    });
  }

  function pickAiStock(list) {
    if (!list || !list.length) return loadAiByCode(state.pickedCode);

    const direct = list.find(r => String(getRowField(r, ['stkCd','stk_cd','code'], '')).trim() === state.pickedCode);
    const row = direct || list[0];

    state.pickedCode = String(getRowField(row, ['stkCd','stk_cd','code'], state.pickedCode));
    state.pickedName = String(getRowField(row, ['stkNm','stk_nm','name'], '-'));
    const grade = String(getRowField(row, ['recGrade','rec_grade'], 'C')).toUpperCase();
    state.pickedSignal = grade ? (grade + '등급') : '추천';
    state.pickedYield = fmtPct(getRowField(row, ['monChgRate','mon_chg_rate'], null));

    if (aiStockNameEl) aiStockNameEl.textContent = state.pickedName + ' (' + state.pickedCode + ')';
    loadAiByCode(state.pickedCode);

    const badge = document.getElementById('aiOpinionBadge');
    if (badge) badge.textContent = state.pickedSignal;
    const yieldEl = document.getElementById('aiYield');
    if (yieldEl) yieldEl.textContent = state.pickedYield;
  }

  function loadAiByCode(code) {
    const url = '/scheduler/finance/getCurrentPriceByInquirePrice.do?in_stockCode=' + encodeURIComponent(code || '005930');
    fetch(url, { credentials: 'include' })
      .then(r => r.json())
      .then(json => {
        const sd = (json && (json.singleData || json.single_data)) || null;
        if (!sd) return;

        const name = sd.stockName || sd.stock_name || state.pickedName || '-';
        const price = num(sd.currentPrice || sd.current_price || 0);
        const diff = num(sd.diff || sd.priceChangeAmt || sd.price_change_amt || 0);
        const pct = num(sd.diffRate || sd.priceChangeRate || sd.price_change_rate || 0);

        if (aiStockNameEl) aiStockNameEl.textContent = name + ' (' + (code || state.pickedCode) + ')';
        if (aiStockPriceEl) aiStockPriceEl.textContent = fmtPrice(price);
        if (aiStockChangeEl) {
          aiStockChangeEl.textContent = fmtPct(pct) + ' · ' + (Number.isFinite(diff) ? (diff > 0 ? '+' : '') + Math.round(diff).toLocaleString() : '-');
          aiStockChangeEl.className = 'ai-price-sub ' + signClass(pct);
        }
      })
      .catch(() => {});
  }

  function bindCtas() {
    document.querySelectorAll('.analysis-card .cta').forEach(btn => {
      btn.addEventListener('click', function() {
        const focus = this.dataset.focus || 'signal';
        const code = state.pickedCode || '005930';
        location.href = (window.__MOBILE.urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp') +
          '?focus=' + encodeURIComponent(focus) + '&code=' + encodeURIComponent(code);
      });
    });
  }

  window.onMarketChanged = function(market) {
    state.market = (market === 'US') ? 'A' : 'N';
    loadSignalMini();
  };

  tabs.forEach(tab => tab.addEventListener('click', () => setTab(tab.dataset.tab)));
  bindCtas();
  loadSignalMini();
})();
