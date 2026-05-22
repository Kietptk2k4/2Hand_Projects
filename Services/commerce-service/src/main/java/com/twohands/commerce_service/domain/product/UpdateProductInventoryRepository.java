package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UpdateProductInventoryRepository {

    Optional<UpdateProductInventorySnapshot> findProductForInventoryUpdate(
            UUID productId,
            UUID sellerId,
            Instant now
    );

    ProductInventoryState upsertInventory(
            UUID productId,
            int stockQuantity,
            int lowStockThreshold,
            Instant now
    );

    void updateProductStatus(UUID productId, ProductStatus status, Instant now);
}
