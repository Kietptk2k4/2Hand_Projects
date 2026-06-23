package com.twohands.auth_service.infrastructure.security.oauth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class OAuthRedirectUriCaptureFilter extends OncePerRequestFilter {

    private final OAuthRedirectUriValidator redirectUriValidator;
    private final String defaultFailureRedirectUrl;

    public OAuthRedirectUriCaptureFilter(
            OAuthRedirectUriValidator redirectUriValidator,
            @Value("${auth.oauth2.redirect.failure-url:http://localhost:5173/oauth/failure}") String defaultFailureRedirectUrl
    ) {
        this.redirectUriValidator = redirectUriValidator;
        this.defaultFailureRedirectUrl = defaultFailureRedirectUrl;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/oauth2/authorization/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri == null || redirectUri.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!redirectUriValidator.isAllowed(redirectUri, request)) {
            URI redirect = UriComponentsBuilder.fromUriString(defaultFailureRedirectUrl)
                    .queryParam("status", "error")
                    .queryParam("code", "AUTH-400")
                    .build(true)
                    .toUri();
            response.sendRedirect(redirect.toString());
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(OAuthRedirectSessionKeys.CLIENT_REDIRECT_URI, redirectUri.trim());
        filterChain.doFilter(request, response);
    }
}
