package com.twohands.authservice.infrastructure.message.kafka;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
        String eventId,
        String eventType,
        String timestamp,
        String source,
        Object data
) {
    public static EventEnvelope of(String eventType, String source, Object data) {
        return new EventEnvelope(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now().toString(),
                source,
                data
        );
    }
}
