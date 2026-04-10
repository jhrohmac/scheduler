/**
 * kisFinance 우측 보유종목 패널과 시장 연동만 담당한다.
 * 추천신호 목록 렌더링은 recSignalPanel.js에서 처리한다.
 */
(function () {
    "use strict";
    // WF-1-3: 보유종목 패널 / 시장 칩 동기화

    function $(sel) { return document.querySelector(sel); }
    function $all(sel) { return Array.prototype.slice.call(document.querySelectorAll(sel)); }

    function safeStr(v) {
        if (v === null || v === undefined) return "";
        return String(v);
    }

    function normalizeStockCode(code, country) {
        var s = safeStr(code).trim();
        if (!s) return "";
        // KR 종목코드는 6자리로 보정 (숫자만인 경우)
        var isDigits = /^[0-9]+$/.test(s);
        var ctry = safeStr(country).toUpperCase();
        if (isDigits && (ctry === "" || ctry === "KR") && s.length < 6) {
            s = ("000000" + s).slice(-6);
        }
        return s;
    }

    function pick(obj, keys) {
        if (!obj) return "";
        for (var i = 0; i < keys.length; i++) {
            var k = keys[i];
            if (obj[k] !== undefined && obj[k] !== null && safeStr(obj[k]) !== "") return obj[k];
            var uk = k.toUpperCase();
            if (obj[uk] !== undefined && obj[uk] !== null && safeStr(obj[uk]) !== "") return obj[uk];
        }
        return "";
    }

    function nfmt(v) {
        var s = safeStr(v).replace(/,/g, "");
        if (!s) return "-";
        var num = Number(s);
        if (isNaN(num)) return safeStr(v);
        return num.toLocaleString();
    }

    function domMktLabel(v) {
        v = safeStr(v).toUpperCase();
        if (v === "STK" || v === "KOSPI") return "KOSPI";
        if (v === "KSQ" || v === "KOSDAQ") return "KOSDAQ";
        return v || "-";
    }

    function normalizeResponse(resp) {
        // ResponseHandler 포맷 흡수
        var code = safeStr(resp && (resp.result_code || resp.resultCode || resp.RESULT_CODE || resp.system_code || resp.systemCode || resp.RESULTCODE));
        var ok = (code === "0000" || code === "0" || code === "1" || code === "");
        var data = null;

        if (resp && resp.data) data = resp.data;
        if (resp && resp.result && resp.result.data) data = resp.result.data;
        if (resp && resp.resultVo && resp.resultVo.data) data = resp.resultVo.data;

        if (resp && resp.data && Array.isArray(resp.data)) data = resp.data;
        if (resp && Array.isArray(resp)) data = resp;

        if (!data && resp && resp.result_msg && !ok) {
            return { ok: false, msg: resp.result_msg, list: [] };
        }

        return { ok: ok, msg: safeStr(resp && (resp.result_msg || resp.resultMsg || resp.RESULT_MSG || resp.system_msg || resp.systemMsg)), list: (data && data.data ? data.data : data) || [] };
    }

    var holdingSelectedCode = "";
    var holdingSelectedGroup = "";
    var holdingSelectedData = null;
    var holdingLoaded = false;
    var holdingLoading = false;
    var holdingLastMarket = "";
    var holdingLastList = [];
    var holdingActionBusy = false;
    var holdingEventFilter = "all";
    var holdingEventCache = [];


    function getHoldingField(row, name) {
        var map = {
            stock_code: ["stock_code", "stockCode", "code", "pdno", "STOCK_CODE"],
            stock_ko_name: ["stock_ko_name", "stockKoName", "name", "STOCK_KO_NAME"],
            stock_en_name: ["stock_en_name", "stockEnName", "name", "STOCK_EN_NAME"],
            stock_market: ["stock_market", "stockMarket", "market", "STOCK_MARKET"],
            stock_country_code: ["stock_country_code", "stockCountryCode", "country", "STOCK_COUNTRY_CODE"],
            stock_close: ["stock_close", "stockClose", "close", "STOCK_CLOSE"],
            price_difference: ["priceDifference", "price_difference", "PRICE_DIFFERENCE"],
            price_percentage: ["pricePercentage", "price_percentage", "PRICE_PERCENTAGE"],
            total_quantity: ["total_quantity", "totalQuantity", "TOTAL_QUANTITY"],
            avg_purchase_price: ["avg_purchase_price", "avgPurchasePrice", "AVG_PURCHASE_PRICE"],
            avg_price_percentage: ["avg_pricePercentage", "avg_price_percentage", "AVG_PRICE_PERCENTAGE"],
            stock_price_roi: ["stock_price_roi", "stockPriceRoi", "STOCK_PRICE_ROI"],
            total_purchase_price: ["total_purchase_price", "totalPurchasePrice", "TOTAL_PURCHASE_PRICE"]
        };
        return pick(row, map[name] || [name]);
    }

    function toNumber(v) {
        var s = safeStr(v);
        if (!s) return NaN;
        s = s.replace(/,/g, "").replace(/%/g, "");
        var n = Number(s);
        return isNaN(n) ? NaN : n;
    }

    function signedNum(v) {
        var n = toNumber(v);
        if (isNaN(n)) return "-";
        var sign = n > 0 ? "+" : (n < 0 ? "-" : "");
        return sign + nfmt(Math.abs(n));
    }

    function signedPct(v) {
        var n = toNumber(v);
        if (isNaN(n)) return "-";
        var sign = n > 0 ? "+" : (n < 0 ? "-" : "");
        return sign + Math.abs(n).toFixed(2) + "%";
    }

    function holdingMarketLabel(market) {
        return (market === "A") ? "해외" : "국내";
    }

    function getHoldingMarket() {
        var wl = document.getElementById("wlMarket");
        var m = wl ? wl.value : "";
        if (!m) m = "N";
        return m;
    }

    function isHoldingTabActive() {
        var tab = document.querySelector(".rt-tab.is-active");
        return tab && tab.getAttribute("data-tab") === "holding";
    }

    function setHoldingMeta(market, count) {
        var asOf = $("#holdingAsOf");
        if (asOf) {
            var lbl = holdingMarketLabel(market);
            asOf.textContent = lbl ? (lbl + " 보유") : "";
        }
        var cnt = $("#holdingCount");
        if (cnt) {
            if (count == null) cnt.textContent = "";
            else cnt.textContent = "총 " + count + "건";
        }
    }

    function setHoldingError(msg) {
        var el = $("#holdingError");
        if (!el) return;
        if (!msg) {
            el.style.display = "none";
            el.textContent = "";
            return;
        }
        el.style.display = "block";
        el.textContent = msg;
    }

    function renderHoldingList(list) {
        var wrap = $("#holdingList");
        if (!wrap) return;

        if (!Array.isArray(list) || list.length === 0) {
            holdingSelectedData = null;
            setHoldingAvgPreview("");
            wrap.innerHTML = '<div class="holding-empty">보유 종목이 없습니다.</div>';
            return;
        }

        var html = "";
        for (var i = 0; i < list.length; i++) {
            var r = list[i] || {};
            var rawCode = safeStr(getHoldingField(r, "stock_code"));
            var name = safeStr(getHoldingField(r, "stock_ko_name")) || safeStr(getHoldingField(r, "stock_en_name"));
            var mkt = safeStr(getHoldingField(r, "stock_market"));
            var ctry = safeStr(getHoldingField(r, "stock_country_code"));
            var code = normalizeStockCode(rawCode, ctry);
            var groupId = safeStr(getHoldingField(r, "stock_group"));

            var totalQty = safeStr(getHoldingField(r, "total_quantity"));
            var close = safeStr(getHoldingField(r, "stock_close"));
            var avgPrice = safeStr(getHoldingField(r, "avg_purchase_price"));
            var totalQtyNum = toNumber(totalQty);
            var closeNum = toNumber(close);
            var avgPriceNum = toNumber(avgPrice);
            var diff = safeStr(getHoldingField(r, "price_difference"));
            var diffPct = safeStr(getHoldingField(r, "price_percentage"));
            var roi = safeStr(getHoldingField(r, "stock_price_roi"));
            var roiPct = safeStr(getHoldingField(r, "avg_price_percentage"));

            var diffNum = toNumber(diff);
            var roiNum = toNumber(roi);

            var diffClass = diffNum > 0 ? "up" : (diffNum < 0 ? "down" : "flat");
            var roiClass = roiNum > 0 ? "up" : (roiNum < 0 ? "down" : "flat");

            var metaText = (code || "-") + (mkt ? (" · " + domMktLabel(mkt)) : "");
            var qtyText = totalQty ? ("보유 " + nfmt(totalQty) + "주") : "보유 -";
            var priceText = close ? ("현재 " + nfmt(close)) : "현재 -";
            var diffText = (!isNaN(diffNum) ? (signedNum(diff) + " (" + signedPct(diffPct) + ")") : "");
            var avgText = avgPrice ? ("평균 " + nfmt(avgPrice)) : "평균 -";
            var roiText = (!isNaN(roiNum) ? ("손익 " + signedNum(roi) + " (" + signedPct(roiPct) + ")") : "");

            html += ''
                + '<div class="holding-item" data-code="' + code + '" data-group="' + groupId + '" data-country="' + ctry + '" data-market="' + mkt + '" data-qty="' + (isNaN(totalQtyNum)?0:totalQtyNum) + '" data-avg="' + (isNaN(avgPriceNum)?0:avgPriceNum) + '" data-close="' + (isNaN(closeNum)?0:closeNum) + '">'
                + '  <div class="holding-top">'
                + '    <div class="holding-name-wrap">'
                + '      <div class="holding-name">' + (name || "-") + '</div>'
                + '      <div class="holding-code">' + metaText + '</div>'
                + '    </div>'
                + '    <span class="holding-qty">' + qtyText + '</span>'
                + '  </div>'
                + '  <div class="holding-mid">'
                + '    <span class="holding-price">' + priceText + '</span>'
                + (diffText ? '<span class="holding-change ' + diffClass + '">' + diffText + '</span>' : '')
                + '  </div>'
                + '  <div class="holding-sub">'
                + '    <span class="holding-avg">' + avgText + '</span>'
                + (roiText ? '<span class="holding-roi ' + roiClass + '">' + roiText + '</span>' : '')
                + '  </div>'
                + '</div>';
        }

        wrap.innerHTML = html;

        $all(".holding-item").forEach(function (row) {
            row.addEventListener("click", function () {
                var code = this.getAttribute("data-code");
                if (!code) return;
                holdingSelectedCode = code;
                holdingSelectedGroup = safeStr(this.getAttribute("data-group"));
                holdingSelectedData = {
                    totalQty: Number(this.getAttribute("data-qty") || 0),
                    avgPrice: Number(this.getAttribute("data-avg") || 0),
                    closePrice: Number(this.getAttribute("data-close") || 0),
                    country: safeStr(this.getAttribute("data-country")),
                    market: safeStr(this.getAttribute("data-market"))
                };
                applyHoldingPriceInputFormat(holdingSelectedData.country);
                var pEl = document.getElementById("holdingPrice");
                if (pEl && holdingSelectedData.closePrice > 0) {
                    pEl.value = formatPriceByCountry(holdingSelectedData.closePrice, holdingSelectedData.country);
                }
                syncMarketByCountry(holdingSelectedData.country);
                highlightHoldingSelected();
                updateHoldingAvgPreview();
                loadHoldingEventHint();
            });
            row.addEventListener("dblclick", function () {
                var code = this.getAttribute("data-code");
                if (!code) return;
                holdingSelectedCode = code;
                holdingSelectedGroup = safeStr(this.getAttribute("data-group"));
                holdingSelectedData = {
                    totalQty: Number(this.getAttribute("data-qty") || 0),
                    avgPrice: Number(this.getAttribute("data-avg") || 0),
                    closePrice: Number(this.getAttribute("data-close") || 0),
                    country: safeStr(this.getAttribute("data-country")),
                    market: safeStr(this.getAttribute("data-market"))
                };
                applyHoldingPriceInputFormat(holdingSelectedData.country);
                var pEl = document.getElementById("holdingPrice");
                if (pEl && holdingSelectedData.closePrice > 0) {
                    pEl.value = formatPriceByCountry(holdingSelectedData.closePrice, holdingSelectedData.country);
                }
                syncMarketByCountry(holdingSelectedData.country);
                highlightHoldingSelected();
                updateHoldingAvgPreview();
                loadHoldingEventHint();
                applyHoldingSelection(code);
            });
        });

        highlightHoldingSelected();
        loadHoldingEventHint();
    }

    function syncMarketByCountry(country) {
        var market = isUsCountry(country) ? "A" : "N";
        var wl = document.getElementById("wlMarket");
        if (wl) wl.value = market;
        setActiveSignalMarket(market);
        if (typeof window.refreshRecSignalPanel === "function") {
            window.refreshRecSignalPanel(false);
        }
    }

    function highlightHoldingSelected() {
        $all(".holding-item").forEach(function (row) {
            var code = row.getAttribute("data-code");
            if (code && holdingSelectedCode === code) row.classList.add("is-selected");
            else row.classList.remove("is-selected");
        });
    }

    function setHoldingEventHint(msg) {
        var el = document.getElementById("holdingEventHint");
        if (!el) return;
        if (!msg) {
            el.style.display = "none";
            el.textContent = "";
            return;
        }
        el.style.display = "block";
        el.textContent = msg;
    }

    function setHoldingAvgPreview(msg, tone, htmlMode) {
        var el = document.getElementById("holdingAvgPreview");
        if (!el) return;
        el.classList.remove("is-up", "is-down", "is-flat");
        if (!msg) {
            el.style.display = "none";
            el.textContent = "";
            return;
        }
        if (tone === "up") el.classList.add("is-up");
        else if (tone === "down") el.classList.add("is-down");
        else el.classList.add("is-flat");

        el.style.display = "block";
        if (htmlMode) el.innerHTML = msg;
        else el.textContent = msg;
    }

    function updateHoldingAvgPreview() {
        if (!holdingSelectedData) {
            setHoldingAvgPreview("");
            return;
        }

        var fv = readHoldingFormValues();
        if (fv.qty == null || fv.price == null) {
            setHoldingAvgPreview("");
            return;
        }

        var beforeQty = Number(holdingSelectedData.totalQty || 0);
        var beforeAvg = Number(holdingSelectedData.avgPrice || 0);
        var currentPrice = Number(holdingSelectedData.closePrice || fv.price || 0);

        if (beforeQty <= 0 || beforeAvg <= 0) {
            setHoldingAvgPreview("");
            return;
        }

        var afterQty = beforeQty + fv.qty;
        var afterAvg = ((beforeQty * beforeAvg) + (fv.qty * fv.price)) / afterQty;
        var beforePct = ((currentPrice - beforeAvg) / beforeAvg) * 100;
        var afterPct = ((currentPrice - afterAvg) / afterAvg) * 100;
        var improveAvg = beforeAvg - afterAvg;
        var improvePct = afterPct - beforePct;

        var tone = "flat";
        if (beforePct > 0) tone = "up";
        else if (beforePct < 0) tone = "down";

        var beforeCls = (beforePct > 0 ? "is-up" : (beforePct < 0 ? "is-down" : "is-flat"));
        var afterCls = (afterPct > 0 ? "is-up" : (afterPct < 0 ? "is-down" : "is-flat"));

        var ctry = String((holdingSelectedData && holdingSelectedData.country) ? holdingSelectedData.country : "").toUpperCase();
        var isUS = (ctry === "US");

        var beforeAvgDisp = isUS
            ? beforeAvg.toLocaleString(undefined, { minimumFractionDigits: 1, maximumFractionDigits: 1 })
            : nfmt(Math.round(beforeAvg));
        var afterAvgDisp = isUS
            ? afterAvg.toLocaleString(undefined, { minimumFractionDigits: 1, maximumFractionDigits: 1 })
            : nfmt(Math.round(afterAvg));
        var improveAvgDisp = isUS
            ? ((improveAvg > 0 ? "+" : (improveAvg < 0 ? "-" : "")) + Math.abs(improveAvg).toLocaleString(undefined, { minimumFractionDigits: 1, maximumFractionDigits: 1 }))
            : signedNum(Math.round(improveAvg));

        var beforePctDisp = signedPct(beforePct);
        var afterPctDisp = signedPct(afterPct);
        var improvePctDisp = signedPct(improvePct);

        var html = ''
            + '<div class="avg-grid">'
            + '  <div class="avg-col">'
            + '    <div class="avg-k">현재 평단/수익률</div>'
            + '    <div class="avg-v">' + beforeAvgDisp + '</div>'
            + '    <div class="avg-p ' + beforeCls + '">' + beforePctDisp + '</div>'
            + '  </div>'
            + '  <div class="avg-col">'
            + '    <div class="avg-k">예상 평단/수익률</div>'
            + '    <div class="avg-v">' + afterAvgDisp + '</div>'
            + '    <div class="avg-p ' + afterCls + '">' + afterPctDisp + '</div>'
            + '  </div>'
            + '</div>'
            + '<div class="avg-k" style="margin-top:4px;">평단 개선 ' + improveAvgDisp + ' · 수익률 변화 ' + improvePctDisp + '</div>';

        setHoldingAvgPreview(html, tone, true);
    }

    function eventTypeClass(t) {
        var x = safeStr(t).toUpperCase();
        if (x === "RISK_OFF") return "is-risk";
        if (x === "TREND_FOLLOW") return "is-trend";
        if (x === "AVERAGE_DOWN") return "is-avg";
        if (x === "EXIT") return "is-exit";
        return "is-enter";
    }

    function applyHoldingEventFilter(items) {
        if (!Array.isArray(items)) return [];
        if (holdingEventFilter === "risk") {
            return items.filter(function (e) {
                var t = safeStr((e || {}).EVENT_TYPE || (e || {}).event_type).toUpperCase();
                return t === "RISK_OFF";
            });
        }
        if (holdingEventFilter === "trend") {
            return items.filter(function (e) {
                var t = safeStr((e || {}).EVENT_TYPE || (e || {}).event_type).toUpperCase();
                return t === "TREND_FOLLOW" || t === "ENTER";
            });
        }
        return items;
    }

    function setHoldingEventList(items, visible) {
        var wrap = document.getElementById("holdingEventListWrap");
        var el = document.getElementById("holdingEventList");
        if (!el || !wrap) return;
        if (!visible) {
            wrap.style.display = "none";
            el.innerHTML = "";
            return;
        }

        var filtered = applyHoldingEventFilter(items);
        if (!Array.isArray(filtered) || filtered.length === 0) {
            wrap.style.display = "block";
            el.innerHTML = '<div class="holding-event-row">필터 조건의 이벤트가 없습니다.</div>';
            return;
        }

        var max = Math.min(filtered.length, 5);
        var html = "";
        for (var i = 0; i < max; i++) {
            var e = filtered[i] || {};
            var t = safeStr(e.EVENT_TYPE || e.event_type);
            var tm = safeStr(e.EVENT_TIME || e.event_time);
            var m = safeStr(e.MESSAGE || e.message);
            html += '<div class="holding-event-row"><span class="ev-type ' + eventTypeClass(t) + '">' + (t || '-') + '</span>'
                 + '<span class="ev-time">' + (tm || '-') + '</span><div>' + (m || '') + '</div></div>';
        }
        wrap.style.display = "block";
        el.innerHTML = html;
    }

    function loadHoldingEventHint(openList) {
        if (!window.jQuery) return;
        var groupId = getActiveGroupId();
        if (!groupId || !holdingSelectedCode) {
            setHoldingEventHint("");
            setHoldingEventList([], false);
            return;
        }

        var url = (window.__URLS && window.__URLS.selectPositionEventList)
            ? window.__URLS.selectPositionEventList
            : "/finance/selectPositionEventList.do";

        window.jQuery.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            data: { group_id: groupId, stock_code: holdingSelectedCode, limit: 20 },
            timeout: 20000
        }).done(function (resp) {
            var norm = normalizeResponse(resp);
            if (!norm.ok) {
                setHoldingEventHint("");
                setHoldingEventList([], false);
                return;
            }
            var list = norm.list;
            if (list && list.data && Array.isArray(list.data)) list = list.data;
            holdingEventCache = Array.isArray(list) ? list.slice() : [];
            if (!Array.isArray(list) || list.length === 0) {
                setHoldingEventHint("최근 포지션 이벤트가 없습니다.");
                setHoldingEventList([], !!openList);
                return;
            }
            var e = list[0] || {};
            var t = safeStr(e.EVENT_TYPE || e.event_type);
            var tm = safeStr(e.EVENT_TIME || e.event_time);
            var m = safeStr(e.MESSAGE || e.message);
            setHoldingEventHint("최근 이벤트: " + (t || "-") + " · " + (tm || "-") + "\n" + (m || ""));
            setHoldingEventList(list, !!openList);
        }).fail(function () {
            setHoldingEventHint("");
            setHoldingEventList([], false);
        });
    }

    function applyHoldingSelection(code) {
        if (!code) return;

        var inp = document.getElementById("topSearchInput");
        if (inp) inp.value = code;

        var codeInput = document.getElementById("stockCode");
        if (codeInput) codeInput.value = code;

        var fromEl = document.getElementById("fromDate");
        var toEl = document.getElementById("toDate");
        if ((fromEl && !fromEl.value) || (toEl && !toEl.value)) {
            if (typeof window.setDefaultDates === "function") {
                window.setDefaultDates();
            }
        }

        try {
            window.__SELECTED_STOCK_COUNTRY = holdingSelectedData ? safeStr(holdingSelectedData.country) : "";
            window.__SELECTED_STOCK_MARKET = holdingSelectedData ? safeStr(holdingSelectedData.market) : "";
        } catch (e) {}

        if (typeof window.doSearch === "function") {
            window.doSearch();
        } else if (typeof window.selectStockByCode === "function") {
            window.selectStockByCode(code);
        } else if (typeof window.loadStockChartByCode === "function") {
            window.loadStockChartByCode(code);
        } else if (typeof window.ChartScript !== "undefined" && ChartScript.loadKisItemchartprice) {
            ChartScript.loadKisItemchartprice({
                stockCode: code,
                periodDivCode: (document.getElementById("periodDivCode") || {}).value || "D",
                fromDate: (fromEl && fromEl.value ? fromEl.value : "").replace(/-/g, ""),
                toDate: (toEl && toEl.value ? toEl.value : "").replace(/-/g, ""),
                orgAdjPrc: "1"
            });
        }
    }

    function getActiveGroupId() {
        var el = document.getElementById("wg_group_id");
        var v = el ? safeStr(el.value) : "";
        if (v) return v;
        if (holdingSelectedGroup) return holdingSelectedGroup;

        if (holdingSelectedCode && Array.isArray(holdingLastList)) {
            for (var i = 0; i < holdingLastList.length; i++) {
                var r = holdingLastList[i] || {};
                var code = normalizeStockCode(safeStr(getHoldingField(r, "stock_code")), safeStr(getHoldingField(r, "stock_country_code")));
                if (code === holdingSelectedCode) {
                    return safeStr(getHoldingField(r, "stock_group"));
                }
            }
        }
        return "";
    }

    function parsePositiveNumber(raw) {
        var s = safeStr(raw).replace(/,/g, "");
        var n = Number(s);
        if (!s || isNaN(n) || n <= 0) return null;
        return n;
    }

    function isUsCountry(ctry) {
        return String(ctry || "").toUpperCase() === "US";
    }

    function formatPriceByCountry(v, ctry) {
        var n = Number(v);
        if (isNaN(n)) return "";
        if (isUsCountry(ctry)) {
            return String((Math.round(n * 10) / 10).toFixed(1));
        }
        return String(Math.round(n));
    }

    function applyHoldingPriceInputFormat(ctry) {
        var pEl = document.getElementById("holdingPrice");
        if (!pEl) return;
        if (isUsCountry(ctry)) {
            pEl.step = "0.1";
            pEl.placeholder = "단가(소수 1자리)";
        } else {
            pEl.step = "1";
            pEl.placeholder = "단가";
            if (pEl.value) {
                var n = Number(String(pEl.value).replace(/,/g, ""));
                if (!isNaN(n)) pEl.value = String(Math.round(n));
            }
        }
    }

    function setHoldingInfo(msg) {
        setHoldingError(msg || "");
        if (msg) {
            setTimeout(function () {
                setHoldingError("");
            }, 2200);
        }
    }

    function resetHoldingFormInputs() {
        var qtyEl = document.getElementById("holdingQty");
        var priceEl = document.getElementById("holdingPrice");
        var descEl = document.getElementById("holdingDesc");

        if (qtyEl) qtyEl.value = "1";
        if (priceEl) priceEl.value = "";
        if (descEl) descEl.value = "";

        holdingSelectedData = null;
        holdingEventCache = [];
        holdingEventFilter = "all";
        $all(".holding-ev-filter").forEach(function (b) {
            if (safeStr(b.getAttribute("data-evf")) === "all") b.classList.add("is-active");
            else b.classList.remove("is-active");
        });
        setHoldingAvgPreview("");
        setHoldingEventHint("");
        setHoldingEventList([], false);
    }

    function holdingApiCall(action, qty, price, description, done) {
        if (!window.jQuery) {
            setHoldingError("jQuery가 필요합니다.");
            return;
        }
        if (holdingActionBusy) {
            setHoldingError("요청 처리 중입니다. 잠시만 기다려주세요.");
            return;
        }

        var groupId = getActiveGroupId();
        if (!groupId) {
            setHoldingError("관심그룹을 먼저 선택해주세요.");
            return;
        }
        if (!holdingSelectedCode) {
            setHoldingError("보유종목에서 종목을 먼저 선택해주세요.");
            return;
        }

        var cleanQty = (action === "DELETE") ? 0 : parsePositiveNumber(qty);
        var cleanPrice = (action === "DELETE") ? 0 : parsePositiveNumber(price);

        if (action !== "DELETE" && cleanQty == null) {
            setHoldingError("수량은 0보다 큰 숫자여야 합니다.");
            return;
        }
        if (action !== "DELETE" && cleanPrice == null) {
            setHoldingError("단가는 0보다 큰 숫자여야 합니다.");
            return;
        }

        var url = (window.__URLS && window.__URLS.holdingPositionEvent)
            ? window.__URLS.holdingPositionEvent
            : "/finance/holdingPositionEvent.do";

        holdingActionBusy = true;
        setHoldingError("");

        window.jQuery.ajax({
            url: url,
            type: "POST",
            dataType: "json",
            data: {
                action: action,
                group_id: groupId,
                stock_code: holdingSelectedCode,
                qty: String(cleanQty == null ? 1 : cleanQty),
                price: String(cleanPrice == null ? 0 : cleanPrice),
                description: description || ""
            },
            timeout: 20000
        }).done(function (resp) {
            var norm = normalizeResponse(resp);
            if (!norm.ok) {
                setHoldingError(norm.msg || "처리에 실패했습니다.");
                return;
            }
            setHoldingInfo("처리 완료: " + action);
            if (typeof done === "function") done();
        }).fail(function (xhr) {
            setHoldingError("통신 오류 (" + xhr.status + ")");
        }).always(function () {
            holdingActionBusy = false;
        });
    }

    function readHoldingFormValues() {
        var qtyEl = document.getElementById("holdingQty");
        var priceEl = document.getElementById("holdingPrice");
        var descEl = document.getElementById("holdingDesc");

        var qtyRaw = qtyEl ? qtyEl.value : "";
        var priceRaw = priceEl ? priceEl.value : "";
        var qtyNum = parsePositiveNumber(qtyRaw);
        var priceNum = parsePositiveNumber(priceRaw);

        return {
            qty: qtyNum,
            price: priceNum,
            description: descEl ? safeStr(descEl.value) : ""
        };
    }

    function openAverageDownPreviewAndExecute() {
        if (!window.jQuery) {
            setHoldingError("jQuery가 필요합니다.");
            return;
        }
        var groupId = getActiveGroupId();
        if (!groupId) {
            setHoldingError("관심그룹을 먼저 선택해주세요.");
            return;
        }
        if (!holdingSelectedCode) {
            setHoldingError("보유종목에서 종목을 먼저 선택해주세요.");
            return;
        }

        var fv = readHoldingFormValues();
        var qty = fv.qty;
        var price = fv.price;
        var desc = fv.description;

        if (qty == null) {
            setHoldingError("수량은 0보다 큰 숫자여야 합니다.");
            return;
        }
        if (price == null) {
            setHoldingError("단가는 0보다 큰 숫자여야 합니다.");
            return;
        }

        var purl = (window.__URLS && window.__URLS.holdingAverageDownPreview)
            ? window.__URLS.holdingAverageDownPreview
            : "/finance/holdingAverageDownPreview.do";

        window.jQuery.ajax({
            url: purl,
            type: "GET",
            dataType: "json",
            data: {
                group_id: groupId,
                stock_code: holdingSelectedCode,
                add_qty: qty,
                add_price: price,
                current_price: price
            },
            timeout: 20000
        }).done(function (resp) {
            var norm = normalizeResponse(resp);
            if (!norm.ok) {
                setHoldingError(norm.msg || "프리뷰 조회 실패");
                return;
            }
            var d = (norm.list && !Array.isArray(norm.list)) ? norm.list : {};
            var beforeAvgN = toNumber(d.before_avg);
            var afterAvgN = toNumber(d.after_avg);
            var beforePctN = toNumber(d.before_pct);
            var afterPctN = toNumber(d.after_pct);
            var msg = [
                "[물타기 프리뷰]",
                "예상 평단: " + safeStr(d.after_avg)
                    + (isNaN(beforeAvgN) || isNaN(afterAvgN) ? "" : (" (개선 " + signedNum(beforeAvgN - afterAvgN) + ")")),
                "예상 수익률: " + safeStr(d.after_pct) + "%"
                    + (isNaN(beforePctN) || isNaN(afterPctN) ? "" : (" (변화 " + signedPct(afterPctN - beforePctN) + ")")),
                "",
                "실행할까요?"
            ].join("\n");

            if (!window.confirm(msg)) return;

            holdingApiCall("AVERAGE_DOWN", qty, price, desc || "UI 물타기", function () {
                loadHoldingsList(true);
            });
        }).fail(function (xhr) {
            setHoldingError("프리뷰 통신 오류 (" + xhr.status + ")");
        });
    }

    function loadHoldingsList(force) {
        if (holdingLoading) return;

        var market = getHoldingMarket();
        if (!force && holdingLoaded && holdingLastMarket === market) {
            return;
        }

        setHoldingError("");
        setHoldingEventHint("");
        setHoldingEventList([], false);
        setHoldingAvgPreview("");
        setHoldingMeta(market, null);

        var url = (window.__URLS && window.__URLS.selectHoldingStocks)
            ? window.__URLS.selectHoldingStocks
            : "/finance/selectHoldingStocks.do";

        if (!window.jQuery) {
            setHoldingError("jQuery가 필요합니다.");
            return;
        }

        holdingLoading = true;
        holdingLastMarket = market;

        window.jQuery.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            data: { market: market },
            timeout: 20000
        }).done(function (resp) {
            var norm = normalizeResponse(resp);
            if (!norm.ok) {
                setHoldingError(norm.msg || "조회 실패");
                renderHoldingList([]);
                setHoldingMeta(market, 0);
                return;
            }

            var list = norm.list;
            if (list && list.data && Array.isArray(list.data)) list = list.data;
            if (!Array.isArray(list)) list = [];
            holdingLastList = list.slice();

            renderHoldingList(list);
            setHoldingMeta(market, list.length);
        }).fail(function (xhr) {
            setHoldingError("통신 오류 (" + xhr.status + ")");
            renderHoldingList([]);
            setHoldingMeta(market, 0);
        }).always(function () {
            holdingLoading = false;
            holdingLoaded = true;
        });
    }

    function setActiveSignalMarket(market) {
        $all(".sig-chip").forEach(function (btn) {
            var m = btn.getAttribute("data-market");
            if (m === market) btn.classList.add("is-active");
            else btn.classList.remove("is-active");
        });
    }

    function bindRightTabs() {
        var tabs = $all(".rt-tab");
        if (!tabs.length) return;
        tabs.forEach(function (tab) {
            tab.addEventListener("click", function () {
                var name = this.getAttribute("data-tab");
                if (!name) return;
                if (name === "holding") {
                    loadHoldingsList(false);
                }
                tabs.forEach(function (t) { t.classList.remove("is-active"); });
                this.classList.add("is-active");
                $all(".rt-pane").forEach(function (pane) {
                    if (pane.getAttribute("data-pane") === name) {
                        pane.classList.add("is-active");
                    } else {
                        pane.classList.remove("is-active");
                    }
                });
            });
        });
    }

    function bindHoldingPanel() {
        var qtyInput = document.getElementById("holdingQty");
        var priceInput = document.getElementById("holdingPrice");
        if (qtyInput) qtyInput.addEventListener("input", updateHoldingAvgPreview);
        if (priceInput) priceInput.addEventListener("input", updateHoldingAvgPreview);

        var reload = $("#btnHoldingReload");
        if (reload) {
            reload.addEventListener("click", function () {
                resetHoldingFormInputs();
                loadHoldingsList(true);
            });
        }

        var addBtn = $("#btnHoldingAdd");
        if (addBtn) {
            addBtn.addEventListener("click", function () {
                var fv = readHoldingFormValues();
                if (fv.qty == null) {
                    setHoldingError("수량은 0보다 큰 숫자여야 합니다.");
                    return;
                }
                if (fv.price == null) {
                    setHoldingError("단가는 0보다 큰 숫자여야 합니다.");
                    return;
                }
                holdingApiCall("ADD", fv.qty, fv.price, fv.description || "UI 보유추가", function () {
                    loadHoldingsList(true);
                });
            });
        }

        var delBtn = $("#btnHoldingDelete");
        if (delBtn) {
            delBtn.addEventListener("click", function () {
                if (!holdingSelectedCode) {
                    alert("삭제할 보유종목을 먼저 선택해주세요.");
                    return;
                }
                if (!window.confirm("선택한 보유종목을 종료(삭제)할까요?")) return;
                var descEl = document.getElementById("holdingDesc");
                var desc = descEl ? safeStr(descEl.value) : "";
                holdingApiCall("DELETE", 0, 0, desc || "UI 보유삭제", function () {
                    loadHoldingsList(true);
                });
            });
        }

        var avgBtn = $("#btnHoldingAvgDown");
        if (avgBtn) {
            avgBtn.addEventListener("click", function () {
                openAverageDownPreviewAndExecute();
            });
        }

        var evBtn = $("#btnHoldingEvents");
        if (evBtn) {
            evBtn.addEventListener("click", function () {
                var panel = document.getElementById("holdingEventListWrap");
                var open = !(panel && panel.style.display !== "none");
                loadHoldingEventHint(open);
            });
        }

        $all(".holding-ev-filter").forEach(function (btn) {
            btn.addEventListener("click", function () {
                var f = safeStr(this.getAttribute("data-evf")) || "all";
                holdingEventFilter = f;
                $all(".holding-ev-filter").forEach(function (b) {
                    if (safeStr(b.getAttribute("data-evf")) === f) b.classList.add("is-active");
                    else b.classList.remove("is-active");
                });
                setHoldingEventList(holdingEventCache, true);
            });
        });

        var wl = document.getElementById("wlMarket");
        if (wl) {
            wl.addEventListener("change", function () {
                var market = (this.value === "A") ? "A" : "N";
                setActiveSignalMarket(market);
                if (typeof window.refreshRecSignalPanel === "function") {
                    window.refreshRecSignalPanel(false);
                }

                resetHoldingFormInputs();
                if (isHoldingTabActive()) {
                    loadHoldingsList(true);
                } else {
                    holdingLoaded = false;
                }
            });
        }
    }

    function bind() {
        // 레거시 추천 조회는 제거하고, 보유종목 패널만 유지한다.

        var initMarket = "N";
        var wl = document.getElementById("wlMarket");
        if (wl && wl.value) initMarket = (wl.value === "A") ? "A" : "N";

        setActiveSignalMarket(initMarket);
        bindRightTabs();
        bindHoldingPanel();
    }

    // DOM ready
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", bind);
    } else {
        bind();
    }
})();
