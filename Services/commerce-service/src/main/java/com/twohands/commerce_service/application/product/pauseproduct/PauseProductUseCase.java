package com.twohands.commerce_service.application.product.pauseproduct;

import com.twohands.commerce_service.application.product.common.ProductPausedOutboxService;
import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.Product;
import com.twohands.commerce_service.domain.product.ProductRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PauseProductUseCase {

    private final ProductRepository productRepository;
    private final SyncCartItemStatusUseCase syncCartItemStatusUseCase;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductPausedOutboxService productPausedOutboxService;

    public PauseProductUseCase(
            ProductRepository productRepository,
            SyncCartItemStatusUseCase syncCartItemStatusUseCase,
            OutboxEventRepository outboxEventRepository,
            ProductPausedOutboxService productPausedOutboxService
    ) {
        this.productRepository = productRepository;
        this.syncCartItemStatusUseCase = syncCartItemStatusUseCase;
        this.outboxEventRepository = outboxEventRepository;
        this.productPausedOutboxService = productPausedOutboxService;
    }

    @Transactional
    public PauseProductResult execute(PauseProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .filter(found -> found.isOwnedBy(command.sellerId()))
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.isRemoved()) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product cannot be paused");
        }

        if (product.isArchived()) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Archived product cannot be paused"
            );
        }

        if (product.isPaused()) {
            return new PauseProductResult(
                    product.id(),
                    product.shopId(),
                    ProductStatus.PAUSED,
                    product.updatedAt(),
                    0,
                    true
            );
        }

        if (!product.canPause()) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Product status does not allow pause: " + product.status()
            );
        }

        Instant now = Instant.now();
        ProductStatus previousStatus = product.status();
        Product paused = productRepository.save(product.withStatus(ProductStatus.PAUSED, now));
        SyncCartItemStatusResult cartSync = syncCartItemStatusUseCase.syncByProductId(product.id());
        int cartItemsInvalidated = cartSync.updated();

        outboxEventRepository.save(productPausedOutboxService.build(
                paused.id(),
                paused.shopId(),
                paused.sellerId(),
                previousStatus,
                now
        ));

        return new PauseProductResult(
                paused.id(),
                paused.shopId(),
                paused.status(),
                paused.updatedAt(),
                cartItemsInvalidated,
                false
        );
    }

    public String successMessage(boolean alreadyPaused) {
        return alreadyPaused
                ? "San pham da duoc pause truoc do."
                : "Pause san pham thanh cong.";
    }
}
