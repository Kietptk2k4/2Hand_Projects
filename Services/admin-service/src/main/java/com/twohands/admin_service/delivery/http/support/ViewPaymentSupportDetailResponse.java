package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.PaymentSupportDetail;
import com.twohands.admin_service.domain.support.PaymentSupportStatusTimelineEntry;
import com.twohands.admin_service.domain.support.PaymentSupportWebhookEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewPaymentSupportDetailResponse(
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
		@JsonProperty("status_timeline") List<StatusTimelineEntryResponse> statusTimeline,
		@JsonProperty("webhook_events") List<WebhookEventResponse> webhookEvents
) {
	public static ViewPaymentSupportDetailResponse from(PaymentSupportDetail detail) {
		return new ViewPaymentSupportDetailResponse(
				detail.paymentId(),
				detail.orderId(),
				detail.payerId(),
				detail.paymentMethod(),
				detail.amount(),
				detail.currency(),
				detail.status(),
				detail.paidAt(),
				detail.expiredAt(),
				detail.createdAt(),
				detail.updatedAt(),
				detail.providerOrderCode(),
				detail.providerTransactionId(),
				detail.checkoutUrlAvailable(),
				detail.checkoutUrlExpiredAt(),
				detail.orderStatus(),
				detail.orderPaymentStatus(),
				detail.reconciliationStatus(),
				detail.statusTimeline().stream().map(StatusTimelineEntryResponse::from).toList(),
				detail.webhookEvents().stream().map(WebhookEventResponse::from).toList()
		);
	}

	public record StatusTimelineEntryResponse(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
		static StatusTimelineEntryResponse from(PaymentSupportStatusTimelineEntry entry) {
			return new StatusTimelineEntryResponse(entry.oldStatus(), entry.newStatus(), entry.occurredAt());
		}
	}

	public record WebhookEventResponse(
			String provider,
			@JsonProperty("event_type") String eventType,
			@JsonProperty("signature_valid") boolean signatureValid,
			boolean processed,
			@JsonProperty("received_at") Instant receivedAt
	) {
		static WebhookEventResponse from(PaymentSupportWebhookEvent event) {
			return new WebhookEventResponse(
					event.provider(),
					event.eventType(),
					event.signatureValid(),
					event.processed(),
					event.receivedAt()
			);
		}
	}
}
