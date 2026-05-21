package com.twohands.commerce_service.domain.catalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ProductPurchaseReadRepository {

    Optional<ProductPurchaseContext> findByProductId(UUID productId);

    Map<UUID, ProductPurchaseContext> findByProductIds(Collection<UUID> productIds);
}
