package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record SellerOrderListShipmentSummaryResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        ShipmentStatus status,
        ShipmentCarrier carrier,
        @JsonProperty("tracking_number") String trackingNumber,
        @JsonProperty("delivery_address_summary") String deliveryAddressSummary
) {
}
