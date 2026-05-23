package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentStatusHistoryEntry;
import com.twohands.commerce_service.domain.payment.PaymentWebhookSummary;
import com.twohands.commerce_service.domain.payment.ViewPaymentSupportDetailResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewPaymentSupportDetailResponse(
        @JsonProperty("payment_id") UUID paymentId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("payer_id") UUID payerId,
        @JsonProperty("payment_method") PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        @JsonProperty("paid_at") Instant paidAt,
        @JsonProperty("expired_at") Instant expiredAt,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("provider_order_code") String providerOrderCode,
        @JsonProperty("provider_transaction_id") String providerTransactionId,
        @JsonProperty("checkout_url_available") boolean checkoutUrlAvailable,
        @JsonProperty("checkout_url_expired_at") Instant checkoutUrlExpiredAt,
        @JsonProperty("order_status") OrderStatus orderStatus,
        @JsonProperty("order_payment_status") PaymentStatus orderPaymentStatus,
        @JsonProperty("reconciliation_status") String reconciliationStatus,
        @JsonProperty("status_timeline") List<PaymentStatusTimelineEntryResponse> statusTimeline,
        @JsonProperty("webhook_events") List<PaymentWebhookEventResponse> webhookEvents
) {
    public static ViewPaymentSupportDetailResponse from(ViewPaymentSupportDetailResult result) {
        return new ViewPaymentSupportDetailResponse(
                result.paymentId(),
                result.orderId(),
                result.payerId(),
                result.paymentMethod(),
                result.amount(),
                result.currency(),
                result.status(),
                result.paidAt(),
                result.expiredAt(),
                result.createdAt(),
                result.updatedAt(),
                result.providerOrderCode(),
                result.providerTransactionId(),
                result.checkoutUrlAvailable(),
                result.checkoutUrlExpiredAt(),
                result.orderStatus(),
                result.orderPaymentStatus(),
                result.reconciliationStatus(),
                result.statusTimeline().stream().map(PaymentStatusTimelineEntryResponse::from).toList(),
                result.webhookEvents().stream().map(PaymentWebhookEventResponse::from).toList()
        );
    }

    public record PaymentStatusTimelineEntryResponse(
            @JsonProperty("old_status") PaymentStatus oldStatus,
            @JsonProperty("new_status") PaymentStatus newStatus,
            @JsonProperty("occurred_at") Instant occurredAt
    ) {
        static PaymentStatusTimelineEntryResponse from(PaymentStatusHistoryEntry entry) {
            return new PaymentStatusTimelineEntryResponse(
                    entry.oldStatus(),
                    entry.newStatus(),
                    entry.occurredAt()
            );
        }
    }

    public record PaymentWebhookEventResponse(
            String provider,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("signature_valid") boolean signatureValid,
            boolean processed,
            @JsonProperty("received_at") Instant receivedAt
    ) {
        static PaymentWebhookEventResponse from(PaymentWebhookSummary summary) {
            return new PaymentWebhookEventResponse(
                    summary.provider(),
                    summary.eventType(),
                    summary.signatureValid(),
                    summary.processed(),
                    summary.receivedAt()
            );
        }
    }
}
