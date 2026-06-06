package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.common.ProductCatalogValidationService;
import com.twohands.commerce_service.domain.catalog.BrandRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCatalogValidationServiceTest {

    @Mock
    private BrandRepository brandRepository;

    private ProductCatalogValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ProductCatalogValidationService(brandRepository);
    }

    @Test
    void shouldNormalizeAllowedCondition() {
        assertThat(validationService.normalizeCondition(" like_new "))
                .isEqualTo("LIKE_NEW");
    }

    @Test
    void shouldRejectInvalidCondition() {
        assertThatThrownBy(() -> validationService.normalizeCondition("NEW"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectInactiveBrand() {
        UUID brandId = UUID.randomUUID();
        when(brandRepository.existsActiveById(brandId)).thenReturn(false);

        assertThatThrownBy(() -> validationService.validateActiveBrandId(brandId))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BRAND_NOT_FOUND);
    }

    @Test
    void shouldRejectStockAboveOne() {
        assertThatThrownBy(() -> validationService.validateSecondHandStockQuantity(2))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}