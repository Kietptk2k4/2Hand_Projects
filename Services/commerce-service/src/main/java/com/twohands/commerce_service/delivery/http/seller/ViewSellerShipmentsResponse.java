package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;

import java.util.List;

public record ViewSellerShipmentsResponse(
        List<SellerShipmentListEntryResponse> items,
        PageMetaResponse pagination,
        SellerShipmentListSummaryResponse summary
) {
}
