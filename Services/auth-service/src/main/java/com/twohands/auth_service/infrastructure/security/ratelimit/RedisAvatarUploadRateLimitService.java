package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadRateLimitService;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@Profile("!test")
public class RedisAvatarUploadRateLimitService implements AvatarUploadRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int maxAttempts;
    private final Duration window;

    public RedisAvatarUploadRateLimitService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${auth.object-storage.avatar-upload.rate-limit.max-attempts:30}") int maxAttempts,
            @Value("${auth.object-storage.avatar-upload.rate-limit.window-seconds:3600}") long windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public void validateUploadUrlRequest(UUID userId) {
        incrementAndValidate(userKey(userId));
    }

    private void incrementAndValidate(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, window);
        }
        if (value != null && value > maxAttempts) {
            throw new AppException(
                    ErrorCode.AVATAR_UPLOAD_RATE_LIMITED,
                    ErrorCode.AVATAR_UPLOAD_RATE_LIMITED.defaultMessage()
            );
        }
    }

    private String userKey(UUID userId) {
        return "auth:ratelimit:avatar-upload:user:" + (userId != null ? userId : "unknown");
    }
}
