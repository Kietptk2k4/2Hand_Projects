package com.twohands.commerce_service.application.product.publishproduct;

import com.twohands.commerce_service.application.product.common.ProductCatalogValidationService;
import com.twohands.commerce_service.application.product.common.ProductPublishedOutboxService;
import com.twohands.commerce_service.common.media.CommerceProductMediaUrlValidator;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductPublishSnapshot;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.PublishProductRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class PublishProductUseCase {

    private final PublishProductRepository publishProductRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductPublishedOutboxService productPublishedOutboxService;
    private final ProductCatalogValidationService productCatalogValidationService;
    private final CommerceProductMediaUrlValidator productMediaUrlValidator;
    private final Clock clock;

    public PublishProductUseCase(
            PublishProductRepository publishProductRepository,
            OutboxEventRepository outboxEventRepository,
            ProductPublishedOutboxService productPublishedOutboxService,
            ProductCatalogValidationService productCatalogValidationService,
            CommerceProductMediaUrlValidator productMediaUrlValidator,
            Clock clock
    ) {
        this.publishProductRepository = publishProductRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productPublishedOutboxService = productPublishedOutboxService;
        this.productCatalogValidationService = productCatalogValidationService;
        this.productMediaUrlValidator = productMediaUrlValidator;
        this.clock = clock;
    }

    @Transactional
    public PublishProductResult execute(PublishProductCommand command) {
        Instant now = clock.instant();
        ProductPublishSnapshot snapshot = publishProductRepository.findForSeller(
                        command.productId(),
                        command.sellerId(),
                        now
                )
                .filter(found -> found.isOwnedBy(command.sellerId()))
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (snapshot.isRemoved()) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product cannot be published");
        }

        if (snapshot.isArchived()) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Archived product cannot be published"
            );
        }

        if (!snapshot.canPublishFromDraftOrPaused() && !snapshot.isAlreadyPublished()) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Product status does not allow publish: " + snapshot.status()
            );
        }

        if (snapshot.canPublishFromDraftOrPaused()) {
            validatePublishReadiness(snapshot);
        }

        ProductStatus targetStatus = snapshot.resolveTargetStatus();

        if (snapshot.isAlreadyPublished()) {
            if (snapshot.status() == targetStatus) {
                return new PublishProductResult(
                        snapshot.productId(),
                        snapshot.shopId(),
                        snapshot.status(),
                        snapshot.updatedAt(),
                        true
                );
            }
            return publishTransition(snapshot, targetStatus, now);
        }

        return publishTransition(snapshot, targetStatus, now);
    }

    private PublishProductResult publishTransition(
            ProductPublishSnapshot snapshot,
            ProductStatus targetStatus,
            Instant now
    ) {
        ProductStatus previousStatus = snapshot.status();
        ProductPublishSnapshot published = publishProductRepository.updateStatus(
                snapshot.productId(),
                targetStatus,
                now
        );

        int stockQuantity = published.stockQuantity() != null ? published.stockQuantity() : 0;
        outboxEventRepository.save(productPublishedOutboxService.build(
                published.productId(),
                published.shopId(),
                published.sellerId(),
                previousStatus,
                targetStatus,
                stockQuantity,
                now
        ));

        return new PublishProductResult(
                published.productId(),
                published.shopId(),
                targetStatus,
                now,
                false
        );
    }

    private void validatePublishReadiness(ProductPublishSnapshot snapshot) {
        if (!ShopStatus.ACTIVE.name().equals(snapshot.shopStatus())) {
            throw new AppException(
                    ErrorCode.SHOP_NOT_OPERATING,
                    "Shop must be ACTIVE to publish products. Current status: " + snapshot.shopStatus()
            );
        }

        if (!snapshot.categoryActive()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Product category is not active");
        }

        if (!hasText(snapshot.title())
                || !hasText(snapshot.description())
                || !hasText(snapshot.condition())) {
            throw new AppException(
                    ErrorCode.BAD_REQUEST,
                    "Required product fields are missing: title, description, condition"
            );
        }

        productCatalogValidationService.normalizeCondition(snapshot.condition());

        if (snapshot.weightGram() <= 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Product weight must be greater than zero");
        }

        if (snapshot.activePrice() == null) {
            throw new AppException(ErrorCode.ACTIVE_PRICE_MISSING);
        }

        if (snapshot.stockQuantity() == null) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Inventory record is required before publish"
            );
        }

        productCatalogValidationService.validateSecondHandStockQuantity(snapshot.stockQuantity());

        productMediaUrlValidator.validateRequiredProductMedia(snapshot.primaryMediaUrl());
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    public String successMessage(boolean alreadyPublished) {
        return alreadyPublished
                ? "San pham da duoc publish truoc do."
                : "Publish san pham thanh cong.";
    }
}
