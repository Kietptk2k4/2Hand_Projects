package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.logout.LogoutRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopLogoutRateLimitService implements LogoutRateLimitService {

    @Override
    public void validateLogoutAttempt(String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }
}
