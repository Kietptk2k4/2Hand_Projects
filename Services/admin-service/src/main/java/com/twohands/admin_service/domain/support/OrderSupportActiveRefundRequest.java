package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSupportActiveRefundRequest(
		UUID refundRequestId,
		String status,
		String requestedBy,
		BigDecimal amount,
		String reason,
		Instant requestedAt
) {
}
