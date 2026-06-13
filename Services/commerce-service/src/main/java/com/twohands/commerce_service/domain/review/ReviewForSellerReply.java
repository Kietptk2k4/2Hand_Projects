package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record ReviewForSellerReply(
        UUID reviewId,
        UUID sellerId,
        UUID buyerId,
        UUID productId,
        ReviewStatus status
) {
    public boolean isOwnedBy(UUID sellerId) {
        return this.sellerId.equals(sellerId);
    }

    public boolean isVisible() {
        return status == ReviewStatus.VISIBLE;
    }
}
