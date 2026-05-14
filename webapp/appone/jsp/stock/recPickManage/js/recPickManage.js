var recPickManageConfig = window.recPickManageConfig || {};
var RP_URL = recPickManageConfig.urls || {};

var RP_STATE = {
  selectedPickId: '',
  selectedPositionId: '',
  selectedStockCode: '',
  selectedStockName: '',
  selectedPickStatus: ''
};

var RP_REALTIME = {
  ws: null,
  reconnectTimer: null,
  lastCodesKey: '',
  syncTimer: null,
  priceCache: {},
  priceInflight: {}
};

function rpAjax(url, data, cb) {
  var params = Object.keys(data).map(function(k) {
    return encodeURIComponent(k) + '=' + encodeURIComponent(data[k] == null ? '' : data[k]);
  }).join('&');
  fetch(url, { method: 'POST', headers: {'Content-Type':'application/x-www-form-urlencoded'}, body: params })
    .then(function(r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
    .then(cb)
    .catch(function(e) { alert('오류: ' + e.message); });
}

function rpPct(v) {
  if (v == null || v === '') return '-';
  var n = parseFloat(v);
  var cls = n > 0 ? 'text-danger' : (n < 0 ? 'text-primary' : '');
  return '<span class="' + cls + ' font-weight-bold">' + (n > 0 ? '+' : '') + n.toFixed(2) + '%</span>';
}

function rpToNumber(v) {
  if (v == null || v === '') return null;
  var n = Number(String(v).replace(/,/g, ''));
  return isFinite(n) ? n : null;
}

function rpFormatPrice(v) {
  var n = rpToNumber(v);
  return n == null ? '-' : n.toLocaleString();
}

function rpTodayString() {
  var d = new Date();
  var m = String(d.getMonth() + 1);
  var day = String(d.getDate());
  return d.getFullYear() + '-' + (m.length < 2 ? '0' + m : m) + '-' + (day.length < 2 ? '0' + day : day);
}

function rpFormatRecDiff(row) {
  var currentPrice = rpToNumber(row.currentPrice);
  var recAnchorPrice = rpToNumber(row.recAnchorPrice);
  if (currentPrice == null || recAnchorPrice == null || recAnchorPrice === 0) return '-';
  var diff = currentPrice - recAnchorPrice;
  var pct = (diff / recAnchorPrice * 100).toFixed(2);
  var cls = diff >= 0 ? 'text-danger' : 'text-primary';
  var sign = diff >= 0 ? '+' : '';
  return '<span class="' + cls + ' font-weight-bold">' + sign + pct + '%</span>'
       + '<br><small class="' + cls + '">' + sign + Math.round(diff).toLocaleString() + '</small>';
}

function rpFormatBuyDiff(row) {
  var currentPrice = rpToNumber(row.currentPrice);
  var firstBuyPrice = rpToNumber(row.firstBuyPrice);
  if (currentPrice == null || firstBuyPrice == null || firstBuyPrice === 0) return '-';
  var diff = currentPrice - firstBuyPrice;
  var pct = (diff / firstBuyPrice * 100).toFixed(2);
  var cls = diff >= 0 ? 'text-danger' : 'text-primary';
  var sign = diff >= 0 ? '+' : '';
  return '<span class="' + cls + ' font-weight-bold">' + sign + pct + '%</span>'
       + '<br><small class="' + cls + '">' + sign + Math.round(diff).toLocaleString() + '</small>';
}

function rpUnwrapSingle(res) {
  if (!res) return null;
  if (res.data) {
    if (res.data.singleData) return res.data.singleData;
    if (res.data.data && res.data.data.singleData) return res.data.data.singleData;
  }
  return res.singleData || null;
}

function rpExtractCurrentPrice(single) {
  var out = single && (single.output || single.out || (single.data && single.data.output));
  if (!out) return null;
  return rpToNumber(out.stckPrpr || out.stck_prpr || out.ovrsNmixPrpr || out.ovrs_nmix_prpr || out.last || out.lastPrice || out.price);
}

function rpPickBadge(s) {
  if (!s) return '';
  var m = { WATCH:'badge-info', BOUGHT:'badge-success', CLOSED:'badge-secondary' };
  return '<span class="badge ' + (m[s] || 'badge-light') + '">' + s + '</span>';
}

function rpSellBadge(s) {
  if (!s) return 'badge-secondary';
  var m = { HOLD:'badge-info', SELL_READY:'badge-warning', STOP_LOSS:'badge-danger', TP1_DONE:'badge-success', TRAILING:'badge-purple' };
  return m[s] || 'badge-secondary';
}

function rpSetFocusChip(id, label, value) {
  var el = document.getElementById(id);
  if (!el) return;
  el.innerHTML = label + ' <strong>' + (value || '미선택') + '</strong>';
}

function rpEsc(v) {
  return String(v == null ? '' : v)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function rpSelectionLabel() {
  var parts = [];
  var stockText = RP_STATE.selectedStockName
    ? RP_STATE.selectedStockName + ' (' + (RP_STATE.selectedStockCode || '-') + ')'
    : (RP_STATE.selectedStockCode || '');
  if (stockText) parts.push(stockText);
  if (RP_STATE.selectedPickId) parts.push('PICK ' + RP_STATE.selectedPickId);
  return parts.length ? parts.join(' / ') : '미선택';
}

function rpBuildWsUrl(path, query) {
  if (!path) return '';
  if (path.indexOf('ws://') === 0 || path.indexOf('wss://') === 0) {
    return query ? path + (path.indexOf('?') >= 0 ? '&' : '?') + query : path;
  }
  var proto = location.protocol === 'https:' ? 'wss://' : 'ws://';
  var normalized = path.charAt(0) === '/' ? path : '/' + path;
  var url = proto + location.host + normalized;
  if (query) {
    url += (url.indexOf('?') >= 0 ? '&' : '?') + query;
  }
  return url;
}

function rpResolveRealtimeMarket(row) {
  var sourceMktCd = String(row && row.sourceMktCd || '').toUpperCase();
  var listingMarket = String(row && row.listingMarket || '').toUpperCase();

  if (listingMarket === 'NXT' || sourceMktCd === 'NXT') {
    return { country: 'KR', market: 'NXT' };
  }
  if (listingMarket === 'UN' || listingMarket === '통합' || sourceMktCd === 'UN') {
    return { country: 'KR', market: 'UN' };
  }
  if (listingMarket === 'OVERTIME' || listingMarket === 'AFTER' || sourceMktCd === 'OVERTIME') {
    return { country: 'KR', market: 'OVERTIME' };
  }

  if (sourceMktCd === 'KR' || listingMarket === 'KOSPI' || listingMarket === 'KOSDAQ' || listingMarket === 'KRX') {
    return { country: 'KR', market: 'KRX' };
  }

  if (sourceMktCd === 'US') {
    if (listingMarket.indexOf('NYSE') >= 0 || listingMarket === 'NYS') {
      return { country: 'US', market: 'NYSE' };
    }
    if (listingMarket.indexOf('AMEX') >= 0 || listingMarket === 'AMS') {
      return { country: 'US', market: 'AMEX' };
    }
    return { country: 'US', market: listingMarket || 'NASDAQ' };
  }

  if (sourceMktCd) {
    return { country: sourceMktCd, market: listingMarket || sourceMktCd };
  }

  if (listingMarket) {
    return { country: 'US', market: listingMarket };
  }

  return { country: 'KR', market: 'KRX' };
}

function rpPickRealtimeToken(row) {
  var code = String(row && row.stkCd || '').trim();
  if (!code) return '';
  var market = rpResolveRealtimeMarket(row);
  return market.country + '|' + market.market + '|' + code;
}

function rpIsDomesticRow(row) {
  return rpResolveRealtimeMarket(row).country === 'KR';
}

function rpUpdatePickRowCells(table, rowIdx, row, price) {
  var currentPriceCell;
  var recDiffCell;
  var buyDiffCell;

  if (!table || !row) return;

  row.currentPrice = price;
  currentPriceCell = table.cell(rowIdx, 7).node();
  recDiffCell = table.cell(rowIdx, 8).node();
  buyDiffCell = table.cell(rowIdx, 9).node();

  if (currentPriceCell) currentPriceCell.innerHTML = rpFormatPrice(row.currentPrice);
  if (recDiffCell) recDiffCell.innerHTML = rpFormatRecDiff(row);
  if (buyDiffCell) buyDiffCell.innerHTML = rpFormatBuyDiff(row);
}

function rpClosePickRealtime(resetKey) {
  if (RP_REALTIME.reconnectTimer) {
    clearTimeout(RP_REALTIME.reconnectTimer);
    RP_REALTIME.reconnectTimer = null;
  }
  if (RP_REALTIME.ws) {
    try {
      RP_REALTIME.ws.__manualClose = true;
      RP_REALTIME.ws.close();
    } catch (e) {}
  }
  RP_REALTIME.ws = null;
  if (resetKey !== false) {
    RP_REALTIME.lastCodesKey = '';
  }
}

function rpGetCurrentPageRealtimeTokens() {
  var table = rpPickTable();
  var seen = {};
  var tokens = [];

  if (!table) return tokens;

  table.rows({ page: 'current' }).every(function() {
    var row = this.data();
    var token;
    if (!row) return;
    token = rpPickRealtimeToken(row);
    if (!token || seen[token]) return;
    seen[token] = true;
    tokens.push(token);
  });

  return tokens;
}

function rpApplyRealtimeQuote(msg) {
  var table = rpPickTable();
  var token;
  var code;
  var price;

  if (!table || !msg) return;

  token = String(msg.token || '').trim();
  code = String(msg.code || '').trim();
  price = rpToNumber(msg.price);
  if (price == null) return;

  table.rows({ page: 'current' }).every(function() {
    var row = this.data();
    var rowToken;
    var rowCode;

    if (!row) return;

    rowToken = rpPickRealtimeToken(row);
    rowCode = String(row.stkCd || '').trim();
    if (token) {
      if (rowToken !== token) return;
    } else if (code) {
      if (rowCode !== code) return;
    } else {
      return;
    }

    rpUpdatePickRowCells(table, this.index(), row, price);
  });
}

function rpSyncCurrentPriceByCode(code, force) {
  var cache;
  var url;

  if (!RP_URL.currentPrice || !code) return;

  cache = RP_REALTIME.priceCache[code];
  if (!force && cache && (Date.now() - cache.ts) < 3000) {
    rpApplyRealtimeQuote({ code: code, price: cache.price });
    return;
  }

  if (RP_REALTIME.priceInflight[code]) return;
  RP_REALTIME.priceInflight[code] = true;

  url = RP_URL.currentPrice + '?in_stockCode=' + encodeURIComponent(code);
  fetch(url, { method: 'GET' })
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(res) {
      var single = rpUnwrapSingle(res);
      var price = rpExtractCurrentPrice(single);
      if (price == null) return;
      RP_REALTIME.priceCache[code] = { ts: Date.now(), price: price };
      rpApplyRealtimeQuote({ code: code, price: price });
    })
    .catch(function() {})
    .then(function() {
      delete RP_REALTIME.priceInflight[code];
    }, function() {
      delete RP_REALTIME.priceInflight[code];
    });
}

function rpSyncVisibleDomesticPrices(force) {
  var table = rpPickTable();
  var seen = {};

  if (!table || !RP_URL.currentPrice) return;

  table.rows({ page: 'current' }).every(function() {
    var row = this.data();
    var code;

    if (!row || !rpIsDomesticRow(row)) return;
    code = String(row.stkCd || '').trim();
    if (!code || seen[code]) return;
    seen[code] = true;
    rpSyncCurrentPriceByCode(code, force);
  });
}

function rpEnsureDomesticSyncTimer() {
  if (RP_REALTIME.syncTimer || !RP_URL.currentPrice) return;
  RP_REALTIME.syncTimer = setInterval(function() {
    rpSyncVisibleDomesticPrices(false);
  }, 5000);
}

function rpConnectPickRealtime(tokens) {
  var codes = [];
  var seen = {};
  var key;
  var ws;

  if (!RP_URL.wsWatchlist) return;

  (tokens || []).forEach(function(token) {
    if (!token || seen[token]) return;
    seen[token] = true;
    codes.push(token);
  });

  if (!codes.length) {
    rpClosePickRealtime();
    return;
  }

  key = codes.join(',');
  if (RP_REALTIME.ws && (RP_REALTIME.ws.readyState === 0 || RP_REALTIME.ws.readyState === 1) && RP_REALTIME.lastCodesKey === key) {
    return;
  }

  rpClosePickRealtime();
  RP_REALTIME.lastCodesKey = key;

  ws = new WebSocket(rpBuildWsUrl(RP_URL.wsWatchlist, 'codes=' + encodeURIComponent(key)));
  RP_REALTIME.ws = ws;

  ws.onmessage = function(evt) {
    try {
      var msg = JSON.parse(evt.data);
      if (!msg || !msg.type) return;
      if (msg.type === 'WL') {
        rpApplyRealtimeQuote(msg);
        return;
      }
      if (msg.type === 'WL_SUB') {
        rpSyncCurrentPriceByCode(String(msg.code || '').trim(), true);
      }
    } catch (e) {}
  };

  ws.onclose = function() {
    if (ws.__manualClose) return;
    if (RP_REALTIME.ws === ws) {
      RP_REALTIME.ws = null;
    }
    RP_REALTIME.reconnectTimer = setTimeout(function() {
      rpRefreshPickRealtimeSubscription();
    }, 1500);
  };
}

function rpRefreshPickRealtimeSubscription() {
  rpConnectPickRealtime(rpGetCurrentPageRealtimeTokens());
}

function rpPositionLabel() {
  return RP_STATE.selectedPositionId ? 'POSITION ' + RP_STATE.selectedPositionId : '매수 연결 전';
}

function rpSetSelectionText(id, value, emptyText) {
  var el = document.getElementById(id);
  if (!el) return;
  var text = value || emptyText;
  el.textContent = text;
  if (value) {
    el.classList.remove('is-empty');
  } else {
    el.classList.add('is-empty');
  }
}

function rpCurrentPickListParam() {
  return {
    mktCd: document.getElementById('rp_listMktCd').value,
    pickStatus: document.getElementById('rp_listStatus').value,
    watchGroupId: document.getElementById('rp_listGroupId').value
  };
}

function rpCurrentTrackParam() {
  return {
    pickId: document.getElementById('rp_trackPickId').value
  };
}

function rpCurrentProbabilityParam() {
  return {
    holdDays: document.getElementById('rp_probHoldDays').value,
    sourceMktCd: document.getElementById('rp_probMktCd').value,
    recGrade: document.getElementById('rp_probGrade').value
  };
}

function rpPickTable() {
  if (!$.fn.dataTable.isDataTable('#rp_pickListTable')) return null;
  return $('#rp_pickListTable').DataTable();
}

function rpPickRowDataFromElement(el) {
  var table = rpPickTable();
  if (!table) return null;
  return table.row($(el).closest('tr')).data();
}

function rpBindPickListTableEvents() {
  var $tbody = $('#rp_pickListTable tbody');
  $tbody.off('click.rpSelectRow').on('click.rpSelectRow', 'tr', function(e) {
    if ($(e.target).closest('button').length) return;
    var rowData = rpPickRowDataFromElement(this);
    if (!rowData) return;
    $tbody.find('tr.is-selected').removeClass('is-selected');
    $(this).addClass('is-selected');
    rpSelectPick(rowData.pickId, rowData.positionId, rowData.stkCd, rowData.stkNm, rowData.pickStatus);
  });

  $tbody.off('click.rpSelectBtn').on('click.rpSelectBtn', '.btn-rp-select', function(e) {
    e.stopPropagation();
    var rowData = rpPickRowDataFromElement(this);
    if (!rowData) return;
    $tbody.find('tr.is-selected').removeClass('is-selected');
    $(this).closest('tr').addClass('is-selected');
    rpSelectPick(rowData.pickId, rowData.positionId, rowData.stkCd, rowData.stkNm, rowData.pickStatus);
  });

  $tbody.off('click.rpBuyBtn').on('click.rpBuyBtn', '.btn-rp-buy', function(e) {
    e.stopPropagation();
    var rowData = rpPickRowDataFromElement(this);
    if (!rowData) return;
    rpOpenBuyModal(rowData.pickId, rowData.recAnchorPrice || 0, rowData.stkCd || '', rowData.stkNm || '');
  });

  $tbody.off('click.rpDeleteBtn').on('click.rpDeleteBtn', '.btn-rp-delete', function(e) {
    e.stopPropagation();
    var rowData = rpPickRowDataFromElement(this);
    if (!rowData) return;
    rpDeletePick(rowData);
  });
}

function rpBuildPickListGrid() {
  var gridObj = {
    grid_id: 'rp_pickListTable',
    url: RP_URL.list,
    param: rpCurrentPickListParam(),
    columns: [
      { data: 'pickId' },
      { data: 'stkCd' },
      { data: 'stkNm', className: 'text-left' },
      { data: 'sourceMktCd', className: 'text-center' },
      { data: 'sourceBaseDt' },
      {
        data: 'recAnchorPrice',
        className: 'text-right',
        render: function(data) {
          return data ? Number(data).toLocaleString() : '-';
        }
      },
      {
        data: 'firstBuyPrice',
        className: 'text-right',
        render: function(data) {
          return data ? Number(data).toLocaleString() : '-';
        }
      },
      {
        data: 'currentPrice',
        className: 'text-right',
        render: function(data) {
          return rpFormatPrice(data);
        }
      },
      {
        data: null,
        className: 'text-right',
        render: function(data, type, row) {
          return rpFormatRecDiff(row);
        }
      },
      {
        data: null,
        className: 'text-right',
        render: function(data, type, row) {
          return rpFormatBuyDiff(row);
        }
      },
      {
        data: 'pickStatus',
        className: 'text-center',
        render: function(data) {
          return rpPickBadge(data);
        }
      },
      {
        data: 'lastTrackDt',
        render: function(data) {
          return data || '-';
        }
      },
      {
        data: null,
        orderable: false,
        render: function(data, type, row) {
          var buttons = '<button type="button" class="btn btn-primary btn-xs btn-rp-select">선택</button>';
          if (row.pickStatus === 'WATCH') {
            buttons += ' <button type="button" class="btn btn-success btn-xs btn-rp-buy">매수</button>';
            buttons += ' <button type="button" class="btn btn-danger btn-xs btn-rp-delete">삭제</button>';
          }
          return buttons;
        }
      }
    ],
    columnDefs: { targets: '_all' },
    columnCheck: false
  };

  var gridOptions = {
    serverSide: false,
    searching: false,
    paging: true,
    pageLength: 10,
    lengthChange: false,
    info: true,
    autoWidth: false,
    responsive: false,
    bDestroy: true,
    processing: true,
    ordering: false,
    rowCallback: function(row, data) {
      $(row).addClass('rp-pick-row');
      var isSelected = RP_STATE.selectedPickId && String(RP_STATE.selectedPickId) === String(data.pickId);
      $(row).toggleClass('is-selected', !!isSelected);
    }
  };

  dataTableGridNew(gridObj, gridOptions);
  rpBindPickListTableEvents();
  $('#rp_pickListTable')
    .off('.rpRealtime')
    .on('draw.dt.rpRealtime', function() {
      rpRefreshPickRealtimeSubscription();
      rpSyncVisibleDomesticPrices(true);
    });

  setTimeout(function() {
    rpRefreshPickRealtimeSubscription();
    rpSyncVisibleDomesticPrices(true);
    rpEnsureDomesticSyncTimer();
  }, 0);
}

function rpBuildTrackGrid() {
  var gridObj = {
    grid_id: 'rp_trackTable',
    url: RP_URL.dailyTrack,
    param: rpCurrentTrackParam(),
    columns: [
      { data: 'tradeDt' },
      {
        data: 'closePrice',
        className: 'text-right',
        render: function(data) {
          return data ? Number(data).toLocaleString() : '-';
        }
      },
      { data: 'returnFromRecPct', render: function(data) { return rpPct(data); } },
      { data: 'returnFromWatchPct', render: function(data) { return rpPct(data); } },
      { data: 'returnFromBuyPct', render: function(data) { return rpPct(data); } },
      { data: 'mfeFromRecPct', render: function(data) { return rpPct(data); } },
      { data: 'maeFromRecPct', render: function(data) { return rpPct(data); } },
      {
        data: 'tp1HitYn',
        className: 'text-center',
        render: function(data) {
          return data === 'Y' ? '<span class="badge badge-success">HIT</span>' : '-';
        }
      },
      {
        data: 'stopHitYn',
        className: 'text-center',
        render: function(data) {
          return data === 'Y' ? '<span class="badge badge-danger">HIT</span>' : '-';
        }
      },
      {
        data: 'sellSignalCode',
        className: 'text-center',
        render: function(data) {
          return (data && data !== 'NONE') ? '<span class="badge badge-danger">' + data + '</span>' : '-';
        }
      }
    ],
    columnDefs: { targets: '_all' },
    columnCheck: false
  };

  var gridOptions = {
    serverSide: false,
    searching: false,
    paging: true,
    pageLength: 10,
    lengthChange: false,
    info: true,
    autoWidth: false,
    responsive: false,
    bDestroy: true,
    processing: true,
    ordering: false
  };

  dataTableGridNew(gridObj, gridOptions);
}

function rpBuildProbabilityGrid() {
  var gridObj = {
    grid_id: 'rp_probTable',
    url: RP_URL.probability,
    param: rpCurrentProbabilityParam(),
    columns: [
      { data: 'evalType' },
      {
        data: 'holdDays',
        className: 'text-center',
        render: function(data) {
          return data ? data + '일' : '';
        }
      },
      { data: 'sourceMktCd', className: 'text-center' },
      {
        data: 'recGrade',
        className: 'text-center',
        render: function(data) {
          return data || '-';
        }
      },
      {
        data: 'entryRuleCode',
        render: function(data) {
          return data || '-';
        }
      },
      { data: 'sampleCnt', className: 'text-right' },
      {
        data: 'winRate',
        className: 'text-right',
        render: function(data) {
          if (data == null) return '-';
          var cls = data >= 60 ? 'text-success font-weight-bold' : '';
          return '<span class="' + cls + '">' + Number(data).toFixed(1) + '%</span>';
        }
      },
      {
        data: 'tp1Rate',
        className: 'text-right',
        render: function(data) {
          return data != null ? Number(data).toFixed(1) + '%' : '-';
        }
      },
      {
        data: 'stopRate',
        className: 'text-right text-danger',
        render: function(data) {
          return data != null ? Number(data).toFixed(1) + '%' : '-';
        }
      },
      { data: 'avgReturnPct', render: function(data) { return rpPct(data); } },
      {
        data: 'avgMfePct',
        className: 'text-danger',
        render: function(data) {
          return data != null ? '+' + Number(data).toFixed(2) + '%' : '-';
        }
      },
      {
        data: 'avgMaePct',
        className: 'text-primary',
        render: function(data) {
          return data != null ? Number(data).toFixed(2) + '%' : '-';
        }
      }
    ],
    columnDefs: { targets: '_all' },
    columnCheck: false
  };

  var gridOptions = {
    serverSide: false,
    searching: false,
    paging: true,
    pageLength: 10,
    lengthChange: false,
    info: true,
    autoWidth: false,
    responsive: false,
    bDestroy: true,
    processing: true,
    ordering: false
  };

  dataTableGridNew(gridObj, gridOptions);
}

function rpRenderSelectionSummary() {
  var pickText = RP_STATE.selectedPickId ? 'PICK ' + RP_STATE.selectedPickId : '미선택';
  var positionText = RP_STATE.selectedPositionId ? 'POSITION ' + RP_STATE.selectedPositionId : '미선택';
  rpSetFocusChip('rp_focusSummary', '선택 상태', rpSelectionLabel() + ' / ' + positionText);
  rpSetSelectionText('rp_trackSelectionText', RP_STATE.selectedPickId ? rpSelectionLabel() : '', '종목을 선택하세요');
  rpSetSelectionText('rp_sellSelectionText', RP_STATE.selectedPickId ? rpSelectionLabel() : '', '종목을 선택하세요');
}

function rpRememberSelection(pickId, positionId, stockCode, stockName, pickStatus) {
  RP_STATE.selectedPickId = pickId ? String(pickId) : '';
  RP_STATE.selectedPositionId = positionId ? String(positionId) : '';
  RP_STATE.selectedStockCode = stockCode || '';
  RP_STATE.selectedStockName = stockName || '';
  RP_STATE.selectedPickStatus = pickStatus || '';
  document.getElementById('rp_trackPickId').value = RP_STATE.selectedPickId;
  document.getElementById('rp_sellPositionId').value = RP_STATE.selectedPositionId;
  rpRenderSelectionSummary();
}

function rpResetSellGuide(message) {
  var card = document.getElementById('rp_sellGuideCard');
  var empty = document.getElementById('rp_sellGuideEmpty');
  var badge = document.getElementById('rp_sellGuideStateBadge');
  card.style.display = 'none';
  empty.style.display = '';
  empty.textContent = message || '상단 추천 저장 이력에서 종목을 선택하면 매도 가이드가 자동으로 표시됩니다.';
  badge.className = 'badge badge-secondary';
  badge.textContent = '-';
  document.getElementById('rp_sg_returnPct').innerHTML = '-';
  document.getElementById('rp_sg_targetPrice').textContent = '-';
  document.getElementById('rp_sg_tp1Price').textContent = '-';
  document.getElementById('rp_sg_stopPrice').textContent = '-';
  document.getElementById('rp_sg_asOfDt').textContent = '-';
  document.getElementById('rp_sg_signalText').textContent = '-';
}

function rpScrollToAnalysis() {
  var section = document.getElementById('rp-analysisSection');
  if (section && typeof section.scrollIntoView === 'function') {
    section.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

function rpSelectPick(pickId, positionId, stockCode, stockName, pickStatus) {
  rpRememberSelection(pickId, positionId, stockCode, stockName, pickStatus);
  if (RP_STATE.selectedPickId) {
    rpLoadDailyTrack();
  }
  if (RP_STATE.selectedPositionId) {
    rpLoadSellGuide();
  } else {
    rpResetSellGuide('선택한 종목은 아직 매수 연결 전이라 매도 가이드가 없습니다.');
  }
  rpLoadPickList();
  rpScrollToAnalysis();
}

function rpLoadPickList() {
  rpBuildPickListGrid();
}

function rpDeletePick(row) {
  var pickLabel;
  if (!row || !row.pickId) return;
  if (row.positionId || row.pickStatus !== 'WATCH') {
    alert('매수 등록된 추천 이력은 삭제할 수 없습니다.');
    return;
  }

  pickLabel = (row.stkNm || row.stkCd || '') + ' (PICK ' + row.pickId + ')';
  if (!confirm(pickLabel + ' 추천 저장 이력을 삭제할까요?')) return;

  rpAjax(RP_URL.deletePick, { pickId: row.pickId }, function(res) {
    if (res.system_code === '0000') {
      if (String(RP_STATE.selectedPickId) === String(row.pickId)) {
        rpRememberSelection('', '', '', '', '');
        rpResetSellGuide();
      }
      rpLoadPickList();
      return;
    }
    alert('오류: ' + (res.system_msg || '알 수 없는 오류'));
  });
}

function rpLoadDailyTrack() {
  var pickId = document.getElementById('rp_trackPickId').value;
  if (!pickId) { alert('상단 추천 저장 이력에서 종목을 먼저 선택하세요'); return; }
  rpRenderSelectionSummary();
  rpBuildTrackGrid();
}

function rpLoadProbability() {
  rpBuildProbabilityGrid();
}

function rpLoadSellGuide() {
  var posId = document.getElementById('rp_sellPositionId').value;
  if (!posId) {
    rpResetSellGuide('선택한 종목은 아직 매수 연결 전이라 매도 가이드가 없습니다.');
    return;
  }
  rpRenderSelectionSummary();
  rpAjax(RP_URL.sellGuide, { positionId: posId }, function(res) {
    var d = res.singleData;
    var card  = document.getElementById('rp_sellGuideCard');
    var empty = document.getElementById('rp_sellGuideEmpty');
    if (!d) {
      rpResetSellGuide('데이터 없음');
      return;
    }
    card.style.display  = '';
    empty.style.display = 'none';
    var badge = document.getElementById('rp_sellGuideStateBadge');
    badge.className = 'badge ' + rpSellBadge(d.sellGuideState);
    badge.textContent = d.sellGuideState || '-';
    document.getElementById('rp_sg_returnPct').innerHTML   = rpPct(d.currentReturnPct);
    document.getElementById('rp_sg_targetPrice').textContent = d.targetPrice ? Number(d.targetPrice).toLocaleString() : '-';
    document.getElementById('rp_sg_tp1Price').textContent    = d.tp1Price    ? Number(d.tp1Price).toLocaleString()    : '-';
    document.getElementById('rp_sg_stopPrice').textContent   = d.stopPrice   ? Number(d.stopPrice).toLocaleString()   : '-';
    document.getElementById('rp_sg_asOfDt').textContent      = d.asOfTradeDt || '-';
    document.getElementById('rp_sg_signalText').textContent  = d.sellSignalText || '-';
  });
}

function rpOpenBuyModal(pickId, anchorPrice, stockCode, stockName) {
  rpRememberSelection(pickId, '', stockCode || RP_STATE.selectedStockCode, stockName || RP_STATE.selectedStockName, 'WATCH');
  document.getElementById('rp_buyPickId').value = pickId;
  document.getElementById('rp_buyPrice').value  = anchorPrice || '';
  document.getElementById('rp_buyQty').value    = '1';
  document.getElementById('rp_buyDate').value   = rpTodayString();
  $('#rp_buyModal').modal('show');
}

function rpSubmitBuy() {
  var price = document.getElementById('rp_buyPrice').value;
  var qty   = document.getElementById('rp_buyQty').value;
  if (!price || !qty) { alert('매수가와 수량을 입력하세요'); return; }
  rpAjax(RP_URL.registerBuy, {
    pickId:        document.getElementById('rp_buyPickId').value,
    buyPrice:      price,
    qty:           qty,
    buyDate:       document.getElementById('rp_buyDate').value,
    entryRuleCode: document.getElementById('rp_buyRuleCode').value
  }, function(res) {
    if (res.system_code === '0000') {
      var d = res.singleData || {};
      alert('매수 등록 완료\n목표가: ' + (d.targetPrice ? Number(d.targetPrice).toLocaleString() : '-') +
            '\nTP1: '  + (d.tp1Price  ? Number(d.tp1Price).toLocaleString()  : '-') +
            '\n손절: ' + (d.stopPrice ? Number(d.stopPrice).toLocaleString() : '-'));
      $('#rp_buyModal').modal('hide');
      rpSelectPick(d.pickId, d.positionId, RP_STATE.selectedStockCode, RP_STATE.selectedStockName, 'BOUGHT');
    } else {
      alert('오류: ' + (res.system_msg || '알 수 없는 오류'));
    }
  });
}

rpRenderSelectionSummary();
rpResetSellGuide();
setTimeout(function() {
  rpLoadPickList();
  rpLoadProbability();
}, 0);
