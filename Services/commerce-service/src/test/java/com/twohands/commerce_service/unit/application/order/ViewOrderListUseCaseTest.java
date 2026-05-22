package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.vieworderlist.ViewOrderListCommand;
import com.twohands.commerce_service.application.order.vieworderlist.ViewOrderListUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.OrderListEntry;
import com.twohands.commerce_service.domain.order.OrderListPaymentSummary;
import com.twohands.commerce_service.domain.order.OrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.ViewOrderListRepository;
import com.twohands.commerce_service.domain.order.ViewOrderListResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewOrderListUseCaseTest {

    @Mock
    private ViewOrderListRepository viewOrderListRepository;

    @InjectMocks
    private ViewOrderListUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T14:00:00Z");

    @Test
    void shouldReturnPaginatedOrderList() {
        when(viewOrderListRepository.countByBuyerId(buyerId, Optional.empty())).thenReturn(1L);
        when(viewOrderListRepository.findByBuyerId(eq(buyerId), eq(Optional.empty()), any(PageQuery.class)))
                .thenReturn(List.of(sampleEntry()));

        ViewOrderListResult result = useCase.execute(new ViewOrderListCommand(buyerId, 1, 20, null));

        assertThat(result.orders()).hasSize(1);
        assertThat(result.orders().getFirst().orderId()).isEqualTo(orderId);
        assertThat(result.orders().getFirst().itemCount()).isEqualTo(2);
        assertThat(result.pagination().totalItems()).isEqualTo(1);
        assertThat(result.pagination().page()).isEqualTo(1);
        verify(viewOrderListRepository).findByBuyerId(eq(buyerId), eq(Optional.empty()), any(PageQuery.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoOrders() {
        when(viewOrderListRepository.countByBuyerId(buyerId, Optional.empty())).thenReturn(0L);

        ViewOrderListResult result = useCase.execute(new ViewOrderListCommand(buyerId, null, null, null));

        assertThat(result.orders()).isEmpty();
        assertThat(result.pagination().totalItems()).isZero();
    }

    @Test
    void shouldFilterByStatus() {
        when(viewOrderListRepository.countByBuyerId(buyerId, Optional.of(OrderStatus.PROCESSING))).thenReturn(0L);

        useCase.execute(new ViewOrderListCommand(buyerId, 1, 10, "PROCESSING"));

        verify(viewOrderListRepository).countByBuyerId(buyerId, Optional.of(OrderStatus.PROCESSING));
    }

    @Test
    void shouldRejectInvalidStatus() {
        assertThatThrownBy(() -> useCase.execute(new ViewOrderListCommand(buyerId, 1, 10, "INVALID")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(() -> useCase.execute(new ViewOrderListCommand(buyerId, 0, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    private OrderListEntry sampleEntry() {
        return new OrderListEntry(
                orderId,
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.PAYOS,
                BigDecimal.valueOf(1_050_000),
                BigDecimal.valueOf(1_050_000),
                now,
                now,
                null,
                2,
                "iPhone 15",
                "http://localhost:9000/2hands-commerce-product/p1.jpg",
                new OrderListPaymentSummary(
                        UUID.randomUUID(),
                        PaymentStatus.PAID,
                        PaymentMethod.PAYOS,
                        BigDecimal.valueOf(1_050_000),
                        "VND"
                ),
                new OrderListShipmentSummary(1, List.of(ShipmentStatus.DELIVERED))
        );
    }
}
