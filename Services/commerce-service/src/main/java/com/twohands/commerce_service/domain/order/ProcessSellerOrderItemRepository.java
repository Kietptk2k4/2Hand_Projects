package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.shipment.CreateShipmentOrderContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessSellerOrderItemRepository {

    List<SellerOrderItemLine> findOrderItemsBySellerAndIds(UUID sellerId, List<UUID> orderItemIds);

    Optional<CreateShipmentOrderContext> findOrderContext(UUID orderId);

    int markPendingItemsProcessing(UUID sellerId, List<UUID> orderItemIds, Instant occurredAt);
}
