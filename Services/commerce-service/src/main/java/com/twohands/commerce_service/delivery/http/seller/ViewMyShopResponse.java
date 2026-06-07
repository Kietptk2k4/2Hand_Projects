package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ViewMyShopResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_name") String shopName,
        String description,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("cover_url") String coverUrl,
        ShopStatus status,
        @JsonProperty("rating_avg") BigDecimal ratingAvg,
        @JsonProperty("rating_count") int ratingCount,
        @JsonProperty("is_vacation") boolean vacationMode,
        @JsonProperty("vacation_message") String vacationMessage,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
