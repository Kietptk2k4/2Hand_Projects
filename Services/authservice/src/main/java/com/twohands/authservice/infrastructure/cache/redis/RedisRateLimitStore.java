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

    /**
     * Increments the counter for a given key and sets the expiration time if it's a new counter.
     * Tăng bộ đếm cho một khóa cho trước và thiết lập thời gian hết hạn nếu đó là bộ đếm mới.
     */
    @Override
    public long increment(String key, long ttlSeconds) {
        // Increment the value in Redis by 1. If the key doesn't exist, Redis creates it with value 1.
        // Tăng giá trị trong Redis lên 1. Nếu khóa chưa tồn tại, Redis sẽ tự tạo nó với giá trị là 1.
        Long count = redisTemplate.opsForValue().increment(key);

        // If this is the first request (count == 1), set the Time-To-Live (TTL) for the window
        // Nếu đây là yêu cầu đầu tiên (count == 1), thiết lập thời gian tồn tại (TTL) cho khung giờ giới hạn
        if (count != null && count == 1) {
            //otp:email → expire sau 5 phút
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }

        // Return the current count, defaulting to 0 if the result from Redis is null
        // Trả về số lượng hiện tại, mặc định là 0 nếu kết quả từ Redis bị rỗng
        return count != null ? count : 0;
    }

    @Override
    public long getCount(String key){
        String value = redisTemplate.opsForValue().get(key);
        return value!=null? Long.parseLong(value):0;
    }
}
