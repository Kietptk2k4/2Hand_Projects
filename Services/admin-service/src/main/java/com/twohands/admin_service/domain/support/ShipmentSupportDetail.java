package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ShipmentSupportDetail(
		UUID shipmentId,
		UUID orderId,
		UUID sellerId,
		UUID buyerId,
		String orderStatus,
		String carrier,
		String shipmentType,
		String internalStatus,
		String carrierStatus,
		String ghnOrderCode,
		String trackingNumber,
		BigDecimal shippingFee,
		BigDecimal codAmount,
		Integer weightGram,
		LocalDate estimatedDeliveryDate,
		Instant shippedAt,
		Instant deliveredAt,
		Instant createdAt,
		Instant updatedAt,
		ShipmentSupportShippingAddress shippingAddress,
		List<ShipmentSupportOrderItem> orderItems,
		List<ShipmentSupportStatusHistoryEntry> statusHistory,
		List<ShipmentSupportCarrierWebhookEvent> carrierWebhookEvents
) {
}
