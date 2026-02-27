package com.scheduler.comm.system;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * 모든 요청 응답 뒤(post) 단계에서 세션의 MaxInactiveInterval을
 * 운영 파라미터(SESSION_TIMEOUT_MINUTES)로 강제 일치시킨다.
 *
 * - 컨트롤러 내부에서 개별적으로 setMaxInactiveInterval을 바꿔도,
 *   최종적으로 여기에서 60분으로 맞춰줌.
 * - 운영 중에는 SystemSettingController에서 ServletContext Attribute로
 *   "SESSION_TIMEOUT_MINUTES" 값을 바꾸면 즉시 반영됨.
 */
public class SessionTimeoutFilter implements Filter {

    private static final Logger logger = Logger.getLogger(SessionTimeoutFilter.class);

    private static final String KEY = "SESSION_TIMEOUT_MINUTES";
    private int defaultMinutes = 60; // web.xml <init-param> 또는 기본값

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String init = filterConfig.getInitParameter("defaultMinutes");
        if (init != null) {
            try {
                int v = Integer.parseInt(init.trim());
                if (v > 0) defaultMinutes = v;
            } catch (NumberFormatException ignore) {
                // 무시하고 기본값 사용
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("SessionTimeoutFilter initialized. defaultMinutes=" + defaultMinutes);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        // 컨트롤러/뷰 처리 먼저 수행
        chain.doFilter(request, response);

        try {
            if (!(request instanceof HttpServletRequest)) return;

            HttpServletRequest req = (HttpServletRequest) request;
            HttpSession session = req.getSession(false); // 존재하는 세션만
            if (session == null) return;

            ServletContext sc = session.getServletContext();
            int minutes = resolveMinutes(sc);

            // 방어: 유효 범위 (1분 ~ 24시간)
            if (minutes < 1) minutes = 1;
            if (minutes > 24 * 60) minutes = 24 * 60;

            session.setMaxInactiveInterval(minutes * 60);

            if (logger.isDebugEnabled()) {
                logger.debug("Session timeout enforced: " + minutes + "m, sessionId=" + session.getId());
            }
        } catch (Exception e) {
            // 세션이 이미 무효화된 경우 등은 조용히 로깅만
            logger.warn("Failed to enforce session timeout.", e);
        }
    }

    @Override
    public void destroy() {
        // no-op
    }

    private int resolveMinutes(ServletContext sc) {
        // 1) 런타임 Attribute 우선 (SystemSettingController가 변경)
        Object attr = sc.getAttribute(KEY);
        if (attr instanceof Integer) {
            return (Integer) attr;
        }
        if (attr instanceof String) {
            try {
                return Integer.parseInt(((String) attr).trim());
            } catch (NumberFormatException ignore) { }
        }

        // 2) 초기값 context-param
        String initParam = sc.getInitParameter(KEY);
        if (initParam != null) {
            try {
                return Integer.parseInt(initParam.trim());
            } catch (NumberFormatException ignore) { }
        }

        // 3) 필터 기본값
        return defaultMinutes;
    }
}
