package com.twohands.commerce_service.application.product.createproduct;

import java.util.UUID;

public record CreateProductCommand(
        UUID sellerId,
        String productType,
        UUID categoryId,
        UUID brandId,
        String condition,
        String title,
        String description,
        Integer weightGram
) {
}
