package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PublishProductRepository {

    Optional<ProductPublishSnapshot> findForSeller(UUID productId, UUID sellerId, Instant now);

    ProductPublishSnapshot updateStatus(UUID productId, ProductStatus status, Instant updatedAt);
}
