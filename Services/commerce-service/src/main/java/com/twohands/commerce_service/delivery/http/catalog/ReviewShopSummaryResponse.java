package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ReviewShopSummaryResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("seller_id") UUID sellerId
) {
}
