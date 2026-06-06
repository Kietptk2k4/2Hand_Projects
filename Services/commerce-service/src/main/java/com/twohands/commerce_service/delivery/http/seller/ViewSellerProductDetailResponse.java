package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewSellerProductDetailResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        ProductStatus status,
        @JsonProperty("product_type") String productType,
        @JsonProperty("category_id") UUID categoryId,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("brand_id") UUID brandId,
        String condition,
        String title,
        String description,
        @JsonProperty("weight_gram") int weightGram,
        @JsonProperty("thumbnail_url") String thumbnailUrl,
        BigDecimal price,
        @JsonProperty("sale_price") BigDecimal salePrice,
        @JsonProperty("effective_price") BigDecimal effectivePrice,
        @JsonProperty("price_id") UUID priceId,
        @JsonProperty("stock_quantity") Integer stockQuantity,
        @JsonProperty("low_stock_threshold") Integer lowStockThreshold,
        @JsonProperty("reserved_quantity") Integer reservedQuantity,
        List<SellerProductAttributeResponse> attributes,
        @JsonProperty("media_urls") List<String> mediaUrls,
        @JsonProperty("has_price") boolean hasPrice,
        @JsonProperty("has_inventory") boolean hasInventory,
        @JsonProperty("has_media") boolean hasMedia,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
