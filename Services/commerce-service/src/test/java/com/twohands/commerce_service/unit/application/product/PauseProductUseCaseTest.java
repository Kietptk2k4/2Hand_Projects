package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductPausedOutboxService;
import com.twohands.commerce_service.application.product.pauseproduct.PauseProductCommand;
import com.twohands.commerce_service.application.product.pauseproduct.PauseProductResult;
import com.twohands.commerce_service.application.product.pauseproduct.PauseProductUseCase;
import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.Product;
import com.twohands.commerce_service.domain.product.ProductRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
class PauseProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SyncCartItemStatusUseCase syncCartItemStatusUseCase;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductPausedOutboxService productPausedOutboxService;

    @InjectMocks
    private PauseProductUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @Test
    void shouldPauseActiveProductAndInvalidateCartItems() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.ACTIVE, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(syncCartItemStatusUseCase.syncByProductId(productId))
                .thenReturn(new SyncCartItemStatusResult(2, 2, 0, 0));
        when(productPausedOutboxService.build(
                eq(productId), eq(shopId), eq(sellerId), eq(ProductStatus.ACTIVE), any(Instant.class)))
                .thenReturn(sampleOutboxEvent());

        PauseProductResult result = useCase.execute(new PauseProductCommand(sellerId, productId));

        assertThat(result.status()).isEqualTo(ProductStatus.PAUSED);
        assertThat(result.cartItemsInvalidated()).isEqualTo(2);
        assertThat(result.alreadyPaused()).isFalse();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().status()).isEqualTo(ProductStatus.PAUSED);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldPauseOutOfStockProduct() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.OUT_OF_STOCK, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(syncCartItemStatusUseCase.syncByProductId(productId))
                .thenReturn(new SyncCartItemStatusResult(0, 0, 0, 0));
        when(productPausedOutboxService.build(any(), any(), any(), any(), any())).thenReturn(sampleOutboxEvent());

        PauseProductResult result = useCase.execute(new PauseProductCommand(sellerId, productId));

        assertThat(result.status()).isEqualTo(ProductStatus.PAUSED);
    }

    @Test
    void shouldReturnIdempotentResultWhenAlreadyPaused() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.PAUSED, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        PauseProductResult result = useCase.execute(new PauseProductCommand(sellerId, productId));

        assertThat(result.alreadyPaused()).isTrue();
        verify(productRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldRejectDraftProduct() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.DRAFT, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(new PauseProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PRODUCT_STATUS);
    }

    @Test
    void shouldRejectArchivedProduct() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.ARCHIVED, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(new PauseProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PRODUCT_STATUS);
    }

    @Test
    void shouldRejectWhenNotOwned() {
        Product product = new Product(productId, UUID.randomUUID(), shopId, "Phone", ProductStatus.ACTIVE, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(new PauseProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductPausedOutboxService.EVENT_TYPE,
                "product:test:paused",
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
