package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.refresh.RefreshRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopRefreshRateLimitService implements RefreshRateLimitService {

    @Override
    public void validateRefreshAttempt(String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }
}
