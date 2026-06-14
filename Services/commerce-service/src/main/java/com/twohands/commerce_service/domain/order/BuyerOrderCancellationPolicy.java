package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

public final class BuyerOrderCancellationPolicy {

    private BuyerOrderCancellationPolicy() {
    }

    public static boolean canCancelByBuyer(
            OrderStatus orderStatus,
            PaymentStatus orderPaymentStatus,
            PaymentMethod paymentMethod
    ) {
        if (RefundCancellationPolicy.routesToRefundQueue(paymentMethod, orderPaymentStatus, orderStatus)) {
            return true;
        }
        if (orderPaymentStatus != PaymentStatus.PENDING) {
            return false;
        }
        if (orderStatus == OrderStatus.CREATED || orderStatus == OrderStatus.AWAITING_PAYMENT) {
            return true;
        }
        return orderStatus == OrderStatus.PROCESSING && paymentMethod == PaymentMethod.COD;
    }
}
