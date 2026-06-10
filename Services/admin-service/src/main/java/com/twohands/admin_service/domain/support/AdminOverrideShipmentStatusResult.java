package com.twohands.admin_service.domain.support;

import java.time.Instant;
import java.util.UUID;

public record AdminOverrideShipmentStatusResult(
		UUID shipmentId,
		UUID orderId,
		String carrier,
		String previousStatus,
		String currentStatus,
		String overrideSource,
		String rawStatus,
		int orderItemsUpdated,
		Instant occurredAt
) {
}
