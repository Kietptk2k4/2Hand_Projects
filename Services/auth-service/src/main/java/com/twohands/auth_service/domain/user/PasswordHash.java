package com.twohands.auth_service.domain.user;

public record PasswordHash(String value) {
    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new UserDomainError("USER_PASSWORD_HASH_REQUIRED", "Password hash is required");
        }
        value = value.trim();
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}
