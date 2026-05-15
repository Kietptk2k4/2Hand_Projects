package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordCommand;
import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.ForgotPasswordRequest;
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
public class ForgotPasswordController {

    private final ForgotPasswordUseCase forgotPasswordUseCase;

    public ForgotPasswordController(ForgotPasswordUseCase forgotPasswordUseCase) {
        this.forgotPasswordUseCase = forgotPasswordUseCase;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpServletRequest
    ) {
        forgotPasswordUseCase.execute(new ForgotPasswordCommand(
                request.email(),
                httpServletRequest.getRemoteAddr()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        forgotPasswordUseCase.successMessage(),
                        null
                ));
    }
}
