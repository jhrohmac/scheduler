var recPickManageConfig = window.recPickManageConfig || {};
var RP_URL = recPickManageConfig.urls || {};

var RP_STATE = {
  selectedPickId: '',
  selectedPositionId: '',
  selectedStockCode: '',
  selectedStockName: '',
  selectedPickStatus: ''
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
          return data ? Number(data).toLocaleString() : '-';
        }
      },
      {
        data: null,
        className: 'text-right',
        render: function(data, type, row) {
          if (!row.currentPrice || !row.recAnchorPrice) return '-';
          var diff = row.currentPrice - row.recAnchorPrice;
          var pct = (diff / row.recAnchorPrice * 100).toFixed(2);
          var cls = diff >= 0 ? 'text-danger' : 'text-primary';
          var sign = diff >= 0 ? '+' : '';
          return '<span class="' + cls + ' font-weight-bold">' + sign + pct + '%</span>'
               + '<br><small class="' + cls + '">' + sign + Math.round(diff).toLocaleString() + '</small>';
        }
      },
      {
        data: null,
        className: 'text-right',
        render: function(data, type, row) {
          if (!row.currentPrice || !row.firstBuyPrice) return '-';
          var diff = row.currentPrice - row.firstBuyPrice;
          var pct = (diff / row.firstBuyPrice * 100).toFixed(2);
          var cls = diff >= 0 ? 'text-danger' : 'text-primary';
          var sign = diff >= 0 ? '+' : '';
          return '<span class="' + cls + ' font-weight-bold">' + sign + pct + '%</span>'
               + '<br><small class="' + cls + '">' + sign + Math.round(diff).toLocaleString() + '</small>';
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
  document.getElementById('rp_buyQty').value    = '';
  document.getElementById('rp_buyDate').value   = '';
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
