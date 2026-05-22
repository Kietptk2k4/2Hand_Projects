package com.twohands.commerce_service.application.product.removeproductbyadmin;

import com.twohands.commerce_service.application.product.common.ProductRemovedOutboxService;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductForModeration;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.RemoveProductByAdminRepository;
import com.twohands.commerce_service.domain.product.RemoveProductByAdminResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class RemoveProductByAdminUseCase {

    private final RemoveProductByAdminRepository removeProductByAdminRepository;
    private final CartItemRepository cartItemRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductRemovedOutboxService productRemovedOutboxService;
    private final Clock clock;

    public RemoveProductByAdminUseCase(
            RemoveProductByAdminRepository removeProductByAdminRepository,
            CartItemRepository cartItemRepository,
            OutboxEventRepository outboxEventRepository,
            ProductRemovedOutboxService productRemovedOutboxService,
            Clock clock
    ) {
        this.removeProductByAdminRepository = removeProductByAdminRepository;
        this.cartItemRepository = cartItemRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productRemovedOutboxService = productRemovedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public RemoveProductByAdminResult execute(RemoveProductByAdminCommand command) {
        validateReason(command.reason());

        ProductForModeration product = removeProductByAdminRepository.findById(command.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Instant occurredAt = clock.instant();

        if (product.isRemoved()) {
            return buildResult(product, product.status(), true, 0, occurredAt);
        }

        boolean updated = removeProductByAdminRepository.updateStatusToRemoved(
                product.productId(),
                product.status(),
                occurredAt
        );
        if (!updated) {
            ProductForModeration latest = removeProductByAdminRepository.findById(command.productId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            if (latest.isRemoved()) {
                return buildResult(latest, latest.status(), true, 0, occurredAt);
            }
            throw new AppException(ErrorCode.INVALID_PRODUCT_STATUS, "Product status changed concurrently");
        }

        int cartItemsInvalidated = cartItemRepository.markInvalidByProductId(product.productId(), occurredAt);

        outboxEventRepository.save(productRemovedOutboxService.build(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                command.adminId(),
                product.status(),
                command.reason(),
                occurredAt
        ));

        return buildResult(product, ProductStatus.REMOVED, false, cartItemsInvalidated, occurredAt);
    }

    public String successMessage(boolean alreadyRemoved) {
        return alreadyRemoved
                ? "San pham da duoc remove truoc do."
                : "Remove san pham thanh cong.";
    }

    private RemoveProductByAdminResult buildResult(
            ProductForModeration product,
            ProductStatus currentStatus,
            boolean alreadyRemoved,
            int cartItemsInvalidated,
            Instant removedAt
    ) {
        return new RemoveProductByAdminResult(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                product.title(),
                currentStatus,
                product.status(),
                alreadyRemoved,
                cartItemsInvalidated,
                removedAt
        );
    }

    private void validateReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "reason is required", "reason", "must not be blank");
        }
    }
}
