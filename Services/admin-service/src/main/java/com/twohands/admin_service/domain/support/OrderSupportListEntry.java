package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSupportListEntry(
		UUID orderId,
		UUID buyerId,
		String orderStatus,
		String paymentStatus,
		String paymentMethod,
		BigDecimal finalAmount,
		Instant createdAt,
		Instant updatedAt
) {
}
