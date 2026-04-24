package com.twohands.authservice.infrastructure.cache.redis;

import com.twohands.authservice.application.auth.port.OtpStore;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisOtpStore implements OtpStore {

    //StringRedisTemplate là một lớp trừu tượng cấp cao được Spring Data Redis cung cấp
    //chuyên để xử lý các thao tác với Redis khi cả khóa (key) và giá trị (value) 
    //đều là chuỗi ký tự (String). Nó mặc định sử dụng trình tuần tự hóa chuỗi, 
    //giúp dữ liệu lưu trong Redis ở dạng văn bản thuần túy, dễ đọc và dễ kiểm tra.
    //Nó chủ yếu được dùng cho các kịch bản lưu trữ khóa-giá trị đơn giản như lưu mã OTP, 
    //token phiên làm việc, các cờ cấu hình, hoặc bất kỳ dữ liệu nào có thể biểu diễn dưới dạng chuỗi.
    //Nó giúp đơn giản hóa việc tương tác với Redis bằng cách loại bỏ việc phải tự chuyển đổi dữ liệu qua lại giữa Object và Byte.
    private final StringRedisTemplate redisTemplate;

    // Constructor injection for Redis template
    // Tiêm phụ thuộc StringRedisTemplate thông qua bộ khởi tạo
    public RedisOtpStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Stores the OTP in Redis with a specific expiration time.
     * Lưu mã OTP vào Redis với thời gian hết hạn cụ thể.
     */
    @Override
    public void save(String key, String otp, long ttlSeconds) {
        // Set the value with a Time-To-Live (TTL) duration
        // Lưu trữ cặp Key-Value (định danh, mã OTP) vào cơ sở dữ liệu Redis.
        // Gắn thời gian sống (Time-To-Live - TTL) là ttlSeconds.
        // Hết thời gian này, Redis sẽ tự động tiêu hủy OTP để đảm bảo bảo mật.
        redisTemplate.opsForValue().set(key, otp, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Retrieves the OTP associated with the given key.
     * Truy xuất mã OTP liên kết với khóa (key) đã cho.
     */
    @Override
    public String get(String key) {
        // Get the value from Redis; returns null if expired or not found
        // Lấy giá trị từ Redis; trả về null nếu đã hết hạn hoặc không tìm thấy
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Removes the OTP from Redis once it is no longer needed.
     * Xóa mã OTP khỏi Redis khi không còn cần thiết (ví dụ: sau khi xác thực xong).
     */
    @Override
    public void delete(String key) {
        // Explicitly delete the key-value pair
        // Chủ động xóa cặp khóa-giá trị khỏi hệ thống
        redisTemplate.delete(key);
    }
}