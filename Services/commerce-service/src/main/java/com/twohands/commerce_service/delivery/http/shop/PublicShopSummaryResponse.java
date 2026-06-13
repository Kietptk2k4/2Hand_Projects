package com.twohands.commerce_service.delivery.http.shop;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicShopSummaryResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        String description,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("cover_url") String coverUrl,
        @JsonProperty("rating_avg") BigDecimal ratingAvg,
        @JsonProperty("rating_count") int ratingCount,
        @JsonProperty("shop_vacation") boolean shopVacation,
        @JsonProperty("vacation_message") String vacationMessage,
        @JsonProperty("seller_id") UUID sellerId
) {
}
