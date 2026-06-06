package com.twohands.commerce_service.unit.common.media;

import com.twohands.commerce_service.common.media.CommerceProductMediaUrlValidator;
import com.twohands.commerce_service.common.media.ProductMediaContentValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommerceProductMediaUrlValidatorTest {

    private CommerceProductMediaUrlValidator validator;

    @BeforeEach
    void setUp() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(true);
        properties.setProductBucket("2hands-commerce-product");
        properties.setPublicUrl("http://localhost:9000");
        properties.setProductMediaMaxCount(10);
        properties.setProductMediaMaxVideoCount(1);
        ProductMediaContentValidator contentValidator = new ProductMediaContentValidator(properties);
        validator = new CommerceProductMediaUrlValidator(properties, contentValidator);
    }

    @Test
    void shouldRejectVideoOnlyComposition() {
        String videoUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/videos/1.mp4";

        assertThatThrownBy(() -> validator.validateProductMediaComposition(List.of(videoUrl)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectMoreThanOneVideo() {
        String imageUrl = "http://localhost:9000/2hands-commerce-product/products/a/b/images/1.jpg";
        String video1 = "http://localhost:9000/2hands-commerce-product/products/a/b/videos/1.mp4";
        String video2 = "http://localhost:9000/2hands-commerce-product/products/a/b/videos/2.mp4";

        assertThatThrownBy(() -> validator.validateProductMediaComposition(List.of(imageUrl, video1, video2)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}