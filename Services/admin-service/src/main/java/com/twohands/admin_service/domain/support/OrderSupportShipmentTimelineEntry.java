package com.twohands.admin_service.domain.support;

import java.time.Instant;

public record OrderSupportShipmentTimelineEntry(
		String oldStatus,
		String newStatus,
		String rawStatus,
		Instant occurredAt
) {
}
