package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductPriceUpdatedOutboxService;
import com.twohands.commerce_service.application.product.updateproductprice.UpdateProductPriceCommand;
import com.twohands.commerce_service.application.product.updateproductprice.UpdateProductPriceUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.OverlappingProductPrice;
import com.twohands.commerce_service.domain.product.ProductPriceRecord;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductPriceProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductPriceRepository;
import com.twohands.commerce_service.domain.product.UpdateProductPriceResult;
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
class UpdateProductPriceUseCaseTest {

    @Mock
    private UpdateProductPriceRepository updateProductPriceRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductPriceUpdatedOutboxService productPriceUpdatedOutboxService;

    private UpdateProductPriceUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");
    private final Instant startAt = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductPriceUseCase(
                updateProductPriceRepository,
                outboxEventRepository,
                productPriceUpdatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldInsertPriceAndClosePreviousActiveWindow() {
        UUID existingPriceId = UUID.randomUUID();
        when(updateProductPriceRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.ACTIVE)));
        when(updateProductPriceRepository.findOverlappingPrices(productId, startAt, null))
                .thenReturn(List.of(new OverlappingProductPrice(
                        existingPriceId,
                        startAt.minusSeconds(3600),
                        null
                )))
                .thenReturn(List.of());
        when(updateProductPriceRepository.closePricesAtStart(eq(productId), eq(List.of(existingPriceId)), eq(startAt)))
                .thenReturn(1);
        when(updateProductPriceRepository.insertPrice(any(), eq(now)))
                .thenReturn(createdPrice());
        when(productPriceUpdatedOutboxService.build(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutboxEvent());

        UpdateProductPriceResult result = useCase.execute(command(BigDecimal.valueOf(100), BigDecimal.valueOf(90)));

        assertThat(result.previousActivePriceClosed()).isTrue();
        assertThat(result.price().effectivePrice()).isEqualByComparingTo("90");
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRejectInvalidSalePrice() {
        assertThatThrownBy(() -> useCase.execute(command(BigDecimal.valueOf(100), BigDecimal.valueOf(150))))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(updateProductPriceRepository, never()).insertPrice(any(), any());
    }

    @Test
    void shouldRejectFutureOverlappingPrice() {
        when(updateProductPriceRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.DRAFT)));
        when(updateProductPriceRepository.findOverlappingPrices(productId, startAt, null))
                .thenReturn(List.of(new OverlappingProductPrice(
                        UUID.randomUUID(),
                        startAt.plusSeconds(60),
                        null
                )));

        assertThatThrownBy(() -> useCase.execute(command(BigDecimal.valueOf(100), null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRICE_WINDOW_OVERLAP);
    }

    @Test
    void shouldRejectRemovedProduct() {
        when(updateProductPriceRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.REMOVED)));

        assertThatThrownBy(() -> useCase.execute(command(BigDecimal.valueOf(100), null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_REMOVED);
    }

    @Test
    void shouldRejectWhenProductNotOwned() {
        when(updateProductPriceRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(BigDecimal.valueOf(100), null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private UpdateProductPriceCommand command(BigDecimal price, BigDecimal salePrice) {
        return new UpdateProductPriceCommand(sellerId, productId, price, salePrice, startAt, null);
    }

    private UpdateProductPriceProductRef productRef(ProductStatus status) {
        return new UpdateProductPriceProductRef(productId, sellerId, shopId, status);
    }

    private ProductPriceRecord createdPrice() {
        return new ProductPriceRecord(
                UUID.randomUUID(),
                productId,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(90),
                startAt,
                null,
                now
        );
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductPriceUpdatedOutboxService.EVENT_TYPE,
                "product:test:price:updated",
                productId,
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
