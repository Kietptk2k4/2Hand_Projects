package com.twohands.commerce_service.unit.application.checkout;

import com.twohands.commerce_service.application.checkout.checkoutfromcart.CheckoutFromCartCommand;
import com.twohands.commerce_service.application.checkout.checkoutfromcart.CheckoutFromCartUseCase;
import com.twohands.commerce_service.config.CommerceCheckoutProperties;
import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryUseCase;
import com.twohands.commerce_service.application.order.common.InventoryReservedOutboxService;
import com.twohands.commerce_service.domain.inventory.InventoryReservationLine;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryResult;
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
    private ReserveInventoryUseCase reserveInventoryUseCase;

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private InventoryReservedOutboxService inventoryReservedOutboxService;

    @Mock
    private CommerceCheckoutProperties checkoutProperties;

    @Mock
    private com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlUseCase createVnpayCheckoutUrlUseCase;

    @InjectMocks
    private CheckoutFromCartUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();

    @Test
    void shouldCheckoutWithCodWhenCodOnlyEnabled() {
        when(checkoutProperties.isCodOnlyEnabled()).thenReturn(true);

        Instant now = Instant.now();
        UUID productId = UUID.randomUUID();
        when(checkoutFromCartRepository.prepareCheckout(any())).thenReturn(CheckoutPrepareOutcome.prepared(
                new CheckoutPreparedData(
                        buyerId,
                        BigDecimal.valueOf(900_000),
                        BigDecimal.valueOf(1_000_000),
                        PaymentMethod.COD,
                        "idem-key",
                        List.of(),
                        List.of(new InventoryReservationLine(productId, 1)),
                        now
                )
        ));
        when(reserveInventoryUseCase.execute(any())).thenReturn(new ReserveInventoryResult(
                List.of(new OrderItemQuantity(productId, productId, 1)),
                now
        ));
        when(createOrderUseCase.execute(any())).thenReturn(new CreateOrderResult(
                orderId,
                paymentId,
                OrderStatus.PROCESSING,
                PaymentStatus.PENDING,
                PaymentMethod.COD,
                BigDecimal.valueOf(900_000),
                BigDecimal.valueOf(1_000_000),
                List.of()
        ));

        CheckoutFromCartResult result = useCase.execute(new CheckoutFromCartCommand(
                buyerId,
                List.of(UUID.randomUUID()),
                UUID.randomUUID(),
                PaymentMethod.COD,
                null,
                "idem-1",
                "127.0.0.1"
        ));

        assertThat(result.orderStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.COD);
    }

    @Test
    void shouldRejectPayosWhenCodOnlyEnabled() {
        when(checkoutProperties.isCodOnlyEnabled()).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CheckoutFromCartCommand(
                buyerId,
                List.of(UUID.randomUUID()),
                UUID.randomUUID(),
                PaymentMethod.PAYOS,
                null,
                "idem-1",
                "127.0.0.1"
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_METHOD);
    }

    @Test
    void shouldRejectInvalidPaymentMethod() {
        when(checkoutProperties.isCodOnlyEnabled()).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CheckoutFromCartCommand(
                buyerId,
                List.of(UUID.randomUUID()),
                UUID.randomUUID(),
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}
