package com.twohands.authservice.domain.session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository {

    RefreshTokenSession save(RefreshTokenSession session);

    Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

    Optional<RefreshTokenSession> findById(UUID id);
    
    List<RefreshTokenSession> findByUserId(UUID userId);

    Optional<RefreshTokenSession> findByIdAndUserId(UUID sessionId, UUID userId);

    /** Revokes all non-revoked sessions for a user (used after password change / account deletion). */
    void revokeAllActiveByUserId(UUID userId);
}
