package com.twohands.commerce_service.domain.shop;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ModerateShopRepository {

    Optional<ShopForModeration> findById(UUID shopId);

    boolean updateStatus(UUID shopId, ShopStatus currentStatus, ShopStatus newStatus, Instant occurredAt);
}
