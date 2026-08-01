package com.twohands.auth_service.infrastructure.security.session;

import com.twohands.auth_service.domain.session.AccessTokenInvalidationStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Profile("test")
public class NoopAccessTokenInvalidationStore implements AccessTokenInvalidationStore {

    @Override
    public void invalidateTokensIssuedBefore(UUID userId, Instant invalidBefore) {
        // No-op for tests without Redis.
    }

    @Override
    public boolean isTokenInvalidated(UUID userId, long issuedAtEpochMilli) {
        return false;
    }
}
