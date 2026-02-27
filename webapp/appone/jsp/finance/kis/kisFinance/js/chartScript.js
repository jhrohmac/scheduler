var ChartScript = (function () {
    var apiUrl = "/scheduler/finance/kisItemchartpriceData.do";
    var metaUrl = "/scheduler/finance/searchStocksKeyword.do";
    var currentPriceUrl = "/scheduler/finance/getCurrentPriceByInquirePrice.do";

    var metaCache = {}; // { [stockCode]: {name, market, raw} }
    var currentPriceCache = { code: null, ts: 0, raw: null }; // 3초 캐시

    var chart = null;
    var lastData = null;
    var lastQuery = null;
    var minuteRangeExtendInFlight = false;

    var headerStatic = { name: "-", code: "-", market: "-", country: "KR" };
    var explainCache = {
        markerTop: null,
        positionState: null,
        regime: null
    };

    var indexMetaMap = {
        "KOSPI": { name: "코스피", market: "KRX" },
        "KOSDAQ": { name: "코스닥", market: "KRX" },
        "KOSPI200": { name: "코스피200", market: "KRX" },
        "0001": { name: "코스피", market: "KRX" },
        "1001": { name: "코스닥", market: "KRX" },
        "2001": { name: "코스피200", market: "KRX" },
        ".DJI": { name: "DOW (.DJI)", market: "US" },
        ".IXIC": { name: "NASDAQ (.IXIC)", market: "US" },
        ".INX": { name: "S&P 500 (.INX)", market: "US" }
    };

    function normalizeIndexCode(code) {
        if (!code) return "";
        var c = String(code).trim().toUpperCase();
        return c;
    }

    function isIndexCode(code) {
        var c = normalizeIndexCode(code);
        if (!c) return false;
        if (c.charAt(0) === ".") return true;
        if (indexMetaMap[c]) return true;
        return false;
    }

    function applyIndexHeaderStatic(stockCode) {
        var c = normalizeIndexCode(stockCode);
        var meta = indexMetaMap[c];
        if (!meta) return;
        headerStatic.code = stockCode;
        headerStatic.name = meta.name || headerStatic.name;
        headerStatic.market = meta.market || headerStatic.market;
        headerStatic.country = (String(meta.market || "").toUpperCase() === "US") ? "US" : "KR";
        applyHeaderStatic();
    }


    function isMobileDevice() {
        try {
            return (/Android|iPhone|iPad|iPod|IEMobile|Opera Mini/i).test(navigator.userAgent || "");
        } catch (e) {
            return false;
        }
    }

    function applyMobilePanFix(stockOptions) {
        if (!stockOptions) return stockOptions;
        if (!isMobileDevice()) return stockOptions;

        stockOptions.chart = stockOptions.chart || {};
        stockOptions.chart.panning = { enabled: true, type: "x" };

        // 모바일에서 드래그가 "줌(좌표/+) 모드"로 잡히지 않도록 차트 줌/핀치 비활성
        stockOptions.chart.zoomType = undefined;
        stockOptions.chart.pinchType = undefined;
        stockOptions.chart.zooming = { type: null };

        stockOptions.tooltip = stockOptions.tooltip || {};
        stockOptions.tooltip.followTouchMove = false;

        // 크로스헤어(+) 느낌 제거
        if (stockOptions.xAxis) {
            var xa = Array.isArray(stockOptions.xAxis) ? stockOptions.xAxis : [stockOptions.xAxis];
            for (var i = 0; i < xa.length; i++) {
                if (!xa[i]) continue;
                if (xa[i].crosshair) xa[i].crosshair = false;
            }
            stockOptions.xAxis = Array.isArray(stockOptions.xAxis) ? xa : xa[0];
        }

        if (stockOptions.yAxis) {
            var ya = Array.isArray(stockOptions.yAxis) ? stockOptions.yAxis : [stockOptions.yAxis];
            for (var j = 0; j < ya.length; j++) {
                if (!ya[j]) continue;
                if (ya[j].crosshair) ya[j].crosshair = false;
            }
            stockOptions.yAxis = Array.isArray(stockOptions.yAxis) ? ya : ya[0];
        }

        return stockOptions;
    }


    function pickString(obj, keys) {
        if (!obj || !keys || !keys.length) return "";
        for (var i = 0; i < keys.length; i++) {
            var k = keys[i];
            var v = obj[k];
            if (v === null || v === undefined) continue;
            v = String(v);
            if ($.trim(v).length) return $.trim(v);
        }
        return "";
    }

    function formatSignedNumber(n) {
        var v = parseFloat(n);
        if (isNaN(v)) return "-";
        var s = formatNumber(Math.abs(v));
        return (v > 0 ? "+" : (v < 0 ? "-" : "")) + s;
    }

    function formatSignedPct(n) {
        var v = parseFloat(n);
        if (isNaN(v)) return "-";
        var abs = Math.abs(v);
        var s = abs.toLocaleString(undefined, { maximumFractionDigits: 2 });
        return (v > 0 ? "+" : (v < 0 ? "-" : "")) + s + "%";
    }

    function unwrapSingle(res) {
        if (!res) return null;

        // ResponseHandler 포맷: {result_code, result_msg, data:{...}}
        if (res.data) {
            if (res.data.singleData) return res.data.singleData;
            if (res.data.data && res.data.data.singleData) return res.data.data.singleData;
        }

        // DataTableSettingVo 직접 형태
        if (res.singleData) return res.singleData;

        return null;
    }

    function unwrapList(res) {
        if (!res) return [];
        if (res.data && Array.isArray(res.data)) return res.data;
        if (res.data && res.data.data && Array.isArray(res.data.data)) return res.data.data;
        if (Array.isArray(res.list)) return res.list;
        return [];
    }

    function getMarketLabelFromMeta(it) {
        var m = (it && (it.stock_market || it.groupMarket || it.market || it.market_section || it.rprsMrktKorName || "")) || "";
        m = String(m).trim();
        if (!m) return "";

        if (m === "KOSPI" || m === "kospi") return "코스피";
        if (m === "KOSDAQ" || m === "kosdaq") return "코스닥";
        if (m === "ETF" || m === "etf") return "ETF";
        return m;
    }

    function applyHeaderStatic() {
        if ($("#kisHdrName").length) $("#kisHdrName").text(headerStatic.name || "-");
        if ($("#kisHdrCode").length) $("#kisHdrCode").text(headerStatic.code || "-");
        if ($("#kisHdrMarket").length) $("#kisHdrMarket").text(headerStatic.market || "-");

        var ctry = String(headerStatic.country || "KR").toUpperCase();
        var isUS = (ctry === "US");
        var $flag = $("#kisHdrFlag");
        if ($flag.length) $flag.text(isUS ? "🇺🇸" : "🇰🇷");

        var $logo = $("#kisHdrLogo");
        if ($logo.length) {
            var code = String(headerStatic.code || "").trim();
            if (code && code.charAt(0) !== ".") {
                var safeCode = code.replace(/\//g, "_");
                var logoSrc = "/scheduler/appone/plugins/media/stock_logo/" + (isUS ? "US" : "KR") + "/" + safeCode + ".png";
                $logo.off("error").on("error", function () {
                    $(this).hide();
                    if ($flag.length) $flag.show();
                });
                $logo.attr("src", logoSrc).show();
                if ($flag.length) $flag.hide();
            } else {
                $logo.hide();
                if ($flag.length) $flag.show();
            }
        }

        // ChartScript 호환용
        if ($("#kisStockName").length) $("#kisStockName").text(headerStatic.name || "");
    }

    function setHeaderStaticFromMeta(stockCode, it) {
        if (!stockCode) return;

        var name = pickString(it, ["stock_ko_name", "stockKoName", "stock_name", "name", "hts_kor_isnm", "kor_isnm", "prdt_name", "item_name", "itmsNm"]);
        var market = getMarketLabelFromMeta(it);
        var ctry = pickString(it, ["stock_country_code", "stockCountryCode", "country", "country_code"]);

        if (name) headerStatic.name = name;
        headerStatic.code = stockCode;
        if (market) headerStatic.market = market;
        if (ctry) headerStatic.country = ctry;

        applyHeaderStatic();
    }

    function fetchStockMeta(stockCode, done) {
        if (!stockCode) {
            if (done) done(null);
            return;
        }

        if (metaCache[stockCode]) {
            setHeaderStaticFromMeta(stockCode, metaCache[stockCode].raw || {});
            if (done) done(metaCache[stockCode]);
            return;
        }

        $.ajax({
            url: metaUrl,
            type: "GET",
            dataType: "json",
            data: { in_stockCode: stockCode },
            success: function (res) {
                var list = unwrapList(res);
                var pick = null;
                for (var i = 0; i < list.length; i++) {
                    var it = list[i] || {};
                    var c = pickString(it, ["stock_code", "stockCode", "code"]);
                    if (c === stockCode) {
                        pick = it;
                        break;
                    }
                }
                if (!pick && list.length) pick = list[0];

                metaCache[stockCode] = { raw: pick || {} };
                setHeaderStaticFromMeta(stockCode, pick || {});
                if (done) done(metaCache[stockCode]);
            },
            error: function () {
                // fallback: 최소한 코드만 세팅
                headerStatic.code = stockCode;
                applyHeaderStatic();
                if (done) done(null);
            }
        });
    }

    function fetchCurrentPrice(stockCode, done) {
        if (!stockCode) {
            if (done) done(null);
            return;
        }

        var nowTs = Date.now();
        if (currentPriceCache.code === stockCode && (nowTs - currentPriceCache.ts) < 3000 && currentPriceCache.raw) {
            if (done) done(currentPriceCache.raw);
            try {
                applyCurrentPriceResult(currentPriceCache.raw);
            } catch (e) { }
            return;
        }

        $.ajax({
            url: currentPriceUrl,
            type: "GET",
            dataType: "json",
            data: { in_stockCode: stockCode },
            success: function (res) {				
                var single = unwrapSingle(res);
                currentPriceCache = { code: stockCode, ts: Date.now(), raw: single };

                try {
                    applyCurrentPriceResult(single);
                } catch (e) { }

                if (done) done(single);
            },
            error: function () {
                if (done) done(null);
            }
        });
    }

    function applyCurrentPriceResult(single) {
        if (!single) return;

        var out = single.output || single.out || null;
        if (!out && single.data && single.data.output) out = single.data.output;
        if (!out) return;

        var cur = pickNumber(out, ["stckPrpr", "stck_prpr", "ovrsNmixPrpr", "ovrs_nmix_prpr", "last", "lastPrice", "price"]);
        var diff = pickNumber(out, ["prdyVrss", "prdy_vrss", "ovrsNmixPrdyVrss", "ovrs_nmix_prdy_vrss", "diff", "change"]);
        var prevClose = pickNumber(out, ["stckSdpr", "stck_sdpr", "ovrsPrdyClpr", "ovrs_prdy_clpr", "prevClose", "base"]);

        if (isNaN(prevClose) && !isNaN(cur) && !isNaN(diff)) {
            prevClose = cur - diff;
        }

        // 종목 기본정보(마켓명) 보강
        if (!headerStatic.market) {
            var m = pickString(out, ["rprsMrktKorName"]);
            if (m) headerStatic.market = m;
            applyHeaderStatic();
        }

        if (!isNaN(cur)) {
            // 0 현재가 방어: 이미 차트에 정상 현재가가 있으면 0으로 덮어쓰지 않음
            if (cur === 0) {
                var last = (lastData && lastData.length) ? lastData[lastData.length - 1] : null;
                var lastClose = last ? parseFloat(last.close || last.stck_prpr || last.ovrs_nmix_prpr || last.ovrsNmixPrpr) : NaN;
                if (!isNaN(lastClose) && lastClose > 0) {
                    return;
                }
            }

            // 누적 거래량
            if ($("#kisHdrVol").length) {
                var v = pickNumber(out, ["acmlVol", "acml_vol", "volume"]);
                if (!isNaN(v)) {
                    $("#kisHdrVol").text("거래량 " + v.toLocaleString());
                }
            }

            // 현재가 라인/라벨도 '현재가' 기준으로 갱신
            try {
                if (chart) {
                    setLastPricePlotLine(chart, cur, prevClose);
                    if (options.maSrEnabled) {
                        refreshMaSupportResistance(chart, cur);
                    }
                }
            } catch (e) {
                // ignore
            }
        }
    }


    function setTextIfDifferent($el, value) {
        if (!$el || !$el.length) return;
        var next = (value === undefined || value === null) ? "" : String(value);
        if ($el.text() === next) return;
        $el.text(next);
    }

    function updateTopHeader(now, prevClose) {
        var c = parseFloat(now);
        var p = parseFloat(prevClose);

        var diff = (isNaN(c) || isNaN(p)) ? NaN : (c - p);
        var pct = (isNaN(c) || isNaN(p) || p === 0) ? NaN : (diff / p * 100);

        setTextIfDifferent($("#kisHdrName"), headerStatic.name || "-");
        setTextIfDifferent($("#kisHdrCode"), headerStatic.code || "-");
        setTextIfDifferent($("#kisHdrMarket"), headerStatic.market || "-");

        if ($("#kisHdrNow").length) $("#kisHdrNow").text(formatNumber(c));
        if ($("#kisHdrPct").length) $("#kisHdrPct").text(formatSignedPct(pct));
        if ($("#kisHdrDiff").length) $("#kisHdrDiff").text(formatSignedNumber(diff));

        // 등락 색상
        setUpDownClass($("#kisHdrNow"), c, p);
        if (!isNaN(diff)) {
            setUpDownClass($("#kisHdrPct"), diff, 0);
            setUpDownClass($("#kisHdrDiff"), diff, 0);
        } else {
            $("#kisHdrPct,#kisHdrDiff").removeClass("kis-up kis-down").addClass("kis-flat");
        }
    }

    // Realtime quote update (from websocket)
    function applyRealtimeQuote(msg) {
        if (!msg) return;

        var price = parseFloat(msg.price);
        if (isNaN(price)) return;
        // 0 틱 방어: 기존 정상 현재가가 있으면 0으로 덮어쓰지 않음
        if (price === 0) {
            var hdrNow = parseFloat(String(($("#kisHdrNow").text() || "")).replace(/,/g, ""));
            if (!isNaN(hdrNow) && hdrNow > 0) {
                return;
            }
            var last = (lastData && lastData.length) ? lastData[lastData.length - 1] : null;
            var lastClose = last ? parseFloat(last.close || last.stck_prpr || last.bstp_nmix_prpr) : NaN;
            if (!isNaN(lastClose) && lastClose > 0) {
                return;
            }
        }
        var diff = parseFloat(msg.diff);
        var prevClose = (!isNaN(diff)) ? (price - diff) : NaN;

        try {
            updateTopHeader(price, prevClose);
        } catch (e) {
            // ignore
        }

        try {
            if (chart) {
                setLastPricePlotLine(chart, price, prevClose);
                if (options.maSrEnabled) {
                    refreshMaSupportResistance(chart, price);
                }
            }
        } catch (e2) {
            // ignore
        }
    }

    // 월 경계선(각 월의 첫 거래일 시간) 전역 보관
    var monthBoundaryTimes = [];

    var options = {
        volumeEnabled: true,
        doubleChartEnabled: true,
        monthLinesEnabled: true,
        highLowEnabled: false,
        maSrEnabled: true,
        doublePointWidth: 38,
        doubleUpColor: "#e83e8c6e",
        doubleDownColor: "#3498db6e",
        maPeriods: [5, 20, 60, 120, 240],
        maConfigs: null,
        crossEnabled: false,
        crossShortPeriod: 5,
        crossLongPeriod: 20,

    };

    function toEpochMillis(dt) {
        if (dt === null || dt === undefined) return NaN;

        if (typeof dt === "number") {
            if (dt < 1e12) return dt * 1000;
            return dt;
        }

        if (typeof dt === "string") {
            var s = dt.replace(/[^0-9]/g, "");
            if (s.length === 8) {
                var y = parseInt(s.substr(0, 4), 10);
                var m = parseInt(s.substr(4, 2), 10) - 1;
                var d = parseInt(s.substr(6, 2), 10);
                return new Date(y, m, d).getTime();
            }
            var t = Date.parse(dt);
            if (!isNaN(t)) return t;
            return NaN;
        }

        if (dt instanceof Date) return dt.getTime();

        return NaN;
    }

    function formatNumber(n) {
        if (n === null || n === undefined) return "-";
        var v = parseFloat(n);
        if (isNaN(v)) return "-";
        // 소수점은 데이터에 따라 유지 (정수면 0자리, 소수면 최대 2자리)
        var isInt = Math.abs(v - Math.round(v)) < 1e-9;
        return isInt ? v.toLocaleString() : v.toLocaleString(undefined, { maximumFractionDigits: 2 });
    }

    function formatNumberNoDecimal(n) {
        if (n === null || n === undefined) return "-";
        var v = parseFloat(n);
        if (isNaN(v)) return "-";
        return v.toLocaleString(undefined, { maximumFractionDigits: 0 });
    }

    function isDomesticMarketContext() {
        try {
            var code = lastQuery ? lastQuery.stockCode : "";
            if (code && isIndexCode(code)) {
                var idx = indexMetaMap[normalizeIndexCode(code)];
                if (idx && idx.market) return (idx.market === "KRX");
            }

            var country = (lastQuery && lastQuery.stockCountryCode) ? String(lastQuery.stockCountryCode).toUpperCase() : "";
            if (country === "KR") return true;

            var market = (lastQuery && lastQuery.stockMarket) ? String(lastQuery.stockMarket).toUpperCase() : "";
            if (market === "KRX" || market === "KOSPI" || market === "KOSDAQ" || market === "STK" || market === "KSQ") return true;

            var m = (headerStatic && headerStatic.market) ? String(headerStatic.market) : "";
            if (m.indexOf("코스피") >= 0 || m.indexOf("코스닥") >= 0) return true;
            if (m.toUpperCase() === "KRX") return true;
        } catch (e) { }
        return false;
    }

    function formatNumberByMarket(n) {
        return isDomesticMarketContext() ? formatNumberNoDecimal(n) : formatNumber(n);
    }


    function pickNumber(obj, keys) {
        if (!obj || !keys || !keys.length) return NaN;
        for (var i = 0; i < keys.length; i++) {
            var k = keys[i];
            if (k in obj) {
                var v = parseFloat(obj[k]);
                if (!isNaN(v)) return v;
            }
        }
        return NaN;
    }

	function formatAmountShort(n) {
	    if (n === null || n === undefined) return "-";

	    var v = Number(String(n).replace(/,/g, ""));
	    if (!isFinite(v)) return "-";

	    var abs = Math.abs(v);

	    function trimZero(s) {
	        // "12.00" -> "12", "12.10" -> "12.1"
	        return String(s).replace(/\.0+$|(\.\d*[1-9])0+$/, "$1");
	    }

	    function fmtUnit(div, unit) {
	        // 2자리 반올림 후 trailing zero 제거
	        return trimZero((v / div).toFixed(2)) + unit;
	    }

	    if (abs >= 1e12) return fmtUnit(1e12, "조");
	    if (abs >= 1e8) return fmtUnit(1e8, "억");
	    if (abs >= 1e4) return fmtUnit(1e4, "만");

	    // 1만 미만은 원 단위로 콤마 유지
	    return formatNumber(v);
	}

	function formatVolumeMan(n) {
	    if (n === null || n === undefined) return "-";
	    var v = parseFloat(n);
	    if (isNaN(v)) return "-";

	    if (Math.abs(v) < 100000) {
	        if (typeof Highcharts !== "undefined") return Highcharts.numberFormat(v, 0);
	        return (Math.round(v)).toLocaleString();
	    }

	    var man = Math.round(v / 10000);   // 핵심: 소숫점 제거
	    if (typeof Highcharts !== "undefined") return Highcharts.numberFormat(man, 0) + "만";
	    return man.toLocaleString() + "만";
	}


    function updateTradeHeader(vol, amt) {
        if ($("#kisHdrVol").length) {
            $("#kisHdrVol").text("거래량 " + (isNaN(parseFloat(vol)) ? "-" : formatAmountShort(vol)));
        }
        //if ($("#kisHdrAmt").length) {
            // KIS 응답에서 거래대금(누적/해당봉) 단위가 다를 수 있어 숫자만 예쁘게 표시
        //	$("#kisHdrAmt").text("거래대금 " + (isNaN(parseFloat(amt)) ? "-" : formatAmountShort(amt)));
        //}
    }

    function isMinuteDivCode(code) {
        if (!code) return false;
        var p = ("" + code).toUpperCase();
        if (p === "T") return true;
        if (p.length > 1 && p.charAt(0) === "T") {
            var n = p.substring(1);
            return /^[0-9]+$/.test(n);
        }
        return false;
    }

    function formatDateLabel(ts, periodDivCode) {
        if (isNaN(ts)) return "-";
        var d = new Date(ts);
        var dow = ["일", "월", "화", "수", "목", "금", "토"][d.getDay()];
        var ymd = d.getFullYear() + "." + ("0" + (d.getMonth() + 1)).slice(-2) + "." + ("0" + d.getDate()).slice(-2);

        var p = (periodDivCode || "D").toUpperCase();
        // 분봉/틱 계열은 시간도 함께 표시
        if (isMinuteDivCode(p) || p === "M" || p === "H") {
            var hm = ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2);
            return ymd + " " + hm + "(" + dow + ")";
        }
        return ymd + "(" + dow + ")";
    }

    function formatAxisLabel(ts, periodDivCode, ctx) {
        if (isNaN(ts)) return "-";
        var p = (periodDivCode || "D").toUpperCase();
        if (isMinuteDivCode(p)) {
            // 분봉: 장 시간 내는 날짜+시간, 장 외(보간값)는 날짜만 표시
            var d = new Date(ts);
            var h = d.getHours();
            var m = d.getMinutes();
            if (h >= 9 && (h < 15 || (h === 15 && m <= 30))) {
                return Highcharts.dateFormat("%m/%d %H:%M", ts);
            }
            return Highcharts.dateFormat("%m/%d", ts);
        }

        // 일/주/월봉: 첫 라벨 또는 연도 변경 지점만 YYYY.MM.DD, 같은 연도는 MM.DD
        var axis = ctx && ctx.axis ? ctx.axis : null;
        var ticks = axis && axis.tickPositions ? axis.tickPositions : null;
        var idx = -1;
        if (ticks && ticks.length) {
            for (var i = 0; i < ticks.length; i++) {
                if (Math.abs(ticks[i] - ts) < 1) {
                    idx = i;
                    break;
                }
            }
        }

        if (idx <= 0) {
            return Highcharts.dateFormat("%Y.%m.%d", ts);
        }

        var prevTs = ticks[idx - 1];
        var y = new Date(ts).getFullYear();
        var py = new Date(prevTs).getFullYear();
        if (y !== py) {
            return Highcharts.dateFormat("%Y.%m.%d", ts);
        }
        return Highcharts.dateFormat("%m.%d", ts);
    }

    function plainToDate(plain) {
        if (!plain) return null;
        var s = ("" + plain).replace(/[^0-9]/g, "");
        if (s.length !== 8) return null;
        var y = parseInt(s.substr(0, 4), 10);
        var m = parseInt(s.substr(4, 2), 10) - 1;
        var d = parseInt(s.substr(6, 2), 10);
        return new Date(y, m, d);
    }

    function dateToPlain(d) {
        if (!d || isNaN(d.getTime())) return "";
        var y = d.getFullYear();
        var m = ("0" + (d.getMonth() + 1)).slice(-2);
        var dd = ("0" + d.getDate()).slice(-2);
        return "" + y + m + dd;
    }

    function addDaysPlain(plain, deltaDays) {
        var d = plainToDate(plain);
        if (!d) return "";
        d.setDate(d.getDate() + (deltaDays || 0));
        return dateToPlain(d);
    }

    function syncDateInputs(plainFrom, plainTo) {
        try {
            if (!$("#fromDate").length || !$("#toDate").length) return;
            var f = plainToDate(plainFrom);
            var t = plainToDate(plainTo);
            if (f) {
                var fPlain = dateToPlain(f);
                $("#fromDate").val(fPlain.substr(0, 4) + "-" + fPlain.substr(4, 2) + "-" + fPlain.substr(6, 2));
            }
            if (t) {
                var tPlain = dateToPlain(t);
                $("#toDate").val(tPlain.substr(0, 4) + "-" + tPlain.substr(4, 2) + "-" + tPlain.substr(6, 2));
            }
        } catch (e) {
            // ignore
        }
    }

    function extendMinuteRangeIfNeeded(ext, ohlc) {
        if (minuteRangeExtendInFlight) return;
        if (!lastQuery || !isMinuteDivCode(lastQuery.periodDivCode)) return;
        if (!ext || !isFinite(ext.min) || !isFinite(ext.max)) return;
        if (!ohlc || !ohlc.length) return;

        var range = ext.max - ext.min;
        if (!isFinite(range) || range <= 0) return;

        if (chart && chart.__lastRange == null) {
            chart.__lastRange = range;
            return;
        }

        var prevRange = chart ? chart.__lastRange : null;
        if (chart) chart.__lastRange = range;
        if (prevRange != null && range <= prevRange * 1.001) {
            return;
        }

        var first = ohlc[0][0];
        var last = ohlc[ohlc.length - 1][0];
        var edgeEps = 5 * 60 * 1000;
        var needPrev = ext.min <= (first + edgeEps);
        var needNext = ext.max >= (last - edgeEps);
        if (!needPrev && !needNext) return;

        var newFrom = lastQuery.fromDate;
        var newTo = lastQuery.toDate;
        if (needPrev) newFrom = addDaysPlain(newFrom, -1);
        if (needNext) newTo = addDaysPlain(newTo, 1);
        if (!newFrom || !newTo) return;
        if (newFrom === lastQuery.fromDate && newTo === lastQuery.toDate) return;

        minuteRangeExtendInFlight = true;
        syncDateInputs(newFrom, newTo);
        loadKisItemchartprice({
            stockCode: lastQuery.stockCode,
            fromDate: newFrom,
            toDate: newTo,
            periodDivCode: lastQuery.periodDivCode,
            orgAdjPrc: lastQuery.orgAdjPrc,
            stockMarket: lastQuery.stockMarket,
            stockCountryCode: lastQuery.stockCountryCode
        });
    }

    function bindMouseWheelZoom(chartObj) {
        if (!chartObj) return;
        if (isMobileDevice()) return;

        var container = chartObj.container;
        if (!container || !container.addEventListener) return;

        // Keep a single wheel handler per container. Chart is recreated often.
        container.__wheelZoomChart = chartObj;
        if (container.__wheelZoomHandler) return;

        var handler = function (e) {
            var activeChart = container.__wheelZoomChart || chart;
            if (!activeChart || !activeChart.xAxis || !activeChart.xAxis[0]) return;
            var axis = activeChart.xAxis[0];
            var ext = axis.getExtremes();
            if (!isFinite(ext.min) || !isFinite(ext.max)) return;

            var delta = e.deltaY;
            if (delta === undefined || delta === null) {
                delta = -e.wheelDelta;
            }
            if (!delta) return;

            e.preventDefault();

            var norm = (activeChart.pointer && activeChart.pointer.normalize) ? activeChart.pointer.normalize(e) : null;
            var anchor = null;
            if (norm && norm.chartX != null) {
                anchor = axis.toValue(norm.chartX, true);
            }
            if (!isFinite(anchor)) {
                anchor = (ext.min + ext.max) / 2;
            }

            var range = ext.max - ext.min;
            if (!isFinite(range) || range <= 0) return;

            var factor = (delta > 0) ? 1.25 : 0.8;
            var newRange = range * factor;

            // 과도 확대 방지: 최소 12봉은 보이도록 제한(간격 과벌어짐 방지)
            var minRange = axis.minRange || (60 * 1000);
            try {
                var s = (activeChart.get && activeChart.get("price")) || (activeChart.series && activeChart.series[0]);
                var xd = (s && s.xData && s.xData.length) ? s.xData : null;
                if (xd && xd.length >= 2) {
                    var step = Math.abs(xd[xd.length - 1] - xd[xd.length - 2]);
                    if (isFinite(step) && step > 0) {
                        var minBars = 12;
                        var byBars = step * minBars;
                        if (byBars > minRange) minRange = byBars;
                    }
                }
            } catch (e2) {
                // ignore
            }
            if (newRange < minRange) newRange = minRange;

            var pos = (anchor - ext.min) / range;
            if (!isFinite(pos)) pos = 0.5;
            if (pos < 0) pos = 0;
            if (pos > 1) pos = 1;
            var newMin = anchor - (newRange * pos);
            var newMax = newMin + newRange;

            var dataMin = ext.dataMin;
            var dataMax = ext.dataMax;
            if (isFinite(dataMin) && isFinite(dataMax)) {
                var dataRange = dataMax - dataMin;
                if (isFinite(dataRange) && dataRange > 0) {
                    if (newRange >= dataRange) {
                        newMin = dataMin;
                        newMax = dataMax;
                    } else {
                        if (newMin < dataMin) {
                            newMin = dataMin;
                            newMax = newMin + newRange;
                        }
                        if (newMax > dataMax) {
                            newMax = dataMax;
                            newMin = newMax - newRange;
                        }
                    }
                }
            }

            axis.setExtremes(newMin, newMax, true, false, { trigger: "mousewheel" });
        };

        container.addEventListener("wheel", handler, { passive: false });
        container.__wheelZoomHandler = handler;
    }

    function setUpDownClass($el, val, ref) {
        if (!$el || !$el.length) return;

        $el.removeClass("kis-up kis-down kis-flat");

        var v = parseFloat(val);
        var r = parseFloat(ref);
        if (isNaN(v) || isNaN(r)) {
            $el.addClass("kis-flat");
            return;
        }

        if (v > r) $el.addClass("kis-up");
        else if (v < r) $el.addClass("kis-down");
        else $el.addClass("kis-flat");
    }

    
    // ==========================
    // Price Label / Crosshair Label
    // ==========================
    function buildLastPriceLabelHtml(price, prevClose) {
        var p = parseFloat(price);
        var prev = parseFloat(prevClose);
        var diff = (isNaN(p) || isNaN(prev)) ? NaN : (p - prev);
        var pct = (isNaN(diff) || isNaN(prev) || prev === 0) ? NaN : (diff / prev * 100);

        var bg = "rgba(108,117,125,0.92)";
        if (!isNaN(diff)) {
            if (diff > 0) bg = "rgba(220,53,69,0.95)";
            else if (diff < 0) bg = "rgba(52,150,255,0.95)";
        }

        var priceText = formatNumber(p);
        var pctText = isNaN(pct) ? "" : formatSignedPct(pct);

        var html = ""
            + "<div style='display:inline-block;min-width:25px;text-align:right;"
            + "padding:2px 4px;border-radius:3px;line-height:1.12;font-size:7px;"
            + "color:#fff;background:" + bg + ";font-weight:700;'>"
            + priceText
            + (pctText ? ("<br/><span style='font-weight:600;opacity:0.95;font-size:8px;'>" + pctText + "</span>") : "")
            + "</div>";
        return html;
    }

        
    function lastPriceLineColor(price, prevClose) {
        var p = parseFloat(price);
        var prev = parseFloat(prevClose);
        var diff = (isNaN(p) || isNaN(prev)) ? NaN : (p - prev);

        if (!isNaN(diff)) {
            if (diff > 0) return "rgba(220,53,69,0.85)";
            if (diff < 0) return "rgba(52,150,255,0.85)";
        }
        return "rgba(108,117,125,0.75)";
    }

function removeLastPricePlotLine(chartObj) {
        try {
            if (chartObj && chartObj.yAxis && chartObj.yAxis[0] && chartObj.yAxis[0].removePlotLine) {
                chartObj.yAxis[0].removePlotLine("kisLastPriceLine");
            }
        } catch (e) {
            // ignore
        }
    }

    function removeLastPriceAxisLabel(chartObj) {
        try {
            if (chartObj && chartObj.kisLastPriceAxisLabel && typeof chartObj.kisLastPriceAxisLabel.destroy === "function") {
                chartObj.kisLastPriceAxisLabel.destroy();
            }
        } catch (e) {
            // ignore
        }
        if (chartObj) chartObj.kisLastPriceAxisLabel = null;
    }

    function refreshLastPriceAxisLabel(chartObj) {
        if (!chartObj) return;
        var p = parseFloat(chartObj.kisLastPriceValue);
        if (isNaN(p)) return;

        var prevClose = chartObj.kisLastPrevClose;
        var yAxis0 = (chartObj.yAxis && chartObj.yAxis[0]) ? chartObj.yAxis[0] : null;
        if (!yAxis0 || !chartObj.renderer) return;

        // y: chart 좌표(상단 기준)
        var y = yAxis0.toPixels(p, false);

        // x: 우측 가격축 라벨 영역 안쪽에 배치(잘림 방지)
        var x = chartObj.plotLeft + chartObj.plotWidth + 4;

        var html = buildLastPriceLabelHtml(p, prevClose);

        removeLastPriceAxisLabel(chartObj);

        // useHTML=true 로 2줄(가격/등락률) 표시
        // renderer.label(text, x, y, shape, anchorX, anchorY, useHTML)
        chartObj.kisLastPriceAxisLabel = chartObj.renderer
            .label(html, x, y - 18, "rect", null, null, true)
            .attr({ padding: 0, zIndex: 20 })
            .add();
    }

    // 마지막 가격 라인 + 가격축 라벨(가격 라인 위치에 붙임)
    function setLastPricePlotLine(chartObj, price, prevClose) {
        if (!chartObj || !chartObj.yAxis || !chartObj.yAxis[0] || typeof chartObj.yAxis[0].addPlotLine !== "function") return;
        var p = parseFloat(price);
        if (isNaN(p)) return;

        chartObj.kisLastPriceValue = p;
        chartObj.kisLastPrevClose = prevClose;

        removeLastPricePlotLine(chartObj);

        // 라벨은 plotLine에 붙이지 않고, 가격축 영역에 직접 렌더링한다(요청사항)
        chartObj.yAxis[0].addPlotLine({
            id: "kisLastPriceLine",
            value: p,
            width: 1,
            color: lastPriceLineColor(p, prevClose),
            zIndex: 9
        });

        // 최초 1회 + 이후 redraw 때마다 라벨 위치 보정
        refreshLastPriceAxisLabel(chartObj);

        try {
            if (!chartObj.kisLastPriceRedrawBound && typeof Highcharts !== "undefined" && Highcharts.addEvent) {
                chartObj.kisLastPriceRedrawBound = true;
                Highcharts.addEvent(chartObj, "redraw", function () {
                    try {
                        refreshLastPriceAxisLabel(chartObj);
                    } catch (e) { }
                });
            }
        } catch (e) {
            // ignore
        }
    }

    // ==========================
    // MA Support / Resistance Labels
    // ==========================
    function escapeHtml(s) {
        s = "" + (s == null ? "" : s);
        return s
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/\"/g, "&quot;")
            .replace(/\'/g, "&#39;");
    }

    function getMaLevelsFromSeries(maSeries) {
        var list = [];
        if (!Array.isArray(maSeries)) return list;

        for (var i = 0; i < maSeries.length; i++) {
            var s = maSeries[i];
            if (!s || !Array.isArray(s.data) || !s.data.length) continue;
            var last = s.data[s.data.length - 1];
            if (!last || last.length < 2) continue;
            var val = parseFloat(last[1]);
            if (isNaN(val)) continue;

            var label = s.maLabel || s.name || s.id || "MA";
            var period = parseInt(s.maPeriod || 0, 10);
            if (!period && s.id) {
                var m = String(s.id).match(/(\d+)/);
                if (m) period = parseInt(m[1], 10);
            }

            list.push({
                value: val,
                label: label,
                period: period,
                color: s.color || s.maColor || "#6c757d"
            });
        }

        return list;
    }

    function removeMaSupportResistanceLabels(chartObj) {
        try {
            if (chartObj && chartObj.kisMaSrLabels && chartObj.kisMaSrLabels.length) {
                for (var i = 0; i < chartObj.kisMaSrLabels.length; i++) {
                    var l = chartObj.kisMaSrLabels[i];
                    if (l && typeof l.destroy === "function") l.destroy();
                }
            }
        } catch (e) {
            // ignore
        }
        if (chartObj) chartObj.kisMaSrLabels = [];

        try {
            if (chartObj && chartObj.kisMaSrArrowKeys && chartObj.kisMaSrArrowKeys.length) {
                for (var j = 0; j < chartObj.kisMaSrArrowKeys.length; j++) {
                    var key = chartObj.kisMaSrArrowKeys[j];
                    if (!key) continue;
                    if (typeof hl_removeArrowText === "function") {
                        hl_removeArrowText(chartObj, key);
                    } else if (chartObj[key] && chartObj[key].destroy) {
                        chartObj[key].destroy();
                    }
                    try { chartObj[key] = null; } catch (e2) { }
                }
            }
        } catch (e3) {
            // ignore
        }
        if (chartObj) chartObj.kisMaSrArrowKeys = [];
    }

    function buildMaSupportResistanceLabelHtml(item, isSupport) {
        var typeText = isSupport ? "지지" : "저항";
        var typeBg = isSupport ? "rgba(52,150,255,0.95)" : "rgba(220,53,69,0.95)";
        var border = item && item.color ? item.color : "#6c757d";
        var label = escapeHtml(item && item.label ? item.label : "MA");
        var valueText = formatNumber(item && item.value);

        var html = ""
            + "<div style='display:inline-block;min-width:36px;text-align:right;"
            + "padding:2px 4px;border-radius:3px;line-height:1.1;font-size:9px;"
            + "background:rgba(255,255,255,0.92);border:1px solid " + border + ";'>"
            + "<div style='white-space:nowrap;font-weight:700;'>"
            + "<span style='display:inline-block;padding:1px 3px;border-radius:2px;"
            + "background:" + typeBg + ";color:#fff;font-size:9px;'>" + typeText + "</span>"
            + "<span style='margin-left:3px;color:" + border + ";font-size:9px;'>" + label + "</span>"
            + "</div>"
            + "<div style='text-align:right;font-weight:700;font-size:9px;color:#111;'>" + valueText + "</div>"
            + "</div>";
        return html;
    }

    function drawMaSupportResistanceLabels(chartObj, levels, currentPrice) {
        if (!chartObj) return;

        removeMaSupportResistanceLabels(chartObj);

        var p = parseFloat(currentPrice);
        if (isNaN(p) || !levels || !levels.length) return;

        var useArrow = (typeof hl_drawLevelArrowText === "function");
        if (useArrow) {
            var keys = [];
            var sorted = levels.slice().sort(function (a, b) {
                return parseFloat(b.value) - parseFloat(a.value);
            });

            for (var i = 0; i < sorted.length; i++) {
                var it = sorted[i];
                if (!it || isNaN(parseFloat(it.value))) continue;

                var isSupport = p >= parseFloat(it.value);
                var typeText = isSupport ? "지지" : "저항";
                var pctText = "";
                var levelValue = parseFloat(it.value);
                if (!isNaN(p) && !isNaN(levelValue) && levelValue !== 0) {
                    pctText = formatSignedPct(((p - levelValue) / levelValue) * 100);
                }
                var valueText = formatNumberByMarket(levelValue);
                var labelText = typeText + " " + valueText + (pctText ? (" (" + pctText + ")") : "");
                var color = (it && it.color) ? it.color : "#6c757d";

                var key = "__maSrFlag_" + i;
                hl_drawLevelArrowText(chartObj, key, it.value, labelText, color);
                keys.push(key);
            }

            chartObj.kisMaSrArrowKeys = keys;
        } else {
            if (!chartObj.renderer || !chartObj.yAxis || !chartObj.yAxis[0]) return;

            var yAxis0 = chartObj.yAxis[0];
            var right = chartObj.plotLeft + chartObj.plotWidth;
            var top = chartObj.plotTop;
            var bottom = chartObj.plotTop + chartObj.plotHeight;

            var labels = [];
            for (var j = 0; j < levels.length; j++) {
                var it2 = levels[j];
                if (!it2 || isNaN(parseFloat(it2.value))) continue;

                var y = yAxis0.toPixels(it2.value, false);
                if (y < top - 4 || y > bottom + 4) continue;

                var isSupport2 = p >= parseFloat(it2.value);
                var html = buildMaSupportResistanceLabelHtml(it2, isSupport2);

                var lb = chartObj.renderer
                    .label(html, 0, 0, "rect", null, null, true)
                    .attr({ padding: 0, zIndex: 18 })
                    .add();

                var bbox = lb.getBBox();
                var xText = right - 4 - bbox.width;
                if (xText < chartObj.plotLeft + 2) xText = chartObj.plotLeft + 2;

                var yText = y - (bbox.height / 2);
                if (yText < top + 2) yText = top + 2;
                if (yText + bbox.height > bottom - 2) yText = bottom - 2 - bbox.height;

                lb.attr({ x: xText, y: yText });
                labels.push(lb);
            }
            chartObj.kisMaSrLabels = labels;
        }

        try {
            if (!chartObj.kisMaSrRedrawBound && typeof Highcharts !== "undefined" && Highcharts.addEvent) {
                chartObj.kisMaSrRedrawBound = true;
                Highcharts.addEvent(chartObj, "redraw", function () {
                    try {
                        refreshMaSupportResistance(chartObj);
                    } catch (e) { }
                });
            }
        } catch (e) {
            // ignore
        }
    }

    function setMaSupportResistance(chartObj, levels, currentPrice) {
        if (!chartObj) return;
        chartObj.kisMaSrLevels = Array.isArray(levels) ? levels.slice() : [];
        chartObj.kisMaSrCurrentPrice = currentPrice;
        drawMaSupportResistanceLabels(chartObj, chartObj.kisMaSrLevels, currentPrice);
    }

    function refreshMaSupportResistance(chartObj, currentPrice) {
        if (!chartObj) return;
        if (currentPrice !== undefined && currentPrice !== null) {
            chartObj.kisMaSrCurrentPrice = currentPrice;
        }
        drawMaSupportResistanceLabels(chartObj, chartObj.kisMaSrLevels || [], chartObj.kisMaSrCurrentPrice);
    }

function updateOhlcHeader(ts, o, h, l, c, periodDivCode, refClose) {
        // 날짜
        if ($("#kisHdrDt").length) {
            $("#kisHdrDt").text(formatDateLabel(ts, periodDivCode));
        }

        // 값 세팅
        if ($("#kisHdrO").length) $("#kisHdrO").text(formatNumber(o));
        if ($("#kisHdrH").length) $("#kisHdrH").text(formatNumber(h));
        if ($("#kisHdrL").length) $("#kisHdrL").text(formatNumber(l));
        if ($("#kisHdrC").length) $("#kisHdrC").text(formatNumber(c));

        // 색상: "전일 종가(prev close)" 기준으로 시/고/저/종 각각 비교
        var ref = parseFloat(refClose);
        if (isNaN(ref)) {
            // fallback: 기준값이 없으면 시가를 기준으로 처리
            ref = parseFloat(o);
        }

        setUpDownClass($("#kisHdrO"), o, ref);
        setUpDownClass($("#kisHdrH"), h, ref);
        setUpDownClass($("#kisHdrL"), l, ref);
        setUpDownClass($("#kisHdrC"), c, ref);
    }

    function bindOhlcHover(chartObj, rows, periodDivCode) {
        if (!chartObj || !rows || !rows.length) return;

        var last = rows[rows.length - 1];
        var lastRef = (rows.length > 1) ? rows[rows.length - 2].c : last.o;
        updateOhlcHeader(last.t, last.o, last.h, last.l, last.c, periodDivCode, lastRef);
        updateTradeHeader(last.v, last.a);

        var priceSeries = chartObj.get("price");
        if (!priceSeries) return;

        function onMove(e) {
            try {
                var ev = chartObj.pointer.normalize(e);
                var p = priceSeries.searchPoint(ev, true);
                if (p) {
                    // candlestick point: open/high/low/close 제공
                    var idx = (p.index !== undefined && p.index !== null) ? p.index : -1;
                    if (idx < 0 && priceSeries && priceSeries.xData) {
                        idx = priceSeries.xData.indexOf(p.x);
                    }
                    var prevClose = null;
                    if (idx > 0 && priceSeries && priceSeries.points && priceSeries.points[idx - 1]) {
                        prevClose = priceSeries.points[idx - 1].close;
                    }
                    if (prevClose === null || prevClose === undefined || isNaN(parseFloat(prevClose))) {
                        prevClose = p.open;
                    }
                    updateOhlcHeader(p.x, p.open, p.high, p.low, p.close, periodDivCode, prevClose);
                    if (idx >= 0 && rows && rows[idx]) {
                        updateTradeHeader(rows[idx].v, rows[idx].a);
}
                }
            } catch (ex) {
                // ignore
            }
        }

        function onLeave() {
            var last2 = rows[rows.length - 1];
            var last2Ref = (rows.length > 1) ? rows[rows.length - 2].c : last2.o;
            updateOhlcHeader(last2.t, last2.o, last2.h, last2.l, last2.c, periodDivCode, last2Ref);
            updateTradeHeader(last2.v, last2.a);
}

        // 기존 바인딩이 중복되지 않도록 네임스페이스 사용
        if (chartObj.container) {
            $(chartObj.container)
                .off(".ohlcHdr")
                .on("mousemove.ohlcHdr touchmove.ohlcHdr", onMove)
                .on("mouseleave.ohlcHdr touchend.ohlcHdr", onLeave);
        }
    }

    function getVisibleOhlcForHighLow(allOhlc, min, max) {
        if (!allOhlc || !allOhlc.length) return [];
        if (min === undefined || min === null || max === undefined || max === null) return allOhlc.slice();
        var visible = [];
        for (var i = 0; i < allOhlc.length; i++) {
            var t = allOhlc[i][0];
            if (t >= min && t <= max) visible.push(allOhlc[i]);
        }
        return visible.length ? visible : allOhlc.slice();
    }

    function updatePriceCandleWidth(chartObj, axis, allOhlc) {
        if (!chartObj || !axis || !allOhlc || !allOhlc.length) return;
        var series = (chartObj.get && chartObj.get("price")) || (chartObj.series && chartObj.series[0]);
        if (!series) return;

        var min = axis.min;
        var max = axis.max;
        if (min === undefined || min === null || max === undefined || max === null) return;

        var visibleCount = 0;
        for (var i = 0; i < allOhlc.length; i++) {
            var t = allOhlc[i][0];
            if (t < min) continue;
            if (t > max) break;
            visibleCount++;
        }
        if (!visibleCount) visibleCount = 1;

        var plotWidth = chartObj.plotWidth || 0;
        if (!plotWidth) return;

        var spacingPx = plotWidth / Math.max(visibleCount, 1);

        // HTS 스타일: 축소 레벨에 따른 캔들 너비 조정
        // 요청 반영: "최대 확대 시" 캔들이 크게 보이도록 상한/비율 확대
        var targetWidth, targetLineWidth;

        if (spacingPx <= 2) {
            targetWidth = 1;
            targetLineWidth = 1;
        } else if (spacingPx <= 4) {
            targetWidth = 1;
            targetLineWidth = 1;
        } else if (spacingPx <= 6) {
            targetWidth = Math.max(1, Math.floor(spacingPx * 0.40));
            targetLineWidth = 1;
        } else if (spacingPx <= 10) {
            targetWidth = Math.max(2, Math.floor(spacingPx * 0.55));
            targetLineWidth = 1;
        } else {
            // 최대 확대 구간: 캔들 바디를 넓게(증권앱 스타일)
            targetWidth = Math.max(3, Math.floor(spacingPx * 0.72));
            targetLineWidth = 1;
        }

        if (!isFinite(targetWidth) || targetWidth <= 0) targetWidth = 1;
        if (targetWidth > 18) targetWidth = 18;
        if (series.options
            && series.options.pointWidth === targetWidth
            && series.options.lineWidth === targetLineWidth) {
            return;
        }
        series.update({ pointWidth: targetWidth, lineWidth: targetLineWidth }, false);
        chartObj.redraw(false);
    }

    function clearHighLowMarkers(chartObj) {
        if (!chartObj) return;
        try {
            if (chartObj.yAxis && chartObj.yAxis[0] && chartObj.yAxis[0].removePlotLine) {
                chartObj.yAxis[0].removePlotLine("previousHighLine");
                chartObj.yAxis[0].removePlotLine("previousLowLine");
            }
            var s1 = chartObj.get("previousHighFlag");
            if (s1) s1.remove(false);
            var s2 = chartObj.get("previousLowFlag");
            if (s2) s2.remove(false);
			try {
			    if (chartObj.__hlPrevHighText && chartObj.__hlPrevHighText.destroy) {
			        chartObj.__hlPrevHighText.destroy();
			    }
			    chartObj.__hlPrevHighText = null;
			    if (chartObj.__hlPrevLowText && chartObj.__hlPrevLowText.destroy) {
			        chartObj.__hlPrevLowText.destroy();
			    }
			    chartObj.__hlPrevLowText = null;
			} catch (e2) {
			    // ignore
			}
            chartObj.redraw();
        } catch (e) {
            // ignore
        }
    }    
    function convertHeader(list) {
        if (!list || !list.length) {
            $("#kisStockName").text("");
            $("#kisStockPrice").text("-");
            $("#kisStockDate").text("-");
            return;
        }
        var last = list[list.length - 1];

        // 상단(종목명/코드/시장) 기본값 세팅
        headerStatic.code = (lastQuery && lastQuery.stockCode) ? lastQuery.stockCode : (pickString(last, ["stck_shrn_iscd", "code"]) || "-");
        headerStatic.name = pickString(last, ["hts_kor_isnm", "stck_nm", "stockName", "name", "prdt_name", "kor_isnm", "itmsNm", "item_name"]) || headerStatic.name;
        headerStatic.market = pickString(last, ["market", "mrkt", "mrkt_div", "mrkt_ctg", "exch", "excd", "marketName", "mkttype"]) || headerStatic.market;

        var price = parseFloat(last.close || last.stck_prpr || 0);
        $("#kisStockName").text("KIS 기간별 시세");
        $("#kisStockPrice").text(price ? "종가: " + price.toLocaleString() : "-");

        var ts = toEpochMillis(last.date || last.time);
        if (!isNaN(ts)) {
            var d = new Date(ts);
            $("#kisStockDate").text(
                "기준일자: " +
                d.getFullYear() + "-" +
                ("0" + (d.getMonth() + 1)).slice(-2) + "-" +
                ("0" + d.getDate()).slice(-2)
            );
        } else {
            $("#kisStockDate").text("-");
        }
        // 상단 헤더(시/고/저/종) 갱신
        var op = parseFloat(last.open);
        var hi = parseFloat(last.high);
        var lo = parseFloat(last.low);
        var cl = parseFloat(last.close);
        var pdc = (lastQuery && lastQuery.periodDivCode) ? lastQuery.periodDivCode : "D";
        var prevClose = null;
        if (list.length > 1) {
            var prev = list[list.length - 2] || {};
            prevClose = parseFloat(prev.close);
        }
        updateOhlcHeader(ts, op, hi, lo, cl, pdc, prevClose);
        // 거래량/거래대금 상단 헤더 갱신
        var v = pickNumber(last, ["volume", "acml_vol", "acml_volu", "cntg_vol", "trqu"]);
        var a = pickNumber(last, ["amount", "acml_tr_pbmn", "acml_tr_amt", "tr_amt", "tr_pbmn"]);
        updateTradeHeader(v, a);

        // fallback: '현재가 조회' 전에는 마지막 종가/전일종가 기준으로 표시
        updateTopHeader(cl, prevClose);

        // 종목명/시장 보강 (DB 키워드 조회 기반)
        try {
            if (isIndexCode(headerStatic.code)) {
                applyIndexHeaderStatic(headerStatic.code);
            } else {
                fetchStockMeta(headerStatic.code);
            }
        } catch (e) {
            // ignore
        }
}

    function normalizeRows(list) {
        var rows = [];
        for (var i = 0; i < list.length; i++) {
            var it = list[i] || {};
            var t = toEpochMillis(it.date || it.time);
            var op = parseFloat(it.open);
            var hi = parseFloat(it.high);
            var lo = parseFloat(it.low);
            var cl = parseFloat(it.close);
            var vol = pickNumber(it, ["volume", "acml_vol", "acml_volu", "cntg_vol", "trqu"]);
            var amt = pickNumber(it, ["amount", "acml_tr_pbmn", "acml_tr_amt", "tr_amt", "tr_pbmn"]);
            if (isNaN(t) || isNaN(op) || isNaN(hi) || isNaN(lo) || isNaN(cl)) continue;
            rows.push({
                t: t,
                o: op,
                h: hi,
                l: lo,
                c: cl,
                v: isNaN(vol) ? 0 : vol,
                a: isNaN(amt) ? NaN : amt
            });
        }
        rows.sort(function (a, b) {
            return a.t - b.t;
        });
        return rows;
    }

    function renderKisChart(list, periodDivCode) {
        if (!list || !list.length) {
            alert("차트에 표시할 데이터가 없습니다.");
            return;
        }

        var rows = normalizeRows(list);
        if (!rows.length) {
            alert("유효한 데이터가 없습니다.");
            return;
        }

        var ohlc = [];
        var volume = [];
        var times = [];
        var closes = [];
        var upperPeriod = (periodDivCode || "").toUpperCase();
        var doubleChartActive = !!options.doubleChartEnabled && upperPeriod === "D";
        var axisPeriod = upperPeriod || ($("#periodDivCode").val() || "D").toUpperCase();

        for (var i = 0; i < rows.length; i++) {
            var r = rows[i];
            ohlc.push([r.t, r.o, r.h, r.l, r.c]);
            volume.push([r.t, r.v]);
            times.push(r.t);
            closes.push(r.c);
        }

        // 월봉 오버레이 + 월 경계선 생성 (DoubleMonthChartScript 사용)
        var monthlyInfo = DoubleMonthChartScript.buildMonthlyOverlayFromDaily(ohlc);
        monthBoundaryTimes = monthlyInfo.boundaries || [];

        var yAxis = [{
            labels: {
                align: "left",
                x: 0,
                style: { fontSize: "9px" },
                formatter: function () {
                        return formatVolumeMan(this.value);
                }
            },
            crosshair: {
                width: 1,
                color: "rgba(0,0,0,0.35)",
                dashStyle: "dash",
                snap: false,
                label: {
                    enabled: true,
                    format: "{value:,.0f}",
                    padding: 4,
                    backgroundColor: "rgba(255,255,255,0.92)",
                    borderColor: "rgba(0,0,0,0.25)",
                    style: { color: "#000", fontWeight: "700", fontSize: "9px" }
                }
            },
            height: options.volumeEnabled ? "65%" : "100%",
            lineWidth: 2,
            resize: { enabled: true }
        }];

        var series = [{
            type: "candlestick",
            name: "가격",
            id: "price",
            data: ohlc,
            yAxis: 0,
            zIndex: 3,
            dataGrouping: { enabled: false },
            lastPrice: { enabled: false },
            lastVisiblePrice: { enabled: false }
        }];

        if (doubleChartActive) {
            var monthOverlay = monthlyInfo.overlay || [];
            if (monthOverlay.length) {
                series.push({
                    type: "candlestick",
                    name: "월봉",
                    id: "monthOverlay",
                    linkedTo: "price",
                    zIndex: 1,
                    data: monthOverlay,
                    yAxis: 0,
                    upColor: options.doubleUpColor,
                    upLineColor: options.doubleUpColor,
                    color: options.doubleDownColor,
                    lineColor: options.doubleDownColor,
                    pointWidth: null,
                    lineWidth: 2,
                    dataGrouping: { enabled: false },
                    zIndex: 1
                });
            }
        }

        // MA 시리즈는 MaScript 에서 생성
        var maSeries = MaScript.buildMaSeries(options, times, closes);
        var maLevels = getMaLevelsFromSeries(maSeries);
        if (maSeries && maSeries.length) {
            for (var ms = 0; ms < maSeries.length; ms++) {
                var s = maSeries[ms];

                // 이동평균선 강조: 두께/레이어/마커 정리
                if (s) {
                    s.type = s.type || "line";
                    s.zIndex = (s.zIndex !== undefined && s.zIndex !== null) ? s.zIndex : 7;
                    s.lineWidth = (s.lineWidth !== undefined && s.lineWidth !== null) ? s.lineWidth : 2.5;
                    s.marker = s.marker || { enabled: false };

                    // Hover 시에도 캔들이 흐려지지 않도록(전역 inactive 비활성화와 함께) 라인만 살짝 강조
                    s.states = s.states || {};
                    s.states.hover = s.states.hover || {};
                    if (s.states.hover.lineWidth === undefined || s.states.hover.lineWidth === null) {
                        s.states.hover.lineWidth = Math.max(3, s.lineWidth + 0.5);
                    }
                }

                series.push(s);
            }
        }


        // 골든/데드 크로스(기본 5/20) - CrossSignalScript 사용
        if (typeof CrossSignalScript !== "undefined" && CrossSignalScript && typeof CrossSignalScript.buildCrossFlagSeries === "function") {
            var crossSeries = CrossSignalScript.buildCrossFlagSeries(options, times, closes);
            if (crossSeries) {
                series.push(crossSeries);
            }
        }

        if (options.volumeEnabled) {
            yAxis.push({
                labels: {
                    align: "left",
                    x: 0,
                    style: { fontSize: "9px" },
                    formatter: function () {
                        return formatVolumeMan(this.value);
                    }
                },                
                top: "70%",
                height: "30%",
                offset: 0,
                lineWidth: 2
            });
            series.push({
                type: "column",
                name: "거래량",
                id: "volume",
                data: volume,
                yAxis: 1,
                dataGrouping: { enabled: false }
            });
        }

        if (chart) chart.destroy();

        var stockOptions = {
            chart: {
                animation: false,
                spacingTop: 2,
                spacingRight: 30,
                spacingBottom: 2,
                spacingLeft: 2
            },
            rangeSelector: { enabled: false },
            navigator: { enabled: false },
            scrollbar: { enabled: true },
            exporting: {
                enabled: false,
                buttons: {
                    contextButton: { enabled: false }
                }
            },
            xAxis: [{
                crosshair: {
                    width: 1,
                    color: "black",
                    dashStyle: "dash"
                },
                labels: {
                    rotation: 0,
                    autoRotation: false,
                    style: {
                        fontSize: '10px'
                    },
                    formatter: function () {
                        return formatAxisLabel(this.value, axisPeriod, this);
                    }
                },
                events: {
                    afterSetExtremes: function () {
                        var periodDivCodeVal = ($("#periodDivCode").val() || "D").toUpperCase();
                        var allowDouble = options.doubleChartEnabled && periodDivCodeVal === "D";
                        updatePriceCandleWidth(chart, this, ohlc);
                        if (allowDouble) {
                            DoubleMonthChartScript.updateMonthOverlayPointWidth(
                                chart,
                                monthBoundaryTimes,
                                this,
                                options,
                                periodDivCodeVal
                            );
                        }

                        var ext = this.getExtremes ? this.getExtremes() : { min: null, max: null };
                        // 요청 반영: 전고/전저 점선/보조라인 제거
                        clearHighLowMarkers(chart);

                        extendMinuteRangeIfNeeded(ext, ohlc);

                        try {
                            refreshLastPriceAxisLabel(chart);
                            if (options.maSrEnabled) {
                                refreshMaSupportResistance(chart);
                            }
                        } catch (e) {
                            // ignore
                        }
                    }
                },
                plotLines: (function () {
                    var periodDivCodeVal = ($("#periodDivCode").val() || "D").toUpperCase();
                    if (!options.monthLinesEnabled) return [];
                    return DoubleMonthChartScript.buildMonthPlotLines(
                        times,
                        periodDivCodeVal,
                        monthBoundaryTimes
                    );
                })()
            }],
            yAxis: yAxis,
            tooltip: { enabled: false },
            plotOptions: {
                series: {
                    states: {
                        // 다른 시리즈(이동평균선 등) Hover 시 캔들이 흐려지는(inactive dimming) 효과 제거
                        inactive: { enabled: false }
                    }
                },
                candlestick: {
                    dataGrouping: { enabled: false },
                    upColor: "red",
                    upLineColor: "red",
                    color: "#3496ff",
                    lineColor: "#3496ff"
                },
                column: { dataGrouping: { enabled: false } },
                line: { dataGrouping: { enabled: false } }
            },
            series: series
        };

        stockOptions = applyMobilePanFix(stockOptions);
        chart = Highcharts.stockChart("kisChartContainer", stockOptions);
        bindMouseWheelZoom(chart);
        try {
            var periodDivCodeVal0 = ($("#periodDivCode").val() || "D").toUpperCase();
            if (chart && chart.xAxis && chart.xAxis[0]) {
                updatePriceCandleWidth(chart, chart.xAxis[0], ohlc);
                if (options.doubleChartEnabled && periodDivCodeVal0 === "D") {
                    DoubleMonthChartScript.updateMonthOverlayPointWidth(
                        chart,
                        monthBoundaryTimes,
                        chart.xAxis[0],
                        options,
                        periodDivCodeVal0
                    );
                }
            }
        } catch (e) {
            // ignore
        }

        // redraw 이벤트 핸들러: 윈도우 리사이즈 등 다른 redraw에서도 월봉 너비 재조정
        try {
            if (doubleChartActive) {
                Highcharts.addEvent(chart, 'redraw', function () {
                    try {
                        var pdv = ($("#periodDivCode").val() || "D").toUpperCase();
                        if (options.doubleChartEnabled && pdv === "D" && chart && chart.xAxis && chart.xAxis[0]) {
                            DoubleMonthChartScript.updateMonthOverlayPointWidth(
                                chart, monthBoundaryTimes, chart.xAxis[0], options, pdv
                            );
                        }
                    } catch (e) { }
                });
            }
        } catch (e) {
            // ignore
        }

        if (isMobileDevice()) {
            try {
                var $hc = $("#kisChartContainer .highcharts-container");
                if ($hc && $hc.length) $hc.css("touch-action", "pan-y");
            } catch (e) { }
            try {
                if (chart && chart.container) {
                    chart.container.style.cursor = "grab";
                }
            } catch (e2) { }
        }

        // 현재가(마지막 종가) 라벨(축 우측)
        try {
            var lastRow = rows[rows.length - 1];
            var prevRowClose = (rows.length > 1) ? rows[rows.length - 2].c : lastRow.o;
            setLastPricePlotLine(chart, lastRow.c, prevRowClose);
            if (options.maSrEnabled && maLevels && maLevels.length) {
                setMaSupportResistance(chart, maLevels, lastRow.c);
            } else {
                if (chart) {
                    chart.kisMaSrLevels = [];
                    chart.kisMaSrCurrentPrice = null;
                    removeMaSupportResistanceLabels(chart);
                }
            }
        } catch (e) {
            // ignore
        }

        // 툴팁은 껐지만(tooltip: false), 마우스 이동으로 상단 시/고/저/종을 갱신
        bindOhlcHover(chart, rows, upperPeriod);

        // 현재 선택 종목의 기본정보/현재가를 조회하여 상단 표시를 '현재 기준'으로 맞춘다.
        try {
            var sc = (lastQuery && lastQuery.stockCode) ? lastQuery.stockCode : ($("#stockCode").val() || "");
            if (sc) {
                if (isIndexCode(sc)) {
                    applyIndexHeaderStatic(sc);
                } else {
                    fetchStockMeta(sc);
                    fetchCurrentPrice(sc);
                }
            }
        } catch (e) {
            // ignore
        }

        
        // 요청 반영: 전고/전저 점선/보조라인 제거
        clearHighLowMarkers(chart);

        // 플래그 제거 요청: 이벤트 플래그 시리즈 비활성화
        try {
            var oldEventMarkers = chart && chart.get ? chart.get("eventMarkers") : null;
            if (oldEventMarkers) oldEventMarkers.remove(false);
        } catch (e) {
            // ignore
        }

        // WF-2-2b PositionState: 현재 포지션 상태 오버레이
        try {
            loadPositionStateOverlay();
        } catch (e) {
            // ignore
        }

        // WF-2-2b RegimeOverlay: KR/US 레짐 점수 배지
        try {
            loadRegimeOverlay();
        } catch (e) {
            // ignore
        }

    }

    function regimeTrendText(score) {
        var n = parseFloat(String(score == null ? "" : score).replace(/,/g, ""));
        if (isNaN(n)) return "추세 확인중";
        if (n >= 5) return "상승추세";
        if (n >= 1) return "약상승";
        if (n <= -5) return "하락추세";
        if (n <= -1) return "약하락";
        return "횡보";
    }

    function renderExplainPanel() {
        var el = document.getElementById("kisExplainPanel");
        if (!el) return;

        // 사용자 요청: 상단 설명 패널은 사용하지 않음(차트 내 표기만 유지)
        el.style.display = "none";
        el.textContent = "";
        return;

        var m = explainCache.markerTop;
        var p = explainCache.positionState;
        var r = explainCache.regime;

        var parts = [];
        if (m) {
            parts.push("신호 " + (m.EVENT_TYPE || m.event_type || "-") + " (" + (m.EVENT_SOURCE || m.event_source || "-") + ")");
        }
        if (p) {
            var st = p.LAST_EVENT_TYPE || p.last_event_type || p.STATE_CODE || p.state_code || "HOLD";
            var qty = p.TOTAL_QTY || p.total_qty || "0";
            var avg = p.AVG_PRICE || p.avg_price || "0";
            parts.push("포지션 " + st + " · 수량 " + qty + " · 평단 " + avg);
        }
        // 사용자 요청: 시장바람 지수 설명은 차트 배지에만 표시(상단 설명 패널 중복 제거)

        if (!parts.length) {
            el.textContent = "설명 데이터 없음";
            return;
        }

        if (isMobileDevice()) {
            el.textContent = parts.join(" · ");
        } else {
            el.textContent = "설명: " + parts.join(" | ");
        }
    }

    function parseEventTimeKeyToTs(v) {
        var s = (v == null ? "" : String(v)).replace(/\D/g, "");
        if (s.length < 8) return NaN;
        var y = parseInt(s.substring(0, 4), 10);
        var m = parseInt(s.substring(4, 6), 10) - 1;
        var d = parseInt(s.substring(6, 8), 10);
        var hh = s.length >= 10 ? parseInt(s.substring(8, 10), 10) : 0;
        var mm = s.length >= 12 ? parseInt(s.substring(10, 12), 10) : 0;
        var ss = s.length >= 14 ? parseInt(s.substring(12, 14), 10) : 0;
        return new Date(y, m, d, hh, mm, ss).getTime();
    }

    function nearestChartTime(ts, times) {
        if (!times || !times.length || isNaN(ts)) return ts;
        var best = times[0];
        var bestDiff = Math.abs(best - ts);
        for (var i = 1; i < times.length; i++) {
            var d = Math.abs(times[i] - ts);
            if (d < bestDiff) {
                best = times[i];
                bestDiff = d;
            }
        }
        return best;
    }

    function markerShapeByType(type) {
        var t = String(type || "").toUpperCase();
        if (t.indexOf("RISK") >= 0 || t.indexOf("CROSSDOWN") >= 0 || t.indexOf("DEAD") >= 0) return "circlepin";
        if (t.indexOf("TREND") >= 0 || t.indexOf("BULL") >= 0 || t.indexOf("ENTER") >= 0) return "flag";
        if (t.indexOf("EXIT") >= 0) return "squarepin";
        return "flag";
    }

    function markerColorByType(type) {
        var t = String(type || "").toUpperCase();
        if (t.indexOf("RISK") >= 0 || t.indexOf("CROSSDOWN") >= 0 || t.indexOf("DEAD") >= 0) return "#dc2626";
        if (t.indexOf("TREND") >= 0 || t.indexOf("BULL") >= 0 || t.indexOf("ENTER") >= 0) return "#16a34a";
        if (t.indexOf("AVERAGE") >= 0) return "#2563eb";
        if (t.indexOf("EXIT") >= 0) return "#6b7280";
        return "#0f172a";
    }

    function loadChartEventMarkers(times) {
        if (!chart || !lastQuery || !lastQuery.stockCode || isIndexCode(lastQuery.stockCode)) return;
        if (!window.jQuery) return;

        var groupId = "";
        try {
            var g = document.getElementById("wg_group_id");
            groupId = g ? $.trim(g.value || "") : "";
        } catch (e) {
            groupId = "";
        }

        var url = (window.__URLS && window.__URLS.selectChartEventMarkers)
            ? window.__URLS.selectChartEventMarkers
            : "/finance/selectChartEventMarkers.do";

        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            data: {
                stock_code: lastQuery.stockCode,
                group_id: groupId,
                limit: 30
            },
            timeout: 15000
        }).done(function (res) {
            var list = [];
            if (res && res.data && $.isArray(res.data)) list = res.data;
            else if ($.isArray(res)) list = res;
            else if (res && res.data && res.data.data && $.isArray(res.data.data)) list = res.data.data;

            if (!list || !list.length) {
                explainCache.markerTop = null;
                renderExplainPanel();
                var old0 = chart.get("eventMarkers");
                if (old0) old0.remove(false);
                chart.redraw(false);
                return;
            }

            explainCache.markerTop = list[0] || null;
            renderExplainPanel();

            var pts = [];
            var maxMarkers = isMobileDevice() ? 8 : 20;
            for (var i = 0; i < list.length && i < maxMarkers; i++) {
                var r = list[i] || {};
                var key = r.EVENT_TIME_KEY || r.event_time_key || r.EVENT_TIME || r.event_time;
                var t = nearestChartTime(parseEventTimeKeyToTs(key), times);
                if (isNaN(t)) continue;
                var typ = r.EVENT_TYPE || r.event_type || "EV";
                var msg = r.MESSAGE || r.message || "";
                var tt = String(typ || "").toUpperCase();
                var shortTitle = "•";
                if (tt.indexOf("RISK") >= 0 || tt.indexOf("CROSSDOWN") >= 0) shortTitle = "위";
                else if (tt.indexOf("TREND") >= 0 || tt.indexOf("BULL") >= 0 || tt.indexOf("ENTER") >= 0) shortTitle = "추";
                else if (tt.indexOf("EXIT") >= 0) shortTitle = "종";
                else if (tt.indexOf("AVERAGE") >= 0) shortTitle = "물";

                pts.push({
                    x: t,
                    title: shortTitle,
                    text: "[" + typ + "] " + msg,
                    shape: markerShapeByType(typ),
                    fillColor: markerColorByType(typ)
                });
            }

            var old = chart.get("eventMarkers");
            if (old) old.remove(false);

            if (pts.length) {
                chart.addSeries({
                    id: "eventMarkers",
                    type: "flags",
                    name: "이벤트",
                    data: pts,
                    onSeries: "price",
                    yAxis: 0,
                    shape: "flag",
                    width: 14,
                    style: { color: "#fff", fontSize: "9px" },
                    dataGrouping: { enabled: false }
                }, false);
            }
            chart.redraw(false);
        });
    }

    function loadPositionStateOverlay() {
        if (!chart || !lastQuery || !lastQuery.stockCode || isIndexCode(lastQuery.stockCode)) return;
        if (!window.jQuery) return;

        var groupId = "";
        try {
            var g = document.getElementById("wg_group_id");
            groupId = g ? $.trim(g.value || "") : "";
        } catch (e) {
            groupId = "";
        }

        var url = (window.__URLS && window.__URLS.selectPositionState)
            ? window.__URLS.selectPositionState
            : "/finance/selectPositionState.do";

        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            data: {
                stock_code: lastQuery.stockCode,
                group_id: groupId
            },
            timeout: 15000
        }).done(function (res) {
            var d = null;
            if (res && res.data && res.data.singleData) d = res.data.singleData;
            else if (res && res.data && !$.isArray(res.data)) d = res.data;
            if (!d) {
                explainCache.positionState = null;
                renderExplainPanel();
                if (chart.kisPositionStateLabel) {
                    chart.kisPositionStateLabel.destroy();
                    chart.kisPositionStateLabel = null;
                }
                try {
                    if (chart && chart.yAxis && chart.yAxis[0]) {
                        chart.yAxis[0].removePlotLine("riskGuardLine");
                    }
                } catch (e) {
                    // ignore
                }
                return;
            }

            explainCache.positionState = d;
            renderExplainPanel();

            var state = d.LAST_EVENT_TYPE || d.last_event_type || d.STATE_CODE || d.state_code || "HOLD";
            var qty = d.TOTAL_QTY || d.total_qty || "0";
            var avg = d.AVG_PRICE || d.avg_price || "0";
            var avgNum = parseFloat(String(avg).replace(/,/g, ""));
            var text = isMobileDevice()
                ? ("상태 " + state)
                : ("STATE " + state + " · qty " + qty + " · avg " + avg);

            var bg = "#334155";
            var st = String(state).toUpperCase();
            if (st.indexOf("RISK") >= 0) bg = "#b91c1c";
            else if (st.indexOf("TREND") >= 0) bg = "#166534";
            else if (st.indexOf("AVERAGE") >= 0) bg = "#1d4ed8";
            else if (st.indexOf("EXIT") >= 0 || st.indexOf("CLOSE") >= 0) bg = "#6b7280";

            if (chart.kisPositionStateLabel) {
                chart.kisPositionStateLabel.destroy();
                chart.kisPositionStateLabel = null;
            }

            chart.kisPositionStateLabel = chart.renderer
                .label(text, 10, 8, "rect", null, null, true)
                .attr({
                    zIndex: 9,
                    r: 4,
                    fill: bg,
                    padding: 4
                })
                .css({
                    color: "#fff",
                    fontSize: "10px",
                    fontWeight: "700"
                })
                .add();

            // WF-2-2b RiskGuard(1차): 평균단가 기준 위험선(-3%) 오버레이
            try {
                if (chart && chart.yAxis && chart.yAxis[0]) {
                    chart.yAxis[0].removePlotLine("riskGuardLine");
                    if (!isNaN(avgNum) && avgNum > 0) {
                        var riskPrice = avgNum * 0.97;
                        chart.yAxis[0].addPlotLine({
                            id: "riskGuardLine",
                            value: riskPrice,
                            color: "#dc2626",
                            width: 1,
                            dashStyle: "Dash",
                            zIndex: 4,
                            label: {
                                text: "RiskGuard -3% (" + formatNumber(riskPrice) + ")",
                                align: "right",
                                x: -6,
                                style: { color: "#b91c1c", fontSize: "9px", fontWeight: "700" }
                            }
                        });
                    }
                }
            } catch (e) {
                // ignore
            }
        });
    }

    function toPlainYmd(d) {
        var y = d.getFullYear();
        var m = ("0" + (d.getMonth() + 1)).slice(-2);
        var dd = ("0" + d.getDate()).slice(-2);
        return "" + y + m + dd;
    }

    function makeRange18m() {
        var to = new Date();
        var from = new Date(to.getTime());
        from.setMonth(from.getMonth() - 18);
        return { from: toPlainYmd(from), to: toPlainYmd(to) };
    }

    function toChartArray(rows) {
        var out = [];
        if (!rows || !rows.length) return out;
        for (var i = 0; i < rows.length; i++) {
            var r = rows[i] || {};
            var ts = Number(r.stck_bsop_date || r.x || 0);
            var o = Number(r.stck_oprc || r.open || 0);
            var h = Number(r.stck_hgpr || r.high || 0);
            var l = Number(r.stck_lwpr || r.low || 0);
            var c = Number(r.stck_clpr || r.close || 0);
            var v = Number(r.acml_vol || r.volume || 0);
            out.push([ts, o, h, l, c, v]);
        }
        return out;
    }

    function pickGuideTerm(evalRes) {
        if (!evalRes || !evalRes.signals || !evalRes.signals.length) return { code: "-", label: "-" };
        var priority = ["IDX_NBZ_240", "IDX_DC_60_240", "IDX_M5260_DW", "IDX_M2060_WARN", "IDX_M20_RN_WARN", "IDX_M2060_GW", "IDX_M520_DW_W", "IDX_M5_RN_WARN", "IDX_M520_GW", "IDX_M520_GU", "IDX_UPTREND_BASE"];
        var labelMap = {
            "NBZ_240": "매수 금지",
            "DC_60_240": "데드크로스",
            "M5260_DW": "5->20->60 하락",
            "M2060_WARN": "20->60 경고",
            "M20_RN_WARN": "경고 20일선 역N자",
            "M2060_GW": "20-60 완만 관망",
            "M520_DW_W": "5->20 하락 관망",
            "M5_RN_WARN": "경고 5일선 역N자",
            "M520_GW": "5-20 완만 관망",
            "M520_GU": "5-20 완만 상승",
            "UPTREND_BASE": "우상향 기준 충족"
        };

        var best = null;
        var bestIdx = 999;
        for (var i = 0; i < evalRes.signals.length; i++) {
            var s = evalRes.signals[i] || {};
            var t = String(s.type || "").toUpperCase();
            var msg = String(s.message || "");
            var idx = priority.indexOf(t);
            if (idx >= 0 && idx < bestIdx) {
                bestIdx = idx;
                best = { type: t, message: msg };
            }
        }
        if (!best) return { code: "-", label: "-" };

        var seg = best.message.split("|");
        var rawCode = (seg[0] ? String(seg[0]).trim() : best.type.replace(/^IDX_/, ""));
        var code = rawCode.replace(/^IDX_/, "");
        var label = "";

        if (seg.length >= 2) label = String(seg[1]).trim();
        if (!label) label = labelMap[code] || labelMap[rawCode] || code;

        return { code: code, label: label };
    }

    function evalIndexTrend(market, code, country, marketCd) {
        var rg = makeRange18m();
        return new Promise(function (resolve) {
            $.ajax({
                url: apiUrl,
                type: "GET",
                dataType: "json",
                data: {
                    in_stockCode: code,
                    in_fromDate: rg.from,
                    in_toDate: rg.to,
                    in_periodDivCode: "D",
                    in_orgAdjPrc: "1",
                    in_stockMarket: marketCd,
                    in_stockCountryCode: country
                },
                timeout: 12000
            }).done(function (res) {
                var list = (res && $.isArray(res.data)) ? res.data : [];
                var chartData = JSON.stringify({ data: toChartArray(list) });
                $.ajax({
                    url: "/scheduler/finance/evaluateIndexTrend.do",
                    type: "POST",
                    dataType: "json",
                    data: { market: market, chartData: chartData },
                    timeout: 12000
                }).done(function (r2) {
                    resolve(r2 && r2.success ? r2 : null);
                }).fail(function () {
                    resolve(null);
                });
            }).fail(function () {
                resolve(null);
            });
        });
    }

    // 월봉 기준 색상 판정용 (양봉=true)
    function evalMonthlyBull(code, country, marketCd) {
        var rg = makeRange18m();
        return new Promise(function (resolve) {
            $.ajax({
                url: apiUrl,
                type: "GET",
                dataType: "json",
                data: {
                    in_stockCode: code,
                    in_fromDate: rg.from,
                    in_toDate: rg.to,
                    in_periodDivCode: "M",
                    in_orgAdjPrc: "1",
                    in_stockMarket: marketCd,
                    in_stockCountryCode: country
                },
                timeout: 12000
            }).done(function (res) {
                var list = (res && $.isArray(res.data)) ? res.data : [];
                if (!list.length) return resolve(null);
                var last = list[list.length - 1] || {};
                var o = Number(last.stck_oprc || last.open || 0);
                var c = Number(last.stck_clpr || last.close || 0);
                if (isNaN(o) || isNaN(c) || o === 0) return resolve(null);
                resolve({ monthlyBull: c >= o, open: o, close: c });
            }).fail(function () {
                resolve(null);
            });
        });
    }

    function loadRegimeOverlay() {
        if (!chart || !window.jQuery) return;

        var m = "N";
        var cc = String((lastQuery && lastQuery.stockCountryCode) ? lastQuery.stockCountryCode : "").toUpperCase();
        if (cc === "US") m = "A";
        else if (cc === "KR") m = "N";
        else if (lastQuery && lastQuery.stockCode && String(lastQuery.stockCode).charAt(0) === ".") m = "A";

        var url = (window.__URLS && window.__URLS.selectRecommendStocks)
            ? window.__URLS.selectRecommendStocks
            : "/finance/selectRecommendStocks.do";

        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            data: {
                market: m,
                minGrade: "HOLD",
                limit: 1,
                onePick: "Y",
                includeNow: "N"
            },
            timeout: 12000
        }).done(function (res) {
            var list = [];
            if (res && res.data && $.isArray(res.data)) list = res.data;
            else if (res && res.data && res.data.data && $.isArray(res.data.data)) list = res.data.data;

            var row = (list && list.length) ? list[0] : null;
            var kr = row ? (row.regime_kr_score || row.REGIME_KR_SCORE || "-") : "-";
            var us = row ? (row.regime_us_score || row.REGIME_US_SCORE || "-") : "-";
            explainCache.regime = { kr: kr, us: us };

            Promise.all([
                evalIndexTrend("KR", "0001", "KR", "KRX"),
                evalIndexTrend("US", ".IXIC", "US", "NAS"),
                evalMonthlyBull("0001", "KR", "KRX"),
                evalMonthlyBull(".IXIC", "US", "NAS")
            ]).then(function (arr) {
                var krEval = arr[0];
                var usEval = arr[1];
                var krMon = arr[2];
                var usMon = arr[3];

                var krKo = krEval && krEval.stateLabelKo ? krEval.stateLabelKo : regimeTrendText(kr);
                var usKo = usEval && usEval.stateLabelKo ? usEval.stateLabelKo : regimeTrendText(us);
                var krGuideObj = pickGuideTerm(krEval);
                var usGuideObj = pickGuideTerm(usEval);

                var focusMarket = (m === "A") ? "US" : "KR";
                var focusKo = (focusMarket === "US") ? usKo : krKo;
                var focusScore = (focusMarket === "US") ? us : kr;
                var focusGuide = (focusMarket === "US") ? usGuideObj.label : krGuideObj.label;

                explainCache.indexTrend = {
                    focusMarket: focusMarket,
                    krKo: krKo,
                    usKo: usKo,
                    krScore: kr,
                    usScore: us,
                    krCode: krEval && krEval.stateCode ? krEval.stateCode : "",
                    usCode: usEval && usEval.stateCode ? usEval.stateCode : "",
                    krGuide: krGuideObj.code,
                    usGuide: usGuideObj.code,
                    krGuideLabel: krGuideObj.label,
                    usGuideLabel: usGuideObj.label,
                    krMonthlyBull: (krMon && krMon.monthlyBull === true),
                    usMonthlyBull: (usMon && usMon.monthlyBull === true)
                };
                renderExplainPanel();

                var text = isMobileDevice()
                    ? ("시장바람 " + focusMarket + " " + focusKo + "(" + focusGuide + ")")
                    : ("시장바람 " + focusMarket + " " + focusKo + "(" + focusScore + ", " + focusGuide + ")");

                // 사용자 요청: 색기준은 월봉 기준
                // 월봉 양봉: 분홍색, 월봉 음봉: 하늘색
                var focusMonthlyBull = (focusMarket === "US")
                    ? (explainCache.indexTrend.usMonthlyBull === true)
                    : (explainCache.indexTrend.krMonthlyBull === true);
                var bg = focusMonthlyBull ? "#ec4899" : "#38bdf8";

                if (chart.kisRegimeLabel) {
                    chart.kisRegimeLabel.destroy();
                    chart.kisRegimeLabel = null;
                }

                chart.kisRegimeLabel = chart.renderer
                    .label(text, 10, 28, "rect", null, null, true)
                    .attr({ zIndex: 9, r: 4, fill: bg, padding: 4 })
                    .css({ color: "#fff", fontSize: "10px", fontWeight: "700" })
                    .add();
            });
        });
    }

    function loadKisItemchartprice(params) {
      lastQuery = {
          stockCode: $.trim(params.stockCode || "005930"),
          fromDate: $.trim(params.fromDate || ""),
          toDate: $.trim(params.toDate || ""),
          periodDivCode: (params.periodDivCode || "D"),
          orgAdjPrc: (params.orgAdjPrc || "1"),
          stockMarket: (params.stockMarket || ""),
          stockCountryCode: (params.stockCountryCode || "")
      };

      // 코드 기준 시장/국가 보정 (US 종목이 KR로, KR 종목이 US로 호출되는 문제 방지)
      var sc = String(lastQuery.stockCode || "").toUpperCase();
      var cc = String(lastQuery.stockCountryCode || "").toUpperCase();
      var mk = String(lastQuery.stockMarket || "").toUpperCase();
      var isKrCode = /^\d{5,6}$/.test(sc);
      var isUsCode = (sc.charAt(0) === ".") || /^[A-Z]{1,6}$/.test(sc);
      if (isKrCode) {
          cc = "KR";
          if (!mk || mk === "NAS" || mk === "NYS" || mk === "AMS") mk = "KRX";
      } else if (isUsCode) {
          if (!cc || cc === "KR") cc = "US";
          if (!mk || mk === "KRX") mk = "NAS";
      }
      lastQuery.stockCountryCode = cc;
      lastQuery.stockMarket = mk;

        try {
            if (isIndexCode(lastQuery.stockCode)) {
                applyIndexHeaderStatic(lastQuery.stockCode);
            } else {
                fetchStockMeta(lastQuery.stockCode);
                var cc = String(lastQuery.stockCountryCode || "").toUpperCase();
                // US는 현재가 API가 국내 포맷 0값을 반환할 수 있어 차트 현재가 라인 덮어쓰기를 방지
                if (cc !== "US") {
                    fetchCurrentPrice(lastQuery.stockCode);
                }
            }
        } catch (e) {
            // ignore
        }

        $.ajax({
            url: apiUrl,
            type: "GET",
            dataType: "json",
          data: {
              in_stockCode: lastQuery.stockCode,
              in_fromDate: lastQuery.fromDate,
              in_toDate: lastQuery.toDate,
              in_periodDivCode: lastQuery.periodDivCode,
              in_orgAdjPrc: lastQuery.orgAdjPrc,
              in_stockMarket: lastQuery.stockMarket,
              in_stockCountryCode: lastQuery.stockCountryCode
          },
            success: function (res) {
                if (!res) {
                    minuteRangeExtendInFlight = false;
                    alert("응답이 없습니다.");
                    return;
                }
                if (res.system_code && res.system_code !== "0000") {
                    minuteRangeExtendInFlight = false;
                    alert("KIS API 오류: " + (res.system_msg || ""));
                    return;
                }
                var list = $.isArray(res.data) ? res.data : [];
                if (!list.length) {
                    minuteRangeExtendInFlight = false;
                    alert("조회된 Chart 데이터가 없습니다.");
                    return;
                }
                lastData = list;
                convertHeader(list);
                renderKisChart(list, lastQuery.periodDivCode);
                minuteRangeExtendInFlight = false;
            },
            error: function (xhr, status, err) {
                minuteRangeExtendInFlight = false;
                console.error("kisItemchartpriceData Ajax error:", status, err);
                alert("KIS 기간별 시세 조회 중 오류가 발생했습니다.");
            }
        });
    }

    function setOptions(next) {
        if (!next || typeof next !== "object") return;

        if (typeof next.volumeEnabled === "boolean") options.volumeEnabled = next.volumeEnabled;
        if (typeof next.doubleChartEnabled === "boolean") options.doubleChartEnabled = next.doubleChartEnabled;
        if (typeof next.monthLinesEnabled === "boolean") options.monthLinesEnabled = next.monthLinesEnabled;
        if (typeof next.highLowEnabled === "boolean") options.highLowEnabled = next.highLowEnabled;
        if (typeof next.maSrEnabled === "boolean") {
            options.maSrEnabled = next.maSrEnabled;
            if (!options.maSrEnabled && chart) {
                chart.kisMaSrLevels = [];
                chart.kisMaSrCurrentPrice = null;
                removeMaSupportResistanceLabels(chart);
            }
        }
        if (typeof next.doublePointWidth === "number" && next.doublePointWidth >= 2) options.doublePointWidth = next.doublePointWidth;
        if (typeof next.doubleUpColor === "string" && next.doubleUpColor.length) options.doubleUpColor = next.doubleUpColor;
        if (typeof next.doubleDownColor === "string" && next.doubleDownColor.length) options.doubleDownColor = next.doubleDownColor;
        if (Array.isArray(next.maPeriods)) options.maPeriods = next.maPeriods;

        if (Array.isArray(next.maConfigs)) {
            options.maConfigs = next.maConfigs.slice();

            var pArr = [];
            for (var i = 0; i < options.maConfigs.length; i++) {
                var c = options.maConfigs[i];
                if (!c) continue;
                if (c.enabledYn && c.enabledYn !== "Y") continue;
                if (typeof c.seriesPeriod === "number" && c.seriesPeriod > 0) {
                    pArr.push(c.seriesPeriod);
                }
            }
            if (pArr.length) {
                pArr.sort(function (a, b) {
                    return a - b;
                });
                options.maPeriods = pArr;
            }
        }

        if (typeof next.crossEnabled === "boolean") options.crossEnabled = next.crossEnabled;
        if (typeof next.crossShortPeriod === "number" && next.crossShortPeriod > 0) options.crossShortPeriod = next.crossShortPeriod;
        if (typeof next.crossLongPeriod === "number" && next.crossLongPeriod > 0) options.crossLongPeriod = next.crossLongPeriod;

    }

    return {
        options: options,
        loadKisItemchartprice: loadKisItemchartprice,
        renderKisChart: renderKisChart,
        setOptions: setOptions,
        applyRealtimeQuote: applyRealtimeQuote,
        get chart() { return chart; },
        get lastData() { return lastData; },
        get lastQuery() { return lastQuery; }
    };
})();
