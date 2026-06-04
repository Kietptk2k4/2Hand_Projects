package com.twohands.commerce_service.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface OrderBuyerRepository {

    Optional<UUID> findBuyerIdByOrderId(UUID orderId);
}
