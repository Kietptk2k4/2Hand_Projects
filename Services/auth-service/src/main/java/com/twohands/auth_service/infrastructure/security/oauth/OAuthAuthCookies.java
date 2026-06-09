package com.twohands.auth_service.infrastructure.security.oauth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public final class OAuthAuthCookies {

    public static final String ACCESS_TOKEN = "th_access_token";
    public static final String REFRESH_TOKEN = "th_refresh_token";

    private OAuthAuthCookies() {
    }

    public static void clear(HttpServletResponse response, boolean secureCookie) {
        addClearedCookie(response, ACCESS_TOKEN, secureCookie);
        addClearedCookie(response, REFRESH_TOKEN, secureCookie);
    }

    private static void addClearedCookie(HttpServletResponse response, String name, boolean secureCookie) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
