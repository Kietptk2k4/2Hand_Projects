package com.twohands.commerce_service.application.product.common;

import com.twohands.commerce_service.domain.catalog.BrandRepository;
import com.twohands.commerce_service.domain.product.ProductCondition;
import com.twohands.commerce_service.domain.product.SecondHandInventoryRules;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductCatalogValidationService {

    private static final String MVP_PRODUCT_TYPE = "PHYSICAL";

    private final BrandRepository brandRepository;

    public ProductCatalogValidationService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public String normalizeProductType(String productType) {
        String normalized = productType.trim().toUpperCase();
        if (!MVP_PRODUCT_TYPE.equals(normalized)) {
            throw fieldError("product_type", "must be " + MVP_PRODUCT_TYPE);
        }
        return normalized;
    }

    public String normalizeCondition(String condition) {
        String normalized = ProductCondition.normalize(condition);
        if (!ProductCondition.isAllowed(normalized)) {
            throw fieldError(
                    "condition",
                    "must be one of: LIKE_NEW, GOOD, FAIR, USED"
            );
        }
        return normalized;
    }

    public void validateActiveBrandId(UUID brandId) {
        if (brandId == null) {
            return;
        }
        if (!brandRepository.existsActiveById(brandId)) {
            throw new AppException(ErrorCode.BRAND_NOT_FOUND);
        }
    }

    public void validateSecondHandStockQuantity(int stockQuantity) {
        if (!SecondHandInventoryRules.isAllowedStockQuantity(stockQuantity)) {
            throw fieldError(
                    "stock_quantity",
                    "must be 0 or 1 for second-hand listings"
            );
        }
    }

    public void validateSecondHandLowStockThreshold(Integer lowStockThreshold) {
        if (lowStockThreshold == null) {
            return;
        }
        if (!SecondHandInventoryRules.isAllowedLowStockThreshold(lowStockThreshold)) {
            throw fieldError(
                    "low_stock_threshold",
                    "must be 0 or 1 for second-hand listings"
            );
        }
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}