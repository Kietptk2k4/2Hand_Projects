package com.twohands.commerce_service.delivery.http.order;

import com.twohands.commerce_service.domain.order.OrderStatusHistoryEntry;
import com.twohands.commerce_service.domain.order.ShippingAddressSnapshot;
import com.twohands.commerce_service.domain.order.ViewOrderDetailItem;
import com.twohands.commerce_service.domain.order.ViewOrderDetailPaymentSummary;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
import com.twohands.commerce_service.domain.order.ViewOrderDetailShipment;

public final class ViewOrderDetailResponseFactory {

	private ViewOrderDetailResponseFactory() {
	}

	public static ViewOrderDetailResponse from(ViewOrderDetailResult result) {
		return new ViewOrderDetailResponse(
				result.orderId(),
				result.buyerId(),
				result.orderStatus(),
				result.orderPaymentStatus(),
				result.paymentMethod(),
				result.totalAmount(),
				result.finalAmount(),
				result.createdAt(),
				result.updatedAt(),
				result.completedAt(),
				toPaymentSummaryResponse(result.payment()),
				result.items().stream().map(ViewOrderDetailResponseFactory::toOrderItemDetailResponse).toList(),
				result.shipments().stream().map(ViewOrderDetailResponseFactory::toShipmentDetailResponse).toList(),
				result.orderTimeline().stream().map(ViewOrderDetailResponseFactory::toOrderDetailTimelineEntry).toList()
		);
	}

	private static ViewOrderDetailResponse.PaymentSummaryResponse toPaymentSummaryResponse(
			ViewOrderDetailPaymentSummary payment
	) {
		if (payment == null) {
			return null;
		}
		return new ViewOrderDetailResponse.PaymentSummaryResponse(
				payment.paymentId(),
				payment.status(),
				payment.paymentMethod(),
				payment.amount(),
				payment.currency(),
				payment.paidAt(),
				payment.expiredAt(),
				payment.checkoutUrlExpiredAt(),
				payment.timeline().stream()
						.map(entry -> new ViewOrderDetailResponse.PaymentStatusTimelineEntryResponse(
								entry.oldStatus(),
								entry.newStatus(),
								entry.occurredAt()
						))
						.toList()
		);
	}

	private static ViewOrderDetailResponse.OrderItemDetailResponse toOrderItemDetailResponse(ViewOrderDetailItem item) {
		return new ViewOrderDetailResponse.OrderItemDetailResponse(
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
		);
	}

	private static ViewOrderDetailResponse.ShipmentDetailResponse toShipmentDetailResponse(ViewOrderDetailShipment shipment) {
		return new ViewOrderDetailResponse.ShipmentDetailResponse(
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
				toShippingAddressResponse(shipment.shippingAddress()),
				shipment.timeline().stream()
						.map(entry -> new ViewOrderDetailResponse.ShipmentStatusTimelineEntryResponse(
								entry.oldStatus(),
								entry.newStatus(),
								entry.rawStatus(),
								entry.occurredAt()
						))
						.toList()
		);
	}

	private static ViewOrderDetailResponse.ShippingAddressSnapshotResponse toShippingAddressResponse(
			ShippingAddressSnapshot address
	) {
		if (address == null) {
			return null;
		}
		return new ViewOrderDetailResponse.ShippingAddressSnapshotResponse(
				address.receiverName(),
				address.phone(),
				address.provinceCode(),
				address.districtCode(),
				address.wardCode(),
				address.addressDetail(),
				address.fullAddress()
		);
	}

	private static ViewOrderDetailResponse.OrderStatusTimelineEntryResponse toOrderDetailTimelineEntry(
			OrderStatusHistoryEntry entry
	) {
		return new ViewOrderDetailResponse.OrderStatusTimelineEntryResponse(
				entry.oldStatus(),
				entry.newStatus(),
				entry.changedBy(),
				entry.note(),
				entry.occurredAt()
		);
	}
}
