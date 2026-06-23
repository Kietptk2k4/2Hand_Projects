package com.twohands.auth_service.infrastructure.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class OAuthRedirectUriResolver {

    private final OAuthRedirectUriValidator redirectUriValidator;

    public OAuthRedirectUriResolver(OAuthRedirectUriValidator redirectUriValidator) {
        this.redirectUriValidator = redirectUriValidator;
    }

    public String resolve(HttpServletRequest request, String fallbackRedirectUrl) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return fallbackRedirectUrl;
        }

        Object stored = session.getAttribute(OAuthRedirectSessionKeys.CLIENT_REDIRECT_URI);
        session.removeAttribute(OAuthRedirectSessionKeys.CLIENT_REDIRECT_URI);
        if (!(stored instanceof String redirectUri) || redirectUri.isBlank()) {
            return fallbackRedirectUrl;
        }

        if (!redirectUriValidator.isAllowed(redirectUri, request)) {
            return fallbackRedirectUrl;
        }

        return redirectUri;
    }
}
