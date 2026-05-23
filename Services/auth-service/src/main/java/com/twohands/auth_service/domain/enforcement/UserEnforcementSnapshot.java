package com.twohands.auth_service.domain.enforcement;

import java.time.Instant;
import java.util.UUID;

public record UserEnforcementSnapshot(
        UUID enforcementId,
        UUID userId,
        UserEnforcementActionType actionType,
        UserEnforcementSnapshotStatus status,
        String reasonCode,
        String description,
        Instant expiresAt,
        UUID eventId,
        Instant appliedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
