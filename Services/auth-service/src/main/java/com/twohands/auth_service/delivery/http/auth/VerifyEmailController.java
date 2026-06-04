package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailCommand;
import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailResult;
import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.VerifyEmailRequest;
import com.twohands.auth_service.delivery.http.auth.response.VerifyEmailResponse;
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
public class VerifyEmailController {

    private final VerifyEmailUseCase verifyEmailUseCase;

    public VerifyEmailController(VerifyEmailUseCase verifyEmailUseCase) {
        this.verifyEmailUseCase = verifyEmailUseCase;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request,
            HttpServletRequest httpServletRequest
    ) {
        VerifyEmailResult result = verifyEmailUseCase.execute(new VerifyEmailCommand(
                request.token(),
                httpServletRequest.getRemoteAddr()
        ));

        VerifyEmailResponse response = new VerifyEmailResponse(
                result.userId(),
                result.emailVerified(),
                result.status()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), result.message(), response));
    }
}
