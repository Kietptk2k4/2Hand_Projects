package com.twohands.authservice.application.auth.port;

public interface OtpStore {
    void save(String key, String otp, long ttlSeconds);
    String get(String Key);
    void delete(String Key);
}
