package com.twohands.commerce_service.unit.domain.order;

import com.twohands.commerce_service.domain.order.BuyerOrderCancellationPolicy;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuyerOrderCancellationPolicyTest {

    @Test
    void shouldAllowCreatedOrAwaitingPaymentWithPendingPayment() {
        assertThat(BuyerOrderCancellationPolicy.canCancelByBuyer(
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                PaymentMethod.COD
        )).isTrue();

        assertThat(BuyerOrderCancellationPolicy.canCancelByBuyer(
                OrderStatus.AWAITING_PAYMENT,
                PaymentStatus.PENDING,
                PaymentMethod.VNPAY
        )).isTrue();
    }

    @Test
    void shouldAllowCodProcessingWithPendingPayment() {
        assertThat(BuyerOrderCancellationPolicy.canCancelByBuyer(
                OrderStatus.PROCESSING,
                PaymentStatus.PENDING,
                PaymentMethod.COD
        )).isTrue();
    }

    @Test
    void shouldAllowVnpayPaidProcessingRefundQueue() {
        assertThat(BuyerOrderCancellationPolicy.canCancelByBuyer(
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.VNPAY
        )).isTrue();
    }

    @Test
    void shouldRejectProcessingWhenPaymentIsNotEligible() {
        assertThat(BuyerOrderCancellationPolicy.canCancelByBuyer(
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.COD
        )).isFalse();

        assertThat(BuyerOrderCancellationPolicy.canCancelByBuyer(
                OrderStatus.PROCESSING,
                PaymentStatus.PENDING,
                PaymentMethod.PAYOS
        )).isFalse();

        assertThat(BuyerOrderCancellationPolicy.canCancelByBuyer(
                OrderStatus.PROCESSING,
                PaymentStatus.PENDING,
                PaymentMethod.VNPAY
        )).isFalse();
    }
}
