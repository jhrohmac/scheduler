<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<link rel="stylesheet" href="<c:url value='/appone/jsp/stock/recPickManage.css?v=20260403-1' />">

<div class="container-fluid" id="div_rec_pick_manage">
	<div class="card card-primary card-outline collapsed-card">
		<div class="card-header pb-2 pt-2">
			<h3 class="card-title">추천 저장 이력</h3>
			<div class="card-tools">
				<span id="rp_focusSummary" class="rp-focus-chip mr-1">선택 상태 <strong>미선택</strong></span>
				<button type="button" class="btn btn-tool"
					data-card-widget="collapse" title="Collapse">
					<i class="fas fa-plus"></i>
				</button>
			</div>
		</div>
		<div class="card-body p-2">
			<div class="form-group search_row">				
				<label for="rp_listMktCd" class="col-sm-1 col-form-label text-right">시장</label>
				<div class="col-sm-1">
					<select id="rp_listMktCd" class="form-control custom-select custom-select-sm form-control-sm">
						<option value="">전체</option>
						<option value="KR">KR</option>
						<option value="US">US</option>
					</select>
				</div>
				<label for="sel_useYn" class="col-sm-1 col-form-label text-right">상태</label>
				<div class="col-sm-1">
					<select id="rp_listStatus" class="form-control form-control-sm">
						<option value="">전체</option>
						<option value="WATCH">WATCH</option>
						<option value="BOUGHT">BOUGHT</option>
						<option value="CLOSED">CLOSED</option>
					</select>
				</div>
				<label for="rp_listGroupId" class="col-sm-1 col-form-label text-right">그룹</label>
				<div class="col-sm-3">
					<input type="text" id="rp_listGroupId" name="rp_listGroupId" class="form-control form-control-sm" placeholder="관심 그룹 ID">
				</div>
				<div class="col-sm-4">
					<button type="button" class="btn btn-info btn-sm float-right" onclick="rpLoadPickList()">
						<i class="fa fa-search"></i> 조회
					</button>					
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-12">
			<div class="card card-info">
				<div class="card-body table-responsive p-2">
					<div class="table-responsive">
						<table id="rp_pickListTable" class="table table-hover table-sm">
							<thead>
								<tr>
									<th>PICK_ID</th>
									<th>종목코드</th>
									<th>종목명</th>
									<th>시장</th>
									<th>추천일</th>
									<th>등록가</th>
									<th>매수가</th>
									<th>현재가</th>
									<th>등록가대비</th>
									<th>매수가대비</th>
									<th>상태</th>
									<th>최종추적</th>
									<th style="width: 150px;">관리</th>
								</tr>
							</thead>
							<tbody id="rp_pickListBody">
								<tr>
									<td colspan="13" class="text-center text-muted py-4">조회 버튼을 클릭하세요</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-lg-8">
			<div class="card card-primary card-outline" id="rp-analysisSection">
				<div class="card-header">
					<h3 class="card-title">
						일별 추적
						<small class="text-muted">추천 저장 이력의 거래일별 성과와 TP1, 손절, 매도 신호</small>
					</h3>
					<div class="card-tools">
						<span id="rp_trackSelectionText" class="rp-focus-chip mr-1 is-empty">종목을 선택하세요</span>
						<button type="button" class="btn btn-tool"
							onclick="rpLoadDailyTrack()" title="새로고침">
							<i class="fa fa-refresh"></i>
						</button>
					</div>
				</div>
				<div class="card-body">
					<input type="hidden" id="rp_trackPickId">
					<div class="table-responsive">
						<table id="rp_trackTable"
							class="table table-hover table-sm rp-track-table">
							<thead>
								<tr>
									<th>거래일</th>
									<th>종가</th>
									<th>추천대비(%)</th>
									<th>관심대비(%)</th>
									<th>매수대비(%)</th>
									<th>MFE_REC(%)</th>
									<th>MAE_REC(%)</th>
									<th>TP1</th>
									<th>STOP</th>
									<th>매도신호</th>
								</tr>
							</thead>
							<tbody id="rp_trackBody">
								<tr>
									<td colspan="10" class="text-center text-muted py-4">PICK_ID
										를 입력하거나 위 목록에서 추적을 선택하세요</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>

		<div class="col-lg-4">
			<div class="card card-primary card-outline">
				<div class="card-header">
					<h3 class="card-title">
						매도 가이드
						<span id="rp_sellGuideStateBadge" class="badge badge-secondary ml-1">-</span>
					</h3>
					<div class="card-tools">
						<span id="rp_sg_asOfDt" class="rp-focus-chip mr-1"></span>
						<span id="rp_sellSelectionText" class="rp-focus-chip mr-1"></span>
						<button type="button" class="btn btn-tool"
							onclick="rpLoadSellGuide()" title="새로고침">
							<i class="fa fa-refresh"></i>
						</button>
					</div>
				</div>
				<div class="card-body p-2">
					<input type="hidden" id="rp_sellPositionId">					
					<div id="rp_sellGuideCard" style="display: none;">
						<div class="rp-sg-signal-row">
							<span>신호: </span>
							<strong id="rp_sg_signalText">-</strong>
						</div>
						<ul class="rp-sg-list">
							<li class="rp-sg-item">
								<div class="rp-sg-icon rp-sg-icon--return">수익률</div>
								<div class="rp-sg-label">현재 수익률</div>
								<div class="rp-sg-value" id="rp_sg_returnPct">-</div>
							</li>
							<li class="rp-sg-item">
								<div class="rp-sg-icon rp-sg-icon--target">목표</div>
								<div class="rp-sg-label">목표가</div>
								<div class="rp-sg-value text-danger" id="rp_sg_targetPrice">-</div>
							</li>
							<li class="rp-sg-item">
								<div class="rp-sg-icon rp-sg-icon--tp1">TP1</div>
								<div class="rp-sg-label">1차 목표가</div>
								<div class="rp-sg-value text-warning" id="rp_sg_tp1Price">-</div>
							</li>
							<li class="rp-sg-item">
								<div class="rp-sg-icon rp-sg-icon--stop">손절</div>
								<div class="rp-sg-label">손절가</div>
								<div class="rp-sg-value text-primary" id="rp_sg_stopPrice">-</div>
							</li>
						</ul>
					</div>
					<p id="rp_sellGuideEmpty" class="rp-guide-placeholder">
						추천 저장 이력에서 종목을 선택하면<br>매도 가이드가 표시됩니다.
					</p>
				</div>
			</div>
		</div>
	</div>
</div>
<div class="row">
	<div class="col-lg-12">
		<div class="card card-primary card-outline">
			<div class="card-header">
				<h3 class="card-title">성공 확률 <small class="text-muted">누적 평가 결과</small></h3>
				<div class="card-tools">
					<span class="rp-focus-chip">집계 기준 <strong>평가 완료 데이터</strong></span>
				</div>
			</div>
			<div class="card-body">
				<div class="form-group search_row">
					<label class="col-sm-1 col-form-label text-right">기간(일)</label>
					<div class="col-sm-2">
						<select id="rp_probHoldDays" class="form-control form-control-sm">
							<option value="">전체</option>
							<option value="5">5일</option>
							<option value="20">20일</option>
							<option value="60">60일</option>
						</select>
					</div>
					<label class="col-sm-1 col-form-label text-right">시장</label>
					<div class="col-sm-2">
						<select id="rp_probMktCd" class="form-control form-control-sm">
							<option value="">전체</option>
							<option value="KR">KR</option>
							<option value="US">US</option>
						</select>
					</div>
					<label class="col-sm-1 col-form-label text-right">등급</label>
					<div class="col-sm-2">
						<select id="rp_probGrade" class="form-control form-control-sm">
							<option value="">전체</option>
							<option value="A">A</option>
							<option value="B">B</option>
							<option value="C">C</option>
						</select>
					</div>
					<div class="col-sm-3">
						<button type="button" class="btn btn-info btn-sm"
							onclick="rpLoadProbability()">
							<i class="fa fa-search"></i> 조회
						</button>
					</div>
				</div>
				<div class="table-responsive">
					<table id="rp_probTable"
						class="table table-hover table-sm rp-prob-table">
						<thead>
							<tr>
								<th>평가기준</th>
								<th>보유기간</th>
								<th>시장</th>
								<th>등급</th>
								<th>진입룰</th>
								<th>샘플수</th>
								<th>승률(%)</th>
								<th>TP1달성(%)</th>
								<th>손절률(%)</th>
								<th>평균수익(%)</th>
								<th>평균MFE(%)</th>
								<th>평균MAE(%)</th>
							</tr>
						</thead>
						<tbody id="rp_probBody">
							<tr>
								<td colspan="12" class="text-center text-muted py-4">조회 버튼을
									클릭하세요</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
</div>
		
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
window.recPickManageConfig = {
  urls: {
    list: "<c:url value='/stock/recPick/list.do'/>",
    dailyTrack: "<c:url value='/stock/recPick/dailyTrack.do'/>",
    probability: "<c:url value='/stock/recPick/probability.do'/>",
    sellGuide: "<c:url value='/stock/recPick/sellGuide.do'/>",
    registerBuy: "<c:url value='/stock/recPick/registerBuy.do'/>"
  }
};
</script>
<script src="<c:url value='/appone/jsp/stock/recPickManage.js?v=20260403-1' />"></script>
