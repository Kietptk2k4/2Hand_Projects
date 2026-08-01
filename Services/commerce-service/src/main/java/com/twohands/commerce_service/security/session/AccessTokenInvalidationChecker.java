package com.twohands.commerce_service.security.session;

import java.util.UUID;

public interface AccessTokenInvalidationChecker {

    boolean isTokenInvalidated(UUID userId, long issuedAtEpochMilli);
}
