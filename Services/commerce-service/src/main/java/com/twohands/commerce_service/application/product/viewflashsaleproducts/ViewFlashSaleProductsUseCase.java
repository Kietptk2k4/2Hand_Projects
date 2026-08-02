package com.twohands.commerce_service.application.product.viewflashsaleproducts;

import com.twohands.commerce_service.domain.catalog.FlashSaleSlot;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class ViewFlashSaleProductsUseCase {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final ProductDiscoveryRepository productDiscoveryRepository;
    private final Clock clock;

    public ViewFlashSaleProductsUseCase(ProductDiscoveryRepository productDiscoveryRepository, Clock clock) {
        this.productDiscoveryRepository = productDiscoveryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ViewFlashSaleProductsResult execute(ViewFlashSaleProductsCommand command) {
        int limit = resolveLimit(command.limit());
        Instant now = clock.instant();
        FlashSaleSlot slot = FlashSaleSlot.current(now);

        List<ProductCardSummary> items = productDiscoveryRepository.findFlashSaleProducts(
                now,
                slot.start(),
                slot.end(),
                limit
        );

        return new ViewFlashSaleProductsResult(items, slot.start(), slot.end());
    }

    public String successMessage() {
        return "Lay danh sach flash sale thanh cong.";
    }

    private int resolveLimit(Integer limit) {
        int resolved = limit == null ? DEFAULT_LIMIT : limit;
        if (resolved < 1 || resolved > MAX_LIMIT) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "limit must be between 1 and " + MAX_LIMIT,
                    "limit",
                    "must be between 1 and " + MAX_LIMIT
            );
        }
        return resolved;
    }
}
