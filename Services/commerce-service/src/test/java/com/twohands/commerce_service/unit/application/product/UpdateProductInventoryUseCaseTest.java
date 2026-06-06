package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.application.product.common.ProductCatalogValidationService;
import com.twohands.commerce_service.application.product.common.ProductInventoryUpdatedOutboxService;
import com.twohands.commerce_service.domain.catalog.BrandRepository;
import com.twohands.commerce_service.application.product.updateproductinventory.UpdateProductInventoryCommand;
import com.twohands.commerce_service.application.product.updateproductinventory.UpdateProductInventoryUseCase;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductInventoryState;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryRepository;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryResult;
import com.twohands.commerce_service.domain.product.UpdateProductInventorySnapshot;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductInventoryUseCaseTest {

    @Mock
    private UpdateProductInventoryRepository updateProductInventoryRepository;

    @Mock
    private SyncCartItemStatusUseCase syncCartItemStatusUseCase;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductInventoryUpdatedOutboxService productInventoryUpdatedOutboxService;

    @Mock
    private BrandRepository brandRepository;

    private UpdateProductInventoryUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        ProductCatalogValidationService productCatalogValidationService =
                new ProductCatalogValidationService(brandRepository);
        useCase = new UpdateProductInventoryUseCase(
                updateProductInventoryRepository,
                syncCartItemStatusUseCase,
                productCatalogValidationService,
                outboxEventRepository,
                productInventoryUpdatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreateInventoryAndMarkOutOfStockForActiveProduct() {
        when(updateProductInventoryRepository.findProductForInventoryUpdate(eq(productId), eq(sellerId), any()))
                .thenReturn(Optional.of(snapshot(ProductStatus.ACTIVE, null)));
        when(updateProductInventoryRepository.upsertInventory(productId, 0, 0, now))
                .thenReturn(new ProductInventoryState(0, 0, 0));
        when(syncCartItemStatusUseCase.syncByProductId(productId))
                .thenReturn(new SyncCartItemStatusResult(2, 1, 1, 0));
        when(productInventoryUpdatedOutboxService.build(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyInt(), any()
        )).thenReturn(sampleOutboxEvent());

        UpdateProductInventoryResult result = useCase.execute(
                new UpdateProductInventoryCommand(sellerId, productId, 0, null)
        );

        assertThat(result.status()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        assertThat(result.statusChanged()).isTrue();
        assertThat(result.cartItemsSynced()).isEqualTo(1);
        verify(updateProductInventoryRepository).updateProductStatus(productId, ProductStatus.OUT_OF_STOCK, now);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRestoreOutOfStockToActiveWhenEligible() {
        when(updateProductInventoryRepository.findProductForInventoryUpdate(eq(productId), eq(sellerId), any()))
                .thenReturn(Optional.of(snapshot(ProductStatus.OUT_OF_STOCK, inventory(0, 2, 0))));
        when(updateProductInventoryRepository.upsertInventory(productId, 1, 2, now))
                .thenReturn(new ProductInventoryState(1, 2, 0));
        when(syncCartItemStatusUseCase.syncByProductId(productId))
                .thenReturn(SyncCartItemStatusResult.empty());
        when(productInventoryUpdatedOutboxService.build(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyInt(), any()
        )).thenReturn(sampleOutboxEvent());

        UpdateProductInventoryResult result = useCase.execute(
                new UpdateProductInventoryCommand(sellerId, productId, 1, null)
        );

        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(updateProductInventoryRepository).updateProductStatus(productId, ProductStatus.ACTIVE, now);
    }

    @Test
    void shouldRejectStockAboveOne() {
        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductInventoryCommand(sellerId, productId, 2, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(updateProductInventoryRepository, never()).findProductForInventoryUpdate(any(), any(), any());
    }

    @Test
    void shouldRejectStockBelowReservedQuantity() {
        when(updateProductInventoryRepository.findProductForInventoryUpdate(eq(productId), eq(sellerId), any()))
                .thenReturn(Optional.of(snapshot(ProductStatus.ACTIVE, inventory(1, 1, 1))));

        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductInventoryCommand(sellerId, productId, 0, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(updateProductInventoryRepository, never()).upsertInventory(any(), anyInt(), anyInt(), any());
    }

    @Test
    void shouldRejectRemovedProduct() {
        when(updateProductInventoryRepository.findProductForInventoryUpdate(eq(productId), eq(sellerId), any()))
                .thenReturn(Optional.of(snapshot(ProductStatus.REMOVED, inventory(1, 0, 0))));

        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductInventoryCommand(sellerId, productId, 1, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_REMOVED);
    }

    @Test
    void shouldRejectWhenProductNotOwned() {
        when(updateProductInventoryRepository.findProductForInventoryUpdate(eq(productId), eq(sellerId), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductInventoryCommand(sellerId, productId, 1, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private UpdateProductInventorySnapshot snapshot(ProductStatus status, ProductInventoryState inventory) {
        return new UpdateProductInventorySnapshot(
                productId,
                sellerId,
                shopId,
                status,
                "ACTIVE",
                true,
                BigDecimal.TEN,
                inventory
        );
    }

    private ProductInventoryState inventory(int stock, int lowStock, int reserved) {
        return new ProductInventoryState(stock, lowStock, reserved);
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductInventoryUpdatedOutboxService.EVENT_TYPE,
                "product:test:inventory:updated",
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
