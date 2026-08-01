package com.twohands.admin_service.security.session;

import java.util.UUID;

public interface AccessTokenInvalidationChecker {

	boolean isTokenInvalidated(UUID userId, long issuedAtEpochMilli);
}
