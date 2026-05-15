package com.twohands.auth_service.application.auth.register;

public interface RegisterRateLimitService {
    void validateRegisterAttempt(String ipAddress);
}
