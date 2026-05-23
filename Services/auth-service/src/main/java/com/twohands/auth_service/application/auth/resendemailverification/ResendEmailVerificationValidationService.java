package com.twohands.auth_service.application.auth.resendemailverification;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ResendEmailVerificationValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public String normalizeAndValidateEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Email is required", "email", "REQUIRED");
        }
        String trimmed = rawEmail.trim();
        if (trimmed.length() > 255) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Email max length is 255", "email", "MAX_LENGTH");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Email format is invalid", "email", "INVALID_FORMAT");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
