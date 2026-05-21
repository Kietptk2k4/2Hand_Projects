package com.twohands.commerce_service.application.product.archiveproduct;

import com.twohands.commerce_service.application.product.common.ProductArchivedOutboxService;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.Product;
import com.twohands.commerce_service.domain.product.ProductRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ArchiveProductUseCase {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductArchivedOutboxService productArchivedOutboxService;

    public ArchiveProductUseCase(
            ProductRepository productRepository,
            CartItemRepository cartItemRepository,
            OutboxEventRepository outboxEventRepository,
            ProductArchivedOutboxService productArchivedOutboxService
    ) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productArchivedOutboxService = productArchivedOutboxService;
    }

    @Transactional
    public ArchiveProductResult execute(ArchiveProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .filter(found -> found.isOwnedBy(command.sellerId()))
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.isRemoved()) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product cannot be archived");
        }

        if (product.isArchived()) {
            return new ArchiveProductResult(
                    product.id(),
                    product.shopId(),
                    ProductStatus.ARCHIVED,
                    product.updatedAt(),
                    0,
                    true
            );
        }

        if (!product.canArchive()) {
            throw new AppException(
                    ErrorCode.INVALID_PRODUCT_STATUS,
                    "Product status does not allow archive: " + product.status()
            );
        }

        Instant now = Instant.now();
        ProductStatus previousStatus = product.status();
        Product archived = productRepository.save(product.withStatus(ProductStatus.ARCHIVED, now));
        int cartItemsInvalidated = cartItemRepository.markInvalidByProductId(product.id(), now);

        outboxEventRepository.save(productArchivedOutboxService.build(
                archived.id(),
                archived.shopId(),
                archived.sellerId(),
                previousStatus,
                now
        ));

        return new ArchiveProductResult(
                archived.id(),
                archived.shopId(),
                archived.status(),
                archived.updatedAt(),
                cartItemsInvalidated,
                false
        );
    }

    public String successMessage(boolean alreadyArchived) {
        return alreadyArchived
                ? "San pham da duoc archive truoc do."
                : "Archive san pham thanh cong.";
    }
}
