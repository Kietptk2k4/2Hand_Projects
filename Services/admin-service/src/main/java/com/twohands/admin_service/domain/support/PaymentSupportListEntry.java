package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSupportListEntry(
		UUID paymentId,
		UUID orderId,
		String paymentMethod,
		BigDecimal amount,
		String currency,
		String status,
		Instant paidAt,
		Instant createdAt
) {
}
