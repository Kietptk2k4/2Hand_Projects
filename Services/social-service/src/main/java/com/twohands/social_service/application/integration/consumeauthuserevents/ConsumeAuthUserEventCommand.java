package com.twohands.social_service.application.integration.consumeauthuserevents;

import java.time.Instant;
import java.util.UUID;

public record ConsumeAuthUserEventCommand(
        UUID eventId,
        AuthUserEventType eventType,
        UUID userId,
        String status,
        String displayName,
        String email,
        String avatarUrl,
        String coverUrl,
        Boolean isPrivate,
        String actionType,
        Instant occurredAt
) {
}
