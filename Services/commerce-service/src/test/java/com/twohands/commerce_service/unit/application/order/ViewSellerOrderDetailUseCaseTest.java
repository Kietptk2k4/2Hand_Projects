package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.viewsellerorderdetail.ViewSellerOrderDetailCommand;
import com.twohands.commerce_service.application.order.viewsellerorderdetail.ViewSellerOrderDetailUseCase;
import com.twohands.commerce_service.application.review.common.ReviewBuyerEnrichmentService;
import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.SellerOrderListEntry;
import com.twohands.commerce_service.domain.order.SellerOrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.ViewSellerOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewSellerOrderDetailResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewSellerOrderDetailUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ViewSellerOrderDetailRepository viewSellerOrderDetailRepository;

    @Mock
    private ReviewBuyerEnrichmentService reviewBuyerEnrichmentService;

    private ViewSellerOrderDetailUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewSellerOrderDetailUseCase(
                sellerShopRepository,
                viewSellerOrderDetailRepository,
                reviewBuyerEnrichmentService
        );
        lenient().when(reviewBuyerEnrichmentService.enrichBuyer(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (id == null) {
                return CommerceBuyerSummary.empty();
            }
            return new CommerceBuyerSummary(id, "Người mua", null);
        });
    }

    @Test
    void shouldReturnSellerOrderDetail() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(sampleShop()));
        when(viewSellerOrderDetailRepository.findSellerOrderDetail(sellerId, orderId))
                .thenReturn(Optional.of(sampleResult()));

        ViewSellerOrderDetailResult result = useCase.execute(new ViewSellerOrderDetailCommand(sellerId, orderId));

        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.sellerItemsSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(890000));
    }

    @Test
    void shouldThrowWhenSellerHasNoShop() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewSellerOrderDetailCommand(sellerId, orderId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SELLER_SHOP_NOT_FOUND);
    }

    @Test
    void shouldThrowWhenOrderNotFoundForSeller() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(sampleShop()));
        when(viewSellerOrderDetailRepository.findSellerOrderDetail(sellerId, orderId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewSellerOrderDetailCommand(sellerId, orderId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    private SellerShop sampleShop() {
        return new SellerShop(UUID.randomUUID(), sellerId, ShopStatus.ACTIVE);
    }

    private ViewSellerOrderDetailResult sampleResult() {
        SellerOrderListEntry item = new SellerOrderListEntry(
                UUID.randomUUID(),
                orderId,
                UUID.randomUUID(),
                1,
                450,
                BigDecimal.valueOf(890000),
                BigDecimal.valueOf(890000),
                BigDecimal.valueOf(30000),
                "Ao khoac denim",
                "http://localhost/img.jpg",
                OrderItemStatus.PENDING,
                now,
                now,
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.PAYOS,
                now,
                null,
                SellerOrderListShipmentSummary.empty()
        );
        return new ViewSellerOrderDetailResult(
                orderId,
                OrderStatus.PROCESSING,
                PaymentStatus.PAID,
                PaymentMethod.PAYOS,
                now,
                null,
                BigDecimal.valueOf(890000),
                BigDecimal.valueOf(30000),
                List.of(item),
                null,
                new CommerceBuyerSummary(buyerId, null, null),
                null,
                null
        );
    }
}
