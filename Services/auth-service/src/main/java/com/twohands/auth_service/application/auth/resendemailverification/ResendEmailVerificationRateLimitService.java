package com.twohands.auth_service.application.auth.resendemailverification;

public interface ResendEmailVerificationRateLimitService {
    void validateResendAttempt(String emailNormalized, String ipAddress);
}
