package com.twohands.admin_service.application.enforcement.revokeuserenforcement;

import java.util.UUID;

public record RevokeUserEnforcementCommand(
		UUID enforcementId,
		String note,
		String reason,
		String bearerToken
) {
}
