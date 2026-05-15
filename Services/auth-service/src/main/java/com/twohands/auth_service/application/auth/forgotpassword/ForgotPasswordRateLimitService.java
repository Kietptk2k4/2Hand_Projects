package com.twohands.auth_service.application.auth.forgotpassword;

public interface ForgotPasswordRateLimitService {
    void validateForgotPasswordAttempt(String emailNormalized, String ipAddress);
}
