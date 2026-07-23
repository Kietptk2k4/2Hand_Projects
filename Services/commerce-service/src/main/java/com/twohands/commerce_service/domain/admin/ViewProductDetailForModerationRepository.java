package com.twohands.commerce_service.domain.admin;

import java.util.Optional;
import java.util.UUID;

public interface ViewProductDetailForModerationRepository {

    Optional<AdminProductDetailEntry> findByProductId(UUID productId);
}
