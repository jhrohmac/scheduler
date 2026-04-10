package com.scheduler.stock.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.stock.batch.RecSignalBatch;
import com.scheduler.stock.batch.RecSignalDailyBatch;
import com.scheduler.stock.dto.RecSignalDto;
import com.scheduler.stock.service.KisDlyPriceSyncService;
import com.scheduler.stock.service.RecSignalService;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

@Controller
public class RecSignalController {

    private RecSignalService recSignalService;
    private RecSignalBatch recSignalBatch;
    private RecSignalDailyBatch recSignalDailyBatch;
    private KisDlyPriceSyncService kisDlyPriceSyncService;

    public void setRecSignalService(RecSignalService recSignalService) {
        this.recSignalService = recSignalService;
    }

    public void setRecSignalBatch(RecSignalBatch recSignalBatch) {
        this.recSignalBatch = recSignalBatch;
    }

    public void setRecSignalDailyBatch(RecSignalDailyBatch recSignalDailyBatch) {
        this.recSignalDailyBatch = recSignalDailyBatch;
    }

    public void setKisDlyPriceSyncService(KisDlyPriceSyncService kisDlyPriceSyncService) {
        this.kisDlyPriceSyncService = kisDlyPriceSyncService;
    }

    @RequestMapping("/stock/recSignal/listView.do")
    public ModelAndView openRecSignalListView(HttpServletRequest req, HttpServletResponse res) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("stock/recSignalList");
        return mv;
    }

    @RequestMapping("/stock/recSignal/detailView.do")
    public ModelAndView openRecSignalDetailView(HttpServletRequest req, HttpServletResponse res) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("baseDt", req.getParameter("baseDt"));
        mv.addObject("stkCd", req.getParameter("stkCd"));
        mv.addObject("mktCd", req.getParameter("mktCd"));
        mv.setViewName("stock/recSignalDetail");
        return mv;
    }

    @RequestMapping("/stock/recSignal/list.do")
    public void selectRecSignalList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<RecSignalDto> list = recSignalService.selectRecSignalList(map);
            if (list == null) {
                list = new ArrayList<RecSignalDto>();
            }

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

    @RequestMapping("/stock/recSignal/detail.do")
    public void selectRecSignalDetail(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            RecSignalDto dto = recSignalService.selectRecSignalDetail(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(dto);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/recSignal/batchStatus.do")
    public void selectRecSignalBatchStatus(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            HashMap<String, Object> statusMap = recSignalService.selectBatchStatus(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(statusMap);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/recSignal/runBatch.do")
    public void runRecSignalBatch(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            HashMap<String, Object> resultMap;
            if (recSignalDailyBatch != null) {
                resultMap = recSignalDailyBatch.run(map);
            } else {
                resultMap = recSignalBatch.run(map);
            }

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(resultMap);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/recSignal/syncDlyPrice.do")
    public void syncRecSignalDlyPrice(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            HashMap<String, Object> resultMap = kisDlyPriceSyncService.sync(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(resultMap);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
}
