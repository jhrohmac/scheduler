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
        // 1) 지표 메타 + 사용자 프리셋 + 관심그룹 목록 병렬 로드
        Promise.all([
            loadIndicatorMeta(),
            loadPresetFromServer(),
            loadWatchGroupList()
        ]).then(function () {
            // 2) 저장된 프리셋이 있으면 state 에 적용
            restorePresetIntoState();
            // 3) 시장 칩, 지표 패널 렌더
            renderMarketChips();
            renderIndicatorPanel();
            applyDefaults();
            // 4) 지표 패널 렌더 후, 저장된 파라미터값을 input 에 주입
            applySavedParamsToInputs();
            applyFilters();
        });
    }

    /* ── 사용자별 프리셋 저장/복원 (DB - TB_USER_FILTER_PRESET) ─────────────────── */

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

    /** DB 에 저장 (적용 시점에 호출, fire-and-forget) */
    function savePreset() {
        try {
            var snap = buildPresetSnapshot();
            snap._savedAt = new Date().toISOString();
            $.ajax({
                url: config.presetSaveUrl,
                method: "POST",
                contentType: "application/json",
                data: JSON.stringify(snap),
                error: function (xhr, status, err) {
                    console.warn("[recPickDynamic] preset save failed:", err);
                }
            });
        } catch (e) {
            console.warn("[recPickDynamic] preset save build failed:", e);
        }
    }

    /** DB 에서 비동기 로드 (init 단계에서 1회 호출) */
    function loadPresetFromServer() {
        return new Promise(function (resolve) {
            $.ajax({
                url: config.presetLoadUrl,
                method: "GET",
                dataType: "json",
                success: function (resp) {
                    var meta = resp && resp.singleData;
                    if (meta && meta.hasPreset && meta.filterJson) {
                        try {
                            _restoredPreset = JSON.parse(meta.filterJson);
                        } catch (e) {
                            console.warn("[recPickDynamic] preset parse failed:", e);
                            _restoredPreset = null;
                        }
                    }
                    resolve();
                },
                error: function () {
                    // 프리셋 조회 실패해도 화면은 정상 표시
                    resolve();
                }
            });
        });
    }

    var _restoredPreset = null;

    /** state 기본값을 저장된 프리셋으로 덮어쓰기 (지표 카드 렌더 전에 호출) */
    function restorePresetIntoState() {
        var p = _restoredPreset;
        if (!p) return;
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

    /* ── 관심그룹 목록 로드 + 마지막 선택 복원 ───────────── */
    var _watchGroupList = [];

    function watchGroupLastUsedKey() {
        return "rpd.lastWatchGroupId." + (config.userId || "anonymous");
    }

    function loadWatchGroupList() {
        return new Promise(function (resolve) {
            $.ajax({
                url: config.watchGroupListUrl,
                method: "GET",
                dataType: "json",
                success: function (resp) {
                    var list = (resp && resp.data) || [];
                    _watchGroupList = list;
                    renderWatchGroupSelect();
                    resolve();
                },
                error: function () {
                    _watchGroupList = [];
                    renderWatchGroupSelect();
                    resolve();
                }
            });
        });
    }

    function renderWatchGroupSelect() {
        var $sel = $("#rpdWatchGroupSelect");
        if (!$sel.length) return;
        $sel.empty();
        if (!_watchGroupList || _watchGroupList.length === 0) {
            $sel.append('<option value="">(등록된 관심그룹 없음 — 관심종목 화면에서 그룹 먼저 생성)</option>');
            $sel.prop("disabled", true);
            return;
        }
        $sel.prop("disabled", false);
        _watchGroupList.forEach(function (g) {
            var id = g.GROUP_ID || g.groupId;
            var name = g.GROUP_NAME || g.groupName;
            var market = g.GROUP_MARKET || g.groupMarket || "";
            var cnt = g.STOCK_COUNT != null ? g.STOCK_COUNT : (g.stockCount != null ? g.stockCount : 0);
            var marketLabel = market === "N" ? "[국내] " : (market === "A" ? "[해외] " : "");
            $sel.append('<option value="' + escapeHtml(id) + '">' +
                marketLabel + escapeHtml(name) + ' (' + cnt + ')</option>');
        });
        // 마지막 선택 복원
        try {
            var last = localStorage.getItem(watchGroupLastUsedKey());
            if (last) $sel.val(last);
            if (!$sel.val() && _watchGroupList.length > 0) {
                $sel.val(_watchGroupList[0].GROUP_ID || _watchGroupList[0].groupId);
            }
        } catch (e) {}
    }

    function currentWatchGroupId() {
        return $("#rpdWatchGroupSelect").val() || "";
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
    // doubleChartSync.js 의 nextDoubleChartMode 순서 그대로:
    //   recent → all → off → recent → ...
    var DOUBLE_MODE_NEXT = { recent: 'all', all: 'off', off: 'recent' };
    // 모드 → 라벨 (doubleChartSync.js doubleChartModeLabel 동일)
    var DOUBLE_MODE_LABEL = { recent: 'RE', all: 'ALL', off: 'OFF' };

    var chartOpts = {
        // 이미지의 색상과 일치 (검정/빨강/초록/파랑/마젠타)
        maList: [
            { period:   5, color: '#000000', enabled: true,  lineWidth: 1.5 },
            { period:  20, color: '#dc2626', enabled: true,  lineWidth: 1.5 },
            { period:  60, color: '#16a34a', enabled: true,  lineWidth: 1.5 },
            { period: 120, color: '#3b82f6', enabled: true,  lineWidth: 1.5 },
            { period: 240, color: '#ec4899', enabled: true,  lineWidth: 1.5 }
        ],
        showVolume: true,
        showCandle: true,
        showHighLow: true,
        doubleChartMode: 'all'   // 'off' | 'recent' | 'all'
    };

    /** 현재 priceList 캐시 (토글 변경 시 재렌더용) */
    var _chartPriceList = [];
    var _chartStock = null;

    /** 월봉 경계 시간 캐시 (xAxis afterSetExtremes 에서 SVG path 재구성용) */
    var _monthBoundaryTimes = [];

    /** 월봉 박스 폭 재계산 — 무한루프 방지 flag 적용 */
    var _applyingMonthWidth = false;
    function applyMonthOverlayWidth(chart) {
        if (_applyingMonthWidth) return;
        if (!chart || !chart.xAxis || !chart.xAxis[0]) return;
        if (chartOpts.doubleChartMode === 'off') return;
        if (typeof DoubleMonthChartScript === "undefined") return;
        if (!_monthBoundaryTimes || _monthBoundaryTimes.length < 2) return;
        _applyingMonthWidth = true;
        try {
            DoubleMonthChartScript.updateMonthOverlayPointWidth(
                chart,
                _monthBoundaryTimes,
                chart.xAxis[0],
                { doubleChartEnabled: true },
                "D"
            );
        } catch (e) {
            console.warn("[recPickDynamic] applyMonthOverlayWidth:", e);
        } finally {
            _applyingMonthWidth = false;
        }
    }

    /** 더블차트 버튼의 모드/dot/라벨 갱신 (doubleChartSync.js applyMode 패턴) */
    function updateDoubleChartButton(mode) {
        var $btn = $("#rpdDoubleChartBtn");
        if (!$btn.length) return;
        var label = DOUBLE_MODE_LABEL[mode] || "OFF";
        $btn.attr("data-double-mode", mode)
            .attr("title", "더블차트 " + label);
        $btn.find(".double-chart-btn-mode").text(label);
        // dot 매핑 (kisFinance 패턴): recent=1번째(왼), all=2번째(중), off=3번째(오)
        var activeIdx = mode === 'recent' ? 0 : (mode === 'all' ? 1 : 2);
        $btn.find(".double-chart-dot").each(function (i) {
            $(this).toggleClass("is-active", i === activeIdx);
        });
        $btn.toggleClass("is-active", mode !== 'off');
    }

    function renderChart(stock, priceList) {
        if (typeof Highcharts === "undefined") {
            $("#rpdChartContainer").text("Highcharts 미로드");
            return;
        }
        _chartStock = stock;
        _chartPriceList = priceList || [];

        // OHLC + 종가 + 거래량 시계열 빌드
        // 거래량 색상: 상승일=빨강, 하락일=파랑 (캔들 색상과 동일 톤)
        var VOL_UP_COLOR   = "rgba(255, 99, 99, 0.85)";   // 상승 — 옅은 빨강
        var VOL_DOWN_COLOR = "rgba(99, 174, 255, 0.85)";  // 하락 — 옅은 파랑
        var closeData = [], ohlcData = [], volumeData = [];
        priceList.forEach(function (p) {
            if (!p.tradeDt || p.close == null) return;
            var ts = parseDateUtc(p.tradeDt);
            closeData.push([ts, p.close]);
            if (p.open != null && p.high != null && p.low != null) {
                ohlcData.push([ts, p.open, p.high, p.low, p.close]);
            }
            if (p.volume != null) {
                // open 이 없으면 close 만으로 판단 불가 → 기본 회색
                var isUp = (p.open != null) ? (p.close >= p.open) : true;
                volumeData.push({
                    x: ts,
                    y: Number(p.volume),
                    color: isUp ? VOL_UP_COLOR : VOL_DOWN_COLOR
                });
            }
        });

        // MA 시리즈 동적 빌드
        var maSeries = [];
        chartOpts.maList.forEach(function (cfg) {
            if (!cfg.enabled) return;
            var maData = [];
            var arr = [];
            priceList.forEach(function (p) {
                if (!p.tradeDt || p.close == null) return;
                arr.push(p.close);
                var ts = parseDateUtc(p.tradeDt);
                maData.push([ts, sma(arr, cfg.period)]);
            });
            maSeries.push({
                name: 'MA' + cfg.period, type: 'line', data: maData,
                color: cfg.color, lineWidth: cfg.lineWidth || 1.5,
                yAxis: 0, marker: { enabled: false }, zIndex: 5,
                dataGrouping: { enabled: false }
            });
        });

        // 메인 시리즈 (캔들 또는 종가 라인) — kisFinance 색상 (red↑ / #3496ff↓)
        var mainSeries = chartOpts.showCandle && ohlcData.length > 0 ? {
            type: 'candlestick',
            name: '가격',
            id: 'price',
            data: ohlcData,
            color: '#3496ff', upColor: 'red',
            lineColor: '#3496ff', upLineColor: 'red',
            yAxis: 0, zIndex: 3,
            dataGrouping: { enabled: false }
        } : {
            type: 'line',
            name: '종가',
            id: 'price',
            data: closeData,
            color: '#374151', lineWidth: 1.5,
            yAxis: 0, marker: { enabled: false }, zIndex: 3,
            dataGrouping: { enabled: false }
        };

        var allSeries = [mainSeries];

        // 더블차트 — kisFinance/doubleMonthChartScript.js 그대로 재사용
        _monthBoundaryTimes = [];
        if (chartOpts.doubleChartMode !== 'off' && ohlcData.length > 0
                && typeof DoubleMonthChartScript !== "undefined") {
            var monthlyInfo = DoubleMonthChartScript.buildMonthlyOverlayFromDaily(ohlcData);
            var monthOverlay = monthlyInfo.overlay || [];
            _monthBoundaryTimes = monthlyInfo.boundaries || [];
            if (chartOpts.doubleChartMode === 'recent' && monthOverlay.length > 0) {
                monthOverlay = [monthOverlay[monthOverlay.length - 1]];
            }
            if (monthOverlay.length > 0) {
                // kisFinance 월봉 색상 — 분홍↑ / 하늘↓ 반투명
                allSeries.push({
                    type: 'candlestick',
                    name: '월봉',
                    id: 'monthOverlay',
                    linkedTo: 'price',
                    data: monthOverlay,
                    color:        '#3498db6e',
                    lineColor:    '#3498db6e',
                    upColor:      '#e83e8c6e',
                    upLineColor:  '#e83e8c6e',
                    yAxis: 0, zIndex: 1,
                    pointWidth: null,
                    lineWidth: 2,
                    dataGrouping: { enabled: false }
                });
            }
        }

        allSeries = allSeries.concat(maSeries);

        if (chartOpts.showVolume) {
            // 각 point 가 자체 color 를 가지고 있어 series.color 는 fallback 만 지정
            allSeries.push({
                type: 'column', name: '거래량', id: 'volume', data: volumeData,
                color: '#94a3b8', yAxis: 1, zIndex: 2,
                dataGrouping: { enabled: false },
                borderWidth: 0
            });
        }

        // 전고/전저점 수평선 — yAxis plotLines
        var plotLines = [];
        if (chartOpts.showHighLow && closeData.length > 0) {
            var maxV = -Infinity, minV = Infinity, maxRow = null, minRow = null;
            ohlcData.forEach(function (r) {
                if (r[2] > maxV) { maxV = r[2]; maxRow = r; }
                if (r[3] < minV) { minV = r[3]; minRow = r; }
            });
            if (maxRow) {
                plotLines.push({
                    value: maxV, color: '#dc2626', dashStyle: 'Dash', width: 1, zIndex: 4,
                    label: { text: '최고 ' + Math.round(maxV).toLocaleString(),
                             align: 'right', x: -10, y: -4,
                             style: { color: '#dc2626', fontSize: '10px', fontWeight: 'bold' } }
                });
            }
            if (minRow) {
                plotLines.push({
                    value: minV, color: '#2563eb', dashStyle: 'Dash', width: 1, zIndex: 4,
                    label: { text: '최저 ' + Math.round(minV).toLocaleString(),
                             align: 'right', x: -10, y: 12,
                             style: { color: '#2563eb', fontSize: '10px', fontWeight: 'bold' } }
                });
            }
        }

        // yAxis 구성 — 가격축은 천단위 콤마 포맷 + crosshair 라벨 (kisFinance 패턴)
        var priceLabelFormatter = function () {
            return Number(this.value).toLocaleString();
        };
        // 가격축 crosshair (수평 점선 + "99,xxx" 라벨 박스)
        var priceCrosshair = {
            width: 1,
            color: "rgba(0,0,0,0.35)",
            dashStyle: "Dash",
            snap: false,
            label: {
                enabled: true,
                format: "{value:,.0f}",
                padding: 4,
                backgroundColor: "rgba(255,255,255,0.92)",
                borderColor: "rgba(0,0,0,0.25)",
                style: { color: "#000", fontWeight: "700", fontSize: "10px" }
            }
        };
        var yAxisCfg;
        if (chartOpts.showVolume) {
            yAxisCfg = [
                { labels: { align: 'right', x: -3, style:{fontSize:'10px'},
                            formatter: priceLabelFormatter },
                  height: '70%', resize: { enabled: true }, lineWidth: 1, title: { text: null },
                  plotLines: plotLines, crosshair: priceCrosshair },
                { labels: { align: 'right', x: -3, style:{fontSize:'9px'},
                            formatter: function () {
                                if (this.value >= 1e8) return Math.round(this.value/1e8) + '억';
                                if (this.value >= 1e4) return Math.round(this.value/1e4) + '만';
                                return Number(this.value).toLocaleString();
                            } },
                  top: '72%', height: '28%', offset: 0, lineWidth: 1, title: { text: null } }
            ];
        } else {
            yAxisCfg = { labels: { align: 'right', x: -3, style:{fontSize:'10px'},
                                   formatter: priceLabelFormatter },
                         lineWidth: 1, title: { text: null }, plotLines: plotLines, crosshair: priceCrosshair };
        }

        // Highcharts Stock (rangeSelector + navigator + lastPrice 자동)
        var useStock = typeof Highcharts.stockChart === "function";
        var ChartCtor = useStock ? Highcharts.stockChart : Highcharts.chart;

        // 종가 시리즈에 lastPrice 라벨 추가 — 천단위 포맷 (Highcharts Stock)
        if (useStock) {
            mainSeries.lastPrice = { enabled: false };
            mainSeries.lastVisiblePrice = {
                enabled: true,
                label: {
                    enabled: true,
                    backgroundColor: '#dc2626',
                    style: { color: '#fff', fontWeight: 'bold' },
                    formatter: function () {
                        return Number(this.y).toLocaleString();
                    }
                }
            };
        }

        ChartCtor('rpdChartContainer', {
            chart: {
                spacing: [10, 10, 8, 8],
                events: {
                    load: function () {
                        // 1) 정보바 — 마지막 캔들 기준 초기 표시
                        var pts = (this.series[0] && this.series[0].points) || [];
                        if (pts.length) {
                            updateChartInfoBar(pts[pts.length - 1].options || pts[pts.length - 1]);
                        }
                        // 2) 월봉 박스 width — 초기 렌더 직후 1회만 재계산
                        var self = this;
                        setTimeout(function () { applyMonthOverlayWidth(self); }, 0);
                    }
                    // ※ redraw 이벤트는 의도적으로 제거 — updateMonthOverlayPointWidth 가
                    //    SVG attr 를 변경하면 redraw 트리거 → 무한 루프/박스 잔상 위험.
                    //    줌/스크롤은 xAxis.events.afterSetExtremes 에서 처리.
                    //    시리즈 토글 시는 renderChart() 가 차트를 새로 만들어 load 이벤트로 재호출됨.
                }
            },
            title: { text: null },
            legend: { enabled: false },
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
            navigator: useStock ? { enabled: false } : undefined,
            scrollbar: useStock ? { enabled: false } : undefined,
            xAxis: {
                type: 'datetime',
                labels: { style: { fontSize: '10px' } },
                // kisFinance 패턴 — 검정 점선 crosshair (마우스 위치 수직선)
                crosshair: { width: 1, color: "black", dashStyle: "Dash" },
                events: {
                    afterSetExtremes: function () {
                        // 줌/스크롤 변경 시 월봉 박스 폭을 가시 영역에 맞게 재계산
                        applyMonthOverlayWidth(this.chart);
                    }
                }
            },
            yAxis: yAxisCfg,
            // 사용자 요청: 차트 hover 툴팁(큰 박스) 제거 → 차트 위 정보바로 대체
            tooltip: { enabled: false },
            plotOptions: {
                series: {
                    dataGrouping: { enabled: false },
                    // kisFinance 패턴: 다른 시리즈 hover 시 dim 효과만 끄고 crosshair는 유지
                    states: { inactive: { enabled: false } },
                    point: {
                        events: {
                            mouseOver: function () {
                                // hover 시 그 지점의 OHLC 를 정보바에 반영
                                updateChartInfoBar(this);
                            }
                        }
                    }
                },
                candlestick: {
                    color: '#3496ff', upColor: 'red',
                    lineColor: '#3496ff', upLineColor: 'red',
                    dataGrouping: { enabled: false }
                },
                column: { dataGrouping: { enabled: false } },
                line: { dataGrouping: { enabled: false } }
            },
            credits: { enabled: false },
            series: allSeries
        });

        // 초기 정보바 갱신 — 마지막 캔들 기준
        updateChartInfoBar(null);
    }

    /* ── 차트 위 정보바 갱신 (이미지 #3) ───────────────────── */
    function updateChartInfoBar(pointOrOptions) {
        var p = null;
        // pointOrOptions: Highcharts Point 또는 OHLC array
        if (pointOrOptions && pointOrOptions.x !== undefined) {
            p = { ts: pointOrOptions.x,
                  open:  pointOrOptions.open,
                  high:  pointOrOptions.high,
                  low:   pointOrOptions.low,
                  close: pointOrOptions.close != null ? pointOrOptions.close : pointOrOptions.y };
        } else if (Array.isArray(pointOrOptions)) {
            p = { ts: pointOrOptions[0], open: pointOrOptions[1], high: pointOrOptions[2],
                  low: pointOrOptions[3], close: pointOrOptions[4] };
        } else {
            // 마지막 데이터로 fallback
            var pl = _chartPriceList;
            if (pl && pl.length) {
                var last = pl[pl.length - 1];
                p = { ts: parseDateUtc(last.tradeDt),
                      open: last.open, high: last.high, low: last.low, close: last.close };
            }
        }
        if (!p) return;

        var d = new Date(p.ts);
        var dow = ['일','월','화','수','목','금','토'][d.getUTCDay()];
        var dt = d.getUTCFullYear() + '.' +
                 ('0'+(d.getUTCMonth()+1)).slice(-2) + '.' +
                 ('0'+d.getUTCDate()).slice(-2) + '(' + dow + ')';
        var fmt = function (v) { return v != null ? Number(v).toLocaleString() : '—'; };

        var $bar = $("#rpdChartInfoBar");
        if (!$bar.length) return;
        var ohlcHtml = '<span class="rpd-info-date">' + dt + '</span>' +
            '<span class="rpd-info-ohlc">시 <b>' + fmt(p.open)  + '</b></span>' +
            '<span class="rpd-info-ohlc">고 <b style="color:#dc2626;">' + fmt(p.high) + '</b></span>' +
            '<span class="rpd-info-ohlc">저 <b style="color:#2563eb;">' + fmt(p.low)  + '</b></span>' +
            '<span class="rpd-info-ohlc">종 <b>' + fmt(p.close) + '</b></span>';
        $bar.find(".rpd-info-left").html(ohlcHtml);

        // MA 색상별 period 라벨 (활성화된 것만, 마지막 MA 값과 함께)
        var pl = _chartPriceList;
        var maHtml = '';
        var closes = [];
        if (pl) pl.forEach(function (row) { if (row.close != null) closes.push(row.close); });
        chartOpts.maList.forEach(function (cfg) {
            if (!cfg.enabled) return;
            var v = sma(closes, cfg.period);
            maHtml += '<span class="rpd-info-ma" style="color:' + cfg.color + ';">' +
                      cfg.period + (v != null ? ' <small>' + Math.round(v).toLocaleString() + '</small>' : '') +
                      '</span>';
        });
        $bar.find(".rpd-info-right").html(maHtml);
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
        var groupId = currentWatchGroupId();
        if (!groupId) {
            onErr("관심그룹을 선택하세요. (등록된 그룹이 없으면 관심종목 화면에서 먼저 그룹을 만드세요)");
            return;
        }
        // 마지막 선택 그룹 기억
        try { localStorage.setItem(watchGroupLastUsedKey(), groupId); } catch (e) {}

        $.ajax({
            url: config.saveToWatchlistUrl,
            method: "POST",
            dataType: "json",
            data: {
                baseDt: stock.baseDt,
                mktCd: stock.mktCd,
                stkCd: stock.stkCd,
                watchGroupId: groupId,
                watchGroupDiv: "recommend"
            },
            success: function (resp) {
                var ok = resp && (
                    resp.result_code === "S001" ||
                    resp.result_code === "0000" ||
                    resp.system_code === "0000" ||
                    !resp.result_code
                );
                if (ok) onOk();
                else onErr(resp && resp.result_msg ? resp.result_msg : "알 수 없는 오류");
            },
            error: function (xhr, st, err) {
                var msg = err;
                if (xhr && xhr.responseText) {
                    try {
                        var j = JSON.parse(xhr.responseText);
                        msg = j.system_msg || j.result_msg || err;
                    } catch (e) {}
                }
                onErr(msg);
            }
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

        // 이동평균선 토글 — checkbox 변경 시
        $(document).on("change", ".rpd-ma-row input[type='checkbox']", function () {
            var $row = $(this).closest(".rpd-ma-row");
            var period = parseInt($row.data("period"), 10);
            var cfg = chartOpts.maList.find(function (m) { return m.period === period; });
            if (cfg) {
                cfg.enabled = $(this).prop("checked");
                if (_chartStock) renderChart(_chartStock, _chartPriceList);
            }
        });
        // 이동평균선 기간 변경
        $(document).on("change", ".rpd-ma-row .rpd-ma-period", function () {
            var $row = $(this).closest(".rpd-ma-row");
            var oldPeriod = parseInt($row.data("period"), 10);
            var newPeriod = parseInt($(this).val(), 10);
            if (!newPeriod || newPeriod < 1) return;
            var cfg = chartOpts.maList.find(function (m) { return m.period === oldPeriod; });
            if (cfg) {
                cfg.period = newPeriod;
                $row.attr("data-period", newPeriod).data("period", newPeriod);
                if (_chartStock) renderChart(_chartStock, _chartPriceList);
            }
        });
        // 전고/전저점 토글
        $(document).on("change", "#rpdToggleHighLow", function () {
            chartOpts.showHighLow = $(this).prop("checked");
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
        // 더블차트 3-dot 버튼 — doubleChartSync.js 와 동일 순환:
        //   recent → all → off → recent → ...
        $(document).on("click", "#rpdDoubleChartBtn", function () {
            var cur = chartOpts.doubleChartMode || 'off';
            var next = DOUBLE_MODE_NEXT[cur] || 'recent';
            chartOpts.doubleChartMode = next;
            updateDoubleChartButton(next);
            if (_chartStock) renderChart(_chartStock, _chartPriceList);
        });

        // 차트 옵션 아코디언 토글 (default 접힘)
        $(document).on("click", "#rpdChartOptionsHeader", function () {
            var $h = $(this);
            var $body = $h.next(".rpd-accordion-body");
            var open = !$h.hasClass("is-open");
            $h.toggleClass("is-open", open);
            $body.slideToggle(180);
        });

        // 관심그룹 select — 변경 시 마지막 선택 저장
        $(document).on("change", "#rpdWatchGroupSelect", function () {
            var v = $(this).val();
            if (v) {
                try { localStorage.setItem(watchGroupLastUsedKey(), v); } catch (e) {}
            }
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
        // 서버 프리셋 삭제 (fire-and-forget)
        $.ajax({
            url: config.presetDeleteUrl,
            method: "POST",
            error: function () {} // 실패해도 화면 초기화는 진행
        });
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
