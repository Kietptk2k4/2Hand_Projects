package com.twohands.social_service.application.post.common;

import com.twohands.social_service.domain.post.ProductTag;

import java.math.BigDecimal;

public record ProductTagSnapshotData(
        String productId,
        BigDecimal price,
        String name,
        String imageUrl,
        String category,
        String categoryId,
        String shopId,
        boolean available
) {
    /** Legacy snapshot shape without Commerce category/shop IDs. */
    public ProductTagSnapshotData(
            String productId,
            BigDecimal price,
            String name,
            String imageUrl,
            String category,
            boolean available
    ) {
        this(productId, price, name, imageUrl, category, null, null, available);
    }

    public static ProductTagSnapshotData fromDomain(ProductTag tag) {
        if (tag == null) {
            return null;
        }
        return new ProductTagSnapshotData(
                tag.productId(),
                tag.price(),
                tag.name(),
                tag.imageUrl(),
                tag.category(),
                tag.categoryId(),
                tag.shopId(),
                tag.isAvailable()
        );
    }
}
