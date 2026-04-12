package com.twohands.authservice.infrastructure.cache.redis;

import com.twohands.authservice.application.auth.port.OtpStore;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisOtpStore implements OtpStore {

    private final StringRedisTemplate redisTemplate;

    public RedisOtpStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String key, String otp, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, otp, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
