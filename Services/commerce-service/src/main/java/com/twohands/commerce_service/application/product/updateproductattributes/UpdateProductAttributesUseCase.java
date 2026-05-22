package com.twohands.commerce_service.application.product.updateproductattributes;

import com.twohands.commerce_service.application.product.common.ProductAttributesUpdatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductAttributeItem;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesRepository;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UpdateProductAttributesUseCase {

    private static final int ATTRIBUTE_NAME_MAX_LENGTH = 255;
    private static final int ATTRIBUTE_VALUE_MAX_LENGTH = 500;

    private final UpdateProductAttributesRepository updateProductAttributesRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductAttributesUpdatedOutboxService productAttributesUpdatedOutboxService;
    private final Clock clock;

    public UpdateProductAttributesUseCase(
            UpdateProductAttributesRepository updateProductAttributesRepository,
            OutboxEventRepository outboxEventRepository,
            ProductAttributesUpdatedOutboxService productAttributesUpdatedOutboxService,
            Clock clock
    ) {
        this.updateProductAttributesRepository = updateProductAttributesRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productAttributesUpdatedOutboxService = productAttributesUpdatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public UpdateProductAttributesResult execute(UpdateProductAttributesCommand command) {
        List<ProductAttributeItem> normalized = validateAndNormalize(command.attributes());

        UpdateProductAttributesProductRef product = updateProductAttributesRepository
                .findProductByIdAndSellerId(command.productId(), command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.status() == ProductStatus.REMOVED) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product attributes cannot be updated");
        }

        List<ProductAttributeItem> saved = updateProductAttributesRepository.replaceAttributes(
                product.productId(),
                normalized
        );

        Instant now = clock.instant();
        outboxEventRepository.save(productAttributesUpdatedOutboxService.build(
                product.productId(),
                product.shopId(),
                product.sellerId(),
                product.status(),
                saved.size(),
                now
        ));

        return new UpdateProductAttributesResult(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                product.status(),
                saved
        );
    }

    public String successMessage() {
        return "Cap nhat thuoc tinh san pham thanh cong.";
    }

    private List<ProductAttributeItem> validateAndNormalize(List<ProductAttributeItem> attributes) {
        if (attributes == null) {
            throw fieldError("attributes", "must not be null");
        }

        Set<String> seenNames = new HashSet<>();
        List<ProductAttributeItem> normalized = new ArrayList<>(attributes.size());

        for (int index = 0; index < attributes.size(); index++) {
            ProductAttributeItem item = attributes.get(index);
            String fieldPrefix = "attributes[" + index + "]";

            if (item == null) {
                throw fieldError(fieldPrefix, "must not be null");
            }
            if (!StringUtils.hasText(item.attributeName())) {
                throw fieldError(fieldPrefix + ".attribute_name", "must not be blank");
            }
            if (!StringUtils.hasText(item.attributeValue())) {
                throw fieldError(fieldPrefix + ".attribute_value", "must not be blank");
            }

            String name = item.attributeName().trim();
            String value = item.attributeValue().trim();

            if (name.length() > ATTRIBUTE_NAME_MAX_LENGTH) {
                throw fieldError(
                        fieldPrefix + ".attribute_name",
                        "must be at most " + ATTRIBUTE_NAME_MAX_LENGTH + " characters"
                );
            }
            if (value.length() > ATTRIBUTE_VALUE_MAX_LENGTH) {
                throw fieldError(
                        fieldPrefix + ".attribute_value",
                        "must be at most " + ATTRIBUTE_VALUE_MAX_LENGTH + " characters"
                );
            }

            String normalizedNameKey = name.toLowerCase(Locale.ROOT);
            if (!seenNames.add(normalizedNameKey)) {
                throw fieldError("attributes", "duplicate attribute_name: " + name);
            }

            normalized.add(new ProductAttributeItem(name, value));
        }

        return normalized;
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
