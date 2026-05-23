package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("test")
public class NoopAvatarUploadRateLimitService implements AvatarUploadRateLimitService {

    @Override
    public void validateUploadUrlRequest(UUID userId) {
        // No-op for test profile to avoid Redis dependency.
    }
}
