package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record CommercePaymentSupportDetailPayload(
		@JsonProperty("payment_id") UUID paymentId,
		@JsonProperty("order_id") UUID orderId,
		@JsonProperty("payer_id") UUID payerId,
		@JsonProperty("payment_method") String paymentMethod,
		BigDecimal amount,
		String currency,
		String status,
		@JsonProperty("paid_at") Instant paidAt,
		@JsonProperty("expired_at") Instant expiredAt,
		@JsonProperty("created_at") Instant createdAt,
		@JsonProperty("updated_at") Instant updatedAt,
		@JsonProperty("provider_order_code") String providerOrderCode,
		@JsonProperty("provider_transaction_id") String providerTransactionId,
		@JsonProperty("checkout_url_available") boolean checkoutUrlAvailable,
		@JsonProperty("checkout_url_expired_at") Instant checkoutUrlExpiredAt,
		@JsonProperty("order_status") String orderStatus,
		@JsonProperty("order_payment_status") String orderPaymentStatus,
		@JsonProperty("reconciliation_status") String reconciliationStatus,
		@JsonProperty("status_timeline") List<StatusTimelinePayload> statusTimeline,
		@JsonProperty("webhook_events") List<WebhookEventPayload> webhookEvents
) {
	record StatusTimelinePayload(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
	}

	record WebhookEventPayload(
			String provider,
			@JsonProperty("event_type") String eventType,
			@JsonProperty("signature_valid") boolean signatureValid,
			boolean processed,
			@JsonProperty("received_at") Instant receivedAt
	) {
	}
}
