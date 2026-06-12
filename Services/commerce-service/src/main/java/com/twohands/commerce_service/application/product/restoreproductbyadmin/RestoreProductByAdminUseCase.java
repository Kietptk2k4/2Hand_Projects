package com.twohands.commerce_service.application.product.restoreproductbyadmin;

import com.twohands.commerce_service.application.product.common.ProductRestoredOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductForRestore;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.RestoreProductByAdminRepository;
import com.twohands.commerce_service.domain.product.RestoreProductByAdminResult;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class RestoreProductByAdminUseCase {

    private final RestoreProductByAdminRepository restoreProductByAdminRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductRestoredOutboxService productRestoredOutboxService;
    private final Clock clock;

    public RestoreProductByAdminUseCase(
            RestoreProductByAdminRepository restoreProductByAdminRepository,
            OutboxEventRepository outboxEventRepository,
            ProductRestoredOutboxService productRestoredOutboxService,
            Clock clock
    ) {
        this.restoreProductByAdminRepository = restoreProductByAdminRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productRestoredOutboxService = productRestoredOutboxService;
        this.clock = clock;
    }

    @Transactional
    public RestoreProductByAdminResult execute(RestoreProductByAdminCommand command) {
        validateReason(command.reason());

        ProductForRestore product = restoreProductByAdminRepository.findById(command.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Instant occurredAt = clock.instant();

        if (!product.isRemoved()) {
            return buildResult(product, product.status(), true, occurredAt);
        }

        if (product.shopStatus() != ShopStatus.ACTIVE) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Product cannot be restored while shop status is " + product.shopStatus()
            );
        }

        ProductStatus targetStatus = resolveTargetStatus(product.stockQuantity());
        boolean updated = restoreProductByAdminRepository.updateStatusFromRemoved(
                product.productId(),
                targetStatus,
                occurredAt
        );
        if (!updated) {
            ProductForRestore latest = restoreProductByAdminRepository.findById(command.productId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            if (!latest.isRemoved()) {
                return buildResult(latest, latest.status(), true, occurredAt);
            }
            throw new AppException(ErrorCode.INVALID_PRODUCT_STATUS, "Product status changed concurrently");
        }

        outboxEventRepository.save(productRestoredOutboxService.build(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                command.adminId(),
                ProductStatus.REMOVED,
                targetStatus,
                command.reason(),
                occurredAt
        ));

        return buildResult(product, targetStatus, false, occurredAt);
    }

    public String successMessage(boolean alreadyRestored) {
        return alreadyRestored
                ? "San pham da duoc restore truoc do."
                : "Restore san pham thanh cong.";
    }

    private ProductStatus resolveTargetStatus(long stockQuantity) {
        return stockQuantity > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK;
    }

    private RestoreProductByAdminResult buildResult(
            ProductForRestore product,
            ProductStatus currentStatus,
            boolean alreadyRestored,
            Instant restoredAt
    ) {
        return new RestoreProductByAdminResult(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                product.title(),
                currentStatus,
                product.status(),
                alreadyRestored,
                restoredAt
        );
    }

    private void validateReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "reason is required", "reason", "must not be blank");
        }
    }
}
