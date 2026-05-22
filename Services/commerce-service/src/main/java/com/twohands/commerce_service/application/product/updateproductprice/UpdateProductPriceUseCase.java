package com.twohands.commerce_service.application.product.updateproductprice;

import com.twohands.commerce_service.application.product.common.ProductPriceUpdatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.OverlappingProductPrice;
import com.twohands.commerce_service.domain.product.ProductPriceRecord;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductPriceDraft;
import com.twohands.commerce_service.domain.product.UpdateProductPriceProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductPriceRepository;
import com.twohands.commerce_service.domain.product.UpdateProductPriceResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UpdateProductPriceUseCase {

    private final UpdateProductPriceRepository updateProductPriceRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductPriceUpdatedOutboxService productPriceUpdatedOutboxService;
    private final Clock clock;

    public UpdateProductPriceUseCase(
            UpdateProductPriceRepository updateProductPriceRepository,
            OutboxEventRepository outboxEventRepository,
            ProductPriceUpdatedOutboxService productPriceUpdatedOutboxService,
            Clock clock
    ) {
        this.updateProductPriceRepository = updateProductPriceRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productPriceUpdatedOutboxService = productPriceUpdatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public UpdateProductPriceResult execute(UpdateProductPriceCommand command) {
        validatePricePayload(command);

        UpdateProductPriceProductRef product = updateProductPriceRepository
                .findProductByIdAndSellerId(command.productId(), command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.status() == ProductStatus.REMOVED) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product price cannot be updated");
        }

        boolean previousClosed = closeOverlappingPrices(command.productId(), command.startAt(), command.endAt());

        Instant now = clock.instant();
        ProductPriceRecord created = updateProductPriceRepository.insertPrice(
                new UpdateProductPriceDraft(
                        command.productId(),
                        command.price(),
                        command.salePrice(),
                        command.startAt(),
                        command.endAt()
                ),
                now
        );

        outboxEventRepository.save(productPriceUpdatedOutboxService.build(
                product.productId(),
                product.shopId(),
                product.sellerId(),
                product.status(),
                created.priceId(),
                created.price(),
                created.salePrice(),
                created.startAt(),
                created.endAt(),
                now
        ));

        return new UpdateProductPriceResult(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                product.status(),
                created,
                previousClosed
        );
    }

    public String successMessage() {
        return "Cap nhat gia san pham thanh cong.";
    }

    private boolean closeOverlappingPrices(UUID productId, Instant startAt, Instant endAt) {
        List<OverlappingProductPrice> overlapping = updateProductPriceRepository.findOverlappingPrices(
                productId,
                startAt,
                endAt
        );

        List<UUID> closableIds = new ArrayList<>();
        for (OverlappingProductPrice existing : overlapping) {
            if (existing.startAt().isAfter(startAt)) {
                throw new AppException(
                        ErrorCode.PRICE_WINDOW_OVERLAP,
                        "Price window overlaps an existing scheduled price"
                );
            }
            closableIds.add(existing.priceId());
        }

        if (closableIds.isEmpty()) {
            return false;
        }

        int closed = updateProductPriceRepository.closePricesAtStart(productId, closableIds, startAt);
        if (closed != closableIds.size()) {
            throw new AppException(ErrorCode.PRICE_WINDOW_OVERLAP, "Failed to close overlapping active price");
        }

        List<OverlappingProductPrice> remaining = updateProductPriceRepository.findOverlappingPrices(
                productId,
                startAt,
                endAt
        );
        if (!remaining.isEmpty()) {
            throw new AppException(ErrorCode.PRICE_WINDOW_OVERLAP, "Price window overlaps an existing price");
        }

        return true;
    }

    private void validatePricePayload(UpdateProductPriceCommand command) {
        if (command.price() == null || command.price().compareTo(BigDecimal.ZERO) < 0) {
            throw fieldError("price", "must be greater than or equal to 0");
        }
        if (command.startAt() == null) {
            throw fieldError("start_at", "must not be null");
        }
        if (command.endAt() != null && !command.endAt().isAfter(command.startAt())) {
            throw fieldError("end_at", "must be after start_at");
        }
        if (command.salePrice() != null) {
            if (command.salePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw fieldError("sale_price", "must be greater than or equal to 0");
            }
            if (command.salePrice().compareTo(command.price()) > 0) {
                throw fieldError("sale_price", "must be less than or equal to price");
            }
        }
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
