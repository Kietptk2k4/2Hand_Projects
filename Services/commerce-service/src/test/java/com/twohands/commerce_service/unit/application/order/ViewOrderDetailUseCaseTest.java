package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.vieworderdetail.ViewOrderDetailCommand;
import com.twohands.commerce_service.application.order.vieworderdetail.ViewOrderDetailUseCase;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderStatusHistoryEntry;
import com.twohands.commerce_service.domain.order.ShippingAddressSnapshot;
import com.twohands.commerce_service.domain.order.ViewOrderDetailItem;
import com.twohands.commerce_service.domain.order.ViewOrderDetailPaymentSummary;
import com.twohands.commerce_service.domain.order.ViewOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
import com.twohands.commerce_service.domain.order.ViewOrderDetailShipment;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
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
class ViewOrderDetailUseCaseTest {

    @Mock
    private ViewOrderDetailRepository viewOrderDetailRepository;

    @InjectMocks
    private ViewOrderDetailUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T14:00:00Z");

    @Test
    void shouldReturnOrderDetailForOwnedOrder() {
        ViewOrderDetailResult expected = sampleResult();
        when(viewOrderDetailRepository.findByOrderIdAndBuyerId(orderId, buyerId))
                .thenReturn(Optional.of(expected));

        ViewOrderDetailResult result = useCase.execute(new ViewOrderDetailCommand(buyerId, orderId));

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().productNameSnapshot()).isEqualTo("iPhone 15");
        assertThat(result.items().getFirst().unitPriceSnapshot()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(result.shipments()).hasSize(1);
        assertThat(result.shipments().getFirst().shippingAddress().fullAddress())
                .isEqualTo("123 Nguyen Van Linh, Q.7, TP.HCM");
        assertThat(result.payment().status()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void shouldRejectWhenOrderNotFoundOrNotOwned() {
        when(viewOrderDetailRepository.findByOrderIdAndBuyerId(orderId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewOrderDetailCommand(buyerId, orderId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    private ViewOrderDetailResult sampleResult() {
        UUID shipmentId = UUID.randomUUID();
        return new ViewOrderDetailResult(
                orderId,
                buyerId,
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.PAYOS,
                BigDecimal.valueOf(1_050_000),
                BigDecimal.valueOf(1_050_000),
                now,
                now,
                null,
                new ViewOrderDetailPaymentSummary(
                        UUID.randomUUID(),
                        PaymentStatus.PAID,
                        PaymentMethod.PAYOS,
                        BigDecimal.valueOf(1_050_000),
                        "VND",
                        now,
                        null,
                        null,
                        List.of()
                ),
                List.of(new ViewOrderDetailItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        shipmentId,
                        1,
                        OrderItemStatus.DELIVERED,
                        new BigDecimal("1000000"),
                        new BigDecimal("1000000"),
                        "SKU-001",
                        "iPhone 15",
                        "http://localhost:9000/2hands-commerce-product/p1.jpg",
                        "{\"color\":\"black\"}",
                        "Tech Shop",
                        new BigDecimal("50000"),
                        null
                )),
                List.of(new ViewOrderDetailShipment(
                        shipmentId,
                        UUID.randomUUID(),
                        ShipmentStatus.DELIVERED,
                        "GHN",
                        "TRACK-001",
                        new BigDecimal("50000"),
                        ShipmentType.STANDARD,
                        null,
                        now,
                        now,
                        new ShippingAddressSnapshot(
                                "Nguyen Van A",
                                "0901234567",
                                "79",
                                "760",
                                "26734",
                                "123 Nguyen Van Linh",
                                "123 Nguyen Van Linh, Q.7, TP.HCM"
                        ),
                        List.of()
                )),
                List.of(new OrderStatusHistoryEntry(
                        OrderStatus.AWAITING_PAYMENT,
                        OrderStatus.PROCESSING,
                        "SYSTEM",
                        null,
                        now
                ))
        );
    }
}
