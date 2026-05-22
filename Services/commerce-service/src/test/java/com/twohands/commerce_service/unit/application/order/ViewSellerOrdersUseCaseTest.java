package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.viewsellerorders.ViewSellerOrdersCommand;
import com.twohands.commerce_service.application.order.viewsellerorders.ViewSellerOrdersUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.SellerOrderListEntry;
import com.twohands.commerce_service.domain.order.SellerOrderListPaymentSummary;
import com.twohands.commerce_service.domain.order.SellerOrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.ViewSellerOrdersRepository;
import com.twohands.commerce_service.domain.order.ViewSellerOrdersResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewSellerOrdersUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ViewSellerOrdersRepository viewSellerOrdersRepository;

    @InjectMocks
    private ViewSellerOrdersUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();

    @Test
    void shouldReturnSellerOrderItems() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(activeShop()));
        when(viewSellerOrdersRepository.countBySellerId(
                eq(sellerId),
                eq(Optional.empty()),
                eq(Optional.of(ShipmentStatus.PENDING))
        )).thenReturn(1L);
        when(viewSellerOrdersRepository.findBySellerId(
                eq(sellerId),
                eq(Optional.empty()),
                eq(Optional.of(ShipmentStatus.PENDING)),
                any()
        )).thenReturn(List.of(sampleEntry()));

        ViewSellerOrdersResult result = useCase.execute(
                new ViewSellerOrdersCommand(sellerId, 1, 20, null, "PENDING")
        );

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().itemStatus()).isEqualTo(OrderItemStatus.PROCESSING);
        assertThat(result.pagination().totalItems()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenSellerHasNoShop() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewSellerOrdersCommand(sellerId, 1, 20, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SELLER_SHOP_NOT_FOUND);

        verify(viewSellerOrdersRepository, never()).findBySellerId(any(), any(), any(), any());
    }

    @Test
    void shouldRejectInvalidItemStatusFilter() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(activeShop()));

        assertThatThrownBy(() -> useCase.execute(new ViewSellerOrdersCommand(sellerId, 1, 20, "BAD", null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private SellerShop activeShop() {
        return new SellerShop(shopId, sellerId, ShopStatus.ACTIVE);
    }

    private SellerOrderListEntry sampleEntry() {
        UUID orderId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-21T10:00:00Z");
        return new SellerOrderListEntry(
                orderItemId,
                orderId,
                UUID.randomUUID(),
                1,
                BigDecimal.valueOf(1_000_000),
                BigDecimal.valueOf(1_000_000),
                BigDecimal.valueOf(50_000),
                "iPhone 15",
                "http://localhost:9000/2hands-commerce-product/p1.jpg",
                OrderItemStatus.PROCESSING,
                now,
                now,
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.PAYOS,
                now,
                new SellerOrderListPaymentSummary(
                        UUID.randomUUID(),
                        PaymentStatus.PAID,
                        PaymentMethod.PAYOS,
                        BigDecimal.valueOf(1_050_000),
                        "VND"
                ),
                new SellerOrderListShipmentSummary(
                        shipmentId,
                        ShipmentStatus.PENDING,
                        ShipmentCarrier.GHN,
                        null,
                        "123 Nguyen Van Linh, Q.7, TP.HCM"
                )
        );
    }
}
