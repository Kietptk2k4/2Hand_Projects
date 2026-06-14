package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.cancelsellerorder.CancelSellerOrderCommand;
import com.twohands.commerce_service.application.order.cancelsellerorder.CancelSellerOrderUseCase;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderResult;
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
class CancelSellerOrderUseCaseTest {

    @Mock
    private OrderCancellationRepository orderCancellationRepository;

    @InjectMocks
    private CancelSellerOrderUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @Test
    void shouldCancelSellerOrderImmediatelyForCod() {
        when(orderCancellationRepository.cancelBySeller(eq(orderId), eq(sellerId), eq("out of stock"), any(Instant.class)))
                .thenReturn(new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.CANCELLED, orderId, now));

        CancelOrderResult result = useCase.execute(new CancelSellerOrderCommand(sellerId, orderId, "out of stock"));

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.pendingRefund()).isFalse();
        assertThat(useCase.successMessage(result)).isEqualTo("Huy don hang thanh cong.");
    }

    @Test
    void shouldQueueRefundForVnpayPaidSellerCancel() {
        UUID refundRequestId = UUID.randomUUID();
        when(orderCancellationRepository.cancelBySeller(any(), any(), any(), any(Instant.class)))
                .thenReturn(new BuyerOrderCancellationResult(
                        BuyerOrderCancelOutcome.PENDING_REFUND,
                        orderId,
                        now,
                        refundRequestId
                ));

        CancelOrderResult result = useCase.execute(new CancelSellerOrderCommand(sellerId, orderId, null));

        assertThat(result.pendingRefund()).isTrue();
        assertThat(result.refundRequestId()).isEqualTo(refundRequestId);
        assertThat(result.status()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(useCase.successMessage(result)).contains("cho hoan tien");
    }

    @Test
    void shouldRejectWhenOrderNotFound() {
        when(orderCancellationRepository.cancelBySeller(any(), any(), any(), any(Instant.class)))
                .thenReturn(new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_FOUND, orderId, null));

        assertThatThrownBy(() -> useCase.execute(new CancelSellerOrderCommand(sellerId, orderId, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }
}
