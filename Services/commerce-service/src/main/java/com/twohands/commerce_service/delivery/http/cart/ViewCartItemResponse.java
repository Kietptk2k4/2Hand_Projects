package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record ViewCartItemResponse(
        @JsonProperty("cart_item_id") UUID cartItemId,
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("image_url") String imageUrl,
        int quantity,
        String status,
        @JsonProperty("effective_price") BigDecimal effectivePrice,
        @JsonProperty("in_stock") boolean inStock,
        @JsonProperty("available_quantity") int availableQuantity,
        @JsonProperty("unavailable_reason") String unavailableReason
) {
}
