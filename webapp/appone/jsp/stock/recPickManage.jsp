<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<style>
  #div_rec_pick_manage .rp-section + .rp-section {
    margin-top: 1rem;
  }

  #div_rec_pick_manage .rp-section .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
  }

  #div_rec_pick_manage .rp-section .card-title-wrap {
    min-width: 0;
  }

  #div_rec_pick_manage .rp-section .card-title {
    margin-bottom: 0.15rem;
  }

  #div_rec_pick_manage .rp-section .card-subtitle {
    color: #6c757d;
    font-size: 0.8125rem;
    margin: 0;
  }

  #div_rec_pick_manage .rp-focus-chip {
    background: #f4f6f9;
    border: 1px solid #d8dee6;
    border-radius: 999px;
    color: #495057;
    display: inline-flex;
    font-size: 0.75rem;
    font-weight: 600;
    gap: 0.375rem;
    max-width: 100%;
    padding: 0.35rem 0.75rem;
    white-space: nowrap;
  }

  #div_rec_pick_manage .rp-focus-chip strong {
    color: #212529;
  }

  #div_rec_pick_manage .rp-side-form .search_row {
    margin-bottom: 0.75rem;
  }

  #div_rec_pick_manage .rp-guide-placeholder {
    background: #f8f9fa;
    border: 1px dashed #ced4da;
    border-radius: 0.5rem;
    color: #6c757d;
    margin: 0;
    padding: 1rem;
  }

  #div_rec_pick_manage .rp-prob-table th,
  #div_rec_pick_manage .rp-prob-table td,
  #div_rec_pick_manage .rp-track-table th,
  #div_rec_pick_manage .rp-track-table td {
    white-space: nowrap;
  }

  #div_rec_pick_manage .rp-pick-row {
    cursor: pointer;
    transition: background-color 0.15s ease;
  }

  #div_rec_pick_manage .rp-pick-row:hover td {
    background: #f7fbff;
  }

  #div_rec_pick_manage .rp-pick-row.is-selected td {
    background: #eaf3ff;
  }

  #div_rec_pick_manage .rp-selection-text {
    color: #212529;
    display: inline-block;
    font-size: 0.95rem;
    font-weight: 600;
    padding-top: 0.4rem;
  }

  #div_rec_pick_manage .rp-selection-text.is-empty {
    color: #6c757d;
    font-weight: 500;
  }

  #div_rec_pick_manage .rp-selection-help {
    color: #6c757d;
    font-size: 0.8125rem;
    margin: 0;
  }

  @media (max-width: 991.98px) {
    #div_rec_pick_manage .rp-section .card-header {
      align-items: flex-start;
      flex-direction: column;
    }

    #div_rec_pick_manage .rp-focus-chip {
      white-space: normal;
    }
  }
</style>

<div class="container-fluid" id="div_rec_pick_manage">
  <div class="card card-primary card-outline rp-section">
    <div class="card-header">
      <div class="card-title-wrap">
        <h3 class="card-title">추천 저장 이력</h3>
      </div>
      <span id="rp_focusSummary" class="rp-focus-chip">선택 상태 <strong>미선택</strong></span>
    </div>
    <div class="card-body">
      <div class="form-group search_row">
        <label class="col-sm-1 col-form-label text-right">시장</label>
        <div class="col-sm-1">
          <select id="rp_listMktCd" class="form-control form-control-sm">
            <option value="">전체</option><option value="KR">KR</option><option value="US">US</option>
          </select>
        </div>
        <label class="col-sm-1 col-form-label text-right">상태</label>
        <div class="col-sm-2">
          <select id="rp_listStatus" class="form-control form-control-sm">
            <option value="">전체</option><option value="WATCH">WATCH</option>
            <option value="BOUGHT">BOUGHT</option><option value="CLOSED">CLOSED</option>
          </select>
        </div>
        <label class="col-sm-1 col-form-label text-right">그룹</label>
        <div class="col-sm-2">
          <input type="text" id="rp_listGroupId" class="form-control form-control-sm" placeholder="관심 그룹 ID">
        </div>
        <div class="col-sm-2">
          <button type="button" class="btn btn-info btn-sm" onclick="rpLoadPickList()">
            <i class="fa fa-search"></i> 조회
          </button>
        </div>
      </div>
      <div class="table-responsive">
        <table id="rp_pickListTable" class="table table-hover table-sm">
          <thead>
            <tr>
              <th>PICK_ID</th><th>종목코드</th><th>종목명</th><th>시장</th><th>추천일</th>
              <th>등록가</th><th>매수가</th><th>상태</th><th>최종추적</th><th style="width:150px;">관리</th>
            </tr>
          </thead>
          <tbody id="rp_pickListBody">
            <tr><td colspan="10" class="text-center text-muted py-4">조회 버튼을 클릭하세요</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-8">
      <div class="card card-secondary card-outline rp-section" id="rp-analysisSection">
        <div class="card-header">
          <div class="card-title-wrap">
            <h3 class="card-title">일별 추적</h3>
            <p class="card-subtitle">추천 이력의 거래일별 성과와 TP1, 손절, 매도 신호</p>
          </div>
          <span id="rp_trackFocus" class="rp-focus-chip">PICK <strong>미선택</strong></span>
        </div>
        <div class="card-body">
          <input type="hidden" id="rp_trackPickId">
          <div class="form-group search_row">
            <label class="col-sm-2 col-form-label text-right">선택 종목</label>
            <div class="col-sm-7">
              <span id="rp_trackSelectionText" class="rp-selection-text is-empty">상단 추천 저장 이력에서 종목을 선택하세요</span>
            </div>
            <div class="col-sm-3">
              <button type="button" class="btn btn-info btn-sm" onclick="rpLoadDailyTrack()">
                <i class="fa fa-refresh"></i> 새로고침
              </button>
            </div>
          </div>
          <div class="table-responsive">
            <table id="rp_trackTable" class="table table-hover table-sm rp-track-table">
              <thead>
                <tr>
                  <th>거래일</th><th>종가</th><th>추천대비(%)</th><th>관심대비(%)</th><th>매수대비(%)</th>
                  <th>MFE_REC(%)</th><th>MAE_REC(%)</th><th>TP1</th><th>STOP</th><th>매도신호</th>
                </tr>
              </thead>
              <tbody id="rp_trackBody">
                <tr><td colspan="10" class="text-center text-muted py-4">PICK_ID 를 입력하거나 위 목록에서 추적을 선택하세요</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="col-lg-4">
      <div class="card card-secondary card-outline rp-section">
        <div class="card-header">
          <div class="card-title-wrap">
            <h3 class="card-title">매도 가이드</h3>
            <p class="card-subtitle">보유 포지션의 현재 수익률과 목표/손절 상태</p>
          </div>
          <span id="rp_sellFocus" class="rp-focus-chip">POSITION <strong>미선택</strong></span>
        </div>
        <div class="card-body rp-side-form">
          <input type="hidden" id="rp_sellPositionId">
          <div class="form-group search_row">
            <label class="col-sm-4 col-form-label text-right">선택 종목</label>
            <div class="col-sm-8">
              <span id="rp_sellSelectionText" class="rp-selection-text is-empty">상단 추천 저장 이력에서 종목을 선택하세요</span>
            </div>
          </div>
          <div class="form-group search_row">
            <label class="col-sm-4 col-form-label text-right">연결 포지션</label>
            <div class="col-sm-8">
              <span id="rp_sellPositionText" class="rp-selection-text is-empty">매수 연결 전</span>
            </div>
          </div>
          <div class="form-group search_row">
            <div class="col-sm-12 text-right">
              <button type="button" class="btn btn-info btn-sm" onclick="rpLoadSellGuide()">
                <i class="fa fa-refresh"></i> 새로고침
              </button>
            </div>
          </div>

          <div id="rp_sellGuideCard" class="card card-secondary mb-0" style="display:none;">
            <div class="card-header">
              <h3 class="card-title" id="rp_sellGuideTitle">매도 가이드</h3>
              <div class="card-tools">
                <span id="rp_sellGuideStateBadge" class="badge badge-secondary">-</span>
              </div>
            </div>
            <div class="card-body p-2">
              <div class="row">
                <div class="col-sm-6">
                  <div class="info-box bg-light mb-2">
                    <div class="info-box-content">
                      <span class="info-box-text text-muted">현재 수익률</span>
                      <span class="info-box-number" id="rp_sg_returnPct">-</span>
                    </div>
                  </div>
                </div>
                <div class="col-sm-6">
                  <div class="info-box bg-light mb-2">
                    <div class="info-box-content">
                      <span class="info-box-text text-muted">목표가</span>
                      <span class="info-box-number" id="rp_sg_targetPrice">-</span>
                    </div>
                  </div>
                </div>
                <div class="col-sm-6">
                  <div class="info-box bg-light mb-2">
                    <div class="info-box-content">
                      <span class="info-box-text text-muted">TP1</span>
                      <span class="info-box-number text-success" id="rp_sg_tp1Price">-</span>
                    </div>
                  </div>
                </div>
                <div class="col-sm-6">
                  <div class="info-box bg-light mb-2">
                    <div class="info-box-content">
                      <span class="info-box-text text-muted">손절가</span>
                      <span class="info-box-number text-danger" id="rp_sg_stopPrice">-</span>
                    </div>
                  </div>
                </div>
                <div class="col-sm-6">
                  <div class="info-box bg-light mb-2">
                    <div class="info-box-content">
                      <span class="info-box-text text-muted">기준일</span>
                      <span class="info-box-number" id="rp_sg_asOfDt">-</span>
                    </div>
                  </div>
                </div>
                <div class="col-sm-6">
                  <div class="info-box bg-light mb-2">
                    <div class="info-box-content">
                      <span class="info-box-text text-muted">신호</span>
                      <span class="info-box-number small" id="rp_sg_signalText">-</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <p id="rp_sellGuideEmpty" class="rp-guide-placeholder">상단 추천 저장 이력에서 종목을 선택하면 매도 가이드가 자동으로 표시됩니다.</p>
        </div>
      </div>
    </div>
  </div>

  <div class="card card-secondary card-outline rp-section">
    <div class="card-header">
      <div class="card-title-wrap">
        <h3 class="card-title">성공 확률</h3>
        <p class="card-subtitle">누적 평가 결과를 기준으로 시장, 등급, 보유기간별 승률과 MFE/MAE 비교</p>
      </div>
      <span class="rp-focus-chip">집계 기준 <strong>평가 완료 데이터</strong></span>
    </div>
    <div class="card-body">
      <div class="form-group search_row">
        <label class="col-sm-1 col-form-label text-right">기간(일)</label>
        <div class="col-sm-1">
          <select id="rp_probHoldDays" class="form-control form-control-sm">
            <option value="">전체</option><option value="5">5일</option>
            <option value="20">20일</option><option value="60">60일</option>
          </select>
        </div>
        <label class="col-sm-1 col-form-label text-right">시장</label>
        <div class="col-sm-1">
          <select id="rp_probMktCd" class="form-control form-control-sm">
            <option value="">전체</option><option value="KR">KR</option><option value="US">US</option>
          </select>
        </div>
        <label class="col-sm-1 col-form-label text-right">등급</label>
        <div class="col-sm-1">
          <select id="rp_probGrade" class="form-control form-control-sm">
            <option value="">전체</option><option value="A">A</option>
            <option value="B">B</option><option value="C">C</option>
          </select>
        </div>
        <div class="col-sm-2">
          <button type="button" class="btn btn-info btn-sm" onclick="rpLoadProbability()">
            <i class="fa fa-search"></i> 조회
          </button>
        </div>
      </div>
      <div class="table-responsive">
        <table id="rp_probTable" class="table table-hover table-sm rp-prob-table">
          <thead>
            <tr>
              <th>평가기준</th><th>보유기간</th><th>시장</th><th>등급</th><th>진입룰</th>
              <th>샘플수</th><th>승률(%)</th><th>TP1달성(%)</th><th>손절률(%)</th>
              <th>평균수익(%)</th><th>평균MFE(%)</th><th>평균MAE(%)</th>
            </tr>
          </thead>
          <tbody id="rp_probBody">
            <tr><td colspan="12" class="text-center text-muted py-4">조회 버튼을 클릭하세요</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

</div><!-- /container-fluid -->

<!-- ── 매수 등록 Bootstrap 모달 ── -->
<div class="modal fade" id="rp_buyModal" tabindex="-1" data-keyboard="true" data-backdrop="static">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">매수 등록</h4>
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="rp_buyPickId">
        <div class="form-group row">
          <label class="col-sm-3 col-form-label text-right">매수가</label>
          <div class="col-sm-9">
            <input type="number" id="rp_buyPrice" class="form-control form-control-sm" placeholder="매수 단가" step="1" min="1">
          </div>
        </div>
        <div class="form-group row">
          <label class="col-sm-3 col-form-label text-right">수량</label>
          <div class="col-sm-9">
            <input type="number" id="rp_buyQty" class="form-control form-control-sm" placeholder="매수 수량" step="1" min="1">
          </div>
        </div>
        <div class="form-group row">
          <label class="col-sm-3 col-form-label text-right">매수일</label>
          <div class="col-sm-9">
            <input type="date" id="rp_buyDate" class="form-control form-control-sm">
            <small class="text-muted">생략 시 오늘 날짜 적용</small>
          </div>
        </div>
        <div class="form-group row">
          <label class="col-sm-3 col-form-label text-right">진입 규칙</label>
          <div class="col-sm-9">
            <select id="rp_buyRuleCode" class="form-control form-control-sm">
              <option value="MANUAL">MANUAL — 기본 ±10% / ±7%</option>
              <option value="BREAKOUT_20">BREAKOUT_20 — ±12% / ±5%</option>
              <option value="PULLBACK_MA20">PULLBACK_MA20 — ±8% / ±5%</option>
            </select>
          </div>
        </div>
      </div>
      <div class="modal-footer justify-content-between">
        <button type="button" class="btn btn-default btn-sm" data-dismiss="modal">취소</button>
        <button type="button" class="btn btn-success btn-sm" onclick="rpSubmitBuy()">
          <i class="fa fa-check"></i> 등록
        </button>
      </div>
    </div>
  </div>
</div>

<script>
var RP_URL = {
  list:        "<c:url value='/stock/recPick/list.do'/>",
  dailyTrack:  "<c:url value='/stock/recPick/dailyTrack.do'/>",
  probability: "<c:url value='/stock/recPick/probability.do'/>",
  sellGuide:   "<c:url value='/stock/recPick/sellGuide.do'/>",
  registerBuy: "<c:url value='/stock/recPick/registerBuy.do'/>"
};

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
  el.className = 'rp-selection-text' + (value ? '' : ' is-empty');
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
    rpSelectPick(rowData.pickId, rowData.positionId, rowData.stkCd, rowData.stkNm, rowData.pickStatus);
  });

  $tbody.off('click.rpSelectBtn').on('click.rpSelectBtn', '.btn-rp-select', function(e) {
    e.stopPropagation();
    var rowData = rpPickRowDataFromElement(this);
    if (!rowData) return;
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
  rpSetFocusChip('rp_trackFocus', 'PICK', pickText);
  rpSetFocusChip('rp_sellFocus', 'POSITION', positionText);
  rpSetSelectionText('rp_trackSelectionText', RP_STATE.selectedPickId ? rpSelectionLabel() : '', '상단 추천 저장 이력에서 종목을 선택하세요');
  rpSetSelectionText('rp_sellSelectionText', RP_STATE.selectedPickId ? rpSelectionLabel() : '', '상단 추천 저장 이력에서 종목을 선택하세요');
  rpSetSelectionText('rp_sellPositionText', RP_STATE.selectedPositionId ? rpPositionLabel() : '', '매수 연결 전');
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
  document.getElementById('rp_sellGuideTitle').textContent = '매도 가이드';
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

/* 추천 저장 이력 */
function rpLoadPickList() {
  rpBuildPickListGrid();
}

/* 일별 추적 */
function rpLoadDailyTrack() {
  var pickId = document.getElementById('rp_trackPickId').value;
  if (!pickId) { alert('상단 추천 저장 이력에서 종목을 먼저 선택하세요'); return; }
  rpSetFocusChip('rp_trackFocus', 'PICK', pickId);
  rpRenderSelectionSummary();
  rpBuildTrackGrid();
}

/* 성공 확률 */
function rpLoadProbability() {
  rpBuildProbabilityGrid();
}

/* 매도 가이드 */
function rpLoadSellGuide() {
  var posId = document.getElementById('rp_sellPositionId').value;
  if (!posId) {
    rpResetSellGuide('선택한 종목은 아직 매수 연결 전이라 매도 가이드가 없습니다.');
    return;
  }
  rpSetFocusChip('rp_sellFocus', 'POSITION', posId);
  rpRenderSelectionSummary();
  rpAjax(RP_URL.sellGuide, { positionId: posId }, function(res) {
    var d = res.data;
    var card  = document.getElementById('rp_sellGuideCard');
    var empty = document.getElementById('rp_sellGuideEmpty');
    if (!d) {
      rpResetSellGuide('데이터 없음');
      return;
    }
    card.style.display  = '';
    empty.style.display = 'none';
    document.getElementById('rp_sellGuideTitle').textContent  = d.stockCode || ('POSITION ' + posId);
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

/* 매수 모달 */
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
    if (res.code === '0000') {
      var d = res.data || {};
      alert('매수 등록 완료\n목표가: ' + (d.targetPrice ? Number(d.targetPrice).toLocaleString() : '-') +
            '\nTP1: '  + (d.tp1Price  ? Number(d.tp1Price).toLocaleString()  : '-') +
            '\n손절: ' + (d.stopPrice ? Number(d.stopPrice).toLocaleString() : '-'));
      $('#rp_buyModal').modal('hide');
      rpSelectPick(d.pickId, d.positionId, RP_STATE.selectedStockCode, RP_STATE.selectedStockName, 'BOUGHT');
    } else {
      alert('오류: ' + (res.message || res.msg || '알 수 없는 오류'));
    }
  });
}

rpRenderSelectionSummary();
rpResetSellGuide();
setTimeout(function() {
  rpLoadPickList();
  rpLoadProbability();
}, 0);
</script>
