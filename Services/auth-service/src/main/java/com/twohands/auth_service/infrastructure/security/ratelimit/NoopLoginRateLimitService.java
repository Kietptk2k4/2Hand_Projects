package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.login.LoginRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopLoginRateLimitService implements LoginRateLimitService {

    @Override
    public void validateLoginAttempt(String emailNormalized, String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }

    @Override
    public void recordFailedAttempt(String emailNormalized, String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }
}
