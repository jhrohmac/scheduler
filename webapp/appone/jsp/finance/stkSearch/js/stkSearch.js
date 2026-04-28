/* 종목 검색 페이지 JS */
(function () {
    'use strict';

    /* ── 초기화 ── */
    $(document).ready(function () {
        fnStkGridLoad();
    });

    /* ── 구분(국내/해외) 변경 ── */
    window.fnStkDomainChange = function () {
        var domain = $('#sel_domain').val();
        if (domain === 'domestic') {
            $('#wrap_domestic_mkt, #wrap_domestic_idx').show();
            $('#wrap_overseas_mkt, #wrap_overseas_idx').hide();
        } else {
            $('#wrap_overseas_mkt, #wrap_overseas_idx').show();
            $('#wrap_domestic_mkt, #wrap_domestic_idx').hide();
        }
        fnStkSearch();
    };

    /* ── 키워드 Enter ── */
    window.fnStkKeywordKeyup = function (e) {
        if (e.keyCode === 13) { fnStkSearch(); }
    };

    /* ── 검색 실행 ── */
    window.fnStkSearch = function () {
        fnStkGridLoad();
    };

    /* ── 숫자 천단위 포맷 ── */
    function fmtNum(val, decimals) {
        if (val == null || val === '') return '-';
        var n = parseFloat(val);
        if (isNaN(n)) return '-';
        if (decimals != null) return n.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        return Math.round(n).toLocaleString('ko-KR');
    }

    /* ── 지수편입 배지 HTML ── */
    function fmtIndex(row) {
        var tags = [];
        if (row.kospi200SectCd)      tags.push('<span class="badge badge-primary">K200</span>');
        if (row.kospi100Yn  === 'Y') tags.push('<span class="badge badge-info">K100</span>');
        if (row.kospi50Yn   === 'Y') tags.push('<span class="badge badge-info">K50</span>');
        if (row.kosdaq150Yn === 'Y') tags.push('<span class="badge badge-success">KQ150</span>');
        if (row.snp500Yn    === 'Y') tags.push('<span class="badge badge-warning text-dark">S&P</span>');
        if (row.dow30Yn     === 'Y') tags.push('<span class="badge badge-warning text-dark">DOW</span>');
        if (row.nas100Yn    === 'Y') tags.push('<span class="badge badge-warning text-dark">NAS</span>');
        return tags.length > 0 ? tags.join(' ') : '-';
    }

    /* ── DataTable 그리드 로드 ── */
    function fnStkGridLoad() {
        var domain = $('#sel_domain').val();
        var param = {
            in_domain   : domain,
            in_mkt_cd   : domain === 'domestic' ? $('#sel_domestic_mkt').val() : $('#sel_overseas_mkt').val(),
            in_index_cd : domain === 'domestic' ? $('#sel_domestic_idx').val() : $('#sel_overseas_idx').val(),
            in_stk_type : $('#sel_stk_type').val(),
            in_sort     : $('#sel_sort').val(),
            in_keyword  : $('#in_keyword').val().trim()
        };

        var columns = [
            { data: 'rnum',          className: 'text-center' },
            { data: 'stkCd',         className: 'text-center' },
            { data: 'stkNm' },
            { data: 'symbol',        className: 'text-center' },
            { data: 'mktCd',         className: 'text-center' },
            { data: 'stkType',       className: 'text-center' },
            {   // 지수편입
                data: 'kospi200SectCd',
                className: 'text-center',
                orderable: false,
                render: function (data, type, row) { return fmtIndex(row); }
            },
            {   // 시가총액(억)
                data: 'prdyAvlsScal',
                className: 'text-right',
                render: function (data) { return fmtNum(data); }
            },
            {   // 영업이익(억)
                data: 'bsopPrfi',
                className: 'text-right',
                render: function (data) { return fmtNum(data); }
            },
            {   // 당기순이익(억)
                data: 'thtrNtin',
                className: 'text-right',
                render: function (data) { return fmtNum(data); }
            },
            {   // ROE(%)
                data: 'roeVal',
                className: 'text-right',
                render: function (data) { return fmtNum(data, 2); }
            },
            { data: 'baseDt',        className: 'text-center' },
            {   // 관리/정리
                data: 'mangYn',
                className: 'text-center',
                orderable: false,
                render: function (data, type, row) {
                    var tags = [];
                    if (row.mangYn === 'Y') tags.push('<span class="badge badge-danger">관리</span>');
                    if (row.sltrYn === 'Y') tags.push('<span class="badge badge-danger">정리</span>');
                    return tags.join(' ');
                }
            }
        ];

        var columnDefs = [
            { targets: 0, orderable: false, width: '40px' }
        ];

        var gridObj = {
            grid_id    : 'stkSearchGrid',
            url        : '/scheduler/stkSearch/selectList.do',
            param      : param,
            columns    : columns,
            columnDefs : columnDefs,
            columnCheck: false
        };

        var gridOptions = {
            serverSide    : true,
            searching     : false,
            paging        : true,
            pageLength    : 50,
            lengthChange  : true,
            lengthMenu    : [[10, 30, 50, 100], [10, 30, 50, 100]],
            processing    : true,
            ordering      : false,
            drawCallback  : function (settings) {
                var json = settings.json;
                var total = json && json.recordsTotal ? json.recordsTotal : 0;
                $('#stkSearch_total_cnt').text('총 ' + total.toLocaleString('ko-KR') + '건');
            }
        };

        dataTableGridNew(gridObj, gridOptions);
    }

}());
