package com.twohands.commerce_service.application.product.createproduct;

import com.twohands.commerce_service.application.product.common.ProductCatalogValidationService;
import com.twohands.commerce_service.application.product.common.ProductCreatedOutboxService;
import com.twohands.commerce_service.domain.catalog.ProductCategoryRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.CreateProductDraft;
import com.twohands.commerce_service.domain.product.CreateProductRepository;
import com.twohands.commerce_service.domain.product.CreateProductResult;
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
public class CreateProductUseCase {

    private static final int TITLE_MAX_LENGTH = 500;

    private final SellerShopRepository sellerShopRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCatalogValidationService productCatalogValidationService;
    private final CreateProductRepository createProductRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductCreatedOutboxService productCreatedOutboxService;
    private final Clock clock;

    public CreateProductUseCase(
            SellerShopRepository sellerShopRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductCatalogValidationService productCatalogValidationService,
            CreateProductRepository createProductRepository,
            OutboxEventRepository outboxEventRepository,
            ProductCreatedOutboxService productCreatedOutboxService,
            Clock clock
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productCatalogValidationService = productCatalogValidationService;
        this.createProductRepository = createProductRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productCreatedOutboxService = productCreatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public CreateProductResult execute(CreateProductCommand command) {
        validatePayload(command);

        SellerShop shop = sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        if (!shop.canCreateProduct()) {
            throw new AppException(
                    ErrorCode.SHOP_NOT_OPERATING,
                    "Shop must be ACTIVE to create products. Current status: " + shop.status()
            );
        }

        if (!productCategoryRepository.existsActiveById(command.categoryId())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        productCatalogValidationService.validateActiveBrandId(command.brandId());

        String productType = productCatalogValidationService.normalizeProductType(command.productType());
        String condition = productCatalogValidationService.normalizeCondition(command.condition());

        Instant now = clock.instant();
        CreateProductResult created = createProductRepository.create(
                new CreateProductDraft(
                        command.sellerId(),
                        shop.id(),
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

        outboxEventRepository.save(productCreatedOutboxService.build(
                created.productId(),
                created.shopId(),
                created.sellerId(),
                created.status(),
                now
        ));

        return created;
    }

    public String successMessage() {
        return "Tao san pham draft thanh cong.";
    }

    private void validatePayload(CreateProductCommand command) {
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
