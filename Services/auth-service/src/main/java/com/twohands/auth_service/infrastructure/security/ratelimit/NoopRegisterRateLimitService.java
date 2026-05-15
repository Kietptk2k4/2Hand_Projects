package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.register.RegisterRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopRegisterRateLimitService implements RegisterRateLimitService {
    @Override
    public void validateRegisterAttempt(String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }
}
