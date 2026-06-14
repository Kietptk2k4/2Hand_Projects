package com.twohands.commerce_service.domain.checkout;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

public final class CheckoutPaymentMethodPolicy {

    private CheckoutPaymentMethodPolicy() {
    }

    public static void validateForCheckout(PaymentMethod paymentMethod, boolean codOnlyEnabled) {
        if (paymentMethod == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Payment method is required",
                    "payment_method",
                    "must not be null"
            );
        }

        if (codOnlyEnabled && paymentMethod != PaymentMethod.COD) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_METHOD,
                    "Only COD payment is available at checkout",
                    "payment_method",
                    "must be COD while cod-only mode is enabled"
            );
        }

        if (paymentMethod != PaymentMethod.COD
                && paymentMethod != PaymentMethod.PAYOS
                && paymentMethod != PaymentMethod.VNPAY) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
    }
}
