package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;

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
	public static AdminOverrideShipmentStatusResponse from(AdminOverrideShipmentStatusResult result) {
		return new AdminOverrideShipmentStatusResponse(
				result.shipmentId(),
				result.orderId(),
				result.carrier(),
				result.previousStatus(),
				result.currentStatus(),
				result.overrideSource(),
				result.rawStatus(),
				result.orderItemsUpdated(),
				result.occurredAt()
		);
	}
}
