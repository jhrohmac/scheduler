package com.scheduler.finance.controller;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.scheduler.finance.dao.StockCodeInfoDao;

/**
 * 좌측 "관심종목" 패널 Ajax
 * - TB_S_INTEREST_WATCHLIST 기반 목록 조회
 */
@Controller
public class InterestWatchlistController {

    private StockCodeInfoDao stockCodeInfoDao;

    public StockCodeInfoDao getStockCodeInfoDao() {
        return stockCodeInfoDao;
    }

    public void setStockCodeInfoDao(StockCodeInfoDao stockCodeInfoDao) {
        this.stockCodeInfoDao = stockCodeInfoDao;
    }

    @RequestMapping(value = "/finance/ajax/selectInterestWatchlist.do", method = RequestMethod.GET)
    public void selectInterestWatchlist(HttpServletResponse response, String interastGroup, String groupMarket) throws Exception {

        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();

        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            String groupId = (interastGroup == null || interastGroup.trim().isEmpty()) ? "0000" : interastGroup.trim();
            String market = (groupMarket == null || groupMarket.trim().isEmpty()) ? "N" : groupMarket.trim();

            // 기존 SQL이 "KR"/"US" 를 사용하므로 UI 파라미터를 변환
            String stockCountryCode = "US";
            if ("N".equalsIgnoreCase(market) || "KR".equalsIgnoreCase(market)) {
                stockCountryCode = "KR";
            }

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("group_id", groupId);
            map.put("groupMarket", stockCountryCode);

            List<?> list = stockCodeInfoDao.selectGroupStockList(map);

            result.put("result", "ok");
            result.put("list", list);
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("message", e.getMessage());
        }

        out.print(new Gson().toJson(result));
        out.flush();
    }
}
