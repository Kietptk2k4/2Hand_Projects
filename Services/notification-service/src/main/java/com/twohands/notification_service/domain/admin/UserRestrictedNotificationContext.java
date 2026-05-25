package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record UserRestrictedNotificationContext(
        UUID targetUserId,
        String enforcementId,
        String enforcementReason,
        String enforcementExpiresAt,
        String restrictedCapabilitiesSummary,
        String referenceType,
        String referenceId
) {
}
