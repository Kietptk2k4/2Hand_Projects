package com.twohands.auth_service.infrastructure.security.oauth;

import com.twohands.auth_service.application.auth.oauth.OAuthExchangeCodePayload;
import com.twohands.auth_service.application.auth.oauth.OAuthExchangeCodeStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisOAuthExchangeCodeStore implements OAuthExchangeCodeStore {

    private static final String KEY_PREFIX = "auth:oauth:exchange:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final long defaultTtlSeconds;

    public RedisOAuthExchangeCodeStore(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${auth.oauth2.exchange-code-ttl-seconds:60}") long defaultTtlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    @Override
    public void save(String code, OAuthExchangeCodePayload payload, long ttlSeconds) {
        long ttl = ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds;
        redisTemplate.opsForValue().set(key(code), payload, Duration.ofSeconds(ttl));
    }

    @Override
    public Optional<OAuthExchangeCodePayload> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String redisKey = key(code.trim());
        Object value = redisTemplate.opsForValue().get(redisKey);
        redisTemplate.delete(redisKey);
        if (value instanceof OAuthExchangeCodePayload payload) {
            return Optional.of(payload);
        }
        return Optional.empty();
    }

    private static String key(String code) {
        return KEY_PREFIX + code;
    }
}
