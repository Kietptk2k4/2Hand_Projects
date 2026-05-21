package com.twohands.commerce_service.domain.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        String eventType,
        String eventKey,
        UUID aggregateId,
        String source,
        String payload,
        OutboxStatus status,
        int retryCount,
        Instant createdAt,
        Instant publishedAt,
        String lastError
) {
}
