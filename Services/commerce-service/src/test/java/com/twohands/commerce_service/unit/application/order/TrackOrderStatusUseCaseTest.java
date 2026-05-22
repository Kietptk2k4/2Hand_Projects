package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.trackorderstatus.TrackOrderStatusCommand;
import com.twohands.commerce_service.application.order.trackorderstatus.TrackOrderStatusUseCase;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderItemTrackingLine;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderStatusHistoryEntry;
import com.twohands.commerce_service.domain.order.TrackOrderStatusRepository;
import com.twohands.commerce_service.domain.order.TrackOrderStatusResult;
import com.twohands.commerce_service.domain.payment.OrderPaymentTracking;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentTrackingLine;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackOrderStatusUseCaseTest {

    @Mock
    private TrackOrderStatusRepository trackOrderStatusRepository;

    @InjectMocks
    private TrackOrderStatusUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T14:00:00Z");

    @Test
    void shouldReturnTrackingForOwnedOrder() {
        TrackOrderStatusResult expected = sampleResult();
        when(trackOrderStatusRepository.findByOrderIdAndBuyerId(orderId, buyerId))
                .thenReturn(Optional.of(expected));

        TrackOrderStatusResult result = useCase.execute(new TrackOrderStatusCommand(buyerId, orderId));

        assertThat(result.orderStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(result.paymentPaid()).isTrue();
        assertThat(result.anyShipmentDelivered()).isTrue();
        assertThat(result.orderCompleted()).isFalse();
        assertThat(result.allItemsCompleted()).isFalse();
    }

    @Test
    void shouldRejectWhenOrderNotFoundOrNotOwned() {
        when(trackOrderStatusRepository.findByOrderIdAndBuyerId(orderId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new TrackOrderStatusCommand(buyerId, orderId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    private TrackOrderStatusResult sampleResult() {
        return new TrackOrderStatusResult(
                orderId,
                buyerId,
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.PAYOS,
                BigDecimal.valueOf(1_000_000),
                BigDecimal.valueOf(1_000_000),
                now,
                now,
                null,
                new OrderPaymentTracking(
                        UUID.randomUUID(),
                        PaymentStatus.PAID,
                        PaymentMethod.PAYOS,
                        now,
                        null,
                        List.of()
                ),
                List.of(new OrderItemTrackingLine(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Phone",
                        1,
                        OrderItemStatus.DELIVERED,
                        UUID.randomUUID(),
                        null
                )),
                List.of(new ShipmentTrackingLine(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ShipmentStatus.DELIVERED,
                        "GHN",
                        "TRACK-001",
                        now,
                        now,
                        List.of()
                )),
                List.of(new OrderStatusHistoryEntry(
                        OrderStatus.AWAITING_PAYMENT,
                        OrderStatus.PROCESSING,
                        "SYSTEM",
                        null,
                        now
                )),
                false,
                true,
                false,
                true,
                true
        );
    }
}
