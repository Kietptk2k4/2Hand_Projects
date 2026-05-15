package com.twohands.auth_service.application.auth.logout;

public interface LogoutRateLimitService {
    void validateLogoutAttempt(String ipAddress);
}
