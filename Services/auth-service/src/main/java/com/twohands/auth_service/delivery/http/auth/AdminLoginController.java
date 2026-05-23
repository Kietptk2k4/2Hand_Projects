package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.adminlogin.AdminLoginCommand;
import com.twohands.auth_service.application.auth.adminlogin.AdminLoginResult;
import com.twohands.auth_service.application.auth.adminlogin.AdminLoginUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.LoginRequest;
import com.twohands.auth_service.delivery.http.auth.response.AdminLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AdminLoginController {

    private final AdminLoginUseCase adminLoginUseCase;

    public AdminLoginController(AdminLoginUseCase adminLoginUseCase) {
        this.adminLoginUseCase = adminLoginUseCase;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(name = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest httpServletRequest
    ) {
        AdminLoginResult result = adminLoginUseCase.execute(new AdminLoginCommand(
                request.email(),
                request.password(),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent"),
                deviceId
        ));

        AdminLoginResponse response = new AdminLoginResponse(
                result.accessToken(),
                result.refreshToken(),
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
                        adminLoginUseCase.loginSuccessMessage(),
                        response
                ));
    }
}
