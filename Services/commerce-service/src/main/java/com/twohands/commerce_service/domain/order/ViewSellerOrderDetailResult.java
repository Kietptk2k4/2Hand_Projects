package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewSellerOrderDetailResult(
        UUID orderId,
        OrderStatus orderStatus,
        PaymentStatus orderPaymentStatus,
        PaymentMethod orderPaymentMethod,
        Instant orderCreatedAt,
        SellerOrderListPaymentSummary payment,
        BigDecimal sellerItemsSubtotal,
        BigDecimal sellerShippingTotal,
        List<SellerOrderListEntry> items,
        ShipmentAddressSnapshot shippingAddress,
        CommerceBuyerSummary buyer,
        PaymentRefundRequestSummary activeRefundRequest,
        String cancellationNote
) {
}
