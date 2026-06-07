package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ViewSellerShipmentsRepository {

    long countBySellerId(UUID sellerId, Optional<ShipmentStatus> status, Optional<String> searchQuery);

    List<SellerShipmentListEntry> findBySellerId(
            UUID sellerId,
            Optional<ShipmentStatus> status,
            Optional<String> searchQuery,
            PageQuery pageQuery
    );

    Map<String, Long> countByStatusForSeller(UUID sellerId);
}
