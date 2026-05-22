package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductUpdatedOutboxService;
import com.twohands.commerce_service.application.product.updateproduct.UpdateProductCommand;
import com.twohands.commerce_service.application.product.updateproduct.UpdateProductUseCase;
import com.twohands.commerce_service.domain.catalog.ProductCategoryRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductDraft;
import com.twohands.commerce_service.domain.product.UpdateProductRepository;
import com.twohands.commerce_service.domain.product.UpdateProductResult;
import com.twohands.commerce_service.domain.product.UpdateProductSnapshot;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class UpdateProductUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private UpdateProductRepository updateProductRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductUpdatedOutboxService productUpdatedOutboxService;

    private UpdateProductUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductUseCase(
                sellerShopRepository,
                productCategoryRepository,
                updateProductRepository,
                outboxEventRepository,
                productUpdatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldUpdateEditableProduct() {
        stubActiveShop();
        when(updateProductRepository.findByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(existingSnapshot(ProductStatus.DRAFT)));
        when(productCategoryRepository.existsActiveById(categoryId)).thenReturn(true);
        when(updateProductRepository.update(any(UpdateProductDraft.class), eq(now)))
                .thenReturn(updatedResult(ProductStatus.DRAFT));
        when(productUpdatedOutboxService.build(any(), any(), any(), any(), any()))
                .thenReturn(sampleOutboxEvent());

        UpdateProductResult result = useCase.execute(updateCommand());

        assertThat(result.title()).isEqualTo("Updated Phone");
        assertThat(result.categoryId()).isEqualTo(categoryId);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRejectRemovedProduct() {
        stubActiveShop();
        when(updateProductRepository.findByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(existingSnapshot(ProductStatus.REMOVED)));

        assertThatThrownBy(() -> useCase.execute(updateCommand()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_REMOVED);

        verify(updateProductRepository, never()).update(any(), any());
    }

    @Test
    void shouldRejectArchivedProduct() {
        stubActiveShop();
        when(updateProductRepository.findByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(existingSnapshot(ProductStatus.ARCHIVED)));

        assertThatThrownBy(() -> useCase.execute(updateCommand()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PRODUCT_STATUS);
    }

    @Test
    void shouldRejectWhenProductNotOwned() {
        stubActiveShop();
        when(updateProductRepository.findByIdAndSellerId(productId, sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(updateCommand()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void shouldRejectInactiveCategory() {
        stubActiveShop();
        when(updateProductRepository.findByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(existingSnapshot(ProductStatus.ACTIVE)));
        when(productCategoryRepository.existsActiveById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(updateCommand()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    private void stubActiveShop() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
    }

    private UpdateProductCommand updateCommand() {
        return new UpdateProductCommand(
                sellerId,
                productId,
                "PHONE",
                categoryId,
                null,
                "USED",
                "Updated Phone",
                "Updated description",
                600
        );
    }

    private UpdateProductSnapshot existingSnapshot(ProductStatus status) {
        return new UpdateProductSnapshot(
                productId,
                sellerId,
                shopId,
                status,
                "PHONE",
                UUID.randomUUID(),
                null,
                "USED",
                "Old Phone",
                "Old description",
                500,
                now.minusSeconds(3600)
        );
    }

    private UpdateProductResult updatedResult(ProductStatus status) {
        return new UpdateProductResult(
                productId,
                sellerId,
                shopId,
                status,
                "PHONE",
                categoryId,
                null,
                "USED",
                "Updated Phone",
                "Updated description",
                600,
                now.minusSeconds(3600),
                now
        );
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductUpdatedOutboxService.EVENT_TYPE,
                "product:test:updated",
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
