package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopVerifyEmailRateLimitService implements VerifyEmailRateLimitService {

    @Override
    public void validateAttempt(String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }
}
