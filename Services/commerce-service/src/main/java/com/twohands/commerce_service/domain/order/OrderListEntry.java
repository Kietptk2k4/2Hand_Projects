package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderListEntry(
        UUID orderId,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        int itemCount,
        String previewProductName,
        String previewImageUrl,
        OrderListPaymentSummary payment,
        OrderListShipmentSummary shipmentSummary
) {
}
