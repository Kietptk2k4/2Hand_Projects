package com.twohands.commerce_service.delivery.http.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.seller.ShippingAddressSnapshotResponse;
import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;

import java.time.Instant;
import java.util.UUID;

public record ViewShippingAddressSnapshotResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("snapshot_id") UUID snapshotId,
        @JsonProperty("accessed_as") ShipmentAccessRole accessedAs,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("address_snapshot") ShippingAddressSnapshotResponse addressSnapshot
) {
}
