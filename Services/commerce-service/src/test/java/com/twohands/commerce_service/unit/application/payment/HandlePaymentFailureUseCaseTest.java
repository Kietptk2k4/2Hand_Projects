package com.twohands.commerce_service.unit.application.payment;

import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureCommand;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureUseCase;
import com.twohands.commerce_service.domain.payment.HandlePaymentFailureRepository;
import com.twohands.commerce_service.domain.payment.HandlePaymentFailureResult;
import com.twohands.commerce_service.domain.payment.LockedPaymentContext;
import com.twohands.commerce_service.domain.payment.PaymentFailureOutcome;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlePaymentFailureUseCaseTest {

    @Mock
    private HandlePaymentFailureRepository handlePaymentFailureRepository;

    private HandlePaymentFailureUseCase useCase;

    private final UUID paymentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T17:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new HandlePaymentFailureUseCase(
                handlePaymentFailureRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldProcessPendingPaymentFailure() {
        LockedPaymentContext payment = pendingPayment();
        when(handlePaymentFailureRepository.lockPaymentById(paymentId)).thenReturn(Optional.of(payment));
        when(handlePaymentFailureRepository.handleFailure(
                eq(payment),
                eq(PaymentStatus.FAILED),
                eq("PAYOS_WEBHOOK_FAILED"),
                eq("SYSTEM:PAYOS_WEBHOOK"),
                any(),
                eq(now)
        )).thenReturn(new HandlePaymentFailureResult(
                PaymentFailureOutcome.PROCESSED,
                paymentId,
                orderId,
                PaymentStatus.FAILED,
                true,
                now
        ));

        HandlePaymentFailureResult result = useCase.execute(HandlePaymentFailureCommand.byPaymentId(
                paymentId,
                PaymentStatus.FAILED,
                "PAYOS_WEBHOOK_FAILED",
                "SYSTEM:PAYOS_WEBHOOK",
                "{}"
        ));

        assertThat(result.outcome()).isEqualTo(PaymentFailureOutcome.PROCESSED);
        assertThat(result.inventoryReleased()).isTrue();
    }

    @Test
    void shouldSkipWhenPaymentAlreadyPaid() {
        LockedPaymentContext paid = new LockedPaymentContext(
                paymentId,
                orderId,
                UUID.randomUUID(),
                "PAID",
                "PAYOS",
                "PROCESSING",
                "PAID"
        );
        when(handlePaymentFailureRepository.lockPaymentById(paymentId)).thenReturn(Optional.of(paid));

        HandlePaymentFailureResult result = useCase.execute(HandlePaymentFailureCommand.byPaymentId(
                paymentId,
                PaymentStatus.FAILED,
                "PAYOS_WEBHOOK_FAILED",
                "SYSTEM:PAYOS_WEBHOOK",
                "{}"
        ));

        assertThat(result.outcome()).isEqualTo(PaymentFailureOutcome.SKIPPED_ALREADY_PAID);
        verify(handlePaymentFailureRepository, never()).handleFailure(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectWhenPaymentNotFound() {
        when(handlePaymentFailureRepository.lockPaymentById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(HandlePaymentFailureCommand.byPaymentId(
                paymentId,
                PaymentStatus.CANCELLED,
                "REASON",
                "SYSTEM",
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
    }

    private LockedPaymentContext pendingPayment() {
        return new LockedPaymentContext(
                paymentId,
                orderId,
                UUID.randomUUID(),
                "PENDING",
                "PAYOS",
                "AWAITING_PAYMENT",
                "PENDING"
        );
    }
}
