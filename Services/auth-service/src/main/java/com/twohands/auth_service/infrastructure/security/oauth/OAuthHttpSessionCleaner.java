package com.twohands.auth_service.infrastructure.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Drops Spring Security OAuth2 state from {@code JSESSIONID} after the web/mobile OAuth
 * bootstrap cookies are issued, so later Bearer JWT API calls are not shadowed by
 * {@code OAuth2AuthenticationToken} in the HTTP session.
 */
public final class OAuthHttpSessionCleaner {

    private OAuthHttpSessionCleaner() {
    }

    public static void invalidate(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
