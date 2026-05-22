package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;

import java.util.List;

public record ViewSellerOrdersResponse(
        List<SellerOrderListEntryResponse> items,
        PageMetaResponse pagination
) {
}
