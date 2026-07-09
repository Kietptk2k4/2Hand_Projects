package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionCommand;
import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionResult;
import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.response.LoginResponse;
import com.twohands.auth_service.infrastructure.security.oauth.OAuthAuthCookies;
import com.twohands.auth_service.infrastructure.security.oauth.OAuthHttpSessionCleaner;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/oauth")
public class OAuthSessionController {

    private final BootstrapOAuthSessionUseCase bootstrapOAuthSessionUseCase;
    private final boolean secureCookie;

    public OAuthSessionController(
            BootstrapOAuthSessionUseCase bootstrapOAuthSessionUseCase,
            @Value("${auth.oauth2.cookie.secure:false}") boolean secureCookie
    ) {
        this.bootstrapOAuthSessionUseCase = bootstrapOAuthSessionUseCase;
        this.secureCookie = secureCookie;
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<LoginResponse>> bootstrapSession(
            @CookieValue(name = OAuthAuthCookies.ACCESS_TOKEN, required = false) String accessToken,
            @CookieValue(name = OAuthAuthCookies.REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        BootstrapOAuthSessionResult result = bootstrapOAuthSessionUseCase.execute(
                new BootstrapOAuthSessionCommand(accessToken, refreshToken)
        );

        OAuthAuthCookies.clear(response, secureCookie);
        OAuthHttpSessionCleaner.invalidate(request);

        LoginResponse body = new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn(),
                new LoginResponse.UserInfo(
                        result.userId().toString(),
                        result.email(),
                        result.status()
                )
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        bootstrapOAuthSessionUseCase.successMessage(),
                        body
                ));
    }
}
