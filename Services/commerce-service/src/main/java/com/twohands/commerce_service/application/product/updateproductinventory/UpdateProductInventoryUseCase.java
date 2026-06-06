package com.twohands.commerce_service.application.product.updateproductinventory;

import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.application.product.common.ProductCatalogValidationService;
import com.twohands.commerce_service.application.product.common.ProductInventoryUpdatedOutboxService;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductInventoryState;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryRepository;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryResult;
import com.twohands.commerce_service.domain.product.UpdateProductInventorySnapshot;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryStatusResolver;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateProductInventoryUseCase {

    private final UpdateProductInventoryRepository updateProductInventoryRepository;
    private final SyncCartItemStatusUseCase syncCartItemStatusUseCase;
    private final ProductCatalogValidationService productCatalogValidationService;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductInventoryUpdatedOutboxService productInventoryUpdatedOutboxService;
    private final Clock clock;

    public UpdateProductInventoryUseCase(
            UpdateProductInventoryRepository updateProductInventoryRepository,
            SyncCartItemStatusUseCase syncCartItemStatusUseCase,
            ProductCatalogValidationService productCatalogValidationService,
            OutboxEventRepository outboxEventRepository,
            ProductInventoryUpdatedOutboxService productInventoryUpdatedOutboxService,
            Clock clock
    ) {
        this.updateProductInventoryRepository = updateProductInventoryRepository;
        this.syncCartItemStatusUseCase = syncCartItemStatusUseCase;
        this.productCatalogValidationService = productCatalogValidationService;
        this.outboxEventRepository = outboxEventRepository;
        this.productInventoryUpdatedOutboxService = productInventoryUpdatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public UpdateProductInventoryResult execute(UpdateProductInventoryCommand command) {
        validateQuantities(command);

        Instant now = clock.instant();
        UpdateProductInventorySnapshot product = updateProductInventoryRepository
                .findProductForInventoryUpdate(command.productId(), command.sellerId(), now)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.status() == ProductStatus.REMOVED) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product inventory cannot be updated");
        }

        if (command.stockQuantity() < product.reservedQuantity()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "stock_quantity",
                    "must be greater than or equal to reserved quantity (" + product.reservedQuantity() + ")"
            );
        }

        int lowStockThreshold = resolveLowStockThreshold(command.lowStockThreshold(), product);
        ProductStatus previousStatus = product.status();

        ProductInventoryState savedInventory = updateProductInventoryRepository.upsertInventory(
                product.productId(),
                command.stockQuantity(),
                lowStockThreshold,
                now
        );

        ProductStatus currentStatus = previousStatus;
        Optional<ProductStatus> targetStatus = UpdateProductInventoryStatusResolver.resolveTargetStatus(
                previousStatus,
                savedInventory.stockQuantity(),
                product.canRestoreActiveFromOutOfStock()
        );

        boolean statusChanged = false;
        if (targetStatus.isPresent() && targetStatus.get() != previousStatus) {
            updateProductInventoryRepository.updateProductStatus(product.productId(), targetStatus.get(), now);
            currentStatus = targetStatus.get();
            statusChanged = true;
        }

        SyncCartItemStatusResult cartSync = syncCartItemStatusUseCase.syncByProductId(product.productId());

        outboxEventRepository.save(productInventoryUpdatedOutboxService.build(
                product.productId(),
                product.shopId(),
                product.sellerId(),
                currentStatus,
                previousStatus,
                savedInventory.stockQuantity(),
                savedInventory.lowStockThreshold(),
                savedInventory.reservedQuantity(),
                now
        ));

        return new UpdateProductInventoryResult(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                currentStatus,
                previousStatus,
                statusChanged,
                savedInventory.stockQuantity(),
                savedInventory.lowStockThreshold(),
                savedInventory.reservedQuantity(),
                cartSync.updated(),
                now
        );
    }

    public String successMessage() {
        return "Cap nhat ton kho san pham thanh cong.";
    }

    private void validateQuantities(UpdateProductInventoryCommand command) {
        productCatalogValidationService.validateSecondHandStockQuantity(command.stockQuantity());
        productCatalogValidationService.validateSecondHandLowStockThreshold(command.lowStockThreshold());
    }

    private int resolveLowStockThreshold(Integer requested, UpdateProductInventorySnapshot product) {
        if (requested != null) {
            return requested;
        }
        return product.currentLowStockThreshold();
    }
}
