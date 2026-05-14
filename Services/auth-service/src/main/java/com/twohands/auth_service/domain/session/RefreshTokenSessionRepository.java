package com.twohands.auth_service.domain.session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository {
    Optional<RefreshTokenSession> findById(UUID sessionId);

    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    List<RefreshTokenSession> findByUserIdAndStatus(UUID userId, SessionStatus status);

    RefreshTokenSession save(RefreshTokenSession session);

    int revokeAllByUserId(UUID userId);
}
