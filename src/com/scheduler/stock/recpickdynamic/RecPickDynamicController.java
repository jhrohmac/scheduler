package com.scheduler.stock.recpickdynamic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.stock.recpickdynamic.service.RecPickDynamicService;
import com.scheduler.stock.recpickdynamic.vo.FilterRequestVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

/**
 * 동적 추천 종목 선별 화면 컨트롤러.
 *  /stock/recPickDynamic/listView.do          (화면 진입)
 *  /stock/recPickDynamic/indicatorMeta.do     (지표 메타데이터)
 *  /stock/recPickDynamic/list.do              (필터 결과)
 *  /stock/recPickDynamic/detail.do            (상세 슬라이드용)
 */
@Controller
public class RecPickDynamicController {

    private RecPickDynamicService recPickDynamicService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setRecPickDynamicService(RecPickDynamicService recPickDynamicService) {
        this.recPickDynamicService = recPickDynamicService;
    }

    @RequestMapping("/stock/recPickDynamic/listView.do")
    public ModelAndView openListView(HttpServletRequest req, HttpServletResponse res) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("stock/recPickDynamic/recPickDynamic");
        return mv;
    }

    /** 지표 메타데이터 — 화면 진입 시 1회 호출 */
    @RequestMapping("/stock/recPickDynamic/indicatorMeta.do")
    public void getIndicatorMeta(HttpServletRequest req, HttpServletResponse res) {
        try {
            List<Map<String, Object>> list = recPickDynamicService.getIndicatorMetadata();
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(list);
            resultVo.setRecordsTotal(list.size());
            resultVo.setRecordsFiltered(list.size());
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /** 동적 종목 검색 — POST JSON */
    @RequestMapping("/stock/recPickDynamic/list.do")
    public void selectList(HttpServletRequest req, HttpServletResponse res) {
        try {
            FilterRequestVo filterReq = parseFilterRequest(req);
            Map<String, Object> result = recPickDynamicService.selectStockList(filterReq);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
            int total = ((Number) result.get("totalCount")).intValue();

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(data);
            resultVo.setRecordsTotal(total);
            resultVo.setRecordsFiltered(total);

            // 추가 메타정보는 singleData 에 동봉 (baseDt, gradeCount, indicatorCount)
            HashMap<String, Object> meta = new HashMap<String, Object>();
            meta.put("baseDt", result.get("baseDt"));
            meta.put("totalCount", total);
            meta.put("indicatorCount", result.get("indicatorCount"));
            meta.put("gradeCount", result.get("gradeCount"));
            resultVo.setSingleData(meta);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /** 종목 상세 (슬라이드 패널용) */
    @RequestMapping("/stock/recPickDynamic/detail.do")
    public void selectDetail(HttpServletRequest req, HttpServletResponse res) {
        try {
            HashMap<String, String> map = RequestHandler.extractParameters(req);
            Map<String, Object> result = recPickDynamicService.selectStockDetail(
                map.get("baseDt"), map.get("mktCd"), map.get("stkCd"));
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(result);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    // ────────────────────────────────────────────────────
    // request 파서 — JSON 본문 또는 form param 양쪽 지원
    // ────────────────────────────────────────────────────
    private FilterRequestVo parseFilterRequest(HttpServletRequest req) throws Exception {
        String contentType = req.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            // JSON body
            StringBuilder sb = new StringBuilder();
            String line;
            java.io.BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null) sb.append(line);
            if (sb.length() == 0) return new FilterRequestVo();
            return objectMapper.readValue(sb.toString(), FilterRequestVo.class);
        }

        // form param fallback (filterJson 한 덩어리로 받음)
        HashMap<String, String> params = RequestHandler.extractParameters(req);
        String filterJson = params.get("filterJson");
        if (filterJson != null && !filterJson.isEmpty()) {
            return objectMapper.readValue(filterJson, FilterRequestVo.class);
        }

        // 최소 파라미터로 구성
        FilterRequestVo vo = new FilterRequestVo();
        if (params.get("mktGroup") != null) vo.setMktGroup(params.get("mktGroup"));
        if (params.get("marketFilter") != null) vo.setMarketFilter(params.get("marketFilter"));
        if (params.get("stockType") != null) vo.setStockType(params.get("stockType"));
        return vo;
    }
}
