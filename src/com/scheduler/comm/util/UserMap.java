package com.scheduler.comm.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpSession;

public class UserMap {

    // thread-safe
    private static final ConcurrentMap<String, HttpSession> userMap = new ConcurrentHashMap<>();

    public static void setUserMap(String userId, HttpSession session) {
        if (userId == null || session == null) return;

        // 기존 세션이 있으면 끊어 단일 로그인 유지(선택 사항)
        HttpSession old = userMap.put(userId, session);
        if (old != null && old != session) {
            try { old.invalidate(); } catch (IllegalStateException ignore) { /* already invalidated */ }
        }
    }

    public static HttpSession getUserMap(String userId) {
        return (userId == null) ? null : userMap.get(userId);
    }

    public static void delUserMap(String userId) {
        if (userId != null) userMap.remove(userId);
    }

    // ★ 리스너에서 사용할 세션 기반 제거
    public static void delUserMapBySession(HttpSession session) {
        if (session == null) return;
        final String sid = session.getId();
        userMap.entrySet().removeIf(e -> {
            HttpSession s = e.getValue();
            return (s == null) || sid.equals(s.getId());
        });
    }
}
