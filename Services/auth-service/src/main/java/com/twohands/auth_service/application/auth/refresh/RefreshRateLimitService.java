package com.twohands.auth_service.application.auth.refresh;

public interface RefreshRateLimitService {
    void validateRefreshAttempt(String ipAddress);
}
