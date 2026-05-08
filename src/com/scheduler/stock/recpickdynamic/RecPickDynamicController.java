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
import com.scheduler.login.service.UserSession;
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
        // 사용자 ID — 프리셋 저장/복원 키로 사용
        UserSession us = (UserSession) req.getSession().getAttribute(UserSession.KEY);
        mv.addObject("userId", us != null && us.user_id != null ? us.user_id : "anonymous");
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

            // 추가 메타정보는 singleData 에 동봉
            HashMap<String, Object> meta = new HashMap<String, Object>();
            meta.put("baseDt", result.get("baseDt"));
            meta.put("baseDtMin", result.get("baseDtMin"));
            meta.put("baseDtMax", result.get("baseDtMax"));
            meta.put("staleCount", result.get("staleCount"));
            meta.put("totalCount", total);
            meta.put("indicatorCount", result.get("indicatorCount"));
            meta.put("gradeCount", result.get("gradeCount"));
            if (result.get("warning") != null) meta.put("warning", result.get("warning"));
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
    // 사용자 프리셋 — TB_USER_FILTER_PRESET
    // ────────────────────────────────────────────────────

    /** 프리셋 조회 (페이지 진입 시 1회 호출) */
    @RequestMapping("/stock/recPickDynamic/preset/load.do")
    public void loadPreset(HttpServletRequest req, HttpServletResponse res) {
        try {
            String userId = currentUserId(req);
            com.scheduler.stock.recpickdynamic.vo.UserFilterPresetVo vo =
                recPickDynamicService.loadUserPreset(userId);

            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("userId", userId);
            data.put("hasPreset", vo != null);
            if (vo != null) {
                // filterJson 은 JSON 문자열 → JS에서 JSON.parse
                data.put("filterJson", vo.getFilterJson());
                data.put("updDt", vo.getUpdDt());
            }
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(data);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /** 프리셋 저장 (적용 시점에 호출) */
    @RequestMapping("/stock/recPickDynamic/preset/save.do")
    public void savePreset(HttpServletRequest req, HttpServletResponse res) {
        try {
            String userId = currentUserId(req);
            String filterJson = readBodyAsString(req);
            if (filterJson == null || filterJson.trim().isEmpty()) {
                HashMap<String, String> p = RequestHandler.extractParameters(req);
                filterJson = p.get("filterJson");
            }
            int updated = recPickDynamicService.saveUserPreset(userId, filterJson);

            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("userId", userId);
            data.put("updated", updated);
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(data);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /** 프리셋 삭제 (초기화 시점에 호출) */
    @RequestMapping("/stock/recPickDynamic/preset/delete.do")
    public void deletePreset(HttpServletRequest req, HttpServletResponse res) {
        try {
            String userId = currentUserId(req);
            int deleted = recPickDynamicService.deleteUserPreset(userId);

            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("userId", userId);
            data.put("deleted", deleted);
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(data);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    private String currentUserId(HttpServletRequest req) {
        UserSession us = (UserSession) req.getSession().getAttribute(UserSession.KEY);
        return us != null && us.user_id != null ? us.user_id : "anonymous";
    }

    private String readBodyAsString(HttpServletRequest req) {
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            java.io.BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (Exception e) {
            return null;
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
