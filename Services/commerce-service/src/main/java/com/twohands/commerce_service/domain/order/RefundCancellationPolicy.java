package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

public final class RefundCancellationPolicy {

    private RefundCancellationPolicy() {
    }

    public static boolean routesToRefundQueue(
            PaymentMethod paymentMethod,
            PaymentStatus orderPaymentStatus,
            OrderStatus orderStatus
    ) {
        return paymentMethod == PaymentMethod.VNPAY
                && orderPaymentStatus == PaymentStatus.PAID
                && orderStatus == OrderStatus.PROCESSING;
    }
}
