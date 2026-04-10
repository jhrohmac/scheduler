package com.scheduler.comm.system;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 로그인 시 DB(TB_M_SYSTEM_CONFIG)의 값을 읽어 세션 유휴시간을 설정한다.
 *
 * - web.xml 에 /login/login.do 로 매핑한다.
 * - SYSTEM_ID 기본값: "SESSION_TIMEOUT_MINUTES" (init-param 으로 변경 가능)
 * - 조회 실패/미설정 시, context-param(SESSION_TIMEOUT_MINUTES) 또는 60분으로 대체
 * - 같은 세션에서 1회만 적용(SESSION_TIMEOUT_APPLIED 플래그)
 */
public class LoginSessionTimeoutFilter implements Filter {

    private static final Logger logger = Logger.getLogger(LoginSessionTimeoutFilter.class);

    private static final String KEY_SESSION_APPLIED = "SESSION_TIMEOUT_APPLIED";
    private static final String KEY_SESSION_MINUTES = "SESSION_TIMEOUT_MINUTES";
    private static final String CTX_PARAM_MINUTES   = "SESSION_TIMEOUT_MINUTES";

    private String systemId = "SESSION_TIMEOUT_MINUTES";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String id = filterConfig.getInitParameter("systemId");
        if (id != null && !id.trim().isEmpty()) {
            systemId = id.trim();
        }
        logger.info("[LoginSessionTimeoutFilter] init - systemId=" + systemId);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 컨트롤러가 먼저 로그인 처리하도록 위임
        chain.doFilter(request, response);

        if (!(request instanceof HttpServletRequest)) return;
        HttpServletRequest req = (HttpServletRequest) request;

        // 로그인 엔드포인트에서만 동작 (/login/login.do)
        String uri = req.getRequestURI();
        if (uri == null || !uri.endsWith("/login/login.do")) return;

        HttpSession session = req.getSession(false);
        if (session == null) return;

        // 이미 적용되었으면 스킵
        Object applied = session.getAttribute(KEY_SESSION_APPLIED);
        if (applied instanceof Boolean && (Boolean) applied) return;

        int minutes = readMinutesFromDb(req.getServletContext());
        if (minutes <= 0) {
            minutes = readMinutesFromContextParam(req.getServletContext(), 60);
        }

        // 가드(1분 ~ 24시간)
        if (minutes < 1) minutes = 1;
        if (minutes > 1440) minutes = 1440;

        try {
            session.setMaxInactiveInterval(minutes * 60);
            session.setAttribute(KEY_SESSION_MINUTES, minutes);
            session.setAttribute(KEY_SESSION_APPLIED, Boolean.TRUE);
            if (logger.isInfoEnabled()) {
                logger.info("[LoginSessionTimeoutFilter] applied: " + minutes + "m, sessionId=" + session.getId());
            }
        } catch (IllegalStateException ise) {
            logger.warn("[LoginSessionTimeoutFilter] session already invalidated.", ise);
        }
    }

    @Override
    public void destroy() {
        // no-op
    }

    private int readMinutesFromDb(ServletContext sc) {
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
        DataSource ds;
        try {
            ds = wac.getBean("dataSource", DataSource.class);
        } catch (Exception e) {
            logger.warn("[LoginSessionTimeoutFilter] dataSource bean not found.", e);
            return -1;
        }

        String sql =
            "SELECT SYSTEM_VALUE " +
            "  FROM TB_M_SYSTEM_CONFIG " +
            " WHERE USE_FLAG = 'Y' " +
            "   AND SYSTEM_ID = ?";

        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, systemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString(1);
                    if (val != null) {
                        val = val.trim();
                        if (!val.isEmpty()) {
                            return Integer.parseInt(val);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("[LoginSessionTimeoutFilter] Failed to read from DB. Falling back. systemId=" + systemId, e);
        }
        return -1;
    }

    private int readMinutesFromContextParam(ServletContext sc, int def) {
        String v = sc.getInitParameter(CTX_PARAM_MINUTES);
        if (v == null || v.trim().isEmpty()) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException nfe) {
            return def;
        }
    }
}
