package com.twohands.auth_service.application.auth.verifyemail;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class VerifyEmailValidationService {

    public String validateAndNormalizeToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Token is required",
                    "token",
                    "REQUIRED"
            );
        }
        String trimmed = token.trim();
        if (trimmed.length() > 512) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Token max length is 512",
                    "token",
                    "MAX_LENGTH"
            );
        }
        return trimmed;
    }
}
