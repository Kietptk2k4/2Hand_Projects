package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.time.Instant;
import java.util.UUID;

public record SellerShipmentListEntryResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("order_id") UUID orderId,
        ShipmentCarrier carrier,
        @JsonProperty("shipment_type") ShipmentType shipmentType,
        ShipmentStatus status,
        @JsonProperty("tracking_number") String trackingNumber,
        @JsonProperty("ghn_order_code") String ghnOrderCode,
        @JsonProperty("delivery_address_summary") String deliveryAddressSummary,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("order_item_count") int orderItemCount
) {
}
