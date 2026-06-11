package com.twohands.social_service.application.post.common;

import com.twohands.social_service.domain.integration.CommerceProductCatalogClient;
import com.twohands.social_service.domain.integration.CommerceProductSnapshot;
import com.twohands.social_service.domain.post.ProductTag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductTagSnapshotResolver {

    private final CommerceProductCatalogClient commerceProductCatalogClient;

    public ProductTagSnapshotResolver(CommerceProductCatalogClient commerceProductCatalogClient) {
        this.commerceProductCatalogClient = commerceProductCatalogClient;
    }

    public List<ProductTag> resolve(List<ProductTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream().map(this::resolveOne).toList();
    }

    private ProductTag resolveOne(ProductTag tag) {
        Optional<CommerceProductSnapshot> snapshot =
                commerceProductCatalogClient.findVisibleProductSnapshot(tag.productId());

        if (snapshot.isEmpty()) {
            return preserveUnavailable(tag);
        }

        CommerceProductSnapshot catalog = snapshot.get();
        return new ProductTag(
                tag.productId(),
                tag.price(),
                catalog.title(),
                catalog.imageUrl(),
                catalog.categoryName(),
                true
        );
    }

    private ProductTag preserveUnavailable(ProductTag tag) {
        if (tag.name() != null || tag.imageUrl() != null || tag.category() != null) {
            return new ProductTag(
                    tag.productId(),
                    tag.price(),
                    tag.name(),
                    tag.imageUrl(),
                    tag.category(),
                    false
            );
        }
        return new ProductTag(tag.productId(), tag.price(), null, null, null, false);
    }
}
