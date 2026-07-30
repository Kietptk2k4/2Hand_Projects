package com.twohands.auth_service.infrastructure.security.session;

import com.twohands.auth_service.domain.session.AccessTokenInvalidationStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@Profile("!test")
public class RedisAccessTokenInvalidationStore implements AccessTokenInvalidationStore {

    public static final String KEY_PREFIX = "auth:token:invalid-before:";

    private final StringRedisTemplate stringRedisTemplate;
    private final Duration keyTtl;

    public RedisAccessTokenInvalidationStore(
            StringRedisTemplate stringRedisTemplate,
            @Value("${jwt.access-expiration:900000}") long accessExpirationMillis
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.keyTtl = Duration.ofMillis(accessExpirationMillis).plusMinutes(1);
    }

    @Override
    public void invalidateTokensIssuedBefore(UUID userId, Instant invalidBefore) {
        if (userId == null || invalidBefore == null) {
            return;
        }

        String key = key(userId);
        long invalidBeforeMs = invalidBefore.toEpochMilli();
        String currentValue = stringRedisTemplate.opsForValue().get(key);
        if (currentValue != null) {
            try {
                long existingMs = Long.parseLong(currentValue);
                if (existingMs >= invalidBeforeMs) {
                    return;
                }
            } catch (NumberFormatException ignored) {
                // Overwrite malformed value.
            }
        }

        stringRedisTemplate.opsForValue().set(key, String.valueOf(invalidBeforeMs), keyTtl);
    }

    @Override
    public boolean isTokenInvalidated(UUID userId, long issuedAtEpochMilli) {
        if (userId == null) {
            return false;
        }

        String raw = stringRedisTemplate.opsForValue().get(key(userId));
        if (raw == null || raw.isBlank()) {
            return false;
        }

        try {
            long invalidBeforeMs = Long.parseLong(raw.trim());
            return issuedAtEpochMilli < invalidBeforeMs;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    static String key(UUID userId) {
        return KEY_PREFIX + userId;
    }
}
