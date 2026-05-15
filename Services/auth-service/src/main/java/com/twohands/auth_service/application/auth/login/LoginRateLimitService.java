package com.twohands.auth_service.application.auth.login;

public interface LoginRateLimitService {
    void validateLoginAttempt(String emailNormalized, String ipAddress);

    void recordFailedAttempt(String emailNormalized, String ipAddress);
}
