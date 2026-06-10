package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record CommerceOrdersSupportPayload(
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages,
		List<OrderListPayload> orders
) {
	record OrderListPayload(
			@JsonProperty("order_id") UUID orderId,
			@JsonProperty("buyer_id") UUID buyerId,
			@JsonProperty("order_status") String orderStatus,
			@JsonProperty("payment_status") String paymentStatus,
			@JsonProperty("payment_method") String paymentMethod,
			@JsonProperty("final_amount") BigDecimal finalAmount,
			@JsonProperty("created_at") Instant createdAt,
			@JsonProperty("updated_at") Instant updatedAt
	) {
	}
}
