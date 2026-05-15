package com.twohands.auth_service.application.auth.register;

import com.twohands.auth_service.domain.user.UserDomainError;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public final class RegisterValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,32}$");

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

    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Password is required", "password", "REQUIRED");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Mat khau phai tu 8-32 ky tu, bao gom chu hoa, chu thuong va so",
                    "password",
                    "WEAK"
            );
        }
    }

    public void validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Confirm password is required", "confirm_password", "REQUIRED");
        }
        if (!confirmPassword.equals(password)) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Confirm password does not match password",
                    "confirm_password",
                    "MISMATCH"
            );
        }
    }

    public RuntimeException mapUserDomainError(UserDomainError error) {
        return new AppException(ErrorCode.VALIDATION_ERROR, error.getMessage());
    }
}
