package com.twohands.social_service.application.post.common;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class ProductTagValidator {

    public static final int MAX_PRODUCT_TAGS = 10;

    public void validate(List<ProductTagValidationItem> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        if (tags.size() > MAX_PRODUCT_TAGS) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "productTags", "Khong duoc tag qua " + MAX_PRODUCT_TAGS + " san pham.");
        }
        Set<String> seenProductIds = new HashSet<>();
        for (ProductTagValidationItem tag : tags) {
            validateProductId(tag.productId());
            if (!seenProductIds.add(tag.productId())) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "productTags", "Khong duoc tag trung mot san pham nhieu lan.");
            }
            if (tag.price() != null && tag.price().signum() < 0) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "productTags[].price", "Gia san pham phai >= 0.");
            }
        }
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "productTags[].product_id", "product_id khong duoc de trong.");
        }
        try {
            UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "productTags[].product_id", "product_id phai la dinh dang UUID hop le.");
        }
    }
}
