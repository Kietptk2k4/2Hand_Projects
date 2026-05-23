package com.twohands.admin_service.domain.support;

import java.time.Instant;

public record OrderSupportPaymentTimelineEntry(
		String oldStatus,
		String newStatus,
		Instant occurredAt
) {
}
