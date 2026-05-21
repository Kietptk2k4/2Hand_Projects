package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public record Product(
        UUID id,
        UUID sellerId,
        UUID shopId,
        String title,
        ProductStatus status,
        Instant updatedAt
) {
    private static final Set<ProductStatus> ARCHIVABLE_STATUSES = EnumSet.of(
            ProductStatus.DRAFT,
            ProductStatus.ACTIVE,
            ProductStatus.PAUSED,
            ProductStatus.OUT_OF_STOCK
    );

    public boolean isOwnedBy(UUID sellerId) {
        return this.sellerId.equals(sellerId);
    }

    public boolean isArchived() {
        return status == ProductStatus.ARCHIVED;
    }

    public boolean isRemoved() {
        return status == ProductStatus.REMOVED;
    }

    public boolean canArchive() {
        return ARCHIVABLE_STATUSES.contains(status);
    }

    public Product withStatus(ProductStatus newStatus, Instant updatedAt) {
        return new Product(id, sellerId, shopId, title, newStatus, updatedAt);
    }
}
