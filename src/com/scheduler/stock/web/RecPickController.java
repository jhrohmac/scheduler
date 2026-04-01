package com.scheduler.stock.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.stock.dto.PositionSellGuideDto;
import com.scheduler.stock.dto.RecPickDailyDto;
import com.scheduler.stock.dto.RecPickDto;
import com.scheduler.stock.dto.RecProbabilityDto;
import com.scheduler.stock.service.PositionExitSignalService;
import com.scheduler.stock.service.RecPickService;
import com.scheduler.stock.service.RecPickTrackService;
import com.scheduler.stock.service.RecProbabilityService;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

@Controller
public class RecPickController {

    private RecPickService recPickService;
    private RecPickTrackService recPickTrackService;
    private RecProbabilityService recProbabilityService;
    private PositionExitSignalService positionExitSignalService;

    public void setRecPickService(RecPickService recPickService) {
        this.recPickService = recPickService;
    }

    public void setRecPickTrackService(RecPickTrackService recPickTrackService) {
        this.recPickTrackService = recPickTrackService;
    }

    public void setRecProbabilityService(RecProbabilityService recProbabilityService) {
        this.recProbabilityService = recProbabilityService;
    }

    public void setPositionExitSignalService(PositionExitSignalService positionExitSignalService) {
        this.positionExitSignalService = positionExitSignalService;
    }

    @RequestMapping("/stock/recPick/saveToWatchlist.do")
    public void saveToWatchlist(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            HashMap<String, Object> resultMap = recPickService.saveToWatchlist(map);
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(resultMap);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/recPick/list.do")
    public void selectRecPickList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            List<RecPickDto> list = recPickService.selectRecPickList(map);
            if (list == null) {
                list = new ArrayList<RecPickDto>();
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

    @RequestMapping("/stock/recPick/detail.do")
    public void selectRecPickDetail(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            RecPickDto dto = recPickService.selectRecPickDetail(map);
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(dto);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/recPick/registerBuy.do")
    public void registerBuy(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            HashMap<String, Object> resultMap = recPickService.registerBuy(map);
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(resultMap);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/recPick/dailyTrack.do")
    public void selectDailyTrack(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            List<RecPickDailyDto> list = recPickTrackService.selectDailyTrack(map);
            if (list == null) {
                list = new ArrayList<RecPickDailyDto>();
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

    @RequestMapping("/stock/recPick/probability.do")
    public void selectProbability(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            List<RecProbabilityDto> list = recProbabilityService.selectProbability(map);
            if (list == null) {
                list = new ArrayList<RecProbabilityDto>();
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

    @RequestMapping("/stock/recPick/sellGuide.do")
    public void selectSellGuide(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        try {
            PositionSellGuideDto dto = positionExitSignalService.selectSellGuide(map);
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(dto);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
}
