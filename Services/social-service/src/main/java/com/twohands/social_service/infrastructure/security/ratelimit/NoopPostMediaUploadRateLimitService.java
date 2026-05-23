package com.twohands.social_service.infrastructure.security.ratelimit;

import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadRateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("test")
public class NoopPostMediaUploadRateLimitService implements PostMediaUploadRateLimitService {

    @Override
    public void validateUploadUrlRequest(UUID userId) {
        // no-op in tests
    }
}
