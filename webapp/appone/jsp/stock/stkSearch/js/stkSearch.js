/* 종목 검색 페이지 JS */
(function () {
    'use strict';

    /* ── 상태 ── */
    var state = {
        domain   : 'domestic',
        mktCd    : '',
        indexCd  : '',
        stkType  : '',
        sort     : '',
        keyword  : ''
    };

    /* ── 초기화 ── */
    $(document).ready(function () {
        fnStkGridLoad();
    });

    /* ── 탭 전환 ── */
    window.fnStkTabChange = function (domain) {
        state.domain  = domain;
        state.mktCd   = '';
        state.indexCd = '';

        if (domain === 'domestic') {
            $('#tab_domestic').addClass('active btn-info').removeClass('btn-outline-secondary');
            $('#tab_overseas').removeClass('active btn-info').addClass('btn-outline-secondary');
            $('#filter_domestic').show();
            $('#filter_overseas').hide();
            fnResetBtnGroup('#grp_domestic_mkt');
            fnResetBtnGroup('#grp_domestic_idx');
        } else {
            $('#tab_overseas').addClass('active btn-info').removeClass('btn-outline-secondary');
            $('#tab_domestic').removeClass('active btn-info').addClass('btn-outline-secondary');
            $('#filter_overseas').show();
            $('#filter_domestic').hide();
            fnResetBtnGroup('#grp_overseas_mkt');
            fnResetBtnGroup('#grp_overseas_idx');
        }
        fnStkGridLoad();
    };

    /* ── 시장 버튼 ── */
    window.fnStkMktClick = function (btn, domain) {
        var grpId = domain === 'domestic' ? '#grp_domestic_mkt' : '#grp_overseas_mkt';
        fnActivateBtn(grpId, btn);
        state.mktCd = $(btn).data('val');
        fnStkGridLoad();
    };

    /* ── 지수 버튼 ── */
    window.fnStkIdxClick = function (btn) {
        var grpId = state.domain === 'domestic' ? '#grp_domestic_idx' : '#grp_overseas_idx';
        fnActivateBtn(grpId, btn);
        state.indexCd = $(btn).data('val');
        fnStkGridLoad();
    };

    /* ── 종목 유형 버튼 ── */
    window.fnStkTypeClick = function (btn) {
        fnActivateBtn('#grp_stk_type', btn);
        state.stkType = $(btn).data('val');
        fnStkGridLoad();
    };

    /* ── 정렬 버튼 ── */
    window.fnStkSortClick = function (btn) {
        fnActivateBtn('#grp_sort', btn);
        state.sort = $(btn).data('val');
        fnStkGridLoad();
    };

    /* ── 키워드 Enter ── */
    window.fnStkKeywordKeyup = function (e) {
        if (e.keyCode === 13) {
            state.keyword = $('#in_keyword').val().trim();
            fnStkGridLoad();
        }
    };

    /* ── 검색 버튼 ── */
    window.fnStkSearch = function () {
        state.keyword = $('#in_keyword').val().trim();
        fnStkGridLoad();
    };

    /* ── DataTable 그리드 로드 ── */
    function fnStkGridLoad() {
        var param = {
            in_domain  : state.domain,
            in_mkt_cd  : state.mktCd,
            in_index_cd: state.indexCd,
            in_stk_type: state.stkType,
            in_sort    : state.sort,
            in_keyword : state.keyword
        };

        var columns = [
            { data: 'rnum'         },
            { data: 'stkCd'        },
            { data: 'stkNm'        },
            { data: 'symbol'       },
            { data: 'mktCd'        },
            { data: 'stkType'      },
            { data: 'indexCd'      },   // 지수편입 (render)
            { data: 'prdyAvlsScal'},
            { data: 'bsopPrfi'    },
            { data: 'thtrNtin'    },
            { data: 'roeVal'      },
            { data: 'baseDt'      },
            { data: null          }    // 관리종목/정리매매 (render)
        ];

        var columnDefs = [
            {
                targets: 0,
                className: 'text-center',
                orderable: false,
                width: '40px'
            },
            {
                targets: [1, 3, 4, 5, 10, 11],
                className: 'text-center'
            },
            {
                targets: [7, 8, 9],
                className: 'text-right',
                render: function (data) {
                    if (data == null || data === '') return '-';
                    var n = parseFloat(data);
                    if (isNaN(n)) return data;
                    return n.toLocaleString('ko-KR');
                }
            },
            {
                targets: 6,
                className: 'text-center',
                orderable: false,
                render: function (data, type, row) {
                    var tags = [];
                    if (row.kospi200SectCd) tags.push('<span class="badge badge-primary">K200</span>');
                    if (row.kospi100Yn   === 'Y') tags.push('<span class="badge badge-info">K100</span>');
                    if (row.kospi50Yn    === 'Y') tags.push('<span class="badge badge-info">K50</span>');
                    if (row.kosdaq150Yn  === 'Y') tags.push('<span class="badge badge-success">KQ150</span>');
                    if (row.snp500Yn     === 'Y') tags.push('<span class="badge badge-warning">S&P</span>');
                    if (row.dow30Yn      === 'Y') tags.push('<span class="badge badge-warning">DOW</span>');
                    if (row.nas100Yn     === 'Y') tags.push('<span class="badge badge-warning">NAS</span>');
                    return tags.join(' ') || '-';
                }
            },
            {
                targets: 12,
                className: 'text-center',
                orderable: false,
                render: function (data, type, row) {
                    var tags = [];
                    if (row.mangYn === 'Y') tags.push('<span class="badge badge-danger">관리</span>');
                    if (row.sltrYn === 'Y') tags.push('<span class="badge badge-danger">정리</span>');
                    return tags.join(' ') || '';
                }
            }
        ];

        var gridObj = {
            grid_id   : 'stkSearchGrid',
            url       : '/scheduler/stkSearch/selectList.do',
            param     : param,
            columns   : columns,
            columnDefs: columnDefs,
            columnCheck: false
        };

        var gridOptions = {
            serverSide  : true,
            searching   : false,
            paging      : true,
            lengthChange: true,
            processing  : true,
            ordering    : false,
            drawCallback: function (settings) {
                var total = settings.json && settings.json.recordsTotal ? settings.json.recordsTotal : 0;
                $('#stkSearch_total_cnt').text('총 ' + total.toLocaleString('ko-KR') + '건');
            }
        };

        dataTableGridNew(gridObj, gridOptions);
    }

    /* ── 버튼 그룹 active 처리 ── */
    function fnActivateBtn(grpSelector, btn) {
        $(grpSelector + ' button').removeClass('active btn-info').addClass('btn-outline-secondary');
        $(btn).addClass('active').removeClass('btn-outline-secondary');
    }

    /* ── 버튼 그룹 전체 초기화 (첫 번째 선택) ── */
    function fnResetBtnGroup(grpSelector) {
        $(grpSelector + ' button').removeClass('active').addClass('btn-outline-secondary');
        $(grpSelector + ' button:first').addClass('active').removeClass('btn-outline-secondary');
    }

}());
