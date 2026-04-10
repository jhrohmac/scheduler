// KIS Mobile - 하단 네비게이션 + 차트 옵션

(function() {
  'use strict';

  // 타임프레임 버튼(차트 페이지에서만 존재)
  document.querySelectorAll('.tf-btn[data-tf]').forEach(btn => {
    btn.addEventListener('click', function() {
      const tf = this.dataset.tf;
      if (window.changeTF) window.changeTF(tf);
    });
  });

  // 차트 옵션 패널(차트 페이지에서만 존재)
  const optionsPanel = document.getElementById('chartOptions');
  const settingsBtn = document.querySelector('.tf-btn.settings');
  const closeBtn = optionsPanel ? optionsPanel.querySelector('.close') : null;

  if (optionsPanel && settingsBtn && closeBtn) {
    settingsBtn.addEventListener('click', function() {
      optionsPanel.classList.remove('hidden');
    });

    closeBtn.addEventListener('click', function() {
      optionsPanel.classList.add('hidden');
    });

    optionsPanel.addEventListener('click', function(e) {
      if (e.target === optionsPanel) {
        optionsPanel.classList.add('hidden');
      }
    });
  }

  // 하단 네비게이션
  document.querySelectorAll('.bottom-nav .nav-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      const go = this.dataset.go;
      if (go === 'watchlist') {
        location.href = (window.__MOBILE.urls.mobileWatchlist || '/scheduler/finance/mobile/watchlist.do');
        return;
      }
      if (go === 'analysis') {
        location.href = (window.__MOBILE.urls.mobileAnalysis || '/scheduler/finance/mobile/analysis.do');
        return;
      }
      if (go === 'chart') {
        location.href = (window.__MOBILE.urls.mobileChart || '/scheduler/finance/mobile/chart.do') +
          '?code=' + encodeURIComponent((window.__MOBILE.code || '005930'));
        return;
      }
      if (go === 'legacy') {
        location.href = window.__MOBILE.urls.legacyHome;
      }
    });
  });
})();
