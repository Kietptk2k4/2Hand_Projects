package com.twohands.admin_service.domain.enforcement;

import java.time.Instant;
import java.util.UUID;

public record UserEnforcementLog(
		UUID id,
		UUID enforcementId,
		UserEnforcementStatus oldStatus,
		UserEnforcementStatus newStatus,
		UUID adminId,
		String note,
		Instant createdAt
) {
}
