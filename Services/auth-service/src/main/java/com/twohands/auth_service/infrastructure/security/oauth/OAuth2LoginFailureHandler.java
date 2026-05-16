package com.twohands.auth_service.infrastructure.security.oauth;

import com.twohands.auth_service.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final String failureRedirectUrl;

    public OAuth2LoginFailureHandler(
            @Value("${auth.oauth2.redirect.failure-url:http://localhost:5173/oauth/failure}") String failureRedirectUrl
    ) {
        this.failureRedirectUrl = failureRedirectUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        URI redirect = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                .queryParam("status", "error")
                .queryParam("code", ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID.code())
                .build(true)
                .toUri();
        response.sendRedirect(redirect.toString());
    }
}
