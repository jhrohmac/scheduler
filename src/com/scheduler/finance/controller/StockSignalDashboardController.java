package com.scheduler.finance.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StockSignalDashboardController {

    @RequestMapping({ "/finance/stockSignal/dashboard.do" })
    public ModelAndView dashboard(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("finance/stockSignal/dashboard");
        return mv;
    }
}
