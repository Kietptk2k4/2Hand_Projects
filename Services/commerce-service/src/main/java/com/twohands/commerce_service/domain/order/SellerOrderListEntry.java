package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellerOrderListEntry(
        UUID orderItemId,
        UUID orderId,
        UUID productId,
        int quantity,
        int lineWeightGram,
        BigDecimal unitPriceSnapshot,
        BigDecimal finalPrice,
        BigDecimal shippingFeeAllocated,
        String productNameSnapshot,
        String imageSnapshot,
        OrderItemStatus itemStatus,
        Instant itemCreatedAt,
        Instant itemUpdatedAt,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus,
        PaymentMethod orderPaymentMethod,
        Instant orderCreatedAt,
        SellerOrderListPaymentSummary payment,
        SellerOrderListShipmentSummary shipment
) {
}
