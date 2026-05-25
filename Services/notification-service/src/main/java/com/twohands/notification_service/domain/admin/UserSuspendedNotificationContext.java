package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record UserSuspendedNotificationContext(
        UUID targetUserId,
        String enforcementId,
        String enforcementReason,
        String enforcementExpiresAt,
        String referenceType,
        String referenceId
) {
}
