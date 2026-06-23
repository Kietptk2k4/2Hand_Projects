package com.twohands.auth_service.infrastructure.security.oauth;

import com.twohands.auth_service.application.auth.oauth.OAuthLoginCommand;
import com.twohands.auth_service.application.auth.oauth.OAuthLoginResult;
import com.twohands.auth_service.application.auth.oauth.OAuthLoginUseCase;
import com.twohands.auth_service.application.auth.oauth.OAuthProfile;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2ProfileExtractor profileExtractor;
    private final OAuthLoginUseCase oAuthLoginUseCase;
    private final OAuthRedirectUriResolver redirectUriResolver;
    private final String successRedirectUrl;
    private final String failureRedirectUrl;
    private final boolean secureCookie;

    public OAuth2LoginSuccessHandler(
            OAuth2ProfileExtractor profileExtractor,
            OAuthLoginUseCase oAuthLoginUseCase,
            OAuthRedirectUriResolver redirectUriResolver,
            @Value("${auth.oauth2.redirect.success-url:http://localhost:5173/oauth/success}") String successRedirectUrl,
            @Value("${auth.oauth2.redirect.failure-url:http://localhost:5173/oauth/failure}") String failureRedirectUrl,
            @Value("${auth.oauth2.cookie.secure:true}") boolean secureCookie
    ) {
        this.profileExtractor = profileExtractor;
        this.oAuthLoginUseCase = oAuthLoginUseCase;
        this.redirectUriResolver = redirectUriResolver;
        this.successRedirectUrl = successRedirectUrl;
        this.failureRedirectUrl = failureRedirectUrl;
        this.secureCookie = secureCookie;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = token.getPrincipal();
            OAuthProfile profile = profileExtractor.extract(token.getAuthorizedClientRegistrationId(), oauth2User);

            OAuthLoginResult result = oAuthLoginUseCase.execute(new OAuthLoginCommand(
                    profile,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    request.getHeader("X-Device-Id")
            ));

            addAuthCookies(response, result);
            String targetRedirectUrl = redirectUriResolver.resolve(request, successRedirectUrl);
            URI redirect = UriComponentsBuilder.fromUriString(targetRedirectUrl)
                    .queryParam("status", "success")
                    .queryParam("first_login", result.firstLogin())
                    .build(true)
                    .toUri();
            response.sendRedirect(redirect.toString());
        } catch (AppException ex) {
            String targetRedirectUrl = redirectUriResolver.resolve(request, failureRedirectUrl);
            URI redirect = UriComponentsBuilder.fromUriString(targetRedirectUrl)
                    .queryParam("status", "error")
                    .queryParam("code", ex.getErrorCode().code())
                    .build(true)
                    .toUri();
            response.sendRedirect(redirect.toString());
        } catch (Exception ex) {
            String targetRedirectUrl = redirectUriResolver.resolve(request, failureRedirectUrl);
            URI redirect = UriComponentsBuilder.fromUriString(targetRedirectUrl)
                    .queryParam("status", "error")
                    .queryParam("code", ErrorCode.INTERNAL_ERROR.code())
                    .build(true)
                    .toUri();
            response.sendRedirect(redirect.toString());
        }
    }

    private void addAuthCookies(HttpServletResponse response, OAuthLoginResult result) {
        addCookie(response, OAuthAuthCookies.ACCESS_TOKEN, result.accessToken(), (int) result.expiresIn());
        addCookie(response, OAuthAuthCookies.REFRESH_TOKEN, result.refreshToken(), 60 * 60 * 24 * 30);
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
