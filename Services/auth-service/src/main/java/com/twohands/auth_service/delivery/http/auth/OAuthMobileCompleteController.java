package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionCommand;
import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionResult;
import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionUseCase;
import com.twohands.auth_service.application.auth.oauth.ExchangeOAuthCodeUseCase;
import com.twohands.auth_service.application.auth.oauth.IssueOAuthExchangeCodeUseCase;
import com.twohands.auth_service.application.auth.oauth.OAuthExchangeCodePayload;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.ExchangeOAuthCodeRequest;
import com.twohands.auth_service.delivery.http.auth.response.LoginResponse;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.infrastructure.security.oauth.OAuthAuthCookies;
import com.twohands.auth_service.infrastructure.security.oauth.OAuthHttpSessionCleaner;
import com.twohands.auth_service.infrastructure.security.oauth.OAuthRedirectUriValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth/oauth")
public class OAuthMobileCompleteController {

    private final BootstrapOAuthSessionUseCase bootstrapOAuthSessionUseCase;
    private final IssueOAuthExchangeCodeUseCase issueOAuthExchangeCodeUseCase;
    private final ExchangeOAuthCodeUseCase exchangeOAuthCodeUseCase;
    private final OAuthRedirectUriValidator redirectUriValidator;
    private final boolean secureCookie;

    public OAuthMobileCompleteController(
            BootstrapOAuthSessionUseCase bootstrapOAuthSessionUseCase,
            IssueOAuthExchangeCodeUseCase issueOAuthExchangeCodeUseCase,
            ExchangeOAuthCodeUseCase exchangeOAuthCodeUseCase,
            OAuthRedirectUriValidator redirectUriValidator,
            @Value("${auth.oauth2.cookie.secure:false}") boolean secureCookie
    ) {
        this.bootstrapOAuthSessionUseCase = bootstrapOAuthSessionUseCase;
        this.issueOAuthExchangeCodeUseCase = issueOAuthExchangeCodeUseCase;
        this.exchangeOAuthCodeUseCase = exchangeOAuthCodeUseCase;
        this.redirectUriValidator = redirectUriValidator;
        this.secureCookie = secureCookie;
    }

    @GetMapping("/mobile-complete")
    public void mobileComplete(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "code", required = false) String errorCode,
            @RequestParam(name = "app_return", required = false) String appReturn,
            @RequestParam(name = "first_login", required = false) Boolean firstLogin,
            @CookieValue(name = OAuthAuthCookies.ACCESS_TOKEN, required = false) String accessToken,
            @CookieValue(name = OAuthAuthCookies.REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String appReturnUrl = resolveAppReturn(appReturn);

        if (!"success".equalsIgnoreCase(status)) {
            OAuthAuthCookies.clear(response, secureCookie);
            redirectToApp(
                    request,
                    response,
                    appReturnUrl,
                    "error",
                    errorCode != null ? errorCode : ErrorCode.OAUTH_SESSION_INVALID.code(),
                    firstLogin
            );
            return;
        }

        try {
            BootstrapOAuthSessionResult session = bootstrapOAuthSessionUseCase.execute(
                    new BootstrapOAuthSessionCommand(accessToken, refreshToken)
            );
            OAuthAuthCookies.clear(response, secureCookie);

            String exchangeCode = issueOAuthExchangeCodeUseCase.execute(new OAuthExchangeCodePayload(
                    session.accessToken(),
                    session.refreshToken(),
                    session.expiresIn(),
                    session.userId(),
                    session.email(),
                    session.status()
            ));

            redirectToApp(request, response, appReturnUrl, "success", exchangeCode, firstLogin);
        } catch (AppException ex) {
            OAuthAuthCookies.clear(response, secureCookie);
            redirectToApp(request, response, appReturnUrl, "error", ex.getErrorCode().code(), firstLogin);
        }
    }

    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<LoginResponse>> exchange(
            @Valid @RequestBody ExchangeOAuthCodeRequest request
    ) {
        OAuthExchangeCodePayload payload = exchangeOAuthCodeUseCase.execute(request.code());
        LoginResponse body = new LoginResponse(
                payload.accessToken(),
                payload.refreshToken(),
                payload.expiresIn(),
                new LoginResponse.UserInfo(
                        payload.userId().toString(),
                        payload.email(),
                        payload.status()
                )
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        exchangeOAuthCodeUseCase.successMessage(),
                        body
                ));
    }

    private String resolveAppReturn(String appReturn) {
        if (appReturn == null || appReturn.isBlank()) {
            return redirectUriValidator.defaultAppReturnUrl();
        }
        return appReturn.trim();
    }

    private static void redirectToApp(
            HttpServletRequest request,
            HttpServletResponse response,
            String appReturnUrl,
            String status,
            String code,
            Boolean firstLogin
    ) throws IOException {
        OAuthHttpSessionCleaner.invalidate(request);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(appReturnUrl)
                .queryParam("status", status)
                .queryParam("code", code);
        if (firstLogin != null) {
            builder.queryParam("first_login", firstLogin);
        }
        URI redirect = builder.build(true).toUri();
        response.sendRedirect(redirect.toString());
    }
}
