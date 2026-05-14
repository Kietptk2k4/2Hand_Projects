package com.twohands.auth_service.domain.session;

import com.twohands.auth_service.domain.shared.DomainEvent;
import com.twohands.auth_service.domain.session.event.SessionLoggedOutEvent;
import com.twohands.auth_service.domain.session.event.SessionRevokedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RefreshTokenSession {
    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final String deviceId;
    private final String ipAddress;
    private final String userAgent;
    private final Instant expiresAt;
    private SessionStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    public RefreshTokenSession(
            UUID id,
            UUID userId,
            String tokenHash,
            String deviceId,
            String ipAddress,
            String userAgent,
            Instant expiresAt,
            SessionStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || userId == null) {
            throw new SessionDomainError("SESSION_ID_REQUIRED", "Session id and user id are required");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new SessionDomainError("SESSION_TOKEN_HASH_REQUIRED", "Token hash is required");
        }
        if (expiresAt == null || status == null || createdAt == null || updatedAt == null) {
            throw new SessionDomainError("SESSION_REQUIRED_FIELDS", "Session required fields are missing");
        }
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash.trim();
        this.deviceId = normalizeNullable(deviceId);
        this.ipAddress = normalizeNullable(ipAddress);
        this.userAgent = normalizeNullable(userAgent);
        this.expiresAt = expiresAt;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RefreshTokenSession createActive(
            UUID id,
            UUID userId,
            String tokenHash,
            String deviceId,
            String ipAddress,
            String userAgent,
            Instant expiresAt,
            Instant now
    ) {
        return new RefreshTokenSession(
                id,
                userId,
                tokenHash,
                deviceId,
                ipAddress,
                userAgent,
                expiresAt,
                SessionStatus.ACTIVE,
                now,
                now
        );
    }

    public void ensureUsableForRefresh(Instant now) {
        if (status != SessionStatus.ACTIVE) {
            throw new SessionDomainError("SESSION_NOT_ACTIVE", "Refresh token session is not active");
        }
        if (expiresAt.isBefore(now)) {
            throw new SessionDomainError("SESSION_EXPIRED", "Refresh token session is expired");
        }
    }

    public void logout(Instant now) {
        if (status == SessionStatus.REVOKED || status == SessionStatus.LOGGED_OUT) {
            return;
        }
        this.status = SessionStatus.LOGGED_OUT;
        this.updatedAt = now;
        this.pendingEvents.add(new SessionLoggedOutEvent(id, userId, now));
    }

    public void revoke(Instant now) {
        if (status == SessionStatus.REVOKED) {
            return;
        }
        this.status = SessionStatus.REVOKED;
        this.updatedAt = now;
        this.pendingEvents.add(new SessionRevokedEvent(id, userId, now));
    }

    public boolean belongsTo(UUID candidateUserId) {
        return userId.equals(candidateUserId);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
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

    public String deviceId() {
        return deviceId;
    }

    public String ipAddress() {
        return ipAddress;
    }

    public String userAgent() {
        return userAgent;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public SessionStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
