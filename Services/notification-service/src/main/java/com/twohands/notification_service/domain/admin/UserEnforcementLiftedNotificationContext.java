package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record UserEnforcementLiftedNotificationContext(
        UUID targetUserId,
        String enforcementId,
        String referenceType,
        String referenceId
) {
}
