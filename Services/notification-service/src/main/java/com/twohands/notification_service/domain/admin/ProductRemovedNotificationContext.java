package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record ProductRemovedNotificationContext(
        UUID sellerUserId,
        String productId,
        String removalReason,
        String referenceType,
        String referenceId
) {
}
