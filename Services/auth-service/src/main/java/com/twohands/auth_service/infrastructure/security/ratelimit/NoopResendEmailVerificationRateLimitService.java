package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopResendEmailVerificationRateLimitService implements ResendEmailVerificationRateLimitService {

    @Override
    public void validateResendAttempt(String emailNormalized, String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }
}
