/**
 * watchlistQuoteSync.js
 * 관심종목 행에 현재가 API 동기화 (MutationObserver 기반)
 * (kisFinance.jsp 인라인 <script> 분리)
 */
(function () {
	var watchPriceCache = {};
	var watchPriceInflight = {};
	var watchPriceTimers = {};
	var WATCH_PRICE_CACHE_MS = 2500;

	function toNumber(value) {
		var text;

		if (value === null || value === undefined) {
			return NaN;
		}

		text = String(value).replace(/,/g, "").trim();
		if (!text) {
			return NaN;
		}

		if (text.charAt(0) === "+") {
			text = text.substring(1);
		}

		return parseFloat(text);
	}

	function decimalsHint(value, fallback) {
		var text;
		var dot;
		var decimals;

		if (value === null || value === undefined) {
			return fallback;
		}

		text = String(value);
		dot = text.indexOf(".");
		if (dot < 0) {
			return fallback;
		}

		decimals = text.length - dot - 1;
		if (decimals < 0) {
			return fallback;
		}

		return decimals > 4 ? 4 : decimals;
	}

	function formatNumber(value, decimals) {
		if (!Number.isFinite(value)) {
			return "-";
		}

		return value.toLocaleString("en-US", {
			useGrouping: true,
			minimumFractionDigits: decimals,
			maximumFractionDigits: decimals
		});
	}

	function formatSignedNumber(value, decimals) {
		if (!Number.isFinite(value)) {
			return "-";
		}

		if (value > 0) {
			return "+" + formatNumber(Math.abs(value), decimals);
		}
		if (value < 0) {
			return "-" + formatNumber(Math.abs(value), decimals);
		}
		return "0";
	}

	function formatSignedPct(value) {
		if (!Number.isFinite(value)) {
			return "-";
		}

		if (value > 0) {
			return "+" + Math.abs(value).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + "%";
		}
		if (value < 0) {
			return "-" + Math.abs(value).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + "%";
		}
		return "0.00%";
	}

	function unwrapSingle(res) {
		if (!res) {
			return null;
		}

		if (res.data) {
			if (res.data.singleData) {
				return res.data.singleData;
			}
			if (res.data.data && res.data.data.singleData) {
				return res.data.data.singleData;
			}
		}

		if (res.singleData) {
			return res.singleData;
		}

		return null;
	}

	function extractPriceOutput(single) {
		if (!single) {
			return null;
		}
		if (single.output) {
			return single.output;
		}
		if (single.out) {
			return single.out;
		}
		if (single.data && single.data.output) {
			return single.data.output;
		}
		return null;
	}

	function pickNumber(source, keys) {
		var i;
		var value;
		var numeric;

		if (!source || !keys || !keys.length) {
			return NaN;
		}

		for (i = 0; i < keys.length; i += 1) {
			value = source[keys[i]];
			numeric = toNumber(value);
			if (Number.isFinite(numeric)) {
				return numeric;
			}
		}

		return NaN;
	}

	function buildWatchPriceState(single) {
		var out = extractPriceOutput(single);
		var current;
		var diff;
		var prevClose;
		var pct;
		var diffDecimals;
		var priceDecimals;
		var dir;

		if (!out) {
			return null;
		}

		current = pickNumber(out, ["stckPrpr", "stck_prpr", "price", "last"]);
		diff = pickNumber(out, ["prdyVrss", "prdy_vrss", "diff", "change"]);
		prevClose = pickNumber(out, ["stckSdpr", "stck_sdpr", "prevClose", "base"]);

		if (!Number.isFinite(diff) && Number.isFinite(current) && Number.isFinite(prevClose)) {
			diff = current - prevClose;
		}
		if (!Number.isFinite(prevClose) && Number.isFinite(current) && Number.isFinite(diff)) {
			prevClose = current - diff;
		}
		if (!Number.isFinite(current)) {
			return null;
		}

		pct = (Number.isFinite(prevClose) && prevClose !== 0 && Number.isFinite(diff))
			? (diff / prevClose * 100)
			: NaN;

		priceDecimals = decimalsHint(out.stckPrpr || out.stck_prpr || out.price || out.last, 0);
		diffDecimals = decimalsHint(out.prdyVrss || out.prdy_vrss || out.diff || out.change, 0);
		dir = Number.isFinite(diff) ? (diff > 0 ? "up" : (diff < 0 ? "down" : "flat")) : "flat";

		return {
			priceText: formatNumber(current, priceDecimals),
			diffText: formatSignedNumber(diff, diffDecimals),
			rateText: formatSignedPct(pct),
			dir: dir
		};
	}

	function applyWatchPriceStateToRow(row, state) {
		var priceEl;
		var diffEl;
		var rateEl;
		var eventEl;

		if (!row || !state) {
			return;
		}

		priceEl = row.querySelector(".wl-price");
		diffEl = row.querySelector(".wl-diff");
		rateEl = row.querySelector(".wl-rate");
		eventEl = row.querySelector(".wl-event");

		if (priceEl) {
			priceEl.textContent = state.priceText;
			priceEl.title = state.diffText + " / " + state.rateText;
			priceEl.classList.toggle("up", state.dir === "up");
			priceEl.classList.toggle("down", state.dir === "down");
			priceEl.classList.toggle("flat", state.dir === "flat");
		}

		if (diffEl) {
			diffEl.textContent = state.diffText;
			diffEl.classList.toggle("up", state.dir === "up");
			diffEl.classList.toggle("down", state.dir === "down");
			diffEl.classList.toggle("flat", state.dir === "flat");
		}

		if (rateEl) {
			rateEl.textContent = state.rateText;
			rateEl.classList.toggle("up", state.dir === "up");
			rateEl.classList.toggle("down", state.dir === "down");
			rateEl.classList.toggle("flat", state.dir === "flat");
		}

		if (eventEl) {
			eventEl.classList.toggle("up", state.dir === "up");
			eventEl.classList.toggle("down", state.dir === "down");
			eventEl.classList.toggle("flat", state.dir === "flat");
		}
	}

	function applyWatchPriceStateByCode(code, state) {
		var rows = document.querySelectorAll("#watchlist .wl-item");
		var i;
		var row;

		if (!code || !state) {
			return;
		}

		for (i = 0; i < rows.length; i += 1) {
			row = rows[i];
			if (String(row.getAttribute("data-code") || "").trim() === code) {
				applyWatchPriceStateToRow(row, state);
			}
		}
	}

	function isDomesticWatchRow(row) {
		var country = String(row && row.getAttribute("data-country") || "").trim().toUpperCase();
		return !country || country === "KR";
	}

	function getWatchCurrentPriceUrl() {
		return (window.__CTX_PATH || "") + "/finance/getCurrentPriceByInquirePrice.do";
	}

	function syncWatchRowFromApi(row, force) {
		var code;
		var cached;
		var nowTs;

		if (!row || !window.jQuery || !isDomesticWatchRow(row)) {
			return;
		}

		code = String(row.getAttribute("data-code") || "").trim();
		if (!code) {
			return;
		}

		nowTs = Date.now();
		cached = watchPriceCache[code];
		if (!force && cached && (nowTs - cached.ts) < WATCH_PRICE_CACHE_MS) {
			applyWatchPriceStateByCode(code, cached.state);
			return;
		}

		if (watchPriceInflight[code]) {
			return;
		}

		watchPriceInflight[code] = true;
		$.ajax({
			url: getWatchCurrentPriceUrl(),
			type: "GET",
			dataType: "json",
			data: { in_stockCode: code },
			success: function (res) {
				var single = unwrapSingle(res);
				var state = buildWatchPriceState(single);

				if (!state) {
					return;
				}

				watchPriceCache[code] = {
					ts: Date.now(),
					state: state
				};
				applyWatchPriceStateByCode(code, state);
			},
			complete: function () {
				delete watchPriceInflight[code];
			}
		});
	}

	function scheduleWatchRowSync(row, force) {
		var code;

		if (!row || !isDomesticWatchRow(row)) {
			return;
		}

		code = String(row.getAttribute("data-code") || "").trim();
		if (!code) {
			return;
		}

		if (watchPriceTimers[code]) {
			clearTimeout(watchPriceTimers[code]);
		}

		watchPriceTimers[code] = window.setTimeout(function () {
			delete watchPriceTimers[code];
			syncWatchRowFromApi(row, force === true);
		}, force === true ? 0 : 120);
	}

	function collectWatchRows(node, rows) {
		var row;
		var found;
		var i;

		if (!node) {
			return;
		}

		if (node.nodeType === 3) {
			row = node.parentElement ? node.parentElement.closest(".wl-item") : null;
			if (row) {
				rows.add(row);
			}
			return;
		}

		if (node.nodeType !== 1) {
			return;
		}

		row = node.closest(".wl-item");
		if (row) {
			rows.add(row);
		}

		if (node.matches(".wl-item")) {
			rows.add(node);
		}

		if (typeof node.querySelectorAll !== "function") {
			return;
		}

		found = node.querySelectorAll(".wl-item");
		for (i = 0; i < found.length; i += 1) {
			rows.add(found[i]);
		}
	}

	function bindWatchlistQuoteSync() {
		var wrap = document.getElementById("watchlist");
		var observer;

		if (!wrap || wrap.__watchlistQuoteSyncBound) {
			return;
		}

		wrap.__watchlistQuoteSyncBound = true;

		Array.prototype.forEach.call(wrap.querySelectorAll(".wl-item"), function (row) {
			scheduleWatchRowSync(row, true);
		});

		observer = new MutationObserver(function (mutations) {
			var rows = new Set();

			mutations.forEach(function (mutation) {
				var i;

				collectWatchRows(mutation.target, rows);

				if (mutation.addedNodes && mutation.addedNodes.length) {
					for (i = 0; i < mutation.addedNodes.length; i += 1) {
						collectWatchRows(mutation.addedNodes[i], rows);
					}
				}
			});

			rows.forEach(function (row) {
				scheduleWatchRowSync(row, false);
			});
		});

		// characterData 감지 제외: WebSocket 가격 업데이트 → Observer 발동 → API 재호출 루프 방지
		observer.observe(wrap, {
			childList: true,
			subtree: true
		});
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", bindWatchlistQuoteSync);
	} else {
		bindWatchlistQuoteSync();
	}
})();
