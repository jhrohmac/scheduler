// KIS Mobile - Watchlist (성능 개선)

(function () {
  'use strict';

  const els = {
    group: document.getElementById('wlGroup'),
    list: document.getElementById('wlList'),
    status: document.getElementById('wlStatus')
  };

  const state = {
    groupId: null,
    items: [],
    ws: null,
    reconnectTimer: null,
    listByToken: Object.create(null),
    inFlight: null,
    sortAsc: true,
    editMode: false,
    market: localStorage.getItem('mobile.market') || 'KR'
  };

  function wsProto() {
    return (location.protocol === 'https:') ? 'wss://' : 'ws://';
  }

  function buildWsUrl(path, query) {
    const p = (path && path.startsWith('/')) ? path : ('/' + (path || ''));
    return wsProto() + location.host + '/scheduler' + p + (query ? ('?' + query) : '');
  }

  function setStatus(text) {
    if (els.status) els.status.textContent = text;
  }

  function fetchJson(url) {
    if (state.inFlight) state.inFlight.abort();
    state.inFlight = new AbortController();
    if (window.mobileProgress) window.mobileProgress.start();
    return fetch(url, { credentials: 'include', signal: state.inFlight.signal })
      .then(r => r.json())
      .finally(() => {
        state.inFlight = null;
        if (window.mobileProgress) window.mobileProgress.done();
      });
  }

  function loadGroups() {
    setStatus('그룹 로딩');
    const groupsUrl = window.__MOBILE.urls.watchlistGroups + '?market=' + encodeURIComponent(state.market);
    return fetchJson(groupsUrl)
      .then(json => {
        const list = (json && Array.isArray(json.data)) ? json.data : [];
        renderGroups(list);
        if (list.length) {
          state.groupId = list[0].GROUP_ID || list[0].groupId;
          if (els.group) els.group.value = state.groupId;
          return loadItems();
        }
        els.list.innerHTML = '<div style="padding:12px 16px; color:#888;">관심그룹이 없습니다.</div>';
      })
      .catch((e) => {
        if (e && e.name === 'AbortError') return;
        setStatus('그룹 오류');
        els.list.innerHTML = '<div style="padding:12px 16px; color:#c00;">관심그룹 조회 실패</div>';
      });
  }

  function renderGroups(list) {
    if (!els.group) return;
    const html = list.map(g => {
      const value = g.GROUP_ID || g.groupId || '';
      const name = g.GROUP_NAME || g.groupName || value;
      const count = g.STOCK_COUNT || 0;
      return `<option value="${value}">${name} (${count})</option>`;
    }).join('');
    els.group.innerHTML = html;
  }

  function tokenOf(it) {
    const country = (it.STOCK_COUNTRY_CODE || it.stockCountryCode || 'KR').toUpperCase();
    const market = (it.STOCK_MARKET || it.stockMarket || 'STK').toUpperCase();
    const code = (it.STOCK_CODE || it.stockCode || '').trim();
    if (!code) return '';
    return country + '|' + market + '|' + code;
  }

  function loadItems() {
    if (!state.groupId) return;
    setStatus('종목 로딩');
    const url = window.__MOBILE.urls.watchlistItems + '?groupId=' + encodeURIComponent(state.groupId);
    return fetchJson(url)
      .then(json => {
        const items = (json && Array.isArray(json.data)) ? json.data : [];
        state.items = items;
        renderItems(items);
        connectRealtime(items);
        setStatus(items.length ? '연결중' : '비어있음');
      })
      .catch((e) => {
        if (e && e.name === 'AbortError') return;
        setStatus('종목 오류');
        els.list.innerHTML = '<div style="padding:12px 16px; color:#c00;">관심종목 조회 실패</div>';
      });
  }

  function renderItems(items) {
    if (!items.length) {
      state.listByToken = Object.create(null);
      els.list.innerHTML = '<div style="padding:12px 16px; color:#888;">등록된 관심종목이 없습니다.</div>';
      return;
    }

    state.listByToken = Object.create(null);

    const html = items.map(it => {
      const code = it.STOCK_CODE;
      const name = it.STOCK_KO_NAME || it.STOCK_EN_NAME || code;
      const close = it.STOCK_CLOSE || '-';
      const summary = it.RECO_EVENT_SUMMARY || '';
      const token = tokenOf(it);
      const closeNum = Number(String(close).replace(/,/g, ''));
      return `
        <div class="wl-item" data-code="${code}" data-token="${token}" data-last-price="${Number.isFinite(closeNum) ? closeNum : ''}">
          <button class="wl-remove" aria-label="삭제" title="관심종목 삭제">−</button>
          <div class="wl-left">
            <div class="wl-name">${name}</div>
            <div class="wl-sub">${code} · ${it.STOCK_MARKET || ''}</div>
            <div class="wl-event">${summary}</div>
          </div>
          <div class="wl-right">
            <div class="wl-price">${Number.isFinite(closeNum) && closeNum > 0 ? closeNum.toLocaleString() : close}</div>
            <div class="wl-diff">-</div>
            <div class="wl-rate">-%</div>
          </div>
        </div>
      `;
    }).join('');

    els.list.innerHTML = html;

    els.list.querySelectorAll('.wl-item').forEach(row => {
      const token = row.dataset.token;
      if (token) state.listByToken[token] = row;
    });
  }

  function closeWs() {
    if (state.ws) {
      try { state.ws.__manualClose = true; state.ws.close(); } catch (e) {}
    }
    state.ws = null;
    if (state.reconnectTimer) {
      clearTimeout(state.reconnectTimer);
      state.reconnectTimer = null;
    }
  }

  function connectRealtime(items) {
    closeWs();
    const tokens = items.map(tokenOf).filter(Boolean);
    if (!tokens.length) return;

    const url = buildWsUrl(window.__MOBILE.urls.wsWatchlist, 'codes=' + encodeURIComponent(tokens.join(',')));
    const ws = new WebSocket(url);
    state.ws = ws;

    ws.onopen = () => setStatus('연결됨');

    ws.onmessage = evt => {
      let msg;
      try { msg = JSON.parse(evt.data); } catch (e) { return; }
      if (!msg || msg.type !== 'WL') return;
      applyUpdate(msg);
    };

    ws.onerror = () => setStatus('오류');

    ws.onclose = () => {
      if (ws.__manualClose) return;
      setStatus('재연결');
      state.reconnectTimer = setTimeout(() => connectRealtime(state.items), 1500);
    };
  }

  function signClass(diff) {
    const d = Number(diff);
    if (!Number.isFinite(d) || d === 0) return 'flat';
    return d > 0 ? 'up' : 'down';
  }

  function fmtSigned(n, digits) {
    const v = Number(n);
    if (!Number.isFinite(v)) return '-';
    const fixed = (digits !== undefined) ? v.toFixed(digits) : String(Math.round(v));
    return (v > 0 ? '+' : '') + fixed;
  }

  function flashRow(row, dir) {
    if (!row || !dir) return;
    const upCls = 'wl-flash-up';
    const downCls = 'wl-flash-down';
    row.classList.remove(upCls, downCls);
    // reflow to restart animation
    void row.offsetWidth;
    row.classList.add(dir === 'up' ? upCls : downCls);
    setTimeout(() => row.classList.remove(upCls, downCls), 520);
  }

  function applyUpdate(msg) {
    const token = (msg.token || msg.code || '').trim();
    if (!token) return;

    const row = state.listByToken[token] || els.list.querySelector(`.wl-item[data-token="${CSS.escape(token)}"]`);
    if (!row) return;

    const price = msg.price;
    const diff = msg.diff;
    const rate = msg.rate;
    const cls = signClass(diff);

    const priceEl = row.querySelector('.wl-price');
    const diffEl = row.querySelector('.wl-diff');
    const rateEl = row.querySelector('.wl-rate');

    const prevPrice = Number((row.getAttribute('data-last-price') || '').replace(/,/g, ''));
    const nextPrice = Number(price);
    if (Number.isFinite(prevPrice) && Number.isFinite(nextPrice) && prevPrice !== nextPrice) {
      flashRow(row, nextPrice > prevPrice ? 'up' : 'down');
    } else if (!Number.isFinite(prevPrice) && Number.isFinite(nextPrice)) {
      flashRow(row, cls === 'up' ? 'up' : (cls === 'down' ? 'down' : ''));
    }

    if (priceEl) {
      const p = Number(price);
      priceEl.textContent = Number.isFinite(p) ? Math.round(p).toLocaleString() : '-';
      priceEl.className = 'wl-price ' + cls;
      if (Number.isFinite(p)) row.setAttribute('data-last-price', String(p));
    }
    if (diffEl) {
      diffEl.textContent = fmtSigned(diff, 0);
      diffEl.className = 'wl-diff ' + cls;
    }
    if (rateEl) {
      const r = Number(rate);
      rateEl.textContent = Number.isFinite(r) ? (fmtSigned(r, 2) + '%') : '-%';
      rateEl.className = 'wl-rate ' + cls;
    }
  }

  function setEditMode(on) {
    state.editMode = !!on;
    if (els.list) els.list.classList.toggle('edit-mode', state.editMode);
    const btn = document.getElementById('wlEdit');
    if (btn) btn.classList.toggle('active', state.editMode);
  }

  function sortItems() {
    state.sortAsc = !state.sortAsc;
    const dir = state.sortAsc ? 1 : -1;
    state.items.sort((a, b) => {
      const an = String(a.STOCK_KO_NAME || a.STOCK_EN_NAME || a.STOCK_CODE || '');
      const bn = String(b.STOCK_KO_NAME || b.STOCK_EN_NAME || b.STOCK_CODE || '');
      return an.localeCompare(bn, 'ko') * dir;
    });
    renderItems(state.items);
    if (state.editMode && els.list) els.list.classList.add('edit-mode');
    setStatus(state.sortAsc ? '이름↑ 정렬' : '이름↓ 정렬');
  }

  function removeItem(code) {
    if (!state.groupId || !code || !window.__MOBILE.urls.watchlistToggle) return;
    const body = new URLSearchParams({ groupId: state.groupId, stockCode: code });
    fetch(window.__MOBILE.urls.watchlistToggle, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: body.toString(),
      credentials: 'include'
    })
      .then(r => r.json())
      .then(() => loadItems())
      .catch(() => setStatus('삭제 실패'));
  }

  function bind() {
    if (els.group) {
      els.group.addEventListener('change', () => {
        state.groupId = els.group.value;
        loadItems();
      });
    }

    const addBtn = document.getElementById('wlAdd');
    if (addBtn) addBtn.addEventListener('click', () => {
      const s = document.querySelector('.toolbar .search');
      if (s) s.click();
    });

    const sortBtn = document.getElementById('wlSort');
    if (sortBtn) sortBtn.addEventListener('click', sortItems);

    const editBtn = document.getElementById('wlEdit');
    if (editBtn) editBtn.addEventListener('click', () => setEditMode(!state.editMode));

    const settingBtn = document.getElementById('wlSetting');
    if (settingBtn) settingBtn.addEventListener('click', () => {
      const m = document.querySelector('.toolbar .menu');
      if (m) m.click();
    });

    if (els.list) {
      els.list.addEventListener('click', (e) => {
        const remove = e.target.closest('.wl-remove');
        if (remove) {
          const row = e.target.closest('.wl-item');
          if (!row) return;
          if (!state.editMode) return;
          removeItem(row.dataset.code);
          return;
        }

        const row = e.target.closest('.wl-item');
        if (!row) return;
        if (state.editMode) return;
        const code = row.dataset.code;
        location.href = window.__MOBILE.urls.mobileChart + '?code=' + encodeURIComponent(code);
      });
    }
  }

  window.reloadWatchlist = function() {
    return loadItems();
  };

  window.onMarketChanged = function(market) {
    state.market = (market === 'US') ? 'US' : 'KR';
    state.groupId = null;
    closeWs();
    loadGroups();
  };

  bind();
  loadGroups();
})();
