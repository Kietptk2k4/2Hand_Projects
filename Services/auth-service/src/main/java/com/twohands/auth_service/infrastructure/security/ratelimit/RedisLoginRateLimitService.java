package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.login.LoginRateLimitService;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Profile("!test")
public class RedisLoginRateLimitService implements LoginRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int maxAttempts;
    private final Duration window;

    public RedisLoginRateLimitService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${auth.login.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${auth.login.rate-limit.window-seconds:900}") long windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public void validateLoginAttempt(String emailNormalized, String ipAddress) {
        Long emailAttempts = redisTemplate.opsForValue().increment(emailKey(emailNormalized), 0L);
        Long ipAttempts = redisTemplate.opsForValue().increment(ipKey(ipAddress), 0L);

        if ((emailAttempts != null && emailAttempts >= maxAttempts)
                || (ipAttempts != null && ipAttempts >= maxAttempts)) {
            throw new AppException(ErrorCode.LOGIN_RATE_LIMITED, ErrorCode.LOGIN_RATE_LIMITED.defaultMessage());
        }
    }

    @Override
    public void recordFailedAttempt(String emailNormalized, String ipAddress) {
        incrementWithExpiry(emailKey(emailNormalized));
        incrementWithExpiry(ipKey(ipAddress));
    }

    private void incrementWithExpiry(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, window);
        }
    }

    private String emailKey(String emailNormalized) {
        String normalized = (emailNormalized == null || emailNormalized.isBlank()) ? "unknown" : emailNormalized;
        return "auth:ratelimit:login:email:" + normalized;
    }

    private String ipKey(String ipAddress) {
        String normalized = (ipAddress == null || ipAddress.isBlank()) ? "unknown" : ipAddress;
        return "auth:ratelimit:login:ip:" + normalized;
    }
}
