package com.twohands.auth_service.domain.user;

import java.time.Instant;
import java.util.UUID;

public final class VerificationToken {
    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final VerificationTokenType type;
    private final Instant expiresAt;
    private Instant usedAt;
    private final Instant createdAt;

    public VerificationToken(
            UUID id,
            UUID userId,
            String tokenHash,
            VerificationTokenType type,
            Instant expiresAt,
            Instant usedAt,
            Instant createdAt
    ) {
        if (id == null || userId == null) {
            throw new UserDomainError("USER_VERIFICATION_TOKEN_ID_REQUIRED", "Token id and user id are required");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new UserDomainError("USER_VERIFICATION_TOKEN_HASH_REQUIRED", "Token hash is required");
        }
        if (type == null || expiresAt == null || createdAt == null) {
            throw new UserDomainError("USER_VERIFICATION_TOKEN_REQUIRED_FIELDS", "Token fields are required");
        }
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash.trim();
        this.type = type;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
    }

    public boolean isExpiredAt(Instant now) {
        return expiresAt.isBefore(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed(Instant now) {
        if (isUsed()) {
            throw new UserDomainError("USER_VERIFICATION_TOKEN_ALREADY_USED", "Verification token already used");
        }
        if (isExpiredAt(now)) {
            throw new UserDomainError("USER_VERIFICATION_TOKEN_EXPIRED", "Verification token is expired");
        }
        this.usedAt = now;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public VerificationTokenType type() {
        return type;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant usedAt() {
        return usedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
