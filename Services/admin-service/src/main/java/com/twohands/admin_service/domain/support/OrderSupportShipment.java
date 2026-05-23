package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrderSupportShipment(
		UUID shipmentId,
		UUID sellerId,
		String status,
		String carrier,
		String trackingNumber,
		BigDecimal shippingFee,
		String shipmentType,
		LocalDate estimatedDeliveryDate,
		Instant shippedAt,
		Instant deliveredAt,
		OrderSupportShippingAddress shippingAddress,
		List<OrderSupportShipmentTimelineEntry> timeline
) {
}
