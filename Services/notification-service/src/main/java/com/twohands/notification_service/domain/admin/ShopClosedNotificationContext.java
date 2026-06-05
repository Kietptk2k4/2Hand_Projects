package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record ShopClosedNotificationContext(
        UUID shopOwnerId,
        String shopId,
        String closeReason,
        String referenceType,
        String referenceId
) {
}
