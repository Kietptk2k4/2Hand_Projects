package com.twohands.auth_service.domain.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        String eventType,
        String source,
        String payload,
        OutboxStatus status,
        int retryCount,
        Instant createdAt,
        Instant publishedAt,
        String lastError
) {
    public OutboxEvent {
        if (id == null) {
            throw new IllegalArgumentException("Outbox id is required");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Outbox event type is required");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Outbox source is required");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Outbox payload is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Outbox status is required");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Outbox createdAt is required");
        }
    }
}
