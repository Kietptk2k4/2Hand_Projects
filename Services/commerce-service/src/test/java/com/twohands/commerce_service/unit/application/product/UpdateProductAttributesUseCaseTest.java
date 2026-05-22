package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductAttributesUpdatedOutboxService;
import com.twohands.commerce_service.application.product.updateproductattributes.UpdateProductAttributesCommand;
import com.twohands.commerce_service.application.product.updateproductattributes.UpdateProductAttributesUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductAttributeItem;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesRepository;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesResult;
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
class UpdateProductAttributesUseCaseTest {

    @Mock
    private UpdateProductAttributesRepository updateProductAttributesRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductAttributesUpdatedOutboxService productAttributesUpdatedOutboxService;

    private UpdateProductAttributesUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductAttributesUseCase(
                updateProductAttributesRepository,
                outboxEventRepository,
                productAttributesUpdatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReplaceAttributesForEditableProduct() {
        when(updateProductAttributesRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.ACTIVE)));
        when(updateProductAttributesRepository.replaceAttributes(eq(productId), any()))
                .thenReturn(List.of(
                        new ProductAttributeItem("Color", "Black"),
                        new ProductAttributeItem("Size", "L")
                ));
        when(productAttributesUpdatedOutboxService.build(any(), any(), any(), any(), eq(2), any()))
                .thenReturn(sampleOutboxEvent());

        UpdateProductAttributesResult result = useCase.execute(new UpdateProductAttributesCommand(
                sellerId,
                productId,
                List.of(
                        new ProductAttributeItem("Color", "Black"),
                        new ProductAttributeItem("Size", "L")
                )
        ));

        assertThat(result.attributes()).hasSize(2);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldAllowEmptyAttributesToClearAll() {
        when(updateProductAttributesRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.DRAFT)));
        when(updateProductAttributesRepository.replaceAttributes(productId, List.of()))
                .thenReturn(List.of());
        when(productAttributesUpdatedOutboxService.build(any(), any(), any(), any(), eq(0), any()))
                .thenReturn(sampleOutboxEvent());

        UpdateProductAttributesResult result = useCase.execute(new UpdateProductAttributesCommand(
                sellerId,
                productId,
                List.of()
        ));

        assertThat(result.attributes()).isEmpty();
    }

    @Test
    void shouldRejectDuplicateAttributeNamesInRequest() {
        assertThatThrownBy(() -> useCase.execute(new UpdateProductAttributesCommand(
                sellerId,
                productId,
                List.of(
                        new ProductAttributeItem("Color", "Black"),
                        new ProductAttributeItem("color", "White")
                )
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(updateProductAttributesRepository, never()).replaceAttributes(any(), any());
    }

    @Test
    void shouldRejectBlankAttributeValue() {
        assertThatThrownBy(() -> useCase.execute(new UpdateProductAttributesCommand(
                sellerId,
                productId,
                List.of(new ProductAttributeItem("Color", "   "))
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectRemovedProduct() {
        when(updateProductAttributesRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.REMOVED)));

        assertThatThrownBy(() -> useCase.execute(new UpdateProductAttributesCommand(
                sellerId,
                productId,
                List.of(new ProductAttributeItem("Color", "Black"))
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_REMOVED);

        verify(updateProductAttributesRepository, never()).replaceAttributes(any(), any());
    }

    @Test
    void shouldRejectWhenProductNotOwned() {
        when(updateProductAttributesRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateProductAttributesCommand(
                sellerId,
                productId,
                List.of(new ProductAttributeItem("Color", "Black"))
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private UpdateProductAttributesProductRef productRef(ProductStatus status) {
        return new UpdateProductAttributesProductRef(productId, sellerId, shopId, status);
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductAttributesUpdatedOutboxService.EVENT_TYPE,
                "product:test:attributes:updated",
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
