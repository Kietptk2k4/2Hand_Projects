package com.twohands.auth_service.application.session;

import com.twohands.auth_service.domain.session.AccessTokenInvalidationStore;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RevokeAllUserSessionsService {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final AccessTokenInvalidationStore accessTokenInvalidationStore;

    public RevokeAllUserSessionsService(
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            AccessTokenInvalidationStore accessTokenInvalidationStore
    ) {
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.accessTokenInvalidationStore = accessTokenInvalidationStore;
    }

    public int revokeAll(UUID userId) {
        int revokedSessionCount = refreshTokenSessionRepository.revokeAllByUserId(userId);
        accessTokenInvalidationStore.invalidateTokensIssuedBefore(userId, Instant.now());
        return revokedSessionCount;
    }
}
