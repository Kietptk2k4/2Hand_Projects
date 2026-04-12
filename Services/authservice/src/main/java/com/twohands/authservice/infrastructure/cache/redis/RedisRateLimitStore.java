package com.twohands.authservice.infrastructure.cache.redis;

import java.util.concurrent.TimeUnit;

import com.twohands.authservice.application.auth.port.RateLimitStore;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

@Component
public class RedisRateLimitStore implements RateLimitStore{
    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public long increment(String key, long ttlSeconds){
        Long count = redisTemplate.opsForValue().increment(key);

        if(count!= null && count == 1){
            redisTemplate.expire(key,ttlSeconds,TimeUnit.SECONDS);
        }
        return count!=null? count:0;
    }

    @Override
    public long getCount(String key){
        String value = redisTemplate.opsForValue().get(key);
        return value!=null? Long.parseLong(value):0;
    }
}
