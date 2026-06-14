package com.twohands.commerce_service.unit.application.payment;

import com.twohands.commerce_service.application.payment.common.PaymentCreatedOutboxService;
import com.twohands.commerce_service.application.payment.createpayment.CreatePaymentCommand;
import com.twohands.commerce_service.application.payment.createpayment.CreatePaymentUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.CreatePaymentRepository;
import com.twohands.commerce_service.domain.payment.CreatePaymentResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentUseCaseTest {

    @Mock
    private CreatePaymentRepository createPaymentRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PaymentCreatedOutboxService paymentCreatedOutboxService;

    @InjectMocks
    private CreatePaymentUseCase useCase;

    private final UUID paymentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();

    @Test
    void shouldCreatePendingPaymentWithOutbox() {
        Instant now = Instant.now();
        when(createPaymentRepository.existsByOrderId(orderId)).thenReturn(false);
        when(createPaymentRepository.createPayment(any())).thenReturn(new CreatePaymentResult(
                paymentId,
                orderId,
                PaymentStatus.PENDING,
                PaymentMethod.PAYOS,
                BigDecimal.valueOf(1_000_000),
                "VND"
        ));
        when(paymentCreatedOutboxService.build(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox(now));

        CreatePaymentResult result = useCase.execute(payosCommand(BigDecimal.valueOf(1_000_000), now));

        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.currency()).isEqualTo("VND");
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldCreatePendingVnpayPaymentWithOutbox() {
        Instant now = Instant.now();
        when(createPaymentRepository.existsByOrderId(orderId)).thenReturn(false);
        when(createPaymentRepository.createPayment(any())).thenReturn(new CreatePaymentResult(
                paymentId,
                orderId,
                PaymentStatus.PENDING,
                PaymentMethod.VNPAY,
                BigDecimal.valueOf(1_000_000),
                "VND"
        ));
        when(paymentCreatedOutboxService.build(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox(now));

        CreatePaymentResult result = useCase.execute(vnpayCommand(BigDecimal.valueOf(1_000_000), now));

        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.VNPAY);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRejectWhenPaymentAlreadyExistsForOrder() {
        when(createPaymentRepository.existsByOrderId(orderId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(payosCommand(BigDecimal.valueOf(1_000_000), Instant.now())))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_EXISTS);

        verify(createPaymentRepository, never()).createPayment(any());
    }

    @Test
    void shouldRejectWhenAmountNotEqualOrderFinalAmount() {
        CreatePaymentCommand command = new CreatePaymentCommand(
                paymentId,
                orderId,
                buyerId,
                buyerId,
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(1_000_000),
                PaymentMethod.COD,
                PaymentMethod.COD,
                "VND",
                null,
                Instant.now()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_AMOUNT);
    }

    @Test
    void shouldRejectWhenAmountIsZeroOrNegative() {
        assertThatThrownBy(() -> useCase.execute(payosCommand(BigDecimal.ZERO, Instant.now())))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_AMOUNT);
    }

    @Test
    void shouldRejectWhenPayerDoesNotMatchBuyer() {
        CreatePaymentCommand command = new CreatePaymentCommand(
                paymentId,
                orderId,
                UUID.randomUUID(),
                buyerId,
                BigDecimal.valueOf(1_000_000),
                BigDecimal.valueOf(1_000_000),
                PaymentMethod.PAYOS,
                PaymentMethod.PAYOS,
                "VND",
                null,
                Instant.now()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectWhenPaymentMethodDiffersFromOrder() {
        CreatePaymentCommand command = new CreatePaymentCommand(
                paymentId,
                orderId,
                buyerId,
                buyerId,
                BigDecimal.valueOf(1_000_000),
                BigDecimal.valueOf(1_000_000),
                PaymentMethod.COD,
                PaymentMethod.PAYOS,
                "VND",
                null,
                Instant.now()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_METHOD);
    }

    private CreatePaymentCommand vnpayCommand(BigDecimal amount, Instant occurredAt) {
        return new CreatePaymentCommand(
                paymentId,
                orderId,
                buyerId,
                buyerId,
                amount,
                amount,
                PaymentMethod.VNPAY,
                PaymentMethod.VNPAY,
                "VND",
                "idem-vnpay",
                occurredAt
        );
    }

    private CreatePaymentCommand payosCommand(BigDecimal amount, Instant occurredAt) {
        return new CreatePaymentCommand(
                paymentId,
                orderId,
                buyerId,
                buyerId,
                amount,
                amount,
                PaymentMethod.PAYOS,
                PaymentMethod.PAYOS,
                "VND",
                "idem-1",
                occurredAt
        );
    }

    private OutboxEvent sampleOutbox(Instant now) {
        return new OutboxEvent(
                UUID.randomUUID(),
                PaymentCreatedOutboxService.EVENT_TYPE,
                "payment:created",
                paymentId,
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
