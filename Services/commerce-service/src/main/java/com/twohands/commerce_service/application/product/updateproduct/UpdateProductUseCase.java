package com.twohands.commerce_service.application.product.updateproduct;

import com.twohands.commerce_service.application.product.common.ProductCatalogValidationService;
import com.twohands.commerce_service.application.product.common.ProductUpdatedOutboxService;
import com.twohands.commerce_service.domain.catalog.ProductCategoryRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductDraft;
import com.twohands.commerce_service.domain.product.UpdateProductRepository;
import com.twohands.commerce_service.domain.product.UpdateProductResult;
import com.twohands.commerce_service.domain.product.UpdateProductSnapshot;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateProductUseCase {

    private static final int TITLE_MAX_LENGTH = 500;

    private final SellerShopRepository sellerShopRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCatalogValidationService productCatalogValidationService;
    private final UpdateProductRepository updateProductRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductUpdatedOutboxService productUpdatedOutboxService;
    private final Clock clock;

    public UpdateProductUseCase(
            SellerShopRepository sellerShopRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductCatalogValidationService productCatalogValidationService,
            UpdateProductRepository updateProductRepository,
            OutboxEventRepository outboxEventRepository,
            ProductUpdatedOutboxService productUpdatedOutboxService,
            Clock clock
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productCatalogValidationService = productCatalogValidationService;
        this.updateProductRepository = updateProductRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productUpdatedOutboxService = productUpdatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public UpdateProductResult execute(UpdateProductCommand command) {
        validatePayload(command);

        SellerShop shop = sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        if (!shop.canCreateProduct()) {
            throw new AppException(
                    ErrorCode.SHOP_NOT_OPERATING,
                    "Shop must be ACTIVE to update products. Current status: " + shop.status()
            );
        }

        UpdateProductSnapshot existing = updateProductRepository
                .findByIdAndSellerId(command.productId(), command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        validateEditableStatus(existing.status());

        if (!productCategoryRepository.existsActiveById(command.categoryId())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        productCatalogValidationService.validateActiveBrandId(command.brandId());

        String productType = productCatalogValidationService.normalizeProductType(command.productType());
        String condition = productCatalogValidationService.normalizeCondition(command.condition());

        Instant now = clock.instant();
        UpdateProductResult updated = updateProductRepository.update(
                new UpdateProductDraft(
                        existing.productId(),
                        existing.sellerId(),
                        existing.shopId(),
                        productType,
                        command.categoryId(),
                        command.brandId(),
                        condition,
                        command.title().trim(),
                        command.description().trim(),
                        command.weightGram()
                ),
                now
        );

        outboxEventRepository.save(productUpdatedOutboxService.build(
                updated.productId(),
                updated.shopId(),
                updated.sellerId(),
                updated.status(),
                now
        ));

        return updated;
    }

    public String successMessage() {
        return "Cap nhat san pham thanh cong.";
    }

    private void validateEditableStatus(ProductStatus status) {
        if (status == ProductStatus.REMOVED) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product cannot be updated");
        }
        if (status == ProductStatus.ARCHIVED) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Archived product cannot be updated"
            );
        }
    }

    private void validatePayload(UpdateProductCommand command) {
        if (!StringUtils.hasText(command.productType())) {
            throw fieldError("product_type", "must not be blank");
        }
        if (command.categoryId() == null) {
            throw fieldError("category_id", "must not be null");
        }
        if (!StringUtils.hasText(command.condition())) {
            throw fieldError("condition", "must not be blank");
        }
        if (!StringUtils.hasText(command.title())) {
            throw fieldError("title", "must not be blank");
        }
        if (command.title().trim().length() > TITLE_MAX_LENGTH) {
            throw fieldError("title", "must be at most " + TITLE_MAX_LENGTH + " characters");
        }
        if (!StringUtils.hasText(command.description())) {
            throw fieldError("description", "must not be blank");
        }
        if (command.weightGram() == null || command.weightGram() <= 0) {
            throw fieldError("weight_gram", "must be greater than 0");
        }
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
