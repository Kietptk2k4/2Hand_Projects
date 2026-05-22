package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.time.Instant;
import java.util.UUID;

public record ModerateShopResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_name") String shopName,
        ShopStatus status,
        @JsonProperty("previous_status") ShopStatus previousStatus,
        @JsonProperty("already_moderated") boolean alreadyModerated,
        @JsonProperty("cart_items_invalidated") int cartItemsInvalidated,
        @JsonProperty("moderated_at") Instant moderatedAt
) {
}
