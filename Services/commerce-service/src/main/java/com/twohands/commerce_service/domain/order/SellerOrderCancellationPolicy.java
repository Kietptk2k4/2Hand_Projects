package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

public final class SellerOrderCancellationPolicy {

    private SellerOrderCancellationPolicy() {
    }

    public static boolean canCancelBySeller(
            OrderStatus orderStatus,
            PaymentStatus orderPaymentStatus,
            PaymentMethod paymentMethod
    ) {
        if (RefundCancellationPolicy.routesToRefundQueue(paymentMethod, orderPaymentStatus, orderStatus)) {
            return true;
        }
        return paymentMethod == PaymentMethod.COD
                && orderPaymentStatus == PaymentStatus.PENDING
                && orderStatus == OrderStatus.PROCESSING;
    }
}
