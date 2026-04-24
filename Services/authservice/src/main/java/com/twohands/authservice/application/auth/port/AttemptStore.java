package com.twohands.authservice.application.auth.port;

public interface AttemptStore {

    long increment(String key, long ttlSeconds);

    long getCount(String key);

    void delete(String key);
}
