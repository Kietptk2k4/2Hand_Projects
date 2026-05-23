package com.twohands.admin_service.domain.support;

import java.time.Instant;

public record OrderSupportOrderTimelineEntry(
		String oldStatus,
		String newStatus,
		String changedBy,
		String note,
		Instant occurredAt
) {
}
