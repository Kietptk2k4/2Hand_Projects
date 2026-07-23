package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminShopDetailResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_name") String shopName,
        String description,
        @JsonProperty("logo_url") String logoUrl,
        ShopStatus status,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("total_product_count") long totalProductCount,
        @JsonProperty("active_product_count") long activeProductCount,
        @JsonProperty("open_order_count") long openOrderCount
) {
}
