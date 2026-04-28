<%@ page contentType="text/html; charset=utf-8" %>
<link rel="stylesheet" href="/scheduler/appone/jsp/stock/stkSearch/css/stkSearch.css"/>

<div class="container-fluid" id="div_stkSearch_main">

    <!-- ① 검색 조건 카드 -->
    <div class="card card-info mb-2">
        <div class="card-header py-1">
            <h3 class="card-title">종목 검색</h3>
            <div class="card-tools">
                <button type="button" class="btn btn-tool" data-card-widget="collapse">
                    <i class="fas fa-minus"></i>
                </button>
            </div>
        </div>
        <div class="card-body p-2">

            <!-- 국내/해외 탭 -->
            <div class="stkSearch-domain-tabs mb-2">
                <button type="button" class="btn btn-sm btn-info active" id="tab_domestic" onclick="fnStkTabChange('domestic')">국내</button>
                <button type="button" class="btn btn-sm btn-outline-secondary" id="tab_overseas" onclick="fnStkTabChange('overseas')">해외</button>
            </div>

            <!-- 국내 시장/지수 필터 -->
            <div id="filter_domestic" class="stkSearch-filter-row mb-2">
                <span class="stkSearch-filter-label">시장</span>
                <div class="btn-group btn-group-sm mr-3" id="grp_domestic_mkt">
                    <button type="button" class="btn btn-outline-secondary active" data-val="" onclick="fnStkMktClick(this,'domestic')">전체</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="KOSPI" onclick="fnStkMktClick(this,'domestic')">코스피</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="KOSDAQ" onclick="fnStkMktClick(this,'domestic')">코스닥</button>
                </div>
                <span class="stkSearch-filter-label">지수</span>
                <div class="btn-group btn-group-sm" id="grp_domestic_idx">
                    <button type="button" class="btn btn-outline-secondary active" data-val="" onclick="fnStkIdxClick(this)">전체</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="KOSPI200" onclick="fnStkIdxClick(this)">KOSPI200</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="KOSDAQ150" onclick="fnStkIdxClick(this)">KOSDAQ150</button>
                </div>
            </div>

            <!-- 해외 시장/지수 필터 -->
            <div id="filter_overseas" class="stkSearch-filter-row mb-2" style="display:none;">
                <span class="stkSearch-filter-label">시장</span>
                <div class="btn-group btn-group-sm mr-3" id="grp_overseas_mkt">
                    <button type="button" class="btn btn-outline-secondary active" data-val="" onclick="fnStkMktClick(this,'overseas')">전체</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="NASDAQ" onclick="fnStkMktClick(this,'overseas')">나스닥</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="NYSE" onclick="fnStkMktClick(this,'overseas')">NYSE</button>
                </div>
                <span class="stkSearch-filter-label">지수</span>
                <div class="btn-group btn-group-sm" id="grp_overseas_idx">
                    <button type="button" class="btn btn-outline-secondary active" data-val="" onclick="fnStkIdxClick(this)">전체</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="SNP500" onclick="fnStkIdxClick(this)">S&amp;P500</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="DOW30" onclick="fnStkIdxClick(this)">다우30</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="NAS100" onclick="fnStkIdxClick(this)">나스닥100</button>
                </div>
            </div>

            <!-- 종목 검색 + 종목유형 + 정렬 -->
            <div class="stkSearch-filter-row">
                <span class="stkSearch-filter-label">종목</span>
                <input type="text" id="in_keyword" class="form-control form-control-sm stkSearch-input-keyword"
                       placeholder="종목명 또는 코드 입력" onkeyup="fnStkKeywordKeyup(event)"/>

                <span class="stkSearch-filter-label ml-2">유형</span>
                <div class="btn-group btn-group-sm mr-3" id="grp_stk_type">
                    <button type="button" class="btn btn-outline-secondary active" data-val="" onclick="fnStkTypeClick(this)">전체</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="STOCK" onclick="fnStkTypeClick(this)">주식</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="ETF" onclick="fnStkTypeClick(this)">ETF</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="ETN" onclick="fnStkTypeClick(this)">ETN</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="ELW" onclick="fnStkTypeClick(this)">ELW</button>
                </div>

                <span class="stkSearch-filter-label ml-2">정렬</span>
                <div class="btn-group btn-group-sm mr-3" id="grp_sort">
                    <button type="button" class="btn btn-outline-secondary active" data-val="" onclick="fnStkSortClick(this)">기본</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="mktcap" onclick="fnStkSortClick(this)">시가총액</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="bsop_prfi" onclick="fnStkSortClick(this)">영업이익</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="thtr_ntin" onclick="fnStkSortClick(this)">당기순이익</button>
                    <button type="button" class="btn btn-outline-secondary" data-val="roe" onclick="fnStkSortClick(this)">ROE</button>
                </div>

                <button type="button" class="btn btn-sm btn-info" onclick="fnStkSearch()">
                    <i class="fas fa-search"></i> 검색
                </button>
            </div>

        </div>
    </div>

    <!-- ② 결과 카드 -->
    <div class="card card-info">
        <div class="card-header py-1">
            <h3 class="card-title">검색 결과</h3>
            <div class="card-tools">
                <span id="stkSearch_total_cnt" class="badge badge-light mr-2"></span>
            </div>
        </div>
        <div class="card-body p-2 table-responsive">
            <table class="table table-sm table-hover" id="stkSearchGrid">
                <thead>
                    <tr>
                        <th>No</th>
                        <th>종목코드</th>
                        <th>종목명</th>
                        <th>심볼</th>
                        <th>시장</th>
                        <th>유형</th>
                        <th>지수편입</th>
                        <th>시가총액(억)</th>
                        <th>영업이익(억)</th>
                        <th>당기순이익(억)</th>
                        <th>ROE(%)</th>
                        <th>기준월</th>
                        <th>관리/정리</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>

</div>

<script src="/scheduler/appone/jsp/stock/stkSearch/js/stkSearch.js"></script>
