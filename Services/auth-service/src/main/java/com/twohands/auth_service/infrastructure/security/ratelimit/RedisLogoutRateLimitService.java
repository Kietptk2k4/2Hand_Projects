package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.logout.LogoutRateLimitService;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Profile("!test")
public class RedisLogoutRateLimitService implements LogoutRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int limit;
    private final Duration window;

    public RedisLogoutRateLimitService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${auth.logout.rate-limit.max-attempts:30}") int limit,
            @Value("${auth.logout.rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.limit = limit;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public void validateLogoutAttempt(String ipAddress) {
        String key = "auth:ratelimit:logout:ip:" + (ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress);
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, window);
        }
        if (value != null && value > limit) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS.defaultMessage());
        }
    }
}
