package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductMediaUpdatedOutboxService;
import com.twohands.commerce_service.application.product.updateproductmedia.UpdateProductMediaCommand;
import com.twohands.commerce_service.application.product.updateproductmedia.UpdateProductMediaUseCase;
import com.twohands.commerce_service.common.media.CommerceProductMediaUrlValidator;
import com.twohands.commerce_service.common.media.ProductMediaContentValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductMediaItem;
import com.twohands.commerce_service.domain.product.ProductMediaType;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductMediaProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductMediaRepository;
import com.twohands.commerce_service.domain.product.UpdateProductMediaResult;
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
class UpdateProductMediaUseCaseTest {

    @Mock
    private UpdateProductMediaRepository updateProductMediaRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProductMediaUpdatedOutboxService productMediaUpdatedOutboxService;

    private CommerceProductMediaUrlValidator productMediaUrlValidator;
    private ProductMediaContentValidator productMediaContentValidator;
    private UpdateProductMediaUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(true);
        properties.setProductBucket("2hands-commerce-product");
        properties.setPublicUrl("http://localhost:9000");
        productMediaContentValidator = new ProductMediaContentValidator(properties);
        productMediaUrlValidator = new CommerceProductMediaUrlValidator(properties, productMediaContentValidator);

        useCase = new UpdateProductMediaUseCase(
                updateProductMediaRepository,
                outboxEventRepository,
                productMediaUpdatedOutboxService,
                productMediaUrlValidator,
                productMediaContentValidator,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReplaceMediaForEditableProduct() {
        String mediaUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/images/1.jpg";
        when(updateProductMediaRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.DRAFT)));
        when(updateProductMediaRepository.replaceMedia(eq(productId), any()))
                .thenReturn(List.of(new ProductMediaItem(mediaUrl, ProductMediaType.IMAGE.name(), 0)));
        when(productMediaUpdatedOutboxService.build(any(), any(), any(), any(), eq(1), any()))
                .thenReturn(sampleOutboxEvent());

        UpdateProductMediaResult result = useCase.execute(new UpdateProductMediaCommand(
                sellerId,
                productId,
                List.of(mediaUrl)
        ));

        assertThat(result.mediaUrls()).containsExactly(mediaUrl);
        assertThat(result.thumbnailUrl()).isEqualTo(mediaUrl);
        assertThat(result.hasMedia()).isTrue();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldUseFirstImageAsThumbnailWhenVideoPresent() {
        String imageUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/images/1.jpg";
        String videoUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/videos/1.mp4";
        when(updateProductMediaRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.DRAFT)));
        when(updateProductMediaRepository.replaceMedia(eq(productId), any()))
                .thenReturn(List.of(
                        new ProductMediaItem(imageUrl, ProductMediaType.IMAGE.name(), 0),
                        new ProductMediaItem(videoUrl, ProductMediaType.VIDEO.name(), 1)
                ));
        when(productMediaUpdatedOutboxService.build(any(), any(), any(), any(), eq(2), any()))
                .thenReturn(sampleOutboxEvent());

        UpdateProductMediaResult result = useCase.execute(new UpdateProductMediaCommand(
                sellerId,
                productId,
                List.of(imageUrl, videoUrl)
        ));

        assertThat(result.thumbnailUrl()).isEqualTo(imageUrl);
        assertThat(result.mediaUrls()).containsExactly(imageUrl, videoUrl);
    }

    @Test
    void shouldRejectVideoOnlyMediaList() {
        String videoUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/videos/1.mp4";

        assertThatThrownBy(() -> useCase.execute(new UpdateProductMediaCommand(
                sellerId,
                productId,
                List.of(videoUrl)
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(updateProductMediaRepository, never()).replaceMedia(any(), any());
    }

    @Test
    void shouldRejectRemovedProduct() {
        String mediaUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/images/1.jpg";
        when(updateProductMediaRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(productRef(ProductStatus.REMOVED)));

        assertThatThrownBy(() -> useCase.execute(new UpdateProductMediaCommand(
                sellerId,
                productId,
                List.of(mediaUrl)
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_REMOVED);

        verify(updateProductMediaRepository, never()).replaceMedia(any(), any());
    }

    @Test
    void shouldRejectInvalidBucketUrlWhenMinioEnabled() {
        assertThatThrownBy(() -> useCase.execute(new UpdateProductMediaCommand(
                sellerId,
                productId,
                List.of("https://picsum.photos/seed/test/600/600")
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MEDIA_URL);
    }

    @Test
    void shouldRejectDuplicateUrls() {
        String mediaUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/images/1.jpg";

        assertThatThrownBy(() -> useCase.execute(new UpdateProductMediaCommand(
                sellerId,
                productId,
                List.of(mediaUrl, mediaUrl)
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private UpdateProductMediaProductRef productRef(ProductStatus status) {
        return new UpdateProductMediaProductRef(productId, sellerId, shopId, status);
    }

    private OutboxEvent sampleOutboxEvent() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ProductMediaUpdatedOutboxService.EVENT_TYPE,
                "product:" + productId + ":media:updated",
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