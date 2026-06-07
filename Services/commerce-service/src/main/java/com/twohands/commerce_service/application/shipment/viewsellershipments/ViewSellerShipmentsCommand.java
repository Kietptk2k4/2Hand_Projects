package com.twohands.commerce_service.application.shipment.viewsellershipments;

import java.util.UUID;

public record ViewSellerShipmentsCommand(
        UUID sellerId,
        Integer page,
        Integer limit,
        String status,
        String searchQuery
) {
}
