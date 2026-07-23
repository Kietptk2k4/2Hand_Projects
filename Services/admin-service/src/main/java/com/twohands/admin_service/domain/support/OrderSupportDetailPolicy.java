package com.twohands.admin_service.domain.support;

import java.util.List;

public final class OrderSupportDetailPolicy {

	private OrderSupportDetailPolicy() {
	}

	public static OrderSupportDetail maskContactFields(OrderSupportDetail detail, boolean revealFullPii) {
		if (revealFullPii || detail == null) {
			return detail;
		}
		List<OrderSupportShipment> maskedShipments = detail.shipments().stream()
				.map(OrderSupportDetailPolicy::maskShipment)
				.toList();
		return new OrderSupportDetail(
				detail.orderId(),
				detail.buyerId(),
				detail.orderStatus(),
				detail.orderPaymentStatus(),
				detail.paymentMethod(),
				detail.totalAmount(),
				detail.finalAmount(),
				detail.createdAt(),
				detail.updatedAt(),
				detail.completedAt(),
				detail.payment(),
				detail.items(),
				maskedShipments,
				detail.orderTimeline(),
				detail.activeRefundRequest(),
				detail.cancellationNote()
		);
	}

	private static OrderSupportShipment maskShipment(OrderSupportShipment shipment) {
		OrderSupportShippingAddress address = shipment.shippingAddress();
		if (address == null) {
			return shipment;
		}
		OrderSupportShippingAddress maskedAddress = new OrderSupportShippingAddress(
				maskReceiverName(address.receiverName()),
				maskPhone(address.phone()),
				address.provinceCode(),
				address.districtCode(),
				address.wardCode(),
				maskAddressDetail(address.addressDetail()),
				maskFullAddress(address.fullAddress())
		);
		return new OrderSupportShipment(
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
				maskedAddress,
				shipment.timeline()
		);
	}

	public static String maskPhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return phone;
		}
		String digits = phone.replaceAll("\\D", "");
		if (digits.length() <= 4) {
			return "****";
		}
		return "***" + digits.substring(digits.length() - 4);
	}

	public static String maskReceiverName(String name) {
		if (name == null || name.isBlank()) {
			return name;
		}
		int space = name.indexOf(' ');
		if (space > 0) {
			return name.substring(0, space) + " ***";
		}
		if (name.length() == 1) {
			return "*";
		}
		return name.charAt(0) + "***";
	}

	public static String maskAddressDetail(String addressDetail) {
		if (addressDetail == null || addressDetail.isBlank()) {
			return addressDetail;
		}
		return "***";
	}

	public static String maskFullAddress(String fullAddress) {
		if (fullAddress == null || fullAddress.isBlank()) {
			return fullAddress;
		}
		return "***";
	}
}
