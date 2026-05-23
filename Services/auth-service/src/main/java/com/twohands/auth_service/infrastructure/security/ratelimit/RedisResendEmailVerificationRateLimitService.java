package com.twohands.auth_service.infrastructure.security.ratelimit;

import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationRateLimitService;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Profile("!test")
public class RedisResendEmailVerificationRateLimitService implements ResendEmailVerificationRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final int maxAttempts;
    private final Duration window;

    public RedisResendEmailVerificationRateLimitService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${auth.resend-email-verification.rate-limit.max-attempts:3}") int maxAttempts,
            @Value("${auth.resend-email-verification.rate-limit.window-seconds:900}") long windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public void validateResendAttempt(String emailNormalized, String ipAddress) {
        incrementAndValidate(emailKey(emailNormalized));
        incrementAndValidate(ipKey(ipAddress));
    }

    private void incrementAndValidate(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, window);
        }
        if (value != null && value > maxAttempts) {
            throw new AppException(
                    ErrorCode.RESEND_EMAIL_VERIFICATION_RATE_LIMITED,
                    ErrorCode.RESEND_EMAIL_VERIFICATION_RATE_LIMITED.defaultMessage()
            );
        }
    }

    private String emailKey(String emailNormalized) {
        String normalized = (emailNormalized == null || emailNormalized.isBlank()) ? "unknown" : emailNormalized;
        return "auth:ratelimit:resend-email-verification:email:" + normalized;
    }

    private String ipKey(String ipAddress) {
        String normalized = (ipAddress == null || ipAddress.isBlank()) ? "unknown" : ipAddress;
        return "auth:ratelimit:resend-email-verification:ip:" + normalized;
    }
}
