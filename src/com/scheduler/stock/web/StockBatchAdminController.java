package com.scheduler.stock.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.stock.batchadmin.service.StockBatchAdminService;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

@Controller
public class StockBatchAdminController {

    private StockBatchAdminService stockBatchAdminService;

    public void setStockBatchAdminService(StockBatchAdminService stockBatchAdminService) {
        this.stockBatchAdminService = stockBatchAdminService;
    }

    @Scheduled(fixedDelay = 60000)
    public void stockBatchAdminTick() {
        try {
            Map<String, Object> result = stockBatchAdminService.schedulerTick();
            System.out.println("[StockBatchAdminController] tick=" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/stock/batchAdmin/view.do")
    public ModelAndView openBatchAdminView(HttpServletRequest req, HttpServletResponse res) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("stock/batchAdmin");
        return mv;
    }

    @RequestMapping("/stock/batchAdmin/taskCatalog.do")
    public void selectTaskCatalog(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<HashMap<String, Object>> list = stockBatchAdminService.selectTaskCatalog(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setData(list);
            vo.setRecordsTotal(list == null ? 0 : list.size());
            vo.setRecordsFiltered(list == null ? 0 : list.size());
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobList.do")
    public void selectJobList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<HashMap<String, Object>> list = stockBatchAdminService.selectJobList(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setData(list);
            vo.setRecordsTotal(list == null ? 0 : list.size());
            vo.setRecordsFiltered(list == null ? 0 : list.size());
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobDetail.do")
    public void selectJobDetail(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            HashMap<String, Object> detail = stockBatchAdminService.selectJobDetail(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setSingleData(detail);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobLogList.do")
    public void selectJobLogList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<HashMap<String, Object>> list = stockBatchAdminService.selectJobLogList(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setData(list);
            vo.setRecordsTotal(list == null ? 0 : list.size());
            vo.setRecordsFiltered(list == null ? 0 : list.size());
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobLogItemFailList.do")
    public void selectJobLogItemFailList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<HashMap<String, Object>> list = stockBatchAdminService.selectJobLogItemFailList(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setData(list);
            vo.setRecordsTotal(list == null ? 0 : list.size());
            vo.setRecordsFiltered(list == null ? 0 : list.size());
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobCreate.do")
    public void createJob(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            Map<String, Object> out = stockBatchAdminService.createJob(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setSingleData(out);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobUpdate.do")
    public void updateJob(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            Map<String, Object> out = stockBatchAdminService.updateJob(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setSingleData(out);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobDelete.do")
    public void deleteJob(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            Map<String, Object> out = stockBatchAdminService.deleteJob(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setSingleData(out);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobRunNow.do")
    public void runJobNow(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            Map<String, Object> out = stockBatchAdminService.runJobNow(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setSingleData(out);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping("/stock/batchAdmin/jobStop.do")
    public void stopJob(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            Map<String, Object> out = stockBatchAdminService.stopJob(map);
            DataTableSettingVo vo = new DataTableSettingVo();
            vo.setSingleData(out);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, vo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

}
