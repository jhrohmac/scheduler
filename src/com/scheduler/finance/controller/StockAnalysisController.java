// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.exp.SchedulerException;
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.util.UserSessionCheck;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.comm.vo.ItemCodeVo;
import com.scheduler.finance.dao.CodeManageDao;
import com.scheduler.finance.dao.StockAnalysisDao;
import com.scheduler.finance.dao.StockManagementDao;
import com.scheduler.finance.vo.CodeVo;
import com.scheduler.finance.vo.StockInfoVo;
import com.scheduler.login.service.UserSession;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

import net.sf.json.JSONObject;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.UUID;
import java.util.logging.Logger;
import com.scheduler.finance.module.BullishMomentumDetector;
import com.scheduler.finance.module.PositionRuleEngine;
import com.scheduler.finance.module.IndexTrendRuleModule;
import com.scheduler.finance.vo.BullishMomentumSignal;
import com.scheduler.finance.vo.PositionPlanVo;
import com.scheduler.finance.vo.PositionSignalVo;
import com.scheduler.finance.vo.StockDataVo;

@Controller
public class StockAnalysisController
{
    public StockAnalysisDao stockAnalysisDao;
    private static final Logger LOG = Logger.getLogger(StockAnalysisController.class.getName());
    private static final long EVAL_PLAN_TIMEOUT_MS = 8000L;

    public StockAnalysisDao getStockAnalysisDao() {
        return this.stockAnalysisDao;
    }
    
    public void setStockAnalysisDao(StockAnalysisDao stockAnalysisDao) {
        this.stockAnalysisDao = stockAnalysisDao;
    }
	
    @SuppressWarnings("unchecked")
	@RequestMapping({ "/stockAnalysis/selectStockList.do" })
    public void c_selectCodeList(HttpServletRequest req, HttpServletResponse res) throws SchedulerException, IOException {
		//PARAMETER KEY VALUE Setting
		HashMap<String, String> map = RequestHandler.extractParameters(req);
    	
        String draw = StringUtil.nvl(req.getParameter("draw"));
        String start = StringUtil.nvl(req.getParameter("start"));
        String length = StringUtil.nvl(req.getParameter("length"));
        int totalPagesize = Integer.parseInt(start) + Integer.parseInt(length);
        int currentPageSize = Integer.parseInt(start);
        
        map.put("totalPagesize", String.valueOf(totalPagesize));
        map.put("currentPageSize", String.valueOf(currentPageSize));
        
		List<StockInfoVo> list = null;
		try {
			list = (List<StockInfoVo>) stockAnalysisDao.selectStockList(map);
			DataTableSettingVo resultVo = new DataTableSettingVo();
	    	resultVo.setData(list);
	    	resultVo.setDraw(draw);
	    	resultVo.setStart_no(Integer.parseInt(start));
	    	resultVo.setPage_length(Integer.parseInt(length));
	    	resultVo.setRecordsFiltered(list.size());
	    	resultVo.setRecordsTotal(list.isEmpty() ? 0 : list.get(0).getTotal_count());
	
	    	ResponseHandler.sendResponse(res,ResultMsg.SUCCESS_CODE,ResultMsg.SUCCESS_MSG, resultVo);
	    } catch (Exception e) {
	    	ResponseHandler.sendResponse(res,ResultMsg.ERROR_CODE,e.getLocalizedMessage(), null);
	    }
    }

    @RequestMapping({ "/stockAnalysis/updateStockCode.do" })
    public ModelAndView c_saveCode(HttpServletRequest req, HttpServletResponse res) throws SchedulerException {
    	 ModelAndView mav = new ModelAndView();
         UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
         if (userSession == null) {
         	mav.addObject(ResultMsg.RESULT_CODE, "error");
         	mav.addObject(ResultMsg.RESULT_MSG, "Session이 종료되었습니다.");
 			mav.setViewName("comm/result/sessionError");
             return mav;
         }
        String use_div = req.getParameter("chk_useYn");
        
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("stock_code", req.getParameter("txt_stock_code"));
        map.put("stock_country_code", req.getParameter("txt_stock_country_code"));
        map.put("stock_market", req.getParameter("txt_stock_market"));
        map.put("stock_type", req.getParameter("txt_stock_type"));
        map.put("use_div", use_div);
        map.put("in_user_id", userSession.user_id);
        try {
            this.stockAnalysisDao.update_stockCode(map);
        }
        catch (Exception e) {
            throw new SchedulerException(String.valueOf(this.getClass().getName()) + ".c_insertCode : " + e.getLocalizedMessage());
        }
        mav.addObject(ResultMsg.RESULT_CODE, "success");
        mav.addObject(ResultMsg.RESULT_MSG, "정상 등록 되었습니다.");
        mav.setViewName("comm/result/result_msg_script");
        return mav;
    }
    
    @RequestMapping({ "/stockAnalysis/deleteStockCode.do" })
    public ModelAndView deleteStockCode( HttpServletRequest req,  HttpServletResponse res) throws SchedulerException {
    	ModelAndView mav = new ModelAndView();
        UserSession userSession = (UserSession)req.getSession().getAttribute(UserSession.KEY);
        if (userSession == null) {
        	mav.addObject(ResultMsg.RESULT_CODE, "error");
        	mav.addObject(ResultMsg.RESULT_MSG, "Session이 종료되었습니다.");
			mav.setViewName("comm/result/sessionError");
            return mav;
        }
        /* 삭제조건 변수 */
		String delStockCode = StringUtil.nvl(req.getParameter("deleteList"));
		String[] listStock  = delStockCode.split(",");
		List<String> list = new ArrayList<String>();
		for(String code : listStock){
			list.add(code);
		}
		try {
			//codeManageDao.delete(map);
			stockAnalysisDao.deleteStockCode(list, userSession.user_id);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SchedulerException(
					String.valueOf(this.getClass().getName()) + ".c_deleteCode() : " + e.getLocalizedMessage());
		}
		mav.addObject(ResultMsg.RESULT_CODE, "success");
        mav.addObject(ResultMsg.RESULT_MSG, "정상 삭제 되었습니다.");
        mav.setViewName("comm/result/result_msg_script");
        return mav;
    }

    /**
     * 이동평균선 정배열 + 장대양봉/연속양봉 패턴 감지
     *
     * 요청 파라미터:
     * - stockCode: 종목 코드 (예: 005930 = 삼성전자)
     * - chartData: 차트 데이터 JSON 배열 ([time, open, high, low, close, volume])
     * - consecutiveBullDays: 연속양봉 기준 일수 (기본: 3)
     * - longCandleRatio: 장대양봉 배율 기준 (기본: 2.0)
     *
     * 응답: { signals: [...], count: N }
     */
    @RequestMapping({ "/finance/detectBullishMomentum.do" })
    public void detectBullishMomentum(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = res.getWriter();

        try {
            String stockCode = StringUtil.nvl(req.getParameter("stockCode"), "");
            String stockName = StringUtil.nvl(req.getParameter("stockName"), "");
            String chartDataJson = StringUtil.nvl(req.getParameter("chartData"), "");

            // 파라미터 파싱
            int consecutiveBullDays = 3;
            double longCandleRatio = 2.0;

            try {
                consecutiveBullDays = Integer.parseInt(
                    StringUtil.nvl(req.getParameter("consecutiveBullDays"), "3"));
            } catch (Exception e) {
                // 기본값 사용
            }

            try {
                longCandleRatio = Double.parseDouble(
                    StringUtil.nvl(req.getParameter("longCandleRatio"), "2.0"));
            } catch (Exception e) {
                // 기본값 사용
            }

            if (stockCode.isEmpty() || chartDataJson.isEmpty()) {
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("message", "stockCode와 chartData는 필수입니다.");
                errorResult.put("signals", new ArrayList<>());
                pw.print(errorResult.toString());
                pw.flush();
                return;
            }

            // 차트 데이터 파싱
            List<?> chartList = null;
            try {
                JSONObject chartObj = JSONObject.fromObject(chartDataJson);
                if (chartObj.has("data")) {
                    Object dataObj = chartObj.get("data");
                    if (dataObj instanceof List) {
                        chartList = (List<?>) dataObj;
                    }
                }
                if (chartList == null) {
                    chartList = JSONObject.fromObject(chartDataJson).getJSONArray("data");
                }
            } catch (Exception e) {
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("message", "chartData 파싱 실패: " + e.getMessage());
                errorResult.put("signals", new ArrayList<>());
                pw.print(errorResult.toString());
                pw.flush();
                return;
            }

            if (chartList == null || chartList.isEmpty()) {
                JSONObject result = new JSONObject();
                result.put("success", true);
                result.put("signals", new ArrayList<>());
                result.put("count", 0);
                result.put("stockCode", stockCode);
                result.put("stockName", stockName);
                pw.print(result.toString());
                pw.flush();
                return;
            }

            // 신호 감지
            List<BullishMomentumSignal> signals =
                BullishMomentumDetector.detectBullishMomentumFromChartList(
                    chartList, stockCode, stockName, consecutiveBullDays, longCandleRatio);

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("signals", signals);
            result.put("count", signals.size());
            result.put("stockCode", stockCode);
            result.put("stockName", stockName);
            pw.print(result.toString());
            pw.flush();

        } catch (Exception e) {
            JSONObject errorResult = new JSONObject();
            errorResult.put("success", false);
            errorResult.put("message", e.getMessage());
            errorResult.put("signals", new ArrayList<>());
            pw.print(errorResult.toString());
            pw.flush();
        }
    }

    /**
     * (MVP) 포지션 진입 전 평가: 일봉 + 30분봉 데이터를 받아 진입 준비/손절/TP1(+1R) 플랜을 산출
     *
     * 요청 파라미터:
     * - stockCode (선택)
     * - stockName (선택)
     * - dailyChartData: {"data": [[time, open, high, low, close, volume], ...]}
     * - m30ChartData:   {"data": [[time, open, high, low, close, volume], ...]}
     */
    @RequestMapping({ "/finance/evaluatePositionPlan.do" })
    public void evaluatePositionPlan(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = res.getWriter();

        final long t0 = System.currentTimeMillis();
        final String traceId = UUID.randomUUID().toString().substring(0, 8);

        try {
            String stockCode = StringUtil.nvl(req.getParameter("stockCode"), "");
            String stockName = StringUtil.nvl(req.getParameter("stockName"), "");
            String dailyChartDataJson = StringUtil.nvl(req.getParameter("dailyChartData"), "");
            String m30ChartDataJson = StringUtil.nvl(req.getParameter("m30ChartData"), "");

            if (dailyChartDataJson.isEmpty() || m30ChartDataJson.isEmpty()) {
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("code", "MISSING_CHART_DATA");
                errorResult.put("traceId", traceId);
                errorResult.put("message", "dailyChartData와 m30ChartData는 필수입니다.");
                pw.print(errorResult.toString());
                pw.flush();
                return;
            }

            final long p0 = System.currentTimeMillis();
            final List<StockDataVo> daily = parseChartDataToCandles(dailyChartDataJson);
            final List<StockDataVo> m30 = parseChartDataToCandles(m30ChartDataJson);
            final long parseMs = System.currentTimeMillis() - p0;

            ExecutorService es = Executors.newSingleThreadExecutor();
            try {
                Future<PositionRuleEngine.Result> f = es.submit(new Callable<PositionRuleEngine.Result>() {
                    @Override
                    public PositionRuleEngine.Result call() {
                        return PositionRuleEngine.evaluatePreEntry(daily, m30);
                    }
                });

                PositionRuleEngine.Result r = f.get(EVAL_PLAN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                PositionPlanVo plan = r.getPlan();
                List<PositionSignalVo> signals = r.getSignals();

                long totalMs = System.currentTimeMillis() - t0;
                LOG.info("[AI-EVAL] trace=" + traceId
                        + " code=" + stockCode
                        + " daily=" + daily.size()
                        + " m30=" + m30.size()
                        + " parseMs=" + parseMs
                        + " totalMs=" + totalMs);

                JSONObject result = new JSONObject();
                result.put("success", true);
                result.put("traceId", traceId);
                result.put("durationMs", totalMs);
                result.put("stockCode", stockCode);
                result.put("stockName", stockName);
                result.put("dailyCount", daily.size());
                result.put("m30Count", m30.size());
                result.put("plan", plan);
                result.put("signals", signals);
                pw.print(result.toString());
                pw.flush();
            } catch (TimeoutException te) {
                long totalMs = System.currentTimeMillis() - t0;
                LOG.warning("[AI-EVAL-TIMEOUT] trace=" + traceId
                        + " code=" + stockCode
                        + " daily=" + daily.size()
                        + " m30=" + m30.size()
                        + " parseMs=" + parseMs
                        + " totalMs=" + totalMs);

                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("code", "EVAL_TIMEOUT");
                errorResult.put("traceId", traceId);
                errorResult.put("durationMs", totalMs);
                errorResult.put("dailyCount", daily.size());
                errorResult.put("m30Count", m30.size());
                errorResult.put("message", "AI 검토 계산 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.");
                pw.print(errorResult.toString());
                pw.flush();
            } finally {
                es.shutdownNow();
            }

        } catch (Exception e) {
            long totalMs = System.currentTimeMillis() - t0;
            LOG.warning("[AI-EVAL-ERROR] trace=" + traceId + " ms=" + totalMs + " msg=" + e.getMessage());
            JSONObject errorResult = new JSONObject();
            errorResult.put("success", false);
            errorResult.put("code", "EVAL_ERROR");
            errorResult.put("traceId", traceId);
            errorResult.put("durationMs", totalMs);
            errorResult.put("message", e.getMessage());
            pw.print(errorResult.toString());
            pw.flush();
        }
    }

    /**
     * WF-2-2c-BASE: 지수 전용 추세 평가
     * 요청 파라미터:
     * - market: KR | US (optional)
     * - chartData: {"data": [[time,open,high,low,close,volume], ...]}
     */
    @RequestMapping({ "/finance/evaluateIndexTrend.do" })
    public void evaluateIndexTrend(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = res.getWriter();

        try {
            String market = StringUtil.nvl(req.getParameter("market"), "");
            String chartDataJson = StringUtil.nvl(req.getParameter("chartData"), "");

            if (chartDataJson.isEmpty()) {
                JSONObject out = new JSONObject();
                out.put("success", false);
                out.put("code", "MISSING_CHART_DATA");
                out.put("message", "chartData는 필수입니다.");
                pw.print(out.toString());
                pw.flush();
                return;
            }

            List<StockDataVo> candles = parseChartDataToCandles(chartDataJson);
            IndexTrendRuleModule.Result r = IndexTrendRuleModule.evaluateDailyIndexTrend(candles);

            JSONObject out = new JSONObject();
            out.put("success", true);
            out.put("market", market);
            out.put("stateCode", r.getStateCode());
            out.put("stateLabelKo", r.getStateLabelKo());
            out.put("stateLabelEn", r.getStateLabelEn());
            out.put("signals", r.getSignals());
            out.put("count", (r.getSignals() == null ? 0 : r.getSignals().size()));
            pw.print(out.toString());
            pw.flush();

        } catch (Exception e) {
            JSONObject out = new JSONObject();
            out.put("success", false);
            out.put("code", "INDEX_EVAL_ERROR");
            out.put("message", e.getMessage());
            pw.print(out.toString());
            pw.flush();
        }
    }

    @SuppressWarnings("unchecked")
    private List<StockDataVo> parseChartDataToCandles(String chartDataJson) {
        List<StockDataVo> candles = new ArrayList<StockDataVo>();

        if (chartDataJson == null || chartDataJson.trim().isEmpty()) return candles;

        Object dataObj = null;
        try {
            JSONObject chartObj = JSONObject.fromObject(chartDataJson);
            if (chartObj.has("data")) {
                dataObj = chartObj.get("data");
            }
        } catch (Exception ignore) {
            // 일부 케이스에서는 chartDataJson 자체가 data 배열일 수 있음
        }

        List<?> rows = null;
        if (dataObj instanceof List) {
            rows = (List<?>) dataObj;
        }

        if (rows == null) {
            try {
                rows = JSONObject.fromObject(chartDataJson).getJSONArray("data");
            } catch (Exception ignore) {
                // noop
            }
        }

        if (rows == null) return candles;

        for (Object row : rows) {
            try {
                if (!(row instanceof List)) {
                    continue;
                }
                List<?> r = (List<?>) row;
                if (r.size() < 6) continue;

                long t = toLong(r.get(0));
                double o = toDouble(r.get(1));
                double h = toDouble(r.get(2));
                double l = toDouble(r.get(3));
                double c = toDouble(r.get(4));
                double v = toDouble(r.get(5));

                candles.add(new StockDataVo(t, o, h, l, c, v));
            } catch (Exception ignore) {
                // skip malformed row
            }
        }

        return candles;
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }

    private double toDouble(Object v) {
        if (v == null) return 0.0;
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 모바일 차트 페이지
     */
    @RequestMapping({ "/finance/mobile/chart.do" })
    public ModelAndView mobileChart(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        // viewResolver: prefix=/appone/jsp/, suffix=.jsp
        mv.setViewName("finance/kis/mobile/chart");
        return mv;
    }

    /**
     * 모바일 관심종목 페이지
     */
    @RequestMapping({ "/finance/mobile/watchlist.do" })
    public ModelAndView mobileWatchlist(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("finance/kis/mobile/watchlist");
        return mv;
    }

    /**
     * 모바일 발굴분석 페이지
     */
    @RequestMapping({ "/finance/mobile/analysis.do" })
    public ModelAndView mobileAnalysis(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("finance/kis/mobile/analysis");
        return mv;
    }
}
