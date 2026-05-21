package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record ArchiveProductResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("shop_id") UUID shopId,
        ProductStatus status,
        @JsonProperty("archived_at") Instant archivedAt,
        @JsonProperty("cart_items_invalidated") int cartItemsInvalidated,
        @JsonProperty("already_archived") boolean alreadyArchived
) {
}
