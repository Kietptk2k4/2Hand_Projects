package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopForgotPasswordRateLimitService implements ForgotPasswordRateLimitService {

    @Override
    public void validateForgotPasswordAttempt(String emailNormalized, String ipAddress) {
        // No-op for test profile to avoid Redis dependency.
    }
}
