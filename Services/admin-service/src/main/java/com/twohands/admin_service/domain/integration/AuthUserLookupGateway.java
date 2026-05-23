package com.twohands.admin_service.domain.integration;

import java.util.UUID;

public interface AuthUserLookupGateway {

	boolean isEnabled();

	void ensureUserExists(UUID userId);
}
