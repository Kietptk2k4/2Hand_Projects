package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderSupportPayment(
		UUID paymentId,
		String status,
		String paymentMethod,
		BigDecimal amount,
		String currency,
		Instant paidAt,
		Instant expiredAt,
		Instant checkoutUrlExpiredAt,
		List<OrderSupportPaymentTimelineEntry> timeline
) {
}
