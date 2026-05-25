package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record ShopSuspendedNotificationContext(
        UUID shopOwnerId,
        String shopId,
        String suspensionReason,
        String suspensionExpiresAt,
        String referenceType,
        String referenceId
) {
}
