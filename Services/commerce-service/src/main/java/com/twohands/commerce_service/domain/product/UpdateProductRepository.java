package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UpdateProductRepository {

    Optional<UpdateProductSnapshot> findByIdAndSellerId(UUID productId, UUID sellerId);

    UpdateProductResult update(UpdateProductDraft draft, Instant occurredAt);
}
