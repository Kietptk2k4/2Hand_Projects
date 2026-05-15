package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.register.RegisterRateLimitService;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Profile("!test")
public class RedisRegisterRateLimitService implements RegisterRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int limit;
    private final Duration window;

    public RedisRegisterRateLimitService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${auth.register.rate-limit.max-attempts:5}") int limit,
            @Value("${auth.register.rate-limit.window-seconds:3600}") long windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.limit = limit;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public void validateRegisterAttempt(String ipAddress) {
        String key = "auth:ratelimit:register:" + (ipAddress == null ? "unknown" : ipAddress);

        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, window);
        }

        if (value != null && value > limit) {
            throw new AppException(ErrorCode.REGISTER_RATE_LIMITED, ErrorCode.REGISTER_RATE_LIMITED.defaultMessage());
        }
    }
}
