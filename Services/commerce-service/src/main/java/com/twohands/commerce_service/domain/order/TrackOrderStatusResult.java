package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.OrderPaymentTracking;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentTrackingLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrackOrderStatusResult(
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
        OrderPaymentTracking payment,
        List<OrderItemTrackingLine> items,
        List<ShipmentTrackingLine> shipments,
        List<OrderStatusHistoryEntry> orderTimeline,
        boolean orderCompleted,
        boolean paymentPaid,
        boolean allItemsCompleted,
        boolean anyShipmentDelivered,
        boolean anyItemDelivered
) {
}
