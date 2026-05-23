package com.twohands.admin_service.domain.auth;

import java.util.UUID;

public record AdminSessionRevokeResult(
		UUID targetAdminUserId,
		UUID sessionId,
		int revokedSessionCount,
		boolean revokeAllSessions
) {
}
