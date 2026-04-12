package com.twohands.authservice.application.auth.port;

public interface PasswordHasher {
    String hash(String rawPassword);
}
