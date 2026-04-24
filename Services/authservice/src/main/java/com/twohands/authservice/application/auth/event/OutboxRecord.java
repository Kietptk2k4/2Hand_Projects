package com.twohands.authservice.application.auth.event;

import java.util.UUID;

public record OutboxRecord(
        UUID id,
        String eventType,
        String source,
        String payload
) {
}
