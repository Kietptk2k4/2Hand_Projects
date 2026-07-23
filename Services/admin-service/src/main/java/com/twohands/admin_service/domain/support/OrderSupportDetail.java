package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderSupportDetail(
		UUID orderId,
		UUID buyerId,
		String orderStatus,
		String orderPaymentStatus,
		String paymentMethod,
		BigDecimal totalAmount,
		BigDecimal finalAmount,
		Instant createdAt,
		Instant updatedAt,
		Instant completedAt,
		OrderSupportPayment payment,
		List<OrderSupportItem> items,
		List<OrderSupportShipment> shipments,
		List<OrderSupportOrderTimelineEntry> orderTimeline,
		OrderSupportActiveRefundRequest activeRefundRequest,
		String cancellationNote
) {
}
