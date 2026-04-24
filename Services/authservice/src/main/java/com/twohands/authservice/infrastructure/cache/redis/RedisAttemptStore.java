package com.twohands.authservice.infrastructure.cache.redis;

import com.twohands.authservice.application.auth.port.AttemptStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisAttemptStore implements AttemptStore {

    private final StringRedisTemplate redisTemplate;

    public RedisAttemptStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Increments the attempt counter and sets expiration on the first attempt.
     * Tăng bộ đếm số lần thử và thiết lập thời gian hết hạn ở lần thử đầu tiên.
     */
    @Override
    public long increment(String key, long ttlSeconds) {
        // Increment the count in an atomic operation to ensure thread safety
        // Tăng giá trị đếm thông qua một thao tác nguyên tử để đảm bảo an toàn đa luồng
        Long count = redisTemplate.opsForValue().increment(key);
        
        // If it's the first failed attempt, start the lockout/tracking window
        // Nếu đây là lần thử thất bại đầu tiên, bắt đầu khung thời gian theo dõi/khóa
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return count != null ? count : 0;
    }

    /**
     * Retrieves the current number of attempts for a specific key.
     * Truy xuất số lượng lần thử hiện tại cho một khóa cụ thể.
     */
    @Override
    public long getCount(String key) {
        // Fetch the string value from Redis and convert it to a long
        // Lấy giá trị dạng chuỗi từ Redis và chuyển đổi nó sang kiểu long
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

    /**
     * Resets the attempt counter by deleting the key from Redis.
     * Đặt lại bộ đếm số lần thử bằng cách xóa khóa khỏi Redis.
     */
    @Override
    public void delete(String key) {
        // Remove the key to allow the user to try again (e.g., after successful login)
        // Xóa khóa để cho phép người dùng thử lại (ví dụ: sau khi đã đăng nhập thành công)
        redisTemplate.delete(key);
    }
}
