package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateProductPriceResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        ProductStatus status,
        @JsonProperty("price_id") UUID priceId,
        BigDecimal price,
        @JsonProperty("sale_price") BigDecimal salePrice,
        @JsonProperty("effective_price") BigDecimal effectivePrice,
        @JsonProperty("start_at") Instant startAt,
        @JsonProperty("end_at") Instant endAt,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("previous_active_price_closed") boolean previousActivePriceClosed
) {
}
