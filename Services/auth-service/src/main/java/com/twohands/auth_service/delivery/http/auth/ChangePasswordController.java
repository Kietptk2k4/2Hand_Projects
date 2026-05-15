package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.changepassword.ChangePasswordCommand;
import com.twohands.auth_service.application.auth.changepassword.ChangePasswordUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.ChangePasswordRequest;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class ChangePasswordController {

    private final ChangePasswordUseCase changePasswordUseCase;

    public ChangePasswordController(ChangePasswordUseCase changePasswordUseCase) {
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        UUID userId = extractUserId(authentication);

        changePasswordUseCase.execute(new ChangePasswordCommand(
                userId,
                request.currentPassword(),
                request.newPassword(),
                request.confirmNewPassword()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        changePasswordUseCase.successMessage(),
                        null
                ));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }

        try {
            return UUID.fromString(authentication.getPrincipal().toString());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
    }
}
