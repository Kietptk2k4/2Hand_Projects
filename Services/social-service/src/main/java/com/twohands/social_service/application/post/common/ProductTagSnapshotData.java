package com.twohands.social_service.application.post.common;

import com.twohands.social_service.domain.post.ProductTag;

import java.math.BigDecimal;

public record ProductTagSnapshotData(
        String productId,
        BigDecimal price,
        String name,
        String imageUrl,
        String category,
        boolean available
) {
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
                tag.isAvailable()
        );
    }
}
