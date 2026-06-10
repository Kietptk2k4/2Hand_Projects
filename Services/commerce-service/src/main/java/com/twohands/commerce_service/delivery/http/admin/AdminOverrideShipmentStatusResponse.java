package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus.AdminOverrideShipmentStatusResult;

import java.time.Instant;
import java.util.UUID;

public record AdminOverrideShipmentStatusResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("order_id") UUID orderId,
        String carrier,
        @JsonProperty("previous_status") String previousStatus,
        @JsonProperty("current_status") String currentStatus,
        @JsonProperty("override_source") String overrideSource,
        @JsonProperty("raw_status") String rawStatus,
        @JsonProperty("order_items_updated") int orderItemsUpdated,
        @JsonProperty("occurred_at") Instant occurredAt
) {
    private static final String OVERRIDE_SOURCE = "ADMIN";

    public static AdminOverrideShipmentStatusResponse from(AdminOverrideShipmentStatusResult result) {
        return new AdminOverrideShipmentStatusResponse(
                result.shipmentId(),
                result.orderId(),
                result.carrier().name(),
                result.previousStatus().name(),
                result.currentStatus().name(),
                OVERRIDE_SOURCE,
                result.applied() ? "admin_override" : null,
                result.orderItemsUpdated(),
                result.occurredAt()
        );
    }
}
