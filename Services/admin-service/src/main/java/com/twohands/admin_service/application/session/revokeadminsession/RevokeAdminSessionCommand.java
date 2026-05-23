package com.twohands.admin_service.application.session.revokeadminsession;

import java.util.UUID;

public record RevokeAdminSessionCommand(
		UUID sessionId,
		boolean revokeAllSessions,
		String bearerToken
) {
}
