/**
 * 추천 종목 모달
 * - 필터: 국내/해외, 코스피/코스닥(국내), 최소등급(+), 가격 범위, 원픽(1개), 현재가(KIS), limit
 * - 리스트: 원픽 뱃지, 선택(라디오), 더블클릭 적용, 선택 적용 버튼
 */
(function () {
    "use strict";
    // WF-1-2: 추천종목 UI (필터/레짐배너/이벤트요약/선택적용)

    function $(sel) { return document.querySelector(sel); }
    function $all(sel) { return Array.prototype.slice.call(document.querySelectorAll(sel)); }

    function safeStr(v) {
        if (v === null || v === undefined) return "";
        return String(v);
    }

    function escapeHtml(s) {
        return safeStr(s)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/\"/g, "&quot;")
            .replace(/'/g, "&#39;");
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

    function getRowField(row, name) {
        // snake_case 우선, 그 다음 camel/UPPER 변형까지 흡수
        var map = {
            stock_code: ["stock_code", "stockCode", "code", "pdno", "STOCK_CODE"],
            stock_ko_name: ["stock_ko_name", "stockKoName", "name", "STOCK_KO_NAME"],
            stock_market: ["stock_market", "stockMarket", "market", "STOCK_MARKET"],
            stock_country_code: ["stock_country_code", "stockCountryCode", "country", "STOCK_COUNTRY_CODE"],
            daily_close: ["daily_close", "dailyClose", "close", "DAILY_CLOSE"],
            daily_dist20_pct: ["daily_dist20_pct", "dailyDist20Pct", "dist20", "DAILY_DIST20_PCT"],
            reco_score: ["reco_score", "recoScore", "score", "RECO_SCORE"],
            reco_signal_code: ["reco_signal_code", "recoSignalCode", "signalCode", "RECO_SIGNAL_CODE"],
            reco_signal_name: ["reco_signal_name", "recoSignalName", "signalName", "RECO_SIGNAL_NAME"],
            reco_trend_text: ["reco_trend_text", "recoTrendText", "trendText", "RECO_TREND_TEXT"],
            reco_reason_detail: ["reco_reason_detail", "recoReasonDetail", "reasonDetail", "RECO_REASON_DETAIL"],
            reco_event_summary: ["reco_event_summary", "recoEventSummary", "eventSummary", "RECO_EVENT_SUMMARY"],
            modify_date: ["modify_date", "modifyDate", "MODIFY_DATE"],
            now_price: ["now_price", "nowPrice", "NOW_PRICE"],
            now_diff: ["now_diff", "nowDiff", "NOW_DIFF"],
            now_pct: ["now_pct", "nowPct", "NOW_PCT"],
            regime_kr_score: ["regime_kr_score", "regimeKrScore", "REGIME_KR_SCORE"],
            regime_us_score: ["regime_us_score", "regimeUsScore", "REGIME_US_SCORE"]
        };
        return pick(row, map[name] || [name]);
    }

    function nfmt(v) {
        var s = safeStr(v).replace(/,/g, "");
        if (!s) return "-";
        var num = Number(s);
        if (isNaN(num)) return safeStr(v);
        return num.toLocaleString();
    }

    function pctfmt(v) {
        var s = safeStr(v).replace(/,/g, "");
        if (!s) return "-";
        var num = Number(s);
        if (isNaN(num)) return safeStr(v);
        return num.toFixed(2) + "%";
    }

    
    function sigIcon(code) {
        code = safeStr(code).toUpperCase();
        if (code === "STRONG_BUY") return "🔥";
        if (code === "BUY") return "▲";
        if (code === "WEAK_BUY") return "↗";
        if (code === "HOLD") return "•";
        if (code === "WEAK_SELL") return "↘";
        if (code === "SELL") return "▼";
        if (code === "STRONG_SELL") return "🧊";
        return "•";
    }

    function domMktLabel(v) {
        v = safeStr(v).toUpperCase();
        if (v === "STK" || v === "KOSPI") return "KOSPI";
        if (v === "KSQ" || v === "KOSDAQ") return "KOSDAQ";
        return v || "-";
    }

    function domMktClass(v) {
        v = safeStr(v).toUpperCase();
        if (v === "STK" || v === "KOSPI") return "reco-mkt-kospi";
        if (v === "KSQ" || v === "KOSDAQ") return "reco-mkt-kosdaq";
        return "reco-mkt-etc";
    }

    function buildMktBadge(mkt) {
        var lbl = domMktLabel(mkt);
        var cls = domMktClass(mkt);
        return '    <span class="reco-mkt-badge ' + cls + '">' + lbl + '</span>';
    }

    function isBuySignal(code) {
        code = safeStr(code).toUpperCase();
        return (code.indexOf("BUY") >= 0);
    }

    function isSellSignal(code) {
        code = safeStr(code).toUpperCase();
        return (code.indexOf("SELL") >= 0);
    }

    function timingLabelShort(text) {
        var t = safeStr(text);
        if (t.indexOf("매도주의") >= 0) return "매도주의";
        if (t.indexOf("매수관심") >= 0) return "매수관심";
        if (t.indexOf("매수대기") >= 0) return "매수대기";
        return "매수대기";
    }

    function signalTagLabel(code, trend, reason, events) {
        var t = timingDecision(code, trend, reason, events);
        return timingLabelShort(t.text);
    }

    function signalTagClass(code, trend, reason, events) {
        var t = timingDecision(code, trend, reason, events);
        return (t.cls === "wait") ? "hold" : t.cls;
    }

    function signalCodeToKorean(code) {
        var c = safeStr(code).toUpperCase();
        if (c === "STRONG_BUY") return "강한매수";
        if (c === "BUY") return "매수";
        if (c === "WEAK_BUY") return "관심매수";
        if (c === "HOLD") return "관망";
        if (c === "WEAK_SELL") return "주의매도";
        if (c === "SELL") return "매도";
        if (c === "STRONG_SELL") return "강한매도";
        return "";
    }

    function signalLabelKorean(sigName, sigCode) {
        var code = safeStr(sigCode).toUpperCase();
        var korByCode = signalCodeToKorean(code);
        var name = safeStr(sigName);
        if (!name && korByCode) return korByCode;
        if (name && korByCode && name.toUpperCase() !== code) {
            return name + " (" + korByCode + ")";
        }
        return korByCode || name || code;
    }

    function timingDecision(sig, trend, reason, events) {
        var s = safeStr(sig).toUpperCase();
        var t = safeStr(trend).toUpperCase();
        var r = safeStr(reason).toUpperCase();
        var e = safeStr(events).toUpperCase();
        var all = s + " " + t + " " + r + " " + e;

        if (all.indexOf("TRIGGER_PULLBACK_READY") >= 0) {
            return { text: "매수관심(눌림형)", cls: "buy" };
        }
        if (all.indexOf("TRIGGER_BREAKOUT_READY") >= 0) {
            return { text: "매수관심(돌파형)", cls: "buy" };
        }
        if (all.indexOf("TRIGGER_PULLBACK_WAIT") >= 0) {
            return { text: "매수대기(눌림형)", cls: "wait" };
        }
        if (all.indexOf("TRIGGER_BREAKOUT_WAIT") >= 0) {
            return { text: "매수대기(돌파형)", cls: "wait" };
        }

        if (all.indexOf("SELL") >= 0 || all.indexOf("RISK") >= 0 || all.indexOf("EXIT") >= 0 || all.indexOf("MACD: SELL") >= 0) {
            return { text: "매도주의", cls: "sell" };
        }
        if (all.indexOf("BUY") >= 0 || all.indexOf("ENTRY_READY") >= 0 || all.indexOf("TREND_OK") >= 0 || all.indexOf("PULLBACK") >= 0) {
            return { text: "매수관심", cls: "buy" };
        }
        return { text: "매수대기", cls: "wait" };
    }

    function setError(msg) {
        var targets = ["#recoError", "#signalError"];
        targets.forEach(function (sel) {
            var el = $(sel);
            if (!el) return;
            if (!msg) {
                el.style.display = "none";
                el.textContent = "";
                return;
            }
            el.style.display = "block";
            el.textContent = msg;
        });
    }

    function setAsOf(text) {
        var targets = ["#recoAsOf", "#signalAsOf"];
        targets.forEach(function (sel) {
            var el = $(sel);
            if (el) el.textContent = text || "";
        });
    }

    function buildAsOfText(list) {
        if (!Array.isArray(list) || list.length === 0) return "";
        var latest = "";
        for (var i = 0; i < list.length; i++) {
            var d = safeStr(getRowField(list[i], "modify_date")).trim();
            if (!d) continue;
            if (!latest || d > latest) latest = d; // YYYY-MM-DD HH:MI 형식이라 문자열 비교 가능
        }
        if (!latest) return "총 " + list.length + "건";
        return "데이터 기준 " + latest + " · 총 " + list.length + "건";
    }

    function setSignalCount(filteredCount, totalCount) {
        var el = $("#signalCount");
        if (!el) return;
        if (!totalCount && totalCount !== 0) {
            el.textContent = "";
            return;
        }
        if (filteredCount === totalCount) {
            el.textContent = "총 " + totalCount + "건";
            return;
        }
        el.textContent = "표시 " + filteredCount + " / 총 " + totalCount + "건";
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

    var selectedCode = "";
    var lastList = [];
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


    function getSignalFilter() {
        var active = document.querySelector(".sig-pill.is-active");
        return active ? (active.getAttribute("data-filter") || "all") : "all";
    }

    function getSignalTypeFilters() {
        var filters = [];
        $all(".sig-type-chip.is-active").forEach(function (btn) {
            var type = btn.getAttribute("data-type");
            if (type) filters.push(type);
        });
        return filters;
    }

    function getSignalKeyword() {
        var el = document.getElementById("signalKeyword");
        return el ? safeStr(el.value).trim().toUpperCase() : "";
    }

    function matchesSignalType(item, selectedTypes) {
        if (!selectedTypes || selectedTypes.length === 0) return true;
        if (selectedTypes.indexOf("all") >= 0) return true;

        var signalCode = safeStr(getRowField(item, "reco_signal_code")).toUpperCase();
        var signalName = safeStr(getRowField(item, "reco_signal_name")).toUpperCase();
        var trendText = safeStr(getRowField(item, "reco_trend_text")).toUpperCase();
        var reasonDetail = safeStr(getRowField(item, "reco_reason_detail")).toUpperCase();
        var eventSummary = safeStr(getRowField(item, "reco_event_summary")).toUpperCase();
        var timing = timingDecision(signalCode, trendText, reasonDetail, eventSummary);

        for (var i = 0; i < selectedTypes.length; i++) {
            var type = selectedTypes[i];
            switch (type) {
                case "buy":
                    if (timing.cls === "buy" || signalCode.indexOf("BUY") >= 0) return true;
                    break;
                case "sell":
                    if (timing.cls === "sell" || signalCode.indexOf("SELL") >= 0) return true;
                    break;
                case "entry_ready":
                    if (reasonDetail.indexOf("ENTRY_READY") >= 0 || reasonDetail.indexOf("진입 검토") >= 0 ||
                        eventSummary.indexOf("ENTRY_READY") >= 0 || eventSummary.indexOf("TRIGGER_") >= 0) return true;
                    break;
                case "caution":
                    if (timing.cls === "sell" || reasonDetail.indexOf("RISK") >= 0 || reasonDetail.indexOf("주의") >= 0 ||
                        reasonDetail.indexOf("WAIT") >= 0 || eventSummary.indexOf("MACD:SELL") >= 0) return true;
                    break;
                case "long_uptrend":
                    if (trendText.indexOf("장기 우상향") >= 0 || trendText.indexOf("장기 상승") >= 0 ||
                        reasonDetail.indexOf("장기 우상향") >= 0 || reasonDetail.indexOf("장기 상승") >= 0 ||
                        eventSummary.indexOf("정배열") >= 0) return true;
                    break;
            }
        }
        return false;
    }

    function filterSignalList(list, filter) {
        if (!Array.isArray(list)) return [];

        var typeFilters = getSignalTypeFilters();
        var filtered = list.slice();

        // 첫 번째: 매수/매도 필터
        if (filter && filter !== "all") {
            if (filter === "buy") {
                filtered = filtered.filter(function (r) {
                    return isBuySignal(getRowField(r, "reco_signal_code"));
                });
            } else if (filter === "sell") {
                filtered = filtered.filter(function (r) {
                    return isSellSignal(getRowField(r, "reco_signal_code"));
                });
            }
        }

        // 두 번째: 신호유형 필터
        filtered = filtered.filter(function (item) {
            return matchesSignalType(item, typeFilters);
        });

        // 세 번째: 키워드 검색(종목명/코드/이벤트/사유/추세)
        var kw = getSignalKeyword();
        if (kw) {
            filtered = filtered.filter(function (item) {
                var code = safeStr(getRowField(item, "stock_code")).toUpperCase();
                var name = safeStr(getRowField(item, "stock_ko_name")).toUpperCase();
                var events = safeStr(getRowField(item, "reco_event_summary")).toUpperCase();
                var reason = safeStr(getRowField(item, "reco_reason_detail")).toUpperCase();
                var trend = safeStr(getRowField(item, "reco_trend_text")).toUpperCase();
                var hay = [code, name, events, reason, trend].join(" ");
                return hay.indexOf(kw) >= 0;
            });
        }

        return filtered;
    }

    function renderSignalList(list) {
        var wrap = $("#signalList");
        if (!wrap) return;

        if (!Array.isArray(list) || list.length === 0) {
            wrap.innerHTML = '<div class="signal-empty">표시할 매매 신호가 없습니다.</div>';
            return;
        }

        var html = "";
        for (var i = 0; i < list.length; i++) {
            var r = list[i] || {};
            var rank = i + 1;
            var rawCode = safeStr(getRowField(r, "stock_code"));
            var name = safeStr(getRowField(r, "stock_ko_name"));
            var mkt = safeStr(getRowField(r, "stock_market"));
            var ctry = safeStr(getRowField(r, "stock_country_code"));
            var code = normalizeStockCode(rawCode, ctry);
            var score = safeStr(getRowField(r, "reco_score"));
            var sig = safeStr(getRowField(r, "reco_signal_code")) || "HOLD";
            var sigName = safeStr(getRowField(r, "reco_signal_name"));
            var dist20 = safeStr(getRowField(r, "daily_dist20_pct"));
            var close = safeStr(getRowField(r, "daily_close"));
            var trend = safeStr(getRowField(r, "reco_trend_text"));
            var reason = safeStr(getRowField(r, "reco_reason_detail"));
            var events = safeStr(getRowField(r, "reco_event_summary"));
            var nowPrice = safeStr(getRowField(r, "now_price"));
            var nowDiff = safeStr(getRowField(r, "now_diff"));
            var nowPct = safeStr(getRowField(r, "now_pct"));

            var priceMain = nowPrice ? nfmt(nowPrice) : nfmt(close);
            var diffClass = "flat";
            var dnum = Number(safeStr(nowDiff).replace(/,/g, ""));
            if (!isNaN(dnum)) {
                if (dnum > 0) diffClass = "up";
                else if (dnum < 0) diffClass = "down";
            }

            var metaText = (code || "-") + (mkt ? (" · " + domMktLabel(mkt)) : "");
            var strategyText = signalLabelKorean(sigName, sig) || trend || reason;

            html += ''
                + '<div class="signal-item" data-code="' + code + '">'
                + '  <div class="signal-top">'
                + '    <span class="signal-rank">' + rank + '</span>'
                + '    <div class="signal-name-wrap">'
                + '      <div class="signal-name">' + (name || "-") + '</div>'
                + '      <div class="signal-code">' + metaText + '</div>'
                + '    </div>'
                + '    <span class="signal-tag ' + signalTagClass(sig, trend, reason, events) + '">' + signalTagLabel(sig, trend, reason, events) + '</span>'
                + '  </div>'
                + '  <div class="signal-mid">'
                + '    <span class="signal-strategy">' + (strategyText ? ("전략명 " + strategyText) : "전략 정보 없음") + '</span>'
                + (score ? '<span>점수 ' + score + '</span>' : '')
                + (dist20 ? '<span>20일 ' + pctfmt(dist20) + '</span>' : '')
                + '  </div>'
                + '  <div class="signal-sub">'
                + '    <span class="signal-price">현재 ' + priceMain + '</span>'
                + (nowPrice ? '<span class="signal-change ' + diffClass + '">' + (nowDiff ? nfmt(nowDiff) : "-") + ' (' + (nowPct ? pctfmt(nowPct) : "-") + ')</span>' : '')
                + '  </div>'
                + '</div>';
        }

        wrap.innerHTML = html;

        $all(".signal-item").forEach(function (row) {
            row.addEventListener("click", function () {
                var code = this.getAttribute("data-code");
                if (!code) return;
                selectedCode = code;
                highlightSelected();
            });
            row.addEventListener("dblclick", function () {
                var code = this.getAttribute("data-code");
                if (!code) return;
                selectedCode = code;
                applySelected();
            });
        });

        highlightSelected();
    }

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
        var recoMarket = $("#recoMarket");
        if (recoMarket) recoMarket.value = market;
        setActiveSignalMarket(market);
        updateControlState();
        loadList();
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

    function regimeLabel(score) {
        var n = Number(String(score).replace(/,/g, ""));
        if (isNaN(n)) return { cls: "rg-mid", text: "중(중립)", score: "-" };
        if (n >= 5) return { cls: "rg-up", text: "상(우호)", score: (n > 0 ? "+" : "") + n };
        if (n <= -5) return { cls: "rg-down", text: "하(비우호)", score: String(n) };
        return { cls: "rg-mid", text: "중(중립)", score: (n > 0 ? "+" : "") + n };
    }

    function renderRegimeSummary(list) {
        var box = $("#recoRegimeSummary");
        if (!box) return;

        if (!Array.isArray(list) || list.length === 0) {
            box.style.display = "none";
            box.innerHTML = "";
            return;
        }

        // 첫 행에 공통(시장) 레짐 점수를 실어 내려오게 구성
        var r0 = list[0] || {};
        var kr = safeStr(getRowField(r0, "regime_kr_score"));
        var us = safeStr(getRowField(r0, "regime_us_score"));

        if (!kr && !us) {
            box.style.display = "none";
            box.innerHTML = "";
            return;
        }

        var html = '';
        if (kr) {
            var a = regimeLabel(kr);
            html += '<span class="rg"><span class="rg-title">KR 레짐</span>'
                + '<span class="rg-pill ' + a.cls + '">' + a.text + '</span>'
                + '<span class="rg-score">(' + a.score + ')</span></span>';
        }
        if (us) {
            var b = regimeLabel(us);
            html += '<span class="rg"><span class="rg-title">US 레짐</span>'
                + '<span class="rg-pill ' + b.cls + '">' + b.text + '</span>'
                + '<span class="rg-score">(' + b.score + ')</span></span>';
        }

        box.innerHTML = html;
        box.style.display = "block";
    }

    function renderList(list) {
        var wrap = $("#recoList");
        if (!wrap) return;

        renderRegimeSummary(list);

        if (!Array.isArray(list) || list.length === 0) {
            wrap.innerHTML = '<div class="reco-empty">추천 종목이 없습니다.</div>';
            return;
        }

        var html = "";
        for (var i = 0; i < list.length; i++) {
            var r = list[i] || {};
            var rank = i + 1;
            var rawCode = safeStr(getRowField(r, "stock_code"));
            var name = safeStr(getRowField(r, "stock_ko_name"));
            var mkt = safeStr(getRowField(r, "stock_market"));
            var ctry = safeStr(getRowField(r, "stock_country_code"));
            var code = normalizeStockCode(rawCode, ctry);
            var score = safeStr(getRowField(r, "reco_score"));
            var sig = safeStr(getRowField(r, "reco_signal_code")) || "HOLD";
            var sigName = safeStr(getRowField(r, "reco_signal_name"));
            var dist20 = safeStr(getRowField(r, "daily_dist20_pct"));
            var close = safeStr(getRowField(r, "daily_close"));
            var trend = safeStr(getRowField(r, "reco_trend_text"));
            var reason = safeStr(getRowField(r, "reco_reason_detail"));
            var events = safeStr(getRowField(r, "reco_event_summary"));
            var timing = timingDecision(sig, trend, reason, events);
            var nowPrice = safeStr(getRowField(r, "now_price"));
            var nowDiff = safeStr(getRowField(r, "now_diff"));
            var nowPct = safeStr(getRowField(r, "now_pct"));

            var isPick = (i === 0);
            var isSelected = (selectedCode && selectedCode === code);

            var priceMain = nowPrice ? nfmt(nowPrice) : nfmt(close);
            var priceSub = nowPrice ? ("종가 " + nfmt(close)) : "";

            var diffClass = "reco-flat";
            var dnum = Number(safeStr(nowDiff).replace(/,/g, ""));
            if (!isNaN(dnum)) {
                if (dnum > 0) diffClass = "reco-up";
                else if (dnum < 0) diffClass = "reco-down";
            }

            var diffArrow = (diffClass === "reco-up" ? "▲" : (diffClass === "reco-down" ? "▼" : "•"));

            html += ''
                + '<div class="reco-row' + (isPick ? ' is-top' : '') + '" data-code="' + code + '">'
                + '  <div class="reco-left">'
                + '    <label class="reco-radio">'
                + '      <input type="radio" name="recoPick" value="' + code + '" ' + (isSelected ? "checked" : "") + '>'
                + '      <span class="reco-radio-ui"></span>'
                + '    </label>'
                + '    <span class="reco-rank reco-rank-' + rank + '">' + rank + '</span>'
                + '    <span class="reco-flag">' + (ctry ? ctry : "KR") + '</span>'
                + '    <span class="reco-name">' + (name || "-") + '</span>'
                + '    <span class="reco-code">' + (code || "-") + '</span>'
                + buildMktBadge(mkt)
                + (isPick ? '<span class="reco-onepick">원픽</span>' : '')
                + '  </div>'
                + '  <div class="reco-right">'
                + '    <span class="reco-sig sig-' + sig.toLowerCase().replace(/_/g, "-") + '">' + sigIcon(sig) + ' '+ signalLabelKorean(sigName, sig) + '</span>'
                + '    <span class="reco-score">' + (score ? ("점수 " + score) : "") + '</span>'
                + '    <span class="reco-dist">' + (dist20 ? ("20일 " + pctfmt(dist20)) : "") + '</span>'
                + '    <span class="reco-price">' + priceMain + '</span>'
                + (nowPrice ? '<span class="reco-sub">' + priceSub + '</span>' : '')
                + (nowPrice ? '<span class="reco-chg ' + diffClass + '">' + diffArrow + ' '+ (nowDiff ? nfmt(nowDiff) : "-") + ' (' + (nowPct ? pctfmt(nowPct) : "-") + ')</span>' : '')
                + '    <span class="reco-arrow">›</span>'
                + '  </div>'
                + '  <div class="reco-meta">'
                + '    <div class="reco-trend">' + trend + '</div>'
                + '    <div><span class="timing-badge ' + timing.cls + '">' + timing.text + '</span></div>'
                + (events ? ('    <div class="reco-events">' + escapeHtml(events) + '</div>') : '')
                + '    <div class="reco-reason">' + reason + '</div>'
                + '  </div>'
                + '</div>';
        }

        wrap.innerHTML = html;

        // radio change
        $all('input[name="recoPick"]').forEach(function (el) {
            el.addEventListener("change", function () {
                selectedCode = this.value;
                highlightSelected();
            });
        });

        // single click select, double click apply
        $all(".reco-row").forEach(function (row) {
            row.addEventListener("click", function () {
                var code = this.getAttribute("data-code");
                try { console.log("[reco] click data-code=", code); } catch (e) {}
                if (!code) return;
                selectedCode = code;
                var radio = this.querySelector('input[name="recoPick"][value="' + code + '"]');
                if (radio) radio.checked = true;
                highlightSelected();
            });
            row.addEventListener("dblclick", function () {
                var code = this.getAttribute("data-code");
                try { console.log("[reco] dblclick data-code=", code); } catch (e) {}
                if (!code) return;
                selectedCode = code;
                applySelected();
            });
        });

        highlightSelected();
    }

    function highlightSelected() {
        $all(".reco-row").forEach(function (row) {
            var code = row.getAttribute("data-code");
            if (code && selectedCode === code) row.classList.add("is-selected");
            else row.classList.remove("is-selected");
        });
        $all(".signal-item").forEach(function (row) {
            var code = row.getAttribute("data-code");
            if (code && selectedCode === code) row.classList.add("is-selected");
            else row.classList.remove("is-selected");
        });
    }

    function applySelected() {
        if (!selectedCode) {
            var checked = document.querySelector('input[name="recoPick"]:checked');
            if (checked && checked.value) {
                selectedCode = checked.value;
            } else {
                var row = document.querySelector(".reco-row.is-selected") || document.querySelector(".reco-row");
                if (row) {
                    selectedCode = row.getAttribute("data-code") || "";
                }
            }
        }
        try { console.log("[reco] applySelected selectedCode=", selectedCode); } catch (e) {}
        if (!selectedCode) return;

        // kisFinance.jsp 내부의 종목 변경 로직과 연동
        // 1) 상단 검색 입력이 있으면 넣고
        var inp = document.getElementById("topSearchInput");
        if (inp) inp.value = selectedCode;

        var codeInput = document.getElementById("stockCode");
        if (codeInput) codeInput.value = selectedCode;

        // 기간이 비어 있으면 기본값 세팅
        var fromEl = document.getElementById("fromDate");
        var toEl = document.getElementById("toDate");
        if ((fromEl && !fromEl.value) || (toEl && !toEl.value)) {
            if (typeof window.setDefaultDates === "function") {
                window.setDefaultDates();
            }
        }

        // 2) 기존 검색/조회 함수가 있으면 호출
        if (typeof window.doSearch === "function") {
            window.doSearch();
        } else if (typeof window.selectStockByCode === "function") {
            window.selectStockByCode(selectedCode);
        } else if (typeof window.loadStockChartByCode === "function") {
            window.loadStockChartByCode(selectedCode);
        } else if (typeof window.ChartScript !== "undefined" && ChartScript.loadKisItemchartprice) {
            ChartScript.loadKisItemchartprice({
                stockCode: selectedCode,
                periodDivCode: (document.getElementById("periodDivCode") || {}).value || "D",
                fromDate: (fromEl && fromEl.value ? fromEl.value : "").replace(/-/g, ""),
                toDate: (toEl && toEl.value ? toEl.value : "").replace(/-/g, ""),
                orgAdjPrc: "1"
            });
        }

        // 모달 닫기
        if (window.jQuery && window.jQuery("#recommendModal").modal) {
            window.jQuery("#recommendModal").modal("hide");
        }
    }

    function readFilters() {
        var market = ($("#recoMarket") ? $("#recoMarket").value : "N");
        var minGrade = ($("#recoMinGrade") ? $("#recoMinGrade").value : "WEAK_BUY");
        var domMarket = ($("#recoDomMarket") ? $("#recoDomMarket").value : "ALL");
        var priceMin = ($("#recoPriceMin") ? $("#recoPriceMin").value : "");
        var priceMax = ($("#recoPriceMax") ? $("#recoPriceMax").value : "");
        var onePick = ($("#recoOnePick") && $("#recoOnePick").checked) ? "Y" : "N";
        var includeNow = ($("#recoIncludeNow") && $("#recoIncludeNow").checked) ? "Y" : "N";
        var nowLimit = ($("#recoNowLimit") ? $("#recoNowLimit").value : "20");
        var limit = ($("#recoLimit") ? $("#recoLimit").value : "50");

        // market이 해외면 domMarket/현재가 비활성
        if (market === "A") {
            domMarket = "ALL";
            includeNow = "N";
        }

        // 원픽이면 limit=1
        if (onePick === "Y") {
            limit = "1";
        }

        return {
            market: market,
            minGrade: minGrade,
            domMarket: domMarket,
            priceMin: priceMin,
            priceMax: priceMax,
            onePick: onePick,
            includeNow: includeNow,
            nowLimit: nowLimit,
            limit: limit
        };
    }

    function updateControlState() {
        var market = ($("#recoMarket") ? $("#recoMarket").value : "N");

        var domSel = $("#recoDomMarket");
        if (domSel) domSel.disabled = (market !== "N");

        var nowChk = $("#recoIncludeNow");
        var nowSel = $("#recoNowLimit");
        if (nowChk) nowChk.disabled = (market !== "N");
        if (nowSel) nowSel.disabled = (market !== "N" || !(nowChk && nowChk.checked));

        var limitSel = $("#recoLimit");
        var onePick = $("#recoOnePick");
        if (limitSel && onePick && onePick.checked) limitSel.disabled = true;
        else if (limitSel) limitSel.disabled = false;
    }

    function loadList() {
        setError("");
        setAsOf("조회 중...");

        updateControlState();

        var f = readFilters();
        setActiveSignalMarket(f.market || "N");
        var url = (window.__URLS && window.__URLS.selectRecommendStocks) ? window.__URLS.selectRecommendStocks : "/finance/selectRecommendStocks.do";

        if (!window.jQuery) {
            setError("jQuery가 필요합니다.");
            setAsOf("");
            return;
        }

        window.jQuery.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            data: f,
            timeout: 15000
        }).done(function (resp) {
            var norm = normalizeResponse(resp);
            if (!norm.ok) {
                setError(norm.msg || "조회 실패");
                renderList([]);
                renderSignalList([]);
                setSignalCount(0, 0);
                setAsOf("");
                return;
            }

            var list = norm.list;
            if (list && list.data && Array.isArray(list.data)) list = list.data;
            if (!Array.isArray(list)) list = [];

            lastList = list.slice();

            // 원픽 모드면 자동 선택
            if (f.onePick === "Y" && list.length > 0) {
                selectedCode = safeStr(getRowField(list[0], "stock_code"));
            }

            renderList(list);
            var filtered = filterSignalList(list, getSignalFilter());
            renderSignalList(filtered);
            setSignalCount(filtered.length, list.length);
            setAsOf(buildAsOfText(list));
        }).fail(function (xhr) {
            setError("통신 오류 (" + xhr.status + ")");
            renderList([]);
            renderSignalList([]);
            setSignalCount(0, 0);
            setAsOf("");
        });
    }

    function setActiveSignalMarket(market) {
        $all(".sig-chip").forEach(function (btn) {
            var m = btn.getAttribute("data-market");
            if (m === market) btn.classList.add("is-active");
            else btn.classList.remove("is-active");
        });
    }

    function setActiveSignalFilter(filter) {
        $all(".sig-pill").forEach(function (btn) {
            var f = btn.getAttribute("data-filter");
            if (f === filter) btn.classList.add("is-active");
            else btn.classList.remove("is-active");
        });
    }

    function refreshSignalListFromCache() {
        var filtered = filterSignalList(lastList, getSignalFilter());
        renderSignalList(filtered);
        setSignalCount(filtered.length, lastList.length);
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

    function bindSignalPanel() {
        $all(".sig-chip").forEach(function (btn) {
            btn.addEventListener("click", function () {
                if (this.classList.contains("is-disabled")) return;
                var market = this.getAttribute("data-market") || "N";
                setActiveSignalMarket(market);
                var recoMarket = $("#recoMarket");
                if (recoMarket) recoMarket.value = market;
                var wl = document.getElementById("wlMarket");
                if (wl) wl.value = market;
                updateControlState();
                loadList();
            });
        });

        $all(".sig-pill").forEach(function (btn) {
            btn.addEventListener("click", function () {
                var filter = this.getAttribute("data-filter") || "all";
                setActiveSignalFilter(filter);
                refreshSignalListFromCache();
            });
        });

        // 상태필터 토글칩
        $all(".sig-type-chip").forEach(function (chip) {
            chip.addEventListener("click", function () {
                var type = this.getAttribute("data-type") || "";
                var chips = $all(".sig-type-chip");
                if (!type) return;

                if (type === "all") {
                    var on = !this.classList.contains("is-active");
                    chips.forEach(function (c) {
                        if (on) c.classList.add("is-active");
                        else c.classList.remove("is-active");
                    });
                    refreshSignalListFromCache();
                    return;
                }

                this.classList.toggle("is-active");

                var others = chips.filter(function (c) { return c.getAttribute("data-type") !== "all"; });
                var activeCount = 0;
                others.forEach(function (c) { if (c.classList.contains("is-active")) activeCount++; });

                var allChip = document.querySelector('.sig-type-chip[data-type="all"]');
                if (allChip) {
                    if (activeCount === others.length) allChip.classList.add("is-active");
                    else allChip.classList.remove("is-active");
                }

                refreshSignalListFromCache();
            });
        });

        var reload = $("#btnSignalReload");
        if (reload) reload.addEventListener("click", function () {
            loadList();
        });

        var kw = document.getElementById("signalKeyword");
        if (kw) {
            kw.addEventListener("input", function () {
                refreshSignalListFromCache();
            });
        }
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
                var recoMarket = $("#recoMarket");
                if (recoMarket) recoMarket.value = market;
                setActiveSignalMarket(market);
                updateControlState();
                loadList();

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
        // 추천 팝업 제거: 매매신호 탭에서 직접 조회/검색

        var initMarket = "N";
        var wl = document.getElementById("wlMarket");
        if (wl && wl.value) initMarket = (wl.value === "A") ? "A" : "N";

        setActiveSignalMarket(initMarket);
        setActiveSignalFilter("all");
        bindRightTabs();
        bindSignalPanel();
        bindHoldingPanel();
        loadList();
    }

    // DOM ready
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", bind);
    } else {
        bind();
    }
})();
