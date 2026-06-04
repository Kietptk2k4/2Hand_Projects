package com.twohands.auth_service.application.auth.verifyemail;

public interface VerifyEmailRateLimitService {

    void validateAttempt(String ipAddress);
}
