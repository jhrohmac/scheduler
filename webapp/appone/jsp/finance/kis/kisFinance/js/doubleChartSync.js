/**
 * doubleChartSync.js
 * 더블차트 버튼 ↔ ChartScript / ChartFeatureToggle / Ajax 동기화
 * (kisFinance.jsp 인라인 <script> 분리)
 */
(function () {
	var doubleChartStateResolved = false;

	function normalizeDoubleChartMode(mode) {
		mode = String(mode || "").toLowerCase();
		if (mode !== "recent" && mode !== "all" && mode !== "off") {
			return "";
		}
		return mode;
	}

	function resolveModeFromState(state) {
		var mode;

		if (!state) {
			return "";
		}

		mode = normalizeDoubleChartMode(state.doubleChartMode || "");
		if (mode) {
			return mode;
		}

		if (typeof state.doubleChartEnabled === "boolean") {
			return state.doubleChartEnabled ? "all" : "off";
		}

		return "";
	}

	function extractDoubleChartModeFromOptionList(list) {
		var mode = "";
		var i;
		var item;
		var key;
		var enabled;
		var period;

		if (!Array.isArray(list)) {
			return "";
		}

		for (i = 0; i < list.length; i += 1) {
			item = list[i] || {};
			key = String(item.seriesKey || "");

			if (key === "doubleChartEnabled") {
				enabled = String(item.enabledYn || "").toUpperCase() === "Y";
				mode = enabled ? "all" : "off";
			}

			if (key === "doubleChartMode") {
				period = parseInt(item.seriesPeriod || 0, 10);
				mode = (period === 1 ? "recent" : (period === 2 ? "all" : "off"));
			}
		}

		return normalizeDoubleChartMode(mode);
	}

	function nextDoubleChartMode(mode) {
		mode = normalizeDoubleChartMode(mode) || "off";
		if (mode === "recent") return "all";
		if (mode === "all") return "off";
		return "recent";
	}

	function doubleChartModeLabel(mode) {
		if (mode === "recent") return "RE";
		if (mode === "all") return "ALL";
		return "OFF";
	}

	function readChartScriptMode() {
		var options = window.ChartScript && window.ChartScript.options;
		var mode;

		if (!options) {
			return "";
		}

		mode = normalizeDoubleChartMode(options.doubleChartMode || "");
		if (mode) {
			return mode;
		}

		if (typeof options.doubleChartEnabled === "boolean") {
			return options.doubleChartEnabled ? "all" : "off";
		}

		return "";
	}

	function readHiddenDoubleChartMode() {
		var checked = document.querySelector("input[name='optDoubleChartMode']:checked");
		if (checked && checked.value) {
			return normalizeDoubleChartMode(checked.value) || "";
		}

		if (document.getElementById("optDoubleChart")) {
			return document.getElementById("optDoubleChart").checked ? "all" : "off";
		}

		return "";
	}

	function writeHiddenDoubleChartMode(mode) {
		var checkbox = document.getElementById("optDoubleChart");
		var radio;

		mode = normalizeDoubleChartMode(mode) || "off";
		radio = document.querySelector("input[name='optDoubleChartMode'][value='" + mode + "']");

		if (!checkbox || !radio) {
			return;
		}

		checkbox.checked = mode !== "off";
		radio.checked = true;
	}

	function markDoubleChartModeResolved(mode) {
		mode = normalizeDoubleChartMode(mode);
		if (!mode) {
			return "";
		}

		doubleChartStateResolved = true;
		writeHiddenDoubleChartMode(mode);
		return mode;
	}

	function resolveDoubleChartMode() {
		var chartMode = readChartScriptMode();
		var hiddenMode;

		if (doubleChartStateResolved && chartMode) {
			writeHiddenDoubleChartMode(chartMode);
			return chartMode;
		}

		hiddenMode = readHiddenDoubleChartMode();
		if (doubleChartStateResolved && hiddenMode) {
			return hiddenMode;
		}

		return "";
	}

	function applyDoubleChartMode(mode) {
		var checkbox = document.getElementById("optDoubleChart");
		var radio = document.querySelector("input[name='optDoubleChartMode'][value='" + mode + "']");

		if (!checkbox || !radio) {
			return;
		}

		mode = markDoubleChartModeResolved(mode) || "off";
		writeHiddenDoubleChartMode(mode);
		radio.dispatchEvent(new Event("change", { bubbles: true }));
	}

	function syncDoubleChartButton(mode) {
		var button = document.getElementById("kisDoubleChartBtn");
		var modeEl;
		var dots;

		if (!button) {
			return;
		}

		if (mode && typeof mode === "object" && typeof mode.type === "string") {
			mode = readHiddenDoubleChartMode() || readChartScriptMode();
		}

		mode = normalizeDoubleChartMode(mode) || resolveDoubleChartMode();
		modeEl = button.querySelector(".double-chart-btn-mode");
		dots = button.querySelectorAll(".double-chart-dot");

		if (!mode) {
			button.dataset.doubleMode = "";
			button.title = "더블차트 설정 확인중";
			button.classList.remove("is-active");

			if (modeEl) {
				modeEl.textContent = "-";
			}

			if (dots.length === 3) {
				dots[0].classList.remove("is-active");
				dots[1].classList.remove("is-active");
				dots[2].classList.remove("is-active");
			}
			return;
		}

		button.dataset.doubleMode = mode;
		button.title = "더블차트 " + doubleChartModeLabel(mode);
		button.classList.toggle("is-active", mode !== "off");

		if (modeEl) {
			modeEl.textContent = doubleChartModeLabel(mode);
		}

		if (dots.length === 3) {
			dots[0].classList.toggle("is-active", mode === "recent");
			dots[1].classList.toggle("is-active", mode === "all");
			dots[2].classList.toggle("is-active", mode === "off");
		}
	}

	function requestHasValue(requestData, key, expectedValue) {
		var pair;

		if (requestData == null) {
			return false;
		}

		if (typeof requestData === "string") {
			pair = key + "=" + encodeURIComponent(expectedValue);
			if (requestData.indexOf(pair) >= 0) {
				return true;
			}

			pair = key + "=" + expectedValue;
			return requestData.indexOf(pair) >= 0;
		}

		return String(requestData[key] || "") === expectedValue;
	}

	function bindDoubleChartAjaxSync() {
		if (!window.jQuery || window.__kisDoubleChartAjaxBound) {
			return;
		}

		window.__kisDoubleChartAjaxBound = true;
		$(document).off("ajaxSuccess.kisDoubleChartSync").on("ajaxSuccess.kisDoubleChartSync", function (event, xhr, settings, data) {
			var dataUrl = window.__URLS && window.__URLS.kisItemchartpriceOptionData;
			var mode;

			if (!dataUrl || !settings || String(settings.url || "").indexOf(dataUrl) === -1) {
				return;
			}

			if (!requestHasValue(settings.data, "chartId", "KIS_ITEMCHART") || !requestHasValue(settings.data, "seriesType", "FEATURE")) {
				return;
			}

			mode = extractDoubleChartModeFromOptionList(data && data.data);
			if (!mode) {
				return;
			}

			syncDoubleChartButton(markDoubleChartModeResolved(mode));
		});
	}

	function patchChartScriptSync() {
		var chartScript = window.ChartScript;
		var originalSetOptions;

		if (!chartScript || typeof chartScript.setOptions !== "function" || chartScript.__kisDoubleChartPatched) {
			return;
		}

		originalSetOptions = chartScript.setOptions;
		chartScript.setOptions = function () {
			var result = originalSetOptions.apply(this, arguments);
			var next = arguments[0] || {};
			var mode = normalizeDoubleChartMode(next.doubleChartMode || "");

			if (!mode && typeof next.doubleChartEnabled === "boolean") {
				mode = next.doubleChartEnabled ? "all" : "off";
			}

			if (mode) {
				mode = markDoubleChartModeResolved(mode);
			}

			syncDoubleChartButton(mode);
			return result;
		};
		chartScript.__kisDoubleChartPatched = true;
	}

	function patchChartFeatureToggleSync() {
		var toggle = window.ChartFeatureToggle;
		var originalWriteModal;
		var originalApply;

		if (!toggle || toggle.__kisDoubleChartPatched) {
			return;
		}

		if (typeof toggle.writeModal === "function") {
			originalWriteModal = toggle.writeModal;
			toggle.writeModal = function () {
				var result = originalWriteModal.apply(this, arguments);
				var mode = resolveModeFromState(arguments[0] || {});
				if (mode) {
					mode = markDoubleChartModeResolved(mode);
				}
				syncDoubleChartButton(mode);
				return result;
			};
		}

		if (typeof toggle.apply === "function") {
			originalApply = toggle.apply;
			toggle.apply = function () {
				var result = originalApply.apply(this, arguments);
				var mode = resolveModeFromState(arguments[0] || {});
				if (mode) {
					mode = markDoubleChartModeResolved(mode);
				}
				syncDoubleChartButton(mode);
				return result;
			};
		}

		toggle.__kisDoubleChartPatched = true;
	}

	function installDoubleChartSync() {
		bindDoubleChartAjaxSync();
		patchChartScriptSync();
		patchChartFeatureToggleSync();
		syncDoubleChartButton();
	}

	function scheduleInitialDoubleChartSync() {
		[0, 100, 300, 700, 1500].forEach(function (delay) {
			window.setTimeout(function () {
				installDoubleChartSync();
			}, delay);
		});
	}

	function bindDoubleChartButton() {
		var button = document.getElementById("kisDoubleChartBtn");
		var checkbox = document.getElementById("optDoubleChart");
		var radios = document.querySelectorAll("input[name='optDoubleChartMode']");

		if (!button || !checkbox || !radios.length) {
			return;
		}

		if (button.__kisDoubleChartBound) {
			installDoubleChartSync();
			return;
		}

		button.__kisDoubleChartBound = true;
		button.addEventListener("click", function () {
			applyDoubleChartMode(nextDoubleChartMode(resolveDoubleChartMode()));
		});

		checkbox.addEventListener("change", syncDoubleChartButton);
		Array.prototype.forEach.call(radios, function (radio) {
			radio.addEventListener("change", syncDoubleChartButton);
		});

		installDoubleChartSync();
		scheduleInitialDoubleChartSync();
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", bindDoubleChartButton);
	} else {
		bindDoubleChartButton();
	}
})();
