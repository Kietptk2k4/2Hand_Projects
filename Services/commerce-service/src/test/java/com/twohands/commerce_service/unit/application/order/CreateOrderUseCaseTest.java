package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.common.OrderCreatedOutboxService;
import com.twohands.commerce_service.application.order.createorder.CreateOrderCommand;
import com.twohands.commerce_service.application.order.createorder.CreateOrderUseCase;
import com.twohands.commerce_service.application.payment.createpayment.CreatePaymentUseCase;
import com.twohands.commerce_service.domain.order.CreateOrderItemResult;
import com.twohands.commerce_service.domain.order.CreateOrderLineRequest;
import com.twohands.commerce_service.domain.order.CreateOrderRepository;
import com.twohands.commerce_service.domain.order.CreateOrderResult;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private CreateOrderRepository createOrderRepository;

    @Mock
    private CreatePaymentUseCase createPaymentUseCase;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OrderCreatedOutboxService orderCreatedOutboxService;

    @InjectMocks
    private CreateOrderUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();

    @Test
    void shouldCreateOrderWithPayosAwaitingPayment() {
        Instant now = Instant.now();
        CreateOrderLineRequest line = sampleLine();
        UUID orderItemId = UUID.randomUUID();

        when(createOrderRepository.createOrder(any(), eq(OrderStatus.AWAITING_PAYMENT)))
                .thenReturn(List.of(new CreateOrderItemResult(
                        orderItemId, productId, sellerId, 1, BigDecimal.valueOf(900_000), BigDecimal.valueOf(900_000)
                )));
        when(createPaymentUseCase.execute(any()))
                .thenReturn(new CreatePaymentResult(
                        paymentId,
                        UUID.randomUUID(),
                        PaymentStatus.PENDING,
                        PaymentMethod.PAYOS,
                        BigDecimal.valueOf(1_000_000),
                        "VND"
                ));
        when(orderCreatedOutboxService.build(any(), any(), any(), any(), any(), any())).thenReturn(new OutboxEvent(
                UUID.randomUUID(),
                OrderCreatedOutboxService.EVENT_TYPE,
                "order:created",
                UUID.randomUUID(),
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        ));

        CreateOrderResult result = useCase.execute(new CreateOrderCommand(
                buyerId,
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(1_000_000),
                PaymentMethod.PAYOS,
                "idem-1",
                List.of(line),
                now
        ));

        assertThat(result.status()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.orderItems()).hasSize(1);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldCreateOrderWithCodProcessingStatus() {
        when(createOrderRepository.createOrder(any(), eq(OrderStatus.PROCESSING)))
                .thenReturn(List.of());
        when(createPaymentUseCase.execute(any()))
                .thenReturn(new CreatePaymentResult(
                        paymentId,
                        UUID.randomUUID(),
                        PaymentStatus.PENDING,
                        PaymentMethod.PAYOS,
                        BigDecimal.valueOf(1_000_000),
                        "VND"
                ));
        when(orderCreatedOutboxService.build(any(), any(), any(), any(), any(), any())).thenReturn(new OutboxEvent(
                UUID.randomUUID(),
                OrderCreatedOutboxService.EVENT_TYPE,
                "order:created",
                UUID.randomUUID(),
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                Instant.now(),
                null,
                null
        ));

        CreateOrderResult result = useCase.execute(new CreateOrderCommand(
                buyerId,
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(900_000),
                PaymentMethod.COD,
                null,
                List.of(sampleLine()),
                Instant.now()
        ));

        assertThat(result.status()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void shouldRejectWhenSnapshotIncomplete() {
        CreateOrderLineRequest line = new CreateOrderLineRequest(
                productId,
                sellerId,
                1,
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(900_000),
                "",
                "Shop A",
                "SKU-1",
                "https://cdn.example.com/p.jpg",
                "{}",
                BigDecimal.ZERO
        );

        assertThatThrownBy(() -> useCase.execute(new CreateOrderCommand(
                buyerId,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                PaymentMethod.COD,
                null,
                List.of(line),
                Instant.now()
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_SNAPSHOT_INCOMPLETE);
    }

    @Test
    void shouldRejectNegativeFinalAmount() {
        assertThatThrownBy(() -> useCase.execute(new CreateOrderCommand(
                buyerId,
                BigDecimal.ZERO,
                BigDecimal.valueOf(-1),
                PaymentMethod.COD,
                null,
                List.of(sampleLine()),
                Instant.now()
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldResolveInitialStatusByPaymentMethod() {
        assertThat(useCase.resolveInitialOrderStatus(PaymentMethod.PAYOS)).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(useCase.resolveInitialOrderStatus(PaymentMethod.COD)).isEqualTo(OrderStatus.PROCESSING);
    }

    private CreateOrderLineRequest sampleLine() {
        return new CreateOrderLineRequest(
                productId,
                sellerId,
                1,
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(900_000),
                "Phone",
                "Shop A",
                "SKU-1",
                "https://cdn.example.com/p.jpg",
                "{}",
                BigDecimal.ZERO
        );
    }
}
