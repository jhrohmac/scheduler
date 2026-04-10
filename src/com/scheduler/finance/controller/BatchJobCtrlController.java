package com.scheduler.finance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.BatchJobCtrlDao;
import com.scheduler.finance.vo.BatchJobCtrlVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

@Controller
public class BatchJobCtrlController {

    private BatchJobCtrlDao batchJobCtrlDao;

    public BatchJobCtrlDao getBatchJobCtrlDao() {
        return batchJobCtrlDao;
    }

    public void setBatchJobCtrlDao(BatchJobCtrlDao batchJobCtrlDao) {
        this.batchJobCtrlDao = batchJobCtrlDao;
    }

    /**
     * 스케줄러 틱: 1분마다 1회만 돌고,
     * 실제 배치 실행 여부는 TB_S_BATCH_JOB_CTRL 설정으로 결정
     * (주기 변경/ON-OFF를 재기동 없이 화면에서 제어 가능)
     */
    @Scheduled(fixedDelay = 60000)
    public void batchSchedulerTick() {
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            Map<String, Object> result = batchJobCtrlDao.schedulerTick(map);
            System.out.println("[BatchJobCtrlController] tick=" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping({ "/finance/batchJobCtrlList.do" })
    public void batchJobCtrlList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<BatchJobCtrlVo> list = batchJobCtrlDao.selectJobList(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(list);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping({ "/finance/batchJobCtrlInsert.do" })
    public void batchJobCtrlInsert(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            int inserted = batchJobCtrlDao.insertJobCtrl(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(inserted);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping({ "/finance/batchJobCtrlSave.do" })
    public void batchJobCtrlSave(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            int updated = batchJobCtrlDao.updateJobCtrl(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(updated);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping({ "/finance/batchJobCtrlDelete.do" })
    public void batchJobCtrlDelete(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            int deleted = batchJobCtrlDao.deleteJobCtrl(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(deleted);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    @RequestMapping({ "/finance/batchJobRunNow.do" })
    public void batchJobRunNow(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            final HashMap<String, String> asyncParam = new HashMap<String, String>(map);
            final String jobId = asyncParam.get("job_id");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        batchJobCtrlDao.runJobNow(asyncParam);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setName("batch-job-run-now-" + (jobId == null ? "unknown" : jobId));
            t.setDaemon(true);
            t.start();

            Map<String, Object> out = new HashMap<String, Object>();
            out.put("code", "QUEUED");
            out.put("message", "RUN REQUESTED");
            out.put("job_id", jobId);
            out.put("asOf", java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul"))
                    .toString().replace("T", " ").substring(0, 19));

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(out);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    @RequestMapping({ "/finance/batchJobStop.do" })
    public void batchJobStop(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            Map<String, Object> out = batchJobCtrlDao.stopJob(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(out);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
}
