package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RestoreProductByAdminRepository {

    Optional<ProductForRestore> findById(UUID productId);

    boolean updateStatusFromRemoved(UUID productId, ProductStatus newStatus, Instant occurredAt);
}
