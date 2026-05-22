package com.twohands.commerce_service.application.product.updateproduct;

import java.util.UUID;

public record UpdateProductCommand(
        UUID sellerId,
        UUID productId,
        String productType,
        UUID categoryId,
        UUID brandId,
        String condition,
        String title,
        String description,
        Integer weightGram
) {
}
