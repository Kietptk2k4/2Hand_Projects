package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductRemovedOutboxService;
import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminCommand;
import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminUseCase;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductForModeration;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.RemoveProductByAdminRepository;
import com.twohands.commerce_service.domain.product.RemoveProductByAdminResult;
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
class RemoveProductByAdminUseCaseTest {

    @Mock
    private RemoveProductByAdminRepository removeProductByAdminRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductRemovedOutboxService productRemovedOutboxService;

    private RemoveProductByAdminUseCase useCase;

    private final UUID adminId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new RemoveProductByAdminUseCase(
                removeProductByAdminRepository,
                cartItemRepository,
                outboxEventRepository,
                productRemovedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void removesActiveProductAndInvalidatesCart() {
        ProductForModeration product = activeProduct();
        when(removeProductByAdminRepository.findById(productId)).thenReturn(Optional.of(product));
        when(removeProductByAdminRepository.updateStatusToRemoved(productId, ProductStatus.ACTIVE, now, "Policy violation"))
                .thenReturn(true);
        when(cartItemRepository.markInvalidByProductId(productId, now)).thenReturn(4);
        when(productRemovedOutboxService.build(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        RemoveProductByAdminResult result = useCase.execute(
                new RemoveProductByAdminCommand(adminId, productId, "Policy violation")
        );

        assertThat(result.status()).isEqualTo(ProductStatus.REMOVED);
        assertThat(result.previousStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.cartItemsInvalidated()).isEqualTo(4);
        assertThat(result.alreadyRemoved()).isFalse();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void returnsIdempotentWhenAlreadyRemoved() {
        ProductForModeration product = new ProductForModeration(
                productId, sellerId, shopId, "Phone", ProductStatus.REMOVED
        );
        when(removeProductByAdminRepository.findById(productId)).thenReturn(Optional.of(product));

        RemoveProductByAdminResult result = useCase.execute(
                new RemoveProductByAdminCommand(adminId, productId, "Already removed")
        );

        assertThat(result.alreadyRemoved()).isTrue();
        verify(removeProductByAdminRepository, never()).updateStatusToRemoved(any(), any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void rejectsBlankReason() {
        assertThatThrownBy(() -> useCase.execute(new RemoveProductByAdminCommand(adminId, productId, "  ")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void rejectsWhenProductNotFound() {
        when(removeProductByAdminRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RemoveProductByAdminCommand(adminId, productId, "reason")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private ProductForModeration activeProduct() {
        return new ProductForModeration(productId, sellerId, shopId, "Phone", ProductStatus.ACTIVE);
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductRemovedOutboxService.EVENT_TYPE,
                "product:test:removed",
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
