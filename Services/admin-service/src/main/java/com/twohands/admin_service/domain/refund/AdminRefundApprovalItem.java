package com.twohands.admin_service.domain.refund;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminRefundApprovalItem(
		UUID id,
		UUID paymentId,
		UUID orderId,
		UUID buyerId,
		String requestedBy,
		UUID requestedByUserId,
		String status,
		BigDecimal amount,
		String reason,
		String adminNote,
		String paymentMethod,
		String orderPaymentStatus,
		String orderStatus,
		Instant requestedAt,
		Instant confirmedAt,
		Instant rejectedAt
) {
}
