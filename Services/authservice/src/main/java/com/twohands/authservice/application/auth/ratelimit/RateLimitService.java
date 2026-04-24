package com.twohands.authservice.application.auth.ratelimit;

import com.twohands.authservice.application.auth.port.RateLimitStore;
import com.twohands.authservice.delivery.http.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RateLimitStore rateLimitStore;

    // Maximum number of requests allowed within the time window
    // Số lượng yêu cầu tối đa được phép trong một khoảng thời gian quy định
    @Value("${auth.rate-limit.max-requests}")
    private int max;

    // The time window duration in seconds
    // Độ dài của khoảng thời gian giới hạn tính bằng giây
    @Value("${auth.rate-limit.window-seconds}")
    private long window;

    public RateLimitService(RateLimitStore rateLimitStore) {
        this.rateLimitStore = rateLimitStore;
    }

    /**
     * Checks if the current request exceeds the rate limit for a specific action and IP.
     * Kiểm tra xem yêu cầu hiện tại có vượt quá giới hạn tần suất cho một hành động và IP cụ thể không.
     */
    public void check(String action, String ip) {
        // Construct a unique key for Redis based on the action type and client IP
        // Tạo một khóa (key) duy nhất cho Redis dựa trên loại hành động và IP của khách hàng
        String key = "auth.rate-limit:" + action + ":" + ip;

        // Atomically increment the request count and set/refresh the expiration window
        // Tăng số lượng đếm yêu cầu một cách nguyên tử và thiết lập/làm mới thời gian giới hạn
        long count = rateLimitStore.increment(key, window);

        // If the count exceeds the threshold, block the request by throwing an exception
        // Nếu số lượng vượt quá ngưỡng cho phép, chặn yêu cầu bằng cách ném ra ngoại lệ
        if (count > max) {
            throw new TooManyRequestsException("Too many requests. Please slow down and try again later.");
        }
    }
}
