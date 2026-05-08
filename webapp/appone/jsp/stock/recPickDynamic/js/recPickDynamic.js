/* ============================================================
 * 동적 추천 종목 선별 — 메인 컨트롤러
 * ============================================================ */
(function () {
    "use strict";

    var config = window.recPickDynamicConfig || {};
    var registry = window.IndicatorRegistry;

    /* ── 시장 그룹별 옵션 (국내/해외) ───────────────────────── */
    var MARKET_OPTIONS = {
        KR: {
            market: [
                { value: "ALL", label: "전체" },
                { value: "KOSPI",  label: "코스피" },
                { value: "KOSDAQ", label: "코스닥" }
            ],
            index: [
                { value: "ALL", label: "전체" },
                { value: "KOSPI200",  label: "KOSPI200" },
                { value: "KOSDAQ150", label: "KOSDAQ150" }
            ],
            type: [
                { value: "ALL",   label: "전체" },
                { value: "STOCK", label: "주식" },
                { value: "ETF",   label: "ETF" },
                { value: "ETN",   label: "ETN" },
                { value: "ELW",   label: "ELW" }
            ]
        },
        US: {
            market: [
                { value: "ALL",    label: "전체" },
                { value: "NASDAQ", label: "NASDAQ" },
                { value: "NYSE",   label: "NYSE" }
            ],
            index: [
                { value: "ALL",    label: "전체" },
                { value: "DOW",    label: "DOW" },
                { value: "SNP500", label: "S&P500" },
                { value: "NDX100", label: "NASDAQ100" }
            ],
            type: [
                { value: "ALL",   label: "전체" },
                { value: "STOCK", label: "주식" },
                { value: "ETF",   label: "ETF" }
            ]
        }
    };

    /* ── 상태 ─────────────────────────────────────── */
    var state = {
        mktGroup: "KR",
        market: "ALL",
        indexFilter: "ALL",
        stockType: "ALL",
        sortColumn: "trendStrength",
        sortDir: "desc",
        baseDt: null,
        indicators: [],         // [{id, params, enabled}]
        indicatorsMeta: [],     // 백엔드 메타 + JS 머지본
        currentStocks: [],      // 화면 그리드 데이터
        currentSelectedIdx: null,
        registeredPicks: {}     // stkCd → true (이미 픽 등록됨 표시용)
    };

    /* ── 페이지 초기화 ─────────────────────────────── */
    function init() {
        bindMarketGroupTabs();
        bindFilterButtons();
        loadIndicatorMeta()
            .then(function () {
                // 1) 저장된 프리셋이 있으면 state 에 적용 (시장/지수/유형/지표 활성여부)
                restorePresetIntoState();
                // 2) 시장 칩, 지표 패널 렌더 (state 기준으로 카드/파라미터 초기값 반영)
                renderMarketChips();
                renderIndicatorPanel();
                applyDefaults();
                // 3) 지표 패널 렌더 후, 저장된 파라미터값을 input 에 주입
                applySavedParamsToInputs();
                applyFilters();
            });
    }

    /* ── 사용자별 프리셋 저장/복원 (localStorage) ─────────────────── */
    function presetKey() {
        var uid = (config.userId || "anonymous");
        return "rpd.preset." + uid;
    }

    /** 현재 화면 상태를 객체로 직렬화 */
    function buildPresetSnapshot() {
        var indicators = collectActiveIndicators();
        return {
            mktGroup: state.mktGroup,
            market: state.market,
            indexFilter: state.indexFilter,
            stockType: state.stockType,
            sortColumn: state.sortColumn,
            sortDir: state.sortDir,
            indicators: indicators
        };
    }

    function savePreset() {
        try {
            var snap = buildPresetSnapshot();
            snap._savedAt = new Date().toISOString();
            localStorage.setItem(presetKey(), JSON.stringify(snap));
        } catch (e) {
            // localStorage 사용 불가 환경 (privacy mode 등)
            console.warn("[recPickDynamic] preset save failed:", e);
        }
    }

    function loadPreset() {
        try {
            var raw = localStorage.getItem(presetKey());
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    }

    var _restoredPreset = null;

    /** state 기본값을 저장된 프리셋으로 덮어쓰기 (지표 카드 렌더 전에 호출) */
    function restorePresetIntoState() {
        var p = loadPreset();
        if (!p) return;
        _restoredPreset = p;
        if (p.mktGroup)    state.mktGroup    = p.mktGroup;
        if (p.market)      state.market      = p.market;
        if (p.indexFilter) state.indexFilter = p.indexFilter;
        if (p.stockType)   state.stockType   = p.stockType;
        if (p.sortColumn)  state.sortColumn  = p.sortColumn;
        if (p.sortDir)     state.sortDir     = p.sortDir;
        // 시장 그룹 탭 활성 표시 동기화
        $("#rpdMarketGroupTabs .rpd-tab").removeClass("active");
        $("#rpdMarketGroupTabs .rpd-tab[data-mkt-group='" + state.mktGroup + "']").addClass("active");
        // 정렬 select 동기화
        $("#rpdSortSelect").val(state.sortColumn);
    }

    /** 지표 카드 렌더 후, 저장된 활성 지표 + 파라미터값을 input 에 주입 */
    function applySavedParamsToInputs() {
        if (!_restoredPreset || !_restoredPreset.indicators) return;
        var savedById = {};
        _restoredPreset.indicators.forEach(function (ind) {
            savedById[ind.id] = ind.params || {};
        });
        // 1) 활성/비활성 토글 — 저장된 목록에 있는 지표만 active
        $(".rpd-indicator-card").each(function () {
            var id = $(this).data("id");
            var $cb = $(this).find('input[type="checkbox"]');
            var isOn = !!savedById[id];
            $cb.prop("checked", isOn);
            $(this).toggleClass("active", isOn);
        });
        // 2) 활성화된 카드의 파라미터값 채우기
        Object.keys(savedById).forEach(function (id) {
            var $card = $('.rpd-indicator-card[data-id="' + id + '"]');
            if (!$card.length) return;
            var params = savedById[id];
            $card.find(".rpd-param-row").each(function () {
                var key = $(this).data("paramKey");
                var $input = $(this).data("input");
                if (!$input || !key || params[key] == null) return;
                $input.val(params[key]);
                // range 타입은 value-display 텍스트도 갱신
                if ($input.attr("type") === "range") {
                    var $vd = $(this).find(".value-display");
                    if ($vd.length) {
                        var unit = $(this).find(".unit").text() || "";
                        $vd.text(params[key] + (unit ? "" : ""));
                    }
                }
            });
        });
    }

    /* ── 백엔드 지표 메타데이터 로드 ──────────────────── */
    function loadIndicatorMeta() {
        return new Promise(function (resolve, reject) {
            $.ajax({
                url: config.indicatorMetaUrl,
                method: "GET",
                dataType: "json",
                success: function (resp) {
                    // ResponseHandler 응답은 평탄(flat) 구조: resp.data 직접 접근
                    var list = (resp && resp.data) || [];
                    state.indicatorsMeta = registry ? registry.merge(list) : list;
                    resolve();
                },
                error: function (xhr, status, err) {
                    showToast("지표 메타데이터 로드 실패: " + err, true);
                    reject(err);
                }
            });
        });
    }

    /* ── 좌측 시장 그룹 탭 ───────────────────────────── */
    function bindMarketGroupTabs() {
        $("#rpdMarketGroupTabs .rpd-tab").on("click", function () {
            var group = $(this).data("mkt-group");
            if (group === state.mktGroup) return;
            state.mktGroup = group;
            $("#rpdMarketGroupTabs .rpd-tab").removeClass("active");
            $(this).addClass("active");
            // 시장/지수/유형 모두 ALL로 리셋 후 칩 재렌더
            state.market = "ALL";
            state.indexFilter = "ALL";
            state.stockType = "ALL";
            renderMarketChips();
            applyFilters();
        });
    }

    /* ── 시장/지수/유형 칩 (그룹에 따라 동적) ──────────── */
    function renderMarketChips() {
        var opts = MARKET_OPTIONS[state.mktGroup] || MARKET_OPTIONS.KR;

        renderChips("#rpdMarketChips", opts.market, state.market, function (v) {
            state.market = v;
        });
        renderChips("#rpdIndexChips", opts.index, state.indexFilter, function (v) {
            state.indexFilter = v;
        });
        renderChips("#rpdTypeChips", opts.type, state.stockType, function (v) {
            state.stockType = v;
        });
    }

    function renderChips(selector, options, selectedValue, onSelect) {
        var $wrap = $(selector);
        $wrap.empty();
        options.forEach(function (opt) {
            var $c = $('<span class="rpd-chip"></span>')
                .text(opt.label)
                .data("value", opt.value);
            if (opt.value === selectedValue) $c.addClass("active");
            $c.on("click", function () {
                $wrap.find(".rpd-chip").removeClass("active");
                $c.addClass("active");
                onSelect(opt.value);
                applyFilters();
            });
            $wrap.append($c);
        });
    }

    /* ── 지표 패널 렌더 (카테고리별) ───────────────────── */
    var CATEGORY_ICONS = {
        "추세": "📈", "모멘텀": "⚡", "유동성": "💹", "기본": "💰"
    };
    var CATEGORY_ORDER = ["추세", "모멘텀", "유동성", "기본"];

    function renderIndicatorPanel() {
        var $cont = $("#rpdIndicatorSections");
        $cont.empty();

        // 카테고리별 그룹
        var byCat = {};
        state.indicatorsMeta.forEach(function (ind) {
            var cat = ind.category || "기타";
            (byCat[cat] = byCat[cat] || []).push(ind);
        });

        var orderedCats = CATEGORY_ORDER.filter(function (c) { return byCat[c]; });
        Object.keys(byCat).forEach(function (c) {
            if (orderedCats.indexOf(c) < 0) orderedCats.push(c);
        });

        orderedCats.forEach(function (cat) {
            var $sec = $('<div class="rpd-indicator-section"></div>');
            var icon = CATEGORY_ICONS[cat] || "•";
            $sec.append('<h6>' + icon + ' ' + cat + ' 지표</h6>');
            byCat[cat].forEach(function (ind) {
                $sec.append(buildIndicatorCard(ind));
            });
            $cont.append($sec);
        });
    }

    function buildIndicatorCard(ind) {
        var enabled = isDefaultEnabled(ind.id);
        var $card = $('<div class="rpd-indicator-card"></div>')
            .attr("data-id", ind.id);
        if (enabled) $card.addClass("active");

        var newTag = ind.isNew ? '<span class="new-tag">신규</span>' : "";
        var $head = $(
            '<div class="rpd-indicator-head">' +
              '<input type="checkbox"' + (enabled ? ' checked' : '') + '>' +
              '<span class="name">' + escapeHtml(ind.label) + newTag + '</span>' +
              '<span class="badge-cat">' + escapeHtml(ind.category || "—") + '</span>' +
            '</div>'
        );
        $head.find('input[type="checkbox"]').on("click", function (e) { e.stopPropagation(); });
        $head.on("click", function (e) {
            if (e.target.tagName !== "INPUT") {
                var $cb = $head.find('input[type="checkbox"]');
                $cb.prop("checked", !$cb.prop("checked"));
            }
            var checked = $head.find('input[type="checkbox"]').prop("checked");
            $card.toggleClass("active", checked);
            applyFilters();
        });
        $card.append($head);

        var $params = $('<div class="rpd-indicator-params"></div>');
        (ind.params || []).forEach(function (pdef) {
            $params.append(buildParamRow(ind.id, pdef));
        });
        $card.append($params);
        return $card;
    }

    function buildParamRow(indId, pdef) {
        var $row = $('<div class="rpd-param-row"></div>');
        $row.append('<label>' + escapeHtml(pdef.label) + '</label>');

        var $input;
        if (pdef.inputType === "select") {
            $input = $('<select></select>');
            (pdef.options || []).forEach(function (o) {
                $input.append('<option value="' + escapeHtml(o.value) + '">' + escapeHtml(o.label) + '</option>');
            });
            $input.val(pdef.defaultValue);
        } else if (pdef.inputType === "range") {
            $input = $('<input type="range">')
                .attr("min", pdef.minValue != null ? pdef.minValue : 0)
                .attr("max", pdef.maxValue != null ? pdef.maxValue : 100)
                .attr("step", pdef.step != null ? pdef.step : 1)
                .val(pdef.defaultValue);
            var $val = $('<span class="value-display"></span>')
                .text(pdef.defaultValue + (pdef.unit || ""));
            $input.on("input", function () {
                $val.text($input.val() + (pdef.unit || ""));
            });
            $input.on("change", function () { applyFilters(); });
            $row.append($input).append($val);
            $row.attr("data-key", pdef.key);
            $row.data("paramKey", pdef.key);
            $row.data("input", $input);
            return $row;
        } else {
            $input = $('<input type="number">')
                .attr("step", pdef.step != null ? pdef.step : 1)
                .val(pdef.defaultValue);
            if (pdef.minValue != null) $input.attr("min", pdef.minValue);
            if (pdef.maxValue != null) $input.attr("max", pdef.maxValue);
        }
        $input.on("change", function () { applyFilters(); });
        $row.append($input);
        if (pdef.unit) $row.append('<span class="unit">' + escapeHtml(pdef.unit) + '</span>');
        $row.attr("data-key", pdef.key);
        $row.data("paramKey", pdef.key);
        $row.data("input", $input);
        return $row;
    }

    function isDefaultEnabled(id) {
        // 기본 활성: 정배열, 당월상승, 추세강도, 거래대금
        return ["GOLDEN_ARRAY", "MONTH_UP", "TREND_STRENGTH", "VOLUME_FILTER"].indexOf(id) >= 0;
    }

    /* ── 필터 적용 (AJAX) ─────────────────────────────── */
    var debounceTimer = null;
    function applyFilters() {
        if (debounceTimer) clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function () { doApplyFilters(); }, 300);
    }

    function doApplyFilters() {
        var indicators = collectActiveIndicators();
        var marketFilter = resolveMarketFilter();

        var payload = {
            mktGroup: state.mktGroup,
            marketFilter: marketFilter,
            stockType: state.stockType,
            indicators: indicators,
            sortColumn: state.sortColumn,
            sortDir: state.sortDir,
            limit: 200
        };

        // 적용 시점에 사용자 프리셋 자동 저장
        savePreset();

        $("#rpdGridBody").html('<tr><td colspan="12" class="rpd-empty-row">조회 중...</td></tr>');
        $("#rpdBtnApply").prop("disabled", true).text("⏳ 조회 중");

        $.ajax({
            url: config.listUrl,
            method: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(payload),
            success: function (resp) {
                if (!resp) {
                    showToast("응답 형식 오류", true);
                    return;
                }
                var data = resp.data || [];
                var meta = resp.singleData || {};
                state.currentStocks = data;
                state.baseDt = meta.baseDt;
                renderSummary(meta);
                renderGrid(data);
            },
            error: function (xhr, status, err) {
                $("#rpdGridBody").html('<tr><td colspan="12" class="rpd-empty-row">조회 실패: ' + escapeHtml(err) + '</td></tr>');
                showToast("조회 실패: " + err, true);
            },
            complete: function () {
                $("#rpdBtnApply").prop("disabled", false).text("🔄 적용");
            }
        });
    }

    /** 활성화된 지표만 수집 */
    function collectActiveIndicators() {
        var list = [];
        $(".rpd-indicator-card.active").each(function () {
            var id = $(this).data("id");
            var params = {};
            $(this).find(".rpd-param-row").each(function () {
                var key = $(this).data("paramKey");
                var $input = $(this).data("input");
                if (!$input || !key) return;
                var val = $input.val();
                if ($input.attr("type") === "number" || $input.attr("type") === "range") {
                    val = parseFloat(val);
                }
                params[key] = val;
            });
            list.push({ id: id, params: params });
        });
        return list;
    }

    /** market vs index → 단일 marketFilter 값으로 머지 */
    function resolveMarketFilter() {
        if (state.indexFilter && state.indexFilter !== "ALL") return state.indexFilter;
        if (state.market && state.market !== "ALL") return state.market;
        return "ALL";
    }

    /* ── 요약 바 ─────────────────────────────────────── */
    function renderSummary(meta) {
        var dt = meta.baseDt || "—";
        if (meta.staleCount && meta.staleCount > 0) {
            $("#rpdBaseDt").html(dt + ' <span style="color:#dc2626;font-size:10.5px;font-weight:normal;">⚠ 누락 ' + meta.staleCount + '건</span>');
        } else {
            $("#rpdBaseDt").text(dt);
        }
        $("#rpdCntTotal").text(meta.totalCount || 0);
        $("#rpdCntIndicators").text(meta.indicatorCount || 0);
        var gc = meta.gradeCount || {};
        $("#rpdGradeA").text(gc.A || 0);
        $("#rpdGradeB").text(gc.B || 0);
        $("#rpdGradeC").text(gc.C || 0);
    }

    /* ── 그리드 ──────────────────────────────────────── */
    function renderGrid(data) {
        var $body = $("#rpdGridBody");
        $body.empty();
        if (!data || data.length === 0) {
            $body.html('<tr><td colspan="12" class="rpd-empty-row">매칭된 종목이 없습니다.</td></tr>');
            return;
        }
        data.forEach(function (s, i) {
            var rateClass = s.monChgRate >= 0 ? "text-up" : "text-down";
            var rateStr = s.monChgRate != null
                ? (s.monChgRate > 0 ? "+" : "") + Number(s.monChgRate).toFixed(2) + "%"
                : "—";
            var trdValEok = s.avgTrdVal20 != null
                ? Math.round(Number(s.avgTrdVal20) / 1e8) : 0;
            var grade = s.recGrade || "-";
            var gradePill = grade !== "-"
                ? '<span class="rpd-grade-pill rpd-grade-pill-' + grade + '">' + grade + '</span>'
                : '<span style="color:#94a3b8;">—</span>';

            var matched = (s.matchedIndicators || []).map(function (id) {
                var mod = registry && registry.get(id);
                var label = mod && mod.renderBadge ? mod.renderBadge() : id;
                return '<span class="rpd-badge-match">' + escapeHtml(label) + '✓</span>';
            }).join('');

            var pickClass = state.registeredPicks[s.stkCd] ? "rpd-pick-row-btn done" : "rpd-pick-row-btn";
            var pickLabel = state.registeredPicks[s.stkCd] ? "✓ 등록됨" : "★ 등록";

            // 코스닥 종목은 종목명 앞에 # 접두
            var stkNmDisplay = (s.listingMarket === "KOSDAQ" ? "# " : "") + (s.stkNm || "");
            // 분석일 — 시장 최신 BASE_DT 보다 오래된 경우 stale 표시
            var rowDt = s.baseDt || "—";
            var isStale = state.baseDt && rowDt && rowDt < state.baseDt;
            var dtCell = isStale
                ? '<span style="color:#dc2626;" title="배치 누락 — 최신 분석일과 다름">⚠ ' + rowDt + '</span>'
                : '<span style="color:#6b7280;">' + rowDt + '</span>';
            var $tr = $(
                '<tr>' +
                '<td>' + (i + 1) + '</td>' +
                '<td>' + escapeHtml(s.stkCd || "") + '</td>' +
                '<td><strong>' + escapeHtml(stkNmDisplay) + '</strong></td>' +
                '<td>' + escapeHtml(s.listingMarket || s.mktCd || "") + '</td>' +
                '<td class="text-right">' + (s.curPrice != null ? Number(s.curPrice).toLocaleString() : "—") + '</td>' +
                '<td class="text-right ' + rateClass + '">' + rateStr + '</td>' +
                '<td class="text-right">' + (s.trendStrength != null ? Number(s.trendStrength).toFixed(2) + '%' : "—") + '</td>' +
                '<td class="text-right">' + trdValEok.toLocaleString() + '</td>' +
                '<td>' + gradePill + '</td>' +
                '<td><div class="rpd-matched-badges">' + matched + '</div></td>' +
                '<td style="font-size:11px;">' + dtCell + '</td>' +
                '<td style="text-align:center;"><button type="button" class="' + pickClass + '">' + pickLabel + '</button></td>' +
                '</tr>'
            );
            $tr.data("idx", i).data("stock", s);
            $tr.on("click", function (e) {
                if ($(e.target).closest(".rpd-pick-row-btn").length) return;
                openDetailDrawer(i);
            });
            $tr.find(".rpd-pick-row-btn").on("click", function (e) {
                e.stopPropagation();
                quickPick(s, $(this));
            });
            $body.append($tr);
        });
    }

    /* ── 상세 슬라이드 ───────────────────────────────── */
    function openDetailDrawer(idx) {
        var s = state.currentStocks[idx];
        if (!s) return;
        state.currentSelectedIdx = idx;

        $("#rpdDName").text(s.stkNm || "—");
        $("#rpdDCode").text((s.stkCd || "—") + " · " + (s.listingMarket || s.mktCd || "—"));

        // 지표 상세 채우기
        $("#rpdDPrice").text(fmtPrice(s.curPrice));
        $("#rpdDMonOpen").text(fmtPrice(s.monOpenPrice));
        var rateHtml = s.monChgRate != null
            ? '<span class="' + (s.monChgRate >= 0 ? "text-up" : "text-down") + '">'
              + (s.monChgRate > 0 ? "+" : "") + Number(s.monChgRate).toFixed(2) + '%</span>'
            : "—";
        $("#rpdDChgRate").html(rateHtml);
        $("#rpdDMa1").text(fmtPrice(s.ma5) + " / " + fmtPrice(s.ma20));
        $("#rpdDMa2").text(fmtPrice(s.ma60) + " / " + fmtPrice(s.ma120));
        $("#rpdDMa240").text(fmtPrice(s.ma240));
        $("#rpdDTrend").text(s.trendStrength != null
            ? Number(s.trendStrength).toFixed(2) + "% "
              + (s.trendStrength >= 5 ? "🟢 강한 추세" : s.trendStrength >= 2 ? "🟡 보통" : "⚪ 약함")
            : "—");
        $("#rpdDTrdVal").text(s.avgTrdVal20 != null
            ? Math.round(Number(s.avgTrdVal20) / 1e8).toLocaleString() + " 억원" : "—");

        // 매칭 조건 리스트
        var $matchList = $("#rpdMatchList");
        $matchList.empty();
        (s.matchedIndicators || []).forEach(function (id) {
            var mod = registry && registry.get(id);
            var label = mod && mod.renderBadge ? mod.renderBadge() : id;
            // 사용자가 입력한 파라미터값으로 설명 생성
            var params = collectIndicatorParamsById(id);
            var desc = mod && mod.renderMatchDesc ? mod.renderMatchDesc(s, params) : "";
            $matchList.append(
                '<li><span class="check">✓</span> <strong>' + escapeHtml(label) + '</strong>: ' + escapeHtml(desc || "") + '</li>'
            );
        });

        // 픽 등록 버튼 상태
        var $pickBtn = $("#rpdPickBtnBig");
        if (state.registeredPicks[s.stkCd]) {
            $pickBtn.addClass("done").text("✓ 픽 완료");
        } else {
            $pickBtn.removeClass("done").text("★ 관심종목에 추가");
        }

        // 차트 비동기 로드
        $("#rpdChartContainer").html('<div style="text-align:center;color:#9ca3af;padding:60px;font-size:11.5px;">차트 로드 중...</div>');
        loadDetailChart(s);

        // RSI는 아직 미반영 (TODO: 차트 로드 후 계산)
        $("#rpdDRsi").text("계산 중...");

        $("#rpdDrawer").addClass("open");
        $("#rpdDrawerOverlay").addClass("open");
    }

    function loadDetailChart(stock) {
        $.ajax({
            url: config.detailUrl,
            method: "GET",
            dataType: "json",
            data: { baseDt: stock.baseDt, mktCd: stock.mktCd, stkCd: stock.stkCd },
            success: function (resp) {
                var meta = resp && resp.singleData;
                var priceList = (meta && meta.priceList) || [];
                renderChart(stock, priceList);
                renderRsi(priceList);
            },
            error: function () {
                $("#rpdChartContainer").html('<div style="text-align:center;color:#dc2626;padding:60px;">차트 로드 실패</div>');
            }
        });
    }

    /* ── 차트 옵션 상태 (UI 토글) ─────────────────────── */
    var chartOpts = {
        ma:        { 5: true, 20: true, 60: true, 120: false, 240: false },
        showVolume: true,
        showCandle: false
    };

    /** 현재 priceList 캐시 (토글 변경 시 재렌더용) */
    var _chartPriceList = [];
    var _chartStock = null;

    var MA_COLORS = {
        5:   '#dc2626',
        20:  '#2563eb',
        60:  '#059669',
        120: '#9333ea',
        240: '#ea580c'
    };

    function renderChart(stock, priceList) {
        if (typeof Highcharts === "undefined") {
            $("#rpdChartContainer").text("Highcharts 미로드");
            return;
        }
        _chartStock = stock;
        _chartPriceList = priceList || [];

        // OHLC + 종가 + 거래량 시계열 빌드
        var closeData = [], ohlcData = [], volumeData = [];
        var closes = [];
        priceList.forEach(function (p) {
            if (!p.tradeDt || p.close == null) return;
            var ts = parseDateUtc(p.tradeDt);
            closes.push(p.close);
            closeData.push([ts, p.close]);
            if (p.open != null && p.high != null && p.low != null) {
                ohlcData.push([ts, p.open, p.high, p.low, p.close]);
            }
            if (p.volume != null) {
                volumeData.push([ts, Number(p.volume)]);
            }
        });

        // MA 시리즈 동적 빌드
        var maSeries = [];
        [5, 20, 60, 120, 240].forEach(function (period) {
            if (!chartOpts.ma[period]) return;
            var maData = [];
            var arr = [];
            priceList.forEach(function (p) {
                if (!p.tradeDt || p.close == null) return;
                arr.push(p.close);
                var ts = parseDateUtc(p.tradeDt);
                maData.push([ts, sma(arr, period)]);
            });
            maSeries.push({
                name: 'MA' + period, type: 'line', data: maData,
                color: MA_COLORS[period], lineWidth: 1,
                dashStyle: period >= 120 ? 'ShortDash' : 'Dash',
                yAxis: 0, marker: { enabled: false }
            });
        });

        // 메인 시리즈 (캔들 또는 종가 라인)
        var mainSeries = chartOpts.showCandle && ohlcData.length > 0 ? {
            type: 'candlestick',
            name: 'OHLC',
            data: ohlcData,
            color: '#2563eb', upColor: '#dc2626',
            lineColor: '#2563eb', upLineColor: '#dc2626',
            yAxis: 0
        } : {
            type: 'line',
            name: '종가',
            data: closeData,
            color: '#374151', lineWidth: 1.5,
            yAxis: 0, marker: { enabled: false }
        };

        var allSeries = [mainSeries].concat(maSeries);
        if (chartOpts.showVolume) {
            allSeries.push({
                type: 'column', name: '거래량', data: volumeData,
                color: '#94a3b8', yAxis: 1
            });
        }

        // yAxis 구성 — 거래량 표시 시 더블차트 (가격 70% + 거래량 30%)
        var yAxisCfg;
        if (chartOpts.showVolume) {
            yAxisCfg = [
                { labels: { align: 'right', x: -3, style:{fontSize:'10px'} },
                  height: '70%', resize: { enabled: true }, lineWidth: 1, title: { text: null } },
                { labels: { align: 'right', x: -3, style:{fontSize:'10px'} },
                  top: '72%', height: '28%', offset: 0, lineWidth: 1, title: { text: null } }
            ];
        } else {
            yAxisCfg = { labels: { align: 'right', x: -3, style:{fontSize:'10px'} },
                         lineWidth: 1, title: { text: null } };
        }

        // Highcharts Stock 사용 (rangeSelector + navigator)
        var useStock = typeof Highcharts.stockChart === "function";
        var ChartCtor = useStock ? Highcharts.stockChart : Highcharts.chart;

        ChartCtor('rpdChartContainer', {
            chart: { spacing: [8, 8, 8, 8] },
            title: { text: null },
            legend: { enabled: true, itemStyle: { fontSize: '10px' } },
            rangeSelector: useStock ? {
                buttons: [
                    { type: 'month', count: 1, text: '1M' },
                    { type: 'month', count: 3, text: '3M' },
                    { type: 'month', count: 6, text: '6M' },
                    { type: 'ytd',  text: 'YTD' },
                    { type: 'all',  text: '전체' }
                ],
                selected: 2,
                inputEnabled: false
            } : undefined,
            navigator: useStock ? { enabled: true, height: 30 } : undefined,
            scrollbar: useStock ? { enabled: false } : undefined,
            xAxis: { type: 'datetime', labels: { style: { fontSize: '10px' } } },
            yAxis: yAxisCfg,
            tooltip: { split: useStock, shared: !useStock, valueDecimals: 0 },
            plotOptions: {
                series: { dataGrouping: { enabled: false } }
            },
            credits: { enabled: false },
            series: allSeries
        });
    }

    function renderRsi(priceList) {
        var rsi = calcWilderRsi(priceList, 14);
        if (rsi == null) {
            $("#rpdDRsi").text("—");
            return;
        }
        var label = rsi >= 70 ? "🔴 과매수" : rsi <= 30 ? "🔵 과매도" : "⚪ 중립";
        $("#rpdDRsi").text(rsi.toFixed(1) + " " + label);
    }

    function calcWilderRsi(priceList, period) {
        if (!priceList || priceList.length < period + 1) return null;
        var avgGain = 0, avgLoss = 0;
        for (var i = 1; i <= period; i++) {
            var d = priceList[i].close - priceList[i - 1].close;
            if (d > 0) avgGain += d; else avgLoss += -d;
        }
        avgGain /= period; avgLoss /= period;
        for (var j = period + 1; j < priceList.length; j++) {
            var diff = priceList[j].close - priceList[j - 1].close;
            var g = diff > 0 ? diff : 0;
            var l = diff < 0 ? -diff : 0;
            avgGain = (avgGain * (period - 1) + g) / period;
            avgLoss = (avgLoss * (period - 1) + l) / period;
        }
        if (avgLoss === 0) return 100;
        var rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    function sma(arr, n) {
        if (arr.length < n) return null;
        var sum = 0;
        for (var i = arr.length - n; i < arr.length; i++) sum += arr[i];
        return sum / n;
    }

    /* ── 픽 등록 ──────────────────────────────────────── */
    function quickPick(stock, $btn) {
        if ($btn.hasClass("done")) return;
        callSaveToWatchlist(stock, function () {
            state.registeredPicks[stock.stkCd] = true;
            $btn.addClass("done").text("✓ 등록됨");
            showToast(stock.stkNm + " 픽 등록 완료");
        }, function (err) {
            showToast("픽 등록 실패: " + err, true);
        });
    }

    function pickFromDrawer() {
        var s = state.currentStocks[state.currentSelectedIdx];
        if (!s) return;
        var $btn = $("#rpdPickBtnBig");
        if ($btn.hasClass("done")) return;
        $btn.prop("disabled", true);
        callSaveToWatchlist(s, function () {
            state.registeredPicks[s.stkCd] = true;
            $btn.addClass("done").text("✓ 픽 완료").prop("disabled", false);
            showToast(s.stkNm + " 픽 등록 완료");
            // 그리드도 갱신
            renderGrid(state.currentStocks);
        }, function (err) {
            $btn.prop("disabled", false);
            showToast("픽 등록 실패: " + err, true);
        });
    }

    function callSaveToWatchlist(stock, onOk, onErr) {
        $.ajax({
            url: config.saveToWatchlistUrl,
            method: "POST",
            dataType: "json",
            data: { baseDt: stock.baseDt, mktCd: stock.mktCd, stkCd: stock.stkCd },
            success: function (resp) {
                // ResponseHandler 의 성공 코드는 result_code 또는 system_code 로 내려옴
                var ok = resp && (
                    resp.result_code === "S001" ||
                    resp.result_code === "0000" ||
                    resp.system_code === "0000" ||
                    !resp.result_code   // 빈 문자열인 경우도 성공으로 간주
                );
                if (ok) onOk();
                else onErr(resp && resp.result_msg ? resp.result_msg : "알 수 없는 오류");
            },
            error: function (xhr, st, err) { onErr(err); }
        });
    }

    /* ── 버튼/이벤트 바인딩 ───────────────────────────── */
    function bindFilterButtons() {
        $("#rpdBtnApply").on("click", doApplyFilters);
        $("#rpdBtnReset").on("click", resetAll);

        $("#rpdSortSelect").on("change", function () {
            state.sortColumn = $(this).val();
            applyFilters();
        });

        $("#rpdDrawerClose").on("click", closeDrawer);
        $("#rpdDrawerOverlay").on("click", closeDrawer);
        $("#rpdPickBtnBig").on("click", pickFromDrawer);
        $("#rpdBtnAddInfo").on("click", function () {
            alert("신규 지표 추가 절차:\n\n1. src/com/scheduler/stock/indicator/impl/ 에 클래스 1개 추가\n2. stockService.xml 의 indicatorRegistry list 에 ref 추가\n3. webapp/.../stock/js/indicators/ 에 JS 모듈 1개 추가\n\n→ 기존 코드 0줄 수정");
        });

        // 차트 옵션 토글 — MA 5/20/60/120/240
        $(document).on("change", ".rpd-chart-controls input[data-ma]", function () {
            var period = parseInt($(this).data("ma"), 10);
            chartOpts.ma[period] = $(this).prop("checked");
            if (_chartStock) renderChart(_chartStock, _chartPriceList);
        });
        // 거래량 / 캔들 토글
        $(document).on("change", "#rpdToggleVolume", function () {
            chartOpts.showVolume = $(this).prop("checked");
            if (_chartStock) renderChart(_chartStock, _chartPriceList);
        });
        $(document).on("change", "#rpdToggleCandle", function () {
            chartOpts.showCandle = $(this).prop("checked");
            if (_chartStock) renderChart(_chartStock, _chartPriceList);
        });
        // 차트 새로고침 — KIS API 다시 호출
        $(document).on("click", "#rpdBtnRefreshChart", function () {
            if (state.currentSelectedIdx == null) return;
            var s = state.currentStocks[state.currentSelectedIdx];
            if (!s) return;
            $("#rpdChartContainer").html('<div style="text-align:center;color:#9ca3af;padding:60px;font-size:11.5px;">실시간 데이터 갱신 중...</div>');
            loadDetailChart(s);
            showToast("실시간 가격 갱신 — KIS API 호출");
        });

        // ESC로 슬라이드 닫기
        $(document).on("keydown.rpd", function (e) {
            if (e.key === "Escape") closeDrawer();
        });
    }

    function closeDrawer() {
        $("#rpdDrawer").removeClass("open");
        $("#rpdDrawerOverlay").removeClass("open");
    }

    function resetAll() {
        state.market = "ALL";
        state.indexFilter = "ALL";
        state.stockType = "ALL";
        state.sortColumn = "trendStrength";
        $("#rpdSortSelect").val("trendStrength");
        try { localStorage.removeItem(presetKey()); } catch (e) {}
        _restoredPreset = null;
        renderMarketChips();
        renderIndicatorPanel();
        applyFilters();
        showToast("프리셋 초기화 — 기본값 적용");
    }

    /* ── 헬퍼 ────────────────────────────────────────── */
    function applyDefaults() {
        $("#rpdBaseDt").text("로딩 중...");
    }

    function collectIndicatorParamsById(id) {
        var $card = $('.rpd-indicator-card[data-id="' + id + '"]');
        if (!$card.length || !$card.hasClass("active")) return {};
        var params = {};
        $card.find(".rpd-param-row").each(function () {
            var key = $(this).data("paramKey");
            var $input = $(this).data("input");
            if (!$input || !key) return;
            var val = $input.val();
            if ($input.attr("type") === "number" || $input.attr("type") === "range") {
                val = parseFloat(val);
            }
            params[key] = val;
        });
        return params;
    }

    function fmtPrice(v) {
        return v != null ? Number(v).toLocaleString() + " 원" : "—";
    }

    function parseDateUtc(yyyymmdd) {
        if (!yyyymmdd) return null;
        var s = String(yyyymmdd).replace(/-/g, "");
        if (s.length !== 8) return null;
        return Date.UTC(
            parseInt(s.substring(0, 4), 10),
            parseInt(s.substring(4, 6), 10) - 1,
            parseInt(s.substring(6, 8), 10)
        );
    }

    function escapeHtml(s) {
        if (s == null) return "";
        return String(s)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
    }

    var toastTimer = null;
    function showToast(msg, isError) {
        var $t = $("#rpdToast");
        $t.text(msg).toggleClass("error", !!isError).addClass("show");
        if (toastTimer) clearTimeout(toastTimer);
        toastTimer = setTimeout(function () { $t.removeClass("show"); }, 2200);
    }

    /* ── 시작 (외부 부트스트래퍼가 호출) ────────────────────
     * JSP 의 의존성 부트스트래퍼가 jQuery/Highcharts/지표모듈 로드 완료 후
     * window.recPickDynamicBootstrap() 을 호출한다.
     * ──────────────────────────────────────────────────── */
    window.recPickDynamicBootstrap = function () {
        if (typeof window.jQuery === "undefined") {
            console.error("[recPickDynamic] jQuery not loaded");
            return;
        }
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", init);
        } else {
            init();
        }
    };
})();
