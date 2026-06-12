package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductRestoredOutboxService;
import com.twohands.commerce_service.application.product.restoreproductbyadmin.RestoreProductByAdminCommand;
import com.twohands.commerce_service.application.product.restoreproductbyadmin.RestoreProductByAdminUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductForRestore;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.RestoreProductByAdminRepository;
import com.twohands.commerce_service.domain.product.RestoreProductByAdminResult;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestoreProductByAdminUseCaseTest {

    @Mock
    private RestoreProductByAdminRepository restoreProductByAdminRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductRestoredOutboxService productRestoredOutboxService;

    private RestoreProductByAdminUseCase useCase;

    private final UUID adminId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new RestoreProductByAdminUseCase(
                restoreProductByAdminRepository,
                outboxEventRepository,
                productRestoredOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void restoresRemovedProductWithStockToActive() {
        ProductForRestore product = removedProduct(1L);
        when(restoreProductByAdminRepository.findById(productId)).thenReturn(Optional.of(product));
        when(restoreProductByAdminRepository.updateStatusFromRemoved(productId, ProductStatus.ACTIVE, now))
                .thenReturn(true);
        when(productRestoredOutboxService.build(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        RestoreProductByAdminResult result = useCase.execute(
                new RestoreProductByAdminCommand(adminId, productId, "Appeal approved")
        );

        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.previousStatus()).isEqualTo(ProductStatus.REMOVED);
        assertThat(result.alreadyRestored()).isFalse();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void restoresRemovedProductWithoutStockToOutOfStock() {
        ProductForRestore product = removedProduct(0L);
        when(restoreProductByAdminRepository.findById(productId)).thenReturn(Optional.of(product));
        when(restoreProductByAdminRepository.updateStatusFromRemoved(productId, ProductStatus.OUT_OF_STOCK, now))
                .thenReturn(true);
        when(productRestoredOutboxService.build(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        RestoreProductByAdminResult result = useCase.execute(
                new RestoreProductByAdminCommand(adminId, productId, "Appeal approved")
        );

        assertThat(result.status()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }

    @Test
    void returnsIdempotentWhenAlreadyRestored() {
        ProductForRestore product = new ProductForRestore(
                productId, sellerId, shopId, "Phone", ProductStatus.ACTIVE, 1L, ShopStatus.ACTIVE
        );
        when(restoreProductByAdminRepository.findById(productId)).thenReturn(Optional.of(product));

        RestoreProductByAdminResult result = useCase.execute(
                new RestoreProductByAdminCommand(adminId, productId, "Already restored")
        );

        assertThat(result.alreadyRestored()).isTrue();
        verify(restoreProductByAdminRepository, never()).updateStatusFromRemoved(any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void rejectsWhenShopSuspended() {
        ProductForRestore product = new ProductForRestore(
                productId, sellerId, shopId, "Phone", ProductStatus.REMOVED, 1L, ShopStatus.SUSPENDED
        );
        when(restoreProductByAdminRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(new RestoreProductByAdminCommand(adminId, productId, "reason")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PRODUCT_STATUS);
    }

    @Test
    void rejectsBlankReason() {
        assertThatThrownBy(() -> useCase.execute(new RestoreProductByAdminCommand(adminId, productId, "  ")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private ProductForRestore removedProduct(long stockQuantity) {
        return new ProductForRestore(
                productId, sellerId, shopId, "Phone", ProductStatus.REMOVED, stockQuantity, ShopStatus.ACTIVE
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductRestoredOutboxService.EVENT_TYPE,
                "product:test:restored",
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
