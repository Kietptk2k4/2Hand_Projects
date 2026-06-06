package com.twohands.commerce_service.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SellerProductDetail(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        String productType,
        UUID categoryId,
        String categoryName,
        UUID brandId,
        String condition,
        String title,
        String description,
        int weightGram,
        String thumbnailUrl,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        UUID priceId,
        Integer stockQuantity,
        Integer lowStockThreshold,
        Integer reservedQuantity,
        List<SellerProductAttributeItem> attributes,
        List<String> mediaUrls,
        boolean hasPrice,
        boolean hasInventory,
        boolean hasMedia,
        Instant createdAt,
        Instant updatedAt
) {
}
