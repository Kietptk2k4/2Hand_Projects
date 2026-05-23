package com.twohands.admin_service.domain.support;

import java.time.Instant;

public record PaymentSupportWebhookEvent(
		String provider,
		String eventType,
		boolean signatureValid,
		boolean processed,
		Instant receivedAt
) {
}
