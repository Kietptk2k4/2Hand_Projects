package com.twohands.commerce_service.delivery.http.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record GhnWebhookResponse(
        @JsonProperty("ghn_order_code") String ghnOrderCode,
        @JsonProperty("raw_status") String rawStatus,
        @JsonProperty("signature_valid") boolean signatureValid,
        boolean processed,
        @JsonProperty("shipment_found") boolean shipmentFound,
        @JsonProperty("status_changed") boolean statusChanged,
        @JsonProperty("previous_status") ShipmentStatus previousStatus,
        @JsonProperty("new_status") ShipmentStatus newStatus,
        @JsonProperty("shipment_id") UUID shipmentId
) {
}
