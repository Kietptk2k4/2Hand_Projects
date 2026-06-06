package com.twohands.commerce_service.unit.common.media;

import com.twohands.commerce_service.common.media.ProductMediaContentValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.product.ProductMediaKind;
import com.twohands.commerce_service.domain.product.ProductMediaType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductMediaContentValidatorTest {

    private ProductMediaContentValidator validator;

    @BeforeEach
    void setUp() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setProductMediaMaxFileSizeBytes(5_242_880L);
        properties.setProductMediaMaxVideoFileSizeBytes(52_428_800L);
        validator = new ProductMediaContentValidator(properties);
    }

    @Test
    void shouldInferImageFromUrl() {
        assertThat(validator.inferMediaTypeFromUrl(
                "http://localhost:9000/2hands-commerce-product/products/a/b/images/1.jpg"
        )).isEqualTo(ProductMediaType.IMAGE);
    }

    @Test
    void shouldInferVideoFromUrl() {
        assertThat(validator.inferMediaTypeFromUrl(
                "http://localhost:9000/2hands-commerce-product/products/a/b/videos/1.mp4"
        )).isEqualTo(ProductMediaType.VIDEO);
    }

    @Test
    void shouldRejectImageContentTypeForVideoKind() {
        assertThatThrownBy(() -> validator.validateContentTypeForMediaKind("image/jpeg", ProductMediaKind.PRODUCT_VIDEO))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MEDIA_TYPE);
    }

    @Test
    void shouldEnforceVideoSizeLimit() {
        assertThatThrownBy(() -> validator.validateUploadFile("video/mp4", 60_000_000L))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MEDIA_SIZE);
    }

    @Test
    void shouldReturnVideoMaxBytes() {
        assertThat(validator.maxBytesForContentType("video/webm")).isEqualTo(52_428_800L);
    }
}