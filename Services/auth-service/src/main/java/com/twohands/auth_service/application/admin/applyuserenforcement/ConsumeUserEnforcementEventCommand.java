package com.twohands.auth_service.application.admin.applyuserenforcement;

import java.time.Instant;
import java.util.UUID;

public record ConsumeUserEnforcementEventCommand(
        UUID eventId,
        String eventType,
        UUID enforcementId,
        UUID userId,
        String actionType,
        String reasonCode,
        String description,
        Instant expiresAt,
        Instant occurredAt
) {
}
