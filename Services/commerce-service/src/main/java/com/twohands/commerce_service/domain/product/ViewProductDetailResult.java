package com.twohands.commerce_service.domain.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ViewProductDetailResult(
        UUID productId,
        UUID sellerId,
        String title,
        String description,
        String condition,
        int weightGram,
        ProductStatus status,
        ViewProductDetailCategory category,
        ViewProductDetailShop shop,
        List<ViewProductDetailMediaItem> media,
        List<ViewProductDetailAttributeItem> attributes,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        ViewProductDetailInventorySummary inventorySummary,
        BigDecimal ratingAvg,
        int ratingCount,
        boolean shopVacation,
        String vacationMessage
) {
}
