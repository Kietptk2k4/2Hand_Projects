package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record CommercePaymentsSupportPayload(
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages,
		List<PaymentListPayload> payments
) {
	record PaymentListPayload(
			@JsonProperty("payment_id") UUID paymentId,
			@JsonProperty("order_id") UUID orderId,
			@JsonProperty("payment_method") String paymentMethod,
			BigDecimal amount,
			String currency,
			String status,
			@JsonProperty("paid_at") Instant paidAt,
			@JsonProperty("created_at") Instant createdAt
	) {
	}
}
