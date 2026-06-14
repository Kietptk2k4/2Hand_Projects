package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewOrderDetailResult(
        UUID orderId,
        UUID buyerId,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        ViewOrderDetailPaymentSummary payment,
        List<ViewOrderDetailItem> items,
        List<ViewOrderDetailShipment> shipments,
        List<OrderStatusHistoryEntry> orderTimeline,
        PaymentRefundRequestSummary activeRefundRequest
) {
}
