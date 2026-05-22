package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record UpdateProductDraft(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String productType,
        UUID categoryId,
        UUID brandId,
        String condition,
        String title,
        String description,
        int weightGram
) {
}
