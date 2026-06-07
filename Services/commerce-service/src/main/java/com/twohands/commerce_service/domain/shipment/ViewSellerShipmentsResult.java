package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;
import java.util.Map;

public record ViewSellerShipmentsResult(
        List<SellerShipmentListEntry> items,
        PageMeta pagination,
        Map<String, Long> statusCounts
) {
}
