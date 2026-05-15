package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.login.LoginUserCommand;
import com.twohands.auth_service.application.auth.login.LoginUserResult;
import com.twohands.auth_service.application.auth.login.LoginUserUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.LoginRequest;
import com.twohands.auth_service.delivery.http.auth.response.LoginResponse;
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
public class LoginController {

    private final LoginUserUseCase loginUserUseCase;

    public LoginController(LoginUserUseCase loginUserUseCase) {
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(name = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest httpServletRequest
    ) {
        LoginUserResult result = loginUserUseCase.execute(new LoginUserCommand(
                request.email(),
                request.password(),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent"),
                deviceId
        ));

        LoginResponse response = new LoginResponse(
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
                        loginUserUseCase.loginSuccessMessage(),
                        response
                ));
    }
}
