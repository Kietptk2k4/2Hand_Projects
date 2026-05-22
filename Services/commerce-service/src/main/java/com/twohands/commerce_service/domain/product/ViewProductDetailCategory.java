package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record ViewProductDetailCategory(
        UUID categoryId,
        String name,
        String slug
) {
}
