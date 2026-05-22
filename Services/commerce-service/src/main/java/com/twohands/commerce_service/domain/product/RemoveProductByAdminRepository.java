package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RemoveProductByAdminRepository {

    Optional<ProductForModeration> findById(UUID productId);

    boolean updateStatusToRemoved(UUID productId, ProductStatus currentStatus, Instant occurredAt);
}
