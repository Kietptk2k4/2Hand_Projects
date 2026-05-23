package com.twohands.commerce_service.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface ViewOrderDetailRepository {

    Optional<ViewOrderDetailResult> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId);

    Optional<ViewOrderDetailResult> findByOrderId(UUID orderId);
}
