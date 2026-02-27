/**
 * 이동평균선 정배열 + 장대양봉/연속양봉 패턴 감지 클라이언트 라이브러리
 *
 * 사용 예:
 * const detector = new BullishMomentumDetector({
 *     consecutiveBullDays: 3,
 *     longCandleRatio: 2.0
 * });
 *
 * detector.detectFromChart(chartData)
 *     .then(signals => {
 *         console.log('감지된 신호:', signals);
 *         detector.displaySignalsOnChart(chart, signals);
 *     });
 */
class BullishMomentumDetector {
    constructor(options) {
        this.options = {
            consecutiveBullDays: options?.consecutiveBullDays || 3,
            longCandleRatio: options?.longCandleRatio || 2.0,
            apiEndpoint: options?.apiEndpoint || '/scheduler/finance/detectBullishMomentum.do'
        };
    }

    /**
     * API를 통해 신호 감지 (서버 처리)
     */
    detectFromChart(chartData, stockCode, stockName) {
        const params = new URLSearchParams();
        params.append('stockCode', stockCode || '');
        params.append('stockName', stockName || '');
        params.append('chartData', JSON.stringify({ data: chartData }));
        params.append('consecutiveBullDays', this.options.consecutiveBullDays);
        params.append('longCandleRatio', this.options.longCandleRatio);

        return fetch(this.options.apiEndpoint, {
            method: 'POST',
            body: params,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                return result.signals || [];
            } else {
                console.error('신호 감지 실패:', result.message);
                return [];
            }
        })
        .catch(error => {
            console.error('API 호출 오류:', error);
            return [];
        });
    }

    /**
     * 클라이언트 처리: StockDataVo 배열에서 신호 감지
     */
    detectLocalFromStockData(candles, stockCode, stockName) {
        if (!candles || candles.length < 240) {
            console.warn(`충분한 캔들 데이터 필요: ${candles?.length || 0} / 240`);
            return [];
        }

        const ma5 = this._calculateSMA(candles, 5);
        const ma20 = this._calculateSMA(candles, 20);
        const ma60 = this._calculateSMA(candles, 60);
        const ma120 = this._calculateSMA(candles, 120);
        const ma240 = this._calculateSMA(candles, 240);

        const signals = [];
        const startIdx = 240;

        for (let i = startIdx; i < candles.length; i++) {
            const current = candles[i];
            const close = current.close;

            // 완전 정배열 확인
            if (!this._isCompleteAlignment(close, ma5[i], ma20[i], ma60[i], ma120[i], ma240[i])) {
                continue;
            }

            // 장대양봉 확인
            const avgBody = this._calculateAverageBody(candles, i);
            const currentBody = Math.abs(current.close - current.open);
            const bodyRatio = avgBody > 0 ? currentBody / avgBody : 0;

            if (bodyRatio >= this.options.longCandleRatio && current.close > current.open) {
                signals.push({
                    signalTime: current.date,
                    stockCode: stockCode,
                    stockName: stockName,
                    closePrice: current.close,
                    signalType: 'LONG_BULL_BODY',
                    bodyRatio: bodyRatio,
                    consecutiveDays: 1,
                    ma5: ma5[i],
                    ma20: ma20[i],
                    ma60: ma60[i],
                    ma120: ma120[i],
                    ma240: ma240[i],
                    open: current.open,
                    high: current.high,
                    low: current.low,
                    volume: current.volume
                });
            }

            // 연속양봉 확인
            const consecutiveDays = this._countConsecutiveBullish(candles, i);
            if (consecutiveDays >= this.options.consecutiveBullDays) {
                signals.push({
                    signalTime: current.date,
                    stockCode: stockCode,
                    stockName: stockName,
                    closePrice: current.close,
                    signalType: 'CONSECUTIVE_BULL',
                    bodyRatio: 0,
                    consecutiveDays: consecutiveDays,
                    ma5: ma5[i],
                    ma20: ma20[i],
                    ma60: ma60[i],
                    ma120: ma120[i],
                    ma240: ma240[i],
                    open: current.open,
                    high: current.high,
                    low: current.low,
                    volume: current.volume
                });
            }
        }

        return signals;
    }

    /**
     * Highcharts 차트에 신호 마킹
     */
    displaySignalsOnChart(chart, signals) {
        if (!chart || !signals || signals.length === 0) {
            return;
        }

        signals.forEach(signal => {
            const color = signal.signalType === 'LONG_BULL_BODY' ? '#FF6B6B' : '#4ECDC4';
            const text = signal.signalType === 'LONG_BULL_BODY'
                ? `장대양봉 ${signal.bodyRatio.toFixed(1)}배`
                : `연속양봉 ${signal.consecutiveDays}일`;

            // Highcharts에 label 추가
            chart.renderer.label(text, 0, 0)
                .attr({
                    fill: color,
                    padding: 5,
                    r: 3,
                    zIndex: 6
                })
                .css({
                    color: 'white',
                    fontSize: '11px',
                    fontWeight: 'bold'
                })
                .add();
        });

        console.log(`차트에 ${signals.length}개 신호 마킹`);
    }

    /**
     * 신호를 HTML 테이블로 렌더링
     */
    renderSignalsTable(signals, containerId) {
        const container = document.getElementById(containerId);
        if (!container) {
            console.error(`Container not found: ${containerId}`);
            return;
        }

        if (!signals || signals.length === 0) {
            container.innerHTML = '<p>감지된 신호가 없습니다.</p>';
            return;
        }

        let html = `
            <table class="table table-sm table-striped">
                <thead class="table-dark">
                    <tr>
                        <th>시간</th>
                        <th>종목</th>
                        <th>종가</th>
                        <th>신호 유형</th>
                        <th>상세</th>
                        <th>MA 값</th>
                    </tr>
                </thead>
                <tbody>
        `;

        signals.forEach(signal => {
            const date = new Date(signal.signalTime).toLocaleDateString('ko-KR');
            const typeLabel = signal.signalType === 'LONG_BULL_BODY'
                ? `<span class="badge bg-danger">장대양봉</span>`
                : `<span class="badge bg-info">연속양봉</span>`;

            const details = signal.signalType === 'LONG_BULL_BODY'
                ? `${signal.bodyRatio.toFixed(2)}배`
                : `${signal.consecutiveDays}일`;

            const maInfo = `
                5: ${signal.ma5?.toFixed(0)}<br/>
                20: ${signal.ma20?.toFixed(0)}<br/>
                60: ${signal.ma60?.toFixed(0)}<br/>
                120: ${signal.ma120?.toFixed(0)}<br/>
                240: ${signal.ma240?.toFixed(0)}
            `;

            html += `
                <tr>
                    <td>${date}</td>
                    <td>${signal.stockCode} ${signal.stockName || ''}</td>
                    <td>${signal.closePrice?.toFixed(0)}</td>
                    <td>${typeLabel}</td>
                    <td>${details}</td>
                    <td style="font-size: 0.85em;">${maInfo}</td>
                </tr>
            `;
        });

        html += `
                </tbody>
            </table>
        `;

        container.innerHTML = html;
    }

    // ======= 비공개 헬퍼 메서드 =======

    _calculateSMA(candles, period) {
        const sma = new Array(candles.length).fill(NaN);
        if (period <= 0 || candles.length < period) {
            return sma;
        }

        let sum = 0;
        for (let i = 0; i < candles.length; i++) {
            sum += candles[i].close;
            if (i >= period) {
                sum -= candles[i - period].close;
            }
            if (i >= period - 1) {
                sma[i] = sum / period;
            }
        }
        return sma;
    }

    _isCompleteAlignment(close, ma5, ma20, ma60, ma120, ma240) {
        return !isNaN(ma5) && !isNaN(ma20) && !isNaN(ma60) && !isNaN(ma120) && !isNaN(ma240)
            && close > ma5 && ma5 > ma20 && ma20 > ma60 && ma60 > ma120 && ma120 > ma240;
    }

    _calculateAverageBody(candles, endIdx) {
        const lookback = Math.min(20, endIdx + 1);
        if (lookback <= 0) return 0;

        let sum = 0;
        for (let i = endIdx - lookback + 1; i <= endIdx; i++) {
            if (i >= 0 && i < candles.length) {
                sum += Math.abs(candles[i].close - candles[i].open);
            }
        }
        return sum / lookback;
    }

    _countConsecutiveBullish(candles, endIdx) {
        let count = 0;
        for (let i = endIdx; i >= 0; i--) {
            if (candles[i].close > candles[i].open) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}

// 글로벌 인스턴스 내보내기
if (typeof window !== 'undefined') {
    window.BullishMomentumDetector = BullishMomentumDetector;
}
