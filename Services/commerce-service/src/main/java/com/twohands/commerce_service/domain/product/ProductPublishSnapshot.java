package com.twohands.commerce_service.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductPublishSnapshot(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String title,
        String description,
        String condition,
        int weightGram,
        ProductStatus status,
        String shopStatus,
        boolean categoryActive,
        BigDecimal activePrice,
        Integer stockQuantity,
        String primaryMediaUrl,
        Instant updatedAt
) {
    public boolean isOwnedBy(UUID sellerId) {
        return this.sellerId.equals(sellerId);
    }

    public boolean isRemoved() {
        return status == ProductStatus.REMOVED;
    }

    public boolean isArchived() {
        return status == ProductStatus.ARCHIVED;
    }

    public boolean canPublishFromDraftOrPaused() {
        return status == ProductStatus.DRAFT || status == ProductStatus.PAUSED;
    }

    public boolean isAlreadyPublished() {
        return status == ProductStatus.ACTIVE || status == ProductStatus.OUT_OF_STOCK;
    }

    public ProductStatus resolveTargetStatus() {
        if (stockQuantity == null) {
            throw new IllegalStateException("Inventory record is required to resolve publish status");
        }
        return stockQuantity > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK;
    }
}
