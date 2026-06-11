package com.twohands.commerce_service.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface ViewSellerOrderDetailRepository {

    Optional<ViewSellerOrderDetailResult> findSellerOrderDetail(UUID sellerId, UUID orderId);
}
