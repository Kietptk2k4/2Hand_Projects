package com.twohands.commerce_service.delivery.http.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.moderation.ProductModerationOwner;

import java.util.UUID;

public record ProductModerationOwnerResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId
) {

    public static ProductModerationOwnerResponse from(ProductModerationOwner owner) {
        return new ProductModerationOwnerResponse(owner.productId(), owner.sellerId(), owner.shopId());
    }
}
