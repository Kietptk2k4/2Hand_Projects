package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewShipmentSupportListResponse(
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages,
		List<ShipmentSupportListItemResponse> shipments
) {
	public static ViewShipmentSupportListResponse from(
			int page,
			int size,
			long totalElements,
			int totalPages,
			List<ShipmentSupportListEntry> shipments
	) {
		return new ViewShipmentSupportListResponse(
				page,
				size,
				totalElements,
				totalPages,
				shipments.stream().map(ShipmentSupportListItemResponse::from).toList()
		);
	}

	public record ShipmentSupportListItemResponse(
			@JsonProperty("shipment_id") UUID shipmentId,
			@JsonProperty("order_id") UUID orderId,
			@JsonProperty("seller_id") UUID sellerId,
			String carrier,
			@JsonProperty("internal_status") String internalStatus,
			@JsonProperty("tracking_number") String trackingNumber,
			@JsonProperty("ghn_order_code") String ghnOrderCode,
			@JsonProperty("shipped_at") Instant shippedAt,
			@JsonProperty("created_at") Instant createdAt,
			@JsonProperty("updated_at") Instant updatedAt
	) {
		static ShipmentSupportListItemResponse from(ShipmentSupportListEntry entry) {
			return new ShipmentSupportListItemResponse(
					entry.shipmentId(),
					entry.orderId(),
					entry.sellerId(),
					entry.carrier(),
					entry.internalStatus(),
					entry.trackingNumber(),
					entry.ghnOrderCode(),
					entry.shippedAt(),
					entry.createdAt(),
					entry.updatedAt()
			);
		}
	}
}
