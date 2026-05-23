package com.twohands.admin_service.application.session.revokeadminsession;

import java.util.UUID;

public record RevokeAdminSessionResult(
		UUID targetAdminUserId,
		UUID sessionId,
		int revokedSessionCount,
		boolean revokeAllSessions
) {
}
