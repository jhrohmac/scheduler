// src/com/scheduler/comm/system/SessionCleanupListener.java
package com.scheduler.comm.system;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.log4j.Logger;
import com.scheduler.comm.util.UserMap;

public class SessionCleanupListener implements HttpSessionListener {
    private static final Logger logger = Logger.getLogger(SessionCleanupListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext sc = se.getSession().getServletContext();

        // 1) 런타임 변경값(Attribute) 우선
        Object attr = sc.getAttribute("SESSION_TIMEOUT_MINUTES");
        // 2) 초기값(context-param) 보조
        String initParam = sc.getInitParameter("SESSION_TIMEOUT_MINUTES");

        int minutes = 30; // 최종 fallback
        try {
            if (attr instanceof Integer) {
                minutes = (Integer) attr;
            } else if (attr instanceof String && !((String) attr).isEmpty()) {
                minutes = Integer.parseInt((String) attr);
            } else if (initParam != null && !initParam.isEmpty()) {
                minutes = Integer.parseInt(initParam);
            }
        } catch (NumberFormatException ignore) { /* fallback 그대로 사용 */ }

        se.getSession().setMaxInactiveInterval(minutes * 60);

        if (logger.isDebugEnabled()) {
            logger.debug("sessionCreated: " + se.getSession().getId()
                + " timeout=" + minutes + "m");
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // (기존 정리 로직 유지)
        UserMap.delUserMapBySession(se.getSession());
        if (logger.isDebugEnabled()) {
            logger.debug("sessionDestroyed: " + se.getSession().getId());
        }
    }
}
