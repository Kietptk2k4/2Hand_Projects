package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.uploadproductmedia.CreateProductMediaUploadUrlCommand;
import com.twohands.commerce_service.application.product.uploadproductmedia.CreateProductMediaUploadUrlResult;
import com.twohands.commerce_service.application.product.uploadproductmedia.CreateProductMediaUploadUrlUseCase;
import com.twohands.commerce_service.application.product.uploadproductmedia.CreateProductMediaUploadUrlValidationService;
import com.twohands.commerce_service.application.product.uploadproductmedia.ProductMediaUploadIntent;
import com.twohands.commerce_service.application.product.uploadproductmedia.ProductMediaUploadStoragePort;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductMediaProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductMediaRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductMediaUploadUrlUseCaseTest {

    @Mock
    private UpdateProductMediaRepository updateProductMediaRepository;

    @Mock
    private ProductMediaUploadStoragePort productMediaUploadStoragePort;

    private CreateProductMediaUploadUrlUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(true);
        properties.setProductMediaMaxFileSizeBytes(5_242_880L);
        properties.setPresignedUrlTtlSeconds(900);

        useCase = new CreateProductMediaUploadUrlUseCase(
                new CreateProductMediaUploadUrlValidationService(properties),
                updateProductMediaRepository,
                properties,
                productMediaUploadStoragePort
        );
    }

    @Test
    void shouldIssueUploadUrlForOwnedProduct() {
        Instant expiresAt = Instant.parse("2026-05-21T10:15:00Z");
        when(updateProductMediaRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(new UpdateProductMediaProductRef(
                        productId, sellerId, shopId, ProductStatus.DRAFT
                )));
        when(productMediaUploadStoragePort.createUploadIntent(
                eq(sellerId),
                eq(productId),
                eq("image/jpeg"),
                eq(CreateProductMediaUploadUrlValidationService.MEDIA_KIND_PRODUCT_IMAGE),
                any()
        )).thenReturn(new ProductMediaUploadIntent(
                "https://minio/upload",
                "products/x/y/images/a.jpg",
                "http://localhost:9000/2hands-commerce-product/products/x/y/images/a.jpg",
                CreateProductMediaUploadUrlValidationService.MEDIA_KIND_PRODUCT_IMAGE,
                expiresAt
        ));

        CreateProductMediaUploadUrlResult result = useCase.execute(new CreateProductMediaUploadUrlCommand(
                sellerId,
                productId,
                "image/jpeg",
                1024L,
                CreateProductMediaUploadUrlValidationService.MEDIA_KIND_PRODUCT_IMAGE
        ));

        assertThat(result.uploadUrl()).isEqualTo("https://minio/upload");
        assertThat(result.mediaUrl()).contains("2hands-commerce-product");
    }

    @Test
    void shouldRejectWhenObjectStorageDisabled() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(false);
        CreateProductMediaUploadUrlUseCase disabledUseCase = new CreateProductMediaUploadUrlUseCase(
                new CreateProductMediaUploadUrlValidationService(properties),
                updateProductMediaRepository,
                properties,
                productMediaUploadStoragePort
        );

        when(updateProductMediaRepository.findProductByIdAndSellerId(productId, sellerId))
                .thenReturn(Optional.of(new UpdateProductMediaProductRef(
                        productId, sellerId, shopId, ProductStatus.DRAFT
                )));

        assertThatThrownBy(() -> disabledUseCase.execute(new CreateProductMediaUploadUrlCommand(
                sellerId,
                productId,
                "image/jpeg",
                1024L,
                CreateProductMediaUploadUrlValidationService.MEDIA_KIND_PRODUCT_IMAGE
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.OBJECT_STORAGE_UNAVAILABLE);
    }
}
