package com.twohands.auth_service.domain.user;

import java.time.Instant;
import java.util.UUID;

public record LoginLog(
        UUID id,
        UUID userId,
        LoginMethod loginMethod,
        String ipAddress,
        String userAgent,
        boolean success,
        Instant createdAt
) {
    public LoginLog {
        if (id == null || userId == null || loginMethod == null || createdAt == null) {
            throw new UserDomainError("USER_LOGIN_LOG_REQUIRED_FIELDS", "Login log required fields must not be null");
        }
    }
}
