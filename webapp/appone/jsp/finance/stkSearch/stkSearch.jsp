<%@ page contentType="text/html; charset=utf-8" %>
<link rel="stylesheet" href="/scheduler/appone/jsp/finance/stkSearch/css/stkSearch.css"/>

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

            <div class="form-group search_row mb-2">
                <!-- 국내/해외 -->
                <label class="stkSearch-label">구분</label>
                <select id="sel_domain" class="form-control form-control-sm stkSearch-select" onchange="fnStkDomainChange()">
                    <option value="domestic">국내</option>
                    <option value="overseas">해외</option>
                </select>

                <!-- 시장 (국내) -->
                <div id="wrap_domestic_mkt" class="d-inline-flex align-items-center">
                    <label class="stkSearch-label ml-2">시장</label>
                    <select id="sel_domestic_mkt" class="form-control form-control-sm stkSearch-select" onchange="fnStkSearch()">
                        <option value="">전체</option>
                        <option value="KOSPI">코스피</option>
                        <option value="KOSDAQ">코스닥</option>
                    </select>
                </div>

                <!-- 지수 (국내) -->
                <div id="wrap_domestic_idx" class="d-inline-flex align-items-center">
                    <label class="stkSearch-label ml-2">지수</label>
                    <select id="sel_domestic_idx" class="form-control form-control-sm stkSearch-select" onchange="fnStkSearch()">
                        <option value="">전체</option>
                        <option value="KOSPI200">KOSPI200</option>
                        <option value="KOSPI100">KOSPI100</option>
                        <option value="KOSPI50">KOSPI50</option>
                        <option value="KOSDAQ150">KOSDAQ150</option>
                        <option value="KRX300">KRX300</option>
                    </select>
                </div>

                <!-- 시장 (해외) -->
                <div id="wrap_overseas_mkt" class="d-inline-flex align-items-center" style="display:none !important;">
                    <label class="stkSearch-label ml-2">시장</label>
                    <select id="sel_overseas_mkt" class="form-control form-control-sm stkSearch-select" onchange="fnStkSearch()">
                        <option value="">전체</option>
                        <option value="NASDAQ">나스닥</option>
                        <option value="NYSE">NYSE</option>
                    </select>
                </div>

                <!-- 지수 (해외) -->
                <div id="wrap_overseas_idx" class="d-inline-flex align-items-center" style="display:none !important;">
                    <label class="stkSearch-label ml-2">지수</label>
                    <select id="sel_overseas_idx" class="form-control form-control-sm stkSearch-select" onchange="fnStkSearch()">
                        <option value="">전체</option>
                        <option value="SNP500">S&amp;P500</option>
                        <option value="DOW30">다우30</option>
                        <option value="NAS100">나스닥100</option>
                    </select>
                </div>
            </div>

            <div class="form-group search_row mb-0">
                <!-- 종목 검색 -->
                <label class="stkSearch-label">종목</label>
                <input type="text" id="in_keyword" class="form-control form-control-sm stkSearch-input-keyword"
                       placeholder="종목명 또는 코드 입력" onkeyup="fnStkKeywordKeyup(event)"/>

                <!-- 유형 -->
                <label class="stkSearch-label ml-2">유형</label>
                <select id="sel_stk_type" class="form-control form-control-sm stkSearch-select" onchange="fnStkSearch()">
                    <option value="">전체</option>
                    <option value="STOCK">주식</option>
                    <option value="ETF">ETF</option>
                    <option value="ETN">ETN</option>
                    <option value="ELW">ELW</option>
                </select>

                <!-- 정렬 -->
                <label class="stkSearch-label ml-2">정렬</label>
                <select id="sel_sort" class="form-control form-control-sm stkSearch-select" onchange="fnStkSearch()">
                    <option value="">기본</option>
                    <option value="mktcap">시가총액</option>
                    <option value="bsop_prfi">영업이익</option>
                    <option value="thtr_ntin">당기순이익</option>
                    <option value="roe">ROE</option>
                </select>

                <button type="button" class="btn btn-sm btn-info ml-2" onclick="fnStkSearch()">
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

<script src="/scheduler/appone/jsp/finance/stkSearch/js/stkSearch.js"></script>
