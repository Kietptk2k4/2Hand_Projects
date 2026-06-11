package com.twohands.social_service.domain.post;

import java.math.BigDecimal;

public record ProductTag(
        String productId,
        BigDecimal price,
        String name,
        String imageUrl,
        String category,
        Boolean available
) {
    public ProductTag(String productId, BigDecimal price) {
        this(productId, price, null, null, null, null);
    }

    public boolean isAvailable() {
        return available == null || available;
    }
}
