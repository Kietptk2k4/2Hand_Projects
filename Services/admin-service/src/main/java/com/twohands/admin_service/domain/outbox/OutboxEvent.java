package com.twohands.admin_service.domain.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
		UUID id,
		String eventType,
		UUID aggregateId,
		String payload,
		OutboxStatus status,
		int retryCount,
		Instant createdAt,
		Instant publishedAt,
		String lastError
) {
}
