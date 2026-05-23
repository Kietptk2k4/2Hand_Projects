package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationCommand;
import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.ResendEmailVerificationRequest;
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
public class ResendEmailVerificationController {

    private final ResendEmailVerificationUseCase resendEmailVerificationUseCase;

    public ResendEmailVerificationController(ResendEmailVerificationUseCase resendEmailVerificationUseCase) {
        this.resendEmailVerificationUseCase = resendEmailVerificationUseCase;
    }

    @PostMapping("/resend-email-verification")
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification(
            @Valid @RequestBody ResendEmailVerificationRequest request,
            HttpServletRequest httpServletRequest
    ) {
        resendEmailVerificationUseCase.execute(new ResendEmailVerificationCommand(
                request.email(),
                httpServletRequest.getRemoteAddr()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        resendEmailVerificationUseCase.successMessage(),
                        null
                ));
    }
}
