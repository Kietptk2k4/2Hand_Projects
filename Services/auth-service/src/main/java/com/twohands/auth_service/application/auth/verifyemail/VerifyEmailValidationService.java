package com.twohands.auth_service.application.auth.verifyemail;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class VerifyEmailValidationService {

    private static final Pattern OTP_PATTERN = Pattern.compile("^\\d{6}$");

    public String validateAndNormalizeToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Ma OTP la bat buoc",
                    "token",
                    "REQUIRED"
            );
        }
        String trimmed = token.trim();
        if (!OTP_PATTERN.matcher(trimmed).matches()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Ma OTP phai gom dung 6 chu so",
                    "token",
                    "INVALID_FORMAT"
            );
        }
        return trimmed;
    }
}
