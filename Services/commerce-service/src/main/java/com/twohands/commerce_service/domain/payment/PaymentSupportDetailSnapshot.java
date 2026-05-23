package com.twohands.commerce_service.domain.payment;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentSupportDetailSnapshot(
        UUID paymentId,
        UUID orderId,
        UUID payerId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant paidAt,
        Instant expiredAt,
        Instant createdAt,
        Instant updatedAt,
        String payosOrderCode,
        String payosTransactionId,
        String payosCheckoutUrl,
        Instant checkoutUrlExpiredAt,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus,
        List<PaymentStatusHistoryEntry> statusTimeline,
        List<PaymentWebhookSummary> webhookEvents
) {
}
