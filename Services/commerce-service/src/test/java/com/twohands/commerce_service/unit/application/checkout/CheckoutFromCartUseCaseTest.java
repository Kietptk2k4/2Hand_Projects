package com.twohands.commerce_service.unit.application.checkout;

import com.twohands.commerce_service.application.checkout.checkoutfromcart.CheckoutFromCartCommand;
import com.twohands.commerce_service.application.checkout.checkoutfromcart.CheckoutFromCartUseCase;
import com.twohands.commerce_service.application.order.common.InventoryReservedOutboxService;
import com.twohands.commerce_service.application.order.createorder.CreateOrderUseCase;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRepository;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartResult;
import com.twohands.commerce_service.domain.checkout.CheckoutPrepareOutcome;
import com.twohands.commerce_service.domain.checkout.CheckoutPreparedData;
import com.twohands.commerce_service.domain.order.CreateOrderResult;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutFromCartUseCaseTest {

    @Mock
    private CheckoutFromCartRepository checkoutFromCartRepository;

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private InventoryReservedOutboxService inventoryReservedOutboxService;

    @InjectMocks
    private CheckoutFromCartUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();

    @Test
    void shouldCheckoutWithPayos() {
        when(checkoutFromCartRepository.prepareCheckout(any())).thenReturn(CheckoutPrepareOutcome.prepared(
                new CheckoutPreparedData(
                        buyerId,
                        BigDecimal.valueOf(900_000),
                        BigDecimal.valueOf(1_000_000),
                        PaymentMethod.PAYOS,
                        "idem-key",
                        List.of(),
                        List.of(),
                        Instant.now()
                )
        ));
        when(createOrderUseCase.execute(any())).thenReturn(new CreateOrderResult(
                orderId,
                paymentId,
                OrderStatus.AWAITING_PAYMENT,
                PaymentStatus.PENDING,
                PaymentMethod.PAYOS,
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(1_000_000),
                List.of()
        ));

        CheckoutFromCartResult result = useCase.execute(new CheckoutFromCartCommand(
                buyerId,
                List.of(UUID.randomUUID()),
                UUID.randomUUID(),
                PaymentMethod.PAYOS,
                null,
                "idem-1"
        ));

        assertThat(result.orderStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.PAYOS);
    }

    @Test
    void shouldRejectInvalidPaymentMethod() {
        assertThatThrownBy(() -> useCase.execute(new CheckoutFromCartCommand(
                buyerId,
                List.of(UUID.randomUUID()),
                UUID.randomUUID(),
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}
