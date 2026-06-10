package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.PaymentSupportListEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewPaymentsForSupportResponse(
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages,
		List<PaymentListEntryResponse> payments
) {
	public static ViewPaymentsForSupportResponse from(
			int page,
			int size,
			long totalElements,
			int totalPages,
			List<PaymentSupportListEntry> payments
	) {
		return new ViewPaymentsForSupportResponse(
				page,
				size,
				totalElements,
				totalPages,
				payments.stream().map(PaymentListEntryResponse::from).toList()
		);
	}

	public record PaymentListEntryResponse(
			@JsonProperty("payment_id") UUID paymentId,
			@JsonProperty("order_id") UUID orderId,
			@JsonProperty("payment_method") String paymentMethod,
			BigDecimal amount,
			String currency,
			String status,
			@JsonProperty("paid_at") Instant paidAt,
			@JsonProperty("created_at") Instant createdAt
	) {
		static PaymentListEntryResponse from(PaymentSupportListEntry entry) {
			return new PaymentListEntryResponse(
					entry.paymentId(),
					entry.orderId(),
					entry.paymentMethod(),
					entry.amount(),
					entry.currency(),
					entry.status(),
					entry.paidAt(),
					entry.createdAt()
			);
		}
	}
}
