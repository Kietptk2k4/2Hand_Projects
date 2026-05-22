package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ViewProductDetailShopResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("cover_url") String coverUrl
) {
}
