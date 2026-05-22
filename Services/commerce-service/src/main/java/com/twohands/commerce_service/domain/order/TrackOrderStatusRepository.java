package com.twohands.commerce_service.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface TrackOrderStatusRepository {

    Optional<TrackOrderStatusResult> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId);
}
