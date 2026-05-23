package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.adminrefresh.AdminRefreshAccessTokenCommand;
import com.twohands.auth_service.application.auth.adminrefresh.AdminRefreshAccessTokenResult;
import com.twohands.auth_service.application.auth.adminrefresh.AdminRefreshAccessTokenUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.RefreshAccessTokenRequest;
import com.twohands.auth_service.delivery.http.auth.response.AdminLoginResponse;
import com.twohands.auth_service.delivery.http.auth.response.AdminRefreshAccessTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AdminRefreshTokenController {

    private final AdminRefreshAccessTokenUseCase adminRefreshAccessTokenUseCase;

    public AdminRefreshTokenController(AdminRefreshAccessTokenUseCase adminRefreshAccessTokenUseCase) {
        this.adminRefreshAccessTokenUseCase = adminRefreshAccessTokenUseCase;
    }

    @PostMapping("/admin/token/refresh")
    public ResponseEntity<ApiResponse<AdminRefreshAccessTokenResponse>> refresh(
            @Valid @RequestBody RefreshAccessTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AdminRefreshAccessTokenResult result = adminRefreshAccessTokenUseCase.execute(
                new AdminRefreshAccessTokenCommand(
                        request.refreshToken(),
                        httpServletRequest.getRemoteAddr()
                )
        );

        AdminRefreshAccessTokenResponse response = new AdminRefreshAccessTokenResponse(
                result.accessToken(),
                result.expiresIn(),
                new AdminLoginResponse.UserInfo(
                        result.userId().toString(),
                        result.email(),
                        result.status()
                ),
                result.roles(),
                result.permissions()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        adminRefreshAccessTokenUseCase.refreshSuccessMessage(),
                        response
                ));
    }
}
