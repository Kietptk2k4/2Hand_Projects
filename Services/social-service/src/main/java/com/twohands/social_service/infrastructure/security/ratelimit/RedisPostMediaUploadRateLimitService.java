package com.twohands.social_service.infrastructure.security.ratelimit;

import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadRateLimitService;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@Profile("!test")
public class RedisPostMediaUploadRateLimitService implements PostMediaUploadRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int maxAttempts;
    private final Duration window;

    public RedisPostMediaUploadRateLimitService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${social.object-storage.post-media-upload.rate-limit.max-attempts:30}") int maxAttempts,
            @Value("${social.object-storage.post-media-upload.rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public void validateUploadUrlRequest(UUID userId) {
        String key = "social:ratelimit:post-media-upload:user:" + (userId != null ? userId : "unknown");
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, window);
        }
        if (value != null && value > maxAttempts) {
            throw new AppException(
                    ErrorCode.POST_MEDIA_UPLOAD_RATE_LIMITED,
                    ErrorCode.POST_MEDIA_UPLOAD_RATE_LIMITED.defaultMessage()
            );
        }
    }
}
