package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductCommand;
import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductResult;
import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductUseCase;
import com.twohands.commerce_service.application.product.common.ProductArchivedOutboxService;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
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
class ArchiveProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductArchivedOutboxService productArchivedOutboxService;

    @InjectMocks
    private ArchiveProductUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @Test
    void shouldArchiveActiveProductAndInvalidateCartItems() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.ACTIVE, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.markInvalidByProductId(eq(productId), any(Instant.class))).thenReturn(3);
        when(productArchivedOutboxService.build(
                eq(productId), eq(shopId), eq(sellerId), eq(ProductStatus.ACTIVE), any(Instant.class)))
                .thenReturn(sampleOutboxEvent());

        ArchiveProductResult result = useCase.execute(new ArchiveProductCommand(sellerId, productId));

        assertThat(result.status()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(result.cartItemsInvalidated()).isEqualTo(3);
        assertThat(result.alreadyArchived()).isFalse();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().status()).isEqualTo(ProductStatus.ARCHIVED);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldReturnIdempotentResultWhenAlreadyArchived() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.ARCHIVED, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ArchiveProductResult result = useCase.execute(new ArchiveProductCommand(sellerId, productId));

        assertThat(result.alreadyArchived()).isTrue();
        assertThat(result.cartItemsInvalidated()).isZero();
        verify(productRepository, never()).save(any());
        verify(cartItemRepository, never()).markInvalidByProductId(any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenProductRemoved() {
        Product product = new Product(productId, sellerId, shopId, "Phone", ProductStatus.REMOVED, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(new ArchiveProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_REMOVED);
    }

    @Test
    void shouldRejectWhenSellerDoesNotOwnProduct() {
        UUID otherSeller = UUID.randomUUID();
        Product product = new Product(productId, otherSeller, shopId, "Phone", ProductStatus.ACTIVE, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(new ArchiveProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ArchiveProductCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void shouldArchiveDraftProduct() {
        Product product = new Product(productId, sellerId, shopId, "Draft item", ProductStatus.DRAFT, now);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.markInvalidByProductId(eq(productId), any(Instant.class))).thenReturn(0);
        when(productArchivedOutboxService.build(
                eq(productId), eq(shopId), eq(sellerId), eq(ProductStatus.DRAFT), any(Instant.class)))
                .thenReturn(sampleOutboxEvent());

        ArchiveProductResult result = useCase.execute(new ArchiveProductCommand(sellerId, productId));

        assertThat(result.status()).isEqualTo(ProductStatus.ARCHIVED);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductArchivedOutboxService.EVENT_TYPE,
                "product:" + productId + ":archived",
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
