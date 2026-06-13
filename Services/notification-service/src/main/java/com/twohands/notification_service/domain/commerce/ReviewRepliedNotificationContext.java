package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record ReviewRepliedNotificationContext(
        UUID buyerId,
        UUID sellerId,
        UUID reviewId,
        UUID productId
) {
}
