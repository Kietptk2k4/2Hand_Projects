package com.twohands.auth_service.domain.oauth;

import java.time.Instant;
import java.util.UUID;

public record OAuthAccount(
        UUID id,
        UUID userId,
        OAuthProvider provider,
        String providerUserId,
        String email,
        Instant createdAt,
        Instant updatedAt
) {
    public OAuthAccount {
        if (id == null || userId == null || provider == null || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("OAuth account required fields are missing");
        }
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("OAuth provider user id is required");
        }
        providerUserId = providerUserId.trim();
        email = normalizeNullable(email);
    }

    public static OAuthAccount create(UUID userId, OAuthProvider provider, String providerUserId, String email, Instant now) {
        return new OAuthAccount(UUID.randomUUID(), userId, provider, providerUserId, email, now, now);
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
