package com.scheduler.finance.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.util.StringUtil;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.StockChartOptionDao;
import com.scheduler.finance.vo.StockChartOptionVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;
import java.util.Collections;
import java.util.Comparator;
@Controller
public class StockChartOptionController {

    private static final double DEFAULT_MA_LINE_WIDTH = 2.5d;

    private StockChartOptionDao stockChartOptionDao;

    public StockChartOptionDao getStockChartOptionDao() {
        return stockChartOptionDao;
    }

    public void setStockChartOptionDao(StockChartOptionDao stockChartOptionDao) {
        this.stockChartOptionDao = stockChartOptionDao;
    }

    /**
     * 차트 옵션 조회
     */
    @RequestMapping({ "/finance/kisItemchartpriceOptionData.do" })
    public void selectKisChartOptionList(HttpServletRequest req, HttpServletResponse res) throws IOException {

        DataTableSettingVo resultVo = new DataTableSettingVo();
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        String chartId = StringUtil.nvl(map.get("chartId"));
        String seriesType = StringUtil.nvl(map.get("seriesType"));
        String forceDefault = StringUtil.nvl(map.get("forceDefault"));

        if (chartId == null || chartId.trim().length() == 0) {
            chartId = "KIS_ITEMCHART";
        }
        if (seriesType == null || seriesType.trim().length() == 0) {
            seriesType = "MA";
        }

        List<StockChartOptionVo> list = null;

        try {
            if ("Y".equalsIgnoreCase(forceDefault)) {
                list = createDefaultList(chartId, seriesType);
            } else {
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("chartId", chartId);
                paramMap.put("seriesType", seriesType);

                list = stockChartOptionDao.selectChartOptionList(paramMap);

                if (list == null || list.isEmpty()) {
                    list = createDefaultList(chartId, seriesType);
                }
            }

            resultVo.setData(list);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    /**
     * 차트 옵션 저장
     */
    @RequestMapping({ "/finance/kisItemchartpriceOptionSave.do" })
    public void saveKisChartOptionList(HttpServletRequest req, HttpServletResponse res) throws IOException {

        HashMap<String, String> map = RequestHandler.extractParameters(req);

        String chartId = StringUtil.nvl(map.get("chartId"));
        String seriesType = StringUtil.nvl(map.get("seriesType"));
        String optionsJson = StringUtil.nvl(map.get("optionsJson"));

        if (chartId == null || chartId.trim().length() == 0) {
            chartId = "KIS_ITEMCHART";
        }
        if (seriesType == null || seriesType.trim().length() == 0) {
            seriesType = "MA";
        }

        try {
            List<StockChartOptionVo> optionList = new ArrayList<StockChartOptionVo>();

            if (optionsJson != null && optionsJson.trim().length() > 0) {
                JSONArray arr = new JSONArray(optionsJson);

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    String seriesKey = obj.optString("seriesKey", null);
                    String seriesLabel = obj.optString("seriesLabel", "");
                    int period = obj.optInt("seriesPeriod", 0);
                    String color = obj.optString("seriesColor", "#000000");
                    String enabledYn = obj.optString("enabledYn", "Y");
                    Double lineWidth = null;
                    Object lineWidthObj = obj.opt("lineWidth");
                    if (lineWidthObj != null) {
                        try {
                            double lw = Double.parseDouble(lineWidthObj.toString());
                            if (lw > 0) {
                                lineWidth = Double.valueOf(lw);
                            }
                        } catch (Exception ignore) {}
                    }
                    int displayOrder = obj.optInt("displayOrder", i + 1);

                    if (seriesKey == null || seriesKey.trim().length() == 0) {
                        continue;
                    }
                    if (seriesLabel == null || seriesLabel.trim().length() == 0) {
                        continue;
                    }
                    if ("MA".equalsIgnoreCase(seriesType) && period <= 0) {
                        continue;
                    }

                    StockChartOptionVo vo = new StockChartOptionVo();
                    vo.setChartId(chartId);
                    vo.setSeriesType(seriesType);
                    vo.setSeriesKey(seriesKey);
                    vo.setSeriesLabel(seriesLabel);
                    vo.setSeriesPeriod(Integer.valueOf(period));
                    vo.setSeriesColor(color);
                    if ("MA".equalsIgnoreCase(seriesType)) {
                        if (lineWidth == null || lineWidth.doubleValue() <= 0) {
                            lineWidth = Double.valueOf(DEFAULT_MA_LINE_WIDTH);
                        }
                        vo.setLineWidth(lineWidth);
                    } else {
                        vo.setLineWidth(lineWidth);
                    }
                    vo.setEnabledYn("Y".equalsIgnoreCase(enabledYn) ? "Y" : "N");
                    vo.setDisplayOrder(Integer.valueOf(displayOrder));

                    optionList.add(vo);
                }
            }

            // SERIES_PERIOD 기준으로 오름차순 정렬 후 DISPLAY_ORDER 재설정
            if (!optionList.isEmpty()) {
                Collections.sort(optionList, new Comparator<StockChartOptionVo>() {
                    @Override
                    public int compare(StockChartOptionVo o1, StockChartOptionVo o2) {
                        Integer p1 = o1.getSeriesPeriod();
                        Integer p2 = o2.getSeriesPeriod();
                        if (p1 == null && p2 == null) return 0;
                        if (p1 == null) return 1;
                        if (p2 == null) return -1;
                        return p1.compareTo(p2);
                    }
                });

                int order = 1;
                for (StockChartOptionVo vo : optionList) {
                    vo.setDisplayOrder(Integer.valueOf(order++));
                }
            }

            HashMap<String, String> delParam = new HashMap<String, String>();
            delParam.put("chartId", chartId);
            delParam.put("seriesType", seriesType);
            stockChartOptionDao.deleteChartOptions(delParam);

            for (StockChartOptionVo vo : optionList) {
                stockChartOptionDao.insertChartOption(vo);
            }

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, null);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /**
     * KIS_ITEMCHART / MA 기본값 생성
     */
    private List<StockChartOptionVo> createDefaultList(String chartId, String seriesType) {
        if ("FEATURE".equalsIgnoreCase(seriesType)) {
            return createDefaultFeatureList(chartId, seriesType);
        }
        if ("CROSS".equalsIgnoreCase(seriesType)) {
            return createDefaultCrossList(chartId, seriesType);
        }
        return createDefaultMaList(chartId, seriesType);
    }

    private List<StockChartOptionVo> createDefaultMaList(String chartId, String seriesType) {
        List<StockChartOptionVo> list = new ArrayList<StockChartOptionVo>();

        list.add(createMaOption(chartId, seriesType, "ma5", "5일선", 5, "#26a69a", Double.valueOf(DEFAULT_MA_LINE_WIDTH), "Y", 1));
        list.add(createMaOption(chartId, seriesType, "ma20", "20일선", 20, "#4caf50", Double.valueOf(DEFAULT_MA_LINE_WIDTH), "Y", 2));
        list.add(createMaOption(chartId, seriesType, "ma60", "60일선", 60, "#ff9800", Double.valueOf(DEFAULT_MA_LINE_WIDTH), "Y", 3));
        list.add(createMaOption(chartId, seriesType, "ma120", "120일선", 120, "#9c27b0", Double.valueOf(DEFAULT_MA_LINE_WIDTH), "Y", 4));
        list.add(createMaOption(chartId, seriesType, "ma240", "240일선", 240, "#607d8b", Double.valueOf(DEFAULT_MA_LINE_WIDTH), "Y", 5));

        return list;
    }

    private List<StockChartOptionVo> createDefaultFeatureList(String chartId, String seriesType) {
        List<StockChartOptionVo> list = new ArrayList<StockChartOptionVo>();
        list.add(createMaOption(chartId, seriesType, "volumeEnabled", "거래량", 0, "#000000", null, "Y", 1));
        list.add(createMaOption(chartId, seriesType, "doubleChartEnabled", "월봉 오버레이(더블차트)", 0, "#000000", null, "Y", 2));
        list.add(createMaOption(chartId, seriesType, "monthLinesEnabled", "월/년 구분선", 0, "#000000", null, "Y", 3));
        list.add(createMaOption(chartId, seriesType, "highLowEnabled", "전고/전저", 0, "#000000", null, "Y", 4));
        return list;
    }

    private List<StockChartOptionVo> createDefaultCrossList(String chartId, String seriesType) {
        List<StockChartOptionVo> list = new ArrayList<StockChartOptionVo>();
        list.add(createMaOption(chartId, seriesType, "crossSignals", "골든/데드", 0, "#000000", null, "N", 1));
        list.add(createMaOption(chartId, seriesType, "crossShort", "단기선", 5, "#000000", null, "Y", 2));
        list.add(createMaOption(chartId, seriesType, "crossLong", "장기선", 20, "#000000", null, "Y", 3));
        return list;
    }

    private StockChartOptionVo createMaOption(String chartId,
                                              String seriesType,
                                              String key,
                                              String label,
                                              int period,
                                              String color,
                                              Double lineWidth,
                                              String enabledYn,
                                              int displayOrder) {

        StockChartOptionVo vo = new StockChartOptionVo();
        vo.setChartId(chartId);
        vo.setSeriesType(seriesType);
        vo.setSeriesKey(key);
        vo.setSeriesLabel(label);
        vo.setSeriesPeriod(Integer.valueOf(period));
        vo.setSeriesColor(color);
        vo.setLineWidth(lineWidth);
        vo.setEnabledYn(enabledYn);
        vo.setDisplayOrder(Integer.valueOf(displayOrder));
        return vo;
    }
}
