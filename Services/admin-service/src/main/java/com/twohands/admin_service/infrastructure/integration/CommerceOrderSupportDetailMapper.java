package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.support.OrderSupportActiveRefundRequest;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportItem;
import com.twohands.admin_service.domain.support.OrderSupportOrderTimelineEntry;
import com.twohands.admin_service.domain.support.OrderSupportPayment;
import com.twohands.admin_service.domain.support.OrderSupportPaymentTimelineEntry;
import com.twohands.admin_service.domain.support.OrderSupportShipment;
import com.twohands.admin_service.domain.support.OrderSupportShipmentTimelineEntry;
import com.twohands.admin_service.domain.support.OrderSupportShippingAddress;

import java.util.Collections;
import java.util.List;

final class CommerceOrderSupportDetailMapper {

	private CommerceOrderSupportDetailMapper() {
	}

	static OrderSupportDetail toDomain(CommerceOrderSupportDetailPayload payload) {
		if (payload == null) {
			return null;
		}
		return new OrderSupportDetail(
				payload.orderId(),
				payload.buyerId(),
				payload.orderStatus(),
				payload.orderPaymentStatus(),
				payload.paymentMethod(),
				payload.totalAmount(),
				payload.finalAmount(),
				payload.createdAt(),
				payload.updatedAt(),
				payload.completedAt(),
				toPayment(payload.payment()),
				toItems(payload.items()),
				toShipments(payload.shipments()),
				toOrderTimeline(payload.orderTimeline()),
				toActiveRefundRequest(payload.activeRefundRequest()),
				payload.cancellationNote()
		);
	}

	private static OrderSupportActiveRefundRequest toActiveRefundRequest(
			CommerceOrderSupportDetailPayload.ActiveRefundRequestPayload refundRequest
	) {
		if (refundRequest == null) {
			return null;
		}
		return new OrderSupportActiveRefundRequest(
				refundRequest.refundRequestId(),
				refundRequest.status(),
				refundRequest.requestedBy(),
				refundRequest.amount(),
				refundRequest.reason(),
				refundRequest.requestedAt()
		);
	}

	private static OrderSupportPayment toPayment(CommerceOrderSupportDetailPayload.PaymentPayload payment) {
		if (payment == null) {
			return null;
		}
		return new OrderSupportPayment(
				payment.paymentId(),
				payment.status(),
				payment.paymentMethod(),
				payment.amount(),
				payment.currency(),
				payment.paidAt(),
				payment.expiredAt(),
				payment.checkoutUrlExpiredAt(),
				payment.timeline() == null
						? List.of()
						: payment.timeline().stream()
								.map(entry -> new OrderSupportPaymentTimelineEntry(
										entry.oldStatus(),
										entry.newStatus(),
										entry.occurredAt()
								))
								.toList()
		);
	}

	private static List<OrderSupportItem> toItems(List<CommerceOrderSupportDetailPayload.ItemPayload> items) {
		if (items == null) {
			return List.of();
		}
		return items.stream()
				.map(item -> new OrderSupportItem(
						item.orderItemId(),
						item.productId(),
						item.sellerId(),
						item.shipmentId(),
						item.quantity(),
						item.status(),
						item.unitPriceSnapshot(),
						item.finalPrice(),
						item.skuSnapshot(),
						item.productNameSnapshot(),
						item.imageSnapshot(),
						item.attributesSnapshot(),
						item.shopNameSnapshot(),
						item.shippingFeeAllocated(),
						item.completedAt()
				))
				.toList();
	}

	private static List<OrderSupportShipment> toShipments(List<CommerceOrderSupportDetailPayload.ShipmentPayload> shipments) {
		if (shipments == null) {
			return List.of();
		}
		return shipments.stream()
				.map(shipment -> new OrderSupportShipment(
						shipment.shipmentId(),
						shipment.sellerId(),
						shipment.status(),
						shipment.carrier(),
						shipment.trackingNumber(),
						shipment.shippingFee(),
						shipment.shipmentType(),
						shipment.estimatedDeliveryDate(),
						shipment.shippedAt(),
						shipment.deliveredAt(),
						toShippingAddress(shipment.shippingAddress()),
						shipment.timeline() == null
								? List.of()
								: shipment.timeline().stream()
										.map(entry -> new OrderSupportShipmentTimelineEntry(
												entry.oldStatus(),
												entry.newStatus(),
												entry.rawStatus(),
												entry.occurredAt()
										))
										.toList()
				))
				.toList();
	}

	private static OrderSupportShippingAddress toShippingAddress(
			CommerceOrderSupportDetailPayload.ShippingAddressPayload address
	) {
		if (address == null) {
			return null;
		}
		return new OrderSupportShippingAddress(
				address.receiverName(),
				address.phone(),
				address.provinceCode(),
				address.districtCode(),
				address.wardCode(),
				address.addressDetail(),
				address.fullAddress()
		);
	}

	private static List<OrderSupportOrderTimelineEntry> toOrderTimeline(
			List<CommerceOrderSupportDetailPayload.OrderTimelinePayload> timeline
	) {
		if (timeline == null) {
			return Collections.emptyList();
		}
		return timeline.stream()
				.map(entry -> new OrderSupportOrderTimelineEntry(
						entry.oldStatus(),
						entry.newStatus(),
						entry.changedBy(),
						entry.note(),
						entry.occurredAt()
				))
				.toList();
	}
}
