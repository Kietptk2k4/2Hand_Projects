package com.twohands.commerce_service.delivery.http.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.moderation.ShopModerationOwner;

import java.util.UUID;

public record ShopModerationOwnerResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("seller_id") UUID sellerId
) {

    public static ShopModerationOwnerResponse from(ShopModerationOwner owner) {
        return new ShopModerationOwnerResponse(owner.shopId(), owner.sellerId());
    }
}
