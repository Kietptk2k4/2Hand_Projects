package com.twohands.admin_service.domain.auth;

import java.util.UUID;

public record AdminSessionRevokeRequest(
		UUID sessionId,
		boolean revokeAllSessions,
		String bearerToken
) {
}
