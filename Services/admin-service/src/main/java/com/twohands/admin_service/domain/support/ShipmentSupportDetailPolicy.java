package com.twohands.admin_service.domain.support;

public final class ShipmentSupportDetailPolicy {

	private ShipmentSupportDetailPolicy() {
	}

	public static ShipmentSupportDetail maskContactFields(ShipmentSupportDetail detail, boolean revealFullPii) {
		if (revealFullPii || detail == null) {
			return detail;
		}
		ShipmentSupportShippingAddress address = detail.shippingAddress();
		if (address == null) {
			return detail;
		}
		ShipmentSupportShippingAddress maskedAddress = new ShipmentSupportShippingAddress(
				OrderSupportDetailPolicy.maskReceiverName(address.receiverName()),
				OrderSupportDetailPolicy.maskPhone(address.phone()),
				address.provinceCode(),
				address.districtCode(),
				address.wardCode(),
				OrderSupportDetailPolicy.maskAddressDetail(address.addressDetail()),
				OrderSupportDetailPolicy.maskFullAddress(address.fullAddress())
		);
		return new ShipmentSupportDetail(
				detail.shipmentId(),
				detail.orderId(),
				detail.sellerId(),
				detail.buyerId(),
				detail.orderStatus(),
				detail.carrier(),
				detail.shipmentType(),
				detail.internalStatus(),
				detail.carrierStatus(),
				detail.ghnOrderCode(),
				detail.trackingNumber(),
				detail.shippingFee(),
				detail.codAmount(),
				detail.weightGram(),
				detail.estimatedDeliveryDate(),
				detail.shippedAt(),
				detail.deliveredAt(),
				detail.createdAt(),
				detail.updatedAt(),
				maskedAddress,
				detail.orderItems(),
				detail.statusHistory(),
				detail.carrierWebhookEvents()
		);
	}
}
