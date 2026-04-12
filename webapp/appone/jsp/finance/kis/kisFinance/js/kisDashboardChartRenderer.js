(function (global) {
  'use strict';

  function toNumber(value) {
    var text;
    if (value === null || value === undefined) return NaN;
    text = String(value).replace(/,/g, '').trim();
    if (!text) return NaN;
    if (text.charAt(0) === '+') {
      text = text.substring(1);
    }
    return parseFloat(text);
  }

  var Renderer = {
    state: null,

    render: function (cfg) {
      cfg = cfg || {};
      var containerId = cfg.containerId || 'kisChartContainer';
      var container = document.getElementById(containerId);
      if (!container) return;

      this.destroy();

      var canvas = document.createElement('canvas');
      canvas.className = 'kis-dashboard-canvas';
      canvas.id = 'kisDashboardCanvas';
      container.innerHTML = '';
      container.classList.add('kis-dashboard-chart-host');
      container.appendChild(canvas);

      var rows = (cfg.rows || []).slice();
      rows.sort(function (a, b) { return a.t - b.t; });

      var maSeries = (cfg.maSeries || []).map(function (s) {
        var map = {};
        (s.data || []).forEach(function (p) {
          map[p[0]] = Number(p[1]);
        });
        return {
          id: s.id || s.name,
          name: s.name || '',
          period: Number(s.maPeriod || String(s.id || '').replace(/[^0-9]/g, '')) || 0,
          color: s.color || '#888',
          lineWidth: Number(s.lineWidth || 1.8),
          map: map
        };
      });

      var view = {
        minCount: 30,
        maxCount: 5000,
        count: Math.min(160, Math.max(60, rows.length || 60)),
        start: Math.max(0, (rows.length || 0) - Math.min(160, Math.max(60, rows.length || 60)))
      };

      this.state = {
        container: container,
        canvas: canvas,
        rows: rows,
        periodDivCode: String(cfg.periodDivCode || 'D').toUpperCase(),
        options: cfg.options || {},
        maSeries: maSeries,
        onHover: typeof cfg.onHover === 'function' ? cfg.onHover : null,
        onLeave: typeof cfg.onLeave === 'function' ? cfg.onLeave : null,
        view: view,
        hoverIndex: -1,
        realtimePrice: null,
        realtimePrevClose: null,
        drag: null,
        resizeObserver: null,
        handlers: []
      };

      this._bindEvents();
      this._bindResize();
      this._draw();
    },

    destroy: function () {
      var s = this.state;
      if (!s) return;
      try {
        if (s.resizeObserver) s.resizeObserver.disconnect();
      } catch (e) {}
      (s.handlers || []).forEach(function (h) {
        try {
          h.el.removeEventListener(h.type, h.fn, h.opts || false);
        } catch (e) {}
      });
      s.handlers = [];
      if (s.container) {
        s.container.classList.remove('kis-dashboard-chart-host');
      }
      this.state = null;
    },

    updateRealtimePrice: function (price, prevClose) {
      var s = this.state;
      var numericPrice;
      var numericPrevClose;
      var lastRow;
      var lastClose;

      if (!s || !s.rows || !s.rows.length) return;

      numericPrice = toNumber(price);
      if (!Number.isFinite(numericPrice)) return;

      if (numericPrice === 0) {
        lastRow = s.rows[s.rows.length - 1];
        lastClose = lastRow ? Number(lastRow.c) : NaN;
        if ((Number.isFinite(s.realtimePrice) && s.realtimePrice > 0) ||
            (Number.isFinite(lastClose) && lastClose > 0)) {
          return;
        }
      }

      numericPrevClose = toNumber(prevClose);
      s.realtimePrice = numericPrice;
      s.realtimePrevClose = Number.isFinite(numericPrevClose) ? numericPrevClose : null;
      this._draw();
    },

    _bindResize: function () {
      var self = this;
      var s = self.state;
      if (!s || !s.container) return;
      if (typeof ResizeObserver !== 'undefined') {
        var ro = new ResizeObserver(function () { self._draw(); });
        ro.observe(s.container);
        s.resizeObserver = ro;
      } else {
        var fn = function () { self._draw(); };
        window.addEventListener('resize', fn);
        s.handlers.push({ el: window, type: 'resize', fn: fn });
      }
    },

    _bindEvents: function () {
      var self = this;
      var s = self.state;
      if (!s || !s.canvas) return;
      var canvas = s.canvas;

      function on(el, type, fn, opts) {
        el.addEventListener(type, fn, opts || false);
        s.handlers.push({ el: el, type: type, fn: fn, opts: opts || false });
      }

      on(canvas, 'wheel', function (e) {
        e.preventDefault();
        var total = s.rows.length;
        if (!total) return;

        var rect = canvas.getBoundingClientRect();
        var x = e.clientX - rect.left;
        var dx = Math.max(1, rect.width - 90 - 16);
        var ratio = Math.max(0, Math.min(1, (x - 16) / dx));

        var prevCount = s.view.count;
        var zoomIn = e.deltaY < 0;
        var nextCount = Math.round(prevCount * (zoomIn ? 0.88 : 1.14));
        nextCount = Math.max(s.view.minCount, Math.min(s.view.maxCount, Math.min(total, nextCount)));
        if (nextCount === prevCount) return;

        var anchor = s.view.start + prevCount * ratio;
        s.view.count = nextCount;
        s.view.start = anchor - nextCount * ratio;
        var maxStart = Math.max(0, total - s.view.count);
        s.view.start = Math.max(0, Math.min(maxStart, s.view.start));
        self._draw();
      }, { passive: false });

      on(canvas, 'pointerdown', function (e) {
        s.drag = {
          startX: e.clientX,
          startView: s.view.start
        };
        try { canvas.setPointerCapture(e.pointerId); } catch (ignore) {}
      });

      on(canvas, 'pointermove', function (e) {
        var total = s.rows.length;
        if (!total) return;

        var rect = canvas.getBoundingClientRect();
        var x = e.clientX - rect.left;
        var left = 16;
        var right = rect.width - 90;

        if (s.drag) {
          var dx = e.clientX - s.drag.startX;
          var pxPerCandle = Math.max(0.5, (right - left) / Math.max(1, s.view.count));
          s.view.start = s.drag.startView - (dx / pxPerCandle);
          var maxStart = Math.max(0, total - s.view.count);
          s.view.start = Math.max(0, Math.min(maxStart, s.view.start));
          self._draw();
          return;
        }

        if (x < left || x > right) {
          if (s.hoverIndex !== -1) {
            s.hoverIndex = -1;
            if (s.onLeave) s.onLeave();
            self._draw();
          }
          return;
        }

        var first = Math.max(0, Math.floor(s.view.start));
        var visible = self._visibleRows();
        if (!visible.length) return;
        var rel = (x - left) / Math.max(1, (right - left));
        var idx = Math.round(rel * (visible.length - 1));
        idx = Math.max(0, Math.min(visible.length - 1, idx));
        var globalIdx = first + idx;

        if (globalIdx !== s.hoverIndex) {
          s.hoverIndex = globalIdx;
          var row = s.rows[globalIdx];
          if (row && s.onHover) s.onHover(row, globalIdx);
          self._draw();
        }
      });

      function stopDrag() { s.drag = null; }
      on(canvas, 'pointerup', stopDrag);
      on(canvas, 'pointercancel', stopDrag);
      on(canvas, 'pointerleave', function () {
        stopDrag();
        if (s.hoverIndex !== -1) {
          s.hoverIndex = -1;
          if (s.onLeave) s.onLeave();
          self._draw();
        }
      });

      on(canvas, 'dblclick', function () {
        var total = s.rows.length;
        s.view.count = Math.min(160, Math.max(60, total || 60));
        s.view.start = Math.max(0, total - s.view.count);
        self._draw();
      });
    },

    _visibleRows: function () {
      var s = this.state;
      if (!s) return [];
      var rows = s.rows;
      var total = rows.length;
      if (!total) return [];
      s.view.count = Math.max(s.view.minCount, Math.min(s.view.maxCount, Math.min(total, s.view.count)));
      var maxStart = Math.max(0, total - s.view.count);
      s.view.start = Math.max(0, Math.min(maxStart, s.view.start));
      var start = Math.max(0, Math.floor(s.view.start));
      var end = Math.min(total, start + Math.ceil(s.view.count) + 2);
      return rows.slice(start, end);
    },

    _drawRoundRect: function (ctx, x, y, w, h, r) {
      ctx.beginPath();
      ctx.moveTo(x + r, y);
      ctx.lineTo(x + w - r, y);
      ctx.quadraticCurveTo(x + w, y, x + w, y + r);
      ctx.lineTo(x + w, y + h - r);
      ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
      ctx.lineTo(x + r, y + h);
      ctx.quadraticCurveTo(x, y + h, x, y + h - r);
      ctx.lineTo(x, y + r);
      ctx.quadraticCurveTo(x, y, x + r, y);
      ctx.closePath();
    },

    _draw: function () {
      var s = this.state;
      if (!s || !s.canvas || !s.container) return;

      var canvas = s.canvas;
      var dpr = window.devicePixelRatio || 1;
      var W = s.container.clientWidth || 0;
      var H = s.container.clientHeight || 0;
      if (!W || !H) return;

      canvas.width = Math.floor(W * dpr);
      canvas.height = Math.floor(H * dpr);
      canvas.style.width = W + 'px';
      canvas.style.height = H + 'px';

      var ctx = canvas.getContext('2d');
      ctx.setTransform(1, 0, 0, 1, 0, 0);
      ctx.scale(dpr, dpr);

      var rows = this._visibleRows();
      if (!rows.length) {
        ctx.clearRect(0, 0, W, H);
        return;
      }

      var marginL = 16, marginR = 90, marginT = 40, marginB = 34;
      var chartW = W - marginL - marginR;
      var totalH = H - marginT - marginB;
      var showVol = !!s.options.volumeEnabled;
      var mainH = showVol ? totalH * 0.68 : totalH;
      var volGap = showVol ? totalH * 0.04 : 0;
      var volH = showVol ? totalH * 0.25 : 0;
      var volY0 = marginT + mainH + volGap;

      var minP = Infinity, maxP = -Infinity;
      rows.forEach(function (r) {
        minP = Math.min(minP, r.l);
        maxP = Math.max(maxP, r.h);
      });

      var maLines = [];
      var firstTs = rows[0].t;
      var lastTs = rows[rows.length - 1].t;
      var last = rows[rows.length - 1];
      var prev = rows.length > 1 ? rows[rows.length - 2] : last;
      var cpValue = Number.isFinite(s.realtimePrice) ? Number(s.realtimePrice) : Number(last.c);
      var cpPrev = Number.isFinite(s.realtimePrevClose) ? Number(s.realtimePrevClose) : Number(prev.c);
      s.maSeries.forEach(function (line) {
        if (!line || !line.map) return;
        var vals = [];
        rows.forEach(function (r) {
          var v = line.map[r.t];
          vals.push(Number.isFinite(v) ? v : null);
          if (Number.isFinite(v)) {
            minP = Math.min(minP, v);
            maxP = Math.max(maxP, v);
          }
        });
        maLines.push({ meta: line, vals: vals });
      });

      if (Number.isFinite(cpValue)) {
        minP = Math.min(minP, cpValue);
        maxP = Math.max(maxP, cpValue);
      }

      if (!Number.isFinite(minP) || !Number.isFinite(maxP) || minP === maxP) {
        minP = rows[0].c * 0.95;
        maxP = rows[0].c * 1.05;
      }
      var pad = (maxP - minP) * 0.08;
      minP -= pad;
      maxP += pad;

      function yPrice(p) {
        return marginT + (1 - ((p - minP) / (maxP - minP))) * mainH;
      }
      function xCandle(i) {
        return marginL + ((i + 0.5) / Math.max(1, rows.length)) * chartW;
      }
      var candleW = Math.max(0.8, chartW / Math.max(1, rows.length) * 0.58);

      ctx.clearRect(0, 0, W, H);
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, W, H);

      ctx.strokeStyle = 'rgba(70,78,90,0.25)';
      ctx.lineWidth = 0.4;
      var grid = 6;
      for (var g = 0; g <= grid; g++) {
        var gy = marginT + (mainH / grid) * g;
        ctx.beginPath();
        ctx.moveTo(marginL, gy);
        ctx.lineTo(W - marginR, gy);
        ctx.stroke();

        var p = maxP - ((maxP - minP) / grid) * g;
        ctx.fillStyle = '#51607a';
        ctx.font = '10px JetBrains Mono, monospace';
        ctx.textAlign = 'left';
        ctx.fillText(Math.round(p).toLocaleString('ko-KR'), W - marginR + 6, gy + 3);
      }

      var boundaryIdx = [];
      if (s.options.monthLinesEnabled !== false) {
        for (var i = 1; i < rows.length; i++) {
          var pm = new Date(rows[i - 1].t).getMonth();
          var cm = new Date(rows[i].t).getMonth();
          var py = new Date(rows[i - 1].t).getFullYear();
          var cy = new Date(rows[i].t).getFullYear();
          if (pm !== cm || py !== cy) boundaryIdx.push(i);
        }
      }

      var doubleMode = String(s.options.doubleChartMode || (s.options.doubleChartEnabled ? 'all' : 'off')).toLowerCase();
      if (s.periodDivCode === 'D' && doubleMode !== 'off') {
        var segStart = 0;
        var segs = [];
        for (var si = 1; si < rows.length; si++) {
          var pKey = (new Date(rows[si - 1].t).getFullYear()) + '-' + (new Date(rows[si - 1].t).getMonth());
          var cKey = (new Date(rows[si].t).getFullYear()) + '-' + (new Date(rows[si].t).getMonth());
          if (pKey !== cKey) {
            segs.push({ start: segStart, end: si - 1 });
            segStart = si;
          }
        }
        segs.push({ start: segStart, end: rows.length - 1 });

        var targetSegs = (doubleMode === 'recent') ? segs.slice(-1) : segs;
        targetSegs.forEach(function (seg) {
          var mRows = rows.slice(seg.start, seg.end + 1);
          if (!mRows.length) return;
          var mOpen = mRows[0].o;
          var mClose = mRows[mRows.length - 1].c;
          var mHigh = mRows.reduce(function (a, r) { return Math.max(a, r.h); }, -Infinity);
          var mLow = mRows.reduce(function (a, r) { return Math.min(a, r.l); }, Infinity);

          var x0 = xCandle(seg.start) - candleW * 0.7;
          var x1 = xCandle(seg.end) + candleW * 0.7;
          var cx = (x0 + x1) / 2;
          var isUpM = mClose >= mOpen;
          var colM = isUpM ? 'rgba(239,68,68,0.45)' : 'rgba(37,99,235,0.45)';
          var bodyTop = yPrice(Math.max(mOpen, mClose));
          var bodyBot = yPrice(Math.min(mOpen, mClose));
          var wickTop = yPrice(mHigh);
          var wickBot = yPrice(mLow);

          ctx.setLineDash([4, 4]);
          ctx.strokeStyle = 'rgba(80,80,80,0.45)';
          ctx.lineWidth = 0.9;
          ctx.beginPath();
          ctx.moveTo(x0, marginT);
          ctx.lineTo(x0, marginT + mainH);
          ctx.stroke();
          ctx.setLineDash([]);

          ctx.strokeStyle = 'rgba(95,95,95,0.55)';
          ctx.lineWidth = 1.2;
          ctx.beginPath();
          ctx.moveTo(cx, wickTop);
          ctx.lineTo(cx, wickBot);
          ctx.stroke();

          ctx.fillStyle = colM;
          ctx.fillRect(cx - Math.max(10, (x1 - x0) * 0.46), bodyTop, Math.max(10, (x1 - x0) * 0.92), Math.max(2, bodyBot - bodyTop));
        });
      }

      boundaryIdx.forEach(function (bi) {
        var xLine = xCandle(bi) - candleW * 0.65;
        ctx.setLineDash([3, 5]);
        ctx.strokeStyle = 'rgba(90,90,90,0.38)';
        ctx.lineWidth = 0.4;
        ctx.beginPath();
        ctx.moveTo(xLine, marginT);
        ctx.lineTo(xLine, marginT + mainH);
        ctx.stroke();
        ctx.setLineDash([]);
      });

      maLines.forEach(function (line) {
        ctx.strokeStyle = line.meta.color || '#888';
        ctx.lineWidth = line.meta.lineWidth || 1.8;
        ctx.beginPath();
        var started = false;
        for (var i = 0; i < line.vals.length; i++) {
          var v = line.vals[i];
          if (!Number.isFinite(v)) continue;
          var x = xCandle(i);
          var y = yPrice(v);
          if (!started) {
            ctx.moveTo(x, y);
            started = true;
          } else {
            ctx.lineTo(x, y);
          }
        }
        if (started) ctx.stroke();
      });

      if (s.options.highLowEnabled && rows.length > 1) {
        var lookback = rows.slice(0, rows.length - 1);
        var currentClose = cpValue;
        var hiIdx = 0, loIdx = 0;
        for (var hk = 1; hk < lookback.length; hk++) {
          if (lookback[hk].h > lookback[hiIdx].h) hiIdx = hk;
          if (lookback[hk].l < lookback[loIdx].l) loIdx = hk;
        }

        function drawHLLabel(ix, price, label, color, up) {
          var xh = xCandle(ix);
          var yh = yPrice(price);
          var by = up ? (yh - 24) : (yh + 24);
          var p = Number(price);
          var priceText = (Math.abs(p - Math.round(p)) > 1e-9)
            ? p.toLocaleString('ko-KR', { minimumFractionDigits: 3, maximumFractionDigits: 3 })
            : Math.round(p).toLocaleString('ko-KR');
          var pct = (Number.isFinite(currentClose) && p !== 0) ? (((currentClose - p) / p) * 100) : NaN;
          var pctText = Number.isFinite(pct) ? (pct.toFixed(2) + '%') : '-';
          var dt = new Date(lookback[ix].t);
          var mm = String(dt.getMonth() + 1).padStart(2, '0');
          var dd = String(dt.getDate()).padStart(2, '0');
          var txt = priceText + '(' + pctText + ', ' + mm + '/' + dd + ')';

          ctx.font = '700 11px Arial, "Apple SD Gothic Neo", "Malgun Gothic", sans-serif';
          ctx.textAlign = 'left';
          var tw = ctx.measureText(txt).width;
          var tx = Math.max(marginL + 14, Math.min(xh - (tw / 2), W - marginR - tw - 4));
          var arrowX = Math.max(marginL + 6, Math.min(xh, W - marginR - 6));
          var arrowY = up ? (yh - 12) : (yh + 12);

          // Anchor arrow must sit on the candle/high-low point.
          ctx.fillStyle = color;
          ctx.beginPath();
          if (up) {
            ctx.moveTo(arrowX, arrowY);
            ctx.lineTo(arrowX - 4, arrowY + 7);
            ctx.lineTo(arrowX + 4, arrowY + 7);
          } else {
            ctx.moveTo(arrowX, arrowY);
            ctx.lineTo(arrowX - 4, arrowY - 7);
            ctx.lineTo(arrowX + 4, arrowY - 7);
          }
          ctx.closePath();
          ctx.fill();
          ctx.lineWidth = 2.2;
          ctx.strokeStyle = 'rgba(255,255,255,0.92)';
          ctx.strokeText(txt, tx, by);
          ctx.fillStyle = color;
          ctx.fillText(txt, tx, by);
        }

        drawHLLabel(hiIdx, lookback[hiIdx].h, '전고', '#ef4444', true);
        drawHLLabel(loIdx, lookback[loIdx].l, '전저', '#2563eb', false);
      }

      rows.forEach(function (r, i) {
        var x = xCandle(i);
        var isUp = r.c >= r.o;
        var color = isUp ? '#ef4444' : '#2563eb';

        ctx.strokeStyle = color;
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(x, yPrice(r.h));
        ctx.lineTo(x, yPrice(r.l));
        ctx.stroke();

        var yTop = yPrice(Math.max(r.o, r.c));
        var yBot = yPrice(Math.min(r.o, r.c));
        ctx.fillStyle = color;
        ctx.fillRect(x - candleW / 2, yTop, candleW, Math.max(1.5, yBot - yTop));
      });

      if (showVol) {
        var maxVol = rows.reduce(function (a, r) { return Math.max(a, Number(r.v || 0)); }, 0);
        ctx.strokeStyle = 'rgba(70,78,90,0.25)';
        ctx.lineWidth = 1;
        ctx.beginPath(); ctx.moveTo(marginL, volY0); ctx.lineTo(W - marginR, volY0); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(marginL, volY0 + volH); ctx.lineTo(W - marginR, volY0 + volH); ctx.stroke();

        rows.forEach(function (r, i) {
          var x = xCandle(i);
          var vh = maxVol > 0 ? (Number(r.v || 0) / maxVol) * volH * 0.95 : 0;
          var isUp = r.c >= r.o;
          ctx.fillStyle = isUp ? 'rgba(239,68,68,0.45)' : 'rgba(37,99,235,0.45)';
          ctx.fillRect(x - candleW / 2, volY0 + volH - vh, candleW, vh);
        });
      }

      var cpColor = cpValue >= cpPrev ? '#ef4444' : '#2563eb';
      var cpY = yPrice(cpValue);

      ctx.setLineDash([6, 4]);
      ctx.strokeStyle = cpColor;
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(marginL, cpY);
      ctx.lineTo(W - marginR, cpY);
      ctx.stroke();
      ctx.setLineDash([]);

      ctx.fillStyle = cpColor;
      this._drawRoundRect(ctx, W - marginR + 1, cpY - 10, 74, 20, 4);
      ctx.fill();
      ctx.fillStyle = '#fff';
      ctx.font = 'bold 11px JetBrains Mono, monospace';
      ctx.textAlign = 'center';
      var lastPriceText = (Math.abs(cpValue - Math.round(cpValue)) > 1e-9)
        ? Number(cpValue).toLocaleString('ko-KR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
        : Math.round(cpValue).toLocaleString('ko-KR');
      ctx.fillText(lastPriceText, W - marginR + 38, cpY + 4);

      ctx.fillStyle = '#51607a';
      ctx.font = '10px JetBrains Mono, monospace';
      ctx.textAlign = 'center';
      var step = Math.max(1, Math.floor(rows.length / 8));
      var prevLabelYear = null;
      for (var di = 0; di < rows.length; di += step) {
        var d = new Date(rows[di].t);
        var m = d.getMonth() + 1;
        var y = d.getFullYear();
        var t = m + '/' + d.getDate();
        if (prevLabelYear !== null && y !== prevLabelYear && m === 1) {
          t = y + '/' + m;
        }
        ctx.fillText(t, xCandle(di), H - 10);
        prevLabelYear = y;
      }

      if (s.hoverIndex >= 0 && s.rows[s.hoverIndex]) {
        var hoverRow = s.rows[s.hoverIndex];
        if (hoverRow.t >= firstTs && hoverRow.t <= lastTs) {
          var localIdx = -1;
          for (var li = 0; li < rows.length; li++) {
            if (rows[li].t === hoverRow.t) { localIdx = li; break; }
          }
          if (localIdx >= 0) {
            var hx = xCandle(localIdx);
            var hy = yPrice(hoverRow.c);
            ctx.setLineDash([2, 4]);
            ctx.strokeStyle = 'rgba(0,0,0,0.45)';
            ctx.beginPath(); ctx.moveTo(hx, marginT); ctx.lineTo(hx, marginT + mainH); ctx.stroke();
            ctx.beginPath(); ctx.moveTo(marginL, hy); ctx.lineTo(W - marginR, hy); ctx.stroke();
            ctx.setLineDash([]);
          }
        }
      }
    }
  };

  global.KisDashboardChartRenderer = Renderer;
})(window);
