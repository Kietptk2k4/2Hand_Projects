package com.twohands.commerce_service.delivery.http.shop;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ViewPublicShopByUserResponse(
        @JsonProperty("has_shop") boolean hasShop,
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("seller_id") UUID sellerId
) {
}
