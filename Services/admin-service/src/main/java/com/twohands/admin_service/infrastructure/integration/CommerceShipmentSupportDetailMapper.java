package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.support.ShipmentSupportCarrierWebhookEvent;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportOrderItem;
import com.twohands.admin_service.domain.support.ShipmentSupportShippingAddress;
import com.twohands.admin_service.domain.support.ShipmentSupportStatusHistoryEntry;

import java.util.List;

final class CommerceShipmentSupportDetailMapper {

	private CommerceShipmentSupportDetailMapper() {
	}

	static ShipmentSupportDetail toDomain(CommerceShipmentSupportDetailPayload payload) {
		if (payload == null) {
			return null;
		}
		return new ShipmentSupportDetail(
				payload.shipmentId(),
				payload.orderId(),
				payload.sellerId(),
				payload.buyerId(),
				payload.orderStatus(),
				payload.carrier(),
				payload.shipmentType(),
				payload.internalStatus(),
				payload.carrierStatus(),
				payload.ghnOrderCode(),
				payload.trackingNumber(),
				payload.shippingFee(),
				payload.codAmount(),
				payload.weightGram(),
				payload.estimatedDeliveryDate(),
				payload.shippedAt(),
				payload.deliveredAt(),
				payload.createdAt(),
				payload.updatedAt(),
				toAddress(payload.shippingAddress()),
				toOrderItems(payload.orderItems()),
				toStatusHistory(payload.statusHistory()),
				toWebhookEvents(payload.carrierWebhookEvents())
		);
	}

	private static ShipmentSupportShippingAddress toAddress(CommerceShipmentSupportDetailPayload.ShippingAddressPayload address) {
		if (address == null) {
			return null;
		}
		return new ShipmentSupportShippingAddress(
				address.receiverName(),
				address.phone(),
				address.provinceCode(),
				address.districtCode(),
				address.wardCode(),
				address.addressDetail(),
				address.fullAddress()
		);
	}

	private static List<ShipmentSupportOrderItem> toOrderItems(List<CommerceShipmentSupportDetailPayload.OrderItemPayload> items) {
		if (items == null) {
			return List.of();
		}
		return items.stream()
				.map(item -> new ShipmentSupportOrderItem(
						item.orderItemId(),
						item.productNameSnapshot(),
						item.quantity(),
						item.status()
				))
				.toList();
	}

	private static List<ShipmentSupportStatusHistoryEntry> toStatusHistory(
			List<CommerceShipmentSupportDetailPayload.StatusHistoryPayload> history
	) {
		if (history == null) {
			return List.of();
		}
		return history.stream()
				.map(entry -> new ShipmentSupportStatusHistoryEntry(
						entry.oldStatus(),
						entry.newStatus(),
						entry.rawStatus(),
						entry.occurredAt()
				))
				.toList();
	}

	private static List<ShipmentSupportCarrierWebhookEvent> toWebhookEvents(
			List<CommerceShipmentSupportDetailPayload.CarrierWebhookPayload> events
	) {
		if (events == null) {
			return List.of();
		}
		return events.stream()
				.map(event -> new ShipmentSupportCarrierWebhookEvent(
						event.carrierStatus(),
						event.processed(),
						event.receivedAt()
				))
				.toList();
	}
}
