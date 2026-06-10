package com.twohands.commerce_service.unit.domain.checkout;

import com.twohands.commerce_service.domain.checkout.CheckoutPaymentMethodPolicy;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckoutPaymentMethodPolicyTest {

    @Test
    void shouldAllowCodWhenCodOnlyEnabled() {
        assertThatCode(() -> CheckoutPaymentMethodPolicy.validateForCheckout(PaymentMethod.COD, true))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectPayosWhenCodOnlyEnabled() {
        assertThatThrownBy(() -> CheckoutPaymentMethodPolicy.validateForCheckout(PaymentMethod.PAYOS, true))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_METHOD);
    }

    @Test
    void shouldAllowPayosWhenCodOnlyDisabled() {
        assertThatCode(() -> CheckoutPaymentMethodPolicy.validateForCheckout(PaymentMethod.PAYOS, false))
                .doesNotThrowAnyException();
    }
}
