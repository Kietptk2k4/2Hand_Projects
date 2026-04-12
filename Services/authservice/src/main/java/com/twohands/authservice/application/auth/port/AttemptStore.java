package com.twohands.authservice.application.auth.port;

public interface AttemptStore {
    void increment(String key, long ttlSeconds);
    int getCount(String key);
    void delete(String key);
}
