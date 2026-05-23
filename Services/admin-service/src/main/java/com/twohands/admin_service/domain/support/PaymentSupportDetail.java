package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentSupportDetail(
		UUID paymentId,
		UUID orderId,
		UUID payerId,
		String paymentMethod,
		BigDecimal amount,
		String currency,
		String status,
		Instant paidAt,
		Instant expiredAt,
		Instant createdAt,
		Instant updatedAt,
		String providerOrderCode,
		String providerTransactionId,
		boolean checkoutUrlAvailable,
		Instant checkoutUrlExpiredAt,
		String orderStatus,
		String orderPaymentStatus,
		String reconciliationStatus,
		List<PaymentSupportStatusTimelineEntry> statusTimeline,
		List<PaymentSupportWebhookEvent> webhookEvents
) {
}
