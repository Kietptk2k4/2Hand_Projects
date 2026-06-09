package com.twohands.commerce_service.delivery.http.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.shipment.syncghnshipment.SyncGhnShipmentResult;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record SyncGhnShipmentResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("synced") boolean synced,
        @JsonProperty("skipped") boolean skipped,
        @JsonProperty("status") ShipmentStatus status,
        @JsonProperty("message") String message
) {
    public static SyncGhnShipmentResponse from(SyncGhnShipmentResult result) {
        return new SyncGhnShipmentResponse(
                result.shipmentId(),
                result.synced(),
                result.skipped(),
                result.status(),
                result.message()
        );
    }
}
