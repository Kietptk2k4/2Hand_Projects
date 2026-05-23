package com.twohands.auth_service.domain.session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository {
    Optional<RefreshTokenSession> findById(UUID sessionId);

    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    List<RefreshTokenSession> findByUserIdAndStatus(UUID userId, SessionStatus status);

    RefreshTokenSessionPage findPageByUserId(UUID userId, SessionStatus statusFilter, int limit, int offset);

    RefreshTokenSession save(RefreshTokenSession session);

    int markLoggedOutIfActive(UUID sessionId, Instant updatedAt);

    int markRevokedIfActive(UUID sessionId, Instant updatedAt);

    int revokeAllByUserId(UUID userId);
}
