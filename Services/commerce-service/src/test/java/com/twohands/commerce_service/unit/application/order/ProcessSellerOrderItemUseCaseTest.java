package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.common.SellerOrderItemProcessingOutboxService;
import com.twohands.commerce_service.application.order.processsellerorderitem.ProcessSellerOrderItemCommand;
import com.twohands.commerce_service.application.order.processsellerorderitem.ProcessSellerOrderItemUseCase;
import com.twohands.commerce_service.domain.order.ProcessSellerOrderItemRepository;
import com.twohands.commerce_service.domain.order.ProcessSellerOrderItemResult;
import com.twohands.commerce_service.domain.order.SellerOrderItemLine;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.CreateShipmentOrderContext;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class ProcessSellerOrderItemUseCaseTest {

    @Mock
    private ProcessSellerOrderItemRepository processSellerOrderItemRepository;

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private SellerOrderItemProcessingOutboxService sellerOrderItemProcessingOutboxService;

    private ProcessSellerOrderItemUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID orderItemId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ProcessSellerOrderItemUseCase(
                processSellerOrderItemRepository,
                sellerShopRepository,
                outboxEventRepository,
                sellerOrderItemProcessingOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void marksPendingItemsAsProcessing() {
        SellerOrderItemLine item = pendingItem();
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(processSellerOrderItemRepository.findOrderItemsBySellerAndIds(sellerId, List.of(orderItemId)))
                .thenReturn(List.of(item));
        when(processSellerOrderItemRepository.findOrderContext(orderId)).thenReturn(Optional.of(processingOrder()));
        when(processSellerOrderItemRepository.markPendingItemsProcessing(sellerId, List.of(orderItemId), now))
                .thenReturn(1);
        when(sellerOrderItemProcessingOutboxService.build(eq(orderId), eq(sellerId), any(), eq(now)))
                .thenReturn(sampleOutbox());

        ProcessSellerOrderItemResult result = useCase.execute(
                new ProcessSellerOrderItemCommand(sellerId, List.of(orderItemId))
        );

        assertThat(result.newlyProcessedCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().newlyProcessed()).isTrue();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void isIdempotentWhenAlreadyProcessing() {
        SellerOrderItemLine item = new SellerOrderItemLine(
                orderItemId, orderId, sellerId, "PROCESSING", "Phone", 1
        );
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(processSellerOrderItemRepository.findOrderItemsBySellerAndIds(sellerId, List.of(orderItemId)))
                .thenReturn(List.of(item));
        when(processSellerOrderItemRepository.findOrderContext(orderId)).thenReturn(Optional.of(processingOrder()));
        when(processSellerOrderItemRepository.markPendingItemsProcessing(sellerId, List.of(orderItemId), now))
                .thenReturn(0);

        ProcessSellerOrderItemResult result = useCase.execute(
                new ProcessSellerOrderItemCommand(sellerId, List.of(orderItemId))
        );

        assertThat(result.alreadyProcessingCount()).isEqualTo(1);
        assertThat(result.newlyProcessedCount()).isZero();
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void rejectsPayosOrderNotPaid() {
        SellerOrderItemLine item = pendingItem();
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(processSellerOrderItemRepository.findOrderItemsBySellerAndIds(sellerId, List.of(orderItemId)))
                .thenReturn(List.of(item));
        when(processSellerOrderItemRepository.findOrderContext(orderId)).thenReturn(Optional.of(
                new CreateShipmentOrderContext(
                        orderId, UUID.randomUUID(), "PROCESSING",
                        PaymentMethod.PAYOS, PaymentStatus.PENDING, BigDecimal.TEN
                )
        ));

        assertThatThrownBy(() -> useCase.execute(new ProcessSellerOrderItemCommand(sellerId, List.of(orderItemId))))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_STATE);
    }

    @Test
    void rejectsItemNotOwned() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(processSellerOrderItemRepository.findOrderItemsBySellerAndIds(sellerId, List.of(orderItemId)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new ProcessSellerOrderItemCommand(sellerId, List.of(orderItemId))))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    private SellerOrderItemLine pendingItem() {
        return new SellerOrderItemLine(orderItemId, orderId, sellerId, "PENDING", "Phone", 1);
    }

    private CreateShipmentOrderContext processingOrder() {
        return new CreateShipmentOrderContext(
                orderId, UUID.randomUUID(), "PROCESSING",
                PaymentMethod.COD, PaymentStatus.PENDING, BigDecimal.TEN
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                SellerOrderItemProcessingOutboxService.EVENT_TYPE,
                "order:test",
                orderId,
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
