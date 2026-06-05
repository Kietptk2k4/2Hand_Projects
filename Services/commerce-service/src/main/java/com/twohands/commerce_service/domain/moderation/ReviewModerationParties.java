package com.twohands.commerce_service.domain.moderation;

import java.util.UUID;

public record ReviewModerationParties(
        UUID reviewId,
        UUID sellerId,
        UUID buyerId
) {
}
