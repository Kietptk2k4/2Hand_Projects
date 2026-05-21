package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.completeorder.CompleteOrderCommand;
import com.twohands.commerce_service.application.order.completeorder.CompleteOrderResponse;
import com.twohands.commerce_service.application.order.completeorder.CompleteOrderUseCase;
import com.twohands.commerce_service.domain.order.CompleteOrderOutcome;
import com.twohands.commerce_service.domain.order.CompleteOrderResult;
import com.twohands.commerce_service.domain.order.OrderCompletionRepository;
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
class CompleteOrderUseCaseTest {

    @Mock
    private OrderCompletionRepository orderCompletionRepository;

    @InjectMocks
    private CompleteOrderUseCase useCase;

    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T15:00:00Z");

    @Test
    void shouldCompleteOrderWhenEligible() {
        when(orderCompletionRepository.completeIfEligible(eq(orderId), any(), any(), any(), any(Instant.class)))
                .thenReturn(new CompleteOrderResult(CompleteOrderOutcome.COMPLETED, orderId, now));

        CompleteOrderResponse response = useCase.execute(new CompleteOrderCommand(orderId, null, null, null));

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(response.alreadyCompleted()).isFalse();
        assertThat(useCase.successMessage(false)).isEqualTo("Hoan tat don hang thanh cong.");
    }

    @Test
    void shouldReturnIdempotentWhenAlreadyCompleted() {
        when(orderCompletionRepository.completeIfEligible(any(), any(), any(), any(), any(Instant.class)))
                .thenReturn(new CompleteOrderResult(CompleteOrderOutcome.ALREADY_COMPLETED, orderId, now));

        CompleteOrderResponse response = useCase.execute(new CompleteOrderCommand(orderId, null, null, null));

        assertThat(response.alreadyCompleted()).isTrue();
    }

    @Test
    void shouldRejectWhenNotEligible() {
        when(orderCompletionRepository.completeIfEligible(any(), any(), any(), any(), any(Instant.class)))
                .thenReturn(new CompleteOrderResult(CompleteOrderOutcome.NOT_ELIGIBLE, orderId, null));

        assertThatThrownBy(() -> useCase.execute(new CompleteOrderCommand(orderId, null, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_COMPLETABLE);
    }

    @Test
    void shouldRejectWhenOrderNotFound() {
        when(orderCompletionRepository.completeIfEligible(any(), any(), any(), any(), any(Instant.class)))
                .thenReturn(new CompleteOrderResult(CompleteOrderOutcome.NOT_FOUND, orderId, null));

        assertThatThrownBy(() -> useCase.execute(new CompleteOrderCommand(orderId, null, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    void tryCompleteReturnsOutcomeWithoutThrowing() {
        when(orderCompletionRepository.completeIfEligible(any(), any(), any(), any(), any(Instant.class)))
                .thenReturn(new CompleteOrderResult(CompleteOrderOutcome.NOT_ELIGIBLE, orderId, null));

        CompleteOrderResult result = useCase.tryComplete(new CompleteOrderCommand(orderId, "JOB", "SYSTEM", "SYSTEM"));

        assertThat(result.outcome()).isEqualTo(CompleteOrderOutcome.NOT_ELIGIBLE);
    }
}
