// KIS Mobile - 상단 툴바 (검색/알림/메뉴/기능버튼)

(function() {
  'use strict';

  let searchModal = null;
  let searchInput = null;
  let searchResults = null;
  let searchDebounceTimer = null;
  let searchAbortController = null;

  const mobileProgress = (function() {
    const bar = document.getElementById('mobileProgress');
    let timer = null;
    let current = 0;
    let active = 0;

    function render() {
      if (!bar) return;
      bar.style.width = current + '%';
      bar.style.opacity = active > 0 ? '1' : '0';
    }

    function start() {
      active += 1;
      if (active === 1) {
        current = 18;
        render();
        if (timer) clearInterval(timer);
        timer = setInterval(() => {
          if (current < 88) {
            current += (88 - current) * 0.18;
            render();
          }
        }, 120);
      }
    }

    function done() {
      if (active > 0) active -= 1;
      if (active > 0) return;
      if (timer) {
        clearInterval(timer);
        timer = null;
      }
      current = 100;
      render();
      setTimeout(() => {
        current = 0;
        render();
      }, 180);
    }

    return { start, done };
  })();

  window.mobileProgress = mobileProgress;

  const urls = (window.__MOBILE && window.__MOBILE.urls) || {};

  function escapeHtml(s) {
    return String(s || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function goChart(code) {
    if (!code) return;
    location.href = (urls.mobileChart || '/scheduler/finance/mobile/chart.do') + '?code=' + encodeURIComponent(code);
  }

  function isWatchlistPage() {
    return !!document.getElementById('wlGroup');
  }

  function addToWatchlist(code) {
    const groupSel = document.getElementById('wlGroup');
    const groupId = groupSel ? groupSel.value : '';
    if (!groupId || !code || !urls.watchlistToggle) return Promise.resolve(false);

    const body = new URLSearchParams({ groupId: groupId, stockCode: code });
    mobileProgress.start();
    return fetch(urls.watchlistToggle, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: body.toString(),
      credentials: 'include'
    })
      .then(r => r.json())
      .then(() => {
        if (typeof window.reloadWatchlist === 'function') window.reloadWatchlist();
        return true;
      })
      .catch(() => false)
      .finally(() => mobileProgress.done());
  }

  function createSearchModal() {
    const modal = document.createElement('div');
    modal.className = 'search-modal';
    modal.innerHTML = `
      <div class="search-modal-content">
        <div class="search-modal-header">
          <input type="text" class="search-modal-input" placeholder="종목명 또는 코드 입력 (예: 삼성전)" autocomplete="off">
          <button class="search-modal-close" aria-label="닫기">×</button>
        </div>
        <div class="search-modal-results"></div>
      </div>
    `;
    document.body.appendChild(modal);

    searchModal = modal;
    searchInput = modal.querySelector('.search-modal-input');
    searchResults = modal.querySelector('.search-modal-results');

    modal.querySelector('.search-modal-close').addEventListener('click', closeSearchModal);

    modal.addEventListener('click', function(e) {
      if (e.target === modal) closeSearchModal();
    });

    searchInput.addEventListener('input', function() {
      const keyword = searchInput.value.trim();
      if (!keyword) {
        if (searchAbortController) {
          searchAbortController.abort();
          searchAbortController = null;
        }
        searchResults.innerHTML = '<div class="search-modal-empty">검색어를 입력하세요.</div>';
        return;
      }
      if (keyword.length < 2) {
        searchResults.innerHTML = '<div class="search-modal-empty">2글자 이상 입력해 주세요.</div>';
        return;
      }
      if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
      searchDebounceTimer = setTimeout(function() {
        performSearch(keyword);
      }, 250);
    });

    searchInput.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') {
        closeSearchModal();
        return;
      }
      if (e.key === 'Enter') {
        e.preventDefault();
        const first = searchResults.querySelector('.search-result-item');
        if (!first) return;
        const code = first.dataset.code;
        if (isWatchlistPage()) {
          addToWatchlist(code).then(function(ok) {
            if (ok) closeSearchModal();
          });
        } else {
          goChart(code);
          closeSearchModal();
        }
      }
    });
  }

  function openSearchModal() {
    if (!searchModal) createSearchModal();
    searchModal.classList.add('active');
    searchInput.value = '';
    searchResults.innerHTML = '<div class="search-modal-empty">검색어를 입력하세요.</div>';
    setTimeout(function() { searchInput.focus(); }, 30);
  }

  function closeSearchModal() {
    if (searchAbortController) {
      searchAbortController.abort();
      searchAbortController = null;
    }
    if (searchDebounceTimer) {
      clearTimeout(searchDebounceTimer);
      searchDebounceTimer = null;
    }
    if (searchModal) searchModal.classList.remove('active');
  }

  function performSearch(keyword) {
    if (!urls.search) return;
    if (searchAbortController) {
      searchAbortController.abort();
    }
    searchAbortController = new AbortController();

    searchResults.innerHTML = '<div class="search-modal-empty">검색중...</div>';

    const url = urls.search + '?in_stockCode=' + encodeURIComponent(keyword);
    mobileProgress.start();
    fetch(url, { signal: searchAbortController.signal })
      .then(r => r.json())
      .then(json => {
        const list = (json && Array.isArray(json.data)) ? json.data.slice(0, 30) : [];
        renderSearchResults(list, keyword);
      })
      .catch((e) => {
        if (e && e.name === 'AbortError') return;
        searchResults.innerHTML = '<div class="search-modal-empty">검색 실패</div>';
      })
      .finally(() => {
        searchAbortController = null;
        mobileProgress.done();
      });
  }

  function renderSearchResults(list, keyword) {
    if (!list.length) {
      searchResults.innerHTML = '<div class="search-modal-empty">검색 결과가 없습니다.</div>';
      return;
    }

    const q = String(keyword || '');
    const qRe = q ? new RegExp('(' + q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi') : null;

    searchResults.innerHTML = list.map(function(item) {
      const code = item.stockCode || item.stock_code || item.code || '';
      const nameKo = item.stockKoName || item.stock_ko_name || item.nameKo || '';
      const nameEn = item.stockEnName || item.stock_en_name || item.nameEn || '';
      const market = item.stockMarket || item.stock_market || item.market || '';
      const close = item.stockClose || item.stock_close || item.close || '';

      const baseName = nameKo || nameEn || code;
      const safeName = escapeHtml(baseName);
      const renderedName = (qRe && safeName)
        ? safeName.replace(qRe, '<mark>$1</mark>')
        : safeName;

      let icon = '';
      if (nameKo) icon = nameKo.substring(0, 2);
      else if (nameEn) icon = nameEn.substring(0, 2).toUpperCase();
      else icon = code.substring(0, 2);

      const p = Number(String(close).replace(/,/g, ''));
      const priceText = Number.isFinite(p) && p > 0 ? p.toLocaleString() : '-';

      return `
        <div class="search-result-item" data-code="${escapeHtml(code)}">
          <div class="search-result-icon">${escapeHtml(icon || 'ST')}</div>
          <div class="search-result-info">
            <div class="search-result-name">${renderedName}</div>
            <div class="search-result-code">${escapeHtml(code)} · ${escapeHtml(market)}</div>
          </div>
          <div class="search-result-price flat">${priceText}</div>
        </div>
      `;
    }).join('');

    searchResults.querySelectorAll('.search-result-item').forEach(function(item) {
      item.addEventListener('click', function() {
        const code = item.dataset.code;
        if (isWatchlistPage()) {
          addToWatchlist(code).then(function(ok) {
            if (ok) closeSearchModal();
          });
        } else {
          closeSearchModal();
          goChart(code);
        }
      });
    });
  }

  function createMenuModal() {
    const isChart = !!document.getElementById('priceChart');
    const modal = document.createElement('div');
    modal.className = 'quick-menu-modal';

    if (isChart) {
      modal.innerHTML = `
        <div class="quick-menu-sheet chart-tool-sheet">
          <button class="quick-menu-item" data-go="indicator">📉 보조지표</button>
          <button class="quick-menu-item" data-go="strategy">⇅ 매매전략</button>
          <button class="quick-menu-item" data-go="type">🕯 차트종류</button>
          <button class="quick-menu-item" data-go="chartOption">🔧 차트설정</button>
          <button class="quick-menu-item close" data-go="close">✕ 닫기</button>
        </div>
      `;
    } else {
      modal.innerHTML = `
        <div class="quick-menu-sheet">
          <button class="quick-menu-item" data-go="watchlist">⭐ 관심종목</button>
          <button class="quick-menu-item" data-go="analysis">🧭 발굴분석</button>
          <button class="quick-menu-item" data-go="chart">📊 주식차트</button>
          <button class="quick-menu-item" data-go="legacy">📈 KIS 전체화면</button>
          <button class="quick-menu-item" data-go="reload">🔄 새로고침</button>
          <button class="quick-menu-item close" data-go="close">닫기</button>
        </div>
      `;
    }

    document.body.appendChild(modal);

    modal.addEventListener('click', function(e) {
      if (e.target === modal) modal.classList.remove('active');
    });

    modal.querySelectorAll('.quick-menu-item').forEach(btn => {
      btn.addEventListener('click', function() {
        const go = this.dataset.go;
        modal.classList.remove('active');
        if (go === 'close') return;
        if (go === 'watchlist') location.href = urls.mobileWatchlist || '/scheduler/finance/mobile/watchlist.do';
        if (go === 'analysis') location.href = urls.mobileAnalysis || '/scheduler/finance/mobile/analysis.do';
        if (go === 'chart') goChart((window.__MOBILE && window.__MOBILE.code) || '005930');
        if (go === 'legacy') location.href = urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp';
        if (go === 'reload') location.reload();
        if (go === 'indicator') location.href = (urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp') + '?focus=indicator&code=' + encodeURIComponent((window.__MOBILE && window.__MOBILE.code) || '');
        if (go === 'strategy') location.href = (urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp') + '?focus=signal&code=' + encodeURIComponent((window.__MOBILE && window.__MOBILE.code) || '');
        if (go === 'type') location.href = (urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp') + '?focus=chart&code=' + encodeURIComponent((window.__MOBILE && window.__MOBILE.code) || '');
        if (go === 'chartOption') {
          var panel = document.getElementById('chartOptions');
          if (panel) panel.classList.remove('hidden');
        }
      });
    });

    return modal;
  }

  let menuModal = null;
  function openMenuModal() {
    if (!menuModal) menuModal = createMenuModal();
    menuModal.classList.add('active');
  }

  function bindMarketSwitch() {
    const btn = document.getElementById('marketToggle');
    if (!btn) return;

    const saved = localStorage.getItem('mobile.market') || 'KR';

    function paint(market) {
      btn.dataset.market = market;
      btn.classList.add('active');
      btn.innerHTML = (market === 'US' ? '미국' : '한국') + ' <span class="dot"></span>';
    }

    paint(saved);

    btn.addEventListener('click', function() {
      const current = btn.dataset.market || 'KR';
      const next = current === 'US' ? 'KR' : 'US';
      localStorage.setItem('mobile.market', next);
      paint(next);

      if (typeof window.onMarketChanged === 'function') {
        window.onMarketChanged(next);
        return;
      }

      if (document.getElementById('priceChart')) {
        const code = next === 'US' ? 'AAPL' : '005930';
        location.href = (urls.mobileChart || '/scheduler/finance/mobile/chart.do') + '?code=' + encodeURIComponent(code);
      }
    });
  }

  bindMarketSwitch();

  const searchBtn = document.querySelector('.toolbar .search');
  if (searchBtn) searchBtn.addEventListener('click', openSearchModal);

  const alarmBtn = document.querySelector('.toolbar .alarm');
  if (alarmBtn) {
    alarmBtn.addEventListener('click', function() {
      location.href = (urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp') + '#marketIssue';
    });
  }

  const refreshBtn = document.querySelector('.toolbar .refresh');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', function() {
      if (typeof window.reloadWatchlist === 'function') {
        window.reloadWatchlist();
        return;
      }
      location.reload();
    });
  }

  const menuBtn = document.querySelector('.toolbar .menu');
  if (menuBtn) menuBtn.addEventListener('click', openMenuModal);

  document.querySelectorAll('.features .feature-btn[data-action]').forEach(btn => {
    btn.addEventListener('click', function() {
      const action = this.dataset.action;
      const code = (window.__MOBILE && window.__MOBILE.code) || '';
      if (action === 'indicator') {
        location.href = (urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp') + '?focus=signal&code=' + encodeURIComponent(code);
        return;
      }
      if (action === 'ai') {
        location.href = (urls.legacyHome || '/scheduler/appone/jsp/finance/kis/kisFinance/kisFinance.jsp') + '?focus=ai&code=' + encodeURIComponent(code);
      }
    });
  });
})();
