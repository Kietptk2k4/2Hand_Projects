package com.twohands.commerce_service.unit.domain.order;

import com.twohands.commerce_service.domain.order.SellerOrderCancellationPolicy;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SellerOrderCancellationPolicyTest {

    @Test
    void shouldAllowCodProcessingImmediateCancel() {
        assertThat(SellerOrderCancellationPolicy.canCancelBySeller(
                OrderStatus.PROCESSING,
                PaymentStatus.PENDING,
                PaymentMethod.COD
        )).isTrue();
    }

    @Test
    void shouldAllowVnpayPaidRefundQueue() {
        assertThat(SellerOrderCancellationPolicy.canCancelBySeller(
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.VNPAY
        )).isTrue();
    }

    @Test
    void shouldRejectAwaitingPaymentAndPayosProcessing() {
        assertThat(SellerOrderCancellationPolicy.canCancelBySeller(
                OrderStatus.AWAITING_PAYMENT,
                PaymentStatus.PENDING,
                PaymentMethod.VNPAY
        )).isFalse();

        assertThat(SellerOrderCancellationPolicy.canCancelBySeller(
                OrderStatus.PROCESSING,
                PaymentStatus.PENDING,
                PaymentMethod.PAYOS
        )).isFalse();
    }
}
