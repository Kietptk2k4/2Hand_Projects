package com.twohands.commerce_service.domain.product;

import java.util.Optional;

public final class UpdateProductInventoryStatusResolver {

    private UpdateProductInventoryStatusResolver() {
    }

    public static Optional<ProductStatus> resolveTargetStatus(
            ProductStatus currentStatus,
            int stockQuantity,
            boolean canRestoreActiveFromOutOfStock
    ) {
        if (currentStatus == ProductStatus.ACTIVE && stockQuantity == 0) {
            return Optional.of(ProductStatus.OUT_OF_STOCK);
        }
        if (currentStatus == ProductStatus.OUT_OF_STOCK
                && stockQuantity > 0
                && canRestoreActiveFromOutOfStock) {
            return Optional.of(ProductStatus.ACTIVE);
        }
        return Optional.empty();
    }
}
