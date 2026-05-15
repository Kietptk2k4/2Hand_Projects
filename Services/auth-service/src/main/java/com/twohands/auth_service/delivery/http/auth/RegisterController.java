package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.register.RegisterUserCommand;
import com.twohands.auth_service.application.auth.register.RegisterUserResult;
import com.twohands.auth_service.application.auth.register.RegisterUserUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.RegisterRequest;
import com.twohands.auth_service.delivery.http.auth.response.RegisterResponse;
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
public class RegisterController {

    private final RegisterUserUseCase registerUserUseCase;

    public RegisterController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        RegisterUserResult result = registerUserUseCase.execute(
                new RegisterUserCommand(
                        request.email(),
                        request.password(),
                        request.confirmPassword(),
                        httpServletRequest.getRemoteAddr()
                )
        );

        RegisterResponse response = new RegisterResponse(result.userId(), result.email(), result.status());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "Dang ky thanh cong. Vui long kiem tra email de xac thuc.",
                        response));
    }
}
