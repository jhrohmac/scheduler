package com.scheduler.finance.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.PositionDao;
import com.scheduler.finance.vo.PositionVo;
import com.scheduler.finance.vo.PositionTxnVo;
import com.scheduler.finance.vo.PositionEventVo;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

/**
 * WF-2-4: 보유종목 관리 컨트롤러
 * - 설계: _workflow/notes/30_BUSINESS_LOGIC_FLOW.md
 */
@Controller
public class PositionController {
    
    private PositionDao positionDao;
    
    public PositionDao getPositionDao() {
        return positionDao;
    }
    
    public void setPositionDao(PositionDao positionDao) {
        this.positionDao = positionDao;
    }
    
    /**
     * 보유종목 목록 조회
     * param: stockGroup (선택)
     * param: closeFlag (N=보유중, Y=청산, 선택)
     */
    @RequestMapping("/position/list.do")
    public void selectPositionList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            List<PositionVo> list = positionDao.selectPositionList(map);
            if (list == null) {
                list = new ArrayList<PositionVo>();
            }
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(list);
            
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * 보유종목 상세 조회
     * param: positionId (필수)
     */
    @RequestMapping("/position/detail.do")
    public void selectPosition(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            PositionVo vo = positionDao.selectPosition(map);

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(vo);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * 보유종목 추가(ADD)
     * param: stockGroup (필수)
     * param: stockCode (필수)
     * param: marketCode (KR/US, 선택, 기본 KR)
     * param: qty (수량, 필수)
     * param: price (매입가, 필수)
     * param: reasonText (사유, 선택)
     */
    @RequestMapping("/position/add.do")
    public void addPosition(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            // VO 생성
            PositionVo vo = new PositionVo();
            vo.setStockGroup(map.get("stockGroup"));
            vo.setStockCode(map.get("stockCode"));
            vo.setMarketCode(map.getOrDefault("marketCode", "KR"));
            vo.setTotalQty(Integer.parseInt(map.get("qty")));
            vo.setAvgPrice(Double.parseDouble(map.get("price")));
            vo.setCloseFlag("N");
            vo.setStateCode("HOLD");
            vo.setCreateUser(map.get("userId"));
            
            // INSERT
            int result = positionDao.insertPosition(vo);
            
            // 거래 내역 기록 + 이벤트 기록
            if (result > 0 && vo.getPositionId() != null) {
                PositionTxnVo txn = new PositionTxnVo();
                txn.setPositionId(vo.getPositionId());
                txn.setStockGroup(vo.getStockGroup());
                txn.setStockCode(vo.getStockCode());
                txn.setActionType("ADD");
                txn.setQty(vo.getTotalQty());
                txn.setPrice(vo.getAvgPrice());
                txn.setAfterQty(vo.getTotalQty());
                txn.setAfterAvg(vo.getAvgPrice());
                txn.setReasonText(map.get("reasonText"));
                txn.setCreateUser(map.get("userId"));
                
                positionDao.insertPositionTxn(txn);

                insertPositionEventSafe(
                    vo.getPositionId(),
                    vo.getStockGroup(),
                    vo.getStockCode(),
                    "ENTER",
                    "INFO",
                    "보유 추가: " + vo.getStockCode() + " / " + vo.getTotalQty() + "주 @" + vo.getAvgPrice(),
                    map.get("reasonText")
                );
            }
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(vo);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * 보유종목 삭제(DELETE)
     * param: positionId (필수)
     * param: reasonText (사유, 선택)
     */
    @RequestMapping("/position/delete.do")
    public void deletePosition(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            PositionVo before = positionDao.selectPosition(map);

            // CLOSE_FLAG='Y' 처리
            int result = positionDao.deletePosition(map);
            
            // 거래 내역 기록 + 이벤트 기록
            if (result > 0) {
                PositionTxnVo txn = new PositionTxnVo();
                txn.setPositionId(Long.parseLong(map.get("positionId")));
                txn.setStockGroup(before != null ? before.getStockGroup() : null);
                txn.setStockCode(before != null ? before.getStockCode() : null);
                txn.setActionType("DELETE");
                txn.setReasonText(map.get("reasonText"));
                txn.setCreateUser(map.get("userId"));
                
                positionDao.insertPositionTxn(txn);

                insertPositionEventSafe(
                    Long.parseLong(map.get("positionId")),
                    before != null ? before.getStockGroup() : null,
                    before != null ? before.getStockCode() : null,
                    "EXIT",
                    "INFO",
                    "보유 삭제: " + (before != null ? before.getStockCode() : "") + " / 청산 처리",
                    map.get("reasonText")
                );
            }
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(result);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * 물타기(AVERAGE_DOWN)
     * param: positionId (필수)
     * param: addQty (추가 수량, 필수)
     * param: addPrice (추가 매입가, 필수)
     * param: reasonText (사유, 선택)
     */
    @RequestMapping("/position/averageDown.do")
    public void averageDownPosition(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            // 기존 포지션 조회
            PositionVo pos = positionDao.selectPosition(map);
            if (pos == null) {
                ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, "보유종목을 찾을 수 없습니다.", null);
                return;
            }
            
            int addQty = Integer.parseInt(map.get("addQty"));
            double addPrice = Double.parseDouble(map.get("addPrice"));
            
            // 평단 재계산
            int beforeQty = pos.getTotalQty();
            double beforeAvg = pos.getAvgPrice();
            int afterQty = beforeQty + addQty;
            double afterAvg = ((beforeQty * beforeAvg) + (addQty * addPrice)) / afterQty;
            
            // 업데이트
            pos.setTotalQty(afterQty);
            pos.setAvgPrice(afterAvg);
            int result = positionDao.updatePosition(pos);
            
            // 거래 내역 기록 + 이벤트 기록
            if (result > 0) {
                PositionTxnVo txn = new PositionTxnVo();
                txn.setPositionId(pos.getPositionId());
                txn.setStockGroup(pos.getStockGroup());
                txn.setStockCode(pos.getStockCode());
                txn.setActionType("AVERAGE_DOWN");
                txn.setQty(addQty);
                txn.setPrice(addPrice);
                txn.setBeforeQty(beforeQty);
                txn.setBeforeAvg(beforeAvg);
                txn.setAfterQty(afterQty);
                txn.setAfterAvg(afterAvg);
                txn.setReasonText(map.get("reasonText"));
                txn.setCreateUser(map.get("userId"));
                
                positionDao.insertPositionTxn(txn);

                insertPositionEventSafe(
                    pos.getPositionId(),
                    pos.getStockGroup(),
                    pos.getStockCode(),
                    "RISK_OFF",
                    "INFO",
                    "물타기: " + pos.getStockCode() + " / " + beforeQty + "→" + afterQty + "주, 평단 " + beforeAvg + "→" + afterAvg,
                    map.get("reasonText")
                );
            }
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(pos);
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * 거래 내역 조회
     * param: positionId (선택)
     * param: stockCode (선택)
     */
    @RequestMapping("/position/txnList.do")
    public void selectPositionTxnList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            List<PositionTxnVo> list = positionDao.selectPositionTxnList(map);
            if (list == null) {
                list = new ArrayList<PositionTxnVo>();
            }
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(list);
            
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }
    
    /**
     * 이벤트 조회
     * param: positionId (선택)
     * param: stockCode (선택)
     */
    @RequestMapping("/position/eventList.do")
    public void selectPositionEventList(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);
        
        try {
            List<PositionEventVo> list = positionDao.selectPositionEventList(map);
            if (list == null) {
                list = new ArrayList<PositionEventVo>();
            }
            
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setData(list);
            
            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    private void insertPositionEventSafe(Long positionId, String stockGroup, String stockCode,
            String eventType, String eventLevel, String message, String reasonText) {
        try {
            PositionEventVo ev = new PositionEventVo();
            ev.setPositionId(positionId);
            ev.setStockGroup(stockGroup);
            ev.setStockCode(stockCode);
            ev.setEventType(eventType);
            ev.setEventLevel(eventLevel);
            ev.setEventTime(new Date());
            ev.setMessage(message);
            ev.setJsonParams(reasonText);
            positionDao.insertPositionEvent(ev);
        } catch (Exception ignore) {
            // 이벤트 기록 실패가 메인 트랜잭션을 막지 않도록 안전 처리
        }
    }
}
