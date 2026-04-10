package com.scheduler.finance.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scheduler.comm.util.ResultMsg;
import com.scheduler.comm.vo.DataTableSettingVo;
import com.scheduler.finance.dao.MarketSummaryDao;
import com.scheduler.util.handler.RequestHandler;
import com.scheduler.util.handler.ResponseHandler;

@Controller
public class MarketSummaryController {

    private MarketSummaryDao marketSummaryDao;

    public MarketSummaryDao getMarketSummaryDao() {
        return marketSummaryDao;
    }

    public void setMarketSummaryDao(MarketSummaryDao marketSummaryDao) {
        this.marketSummaryDao = marketSummaryDao;
    }

    /**
     * 좌측 관심그룹 조회 (TB_S_INTEREST_GROUP)
     * param: market (N=국내, A=해외) - 선택
     */
    @RequestMapping({ "/finance/selectWatchlistGroups.do" })
    public void selectWatchlistGroups(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<?> list = marketSummaryDao.selectWatchlistGroups(map);
            if (list == null) {
                list = new ArrayList<Object>();
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
     * 좌측 관심종목 조회 (TB_S_INTEREST_WATCHLIST)
     * param: market (N=국내, A=해외) - 선택
     * param: groupId (TB_S_INTEREST_GROUP.GROUP_ID)
     */
    @RequestMapping({ "/finance/selectWatchlistItems.do" })
    public void selectWatchlistItems(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<?> list = marketSummaryDao.selectWatchlistItems(map);
            if (list == null) {
                list = new ArrayList<Object>();
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
     * 관심(☆) 버튼 클릭 시 관심그룹에 종목 추가
     * param: groupId (TB_S_INTEREST_GROUP.GROUP_ID) - 필수
     * param: stockCode (TB_S_INTEREST_STOCK.STOCK_CODE) - 필수
     * param: stockClose (옵션, 화면 표시 가격)
     */
    @RequestMapping({ "/finance/insertWatchlistItem.do" })
    public void insertWatchlistItem(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            int affected = marketSummaryDao.insertWatchlistItem(map);

            Map<String, Object> out = new HashMap<String, Object>();
            out.put("affected", affected);
            out.put("groupId", map.get("groupId"));
            out.put("stockCode", map.get("stockCode"));
            out.put("favYn", "Y");

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(out);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /**
     * 관심(★) 토글: 이미 관심이면 삭제, 아니면 추가
     * param: groupId (TB_S_INTEREST_GROUP.GROUP_ID) - 필수
     * param: stockCode (TB_S_INTEREST_STOCK.STOCK_CODE) - 필수
     * param: stockClose (옵션, 화면 표시 가격)
     *
     * return: singleData { action: ADD/REMOVE, favYn: Y/N, affected: n }
     */
    @RequestMapping({ "/finance/toggleWatchlistItem.do" })
    public void toggleWatchlistItem(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            int cnt = marketSummaryDao.selectWatchlistItemCount(map);

            String action;
            String favYn;
            int affected;

            if (cnt > 0) {
                affected = marketSummaryDao.deleteWatchlistItem(map);
                action = "REMOVE";
                favYn = "N";
            } else {
                affected = marketSummaryDao.insertWatchlistItem(map);
                action = "ADD";
                favYn = "Y";
            }

            Map<String, Object> out = new HashMap<String, Object>();
            out.put("action", action);
            out.put("favYn", favYn);
            out.put("affected", affected);
            out.put("groupId", map.get("groupId"));
            out.put("stockCode", map.get("stockCode"));

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(out);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /**
     * 페이지 로드시(또는 종목 변경 시) 관심 여부 확인
     * param: groupId (TB_S_INTEREST_GROUP.GROUP_ID) - 필수
     * param: stockCode (TB_S_INTEREST_STOCK.STOCK_CODE) - 필수
     *
     * return: singleData { favYn: Y/N, count: n, groupId, stockCode }
     */
    @RequestMapping({ "/finance/selectWatchlistItemYn.do" })
    public void selectWatchlistItemYn(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            int cnt = marketSummaryDao.selectWatchlistItemCount(map);
            String favYn = (cnt > 0) ? "Y" : "N";

            Map<String, Object> out = new HashMap<String, Object>();
            out.put("favYn", favYn);
            out.put("count", cnt);
            out.put("groupId", map.get("groupId"));
            out.put("stockCode", map.get("stockCode"));

            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(out);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

    /**
     * 좌측 관심종목 조회 (KIS 실시간 반영)
     * - 국내: /uapi/domestic-stock/v1/quotations/inquire-price
     * - 해외: /uapi/overseas-price/v1/quotations/price
     * - 국내지수(KOSPI/KOSDAQ 등): /uapi/domestic-stock/v1/quotations/inquire-index-price
     *
     * param: market (N=국내, A=해외) - 선택
     * param: groupId (TB_S_INTEREST_GROUP.GROUP_ID)
     */
    @RequestMapping({ "/finance/selectWatchlistItemsRealtime.do" })
    public void selectWatchlistItemsRealtime(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            List<?> list = marketSummaryDao.selectWatchlistItemsRealtime(map);
            if (list == null) {
                list = new ArrayList<Object>();
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
     * 우측 시장요약 조회 (KIS: 국내지수/해외지수/환율)
     * return: singleData(Map)
     */
    @RequestMapping({ "/finance/selectMarketSummary.do" })
    public void selectMarketSummary(HttpServletRequest req, HttpServletResponse res) {
        HashMap<String, String> map = RequestHandler.extractParameters(req);

        try {
            Map<String, Object> summary = marketSummaryDao.selectMarketSummary(map);
            System.out.println("summary=============" + summary.size());
            DataTableSettingVo resultVo = new DataTableSettingVo();
            resultVo.setSingleData(summary);

            ResponseHandler.sendResponse(res, ResultMsg.SUCCESS_CODE, ResultMsg.SUCCESS_MSG, resultVo);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendResponse(res, ResultMsg.ERROR_CODE, e.getLocalizedMessage(), null);
        }
    }

}
