package com.twohands.commerce_service.delivery.http.order;

import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrackOrderStatusResponse(
        UUID orderId,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal finalAmount,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        boolean orderCompleted,
        boolean paymentPaid,
        boolean allItemsCompleted,
        boolean anyShipmentDelivered,
        boolean anyItemDelivered,
        PaymentTrackingResponse payment,
        List<OrderItemTrackingResponse> items,
        List<ShipmentTrackingResponse> shipments,
        List<OrderStatusTimelineEntryResponse> orderTimeline
) {
    public record PaymentTrackingResponse(
            UUID paymentId,
            PaymentStatus status,
            PaymentMethod paymentMethod,
            Instant paidAt,
            Instant expiredAt,
            List<PaymentStatusTimelineEntryResponse> timeline
    ) {
    }

    public record PaymentStatusTimelineEntryResponse(
            PaymentStatus oldStatus,
            PaymentStatus newStatus,
            Instant occurredAt
    ) {
    }

    public record OrderItemTrackingResponse(
            UUID orderItemId,
            UUID productId,
            UUID sellerId,
            String productName,
            int quantity,
            OrderItemStatus status,
            UUID shipmentId,
            Instant completedAt
    ) {
    }

    public record ShipmentTrackingResponse(
            UUID shipmentId,
            UUID sellerId,
            ShipmentStatus status,
            String carrier,
            String trackingNumber,
            Instant shippedAt,
            Instant deliveredAt,
            List<ShipmentStatusTimelineEntryResponse> timeline
    ) {
    }

    public record ShipmentStatusTimelineEntryResponse(
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            String rawStatus,
            Instant occurredAt
    ) {
    }

    public record OrderStatusTimelineEntryResponse(
            OrderStatus oldStatus,
            OrderStatus newStatus,
            String changedBy,
            String note,
            Instant occurredAt
    ) {
    }
}
