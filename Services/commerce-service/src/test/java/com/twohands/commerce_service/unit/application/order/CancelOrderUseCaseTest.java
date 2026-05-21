package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.cancelorder.CancelOrderCommand;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderResult;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderUseCase;
import com.twohands.commerce_service.domain.order.BuyerOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.BuyerOrderCancellationResult;
import com.twohands.commerce_service.domain.order.OrderCancellationRepository;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {

    @Mock
    private OrderCancellationRepository orderCancellationRepository;

    @InjectMocks
    private CancelOrderUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @Test
    void shouldCancelOrderSuccessfully() {
        when(orderCancellationRepository.cancelByBuyer(eq(orderId), eq(buyerId), eq("changed mind"), any(Instant.class)))
                .thenReturn(new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.CANCELLED, orderId, now));

        CancelOrderResult result = useCase.execute(new CancelOrderCommand(buyerId, orderId, "changed mind"));

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.alreadyCancelled()).isFalse();
        assertThat(useCase.successMessage(false)).isEqualTo("Huy don hang thanh cong.");
    }

    @Test
    void shouldReturnAlreadyCancelledIdempotently() {
        when(orderCancellationRepository.cancelByBuyer(any(), any(), any(), any(Instant.class)))
                .thenReturn(new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.ALREADY_CANCELLED, orderId, now));

        CancelOrderResult result = useCase.execute(new CancelOrderCommand(buyerId, orderId, null));

        assertThat(result.alreadyCancelled()).isTrue();
        assertThat(useCase.successMessage(true)).isEqualTo("Don hang da duoc huy truoc do.");
    }

    @Test
    void shouldRejectWhenOrderNotFound() {
        when(orderCancellationRepository.cancelByBuyer(any(), any(), any(), any(Instant.class)))
                .thenReturn(new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_FOUND, orderId, null));

        assertThatThrownBy(() -> useCase.execute(new CancelOrderCommand(buyerId, orderId, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenOrderNotCancellable() {
        when(orderCancellationRepository.cancelByBuyer(any(), any(), any(), any(Instant.class)))
                .thenReturn(new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null));

        assertThatThrownBy(() -> useCase.execute(new CancelOrderCommand(buyerId, orderId, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_CANCELLABLE);
    }
}
