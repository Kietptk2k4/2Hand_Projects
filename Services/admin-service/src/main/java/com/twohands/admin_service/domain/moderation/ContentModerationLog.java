package com.twohands.admin_service.domain.moderation;

import java.time.Instant;
import java.util.UUID;

public record ContentModerationLog(
		UUID id,
		ContentModerationTargetType targetType,
		String targetId,
		ContentModerationAction action,
		String reason,
		UUID adminId,
		Instant createdAt,
		String note
) {
}
