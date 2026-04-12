package com.twohands.authservice.application.auth.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.twohands.authservice.application.auth.port.RateLimitStore;

@Service
public class RateLimitService {
    private final RateLimitStore rateLimitStore;

    public RateLimitService(RateLimitStore rateLimitStore) {
        this.rateLimitStore = rateLimitStore;
    }
    @Value("${auth.rate-limit.max-requests}")
    private int max;

    @Value("${auth.rate-limit.window-seconds}")
    private long window;

    public void check(String action, String ip){
        String key = "auth.rate-limit:" + action + ":" + ip;

        long count = rateLimitStore.increment(key,window);

        if(count > max){
            throw new RuntimeException("Too many requests");
        }
    }
}
