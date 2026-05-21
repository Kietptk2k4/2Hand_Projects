package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateShopResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_name") String shopName,
        String description,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("cover_url") String coverUrl,
        ShopStatus status,
        @JsonProperty("is_vacation") boolean vacationMode,
        @JsonProperty("shipping_profile_created") boolean shippingProfileCreated,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
