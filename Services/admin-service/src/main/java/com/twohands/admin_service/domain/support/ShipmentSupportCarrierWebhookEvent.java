package com.twohands.admin_service.domain.support;

import java.time.Instant;

public record ShipmentSupportCarrierWebhookEvent(
		String carrierStatus,
		boolean processed,
		Instant receivedAt
) {
}
