package com.twohands.authservice.application.auth.port;

public interface RateLimitStore {
    long increment(String key, long ttlSeconds);
    long getCount(String key);
}
