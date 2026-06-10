package com.twohands.admin_service.domain.support;

import java.time.Instant;
import java.util.UUID;

public record ShipmentSupportListEntry(
		UUID shipmentId,
		UUID orderId,
		UUID sellerId,
		String carrier,
		String internalStatus,
		String trackingNumber,
		String ghnOrderCode,
		Instant shippedAt,
		Instant createdAt,
		Instant updatedAt
) {
}
