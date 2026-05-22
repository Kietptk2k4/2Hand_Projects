package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewSellerOrdersRepository {

    long countBySellerId(
            UUID sellerId,
            Optional<OrderItemStatus> itemStatus,
            Optional<ShipmentStatus> shipmentStatus
    );

    List<SellerOrderListEntry> findBySellerId(
            UUID sellerId,
            Optional<OrderItemStatus> itemStatus,
            Optional<ShipmentStatus> shipmentStatus,
            PageQuery pageQuery
    );
}
