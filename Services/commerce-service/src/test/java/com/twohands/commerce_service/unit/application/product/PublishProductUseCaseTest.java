package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductPublishedOutboxService;
import com.twohands.commerce_service.application.product.publishproduct.PublishProductCommand;
import com.twohands.commerce_service.application.product.publishproduct.PublishProductResult;
import com.twohands.commerce_service.application.product.publishproduct.PublishProductUseCase;
import com.twohands.commerce_service.common.media.CommerceProductMediaUrlValidator;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductPublishSnapshot;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.PublishProductRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishProductUseCaseTest {

    @Mock
    private PublishProductRepository publishProductRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductPublishedOutboxService productPublishedOutboxService;

    @Mock
    private CommerceProductMediaUrlValidator productMediaUrlValidator;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-21T10:00:00Z"), ZoneOffset.UTC);

    private PublishProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PublishProductUseCase(
                publishProductRepository,
                outboxEventRepository,
                productPublishedOutboxService,
                productMediaUrlValidator,
                clock
        );
    }

    private final UUID sellerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @Test
    void shouldPublishDraftProductAsActiveWhenStockAvailable() {
        ProductPublishSnapshot draft = readyDraftSnapshot(5);
        ProductPublishSnapshot published = withStatus(draft, ProductStatus.ACTIVE, now);

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(draft));
        doNothing().when(productMediaUrlValidator).validateRequiredProductMedia(any());
        when(publishProductRepository.updateStatus(productId, ProductStatus.ACTIVE, now)).thenReturn(published);
        when(productPublishedOutboxService.build(
                eq(productId), eq(shopId), eq(sellerId), eq(ProductStatus.DRAFT), eq(ProductStatus.ACTIVE), eq(5), eq(now)))
                .thenReturn(sampleOutboxEvent());

        PublishProductResult result = useCase.execute(new PublishProductCommand(sellerId, productId));

        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.alreadyPublished()).isFalse();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldPublishDraftProductAsOutOfStockWhenStockZero() {
        ProductPublishSnapshot draft = readyDraftSnapshot(0);
        ProductPublishSnapshot published = withStatus(draft, ProductStatus.OUT_OF_STOCK, now);

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(draft));
        doNothing().when(productMediaUrlValidator).validateRequiredProductMedia(any());
        when(publishProductRepository.updateStatus(productId, ProductStatus.OUT_OF_STOCK, now)).thenReturn(published);
        when(productPublishedOutboxService.build(any(), any(), any(), any(), any(), any(Integer.class), any()))
                .thenReturn(sampleOutboxEvent());

        PublishProductResult result = useCase.execute(new PublishProductCommand(sellerId, productId));

        assertThat(result.status()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }

    @Test
    void shouldResumePausedProduct() {
        ProductPublishSnapshot paused = withStatus(readyDraftSnapshot(3), ProductStatus.PAUSED, now);
        ProductPublishSnapshot published = withStatus(paused, ProductStatus.ACTIVE, now);

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(paused));
        doNothing().when(productMediaUrlValidator).validateRequiredProductMedia(any());
        when(publishProductRepository.updateStatus(productId, ProductStatus.ACTIVE, now)).thenReturn(published);
        when(productPublishedOutboxService.build(any(), any(), any(), any(), any(), any(Integer.class), any()))
                .thenReturn(sampleOutboxEvent());

        PublishProductResult result = useCase.execute(new PublishProductCommand(sellerId, productId));

        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void shouldReturnIdempotentWhenAlreadyActiveWithStock() {
        ProductPublishSnapshot active = withStatus(readyDraftSnapshot(2), ProductStatus.ACTIVE, now);

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(active));

        PublishProductResult result = useCase.execute(new PublishProductCommand(sellerId, productId));

        assertThat(result.alreadyPublished()).isTrue();
        verify(publishProductRepository, never()).updateStatus(any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenShopNotActive() {
        ProductPublishSnapshot draft = withShopStatus(readyDraftSnapshot(1), ShopStatus.SUSPENDED.name());

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> useCase.execute(new PublishProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_NOT_OPERATING);
    }

    @Test
    void shouldRejectWhenActivePriceMissing() {
        ProductPublishSnapshot draft = withActivePrice(readyDraftSnapshot(1), null);

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> useCase.execute(new PublishProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACTIVE_PRICE_MISSING);
    }

    @Test
    void shouldRejectWhenInventoryMissing() {
        ProductPublishSnapshot draft = withStockQuantity(readyDraftSnapshot(1), null);

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> useCase.execute(new PublishProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PRODUCT_STATUS);
    }

    @Test
    void shouldRejectArchivedProduct() {
        ProductPublishSnapshot archived = withStatus(readyDraftSnapshot(1), ProductStatus.ARCHIVED, now);

        when(publishProductRepository.findForSeller(productId, sellerId, now)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> useCase.execute(new PublishProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PRODUCT_STATUS);
    }

    private ProductPublishSnapshot readyDraftSnapshot(int stockQuantity) {
        return new ProductPublishSnapshot(
                productId,
                sellerId,
                shopId,
                "iPhone 14",
                "Like new",
                "USED",
                200,
                ProductStatus.DRAFT,
                ShopStatus.ACTIVE.name(),
                true,
                BigDecimal.valueOf(100),
                stockQuantity,
                "http://localhost:9000/2hands-commerce-product/img.jpg",
                now
        );
    }

    private ProductPublishSnapshot withStatus(
            ProductPublishSnapshot snapshot,
            ProductStatus status,
            Instant updatedAt
    ) {
        return copy(snapshot, status, snapshot.shopStatus(), snapshot.activePrice(), snapshot.stockQuantity(), updatedAt);
    }

    private ProductPublishSnapshot withShopStatus(ProductPublishSnapshot snapshot, String shopStatus) {
        return copy(snapshot, snapshot.status(), shopStatus, snapshot.activePrice(), snapshot.stockQuantity(), snapshot.updatedAt());
    }

    private ProductPublishSnapshot withActivePrice(ProductPublishSnapshot snapshot, BigDecimal activePrice) {
        return copy(snapshot, snapshot.status(), snapshot.shopStatus(), activePrice, snapshot.stockQuantity(), snapshot.updatedAt());
    }

    private ProductPublishSnapshot withStockQuantity(ProductPublishSnapshot snapshot, Integer stockQuantity) {
        return copy(snapshot, snapshot.status(), snapshot.shopStatus(), snapshot.activePrice(), stockQuantity, snapshot.updatedAt());
    }

    private ProductPublishSnapshot copy(
            ProductPublishSnapshot snapshot,
            ProductStatus status,
            String shopStatus,
            BigDecimal activePrice,
            Integer stockQuantity,
            Instant updatedAt
    ) {
        return new ProductPublishSnapshot(
                snapshot.productId(),
                snapshot.sellerId(),
                snapshot.shopId(),
                snapshot.title(),
                snapshot.description(),
                snapshot.condition(),
                snapshot.weightGram(),
                status,
                shopStatus,
                snapshot.categoryActive(),
                activePrice,
                stockQuantity,
                snapshot.primaryMediaUrl(),
                updatedAt
        );
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductPublishedOutboxService.EVENT_TYPE,
                "product:test:published",
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
