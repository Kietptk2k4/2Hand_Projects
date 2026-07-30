package com.twohands.auth_service.domain.session;

import java.time.Instant;
import java.util.UUID;

public interface AccessTokenInvalidationStore {

    void invalidateTokensIssuedBefore(UUID userId, Instant invalidBefore);

    boolean isTokenInvalidated(UUID userId, long issuedAtEpochMilli);
}
