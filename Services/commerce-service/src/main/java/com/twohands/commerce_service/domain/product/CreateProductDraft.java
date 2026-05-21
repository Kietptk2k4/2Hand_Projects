package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record CreateProductDraft(
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
